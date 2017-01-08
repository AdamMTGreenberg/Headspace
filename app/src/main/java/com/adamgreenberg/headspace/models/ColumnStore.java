package com.adamgreenberg.headspace.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by adamgreenberg on 1/8/17.
 */
@Table(database = AppDatabase.class)
public class ColumnStore extends BaseModel {

    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    long mRowId

    @Column
    int mColumnId;

    @Column
    String mData;


}
