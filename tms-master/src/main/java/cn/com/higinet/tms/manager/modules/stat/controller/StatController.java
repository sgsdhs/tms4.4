/*
 * Copyright © 2000 Shanghai XXX Co. Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms.manager.modules.stat.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.entity.common.Page;
import cn.com.higinet.tms.base.entity.common.RequestModel;
import cn.com.higinet.tms.engine.comm.web_tool;
import cn.com.higinet.tms.engine.core.cache.cache_init;
import cn.com.higinet.tms.engine.core.cache.db_process;
import cn.com.higinet.tms.engine.core.cache.db_rule;
import cn.com.higinet.tms.engine.core.cache.db_rule_action;
import cn.com.higinet.tms.engine.core.cache.db_stat;
import cn.com.higinet.tms.engine.core.cache.db_strategy;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.manager.common.ManagerConstants;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.modules.common.CommonCheckService;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.common.DBConstant.TMS_COM_TAB;
import cn.com.higinet.tms.manager.modules.common.exception.TmsMgrWebException;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;
import cn.com.higinet.tms.manager.modules.stat.service.StatService;
import cn.com.higinet.tms.manager.modules.tran.TransCommon;
import cn.com.higinet.tms.manager.modules.tran.service.TransModelService;

/**
 * 功能/模块:交易配置-统计
 * @author 张立群
 * @version 1.0  Apr 26, 2013
 * 类描述:统计业务类
 * 
 * @author zhang.lei
 */

@RestController("statController")
@RequestMapping(ManagerConstants.URI_PREFIX + "/stat")
public class StatController {

	private static final Logger logger = LoggerFactory.getLogger( StatController.class );
	@Autowired
	private StatService statService;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	@Qualifier("dynamicDataSource")
	private DataSource dynamicDataSource;
	@Autowired
	private CommonCheckService commonCheckService;
	@Autowired
	@Qualifier("onlineSimpleDao")
	private SimpleDao onlineSimpleDao;
	@Autowired
	private TransModelService transModelService;

	/**
	* 方法描述:查询统计列表
	* @param reqs
	* @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public Model listStatAction( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		Page<Map<String, Object>> statList = statService.statList( reqs );
		model.setPage( statList );
		model.set( "enableStoreFd", transModelService.getAvailableStoreFd( MapUtil.getString( reqs, "txnId" ) ) );
		model.set( "allStoreFd", transModelService.getAllStoreFd() );
		return model;
	}

	/**
	* 方法描述:保存统计配置
	* @param reqs
	* @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public Model saveStatAction( @RequestBody Map<String, Object> reqs ) {
		String json = MapUtil.getString( reqs, "postData" );
		Map<String, List<Map<String, ?>>> formList = null;
		try {
			formList = objectMapper.readValue( json, Map.class );
		}
		catch( Exception e ) {
			logger.error( e.getMessage(), e );
			throw new TmsMgrWebException( "保存统计Json数据解析异常" );
		}
		Model m = new Model();
		try {
			Map<String, List<Map<String, ?>>> formListUpperCase = saveParams2UpperCase(formList);
			m.setRow( statService.saveStat( formListUpperCase ) );
		}
		catch( Exception e ) {
			logger.error( e.getMessage(), e );
			m.addError( e.getMessage() );
		}
		return m;
	}

	/**
	* 方法描述:查询交易的属性信息(已废弃)
	* @param reqs
	* @return
	 */
	@RequestMapping(value = "/txnFeatureData", method = RequestMethod.POST)
	public Model queryTxnFeatureData( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		model.setRow( statService.queryTxnFeature( reqs ) );
		return model;
	}

	/**
	* 方法描述:查询统计目标下拉列表
	* @return
	 */
	@RequestMapping(value = "/code", method = RequestMethod.POST)
	public Model queryCode(@RequestBody RequestModel requestModel) {
		Model model = new Model();
		String categoryId = requestModel.getString("category_id");
		String args =  requestModel.getString("args");
		String [] argsArr = null;
		if(StringUtil.isNotEmpty(args)){
			argsArr = new String[]{args};
		}
		model.setRow( statService.codeList( categoryId, argsArr));
		return model;
	}

	/**
	* 方法描述:查询统计目标下拉列表（已废弃)
	* @return
	 */
	@RequestMapping(value = "/codeList", method = RequestMethod.POST)
	public Model queryCodeList( @RequestBody String category_ids ) {
		Model model = new Model();
		Map<String, List<Map<String, Object>>> codeMap = new HashMap<String, List<Map<String, Object>>>();

		String[] caids = category_ids.split( "|" );
		for( String args : caids ) {
			String[] p = args.split( "," );
			String[] a = null;
			for( int i = 0; i < p.length; i++ ) {
				if( i == 0 ) continue;
				if( a == null ) {
					a = new String[p.length - 1];
				}
				a[i - 1] = p[i];
			}
			codeMap.put( args, statService.codeList( p[0], a ) );
		}

		model.setRow( codeMap );
		return model;
	}

