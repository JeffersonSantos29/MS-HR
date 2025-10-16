package com.mshr.hrapigatewayspringcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@EnableDiscoveryClient
@SpringBootApplication
public class HrApiGatewaySpringCloudApplication {

	public static void main(String[] args) {
		SpringApplication.run(HrApiGatewaySpringCloudApplication.class, args);
	}

}
