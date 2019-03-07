<!--
  Title: Retroswagger - Kotlin code generator for Retrofit2 and RxJava2 based on Swagger
  Description: Retroswagger is a library that generates kotlin code for Retrofit 2 based on a Swagger endpoint. Includes an Annotation Processor to export and write the code.
  Author: SchibstedSpain
  -->

<img src="https://github.com/SchibstedSpain/Retroswagger/raw/master/media/retroswagger_logo.png" align="left" height="128px" />
<img align="left" width="0" height="128px" hspace="10" />

<div style="display:block; height: 168px;">
<i>A library that generates kotlin code for Retrofit 2 based on a Swagger endpoint; includes an Annotation Processor to generate and export the code</i></div>

<br/><br/>
<p align="center">
<b><a href="#features">Features</a></b>
|
<b><a href="#download">Download</a></b>
|
<b><a href="#who-made-this">Who Made This</a></b>
|
<b><a href="#contribute">Contribute</a></b>
|
<b><a href="#bugs-and-feedback">Bugs and Feedback</a></b>
|
<b><a href="#license">License</a></b>
</p>
<br/>


### Features

* Easy interface
* Model and DataSource auto-generated
* Swagger v2 integration: create Data Sources based on swagger documentation

### Download

It requires kapt in order to make it run. You need to add this line on the build.gradle of the module where you want use it.

```groovy
apply plugin: "kotlin-kapt"
```

```groovy
kapt "com.schibsted.spain:retroswagger:1.0.0"
annotationProcessor "com.schibsted.spain:retroswagger:1.0.0"
implementation "com.schibsted.spain:retroswagger:1.0.0"
```

In order for your project recognizes the generated code you will need to include it into your sources path, like this:

```groovy
android {
  ...

  sourceSets {
    main.java.srcDirs += "${buildDir.absolutePath}/tmp/kapt3/incrementalData/debug/path/to/your/generated/code"
  }

  ...
}
```

### How to use it

You can use your own file creator or use the annotation comes with the library that generates the files for you.
On the class you want to include the generated code you need to add this annotation "@Retroswagger()".

```kotlin
@Retroswagger(
    "Swagger URL",
    "Name of the api interface you want to have",
    "Cache policy in Days",
    "Remove trailing slash on the interface methods")
class MyClass() {
}
```

Trailing slash example:

- option to "true": `@GET("pet/findByStatus")`
- option to "false": `@GET("/pet/findByStatus")`

Example:

```kotlin
@Retroswagger("http://petstore.swagger.io/v2/swagger.json", "PetStore", 1, true)
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
```

You need to take into account that this will generate all the files on the same package you include this annotation.
For example, if you include it on the class "MyActivity" that is on the package "com.company.mymodule.myfeature.views" it will assign the package for all the generated files to this package.

### Android

If you are using it on an Android project and you use Proguard, you will need to add this proguard rules:

```kotlin
-dontwarn org.yaml.snakeyaml.**
-dontwarn org.slf4j.**
-dontwarn io.swagger.**
-dontwarn com.google.common.**
-dontwarn com.google.auto.**
-dontwarn javax.xml.datatype.**
-dontwarn com.fasterxml.jackson.**
-dontwarn com.squareup.kotlinpoet.**
-dontwarn com.schibsted.spain.**
```

Beware, if you are using one of these package maybe one or more of these rule will not be needed.
Also, it's possible that you will need to add more rules.

If you are using using a test framework it will be needed to add a explicit dependency in order to use it on your tests

```groovy
androidTestAnnotationProcessor "com.schibsted.spain:retroswagger:1.0.0"
```


Who made this
--------------

| <a href="https://github.com/ferranpons"><img src="https://avatars2.githubusercontent.com/u/1225463?v=3&s=460" alt="Ferran Pons" align="left" height="100" width="100" /></a>
|---
| [Ferran Pons](https://github.com/ferranpons)


Contribute
----------

1. Create an issue to discuss about your idea
2. [Fork it] (https://github.com/SchibstedSpain/retroswagger/fork)
3. Create your feature branch (`git checkout -b my-new-feature`)
4. Commit your changes (`git commit -am 'Add some feature'`)
5. Push to the branch (`git push origin my-new-feature`)
6. Create a new Pull Request
7. Profit! :white_check_mark:


Bugs and Feedback
-----------------

For bugs, questions and discussions please use the [Github Issues](https://github.com/SchibstedSpain/retroswagger/issues).


License
-------

Copyright 2019 Schibsted Classified Media Spain S.L.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
