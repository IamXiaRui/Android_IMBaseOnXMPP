package com.imbaseonxmpp.utils;

import opensource.jpinyin.PinyinFormat;
import opensource.jpinyin.PinyinHelper;

/**
 * 汉字转拼音工具类
 */
public class PinyinUtil {
	public static String getPinyin(String str) {
		return PinyinHelper.convertToPinyinString(str, "", PinyinFormat.WITHOUT_TONE);
	}
}
