package com.leyou.user.service;

import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "user:verify:";

    /**
     * 校验用户名或者手机号是否可用
     * @param data
     * @param type
     * @return
     */
    public Boolean checkUser(String data, Integer type) {
        User record = new User();
        if (type == 1){
            record.setUsername(data);
        } else if (type == 2) {
            record.setPhone(data);
        } else {
            return null;
        }
        return this.userMapper.selectCount(record) == 0;
    }

    public void verifyCode(String phone) {
        // 判断phone是否为空
        if (StringUtils.isBlank(phone)){
            return;
        }

        // 生成验证码
        String code = NumberUtils.generateCode(6);

        // 发送消息给队列
        Map<String, String> msg = new HashMap<>();
        msg.put("phone", phone);
        msg.put("code", code);
        this.amqpTemplate.convertAndSend("LEYOU.SMS.EXCHANGE", "sms.verify", msg);

        // 缓存验证码
        this.redisTemplate.opsForValue().set(KEY_PREFIX + phone, code, 10, TimeUnit.MINUTES);
    }

    public void register(User user, String code) {
        // 1.校验验证码
        String redisCode = this.redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        if (!StringUtils.equals(code, redisCode)){
            return ;
        }

        // 2.生成随机码
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);

        // 3.加盐加密存储MD5
        user.setPassword(CodecUtils.md5Hex(user.getPassword(), salt));

        // 4.新增用户信息
        user.setId(null);
        user.setCreated(new Date());
        this.userMapper.insertSelective(user);

        this.redisTemplate.delete(KEY_PREFIX + user.getPhone());
    }

    public User queryUser(String username, String password) {

        // 1.先根据用户名查询用户
        User record = new User();
        record.setUsername(username);
        User user = this.userMapper.selectOne(record);

        if (user == null) {
            return user; // 抛出异常
        }

        // 2.对用户输入的密码加盐加密
        password = CodecUtils.md5Hex(password, user.getSalt());

        // 3.判断用户输入的密码是否正确
        if (!StringUtils.equals(password, user.getPassword())){
            return null; // 抛出异常
        }

        return user;
    }
}
