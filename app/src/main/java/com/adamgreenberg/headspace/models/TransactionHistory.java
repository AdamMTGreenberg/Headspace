package com.adamgreenberg.headspace.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by adamgreenberg on 1/8/17.
 * POJO that models the transaction history of the spreadsheet.
 */
@Table(database = AppDatabase.class)
public class TransactionHistory extends BaseModel {

    @PrimaryKey(autoincrement = true)
    public long id;

    @Column
    public int mRow;

    @Column
    public int mColumn;

    @Column
    public String mOldData;

}
