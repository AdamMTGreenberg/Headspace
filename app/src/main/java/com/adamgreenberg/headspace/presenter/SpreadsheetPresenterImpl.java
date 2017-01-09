package com.adamgreenberg.headspace.presenter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.adamgreenberg.headspace.models.ClearStackTable;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
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
    private Subscription mClearSubscription;

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
        if (savedInstanceState != null) {
            populateData();
        } else {
            restoreData(savedInstanceState);
        }
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
        if (mClearSubscription != null && !mClearSubscription.isUnsubscribed()) {
            mClearSubscription.unsubscribe();
        }
        mAdapter.unregisterOnCellClickedListener();
    }

    @Override
    public void onFabClicked() {
       // TODO read in the last action

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
        mGridLayoutManager.setTotalColumnCount(mColumns);
        mAdapter.setColumnSpan(mColumns);

        // Notify the undo stack of a column add
        addToUndoStack(false);
    }

    @Override
    public void onAddRowClicked() {
        // Set the row value increased
        mRows++;

        // Add a new null value to each row in the data set
        addNullRow();

        // Set the row value increased on the adapter
        mAdapter.setRowSpan(mRows);

        // Notify the undo stack of a row add
        addToUndoStack(true);
    }

    @Override
    public void onSaveClicked() {
        // TODO Show interstitial blocking UI
        // Update the table params
        updateSpreadsheetParams();

        // Cycle through all records and update
        updateAndSaveRecords();
    }

    @Override
    public void onReloadClicked() {
        // TODO Show interstitial blocking UI
        // Load the table info
        final SpreadsheetInfo info = getInfo();
        // Update the table info in memory
        mRows = info.mRowCount;
        mColumns = info.mColumnCount;

        // Load the data with saved = 1
        // Update table on load
        reloadSpreadsheetData();
    }

    @Override
    public void onClearClicked() {
        mClearSubscription = addToClearStack(mData);
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
        // TODO save the current data in memory
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

    private void updateSpreadsheetParams() {
        SpreadsheetInfo info = getInfo();
        info.mColumnCount = mColumns;
        info.mColumnCountSaved = mColumns;
        info.mRowCount = mRows;
        info.mRowCountSaved = mRows;
        info.update();
    }

    private void reloadSpreadsheetData() {
        mDst.querySavedData(mRows, mColumns);
    }

    private void getSpreadsheetData() {
        initSpreadsheetCheck();
        mSubscription = mDst.register(sqlDataObserver);
        mDst.queryData(mRows, mColumns);
    }

    private void updateAndSaveRecords() {
        mDst.save(mData, mRows, mColumns);
    }

    private void addToUndoStack(final boolean isRowAdd) {
        final TransactionHistory history = new TransactionHistory();
        if (isRowAdd) {
            history.mRowAdded = mRows;
        } else {
            history.mColumnAdded = mColumns;
        }
        history.save();
    }

    private void addClearToUndoStack(final Long time) {
        final TransactionHistory history = new TransactionHistory();
        history.mWasClear = true;
        history.mClearTime  = time;
        history.save();
    }

    private Subscription addToClearStack(final List<List<String>> data) {
        return Observable.just(data)
                .zipWith(
                        Observable.defer(new Func0<Observable<Long>>() {
                            @SuppressWarnings("unchecked")
                            @Override
                            public Observable<Long> call() {
                                return Observable.just(System.currentTimeMillis()); // For non production app, OK to assume non edge cases
                            }
                        }),
                        new Func2<List<List<String>>, Long,  List<List<String>>>() {
                            @Override
                            public  List<List<String>> call(final List<List<String>> lists, final Long time) {
                                int row = 0;
                                // Add all the data to a stored table
                                for (final List<String> rows : lists) {
                                    int col = 0;
                                    for (final String columnData : rows) {
                                        final ClearStackTable clearStackTable = new ClearStackTable();
                                        clearStackTable.mClearTime = time;
                                        clearStackTable.mRow = row;
                                        clearStackTable.mColumn = col;
                                        clearStackTable.mData = columnData;
                                        clearStackTable.save();

                                        col++;
                                    }
                                    row++;
                                }

                                // Create an empty list of data for backing the spreadsheet
                                final List<List<String>> tempData = new ArrayList<>(mRows);
                                for(int i = 0; i < mRows; i++) {
                                    final List<String> tempCol = new ArrayList<>(mColumns);
                                    for (int x = 0; x < mColumns; x++) {
                                        tempCol.add(null);
                                    }
                                }

                                addClearToUndoStack(time);
                                return tempData;
                            }
                        }
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(sqlDataObserver);
    }

    private void addNullColumn() {
        final Iterator<List<String>> iter = mData.iterator();
        while (iter.hasNext()) {
            final List<String> col = iter.next();
            col.add(null);
        }
    }

    private void addNullRow() {
        final List<String> row = new ArrayList<>(mColumns);
        for (int i = 0; i < mColumns; i++) {
            row.add(null);
        }
        mData.add(row);
    }

    private void addToUndoStack(final String oldData, final int row, final int col) {
        final TransactionHistory history = new TransactionHistory();
        history.mRow = row;
        history.mColumn = col;
        history.mOldData = oldData;
        history.save();
    }

    private void restoreData(final Bundle savedInstanceState) {
        // TODO
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
                    ds.mIsSaved = 1;
                    ds.save();
                }
            }
        }
    }

    private void setData(final List<List<String>> lists) {
        mData = lists;
        mAdapter.setRowSpan(mRows);
        mAdapter.setColumnSpan(mColumns);
        mAdapter.setData(mData);
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
            setData(lists);
        }
    };
}

