package com.example.myapplication.services

import com.example.myapplication.entities.Order
import com.example.myapplication.entities.OrderDTO
import com.example.myapplication.entities.SimplifiedOrderDTO
import com.example.myapplication.enum.HCredentials
import com.example.myapplication.exceptions.HttpException
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.CountDownLatch

class httpServices {


    companion object {
        fun addOrder(order: OrderDTO) {
            val client = OkHttpClient()

            val body = Json.encodeToString(order).toRequestBody()


            val request =
                Request.Builder()
                    .post(body)
                    .url("${HCredentials.API_ORDER_URL.cred}/orders/")
                    .addHeader("content-type", "application/json")
                    .build()
            val countDownLatch = CountDownLatch(1)
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    countDownLatch.countDown();
                    throw HttpException("Call failed")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        countDownLatch.countDown();
                        throw HttpException("Unexpected code $response")
                    }
                    countDownLatch.countDown();

                }

            })


        }


        fun getEmployeeOrder(employee: String): Pair<List<SimplifiedOrderDTO>, Int> {

            var ordersCount: Int = 0

            val client = OkHttpClient()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val type = Types.newParameterizedType(List::class.java, SimplifiedOrderDTO::class.java)

            val jsonAdapter = moshi.adapter<List<SimplifiedOrderDTO>>(type)

            var simplifiedOrderDTOList: List<SimplifiedOrderDTO> = listOf()

            val request = if (employee.equals("rayssa"))
                Request.Builder()
                    .url("${HCredentials.API_ORDER_URL.cred}/orders/month/")
                    .build()
            else
                Request.Builder()
                    .url("${HCredentials.API_ORDER_URL.cred}/orders/month/${employee}")
                    .build()

            val countDownLatch = CountDownLatch(1)

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    countDownLatch.countDown();
                    throw HttpException("Call failed")


                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) {
                            countDownLatch.countDown();
                            throw HttpException("Unexpected code $response")
                        }
                        ordersCount = Integer.parseInt(response.headers["X-Total-Count"].toString())
                        simplifiedOrderDTOList = jsonAdapter.fromJson(response.body!!.source())!!
                        countDownLatch.countDown();

                    }
                }
            })

            countDownLatch.await()

            if (simplifiedOrderDTOList.equals(arrayListOf<SimplifiedOrderDTO>())) throw IOException(
                "Nothing found"
            )

            return Pair(simplifiedOrderDTOList, ordersCount)
        }
    }


}

