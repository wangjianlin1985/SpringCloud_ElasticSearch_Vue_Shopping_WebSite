package com.leyou.goods.service;

import com.leyou.goods.client.BrandClient;
import com.leyou.goods.client.CategoryClient;
import com.leyou.goods.client.GoodsClient;
import com.leyou.goods.client.SpecificationClient;
import com.leyou.item.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GoodsService {

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    public Map<String,Object> loadData(Long spuId) {

        Map<String,Object> model = new HashMap<>();

        // 根据spuId查询spu
        Spu spu = this.goodsClient.querySpuById(spuId);
        // 根据分类cid1，cid2，cid3查询分类
        List<Map<String, Object>> categories = new ArrayList<>();
        List<Long> cids = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
        List<String> names = this.categoryClient.queryNamesByIds(cids);
        for (int i = 0; i < cids.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", cids.get(i));
            map.put("name", names.get(i));
            categories.add(map);
        }

        // 根据brandId查询brand
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());

        // 根据spuId查询skus
        List<Sku> skus = this.goodsClient.querySkusBySpuId(spuId);

        SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuId(spuId);

        List<SpecGroup> groups = this.specificationClient.queryGroupWithParam(spu.getCid3());

        // 查询特殊的规格参数
        List<SpecParam> params = this.specificationClient.queryParams(null, spu.getCid3(), false, null);
        Map<Long, String> paramMap = new HashMap<>();
        params.forEach(param -> {
            paramMap.put(param.getId(), param.getName());
        });

        model.put("categories", categories);
        model.put("brand", brand);
        model.put("spu", spu);
        model.put("skus", skus);
        model.put("spuDetail", spuDetail);
        model.put("groups", groups);
        model.put("paramMap", paramMap);
        return model;
    }
}
