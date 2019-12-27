package server;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import util.ServerInfo;

/**
 * 服务器窗口类
 * 
 * @author sakura
 *
 */
public class ServerView extends JFrame {
	private static final long serialVersionUID = 1L;
	int onlineCount = 0;
	RevMessage server;
	DefaultListModel<String> model = new DefaultListModel<String>();

	// 服务器信息面板
	JPanel pnlServer, pnlServerInfo;
	JLabel lblStatus, lblNumber, lblMax, lblServerName, lblIP, lblPort, lblLog;
	JTextField txtStatus, txtNumber, txtMax, txtServerName, txtIP, txtPort;
	JButton btnStart, btnStop, btnSaveLog;
	TextArea taLog;
	JTabbedPane tpServer;
	TextArea taMessage;

	// 用户信息面板
	JPanel pnlUser;
	JLabel lblMessage, lblUser, lblNotice, lblUserCount;
	JList<String> lstUser;
	JScrollPane spUser;
	JTextField txtNotice;
	JButton btnSend, btnKick;
	String ti = "";

	String serverAddr;
	int serverPort;

	public ServerView() {
		init();
		addListeners();
	}

	public void init() {
		// 初始化配置
		serverAddr = ServerInfo.getServerAddr();
		serverPort = ServerInfo.getServerPort();

		// 服务器窗口
		setTitle("HR聊天服务器");
		setSize(550, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setLocationRelativeTo(null);// 居中显示

		// ==========服务器信息面板=========================
		pnlServer = new JPanel();
		pnlServer.setLayout(null);
		pnlServer.setBackground(new Color(60, 150, 100));

		pnlServerInfo = new JPanel(new GridLayout(14, 1));
		pnlServerInfo.setBackground(new Color(60, 150, 100));
		pnlServerInfo.setFont(new Font("宋体", 0, 12));
		pnlServerInfo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(""),
				BorderFactory.createEmptyBorder(1, 1, 1, 1)));

		lblStatus = new JLabel("当前状态:");
		lblStatus.setForeground(Color.YELLOW);
		lblStatus.setFont(new Font("宋体", 0, 12));
		txtStatus = new JTextField("未启动", 10);
		txtStatus.setBackground(Color.decode("#d6f4f2"));
		txtStatus.setFont(new Font("宋体", 0, 12));
		txtStatus.setEditable(false);

		lblNumber = new JLabel("当前在线人数:");
		lblNumber.setForeground(Color.YELLOW);
		lblNumber.setFont(new Font("宋体", 0, 12));
		txtNumber = new JTextField("0 人", 10);
		txtNumber.setBackground(Color.decode("#d6f4f2"));
		txtNumber.setFont(new Font("宋体", 0, 12));
		txtNumber.setEditable(false);

		lblMax = new JLabel("最多在线人数:");
		lblMax.setForeground(Color.YELLOW);
		lblMax.setFont(new Font("宋体", 0, 12));
		txtMax = new JTextField("20 人", 10);
		txtMax.setBackground(Color.decode("#d6f4f2"));
		txtMax.setFont(new Font("宋体", 0, 12));
		txtMax.setEditable(false);

		lblServerName = new JLabel("服务器名称:");
		lblServerName.setForeground(Color.YELLOW);
		lblServerName.setFont(new Font("宋体", 0, 12));
		txtServerName = new JTextField("HR聊天服务器", 10);
		txtServerName.setBackground(Color.decode("#d6f4f2"));
		txtServerName.setFont(new Font("宋体", 0, 12));
		txtServerName.setEditable(false);

		lblIP = new JLabel("服务器IP:");
		lblIP.setForeground(Color.YELLOW);
		lblIP.setFont(new Font("宋体", 0, 12));
		txtIP = new JTextField(serverAddr, 10);
		txtIP.setBackground(Color.decode("#d6f4f2"));
		txtIP.setFont(new Font("宋体", 0, 12));
		txtIP.setEditable(false);

		lblPort = new JLabel("服务器端口:");
		lblPort.setForeground(Color.YELLOW);
		lblPort.setFont(new Font("宋体", 0, 12));
		txtPort = new JTextField(Integer.toString(serverPort), 10);
		txtPort.setBackground(Color.decode("#d6f4f2"));
		txtPort.setFont(new Font("宋体", 0, 12));
		txtPort.setEditable(false);

		btnStart = new JButton("启动服务器");
		btnStart.setBackground(Color.lightGray);
		btnStart.setFont(new Font("宋体", 0, 12));

		btnStop = new JButton("关闭服务器");
		btnStop.setBackground(Color.lightGray);
		btnStop.setFont(new Font("宋体", 0, 12));

		lblLog = new JLabel("[服务器日志]");
		lblLog.setForeground(Color.YELLOW);
		lblLog.setFont(new Font("宋体", 0, 12));

