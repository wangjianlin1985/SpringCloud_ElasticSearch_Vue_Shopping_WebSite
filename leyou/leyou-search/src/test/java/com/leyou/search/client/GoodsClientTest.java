package com.leyou.search.client;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GoodsClientTest {

    @Autowired
    private GoodsClient goodsClient;

    @Test
    public void testSpu(){
        PageResult<SpuBo> result = this.goodsClient.querySpuByPage(null, true, 1, 5);
        result.getItems().forEach(spuBo -> System.out.println(spuBo.getTitle()));
    }
}