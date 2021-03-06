package cn.com.higinet.sf0622;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

/**
 * 调用短信服务的客户端实现
 * 
 * @author wujinwang
 * */

public class SFNPP101Socket {

	/**
	 * 报文头长度
	 */
	private final int headLength = 32;

	/**
	 * 短信的IP地址,通过配置tms-service.xml配置
	 * */
    //public String ip = "10.118.244.172";
	//private String ip = "127.0.0.1";
	//private String ip = "58.17.245.132";
    public String ip = "10.118.201.136";
	/**
	 * 短信的端口，通过配置tms-service.xml配置
	 * */
	private int port = 8000;

	/**
	 * 短信的连接超时，通过配置tms-service.xml配置
	 * */
	private int timeOut = 2000;

	public boolean sendMessage(Map<String, Object> map) {
		boolean b = false;
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		String msgBody = xml
				+ "<Message><SYNCFLAG>1</SYNCFLAG><TXNCODE>f6cab9a7fa8f4e90a95d9ff7dfe24bd1</TXNCODE><TXNTIME>2016-06-22 15:49:59</TXNTIME><TXNID>NPP101</TXNID><CHANCODE>CH01</CHANCODE><USERID>20145939931904</USERID><SESSIONID>1po2dzu7hfm96!1466581766952</SESSIONID><IPADDR>121.15.143.202</IPADDR><DEVICEFINGER>1|2001:862970020019750|2003:00:0a:f5:a7:81:48|2004:e429e052|2006:3f0fdd33e07ed476|2009:0|2010:true|2013:4.589389937671455|2014:1080*1920|2015:MF198|2016:s700|2018:android|2019:5.1.1|2020:zh|2021:com.sfpay.mobile|2022:V2.1.2|2023:322d4985062b16|2024:22|2025:1.15 GB|2026:1.84 GB|2027:66|2028:.2.0.2.c1-00098-M8936FAAAANUZM-1,.2.0.2.c1-00098-M8936FAAAANUZM-1|2029:3.10.49-g4382015|2030:CN|2031:TimeZone   GMT+08:00Timezon id :: Asia/Shanghai|2032:192.168.33.174|2033:192.168.33.1|2034:0|2035:0|2036:0|2037:20:76:93:38:83:80|2039:SFPAY-SECU|2051:12.50 GB|2052:AArch64 Processor rev 1 (aarch64) 0|2053:BOARD: msm8916, BOOTLOADER: unknown|2054:MF198|2055:wifi</DEVICEFINGER><DEVICETOKEN>e08cbe81e1434d3aa0fcbac7c6fae9ef</DEVICETOKEN><DISPATCH>20145939931904</DISPATCH><AUTHLEVEL>MIDD_AUTH</AUTHLEVEL><LOGINTYPE>01</LOGINTYPE><BUSYSRC>APP</BUSYSRC><LONGITUDE>0.0</LONGITUDE><LATITUDE>0.0</LATITUDE></Message>";

		// String msgBody = xml + "";
		int bodyLength = 0;
		try {
			bodyLength = msgBody.getBytes("UTF-8").length;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String msgHead = this.getReqMsgHead(bodyLength + 32);
		System.out.println("---msgHead--:" + msgHead);

		String msg = msgHead + msgBody;
		System.out.println("---msg--:" + msg);

		String response = callServer(ip, port, msg, timeOut);

		return b;
	}

	/**
	 * 组装报文头
	 * 
	 * @param actionCode
	 *            操作代码
	 * @param bodyLength
	 *            报文体长度
	 * @return
	 */
	public String getReqMsgHead(int bodyLength) {
		// 00000329TMS 0002XML
		StringBuffer sb = new StringBuffer();
		String actionCode = "0002";
		String len = String.valueOf(bodyLength);
		len = "00000000".substring(len.length()) + len;
		sb.append(len);// 报文体长度
		sb.append("TMS").append(" ").append(" ").append(" ").append(" ").append(" ");// 服务号
		sb.append(actionCode);// 交易号
		sb.append("XML").append(" ");// 报文体类型
		sb.append(" ").append(" ").append(" ").append(" ").append(" ").append(" ").append(" ").append(" ");// 返回码
		sb.append("20180989981961                  ");
		return sb.toString();
	}

	/**
	 * 向TMS送报文
	 * 
	 * @param message
	 * @return
	 */
	private String callServer(String ip, int port, String message, int timeOut) {
		String responseBody = "";
		Socket socket = null;
		DataOutputStream dos = null;
		InputStream dis = null;

		try {
			// log.debug("SMS socket start.");
			socket = new Socket();
			// 连接超时和read超时应用一个数据项 P302
			socket.connect(new InetSocketAddress(ip, port), timeOut);
			if (socket.isConnected()) {
				socket.setSoTimeout(timeOut);
				socket.setTcpNoDelay(true);
				dos = new DataOutputStream(socket.getOutputStream());
				dos.write(message.getBytes());
				dos.flush();
				socket.shutdownOutput();

				dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

				// 取返回数据长度
				byte[] byteHead = new byte[headLength];
				int headLength = dis.read(byteHead);

				System.out.println("rhead:" + new String(byteHead, 0, 8, "utf-8"));
				// 判断报文头是否正确
				if (headLength > 0) {
					// 得到报文体长度
					int msgLen = Integer.parseInt(new String(byteHead, 0, 8, "utf-8"));

					byte[] msgBody = new byte[msgLen];
					int readedLen = 0, i = 0;
					while (readedLen < msgLen) {
						i = dis.read(msgBody, readedLen, msgLen - readedLen);
						readedLen += i;
						if (i < 0)
							break;
					}

					// resMsg = new String(byteHead, 0, headLength, "utf-8");
					responseBody = new String(msgBody, 0, msgLen, "utf-8");
				}
				System.out.println("responseBody:" + responseBody);
				return responseBody;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (dis != null)
					dis.close();
				if (dos != null)
					dos.close();
				if (socket != null)
					socket.close();
			} catch (IOException e) {
				System.out.println("Close connectServer error:" + e.getMessage());
			}
		}
		return responseBody;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	public static void main(String[] args) {

		SFNPP101Socket ep0001Socket = new SFNPP101Socket();
		ep0001Socket.sendMessage(null);

	}
}