		taLog = new TextArea(20, 50);
		taLog.setFont(new Font("宋体", 0, 12));
		btnSaveLog = new JButton("保存日志");
		btnSaveLog.setBackground(Color.lightGray);
		btnSaveLog.setFont(new Font("宋体", 0, 12));

		pnlServerInfo.add(lblStatus);
		pnlServerInfo.add(txtStatus);
		pnlServerInfo.add(lblNumber);
		pnlServerInfo.add(txtNumber);
		pnlServerInfo.add(lblMax);
		pnlServerInfo.add(txtMax);
		pnlServerInfo.add(lblServerName);
		pnlServerInfo.add(txtServerName);
		pnlServerInfo.add(lblIP);
		pnlServerInfo.add(txtIP);
		pnlServerInfo.add(lblPort);
		pnlServerInfo.add(txtPort);

		pnlServerInfo.setBounds(5, 5, 100, 400);
		lblLog.setBounds(110, 5, 100, 30);
		taLog.setBounds(110, 35, 400, 370);
		btnStart.setBounds(120, 410, 120, 30);
		btnStop.setBounds(240, 410, 120, 30);
		btnSaveLog.setBounds(360, 410, 120, 30);
		pnlServer.add(pnlServerInfo);
		pnlServer.add(lblLog);
		pnlServer.add(taLog);
		pnlServer.add(btnStart);
		pnlServer.add(btnStop);
		pnlServer.add(btnSaveLog);

		// ===========在线用户面板====================
		pnlUser = new JPanel();
		pnlUser.setLayout(null);
		pnlUser.setBackground(new Color(60, 150, 100));
		pnlUser.setFont(new Font("宋体", 0, 12));
		lblMessage = new JLabel("[用户消息]");
		lblMessage.setFont(new Font("宋体", 0, 12));
		lblMessage.setForeground(Color.YELLOW);
		taMessage = new TextArea(20, 20);
		taMessage.setFont(new Font("宋体", 0, 12));
		lblNotice = new JLabel("通知：");
		lblNotice.setFont(new Font("宋体", 0, 12));
		txtNotice = new JTextField(20);
		txtNotice.setFont(new Font("宋体", 0, 12));
		btnSend = new JButton("发送");
		btnSend.setBackground(Color.lightGray);
		btnSend.setFont(new Font("宋体", 0, 12));
		btnSend.setEnabled(true);

		lblUserCount = new JLabel("在线总人数 0 人");
		lblUserCount.setFont(new Font("宋体", 0, 12));

		btnKick = new JButton("踢人");
		btnKick.setBackground(Color.lightGray);
		btnKick.setFont(new Font("宋体", 0, 12));
		lblUser = new JLabel("[在线用户列表]");
		lblUser.setFont(new Font("宋体", 0, 12));
		lblUser.setForeground(Color.YELLOW);

		lstUser = new JList<String>(model);
		lstUser.setFont(new Font("宋体", 0, 12));
		lstUser.setVisibleRowCount(17);
		lstUser.setFixedCellWidth(180);
		lstUser.setFixedCellHeight(18);

		spUser = new JScrollPane();
		spUser.setBackground(Color.decode("#15c7f3"));
		spUser.setFont(new Font("宋体", 0, 12));
		// spUser.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		spUser.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		spUser.getViewport().setView(lstUser);

		lblMessage.setBounds(5, 5, 100, 25);
		taMessage.setBounds(5, 35, 300, 360);
		taMessage.setEditable(false);
		lblUser.setBounds(310, 5, 100, 25);
		spUser.setBounds(310, 35, 220, 360);
		lblNotice.setBounds(5, 410, 40, 25);
		txtNotice.setBounds(50, 410, 160, 25);
		btnSend.setBounds(210, 410, 80, 25);
		lblUserCount.setBounds(320, 410, 100, 25);
		btnKick.setBounds(440, 410, 80, 25);

		pnlUser.add(lblMessage);
		pnlUser.add(taMessage);
		pnlUser.add(lblUser);
		pnlUser.add(spUser);

		pnlUser.add(lblNotice);
		pnlUser.add(txtNotice);
		pnlUser.add(btnSend);
		pnlUser.add(lblUserCount);
		pnlUser.add(btnKick);

		// ============主标签面板========================

