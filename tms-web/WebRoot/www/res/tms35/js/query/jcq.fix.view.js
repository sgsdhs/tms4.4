/**
 * 自定义查询
 * 普通查询列表
 */
jcq.view.GeneralQuery = function(options, container){
	var _self = this;
	var grid = null;
	
	this.opts = $.extend({
		custom:null,
		fields:null,
		params:null
	}, options);
	
	this.jqDom = (function(opts){
		_self.parentContainer = container ? container : $('body');
		_self.grid = _buildCustomQueryGrid();
		var jqDom = _self.grid.jqDom;
		_bindGridEvent(jqDom);
		if(_self.opts.custom.result.initData){
			_submitQForm();
		}else{
			_autoSiteIframeHeight();
		}
		return jqDom;
	})(this.opts);
	
	/**
	 * 构建grid
	 */
	function _buildCustomQueryGrid(){
		var opts = _self.opts;
		var custom = opts.custom;
		grid = new jcl.ui.Grid({
			title: custom.wrapTitle,
			//marginTop: 0,
			width: jcq.util.checkNumber(custom.width) ? custom.width * 1 : 'auto',
			toolbar: _buildGridToolbar(custom.toolbar),
			qform: {
				display:custom.cond.unfold,
				layout:custom.cond.layout,
				items:_buildGridQForm(custom.cond.groups, opts.fields, opts.params),
				btns:[
				      {text:'查询', id:'custom-query-sure', action:'submit'}
				]
			},
			table:{
				checkboxEnable:custom.result.mcEnable,
				columns: jcq.common.buildGridColumns(custom.result.columns, opts.fields, dualAudit),
				ds:{
					url: jcq.common.reSetUrl(dualAudit, '/tms35/query/result')/*,
					callback: function(list) {
						list = jcq.common.resultCallBack(list, custom.result.columns, opts.fields, opts.params);
					}*/
				},
            	pagebar: custom.result.pagination.pagebar
			}
		}, _self.parentContainer);
		return grid;
	}
	
	/**
	 * 构建grid中的toolbar工具条
	 * @param toolbars json中的toolbar节点数据
	 * @returns {Array}
	 */
	function _buildGridToolbar(toolbars){
		if(!toolbars) return null;
		var _toolbar = [];
		$.each(toolbars, function(i, toolbar){
			_toolbar.push(toolbar);
		});
		return _toolbar;
	}
	
	/**
	 * 构建gird中的查询条件界面
	 * @param groups	json中的cond.groups节点数据
	 * @param fields	字段集合
	 * @param params	查询参数值集合
	 * @returns {Array}
	 */
	function _buildGridQForm(groups, fields, params){
		var qform = [];
		$.each(groups, function(i, group){
			var item = _resolveQueryGroup(group);
			qform.push(item);
		});
		return qform;
	}
	
	/**
	 * 解析查询条件组
	 * @param group		查询条件组信息
	 * @param params	查询参数值集合
	 */
	function _resolveQueryGroup(group, params){
		var opts = _self.opts;
		var fields = opts.fields;
		var item = null;
		if(group.type == 'SCP' || group.type == 'CBT'){
			var members = group.members, _items = [];
			$.each(members, function(i, member){
				var _item = _resolveQueryMember(member, fields[member.fdName], _getParamValue(member.name, params));
				_items.push(_item);
			});
			item = {type:'jcq.ui.formItem.CustomQueryItem', items:_items};
		}else{
			item = _resolveQueryMember(group, fields[group.fdName], _getParamValue(group.name, params));
		}
		return $.extend({}, group, item);
	}
	
	function _getParamValue(key, params){
		var value, key = key.toLowerCase();
		if(params){
			$.each(params, function(k, v){
				if(key == k.toLowerCase()){
					value = v;
					return;
				}
			});
		}
		return value;
	}
	
	/**
	 * 
	 * @param member	查询组成员
	 * @param fields	字段数据集合
	 * @param params	参数值
	 */
	function _resolveQueryMember(member, field, value){
		var item = _getJclFormItemOptionForTmsType(member, field);
		if(member.defValue && member.defValue.value) item.value = member.defValue.value;
		if(value) item.value = value;
		if(member.item){
			var _item = JSON.parse(member.item);
			$.extend(item, _item);
		}
		return item;
	}
	
	/**
	 * tms数据类型对应默认配置
	 * @param member
	 * @param field
	 * @returns {}
	 */
	function _getJclFormItemOptionForTmsType(member, field){
		var tms_type = field['TYPE'], code = field['CODE'];
		var item = {name:member.name};
		switch(tms_type){
			case 'long':
			case 'double':
			case 'string':
			case 'money':
			case 'devid':
			case 'ip':
			case 'userid':
			case 'acc':
				item.type = 'text';
				break;
			case 'time':
			case 'datetime':
				item.type = 'date';
				item.layout = 'datetime';
				break;
			case 'code':
				if(code){
					item.ds = {type:'code', category:code};
				}
				item.type = 'selector';
				break;
			default:
				item.type = 'text';
		}
		return item;
	}
	
	/**
	 * 提交查询数据
	 * @returns
	 */
	function _submitQForm(){
		var opts = _self.opts;
		var custom = opts.custom;
		var fields = opts.fields;
		var paramArray = grid.qform.jqDom.serializeArray();
		var paramObj = jcq.util.paramToObject(paramArray);
		if(_dataCheck(custom.stmt.mode, custom.cond, fields, paramObj)){
			var afferParam = jcq.util.objToParam(opts.params);
			var queryParam = jcq.util.objToParam(paramObj,true);
			var params = [queryParam, afferParam].join('&');
			grid.setDsParams(params);
			grid.goPage();
		}
	}
	
	/**
	 * 查询数据提交校验
	 * @param stmtMode	查询语句组织模式
	 * @param cond		查询条件
	 * @param fields	字段集合
	 * @param paramObj	查询条件参数值对象
	 */
	function _dataCheck(stmtMode, cond, fields, paramObj){
		try{
			var expr = cond.expr;
			var gIndexs = _getNumbersForString(expr);
			var groups = cond.groups;
			var errorMsg = '';
			var pmSize = {};
			$.each(groups, function(g, group) {
				var label = group.label;
				var type = group.type;
				if(type == 'SCP' || type == 'CBT'){
					var gExpr = group.expr;//多个成员的组合表达式
					var gRelation = group.relation;//多个成员之间的关系
					var mIndexs = _getNumbersForString(gExpr);
					var relOpers = _getNonNumbersForString(gRelation);//关系符
					var relIndexs = _getNumbersForString(gRelation);
					var members = group.members;
					var memberValues = [];
					$.each(members, function(m, member){
						errorMsg = _memberCheck(pmSize, paramObj, member, memberValues, fields, (stmtMode == 'AUTO' ? true : (($.inArray((g+1+''), gIndexs) != -1) ? ((mIndexs.length > 0) ? $.inArray((m+1+''), mIndexs) != -1 : true) : false)));
						if(!jcq.util.isEmpty(errorMsg)){
							if(jcq.util.isEmpty(member.prompt)){
								errorMsg = label + '，第' + (m+1) + '项，' + errorMsg;
							}else{
								errorMsg = label + '，' + member.prompt + '，' + errorMsg;
							}
							throw errorMsg;
						}
						
						if(m > 0 && jcq.util.isEmpty(gExpr)){//未配置表达式，都填或都不填写
							var currValue = memberValues[m];
							var lastValue = memberValues[m-1];
							if((jcq.util.isEmpty(currValue) && !jcq.util.isEmpty(lastValue)) ||
									(!jcq.util.isEmpty(currValue) && jcq.util.isEmpty(lastValue))){
								var _member = jcq.util.isEmpty(currValue) ? members[m] : members[m-1];
								var _memberIndex = jcq.util.isEmpty(currValue) ? (m+1) : m;
								if(jcq.util.isEmpty(_member.prompt)){
									errorMsg = label + '，第' + _memberIndex + '项，不能为空';
								}else{
									errorMsg = label + '，' + _member.prompt + '，不能为空';
								}
								throw errorMsg;
							}
						}
						
						if(m > 0 && !jcq.util.isEmpty(gRelation) && !jcq.util.isEmpty(memberValues[m])){
							for(var r=0;r<relIndexs.length;r++){
								var relIndex = relIndexs[r];
								if((m+1) == relIndex){
									var lastIndex = (r > 0) ? relIndexs[r-1] : -1;
									var nextIndex = (r < (relIndexs.length-1)) ? relIndexs[r+1] : -1;
									if(relIndex > lastIndex && lastIndex != -1){
										errorMsg = _betweenMembersRelationCheck(relIndex, lastIndex, false, label, relOpers[r-1], members, memberValues);
										if(!jcq.util.isEmpty(errorMsg)){
											throw errorMsg;
										}
									}
									if(relIndex > nextIndex && nextIndex != -1){
										errorMsg = _betweenMembersRelationCheck(relIndex, nextIndex, true, label, relOpers[r], members, memberValues);
										if(!jcq.util.isEmpty(errorMsg)){
											throw errorMsg;
										}
									}
									break;
								}
							}
						}
					});
				}else{//一个查询成员项
					var groupValue = [];
					errorMsg = _memberCheck(pmSize, paramObj, group, groupValue, fields, (stmtMode == 'AUTO' ? true : ($.inArray((g+1+''), gIndexs) != -1)));
					if(!jcq.util.isEmpty(errorMsg)){
						throw (label + '，' + errorMsg);
					}
				}
			});
		}catch(e){
			jcl.msg.info(e);
			return false;
		}
		return true;
	}
	
	/**
	 * 校验查询成员
	 * @param pmSize		已经遍历参数个数
	 * @param paramObj		参数对象
	 * @param member		查询成员
	 * @param memberValues	查询组中成员的值集合
	 * @param fields		字段集合
	 * @param letEmpty		是否允许为空
	 */
	function _memberCheck(pmSize, paramObj, member, memberValues, fields, letEmpty){
		var name = member.name;
		var size = pmSize[name] ? pmSize[name] : 0;
		var error = '';
		if(paramObj[name] == undefined){//未定义
			return '未定义';
		}else{
			var obj = paramObj[name];
			var value = null;
			if(typeof(obj) == 'string'){
				if(size > 0){
					return '参数数量不符';
				}else{
					value = obj;
					if(jcq.util.isEmpty(value)){
						if(!letEmpty){
							return '不能为空';
						}
					}else{
						if(_memberSpecialCharacterCheck(member, fields, value)){
							value = _memberHandleForScript(value, member.handle);
							paramObj[name] = value;
						}else{
							return '数据不合法';
						}
					}
				}
			}else if($.isArray(obj)){
				if(size >= obj.length){
					return '参数数量不符';
				}else{
					value = obj[size];
					if(jcq.util.isEmpty(value)){
						if(!letEmpty){
							return '不能为空';
						}
					}else{
						if(_memberSpecialCharacterCheck(member, fields, value)){
							value = _memberHandleForScript(value, member.handle);
							paramObj[name][size] = value;
						}else{
							return '数据不合法';
						}
					}
				}
			}else{
				return '数据类型错误';
			}
		}
		memberValues.push(value);
		pmSize[name] = size + 1;
	}
	
	function _memberSpecialCharacterCheck(member, fields, value){
		var field = fields[member.fdName];
		var item = _resolveQueryMember(member, field);
		if(item){
			if(item.type == 'text'){
				if(item.charType){
					if(item.charType == 'none'){
						return true;
					}else if(item.charType == 'ip'){
						return jcq.util.checkIpAddr(value);
					}else if(item.charType == 'double'){
						return jcq.util.checkNumber(value);
					}else if(item.charType == 'long'){
						return jcq.util.checkLong(value);
					}
				}else{
					return jcq.util.checkSpecialCharacter(value, 2);
				}
			}
		}
		return true;
	}
	
	/**
	 * 成员之间的值校验
	 * @param currIndex		当前索引
	 * @param quiteIndex	比较索引
	 * @param isBefore		当前索引在比较索引之前
	 * @param label			表签名
	 * @param relation		两个值得关系操作符
	 * @param members		成员集合
	 * @param memberValues	成员值集合
	 */
	function _betweenMembersRelationCheck(currIndex, quiteIndex, isBefore, label, relation, members, memberValues){
		var error = null;
		var currValue = memberValues[currIndex-1];
		var quiteValue = memberValues[quiteIndex-1];
		switch(relation){
			case '=':
				if(currValue != quiteValue){
					error = '等于';
				}
				break;
			case '>':
				if(isBefore){
					if(currValue <= quiteValue){
						error = '大于';
					}
				}else{
					if(currValue >= quiteValue){
						error = '小于';
					}
				}
				break;
			case '<':
				if(isBefore){
					if(currValue >= quiteValue){
						error = '小于';
					}
				}else{
					if(currValue <= quiteValue){
						error = '大于';
					}
				}
				break;
			case '<>':
				if(currValue == quiteValue){
					error = '不等于';
				}
				break;
			case '>=':
				if(isBefore){
					if(currValue < quiteValue){
						error = '大于等于';
					}
				}else{
					if(currValue > quiteValue){
						error = '小于等于';
					}
				}
				break;
			case '<=':
				if(isBefore){
					if(currValue > quiteValue){
						error = '小于等于';
					}
				}else{
					if(currValue < quiteValue){
						error = '大于等于';
					}
				}
				break;
		}
		if(!jcq.util.isEmpty(error)){
			var currMember = members[currIndex-1];
			var quiteMember = members[quiteIndex-1];
			if(jcq.util.isEmpty(currMember.prompt) && jcq.util.isEmpty(quiteMember.prompt)){
				error = label + '，第' + currIndex + '项，必须' + error + '，第' + quiteIndex + '项';
			}else{
				error = label + '，' + currMember.prompt + '，必须' + error + '，' + quiteMember.prompt;
			}
		}
		return error;
	}
	
	function _memberHandleForScript(value, handle){
		if(handle && handle.script){
			return jcq.util.convert(value, handle.script);
		}else{
			return value;
		}
	}
	
	/**
	 * 获取字符串中的数字 
	 * @param str 字符串
	 */
	function _getNumbersForString(str){
		if(jcq.util.isEmpty(str)){
			return [];
		}
		var nums = [];
		nums = str.match(/(\d+)/g);
		return nums;
	}
	
	/**
	 * 获取字符串中非数字的字符
	 * @param str	字符串
	 */
	function _getNonNumbersForString(str){
		if(jcq.util.isEmpty(str)){
			return [];
		}
		var nonNums = [];
		nonNums = str.match(/(\D+)/g);
		return nonNums;
	}
	
	function _autoSiteIframeHeight(){
		var opts = _self.opts;
		var paramStr = jcq.util.objToParam(opts.params);
		jcq.common.autoSiteIframeHeight(opts.newView, paramStr, dualAudit);
	}
	
	/**
	 * 绑定页面grid处理事件
	 * @param event 事件代码字符串
	 */
	function _bindGridEvent(jqDom){
		var opts = _self.opts;
		grid.qform.jqDom.find('.form-btns > #custom-query-sure').on('click', function(){
			_submitQForm();
		});
		grid.table.onRenderPage(function(){
			_autoSiteIframeHeight();
		});
		var r_export = opts.custom.result['export'];
		if (r_export && r_export.expBtn) {
			jqDom.find('#' + r_export.expBtn).on('click', function(){
				var paramArray = grid.qform.jqDom.serializeArray();
				var paramObj = jcq.util.paramToObject(paramArray);
				var afferParam = jcq.util.objToParam(opts.params, true);
				var queryParam = jcq.util.objToParam(paramObj, true);
				var params = [queryParam, afferParam].join('&');
				var url = jcq.common.reSetUrl(dualAudit, '/tms35/query/export');
				url = url + "?" + decodeURIComponent(params);
				if (r_export.expType) {
					url += ('&exportType=' + r_export.expType);
				}
				window.open(jcl.env.contextPath + url);
			});
		}
		jcq.common.bindLinkEvent(_self, jqDom);
	}
};

