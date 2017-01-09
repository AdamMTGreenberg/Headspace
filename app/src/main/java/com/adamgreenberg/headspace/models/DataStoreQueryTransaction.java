package com.adamgreenberg.headspace.models;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.CursorResult;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;


/**
 * Created by adamgreenberg on 1/8/17.
 *
 * Class that receives the DB results from the query
 */

public class DataStoreQueryTransaction implements QueryTransaction.QueryResultCallback<DataStore> {

    private PublishSubject<ArrayList<ArrayList<String>>> mSubject;

    public DataStoreQueryTransaction() {
        mSubject = PublishSubject.create();
    }

    public Subscription register(Observer<ArrayList<ArrayList<String>>> observer) {
        return mSubject.subscribe(observer);
    }

    public void query(){
        SQLite.select()
                .from(DataStore.class)
                .orderBy(DataStore_Table.mRow, true)
                .orderBy(DataStore_Table.mColumn, true)
                .async()
                .queryResultCallback(this)
                .execute();
    }

    @Override
    public void onQueryResult(final QueryTransaction<DataStore> transaction,
                              @NonNull final CursorResult<DataStore> tResult) {
        final List<DataStore> data = tResult.toListClose();
        parseAndEmit(data);
    }

    private void parseAndEmit(final List<DataStore> data) {
        Observable.just(data)
                .flatMap(new Func1<List<DataStore>, Observable<ArrayList<ArrayList<String>>>>() {
                    @Override
                    public Observable<ArrayList<ArrayList<String>>> call(final List<DataStore> dataStores) {
                        return null;
                    }
                })
                .observeOn(Schedulers.newThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ArrayList<ArrayList<String>>>() {

                    @Override
                    public void onCompleted() {
                        // Do nothing
                    }

                    @Override
                    public void onError(final Throwable e) {
                        mSubject.onError(e);
                        // Rebind
                        mSubject = PublishSubject.create();
                    }

                    @Override
                    public void onNext(final ArrayList<ArrayList<String>> arrayLists) {
                        mSubject.onNext(arrayLists);
                    }

                });
    }

}
