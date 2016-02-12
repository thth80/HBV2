package project.model;

public class GameStateManager {
	
	public static final int TEAM_WH = 0, TEAM_BL = 1, TEAM_NONE = 2, NONE_CHOSEN = -1;
	private Board board;
	private DicePair whiteDice, blackDice;
	private Move[] movesMade; 
	private String[] players;
	private int pivotSquare, turnOwner, multiplier;
	private UserStack observers;

	private GameStateManager(String p1, String p2) //gæti verið týpa að láta halda á hvaða lið hefur aðgang að tening
	{
		players = new String[]{p1, p2};
		board = Board.createNormalBoard();
		
		whiteDice = new DicePair();
		blackDice = new DicePair();
		movesMade = null;
		pivotSquare = NONE_CHOSEN;
		turnOwner = TEAM_NONE;
		multiplier = 1;
		observers = new UserStack();
	}
	
	public void removePlayerAsSubscriber(String playerName)
	{
		if(playerName.equals(players[0])) players[0] = "";
		else							   players[1] = "";
		turnOwner = TEAM_NONE;
	}
	
	public static GameStateManager cloneGame(GameStateManager game)  
	{
		GameStateManager clone = new GameStateManager(game.players[0], game.players[1]);
		clone.observers = game.observers;
		return clone;
	}
	
	public boolean isStillPlaying()
	{
		return turnOwner != TEAM_NONE;
	}

	public static GameStateManager regularGame(String u1, String u2)
	{
		return new GameStateManager(u1, u2);
	}
	
	public void gameWasStarted(boolean isOnline)
	{
		while(true)
		{
			whiteDice.throwSingle();
			blackDice.throwSingle();
			if(whiteDice.getStartingThrow() > blackDice.getStartingThrow())
			{ 
				if(isOnline)prepareOnlineGameStart(TEAM_WH);
				else		prepareOfflineGameStart(TEAM_WH);
				return; 
			}
			else if(whiteDice.getStartingThrow() < blackDice.getStartingThrow())
			{	
				if(isOnline) prepareOnlineGameStart(TEAM_BL);
				else		 prepareOfflineGameStart(TEAM_BL);
				return;
			}
		}
	}
	
	private void prepareOnlineGameStart(int team) 
	{
		turnOwner = team;
		if(team == TEAM_WH) whiteDice = new DicePair(blackDice.getStartingThrow(), whiteDice.getStartingThrow());
		else				blackDice = new DicePair(blackDice.getStartingThrow(), whiteDice.getStartingThrow());
		
		int moveCount = (CalculationCenter.randomLegalMoves(board, getCurrentDice(), turnOwner)).length;
		movesMade = new Move[moveCount];
		
		deliverHumanStartingMessage(getCurrentUserName(), turnOwner, getCurrentDice(), moveCount);
	}

	private void prepareOfflineGameStart(int team) 
	{
		turnOwner = team;
		if(team == TEAM_WH) whiteDice = new DicePair(blackDice.getStartingThrow(), whiteDice.getStartingThrow());
		else				blackDice = new DicePair(blackDice.getStartingThrow(), whiteDice.getStartingThrow());
		
		if(isTurnHuman()) 
		{					
			int moveCount = (CalculationCenter.randomLegalMoves(board, getCurrentDice(), turnOwner)).length;
			movesMade = new Move[moveCount];
			deliverHumanStartingMessage(getCurrentUserName(), turnOwner, getCurrentDice(), moveCount);
		}
		else
		{
			String starterName = getCurrentUserName();
			int startingTeam = turnOwner;
			DicePair startingDice = getCurrentDice();			
			Move[] movs = getBotMoves();
			executeBotMoves(movs);
			beginNewRound();
			deliverBotStartingMessage(starterName, startingTeam, startingDice, movs);
		}
	}

