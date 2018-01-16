package cn.com.higinet.tms.common.elasticsearch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({ ElasticSearchConfig.class, ElasticSearchAdapter.class })
public @interface EnableElasticSearch {

}
