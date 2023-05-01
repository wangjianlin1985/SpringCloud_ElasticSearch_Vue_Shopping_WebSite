package com.leyou.auth.test;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {

    private static final String pubKeyPath = "E:\\LY_Idea\\rsa.pub";

    private static final String priKeyPath = "E:\\LY_Idea\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjksInVzZXJuYW1lIjoidXNlcjIiLCJleHAiOjE2Mzk3NTcxMDV9.gF5YizOEYTtpzKsusKeabPbXJKAQwFUntRL_0B9qpbASkBqscpeF0cT720oZtlRvQ9cgBTGwVoR_GeQUof3XXN2gkar9K7ZmNI6N3IsobSsGxLjKrZXGr2UaWeZXVpqhxTZBsO_y5_AfOTX0L3zYh68RxNe8NqhuHoXnQFQDtWA";
        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}