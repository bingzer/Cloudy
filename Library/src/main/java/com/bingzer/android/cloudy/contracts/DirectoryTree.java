package com.bingzer.android.cloudy.contracts;

/**
* Created by Ricky on 5/21/2014.
*/
public interface DirectoryTree {

    String FILTER_ALL = "*";

    DirectoryTree addNode(String remoteName, String localDirectory);

    DirectoryTree getNode(String remoteName);

    void filter(String... extensions);

}
