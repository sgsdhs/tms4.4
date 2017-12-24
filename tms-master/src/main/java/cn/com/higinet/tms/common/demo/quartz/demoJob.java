package cn.com.higinet.tms.common.demo.quartz;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class demoJob implements TmsJob {

	private static Logger logger = LoggerFactory.getLogger( demoJob.class );

	@Override
	public void execute( JobExecutionContext context ) throws JobExecutionException {
		logger.info( context.getFireTime().toString() );
	}

	@Override
	public String getJobName() {
		return "demoJob";
	}

}
