package com.like.study.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;

public class CuratorWatch {
    
    public static void main(String[] args) {
//        curatorWatchOnce();
        curatorWatchPersistent();
    }
    
    // 一次性监听
    private static void curatorWatchOnce() {
        String connectStr = "192.168.0.8:2181";
        CuratorFramework curatorFramework = CuratorFrameworkFactory
                .builder()
                .connectionTimeoutMs(20000)
                .connectString(connectStr)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        curatorFramework.start();
        try {
            // 创建节点 curator-watch-once
            String znode = curatorFramework.create().withMode(CreateMode.PERSISTENT).forPath("/curator-watch-once", "".getBytes());
            System.out.println("节点创建成功: " + znode);

            // 给节点 curator-watch-once 添加一次性watch
            curatorFramework.getData().usingWatcher(new CuratorWatcher() {
                @Override
                public void process(WatchedEvent event) throws Exception {
                    System.out.println("节点发生变化: " + event);
                }
            }).forPath(znode);
            System.out.println("给节点curator-watch-once 添加watch成功.");
            // 让当前进程不结束
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 永久监听
    private static void curatorWatchPersistent() {
        String connectStr = "192.168.0.8:2181";
        CuratorFramework curatorFramework = CuratorFrameworkFactory
                .builder()
                .connectionTimeoutMs(20000)
                .connectString(connectStr)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        curatorFramework.start();
        try {
            // 创建节点 curator-watch-persistent
            String znode = curatorFramework.create().forPath("/curator-watch-persistent", "".getBytes());
            System.out.println("节点创建成功: " + znode);

            // 永久的监听
            CuratorCache curatorCache = CuratorCache.build(curatorFramework, znode, CuratorCache.Options.SINGLE_NODE_CACHE);
            CuratorCacheListener listener = CuratorCacheListener.builder().forAll(new CuratorCacheListener() {
                @Override
                public void event(Type type, ChildData oldData, ChildData data) {
                    // 等同于Watch#process回调
                    System.out.println("节点 "+data.getPath()+" 发生改变, 事件类型为: " + type);
                }
            }).build();
            curatorCache.listenable().addListener(listener);
            curatorCache.start();

            System.out.println("给节点curator-watch-persistent 添加watch成功.");
            // 让当前进程不结束
            System.in.read();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}