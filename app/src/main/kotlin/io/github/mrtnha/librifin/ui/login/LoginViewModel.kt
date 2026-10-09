package io.github.mrtnha.librifin.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.mrtnha.librifin.api.JellyfinClient
import io.github.mrtnha.librifin.api.Server
import io.github.mrtnha.librifin.api.Session
import io.github.mrtnha.librifin.api.clientErrorStatus
import io.github.mrtnha.librifin.api.toUserMessage
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LoginViewModel(
    private val server: Server,
    private val jellyfin: JellyfinClient,
    initialUsername: String,
) : ViewModel() {
    var username by mutableStateOf(initialUsername)
    var password by mutableStateOf("")
    var isLoggingIn by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun login(onLoggedIn: (Session) -> Unit) {
        if (isLoggingIn) return
        val name = username.trim()
        if (name.isEmpty()) {
            error = "Please enter a username."
            return
        }
        error = null
        isLoggingIn = true
        viewModelScope.launch {
            try {
                val result = jellyfin.authenticateByName(server.baseUrl, name, password)
                val user = result.user
                val token = result.accessToken
                if (user == null || token == null) {
                    error = "The server sent an unexpected response."
                } else {
                    onLoggedIn(
                        Session(
                            server = server,
                            userId = user.id,
                            userName = user.name ?: name,
                            userImageTag = user.primaryImageTag,
                            accessToken = token,
                        ),
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = when (e.clientErrorStatus) {
                    HttpStatusCode.Unauthorized -> "Wrong username or password."
                    HttpStatusCode.Forbidden -> "This account isn't allowed to sign in."
                    else -> e.toUserMessage()
                }
            } finally {
                isLoggingIn = false
            }
        }
    }
}
