# Kotlinx serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.campusconnect.app.**$$serializer { *; }
-keepclassmembers class com.campusconnect.app.** { *** Companion; }
-keepclasseswithmembers class com.campusconnect.app.** { kotlinx.serialization.KSerializer serializer(...); }

# Room
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-dontwarn androidx.room.paging.**

# Ktor / OkHttp
-dontwarn org.slf4j.**
-dontwarn kotlinx.coroutines.debug.**

# Google Maps
-keep class com.google.android.gms.maps.** { *; }
