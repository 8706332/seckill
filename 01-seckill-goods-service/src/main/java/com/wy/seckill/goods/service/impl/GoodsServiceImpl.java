package com.wy.seckill.goods.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.wy.seckill.common.Constants;
import com.wy.seckill.goods.mapper.GoodsMapper;
import com.wy.seckill.goods.model.Goods;
import com.wy.seckill.goods.service.GoodsService;
import org.checkerframework.checker.units.qual.K;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

    @Autowired
    private AmqpTemplate amqpTemplate;//队列的模板对象

    /**
     * 查询所有商品信息
     *
     * @return
     */
    @Override
    public List<Goods> queryAllGoods() {

        List<Goods> list = goodsMapper.selectAllGoods();
        return list;
    }

    /**
     * 根据id查询对应的详情信息
     *
     * @param id
     * @return
     */
    @Override
    public Goods queryGoodsInfo(Integer id) {

        /**
         * 这里也可能出现高并发情况，必要时可以从redis当中获取信息
         */
        return goodsMapper.selectGoodsInfo(id);
    }

    /**
     * 秒杀方法，需要完成控制库存，控制限流，控制限购，生成订单信息
     * ps:
     * 此方法一定会出现高并发情况,因此不能直接操作mysql，应该操作redis+rabbitmq完成业务
     *
     * @param goodsId
     * @param randomName
     * @param id
     * @param isRecursion 是否递归调用本方法
     * @return 0 表示成功
     * 1 商品随机名称错误
     * 2 商品库存不足
     * 3 重复购买
     * 4 超过限流人数闸值
     */
    @Override
    public Integer secKill(Integer goodsId, String randomName, Integer id, Boolean isRecursion) {

        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(stringRedisSerializer);

        //根据前缀和商品随机名称获取商品库存
        String num = redisTemplate.opsForValue().get(Constants.GOODS_STORE + randomName);

        /**
         *  商品库存逻辑控制
         */

        //如果商品库存为空，可能是用户手动拼接的请求
        if (null == num || "".equals(num)) {
            return 1;
        }

        //如果库存小于0，说明商品库存不足
        if (Integer.parseInt(num) <= 0) {
            //使用递归调用前用户已经有了限流名额，需要减去本次限流名额
            if (isRecursion) {
                redisTemplate.opsForValue().decrement(Constants.CURRENT_LIMITING);
            }
            return 2;
        }

        /**
         *商品限购限购逻辑控制
         *
         *第二次拦截用户重复购买，这里不能拦截100%的请求，只能拦截一个用户在同一时间发送2次购买请求
         */

        //根据商品随机名称和id做为key从redis获取用户的购买记录
        String result = redisTemplate.opsForValue().get(Constants.PURCHASE_RESTRICTIONS + randomName + id);

        //如果购买记录不为空，说明用户已经购买过了
        if (null != result && !"".equals(result)) {
            //使用递归调用前用户已经有了限流名额，需要减去本次限流名额
            if (isRecursion) {
                redisTemplate.opsForValue().decrement(Constants.CURRENT_LIMITING);
            }
            return 3;
        }

        /**
         * 限流逻辑控制
         * 不是递归调用才能增加限流名额
         */
        if (!isRecursion) {
            // 程序走到这，需要计算当前服务器并发数量
            // 利用redis单线程的特点，就算有多个线程进行+1操作，redis也只会一次执行一条命令
            // 在redis进行+1操作，并返回+1后的结果
            Long increment = redisTemplate.opsForValue().increment(Constants.CURRENT_LIMITING);

            //判断并发量是否超过限流人数闸值
            //ps: 闸值是根据服务测试的运行能力决定的一个服务器的访问限制，这个值通常是一个固定的值，也可以是动态的或计算的值
            if (increment >= 1000) {
                //如果超过了闸值，需要立刻终止用户继续访问位我们服务器
                //还需要减少当前服务器并发数量,进行-1操作，用于回滚刚刚的+1操作
                redisTemplate.opsForValue().decrement(Constants.CURRENT_LIMITING);
                return 4;
            }
        }

        /**
         *启用redis事务来保证库存减少和生成订单同时完成
         */

        //参数为具体执行事务的逻辑代码
        //execute方法会返回一个Object类型的数据表示事务的执行结果
        //如果返回类型为List并且有长度表示事务执行成功

        //把订单信息放到map当中，并转成json格式的数据，写入到队列
        Map<String, Object> map = new HashMap<>();
        map.put("goodsId", goodsId);
        map.put("uid", id);
        map.put("createTime", System.currentTimeMillis());

        Object object = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                //定义一个List集合设置需要监控的key
                List<String> list = new ArrayList<>();
                //商品库存
                list.add(Constants.GOODS_STORE + randomName);
                //商品限购情况
                list.add(Constants.PURCHASE_RESTRICTIONS + randomName + id);
                //设置key监控，相当于锁定了key所对应的value
                redisOperations.watch(list);

                //获取现有库存，防止超卖
                String num = redisTemplate.opsForValue().get(Constants.GOODS_STORE + randomName);
                if (Integer.parseInt(num) <= 0) {
                    //如果库存不足，释放key的监控
                    redisOperations.unwatch();
                    return 2;
                }

                //获取用户购买情况,防止重复购买
                String result = redisTemplate.opsForValue().get(Constants.PURCHASE_RESTRICTIONS + randomName + id);
                if (null != result && !"".equals(result)) {
                    //释放key的监控
                    redisOperations.unwatch();
                    //如果不是空，说明用户已经购买过了
                    return 3;
                }


                //开启redis事务
                redisOperations.multi();
                //减少库存
                redisOperations.opsForValue().decrement(Constants.GOODS_STORE + randomName);
                //添加购买记录
                redisOperations.opsForValue().set(Constants.PURCHASE_RESTRICTIONS + randomName + id, "1");
                //把订单信息存入redis当中进行备份，用于防掉单处理

                //我们需要开启定时任务定时扫描redis中的这个数据来判断是否掉单，如果掉单了，我们需要重新补发消息到rabbitmq当中
                //这个订单备份数据会在订单系统完成数据库下单之后自动删除
                //可能会有消息的补发，rabbitmq会写入重复消息，因此要做好消息的防重复处理
                redisOperations.opsForZSet().add(Constants.ORDER + goodsId + id, JSONObject.toJSONString(map),System.currentTimeMillis());

                //提交事务，返回一个list集合，如果list集合长度大于0，说明事务执行成功
                //如果list集合长度等于0，事务提交失败，原因是redis提交事务前发现它监控的key对应的value已经被其他线程修改过了，会放弃提交事务
                //使用了redis事务防止了超卖和用户重复购买（可以100%拦截）
                return redisOperations.exec();
            }
        });

        //根据事务执行结果的类型来判断是否成功秒杀

        //如果返回类型是int，说明减少库存或者生产订单出现逻辑错误，直接返回错误码即可
        if (object instanceof Integer) {
            return (Integer) object;
        }

        List list = (List) object;

        //如果集合的长度等于0的话，说明提交事务的时候，redis监控的key被修改了，具体情况不详
        if (list.isEmpty()) {
            //需要递归再次执行秒杀逻辑
            //如果普通递归调用又会增加限流名额，需要一个标记来做判断,true表示使用递归调用
            this.secKill(goodsId, randomName, id, true);
        }

        //程序走到这，说明秒杀成功，可以生成订单信息了
        //不能直接写入到mysql当中，mysql服务器会崩溃的，需要写入到rabbitmq当中


        amqpTemplate.convertAndSend("secKillExchange", "secKillRoutingKey", JSONObject.toJSONString(map));

        //写入成功后，秒杀完成，用户退出限流名额
        redisTemplate.opsForValue().decrement(Constants.CURRENT_LIMITING);

        return 0;
    }

    /**
     * 根据id查询对应的价格
     * @param goodsId
     * @return
     */
    @Override
    public BigDecimal queryGoodsPrice(Integer goodsId) {

        return goodsMapper.selectGoodsPrice(goodsId);
    }
}
