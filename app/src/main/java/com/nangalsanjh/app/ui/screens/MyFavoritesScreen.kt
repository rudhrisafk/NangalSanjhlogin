// MyFavoritesScreen.kt — NEW FILE

package com.nangalsanjh.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nangalsanjh.app.viewmodel.FavoriteViewModel

@Composable
fun MyFavoritesScreen(onBack: () -> Unit) {
    val viewModel: FavoriteViewModel = viewModel()
    val favorites by viewModel.favorites.collectAsState()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Text("My Favorites", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        if (favorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No saved workers yet", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(favorites) { fav ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(fav.workerName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(fav.category, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Row {
                                IconButton(onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:${fav.phone}") }
                                    context.startActivity(intent)
                                }) { Icon(Icons.Default.Call, contentDescription = "Call") }
                                TextButton(onClick = { viewModel.removeFavorite(fav.workerUid) }) { Text("Remove") }
                            }
                        }
                    }
                }
            }
        }
    }
}