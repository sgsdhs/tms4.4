package cn.com.higinet.tms35.manage.mgr.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.com.higinet.rapid.base.dao.Order;
import cn.com.higinet.rapid.base.dao.Page;
import cn.com.higinet.rapid.base.dao.SimpleDao;
import cn.com.higinet.rapid.base.util.MapWrap;
import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tms35.manage.common.util.StringUtil;
import cn.com.higinet.tms35.manage.mgr.service.BankQuotaService;

/**
 * 通道限额管理服务类
 * @author tlh
 */

@Service("bankQuotaService")
public class  BankQuotaServiceImpl implements BankQuotaService{

	@Autowired
	private SimpleDao tmsSimpleDao; 
	
	
	/**
	 * 通道限额列表查询
	 * @param conds 所有查询条件
	 * @return 通道限额条件查询的所有信息
	 */
	@Override
	public Page<Map<String, Object>> listBankQuotaPage(Map<String, String> conds) {
		StringBuilder sql = new StringBuilder();
		sql.append("select ID,BANK_CODE,CARD_TYPE,BUSINESS_TYPE,ONE_QUOTA,DAY_QUOTA,MONTH_QUOTA,DAY_TIMES,MONTH_TIMES from TMS_RUN_BANK_QUOTA ");
		if(!StringUtil.isEmpty(conds.get("ID"))){
			sql.append(" AND ID =:ID");
		}if(!StringUtil.isEmpty(conds.get("BANK_CODE"))){
			sql.append(" AND BANK_CODE =:BANK_CODE");
		}if(!StringUtil.isEmpty(conds.get("CARD_TYPE"))){
			sql.append(" AND CARD_TYPE=:CARD_TYPE");
		}if(!StringUtil.isEmpty(conds.get("BUSINESS_TYPE"))){
			sql.append(" AND BUSINESS_TYPE=:BUSINESS_TYPE");
		}
		Page<Map<String, Object>> bankQuotaPage = tmsSimpleDao.pageQuery(sql.toString().replaceFirst("AND", "WHERE"), conds, new Order().asc("BANK_CODE"));		
		return bankQuotaPage;
	}

	/**
	 * 根据id删除通道限额信息
	 * @param conds 包含需删除的id
	 */
	@Override
	public void delBankQuotaList(Map<String,List<Map<String,String>>> batchMap) {
		
		List<Map<String, String>> delList = batchMap.get("del");
		for(Map<String,String> delMap : delList){
			Map<String, String> conds = new HashMap<String, String>();
			conds.put("ID", MapUtil.getString(delMap, "ID"));
			tmsSimpleDao.delete("TMS_RUN_BANK_QUOTA",conds);
		}	
		
	}
	
	/**
	 * 插入单条通道限额信息
	 * @param conds 所有需插入的信息
	 */
	public void addBankQuota(Map<String, String> reqs){
		StringBuilder sql = new StringBuilder();
		sql.append("insert into TMS_RUN_BANK_QUOTA (ID,BANK_CODE,CARD_TYPE, BUSINESS_TYPE,ONE_QUOTA,DAY_QUOTA,MONTH_QUOTA,DAY_TIMES,MONTH_TIMES) values (:ID,:BANK_CODE,:CARD_TYPE, :BUSINESS_TYPE,:ONE_QUOTA,:DAY_QUOTA,:MONTH_QUOTA,:DAY_TIMES,:MONTH_TIMES)");
		tmsSimpleDao.executeUpdate(sql.toString(), reqs);
	}
	
	/**
	 * 根据id查询单条通道限额信息
	 * @param  所需的id
	 * @return 单条通道限额信息
	 */
	public Map<String, Object> getBankQuotaById(String id){
		
		Map<String, Object> List = tmsSimpleDao.retrieve("TMS_RUN_BANK_QUOTA", MapWrap.map("ID", id).getMap());
		return List;
	}
	
	/**
	 * 根据id更新单条通道限额信息
	 * @param conds 包含所需的id
	 */
	public void updateBankQuotaById(Map<String, String> req){
		StringBuilder sql = new StringBuilder();
		sql.append("update TMS_RUN_BANK_QUOTA set BANK_CODE=:BANK_CODE,CARD_TYPE=:CARD_TYPE,BUSINESS_TYPE=:BUSINESS_TYPE,ONE_QUOTA=:ONE_QUOTA,DAY_QUOTA=:DAY_QUOTA,MONTH_QUOTA=:MONTH_QUOTA,DAY_TIMES=:DAY_TIMES,MONTH_TIMES=:MONTH_TIMES where ID=:ID");
		tmsSimpleDao.executeUpdate(sql.toString(), req);
	}
}
