package com.example.myapplication.services

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.asByteStream
import java.net.URI
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*

class S3aws {
    suspend fun putS3Object(
        bucket: String,
        uri: Uri,
        local: String,
        equipment: String,
        i: Int,
        timeWhen: String
    ): Boolean {
        val metadataVal: MutableMap<String, String> = mutableMapOf<String, String>()
        metadataVal["Local"] = local
        val request = PutObjectRequest {
            key = "${equipment.split("-").last().trimStart()}_" +
                    "${SimpleDateFormat("dd-MM-yyyy").format(Date())}_${timeWhen}_" +
                    "${i.toString().padStart(2, '0')}.JPEG".replace("/", "-")
            metadata = metadataVal
            this.body = Paths.get(uri.path).asByteStream()
        }

        S3Client {
            region = "us-east-2"
        }.use { s3 ->
            val response = s3.putObject(request)

            if (response.eTag != null) {
                return true
            }
            return false
        }
    }
}