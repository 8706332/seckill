package com.wy.seckill.goods.controller;

import com.wy.seckill.common.Constants;
import com.wy.seckill.common.ReturnObject;
import com.wy.seckill.goods.model.Goods;
import com.wy.seckill.goods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * eureka的服务提供者，返回的都是rest风格的数据
 */
@RestController
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 查询所有商品信息
     *
     * @return
     */
    @RequestMapping("/getAllGoods")
    public Object getAllGoods() {
        //实际工作不能获取所有商品，应该根据系统时间获取即将开始秒杀的商品信息
        List<Goods> list = goodsService.queryAllGoods();
        ReturnObject<List<Goods>> ro = new ReturnObject<>();
        ro.setCode(Constants.SUCCESS);
        ro.setMessage("获取商品成功");
        ro.setData(list);
        return ro;
    }

    /**
     * 根据id获取商品详情
     *
     * @param id
     * @return
     */
    @RequestMapping("/goodsInfo")
    public Object getGoodsInfo(Integer id) {

        Goods goods = goodsService.queryGoodsInfo(id);
        ReturnObject<Goods> ro = new ReturnObject<>();
        ro.setCode(Constants.SUCCESS);
        ro.setMessage("获取商品详情成功");
        ro.setData(goods);
        return ro;
    }

    /**
     * 秒杀方法
     *
     * @param goodsId
     * @param randomName
     * @param id
     * @return
     */
    @RequestMapping("/secKill")
    public Object secKill(Integer goodsId, String randomName, Integer id) {
        ReturnObject ro = new ReturnObject();

        //秒杀的业务方法,返回int类型的值表示结果
        Integer result = goodsService.secKill(goodsId, randomName, id, false);

        switch (result) {

            case 0:
                ro.setCode(Constants.SUCCESS);
                ro.setMessage("商品秒杀成功");
                break;

            case 1:
                ro.setCode(Constants.ERROR);
                ro.setMessage("您购买的商品不存在");
                break;
            case 2:
                ro.setCode(Constants.ERROR);
                ro.setMessage("商品已被抢光");
                break;
            case 3:
                ro.setCode(Constants.ERROR);
                ro.setMessage("你已经购买过本商品，请在<我的订单>中查看");
                break;
            case 4:
                ro.setCode(Constants.ERROR);
                ro.setMessage("当前服务器出现异常，请重试");
                break;
        }


        return ro;
    }

    /**
     * 查询商品id对应的价格
     * @param goodsId
     * @return
     */
    @RequestMapping("/getGoodsPrice")
    public Object getGoodsPrice(Integer goodsId) {

        ReturnObject<BigDecimal> ro = new ReturnObject<>();
        ro.setCode(Constants.SUCCESS);
        ro.setMessage("获取商品价格成功");
        BigDecimal price = goodsService.queryGoodsPrice(goodsId);
        ro.setData(price);
        return ro;
    }
}