	public void squareClicked(int squarePos) 
	{
		if(irrelevantSquare(squarePos)) 
			deliverExplainMessage(squarePos);
		else if(squarePos == pivotSquare)
			clearPivot();
		else if(pivotSwitchCommand(squarePos))
			deliverNewPivotMessage(setNewPivot(squarePos));
		else 
			performMovement(squarePos);
	}
	
	private void clearPivot()
	{
		UnreadMessageStorage.storeMessage(getCurrentUserName(), Message.newPivotMessage(-1, -1));
		pivotSquare = NONE_CHOSEN;
		board.unHighlightAll();
	}
	
	public void setObservers(UserStack obs)
	{
		this.observers = obs;
	}
	
	public void addObserver(String username)
	{
		this.observers.addUser(username);
	}
	public void removeObserver(String username)
	{
		this.observers.removeUser(username);
	}
	
	public void throwDiceClicked() 
	{
		getCurrentDice().rollDice();
		int moveCount = (CalculationCenter.randomLegalMoves(board, getCurrentDice(), turnOwner)).length;
		movesMade = new Move[moveCount];
		deliverPostDiceThrowMessage(moveCount, getCurrentUserName());				 
	}
	
	public void endTurnClickedOffline()
	{		
		getCurrentDice().markAllAsUnused();
		deliverHumanMovesToObservers(this.movesMade);
		
		if(hasGameEnded()) return; 
		else			   beginNewRound(); 

		Move[] botMoves = getBotMoves();
		DicePair botDice = getCurrentDice();
		executeBotMoves(botMoves);

		if(!hasGameEnded()) beginNewRound();
		deliverBotInfoMessage(botMoves, botDice);  
	}
	
	public void endTurnClickedOnline()
	{	
		getCurrentDice().markAllAsUnused();
		String humanActor = getCurrentUserName();
		Move[] humanMoves = this.movesMade;
		
		if(!hasGameEnded()) beginNewRound();
		
		deliverPostTurnMoveMessages(humanMoves, humanActor);	
	}
	
	public String getOtherUserName(String username)
	{
		return (username.equals(players[0]))? players[1]: players[0] ;
	}
	private int getTeamNumberFromName(String username)
	{
		return (username.equals(players[0]))? TEAM_WH : TEAM_BL ;
	}
		
	public int[] getEndGameStats(String winner)  
	{
		int losingTeam = getTeamNumberFromName(this.getOtherUserName(winner));
		int winningTeamNumber = getTeamNumberFromName(winner);
		
		int cubeMultiplier = this.multiplier;
		int regularMultiplier = board.getMultiplier(losingTeam);
		int lossSteps = board.countRemainingSteps(losingTeam);
		int winSteps = board.countRemainingSteps(winningTeamNumber);
		int lossPawns = board.countRemainingPawns(losingTeam);
		int winPawns = board.countRemainingPawns(winningTeamNumber);

		int[] stats = {winningTeamNumber, regularMultiplier, cubeMultiplier, lossSteps, lossPawns,winSteps,winPawns };
		return stats;
	}

	public void doublingDiceClicked()
	{
		deliverDoublingOfferMessage();
	}

	public void doublingAccepted()
	{
		this.multiplier *= 2;
		getCurrentDice().rollDice();
		int moveCount = (CalculationCenter.randomLegalMoves(board, getCurrentDice(), turnOwner)).length;
		movesMade = new Move[moveCount];
		deliverPostAcceptMessage(moveCount);
	}
	
	public boolean isThisAPlayer(String username)
	{
		return username.equals(players[0]) || username.equals(players[1]);
	}

	boolean teamHasTurn(String username)
	{
		return username.equals(getCurrentUserName());
	}

	private Move[] getBotMoves()
	{
		getCurrentDice().rollDice();
		String botName = getCurrentUserName();
		if(botName.equals("Hard Bot"))     return CalculationCenter.hardBotMoves(board, getCurrentDice(), turnOwner);
		else if(botName.equals("Medium Bot")) return CalculationCenter.mediumBotMoves(board, getCurrentDice(), turnOwner);
		else                          return CalculationCenter.easyBotMoves(board, getCurrentDice(), turnOwner);
	}

