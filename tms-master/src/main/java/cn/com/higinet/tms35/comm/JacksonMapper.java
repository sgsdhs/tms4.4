package cn.com.higinet.tms35.comm;

import org.codehaus.jackson.map.ObjectMapper;

public class JacksonMapper {

	private static final ObjectMapper mapper = new ObjectMapper();

	private JacksonMapper() {

	}

	public static ObjectMapper getInstance() {
		return mapper;
	}
}