package com.adamgreenberg.headspace.presenter;

import android.os.Bundle;

/**
 * Created by adamgreenberg on 1/8/17.
 * Presenter contract for the business logic of the presenter class.
 */

public interface SpreadsheetPresenter {

    /**
     * Activity created
     *
     * @param savedInstanceState bundle of data on creation of the Activity
     */
    void created(Bundle savedInstanceState);

    /**
     * Activity paused
     */
    void paused();

    /**
     * Activity resumed
     */
    void resumed();

    /**
     * Activity destroyed
     */
    void destroyed();

    /**
     * On the instance that the floating action button is clicked, hence undo action
     */
    void onFabClicked();

    /**
     * Text was entered for a highlighted cell
     *
     * @param dataEntered the text that was entered
     */
    void textEntered(String dataEntered);

    /**
     * On the instance that the add column button is clicked
     */
    void onAddColumnClicked();

    /**
     * On the instance that the add row button is clicked
     */
    void onAddRowClicked();

    /**
     * On the instance that the save button is clicked
     */
    void onSaveClicked();

    /**
     * On the instance that the reload button is clicked
     */
    void onReloadClicked();

    /**
     * On the instance that the clear button is clicked
     */
    void onClearClicked();

    /**
     * Provides the view with the necessary hookup of the adapter for the base view of data
     *
     * @return instance of the adapter for the activity to display
     */
    SpreadsheetAdapter getAdapter();

    /**
     * Hook called when the save instance state is about to die and needs to store the data
     *
     * @param outState bundle containing data for
     */
    void saveInstance(Bundle outState);

}
