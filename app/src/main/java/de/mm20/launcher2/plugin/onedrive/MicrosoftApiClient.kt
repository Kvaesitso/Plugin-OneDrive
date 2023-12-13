package de.mm20.launcher2.plugin.onedrive

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.datastore.dataStore
import com.azure.core.credential.AccessToken
import com.microsoft.graph.authentication.TokenCredentialAuthProvider
import com.microsoft.graph.models.DriveItem
import com.microsoft.graph.models.DriveSearchParameterSet
import com.microsoft.graph.requests.GraphServiceClient
import com.microsoft.identity.client.AcquireTokenSilentParameters
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.IPublicClientApplication.ISingleAccountApplicationCreatedListener
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.ISingleAccountPublicClientApplication.SignOutCallback
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.SignInParameters
import com.microsoft.identity.client.SilentAuthenticationCallback
import com.microsoft.identity.client.exception.MsalException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import reactor.core.publisher.Mono
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MicrosoftApiClient(
    private val context: Context,
) {
    private lateinit var clientApplication: ISingleAccountPublicClientApplication
    private var accessToken: String? = null

    private val Context.dataStore by dataStore<Account>("account.json", AccountSerializer)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val client: GraphServiceClient<Request> = GraphServiceClient
        .builder()
        .authenticationProvider(TokenCredentialAuthProvider {
            Mono.just(AccessToken(accessToken, null))
        })
        .buildClient()

    private suspend fun getClientApplication(): ISingleAccountPublicClientApplication {
        if (!this::clientApplication.isInitialized) {
            val application = suspendCoroutine {
                PublicClientApplication
                    .createSingleAccountPublicClientApplication(
                        context.applicationContext,
                        R.raw.msal_auth_config,
                        object : ISingleAccountApplicationCreatedListener {
                            override fun onCreated(application: ISingleAccountPublicClientApplication) {
                                it.resume(application)
                            }

                            override fun onError(exception: MsalException) {
                                it.resumeWithException(exception)
                            }
                        }
                    )
            }
            clientApplication = application
        }
        return clientApplication
    }


    fun login(activity: Activity) {
        scope.launch {
            val application = getClientApplication()
            val authResult = suspendCoroutine {
                application.signIn(
                    SignInParameters
                        .builder()
                        .withActivity(activity)
                        .withScopes(SCOPES)
                        .withCallback(object : AuthenticationCallback {
                            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                                it.resume(authenticationResult)
                            }

                            override fun onError(exception: MsalException) {
                                it.resumeWithException(exception)
                            }

                            override fun onCancel() {
                                it.resume(null)
                            }
                        })
                        .build()
                )
            }
            if (authResult != null) {
                context.dataStore.updateData {
                    MicrosoftAccount(
                        username = authResult.account.username,
                    )
                }
                accessToken = authResult.accessToken
                refreshProfile()
            }
        }
    }

    private suspend fun refreshProfile() {
        refreshToken()
        val user = withContext(Dispatchers.IO) {
            try {
                client.me().buildRequest().get()
            } catch (e: Exception) {
                Log.e("OneDrivePlugin", "Failed to load profile", e)
                null
            }
        }
        val photo = withContext(Dispatchers.IO) {
            try {
                client.me().photos("96x96").content().buildRequest().get()?.use {
                    it.readBytes()
                }
            } catch (e: Exception) {
                Log.e("OneDrivePlugin", "Failed to load profile photo", e)
                null
            }
        }
        if (user != null) {
            context.dataStore.updateData {
                if (it is MicrosoftAccount) {
                    it.copy(
                        displayName = user.displayName,
                        photo = photo,
                    )
                } else {
                    NoAccount
                }
            }
        }
    }

    private suspend fun refreshToken(): Boolean {
        try {
            val application = getClientApplication()
            val authority = application.configuration.defaultAuthority.authorityURL.toString()
            val account = application.currentAccount?.currentAccount ?: return false
            val result = suspendCoroutine {
                application.acquireTokenSilentAsync(
                    AcquireTokenSilentParameters.Builder()
                        .fromAuthority(authority)
                        .forAccount(account)
                        .withScopes(SCOPES)
                        .withCallback(object : SilentAuthenticationCallback {
                            override fun onSuccess(authenticationResult: IAuthenticationResult?) {
                                it.resume(authenticationResult)
                            }

                            override fun onError(exception: MsalException) {
                                it.resumeWithException(exception)
                            }
                        })
                        .build()
                )
            }
            if (result != null) {
                accessToken = result.accessToken
                return true
            }
            return false
        } catch (e: MsalException) {
            Log.e("OneDrivePlugin", "Failed to refresh token", e)
            return false
        }
    }


    fun logout() {
        scope.launch {
            val application = getClientApplication()
            suspendCoroutine {
                application.signOut(object : SignOutCallback {
                    override fun onSignOut() {
                        it.resume(Unit)
                    }

                    override fun onError(exception: MsalException) {
                        it.resumeWithException(exception)
                    }
                })
            }
            context.dataStore.updateData {
                NoAccount
            }
        }
    }

    val currentAccount = context.dataStore.data

    suspend fun queryOneDriveFiles(query: String): List<DriveItem>? {
        refreshToken()
        return withContext(Dispatchers.IO) {
            client.me()
                .drive()
                .search(
                    DriveSearchParameterSet.newBuilder()
                        .withQ(query)
                        .build()
                )
                .buildRequest()
                .select("id,name,file,folder,size,video,image,webUrl,shared,createdBy,parentReference")
                .top(10)
                .get()
                ?.currentPage
        }
    }

    companion object {
        private val SCOPES = listOf(
            "User.Read",
            "Files.Read.All"
        )
    }
}