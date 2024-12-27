package com.like.study;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

public class CreateZNode {

    private ZooKeeper zooKeeper;

    public CreateZNode(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    // 同步创建节点
    public void createZNodeWithSync() throws Exception {
        String znode = zooKeeper.create("/zookeeper-api-sync", "111".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println("创建节点成功: " + znode);
    }

    // 异步创建节点
    public void createZNodeWithAsync() {
        zooKeeper.create("/zookeeper-api-async", "111".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, new AsyncCallback.StringCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, String name) {
                System.out.println("rc: " + rc);
                System.out.println("path: " + path);
                System.out.println("ctx: " + ctx);
                System.out.println("name: " + name);
            }
        }, "create-asyn");
    }

    public static void main(String[] args) throws Exception {
        CreateZNode createZNode = new CreateZNode(ZkConnUtil.getZkConn("192.168.0.8:2181"));
//        createZNode.createZNodeWithSync();
        createZNode.createZNodeWithAsync();
        System.in.read();
    }
}