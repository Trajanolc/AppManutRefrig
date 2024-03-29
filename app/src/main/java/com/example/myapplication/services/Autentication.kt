package com.example.myapplication.services

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.auth0.android.result.UserProfile
import com.example.myapplication.R
import com.example.myapplication.entities.ListEquip
import com.example.myapplication.enum.HCredentials
import com.example.myapplication.enum.Debugging
import kotlinx.coroutines.*

class Autentication (val context : Context){
    var account = Auth0(
        HCredentials.AUTH_CLIENT_ID.cred,
        HCredentials.AUTH_DOMAIN.cred
    )
    var cachedCredentials: Credentials? = null
    var cachedUserProfile: UserProfile? = null
    val listEquip = ListEquip(context)


    suspend fun login(){

        if (Debugging.LOGIN.B){
            context.getSharedPreferences("login", AppCompatActivity.MODE_PRIVATE).edit().clear()
                .putString("login", "Mario Marques").apply()
            runBlocking {

                listEquip.organizeEquips(arrayListOf("Equatorial"))
            }
        } else {
            loginWithBrowser(account)
        }
    }

    fun logout(){
        WebAuthProvider.logout(account)
            .withScheme(context.getString(R.string.com_auth0_scheme))
            .start(context, object : Callback<Void?, AuthenticationException> {
                override fun onSuccess(payload: Void?) {
                    // The user has been logged out!
                    cachedCredentials = null
                    cachedUserProfile = null
                    val login = context.getSharedPreferences("login", AppCompatActivity.MODE_PRIVATE)
                    login.edit().clear().putString("login","").apply()
                }

                override fun onFailure(exception: AuthenticationException) {

                }
            })
    }

    fun loginOut(){
        if(context.getSharedPreferences("login", AppCompatActivity.MODE_PRIVATE).getString("login","") == ""){
            CoroutineScope(MainScope().coroutineContext).async {
                Autentication(context).login()
            }
        }
        else{
            logout()
            Toast.makeText(context, "Logout realizado com sucesso!", Toast.LENGTH_SHORT).show()

        }
    }

    private fun loginWithBrowser(account: Auth0) {
        // Setup the WebAuthProvider, using the custom scheme and scope.

        WebAuthProvider.login(account)
            .withScheme(context.getString(R.string.com_auth0_scheme))
            .withScope("openid profile email read:current_user update:current_user_metadata")
            .withAudience("https://${context.getString(R.string.com_auth0_domain)}/api/v2/")
            // Launch the authentication passing the callback where the results will be received
            .start(context, object : Callback<Credentials, AuthenticationException> {
                // Called when there is an authentication failure
                override fun onFailure(exception: AuthenticationException) {
                    // Something went wrong!
                }

                // Called when authentication completed successfully
                override fun onSuccess(credenciais: Credentials) {
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
                                val login = context.getSharedPreferences("login",
                                    AppCompatActivity.MODE_PRIVATE
                                )
                                login.edit().clear().putString("login",profile.nickname).apply()

                                runBlocking{
                                    val plants = DynamoAws().getItem("Usuarios","email",profile.name!!,"empresas")
                                    val plantsStrings = plants.toString().substring(10,plants.toString().length - 2).split(", ")
                                    val arrayOfPlants : ArrayList<String> = arrayListOf()
                                    plantsStrings.forEach { plant ->
                                        arrayOfPlants.add(plant)
                                    }
                                    listEquip.organizeEquips(arrayOfPlants)
                                }


                                Toast.makeText(context,"Bem vindo, ${profile.nickname}!",
                                    Toast.LENGTH_SHORT).show()
                            }
                        })
                }

            })
    }


}