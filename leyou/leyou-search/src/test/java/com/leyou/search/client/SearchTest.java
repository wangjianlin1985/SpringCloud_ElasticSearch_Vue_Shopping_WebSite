package com.leyou.search.client;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.search.GoodsRepository;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SearchTest {

    @Autowired
    private ElasticsearchTemplate template;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SearchService searchService;

    @Test
    public void test(){
        this.template.createIndex(Goods.class);
        this.template.putMapping(Goods.class);

        Integer page = 1;
        Integer rows = 100;
        do {
            // 分批查询spu
            PageResult<SpuBo> result = this.goodsClient.querySpuByPage(null, true, page, rows);
            List<SpuBo> items = result.getItems();
            // list<spuBo> ==> List<Goods>
            List<Goods> goodsList = items.stream().map(spuBo -> {
                try {
                    return this.searchService.buildGoods(spuBo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());
            this.goodsRepository.saveAll(goodsList);

            page++;
            rows = items.size();
        } while (rows == 100);

    }
}
