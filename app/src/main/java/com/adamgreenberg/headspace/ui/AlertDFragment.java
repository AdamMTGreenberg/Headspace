package com.adamgreenberg.headspace.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.adamgreenberg.headspace.presenter.SpreadsheetPresenter;

/**
 * Created by adamgreenberg on 1/9/17.
 */

public class AlertDFragment extends DialogFragment {

    private SpreadsheetPresenter mPresenter;

    public AlertDFragment() {
        // Standard for recreation
    }

    public void setPresenter(final SpreadsheetPresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                // Set Dialog Title
                .setTitle("Welcome")
                // Set Dialog Message
                .setMessage("Load Previous Database?")

                // Positive button
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        AlertDFragment.this.dismiss();
                    }
                })

                // Negative Button
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,	int which) {
                        // Clear the DB
                        mPresenter.onClearClicked();
                        AlertDFragment.this.dismiss();
                    }
                }).create();
    }
}