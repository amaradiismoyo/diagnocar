package com.amaradism.diagnocar.data

sealed class StatusHasil<out R> private constructor() {
    data class Success<out T>(val data: T) : StatusHasil<T>()
    data class Error(val error: String) : StatusHasil<Nothing>()
    data object Loading : StatusHasil<Nothing>()
}
