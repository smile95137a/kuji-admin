package com.group.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.group.admin", exclude = {
		org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration.class
})
@MapperScan("com.group.admin.mapper")
public class AdminApplication {
	public static void main(String[] args) {
		SpringApplication.run(AdminApplication.class, args);
	}
}
