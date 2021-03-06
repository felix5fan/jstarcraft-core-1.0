package com.jstarcraft.core.storage.adapter;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jstarcraft.core.storage.exception.StorageException;
import com.jstarcraft.core.utility.JsonUtility;
import com.jstarcraft.core.utility.TypeUtility;

/**
 * JSON适配器
 * 
 * @author Birdy
 */
public class JsonAdapter implements FormatAdapter {

	/** 类型转换器(基于Jackson) */
	private static final ObjectMapper TYPE_CONVERTER = new ObjectMapper();

	static {
		TYPE_CONVERTER.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		JavaTimeModule module = new JavaTimeModule();
		TYPE_CONVERTER.registerModule(module);
	}

	@Override
	public <E> Iterator<E> iterator(Class<E> clazz, InputStream stream) {
		try {
			JavaType type = JsonUtility.type2Java(TypeUtility.parameterize(LinkedList.class, clazz));
			List<E> list = TYPE_CONVERTER.readValue(stream, type);
			return list.iterator();
		} catch (Exception exception) {
			throw new StorageException("遍历JSON异常", exception);
		}
	}

}
