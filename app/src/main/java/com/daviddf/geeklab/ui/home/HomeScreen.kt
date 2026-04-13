package com.daviddf.geeklab.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.daviddf.geeklab.R
import com.daviddf.geeklab.ui.components.FavoriteCard
import com.daviddf.geeklab.ui.theme.CardAplicaciones
import com.daviddf.geeklab.ui.theme.CardBateria
import com.daviddf.geeklab.ui.theme.CardInformacion
import com.daviddf.geeklab.ui.theme.CardNotificaciones
import com.daviddf.geeklab.ui.theme.GeekLabTheme
import com.daviddf.geeklab.ui.theme.TextAplicaciones
import com.daviddf.geeklab.ui.theme.TextBateria
import com.daviddf.geeklab.ui.theme.TextInformacion
import com.daviddf.geeklab.ui.theme.TextNotificaciones

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNotificationClick: () -> Unit,
    onBatteryClick: () -> Unit,
    onInfoClick: () -> Unit,
    onAppsClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.welcome),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                actions = {
                    Image(
                        painter = painterResource(id = R.drawable.preset),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { /* Profile Click */ },
                        contentScale = ContentScale.Crop
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Column {
                    SectionHeader(title = stringResource(R.string.favorites), onSeeAllClick = {})
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        FavoriteCard(
                            modifier = Modifier.weight(1f),
                            title = stringResource(R.string.notificaciones),
                            icon = Icons.Rounded.Notifications,
                            containerColor = CardNotificaciones,
                            contentColor = TextNotificaciones,
                            onClick = onNotificationClick
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        FavoriteCard(
                            modifier = Modifier.weight(1f),
                            title = stringResource(R.string.battery_title),
                            icon = Icons.Rounded.BatteryFull,
                            containerColor = CardBateria,
                            contentColor = TextBateria,
                            onClick = onBatteryClick
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        FavoriteCard(
                            modifier = Modifier.weight(1f),
                            title = stringResource(R.string.info_title),
                            icon = Icons.Rounded.Info,
                            containerColor = CardInformacion,
                            contentColor = TextInformacion,
                            onClick = onInfoClick
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        FavoriteCard(
                            modifier = Modifier.weight(1f),
                            title = stringResource(R.string.apps_title),
                            icon = Icons.Rounded.GridView,
                            containerColor = CardAplicaciones,
                            contentColor = TextAplicaciones,
                            onClick = onAppsClick
                        )
                    }
                }
            }

            item {
                Column {
                    SectionHeader(title = stringResource(R.string.featured), onSeeAllClick = {})
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    FeaturedCard(
                        title = stringResource(R.string.featured_apps_title),
                        imageRes = R.drawable.preview_loop
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    FeaturedCard(
                        title = stringResource(R.string.featured_design_title),
                        imageRes = R.drawable.preview_notification
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAllClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.see_all),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.clickable { onSeeAllClick() }
        )
    }
}

@Composable
fun FeaturedCard(title: String, imageRes: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    GeekLabTheme {
        HomeScreen(
            onNotificationClick = {},
            onBatteryClick = {},
            onInfoClick = {},
            onAppsClick = {}
        )
    }
}
