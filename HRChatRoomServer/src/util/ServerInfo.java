package util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class ServerInfo {
	// 加载服务器配置文件
	private static String configPath = "config/project.properties";
	private static Properties prop = new Properties();
	static {
		try {
			prop.load(new FileInputStream(configPath));
		} catch (FileNotFoundException e) {
			System.out.println("找不到服务器配置文件");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String serverAddr = (String)prop.getProperty("serverAddr");
	private static int serverPort = Integer.valueOf((String)prop.getProperty("serverPort"));
	private static String serverPath = (String)prop.getProperty("server_path");
	private static String myserverPath = (String)prop.getProperty("myserver_path");
	private static String serverPass = (String)prop.getProperty("server_pass");
	private static String myserverPass = (String)prop.getProperty("myserver_pass");
	
	// 返回服务器绑定IP
	public static String getServerAddr() {
		return serverAddr;
	}
	
	// 返回服务器绑定端口
	public static int getServerPort() {
		return serverPort;
	}
	
	// 返回服务器密钥库路径
	public static String getServerPath() {
		return serverPath;
	}

	// 返回服务器可信任密钥库路径
	public static String getMyserverPath() {
		return myserverPath;
	}

	// 返回服务器密钥库密码
	public static String getServerPass() {
		return serverPass;
	}

	// 返回服务器可信任密钥库密码
	public static String getMyserverPass() {
		return myserverPass;
	}

	// 获取当前时间
	public static String getCurrentTime() {
		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
		return dateFormat.format(date);
	}
	
}
