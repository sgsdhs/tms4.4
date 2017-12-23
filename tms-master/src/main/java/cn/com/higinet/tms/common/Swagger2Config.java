package cn.com.higinet.tms.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author 张磊
 */

@Configuration
@EnableSwagger2
public class Swagger2Config {

	@Bean
	public Docket createRestApi() {
		return new Docket( DocumentationType.SWAGGER_2 ).apiInfo( apiInfo() ).select().apis( RequestHandlerSelectors.basePackage( "cn.com.higinet.tms" ) ).paths( PathSelectors.any() ).build();
	}

	@SuppressWarnings("deprecation")
	private ApiInfo apiInfo() {
		return new ApiInfoBuilder().title( "SOAM RESTful APIs" ).description( "" ).contact( "" ).version( "1.0" ).build();
	}
}