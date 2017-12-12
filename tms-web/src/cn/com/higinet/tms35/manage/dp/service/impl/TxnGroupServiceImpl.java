/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms35.manage.dp.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.com.higinet.rapid.base.dao.Order;
import cn.com.higinet.rapid.base.dao.SimpleDao;
import cn.com.higinet.tms35.manage.common.DBConstant;
import cn.com.higinet.tms35.manage.dp.service.TxnGroupService;

/**
 * 交易树服务类
 * 
 * @author zhangfg
 * @date 2011-7-7 
 * @version 2.0.0
 */
@Service("tmsTxnGroupService")
public class TxnGroupServiceImpl implements TxnGroupService {

	@Autowired
	private SimpleDao tmsSimpleDao;

	/**
	 * 获得所有渠道信息
	 * @return
	 */
	public List<Map<String, Object>> listAllChannel(Map<String, String> conds) {
		List<Map<String, Object>> channellist = tmsSimpleDao
				.listAll(DBConstant.TMS_DP_CHANNEL);
		return channellist;
	}
	
	/**
	 * 获得所有渠道信息,order by orderby
	 * @return
	 */
	public List<Map<String, Object>> listAllChannelWithOrder() {
		
		List<Map<String, Object>> channellist = tmsSimpleDao.listAll(DBConstant.TMS_DP_CHANNEL, new Order().asc("ORDERBY"));
		return channellist;
	}

}
