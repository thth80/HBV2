package project.model;

public class OnlineLobbyManager {
	
	private static StringStack waitingList = new StringStack();
	private static StringStack matchList = new StringStack();
	private static ChatManager chatManager = new ChatManager();
	private static UserStack userList = new UserStack();
	private static int globalId = 0;
	
	private OnlineLobbyManager()
	{
		
	}
	
	public static int createOfflineMatchEntry(String offlineMatchEntry) //human_bot_points_clock
	{
		int id = globalId++;
		String[] entrySplit = offlineMatchEntry.split("_");
		String entryToStore = id+"_"+entrySplit[0]+"_"+entrySplit[1]+"_"+entrySplit[2]+"_"+entrySplit[3];
		matchList.addStringEntry(entryToStore);
		sendMessageToAll(Message.ongoingEntryMessage(entryToStore));
		
		return id;
	}
	
	public static void subscribeUser(String username) 
	{
		userList.addUser(username);  //þeir sem eru a userList fá skilaboð varðandi chat, waiting list og ongoing matches
		
		String[] recentChat = chatManager.getRecentChat();
		String[] waiting = waitingList.getAllStrings();
		String[] ongoing = matchList.getAllStrings();
		
		convertToMessageListAndSend(recentChat, "entry", username); 
		convertToMessageListAndSend(waiting, "wait", username);
		convertToMessageListAndSend(ongoing, "ongoing", username); 
	}
	
	public static void playerExitingApplication(String username)
	{
		playerExitingMatch(username);
		userList.removeUser(username);
		
		deleteWaitEntries(username);
	}
	
	public static void playerExitingMatch(String username)
	{
		
	}
	
	public static void createwaitListEntry(String entry) 
	{
		int freshId = globalId++;
		String fullEntry = freshId + "_"+entry;
		waitingList.addStringEntry(fullEntry);
		sendMessageToAll(Message.waitingEntryMessage(fullEntry));
	}
	
	public static void matchEnded(String eitherPlayer) //tilgangslaust?
	{
		String id = "5";
		deleteMatchEntries(eitherPlayer);
		sendMessageToAll(Message.deleteEntryMessage(id));
	}
	
	public static void receiveChatEntry(String username, String chat)
	{
		String formatted = chatManager.formatChatString(username, chat);
		chatManager.storeChatEntry(formatted);
		sendMessageToAllLast(Message.newChatMessage(formatted, "lobby"));
	}
	
	private static void convertToMessageListAndSend(String[] toConvert, String type,String username)
	{
		Message[] toSignUpper = new Message[toConvert.length];
		int pointer = 0;
		for(String entry: toConvert)
			if(type == "entry") toSignUpper[pointer++] = Message.newChatMessage(entry, "lobby");
			else if(type == "wait") toSignUpper[pointer++] = Message.waitingEntryMessage(entry);
			else if(type=="ongoing") toSignUpper[pointer++] = Message.ongoingEntryMessage(entry);
		
		UnreadMessageStorage.storeMessages(username, toSignUpper);   
	}
	
	public static synchronized int createMatchEntryIfPossible(String waitId, String joiningPlayer)
	{
		String entry = getWaitEntry(waitId);
		if(entry == null)
		{
			UnreadMessageStorage.storeMessage(joiningPlayer, Message.explainMessage("Game no longer available"));
			return -1;
		}
		else
		{
			waitingList.removeString(entry);
			return createMatchEntry(entry, joiningPlayer, globalId++);
		}
	}
	
	private static int createMatchEntry(String waitEntry, String joiningPlayer, int freshId)
	{
		String[] waitSplit = waitEntry.split("_");
		String playerOne = waitSplit[1];
		String matchEntry = freshId+"_"+playerOne+"_"+joiningPlayer+"_"+waitSplit[2]+"_"+waitSplit[3];
		
		sendMessageToAll(Message.deleteEntryMessage(getId(waitEntry)));
		sendMessageToAll(Message.ongoingEntryMessage(matchEntry));
		matchList.addStringEntry(matchEntry);
		return freshId;
	}
	
	public static String[] getPlayers(int gameId)
	{
		return matchList.getPlayers(gameId);
	}
	
	public static void deleteMatchEntry(int id)
	{
		String removed = matchList.removeStringById(""+id);
		sendMessageToAll(Message.deleteEntryMessage(""+id));
	}
	
	public static void deleteSingleWaitEntry(String id)
	{
		sendMessageToAll(Message.deleteEntryMessage(id));
		waitingList.removeStringById(id);
	}
	
	public static void deleteWaitEntries(String player) //þarf líka að delete-a entry sem varð að leik
	{
		String[] deletedEntries = waitingList.deleteAllEntriesContaining("_"+player+"_");
		Message[] deletionMsgs = new Message[deletedEntries.length];
		int pointer = 0;
		for(String entry: deletedEntries)
			deletionMsgs[pointer++] = Message.deleteEntryMessage(getId(entry));
		
		sendMessagesToAll(deletionMsgs);
	}
	
	private static String getId(String entry)
	{
		return entry.split("_")[0];
	}
	
	public static void deleteMatchEntries(String[] players)
	{
		matchList.deleteAllEntriesContaining(players[0]);
		matchList.deleteAllEntriesContaining(players[1]);
	}
	public static void deleteMatchEntries(String player)
	{
		matchList.deleteAllEntriesContaining(player);
	}
	
	private static void sendMessageToAllLast(Message msg)
	{
		for(String userN: userList.getAllUsers())
			UnreadMessageStorage.storeMessageLast(userN, msg);
	}
	
	private static void sendMessageToAll(Message msg)
	{
		for(String userN: userList.getAllUsers())
			UnreadMessageStorage.storeMessage(userN, msg);
	}
	private static void sendMessagesToAll(Message[] msgs)
	{
		for(String userN: userList.getAllUsers())
			UnreadMessageStorage.storeMessages(userN, msgs);
	}
	
	
	private static String getWaitEntry(String gameId)
	{
		for(String entry: waitingList.getAllStrings())
			if(parseId(entry).equals(gameId)) return entry;
		return null;
	}
	
	
	private static String parseId(String entry)
	{
		String[] split = entry.split("_");
		return split[0];
	}
	
}
