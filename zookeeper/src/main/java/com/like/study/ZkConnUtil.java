package com.like.study;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

public class ZkConnUtil {

    private static ZooKeeper zookeeper;

    private static final CountDownLatch countDownLatch = new CountDownLatch(1);

    // 获得zkConn
    public static ZooKeeper getZkConn(String zkServer) throws Exception {
        zookeeper = new ZooKeeper(zkServer, 30000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                Event.KeeperState state = event.getState();
                if (Watcher.Event.KeeperState.SyncConnected == state) {
                    System.out.println("连接zkServer成功.");
                    countDownLatch.countDown();
                }
            }
        });
        countDownLatch.await();   // 这边会默认阻塞着，等着这边 countDownLatch.countDown()调用成功
        return zookeeper;
    }

    public static void main(String[] args) throws Exception {
        ZooKeeper zooKeeper = getZkConn("192.168.0.8:2181");
    }
}






