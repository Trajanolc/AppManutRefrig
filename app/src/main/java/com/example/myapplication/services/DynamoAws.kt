package com.example.myapplication.services

import android.app.AlertDialog
import android.content.Context
import aws.sdk.kotlin.runtime.auth.credentials.Credentials
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.GetItemRequest
import aws.sdk.kotlin.services.dynamodb.model.PutItemRequest
import com.example.myapplication.entities.Order
import com.example.myapplication.enum.HCredentials

class DynamoAws {
    private val credentialsAWS = StaticCredentialsProvider(Credentials(HCredentials.AWS_ACCESS_KEY.cred,HCredentials.AWS_SECRET_KEY.cred
    ))

    suspend fun putOrder(
        Order: Order,
        context: Context
    ) {
        val table = "ordemServico"
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
            credentialsProvider = credentialsAWS
            region = "us-east-2"
        }.use { ddb ->

            ddb.putItem(request)
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Sucesso")
            builder.setMessage("Ordem Nº ${Order.id} referente ao ${Order.equipment} enviada com sucesso!")
            builder.setPositiveButton("Ok") { _, _ -> }
            builder.show()
        }
    }

    suspend fun getItem(
        tableNameVal: String,
        keyName: String,
        keyVal: String,
        coluna: String
    ): AttributeValue? {
        val keyToGet = mutableMapOf<String, AttributeValue>()
        var retorno: AttributeValue? = null
        keyToGet[keyName] = AttributeValue.S(keyVal)
        val request = GetItemRequest {
            tableName = tableNameVal
            key = keyToGet
        }


        DynamoDbClient {
            region = "us-east-2"
            credentialsProvider = credentialsAWS
        }.use { ddb ->

            val returnedItem = ddb.getItem(request)
            val numbersMap = returnedItem.item

            numbersMap?.forEach { key1 ->
                if (key1.key == coluna) {
                    retorno = key1.value
                }
            }


        }
        return retorno

    }


}