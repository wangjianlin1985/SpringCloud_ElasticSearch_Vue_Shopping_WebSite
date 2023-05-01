package com.leyou.item.mapper;

import com.leyou.item.pojo.Sku;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

public interface SkuMapper extends Mapper<Sku>, InsertListMapper<Sku> {
}
