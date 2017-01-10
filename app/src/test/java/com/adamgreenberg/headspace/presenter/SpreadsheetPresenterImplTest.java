package com.adamgreenberg.headspace.presenter;

import com.adamgreenberg.headspace.BuildConfig;
import com.adamgreenberg.headspace.TestApplication;
import com.adamgreenberg.headspace.models.ClearStack;
import com.adamgreenberg.headspace.models.DataStore;
import com.adamgreenberg.headspace.models.FixedGridLayoutManager;
import com.adamgreenberg.headspace.models.ParcelableArrayList;
import com.adamgreenberg.headspace.models.Spreadsheet;
import com.adamgreenberg.headspace.models.SpreadsheetInfo;
import com.adamgreenberg.headspace.models.TransactionHistory;
import com.adamgreenberg.headspace.ui.MainActivity;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

/**
 * Created by adamgreenberg on 1/9/17.
 * Test suite for the effective methods in the presenter implementation.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, application = TestApplication.class)
public class SpreadsheetPresenterImplTest {

    private static final int BASE_COUNT = Spreadsheet.MIN_ROWS;

    @Resource
    private PodamFactory factory;

    private MainActivity activity;

    // Clear the databases
    @Before
    public void before() {
        SQLite.delete().from(ClearStack.class).execute();
        SQLite.delete().from(DataStore.class).execute();
        SQLite.delete().from(SpreadsheetInfo.class).execute();
        SQLite.delete().from(TransactionHistory.class).execute();

        factory = new PodamFactoryImpl();
        // activity = Robolectric.setupActivity(MainActivity.class);
    }

    @After
    public void after() {
        activity = null;
        factory = null;
    }

    @Test
    public void testOnFabClickedDataUndo() throws Exception {
        generateRandomInitData();
        final String oldData = "Test Test Test";
        final TransactionHistory history = new TransactionHistory();
        history.mOldData = oldData;
        history.mRow = 2;
        history.mColumn = 5;
        history.save();

        final SpreadsheetPresenterImpl presenter = new SpreadsheetPresenterImpl(null);
        final List<ParcelableArrayList> data = getMockData();
        final SpreadsheetAdapter adapter = initTestAdapter(data);
        presenter.setData(data, BASE_COUNT, BASE_COUNT, adapter);
        presenter.onFabClicked();

        final long count = SQLite.select().from(TransactionHistory.class).count();
        Assert.assertEquals(count, 0);
    }

    @Test
    public void testOnFabClickedColumnUndo() throws Exception {
        generateRandomInitData();
        final TransactionHistory history = new TransactionHistory();
        history.mColumnAdded = 9;
        history.save();

        final SpreadsheetPresenterImpl presenter = new SpreadsheetPresenterImpl(null);
        final List<ParcelableArrayList> data = getMockData();
        final SpreadsheetAdapter adapter = initTestAdapter(data);
        presenter.setData(data, BASE_COUNT, BASE_COUNT, adapter);
        presenter.setGridLayoutManager(new FixedGridLayoutManager());
        presenter.onFabClicked();

        Assert.assertEquals(adapter.mColumnSpan, BASE_COUNT - 1);

        final long count = SQLite.select().from(TransactionHistory.class).count();
        Assert.assertEquals(count, 0);
    }

    @Test
    public void testOnFabClickedRowUndo() throws Exception {
        generateRandomInitData();
        final TransactionHistory history = new TransactionHistory();
        history.mRowAdded = 9;
        history.save();

        final SpreadsheetPresenterImpl presenter = new SpreadsheetPresenterImpl(null);
        final List<ParcelableArrayList> data = getMockData();
        final SpreadsheetAdapter adapter = initTestAdapter(data);
        presenter.setData(data, BASE_COUNT, BASE_COUNT, adapter);
        presenter.onFabClicked();

        Assert.assertEquals(adapter.mRowSpan, BASE_COUNT - 1);

        final long count = SQLite.select().from(TransactionHistory.class).count();
        Assert.assertEquals(count, 0);
    }

    private SpreadsheetAdapter initTestAdapter(final List<ParcelableArrayList> data) {
        final SpreadsheetAdapter adapter = new SpreadsheetAdapter();
        adapter.setData(data);
        return adapter;
    }

    private List<ParcelableArrayList> getMockData() {
        final Random random = new Random();
        final List<ParcelableArrayList> data = new ArrayList<>(BASE_COUNT);

        for (int x = 0 ; x < BASE_COUNT; x++) {
            final ParcelableArrayList sub = new ParcelableArrayList(BASE_COUNT);
            for (int y = 0; y < BASE_COUNT; y++) {
                if (random.nextBoolean()) {
                    sub.add(getRandomString());
                } else {
                    sub.add(null);
                }
            }
            data.add(sub);
        }
        return data;
    }

    private void generateRandomInitData() {
        final Random random = new Random();

        for (int x = 0 ; x < BASE_COUNT; x++) {
            for (int y = 0; y < BASE_COUNT; y++){
                final DataStore ds = new DataStore();
                ds.mColumn = y;
                ds.mRow = x;
                if (random.nextBoolean()) {
                    ds.mData = getRandomString();
                }
                ds.save();
            }
        }
    }

    public String getRandomString() {
        byte[] array = new byte[7]; // length is bounded by 7
        new Random().nextBytes(array);
        String generatedString = new String(array, Charset.forName("UTF-8"));
        return generatedString;
    }


}
