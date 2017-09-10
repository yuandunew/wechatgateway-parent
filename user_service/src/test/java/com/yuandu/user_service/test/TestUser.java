package com.yuandu.user_service.test;

import com.yuandu.APP;
import com.yuandu.user_service.dao.model.UserEntity;
import com.yuandu.user_service.dao.service.UserService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import yuandu_common.date.DateUtils;

import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = APP.class)
public class TestUser {

    @Autowired
    private UserService userService;

    private Logger logger = LogManager.getLogger(TestUser.class);

    @org.junit.Test
    public void test() throws Exception {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setName("龙");
        user.setSex(1);
        user.setYuanduId("yuandu1");
        user.setMobeile("18476030086");
        user.setBirthday(DateUtils.getAfterDate(new Date(), 1));
        userService.saveUser(user);
    }

}
