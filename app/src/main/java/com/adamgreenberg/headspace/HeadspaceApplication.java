package com.adamgreenberg.headspace;

import android.app.Application;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import timber.log.Timber;

/**
 * Created by adamgreenberg on 1/8/17.
 */

public class HeadspaceApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FlowManager.init(new FlowConfig.Builder(this).build());
        Timber.plant(new Timber.DebugTree());
    }
}
