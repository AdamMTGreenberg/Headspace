package com.adamgreenberg.headspace.models;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import com.adamgreenberg.headspace.R;

/**
 * Created by adamgreenberg on 1/8/17.
 */

public class CellViewHolder extends RecyclerView.ViewHolder {

    public EditText data;

    public CellViewHolder(final View itemView) {
        super(itemView);
        data = (EditText) itemView.findViewById(R.id.cell);
    }
}
