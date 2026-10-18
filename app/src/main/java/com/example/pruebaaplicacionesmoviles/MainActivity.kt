package com.example.pruebaaplicacionesmoviles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import com.example.pruebaaplicacionesmoviles.ui.screens.UtpScreen
import com.example.pruebaaplicacionesmoviles.ui.theme.PruebaAplicacionesMovilesTheme
import com.example.pruebaaplicacionesmoviles.viewmodel.UtpViewModel
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val utpViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[UtpViewModel::class.java]

        setContent {
            PruebaAplicacionesMovilesTheme {
                UtpScreen(viewModel = utpViewModel)
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