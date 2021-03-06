package cn.com.higinet.tms.consumer.traffic.test;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.com.higinet.tms.base.entity.TrafficData;
import cn.com.higinet.tms.consumer.traffic.queue.TrafficQueue;

@Controller
@RequestMapping(value = "/esOpt")
public class Test {

	@Autowired
	private TrafficQueue trafficQueue;

	@RequestMapping(value = "testBatchUpdate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String testBatchUpdate() throws Exception {
		for( int i = 0; i < 20; i++ ) {
			trafficQueue.put( createData() );
		}
		return "aaa";
	}

	private TrafficData createData() {
		TrafficData data = new TrafficData();
		data.setTxnCode( UUID.randomUUID().toString() );
		data.setIpAddress( "11.11.11.11" );
		data.setDisposal( "PS01" );
		data.setCounTryCode( "5044啊啊啊啊啊啊啊大大大大大大撒大" );
		data.setRegionCode( "5044110000啊啊啊啊啊啊啊大大大" );
		data.setCityCode( "5044110000啊啊啊啊啊啊啊大大大大大大撒大苏打" );
		data.setIspCode( "504411000050441100005044110000" );
		data.setCreateTime( 1513040400000L );
		data.setFinishTime( 1513040400000L );
		data.setTxnStatus( 1 );
		data.setBatchNo( "123123啊啊啊啊啊啊啊大大大大大大撒大苏打似的" );
		data.setHitrulenum( 1 );
		data.setIsCorrect( "1啊啊啊啊啊啊啊大大大大大大撒大苏打似的啊啊啊啊啊啊啊大大" );
		data.setConfirmRisk( "1啊啊啊啊啊啊啊大大大大大大撒大苏打似的啊啊啊啊啊啊啊大大大" );
		data.setIsEval( "1啊啊啊啊啊啊啊大大大大大大撒大苏打似的啊啊啊啊啊啊啊大大大大大" );
		data.setModelId( "102啊啊啊啊啊啊啊大大大大大大撒大苏打似的啊啊啊啊啊啊啊大大大大" );
//		data.setText1( "北京三里屯啊啊啊啊啊啊啊大大大大大大撒大苏打似的" );
		data.setText1(Enums.random(NameEnum.class).toString());
		data.setText2( "北京西城区啊啊啊啊啊啊啊大大大大大大撒大苏打似的啊啊啊啊啊啊啊大大大大大大撒大苏" );
		data.setText3( "啊啊啊啊啊啊啊大大大大大大撒大苏打似的啊啊啊啊啊" );
		data.setText5( "北京三里屯啊啊啊啊啊啊啊大大大大" );
		data.setText6( "北京三里屯啊啊啊啊啊啊啊大大大大" );
		data.setText7( "北京三里屯啊啊啊啊啊啊啊大大大大" );
		data.setText8( "北京三里屯啊啊啊啊啊啊啊大大大大" );
		data.setText9( "北京三里屯啊啊啊啊啊啊啊大大大大" );
		data.setNum1( 1.00 );
		data.setNum2( 1.00 );
		data.setNum3( 1.00 );
		data.setNum4( 1.00 );
		data.setNum5( 1.00 );
		data.setNum6( 1.00 );
		data.setNum7( 1.00 );
		data.setNum8( 1.00 );

		return data;
	}

}
