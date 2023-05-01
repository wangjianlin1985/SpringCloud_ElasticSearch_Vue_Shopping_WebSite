package cn.itcast.service.controller;

import cn.itcast.service.client.UserClient;
import cn.itcast.service.pojo.User;
import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("consumer/user")
//@DefaultProperties(defaultFallback = "fallbackMethod")
public class UserController {

//    @Autowired
//    private RestTemplate restTemplate;

    //@Autowired
    //private DiscoveryClient discoveryClient; // 服务地址列表

    @Autowired
    private UserClient userClient;

    @GetMapping
    @HystrixCommand
    public String queryUserById(@RequestParam("id") Long id) {

//        if (id == 1){
//            throw new RuntimeException();
//        }
        // 通过服务的id获取服务实例的集合
        //List<ServiceInstance> instances = discoveryClient.getInstances("service-provider");
        //ServiceInstance instance = instances.get(0);
        // return this.restTemplate.getForObject("http://service-provider/user/" + id, String.class);
        return this.userClient.queryUserById(id).toString();
    }

//    public String fallbackMethod(){
//        return "服务器正忙，请稍后再试。fallback";
//    }

//    public String queryUserByIdFallback(Long id){
//        return "服务器正忙，请稍后再试";
//    }
}
