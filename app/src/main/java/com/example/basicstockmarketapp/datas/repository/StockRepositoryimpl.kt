package com.plcoding.stockmarketapp.datas.repository

import androidx.compose.ui.platform.LocalTextInputService
import com.plcoding.stockmarketapp.datas.csv.CSVParser
import com.plcoding.stockmarketapp.datas.csv.CompanyListingsParser
import com.plcoding.stockmarketapp.datas.csv.IntradayInfoParser
import com.plcoding.stockmarketapp.datas.local.StockDatabase
import com.plcoding.stockmarketapp.datas.mapper.toCompanyInfo
import com.plcoding.stockmarketapp.datas.mapper.toCompanyListing
import com.plcoding.stockmarketapp.datas.mapper.toCompanyListingEntity
import com.plcoding.stockmarketapp.datas.remote.StockApi
import com.plcoding.stockmarketapp.domain.model.CompanyInfo
import com.plcoding.stockmarketapp.domain.model.CompanyListing
import com.plcoding.stockmarketapp.domain.model.IntradayInfo
import com.plcoding.stockmarketapp.domain.repository.StockRepository
import com.plcoding.stockmarketapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryimpl @Inject constructor (
    private val api: StockApi,
    private val db: StockDatabase,
    private val companyListingsParser: CSVParser<CompanyListing>,
    private val intradayInfoParser: CSVParser<IntradayInfo>,
        ): StockRepository {

    private val dao = db.dao

    override suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>> {
        return flow{
            emit(Resource.Loading(true))
            val localListings = dao.searchCompanyListing(query)
            emit(Resource.Success(
                data = localListings.map { it.toCompanyListing() }
            ))

            val isDbEmpty = localListings.isEmpty() && query.isBlank()
            val shouldJustLoadFromCache = !isDbEmpty && !fetchFromRemote

            if(shouldJustLoadFromCache){
                emit(Resource.Loading(false))
                return@flow
            }
            val remoteListings = try{
                val response = api.getListings()
                companyListingsParser.parse( response.byteStream())
            } catch (e: IOException){
                e.printStackTrace()
                emit(Resource.Error("Couldnt load data"))
                null
            } catch (e: HttpException){
                emit(Resource.Error("Couldnt load data"))
                null
            }
            remoteListings?.let { listings ->
                dao.clearCompanyListings()
                dao.insertCompanyListings(
                    listings.map{
                        it.toCompanyListingEntity()
                    }
                )
            emit(Resource.Success(
                data = dao
                    .searchCompanyListing("")
                    .map { it.toCompanyListing() }

            ))
                emit(Resource.Loading(isLoading = false))
            }


        }
    }

    override suspend fun getIntradayInfo(symbol: String): Resource<List<IntradayInfo>> {
        return try{
            val response = api.getIntradayInfo(symbol)
            val result = intradayInfoParser.parse(response.byteStream())
            Resource.Success(result)
        } catch(e: IOException){
            e.printStackTrace()
            Resource.Error(
                message = "Couldnt load intraday info"
            )
        } catch(e: HttpException){
            e.printStackTrace()
            Resource.Error(
                message = "Couldnt load intraday info"
            )
        }
    }

    override suspend fun getCompanyInfo(symbol: String): Resource<CompanyInfo> {
      return try {
           val result = api.getCompanyInfo(symbol)
          Resource.Success(result.toCompanyInfo())
       }catch(e: IOException){
           e.printStackTrace()
           Resource.Error(
               message = "Couldnt load Company info"
           )
       } catch(e: HttpException){
           e.printStackTrace()
           Resource.Error(
               message = "Couldnt load Company info"
           )
       }
    }
}