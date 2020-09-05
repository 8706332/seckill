package com.wy.seckill.orders.controller;

import com.wy.seckill.common.Constants;
import com.wy.seckill.common.ReturnObject;
import com.wy.seckill.order.model.Order;
import com.wy.seckill.orders.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 */
@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @RequestMapping("/getOrdersResult")
    public Object getOrdersResult(Integer goodsId, Integer id) {

        Order order = orderService.getOrdersResult(goodsId, id);

        ReturnObject<Order> ro = new ReturnObject<>();

        if (order == null) {
            ro.setCode(Constants.ERROR);
            ro.setMessage("订单还没有准备好");
            return null;
        }

        ro.setCode(Constants.SUCCESS);
        ro.setMessage("已完成下单");
        ro.setData(order);

        return ro;
    }
}
