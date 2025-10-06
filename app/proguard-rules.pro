# ===================================
# H PDF Creator - ProGuard Rules
# ===================================

# Keep source file and line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep debug information
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exception

# ===================================
# iText PDF Library
# ===================================
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**
-keepclassmembers class com.itextpdf.** { *; }

# Keep PDF font and encoding classes
-keep class com.itextpdf.io.font.** { *; }
-keep class com.itextpdf.kernel.font.** { *; }
-keep class com.itextpdf.kernel.pdf.** { *; }

# ===================================
# Jetpack Compose
# ===================================
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Compose runtime classes
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }

# Keep composable functions
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# ===================================
# Kotlin Coroutines
# ===================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ===================================
# Kotlin Serialization
# ===================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Serializable classes
-keep class * implements java.io.Serializable {
    *;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ===================================
# AndroidX & Material Components
# ===================================
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# ===================================
# Activity & Fragment
# ===================================
-keep class * extends android.app.Activity
-keep class * extends androidx.fragment.app.Fragment
-keep class * extends androidx.activity.ComponentActivity

# ===================================
# Coil Image Loading
# ===================================
-keep class coil.** { *; }
-dontwarn coil.**

# ===================================
# Application Classes
# ===================================
# Keep all application activities
-keep class com.tsavvy.pdfcreator.** { *; }

# Keep all data classes
-keep class com.tsavvy.pdfcreator.**$* { *; }

# Keep ViewModel classes
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(...);
}

# ===================================
# Parcelable & Bundle
# ===================================
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# ===================================
# Reflection
# ===================================
-keepattributes InnerClasses
-keep class **.R$* { *; }

# ===================================
# Native Methods
# ===================================
-keepclasseswithmembernames class * {
    native <methods>;
}

# ===================================
# Enums
# ===================================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ===================================
# Remove Logging (optional, uncomment for production)
# ===================================
# -assumenosideeffects class android.util.Log {
#     public static *** d(...);
#     public static *** v(...);
#     public static *** i(...);
# }

# ===================================
# Optimization
# ===================================
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# ===================================
# Warnings to ignore
# ===================================
-dontwarn org.bouncycastle.**
-dontwarn org.slf4j.**
-dontwarn javax.xml.**
-dontwarn org.apache.**