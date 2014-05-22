package com.bingzer.android.cloudy.contracts;

public interface Remote {

    DirectoryTree mapRoot(String remoteName, String localRoot);

    DatabaseMapping mapDatabase(String remoteDb, String localDb, EntityFactory factory);

}
