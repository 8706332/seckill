package com.wy.seckill.order.service;

import com.wy.seckill.common.ReturnObject;
import com.wy.seckill.order.model.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("01-order-service")
public interface OrderService {

    @RequestMapping("/getOrdersResult")
    ReturnObject<Order> getOrdersResult(@RequestParam("goodsId") Integer goodsId, @RequestParam("id") Integer id);
}