	/**
	* 方法描述:查询单个统计信息（已废弃）
	* @param statId
	* @return
	 */
	@RequestMapping(value = "/get")
	public Model getStatActoin( @RequestBody String statId ) {
		Model model = new Model();
		model.setRow( statService.getOneStat( statId ) );
		return model;
	}

	/**
	* 方法描述:初始化条件页面（已废弃）
	* @param statId
	* @return
	 */
	@RequestMapping(value = "/getCond", method = RequestMethod.POST)
	public Model getStaCondtActoin( @RequestBody String statId ) {
		Model model = new Model();
		model.setRow( statService.getOneStatForCond( statId ) );
		return model;
	}

	/**
	* 方法描述:校验是否被引用（已废弃）
	* @param reqs
	* @return
	 */
	@RequestMapping(value = "/checkRef", method = RequestMethod.POST)
	public Model checkRefActoin( @RequestBody Map<String, String> reqs ) {

		String operation = MapUtil.getString( reqs, "MARK" );
		String stat_name = MapUtil.getString( reqs, DBConstant.TMS_COM_STAT_STAT_NAME );
		String txnId = MapUtil.getString( reqs, DBConstant.TMS_COM_STAT_STAT_TXN );

		Model model = new Model();
		if( !operation.equals( "A" ) ) {
			// 校验是否被引用
			// 初始化缓存
			commonCheckService.initCache( dynamicDataSource );
			// 缓存中查找引用
			if( commonCheckService.find_ref_stat( txnId, stat_name ) ) return model.addError( "[" + stat_name + "]被引用！" );
		}
		return model;
	}

	/**
	* 方法描述:校验条件有效性
	* @param reqs
	* @return
	 */
	@RequestMapping(value = "/checkCond", method = RequestMethod.POST)
	public Model checkCondActoin( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		String stat_cond_value = MapUtil.getString( reqs, "STAT_COND_VALUE" );
		String txnid = MapUtil.getString( reqs, "TXNID" );
		StringBuffer error = new StringBuffer();

		try {
			// 校验条件的有效性
			boolean isTrue = checkCond( stat_cond_value, txnid, error );
			// 不正确弹出错误信息
			if( !isTrue ) {
				model.addError( error.toString() );
				return model;
			}
		}
		catch( Exception e ) {
			logger.error( e.getMessage(), e );
			throw new TmsMgrWebException( "条件错误，由于" + e.getLocalizedMessage() );
		}

		return model;
	}

	/**
	* 方法描述:校验条件有效性
	* @param reqs
	* @return
	 */
	@RequestMapping(value = "/statfunc", method = RequestMethod.POST)
	public Model statfunc( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		String stat_sql = "SELECT STAT_ID ID,STAT_TXN,STAT_NAME, stat_txn fid,STAT_DESC CODE_VALUE,'2' ftype,STAT_TXN FROM TMS_COM_STAT where STAT_VALID =1";
		// 查询统计
		List<Map<String, Object>> stat_list = onlineSimpleDao.queryForList( stat_sql );

		if( stat_list == null || stat_list.size() == 0 ) {
			model.setRow( stat_list );
			return model;
		}

		String txn_id = "";
		Map<String, String> id_m = new HashMap<String, String>();
		for( Map<String, Object> map : stat_list ) {
			String txnid = MapUtil.getString( map, "fid" );
			String stat_txn = MapUtil.getString( map, "STAT_TXN" );
			String stat_name = MapUtil.getString( map, "STAT_NAME" );
			map.put( "CODE_KEY", stat_txn + ":" + stat_name );
			String[] id_arr = TransCommon.cutToIds( txnid );
			
			for( String id : id_arr ) {
				id_m.put( id, id );
			}
		}

		Set<String> key = id_m.keySet();
		for( String s : key ) {
			if( txn_id.length() > 0 ) {
				txn_id += ",";
			}
			txn_id += "'" + s + "'";
		}

		String txn_sql = "SELECT TAB_NAME ID,M.TAB_NAME CODE_KEY,m.parent_tab fid,m.tab_desc CODE_VALUE ,'1' ftype, TAB_NAME STAT_TXN FROM TMS_COM_TAB M WHERE M.TAB_NAME in (" + txn_id + ") order by STAT_TXN";
		// 查询交易树
		List<Map<String, Object>> txn_list = onlineSimpleDao.queryForList( txn_sql );

		stat_list.addAll( txn_list );
		model.setRow( stat_list );
		return model;
	}

