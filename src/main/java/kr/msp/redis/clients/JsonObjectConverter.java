package kr.msp.redis.clients;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;


public class JsonObjectConverter {

	private static ObjectMapper mapper;

	static {
		mapper = new ObjectMapper();
		// 1.9.x 버전 이상
		mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
		mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_EMPTY);
		// 1.8.x 버전 이하.
		mapper.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES,
				false);
		mapper.configure(SerializationConfig.Feature.WRITE_EMPTY_JSON_ARRAYS,
				false);
		mapper.configure(
				DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,
				true);
	}

	public static final <E> String getAsJSON(E inClass) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(inClass) ; 
	}
/*
	public static final <E> E parseAsInputClassForArrayList(String json,Class outClass) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		TypeFactory t = TypeFactory.defaultInstance();
		mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		return (E) mapper.readValue(json,t.constructCollectionType(ArrayList.class,outClass)); 
	}

	public static final <E> E parseAsInputClassForSimpleClass(String json,Class outClass) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		return (E) mapper.readValue(json,outClass); 
	}

	public static final <E> E parseAsInputClassForHashMap(String json,Class key,Class outClass) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		TypeFactory t = TypeFactory.defaultInstance();
		mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		return (E) mapper.readValue(json,t.constructMapType(HashMap.class,key,outClass)); 
	}
  */

	public static String getJSONFromObject(Object obj) {
		try {
			StringWriter sw = new StringWriter(); // serialize
			mapper.writeValue(sw, obj);
			sw.close();

			return sw.getBuffer().toString();
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static <T> T getObjectFromJSON(String json, Class<T> classOfT) {
		try {
			StringReader sr = new StringReader(json);
			return mapper.readValue(json.getBytes("UTF-8"), classOfT);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}