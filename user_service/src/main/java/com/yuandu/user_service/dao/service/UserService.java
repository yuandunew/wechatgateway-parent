package com.yuandu.user_service.dao.service;

import com.yuandu.user_service.dao.mapper.UserEntityMapper;
import com.yuandu.user_service.dao.model.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserEntityMapper userEntityMapper;

    public void saveUser(UserEntity userEntity){
        userEntityMapper.insert(userEntity);
    }

}
