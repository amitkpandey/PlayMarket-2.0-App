package com.blockchain.store.playmarket.ui.exchange_screen;

import android.util.Log;
import android.util.Pair;

import com.blockchain.store.playmarket.api.RestApi;
import com.blockchain.store.playmarket.data.entities.ChangellyBaseBody;
import com.blockchain.store.playmarket.data.entities.ChangellyCreateTransactionResponse;
import com.blockchain.store.playmarket.data.entities.ChangellyCurrency;
import com.blockchain.store.playmarket.data.entities.ChangellyMinimumAmountResponse;

import java.util.ArrayList;

import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.DisposableHelper;
import okhttp3.ResponseBody;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Crypton04 on 27.02.2018.
 */

public class ExchangeActivityPresenter implements ExchangeActivityContract.Presenter {
    private static final String TAG = "ExchangeActivityPresent";
    private ExchangeActivityContract.View view;

    @Override
    public void init(ExchangeActivityContract.View view) {
        this.view = view;
    }

    @Override
    public void loadAllCurrencies() {
        RestApi.getChangellyApi().getCurrenciesFull(ChangellyBaseBody.getCurrenciesFull())
                .map(changellyCurrenciesResponse -> {
                    ArrayList<ChangellyCurrency> changellyCurrencies = new ArrayList<>();
                    for (ChangellyCurrency currency : changellyCurrenciesResponse.result) {
                        if (currency.enabled && !currency.name.equalsIgnoreCase("eth")) {
                            changellyCurrencies.add(currency);
                        }
                    }
                    return changellyCurrencies;
                })
                .flatMap(currencies -> RestApi.getChangellyApi().getMinimumAmount(ChangellyBaseBody.getMinAmount(currencies.get(0).name)), Pair::new)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> view.showLoadCurrenciesProgress(true))
                .doOnUnsubscribe(() -> view.showLoadCurrenciesProgress(false))
                .subscribe(this::onAllCurrenciesReady, this::onAllCurrenciesError);
    }

    private void onAllCurrenciesReady(Pair<ArrayList<ChangellyCurrency>, ChangellyMinimumAmountResponse> responsePair) {
        view.onLoadCurrenciesReady(responsePair.first);
        view.onMinumumAmountReady(responsePair.second.result);
    }

    private void onAllCurrenciesError(Throwable throwable) {
        view.onLoadCurrenciesFailed(throwable);
    }

    @Override
    public void getEstimatedAmount(String name, String amount) {
        RestApi.getChangellyApi().getExchangeAmount(ChangellyBaseBody.getExchangeAmount(name, amount))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> view.showLoadCurrenciesProgress(true))
                .doOnUnsubscribe(() -> view.showLoadCurrenciesProgress(false))
                .subscribe(this::onEstimatedAmountReady, this::onEstimatedAmountFail);
    }

    private void onEstimatedAmountReady(ChangellyMinimumAmountResponse changellyMinimumAmountResponse) {
        view.onEstimatedAmountReady(changellyMinimumAmountResponse.result);
    }

    private void onEstimatedAmountFail(Throwable throwable) {
        view.onEstimatedAmountFail(throwable);
    }

    @Override
    public void createTransaction(String from, String address, String amount, String extraId) {
        RestApi.getChangellyApi().createTransaction(ChangellyBaseBody.createTransactionBody(from, address, amount, extraId))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> view.showLoadCurrenciesProgress(true))
                .doOnUnsubscribe(() -> view.showLoadCurrenciesProgress(false))
                .subscribe(this::onCreateTransationReady, this::onCreateTransactionFailed);
    }

    private void onCreateTransationReady(ChangellyCreateTransactionResponse changellyCreateTransactionResponse) {
        view.onTransactionCreatedSuccessfully(changellyCreateTransactionResponse);
    }

    private void onCreateTransactionFailed(Throwable throwable) {
        view.onTransactionCreatedFailed(throwable);
    }

    @Override
    public void loadMinimumAmount(ChangellyCurrency changellyCurrency) {
        RestApi.getChangellyApi().getMinimumAmount(ChangellyBaseBody.getMinAmount(changellyCurrency.name))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> view.showLoadCurrenciesProgress(true))
                .doOnUnsubscribe(() -> view.showLoadCurrenciesProgress(false))
                .subscribe(this::onMinimumAmountReady, this::onMinimumAmountFailed);

    }

    private void onMinimumAmountReady(ChangellyMinimumAmountResponse changellyMinimumAmountResponse) {
        view.onMinumumAmountReady(changellyMinimumAmountResponse.result);
    }

    private void onMinimumAmountFailed(Throwable throwable) {
        view.onMinimumAmountError(throwable);
    }


}
