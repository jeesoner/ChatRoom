package server.entity;

import java.net.DatagramPacket;

/**
 * @author sakura
 *
 */
public class User {
	private String id;
	private String password;
	private DatagramPacket packet;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public DatagramPacket getPacket() {
		return packet;
	}
	public void setPacket(DatagramPacket packet) {
		this.packet = packet;
	}
	@Override
	public String toString() {
		return "User [id=" + id + ", password=" + password + ", packet=" + packet + "]";
	}
}
