# Add project specific ProGuard rules here.
# Minification is disabled for this prototype (see app/build.gradle.kts),
# so these rules are not currently exercised - kept here ready for the
# final PoE release build.

-keepattributes Signature
-keepattributes *Annotation*

# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keepattributes Exceptions

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Gson models used as Retrofit DTOs
-keep class com.vetqueue.app.data.remote.dto.** { *; }