	private void executeBotMoves(Move[] moves)
	{
		for(int i = 0; i < moves.length; i++)
			board.forwardMovement(moves[i]);
	}

	private void beginNewRound()
	{
		switchTurn();
		this.movesMade = null;
		pivotSquare = NONE_CHOSEN;
		unHighlightAll();
	}
	
	private int movesLeft() 
	{
		if(movesMade == null) return 1;

		int movesLeft = 0;
		for(int i = 0; i < movesMade.length; i++)
			if(movesMade[i] == null) movesLeft++;
		
		return movesLeft;
	}
	
	public void cleanUpMovesMade()
	{
		int counter = 0;
		for(Move m: this.movesMade)
			if(m != null) counter++;
		Move[] cleaned = new Move[counter];
		
		int pointer = 0;
		for(int i = 0; i < this.movesMade.length; i++)
			if(this.movesMade[i] != null)
				cleaned[pointer++] = this.movesMade[i];
		
		this.movesMade = cleaned;
	}

	private Move[] getPossibleForwardMoves(int[] diceVals)
	{
		Move[] moves = new Move[diceVals.length];
		for(int i = 0; i < diceVals.length; i++)
		{
			int destination = (turnOwner == TEAM_WH)? pivotSquare-diceVals[i] :pivotSquare+diceVals[i] ;
			moves[i] = new Move(pivotSquare, destination, this.turnOwner);
		}
		return moves;
	}

	private Move[] getPossibleBackMoves(int squarePos)
	{
		int counter = 0;
		for(int i = 0; i < movesMade.length; i++)
			if(movesMade[i] != null && movesMade[i].to == squarePos) counter++;

		Move[] moves = new Move[counter];
		counter = 0;
		for(int i = 0; i < movesMade.length; i++)
			if(movesMade[i] != null && movesMade[i].to == squarePos) moves[counter++] = movesMade[i];

		return moves;
	}

	private int setNewPivot(int squarePos)
	{
		pivotSquare = squarePos;
		unHighlightAll();
		int[] diceValuesLeft = getCurrentDice().getUnusedValues();
		Move[] moves = getPossibleForwardMoves(diceValuesLeft); //þessi move geta falið í sér ólögleg(<0 eða >25) gildi, þarf að leiðrétta
		Move[] backMoves = getPossibleBackMoves(squarePos);		

		for(int i = 0; i < moves.length; i++)
			if(board.isPlayPossible(moves[i]))
			{
				int destination = moves[i].to;
				if(destination > 24) destination = 27; 		//Hér fer leiðrétting fram. Hér er leyfi gefið fyrir því að nota reiti 26 og 27
				else if(destination < 1) destination = 26;
				highlightSquare(destination);
			}

		for(int i = 0; i < backMoves.length; i++)
			highlightSquare(backMoves[i].from);
		
		return pivotSquare;
	}

	private Move checkIfReverseRemoveMoveIfSo(int squarePos)
	{
		for(int i = 0; i < movesMade.length; i++)
			if(movesMade[i] != null && movesMade[i].to == pivotSquare && movesMade[i].from == squarePos)
			{
				Move move = movesMade[i];
				movesMade[i] = null;
				return move;
			}
		return null;
	}

	private void storeMove(Move move)
	{
		for(int i = 0; i < movesMade.length; i++)
			if(movesMade[i] == null)
				{movesMade[i] = move;  return; }
	}
	
	private Message[] prepareSingleReverseMove(Move move)  //ef move var reverse þá er from: fyrrum endastöð og to: fyrrum upphaf
	{													   		  
		Message[] moves = (move.killed)? new Message[2]: new Message[1] ;
		moves[0] = Message.moveMessage(move.to, move.from, false);
		if(!move.killed) return moves;
		
		int deadzone = (move.team == TEAM_WH)? 0 : 25 ;
		moves[1] = Message.moveMessage(deadzone, move.to, false); 
		return moves;
	}
	
