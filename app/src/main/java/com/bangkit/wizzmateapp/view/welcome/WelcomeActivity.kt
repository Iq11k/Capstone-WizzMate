package com.bangkit.wizzmateapp.view.welcome

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bangkit.wizzmateapp.BuildConfig
import com.bangkit.wizzmateapp.R
import com.bangkit.wizzmateapp.data.local.SessionPreferences
import com.bangkit.wizzmateapp.data.local.dataStore
import com.bangkit.wizzmateapp.databinding.ActivityWelcomeBinding
import com.bangkit.wizzmateapp.helper.StringHelper.makeTextLink
import com.bangkit.wizzmateapp.view.authentication.AuthenticationActivity
import com.bangkit.wizzmateapp.view.authentication.LoginViewModel
import com.bangkit.wizzmateapp.view.authentication.LoginViewModelFactory
import com.bangkit.wizzmateapp.view.main.MainActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var auth: FirebaseAuth

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("Permission", "Location permission granted")
        } else {
            Log.d("Permission", "Location permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestLocationPermission()

        val pref = SessionPreferences.getInstance(this.dataStore)
        val loginViewModel = ViewModelProvider(this, LoginViewModelFactory(pref))[LoginViewModel::class.java]
        binding.buttonLogin.setOnClickListener {
            startActivity(Intent(this, AuthenticationActivity::class.java))
        }

        // Initialize Firebase Auth
        auth = Firebase.auth

        makeTextLink(binding.tvConfirmAccount, "Sign Up", true, R.color.primaryColor) {
            val intent = Intent(this, AuthenticationActivity::class.java).apply {
                putExtra("isRegister", true)
            }
            startActivity(intent)
        }
        makeTextLink(binding.tvTermDescription, "Terms of Services", false, R.color.primaryColor) {
            val intent = Intent(this, AuthenticationActivity::class.java).apply {
                putExtra("isRegister", true)
            }
            startActivity(intent)
        }
        makeTextLink(binding.tvTermDescription, "Privacy Policy", false, R.color.primaryColor) {
            val intent = Intent(this, AuthenticationActivity::class.java).apply {
                putExtra("isRegister", true)
            }
            startActivity(intent)
        }

        binding.buttonLoginGoogle.setOnClickListener {
            signIn()
        }

        loginViewModel.isAlreadyLogin()
        loginViewModel.isLogin.observe(this){
            if (it){
                val intent = Intent(this, MainActivity::class.java).
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
        }
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted
                Log.d("Permission", "Location permission already granted")
            }

            shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Show an explanation to the user
                Log.d("Permission", "Explain why the app needs location access")
                // Here, you can show a dialog explaining why the permission is needed
                requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }

            else -> {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun signIn() {
        val credentialManager = CredentialManager.create(this) //import from androidx.CredentialManager
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.AUTH_WEB_CLIENT_ID)
            .build()
        val request = GetCredentialRequest.Builder() //import from androidx.CredentialManager
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result: GetCredentialResponse = credentialManager.getCredential( //import from androidx.CredentialManager
                    request = request,
                    context = this@WelcomeActivity,
                )
                handleSignIn(result)
            } catch (e: GetCredentialException) { //import from androidx.CredentialManager
                Log.d("Error", e.message.toString())
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        // Handle the successfully returned credential.
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                } else {
                    // Catch any unrecognized custom credential type here.
                    Log.e(TAG, "Unexpected type of credential")
                }
            }
            else -> {
                // Catch any unrecognized credential type here.
                Log.e(TAG, "Unexpected type of credential")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential: AuthCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user: FirebaseUser? = auth.currentUser
                    user?.let {
                        saveUserToDatabase(it)
                        updateUI(it)
                    }
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            val username = currentUser.displayName!!.split(" ")[0]
            lifecycleScope.launch {
                val pref = SessionPreferences.getInstance(dataStore)
                pref.saveusername(username)
            }

            startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
            finish()
        }
    }

    private fun saveUserToDatabase(user: FirebaseUser) {
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        val userData = mapOf(
            "email" to user.email,
            "username" to (user.displayName ?: "Anonymous")
        )

        usersRef.child(user.uid).setValue(userData)
            .addOnSuccessListener {
                Log.d(TAG, "User data saved to Realtime Database")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to save user data: ", exception)
            }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    companion object {
        val TAG = "Welcome Activity"
    }
}