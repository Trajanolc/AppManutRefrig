package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import aws.sdk.kotlin.runtime.auth.credentials.*
import aws.sdk.kotlin.runtime.client.AwsClientConfig
import aws.sdk.kotlin.runtime.config.AwsClientConfigLoadOptions
import aws.sdk.kotlin.runtime.config.profile.loadActiveAwsProfile
import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue.*
import aws.sdk.kotlin.services.dynamodb.model.GetItemRequest
import com.example.myapplication.databinding.HomeBinding
import kotlinx.coroutines.runBlocking


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class Home : Fragment() {

    private var _binding: HomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        _binding = HomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addOS.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_insert_form)
                var lista : AttributeValue? = null
                runBlocking {
                    lista = getSpecificItem("instalacoes2-dev", "empresa", "Equatorial","Equipamentos")
                    var lista_strings = lista.toString().subSequence(10,lista.toString().length-2).split(", ")
                    lista_strings.forEach { i ->
                        println(i)
                    }
                }




        }
    }

    suspend fun getSpecificItem(tableNameVal: String, keyName: String, keyVal: String,coluna: String):AttributeValue? {

        val keyToGet = mutableMapOf<String, AttributeValue>()
        var retorno : AttributeValue? = null
        keyToGet[keyName] = AttributeValue.S(keyVal)
        val request = GetItemRequest {
            tableName = tableNameVal
            key = keyToGet
        }


        DynamoDbClient { region="us-east-2"
        credentialsProvider=StaticCredentialsProvider(Credentials("AKIAXJ6IWE3BCGFE22SY","kQkf4fhf9baLmOpc0V/+IggLutlb6XJK+ZFjDHlB"))}.use { ddb ->

                val returnedItem = ddb.getItem(request)
                val numbersMap = returnedItem.item

                numbersMap?.forEach { key1 ->
                    if(key1.key == coluna) {
                        retorno = key1.value
                    }
                }



        }
    return retorno
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}