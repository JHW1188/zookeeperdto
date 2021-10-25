package com.wjh.demoservice1.client;

import com.wjh.zkservice.HttpClient;
import com.wjh.zkservice.ZkService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClientActive implements InitializingBean {


    @Autowired
    private ZkService zkService;

    @Override
    public void afterPropertiesSet() throws Exception {
        zkService.doGet("/service_root/service_ephemeral/zk_test_service","/api1/getapi");
    }
}
