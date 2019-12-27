package client.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

/**
 * 封装一些通用的加密算法
 * getHash，对字符串明文计算摘要，主要用于密码加密传输和将密码以摘要形式保存于数据库
 * generateNewKey，生成AES对称密钥
 * @author sakura
 *
 */
public class Cryptography {
	private static final int BUFSIZE=8192;  //缓冲区大小
	
	public static String getHash(String plainText, String hashType) {
		try {
			MessageDigest md = MessageDigest.getInstance(hashType);
			byte[] encrytString = md.digest(plainText.getBytes("UTF-8"));
			return DatatypeConverter.printHexBinary(encrytString);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			return null;
		}
	}
	
	public static SecretKey generateNewKey() {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(128);
			SecretKey secretKey = keyGenerator.generateKey();
			return secretKey;
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
	
	private Cryptography() {}
}
