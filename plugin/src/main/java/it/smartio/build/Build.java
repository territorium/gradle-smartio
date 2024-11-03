/**
 *
 */

package it.smartio.build;

/**
 * Defines the environment variables used for the build system.
 */
public interface Build {

  String BUILD_DIR     = "BUILD_DIR";
  String TARGET_DIR    = "TARGET_DIR";
  String ARTIFACTS_DIR = "ARTIFACTS_DIR";


  String PATH_LINUX      = "PATH";
  String PATH_WIN64      = "Path";
  String EXTRA_PATH      = "EXTRA_PATH";
  String LD_LIBRARY_PATH = "LD_LIBRARY_PATH";


  String PLATFORM                  = "PLATFORM";
  String BUILDNUMBER               = "BUILD_NUMBER";

  String QT_ROOT                   = "QT_ROOT";
  String QT_CONFIG                 = "QT_CONFIG";
  String QT_VERSION                = "QT_VERSION";
  String QT_ANDROID_DEPLOY         = "QT_ANDROID_DEPLOY";

  String ANDROID_ABIS              = "ANDROID_ABIS";
  String ANDROID_SDK_ROOT          = "ANDROID_SDK_ROOT";
  String ANDROID_NDK_ROOT          = "ANDROID_NDK_ROOT";
  String ANDROID_JAVA_HOME         = "ANDROID_JAVA_HOME";

  String ANDROID_ID                = "ANDROID_ID";
  String ANDROID_MANIFEST          = "ANDROID_MANIFEST";
  String ANDROID_KEYSTORE          = "ANDROID_KEYSTORE";
  String ANDROID_KEYSTORE_ALIAS    = "ANDROID_KEYSTORE_ALIAS";
  String ANDROID_KEYSTORE_PASSWORD = "ANDROID_KEYSTORE_PASSWORD";

  String IOS_EXPORT_ID             = "IOS_EXPORT_ID";
  String IOS_EXPORT_TYPE           = "IOS_EXPORT_TYPE";
  String IOS_EXPORT_TEAM           = "IOS_EXPORT_TEAM";
  String IOS_EXPORT_PLIST          = "IOS_EXPORT_PLIST";
  String IOS_UPLOAD_API            = "IOS_UPLOAD_API";
  String IOS_UPLOAD_ISSUER         = "IOS_UPLOAD_ISSUER";

  String MSVC_ROOT                 = "MSVC_ROOT";
  String MSVC_VERSION              = "MSVC_VERSION";

  String REVISION                  = "REVISION";
  String PACKAGE_RELEASE           = "RELEASE";
  String PACKAGE_VERSION           = "VERSION";

  String GIT_DATE                  = "GIT_DATE";
  String GIT_HASH                  = "GIT_HASH";
  String GIT_VERSION               = "GIT_VERSION";


  String PRODUCT_NAME     = "PRODUCT_NAME";
  String PRODUCT_FILE     = "PRODUCT_FILE";
  String PRODUCT_RESOURCE = "PRODUCT_RESOURCE";
  String PRODUCT_MODEL    = "PRODUCT_MODEL";
  String PRODUCT_OFFLINE  = "PRODUCT_OFFLINE";


  // deprecated
  String QT_BUILD = "QT_BUILD";
  String QT_ARCH  = "QT_ARCH";
}
