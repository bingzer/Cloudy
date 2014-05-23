package com.bingzer.android.cloudy.dir;

import android.webkit.MimeTypeMap;

import com.bingzer.android.cloudy.contracts.DirectoryTree;
import com.bingzer.android.cloudy.contracts.SyncProvider;
import com.bingzer.android.driven.Driven;
import com.bingzer.android.driven.DrivenFile;

import java.io.File;
import java.io.FilenameFilter;

public class DirectorySyncProvider implements SyncProvider {

    private Driven driven;
    private DirectoryTree root;

    public DirectorySyncProvider(DirectoryTree root, Driven driven){
        this.driven = driven;
        this.root = root;
    }

    @Override
    public void sync(long lastTimestamp) {
        ensureNodeExists(root);
    }

    private void ensureNodeExists(final DirectoryTree node) {
        // assign the driven file
        DrivenFile drivenFile = driven.get(node.getDrivenFile(), node.getName());
        if (drivenFile == null)
            drivenFile = driven.create(node.getName());
        node.setDrivenFile(drivenFile);

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
            drivenFile.upload(MimeTypeMap.getSingleton().getMimeTypeFromExtension(f.getName()), f);
        }

        // do the same for each children
        for(DirectoryTree child : node.getNodes()){
            ensureNodeExists(child);
        }
    }

}
