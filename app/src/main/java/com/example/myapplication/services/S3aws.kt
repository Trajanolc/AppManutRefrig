package com.example.myapplication.services

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import aws.sdk.kotlin.runtime.auth.credentials.Credentials
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.PutItemRequest
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.asByteStream
import com.example.myapplication.R
import com.example.myapplication.entities.Order
import com.example.myapplication.enum.HCredentials
import java.net.URI
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class S3aws {

    private val credentialsAWS = StaticCredentialsProvider(
        Credentials(
            HCredentials.AWS_ACCESS_KEY.cred, HCredentials.AWS_SECRET_KEY.cred
    )
    )

    suspend fun putS3Object(
        bucketName: String,
        uri: Uri,
        local: String,
        keyImg: String
    ): Boolean {
        val metadataVal: MutableMap<String, String> = mutableMapOf<String, String>()
        metadataVal["Local"] = local
        val request = PutObjectRequest {
            bucket = bucketName
            key = keyImg
            metadata = metadataVal
            this.body = Paths.get(uri.path).asByteStream()

        }

        S3Client {
            region = "us-east-2"
            credentialsProvider = credentialsAWS
        }.use { s3 ->
            val response = s3.putObject(request)

            if (response.eTag != null) {
                return true
            }
            return false
        }
    }
}