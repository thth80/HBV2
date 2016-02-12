package project.model;

public class UnreadMessageStorage {
	private static UserStack msgsAwaitingUsers = new UserStack();
	
	private UnreadMessageStorage() //HVAÐ MEÐ skilaboð til BOT?
	{
		
	}
	
	public synchronized static void addUser(String username) 
	{														
		msgsAwaitingUsers.addUser(username);
	}
	public synchronized static void removeUser(String username)
	{
		msgsAwaitingUsers.removeUser(username);
	}
	
	public static void storeMessageLast(String username, Message msg)
	{
		msgsAwaitingUsers.storeMessageLast(username, msg);
	}
	
	public static void storeMessage(String username, Message msg)
	{
		if(username.equals("HARD_BOT") || username.equals("MED_BOT")|| username.equals("EASY_BOT")) return;
		Message[] msgs = {msg};
		msgsAwaitingUsers.storeMessages(username, msgs);
	}
	
	public static void storeDelayedMessage(String username, Message msg, int callCount)
	{
		Message[] msgs = {msg};
		storeDelayedMessages(username, msgs, callCount);
	}
	
	public static void storeDelayedMessages(String username, Message[] msgs, int callCount)
	{
		msgsAwaitingUsers.storeDelayedMessages(username, msgs, callCount);
	}
	
	public static void storeMessages(String username, Message[] msgs)
	{
		msgsAwaitingUsers.storeMessages(username, msgs);
	}
	public static Message[] retrieveUserMessages(String username)
	{
		return msgsAwaitingUsers.getMessages(username);
	}
	
	public static Message[] dumpAllMessages()
	{
		String[] allUsers = msgsAwaitingUsers.getAllUsers();
		Message[] msgs = new Message[0];
		for(String user: allUsers)
			msgs = Message.mergeMessages(msgs, msgsAwaitingUsers.getMessages(user));
		
		return msgs;
	}
	public static String[] dumpAllUsers()
	{
		return msgsAwaitingUsers.getAllUsers();
	}
}
