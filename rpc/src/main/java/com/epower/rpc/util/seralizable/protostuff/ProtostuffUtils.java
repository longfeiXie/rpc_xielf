package com.epower.rpc.util.seralizable.protostuff;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

/**
 * 序列化帮助类
 * @author xielf
 *
 */
@SuppressWarnings("unchecked")
public class ProtostuffUtils{

	/**schema 缓存*/
	private static final Map<Class<?>,Schema<?>> cacheSchema = new ConcurrentHashMap<Class<?>,Schema<?>>();
	
	/**
	 * 获取schema
	 * @param clazz
	 * @return
	 */
	private static <T> Schema<T> getSchema(Class<T> clazz){
		
		Schema<T> schema = (Schema<T>) cacheSchema.get(clazz);
		if(schema==null){
			schema = RuntimeSchema.getSchema(clazz);
			if(schema != null){
				cacheSchema.put(clazz, schema);
			}
		}
		return schema;
	}
	
	/**
	 * 序列化
	 * @param obj
	 * @return
	 */
	public static <T> byte[] serialize(T obj){
		
		Schema<T> schema = (Schema<T>) getSchema(obj.getClass());
		final LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
		byte[] bytes = ProtostuffIOUtil.toByteArray(obj, schema, buffer);
		return bytes;
	}
	
	/**
	 * 反序列化
	 * @param bytes
	 * @param cls
	 * @return
	 */
	public static <T> T deserialize(byte[] bytes, Class<T> cls){
		Schema<T> schema = getSchema(cls);
		T obj = schema.newMessage();
		ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
		return obj;
	}
}
