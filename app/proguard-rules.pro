# Media3 ProGuard rules
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Room Database ProGuard rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Data Models & Room DAOs
-keep class com.hughdevelopment.player.data.model.** { *; }
-keep class com.hughdevelopment.player.data.db.** { *; }
-keep class com.hughdevelopment.player.data.download.** { *; }

# NewPipe Extractor rules
-keep class org.schabi.newpipe.extractor.** { *; }
-dontwarn org.schabi.newpipe.extractor.**
-dontwarn javax.annotation.**
-dontwarn org.mozilla.javascript.**

# OkHttp rules
-dontwarn okhttp3.**
-dontwarn okio.**
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
