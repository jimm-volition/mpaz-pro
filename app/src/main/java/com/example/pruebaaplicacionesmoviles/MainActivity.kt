package com.example.pruebaaplicacionesmoviles
import com.example.pruebaaplicacionesmoviles.ui.screens.LoginScreen
import androidx.lifecycle.ViewModelProvider
import com.example.pruebaaplicacionesmoviles.viewmodel.LoginViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import com.example.pruebaaplicacionesmoviles.ui.theme.PruebaAplicacionesMovilesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        enableEdgeToEdge()
        setContent {
            PruebaAplicacionesMovilesTheme {
                LoginScreen(loginViewModel)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PruebaAplicacionesMovilesTheme {
        Greeting("Android")
    }
}