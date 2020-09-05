package com.wy.seckill.order.controller;

import com.wy.seckill.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 */
@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 从redis当中获取下单结果
     * @param goodsId
     * @return
     */
    @RequestMapping("/getOrdersResult")
    @ResponseBody
    public Object getOrdersResult(Integer goodsId) {
        Integer id = 4396;

        return orderService.getOrdersResult(goodsId,id);
    }
}
