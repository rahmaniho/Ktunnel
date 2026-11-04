# The WireGuard tunnel library is reached through JNI and reflection-free
# callbacks; keep its backend entry points.
-keep class com.wireguard.android.backend.** { *; }
-keep class com.wireguard.config.** { *; }
-keep class com.wireguard.crypto.** { *; }

# kotlinx.serialization generated serializers
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class ir.karensoft.karentunnel.**$$serializer { *; }
-keepclasseswithmembers class ir.karensoft.karentunnel.** {
    kotlinx.serialization.KSerializer serializer(...);
}
