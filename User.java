package sentinelFormatter.bot;

import java.util.Date;

public class User {
	
	private String chatId;
	private SentinelMessage sentinelMessage;
	private Date lastUpdate;
		
	public User(String chatId) {
		this.chatId = chatId;
		sentinelMessage = new SentinelMessage();
		lastUpdate = new Date();
	}
	
	public String getChatId() {
		return chatId;
	}
	public void setChatId(String chatId) {
		this.chatId = chatId;
	}
	public SentinelMessage getSentinelMessage() {
		return sentinelMessage;
	}
	public void setSentinelMessage(SentinelMessage sentinelMessage) {
		this.sentinelMessage = sentinelMessage;
	}
	public Date getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
}
