package com.adamgreenberg.headspace.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;


/**
 * Created by adamgreenberg on 1/8/17.
 * Class that contains the model for backing the data input of the spreadsheet
 */
@Table(database = AppDatabase.class)
public class DataStore extends BaseModel {

    @PrimaryKey(autoincrement = true)
    public long mID;

    @Column
    public int mRow;

    @Column
    public int mColumn;

    @Column
    public String mData;

    /**
     * {@code 0} represents that the data isn't saved, {@code 1} represents that it has been saved.
     */
    @Column(defaultValue = "0")
    public int mIsSaved;

    /**
     * {@code 0} represents that the data isn't showing, {@code 1} represents that it is showing.
     */
    @Column(defaultValue = "1")
    public int mIsShowing;

    public static DataStore empty() {
        final DataStore ds = new DataStore();
        ds.mRow = Integer.MIN_VALUE;
        ds.mColumn = Integer.MIN_VALUE;
        return ds;
    }
}
