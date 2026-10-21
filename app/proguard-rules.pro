# Media3 ProGuard rules
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Room Database ProGuard rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Data Models
-keep class com.hughdevelopment.player.data.model.** { *; }
-keep class com.hughdevelopment.player.data.db.** { *; }
