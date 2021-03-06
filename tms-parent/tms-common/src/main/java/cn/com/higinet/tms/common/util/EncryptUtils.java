/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  EncryptUtils.java   
 * @Package cn.com.higinet.tms.common.util   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-5-16 11:24:27   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.util;

import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * 提供了通用的加密解密工具类，包含了用于数字加密的{@link #simpleEncrypt(String)}{@link #simpleDecrypt(String)}，
 * base64加密{@link #base64Encrypt(String)}{@link #base64Decrypt(String)}，DES加密{@link #desEncrypt(String)}，{@link #desDecrypt(String)}，
 * 以及自主实现的非重复加密{@link #roachEncrypt(String)}{@link #roachDecrypt(String)}，
 * 通过添加salt，使同一个字符串每次加密结果不一样，但是加密后长度会增加3倍左右，请谨慎使用。
 *
 * @author: 王兴
 * @date:   2017-5-16 11:24:27
 * @since:  v4.3
 */
public class EncryptUtils {
	
	/** 常量 KEY. */
	private static final String KEY = "gb6yhn7ujm8ik";//默认KEY

	/**
	 * 移位异或，不改变字符串长度，只用于数字类型的字符串
	 * @param str 
	 * @return
	 */
	public static String simpleEncrypt(String str) {
		if (str == null) {
			return null;
		}
		return new String(xor(str.getBytes(), KEY.getBytes()));
	}

	/**
	 * 移位异或，不改变字符串长度,只用于数字类型的字符串
	 * @param str
	 * @return
	 */
	public static String simpleDecrypt(String str) {
		if (str == null) {
			return null;
		}
		return new String(xor(str.getBytes(), KEY.getBytes()));
	}

	/**
	 * 返回默认KEY的加密字符串
	 * @param str 需要加密的字符串
	 * @return 返回默认KEY的加密字符串
	 */
	public static String base64Encrypt(String str) {
		return base64Encrypt(str, KEY);
	}

	/**
	 * 返回默认KEY的解密字符串
	 * @param str 需要解密的字符串
	 * @return 返回默认KEY的解密字符串
	 */
	public static String base64Decrypt(String str) {
		return base64Decrypt(str, KEY);
	}

	/**
	 * Base 64 encrypt.
	 *
	 * @param str the str
	 * @param key the key
	 * @return the string
	 */
	//加密
	public static String base64Encrypt(String str, String key) {
		if (str == null) {
			return null;
		}
		BASE64Encoder base64 = new BASE64Encoder();

		return base64.encode(xor(str.getBytes(), key.getBytes()));
	}

	/**
	 * Base 64 decrypt.
	 *
	 * @param str the str
	 * @param key the key
	 * @return the string
	 */
	//解密
	public static String base64Decrypt(String str, String key) {
		if (str == null) {
			return null;
		}
		try {
			BASE64Decoder base64 = new BASE64Decoder();
			byte[] bStr = base64.decodeBuffer(str);

			return new String(xor(bStr, key.getBytes()));
		} catch (Exception ex) {
			throw new RuntimeException("解密失败", ex);
		}
	}

	/**
	 * Xor.
	 *
	 * @param d1 the d 1
	 * @param d2 the d 2
	 * @return the byte[]
	 */
	private static byte[] xor(byte[] d1, byte[] d2) {
		int i = 0;
		for (int j = 0; i < d1.length; j++) {
			if (j == d2.length)
				j = 0;
			d1[i] = (byte) (d1[i] ^ d2[j]);

			i++;
		}
		return d1;
	}

	/**
	 * DES加密
	 * @param str
	 * @return
	 */
	public static String desEncrypt(String str) {
		return desEncrypt(str, KEY);
	}

	/**
	 * DES解密
	 * @param str
	 * @param key
	 * @return
	 */
	public static String desDecrypt(String str) {
		return desDecrypt(str, KEY);
	}

	/**
	 * Des encrypt.
	 *
	 * @param str the str
	 * @param key the key
	 * @return the string
	 */
	public static String desEncrypt(String str, String key) {
		if (str == null) {
			return null;
		}
		try {
			return Desc.encryptString(str, key);
		} catch (Exception ex) {
			throw new RuntimeException("DES加密失败", ex);
		}
	}

	/**
	 * Des decrypt.
	 *
	 * @param str the str
	 * @param key the key
	 * @return the string
	 */
	public static String desDecrypt(String str, String key) {
		if (str == null) {
			return null;
		}
		try {
			return Desc.decryptString(str, key);
		} catch (Exception ex) {
			throw new RuntimeException("DES解密失败", ex);
		}
	}

	/**
	 * {类的详细说明}.
	 *
	 * @author: 王兴
	 * @date:   2017-5-16 11:24:27
	 * @since:  v4.3
	 */
	private static class Desc {
		
		/**
		 * 返回 key 属性.
		 *
		 * @param strKey the str key
		 * @return key 属性
		 * @throws Exception the exception
		 */
		private static Key getKey(String strKey) throws Exception {
			KeyGenerator _generator = KeyGenerator.getInstance("DES");
			_generator.init(new SecureRandom(strKey.getBytes()));

			return _generator.generateKey();
		}

		/**
		 * Encrypt.
		 *
		 * @param data the data
		 * @param key the key
		 * @return the byte[]
		 * @throws Exception the exception
		 */
		public static byte[] encrypt(byte[] data, String key) throws Exception {
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(1, getKey(key));

			return cipher.doFinal(data);
		}

		/**
		 * Decrypt.
		 *
		 * @param data the data
		 * @param key the key
		 * @return the byte[]
		 * @throws Exception the exception
		 */
		public static byte[] decrypt(byte[] data, String key) throws Exception {
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(2, getKey(key));

			return cipher.doFinal(data);
		}

		/**
		 * Encrypt string.
		 *
		 * @param str the str
		 * @param key the key
		 * @return the string
		 * @throws Exception the exception
		 */
		public static String encryptString(String str, String key) throws Exception {
			BASE64Encoder base64 = new BASE64Encoder();
			byte[] ret = encrypt(str.getBytes(), key);
			return base64.encode(ret);
		}

		/**
		 * Decrypt string.
		 *
		 * @param str the str
		 * @param key the key
		 * @return the string
		 * @throws Exception the exception
		 */
		public static String decryptString(String str, String key) throws Exception {
			BASE64Decoder base64 = new BASE64Decoder();
			byte[] ret = decrypt(base64.decodeBuffer(str), key);

			return new String(ret);
		}
	}

	/** 常量 ROACH_KEY. */
	private static final String ROACH_KEY = "1qaz2wsx3edc4rfv5tgb6yhn7ujm8ik,9ol.0p;/!@#$%^&*()";

	/**
	 * 通过增加数据冗余(3-4倍)来增加数据加密安全性，即使同一个字符串，每次加密也是不一样的
	 * 适合数据量较小的加密，例如密码，手机号等
	 * @param str 需要加密的字符串
	 * @return 加密后的字符串
	 */
	public static final String roachEncrypt(String str) {
		int strlength = str.length();
		boolean even = str.length() % 2 == 0;//加密字符串长度是否是偶数
		if (even) {
			strlength += 1;//如果是偶数需要补位
		}
		String[] finalString = new String[strlength];
		char[] targetChars = new char[strlength];
		char[] originalChars = str.toCharArray();
		if (even) {
			for (int i = 0, len = originalChars.length; i < len; i++) {
				targetChars[i] = originalChars[i];
			}
			targetChars[strlength - 1] = 'a';//偶数长度的字符串加密补位
		} else {
			targetChars = originalChars;
		}
		int targetCharsLength = targetChars.length;
		for (int i = 0; i < targetChars.length; i++) {
			if (i % 2 == 0) {//偶数位
				finalString[i] = concatStr(getSalt(), targetChars[targetCharsLength - i - 1]);
			} else {//奇数位
				finalString[i] = concatStr(targetChars[i], getSalt());
			}
		}
		StringBuilder fs = new StringBuilder();
		for (int i = 0; i < finalString.length; i++) {
			if (even && i == 0) {
				continue;
			}
			fs.append(finalString[i]);
		}
		fs = fs.append(concatStr(getSalt(), getSalt()));
		return base64Encrypt(fs.toString(), KEY);
	}

	/**
	 * Concat str.
	 *
	 * @param a the a
	 * @return the string
	 */
	private static String concatStr(char... a) {
		return new String(a);
	}

	/**
	* @Description: Roach解密 
	* @param str
	* @return 参数 
	* String 返回类型 
	* @throws
	*/
	public static final String roachDecrypt(String str) {
		str = base64Decrypt(str, KEY);
		String[] finalString;
		String[] tokens = getTokens(str);
		int finallength = tokens.length;
		int strlength = tokens.length;
		if (strlength % 2 != 0) {
			strlength += 1;
		}
		int count = strlength - 1;
		String[] model = new String[count];
		finalString = new String[count];
		boolean odd = finallength % 2 != 0;
		for (int i = 0; i < count; i++) {
			if (odd) {
				if (i == 0) {
					model[i] = "wx";//仅用来辅助解密
				} else {
					model[i] = tokens[i - 1];
				}
			} else {
				model[i] = tokens[i];
			}
		}
		for (int i = 0; i < count; i++) {
			if (i % 2 == 0) {
				finalString[count - i - 1] = String.valueOf(model[i].charAt(model[i].length() - 1));
			} else {
				finalString[i] = String.valueOf(model[i].charAt(0));
			}
		}
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < finalString.length; i++) {
			if (odd && i == finallength - 1)
				break;
			res.append(finalString[i]);
		}
		return res.toString();
	}

	/**
	 * 返回 tokens 属性.
	 *
	 * @param str the str
	 * @return tokens 属性
	 */
	private static String[] getTokens(String str) {
		int x = 0, b = 2;
		int len = str.length() / b;
		String[] res = new String[len];
		for (int i = 0; i < len; x = (i + 1) * b, i++) {
			res[i] = str.substring(x, (i + 1) * b);
		}
		return res;
	}

	private static char getSalt() {
		byte[] b = ROACH_KEY.getBytes();
		int len = b.length;
		int x = (int) (Math.random() * len);
		return (char) b[x];
	}

	/**
	 * 主函数.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		String str = "123456";
		System.out.println(str.length());
		//xor转换
//				String xorStr = simpleEncrypt(str);
		//		System.out.println(xorStr);
		//		System.out.println(xorStr.length());
		//		xorStr = simpleDecrypt(xorStr);
		//		System.out.println(xorStr);
		String xorStr = roachEncrypt(str);
		System.out.println(xorStr.length() + " " + xorStr);
		//		xorStr = roachEncrypt(str);
		//		System.out.println(xorStr);
		//		xorStr = roachEncrypt(str);
		//		System.out.println(xorStr);
		xorStr = roachDecrypt(xorStr);
//		xorStr=simpleDecrypt(xorStr);
		System.out.println(xorStr);
		//base64 
				String encryptedStr = base64Encrypt(str);
				System.out.println(encryptedStr.length()+" "+encryptedStr);
				String decipheredStr = base64Decrypt(encryptedStr);
				System.out.println(decipheredStr);
		//DES
		//		String encryptedStr = desEncrypt(str);
		//		System.out.println(encryptedStr);
		//		System.out.println(encryptedStr.length());
		//		String decipheredStr = desDecrypt(encryptedStr);
		//		System.out.println(decipheredStr);
	}
}
