package de.mm20.launcher2.plugin.onedrive

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import org.koin.android.ext.android.inject

class SettingsActivity : AppCompatActivity() {

    private val client: MicrosoftApiClient by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val darkMode = isSystemInDarkTheme()
            val theme = if (darkMode) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    dynamicDarkColorScheme(this)
                } else {
                    darkColorScheme()
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    dynamicLightColorScheme(this)
                } else {
                    lightColorScheme()
                }
            }
            MaterialTheme(theme) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val account by client.currentAccount.collectAsStateWithLifecycle(null)

                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .statusBarsPadding()
                    )
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.5f),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.extraLarge.copy(
                            bottomEnd = CornerSize(0),
                            bottomStart = CornerSize(0)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .navigationBarsPadding()
                        ) {
                            when (account) {
                                is MicrosoftAccount -> LogoutScreen(account as MicrosoftAccount)
                                is NoAccount -> LoginScreen()
                                else -> {

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun LogoutScreen(account: MicrosoftAccount) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineSmall
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (account.photo != null) {
                    AsyncImage(
                        account.photo,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .size(64.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        Icons.Rounded.Person, null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .size(64.dp)
                            .padding(16.dp)
                    )
                }
                if (account.displayName != null) {
                    Text(
                        account.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                if (account.username != null) {
                    Text(
                        account.username,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Button(
                    modifier = Modifier.padding(top = 24.dp),
                    onClick = { client.logout() },
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.Logout,
                        null,
                        modifier = Modifier
                            .padding(end = ButtonDefaults.IconSpacing)
                            .size(ButtonDefaults.IconSize),
                    )
                    Text(stringResource(R.string.sign_out_button))
                }
            }
        }
    }

    @Composable
    fun LoginScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineSmall
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SigninButton(
                    modifier = Modifier,
                    onClick = {
                        client.login(this@SettingsActivity)
                    }
                )
            }
        }
    }

    @Composable
    fun SigninButton(
        modifier: Modifier = Modifier,
        onClick: () -> Unit
    ) {
        val darkTheme = !isSystemInDarkTheme()
        Button(
            modifier = modifier,
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (darkTheme) Color(0xFF2f2f2f) else Color.White,
                contentColor = if (darkTheme) Color.White else Color(0xFF5e5e5e),
            ),
            shape = RectangleShape,
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        ) {
            Image(
                painterResource(R.drawable.ic_microsoft),
                null,
                modifier = Modifier
                    .padding(end = ButtonDefaults.IconSpacing)
                    .size(ButtonDefaults.IconSize),
            )
            Text(
                stringResource(R.string.sign_in_button),
            )
        }
    }
}