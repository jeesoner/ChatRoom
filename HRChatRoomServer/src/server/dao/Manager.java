package server.dao;

import java.util.HashSet;

import util.PropertiesUtil;

/**
 * 管理用户的注册登录
 * @author sakura
 *
 */
public class Manager {
	// 用户登录表
	private HashSet<String> userSet = new HashSet<String>(); 
	
	/**
	 * 处理用户登录，判断用户密码是否正确
	 */
	public boolean login(String userId, String password) {
		String value = PropertiesUtil.get(userId);
		if (value == null) {
			return false;
		}
		return value.equals(password);
	}
	
	
	/**
	 * 处理用户注册，判断id是否被注册过
	 * 注册过了返回flase
	 * 没有则注册用户返回true
	 */
	public boolean register(String userId, String password) {
		String value = PropertiesUtil.get(userId);
		// 这个userid还没被注册过
		if(value == null) {
			// 往配置文件写用户信息
			PropertiesUtil.update(userId, password);
			return true;
		}
		return false;
	}
	
	
	/**
	 * 判断用户是否已经登录
	 */
	public boolean isLogin() {
		return true;
	}
}
