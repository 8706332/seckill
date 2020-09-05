package com.wy.seckill.orders.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.wy.seckill.common.Constants;
import com.wy.seckill.order.model.Order;
import com.wy.seckill.orders.mapper.OrderMapper;
import com.wy.seckill.orders.service.OrderService;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

/**
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    private StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

    /**
     * 把订单写入到数据库
     *
     * @param order
     */
    @Override
    public Integer addSecKillOrder(Order order) {

        int i = orderMapper.insert(order);


        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(stringRedisSerializer);

        //下单完成之后，需要向redis返回一个结果，前台会获取这个结果进行后续操作
        //目的是通知用户下单完成进行支付
        //此数据不要求100%写入到redis当中，就算写入用户也不一定能看到，所以这个key需要添加失效时间

        //设置失效时间为15分钟
        Duration duration = Duration.ofMinutes(15);

        redisTemplate.opsForValue().set(Constants.ORDER_RESULT + order.getGoodsId() + order.getUid(), JSONObject.toJSONString(order), duration);

        return i;
    }

    /**
     * 从redis当中获取下单结果，并转成对象返回
     * @param goodsId
     * @param id
     * @return
     */
    @Override
    public Order getOrdersResult(Integer goodsId, Integer id) {

        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(stringRedisSerializer);

        String result = redisTemplate.opsForValue().get(Constants.ORDER_RESULT + goodsId + id);
        if (result == null || "".equals(result)) {
            return null;
        }
        return JSONObject.parseObject(result, Order.class);

    }
}
