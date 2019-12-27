package client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

import client.entity.Record;
import util.Message;
import util.MessageType;
import util.Translate;

public class RevMessage extends Thread {
	private DatagramSocket clientSocket;
	private ClientView parentUI;
	private String groupName = "HR聊天室";
	// 保存用户聊天记录
	private HashMap<String, ArrayList<Record>> chatRecords = new HashMap<String, ArrayList<Record>>();
	private byte[] data = new byte[8096];

	public RevMessage(DatagramSocket clientSocket, ClientView parentUI) {
		this.clientSocket = clientSocket;
		this.parentUI = parentUI;
		chatRecords.put(groupName, new ArrayList<Record>());
	}

	public ArrayList<Record> getChatRecords(String userId) {
		return chatRecords.get(userId);
	}
	
	public void addChatRecords(int type, String userId, String sender, String text) {
		// 判断聊天记录表中是否有该用户，没有则添加新用户
		if(!chatRecords.containsKey(userId)) {
			chatRecords.put(userId, new ArrayList<Record>());
		}
		Record record = new Record(type, sender, text);
		chatRecords.get(userId).add(record);
	}
	
	@Override
	public void run() {
		while(true) { // 接收消息
			try {
				DatagramPacket packet = new DatagramPacket(data, data.length);
				clientSocket.receive(packet); // 接收报文
				Message msg = (Message) Translate.ByteToObject(data);
				System.out.println("来自" + msg.getUserId() + "用户的协议：" + msg.getType());
				String userId = msg.getUserId();
				// 通过协议分类处理
				
				// 其他用户登录消息
				if (msg.getType() == MessageType.M_LOGIN || msg.getType() == MessageType.M_ACK) {
					chatRecords.put(msg.getUserId(), new ArrayList<Record>());
					parentUI.updateGUI(msg.getType(), msg.getUserId(), "");
				// 服务器确认消息
				} else if (msg.getType() == MessageType.M_ACK) {
					parentUI.updateGUI(msg.getType(), msg.getUserId(), "");
				// 群聊消息
				} else if (msg.getType() == MessageType.M_GROUP) {
					addChatRecords(msg.getType(), groupName, msg.getUserId(), msg.getText());
					parentUI.updateGUI(msg.getType(), msg.getUserId(), msg.getText());
				// 普通会话消息
				} else if (msg.getType() == MessageType.M_MSG) {
					addChatRecords(msg.getType(), msg.getUserId(), msg.getUserId(), msg.getText());
					parentUI.updateGUI(msg.getType(), msg.getUserId(), msg.getText());
				// 其他用户下线消息
				} else if (msg.getType() == MessageType.M_QUIT) {
					parentUI.updateGUI(msg.getType(), msg.getUserId(), "");
				}
			} catch (Exception e) {
				
			}
		}
	}
}
