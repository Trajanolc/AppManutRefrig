package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import aws.sdk.kotlin.runtime.auth.credentials.*
import aws.sdk.kotlin.runtime.config.profile.loadActiveAwsProfile
import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue.*
import aws.sdk.kotlin.services.dynamodb.model.GetItemRequest
import com.example.myapplication.databinding.HomeBinding
import kotlinx.coroutines.runBlocking
import com.auth0.android.result.UserProfile


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
            val login = requireActivity().getSharedPreferences("login", AppCompatActivity.MODE_PRIVATE)
            println(login.getString("login",""))
            if(login.getString("login","") != "") {
                findNavController().navigate(R.id.action_FirstFragment_to_insert_form)
            }
                else {
                Toast.makeText(requireContext(), "Por favor, faça o login antes", Toast.LENGTH_SHORT).show()
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
        credentialsProvider=StaticCredentialsProvider(Credentials("AKIAXJ6IWE3BPJLVFANI","MFr8G6u2JsoYzPLxtSjt3bgE2lVL4qKoZ0NBwOpT"))}.use { ddb ->

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