		btnStop.setEnabled(false);
		tpServer = new JTabbedPane(JTabbedPane.TOP);
		tpServer.setBackground(Color.decode("#0ea276"));
		tpServer.setFont(new Font("宋体", 0, 12));
		tpServer.add("服务器管理", pnlServer);
		tpServer.add("公共聊天室", pnlUser);
		this.getContentPane().add(tpServer);
		setVisible(true);
	}

	private void addListeners() {
		// 启动服务器
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				btnStartActionPerformed(event);
			}
		});

		// 关闭服务器
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnCloseServer();
			}
		});

		// 保存日志
		btnSaveLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnSaveLog();
			}
		});

		// 发送通知
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnSendNotice();
			}
		});

		// 踢人
		btnKick.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnTiRen();
			}
		});
	}

	/**
	 * 管理员发通知
	 */
	private void btnSendNotice() {
		String text = txtNotice.getText();
		if (!text.isEmpty()) {
			txtNotice.setText("");
			System.out.println(text);

		} else {
			JOptionPane.showMessageDialog(getParent(), "消息不能为空!");
		}
	}

	private void btnSaveLog() {
		try {
			FileOutputStream fileoutput = new FileOutputStream("log/log.txt", true);
			String temp = taLog.getText();
			// System.out.println(temp);
			fileoutput.write(temp.getBytes());
			fileoutput.close();
			JOptionPane.showMessageDialog(null, "记录保存在log.txt");
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private void btnTiRen() {
		// TODO 自动生成方法存根

	}

	private void btnCloseServer() {
		// TODO 自动生成方法存根
		server.stopServer();
		this.dispose();
	}

	// 启动服务器按钮
	private void btnStartActionPerformed(ActionEvent event) {
		// 创建UDP数据报套接字,在指定端口侦听
		try {
			InetAddress sa = InetAddress.getByName(serverAddr);
			DatagramSocket serverSocket = new DatagramSocket(serverPort, sa);
			updateLog("服务器" + sa.getHostAddress() + ":" + serverSocket.getLocalPort() + "开始侦听...\n");
			// 启动通信子线程
			server = new RevMessage(serverSocket, this);
			server.start();
			txtStatus.setText("启动");
			
			// 启动文件接收线程
			FileThread fileThread = new FileThread();
			fileThread.start();
		}
		catch (SocketException e1) {
			e1.printStackTrace();
		} catch (UnknownHostException e2) {
			e2.printStackTrace();
		}
		
		btnStart.setEnabled(false);
		btnStop.setEnabled(true);
	}

	// 更新服务器日志
	public void updateLog(String msg) {
		taLog.append(ServerInfo.getCurrentTime() + " : " + msg + "\n");
	}

	// 更新消息列表
	public void updateMessageList(String msg) {
		taMessage.append(msg + "\n");
	}

	// 更新用户列表
	public void updateUserList(boolean type, String id) {
		// 添加在线用户
		if (type) {
			model.addElement(id);
			onlineCount++;
			lblUserCount.setText("在线总人数 " + onlineCount + " 人");
			txtNumber.setText(Integer.toString(onlineCount));
		} else { // 移除用户
			if (model.contains(id)) {
				model.removeElement(id);
				onlineCount--;
				lblUserCount.setText("在线总人数 " + onlineCount + " 人");
				txtNumber.setText(Integer.toString(onlineCount));
			}
		}

	}

	public static void main(String[] args) {
		new ServerView();
	}

	private class FileThread extends Thread {
		@Override
		public void run() {
			try {
				// 获取密钥库密码
				String serverPass = ServerInfo.getServerPass();
				String myserverPass = ServerInfo.getMyserverPass();
				// 获取密钥库路径
				String serverPath = ServerInfo.getServerPath();
				String myserverPath = ServerInfo.getMyserverPath();

				// 获取证书库
				InputStream key = new FileInputStream(serverPath);// 私钥库
				InputStream tkey = new FileInputStream(myserverPath);// 公钥库

				SSLContext ctx = SSLContext.getInstance("SSL");// SSL上下文
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
				// 服务器密钥库
				KeyStore ks = KeyStore.getInstance("JKS");
				// 可信任的密钥库
				KeyStore tks = KeyStore.getInstance("JKS");
				// 加载私钥证书库
				ks.load(key, serverPass.toCharArray());
				// 加载公钥证书库
				tks.load(tkey, myserverPass.toCharArray());
				kmf.init(ks, serverPass.toCharArray());
				tmf.init(tks);
				ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
				// 服务器侦听安全连接
				SSLServerSocket sslListenSocket = (SSLServerSocket) ctx.getServerSocketFactory()
						.createServerSocket(ServerInfo.getServerPort());
				int processors = Runtime.getRuntime().availableProcessors();// CPU数
				ExecutorService fixedPool = Executors.newFixedThreadPool(processors * 2);// 创建固定大小线程池
				while (true) { // 处理所有客户机连接
					SSLSocket fileSocket = (SSLSocket) sslListenSocket.accept();// 如果无连接，则阻塞，否则接受连接并创建新的会话套接字
					// 文件接收线程为SwingWorker类型的后台工作线程
					SwingWorker<Integer, Object> recver = new RevFile(fileSocket, ServerView.this, tks, ks); // 创建客户线程
					fixedPool.execute(recver); // 用线程池调度客户线程运行
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(null, ex.getMessage(), "错误提示", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
