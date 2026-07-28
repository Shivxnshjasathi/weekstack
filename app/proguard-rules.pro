# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep class * { @androidx.room.Dao *; }

# Hilt
-keep class * extends android.app.Application
-keep class * extends android.app.Activity
-keep class * extends androidx.fragment.app.Fragment
-keep class * extends androidx.lifecycle.ViewModel
-keep @dagger.hilt.android.lifecycle.HiltViewModel class *

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.android.HandlerContext {
    private final android.os.Handler handler;
}

# General Compose
-keep class androidx.compose.ui.platform.AndroidComposeView { *; }

# Gson & Domain Models (Prevents JSON serialization issues with TypeConverters)
-keep class com.zincstate.hepta.domain.model.** { *; }

# Gson generic rules
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep necessary attributes for Compose and Kotlin reflection (Critical for stability inference)
-keepattributes RuntimeVisible*Annotations,Metadata,Signature,InnerClasses,EnclosingMethod

# Keep data class equality methods
-keepclassmembers class * {
    boolean equals(java.lang.Object);
    int hashCode();
    java.lang.String toString();
}