package cn.itcast.service.service;

import cn.itcast.service.mapper.UserMapper;
import cn.itcast.service.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User queryUserById(Long id) {
        return this.userMapper.selectByPrimaryKey(id);
    }
}
