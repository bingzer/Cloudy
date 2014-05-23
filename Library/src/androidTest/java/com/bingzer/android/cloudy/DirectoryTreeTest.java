package com.bingzer.android.cloudy;

import android.test.AndroidTestCase;

import com.bingzer.android.cloudy.contracts.DirectoryTree;

import java.util.Arrays;

public class DirectoryTreeTest extends AndroidTestCase {
    SyncManager manager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        manager = new SyncManager(null);
        DirectoryTreeImpl root = (DirectoryTreeImpl) manager.mapRoot("app", "/sdcard/app");
        root.filter("*.exe", "*.txt");

        root.addNode("folder10", "/sdcard/app/folder10").addNode("folder11", "/sdcard/app/folder10/folder11");
        root.addNode("folder20", "/sdcard/app/folder20").addNode("folder21", "/sdcard/app/folder20/folder21");
        root.addNode("folder30", "/sdcard/app/folder30").addNode("folder31", "/sdcard/app/folder30/folder31");

        root.getNode("folder30").addNode("folder32", "/sdcard/app/folder30/folder32");

        assertEquals(3, root.getNodes().size());
    }

    public void test_getFilters(){
        DirectoryTreeImpl root = (DirectoryTreeImpl) manager.getRoot();
        assertTrue(Arrays.equals(root.getFilters().toArray(), new String[]{"*.exe", "*.txt"}));

        DirectoryTreeImpl folder10 = (DirectoryTreeImpl) root.getNode("folder10");
        assertTrue(Arrays.equals(folder10.getFilters().toArray(), new String[]{ DirectoryTree.FILTER_ALL }));
    }

    public void test_addNodeExists(){
        try{
            DirectoryTreeImpl root = (DirectoryTreeImpl) manager.getRoot();
            root.addNode("folder30", "");
            fail("Should throw IllegalArgumentException");
        }
        catch (IllegalArgumentException e){
            assertTrue("good", true);
        }
    }

    public void test_getManager(){
        DirectoryTreeImpl root = (DirectoryTreeImpl) manager.getRoot();
        assertEquals(manager, root.getManager());
    }

    public void test_getLocal(){
        DirectoryTreeImpl root = (DirectoryTreeImpl) manager.getRoot();
        assertEquals("/sdcard/app", root.getLocal());

        assertEquals("/sdcard/app/folder10", root.getNodes().get(0).getLocal());
        assertEquals("/sdcard/app/folder20", root.getNodes().get(1).getLocal());
        assertEquals("/sdcard/app/folder30", root.getNodes().get(2).getLocal());
    }

    public void test_getParent(){
        DirectoryTreeImpl root = (DirectoryTreeImpl) manager.getRoot();

        assertEquals("/app", ((DirectoryTreeImpl) root.getNodes().get(0)).getParent().toString());
        assertEquals("/app", ((DirectoryTreeImpl) root.getNodes().get(1)).getParent().toString());
        assertEquals("/app", ((DirectoryTreeImpl) root.getNodes().get(2)).getParent().toString());
    }

    public void test_nodes(){
        DirectoryTreeImpl root = (DirectoryTreeImpl) manager.getRoot();
        assertEquals(root.getNodes().get(0).toString(), "/app/folder10");
        assertEquals(root.getNodes().get(0).getNodes().get(0).toString(), "/app/folder10/folder11");

        assertEquals(root.getNodes().get(1).toString(), "/app/folder20");
        assertEquals(root.getNodes().get(1).getNodes().get(0).toString(), "/app/folder20/folder21");

        assertEquals(root.getNodes().get(2).toString(), "/app/folder30");
        assertEquals(root.getNodes().get(2).getNodes().get(0).toString(), "/app/folder30/folder31");

        assertEquals(root.getNodes().get(2).toString(), "/app/folder30");
        assertEquals(root.getNodes().get(2).getNodes().get(1).toString(), "/app/folder30/folder32");
    }
}