	private Message[] prepareSingleForwardMove(Move move) 
	{												
		Message[] moves = (move.killed)? new Message[2]: new Message[1] ;
		moves[0] = Message.moveMessage(move.from, move.to, move.killed);
		if(!move.killed) return moves;
		else 
		{
			int deadZone = (move.team == TEAM_WH)? 0 : 25 ;
			moves[1] = Message.moveMessage(move.to, deadZone, false);
		}
		return moves;
	}

	private void performMovement(int squarePos) 
	{
		Move reverseMove = checkIfReverseRemoveMoveIfSo(squarePos);
		if(reverseMove != null) 
		{
			board.reverseMovement(reverseMove);
			getCurrentDice().markAsUnused(calculateRealMovement(reverseMove.from, reverseMove.to));
			deliverInTurnMoveMessage(prepareSingleReverseMove(reverseMove));
		}
		else
		{
			Move forwardMove = new Move(pivotSquare, squarePos, turnOwner);
			if(board.containsTeam(squarePos, otherTeam(turnOwner))) forwardMove.setKilledToTrue();
			board.forwardMovement(forwardMove);
			storeMove(forwardMove);
			
			getCurrentDice().markAsUsed(calculateRealMovement(forwardMove.from, forwardMove.to));
			deliverInTurnMoveMessage(prepareSingleForwardMove(forwardMove));
		}
		
		unHighlightAll();
		pivotSquare = NONE_CHOSEN;  
	}
	
	private int calculateRealMovement(int from, int to)
	{
		if(to == 26) to = 0;
		else if(to == 27) to = 25;
		return Math.abs(to - from);
	}
	
	private void deliverInTurnMoveMessage(Message[] moves)
	{
		if(movesLeft() == 0) UnreadMessageStorage.storeMessage(getCurrentUserName(), Message.mayEndTurnMessage());
		UnreadMessageStorage.storeMessages(getCurrentUserName(), moves);
	}
	
	public String[] getPlayersAndObservers()
	{
		String[] observerList = observers.getAllUsers();   
		String[] combined = new String[observerList.length + 2];   
		combined[0] = players[0];
		combined[1] = players[1];
		int pointer = 2;
		for(String observer: observerList)
			combined[pointer++] = observer;
		
		return combined;
	}
	
	//************ Delivery adferdir her ad nedan ***************************
	
	public void deliverEndMovesToOthers()
	{
		this.deliverPostTurnMoveMessages(this.movesMade, this.getCurrentUserName());
	}
	
	
	public void deliverEntireGameState(String observer)
	{
		Message[] toDelay = new Message[33];
		int pointer = 0;
		toDelay[pointer++] = Message.buildBoardMessage("dice", whiteDice.first(), TEAM_WH, 0);
		toDelay[pointer++] = Message.buildBoardMessage("dice", whiteDice.second(), TEAM_WH, 0);
		toDelay[pointer++] = Message.buildBoardMessage("dice", blackDice.first(), TEAM_BL, 0);
		toDelay[pointer++] = Message.buildBoardMessage("dice", blackDice.second(), TEAM_BL, 0);
		toDelay[pointer++] = Message.buildBoardMessage("cube", this.multiplier, TEAM_NONE, 0);
		
		Square[] squares = board.getSquares();
		for(int i = 0; i < squares.length; i++)
		{
			Square sq = squares[i];
			toDelay[pointer++] = Message.buildBoardMessage("square", sq.count(), sq.getTeam(), i); 
		}
		
		UnreadMessageStorage.storeMessages(observer, toDelay);
	}
	
	//observers þurfa líka að fá gameAccess(false) til að fjarlægja announcement
	
