package com.bingzer.android.cloudy;

import com.bingzer.android.cloudy.contracts.DirectoryTree;
import com.bingzer.android.driven.DrivenFile;
import com.bingzer.android.driven.api.Path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class DirectoryTreeImpl implements DirectoryTree {

    private SyncManager manager;
    private DirectoryTreeImpl parent;
    private String name;
    private String local;
    private List<String> filters = new ArrayList<String>(Arrays.asList(FILTER_ALL));
    private List<DirectoryTree> nodes = new ArrayList<DirectoryTree>();
    private DrivenFile drivenFile;

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

    protected DirectoryTreeImpl getParent() {
        return parent;
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
        for (DirectoryTree node : nodes){
            if(node.getName().equalsIgnoreCase(remoteName)){
                return node;
            }
        }

        return null;
    }

    @Override
    public List<DirectoryTree> getNodes(){
        return nodes;
    }

    @Override
    public void filter(String... extensions) {
        filters.clear();
        filters.addAll(Arrays.asList(extensions));
    }

    @Override
    public String toString() {
        return Path.combine(parent != null ? parent.toString() : "", name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getFilters(){
        return filters;
    }

    public DrivenFile getDrivenFile() {
        return drivenFile;
    }

    public void setDrivenFile(DrivenFile drivenFile) {
        this.drivenFile = drivenFile;
    }

    @Override
    public String getLocal() {
        return local;
    }
}
