package com.wy.seckill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableEurekaClient
@MapperScan("com.wy.seckill.orders.mapper")
@EnableFeignClients
public class OrderS {

	public static void main(String[] args) {
		SpringApplication.run(OrderS.class, args);
	}

}
