package com.adamgreenberg.headspace.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by adamgreenberg on 1/8/17.
 * Class that backs the spreadsheet info allowing us to adjust appropriately.
 */
@Table(database = AppDatabase.class)
public class SpreadsheetInfo extends BaseModel {

    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    public int mRowCount;

    @Column
    public int mColumnCount;
    
}
