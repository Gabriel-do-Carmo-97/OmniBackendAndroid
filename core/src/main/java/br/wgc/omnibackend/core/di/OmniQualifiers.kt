package br.wgc.omnibackend.core.di

import javax.inject.Qualifier

/** Qualificador DI para o driver Firebase. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FirebaseBackend

/** Qualificador DI para o driver Supabase. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SupabaseBackend

/** Qualificador DI para o driver Appwrite. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppwriteBackend

/** Qualificador DI para o driver PocketBase. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PocketBaseBackend

/** Qualificador DI para o driver Back4App. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Back4AppBackend

/** Qualificador DI para o driver AWS Amplify. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AmplifyBackend

/** Qualificador DI para o driver Custom REST. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RestBackend

/** Qualificador DI para o driver Cloudflare. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CloudflareBackend

/** Qualificador DI para o driver ativo resolvido dinamicamente em runtime. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ActiveBackend
