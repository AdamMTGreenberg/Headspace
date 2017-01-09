package com.adamgreenberg.headspace.models;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.adamgreenberg.headspace.R;

/**
 * Created by adamgreenberg on 1/8/17.
 */

public class CellViewHolder extends RecyclerView.ViewHolder {

    public TextView data;

    public CellViewHolder(final View itemView) {
        super(itemView);
        data = (TextView) itemView.findViewById(R.id.cell);
    }
}
