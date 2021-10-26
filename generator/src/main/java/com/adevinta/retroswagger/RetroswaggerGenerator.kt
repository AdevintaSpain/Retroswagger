package com.adevinta.retroswagger

import com.google.auto.service.AutoService
import com.adevinta.retroswagger.annotation.Retroswagger
import com.adevinta.retroswagger.lib.RetroswaggerApiBuilder
import com.adevinta.retroswagger.lib.RetroswaggerApiConfiguration
import com.adevinta.retroswagger.lib.RetroswaggerErrorTracking
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

private const val DEFAULT_FILE_CACHE_POLICY_IN_DAYS = 5

@AutoService(Processor::class)
class RetroswaggerGenerator : AbstractProcessor() {
    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Retroswagger::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun process(set: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val startTime = System.currentTimeMillis()
        roundEnv.getElementsAnnotatedWith(Retroswagger::class.java)
            .forEach {
                val className = it.simpleName.toString()
                val pack = processingEnv.elementUtils.getPackageOf(it).toString()

                // We need to be able to get properties like name, fields etc from the variable which can be done if it's converted to an Element type
                val variableAsElement = processingEnv.typeUtils.asElement(it.asType())
                val swaggerUrl = variableAsElement.getAnnotation(Retroswagger::class.java).swaggerUrl
                val apiInterfaceName = variableAsElement.getAnnotation(Retroswagger::class.java).apiInterfaceName
                val customCachePolicyInDays = variableAsElement.getAnnotation(Retroswagger::class.java).customCachePolicyInDays
                val overrideInterfaceSlash = variableAsElement.getAnnotation(Retroswagger::class.java).overrideInterfaceSlash

                saveSwaggerJsonIntoFile(apiInterfaceName, swaggerUrl, customCachePolicyInDays)

                processingEnv.messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "*** Processing: $className")
                processKotlin(pack, apiInterfaceName, className, overrideInterfaceSlash)
            }
        val endTime = System.currentTimeMillis()
        logElapsedProcessingTime(endTime, startTime)
        return true
    }

    private fun logElapsedProcessingTime(endTime: Long, startTime: Long) {
        val elapsedTimeInMillis = endTime - startTime
        val elapsedTime = SimpleDateFormat("s.SSS", Locale.getDefault()).format(elapsedTimeInMillis)
        processingEnv.messager.printMessage(
            Diagnostic.Kind.MANDATORY_WARNING,
            "*** Processed in $elapsedTime seconds"
        )
    }

    private fun saveSwaggerJsonIntoFile(
        apiInterfaceName: String,
        swaggerUrl: String,
        customCachePolicyInDays: Int
    ) {
        val fileName = "${apiInterfaceName}Swagger.tmp"
        val file = File(fileName)
        if (!file.exists()) {
            processingEnv.messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "*** DOWNLOADING SWAGGER")
            val inputStream = downloadSwagger(swaggerUrl)
            processingEnv.messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "*** CREATING FILE")
            createCacheFile(fileName, inputStream)
            processingEnv.messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "*** FILE CREATED")
        } else {
            processingEnv.messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "*** FILE EXISTS")

            val fileCachedInDays = getTimeCachedInDays(file)

            processingEnv.messager.printMessage(
                Diagnostic.Kind.MANDATORY_WARNING,
                "*** FILE TIME CACHED IN DAYS: $fileCachedInDays"
            )

            val cachePolicyInDays = if (customCachePolicyInDays > 0) {
                customCachePolicyInDays
            } else {
                DEFAULT_FILE_CACHE_POLICY_IN_DAYS
            }

            if (fileCachedInDays > cachePolicyInDays) {
                file.delete()
                processingEnv.messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "*** DOWNLOADING SWAGGER")
                val inputStream = downloadSwagger(swaggerUrl)
                processingEnv.messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "*** CREATING FILE")
                createCacheFile(fileName, inputStream)
                processingEnv.messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "*** FILE CREATED")
            } else {
                processingEnv.messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "*** FILE UNDER CACHE POLICY")
            }
        }
    }

    private fun createCacheFile(fileName: String, inputStream: InputStream?) {
        val outputStream = FileOutputStream(fileName, true)

        inputStream.use { input ->
            outputStream.use { output ->
                input?.copyTo(output)
            }
        }

        outputStream.flush()
        outputStream.close()
        inputStream?.close()
    }

    private fun getTimeCachedInDays(file: File): Int {
        val simpleDateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val lastFileModification = simpleDateFormat.format(file.lastModified()).toInt()
        val currentDay = simpleDateFormat.format(System.currentTimeMillis()).toInt()
        return currentDay - lastFileModification
    }

    private fun downloadSwagger(swaggerUrl: String): InputStream? {
        val request = Request.Builder().url(swaggerUrl).build()
        val response = OkHttpClient().newCall(request).execute()
        return response.body?.byteStream()
    }

    private fun processKotlin(
        pack: String,
        apiInterfaceName: String,
        className: String,
        overrideInterfaceSlash: Boolean
    ) {
        val configuration = RetroswaggerApiConfiguration(
            "http://myendpoint.com",
            "",
            pack,
            apiInterfaceName,
            className,
            "${apiInterfaceName}Swagger.tmp",
            overrideInterfaceSlash
        )

        val kotlinApiBuilder = RetroswaggerApiBuilder(
            configuration,
            DummyRetroswaggerErrorTracking()
        )
        kotlinApiBuilder.build()

        generateClass(className, pack, kotlinApiBuilder.getGeneratedApiInterfaceTypeSpec())
        for (typeSpec in kotlinApiBuilder.getGeneratedModelListTypeSpec()) {
            generateClass(className, pack, typeSpec)
        }
        for (typeSpec in kotlinApiBuilder.getGeneratedEnumListTypeSpec()) {
            generateClass(className, pack, typeSpec)
        }
    }

    private fun generateClass(
        className: String,
        pack: String,
        generatedTypeSpec: TypeSpec
    ) {
        val fileName = "Generated_$className"
        val file = FileSpec.builder(pack, generatedTypeSpec.name!!).addType(generatedTypeSpec).build()

        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        file.writeTo(File(kaptKotlinGeneratedDir, "$fileName.kt"))
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    class DummyRetroswaggerErrorTracking : RetroswaggerErrorTracking {
        override fun logException(throwable: Throwable) {
        }
    }
}
