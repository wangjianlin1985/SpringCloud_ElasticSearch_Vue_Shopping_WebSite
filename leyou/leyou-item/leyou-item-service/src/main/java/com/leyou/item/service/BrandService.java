package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;

    /**
     * 分页查询品牌
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    public PageResult<Brand> queryBrandsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        // 1初始化example对象
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();

        // 2添加模糊查询
        if (StringUtils.isNotBlank(key)){
            criteria.andLike("name", "%" + key + "%").orEqualTo("letter", key);
        }

        // 3添加分页
        PageHelper.startPage(page, rows);

        // 4添加排序
        if (StringUtils.isNotBlank(sortBy)){
            example.setOrderByClause(sortBy + (desc ? " desc" : " asc"));
        }

        // 5找到适合的通用mapper方法
        List<Brand> brands = this.brandMapper.selectByExample(example);
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);

        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 新增品牌
     * @param brand
     * @param cids
     */
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        // 新增品牌
        this.brandMapper.insertSelective(brand);
        cids.forEach(cid -> this.brandMapper.saveCategoryAndBrand(cid, brand.getId()));
    }

    /**
     * 根据分类id查询分类的集合
     * @param cid
     * @return
     */
    public List<Brand> queryBrandsByCid(Long cid) {

        return this.brandMapper.selectByCid(cid);
    }

    public Brand queryBrandById(Long id) {
        return this.brandMapper.selectByPrimaryKey(id);
    }
}
