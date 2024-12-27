package com.like.study.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

public class CuratorApi {
    public static void main(String[] args) {
        String connectStr = "192.168.0.8:2181";
        CuratorFramework curatorFramework = CuratorFrameworkFactory   // fluent
                .builder()
                .connectionTimeoutMs(20000)
                .connectString(connectStr)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))   // 设置客户端的重试策略，每隔10秒中重试一次，最多3次
                .build();
        curatorFramework.start();
        try {
            // 创建节点 curator-api
            String znode = curatorFramework
                    .create()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath("/curator-api", "666".getBytes());
            System.out.println("创建节点成功: " + znode);

            // 查询节点 curator-api 数据
            byte[] bytes = curatorFramework.getData().forPath(znode);
            System.out.println("节点curator-api 数据查询成功: " + new String(bytes));

            // 修改节点 curator-api 数据
            curatorFramework.setData().forPath(znode, "888".getBytes());
            System.out.println("节点curator-api 数据修改成功.");

            // 删除节点 curator-api
            curatorFramework.delete().forPath(znode);
            System.out.println("节点curator-api 已被删除.");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}