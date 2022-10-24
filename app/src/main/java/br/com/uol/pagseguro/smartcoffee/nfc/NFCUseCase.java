package br.com.uol.pagseguro.smartcoffee.nfc;

import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.APDU_COMMAND_FAIL;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.BEEP_FAIL;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.CARD_NOT_FOUND;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.CARD_NOT_REMOVED;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.LED_FAIL;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.NFC_START_FAIL;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.NO_NEAR_FIELD_FOUND;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.RET_OK;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.RET_WAITING_REMOVE_CARD;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.TEST_16_BYTES;

import java.util.Locale;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagNFCDetectRemoveCard;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagNFCResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagNearFieldCardData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagNearFieldRemoveCardData;
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.EM1KeyType;
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.PlugPagBeepData;
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.PlugPagLedData;
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.PlugPagNFCAuth;
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.PlugPagNFCAuthDirectly;
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.PlugPagSimpleNFCData;
import br.com.uol.pagseguro.plugpagservice.wrapper.data.result.PlugPagCmdExchangeResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.data.result.PlugPagNFCInfosResultDirectly;
import br.com.uol.pagseguro.plugpagservice.wrapper.exception.PlugPagException;
import br.com.uol.pagseguro.smartcoffee.utils.Utils;
import io.reactivex.Completable;
import io.reactivex.Observable;

public class NFCUseCase {

    private final PlugPag mPlugPag;

    public NFCUseCase(PlugPag plugPag) {
        mPlugPag = plugPag;
    }

    /**
     * Chave de autenticacao de um cartao NFC virgem
     */
    private static final byte[] DEFAULT_KEY_NFC = {
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF,
            (byte) 0xFF
    };

    public Observable<PlugPagNFCResult> readNFCCard() {
        return Observable.create(emitter -> {
            PlugPagNearFieldCardData cardData = new PlugPagNearFieldCardData();
            cardData.setStartSlot(28);
            cardData.setEndSlot(28);

            PlugPagNFCResult result = mPlugPag.readFromNFCCard(cardData);

            if (result.getResult() == RET_OK) {
                emitter.onNext(result);
            } else {
                emitter.onError(new PlugPagException(CARD_NOT_FOUND + result.getResult()));
            }

            emitter.onComplete();
        });
    }

    public Observable<PlugPagNFCResult> writeNFCCard() {
        return Observable.create(emitter -> {
            PlugPagNearFieldCardData cardData = new PlugPagNearFieldCardData();
            cardData.setStartSlot(28);
            cardData.setEndSlot(28);

            cardData.getSlots()[28].put("data", Utils.convertString2Bytes(TEST_16_BYTES));

            PlugPagNFCResult result = mPlugPag.writeToNFCCard(cardData);

            if (result.getResult() == RET_OK) {
                emitter.onNext(result);
            } else {
                emitter.onError(new PlugPagException(CARD_NOT_FOUND + result.getResult()));
            }

            emitter.onComplete();
        });
    }

    public Observable<Integer> writeNFCCardBlockB() {
        PlugPagSimpleNFCData cardData = new PlugPagSimpleNFCData(
                PlugPagNearFieldCardData.ONLY_M, 28,
                Utils.convertString2Bytes(TEST_16_BYTES)
        );

        return Observable.create(emitter -> {
            try {
                int resultStartNfc = mPlugPag.startNFCCardDirectly();
                if (resultStartNfc != RET_OK) {
                    emitter.onError(new PlugPagException(NFC_START_FAIL + resultStartNfc));
                    emitter.onComplete();
                    return;
                }

                PlugPagNFCAuth auth = new PlugPagNFCAuth(
                        PlugPagNearFieldCardData.ONLY_M,
                        (byte) cardData.getSlot(),
                        DEFAULT_KEY_NFC,
                        EM1KeyType.TYPE_B
                );
                int resultAuth = mPlugPag.authNFCCardDirectly(auth);

                if (resultAuth != RET_OK) {
                    emitter.onError(new PlugPagException(String.format(
                            Locale.getDefault(),
                            "Erro ao autenticar bloco [ %s ]: %d", cardData.getSlot(), resultAuth)));
                    emitter.onComplete();
                    return;
                }

                int result = mPlugPag.writeToNFCCardDirectly(cardData);

                if (result == RET_OK) {
                    emitter.onNext(result);
                } else {
                    emitter.onError(
                            new PlugPagException(String.format(
                                    Locale.getDefault(),
                                    "Ocorreu um erro ao escrever no bloco [%s]  do cartão nfc: %d", cardData.getSlot(), result))
                    );
                }

                mPlugPag.stopNFCCardDirectly();
            } catch (Exception e) {
                e.printStackTrace();
                emitter.onError(e);
            }

            emitter.onComplete();
        });
    }

