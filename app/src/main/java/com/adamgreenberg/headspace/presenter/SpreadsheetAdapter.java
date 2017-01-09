package com.adamgreenberg.headspace.presenter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adamgreenberg.headspace.R;
import com.adamgreenberg.headspace.models.CellViewHolder;
import com.adamgreenberg.headspace.models.OnCellClickedListener;

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
    private int mRowSpan = MIN_ROWS;
    private int mColumnSpan = MIN_COLUMNS;
    private OnCellClickedListener mOnCellClickedListener;

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
        final int row = (int) (position / mColumnSpan);
        final int col = position % mColumnSpan;

        holder.data.setClickable(true);
        holder.data.setFocusable(true);
        holder.data.setOnClickListener(new CellClickListener(row, col));

        if (!mData.isEmpty()) {
            final String data = mData.get(row).get(col);
            holder.data.setText(data);
        }
    }

    @Override
    public int getItemCount() {
        return mRowSpan * mColumnSpan;
    }

    public void registerOnCellClickedListener(final OnCellClickedListener listener) {
        mOnCellClickedListener = listener;
    }

    public void unregisterOnCellClickedListener() {
        mOnCellClickedListener = null;
    }

    public void setRowSpan(final int span) {
        if (mRowSpan != span) {
            final int count = getItemCount();
            mRowSpan = span;
            notifyItemRangeInserted(count - 1, mColumnSpan);
        }
    }

    public void setColumnSpan(final int span) {
        if (span != mColumnSpan) {
            mColumnSpan = span;
            notifyDataSetChanged();
        }
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

    private class CellClickListener implements View.OnClickListener {
        final int row;
        final int col;

        public CellClickListener(final int row, final int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void onClick(final View v) {
            if (mOnCellClickedListener != null) {
                mOnCellClickedListener.onCellClicked(row, col);
            }
        }
    }
}
