package com.wy.seckill.orders.service;

import com.wy.seckill.common.ReturnObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 *
 */
@FeignClient("01-seckill-goods-service")
public interface GoodService {

    @RequestMapping("/getGoodsPrice")
    ReturnObject<BigDecimal> getGoodsPrice(@RequestParam("goodsId") Integer goodsId);
}