    public Observable<PlugPagNFCInfosResultDirectly> detectCardDirectly() {
        return Observable.create(emitter -> {
            try {
                int resultStartNfc = mPlugPag.startNFCCardDirectly();
                if (resultStartNfc != RET_OK) {
                    emitter.onError(new PlugPagException(NFC_START_FAIL + resultStartNfc));
                    emitter.onComplete();
                    return;
                }

                PlugPagNFCInfosResultDirectly plugPagNFCInfosResult = mPlugPag.detectNfcCardDirectly(PlugPagNearFieldCardData.ONLY_M, 20);

                if (plugPagNFCInfosResult.getResult() != RET_OK) {
                    emitter.onError(new PlugPagException(CARD_NOT_FOUND + plugPagNFCInfosResult.getResult()));
                    mPlugPag.stopNFCCardDirectly();
                    emitter.onComplete();
                    return;
                }

                emitter.onNext(plugPagNFCInfosResult);

                mPlugPag.stopNFCCardDirectly();
            } catch (Exception e) {
                e.printStackTrace();
                emitter.onError(new PlugPagException(CARD_NOT_FOUND));
            }

            emitter.onComplete();
        });
    }

    public Observable<Integer> detectRemoveCardDirectly() {
        return Observable.create(emitter -> {
            try {
                int resultStartNfc = mPlugPag.startNFCCardDirectly();
                if (resultStartNfc != RET_OK) {
                    emitter.onError(new PlugPagException(NFC_START_FAIL + resultStartNfc));
                    emitter.onComplete();
                    return;
                }

                PlugPagNFCInfosResultDirectly plugPagNFCInfosResult =
                        mPlugPag.detectNfcCardDirectly(PlugPagNearFieldCardData.ONLY_M, 20);

                if (plugPagNFCInfosResult.getResult() != RET_OK) {
                    emitter.onError(
                            new PlugPagException(CARD_NOT_FOUND + plugPagNFCInfosResult.getResult())
                    );
                    mPlugPag.stopNFCCardDirectly();
                    emitter.onComplete();
                    return;
                }

                emitter.onNext(RET_WAITING_REMOVE_CARD);
                Thread.sleep(5000);
                int result = 0;
                if (plugPagNFCInfosResult.getCid() != null) {
                    final PlugPagNFCDetectRemoveCard plugPagNFCDetectRemoveCard =
                            new PlugPagNFCDetectRemoveCard(
                                    PlugPagNearFieldRemoveCardData.REMOVE,
                                    plugPagNFCInfosResult.getCid()
                            );

                    result = mPlugPag.detectNfcRemoveDirectly(plugPagNFCDetectRemoveCard);
                    if (result != RET_OK) {
                        emitter.onError(new PlugPagException(CARD_NOT_REMOVED + result));
                        mPlugPag.stopNFCCardDirectly();
                        emitter.onComplete();
                        return;
                    }
                }

                emitter.onNext(result);
                mPlugPag.stopNFCCardDirectly();
            } catch (Exception e) {
                e.printStackTrace();
                if (e.getMessage().equals(NO_NEAR_FIELD_FOUND)) {
                    emitter.onError(new PlugPagException(CARD_NOT_FOUND));
                } else {
                    emitter.onError(e);
                }
            }

            emitter.onComplete();
        });
    }

    public Observable<PlugPagCmdExchangeResult> cmdExchange() {
        return Observable.create(emitter -> {
            byte[] command = new byte[]{
                    (byte) 0x00,
                    (byte) 0xa4,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x02,
                    (byte) 0x2f,
                    (byte) 0xf7
            };
            try {
                PlugPagCmdExchangeResult resultAuth = mPlugPag.apduCommand(command, 256);

                if (resultAuth.getCmd() != null && resultAuth.getCmd().length > 0) {
                    emitter.onNext(resultAuth);
                } else {
                    emitter.onError(new PlugPagException(APDU_COMMAND_FAIL + resultAuth));
                }

                emitter.onComplete();
            } catch (Exception e) {
                e.printStackTrace();
                emitter.onError(e);
            }
        });
    }