	private void deliverHumanStartingMessage(String starter, int startTeam, DicePair startDice, int moveCount) 
	{
		Message gameAccess = Message.gameAccessMessage(starter, true);
		Message obsGameAccess = Message.gameAccessMessage(starter, false);
		Message diceVals = Message.diceValueMessage(startDice.first(), startDice.second(), startTeam, starter, moveCount);
		Message[] forPlayers = {gameAccess, diceVals};  
		Message[] forObservers = {obsGameAccess, diceVals};
		
		for(String user: getPlayersAndObservers()) //röng staðsetning á present match skilaboðum, eiga aðeins að gerast í upphafi
			if(user.equals(players[0]) || user.equals(players[1]))  
				UnreadMessageStorage.storeDelayedMessages(user, forPlayers, 3);
			else
				UnreadMessageStorage.storeDelayedMessages(user, forObservers, 3);
	}
	
	private void deliverBotStartingMessage(String starter, int startTeam, DicePair startDice, Move[] moves)
	{
		Message[] movs = prepareReturnMoves(moves);
		Message gameAccess = Message.gameAccessMessage(starter, true);
		Message obsGameAccess = Message.gameAccessMessage(starter, false);
		Message diceVals = Message.diceValueMessage(startDice.first(), startDice.second(), startTeam, starter, -1);
		Message turnFinished = Message.turnFinishedMessage(starter);
		
		Message[] forPlayer = {gameAccess, diceVals, turnFinished};  
		forPlayer = Message.mergeMessages(forPlayer, movs);
		Message[] forObservers = Message.mergeMessages(movs, diceVals);   
		
		for(String user: getPlayersAndObservers())
			if(user.equals(players[0])) 
				UnreadMessageStorage.storeDelayedMessages(user, forPlayer, 3);
			else
				UnreadMessageStorage.storeDelayedMessages(user, forObservers, 3);
	}
	
	private void deliverPostTurnMoveMessages(Move[] moves, String actor)
	{
		Message turnFinished = Message.turnFinishedMessage(actor); 
		Message[] toAllButActor = Message.mergeMessages(prepareReturnMoves(moves), turnFinished);
		
		for(String user: getPlayersAndObservers())
			if(!user.equals(actor))
				UnreadMessageStorage.storeMessages(user, toAllButActor);
			else
				UnreadMessageStorage.storeMessage(actor, turnFinished);
	}
	
	private Message[] prepareReturnMoves(Move[] moves) 
	{
		int moveCount = moves.length;
		for(int i = 0; i < moves.length; i++)
			if(moves[i].killed) moveCount++;
		
		Message[] clientMoves = new Message[moveCount];
		int pointer = 0;
		
		for(int i = 0; i < moves.length; i++)
		{
			Move m = moves[i];
			clientMoves[pointer++] = Message.animateMessage(m.from, m.to, m.killed);
			if(m.killed)
			{
				int deadZone = (m.team == TEAM_WH)? 0 : 25 ;
				clientMoves[pointer++] = Message.animateMessage(m.to, deadZone, false);
			}
		}
		return clientMoves;
	}
	
	public Message prepareMatchPresentationMessage()
	{
		return Message.announcementMessage("pres_match", players[0]+"_"+players[1]);
	}
	
	private void deliverPostAcceptMessage(int moveCount)  //deilir virkni með dicethrow
	{
		String accepter = getOtherUserName();
		String doubler = getCurrentUserName();
		Message accepted = Message.playerAcceptedMessage(accepter);  
		Message diceMsg = Message.diceValueMessage(getCurrentDice().first(), getCurrentDice().second(), turnOwner, doubler, moveCount);
		
		Message[] toAll = {accepted, diceMsg};
		for(String user: getPlayersAndObservers())
				UnreadMessageStorage.storeMessages(user, toAll);
	}
	
	private void deliverDoublingOfferMessage()
	{
		String doubler = getCurrentUserName();
		Message doubling = Message.playerDoubledMessage(doubler, 1024);  //ath að það þarf helst að breyta doubling cube kerfinu, einfalda það
		
		for(String user: getPlayersAndObservers())
				UnreadMessageStorage.storeMessage(user, doubling);
	}
	
