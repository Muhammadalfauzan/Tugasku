# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
#keep,allowobfuscation,allowshrinking class kotlinx.coroutines { *; }
#dontwarn org.jetbrains.kotlinx.**
#keepclassmembernames class kotlinx.** {
#   volatile <fields>;
#}

#keep class kotlinx.coroutines.android.AndroidExceptionPreHandler
#keep class kotlinx.coroutines.android.AndroidDispatcherFactory


-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep class com.example.ecommerce.data.apimodel.DataCategory { *; }
-keep class com.example.ecommerce.data.apimodel.Product { *; }

-keep class com.facebook.android.*
-keep class android.webkit.WebViewClient
-keep class * extends android.webkit.WebViewClient
-keepclassmembers class * extends android.webkit.WebViewClient {
    <methods>;}

-keep,includedescriptorclasses class net.sqlcipher.** { * ;}
-keep,includedescriptorclasses interface net.sqlcipher.** { *; }

-keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.
-keep public class * extends java.lang.Excep
