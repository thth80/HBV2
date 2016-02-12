package project.model;

public class ChatManager {
	private StringStack chatEntries;
	
	public ChatManager()
	{
		chatEntries = new StringStack();
	}
	
	public String formatChatString(String username, String chat)
	{
		return "["+username + "]: " + chat; 
	}
	
	
	public void storeChatEntry(String fullEntry)
	{
		chatEntries.addStringEntryLast(fullEntry);
	}
	
	public String[] getRecentChat()
	{
		if(chatEntries.getSize() > 70)
			chatEntries.cutStack(50);
		return chatEntries.getAllStrings();
	}
}