	/**
	 * 方法描述:校验条件有效性
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/txnrulelist", method = RequestMethod.POST)
	public Model txnrulelist( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		String txn_id = MapUtil.getString( reqs, "txn_id" );

		String txn_rule_sql = "SELECT RULE_TXN,RULE_NAME, RULE_SHORTDESC CODE_VALUE,R.RULE_TXN P FROM TMS_COM_RULE R WHERE RULE_ENABLE = 1 AND RULE_TXN IN (" + TransCommon.arr2str( TransCommon.cutToIds( txn_id ) ) + ")  ORDER BY RULE_TXN";
		List<Map<String, Object>> txn_rule_list = onlineSimpleDao.queryForList( txn_rule_sql );

		if( txn_rule_list != null && txn_rule_list.size() > 0 ) {
			for( Map<String, Object> map : txn_rule_list ) {
				String rule_txn = MapUtil.getString( map, "RULE_TXN" );
				String rule_name = MapUtil.getString( map, "RULE_NAME" );
				map.put( "CODE_KEY", rule_txn + ":" + rule_name );
			}
		}
		model.setRow( txn_rule_list );
		return model;
	}

	/**
	 * 方法描述:校验条件有效性
	 * @param reqs
	 * @return
	 */
	@RequestMapping(value = "/txnFeature", method = RequestMethod.POST)
	public Model txnFeature( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		String txn_id = MapUtil.getString( reqs, "txn_id" );

		String txn_rule_sql = "SELECT CODE_KEY, CODE_VALUE, type, code fd_code FROM (SELECT REF_NAME CODE_KEY, NAME CODE_VALUE, type, code, TAB_NAME FROM TMS_COM_FD UNION SELECT crf.REF_NAME CODE_KEY, crf.REF_DESC CODE_VALUE, cf.type TYPE, cf.code code, crf.TAB_NAME from TMS_COM_REFFD crf left join tms_com_reftab crt on crf.ref_id = crt.ref_id left join tms_com_fd cf on crt.ref_tab_name = cf.tab_name and crf.ref_name = cf.fd_name) F where TAB_NAME in ("
				+ TransCommon.arr2str( TransCommon.cutToIds( txn_id ) ) + ") ORDER BY TAB_NAME";

		List<Map<String, Object>> txn_rule_list = onlineSimpleDao.queryForList( txn_rule_sql );

		model.setRow( txn_rule_list );
		return model;
	}

	private boolean checkCond( String stat_cond_value, String txnid, StringBuffer error ) {
		if( StringUtil.isEmpty( stat_cond_value ) ) return true;
		// 初始化缓存
		cache_init.init( new data_source( dynamicDataSource ) );
		// 检查条件正确性
		boolean isTrue = web_tool.compile_expr( txnid, stat_cond_value, error );
		return isTrue;
	}

