package com.wy.seckill.common;

/**
 * 常量类
 */
public class Constants {

    /**
     * 成功
     */
    public static final String SUCCESS = "1";

    /**
     * 失败
     */
    public static final String ERROR = "0";
    /**
     * 商品随机名称在redis的key的前缀
     */
    public static final String GOODS_STORE = "GOODS_STORE:";
    /**
     * 用户的限购情况在redis的key的前缀
     */
    public static final String PURCHASE_RESTRICTIONS = "PURCHASE_RESTRICTIONS:";
    /**
     * 服务器限流
     */
    public static final String CURRENT_LIMITING = "CURRENT_LIMITING";
    /**
     * 订单数据在redis的前缀
     */
    public static final String ORDER = "ORDER:";
    /**
     * 下单结果
     */
    public static final String ORDER_RESULT = "ORDER_RESULT";
}
