package com.example.myapplication.entities

import android.annotation.SuppressLint
import android.content.Context
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import com.example.myapplication.R
import com.example.myapplication.services.DynamoAws

class ListEquip(val context: Context) {

    private val equips = context.getSharedPreferences(
        "Equipamentos",
        Context.MODE_PRIVATE
    ).getStringSet("Equipamentos", mutableSetOf("Erro"))

    var arrayAdapterPlant: ArrayAdapter<String> = ArrayAdapter(
        context,
        R.layout.spinner_layout
    )
    var arrayAdapterLocal: ArrayAdapter<String> = ArrayAdapter(
        context,
        R.layout.spinner_layout
    )
    var arrayAdapterEquip: ArrayAdapter<String> = ArrayAdapter(
        context,
        R.layout.spinner_layout
    )

    suspend fun organizeEquips(plants: ArrayList<String>) {
        context.getSharedPreferences("Equipamentos", AppCompatActivity.MODE_PRIVATE).edit()
            .remove("Equipamentos").apply()
        plants.forEach { plant ->

            val equips = getEquipsFromDB(plant)
            val sharedPref =
                context.getSharedPreferences("Equipamentos", AppCompatActivity.MODE_PRIVATE)
            val newEquips = sharedPref.getStringSet("Equipamentos", mutableSetOf("Off limits"))
            newEquips!!.remove("Off limits")
            newEquips.addAll(equips)
            sharedPref.edit().putStringSet("Equipamentos", newEquips).apply()
        }


    }

    private suspend fun getEquipsFromDB(plant: String): MutableSet<String> {

        val request = DynamoAws().getItem(
            "instalacoes2-dev",
            "empresa",
            keyVal = plant,
            "Equipamentos"
        )
        val requestreturn =
            request.toString().subSequence(10, request.toString().length - 2).split(", ")
                .toMutableSet() //Transform the result and returns a set of "Plant_Local_Equipment"

        return requestreturn

    }

    //att
    fun plantAtt(plant: String) {
        if (plant == "Selecione uma Instalação" || plant == "") { //0 selection check
            resetLocal()
            return
        }

        val listLocals = ArrayList<String>(0)

        equips!!.forEach { equip ->
            if (plant.equals(equip.split("_")[0])) {
                listLocals.add(equip.split("_")[1])
            }
        }
        arrayAdapterLocal.clear()
        arrayAdapterLocal.add("")
        arrayAdapterLocal.addAll(listLocals.distinct().sorted())

    }


    fun localAtt(plant: String, local: String) {
        if (local == "") {
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

            arrayAdapterEquip.clear()
            arrayAdapterEquip.add("")
            arrayAdapterEquip.addAll(listEquips.distinct().sorted())
            arrayAdapterEquip.notifyDataSetChanged()
        }
    }



    //Reseters
    fun resetPlant() {
        val listPlant: ArrayList<String> = ArrayList(0)
        listPlant.add(" Selecione uma Instalação")// to stay on top
        println(equips)
        equips!!.forEach { equip ->
            listPlant.add(equip.split("_")[0])
        }
        arrayAdapterPlant =
            ArrayAdapter(
                context,
                R.layout.spinner_layout,
                listPlant.distinct().sorted()
            )
        resetLocal()
    }

    private fun resetLocal() {
        arrayAdapterLocal.clear()
        arrayAdapterLocal.add("")
        resetEquip()
    }

    private fun resetEquip() {
        arrayAdapterEquip.clear()
        arrayAdapterEquip.add("")
    }



    fun getNearEquips(plant: String, local: String): ArrayList<String> {
        if (local == " ") {
            return arrayListOf("Falha") //TODO Transformar em erro
        }
        val listEquips = ArrayList<String>(0)

        equips!!.forEach { equip ->
            if (plant == equip.split("_")[0] &&
                local == equip.split("_")[1]
            ) {
                listEquips.add("${equip.split("_")[2]} - ${equip.split("_")[3]}")
            }
        }
        return listEquips
    }
}