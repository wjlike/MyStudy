package com.like.study;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class GetZNodeData {

    private ZooKeeper zooKeeper;

    public GetZNodeData(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    // 同步获取数据
    public void getDataSync(){
        Stat stat = new Stat();
        try {
            byte[] data = zooKeeper.getData("/zookeeper-api-sync", new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    // 一旦节点发生变化，则会回调该方法
                    System.out.println("event: "+event);
                }
            }, stat);
            String s = new String(data);
            System.out.println("data: "+s);
            System.out.println("stat: "+stat);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // 异步获取数据
    public void getDataAsync(){
        zooKeeper.getData("/zookeeper-api-async",false, new AsyncCallback.DataCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                System.out.println("rc: "+rc);
                System.out.println("path: "+path);
                System.out.println("ctx: "+ctx);
                System.out.println("data: "+new String(data));
                System.out.println("stat: "+stat);
            }
        },"get-data-async");
    }

    public static void main(String[] args) throws Exception {
        GetZNodeData getZNodeData = new GetZNodeData(ZkConnUtil.getZkConn("192.168.0.8:2181"));
//        getZNodeData.getDataSync();
        getZNodeData.getDataAsync();
        System.in.read();
    }
}