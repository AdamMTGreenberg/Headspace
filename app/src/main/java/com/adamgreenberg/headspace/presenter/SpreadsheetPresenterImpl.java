package com.adamgreenberg.headspace.presenter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.adamgreenberg.headspace.models.DataStoreQueryTransaction;
import com.adamgreenberg.headspace.models.Spreadsheet;
import com.adamgreenberg.headspace.models.SpreadsheetInfo;
import com.adamgreenberg.headspace.ui.GridDividerDecoration;
import com.adamgreenberg.headspace.ui.SpreadsheetView;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

import rx.Observer;
import rx.Subscription;
import timber.log.Timber;

/**
 * Created by adamgreenberg on 1/8/17.
 * Presenter implementation. Contains all business logic.
 */

public class SpreadsheetPresenterImpl implements SpreadsheetPresenter {

    private static final int DEFAULT_COUNT = Spreadsheet.MIN_ROWS;

    private final SpreadsheetView mView;
    private final SpreadsheetAdapter mAdapter;
    private GridLayoutManager mGridLayoutManager;
    private RecyclerView.ItemDecoration mItemDecoration;

    /**
     * DB constraints
     */
    private int mRows = Spreadsheet.MIN_ROWS;
    private int mColumns = Spreadsheet.MIN_COLUMNS;

    /**
     * We use this so we can dynamically control the data backing the spreadsheet.
     */
    private List<List<String>> mData;

    /**
     * SQL async helper
     */
    private DataStoreQueryTransaction mDst;

    /**
     * Subscription to the DB helper object
     */
    private Subscription mSubscription;

    public SpreadsheetPresenterImpl(final SpreadsheetView mainActivity) {
        mView = mainActivity;
        mAdapter = new SpreadsheetAdapter();
        mDst = new DataStoreQueryTransaction();
    }

    @Override
    public void created(final Bundle savedInstanceState) {
        populateData();
        populateAdapter();
    }

    @Override
    public void paused() {
    }

    @Override
    public void resumed() {
    }

    @Override
    public void destroyed() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    @Override
    public void onFabClicked() {
// TODO
    }

    @Override
    public void textEntered(final String dataEntered) {
// TODO
    }

    @Override
    public void onAddColumnClicked() {
// TODO
    }

    @Override
    public void onAddRowClicked() {
// TODO
    }

    @Override
    public void onSaveClicked() {
// TODO
    }

    @Override
    public void onReloadClicked() {
// TODO
    }

    @Override
    public void onClearClicked() {
// TODO
    }

    @Override
    public SpreadsheetAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public GridLayoutManager getGridLayoutManager(final Context ctx) {
        mGridLayoutManager = new GridLayoutManager(ctx, mColumns);
        return mGridLayoutManager;
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration(final Context ctx) {
        mItemDecoration = new GridDividerDecoration(ctx);
        return mItemDecoration;
    }

    @Override
    public void saveInstance(final Bundle outState) {
    }

    private void populateAdapter() {

    }

    private void populateData() {
        final SpreadsheetInfo info = getInfo();
        mRows = info.mRowCount;
        mColumns = info.mColumnCount;

        getSpreadsheetData();
    }

    private SpreadsheetInfo getInfo() {
        SpreadsheetInfo info = SQLite.select()
                .from(SpreadsheetInfo.class)
                .querySingle(); // Should only be one

        if (info == null) {
            info = new SpreadsheetInfo();
            info.mColumnCount = DEFAULT_COUNT;
            info.mColumnCountSaved = DEFAULT_COUNT;
            info.mRowCount = DEFAULT_COUNT;
            info.mRowCountSaved = DEFAULT_COUNT;
            info.save();
        }

        return info;
    }

    private void getSpreadsheetData() {
        mSubscription = mDst.register(sqlDataObserver);
        mDst.queryData(mRows, mColumns);
    }

    /**
     * Spreadsheet data observer
     */
    private Observer<List<List<String>>> sqlDataObserver = new Observer<List<List<String>>>() {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(final Throwable e) {
            Timber.d(e, "Error fetching spreadsheet");
        }

        @Override
        public void onNext(final List<List<String>> lists) {
            mData = lists;
            mAdapter.setData(mData);
        }
    };
}

