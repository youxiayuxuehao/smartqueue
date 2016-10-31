package com.xuehao.smartqueue.utils;

public class StringUtils {

	public static boolean isEmpty(String str) {
		return null == str || "".equals(str);
	}

	public static boolean isBoolean(String str) {
		if (null != str) {
			str = str.toLowerCase().trim();
			return "true".equals(str) || "false".equals(str);
		}
		return false;
	}
}
