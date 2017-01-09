package com.adamgreenberg.headspace.presenter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adamgreenberg.headspace.R;
import com.adamgreenberg.headspace.models.CellViewHolder;

import java.util.ArrayList;
import java.util.List;

import static com.adamgreenberg.headspace.models.Spreadsheet.MIN_COLUMNS;
import static com.adamgreenberg.headspace.models.Spreadsheet.MIN_ROWS;

/**
 * Created by adamgreenberg on 1/8/17.
 * Adapter module for presenting data on the spreadsheet
 */

public class SpreadsheetAdapter extends RecyclerView.Adapter<CellViewHolder> {

    private List<List<String>> mData;

    public SpreadsheetAdapter() {
        mData = new ArrayList<>();
    }

    @Override
    public CellViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.spreadsheet_cell, parent, false);
        CellViewHolder holder = new CellViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final CellViewHolder holder, final int position) {

    }

    @Override
    public int getItemCount() {
        return mData.isEmpty() ? MIN_ROWS * MIN_COLUMNS : mData.size();
    }

    public void addRow() {
    }

    public void addColumn() {
    }

    /**
     * Creates a total setting of all data for the class
     *
     * @param data data that is used for the backing of the spreadsheet
     */
    public void setData(final List<List<String>> data) {
        this.mData = data;
        notifyDataSetChanged();
    }
}
