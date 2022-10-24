package br.com.uol.pagseguro.smartcoffee.nfc;

import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.AUTH_BLOCK_B_CARD_SUCCESS;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.AUTH_CARD_SUCCESS;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.BEEP_SUCCESS;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.CARD_DETECTED_SUCCESS;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.LED_OFF_SUCCESS;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.LED_ON_SUCCESS;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.REMOVED_CARD;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.RET_OK;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.RET_WAITING_REMOVE_CARD;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.VALUE_RESULT;
import static br.com.uol.pagseguro.smartcoffee.utils.SmartCoffeeConstants.WAITING_REMOVE_CARD;

import com.hannesdorfmann.mosby.mvp.MvpNullObjectBasePresenter;

import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagNFCResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.PlugPagLedData;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class NFCPresenter extends MvpNullObjectBasePresenter<NFCContract> {

    private final NFCUseCase mUseCase;
    private Disposable mSubscribe;

    @Inject
    public NFCPresenter(NFCUseCase useCase) {
        mUseCase = useCase;
    }

    public void readNFCCard() {
        mSubscribe = mUseCase.readNFCCard()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> getView().showLoading(true))
                .doFinally(() -> getView().showLoading(false))
                .subscribe(
                        result -> getView().showDialog(formatResult(result)),
                        throwable -> getView().showSnackbar(throwable.getMessage())
                );
    }

    public void writeNFCCard() {
        mSubscribe = mUseCase.writeNFCCard()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> getView().showLoading(true))
                .doFinally(() -> getView().showLoading(false))
                .subscribe(result -> getView().showDialog(formatResult(result)),
                        throwable -> getView().showSnackbar(throwable.getMessage()));
    }

    public void writeDirectlyNFCCard() {
        mSubscribe = mUseCase.writeNFCCardBlockB()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> getView().showLoading(true))
                .doFinally(() -> getView().showLoading(false))
                .subscribe(
                        result -> getView().showDialog(VALUE_RESULT + result),
                        error -> getView().showSnackbar(error.getMessage())
                );
    }

    public void detectCardDirectly() {
        mSubscribe = mUseCase.detectCardDirectly()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> getView().showLoading(true))
                .doFinally(() -> getView().showLoading(false))
                .subscribe(
                        result -> {
                            if (result.getResult() == RET_OK) {
                                getView().showSnackbar(CARD_DETECTED_SUCCESS + result.getCid());
                            }
                        },
                        error -> getView().showSnackbar(error.getMessage())
                );
    }

    public void detectRemoveCardDirectly() {
        mSubscribe = mUseCase.detectRemoveCardDirectly()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> getView().showLoading(true))
                .doFinally(() -> getView().showLoading(false))
                .subscribe(
                        result -> {
                            switch (result) {
                                case RET_OK: {
                                    getView().showSnackbar(REMOVED_CARD);
                                    break;
                                }
                                case RET_WAITING_REMOVE_CARD: {
                                    getView().showSnackbar(WAITING_REMOVE_CARD);
                                    break;
                                }
                            }
                        },
                        error -> getView().showSnackbar(error.getMessage())
                );
    }

    public void cmdExchange() {
        mSubscribe = mUseCase.cmdExchange()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> getView().showLoading(true))
                .doFinally(() -> getView().showLoading(false))
                .subscribe(result -> {
                            final byte[] commandApdu = result.getCmd();

                            if (commandApdu != null) {
                                final int swa = commandApdu[0];
                                final int swb = commandApdu[1];

                                getView().showSnackbar("SWA: " + swa + " - " + "SWB: " + swb);
                            }

                        },
                        throwable -> getView().showSnackbar(throwable.getMessage()));
    }

    public void detectJustAuthDirectly() {
        mSubscribe = mUseCase.detectJustAuthDirectly()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> getView().showLoading(true))
                .doFinally(() -> getView().showLoading(false))
                .subscribe(
                        result -> {
                            if (result == RET_OK) {
                                getView().showSnackbar(AUTH_CARD_SUCCESS);
                            }
                        },
                        error -> getView().showSnackbar(error.getMessage())
                );
    }

    public void authNfcBlocoBDirectly() {
        mSubscribe = mUseCase.authNfcBlocoBDirectly()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            if(result == RET_OK){
                                getView().showSnackbar(AUTH_BLOCK_B_CARD_SUCCESS);
                            }
                        },
                        error -> getView().showSnackbar(error.getMessage())
                );
    }

    public void beepNfc() {
        mSubscribe = mUseCase.beepNFC()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> getView().showSnackbar(BEEP_SUCCESS),
                        error -> getView().showSnackbar(error.getMessage())
                );
    }

    public void setLedBlueNFC() {
        mSubscribe = mUseCase.setLedNFC(PlugPagLedData.LED_BLUE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> getView().showSnackbar(LED_ON_SUCCESS),
                        error -> getView().showSnackbar(error.getMessage())
                );
    }

    public void setLedYellowNFC() {
        mSubscribe = mUseCase.setLedNFC(PlugPagLedData.LED_YELLOW)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> getView().showSnackbar(LED_ON_SUCCESS),
                        error -> getView().showSnackbar(error.getMessage())
                );
    }

    public void setLedGreenNFC() {
        mSubscribe = mUseCase.setLedNFC(PlugPagLedData.LED_GREEN)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> getView().showSnackbar(LED_ON_SUCCESS),
                        error -> getView().showSnackbar(error.getMessage())
                );
    }

    public void setLedRedNFC() {
        mSubscribe = mUseCase.setLedNFC(PlugPagLedData.LED_RED)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> getView().showSnackbar(LED_ON_SUCCESS),
                        error -> getView().showSnackbar(error.getMessage())
                );
    }

    public void setLedOffNFC() {
        mSubscribe = mUseCase.setLedNFC(PlugPagLedData.LED_OFF)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> getView().showSnackbar(LED_OFF_SUCCESS),
                        error -> getView().showSnackbar(error.getMessage())
                );
    }

    private String formatResult(PlugPagNFCResult result) {
        return "Valor do slot: " + new String(
                result.getSlots()[result.getStartSlot()].get("data"),
                StandardCharsets.UTF_8
        );
    }

    @Override
    public void detachView(boolean retainInstance) {
        super.detachView(retainInstance);
        if (mSubscribe != null) {
            mSubscribe.dispose();
        }
    }

    public void abort() {
        mSubscribe = mUseCase.abort()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }
}
