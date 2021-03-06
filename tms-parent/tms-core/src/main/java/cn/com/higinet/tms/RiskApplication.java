package cn.com.higinet.tms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

import cn.com.higinet.tms.config.StatBeans;

@SpringBootApplication
@EnableDiscoveryClient
@PropertySource("classpath:riskConfig.properties")
@ImportResource(locations = { "classpath:service-context.xml" })
@ComponentScan(basePackages = { "cn.com.higinet.tms*" }, excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = { StatApplication.class, StatBootstrap.class, StatBeans.class, DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class }))
public class RiskApplication {
	public static void main(String[] args) {
		SpringApplication.run(RiskApplication.class, args);
	}
}
