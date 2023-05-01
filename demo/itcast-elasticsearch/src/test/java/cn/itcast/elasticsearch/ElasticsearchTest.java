package cn.itcast.elasticsearch;

import cn.itcast.elasticsearch.pojo.Item;
import cn.itcast.elasticsearch.repository.ItemRepository;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ElasticsearchTest {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    public void testIndex(){
        this.elasticsearchTemplate.createIndex(Item.class);
        this.elasticsearchTemplate.putMapping(Item.class);
    }

    @Test
    public void testCreate(){
        Item item = new Item(1L, "小米手机8", " 手机",
                "小米", 3499.00, "http://image.leyou.com/13123.jpg");
        this.itemRepository.save(item);
    }

    @Test
    public void indexList() {
        List<Item> list = new ArrayList<>();
        list.add(new Item(2L, "坚果手机R1", " 手机", "锤子", 3699.00, "http://image.leyou.com/123.jpg"));
        list.add(new Item(3L, "华为META10", " 手机", "华为", 4499.00, "http://image.leyou.com/3.jpg"));
        // 接收对象集合，实现批量新增
        itemRepository.saveAll(list);
        //itemRepository.deleteById();
    }

    @Test
    public void testFind(){
//        Optional<Item> item = this.itemRepository.findById(1l);
//        System.out.println(item.get());
        //List<Item> items = this.itemRepository.findByTitle("手机");
        List<Item> items = this.itemRepository.findByPriceBetween(3499d, 3699d);
        items.forEach(System.out::println);
    }

    @Test
    public void testMatch(){
        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("title", "手机");
        Iterable<Item> items = this.itemRepository.search(queryBuilder);
        items.forEach(System.out::println);
    }

    @Test
    public void indexList1() {
        List<Item> list = new ArrayList<>();
        list.add(new Item(1L, "小米手机7", "手机", "小米", 3299.00, "http://image.leyou.com/13123.jpg"));
        list.add(new Item(2L, "坚果手机R1", "手机", "锤子", 3699.00, "http://image.leyou.com/13123.jpg"));
        list.add(new Item(3L, "华为META10", "手机", "华为", 4499.00, "http://image.leyou.com/13123.jpg"));
        list.add(new Item(4L, "小米Mix2S", "手机", "小米", 4299.00, "http://image.leyou.com/13123.jpg"));
        list.add(new Item(5L, "荣耀V10", "手机", "华为", 2799.00, "http://image.leyou.com/13123.jpg"));
        // 接收对象集合，实现批量新增
        itemRepository.saveAll(list);
    }

    @Test
    public void testNative(){
        // 构建了一个自定义查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 给构建器添加查询条件
        queryBuilder.withQuery(QueryBuilders.matchQuery("category", "手机"));
        // 添加了分页条件，页码从0开始
        queryBuilder.withPageable(PageRequest.of(1, 2));
        queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC));
        // 执行查询，返回分页结果集
        Page<Item> itemPage = this.itemRepository.search(queryBuilder.build());
        // 总页数
        System.out.println(itemPage.getTotalPages());
        // 总条数
        System.out.println(itemPage.getTotalElements());
        // 当前页的记录
        itemPage.forEach(System.out::println);
    }

    @Test
    public void testAgg(){
        // 构建自定义查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 添加聚合查询，AggregationBuilders工具类构建聚合，
        queryBuilder.addAggregation(AggregationBuilders.terms("brands").field("brand"));
        // 添加结果集过滤，不包含任何字段，也就是不需要普通结果集
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{}, null));
        // 执行聚合查询，获取聚合结果集，需要强转
        AggregatedPage<Item> itemPage = (AggregatedPage<Item>)this.itemRepository.search(queryBuilder.build());
        // 获取聚合结果集中的聚合对象:根据聚合名称获取想要的聚合对象。强转成子类：LongTerms StringTerms DoubleTerms
        StringTerms brandAgg = (StringTerms)itemPage.getAggregation("brands");
        // 获取聚合中的桶
        brandAgg.getBuckets().forEach(bucket -> {
            // 获取桶中的key
            System.out.println(bucket.getKeyAsString());
            // 获取桶中的记录数
            System.out.println(bucket.getDocCount());
        });
    }

    @Test
    public void testSubAgg(){
        // 构建自定义查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 添加聚合查询，AggregationBuilders工具类构建聚合，
        queryBuilder.addAggregation(AggregationBuilders.terms("brands").field("brand")
                .subAggregation(AggregationBuilders.avg("price_avg").field("price")));
        // 添加结果集过滤，不包含任何字段，也就是不需要普通结果集
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{}, null));
        // 执行聚合查询，获取聚合结果集，需要强转
        AggregatedPage<Item> itemPage = (AggregatedPage<Item>)this.itemRepository.search(queryBuilder.build());
        // 获取聚合结果集中的聚合对象:根据聚合名称获取想要的聚合对象。强转成子类：LongTerms StringTerms DoubleTerms
        StringTerms brandAgg = (StringTerms)itemPage.getAggregation("brands");
        // 获取聚合中的桶
        brandAgg.getBuckets().forEach(bucket -> {
            // 获取桶中的key
            System.out.println(bucket.getKeyAsString());
            // 获取桶中的记录数
            System.out.println(bucket.getDocCount());
            // 解析子聚合，子聚合结果集转成map结构，key-聚合名称，value-聚合对象 Map<price_avg, aggregation>
            Map<String, Aggregation> stringAggregationMap = bucket.getAggregations().asMap();
            InternalAvg price_avg = (InternalAvg)stringAggregationMap.get("price_avg");
            System.out.println(price_avg.getValue());
        });
    }

}
