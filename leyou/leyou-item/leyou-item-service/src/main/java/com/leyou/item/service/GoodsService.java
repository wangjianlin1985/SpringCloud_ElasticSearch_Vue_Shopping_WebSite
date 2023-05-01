package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * 分页查询spu
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    public PageResult<SpuBo> querySpuByPage(String key, Boolean saleable, Integer page, Integer rows) {

        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

        // 1.添加模糊查询
        if (StringUtils.isNotBlank(key)){
            criteria.andLike("title", "%" + key + "%");
        }

        // 2.添加是否上下架的过滤
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }

        // 3.添加分页
        PageHelper.startPage(page, rows);

        // 4.执行查询
        List<Spu> spus = this.spuMapper.selectByExample(example);
        PageInfo<Spu> pageInfo = new PageInfo<>(spus);

        // 5.把List<Spu>转化成List<SpuBo>
        List<SpuBo> spuBos = spus.stream().map(spu -> {
            SpuBo spuBo = new SpuBo();
            // 把spu所有的属性值copy给spuBo
            BeanUtils.copyProperties(spu, spuBo);
            // 设置品牌名称和分类名称
            Brand brand = this.brandMapper.selectByPrimaryKey(spu.getBrandId());
            spuBo.setBname(brand.getName());
            List<String> names = this.categoryService.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            spuBo.setCname(StringUtils.join(names, "-"));
            return spuBo;
        }).collect(Collectors.toList());

        // 返回分页结果集
        return new PageResult<>(pageInfo.getTotal(), spuBos);
    }

    /**
     * 新增商品
     * @param spuBo
     * @return
     */
    @Transactional
    public void saveGoods(SpuBo spuBo) {
        // 1.新增spu
        spuBo.setId(null);
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
        this.spuMapper.insertSelective(spuBo);

        // 2.新增spuDetail
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        this.spuDetailMapper.insertSelective(spuDetail);

        saveSkuAndStock(spuBo);
        // this.skuMapper.insertList(skus);

        sendMsg("insert", spuBo.getId());
    }

    private void sendMsg(String type, Long spuId) {
        try {
            this.amqpTemplate.convertAndSend("item." + type, spuId);
        } catch (AmqpException e) {
            e.printStackTrace();
        }
    }

    private void saveSkuAndStock(SpuBo spuBo) {
        List<Sku> skus = spuBo.getSkus();
        skus.forEach(sku -> {
            // 3.新增sku
            sku.setId(null);
            sku.setSpuId(spuBo.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insertSelective(sku);

            // 4.新增stock
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insertSelective(stock);
        });
    }

    /**
     * 根据主键查询spuDetail
     * @param spuId
     * @return
     */
    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        return this.spuDetailMapper.selectByPrimaryKey(spuId);
    }

    /**
     * 根据spuId查询sku
     * @param spuId
     * @return
     */
    public List<Sku> querySkusBySpuId(Long spuId) {
        Sku record = new Sku();
        record.setSpuId(spuId);
        List<Sku> skus = this.skuMapper.select(record);
        // 查询每一个sku对应的库存
        skus.forEach(sku -> {
            Stock stock = this.stockMapper.selectByPrimaryKey(sku.getId());
            if(stock != null)
                sku.setStock(stock.getStock());
            else
                sku.setStock(0);
        });
        return skus;
    }

    /**
     * 更新商品
     * @param spuBo
     */
    @Transactional
    public void updateGoods(SpuBo spuBo) {
        // 先删除sku和stock
        // 删除stock
        Sku record = new Sku();
        record.setSpuId(spuBo.getId());
        List<Sku> skus = this.skuMapper.select(record);
        skus.forEach(sku -> {
            this.stockMapper.deleteByPrimaryKey(sku.getId());
        });

        // 直接根据spuId删除sku
        this.skuMapper.delete(record);

        // 在新增sku和stock
        this.saveSkuAndStock(spuBo);

        // 更新spu和spuDetail
        spuBo.setSaleable(null);
        spuBo.setValid(null);
        spuBo.setCreateTime(null);
        spuBo.setLastUpdateTime(new Date());
        this.spuMapper.updateByPrimaryKeySelective(spuBo);

        this.spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());

        sendMsg("update", spuBo.getId());
    }

    public Spu querySpuById(Long spuId) {
        return this.spuMapper.selectByPrimaryKey(spuId);
    }

    public Sku querySkuById(Long skuId) {
        return this.skuMapper.selectByPrimaryKey(skuId);
    }
}
