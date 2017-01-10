package com.adamgreenberg.headspace.presenter;

import com.adamgreenberg.headspace.BuildConfig;
import com.adamgreenberg.headspace.TestApplication;
import com.adamgreenberg.headspace.models.ClearStack;
import com.adamgreenberg.headspace.models.DataStore;
import com.adamgreenberg.headspace.models.SpreadsheetInfo;
import com.adamgreenberg.headspace.models.TransactionHistory;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by adamgreenberg on 1/9/17.
 * Test suite for the effective methods in the presenter implementation.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, application = TestApplication.class)
public class SpreadsheetPresenterImplTest {

    // Clear the databases
    @Before
    public void before() {
        SQLite.delete().from(ClearStack.class).execute();
        SQLite.delete().from(DataStore.class).execute();
        SQLite.delete().from(SpreadsheetInfo.class).execute();
        SQLite.delete().from(TransactionHistory.class).execute();
    }

}
