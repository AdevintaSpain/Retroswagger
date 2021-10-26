package com.adevinta.retroswagger.lib

interface RetroswaggerErrorTracking {
    fun logException(throwable: Throwable)
}
