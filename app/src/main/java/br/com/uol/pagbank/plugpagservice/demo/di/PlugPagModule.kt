package br.com.uol.pagbank.plugpagservice.demo.di

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag
import org.koin.dsl.module

val plugpagModule = module {
    single(createdAtStart = true) { PlugPag(get()) }
}