/**
 * 自定义查询
 * 实体查询组织
 */
jcq.view.EntityQuery = function(options, container){
	var _self = this;
	
	this.opts = $.extend({
		newView:true,
		unique:null,
		custom:null
	}, options);
	
	this.jqDom = (function(opts){
		_self.parentContainer = container ? container : $('body');
		var custom = opts.custom;
		if(custom.wrapTitle){
			_self.box = new jcl.ui.Box({
				title: custom.wrapTitle,
				contentWrap: false
			}, _self.parentContainer);
			_self.parentContainer = _self.box.container;
		}else{
			_self.parentContainer.addClass('box');
			_self.parentContainer.css({width:'100%'});
		}
		var jqDom = '';
		if(custom.effect == 'COMBTTAB'){//标签页
			_buildTagPage();
			jqDom = _self.tabPanel.jqDom;
		}else if(custom.effect == 'COMBTPAGE'){
			_buildComposePage();
			jqDom = _self.parentContainer;
		}else{
			_self.detail = new jcq.view.EntityDetail(opts, _self.parentContainer);
			jqDom = _self.detail.jqDom;
		}
		return jqDom;
	})(this.opts);
	
	/**
	 * 创建实体标签页
	 */
	function _buildTagPage(){
		var opts = _self.opts;
		var custom = opts.custom;
		var content = custom.stmt.content;
		var queryCfgs = content.split('\;');
		_self.tabPanel = new jcl.ui.TabPanel({}, _self.parentContainer);
		_self.tabPanel.onTabClick(function(index){
			if(opts.newView){
				var iframeContent = _self.tabPanel.tabps.children('div.tabp-content:eq('+index+')');
				var iframe = iframeContent.children('iframe');
				if(!iframe.attr('src') && iframe.attr('url')){
					iframe.attr('src', iframe.attr('url'));
					iframe.removeAttr('url');
				}
			}
	    	return true;
   	 	});
		
		var tagIndex = 1;
		for(var i=0;i<queryCfgs.length;i++) {
			var queryCfg = queryCfgs[i];
			var queryId = queryCfg.substring(0,queryCfg.indexOf('['));
			var queryTitle = queryCfg.substring(queryCfg.indexOf('[')+1, queryCfg.indexOf(']'));
			var isActive = ((tagIndex == i+1) ? true : false);
			var params = null, isQuery = true;
			if(isNaN(queryId)) {
				isQuery = false;
				params = jcq.util.paramRemove('queryId', opts.params);
			} else {
				params = jcq.util.paramPut(opts.params, 'queryId', queryId);
			}
			if(opts.newView){
				if (isQuery) {
					_self.tabPanel.addTab(queryTitle, _buildQueryIframe(jcq.util.objToParam(params), isActive));
				} else {
					_self.tabPanel.addTab(queryTitle, _buildUnQueryIframe(queryId, jcq.util.objToParam(params), isActive));
				}
			}else{
				var options = $.extend({}, opts, {params:params});
				var query = new jcq.loadQuery(options);
				_self.tabPanel.addTab(queryTitle, query.jqDom);
			}
		}
		_self.tabPanel.activeTab(tagIndex-1);
	}
	
	/**
	 * 上下结构页
	 */
	function _buildComposePage() {
		var opts = _self.opts;
		var custom = opts.custom;
		var content = custom.stmt.content;
		var queryCfgs = content.split('\;');
		for(var i=0;i<queryCfgs.length;i++){
			var queryId = queryCfgs[i];
			var params = jcq.util.paramPut(opts.params, 'queryId', queryId);
			if(opts.newView){
				$(_buildQueryIframe(jcq.util.objToParam(params), true)).appendTo(_self.parentContainer);
			}else{
				var options = $.extend({}, opts, {params:params});
				new jcq.loadQuery(options, _self.parentContainer);
			}
		}
	}
	
	/**
	 * 创建自定义查询Iframe页面
	 * @param params	查询参数
	 * @param isActive	是否显示
	 */
	function _buildQueryIframe(params, isActive){
		var opts = _self.opts;
		var url = jcl.env.contextPath + jcq.common.reSetUrl(dualAudit, '/tms35/query/show?') + params;
		return _buildIframeHtml(url, isActive);
	}
	
	/**
	 * 创建非自定义查询的Iframe页面
	 * @param url		页面URL
	 * @param params	查询参数
	 * @param isActive	是否显示
	 */
	function _buildUnQueryIframe(url, params, isActive){
		url = jcl.env.contextPath + jcq.common.reSetUrl(dualAudit, url) + ((params && params.length) > 0 ? ('?' + params) : '');
		return _buildIframeHtml(url, isActive);
	}
	
	/**
	 * 生产iframe html代码
	 */
	function _buildIframeHtml(url, isActive) {
		var id = 'query_iframe_' + new Date().getTime();
		var iframeHtml = '<iframe id="' + id + '" ' + (isActive === true ? ('src="' + url + '"') : ('url="' + url + '"'));
		iframeHtml += 'frameborder="0" style="border: medium none; height: 100%;width: 100%;"></iframe>';
		return iframeHtml;
	}
};

