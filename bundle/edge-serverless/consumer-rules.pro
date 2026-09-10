# Regras Proguard para o modulo :core do OmniBackend
-keep class br.wgc.omnibackend.core.model.** { *; }
-keep class br.wgc.omnibackend.core.utils.** { *; }
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
