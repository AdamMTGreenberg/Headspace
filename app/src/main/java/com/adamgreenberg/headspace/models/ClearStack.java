package com.adamgreenberg.headspace.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by adamgreenberg on 1/9/17.
 */
@Table(database = AppDatabase.class)
public class ClearStack extends BaseModel {

    @PrimaryKey(autoincrement = true)
    public long mID;

    @Column
    public long mClearTime;

    @Column
    public int mRow;

    @Column
    public int mColumn;

    @Column
    public String mData;
}
