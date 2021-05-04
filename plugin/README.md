# Qt Build Plugin

## Publishing

Gradle allows to define custom plugins that can be published to the Gradle repository. For testing usually we publish only to the local maven repository.

~~~
../gradlew clean publishToMavenLocal
	OR
../gradlew clean publishPlugins
~~~



## Configure

The plugin implements the TOL build management C++ sources, based on the Qt framework.

~~~
plugins {
  id "it.smartio.build" version "0.7.20"
}


smartIO {
  qtRoot       = '/data/Software/Qt'
  qtVersion    = '6.2.1'
  qtConfig     = ['release', 'qtquickcompiler']

  git {
    remote     = 'http://git.tol.info/smartio/smartio.git'
    username   = '****'
    password   = '****'
    branch     = 'develop'
  }

  androidAbis          = ['armeabi-v7a', 'arm64-v8a']
  androidSdkRoot       = '/data/Software/android'
  androidNdkRoot       = '/data/Software/android/ndk/21.3.6528147'
  androidNdkPlatform   = 'android-21'

  vcvarsall    = 'C:/Program Files (x86)/Microsoft Visual Studio/2019/BuildTools/VC/Auxiliary/Build'

  android {
    id           = 'info.tol.fm'
    manifest     = 'android-build/AndroidManifest.xml'
    keystore     = 'android-build/android.keystore'
    alias        = '*******'
  }

  ios {
    id           = '*******'
    type         = 'Release'
    team         = '*******'
    export       = 'platform/ios/exportOptions.plist'

//    type         = 'AppStoreDistribution'
//    export       = 'platform/ios/exportOptionsAppStore.plist'
  }
}
~~~
