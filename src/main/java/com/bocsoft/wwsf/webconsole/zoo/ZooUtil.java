package com.bocsoft.wwsf.webconsole.zoo;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.util.IOUtils;
import com.alibaba.fastjson.util.ThreadLocalCache;

@SuppressWarnings("deprecation")
public class ZooUtil {

	public static Object deserializer(byte[] json) {
		return parse(json);
	}

	public static <T> T deserializer(byte[] json, Class<T> clzz) {
		return parseObject(json, clzz);
	}

	public static Object parse(byte[] input) {
		if (input == null || input.length == 0) {
			return null;
		}

		int featureValues = JSON.DEFAULT_PARSER_FEATURE;
		CharsetDecoder charsetDecoder = ThreadLocalCache.getUTF8Decoder();
		charsetDecoder.reset();
		int scaleLength = (int) (input.length * (double) charsetDecoder.maxCharsPerByte());
		char[] chars = ThreadLocalCache.getChars(scaleLength);

		ByteBuffer byteBuf = ByteBuffer.wrap(input);
		CharBuffer charBuf = CharBuffer.wrap(chars);
		IOUtils.decode(charsetDecoder, byteBuf, charBuf);

		int position = charBuf.position();
		DefaultJSONParser parser = new DefaultJSONParser(chars, position, ParserConfig.getGlobalInstance(),
				featureValues);
		Object value = parser.parse();
		parser.handleResovleTask(value);
		parser.close();

		return value;
	}

	public static <T> T parseObject(byte[] input, Class<T> clazz) {
		if (input == null || input.length == 0) {
			return null;
		}

		int featureValues = JSON.DEFAULT_PARSER_FEATURE;
		CharsetDecoder charsetDecoder = ThreadLocalCache.getUTF8Decoder();
		charsetDecoder.reset();
		int scaleLength = (int) (input.length * (double) charsetDecoder.maxCharsPerByte());
		char[] chars = ThreadLocalCache.getChars(scaleLength);

		ByteBuffer byteBuf = ByteBuffer.wrap(input);
		CharBuffer charBuf = CharBuffer.wrap(chars);
		IOUtils.decode(charsetDecoder, byteBuf, charBuf);

		int position = charBuf.position();

		DefaultJSONParser parser = new DefaultJSONParser(chars, position, ParserConfig.getGlobalInstance(),
				featureValues);
		T value = (T) parser.parseObject(clazz);
		parser.handleResovleTask(value);
		parser.close();

		return (T) value;
	}

	public static byte[] serializer(Object json, String[] excludes) {
		return toJsonBytes(json, excludes);
	}

	public static byte[] serializer(Object json) {
		return toJsonBytes(json, null);
	}

	public static byte[] toJsonBytes(Object object, String[] excludes) {

		SerializeWriter out = new SerializeWriter();
		try {
			JSONSerializer serializer = new JSONSerializer(out);
			if (excludes != null && excludes.length > 0) {
				for (final String exclude : excludes) {
					PropertyFilter pf = new PropertyFilter() {
						public boolean apply(Object object, String name, Object value) {
							return !name.equals(exclude);
						}
					};
					serializer.getPropertyFilters().add(pf);
				}
			}
			serializer.write(object);
			return out.toBytes("UTF-8");
		} finally {
			out.close();
		}
	}
}
