package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.security.DigestOutputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLSocket;
import javax.swing.SwingWorker;
import javax.xml.bind.DatatypeConverter;

/**
 * RevFile类，接收文件的后台线程类
 * 
 * @author sakura
 */
public class RevFile extends SwingWorker<Integer, Object> {
	private final SSLSocket fileSocket; // 接收文件的套接字
	private ServerView parentUI; // 主窗体类
	private KeyStore tks; // 公钥库
	private KeyStore ks; // 私钥库
	private static final int BUFSIZE = 8096;// 缓冲区大小

	public RevFile(SSLSocket fileSocket, ServerView parentUI, KeyStore tks, KeyStore ks) {
		this.fileSocket = fileSocket;
		this.parentUI = parentUI;
		this.tks = tks;
		this.ks = ks;
	}

	@Override
	protected Integer doInBackground() throws Exception {
		String SERVER_KEY_STORE_PASSWORD = "123456"; // server.keystore私钥库密码
		// 获取服务器私钥
		PrivateKey privateKey = (PrivateKey) ks.getKey("server", SERVER_KEY_STORE_PASSWORD.toCharArray());
		// 获取客户机公钥
		PublicKey publicKey = (PublicKey) tks.getCertificate("client").getPublicKey();
		// 获取套接字输入流
		DataInputStream in = new DataInputStream(new BufferedInputStream(fileSocket.getInputStream()));
		// 1.接收文件名、文件长度
		String filename = in.readUTF(); // 文件名
		int fileLen = (int) in.readLong(); // 文件长度
		parentUI.updateLog("1.收到文件名：" + filename + "文件长度：" + fileLen + "字节");
		// 创建文件输出流
		File file = new File("./upload/" + filename);
		// 文件输出流
		BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream(file));
		// 定义消息摘要算法
		MessageDigest sha256 = MessageDigest.getInstance("SHA-256");// 256位
		// 基于文件输出流和摘要算法构建消息摘要流
		DigestOutputStream out = new DigestOutputStream(fout, sha256);
		// 2.接收文件内容，存储为外部文件
		byte[] buffer = new byte[BUFSIZE]; // 读入缓冲区
		int numRead = 0; // 单次读取的字节数
		int numFinished = 0;// 总完成字节数
		while (numFinished < fileLen && (numRead = in.read(buffer)) != -1) { // 输入流可读
			out.write(buffer, 0, numRead);
			numFinished += numRead; // 已完成字节数
		} // end while
		parentUI.updateLog("2.接收文件内容结束！");
		// 3.接收加密的数字签名
		int size = in.readInt();
		byte[] signature = new byte[size];
		int i = in.read(signature);
		parentUI.updateLog("3.收到加密的数字签名：" + DatatypeConverter.printHexBinary(signature));

		// 4.接收加密的密钥
		byte[] encryptKey = new byte[128];
		i = in.read(encryptKey);
		parentUI.updateLog("4.收到加密的密钥：" + DatatypeConverter.printHexBinary(encryptKey));

		// 用服务器私钥解密密钥
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");// 解密器
		cipher.init(Cipher.DECRYPT_MODE, privateKey); // 用服务器私钥初始化解密器
		byte[] decryptKey = cipher.doFinal(encryptKey);// 解密密钥
		parentUI.updateLog("密钥解密：" + DatatypeConverter.printHexBinary(decryptKey));

		// 用密钥解密数字签名
		SecretKey secretKey = new SecretKeySpec(decryptKey, "AES");
		Cipher cipher2 = Cipher.getInstance("AES");// 解密器
		cipher2.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] decryptSign = cipher2.doFinal(signature);// 解密数字签名
		parentUI.updateLog("签名解密：" + DatatypeConverter.printHexBinary(decryptSign));

		// "SHA-256"算法计算的摘要为256位，合32字节
		byte[] sourceDigest = new byte[32]; // 收到的摘要
		cipher.init(Cipher.DECRYPT_MODE, publicKey); // 用客户机公钥初始化解密器
		sourceDigest = cipher.doFinal(decryptSign); // 还原消息摘要
		parentUI.updateLog("去掉签名后的摘要：" + DatatypeConverter.printHexBinary(sourceDigest));
//更新显示

		// 5.根据文件输出流重新计算消息摘要
		byte[] computedDigest = new byte[32];// 重新计算的摘要
		computedDigest = out.getMessageDigest().digest();
		// 输出相关提示信息
		parentUI.updateLog("服务器根据收到的文件重新计算的摘要：" + DatatypeConverter.printHexBinary(computedDigest));

		// 定义字符输出流
		PrintWriter pw = new PrintWriter(fileSocket.getOutputStream(), true);
		// 比较重新计算的摘要与收到的摘要是否相同
		if (Arrays.equals(sourceDigest, computedDigest)) {// 验证数字签名
			pw.println("M_DONE"); // 回送成功消息
			parentUI.updateLog("5." + filename + "  接收成功!");
		} else {
			pw.println("M_LOST"); // 回送失败消息
			parentUI.updateLog("5." + filename + "  接收失败！");
		} // end if
			// 关闭流
		in.close();
		out.close();
		fout.close();
		pw.close();
		fileSocket.close();
		return 100;
	}// end doInBackground
}