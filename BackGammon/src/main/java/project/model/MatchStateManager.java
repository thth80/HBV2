package project.model;

import org.springframework.stereotype.Service;

@Service
public class MatchStateManager {
	
	private GameStateManager gameState;
	private ChatManager chatManager;
	private int id, pointsToWin, playerOnePoints, playerTwoPoints;
	private boolean isOnline;
	private String[] players;
	
	private MatchStateManager()
	{
		
	}
	
	private MatchStateManager(String playerWhite, String playerBlack)
	{
		players = new String[]{playerWhite, playerBlack};
		chatManager = null;
		gameState = null;
	}
	
	public static MatchStateManager createNewOnlineGame(String playerName1, String playerName2, int points, int id)
	{
		MatchStateManager match = new MatchStateManager(playerName1, playerName2);
		match.chatManager = new ChatManager();
		match.gameState = GameStateManager.regularGame(playerName1, playerName2);
		match.pointsToWin = points;
		match.playerOnePoints = 0;
		match.playerTwoPoints = 0;
		match.id = id;
		match.isOnline = true;
		
		return match;
	}
	
	public static MatchStateManager createNewOfflineMatch(String humanName,String botName ,int points, int id)
	{	
		MatchStateManager match = new MatchStateManager(humanName, botName);
		match.chatManager = new ChatManager();
		
		match.gameState = GameStateManager.regularGame(humanName, botName);
		match.pointsToWin = points;
		match.playerOnePoints = 0;
		match.playerTwoPoints = 0;
		match.isOnline = false;
		match.id = id;
		
		return match;
	}
	
	public void receiveChatEntry(String username, String chat)
	{
		String formattedChat = chatManager.formatChatString(username, chat);
		chatManager.storeChatEntry(formattedChat);
		sendMessageToAllLast(Message.newChatMessage(formattedChat, "match"));
	}
	
	public void startGame()
	{
		gameState.gameWasStarted(this.isOnline);
		sendMessageToAll(gameState.prepareMatchPresentationMessage());
	}
	
	private boolean hasMatchEnded()
	{
		return playerOnePoints >= pointsToWin || playerTwoPoints >= pointsToWin;
	}
	
	private void sendMessageToAll(Message msg)
	{
		for(String user: gameState.getPlayersAndObservers())
			UnreadMessageStorage.storeMessage(user, msg);
	}
	private void sendMessageToAllLast(Message msg)
	{
		for(String user: gameState.getPlayersAndObservers())
			UnreadMessageStorage.storeMessageLast(user, msg);
	}
	private void sendDelayedMessageToAll(Message msg, int callCount)
	{
		for(String user: gameState.getPlayersAndObservers())
			UnreadMessageStorage.storeDelayedMessage(user, msg, callCount);
	}
	
	private void updateMatchScore(int winner, int totalMult)
	{
		if(winner == 0) this.playerOnePoints += totalMult;
		else			this.playerTwoPoints += totalMult;
	}
	
	public void handleDiceThrow()
	{
		gameState.throwDiceClicked();
	}
	
	public void handleDoublingAccept()
	{
		gameState.doublingAccepted();
	}
	
	public boolean handleDoublingReject()
	{
		String rejecter = gameState.getOtherUserName();
		handleAbortedEndOfGame(rejecter);
		if(this.hasMatchEnded())
			decideOnAbortedMatchOutcome(rejecter);
		
		return this.hasMatchEnded();
	}
	
	public int handleUserLeavingMatch(String quitter)  //þarf að senda game end messages ásamt þeim match end messages sem eru send nú þegar
	{
		boolean quitterIsAPlayer = gameState.isThisAPlayer(quitter);
		if(quitterIsAPlayer && gameState.isStillPlaying())
		{	
			handleAbortedEndOfGame(quitter);
			decideOnAbortedMatchOutcome(quitter);
		}
		else
			gameState.removeObserver(quitter);	
		
		return this.id;
	}
	
