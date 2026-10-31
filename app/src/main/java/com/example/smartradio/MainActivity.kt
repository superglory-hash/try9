package com.example.smartradio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                RadioAppScreen()
            }
        }
    }
}

data class Station(val name: String, val url: String, var isFavorite: Boolean = false)

@Composable
fun RadioAppScreen() {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    val stations = remember { mutableStateListOf(
        Station("임시방송 1", "http://c15.smartradio.co.kr/sbs"),
        Station("임시방송 2", "http://c15.smartradio.co.kr/mbc"),
        Station("임시방송 3", "http://c15.smartradio.co.kr/kbs")
    ) }
    
    var currentStation by remember { mutableStateOf(stations[0]) }
    var isPlaying by remember { mutableStateOf(false) }
    var showLyricsDialog by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(currentStation) {
        try {
            exoPlayer.stop()
            val mediaItem = MediaItem.fromUri(currentStation.url)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
            isPlaying = true
        } catch (e: Exception) {
            isPlaying = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Smart Radio",
            color = Color(0xFF00E5FF),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(currentStation.name, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(if (isPlaying) "라디오 연결 중 🎵" else "대기 중...", color = Color.LightGray, fontSize = 16.sp)
                }
                
                IconButton(
                    onClick = { 
                        val index = stations.indexOf(currentStation)
                        stations[index] = currentStation.copy(isFavorite = !currentStation.isFavorite)
                        currentStation = stations[index]
                    },
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (currentStation.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (currentStation.isFavorite) Color.Red else Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable { showLyricsDialog = true }
                .padding(16.dp)
        ) {
            Text("현재 재생중인 곡", color = Color(0xFFAA00FF), fontSize = 14.sp)
            Text("음악 정보 수신 중...", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("(터치하여 가사 보기)", color = Color.Gray, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { isSearching = !isSearching },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.2f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = if (isSearching) "주변 음악 듣는 중..." else "운전 중 음악 검색 (터치)",
                color = Color.Black,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(stations) { station ->
                Button(
                    onClick = { currentStation = station },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (station == currentStation) Color(0xFFAA00FF) else Color(0xFF2C2C2C)
                    )
                ) {
                    Text(station.name, color = Color.White)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showLyricsDialog) {
        Dialog(onDismissRequest = { showLyricsDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("가사 검색 결과", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "현재 재생 중인 곡의 가사를 불러옵니다.
(API 연동 시 실제 가사 표시)",
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { showLyricsDialog = false }, modifier = Modifier.align(Alignment.End)) {
                        Text("닫기")
                    }
                }
            }
        }
    }
}
