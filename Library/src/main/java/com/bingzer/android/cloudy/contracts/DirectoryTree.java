package com.bingzer.android.cloudy.contracts;

import com.bingzer.android.driven.RemoteFile;

import java.util.List;

/**
* Created by Ricky on 5/21/2014.
*/
public interface DirectoryTree {

    String FILTER_ALL = "*";

    String getName();

    String getLocal();

    DirectoryTree addNode(String remoteName, String localDirectory);

    DirectoryTree getNode(String remoteName);

    List<DirectoryTree> getNodes();

    void filter(String... extensions);

    List<String> getFilters();

    void setRemoteFile(RemoteFile remoteFile);

    RemoteFile getRemoteFile();

}
