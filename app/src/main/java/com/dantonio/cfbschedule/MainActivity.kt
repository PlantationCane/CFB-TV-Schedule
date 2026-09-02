package com.dantonio.cfbschedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.dantonio.cfbschedule.ui.ScheduleScreen
import com.dantonio.cfbschedule.ui.ScheduleViewModel
import com.dantonio.cfbschedule.ui.theme.CfbScheduleTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ScheduleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CfbScheduleTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ScheduleScreen(viewModel = viewModel)
                }
            }
        }
    }
}
