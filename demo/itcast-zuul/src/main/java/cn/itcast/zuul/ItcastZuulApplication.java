package cn.itcast.zuul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication
@EnableZuulProxy // 启用zuul网关的代理功能
@EnableEurekaClient
public class ItcastZuulApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItcastZuulApplication.class, args);
	}

}

