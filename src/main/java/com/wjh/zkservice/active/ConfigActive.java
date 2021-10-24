package com.wjh.zkservice.active;

/**
 * 配置有变动的active
 */
public abstract class ConfigActive{
    public String config;

    public abstract void getConfigSuccessful();

    public abstract void getConfigFail();
}

