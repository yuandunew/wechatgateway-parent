package com.yuandu.user.rest.Controller;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private Logger logger = LogManager.getLogger(UserController.class);


    @Value("${eureka.user.name}")
    private String eurekaUserName;

    /**
     * 查询工单
     *
     * @return
     */
        @RequestMapping(path = "/configs", method = RequestMethod.GET)
    @ResponseBody
    public String orderByPo() {
        return eurekaUserName;
    }

}
