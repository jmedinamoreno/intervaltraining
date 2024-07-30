package com.medina.intervaltraining.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun ButtonPlaySound() {
    val context = LocalContext.current
    var soundPool by remember { mutableStateOf<FXSoundPool?>(null) }
    // Initialize SoundPool & dispose it when the composable leaves the composition
    LaunchedEffect(key1 = context) { soundPool = FXSoundPool(context).build() }
    DisposableEffect(Unit) {onDispose {soundPool?.release();soundPool = null}}

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            if(soundPool!=null)
                soundPool?.playSound(FXSoundPool.FX.TIN)
            else
                Log.d("JMMLOG", "SoundComponents: sound not played -> ");
        }
        ) {
            Text("Play Sound")
        }
    }
}

@Preview
@Composable
fun PreviewPlaySound() {
    ButtonPlaySound()
}