    public Observable<Integer> detectJustAuthDirectly() {
        return Observable.create(emitter -> {
            try {
                int resultStartNfc = mPlugPag.startNFCCardDirectly();
                if (resultStartNfc != RET_OK) {
                    emitter.onError(new PlugPagException(NFC_START_FAIL + resultStartNfc));
                    mPlugPag.stopNFCCardDirectly();
                    emitter.onComplete();
                    return;
                }

                PlugPagNFCInfosResultDirectly plugPagNFCInfosResult =
                        mPlugPag.detectNfcCardDirectly(PlugPagNearFieldCardData.ONLY_M, 20);

                if (plugPagNFCInfosResult.getResult() != RET_OK) {
                    emitter.onError(
                            new PlugPagException(CARD_NOT_FOUND + plugPagNFCInfosResult.getResult())
                    );
                    mPlugPag.stopNFCCardDirectly();
                    emitter.onComplete();
                    return;
                }

                final PlugPagNFCAuthDirectly auth = new PlugPagNFCAuthDirectly((byte) 0,
                        DEFAULT_KEY_NFC, EM1KeyType.TYPE_A, plugPagNFCInfosResult.getSerialNumber());
                int resultAuth = mPlugPag.justAuthNfcDirectly(auth);

                if (resultAuth != RET_OK) {
                    emitter.onError(new PlugPagException(String.format(
                            Locale.getDefault(),
                            "Erro ao autenticar bloco: %d", resultAuth)));
                    mPlugPag.stopNFCCardDirectly();
                    emitter.onComplete();
                    return;
                }

                emitter.onNext(resultAuth);
                mPlugPag.stopNFCCardDirectly();
            } catch (Exception e) {
                e.printStackTrace();
                emitter.onError(new PlugPagException(CARD_NOT_FOUND));
            }

            emitter.onComplete();
        });
    }

    public Observable<Integer> authNfcBlocoBDirectly() {
        PlugPagSimpleNFCData cardData = new PlugPagSimpleNFCData(
                PlugPagNearFieldCardData.ONLY_M, 28,
                Utils.convertString2Bytes(TEST_16_BYTES)
        );

        return Observable.create(emitter -> {
            try {
                int resultStartNfc = mPlugPag.startNFCCardDirectly();
                if (resultStartNfc != RET_OK) {
                    emitter.onError(new PlugPagException(NFC_START_FAIL + resultStartNfc));
                    emitter.onComplete();
                    return;
                }

                PlugPagNFCAuth auth = new PlugPagNFCAuth(PlugPagNearFieldCardData.ONLY_M,
                        (byte) cardData.getSlot(), DEFAULT_KEY_NFC, EM1KeyType.TYPE_B);
                int resultAuth = mPlugPag.authNFCCardDirectly(auth);

                if (resultAuth != RET_OK) {
                    emitter.onError(new PlugPagException(String.format(
                            Locale.getDefault(),
                            "Erro ao autenticar bloco [ %s ]: %d", cardData.getSlot(), resultAuth)));
                    emitter.onComplete();
                    return;
                }

                emitter.onNext(resultAuth);

                mPlugPag.stopNFCCardDirectly();
            } catch (Exception e) {
                e.printStackTrace();
                emitter.onError(new PlugPagException(CARD_NOT_FOUND));
            }

            emitter.onComplete();
        });
    }

    public Observable<Integer> beepNFC() {
        return Observable.create(emitter -> {

            int result = mPlugPag.beep(new PlugPagBeepData(
                    PlugPagBeepData.FREQUENCE_LEVEL_1, 500)
            );

            if (result == RET_OK) {
                emitter.onNext(result);
            } else {
                emitter.onError(new PlugPagException(BEEP_FAIL));
            }

            emitter.onComplete();
        });
    }

    public Observable<Integer> setLedNFC(Byte ledColor) {
        return Observable.create(emitter -> {

            int result = mPlugPag.setLed(new PlugPagLedData(ledColor));

            if (result == RET_OK) {
                emitter.onNext(result);
            } else {
                emitter.onError(new PlugPagException(LED_FAIL));
            }

            emitter.onComplete();
        });
    }

    public Completable abort() {
        return Completable.create(emitter -> mPlugPag.abortNFC());
    }
}
