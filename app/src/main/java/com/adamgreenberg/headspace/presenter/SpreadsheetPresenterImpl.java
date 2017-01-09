package com.adamgreenberg.headspace.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.adamgreenberg.headspace.models.DataStore;
import com.adamgreenberg.headspace.models.DataStore_Table;
import com.adamgreenberg.headspace.models.SpreadsheetInfo;
import com.adamgreenberg.headspace.ui.SpreadsheetView;
import com.raizlabs.android.dbflow.sql.language.CursorResult;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adamgreenberg on 1/8/17.
 * Presenter implementation. Contains all business logic.
 */

public class SpreadsheetPresenterImpl implements SpreadsheetPresenter {

    private static final int DEFAULT_COUNT = 8;

    private final SpreadsheetView mView;
    private final SpreadsheetAdapter mAdapter;

    /**
     * DB constraints
     */
    private int mRows;
    private int mColumns;

    /**
     * We use this so we can dynamically control the data backing the spreadsheet.
     */
    private ArrayList<ArrayList<String>> mData;

    public SpreadsheetPresenterImpl(final SpreadsheetView mainActivity) {
        mView = mainActivity;
        mAdapter = new SpreadsheetAdapter();
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
        // TODO close any DB connections
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
        return null; // TODO
    }

    @Override
    public void saveInstance(final Bundle outState) {
// TODO
    }

    private void populateAdapter() {

    }

    private void populateData() {
        final SpreadsheetInfo info = getInfo();
        mRows = info.mRowCount;
        mColumns = info.mColumnCount;

        mData = getSpreadsheetData();
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

    public void getSpreadsheetData() {
        SQLite.select()
                .from(DataStore.class)
                .orderBy(DataStore_Table.mRow, true)
                .orderBy(DataStore_Table.mColumn, true)
                .async()
                .queryResultCallback(new QueryTransaction.QueryResultCallback<DataStore>() {
                    @Override
                    public void onQueryResult(final QueryTransaction<DataStore> transaction,
                                              @NonNull final CursorResult<DataStore> tResult) {
                        final List<DataStore> data = tResult.toListClose();


                    }

                }).execute();
    }
}

