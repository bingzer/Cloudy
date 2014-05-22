package com.bingzer.android.cloudy;

import com.bingzer.android.cloudy.contracts.DirectoryTree;
import com.bingzer.android.driven.api.Path;

import java.util.ArrayList;
import java.util.List;

class DirectoryTreeImpl implements DirectoryTree {

    private SyncManager manager;
    private DirectoryTreeImpl parent;
    private String name;
    private String local;
    private String[] extensions = { FILTER_ALL };
    private List<DirectoryTreeImpl> nodes = new ArrayList<DirectoryTreeImpl>();

    //////////////////////////////////////////////////////////////////////////////////////////

    DirectoryTreeImpl(SyncManager manager, String name, String local){
        this(manager, null, name, local);
    }

    DirectoryTreeImpl(SyncManager manager, DirectoryTreeImpl parent, String name, String local){
        this.parent = parent;
        this.manager = manager;
        this.name = name;
        this.local = local;
    }

    //////////////////////////////////////////////////////////////////////////////////////////

    protected SyncManager getManager() {
        return manager;
    }

    protected String getName() {
        return name;
    }

    protected String getLocal() {
        return local;
    }

    protected List<DirectoryTreeImpl> getNodes(){
        return nodes;
    }

    protected DirectoryTreeImpl getParent() {
        return parent;
    }

    protected String[] getExtensions(){
        return extensions;
    }

    //////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public DirectoryTree addNode(String remoteName, String localDirectory) {
        if(getNode(remoteName) != null) throw new IllegalArgumentException("Remote name {" + remoteName + "} exists");

        DirectoryTreeImpl node = new DirectoryTreeImpl(manager, this, remoteName, localDirectory);
        nodes.add(node);

        return node;
    }

    @Override
    public DirectoryTree getNode(String remoteName) {
        for (DirectoryTreeImpl node : nodes){
            if(node.getName().equalsIgnoreCase(remoteName)){
                return node;
            }
        }

        return null;
    }

    @Override
    public void filter(String... extensions) {
        this.extensions = extensions;
    }

    @Override
    public String toString() {
        return Path.combine(parent != null ? parent.toString() : "", name);
    }
}
