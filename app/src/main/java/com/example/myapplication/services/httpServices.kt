package com.example.myapplication.services

import com.example.myapplication.entities.OrderDTO
import com.example.myapplication.enum.HCredentials
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import java.io.IOException
import java.util.concurrent.CountDownLatch

class httpServices {


    companion object{
        fun getEmployeeOrder(employee :String): List<OrderDTO> {
            val client = OkHttpClient()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val type = Types.newParameterizedType(List::class.java,OrderDTO::class.java)

            val jsonAdapter = moshi.adapter<List<OrderDTO>>(type)

            var orderDTOList : List<OrderDTO> = listOf<OrderDTO>()

            val request = Request.Builder()
                .url("${HCredentials.API_ORDER_URL.cred}/orders/latest/$employee")
                .build()
            val countDownLatch = CountDownLatch(1)
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    countDownLatch.countDown();

                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) {
                            countDownLatch.countDown();
                            throw IOException("Unexpected code $response")
                        }


                        orderDTOList = jsonAdapter.fromJson(response.body!!.source())!!
                        countDownLatch.countDown();

                    }
                }
            })
            countDownLatch.await()
            if (orderDTOList.equals(arrayListOf<OrderDTO>())) throw IOException("Nothing found")

            return orderDTOList
        }

    }




}

