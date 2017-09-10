package com.yuandu.user_service.test;

import com.yuandu.APP;
import com.yuandu.user_service.dao.model.UserEntity;
import com.yuandu.user_service.dao.service.UserService;
import com.yuandu.yuandu_redis.repository.RedisObjectRepository;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import yuandu_common.date.DateUtils;
import yuandu_common.json.JsonUtils;

import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = APP.class)
public class TestUser {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisObjectRepository redisObjectRepository;

    private Logger logger = LogManager.getLogger(TestUser.class);

    @org.junit.Test
    public void test() throws Exception {
        UserEntity user = new UserEntity();
        user.setId(3L);
        user.setName("龙");
        user.setSex(1);
        user.setYuanduId("yuandu1");
        user.setMobeile("18476030086");
        user.setBirthday(DateUtils.getAfterDate(new Date(), 1));
        userService.saveUser(user);

        redisObjectRepository.add("user_redis", user, 600L);
    }

    @org.junit.Test
    public void test2(){
        Object user = redisObjectRepository.get("user_redis");
        if(user != null){
            UserEntity userEntity = (UserEntity)user;
            logger.info(JsonUtils.toJson(userEntity));
        }
    }



}
