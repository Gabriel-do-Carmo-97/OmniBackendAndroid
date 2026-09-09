# Regras de Proguard / R8 preservando modelos do backend-firebase
-keep class br.wgc.omnibackend.firebase.data.model.** { *; }
-keep class br.wgc.omnibackend.firebase.domain.model.** { *; }
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
