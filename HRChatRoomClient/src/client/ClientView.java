package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import client.entity.Record;
import util.GoUrl;
import util.Message;
import util.MessageType;
import util.Translate;

public class ClientView {

	private DatagramSocket clientSocket; // 客户端套接字
	private Message msg; // 消息对象
	private byte[] data = new byte[8096];

	private JFrame frame;
	private RevMessage client;
	private JList<String> userList;
	private JTextArea textArea, txt_msg;
	private JTextField txt_port, txt_hostIP, txt_name;
	private JButton btn_start, btn_stop, btn_send, btn_sendFile, btn_goNew, btn_goGame;
	private JPanel northPanel, eastPanel, southPanel, panel;
	private JScrollPane rightScroll, leftScroll, msgScroll;
	private JSplitPane centerSplit, rightSplit;
	private DefaultListModel<String> listModel;
	private String currentUser, chatUser;
	private boolean isGroup;

	// private List<User> onlineUsers;

	private static String[] DEFAULT_FONT = new String[] { "Table.font", "TableHeader.font", "CheckBox.font",
			"Tree.font", "Viewport.font", "ProgressBar.font", "RadioButtonMenuItem.font", "ToolBar.font",
			"ColorChooser.font", "ToggleButton.font", "Panel.font", "TextArea.font", "Menu.font", "TableHeader.font",
			"OptionPane.font", "MenuBar.font", "Button.font", "Label.font", "PasswordField.font", "ScrollPane.font",
			"MenuItem.font", "ToolTip.font", "List.font", "EditorPane.font", "Table.font", "TabbedPane.font",
			"RadioButton.font", "CheckBoxMenuItem.font", "TextPane.font", "PopupMenu.font", "TitledBorder.font",
			"ComboBox.font" };

	public ClientView(DatagramSocket clientSocket, Message msg) {
		this();
		this.clientSocket = clientSocket;
		this.msg = msg;
		initConfig();
		// 创建客户机消息接受和处理线程
		client = new RevMessage(clientSocket, this);
		client.start();
	}

	public ClientView() {
		initialGUI();
		addListeners();
	}

	public void initConfig() {
		// 信息初始化
		this.currentUser = msg.getUserId();
		this.chatUser = "HR聊天室";
		this.listModel.addElement(chatUser);
		this.userList.setSelectedIndex(0);
		this.isGroup = true;
	}

	private void formWindowClosing(WindowEvent event) {
		try {
			msg.setType(MessageType.M_QUIT);
			msg.setText(null);
			data = Translate.ObjectToByte(msg);
			// 发送
			DatagramPacket packet = new DatagramPacket(data, data.length, msg.getToAddr(), msg.getToPort());
			clientSocket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (clientSocket != null)
				clientSocket.close();
		}

	}
	
	private void sendFileListener(ActionEvent e) {
		// TODO Auto-generated method stub
		JFileChooser fd = new JFileChooser();
		fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fd.showOpenDialog(null);
		// 获取选中的文件
		File f = fd.getSelectedFile();
		SwingWorker<List<String>, String> sender = new SendFile(f, msg, this);
		sender.execute();
	}