	private void handleAbortedEndOfGame(String quitter) 
	{
		String winner = gameState.getOtherUserName(quitter);
		int[] results = gameState.getEndGameStats(winner);
		
		updateMatchScore(results[0], results[2]);
		try {
			processPostGameDatabase(results, winner, quitter);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void decideOnAbortedMatchOutcome(String quitter)
	{
		String winnerName = gameState.getOtherUserName(quitter);
		int winPoints = (winnerName.equals(players[0]))? this.playerOnePoints: this.playerTwoPoints;	
		int lossPoints = (winnerName.equals(players[0]))? this.playerTwoPoints: this.playerOnePoints;	
	
		this.processPostMatchDatabase(winnerName, quitter, winPoints, lossPoints);
		
		sendDelayedMessageToAll(Message.announcementMessage("match_over", winnerName+"_"+quitter+"_"+winPoints+"_"+lossPoints), 3);
		gameState.removePlayerAsSubscriber(quitter);
	}
	
	public boolean handleSquareClick(int squarePos) 
	{
		gameState.squareClicked(squarePos);
		if(gameState.hasGameEnded())
		{
			gameState.cleanUpMovesMade();
			gameState.deliverEndMovesToOthers();
			return processRegularGameEnd();
		}
		else 
			return false;
	}
	
	public void handleClockTurnFinish() 
	{
		
	}
	
	public boolean handleTurnFinish()		
	{
		if(isOnline) gameState.endTurnClickedOnline();
		else		 gameState.endTurnClickedOffline(); 
		
		if(gameState.hasGameEnded())
			return processRegularGameEnd();
		else 
			return false;
	}
	
	private boolean processRegularGameEnd()  
	{										 		 
		String winner = gameState.getCurrentUserName();
		handleNaturalEndOfGame(gameState.getEndGameStats(winner), winner);
		deliverEndGameMessages(gameState.getEndGameStats(gameState.getCurrentUserName()), gameState.getCurrentUserName(), "reg");	
		
		if(hasMatchEnded())
			handleRegularEndOfMatch(gameState.getCurrentUserName(), gameState.getOtherUserName()); 	//put at end of stack?
		else
		{
			gameState = GameStateManager.cloneGame(gameState); 
			gameState.gameWasStarted(isOnline);
		}
		
		return hasMatchEnded();	
	}
	
	private void handleRegularEndOfMatch(String winner, String loser) //mögulega delay-a Match ending msg
	{
		processPostMatchDatabase(winner, loser, Math.max(playerOnePoints, playerTwoPoints), Math.min(playerOnePoints, playerTwoPoints));
		int winPoints = Math.max(playerOnePoints, playerTwoPoints);
		int lossPoints = Math.min(playerOnePoints, playerTwoPoints);
		
		sendDelayedMessageToAll(Message.announcementMessage("match_over", winner+"_"+loser+"_"+winPoints+"_"+lossPoints ), 4); 
	}
																	   //   0        1     2        3           4           5        6
	private void handleNaturalEndOfGame(int[] results, String winner)  //turnowner, mult, cube, losersteps, loserpawns, winsteps, winpawns
	{
		String loser = gameState.getOtherUserName();
		updateMatchScore(results[0], results[1] * results[2]);
		try {
			processPostGameDatabase(results, winner, loser);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void processPostMatchDatabase(String winner, String loser, int winPoints, int lossPoints)
	{
		while(!DataCenter.storeMatchResults(winner, loser, this.pointsToWin ,winPoints, lossPoints,  1  ));
		try {
			while(!DataCenter.checkForNewTrophies(winner));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			while(!DataCenter.checkForNewTrophies(loser));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(!DataCenter.generateAllTrophyMessages(winner));
		while(!DataCenter.generateAllTrophyMessages(loser));
	}
	
	private void processPostGameDatabase(int[] results, String winner, String loser) throws Exception
	{
		while(!DataCenter.storeSingleGameResults(winner, loser, results[1]*results[2], results[2], results[1], results[6], results[4],
										         results[5], results[3],  1  )) ;
		String newVersusData = null;
		while(newVersusData == null)
			newVersusData = DataCenter.updateGameVersus(winner, loser, results[1]*results[2], 15-results[6], 15 - results[4]);
		
		while(!DataCenter.sendUpdatedVersusAndOverall(winner, loser, newVersusData)) ; 
		
		/* Hér að neðan er aðeins til að prófa kerfið, á að gera þetta í lok Match */
		while(!DataCenter.checkForNewTrophies(winner));
		while(!DataCenter.checkForNewTrophies(loser));
	}
    
	//ATHATHATHATH:
	//Hér þarf að afrita allt borðið, framkvæma reverse moves og senda niður í aðferð
    public void addGameObserver(String observer)
    {
    	gameState.addObserver(observer);	
    	
    	String[] chatForObserver = chatManager.getRecentChat();
    	Message[] chatMsgs = new Message[chatForObserver.length];
    	for(int i = 0; i < chatForObserver.length; i++)
    		chatMsgs[i] = Message.newChatMessage(chatForObserver[i], "match");
    	
    	UnreadMessageStorage.storeMessages(observer, chatMsgs);
    	gameState.deliverEntireGameState(observer);
    }
    
    public int getId()
    {
    	return id;
    }
    
    //**************DElivery aðferðir hér að neðan
    
    private void deliverEndGameMessages(int[] vals, String winner, String endType)  
	{
    	Message[] forAll = {Message.announcementMessage("game_over", winner+"_"+endType+"_"+vals[1]+"_"+vals[2])};
		for(String user: getAllSubscribers() )
			UnreadMessageStorage.storeMessages(user, forAll);
	}
    
    private String[] getAllSubscribers()
    {
    	return gameState.getPlayersAndObservers();
    }
}
