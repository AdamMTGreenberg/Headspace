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
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;


/**
 * Created by adamgreenberg on 1/8/17.
 * <p>
 * Class that receives the DB results from the query
 */

public class DataStoreQueryTransaction implements QueryTransaction.QueryResultCallback<DataStore> {

    private PublishSubject<List<List<String>>> mSubject;
    private Observable<int[]> mBoundsObservable;

    public DataStoreQueryTransaction() {
        mSubject = PublishSubject.create();
    }

    public Subscription register(Observer<List<List<String>>> observer) {
        return mSubject.subscribe(observer);
    }

    public void queryData(final int rows, final int columns) {
        mBoundsObservable = Observable.just(new int[]{rows, columns});
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
                .zipWith(mBoundsObservable, new Func2<List<DataStore>, int[], List<List<String>>>() {
                    @Override
                    public List<List<String>> call(final List<DataStore> dataStores, final int[] bounds) {
                        final int rows = bounds[0];
                        final int columns = bounds[1];

                        final List<List<String>> rowList = new ArrayList<>(rows);
                        int dataStoresIndx = 0;

                        // Build the matrix
                        for (int r = 0; r < rows; r++) {
                            // Init a new row
                            final List<String> column = new ArrayList<>(columns);
                            rowList.add(column);

                            for (int c = 0; c < columns; c++) {
                                final DataStore ds = dataStores.get(dataStoresIndx);

                                if (ds.mColumn == c && ds.mRow == r) {
                                    dataStoresIndx++;
                                    column.add(ds.mData);
                                } else {
                                    column.add(null);
                                }
                            }
                        }

                        return rowList;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<List<List<String>>>() {

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
                    public void onNext(final List<List<String>> arrayLists) {
                        mSubject.onNext(arrayLists);
                    }

                });
    }

}
