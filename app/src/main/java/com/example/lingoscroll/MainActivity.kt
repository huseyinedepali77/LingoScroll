package com.example.lingoscroll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.lingoscroll.theme.LingoScrollTheme
import com.example.lingoscroll.ui.main.MainScreenViewModel

class MainActivity : ComponentActivity() {
    private var viewModel: MainScreenViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        
        // Tam ekran yapmak için üst bildirim barını gizliyoruz
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            val context = LocalContext.current.applicationContext
            val vm: MainScreenViewModel = viewModel {
                MainScreenViewModel(context)
            }
            viewModel = vm // Yaşam döngüsünde diske kaydetmek üzere referansını tutuyoruz

            LingoScrollTheme { 
                Surface(
                    modifier = Modifier.fillMaxSize(), 
                    color = MaterialTheme.colorScheme.background
                ) { 
                    MainNavigation(viewModel = vm) 
                } 
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Kullanıcı uygulamayı arka plana aldığında RAM'deki ilerlemeyi asenkron olarak diske yaz
        viewModel?.saveProgress()
    }

    override fun onStop() {
        super.onStop()
        // Kullanıcı uygulamayı kapattığında RAM'deki ilerlemeyi diske yaz
        viewModel?.saveProgress()
    }
}
