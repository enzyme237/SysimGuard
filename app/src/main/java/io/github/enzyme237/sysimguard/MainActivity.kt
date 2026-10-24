package io.github.enzyme237.sysimguard

import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.enzyme237.sysimguard.ui.theme.SysimGuardTheme

// Ce que Sysim Guard retient de chaque application (étape 1)
data class AppInfo(
    val name: String,
    val packageName: String,
    val isSystem: Boolean
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val apps = listInstalledApps()
        setContent {
            SysimGuardTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppList(apps = apps, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    // Les "yeux" de Sysim Guard : interroger le PackageManager
    private fun listInstalledApps(): List<AppInfo> {
        val pm = packageManager
        return pm.getInstalledApplications(0)
            .map { info ->
                AppInfo(
                    name = pm.getApplicationLabel(info).toString(),
                    packageName = info.packageName,
                    isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }
            .sortedBy { it.name.lowercase() }
    }
}

@Composable
fun AppList(apps: List<AppInfo>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        item {
            Text(
                text = "${apps.size} applications installées",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
        items(apps) { app ->
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(text = app.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = if (app.isSystem) "${app.packageName} · système" else app.packageName,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            HorizontalDivider()
        }
    }
}