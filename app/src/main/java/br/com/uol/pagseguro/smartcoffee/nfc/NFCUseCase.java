package br.com.uol.pagseguro.smartcoffee.nfc;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagNFCResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagNearFieldCardData;
import br.com.uol.pagseguro.plugpagservice.wrapper.exception.PlugPagException;
import io.reactivex.Observable;

public class NFCUseCase {

    private final PlugPag mPlugPag;

    public NFCUseCase(PlugPag plugPag) {
        mPlugPag = plugPag;
    }


    public Observable<PlugPagNFCResult> readNFCCard() {
        return Observable.create(emitter -> {
            PlugPagNearFieldCardData cardData = new PlugPagNearFieldCardData();
            cardData.setStartSlot(28);
            cardData.setEndSlot(28);

            PlugPagNFCResult result = mPlugPag.readFromNFCCard(cardData);

            if (result.getResult() == 1) {
                emitter.onNext(result);
            } else {
                emitter.onError(new PlugPagException());
            }

            emitter.onComplete();
        });
    }

    public Observable<PlugPagNFCResult> writeNFCCard() {
        return Observable.create(emitter -> {
            PlugPagNearFieldCardData cardData = new PlugPagNearFieldCardData();
            cardData.setStartSlot(28);
            cardData.setEndSlot(28);

            String teste = "teste_com16bytes";
            byte[] bytes = teste.getBytes();

            cardData.getSlots()[28].put("data", bytes);

            PlugPagNFCResult result = mPlugPag.writeToNFCCard(cardData);

            if (result.getResult() == 1) {
                emitter.onNext(result);
            } else {
                emitter.onError(new PlugPagException());
            }

            emitter.onComplete();
        });
    }
}
