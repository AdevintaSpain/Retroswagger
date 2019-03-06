package com.schibsted.spain.retroswagger.lib

interface RetroswaggerErrorTracking {
    fun logException(throwable: Throwable)
}
