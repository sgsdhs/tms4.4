/*
 * Copyright © 2000 Shanghai XXX Co. Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms.manager.modules.ac.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.entity.common.RequestModel;
import cn.com.higinet.tms.engine.comm.web_tool;
import cn.com.higinet.tms.engine.core.cache.cache_init;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.manager.common.ManagerConstants;
import cn.com.higinet.tms.manager.modules.ac.service.AcService;
import cn.com.higinet.tms.manager.modules.common.exception.TmsMgrWebException;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;
import cn.com.higinet.tms.manager.modules.stat.service.StatService;

/**
 * 功能/模块:
 * @author 张立群
 * @version 1.0  May 31, 2013
 * 类描述:
 * 修订历史:
 * 日期  作者  参考  描述
 *
 */
@RestController("actionController35")
@RequestMapping(ManagerConstants.URI_PREFIX + "/action")
public class AcController {

	private static final Logger log = LoggerFactory.getLogger( AcController.class );

	@Autowired
	@Qualifier("dynamicDataSource")
	private DataSource dynamicDataSource;

	@Autowired
	AcService actionService35;

	@Autowired
	StatService statService;

	@Autowired
	private ObjectMapper objectMapper;

	/**
	* 方法描述:动作列表页面（已废弃）
	* @return
	 */
	/*@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String listActionView() {
		return "tms35/action/action_list";
	}*/

	/**
	* 方法描述:动作查询列表
	* @param reqs
	* @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public Model listAction( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		model.setRow( actionService35.listAction( reqs ) );
		return model;
	}

	/**
	* 方法描述:保存动作配置
	* @param reqs
	* @return
	 */
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public Model saveAcAction( @RequestBody Map<String, Object> reqs ) {

		String json = MapUtil.getString( reqs, "postData" );

		Map<String, List<Map<String, Object>>> formList = null;
		try {
			formList = objectMapper.readValue( json, Map.class );
		}
		catch( Exception e ) {
			log.error( e.getMessage(), e );
			throw new TmsMgrWebException( "保存动作Json数据解析异常" );
		}
		Model m = new Model();
		try {
			Map<String, List<Map<String, Object>>> formListUpperCase = saveParams2UpperCase(formList);
			m.setRow( actionService35.saveAc( formListUpperCase ) );
		}
		catch( Exception e ) {
			m.addError( e.getMessage() );
		}
		return m;
	}

	/**
	* 方法描述:编辑页面获取初始化信息（已废弃）
	* @param actionId
	* @return
	 */
	@RequestMapping(value = "/get", method = RequestMethod.POST)
	public Model getActoin( @RequestBody RequestModel modelMap ) {
		Model model = new Model();
		model.setRow( actionService35.getOneAction( modelMap.getString( "actionId" ) ) );
		return model;
	}

	/**
	* 方法描述: 有效性（已废弃）
	* @param stat_id
	* @param stat_status
	* @return
	 */
	@RequestMapping(value = "/validStat", method = RequestMethod.POST)
	public Model validAcAction( @RequestParam String[] stat_id, String stat_status ) {
		Model model = new Model();

		if( "0".equals( stat_status ) ) {
			stat_status = "1";
		}
		else if( "1".equals( stat_status ) ) {
			stat_status = "0";
		}

		actionService35.updateValidAc( stat_id, stat_status );
		return model;
	}

	/**
	* 方法描述:更新条件（已废弃）
	* @param statId
	* @return
	 */
	@RequestMapping(value = "/updateCond", method = RequestMethod.POST)
	public Model updateCondActoin( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		String ac_cond_value = MapUtil.getString( reqs, "AC_COND_VALUE" );
		String ac_cond_column = MapUtil.getString( reqs, "AC_COND_COLUMN" );
		Long ac_id = MapUtil.getLong( reqs, "AC_ID" );
		String txnid = MapUtil.getString( reqs, "TXNID" );
		StringBuffer error = new StringBuffer();

		// 校验条件的有效性
		boolean isTrue = checkCond( ac_cond_value, txnid, error );
		// 不正确弹出错误信息
		if( !isTrue ) {
			model.addError( error.toString() );
			return model;
		}

		Map<String, Object> input = new HashMap<String, Object>();
		input.put( "AC_ID", ac_id );
		input.put( "AC_COND_VALUE", ac_cond_value );
		input.put( "AC_COND_COLUMN", ac_cond_column );
		// 更新条件
		actionService35.updateAcCond( input );
		return model;
	}

