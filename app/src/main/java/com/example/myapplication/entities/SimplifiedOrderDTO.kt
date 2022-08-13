package com.example.myapplication.entities

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class SimplifiedOrderDTO(var ordemID: Int, var local: String, var equipamento: String, var dataFim: Long){


}
