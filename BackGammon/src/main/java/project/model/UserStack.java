package project.model;

public class UserStack {
	
	private class UserNode
	{
		private class ObjectNode
		{
			public ObjectNode next;
			public Object item;
			public ObjectNode(Object item)
			{
				this.item = item;
				this.next = null;
			}	
		}
		
		private String username;
		private UserNode next;
		private ObjectNode stackTop;
		private ObjectNode delayedStackTop;
		private int stackSize, delayStackSize ,callsUntilDelayRelease;;
		
		public UserNode(String username)
		{
			this.username = username;
			this.next = null;
			
			this.stackTop = null;
			this.delayedStackTop = null;
			this.stackSize = 0;
			this.delayStackSize = 0;
			this.callsUntilDelayRelease = -100;
		}
		
		public void putItemLast(Object item)
		{
			stackSize++;
			if(this.stackTop == null)
				this.stackTop = new ObjectNode(item);
			else
			{
				ObjectNode curr = this.stackTop;
				while(curr.next != null)
					curr = curr.next;
				curr.next = new ObjectNode(item);
			}
		}
		
		public void pushItem(Object item)
		{
			stackSize++;
			if(this.stackTop == null)
				this.stackTop = new ObjectNode(item);
			else
			{
				ObjectNode oldTop = this.stackTop;
				this.stackTop = new ObjectNode(item);
				this.stackTop.next = oldTop;
			}
		}
		
		public void pushItemOnDelay(Object item)
		{
			this.delayStackSize++;
			if(this.delayedStackTop == null)
				this.delayedStackTop = new ObjectNode(item);
			else
			{
				ObjectNode oldTop = this.delayedStackTop;
				this.delayedStackTop = new ObjectNode(item);
				this.delayedStackTop.next = oldTop;
			}
		}
		
		public Object peekAtTop()
		{	
			if(stackTop == null) return null;
			else				 return stackTop.item;
		}
		
		public Object[] popEntireDelayStack()
		{
			Object[] items = new Object[this.delayStackSize];
			this.delayStackSize = 0;
			int pointer = 0;
			
			ObjectNode currNode = this.delayedStackTop;
			while(currNode != null)
			{
				items[pointer++] = currNode.item;
				currNode = currNode.next;
			}
			this.delayedStackTop = null;
			return items;
		}
		
		public Object[] popEntireStack()
		{
			this.callsUntilDelayRelease--;
			
			Object[] items = new Object[stackSize];
			stackSize = 0;
			int pointer = 0;
			
			ObjectNode currNode = this.stackTop;
			while(currNode != null)
			{
				items[pointer++] = currNode.item;
				currNode = currNode.next;
			}
			this.stackTop = null;
			return items;
		}
	}
	
	private UserNode head;
	private int stackSize;
	public UserStack()
	{
		head = null;
		stackSize = 0;
	}
	
	public UserStack(String startsIn)
	{
		head = null;
		stackSize = 0;
		addUser(startsIn);
	}
	
	public void addMatch(String username, MatchStateManager psm)
	{
		UserNode user = getUserNode(username);
		if(user != null) user.pushItem(psm);
	}
	
	public void removeMatch(String username)
	{
		UserNode user = getUserNode(username);
		user.popEntireStack();
	}
	
	public MatchStateManager getMatch(String username)
	{
		UserNode user = getUserNode(username);
		if(user != null) return (MatchStateManager) user.peekAtTop();
		else			return null;
	}
	public MatchStateManager getMatch(int id)
	{
		UserNode curr = head;
		while(curr != null)
		{
			MatchStateManager possibleMatch = (MatchStateManager) curr.peekAtTop();
			if(possibleMatch != null && id == possibleMatch.getId())
				return possibleMatch;
			else 
				curr = curr.next;
		}
		
		return null;
	}
	
	
	public synchronized void addUser(String username)
	{
		stackSize++;
	
		UserNode oldTop = head;
		head = new UserNode(username);
		head.next = oldTop;
	}
	
	public synchronized void removeUser(String username)
	{
		UserNode currNode = head;
		UserNode trailer = head;
		while(currNode != null)
		{
			if(currNode.username.equals(username)) 
			{
				if(currNode == head) head = head.next;
				else				 trailer.next = currNode.next;
				stackSize--;
				return;
			}
			
			trailer = currNode;
			currNode = currNode.next;
		}
	}
	
	private UserNode getUserNode(String username)
	{
		UserNode currNode = head;
		while(currNode != null)
		{
			if(currNode.username.equals(username))
				return currNode;
			else
				currNode = currNode.next;
		}
		return null;
	}
	
	public String[] getAllUsers()
	{
		String[] users = new String[stackSize];
		int pointer = 0;
		UserNode currNode = head;
		while(currNode != null)
		{
			users[pointer++] = currNode.username;
			currNode = currNode.next;
		}
		return users;
	}
	
	private Message[] reverseArray(Message[] msgs)
	{
		Message[] reverse = new Message[msgs.length];
		int pointer = msgs.length-1;
		int pointerTwo = 0;
		while(pointer >= 0)
			reverse[pointer--] = msgs[pointerTwo++];
		return reverse;
	}
	
	public void storeDelayedMessages(String username, Message[] msgs, int callCount)
	{
		UserNode user = getUserNode(username);
		if(user == null) return;
			
		user.callsUntilDelayRelease = callCount;	
		Message[] reversed = reverseArray(msgs);
		storeDelayedMessagesLIFO(username, reversed);
	}
	
	private void storeDelayedMessagesLIFO(String username, Message[] msgs)
	{
		UserNode user = getUserNode(username);
		if(user == null) return;
		for(int i = 0; i < msgs.length; i++)
			user.pushItemOnDelay(msgs[i]);
	}
	
	public void storeMessages(String username, Message[] msgs)
	{
		Message[] reversed = reverseArray(msgs);
		storeMessagesLIFO(username, reversed);
	}
	
	public void storeMessagesLIFO(String username, Message[] msgs)
	{
		UserNode user = getUserNode(username);
		if(user == null) return;
		for(int i = 0; i < msgs.length; i++)
			user.pushItem(msgs[i]);
	}
	
	public void storeMessageLast(String username, Message msg)
	{
		UserNode user = getUserNode(username);
		if(user != null) user.putItemLast(msg);
	}
	
	public void storeMessage(String username, Message msg)
	{
		UserNode user = getUserNode(username);
		if(user != null) user.pushItem(msg);
	}
	
	public Message[] getMessages(String username)
	{
		Object[] obs = getObjects(username);
		Message[] msgs = new Message[obs.length];
		int pointer = 0;
		for(Object obj: obs)
			msgs[pointer++] = (Message)obj;
		
		return msgs;
	}
	
	private Object[] getObjects(String username) 
	{
		UserNode user = getUserNode(username);
		if(user == null) return new Object[0];
		else if(user.callsUntilDelayRelease == 0)			
		{
			Object[] delayed = user.popEntireDelayStack();
			Object[] regular = user.popEntireStack();
			return combineArrays(delayed, regular);
		}
		else
			return user.popEntireStack();
	}
	private Object[] combineArrays(Object[] delay, Object[] reg)
	{
		Object[] combined = new Object[delay.length+reg.length];
		int pointer = 0;
		int combinedPointer = 0;
		while(pointer < delay.length)
			combined[combinedPointer++] = delay[pointer++];
		 
		pointer = 0;
		while(pointer < reg.length)
			combined[combinedPointer++] = reg[pointer++];
			
		return combined;
	}
	
}
