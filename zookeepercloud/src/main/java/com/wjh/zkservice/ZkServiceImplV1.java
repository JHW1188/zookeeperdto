package com.wjh.zkservice;

import com.wjh.zkservice.active.ConfigActive;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;


public class ZkServiceImplV1 implements ZkService{
    private static final Logger logger = LoggerFactory.getLogger(ZkServiceImplV1.class);

    private static final int VERSION = -1;
    private ZooKeeper zookeeper;
    private Properties properties;
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    private Stat stat = new Stat();

    private static String CONNECT_STRING;
    private static String SERVICE_HOST;
    private static String SERVICE_NAME;
    private static int SESSION_TIME_OUT ;
    private static String ROOT_PATH;
    private static String ROOT_CONFIG;
    private static String CONFIG_PATH;

    public ZkServiceImplV1() throws IOException {
        logger.info("-------------------------------------------------------------------------------");
        logger.info("-------------------------------------------------------------------------------");
        logger.info("-------------------------------------------------------------------------------");
        logger.info("--------------------------start zk service-------------------------------------");
        getProperties();
        connectZookeeper();
        registerNode();
        logger.info("-------------------------zk service alive----------------------------------");
        logger.info("-------------------------------------------------------------------------------");
        logger.info("-------------------------------------------------------------------------------");
        logger.info("-------------------------------------------------------------------------------");
    }

    /**
     * 注册节点：目前所有的服务都是动态节点
     */
    private void registerNode(){
        createEphemeralNode(ROOT_PATH+"/"+SERVICE_NAME+"/"+SERVICE_HOST,"");
    }

    private void getProperties() throws IOException {
        this.properties = new Properties();
        // 使用ClassLoader加载properties配置文件生成对应的输入流
        InputStream in = ZkServiceImplV1.class.getClassLoader().getResourceAsStream("application.properties");
        // 使用properties对象加载输入流
        properties.load(in);
        CONNECT_STRING = String.valueOf(properties.get("zkservice.connection"));
        SERVICE_HOST = getLocalHost()+":"+properties.getProperty("server.port");
        SESSION_TIME_OUT = Integer.parseInt(String.valueOf(properties.get("zkservice.session.time.out")));
        SERVICE_NAME = properties.getProperty("spring.application.name");
        ROOT_PATH = properties.getProperty("zkservice.root.path");
        ROOT_CONFIG = properties.getProperty("zkservice.root.config");
        CONFIG_PATH = ROOT_CONFIG+"/"+SERVICE_NAME;
    }

    /**
     * 配置功能
     *
     * @param configActive
     */
    @Override
    public void setConfig(ConfigActive configActive){
        if(configActive==null){
            throw new RuntimeException("config watcher is null");
        }

        try {
            ConfigWatcher configWatcher = new ConfigWatcher(configActive);
            zookeeper.getData(CONFIG_PATH, configWatcher, stat);
        } catch (KeeperException|InterruptedException e) {
            logger.error("get config error :{}",e);
        }
    }

    /**
     * 获取配置的行为体
     */
    private class ConfigWatcher implements Watcher {
        private ConfigActive configActive;
        public ConfigWatcher(ConfigActive configActive){
            this.configActive = configActive;
        }
        public void process(WatchedEvent var1){
            try {
                byte[] bytes = zookeeper.getData(CONFIG_PATH,this,stat);
                if(bytes!=null){
                    configActive.config = new String(bytes);
                    configActive.getConfigSuccessful();
                }else {
                    logger.error("get config error");
                    configActive.getConfigFail();
                }
            } catch (KeeperException|InterruptedException e) {
                logger.error("get config fail:{}",e);
            }
        }
    }


    /**
     * 获取当前ip
     *
     * @return
     */
    public static String getLocalHost(){
        String host = null;
        try {
            InetAddress ip4 = Inet4Address.getLocalHost();
            host = ip4.getHostAddress();
        }catch (UnknownHostException e) {
            logger.error("{}",e);
        }
        return host;
    }



    private void connectZookeeper() {
        if (zookeeper == null) {
            try {
                zookeeper = new ZooKeeper(CONNECT_STRING, SESSION_TIME_OUT, null);
                countDownLatch.countDown();
            } catch (IOException e) {
                logger.error(" 连接zk服务失败:{}", e);
            }
        }
    }


    private void closeConnection() {
        if (zookeeper != null) {
            try {
                zookeeper.close();
            } catch (InterruptedException e) {
                logger.error("close zk :{}", e);
            }
        }
    }


    /**
     * 不走发布订阅就传nullji进来就可以了
     * @param path
     * @param watcher
     * @return
     */
    private List<String> getChildren(String path,Watcher watcher) {
        List<String> children = null;
        try {
            if(watcher==null){
                children = zookeeper.getChildren(path,false);
            }else {
                children = zookeeper.getChildren(path,watcher);
            }
        } catch (KeeperException | InterruptedException e) {
            logger.error("获取子节点报错");
        }
        return children;
    }

    private void createEphemeralNode(String path, String data) {
        try {
            zookeeper.create(path,data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        } catch (KeeperException|InterruptedException e) {
            logger.error("创建临时节点失败:{}",e);
        }
    }


    @Override
    public String getData(String path) {
        byte[] data = new byte[0];
        try {
            data = zookeeper.getData(path, false, stat);
        } catch (KeeperException | InterruptedException e) {
            logger.error("获取数据失败:{}", e);
        }
        if (data == null) {
            return null;
        }
        return new String(data);
    }


    @Deprecated
    public String createLockNode(String path, String data) {
        try {
            return this.zookeeper.create(path, data.getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (KeeperException | InterruptedException e) {
            logger.error("创建节点失败：{}", e);
        }
        return null;
    }

    public String getLockNode(String path){
        return getData(path);
    }

    @Deprecated
    public void deleteLockNode(String path){
        try {
            zookeeper.delete(path,VERSION);
        } catch (InterruptedException|KeeperException e) {
            logger.error(":{}",e);
        }
    }

    @Override
    public String doGet(String serviceFullName,String url){
        HttpClient httpClient = HttpClient.initHttpClient();
        List<String> list = getChildren(serviceFullName,null);
        return httpClient.doGet(list.get(0)+url);
    }

    @Override
    public String doPost(String serviceFullName,String url, String data) {
        return null;
    }

    private Stat setData(String path, String data) {
        Stat stat = null;
        try {
            stat = zookeeper.setData(path, data.getBytes(), VERSION);
        } catch (KeeperException | InterruptedException e) {
            logger.error("设置数据:{}", e);
        }
        return stat;
    }


    @Deprecated
    private void deleteNode(String path) {
        try {
            zookeeper.delete(path, VERSION);
        } catch (InterruptedException|KeeperException e) {
            logger.error("删除节点失败:{}",e);
        }
    }


    private String getCTime(String path) {
        Stat stat = null;
        try {
            stat = zookeeper.exists(path, false);
        } catch (KeeperException|InterruptedException e) {
            logger.error("获取节点创建失败:{}",e);
        }
        return String.valueOf(stat.getCtime());
    }


    private Integer getChildrenNum(String path) {
        int childenNum = 0;
        try {
            childenNum = zookeeper.getChildren(path, false).size();
        } catch (KeeperException|InterruptedException e) {
            logger.error("{}",e);
        }
        return childenNum;
    }
}