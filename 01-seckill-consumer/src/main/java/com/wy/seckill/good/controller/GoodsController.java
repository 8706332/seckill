package com.wy.seckill.good.controller;

import com.wy.seckill.common.Constants;
import com.wy.seckill.common.ReturnObject;
import com.wy.seckill.good.service.GoodsService;
import com.wy.seckill.goods.model.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Objects;

/**
 * 服务消费者
 */
@Controller
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 获取所有商品的信息
     */
    @RequestMapping("/getAllGoods")
    public String getAllGoods(Model model) {

        ReturnObject<List<Goods>> ro = goodsService.getAllGoods();

        model.addAttribute("goodsList", ro.getData());

        return "allGoods";
    }

    /**
     * 根据商品id获取具体消息
     */
    @RequestMapping("/goodsInfo")
    public String getGoodsInfo(Integer id, Model model) {

        ReturnObject<Goods> ro = goodsService.getGoodsInfo(id);

        model.addAttribute("goods", ro.getData());

        return "goodsInfo";
    }

    /**
     * 获取当前浏览器的时间
     *
     * @return
     */
    @RequestMapping("/getSystemTime")
    @ResponseBody
    public Object getSystemTime() {
        //java的时间无法到js当中使用，只能获取毫秒值了
        ReturnObject<Long> ro = new ReturnObject<>();

        ro.setCode(Constants.SUCCESS);
        ro.setMessage("获取系统时间成功");
        ro.setData(System.currentTimeMillis());//获取系统时间
        return ro;
    }

    /**
     * 获取商品的随机名称
     *
     * @param id
     * @return
     */
    @RequestMapping("/getRandomName")
    @ResponseBody
    public Object getRandomName(Integer id) {
        ReturnObject<Goods> goodsInfo = goodsService.getGoodsInfo(id);
        Goods goods = (Goods) goodsInfo.getData();

        ReturnObject<String> returnObject = new ReturnObject<>();

        //如果goods为空，可能是用户手动拼接的请求
        if (Objects.equals(null, goods)) {
            returnObject.setCode(Constants.ERROR);
            returnObject.setMessage("获取商品信息异常");
            return returnObject;
        }

        //还需要判断商品活动是否开始，原因：可能是用户手动拼接的请求
        long nowTome = System.currentTimeMillis();//当前系统时间
        if (nowTome < goods.getStartTime().getTime()) {
            returnObject.setCode(Constants.ERROR);
            returnObject.setMessage("活动还没有开始");
            return returnObject;
        }

        //还需要判断商品活动是否已经结束，原因：可能是用户手动拼接的请求
        //或者是用户停留在抢购页面过长时间，导致超时购买
        if (nowTome > goods.getEndTime().getTime()) {
            returnObject.setCode(Constants.ERROR);
            returnObject.setMessage("活动已结束");
            return returnObject;
        }

        returnObject.setCode(Constants.SUCCESS);
        returnObject.setMessage("获取随机名称成功");
        returnObject.setData(goods.getRandomName());
        return returnObject;

    }

    /**
     * 秒杀方法
     *
     * @param goodsId
     * @param randomName
     * @return
     */
    @RequestMapping("/secKill")
    @ResponseBody
    public Object secKill(Integer goodsId, String randomName) {
        //这里应该是用户登录后才能使用的方法
        //需要从用户的session当中获取id
        Integer id = 4396;
        ReturnObject ro = goodsService.secKill(goodsId, randomName, id);

        return ro;
    }
}
