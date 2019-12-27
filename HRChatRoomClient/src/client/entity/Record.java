package client.entity;


/**
 * 消息记录条目
 * 保存了用户的每一条消息
 * @author sakura
 *
 */
public class Record {
	private int type; // 消息类型
	private String sender; // 消息发送者
	private String text; // 消息内容
	
	public Record() {
		
	}
	
	public Record(int type, String sender, String text) {
		this.type = type;
		this.sender = sender;
		this.text = text;
	}
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getSender() {
		return sender;
	}
	
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	@Override
	public String toString() {
		return "Record [type=" + type + ", text=" + text + "]";
	}
}
