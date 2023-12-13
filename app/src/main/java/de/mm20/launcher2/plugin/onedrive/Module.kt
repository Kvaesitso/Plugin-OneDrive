package de.mm20.launcher2.plugin.onedrive

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val module = module {
    single { MicrosoftApiClient(androidContext()) }
}