	private void deliverHumanMovesToObservers(Move[] humanMoves)
	{
		Message[] movs = prepareReturnMoves(humanMoves);  
		String[] observers = {"obs"};
		for(String observer: observers)
			UnreadMessageStorage.storeMessages(observer, movs);
		
	}
	
	private void deliverBotInfoMessage(Move[] botMoves, DicePair botDice) 
	{
		Message yourTurn = Message.turnFinishedMessage(players[1]);
		Message diceVals = Message.diceValueMessage(botDice.first(), botDice.second(), TEAM_BL, players[1], -1);
		Message[] combo = {yourTurn, diceVals};
		Message[] botMov = prepareReturnMoves(botMoves);
		UnreadMessageStorage.storeMessages(players[0], Message.mergeMessages(botMov, combo) );
	}
	
	private void deliverPostDiceThrowMessage(int moveCount, String thrower)
	{
		Message diceVals = Message.diceValueMessage(getCurrentDice().first(), getCurrentDice().second(), turnOwner, thrower, moveCount );
		if(moveCount == 0) UnreadMessageStorage.storeMessage(thrower, Message.mayEndTurnMessage());
		for(String user: getPlayersAndObservers())
				UnreadMessageStorage.storeMessage(user, diceVals);   
	}
	
	private void deliverNewPivotMessage(int pivot)
	{
		int[] lit = board.getHighlighted();
		int pos1 = (lit.length > 0)? lit[0]: -1;		
		int pos2 = (lit.length > 1)? lit[1] : -1 ;
		
		UnreadMessageStorage.storeMessage(getCurrentUserName(), Message.newPivotMessage(pos1, pos2));
	}
	
	private void deliverExplainMessage(int squarePos)
	{
		Message[] msg = {Message.explainMessage("Play not possible")};
		UnreadMessageStorage.storeMessages(getCurrentUserName(), msg);
	}

	private boolean isMoveReverse(Move move)
	{
		return (move.to > move.from && move.team == TEAM_WH)||(move.to < move.from && move.team == TEAM_BL);
	}
	

	private boolean isTurnHuman()
	{
		String currUser = getCurrentUserName();
		return !(currUser.equals("Hard Bot") || currUser.equals("Medium Bot") || currUser.equals("Easy Bot"));
	}

	public String getCurrentUserName()
	{
		return (turnOwner == TEAM_WH)? players[0] : players[1] ;
	}
	public String getOtherUserName()
	{
		return (turnOwner == TEAM_WH)? players[1] : players[0] ;
	}
	
	public boolean hasGameEnded(){
		return board.whoWon() != TEAM_NONE;
	}
	private void switchTurn(){
		this.turnOwner = (this.turnOwner+1)%2;
	}
	
	private int otherTeam(int team){
		return (team+1)%2;
	}
	public UserStack getObservers()
	{
		return this.observers;
	}
	public String[] getPlayers()
	{
		return players;
	}
	
	private void highlightSquare(int pos){
		board.highlightSquare(pos);
	}
	private void unHighlightAll(){
		board.unHighlightAll();
	}
	private DicePair getCurrentDice(){
		return (turnOwner == TEAM_WH)? whiteDice : blackDice ;
	}
	private boolean currentTeamSelected(int squarePos){
		return board.containsTeam(squarePos, this.turnOwner);
	}
	private boolean irrelevantSquare(int squarePos){
		return !board.containsTeam(squarePos, this.turnOwner) && !isSquareHighlighted(squarePos);
	}
	private boolean isSquareHighlighted(int squarePos){
		return board.getSquare(squarePos).isLightOn();
	}
	private boolean pivotSwitchCommand(int squarePos){
		return (currentTeamSelected(squarePos)) && !isSquareHighlighted(squarePos);
	}
}