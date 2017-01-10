package com.adamgreenberg.headspace.presenter;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.RecyclerView;

import com.adamgreenberg.headspace.models.ClearStack;
import com.adamgreenberg.headspace.models.DataStore;
import com.adamgreenberg.headspace.models.DataStoreQueryTransaction;
import com.adamgreenberg.headspace.models.DataStore_Table;
import com.adamgreenberg.headspace.models.FixedGridLayoutManager;
import com.adamgreenberg.headspace.models.OnCellClickedListener;
import com.adamgreenberg.headspace.models.ParcelableArrayList;
import com.adamgreenberg.headspace.models.Spreadsheet;
import com.adamgreenberg.headspace.models.SpreadsheetInfo;
import com.adamgreenberg.headspace.models.TransactionHistory;
import com.adamgreenberg.headspace.models.TransactionHistory_Table;
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
    private static final String KEY = "DATA_KEY";
    private static final String COL_KEY = "COL_KEY";
    private static final String ROW_KEY = "ROW_KEY";

    private final SpreadsheetView mView;
    private SpreadsheetAdapter mAdapter;
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
    private List<ParcelableArrayList> mData;

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
        if (savedInstanceState == null || !savedInstanceState.containsKey(KEY)) {
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
        // Read in the last action
        final TransactionHistory history = getLastAction();
        // Set the data according to the last action
        if (history.mWasClear) {
            resetClearData(history);
        } else if(history.mColumnAdded != -1) {
            resetColumn(history.mColumnAdded);
        } else if(history.mRowAdded != -1) {
            resetRow(history.mRowAdded);
        } else {
            resetData(history.mOldData, history.mRow, history.mColumn);
        }

        // Remove the last undo data from table
        history.delete();
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
        outState.putParcelableArrayList(KEY, (ArrayList<? extends Parcelable>) mData);
        outState.putInt(COL_KEY, mColumns);
        outState.putInt(ROW_KEY, mRows);
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
        history.mClearTime = time;
        history.mRow = mRows;
        history.mColumn = mColumns;
        history.save();
    }

    private Subscription addToClearStack(final List<ParcelableArrayList> data) {
        return Observable.just(data)
                .zipWith(
                        Observable.defer(new Func0<Observable<Long>>() {
                            @SuppressWarnings("unchecked")
                            @Override
                            public Observable<Long> call() {
                                return Observable.just(System.currentTimeMillis()); // For non production app, OK to assume non edge cases
                            }
                        }),
                        new Func2<List<ParcelableArrayList>, Long, List<ParcelableArrayList>>() {
                            @Override
                            public List<ParcelableArrayList> call(final List<ParcelableArrayList> lists, final Long time) {
                                int row = 0;
                                // Add all the data to a stored table
                                for (final ParcelableArrayList rows : lists) {
                                    int col = 0;
                                    for (final String columnData : rows) {
                                        final ClearStack clearStack = new ClearStack();
                                        clearStack.mClearTime = time;
                                        clearStack.mRow = row;
                                        clearStack.mColumn = col;
                                        clearStack.mData = columnData;
                                        clearStack.save();

                                        col++;
                                    }
                                    row++;
                                }

                                // Create an empty list of data for backing the spreadsheet
                                final List<ParcelableArrayList> tempData = new ArrayList<>(mRows);
                                for (int i = 0; i < mRows; i++) {
                                    final ParcelableArrayList tempCol = new ParcelableArrayList(mColumns);
                                    tempData.add(tempCol);
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
        final Iterator<ParcelableArrayList> iter = mData.iterator();
        while (iter.hasNext()) {
            final ParcelableArrayList col = iter.next();
            col.add(null);
        }
    }

    private void addNullRow() {
        final ParcelableArrayList row = new ParcelableArrayList(mColumns);
        for (int i = 0; i < mColumns; i++) {
            row.add(null);
        }
        mData.add(row);
    }

    private void decrementColumn() {
        final Iterator<ParcelableArrayList> iter = mData.iterator();
        while (iter.hasNext()) {
            final ParcelableArrayList col = iter.next();
            col.remove(mColumns - 1);
        }
        mColumns--;
    }

    private void resetData(final String oldData, final int row, final int column) {
        mData.get(row).set(column, oldData);
        mAdapter.dataChanged(row, column);
    }

    private void resetRow(final int rowAddedIndex) {
        mData.remove(mRows - 1);
        mRows--;
        mAdapter.setRowSpan(mRows);
    }

    private void resetColumn(final int columnAddedIndex) {
        decrementColumn();

        // Set the column value increased on the adapter
        mGridLayoutManager.setTotalColumnCount(mColumns);
        mAdapter.setColumnSpan(mColumns);
    }

    private void resetClearData(final TransactionHistory history) {
        final long timestamp = history.mClearTime;
        mDst.restoreClearedData(timestamp, history.mRow, history.mColumn);
    }

    private TransactionHistory getLastAction() {
        return SQLite.select()
                .from(TransactionHistory.class)
                .orderBy(TransactionHistory_Table.id, false)
                .querySingle();
    }

    private void addToUndoStack(final String oldData, final int row, final int col) {
        final TransactionHistory history = new TransactionHistory();
        history.mRow = row;
        history.mColumn = col;
        history.mOldData = oldData;
        history.save();
    }

    private void restoreData(final Bundle savedInstanceState) {
        final List<ParcelableArrayList> data = savedInstanceState.getParcelableArrayList(KEY);
        final int row = savedInstanceState.getInt(ROW_KEY);
        final int col = savedInstanceState.getInt(COL_KEY);

        mRows = row;
        mColumns = col;
        setData(data);
        mSubscription = mDst.register(sqlDataObserver);
    }

    private void initSpreadsheetCheck() {
        final long count = SQLite.selectCountOf(DataStore_Table.mID)
                .from(DataStore.class)
                .count();

        if (count < Spreadsheet.MIN_COLUMNS * Spreadsheet.MIN_ROWS) {
            // Initialize with empty data
            for (int r = 0; r < Spreadsheet.MIN_ROWS; r++) {
                for (int c = 0; c < Spreadsheet.MIN_COLUMNS; c++) {
                    final DataStore ds = new DataStore();
                    ds.mColumn = c;
                    ds.mRow = r;
                    ds.mIsSaved = 1;
                    ds.save();
                }
            }
        }
    }

    private void setData(final List<ParcelableArrayList> lists) {
        mData = lists;
        mAdapter.setRowSpan(mRows);
        mAdapter.setColumnSpan(mColumns);
        mAdapter.setData(mData);
    }

    /**
     * Spreadsheet data observer
     */
    private Observer<List<ParcelableArrayList>> sqlDataObserver = new Observer<List<ParcelableArrayList>>() {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(final Throwable e) {
            Timber.d(e, "Error fetching spreadsheet");
            // Error on reset of a clear
        }

        @Override
        public void onNext(final List<ParcelableArrayList> lists) {
            setData(lists);
        }
    };

    @VisibleForTesting
    void setData(final List<ParcelableArrayList> lists, final int row, final int column,
                 final SpreadsheetAdapter adapter) {
        mData = lists;
        mRows = row;
        mColumns = column;
        mAdapter = adapter;
    }
}

