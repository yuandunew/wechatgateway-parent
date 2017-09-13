package com.yuandu.user.service;

import com.yuandu.user.dao.mapper.UserEntityMapper;
import com.yuandu.user.dao.model.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserEntityMapper userEntityMapper;

    public void save(UserEntity userEntity){
        userEntityMapper.insert(userEntity);
    }

    public void remove(Long id){
        userEntityMapper.remove(id);
    }
}
