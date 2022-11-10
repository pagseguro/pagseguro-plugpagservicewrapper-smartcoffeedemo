package br.com.uol.pagseguro.smartcoffee.payments.preauto;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;

public interface DismissListenerEffectivate {
    void onDismissEffectivate(String effectuatedValue, PlugPagTransactionResult plugPagTransactionResult);
}
