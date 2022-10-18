package br.com.uol.pagseguro.smartcoffee.printer;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrintResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrinterData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrinterListener;
import br.com.uol.pagseguro.plugpagservice.wrapper.exception.PlugPagException;
import br.com.uol.pagseguro.smartcoffee.ActionResult;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

public class PrinterUseCase {

    private final PlugPag mPlugPag;

    public PrinterUseCase(PlugPag plugPag) {
        mPlugPag = plugPag;
    }

    public Observable<ActionResult> printFile() {
        return Observable.create((ObservableEmitter<ActionResult> emitter) -> {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/teste.jpg";
            File file = new File(path);

            ActionResult actionResult = new ActionResult();


            if (file.exists()) {
                PlugPagPrintResult result = mPlugPag.printFromFile(
                        new PlugPagPrinterData(
                                path,
                                4,
                                0));

                actionResult.setResult(result.getResult());
                actionResult.setMessage(result.getMessage());
                actionResult.setErrorCode(result.getErrorCode());
                setPrintListener(emitter, actionResult);

                emitter.onNext(actionResult);
            } else {
                emitter.onError(new FileNotFoundException());
            }
            emitter.onComplete();
        });
    }

    private void setPrintListener(ObservableEmitter<ActionResult> emitter, ActionResult result) {
        mPlugPag.setPrinterListener(new PlugPagPrinterListener() {
            @Override
            public void onError(PlugPagPrintResult printResult) {
                emitter.onError(new PlugPagException(String.format("Error %s %s", printResult.getErrorCode(), printResult.getMessage())));
            }

            @Override
            public void onSuccess(PlugPagPrintResult printResult) {
                emitter.onError(new PlugPagException(String.format(Locale.getDefault(), "Print OK: steps [%d]", printResult.getSteps())));
            }
        });
    }
}
