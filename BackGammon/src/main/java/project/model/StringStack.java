package project.model;

public class StringStack {
	
	public class StringNode{
		private StringNode next;
		private String item;
		public StringNode(String entry)
		{
			this.item = entry;
			this.next = null;
		}
	}
	
	private StringNode head;
	private int stackSize;

	public StringStack()
	{
		head = null;
		stackSize = 0;
	}
	
	public void addStringEntry(String entry)
	{
		StringNode oldTop = head;
		head = new StringNode(entry);
		head.next = oldTop;
		stackSize++;
	}
	
	public void addStringEntryLast(String entry)
	{
		if(stackSize == 0)
		{
			head = new StringNode(entry);
			head.next = null;
		}
		else
		{
			StringNode curr = head;
			while(curr.next != null)
				curr = curr.next;
			curr.next = new StringNode(entry);
		}
		stackSize++;
	}
	
	public void removeString(String entry)
	{
		StringNode current = head;
		StringNode trailer = current;
		while(current != null)
		{
			if(current.item.equals(entry)) 
			{
				if(trailer != current) trailer.next = current.next;
				else				   head = current.next;
				stackSize--;
				return;
			}
			
			trailer = current;
			current = trailer.next;
		}
	}
	
	
	public String removeStringById(String id)
	{
		int idLength = id.length();
		StringNode current = head;
		StringNode trailer = current;
		while(current != null)
		{
			String entry = current.item;
			if(entry.substring(0,idLength).equals(id)) 
			{
				if(trailer != current) trailer.next = current.next;
				else				   head = current.next;
				stackSize--;
				return entry;
			}
				
			trailer = current;
			current = trailer.next;
		}
		return null;
	}
	
	public String[] getPlayers(int gameId)
	{
		String[] players = new String[2];
		StringNode curr = head;
		
		while(curr != null)
		{
			int parsedId = Integer.parseInt(curr.item.split("_")[0]);
			if(parsedId == gameId)
			{
				players[0] = curr.item.split("_")[1];
				players[1] = curr.item.split("_")[2];
				return players;
			}
			else
				curr = curr.next;
		}
		
		return null;
	}

	
	public String[] getAllStrings()
	{
		String[] allChat = new String[stackSize];
		int pointer = 0;
		StringNode current = head;
		
		while(current != null)
		{
			allChat[pointer++] = current.item;
			current = current.next;
		}
		return allChat;
	}
	
	public String[] deleteAllEntriesContaining(String username)  
	{
		String[] entries = new String[100];
		int pointer = 0;
		StringNode curr = head;
		StringNode trailer = curr;
		
		while(curr != null)
		{
			if(curr.item.contains(username)) 
			{
				stackSize--;
				entries[pointer++] = curr.item;
				
				if(curr == trailer)
					curr = trailer = head = curr.next;
				else
					trailer.next = curr = curr.next;	
			}
			else
			{
				trailer = curr;
				curr = curr.next;
			}
		}
		
		String[] removedEntries = new String[pointer];
		for(int i = 0; i < pointer; i++)
			removedEntries[i] = entries[i];
		return removedEntries;
	}
	
	public int getSize()
	{
		return stackSize;
	}
	
	public void cutStack(int cutPoint)
	{
		StringNode curr = head;
		int counter = 0;
		while(curr != null)
		{
			counter++;
			if(counter == cutPoint)
			{
				curr.next = null;
				stackSize = counter;
				return;
			}
			else
				curr = curr.next;
		}
	}
}
