
function messageDispatcher(msgArray) 
{
	if(msgArray.length > 0) console.log(msgArray);
	//else                    console.log("RECEIVED EMPTY");

	for(var i = 0; i < msgArray.length; i++)
	{
		var data = msgArray[i];

		if(data.action === "legal_signup")
			response_controller.successfulSignUp(data.desc);
		else if(data.action === "no_response")       
			response_controller.deliverExplanations(data.desc, data.str);
		else if(data.action === "new_pivot")
			response_controller.highlightSquares(data);
		else if(data.action === "player_doubled")
			response_controller.doubleWasOffered(data.valOne, data.desc);
		else if(data.action === "offer_accepted")
			response_controller.offerWasAcceptedOffer(data.desc);
		else if(data.action === "turn_finished")
			response_controller.turnWasFinished(data.desc);
		else if(data.action === "game_access")
			response_controller.userGotGameAccess(data.desc, data.killed);
		else if(data.action === "move")					
			i = response_controller.inTurnPawnMovement(msgArray, i);
		else if(data.action === "animate")
			i = response_controller.animatedPawnMovement(msgArray, i);
		else if(data.action === "dice_throw")
			response_controller.handleDiceRoll(data.desc, data.valOne, data.valTwo,data.valThree,data.white );
		else if(data.action === "may_end_turn")			
			response_controller.displayEndTurn();
		else if(data.action === "chat_entry")
			response_controller.forwardNewChatEntry(data.str, data.desc);
		else if(data.action === "wait_entry")
			response_controller.addWaitingListEntry(data.str);
		else if(data.action === "ongoing_entry")
			response_controller.addOngoingEntry(data.str);
		else if(data.action === "delete_entry")
			response_controller.deleteGlobalEntry(data.desc);
		else if(data.action === "build")
			i = response_controller.buildEntireBoard(msgArray, i);
		else if(data.action === "refresh")
			response_controller.startRefreshCycle(data.desc);  //fá mismunandi tíðnir hér?
		else if(data.action === "announcement")
			response_controller.processAnnouncement(data.str, data.desc);
		else if(data.action === "versus_entry")
			response_controller.updateStats(data.str, data.desc);
		else if(data.action === "trophy_entry")
			response_controller.updateTrophyEntry(data.str);
		else if(data.action === "loading_finished")
			response_controller.alertDOMOfLoading();
	}
}


