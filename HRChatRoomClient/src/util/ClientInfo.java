package util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ClientInfo {
	// 加载客户端配置文件
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

	private static String serverAddr = (String) prop.getProperty("serverAddr");
	private static int serverPort = Integer.valueOf((String) prop.getProperty("serverPort"));
	private static String clientPath = (String)prop.getProperty("client_path");
	private static String myclientPath = (String)prop.getProperty("myclient_path");
	private static String clientPass = (String)prop.getProperty("client_pass");
	private static String myclientPass = (String)prop.getProperty("myclient_pass");
	
	// 返回服务器绑定IP
	public static String getServerAddr() {
		return serverAddr;
	}

	// 返回服务器绑定端口
	public static int getServerPort() {
		return serverPort;
	}

	public static String getClientPath() {
		return clientPath;
	}

	public static String getMyclientPath() {
		return myclientPath;
	}

	public static String getClientPass() {
		return clientPass;
	}

	public static String getMyclientPass() {
		return myclientPass;
	}
	
	public static void main(String[] args) {
		System.out.println(getClientPass());
		System.out.println(getClientPath());
		System.out.println(getMyclientPass());
		System.out.println(getMyclientPath());
	}
}
