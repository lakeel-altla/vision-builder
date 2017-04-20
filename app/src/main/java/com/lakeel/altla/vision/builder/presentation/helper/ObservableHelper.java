package com.lakeel.altla.vision.builder.presentation.helper;

import com.lakeel.altla.vision.helper.ObservableData;
import com.lakeel.altla.vision.helper.ObservableList;
import com.lakeel.altla.vision.helper.ObservableListEvent;

import android.support.annotation.NonNull;

import java.util.concurrent.Callable;

import io.reactivex.Observable;

public final class ObservableHelper {

    private ObservableHelper() {
    }

    @NonNull
    public static <TData> Observable<TData> usingData(@NonNull Callable<ObservableData<TData>> observableDataFactory) {
        return Observable.using(observableDataFactory,
                                observableData -> Observable.<TData>create(subscriber -> {
                                    observableData.addOnDataChangeListener(subscriber::onNext);
                                    observableData.addOnFailureListener(subscriber::onError);
                                    observableData.observe();
                                }),
                                ObservableData::close);
    }

    @NonNull
    public static <TData> Observable<ObservableListEvent<TData>> usingList(
            @NonNull Callable<ObservableList<TData>> observableListFactory) {
        return Observable.using(observableListFactory,
                                observableList -> Observable.<ObservableListEvent<TData>>create(subscriber -> {
                                    observableList.addOnDataListEventListener(subscriber::onNext);
                                    observableList.addOnFailureListener(subscriber::onError);
                                    observableList.observe();
                                }),
                                ObservableList::close);
    }
}
