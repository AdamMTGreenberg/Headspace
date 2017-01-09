package com.adamgreenberg.headspace.presenter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.adamgreenberg.headspace.models.DataStore;
import com.adamgreenberg.headspace.models.DataStoreQueryTransaction;
import com.adamgreenberg.headspace.models.DataStore_Table;
import com.adamgreenberg.headspace.models.FixedGridLayoutManager;
import com.adamgreenberg.headspace.models.OnCellClickedListener;
import com.adamgreenberg.headspace.models.Spreadsheet;
import com.adamgreenberg.headspace.models.SpreadsheetInfo;
import com.adamgreenberg.headspace.models.TransactionHistory;
import com.adamgreenberg.headspace.ui.GridDividerDecoration;
import com.adamgreenberg.headspace.ui.SpreadsheetView;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.Iterator;
import java.util.List;

import rx.Observer;
import rx.Subscription;
import timber.log.Timber;

/**
 * Created by adamgreenberg on 1/8/17.
 * Presenter implementation. Contains all business logic.
 */

public class SpreadsheetPresenterImpl implements SpreadsheetPresenter, OnCellClickedListener {

    private static final int DEFAULT_COUNT = Spreadsheet.MIN_ROWS;

    private final SpreadsheetView mView;
    private final SpreadsheetAdapter mAdapter;
    private FixedGridLayoutManager mGridLayoutManager;
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

    /**
     * Reference for the active input cell
     */
    private int mInputRow = 0;
    private int mInputCol = 0;

    public SpreadsheetPresenterImpl(final SpreadsheetView mainActivity) {
        mView = mainActivity;
        mAdapter = new SpreadsheetAdapter();
        mDst = new DataStoreQueryTransaction();
    }

    @Override
    public void created(final Bundle savedInstanceState) {
        populateData();
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
        mAdapter.unregisterOnCellClickedListener();
    }

    @Override
    public void onFabClicked() {
// TODO undo
    }

    @Override
    public void textEntered(final String dataEntered) {
        // Modify the result in memory
        final String oldData = mData.get(mInputRow).set(mInputCol, dataEntered);
        // Add to the undo stack
        addToUndoStack(oldData, mInputRow, mInputCol);
        // Update the view
        mAdapter.dataChanged(mInputRow, mInputCol);
    }

    @Override
    public void onAddColumnClicked() {
        // Set the column value increased
        mColumns++;

        // Add a new null value to each row in the data set
        addNullColumn();

        // Set the column value increased on the adapter
        mAdapter.setColumnSpan(mColumns);

        // Notify the undo stack of a column add
        addToUndoStack(false);
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
        mAdapter.registerOnCellClickedListener(this);
        return mAdapter;
    }

    @Override
    public FixedGridLayoutManager getGridLayoutManager(final Context ctx) {
        mGridLayoutManager = new FixedGridLayoutManager();
        mGridLayoutManager.setTotalColumnCount(mColumns);
        return mGridLayoutManager;
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration(final Context ctx) {
        mItemDecoration = new GridDividerDecoration(ctx);
        return mItemDecoration;
    }

    @Override
    public void onCellClicked(final int row, final int col) {
        mInputRow = row;
        mInputCol = col;

        mView.notifyCellClicked();
        Timber.v("Cell: " + row + " x " + col + " clicked.");
    }

    @Override
    public void saveInstance(final Bundle outState) {
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
        initSpreadsheetCheck();
        mSubscription = mDst.register(sqlDataObserver);
        mDst.queryData(mRows, mColumns);
    }
    
    private void addToUndoStack(final boolean isRowAdd) {
        final TransactionHistory history = new TransactionHistory();
        if (isRowAdd) {
            history.mWasRowAdd = true;
        } else {
            history.mWasColumnAdd = true;
        }
        history.save();
    }

    private void addNullColumn() {
        final Iterator<List<String>> iter = mData.iterator();
        while (iter.hasNext()) {
            final List<String> col = iter.next();
            col.add(null);
        }
    }

    private void addToUndoStack(final String oldData, final int row, final int col) {
        final TransactionHistory history = new TransactionHistory();
        history.mRow = row;
        history.mColumn = col;
        history.mOldData = oldData;
        history.save();
    }

    private void initSpreadsheetCheck() {
        final long count = SQLite.selectCountOf(DataStore_Table.mID)
                .from(DataStore.class)
                .count();

        if (count < Spreadsheet.MIN_COLUMNS * Spreadsheet.MIN_ROWS) {
            // Initialize with empty data
            for (int r = 0; r < Spreadsheet.MIN_ROWS; r++) {
                for (int c = 0; c < Spreadsheet.MIN_COLUMNS ; c++) {
                    final DataStore ds = new DataStore();
                    ds.mColumn = c;
                    ds.mRow = r;
                    ds.save();
                }
            }
        }
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
            mAdapter.setRowSpan(mRows);
            mAdapter.setColumnSpan(mColumns);
            mAdapter.setData(mData);
        }
    };
}

