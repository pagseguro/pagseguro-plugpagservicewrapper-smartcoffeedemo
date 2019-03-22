package br.com.uol.pagseguro.smartcoffee.printer;

import android.os.Environment;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrintResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrinterData;
import br.com.uol.pagseguro.plugpagservice.wrapper.exception.PlugPagException;
import io.reactivex.Observable;

public class PrinterUseCase {

    private final PlugPag mPlugPag;

    public PrinterUseCase(PlugPag plugPag) {
        mPlugPag = plugPag;
    }

    public Observable<Object> printFile() {
        return Observable.create(emitter -> {

            PlugPagPrintResult result = mPlugPag.printFromFile(new PlugPagPrinterData(
                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/goku.jpg",
                    4,
                    10 * 12));

            if (result.getResult() == 0) {
                emitter.onNext(result);
            } else {
                emitter.onError(new PlugPagException());
            }

            emitter.onComplete();
        });
    }
}
