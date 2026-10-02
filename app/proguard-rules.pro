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

# Official TDLib Java/JNI bindings use class and method names during JNI registration.
# Keep the complete generated API unchanged in minified builds.
-keep class org.drinkless.tdlib.** { *; }
-keepnames class org.drinkless.tdlib.**
-keepclassmembers class org.drinkless.tdlib.** {
    native <methods>;
}
-keepattributes *Annotation*, InnerClasses, EnclosingMethod
