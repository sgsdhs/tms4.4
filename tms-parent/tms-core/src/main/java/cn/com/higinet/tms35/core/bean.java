package cn.com.higinet.tms35.core;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service("bean_fac")
public class bean implements ApplicationContextAware {
	private static ApplicationContext context;

	final public static <E> E get(Class<E> c) {
		try {
			return (E) context.getBean(c);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	final public static Object get(String name) {
		return context.getBean(name);
	}

	final public static boolean containsBean(String name) {
		return context.containsBean(name);
	}

	public void setApplicationContext(ApplicationContext context) throws BeansException {
		bean.context = context;
	}

	final static public DataSource get_DataSource() {
		return (DataSource) get("tmsDataSource");
	}

	final static public JdbcTemplate get_JdbcTemplate() {
		return (JdbcTemplate) get("tmsJdbcTemplate");
	}

	final static public DataSource get_DataSource_tmp() {
		return (DataSource) get("tmpDataSource");
	}
}
