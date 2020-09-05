package com.wy.seckill.good.service;

import com.wy.seckill.common.ReturnObject;
import com.wy.seckill.goods.model.Goods;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("01-seckill-goods-service")
public interface GoodsService {

    @RequestMapping("/getAllGoods")
    ReturnObject<List<Goods>> getAllGoods();//获取所有商品信息

    @RequestMapping("/goodsInfo")
    ReturnObject<Goods> getGoodsInfo(@RequestParam("id") Integer id);//获取id对应的详情

    @RequestMapping("/secKill")
    ReturnObject secKill(@RequestParam("goodsId")Integer goodsId, @RequestParam("randomName")String randomName, @RequestParam("id")Integer id);//秒杀
}
