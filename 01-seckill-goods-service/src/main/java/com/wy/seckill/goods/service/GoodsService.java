package com.wy.seckill.goods.service;

import com.wy.seckill.common.ReturnObject;
import com.wy.seckill.goods.model.Goods;

import java.math.BigDecimal;
import java.util.List;

public interface GoodsService {

    List<Goods> queryAllGoods();

    Goods queryGoodsInfo(Integer id);

    Integer secKill(Integer goodsId, String randomName, Integer id,Boolean isRecursion);

    BigDecimal queryGoodsPrice(Integer goodsId);
}
