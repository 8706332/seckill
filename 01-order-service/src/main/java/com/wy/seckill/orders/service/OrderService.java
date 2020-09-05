package com.wy.seckill.orders.service;


import com.wy.seckill.order.model.Order;

public interface OrderService {

    Integer addSecKillOrder(Order order);//把redis当中的订单信息写入到数据库

    Order getOrdersResult(Integer goodsId, Integer id);//从redis当中获取下单结果
}
