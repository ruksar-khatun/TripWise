package com.example.tripwise.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tripwise.R
import com.example.tripwise.ui.components.TripWiseOutlinedButton
import com.example.tripwise.ui.components.TripWisePrimaryButton
import com.example.tripwise.ui.theme.AzureMist
import com.example.tripwise.ui.theme.DuskBlue
import com.example.tripwise.ui.theme.SeaBlue
import com.example.tripwise.ui.theme.SeaSalt

data class IntroSlide(
    val title: String,
    val description: String,
    val imageRes: Int
)

@Composable
fun OnboardingScreen(
    onSignUp: () -> Unit,
    onLogin: () -> Unit
) {
    val slides = listOf(
        IntroSlide(
            title = "Discover World Destinations",
            description = "Explore iconic landmarks, historic temples, and scenic paradises tailored to your travel style.",
            imageRes = R.drawable.img_3
        ),
        IntroSlide(
            title = "Plan Custom Itineraries",
            description = "Build day-by-day travel schedules, organize activities, and track your total budget effortlessly.",
            imageRes = R.drawable.img_6
        ),
        IntroSlide(
            title = "Travel Smarter",
            description = "Stay organized on the go with real-time day schedules, expense breakdowns, and saved places.",
            imageRes = R.drawable.img_9
        )
    )

    val pagerState = rememberPagerState(pageCount = { slides.size })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzureMist)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val slide = slides[page]
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = slide.imageRes),
                    contentDescription = slide.title,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(260.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = slide.title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuskBlue,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = slide.description,
                    fontSize = 15.sp,
                    color = DuskBlue.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }

        // Indicators
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            repeat(slides.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (isSelected) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) SeaBlue else SeaSalt.copy(alpha = 0.4f))
                )
            }
        }

        TripWisePrimaryButton(
            text = "Sign Up Now",
            onClick = onSignUp
        )

        Spacer(modifier = Modifier.height(12.dp))

        TripWiseOutlinedButton(
            text = "Log In",
            onClick = onLogin
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
