package com.bingzer.android.cloudy;

import android.test.AndroidTestCase;

/**
 * Created by Ricky on 5/21/2014.
 */
public class SyncManagerTest extends AndroidTestCase {

    private SyncManager manager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        manager = new SyncManager(null);
    }


}