	/**
	* 方法描述:查询统计列表
	* @param reqs
	* @return
	 */
	@RequestMapping(value = "/refTree", method = RequestMethod.POST)
	public Model refTreeAction( @RequestBody Map<String, Object> reqs ) {
		String txn_id = MapUtil.getString( reqs, "txnId" );
		String stat_name = MapUtil.getString( reqs, "statName" );
		String type = MapUtil.getString( reqs, "type" );

		// 初始化缓存
		try {
			cache_init.init( new data_source( dynamicDataSource ) );
		}
		catch( Exception e ) {
			logger.error( e.getMessage(), e );
			throw new TmsMgrWebException( "初始化缓存错误，由于" + e.getLocalizedMessage() );
		}

		// 缓存中查找引用
		List<db_stat> ref_stat = new ArrayList<db_stat>();
		List<db_rule> ref_rule = new ArrayList<db_rule>();
		List<db_strategy> ref_sw = new ArrayList<db_strategy>();
		List<db_rule_action> ref_ac = new ArrayList<db_rule_action>();
		List<db_process> ref_ps = new ArrayList<db_process>();
		try {
			if( type != null && type.equals( "0" ) ) {
				commonCheckService.find_ref_rule( txn_id, stat_name, ref_ac );
			}
			else {
				commonCheckService.find_ref_stat( txn_id, stat_name, ref_stat, ref_rule, ref_sw, ref_ac, ref_ps );
			}

		}
		catch( Exception e ) {
			logger.error( e.getMessage(), e );
			throw new TmsMgrWebException( "查找引用错误，由于" + e.getLocalizedMessage() );
		}

		// 引用树
		List<Map<String, Object>> treeCon = new ArrayList<Map<String, Object>>();

		Map<String, String> txnidMap = new HashMap<String, String>();

		if( ref_stat != null && ref_stat.size() > 0 ) {

			Map<String, Object> txnsMap = new HashMap<String, Object>();

			// 一级以下的节点
			for( db_stat stat : ref_stat ) {

				txnidMap.put( stat.stat_txn, stat.stat_txn );

				// 组织引用统计节点
				Map<String, Object> treeMap = new HashMap<String, Object>();
				treeMap.put( "text", stat.stat_name + ":" + stat.stat_desc );
				treeMap.put( "id", stat.stat_id + "_1" );
				treeMap.put( "fid", stat.stat_txn + "_1" );
				treeMap.put( "ftype", "2" );
				treeMap.put( "flag", "1" );
				treeMap.put( "parentname", "" );
				treeCon.add( treeMap );

				if( !MapUtil.isContainsKey( txnsMap, stat.stat_txn ) ) {
					// 一级节点
					Map<String, Object> oneTreeMap = new HashMap<String, Object>();
					oneTreeMap.put( "text", "交易统计" );
					oneTreeMap.put( "id", stat.stat_txn + "_1" );
					oneTreeMap.put( "fid", stat.stat_txn );
					oneTreeMap.put( "ftype", "1" );
					oneTreeMap.put( "flag", "1" );
					oneTreeMap.put( "parentname", "" );
					treeCon.add( oneTreeMap );

					txnsMap.put( stat.stat_txn, stat.stat_txn );
				}
			}

		}

		if( ref_rule != null && ref_rule.size() > 0 ) {
			Map<String, Object> txnsMap = new HashMap<String, Object>();
			// 一级以下的节点
			for( db_rule rule : ref_rule ) {

				txnidMap.put( rule.txn_name, rule.txn_name );

				// 组织引用统计节点
				Map<String, Object> treeMap = new HashMap<String, Object>();
				treeMap.put( "text", rule.code + ":" + rule.name );
				treeMap.put( "id", rule.id + "_2" );
				treeMap.put( "fid", rule.txn_name + "_2" );
				treeMap.put( "ftype", "2" );
				treeMap.put( "flag", "1" );
				treeMap.put( "parentname", "" );
				treeCon.add( treeMap );

				if( !MapUtil.isContainsKey( txnsMap, rule.txn_name ) ) {
					// 一级节点
					Map<String, Object> oneTreeMap = new HashMap<String, Object>();
					oneTreeMap.put( "text", "交易规则" );
					oneTreeMap.put( "id", rule.txn_name + "_2" );
					oneTreeMap.put( "fid", rule.txn_name );
					oneTreeMap.put( "ftype", "1" );
					oneTreeMap.put( "flag", "1" );
					oneTreeMap.put( "parentname", "" );
					treeCon.add( oneTreeMap );

					txnsMap.put( rule.txn_name, rule.txn_name );
				}
			}

		}

		if( ref_ac != null && ref_ac.size() > 0 ) {
			// 一级以下的节点
			for( db_rule_action ac : ref_ac ) {

				// 组织引用统计节点
				Map<String, Object> treeMap = new HashMap<String, Object>();
				treeMap.put( "text", ac.ac_name );
				treeMap.put( "id", ac.ac_id + "_4" );
				treeMap.put( "fid", ac.rule_id + "_2" );
				treeMap.put( "ftype", "2" );
				treeMap.put( "flag", "1" );
				treeMap.put( "parentname", "" );
				treeCon.add( treeMap );

			}
		}

		if( ref_sw != null && ref_sw.size() > 0 ) {
			Map<String, Object> txnsMap = new HashMap<String, Object>();
			// 一级以下的节点
			for( db_strategy sw : ref_sw ) {

				txnidMap.put( sw.st_txn, sw.st_txn );

				// 组织引用统计节点
				Map<String, Object> treeMap = new HashMap<String, Object>();
				treeMap.put( "text", sw.st_name );
				treeMap.put( "id", sw.st_id + "_3" );
				treeMap.put( "fid", sw.st_txn + "_3" );
				treeMap.put( "ftype", "2" );
				treeMap.put( "flag", "1" );
				treeMap.put( "parentname", "" );
				treeCon.add( treeMap );

				if( !MapUtil.isContainsKey( txnsMap, sw.st_txn ) ) {
					// 一级节点
					Map<String, Object> oneTreeMap = new HashMap<String, Object>();
					oneTreeMap.put( "text", "交易开关" );
					oneTreeMap.put( "id", sw.st_txn + "_3" );
					oneTreeMap.put( "fid", sw.st_txn );
					oneTreeMap.put( "ftype", "1" );
					oneTreeMap.put( "flag", "1" );
					oneTreeMap.put( "parentname", "" );
					treeCon.add( oneTreeMap );

					txnsMap.put( sw.st_txn, sw.st_txn );
				}

			}
		}
		if( ref_ps != null && ref_ps.size() > 0 ) {
			Map<String, Object> txnsMap = new HashMap<String, Object>();
			// 一级以下的节点
			for( db_process ps : ref_ps ) {

				txnidMap.put( ps.ps_txn, ps.ps_txn );

				// 组织引用统计节点
				Map<String, Object> treeMap = new HashMap<String, Object>();
				treeMap.put( "text", ps.ps_name );
				treeMap.put( "id", ps.ps_id + "_5" );
				treeMap.put( "fid", ps.ps_txn + "_5" );
				treeMap.put( "ftype", "2" );
				treeMap.put( "flag", "1" );
				treeMap.put( "parentname", "" );
				treeCon.add( treeMap );

				if( !MapUtil.isContainsKey( txnsMap, ps.ps_txn ) ) {
					// 一级节点
					Map<String, Object> oneTreeMap = new HashMap<String, Object>();
					oneTreeMap.put( "text", "交易处置" );
					oneTreeMap.put( "id", ps.ps_txn + "_5" );
					oneTreeMap.put( "fid", ps.ps_txn );
					oneTreeMap.put( "ftype", "1" );
					oneTreeMap.put( "flag", "1" );
					oneTreeMap.put( "parentname", "" );
					treeCon.add( oneTreeMap );

					txnsMap.put( ps.ps_txn, ps.ps_txn );
				}
			}
		}

		if( !MapUtil.isEmpty( txnidMap ) ) {
			// 组织所属交易节点
			buildTxnTreeByTxnId( treeCon, txnidMap );
		}

		Model model = new Model();
		model.setList( treeCon );
		return model;
	}

