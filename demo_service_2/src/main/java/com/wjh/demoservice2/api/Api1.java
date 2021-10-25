package com.wjh.demoservice2.api;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api1")
public class Api1 {

    private static final Logger logger = LoggerFactory.getLogger(Api1.class);

    @RequestMapping(value = "/getapi",method = RequestMethod.GET)
    public String getApi(){
        logger.info("----------------------------------------");
        return UUID.randomUUID().toString();
    }
}
