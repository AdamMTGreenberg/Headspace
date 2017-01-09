package com.adamgreenberg.headspace.models;

/**
 * Created by adamgreenberg on 1/8/17.
 * Listener callback for what cell is clicked
 */

public interface OnCellClickedListener {

    /**
     * Callback for when a row is clicked
     * @param row row that is clicked, 0 - n
     * @param col column that is clicked, 0 - n
     */
    void onCellClicked(int row, int col);
}
