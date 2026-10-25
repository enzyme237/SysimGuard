package io.github.enzyme237.sysimguard

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
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

// La fiche que Sysim Guard remplit pour chaque application (étape 2)
data class AppInfo(
    val name: String,
    val packageName: String,
    val isSystem: Boolean,
    val nbPermissionsDemandees: Int,
    val permissionsConnues: List<String>
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val permissionsDuModele = chargerPermissionsDuModele()
        val apps = listInstalledApps(permissionsDuModele)
        setContent {
            SysimGuardTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppList(
                        apps = apps,
                        nbPermissionsModele = permissionsDuModele.size,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    // Les permissions connues du modèle, exportées depuis le notebook KronoDroid
    private fun chargerPermissionsDuModele(): Set<String> =
        assets.open("permissions.txt").bufferedReader().useLines { lignes ->
            lignes.map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        }

    // Les "yeux" de Sysim Guard : applications installées et permissions demandées
    private fun listInstalledApps(permissionsDuModele: Set<String>): List<AppInfo> {
        val pm = packageManager
        return pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            .filter { it.packageName != packageName } // Sysim Guard ne s'analyse pas lui-même
            .mapNotNull { pkg ->
                val info = pkg.applicationInfo ?: return@mapNotNull null
                val demandees = pkg.requestedPermissions?.toList() ?: emptyList()
                val connues = demandees
                    .map { it.substringAfterLast('.') } // "android.permission.READ_SMS" -> "READ_SMS"
                    .filter { it in permissionsDuModele }
                    .distinct()
                AppInfo(
                    name = pm.getApplicationLabel(info).toString(),
                    packageName = pkg.packageName,
                    isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    nbPermissionsDemandees = demandees.size,
                    permissionsConnues = connues
                )
            }
            .sortedWith(compareBy<AppInfo>({ it.isSystem }, { it.name.lowercase() }))
    }
}

@Composable
fun AppList(apps: List<AppInfo>, nbPermissionsModele: Int, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "${apps.size} applications analysées",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$nbPermissionsModele permissions connues du modèle",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        items(apps) { app ->
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(text = app.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = if (app.isSystem) "${app.packageName} · système" else app.packageName,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${app.nbPermissionsDemandees} permissions demandées · " +
                            "${app.permissionsConnues.size} reconnues par le modèle",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            HorizontalDivider()
        }
    }
}