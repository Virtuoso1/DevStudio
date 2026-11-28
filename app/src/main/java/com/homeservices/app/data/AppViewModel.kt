package com.homeservices.app.data

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.homeservices.app.data.api.dto.AuthResponse
import com.homeservices.app.data.api.dto.RegisterRequest
import com.homeservices.app.data.repository.HomeServicesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HomeServicesRepository()
    private val preferences = application.getSharedPreferences("session", Context.MODE_PRIVATE)
    private val _session = MutableStateFlow<AuthResponse?>(null)
    val session = _session.asStateFlow()
    private val _authBusy = MutableStateFlow(false)
    val authBusy = _authBusy.asStateFlow()
    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    init {
        preferences.getString("token", null)?.let { token ->
            authenticate {
                AuthResponse(token, repository.me(token))
            }
        }
    }

    fun login(email: String, password: String) =
        authenticate { repository.login(email.trim(), password) }

    fun register(request: RegisterRequest) = authenticate { repository.register(request) }

    fun clearAuthError() { _authError.value = null }

    fun signOut() {
        preferences.edit().remove("token").apply()
        _authError.value = null
        _session.value = null
    }

    fun expireSession() {
        signOut()
        _authError.value = "Your session expired. Sign in again."
    }

    private fun authenticate(action: suspend () -> AuthResponse) {
        if (_authBusy.value) return
        _authBusy.value = true
        _authError.value = null
        viewModelScope.launch {
            try {
                val result = action()
                preferences.edit().putString("token", result.token).apply()
                _session.value = result
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (error is HttpException && error.code() in listOf(401, 404)) {
                    preferences.edit().remove("token").apply()
                }
                _authError.value = apiError(error)
            } finally {
                _authBusy.value = false
            }
        }
    }
}

internal fun apiError(error: Exception): String = when (error) {
    is HttpException -> try {
        val body = JSONObject(error.response()?.errorBody()?.string().orEmpty())
        body.optJSONArray("details")?.optJSONObject(0)?.optString("message")
            ?.takeIf(String::isNotBlank)
            ?: body.optString("error").ifBlank { "Request failed (HTTP ${error.code()})." }
    } catch (_: Exception) {
        "Request failed (HTTP ${error.code()})."
    }
    is IOException -> "Couldn't connect to Home Services. Check that the API is running and try again."
    else -> "Something went wrong. Please try again."
}
