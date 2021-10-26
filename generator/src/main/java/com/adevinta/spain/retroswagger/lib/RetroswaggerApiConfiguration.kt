package com.adevinta.spain.retroswagger.lib

data class RetroswaggerApiConfiguration(
    override val serviceEndpoint: String,
    override val swaggerUrl: String,
    override val packageName: String,
    override val componentName: String,
    override val moduleName: String,
    override val swaggerFile: String,
    override val overrideInterfaceSlash: Boolean
) : RetroswaggerConfiguration

interface RetroswaggerConfiguration {
    val serviceEndpoint: String
    val swaggerUrl: String
    val packageName: String
    val componentName: String
    val moduleName: String
    val swaggerFile: String
    val overrideInterfaceSlash: Boolean
}
