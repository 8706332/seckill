package com.wy.seckill.common;

/**
 *  json统一返回值对象
 */
public class ReturnObject<T> {

   private String code;
   private String message;
   private T data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ReturnObject(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ReturnObject() {
    }
}
