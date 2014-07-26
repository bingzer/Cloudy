package com.bingzer.android.cloudy.contracts;

import com.bingzer.android.cloudy.SyncException;
import com.bingzer.android.driven.RemoteFile;

import java.io.File;

public interface ISyncDirectoryProvider extends ISyncProvider {
    void sync(File dir, RemoteFile remoteDir) throws SyncException;
}
