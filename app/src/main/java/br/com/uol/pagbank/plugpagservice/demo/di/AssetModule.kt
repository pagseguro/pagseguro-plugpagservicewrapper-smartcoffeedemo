package br.com.uol.pagbank.plugpagservice.demo.di

import android.content.Context
import org.koin.dsl.module

val assetModule = module {
    single(createdAtStart = true) { (get() as Context).assets }
}
