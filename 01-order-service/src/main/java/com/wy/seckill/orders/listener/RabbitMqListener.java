package com.wy.seckill.orders.listener;

import com.alibaba.fastjson.JSONObject;
import com.wy.seckill.common.Constants;
import com.wy.seckill.common.ReturnObject;
import com.wy.seckill.order.model.Order;
import com.wy.seckill.orders.service.GoodService;
import com.wy.seckill.orders.service.OrderService;
import org.checkerframework.checker.units.qual.A;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 */
@Component
public class RabbitMqListener {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    private StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

    @Autowired
    private GoodService goodService;

    /**
     * 消息监听器，持续监听消息队列
     * 方法正常结束，消息就会出队（确认消息）
     * 如果此方法出现异常，不会确认消息
     */
    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = "secKillQueue"), exchange = @Exchange(value = "secKillExchange"), key = "secKillRoutingKey")})
    public void secKillListener(String message) {
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(stringRedisSerializer);

        //把redis当中的数据写入到数据库当中
        Order order = JSONObject.parseObject(message, Order.class);
        order.setCreateTime(new Date());//下单时间
        order.setStatus(1);//订单状态 1待支付 2待发货
        order.setBuyNum(1);//购买数量
        try {
            //需要补充订单的数据
            ReturnObject<BigDecimal> goodsPrice = goodService.getGoodsPrice(order.getGoodsId());
            BigDecimal price = goodsPrice.getData();
            //订单金额=商品价格*购买数量
            order.setBuyPrice(price);
            order.setOrderMoney(price.multiply(new BigDecimal(order.getBuyNum())));
            /*
                处理rabbitmq当中的重复消息

                执行数据库插入，这个方法可能会出现异常，抛出一个DuplicateKeyException异常
                这个异常表示违法数据库的唯一约束，只要抛出异常说明数据已经写入到数据库当中了，完成数据库下单了
                抛出这个异常说明就是从redis拿到的是重复消息了，我们正常捕获完成消息确认就行
             */
            orderService.addSecKillOrder(order);
            /*
              订单已经写入到数据库当中了，我们需要把redis当中的订单备份信息给删除掉，把根源给抹除了
             */
            redisTemplate.delete(Constants.ORDER + order.getGoodsId() + order.getUid());
        } catch (DuplicateKeyException e) {
            //重复的消息也要从redis当中删除
            redisTemplate.delete(Constants.ORDER + order.getGoodsId() + order.getUid());
        }

    }
}
