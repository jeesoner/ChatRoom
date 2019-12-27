package util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * 用户配置文件工具类
 * @author sakura
 *
 */
public class PropertiesUtil {

	private static String path = "config/userinfo.properties";
	private static Properties prop = new Properties();
	
	static {
		try {
			prop.load(new FileInputStream(path));
		} catch (FileNotFoundException e) {
			System.out.println("找不到服务器配置文件");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 通过key获取value
	 * 
	 * @param key
	 * @return
	 */
	public static String get(String key) {
		return prop.getProperty(key);
	}

	/**
	 * 修改或者新增key
	 * 
	 * @param key
	 * @param value
	 */
	public static void update(String key, String value) {
		prop.setProperty(key, value);
		FileOutputStream fo = null;
		try {
			fo = new FileOutputStream(path);
			// 将Properties中的属性列表（键和元素对）写入输出流
			prop.store(fo, "");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fo.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 通过key删除value
	 * 
	 * @param key
	 */
	public static void delete(String key) {
		prop.remove(key);
		FileOutputStream oFile = null;
		try {
			oFile = new FileOutputStream(path);
			prop.store(oFile, "");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				oFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 循环所有key value
	 */
	public static void list() {
		Enumeration en = prop.propertyNames(); // 得到配置文件的名字
		while (en.hasMoreElements()) {
			String strKey = (String) en.nextElement();
			String strValue = prop.getProperty(strKey);
			System.out.println(strKey + "=" + strValue);
		}
	}
}