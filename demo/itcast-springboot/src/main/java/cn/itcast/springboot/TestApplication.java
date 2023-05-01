package cn.itcast.springboot;

import cn.itcast.springboot.controller.HelloController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

//@EnableAutoConfiguration // 启用自动配置
//@ComponentScan // 类似于<context:component-scan base-package=>
@SpringBootApplication
public class TestApplication { // 称为引导类，是springboot应用程序的入口

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
