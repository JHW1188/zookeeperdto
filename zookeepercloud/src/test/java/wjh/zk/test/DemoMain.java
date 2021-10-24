package wjh.zk.test;


import com.wjh.zkservice.ZkServiceImplV1;
import com.wjh.zkservice.active.ConfigActive;

import java.io.IOException;

public class DemoMain {
    public static void main(String[] args) throws IOException, InterruptedException {

        /*System.out.println(UUID.randomUUID().toString().replace("-",""));
        String x = "3e6696_{687898&3e6@7ebcc0{d9b5";
        System.out.println(x.length());*/
        ZkServiceImplV1 zkService = new ZkServiceImplV1();
        zkService.getData("");
        zkService.setConfig(new ConfigActive() {
            @Override
            public void getConfigSuccessful() {

            }

            @Override
            public void getConfigFail() {

            }
        });

        /*

//        System.out.println(zkService.getChildren("/service_root/service_ephermer/zk-test-service/foo"));
//        System.out.println(zkService.getChildren("/service_root"));
//        System.out.println(zkService.getData("/service_root/foo"));

        Thread.currentThread().sleep(50200);
        zkService.closeConnection();*/
    }
}