var LOGIN_FRAME = 0, TROPHY_FRAME = 4 ,GAME_FRAME = 2, LOBBY_FRAME = 3;  
var response_controller = {

	clientState: LOGIN_FRAME,

	alertDOMOfLoading: function()
	{
		dom_manipulator.initialLoadingIsDone();
	},

	goToTrophyRoom: function()
	{
		this.clientState = TROPHY_FRAME;
		dom_manipulator.switchToFrame(4);
	},

	goToGameLobby: function()
	{
		this.clientState = LOBBY_FRAME;
		dom_manipulator.switchToFrame(3);
	},

	leaveMatchTransition: function()
	{
		this.removeAllAnnouncements();
		this.clientState = LOBBY_FRAME;
		dom_manipulator.switchToFrame(3);
		dom_manipulator.clearMatchChat();
		dom_manipulator.showTrophyAnnouncements();
	},

	presentEndedMatch: function(winner, loser, winPoints, lossPoints)
	{
		this.hideEndTurn();
		request_controller.gameHasEnded();
		this.removeAllAnnouncements();
		dom_manipulator.displayEndMatchWindow(winner, loser, winPoints, lossPoints);
	},

	presentEndedGame: function(winner, endedHow, multiplier, cube)
	{
		this.hideEndTurn();
		request_controller.gameHasEnded();
		dom_manipulator.displayEndGameWindow(winner, endedHow, multiplier, cube);
	},

	switchToPreMatchView: function(playerOne, playerTwo) //annaðhvort fleiri gögn hér í gegn eða geyma gögn á client
	{
		this.clientState = GAME_FRAME;
		g_animator.switchToFreshBoard();
		dom_manipulator.displayPreMatchPresentation(playerOne, playerTwo);
		dom_manipulator.switchToFrame(2);
	},

	//Tekur við match presentation, end game, end match og trophy announcement
	//þetta fall veit hvernig skal unpacka data sem er á forminu str_str2_str3_.....
	processAnnouncement: function(data, type)
	{
		var unpacked = data.split("_");
		if(type === "trophy")
		{
			dom_manipulator.storeTrophyAnnouncement(unpacked[0], unpacked[1], unpacked[2]);
			if(this.clientState !== GAME_FRAME) 
				dom_manipulator.showTrophyAnnouncements();
		}
		else if(type === "pres_match")
			this.switchToPreMatchView(unpacked[0], unpacked[1]);
		else if(type === "game_over")
			this.presentEndedGame(unpacked[0], unpacked[1], unpacked[2], unpacked[3]);
		else if(type === "match_over")
			this.presentEndedMatch(unpacked[0], unpacked[1], unpacked[2], unpacked[3]);
	},

	startRefreshCycle: function(command)
	{
		main.init();
	},

	updateTrophyEntry: function(data)
	{
		var info = data.split("_");
		dom_manipulator.updateTrophyEntry(info[0],info[1], info[2], info[3]);
	},

	updateStats: function(data, type)
	{
		if(type === "versus")
		{
			var info = data.split("_");
			if(info[0] === request_controller.username)
				dom_manipulator.updateVersusStats(info[0], info[1], info[2], info[3], info[4], info[5]);
			else 
				dom_manipulator.updateVersusStats(info[1], info[0], info[3], info[2], info[5], info[4]);
		}
		else if(type === "overall")
		{
			var winCountStr = data.split("_")[0];
			var lossCountStr = data.split("_")[1];
			dom_manipulator.updateOverallStats(winCountStr, lossCountStr);
		}
	},

	buildEntireBoard: function(msgArray, pointer)  //búist við sequential pakka frá server?
	{
		var countList = [];
		var teamList = [];
		var diceVals = [];
		var doubCube = null;

		while(pointer < msgArray.length && msgArray[pointer].action === "build")
		{
			var buildMsg = msgArray[pointer];
			if(buildMsg.desc === "square")
			{
				countList[buildMsg.valThree] = buildMsg.valOne;
				teamList[buildMsg.valThree] = buildMsg.valTwo;
			}
			else if(buildMsg.desc === "dice")
				diceVals.push(buildMsg.valOne);
			else
				doubCube = buildMsg.valOne;
			
			pointer++;
		}

		g_animator.buildCustomBoard(countList, teamList, diceVals, doubCube);
		dom_manipulator.switchToFrame(2);
		return pointer - 1;
	},

	animatedPawnMovement: function(msgArray, pointer)  //búist við sequential pakka frá server
	{
		request_controller.animationHasStarted();
		this.hideEndTurn();
		var moveList = [];
		while(pointer < msgArray.length && msgArray[pointer].action === "animate")
		{
			var moveMsg = msgArray[pointer];
			moveList.push({from: moveMsg.valOne, to: moveMsg.valTwo, killed: moveMsg.killed});
			pointer++;
		}

		g_animator.receiveMovePackage(moveList);
		return pointer - 1;
	},

	inTurnPawnMovement: function(msgArray, pointer)  
	{
		request_controller.animationHasStarted();
		g_animator.unHighlightAll();
		this.hideEndTurn();
		var moveList = [];
		while(pointer < msgArray.length && msgArray[pointer].action === "move")
		{
			var moveMsg = msgArray[pointer];
			moveList.push({from: moveMsg.valOne, to: moveMsg.valTwo, killed: moveMsg.killed});
			pointer++;
		}

		g_animator.receiveMovePackage(moveList);
		return pointer - 1;
	},

	deleteGlobalEntry: function(entryId)
	{
		dom_manipulator.deleteEntry(entryId);
	},

	addWaitingListEntry: function(entryString) //idStr, playername, pointsToWin, clock
	{
		var unpacked = entryString.split("_");
		dom_manipulator.addToWaitingList(unpacked[0], unpacked[1], unpacked[2], unpacked[3]);
	},

	addOngoingEntry: function(entryString) //idStr, pOne, pTwo, points, clock
	{
		var unpacked = entryString.split("_");
		dom_manipulator.addToOngoingList(unpacked[0], unpacked[1], unpacked[2], unpacked[3], unpacked[4]);
	},

	forwardNewChatEntry: function(entry, chatType)  //athuga hér hvort lobby frame sé client state?
	{
		if(chatType === "match")
			dom_manipulator.renderNewGameChatEntry(entry);
		else if(chatType === "lobby")
			dom_manipulator.renderNewLobbyChatEntry(entry);
	},

	hideEndTurn: function()
	{
		$('#rammi2 #end_turn').hide(); 
	},

	displayEndTurn: function()
	{
		$('#rammi2 #end_turn').show();
	},

	removeAllAnnouncements: function()
	{
		$('body .announce').remove();
	},

	handleDiceRoll: function(thrower, diceOne, diceTwo, moveCount, isWhite)
	{
		request_controller.animationHasStarted();

		if(thrower === request_controller.username)
			request_controller.userThrewDice();

		//if(moveCount >= 0)
		//	dom_manipulator.explainSuccess("Moves left to perform: "+ moveCount, 5000);
		
		if(isWhite)
			g_animator.whiteDiceAnimation(diceOne, diceTwo);
		else
			g_animator.blackDiceAnimation(diceOne, diceTwo);
	},

	userGotGameAccess: function(starterName, hasPlayingAccess) 
	{
		if(hasPlayingAccess) 
			request_controller.userGotGameAccess();

		if(request_controller.username === starterName)
			request_controller.userStartsGame();
		
		dom_manipulator.explainSuccess(starterName+" starts the game", 3500);
		g_animator.switchToFreshBoard();
		this.removeAllAnnouncements();
	},

	turnWasFinished: function(finisher)
	{
		if(finisher === request_controller.username)  
			request_controller.userFinishedTurn();
		else if(request_controller.isUserPlaying())
		{
			if(request_controller.currentlyAnimating)
				g_animator.showOptionsAfterAnimation();
			else
				dom_manipulator.showCubeAndDice();

			request_controller.opponentFinishedTurn();
		}

	},

	offerWasAccepted: function(accepter)
	{
		if(accepter === "username")
			request_controller.userAccepted();
		else if(request_controller.isUserPlaying())
			request_controller.opponentAccepted();

		//einhver visual lýsing til allra/flestra á því að doubling tilboðinu var tekið og leikur getur haldið áfram
	},

	doubleWasOffered: function(newStakes, offeringPlayer)
	{
		if(offeringPlayer === "username")
			request_controller.userDoubled();
		else if(request_controller.isUserPlaying())
			request_controller.opponentDoubled();

		g_animator.userDoubling(newStakes);
	},

	deliverExplanations: function(reason, type)
	{
		if(type === "error") 
			dom_manipulator.explainNonResponse(reason,3000);
	},

	highlightSquares: function(data)
	{
		g_animator.highlightSquares(data.valOne, data.valTwo);
	},

	successfulSignUp: function(username) 
	{								     
		dom_manipulator.explainSuccess("Welcome to the game "+username, 5000);
		this.clientState = LOBBY_FRAME;

		dom_manipulator.switchToFrame(3);

		request_controller.username = username;
	},

	
}