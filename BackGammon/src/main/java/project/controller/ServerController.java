package project.controller;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import project.model.DataCenter;
import project.model.Message;
import project.model.Move;
import project.model.OnlineLobbyManager;
import project.model.MatchStateManager;
import project.model.UnreadMessageStorage;
import project.model.UserStack;

@RestController
public class ServerController {
	
	private static UserStack users = new UserStack();
	
	private ServerController()
	{
		
	} 
	
	@RequestMapping("/accept")   
    public String accept(HttpSession session)
    {
    	String username = (String)session.getAttribute("username");
    	(getMatch(username)).handleDoublingAccept();
    	return "derp";
    }
	
	@RequestMapping("/addWaitEntry")   //username_points_clock
    public Message[] addWaitEntry(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="entry", required=true) String waitEntry
    		)  
    {
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	OnlineLobbyManager.createwaitListEntry(waitEntry);  
		String[] unpacked = waitEntry.split("_");
    	
    	return UnreadMessageStorage.retrieveUserMessages(unpacked[0]);
    }
	
	
	@RequestMapping("/refresh") 
    public Message[] checkForMessages(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="name", required=true) String username)
    {
    	response.addHeader("Access-Control-Allow-Origin", "*");
        return UnreadMessageStorage.retrieveUserMessages(username);
    }
    
    @RequestMapping("/endTurn")   
    public Message[] doneMoving(HttpSession session ,HttpServletResponse response,
    		@RequestParam(value="name", required=true) String username)
    {
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	boolean isGameOver = getMatch(username).handleTurnFinish();
    	if(isGameOver)
    		; //höfum samband við OLM ef það skal breyta status á leik
    		
    	return UnreadMessageStorage.retrieveUserMessages(username);
    }
    
    @RequestMapping("/cube")   
    public String flipCube(HttpSession session)
    {
    	String username = (String)session.getAttribute("username");
    	(getMatch(username)).handleDiceThrow(); //handleCubeFlip
    	return "derp";
    }
    
    private MatchStateManager getMatch(int id)
	{
		return users.getMatch(id);
	}
    
    private MatchStateManager getMatch(String username)
	{
		return users.getMatch(username);
	}
    
    @RequestMapping("/getUserData")   
    public Message[] getUserData(HttpSession session,
    		@RequestParam(value="user", required=true, defaultValue="") String otherUser)
    {
    	String username = (String)session.getAttribute("username");
    	try {
			DataCenter.deliverUserImage(otherUser);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return UnreadMessageStorage.retrieveUserMessages(username);
    }
    
    @RequestMapping("/joinOfflineMatch")  
    public Message[] joinOfflineMatch(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="entry", required=true) String offlineMatchEntry  )
    {
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	String[] entryParts = offlineMatchEntry.split("_");
    	String username = entryParts[0];
    	String selectedBot = entryParts[1];
    	int points = Integer.parseInt(entryParts[2]);
    	
    	int generatedMatchId = OnlineLobbyManager.createOfflineMatchEntry(offlineMatchEntry);
    	OnlineLobbyManager.deleteWaitEntries(username);
    	MatchStateManager match = MatchStateManager.createNewOfflineMatch(username,selectedBot ,points, generatedMatchId);
    	users.addMatch(username, match);
    	match.startGame();   
    	
    	return UnreadMessageStorage.retrieveUserMessages(username);
    }
    
    @RequestMapping("/joinOnlineMatch")  
    public Message[] joinOnlineMatch(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="id", required=true) String waitId,
    		@RequestParam(value="name", required=true) String username)
    {
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	
    	int generatedGameId = OnlineLobbyManager.createMatchEntryIfPossible(waitId, username);
    	if(generatedGameId >= 0)
    	{
    		String[] players = OnlineLobbyManager.getPlayers(generatedGameId);
    		MatchStateManager match = MatchStateManager.createNewOnlineGame(players[0], players[1], 5, generatedGameId);
    		
    		for(String player: players)
    		{
    			OnlineLobbyManager.deleteWaitEntries(player);
    			users.addMatch(player, match);
    			//UnreadMessageStorage.storeMessage(player, Message.refreshStatusMessage("stop"));
    		}
    		match.startGame();
    	}
    	
    	return UnreadMessageStorage.retrieveUserMessages(username);
    }
    
    
    @RequestMapping("/leaveMatch")   		
    public Message[] leaveMatch(HttpSession session , HttpServletResponse response,
    		@RequestParam(value="name", required=true) String username)
    {
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	
    	if(getMatch(username) != null)		
    		leaveTheMatch(username);
    	
    	return UnreadMessageStorage.retrieveUserMessages(username);
    }
    
    @RequestMapping("/leaveProgram")   
    public Message[] leaveProgram(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="name", required=true) String username)
    {
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	
    	if(getMatch(username) != null)
    		leaveTheMatch(username);
    		
    	users.removeUser(username);  		
    	OnlineLobbyManager.playerExitingApplication(username);
    	
    	return UnreadMessageStorage.retrieveUserMessages(username);
    }
    
    //kannski uppfæra entry á client/OLM, sbr PLAYING eða FINISHED?
    //Transition á CLIENT gerist áður en request á server fer fram
    private void leaveTheMatch(String username)		
    {	
    	int matchId = getMatch(username).handleUserLeavingMatch(username);
		users.removeMatch(username);
		
		if(users.getMatch(matchId) == null)
			OnlineLobbyManager.deleteMatchEntry(matchId);
    }
    
    @RequestMapping("/login")    
    public Message[] logIn(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="name", required=true) String username,
    		@RequestParam(value="pw", required=true) String password) throws Exception
	{
		response.addHeader("Access-Control-Allow-Origin", "*");
    	String allowedUsername = null;
		while(allowedUsername == null)
			allowedUsername = DataCenter.attemptLogIn(username, password);
			
    	if(allowedUsername.length() > 0){
    		getApplicationReadyForLoggedUser(username, session);
    		
    		while(!DataCenter.generateVersusStatsMessages(username));
    		while(!DataCenter.generateAllTrophyMessages(username));
    		
    		UnreadMessageStorage.storeMessage(username, Message.welcomeMessage(username));
    		return UnreadMessageStorage.retrieveUserMessages(username);
    	}
    	else
    		return Message.mergeMessages(new Message[0], Message.explainMessage("There was no matching username found"));	
    }
    
    private void getApplicationReadyForLoggedUser(String username, HttpSession session)
    {
    	UnreadMessageStorage.addUser(username);
    	session.setAttribute("username", username);
    	OnlineLobbyManager.subscribeUser(username);
		UnreadMessageStorage.storeMessage(username, Message.refreshStatusMessage("start"));
		users.addUser(username);
    }
    
    @RequestMapping("/signup")  
    public Message[] signUp(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="name", required=true) String username,
    		@RequestParam(value="pw", required=true) String password) throws Exception
    {
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	String allowedUsername = null;
    	while(allowedUsername == null)
			allowedUsername = DataCenter.attemptSignUp(username, password);
			
    	if(allowedUsername.length() > 0){
    		getApplicationReadyForLoggedUser(username, session);
    		
    		while(!DataCenter.setUpNewUserTrophyEntries(username));
    		while(!DataCenter.setUpNewUserVersusEntries(username));  
    		
    		while(!DataCenter.generateVersusStatsMessages(username));
    		while(!DataCenter.generateAllTrophyMessages(username));
    		
    		UnreadMessageStorage.storeMessage(username, Message.welcomeMessage(username));
    		return UnreadMessageStorage.retrieveUserMessages(username);
    	}
    	else
    		return Message.mergeMessages(new Message[0], Message.explainMessage("Username is already in use"));
    }
    
    //ATHATHATH : Ekki má senda OBSERVER ástand leiksins eins og það er eftir 1+ action
    
    @RequestMapping("/observeMatch")    
    public Message[] observeMatch(HttpSession session, HttpServletResponse response ,
    		@RequestParam(value="name", required=true) String username,
    		@RequestParam(value="id", required=true) int id)
    {
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	
    	MatchStateManager matchToObserve = getMatch(id);
    	users.addMatch(username, matchToObserve);
    	if(matchToObserve != null)
    		matchToObserve.addGameObserver(username);    //hérna gerist slatti, td sending á heilu borði
    	
    	return UnreadMessageStorage.retrieveUserMessages(username);
    }
    
    @RequestMapping("/reject")   
    public String[] reject(HttpSession session)
    {
    	String username = (String)session.getAttribute("username");
    	String[] s = {"derp"};
    	return s;
    }
    
    @RequestMapping("/removeWaitEntry")   
    public Message[] removeWaitEntry(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="name", required=true) String username,
    		@RequestParam(value="id", required=true) String id)  	
    {
    	response.addHeader("Access-Control-Allow-Origin", "*");

    	OnlineLobbyManager.deleteSingleWaitEntry(id);
    	
    	return UnreadMessageStorage.retrieveUserMessages(username);
    }
    
    @RequestMapping("/saveOffline")   //býst við streng sem inniheldur gildin, DataCenter kann að lesa  
    public String saveOffline(HttpSession session, 
    		@RequestParam(value="settings", required=true, defaultValue="") String settings)
    {
    	String username = (String)session.getAttribute("username");
    	try {
			DataCenter.storeOfflineDefaults(username, settings);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return "derp";
    }
    
    @RequestMapping("/saveOnline")   
    public Message saveOnline(HttpSession session, HttpServletResponse response)
    {
    	//String username = (String)session.getAttribute("username");
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	return Message.playerDoubledMessage("derp",4);
    }

    @RequestMapping("/square")   //útfæra það í handleSquareClick að ljúka leik strax og peðið lendir, kemur í veg fyrir end of game bug
    public Message[] square(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="pos", required=true) int pos,
    		@RequestParam(value="name", required=true) String username)
    {
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	getMatch(username).handleSquareClick(pos);
    	return UnreadMessageStorage.retrieveUserMessages(username);
    }
    
    @RequestMapping("/submitChat")   
    public Message[] submitChat(HttpSession session, HttpServletResponse response, 
    		@RequestParam(value="name", required=true) String username,
    		@RequestParam(value="chat", required=true) String chat)
    {
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	if(getMatch(username) == null)
    		OnlineLobbyManager.receiveChatEntry(username, chat);
    	else
    		getMatch(username).receiveChatEntry(username, chat);
    	
    	return UnreadMessageStorage.retrieveUserMessages(username);
    }
    
    @RequestMapping("/diceThrow")   
    public Message[] throwDice(HttpSession session, HttpServletResponse response,
    		@RequestParam(value="name", required=true) String username)
    {
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	(getMatch(username)).handleDiceThrow();
    	return UnreadMessageStorage.retrieveUserMessages(username);
    }
   
}
