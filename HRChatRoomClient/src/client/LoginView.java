package client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import client.security.Cryptography;
import util.ClientInfo;
import util.Message;
import util.MessageType;
import util.Translate;

public class LoginView extends JFrame {
	private Container container;
	private JPanel panel_1, panel_2, panel_3;
	private JLabel lbId, lbPassword;
	private JTextField txId;
	private JPasswordField txPassword;
	private JButton btnLogin, btnRegist;
	private InetAddress remoteAddr = null;
	private DatagramSocket clientSocket = null;
	private int remotePort;

	public LoginView() {
		initComponent();
		addListener();
		addClientSocket();
	}

	private void addClientSocket() {
		try {
			// 获取服务器地址和端口
			remoteAddr = InetAddress.getByName(ClientInfo.getServerAddr());
			remotePort = ClientInfo.getServerPort();
			// 创建UDP套接字
			clientSocket = new DatagramSocket();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initComponent() {
		container = this.getContentPane();
		container.setLayout(new BorderLayout());

		panel_1 = new JPanel();
		panel_2 = new JPanel();
		panel_3 = new JPanel();
		lbId = new JLabel("用户名");
		lbPassword = new JLabel("密码");
		txId = new JTextField(10);
		txPassword = new JPasswordField(10);
		btnLogin = new JButton("登录");
		btnRegist = new JButton("注册");

		// 标题 north
		panel_1.add(new JLabel("HR聊天室"));
		// 输入 center
		lbId.setBounds(100, 20, 50, 20);
		txId.setBounds(160, 20, 120, 20);
		lbPassword.setBounds(100, 60, 50, 20);
		txPassword.setBounds(160, 60, 120, 20);
		panel_2.setLayout(null);
		panel_2.add(lbId);
		panel_2.add(txId);
		panel_2.add(lbPassword);
		panel_2.add(txPassword);
		// 按钮
		panel_3.setLayout(new FlowLayout());
		panel_3.add(btnLogin);
		panel_3.add(btnRegist);

		container.add(panel_1, "North");
		container.add(panel_2, "Center");
		container.add(panel_3, "South");

		this.setSize(400, 230);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	private void addListener() {
		btnLogin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LoginActionPerformed(e);
			}
		});

		btnRegist.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				RegistActionPerformed(e);
			}
		});
	}

	private void LoginActionPerformed(ActionEvent e) {
		try {
			String id = txId.getText();
			String password = String.valueOf(txPassword.getPassword());
			if (id.equals("") || password.equals("")) {
				JOptionPane.showMessageDialog(null, "帐号或密码不能为空！", "错误提示", JOptionPane.ERROR_MESSAGE);
				return;
			}
			clientSocket.setSoTimeout(3000);// 设置超时时间
			// 构建用户登录消息
			Message msg = new Message();
			msg.setUserId(id);// 登录名
			msg.setPassword(Cryptography.getHash(password, "SHA-256")); // 密码

			msg.setType(MessageType.M_LOGIN); // 登录消息类型
			msg.setToAddr(remoteAddr); // 目标地址
			msg.setToPort(remotePort); // 目标端口
			byte[] data = Translate.ObjectToByte(msg); // 消息对象序列化
			// 定义登录报文
			DatagramPacket packet = new DatagramPacket(data, data.length, remoteAddr, remotePort);
			// 发送登录报文
			clientSocket.send(packet);

			// 接收服务器回送的报文
			DatagramPacket backPacket = new DatagramPacket(data, data.length);
			clientSocket.receive(backPacket);

			clientSocket.setSoTimeout(0);// 取消超时时间
			Message backMsg = (Message) Translate.ByteToObject(data);
			// 处理登录结果
			if (backMsg.getType() == MessageType.M_SUCCESS) { // 登录成功
				System.out.println("用户" + msg.getUserId() + "登录成功");
				this.dispose(); // 关闭登录对话框
				ClientView client = new ClientView(clientSocket, msg); // 创建客户机界面
				client.setTitle(msg.getUserId()); // 设置标题
			} else { // 登录失败
				System.out.println("用户" + msg.getUserId() + "登录失败");
				JOptionPane.showMessageDialog(null, "用户ID或密码错误！\n\n登录失败！\n", "登录失败", JOptionPane.ERROR_MESSAGE);
			}

		} catch (IOException ex) {
			JOptionPane.showMessageDialog(null, ex.getMessage(), "登录错误", JOptionPane.ERROR_MESSAGE);
		} // end try
	}

	private void RegistActionPerformed(ActionEvent e) {
		try {
			String id = txId.getText();
			String password = String.valueOf(txPassword.getPassword());
			if (id.equals("") || password.equals("")) {
				JOptionPane.showMessageDialog(null, "帐号或密码不能为空！", "错误提示", JOptionPane.ERROR_MESSAGE);
				return;
			}
			clientSocket.setSoTimeout(3000);// 设置超时时间
			// 构建用户登录消息
			Message msg = new Message();
			msg.setUserId(id);// 登录名
			msg.setPassword(Cryptography.getHash(password, "SHA-256")); // 密码

			msg.setType(MessageType.M_REGIST); // 登录消息类型
			msg.setToAddr(remoteAddr); // 目标地址
			msg.setToPort(remotePort); // 目标端口
			byte[] data = Translate.ObjectToByte(msg); // 消息对象序列化
			// 定义登录报文
			DatagramPacket packet = new DatagramPacket(data, data.length, remoteAddr, remotePort);
			// 发送登录报文
			clientSocket.send(packet);

			// 接收服务器回送的报文
			DatagramPacket backPacket = new DatagramPacket(data, data.length);
			clientSocket.receive(backPacket);

			clientSocket.setSoTimeout(0);// 取消超时时间
			Message backMsg = (Message) Translate.ByteToObject(data);
			System.out.println(backMsg.getType());
			// 处理注册结果
			if (backMsg.getType() == MessageType.M_SUCCESS) { // 登录成功
				// 清空页面，重新输入账户密码
				txId.setText("");
				txPassword.setText("");
				System.out.println("用户" + msg.getUserId() + "注册成功");
			} else { // 注册失败
				// 清空页面，重新输入账户密码
				txId.setText("");
				txPassword.setText("");
				System.out.println("用户" + msg.getUserId() + "注册失败");
				JOptionPane.showMessageDialog(null, "用户ID已存在！\n\n注册失败！\n", "注册失败", JOptionPane.ERROR_MESSAGE);
			}
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(null, ex.getMessage(), "注册错误", JOptionPane.ERROR_MESSAGE);
		} // end try
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				new LoginView();
			}
		});
	}
}
