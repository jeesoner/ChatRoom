package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

import server.dao.Manager;
import server.entity.User;
import util.Message;
import util.MessageType;
import util.Translate;

/**
 * 服务器接收消息和处理消息的线程类
 * 
 * @author sakura
 *
 */
public class RevMessage extends Thread {

	private DatagramSocket serverSocket = null; // 服务器套接字
	private DatagramPacket packet = null; // 通信报文
	private ServerView parentUI = null; // 窗口
	private List<User> userList = new ArrayList<User>(); // 在线用户列表
	private Manager manager = null;
	private boolean isRunnable = true;
	private byte[] data = new byte[8096]; // 8k

	/**
	 * 构造函数
	 * 
	 * @param serverSocket 服务器套接字
	 * @param ParentUI     操作窗口数据显示
	 */
	public RevMessage(DatagramSocket serverSocket, ServerView parentUI) {
		this.serverSocket = serverSocket;
		this.parentUI = parentUI;
		manager = new Manager();
	}

	public void stopServer() {
		isRunnable = false;
	}

	@Override
	public void run() {
		int type = -1;
		// 循环处理收到的各种消息
		while (isRunnable) {
			try {
				packet = new DatagramPacket(data, data.length); // 接收报文
				serverSocket.receive(packet); // 接收数据
				// 将收到的信息转化为对象
				Message msg = (Message) Translate.ByteToObject(packet.getData());
				// 获取消息类型
				// 消息类型有登录，注册，发信息，确认，成功，失败
				type = msg.getType();
				parentUI.updateLog("接受到客户端数据:" + type);

				switch (type) {
				case MessageType.M_REGIST:
					registHandle(msg.getUserId(), msg.getPassword());
					break;

				case MessageType.M_LOGIN:
					loginHandle(msg.getUserId(), msg.getPassword());
					break;

				case MessageType.M_GROUP:
					groupMessageHandle(msg.getUserId(), msg.getText());
					break;

				case MessageType.M_MSG:
					messageHandle(msg.getUserId(), msg.getTargetId());
					break;

				case MessageType.M_QUIT:
					logoutHandle(msg.getUserId());
					break;

				default:
					break;
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 处理注册
	 * @throws IOException 
	 */
	public void registHandle(String userId, String password) throws IOException {
		Message backMsg = new Message();
		// 注册成功
		if (manager.register(userId, password)) {
			backMsg.setType(MessageType.M_SUCCESS);
			byte[] buf = Translate.ObjectToByte(backMsg);
			DatagramPacket backPacket = new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
			serverSocket.send(backPacket);
		// 注册失败
		} else {
			backMsg.setType(MessageType.M_FAILURE);
			byte[] buf = Translate.ObjectToByte(backMsg);
			DatagramPacket backPacket = new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
			serverSocket.send(backPacket);
		}
	}

	/**
	 * 处理登录
	 * @param userId
	 * @throws IOException
	 */
	public void loginHandle(String userId, String password) throws IOException {
		Message backMsg = new Message();
		// 登录成功
		// if ("1000".equals(userId) || "2000".equals(userId) || "4000".equals(userId)) {
		if (manager.login(userId, password)) {
			// 回送成功报文
			backMsg.setType(MessageType.M_SUCCESS);
			byte[] buf = Translate.ObjectToByte(backMsg);
			DatagramPacket backPacket = new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
			serverSocket.send(backPacket);

			User user = new User();
			user.setId(userId); // 保存用户名
			user.setPacket(packet); // 保存收到的报文
			userList.add(user);
			// 更新服务器日志
			parentUI.updateLog(userId + "登录");
			// 更新在线用户列表
			parentUI.updateUserList(true, userId);

			// 向其他用户发送M_LOGIN消息，向新用户发送在线用户列表
			DatagramPacket oldPacket, newPacket;
			Message current = new Message();
			for (int i = 0; i < userList.size(); i++) {
				// 遍历在线用户列表
				// if 老用户 发送新用户信息
				// 发送所有老用户信息 if 新用户 不发送
				// 给老用户发送M_LOGIN消息，此出的data是新用户发送过来的数据的缓存，里面的类型是M_LOGIN
				if (!userId.equalsIgnoreCase(userList.get(i).getId())) {
					oldPacket = userList.get(i).getPacket(); // 老用户的报文
					newPacket = new DatagramPacket(data, data.length, oldPacket.getAddress(), // 待发送的新报文
							oldPacket.getPort());
					serverSocket.send(newPacket);
					// 向当前用户发送M_ACK信息-在线用户列表(不算自己)
					// 给新用户发送的信息
					System.out.println("发送的信息ACK给" + userId + userList.get(i).getId());
					current.setUserId(userList.get(i).getId()); // 设置发送报文的用户id为老用户id
					current.setType(MessageType.M_ACK);
					byte[] buffer = Translate.ObjectToByte(current);
					newPacket = new DatagramPacket(buffer, buffer.length, packet.getAddress(), packet.getPort());
					serverSocket.send(newPacket);
				}
			}
		} else { // 登录失败
			backMsg.setType(MessageType.M_FAILURE);
			byte[] buf = Translate.ObjectToByte(backMsg);
			DatagramPacket backPacket = new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
			serverSocket.send(backPacket);
		}
	}

	/**
	 * 
	 * @param userId
	 * @param text
	 * @throws IOException
	 */
	public void groupMessageHandle(String userId, String text) throws IOException {
		// 更新服务器消息列表 public
		parentUI.updateMessageList(userId + ": " + text);

		// 遍历其他用户发送消息
		DatagramPacket oldPacket, newPacket;
		for (int i = 0; i < userList.size(); i++) {
			if (!userList.get(i).getId().equalsIgnoreCase(userId)) {
				oldPacket = userList.get(i).getPacket();
				newPacket = new DatagramPacket(data, data.length, oldPacket.getAddress(), oldPacket.getPort());
				serverSocket.send(newPacket);
			}
		}
	}

	/**
	 * 
	 * @param userId   来源用户id
	 * @param targetId 目标用户id
	 * @param text     发送的内容
	 * @throws IOException
	 */
	public void messageHandle(String userId, String targetId) throws IOException {
		// 转发给目标用户 遍历列表取得目标用户的报文
		for (int i = 0; i < userList.size(); i++) {
			if (userList.get(i).getId().equalsIgnoreCase(targetId)) {
				DatagramPacket oldPacket = userList.get(i).getPacket();
				DatagramPacket newPacket = new DatagramPacket(data, data.length, oldPacket.getAddress(),
						oldPacket.getPort());
				serverSocket.send(newPacket);
				return;
			}
		}
	}

	/**
	 * 
	 * @param userId
	 * @throws IOException
	 */
	public void logoutHandle(String userId) throws IOException {
		// 从在线用户列表移除该用户
		for (int i = 0; i < userList.size(); i++) {
			if (userList.get(i).getId().equalsIgnoreCase(userId)) {
				userList.remove(i);
				break;
			}
		}
		// 从服务器面板移除用户，更新日志
		parentUI.updateUserList(false, userId);
		parentUI.updateLog(userId + "下线");
		// 向其他用户转发下线信息
		DatagramPacket oldPacket, newPacket;
		for (int i = 0; i < userList.size(); i++) {
			oldPacket = userList.get(i).getPacket();
			newPacket = new DatagramPacket(data, data.length, oldPacket.getAddress(), oldPacket.getPort());
			serverSocket.send(newPacket);
		}
	}
}
