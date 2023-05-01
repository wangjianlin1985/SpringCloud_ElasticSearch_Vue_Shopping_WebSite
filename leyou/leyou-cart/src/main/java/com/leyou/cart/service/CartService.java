package com.leyou.cart.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.cart.client.GoodsClient;
import com.leyou.cart.interceptor.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.Sku;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GoodsClient goodsClient;

    public void addCart(Cart cart) {

        UserInfo userInfo = LoginInterceptor.get();
        // 先查询
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(userInfo.getId().toString());

        String skuId = cart.getSkuId().toString();
        Integer num = cart.getNum();

        if (hashOperations.hasKey(skuId)){
            // 判断是否有,更新数量
            String cartJson = hashOperations.get(skuId).toString();
            cart = JsonUtils.parse(cartJson, Cart.class);
            cart.setNum(num + cart.getNum());
        } else {
            // 没有，新增
            cart.setUserId(userInfo.getId());
            // 查询商品信息
            Sku sku = this.goodsClient.querySkuById(cart.getSkuId());
            cart.setPrice(sku.getPrice());
            cart.setImage(StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(), ",")[0]);
            cart.setOwnSpec(sku.getOwnSpec());
            cart.setTitle(sku.getTitle());
        }
        hashOperations.put(skuId, JsonUtils.serialize(cart));
    }

    public List<Cart> queryCarts() {
        // 获取用户信息
        UserInfo userInfo = LoginInterceptor.get();

        // 判断hash操作对象是否存在
        if (!this.redisTemplate.hasKey(userInfo.getId().toString())){
            return null;
        }
        // 查询
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(userInfo.getId().toString());

        List<Object> cartJsons = hashOperations.values();

        return cartJsons.stream().map(cartJson -> JsonUtils.parse(cartJson.toString(), Cart.class)).collect(Collectors.toList());
    }
}
