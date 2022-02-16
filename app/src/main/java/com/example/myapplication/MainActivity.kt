package com.example.myapplication


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import aws.sdk.kotlin.runtime.config.profile.awsSecretAccessKey
import aws.sdk.kotlin.runtime.config.profile.loadActiveAwsProfile
import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.*
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.example.myapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.crypto.Cipher.SECRET_KEY


class MainActivity : AppCompatActivity() {


    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding








    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)




        val versaoequipamentos = getSharedPreferences("VersaoInstalacao", MODE_PRIVATE)
        val listaequipamentos = getSharedPreferences("ListaEquipamentos", MODE_PRIVATE)

//        var versaolocal: Map<String, AttributeValue>? = null
//        GlobalScope.launch(Dispatchers.Main) {
//            Toast.makeText(this@MainActivity,"zero1",Toast.LENGTH_SHORT).show()
//            Toast.makeText(this@MainActivity,"zero",Toast.LENGTH_SHORT).show()
//            getSpecificItem("instalacoes2-dev", "empresa", "Equatorial")
//        }
//       suspend {getSpecificItem("Instalacoes","nomeInstalacao","Equatorial")
//           if(versaoequipamentos.getString("VersaoInstalacao","") != versaolocal?.get("Versao").toString()){
//               //var editor = listaequipamentos.edit()
//               print(versaolocal?.get("listaEquipamentos"))
//           }
//
//       }


//        //atualizar
//            //atualizar a tabela de locais
//            //atualizar versão
//        }






        //TODO checar a versao dos csvs de locais no dynamo, caso seja um maior, baixar do bucket s3 e atualizar a versão localmente


    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }


    suspend fun getSpecificItem(tableNameVal: String, keyName: String, keyVal: String) {

        val keyToGet = mutableMapOf<String, AttributeValue>()
        keyToGet[keyName] = AttributeValue.S(keyVal)
        Toast.makeText(this,"one",Toast.LENGTH_SHORT).show()
        val request = GetItemRequest {
            key = keyToGet
            tableName = tableNameVal


        }
        Toast.makeText(this,"two",Toast.LENGTH_SHORT).show()



        DynamoDbClient { region="eu-east-2" }.use { ddb ->
            try {
                var returnedItem = ddb.getItem(request)
                Toast.makeText(this, returnedItem.consumedCapacity.toString(), Toast.LENGTH_SHORT)
                    .show()
                val numbersMap = returnedItem.item
                numbersMap?.forEach { key1 ->
                    Toast.makeText(this, key1.key, Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, key1.value.toString(), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "tree", Toast.LENGTH_SHORT).show()
            }


        }

    }

    suspend fun queryDynTable(
        tableNameVal: String,
        partitionKeyName: String,
        partitionKeyVal: String,
        partitionAlias: String
    ): List<Map<String,AttributeValue>>? {

        val attrNameAlias = mutableMapOf<String, String>()
        attrNameAlias[partitionAlias] = partitionKeyName

        // Set up mapping of the partition name with the value.
        val attrValues = mutableMapOf<String, AttributeValue>()
        attrValues[":$partitionKeyName"] = AttributeValue.S(partitionKeyVal)

        val request = QueryRequest {
            tableName = tableNameVal
            keyConditionExpression = "$partitionAlias = :$partitionKeyName"
            expressionAttributeNames = attrNameAlias
            this.expressionAttributeValues = attrValues
        }

        DynamoDbClient { region = "us-east-1" }.use { ddb ->
            val response = ddb.query(request)
            return response.items
        }
    }



    private fun isPermissionsAllowed(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    fun askForPermissions(): Boolean {
        if (!isPermissionsAllowed()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this as Activity,Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showPermissionDeniedDialog()
            } else {
                ActivityCompat.requestPermissions(this as Activity,arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
            }
            return false
        }
        return true
    }




    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<String>,grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission is granted, you can perform your operation here
                } else {
                    // permission is denied, you can ask for permission again, if you want
                    //  askForPermissions()
                }
                return
            }
        }
    }



    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("Permission is denied, Please allow permissions from App Settings.")
            .setPositiveButton("App Settings",
                DialogInterface.OnClickListener { dialogInterface, _ ->
                    // send to app settings if permission is denied permanently
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", getPackageName(), null)
                    intent.data = uri
                    startActivity(intent)
                })
            .setNegativeButton("Cancel",null)
            .show()
    }
}

