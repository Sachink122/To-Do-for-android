# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

#-------------------------------------------
# General Android optimizations
#-------------------------------------------
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

# Keep line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

#-------------------------------------------
# AndroidX and Support Libraries
#-------------------------------------------
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

#-------------------------------------------
# Room Database
#-------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-dontwarn androidx.room.paging.**

# Keep Room schema location information
-keepattributes *Annotation*

#-------------------------------------------
# Hilt Dependency Injection
#-------------------------------------------
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-dontwarn dagger.hilt.**

# Keep Hilt generated components
-keep class **_HiltModules { *; }
-keep class **_HiltComponents { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }

#-------------------------------------------
# Gson JSON parsing
#-------------------------------------------
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }

# Keep model classes for Gson serialization
-keep class com.todoapp.data.model.** { *; }
-keep class com.todoapp.domain.model.** { *; }

# Prevent obfuscation of model fields
-keepclassmembers class com.todoapp.data.model.** { *; }
-keepclassmembers class com.todoapp.domain.model.** { *; }

# Keep generic signature
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

#-------------------------------------------
# Google Play Services
#-------------------------------------------
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

#-------------------------------------------
# Glide Image Loading
#-------------------------------------------
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

#-------------------------------------------
# RxJava
#-------------------------------------------
-dontwarn java.util.concurrent.Flow*

#-------------------------------------------
# Keep Application class
#-------------------------------------------
-keep class com.todoapp.TodoApplication { *; }

#-------------------------------------------
# Keep custom view classes
#-------------------------------------------
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

#-------------------------------------------
# Keep Parcelable implementations
#-------------------------------------------
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

#-------------------------------------------
# Keep Serializable classes
#-------------------------------------------
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

#-------------------------------------------
# Keep View Binding and Data Binding classes
#-------------------------------------------
-keep class * extends androidx.viewbinding.ViewBinding { *; }
-keep class * extends androidx.databinding.ViewDataBinding { *; }

#-------------------------------------------
# Navigation Component
#-------------------------------------------
-keepnames class androidx.navigation.fragment.NavHostFragment
-keep class * implements androidx.navigation.Navigator

#-------------------------------------------
# Remove verbose logging in release
#-------------------------------------------
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
