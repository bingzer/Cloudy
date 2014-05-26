package com.bingzer.android.cloudy.dir;

import android.webkit.MimeTypeMap;

import com.bingzer.android.cloudy.contracts.DirectoryTree;
import com.bingzer.android.cloudy.contracts.SyncProvider;
import com.bingzer.android.driven.LocalFile;
import com.bingzer.android.driven.RemoteFile;
import com.bingzer.android.driven.StorageProvider;

import java.io.File;
import java.io.FilenameFilter;

public class DirectorySyncProvider implements SyncProvider {

    private StorageProvider provider;
    private DirectoryTree root;

    public DirectorySyncProvider(DirectoryTree root, StorageProvider provider){
        this.provider = provider;
        this.root = root;
    }

    @Override
    public void sync(long lastTimestamp) {
        ensureNodeExists(root);
    }

    private void ensureNodeExists(final DirectoryTree node) {
        // assign the provider file
        RemoteFile remoteFile = provider.get(node.getRemoteFile(), node.getName());
        if (remoteFile == null)
            remoteFile = provider.create(node.getName());
        node.setRemoteFile(remoteFile);

        // get all the files according to the filter
        for(File f : new File(node.getLocal()).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                for(String filter : node.getFilters()){
                    if (filename.toLowerCase().endsWith(filter.toLowerCase()))
                        return true;
                }

                return false;
            }
        })){
            LocalFile localFile = new LocalFile(MimeTypeMap.getSingleton().getMimeTypeFromExtension(f.getName()), f);
            remoteFile.upload(localFile);
        }

        // do the same for each children
        for(DirectoryTree child : node.getNodes()){
            ensureNodeExists(child);
        }
    }

}
