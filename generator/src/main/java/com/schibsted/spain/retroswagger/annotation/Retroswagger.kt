package com.schibsted.spain.retroswagger.annotation

annotation class Retroswagger(
    val swaggerUrl: String,
    val apiInterfaceName: String,
    val customCachePolicyInDays: Int = 0,
    val overrideInterfaceSlash: Boolean = false
)