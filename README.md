# Gradle & Wrapper

Gradle is a build management written in Java that provides the Gradle Wrapper. The Wrapper is a script that invokes a declared version of Gradle, downloading it beforehand if necessary. As a result, developers can get up and running with a Gradle project quickly without having to follow manual installation processes saving your company time and money. The wrapper provides following structure:

- *gradle* - The gradle wrapper initializer that fetches the version from gradle distribuition. 
- *gradlew* - The gradle wrapper script for Linux to loading the gradle dependencies 
- *gradlew.bat* - The gradle wrapper script for Windows to loading the gradle dependencies 

A gradle project needs only 1 (often 2 files):

- *build.gradle* - The project configuration
- *settings.gradle* - The global settings


Gradle allows to define custom plugins that can be published to the Gradle repository. For testing usually we publish only to the local maven repository.

~~~
./gradlew clean publishToMavenLocal
	OR
./gradlew clean publishPlugins
~~~
