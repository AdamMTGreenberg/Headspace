package com.adamgreenberg.headspace.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.adamgreenberg.headspace.R;
import com.adamgreenberg.headspace.presenter.SpreadsheetPresenter;
import com.adamgreenberg.headspace.presenter.SpreadsheetPresenterImpl;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SpreadsheetView {

    @BindView(R.id.spreadsheet)
    RecyclerView mSpreadsheet;

    @BindView(R.id.add_column)
    Button addColumn;

    @BindView(R.id.edit_cell)
    EditText editCell;

    @BindView(R.id.add_row)
    Button addRow;

    private SpreadsheetPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mPresenter = new SpreadsheetPresenterImpl(this);
        mPresenter.created(savedInstanceState);
        initSpreadSheetUi();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.destroyed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.paused();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.resumed();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        mPresenter.saveInstance(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void notifyCellClicked() {
        Timber.v("notifyCellClicked()");
        editCell.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editCell, InputMethodManager.SHOW_IMPLICIT);
    }

    @OnClick(R.id.add_column)
    void onAddColumnClicked() {
        mPresenter.onAddColumnClicked();
    }

    @OnClick(R.id.add_row)
    void onAddRowClicked() {
        mPresenter.onAddRowClicked();
    }

    private void initSpreadSheetUi() {
        // Auto generated
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               mPresenter.onFabClicked();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initRecycler();
        initEditCell();
    }

    private void initEditCell() {
        editCell.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    mPresenter.textEntered(editCell.getText().toString());
                    editCell.setText("");
                    return true;
                }
                return false;
            }
        });
    }

    private void initRecycler() {
        mSpreadsheet.setAdapter(mPresenter.getAdapter());
        mSpreadsheet.setLayoutManager(mPresenter.getGridLayoutManager(this));
        mSpreadsheet.addItemDecoration(mPresenter.getItemDecoration(this));
    }
}
