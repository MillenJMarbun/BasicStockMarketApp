package com.plcoding.stockmarketapp.di

import com.plcoding.stockmarketapp.datas.csv.CSVParser
import com.plcoding.stockmarketapp.datas.csv.CompanyListingsParser
import com.plcoding.stockmarketapp.datas.csv.IntradayInfoParser
import com.plcoding.stockmarketapp.datas.repository.StockRepositoryimpl
import com.plcoding.stockmarketapp.domain.model.CompanyListing
import com.plcoding.stockmarketapp.domain.model.IntradayInfo
import com.plcoding.stockmarketapp.domain.repository.StockRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCompanyListingsParser(
        companyListingsParser: CompanyListingsParser
    ): CSVParser<CompanyListing>

    @Binds
    @Singleton
    abstract fun bindIntradayInfoParser(
        intradayInfoParser: IntradayInfoParser
    ): CSVParser<IntradayInfo>

    @Binds
    @Singleton
    abstract fun bindStockRepository(
        stockRepositoryimpl: StockRepositoryimpl
    ): StockRepository

}