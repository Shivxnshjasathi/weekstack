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

# Jetpack Compose Stability & Metadata
# Prevents R8 from stripping the information Compose needs to track state
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature, SourceFile, LineNumberTable
-keepattributes RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations, RuntimeVisibleParameterAnnotations, RuntimeInvisibleParameterAnnotations
-keep class kotlin.Metadata { *; }

# Prevent R8 from stripping/mangling Compose-generated parameters (like $composer)
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# Keep the Snapshot system and state observation classes intact
-keep class androidx.compose.runtime.ParcelableSnapshotMutationPolicy { *; }
-keep class androidx.compose.runtime.SnapshotMutationPolicy { *; }
-keep class androidx.compose.runtime.snapshots.SnapshotKt { *; }

# Domain Models & Data Class Equality
# This ensures that 'Task.equals()' works perfectly for Strong Skipping
-keep class com.zincstate.hepta.domain.model.** { *; }
-keepclassmembers class com.zincstate.hepta.domain.model.** {
    <fields>;
    boolean equals(java.lang.Object);
    int hashCode();
    java.lang.String toString();
}

# Gson generic rules
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# UI State & Compose Stability (Prevents recomposition skipping in release builds)
-keep class com.zincstate.hepta.presentation.** { *; }
