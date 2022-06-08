package com.example.myapplication.entities

import android.content.Context
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import com.example.myapplication.services.DynamoAws

class ListEquip(val context: Context) {

    private val equips = context.getSharedPreferences(
        "Equipamentos",
        Context.MODE_PRIVATE
    ).getStringSet("listaEquipamentos", mutableSetOf("Erro"))

    var arrayAdapterPlant: ArrayAdapter<String> = ArrayAdapter(
        context,
        android.R.layout.simple_spinner_item
    )
    var arrayAdapterLocal: ArrayAdapter<String> = ArrayAdapter(
        context,
        android.R.layout.simple_spinner_item
    )
    var arrayAdapterEquip: ArrayAdapter<String> = ArrayAdapter(
        context,
        android.R.layout.simple_spinner_item
    )

    suspend fun organizeEquips(plants: ArrayList<String>) {
        context.getSharedPreferences("Equipamentos", AppCompatActivity.MODE_PRIVATE).edit().clear()
            .apply()

        plants.forEach { plant ->
            println(plant + " plant")
            val equips = getEquipsFromDB(plant)

            context.getSharedPreferences("Equipamentos", AppCompatActivity.MODE_PRIVATE).edit()
                .putStringSet("listaEquipamentos", equips)
                .apply()
            println(equips + " equips")
        }
        println(
            context.getSharedPreferences("Equipamentos", AppCompatActivity.MODE_PRIVATE)
                .getStringSet(
                    "listaEquipamentos",
                    mutableSetOf(" ")
                )
        )

    }

    suspend fun getEquipsFromDB(plant: String): MutableSet<String> {//adcionar objeto funcionario linkando as áreas dele getEquipsFromDB(empresa : String){
        println("entrou aq")
        val request = DynamoAws().getItem(
            "instalacoes2-dev",
            "empresa",
            keyVal = plant,
            "Equipamentos"
        )
        val requestreturn = request.toString().subSequence(10, request.toString().length - 2).split(", ")
            .toMutableSet() //Transform the result and returns "Plant_Local_Equipment"
        println(requestreturn)
        return requestreturn

    }

    fun plantCheck(plant: String) {
        if (plant == "Selecione uma Instalação" || plant == " ") {
            resetLocal()
        } else {
            val listLocals = ArrayList<String>(0)

            equips!!.forEach { equip ->
                if (plant == equip.split("_")[0]) {
                    listLocals.add(equip.split("_")[1])
                }
            }
            arrayAdapterLocal =
                ArrayAdapter(
                    context,
                    android.R.layout.simple_spinner_item,
                    listLocals.distinct().sorted()
                )
            resetEquip()
        }
    }

    fun localCheck(plant: String, local: String) {
        if (local == " ") {
            resetEquip()
        } else {
            val listEquips = ArrayList<String>(0)

            equips!!.forEach { equip ->
                if (plant == equip.split("_")[0] &&
                    local == equip.split("_")[1]
                ) {
                    listEquips.add("${equip.split("_")[2]} - ${equip.split("_")[3]}")
                }
            }

            arrayAdapterEquip =
                ArrayAdapter(
                    context,
                    android.R.layout.simple_spinner_item,
                    listEquips.distinct().sorted()
                )
        }
    }


    //Reseters
    fun resetPlant() {
        val listPlant: ArrayList<String> = ArrayList(0)
        listPlant.add(" Selecione uma Instalação")// to stay on top
        equips!!.forEach { equip ->
            listPlant.add(equip.split("_")[0])
        }
        arrayAdapterPlant =
            ArrayAdapter(
                context,
                android.R.layout.simple_spinner_item,
                listPlant.distinct().sorted()
            )
        resetLocal()
    }

    private fun resetLocal() {
        arrayAdapterLocal.clear()
        arrayAdapterLocal.add(" ")
        resetEquip()
    }

    private fun resetEquip() {
        arrayAdapterEquip.clear()
        arrayAdapterEquip.add(" ")
    }


}