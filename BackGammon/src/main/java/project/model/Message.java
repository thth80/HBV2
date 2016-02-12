package project.model;

public class Message {
	
	private String action;
	private int count;
	
	private String desc, str;
	private boolean killed, white;
	
	private int valOne, valTwo, valThree, valFour;
	
	public Message() 
	{

	}
	private Message(String action)
	{
		this.action = action;
	}
	
	public static Message animateMessage(int from, int to, boolean killed) 
	{
		Message msg = new Message("animate");
		msg.valOne = from;
		msg.valTwo = to;
		msg.killed = killed;
		return msg;
	}
	
	public static Message announcementMessage(String type, String data)
	{
		Message msg = new Message("announcement");
		msg.desc = type;
		msg.str = data;
		return msg;
	}
	
	public static Message buildBoardMessage(String boardItem, int numValue, int itemTeam, int squarePos)
	{
		Message msg = new Message("build");
		msg.desc = boardItem;
		msg.valOne = numValue;
		msg.valTwo = itemTeam;
		msg.valThree = squarePos;
		return msg;
	}
	public static Message deleteEntryMessage(String id)
	{
		Message msg = new Message("delete_entry");
		msg.desc = id;
		return msg;
	}
	public static Message diceValueMessage(int first, int second, int team, String throwingName, int moveCount)
	{
		Message msg = new Message("dice_throw");
		msg.valOne = first;
		msg.valTwo = second;
		msg.valThree = moveCount;
		msg.white = (team == 0);
		msg.desc = throwingName;
		return msg;
	}
	public static Message explainMessage(String reason)
	{
		Message msg = new Message("no_response");
		msg.desc = reason;
		msg.str = "error";
		return msg;
	}
	
	public static Message gameAccessMessage(String starterName, boolean hasPlayingAccess)
	{
		Message msg = new Message("game_access");
		msg.desc = starterName;
		msg.killed = hasPlayingAccess;
		return msg;
	}
	
	public static Message loadingFinishedMessage()
	{
		Message msg = new Message("loading_finished");
		return msg;
	}
	
	public static Message mayEndTurnMessage() 
	{										  
		Message msg = new Message("may_end_turn");
		return msg;
	}
	
	public static Message moveMessage(int from, int to, boolean killed)
	{
		Message msg = new Message("move");
		msg.valOne = from;
		msg.valTwo = to;
		msg.killed = killed;
		return msg;
	}
	
	
	public static Message newChatMessage(String entry, String type)
	{
		Message msg = new Message("chat_entry");
		msg.str = entry;
		msg.desc = type;
		return msg;
	}
	
	public static Message newPivotMessage(int pos1, int pos2)
	{
		Message msg = new Message("new_pivot");
		msg.valOne = pos1;
		msg.valTwo = pos2;
		return msg;
	}
	
	public static Message trophyDataMessage(String data)
	{
		Message msg = new Message("trophy_entry");
		msg.str = data;
		return msg;
	}
	
	public static Message ongoingEntryMessage(String entry)
	{
		Message msg = new Message("ongoing_entry");
		msg.str = entry;
		return msg;
	}
	
	public static Message playerAcceptedMessage(String player)
	{
		Message msg = new Message("offer_accepted");
		msg.desc = player;
		return msg;
	}
	
	public static Message playerDoubledMessage(String doublerName, int newStakes)
	{
		Message msg = new Message("player_doubled");
		msg.valOne = newStakes;
		msg.desc = doublerName;
		return msg;
	}

	public static Message refreshStatusMessage(String command) //bæta við frequency?
	{
		Message msg = new Message("refresh");
		msg.desc = command;
		return msg;
	}	
	
	public static Message removeEntryMessage(int entryId)
	{
		Message msg = new Message("delete_entry");
		msg.valOne = entryId;
		return msg;
	}
	
	public static Message turnFinishedMessage(String finisher)
	{
		Message msg = new Message("turn_finished");
		msg.desc = finisher;
		return msg;
	}
	
	public static Message versusEntryMessage(String data, String type)
	{
		Message msg = new Message("versus_entry");
		msg.str = data;
		msg.desc = type;
		return msg;
	}
	
	public static Message userImageMessage(String imageUrl, String title)
	{
		Message msg = new Message("user_image");
		msg.str = imageUrl;
		msg.desc = title;
		return msg;
	}
	
	public static Message waitingEntryMessage(String entry)
	{
		Message msg = new Message("wait_entry");
		msg.str = entry;
		return msg;
	}
	
	public static Message welcomeMessage(String username)  //vantar fleiri gögn til að senda um user
	{
		Message msg = new Message("legal_signup");
		msg.desc = username;
		return msg;
	}
	
	public static Message[] mergeMessages(Message[] m1, Message m2)
	{
		Message[] merge = new Message[m1.length+1];
		merge[m1.length] = m2;
		for(int i = 0; i < m1.length; i++)
			merge[i] = m1[i];
		return merge;
	}
	
	public static Message[] mergeMessages(Message[] m1, Message[] m2)  
	{
		Message[] merge = new Message[m1.length+m2.length];
		int pointer = 0;
		for(int i = 0; i < m1.length; i++)
			merge[pointer++] = m1[i];
		for(int i = 0; i < m2.length; i++)
			merge[pointer++] = m2[i];
		return merge;
	}
	
	
	public String getAction() 
	{
		return action;
	}
	public int getCount()
	{
		return count;
	}
	public String getDesc() 
	{
		return desc;
	}
	public boolean getKilled ()
	{
		return killed;
	}
	public String getStr() 
	{
		return str;
	}
	public int getValFour() 
	{
		return valFour;
	}
	public int getValOne() 
	{
		return valOne;
	}
	public int getValThree() 
	{
		return valThree;
	}
	public int getValTwo() 
	{
		return valTwo;
	}
	public boolean getWhite() 
	{
		return white;
	}
	
	public void setAction(String s) 
	{
		action = s;
	}
	public void setCount(int i)
	{
		count = i;
	}
	public void setDesc(String s)
	{
		desc = s;
	}
	public void setKilled(boolean b)
	{
		killed = b;
	}
	public void setStr(String s)
	{
		str = s;
	}
	public void setValFour(int i)
	{
		valFour = i;
	}
	public void setValOne(int i)
	{
		valOne = i;
	}
	public void setValThree(int i)
	{
		valThree = i;
	}
	public void setValTwo(int i)
	{
		valTwo = i;
	}
	public void setWhite(boolean b)
	{
		white = b;
	}
}