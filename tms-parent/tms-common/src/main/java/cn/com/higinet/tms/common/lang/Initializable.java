/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  Initializable.java   
 * @Package cn.com.higinet.tms.common.util   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-5-9 15:00:09   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.lang;

/**
 * 可以初始化和销毁的接口
 *
 * @author: 王兴
 * @date:   2017-5-9 15:00:09
 * @since:  v4.3
 */
public interface Initializable {
	
	/**
	 * 初始化.
	 * @throws Exception 
	 */
	public void initialize() throws Exception;

	/**
	 * 销毁.
	 */
	public void destroy() throws Exception;
}
