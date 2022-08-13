package com.example.myapplication.entities

import kotlinx.serialization.Serializable

@Serializable
data class OrderDTO(val ordemID: String,
                    val dataFim: String,
                    val equipamento: String,
                    val fotosAntes: ArrayList<String>,
                    val fotosDepois: ArrayList<String>,
                    val funcionarioID: String,
                    val gas_KG: String,
                    val instalacao: String,
                    val local:String,
                    val obs:String,
                    val pat:String,
                    val tipoServicos: ArrayList<String>,
                    val tipoManut: ArrayList<String>,
                    val tipoTroca: ArrayList<String>)
{


}