	private void buildTxnTreeByTxnId( List<Map<String, Object>> treeCon, Map<String, String> txnidMap ) {
		// 组织所属交易节点
		Map<String, Object> tab = new HashMap<String, Object>();
		Set<String> set = txnidMap.keySet();
		StringBuilder txnids = new StringBuilder();

		Map<String, String> id_map = new HashMap<String, String>();
		for( String k : set ) {
			String[] o = TransCommon.cutToIds( k );
			for( String s : o ) {
				id_map.put( s, s );
			}
		}

		Set<String> id_set = id_map.keySet();
		for( String id : id_set ) {
			if( txnids.length() > 0 ) {
				txnids.append( "," );
			}
			txnids.append( "'" ).append( id ).append( "'" );
		}

		tab.put( "tab_name", txnids.toString() );

		List<Map<String, Object>> txnTreeList = statService.getTxnTree( tab );
		for( Map<String, Object> map : txnTreeList ) {
			// 组织所属交易节点
			Map<String, Object> treeMap = new HashMap<String, Object>();
			treeMap.put( "text", MapUtil.getString( map, TMS_COM_TAB.TAB_DESC ) );
			treeMap.put( "id", MapUtil.getString( map, TMS_COM_TAB.TAB_NAME ) );
			String fid = MapUtil.getString( map, TMS_COM_TAB.PARENT_TAB );
			treeMap.put( "fid", StringUtil.isEmpty( fid ) ? "-1" : fid );
			treeMap.put( "ftype", "0" );
			treeMap.put( "flag", "1" );
			treeMap.put( "parentname", "" );
			treeCon.add( treeMap );
		}
	}

	private Map<String, List<Map<String, ?>>> saveParams2UpperCase(Map<String, List<Map<String, ?>>> formMap) {
		Map<String, List<Map<String, ?>>> resultMap = new HashMap<>();
		List<Map<String, Object>> delList = MapUtil.getList( formMap, "del" );
		List<Map<String, Object>> modList = MapUtil.getList( formMap, "mod" );
		List<Map<String, Object>> addList = MapUtil.getList( formMap, "add" );
		List<Map<String, Object>> validYList = MapUtil.getList( formMap, "valid-y" );
		List<Map<String, Object>> validNList = MapUtil.getList( formMap, "valid-n" );

		List<Map<String, ?>> delListTemp = new ArrayList<>();
		List<Map<String, ?>> modListTemp = new ArrayList<>();
		List<Map<String, ?>> addListTemp = new ArrayList<>();
		List<Map<String, ?>> validYListTemp = new ArrayList<>();
		List<Map<String, ?>> validNListTemp = new ArrayList<>();

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
