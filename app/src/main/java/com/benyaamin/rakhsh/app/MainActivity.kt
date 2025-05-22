package com.benyaamin.rakhsh.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.benyaamin.rakhsh.app.list.ListScreen
import com.benyaamin.rakhsh.app.list.ListViewModel
import com.benyaamin.rakhsh.app.ui.theme.RakhshTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lightSystemBarStyle = SystemBarStyle.light(0, 0)
        enableEdgeToEdge(statusBarStyle = lightSystemBarStyle, navigationBarStyle = lightSystemBarStyle)
        viewModel.init(this)
        setContent {
            RakhshTheme(darkTheme = false) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val listState = viewModel.downloadsList.collectAsStateWithLifecycle(emptyList())
                    ListScreen(listState.value) {
                        viewModel.processAction(it)
                    }
                }
            }
        }
    }
}