/**
 * 自定义查询
 * 单个查询实体
 */
jcq.view.EntityDetail = function(options, container){
	var _self = this;
	this.grid = null;
	this.opts = $.extend({
		custom:null,
		fields:null,
		params:null
	}, options);
	
	this.jqDom = (function(opts){
		_self.parentContainer = container ? container : $('body');
		var paramStr = jcq.util.objToParam(opts.params);
		jcl.postJSON(jcq.common.reSetUrl(dualAudit, '/tms35/query/result'), paramStr, function(data) {
			var htmlstr = null;
			_self.page = data.page;
			if(_self.page.list && _self.page.list.length > 0) {
				var custom = opts.custom;
				if(custom.effect == 'LIST') {
					_generateList(jcq.common.reSetUrl(dualAudit, '/tms35/query/result'), paramStr);
				} else {
					_self.page.list = jcq.common.resultCallBack(_self.page.list, custom.result.columns, opts.fields, dualAudit, opts.params);
					if(custom.effect == 'WIRELIST') {
						htmlstr = _generateWireList();
					} else if(custom.effect == 'NOWIRELIST') {
						htmlstr = _generateNoWireList();
					} else if(custom.effect == 'DETAIL') {
						if(_self.page.list.length == 1) {
							htmlstr = _generateDetail();
						} else {
							_generateList();
						}
					}
				}
			}else{
				htmlstr = '<div align="center"><b>没有信息记录</b></div>';
			}
			var jqDom = null;
			if(htmlstr){
				jqDom = $(htmlstr).appendTo(_self.parentContainer);
			}else if(_self.grid){
				jqDom = $(_self.grid.jqDom);
			}
			jcq.common.autoSiteIframeHeight(opts.newView, paramStr, dualAudit);
			_bindEvent(jqDom);
			return jqDom;
		});
	})(this.opts);
	
	/**
	 * 生成jcl构建的列表
	 */
	function _generateList(url, params){
		var opts = _self.opts;
		var custom = opts.custom;
		_self.grid = new jcl.ui.Grid({
			title: custom.wrapTitle ? custom.wrapTitle : custom.pageTitle,
			//marginTop : 0,
			width: jcq.util.checkNumber(custom.width) ? custom.width * 1 : 'auto',
			table:{
				checkboxEnable: custom.result.mcEnable,
				columns: jcq.common.buildGridColumns(custom.result.columns, opts.fields, dualAudit),
				ds:{
					url: url,
					params: params
				},
            	pagebar: custom.result.pagination.pagebar
			}
		}, _self.parentContainer);
		_self.grid.renderPage(_self.page);
	}

	/**
	 * 生成普通table列表
	 */
	function _generateWireList(){
		var opts = _self.opts;
		var custom = opts.custom;
		//var fields = opts.fields;
		var sHtml = "<table width='100%' border='1' style='border-collapse: collapse;'>";
		sHtml += "<tr style='font-weight: bold;font-size: 15px'>";
		var columns = custom.result.columns;
		$.each(custom.result.columns, function(c, column) {
			if(column.show) {
				var name = column.name;//fields[column.fdName]['NAME'];
				var width = column.width;
				sHtml += ("<td width='" + width + "' valign='top' align='center'>" + name + "</td>");
			}
		});
		sHtml += "</tr><tbody>";
		$.each(_self.page.list, function(i, entry) {
			sHtml += "<tr>";
			$.each(columns, function(c, column) {
				if(column.show) {
					var fdname = jcq.util.getValidFdName(column.csName);
					var value = entry[fdname];
					if(jcq.util.isEmpty(value)) {
						value = '';
					}
					sHtml += "<td valign='top' align='center'>" + value + "</td>";
				}
			});
			sHtml += "</tr>";
		});
		sHtml += "</tbody></table>";
		return sHtml;
	}

	function _generateNoWireList(){
		var opts = _self.opts;
		var custom = opts.custom;
		//var fields = opts.fields;
		var sHtml = '<table class="tree_table" cellspacing="0" cellpadding="0" border="0" align="left" style="width:100%">';
		sHtml += '<thead><tr>';
		var columns = custom.result.columns;
		$.each(custom.result.columns, function(c, column) {
			if(column.show) {
				var name = column.name;//fields[column.fdName]['NAME'];
				var width = column.width;
				sHtml += ("<th width='" + width + "' valign='top' align='center'>" + name + "</th>");
			}
		});
		sHtml += '</tr></thead><tbody>';
		$.each(_self.page.list, function(i, entry) {
			sHtml += "<tr>";
			$.each(columns, function(c, column) {
				if(column.show) {
					var fdname = jcq.util.getValidFdName(column.csName);
					var value = entry[fdname];
					if(jcq.util.isEmpty(value)) {
						value = '';
					}
					sHtml += "<td valign='top' align='center'>" + value + "</td>";
				}
			});
			sHtml += "</tr>";
		});
		sHtml += '</tbody></table>';
		return sHtml;
	}

	/**
	 * 生成明细
	 */
	function _generateDetail(){
		var opts = _self.opts;
		var custom = opts.custom;
		//var fields = opts.fields;
		var sHtml = "<ul class='list-box'>";
		var columns = custom.result.columns;
		$.each(_self.page.list, function(i, entry) {
			$.each(columns, function(c, column) {
				if(column.show) {
					//var field = fields[column.fdName];
					var name = column.name;//field['NAME'];
					var dataIndex = jcq.util.getValidFdName(column.csName);
					var value = entry[dataIndex];
					if(jcq.util.isEmpty(value)) {
						value = '';
					}
					sHtml += "<li class='list-box-item'>";
					sHtml += "<label class='list-box-item-label'>" + name + ":</label>";
					sHtml += "<span class='list-box-item-content'><div id='" + dataIndex + "' style='font-weight: bold'>" + value + "</div></span>";
					sHtml +="</li>";
				}
			});
		});
		sHtml +="</ul>";
		return sHtml;
	}
	
	function _bindEvent(jqDom){
		jcq.common.bindLinkEvent(_self, jqDom);
	}
};