	private void addListeners() {

		// 添加窗体事件监听器
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				formWindowClosing(e);
			}
		});

		// 发送消息
		btn_send.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String txt = txt_msg.getText();
				System.out.println(isGroup);
				// 私聊
				if (!txt.isEmpty() && !isGroup) {
					sendMessage(MessageType.M_MSG, txt, chatUser);
					receiveMessage("我", txt);
					client.addChatRecords(MessageType.M_MSG, chatUser, "我", txt);
					txt_msg.setText("");
					// 群聊
				} else if (!txt.isEmpty() && isGroup) {
					sendMessage(MessageType.M_GROUP, txt, "");
					receiveMessage("我", txt);
					client.addChatRecords(MessageType.M_MSG, chatUser, "我", txt);
					txt_msg.setText("");
				} else {
					JOptionPane.showMessageDialog(frame, "消息不能为空!", "", JOptionPane.WARNING_MESSAGE);
				}
			}
		});

		// 发送文件
		btn_sendFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendFileListener(e);
			}
		});

		// 切换窗口
		userList.addMouseListener(new MouseAdapter() {

			// 用户切换窗口，加载聊天记录
			@Override
			public void mousePressed(java.awt.event.MouseEvent e) {
				// 获取选中的列表值
				String content = (String) userList.getSelectedValue();
				// 获取选中的索引
				int i = userList.getSelectedIndex();
				System.out.println("切换窗口" + content);
				// 处理点击新消息列
				if (content != null && content.contains("(New Message)")) {
					chatUser = content.substring(0, content.indexOf('('));
					listModel.add(i, chatUser);
					listModel.remove(i + 1);
				} else {
					chatUser = content;
				}
				// 判断是否群聊
				if (chatUser.contains("HR聊天室")) {
					isGroup = true;
				} else {
					isGroup = false;
				}
				textArea.setText("");
				ArrayList<Record> chatRecords = client.getChatRecords(chatUser);
				for (int j = 0; j < chatRecords.size(); j++) {
					// 将聊天消息展示到聊天界面
					receiveMessage(chatRecords.get(j).getSender(), chatRecords.get(j).getText());
				}
			}
		});
		
		// 打开新闻链接
		btn_goNew.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				GoUrl.openURL("http://news.sise.com.cn/");
			}
		});
		
		// 打开游戏
		btn_goGame.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				GoUrl.openURL("https://2048game.com/");
			}
		});
	}

	private void initialGUI() {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		UIManager.put("RootPane.setupButtonVisible", false);

		// 调整默认字体
		for (int i = 0; i < DEFAULT_FONT.length; i++)
			UIManager.put(DEFAULT_FONT[i], new Font("Microsoft YaHei UI", Font.PLAIN, 15));

		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setForeground(Color.gray);

		txt_msg = new JTextArea();
		txt_port = new JTextField("8080");
		txt_hostIP = new JTextField();
		txt_name = new JTextField("");
		btn_start = new JButton("连接");
		btn_stop = new JButton("退出");
		btn_send = new JButton("发送");
		btn_sendFile = new JButton("发送文件");
		btn_goNew = new JButton("新闻");
		btn_goGame = new JButton("游戏");

		listModel = new DefaultListModel<String>();
		userList = new JList<String>(listModel);

		northPanel = new JPanel();

		GridBagLayout gridBagLayout = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();

		northPanel.setLayout(gridBagLayout);

		constraints.insets = new Insets(0, 5, 0, 5);
		constraints.fill = GridBagConstraints.BOTH;

		JLabel label;

		constraints.weightx = 1.0;
		label = new JLabel("端口");
		gridBagLayout.setConstraints(label, constraints);
		northPanel.add(label);

		constraints.weightx = 3.0;
		gridBagLayout.setConstraints(txt_port, constraints);
		northPanel.add(txt_port);

		constraints.weightx = 1.0;
		label = new JLabel("服务器IP");
		gridBagLayout.setConstraints(label, constraints);
		northPanel.add(label);

		constraints.weightx = 3.0;
		gridBagLayout.setConstraints(txt_hostIP, constraints);
		northPanel.add(txt_hostIP);

		constraints.weightx = 1.0;
		label = new JLabel("姓名");
		gridBagLayout.setConstraints(label, constraints);
		northPanel.add(label);

		constraints.weightx = 3.0;
		gridBagLayout.setConstraints(txt_name, constraints);
		northPanel.add(txt_name);
		gridBagLayout.setConstraints(btn_start, constraints);
		northPanel.add(btn_start);
		gridBagLayout.setConstraints(btn_stop, constraints);
		northPanel.add(btn_stop);

		// northPanel.setBorder(new TitledBorder("连接信息"));

		rightScroll = new JScrollPane(textArea);
		rightScroll.setBorder(new TitledBorder("聊天消息"));

		leftScroll = new JScrollPane(userList);
		leftScroll.setBorder(new TitledBorder("在线用户"));

		msgScroll = new JScrollPane(txt_msg);

		southPanel = new JPanel(new BorderLayout());
		southPanel.add(msgScroll, "Center");

		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		btn_send.setMargin(new Insets(5, 20, 5, 20));
		btn_sendFile.setMargin(new Insets(5, 20, 5, 20));
		btn_goNew.setMargin(new Insets(5, 20, 5, 20));
		btn_goGame.setMargin(new Insets(5, 20, 5, 20));
		panel.add(btn_goGame);
		panel.add(btn_goNew);
		panel.add(btn_sendFile);
		panel.add(btn_send);

		southPanel.add(panel, "South");
		southPanel.setBorder(new TitledBorder("发送"));

		rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rightScroll, southPanel);
		rightSplit.setDividerLocation(400);

		eastPanel = new JPanel(new BorderLayout());
		eastPanel.add(rightSplit, "Center");

		centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, eastPanel);
		centerSplit.setDividerLocation(200);

		frame = new JFrame();
		frame.setSize(800, 600);

		frame.setLayout(new BorderLayout());
		frame.add(centerSplit, "Center");

		int screenWidth = toolkit.getScreenSize().width;
		int screenHeight = toolkit.getScreenSize().height;

		frame.setLocation((screenWidth - frame.getWidth()) / 2, (screenHeight - frame.getHeight()) / 2);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	/**
	 * 发送报文
	 * 
	 * @param type     报文协议类型
	 * @param text     消息内容
	 * @param targetId 目标
	 */
	public void sendMessage(int type, String text, String targetId) {
		try {
			msg.setType(type);
			msg.setText(text);
			msg.setTargetId(targetId);
			data = Translate.ObjectToByte(msg);
			DatagramPacket packet = new DatagramPacket(data, data.length, msg.getToAddr(), msg.getToPort());
			clientSocket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 写入信息到消息列表
	 * 
	 * @param user
	 * @param text
	 */
	public void receiveMessage(String user, String text) {
		textArea.append(user + " : " + text);
		textArea.append("\r\n\r\n");
	}

	/**
	 * 根据报文协议类型，更新用户列表显示 e.g 收到群聊新消息后，若当前的JList索引不在群聊上，则群聊名称后显示(New Message)
	 * 
	 * @param type
	 * @param sender
	 * @param text
	 */
	public void updateGUI(int type, String sender, String text) {
		if (type == MessageType.M_GROUP) {
			if (chatUser.equals("HR聊天室")) {
				receiveMessage(sender, text);
			} else {
				String name = (String) listModel.elementAt(0);
				listModel.add(0, name + "(New Message)");
				listModel.remove(1);
			}
		} else if (type == MessageType.M_MSG) {
			if (chatUser.equals(sender)) {
				receiveMessage(sender, text);
			} else {
				for (int i = 0; i < listModel.size(); i++) {
					String name = (String) listModel.elementAt(i);
					if (name.contains(sender)) {
						listModel.remove(i);
						listModel.add(i, name + "(New Message)");
						break;
					}
				}
			}
		} else if (type == MessageType.M_LOGIN || type == MessageType.M_ACK) {
			listModel.addElement(sender);
		} else if (type == MessageType.M_QUIT) {
			for (int i = 0; i < listModel.size(); i++) {
				String name = (String) listModel.elementAt(i);
				if (name.contains(sender)) {
					listModel.remove(i);
					return;
				}
			}
		} else if (type == MessageType.M_FILE) {
			JOptionPane.showMessageDialog(frame, text, "系统消息", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public void setTitle(String name) {
		frame.setTitle(name);
	}
	
	public static void main(String[] args) {
		new ClientView();
	}
}
