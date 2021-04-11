# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-dontwarn com.android.volley.**
-dontwarn com.google.android.gms.**
-dontwarn java.lang.instrument.*
-dontwarn sun.misc.SignalHandler

-dontobfuscate
-keep class com.facebook.yoga.** { *; }
-keep class com.maubis.scarlet.** { *; }
