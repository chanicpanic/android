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

# do not remove any game components (for Serialiation and Ability reflection and safety)
-keep public class com.chanicpanic.chanicpanicmobile.game.** { *; }

# protect serialization of lazy properties
-keep class kotlin.Lazy { *; }
-keep class kotlin.LazyKt { *; }
-keep class kotlin.InitializedLazyImpl { *; }
-keep class kotlin.SafePublicationLazyImpl { *; }
-keep class kotlin.UnsafeLazyImpl { *; }
# this is the only one that is necessary at the moment
# keep the others to be safe
-keep class kotlin.SynchronizedLazyImpl { *; }

# for FirebaseCrashlytics.getInstance()
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

#-------------------------------------------------
# JetPack Navigation
# This fixes: Caused by: androidx.fragment.app.Fragment$InstantiationException: Unable to instantiate fragment androidx.navigation.fragment.NavHostFragment: make sure class name exists
#-------------------------------------------------
-keep class androidx.navigation.fragment.NavHostFragment

-keep class com.chanicpanic.chanicpanicmobile.settings.PrivacyPreferenceFragment
-keep class com.chanicpanic.chanicpanicmobile.settings.AboutPreferencesFragment
-keep class com.chanicpanic.chanicpanicmobile.settings.CreditsPreferenceFragment
-keep class com.chanicpanic.chanicpanicmobile.settings.QuickGamePreferenceFragment
