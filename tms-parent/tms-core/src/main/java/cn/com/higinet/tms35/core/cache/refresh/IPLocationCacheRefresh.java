/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  IPLocationCacheRefresh.java   
 * @Package cn.com.higinet.tms35.core.cache.refresh   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-12-12 13:28:33   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms35.core.cache.refresh;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.core.cache.ip_cache;
import cn.com.higinet.tms35.core.dao.dao_ip;

/**
 * IP地址刷新
 *
 * @ClassName:  IPLocationCacheRefresh
 * @author: 王兴
 * @date:   2017-12-12 13:28:33
 * @since:  v4.3
 */
public class IPLocationCacheRefresh implements CacheRefresh {
	
	/** 常量 log. */
	private static final Logger log = LoggerFactory.getLogger(UserPatternCacheRefresh.class);

	/** 常量 NAME. */
	private static final String NAME = "TMS_MGR_IPLOCATION";

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.core.cache.refresh.CacheRefresh#refresh(java.lang.String, java.util.Map, int, cn.com.higinet.tms35.core.cache.refresh.RefreshChain)
	 */
	@Override
	public boolean refresh(String cacheName, Map<String, Object> params, int index, RefreshChain chain) throws Exception {
		if (NAME.equals(cacheName)) {
			log.debug("iplocation cache refresh");
			dao_ip.reset();
			ip_cache.reset();
			ip_cache.Instance();
			return true;
		} else {
			return chain.refresh(cacheName, params, ++index);
		}
	}
}
