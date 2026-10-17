package com.example.sanaesenciaapp.ui.views

<<<<<<< HEAD
import androidx.compose.animation.core.*
=======
>>>>>>> 0c342bb8295f777899d0c8481aac18b66bff87b5
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
<<<<<<< HEAD
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
=======
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
>>>>>>> 0c342bb8295f777899d0c8481aac18b66bff87b5
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController
) {
<<<<<<< HEAD
    // Estados para controlar la animación
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "alpha"
    )

    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000) // Tiempo que dura el Splash en pantalla antes de ir a home
        navController.navigate("home") {
=======

    LaunchedEffect(Unit) {

        delay(1000)

        navController.navigate("home") {

>>>>>>> 0c342bb8295f777899d0c8481aac18b66bff87b5
            popUpTo("splash") {
                inclusive = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            ),
<<<<<<< HEAD
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🌿",
            fontSize = 72.sp,
            modifier = Modifier
                .scale(scaleAnim.value)
                .alpha(alphaAnim.value)
=======

        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "🌿",
            fontSize = 72.sp
>>>>>>> 0c342bb8295f777899d0c8481aac18b66bff87b5
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "SanaEsencia",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
<<<<<<< HEAD
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.alpha(alphaAnim.value)
=======
            color = MaterialTheme.colorScheme.secondary
>>>>>>> 0c342bb8295f777899d0c8481aac18b66bff87b5
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Bienestar integral",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary,
<<<<<<< HEAD
            textAlign = TextAlign.Center,
            modifier = Modifier.alpha(alphaAnim.value)
=======
            textAlign = TextAlign.Center
>>>>>>> 0c342bb8295f777899d0c8481aac18b66bff87b5
        )
    }
}