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
    long mID;

    @Column
    public int mRow;

    @Column
    public int mColumn;

    @Column
    public String mData;
}
