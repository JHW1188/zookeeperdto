package com.wjh.zkservice;

import com.wjh.zkservice.active.ConfigActive;

public interface ZkService {
    /**
     * 获取数据
     * @param path
     * @return
     */
    String getData(String path);
    /**
     * 获取config ,并注册订阅
     *
     * @param configActive
     */
    void setConfig(ConfigActive configActive);

    /**
     * 创建分布式锁
     *
     * @param path
     * @param data
     * @return
     */
    @Deprecated
    String createLockNode(String path, String data);

    /**
     * 获得锁
     *
     * @param path
     * @return
     */
    String getLockNode(String path);

    /**
     * 删除锁
     *
     * @param path
     */
    @Deprecated
    void deleteLockNode(String path);

    String doGet(String url);

    String doPost(String url,String data);
}
