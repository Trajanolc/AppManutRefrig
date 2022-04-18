package com.example.myapplication


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import aws.sdk.kotlin.runtime.auth.credentials.Credentials
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.*
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.provider.WebAuthProvider
import com.example.myapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.runBlocking

import com.auth0.android.authentication.AuthenticationAPIClient

import com.auth0.android.callback.Callback
import com.auth0.android.management.ManagementException
import com.auth0.android.management.UsersAPIClient

import com.auth0.android.result.Credentials as Credencias
import com.auth0.android.result.UserProfile
import java.util.*


class MainActivity : AppCompatActivity() {


    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    var cachedCredentials: Credencias? = null
    public var cachedUserProfile: UserProfile? = null
    var account = Auth0(
        "mkIWwOBPcHort6RWafUCx0YJbhZME8rb",
        "dev-l9wd1tpd.us.auth0.com"
    )

    private val emteste =false






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)





        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)



        if (emteste){
            val login = getSharedPreferences("login", MODE_PRIVATE)
            login.edit().clear().putString("login","Trajano").apply()
        } else {

            loginWithBrowser(account)
        }

            runBlocking {
                val login = getSharedPreferences("login", MODE_PRIVATE).getString("login","")
                val versaoequipamentos = getSharedPreferences("Equipamentos", MODE_PRIVATE)

                versaoequipamentos.edit().clear().commit()

                if(login=="geronildo"||login=="elias"||login=="Trajano") {
                    var lista =
                        getSpecificItem("instalacoes2-dev", "empresa", "Equatorial", "Equipamentos")

                    val listastrings =
                        lista.toString().subSequence(10, lista.toString().length - 2).split(", ")
                            .toMutableSet()
                    versaoequipamentos.edit()
                        .putStringSet("listaEquipamentos", listastrings)
                        .apply()
                }
                if(login=="marcus"||login=="Trajano"){
                    var lista =
                        getSpecificItem("instalacoes2-dev", "empresa", "Agropalma", "Equipamentos")

                    val listastrings =
                        lista.toString().subSequence(10, lista.toString().length - 2).split(", ")
                            .toMutableSet()

                    val versaoEquipamentosNew = getSharedPreferences("Equipamentos", MODE_PRIVATE).getStringSet("listaEquipamentos",
                        mutableSetOf(""))

                    listastrings.addAll(Collections.unmodifiableCollection(versaoEquipamentosNew))

                    versaoequipamentos.edit()
                        .putStringSet("listaEquipamentos", listastrings)
                        .apply()
                    println(versaoequipamentos.toString())
                }
            }

    }







    private fun loginWithBrowser(account: Auth0) {
        // Setup the WebAuthProvider, using the custom scheme and scope.

        WebAuthProvider.login(account)
            .withScheme(getString(R.string.com_auth0_scheme))
            .withScope("openid profile email read:current_user update:current_user_metadata")
            .withAudience("https://${getString(R.string.com_auth0_domain)}/api/v2/")
            // Launch the authentication passing the callback where the results will be received
            .start(this, object : Callback<Credencias, AuthenticationException> {
                // Called when there is an authentication failure
                override fun onFailure(exception: AuthenticationException) {
                    // Something went wrong!
                }

                // Called when authentication completed successfully
                override fun onSuccess(credenciais: Credencias) {
                    // Get the access token from the credentials object.
                    // This can be used to call APIs
                    cachedCredentials = credenciais

                    println("access token: ${credenciais.accessToken})")
                    println("ID: ${credenciais.idToken}")

                    val client = AuthenticationAPIClient(account)
                    client.userInfo(cachedCredentials!!.accessToken!!)
                        .start(object : Callback<UserProfile, AuthenticationException> {
                            override fun onFailure(exception: AuthenticationException) {

                            }

                            override fun onSuccess(profile: UserProfile) {
                                cachedUserProfile = profile;
                                println("profile: ${profile.name}, nickname: ${profile.nickname}")
                                val login = getSharedPreferences("login", MODE_PRIVATE)
                                login.edit().clear().putString("login",profile.nickname).apply()

                                Toast.makeText(this@MainActivity,"Bem vindo, ${profile.nickname}!",Toast.LENGTH_SHORT).show()
                            }
                        })
                }

            })
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
            R.id.action_settings -> {
                if(cachedUserProfile == null){
                    loginWithBrowser(account)
                }
                else{
                    logout()
                    Toast.makeText(this, "Logout realizado com sucesso!", Toast.LENGTH_SHORT).show()

                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun logout() {
        WebAuthProvider.logout(account)
            .withScheme(getString(R.string.com_auth0_scheme))
            .start(this, object : Callback<Void?, AuthenticationException> {
                override fun onSuccess(payload: Void?) {
                    // The user has been logged out!
                    cachedCredentials = null
                    cachedUserProfile = null
                    val login = getSharedPreferences("login", MODE_PRIVATE)
                    login.edit().clear().putString("login","").apply()
                }

                override fun onFailure(exception: AuthenticationException) {

                }
            })
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
            credentialsProvider=
                StaticCredentialsProvider(Credentials("AKIAXJ6IWE3BPJLVFANI","MFr8G6u2JsoYzPLxtSjt3bgE2lVL4qKoZ0NBwOpT"))
        }.use { ddb ->

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



