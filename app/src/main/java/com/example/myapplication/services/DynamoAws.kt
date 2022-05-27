package com.example.myapplication.services

import android.app.AlertDialog
import android.content.Context
import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.PutItemRequest
import com.example.myapplication.R
import com.example.myapplication.entities.Order

class DynamoAws {

    suspend fun putDynamoDB(
        Order: Order,
        table: String,
        context: Context
    ) {

        val itemValues = mutableMapOf<String, AttributeValue>()

        // Add all content to the table.
        itemValues["ordemID"] = AttributeValue.N(Order.id)
        itemValues["PAT"] = AttributeValue.S(Order.pat)
        itemValues["Local"] = AttributeValue.S(Order.local)
        itemValues["Instalacao"] = AttributeValue.S(Order.plant)
        itemValues["Equipamento"] = AttributeValue.S(Order.equipment)
        itemValues["tipoManut"] = AttributeValue.Ss(Order.getTypeManut())
        itemValues["tipoServicos"] = AttributeValue.Ss(Order.getTypeServices())
        itemValues["tipoTroca"] = AttributeValue.Ss(Order.getTypeSwap())
        itemValues["OBS"] = AttributeValue.S(Order.obs)
        itemValues["FuncionarioID"] = AttributeValue.S(Order.employeeId!!)
        itemValues["DataFim"] = AttributeValue.S(Order.dateEnd)
        if (Order.getImgKeysBefore().isNotEmpty()) itemValues["fotosAntes"] =
            AttributeValue.Ss(Order.getImgKeysBefore())
        if (Order.getImgKeysAfter().isNotEmpty()) itemValues["fotosDepois"] =
            AttributeValue.Ss(Order.getImgKeysAfter())


        val request = PutItemRequest {

            tableName = table
            item = itemValues
        }

        DynamoDbClient {
            region = "us-east-2"
        }.use { ddb ->

            ddb.putItem(request)
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Sucesso")
            builder.setMessage("Ordem Nº ${Order.id} referente ao ${Order.equipment} enviada com sucesso!")
            builder.setPositiveButton("Ok") { _, _ ->
                try {
                    findNavController().navigate(R.id.action_insert_form_to_FirstFragment)
                } catch (e: IllegalArgumentException) {
                    //Do nothing
                }

            }
            builder.show()
        }

    }
}