	/**
	* 方法描述:校验条件是否被规则，动作，统计所引用
	* @param stat_cond_value
	* @param txnid
	* @param error
	* @return
	 */
	private boolean checkCond( String stat_cond_value, String txnid, StringBuffer error ) {
		if( StringUtil.isEmpty( stat_cond_value ) ) return true;
		// 初始化缓存
		cache_init.init( new data_source( dynamicDataSource ) );

		// 检查条件正确性
		boolean isTrue = web_tool.compile_expr( txnid, stat_cond_value, error );
		return isTrue;
	}

	private Map<String, List<Map<String, Object>>> saveParams2UpperCase(Map<String, List<Map<String, Object>>> formMap) {
		Map<String, List<Map<String, Object>>> resultMap = new HashMap<>();
		List<Map<String, Object>> delList = MapUtil.getList( formMap, "del" );
		List<Map<String, Object>> modList = MapUtil.getList( formMap, "mod" );
		List<Map<String, Object>> addList = MapUtil.getList( formMap, "add" );
		List<Map<String, Object>> copyList = MapUtil.getList( formMap, "copy" );

		List<Map<String, Object>> validYList = MapUtil.getList( formMap, "valid-y" );
		List<Map<String, Object>> validNList = MapUtil.getList( formMap, "valid-n" );

		List<Map<String, Object>> delListTemp = new ArrayList<>();
		List<Map<String, Object>> modListTemp = new ArrayList<>();
		List<Map<String, Object>> addListTemp = new ArrayList<>();
		List<Map<String, Object>> copyListTemp = new ArrayList<>();
		List<Map<String, Object>> validYListTemp = new ArrayList<>();
		List<Map<String, Object>> validNListTemp = new ArrayList<>();

		if(delList != null){
			for(Map<String, Object> loop : delList) {
				Map<String, Object> loopMapTemp = new HashMap<>();
				for (Map.Entry<String, Object> entry : loop.entrySet()) {
					loopMapTemp.put(entry.getKey().toUpperCase(), entry.getValue());
				}
				delListTemp.add(loopMapTemp);
			}
			resultMap.put("del", delListTemp);
		}
		if(modList != null){
			for(Map<String, Object> loop : modList) {
				Map<String, Object> loopMapTemp = new HashMap<>();
				for (Map.Entry<String, Object> entry : loop.entrySet()) {
					loopMapTemp.put(entry.getKey().toUpperCase(), entry.getValue());
				}
				modListTemp.add(loopMapTemp);
			}
			resultMap.put("mod", modListTemp);
		}
		if(addList != null){
			for(Map<String, Object> loop : addList) {
				Map<String, Object> loopMapTemp = new HashMap<>();
				for (Map.Entry<String, Object> entry : loop.entrySet()) {
					loopMapTemp.put(entry.getKey().toUpperCase(), entry.getValue());
				}
				addListTemp.add(loopMapTemp);
			}
			resultMap.put("add", addListTemp);
		}
		if(copyList != null){
			for(Map<String, Object> loop : copyList) {
				Map<String, Object> loopMapTemp = new HashMap<>();
				for (Map.Entry<String, Object> entry : loop.entrySet()) {
					loopMapTemp.put(entry.getKey().toUpperCase(), entry.getValue());
				}
				copyListTemp.add(loopMapTemp);
			}
			resultMap.put("copy", copyListTemp);
		}
		if(validYList != null){
			for(Map<String, Object> loop : validYList) {
				Map<String, Object> loopMapTemp = new HashMap<>();
				for (Map.Entry<String, Object> entry : loop.entrySet()) {
					loopMapTemp.put(entry.getKey().toUpperCase(), entry.getValue());
				}
				validYListTemp.add(loopMapTemp);
			}
			resultMap.put("valid-y", validYListTemp);
		}
		if(validNList != null){
			for(Map<String, Object> loop : validNList) {
				Map<String, Object> loopMapTemp = new HashMap<>();
				for (Map.Entry<String, Object> entry : loop.entrySet()) {
					loopMapTemp.put(entry.getKey().toUpperCase(), entry.getValue());
				}
				validNListTemp.add(loopMapTemp);
			}
			resultMap.put("valid-n", validNListTemp);
		}
		return resultMap;
	}
}
