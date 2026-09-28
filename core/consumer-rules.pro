# Regras Proguard para o modulo :core do OmniBackend
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

-keep class br.wgc.omnibackend.core.model.** { *; }
-keep class br.wgc.omnibackend.core.utils.** { *; }
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
