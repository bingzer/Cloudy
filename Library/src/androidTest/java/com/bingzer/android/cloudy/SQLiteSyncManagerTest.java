package com.bingzer.android.cloudy;

import android.test.AndroidTestCase;

import com.bingzer.android.driven.RemoteFile;

import java.io.File;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SQLiteSyncManagerTest extends AndroidTestCase {

    private SQLiteSyncManager manager;
    private File internalFile;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        RemoteFile dbRemoteFile = mock(RemoteFile.class);
        when(dbRemoteFile.getName()).thenReturn("SyncDb");

        RemoteFile root = mock(RemoteFile.class);
        when(root.isDirectory()).thenReturn(true);
        when(root.get("SyncDb")).thenReturn(dbRemoteFile);

        manager = new SQLiteSyncManager(getContext(), root);
        internalFile = new File(getContext().getFilesDir(), "Cloudy.Client");
    }

    /////////////////////////////////////////////////////////////////////////////////

    public void test_when_rootIsNotDirectory(){
        RemoteFile root = mock(RemoteFile.class);
        when(root.isDirectory()).thenReturn(false);

        try{
            manager = new SQLiteSyncManager(getContext(), root);
            fail("Should throw an exception");
        }
        catch (SyncException e){
            assertTrue("good", true);
        }
    }

    public void test_getRoot(){
        assertNotNull(manager.getRoot());
    }

    public void test_getClientId(){
        assertTrue(manager.getClientId() != -1);
        assertTrue(internalFile.exists());
    }

}
