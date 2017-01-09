package com.adamgreenberg.headspace.presenter;

import android.os.Bundle;
import android.util.SparseArray;

import java.util.ArrayList;

/**
 * Created by adamgreenberg on 1/8/17.
 * Presenter implementation. Contains all business logic.
 */

public class SpreadsheetPresenterImpl implements SpreadsheetPresenter {

    /**
     * We use this so we can dynamically control the data backing the spreadsheet.
     */
    private SparseArray<String> mData;

    @Override
    public void created(final Bundle savedInstanceState) {
        // Restore any data
        populateAdapter(savedInstanceState);

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
}
