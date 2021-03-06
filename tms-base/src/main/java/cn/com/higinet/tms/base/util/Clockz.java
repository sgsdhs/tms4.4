package cn.com.higinet.tms.base.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 计时工具类
 * 支持多线程使用
 * @author zhang.lei
 * */
public class Clockz {

	private static Map<Object, Long> timezs = new LinkedHashMap<Object, Long>( 2000 );

	public static Long now() {
		return System.currentTimeMillis();
	}

	public static Long start() {
		return start( Thread.currentThread().getId() );
	}

	public static Long stop() {
		return stop( Thread.currentThread().getId() );
	}

	public static Long start( Object key ) {
		Long time = System.currentTimeMillis();
		timezs.put( key, time );
		return time;
	}

	public static Long stop( Object key ) {
		Long startTime = timezs.remove( key );
		if( startTime != null ) return System.currentTimeMillis() - startTime;
		else return null;
	}
}
