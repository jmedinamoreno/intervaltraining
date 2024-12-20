package com.medina.intervaltraining.ui.theme

import android.graphics.RuntimeShader
import android.os.Build
import android.os.SystemClock
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ShaderBrush
import org.intellij.lang.annotations.Language

@Language("AGSL")
val VERTICAL_GRADIENT_SHADER = """
    // Simple Animated Vertical Gradient Shader

    uniform float2 iResolution;
    uniform float iTime;
    layout(color) uniform half4 color1;
    layout(color) uniform half4 color2;

    half4 main(in float2 fragCoord) {
        // Normalized vertical coordinate (0.0 at the top, 1.0 at the bottom)
        float normalizedY = fragCoord.y / iResolution.y;

        // Time-based animation (oscillates between 0 and 1)
        float t = (sin(iTime) + 1.0) / 2.0;

        // Blend between the two colors based on the normalized Y and time
        half4 color = mix(color1, color2, normalizedY * t);

        return color;
    }
""".trimIndent()

@Composable
fun Modifier.shader(): Modifier = if(supportsShaders()){
    val Color1 = MaterialTheme.colorScheme.onPrimary
    val Color2 = MaterialTheme.colorScheme.primary
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val animatedTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3.1416f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing)
        ), label = "animatedTime"
    )
    this.then(Modifier.drawWithCache {
        val shader = RuntimeShader(VERTICAL_GRADIENT_SHADER)
        val shaderBrush = ShaderBrush(shader)
        shader.setFloatUniform("iResolution", size.width, size.height)
        onDrawBehind {
            shader.setColorUniform(
                "color1",
                android.graphics.Color.valueOf(
                    Color1.red,
                    Color1.green,
                    Color1.blue,
                    Color1.alpha
                )
            )
            shader.setColorUniform(
                "color2",
                android.graphics.Color.valueOf(
                    Color2.red,
                    Color2.green,
                    Color2.blue,
                    Color2.alpha
                )
            )
            shader.setFloatUniform("iTime", animatedTime)
            drawRect(shaderBrush)
        }
    }
    )
}else{
    this
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
fun supportsShaders() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU