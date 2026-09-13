package br.wgc.omnibackend.bundle.all.di

import br.wgc.omnibackend.bundle.all.OmniAll
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt corporativo para o bundle completo (:bundle:all).
 *
 * Provê instâncias master e orquestração dinâmica de todos os 8 provedores de backend
 * (Firebase, Supabase, Appwrite, PocketBase, Back4App, Amplify, REST e Cloudflare).
 */
@Module
@InstallIn(SingletonComponent::class)
object AllBackendsModule {

    /** Constante de qualificação por nome para a orquestração mestre. */
    const val QUALIFIER_MASTER = "all_master"

    /**
     * Provê o [OmniAll] como ponto de acesso único e unificado a todos os drivers.
     */
    @Provides
    @Singleton
    fun provideOmniAll(): OmniAll = OmniAll
}
