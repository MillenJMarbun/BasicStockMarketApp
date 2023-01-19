package com.plcoding.stockmarketapp.datas.csv

import java.io.InputStream

interface CSVParser <T>  {

    suspend fun parse(stream: InputStream):List<T>



}