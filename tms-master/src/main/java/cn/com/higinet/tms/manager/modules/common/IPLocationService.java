package cn.com.higinet.tms.manager.modules.common;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms35.core.dao.dao_ip;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

@Component("ipLocationService")
public class IPLocationService {
	@Autowired
	private DataSource dynamicDataSource;
	
	public String getLocationCurrName(){
		return getLocationCurrName(null);
	}
	
	public String getLocationOperName(){
		return getLocationOperName(null);
	}
	
	public String getLocationCurrName(String tabName){
		if(CmcStringUtil.isBlank(tabName)){
			return dao_ip.Instance(new data_source(dynamicDataSource)).curr_name();
		}else{
			return dao_ip.Instance(new data_source(dynamicDataSource)).curr_name(tabName);
		}
	}
	
	public String getLocationOperName(String tabName){
		if(CmcStringUtil.isBlank(tabName)){
			return dao_ip.Instance(new data_source(dynamicDataSource)).oper_name();
		}else{
			return dao_ip.Instance(new data_source(dynamicDataSource)).oper_name(tabName);
		}
	}
	
	public void reset(){
		dao_ip.reset();
	}
}