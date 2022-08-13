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