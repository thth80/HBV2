function refreshCallback()
{
	$.ajax({
           	'url': "http://localhost:9090/refresh?name="+request_controller.username,
           	'type' : 'POST',
           	'dataType':'json',
           	'success' : messageDispatcher,
           	'error' : function()
           	{
               	console.log("error");
           	}
       	});
}


var NOT_PLAYING = 0, NO_ACCESS = 1, CUBE_DICE = 2, CUBE = 3, SQUARES = 4;
var request_controller = {

	username: "",
	shouldRefresh: false,
	clientGameAccess: NOT_PLAYING,
	currentlyAnimating: false,
	boundingBoxes: [],
	clientRemainingTime: 100000,
	opponentRemainingTime: 100000,
	lastClientClockTick: 0,

	animationHasStarted: function()
	{
		this.currentlyAnimating = true;
	},
	animationHasStopped: function()
	{
		this.currentlyAnimating = false;
	},

	isUserPlaying: function()
	{
		return this.clientGameAccess !== NOT_PLAYING;
	},

	userGotGameAccess: function()  
	{
		this.clientGameAccess = NO_ACCESS;
	},

	userStartsGame: function() 
	{
		this.clientGameAccess = SQUARES;
	},

	userThrewDice: function()
	{
		this.clientGameAccess = SQUARES;
	},

	gameHasEnded: function()  
	{
		this.clientGameAccess = NOT_PLAYING;
	},

	opponentFinishedTurn: function()  
	{
		this.clientGameAccess = CUBE_DICE;
		shouldRefresh = false;
	},

	userFinishedTurn: function()  
	{
		this.clientGameAccess = NO_ACCESS;
		shouldRefresh = true;
	},

	opponentDoubled: function()  
	{
		this.clientGameAccess = CUBE;
	},

	userDoubled: function() 
	{
		this.clientGameAccess = NO_ACCESS;
	},

	userAccepted: function()
	{
		this.clientGameAccess = NO_ACCESS;
	},

	opponentAccepted: function()
	{
		this.clientGameAccess = SQUARES;
	},

	handleDOMEventsById: function(objectId)
	{
		if(objectId === "signup")
			this.signUpAttempt();
		else if(objectId === "login")
			this.logInAttempt();
		else if(objectId === "end_turn")
			this.finishUserTurn();
		else if(objectId === "leave_match")
		{ 
			$('#leave_match').hide();
			$('#leave_decide').show();
		}
		else if(objectId === "confirm_leave")
		{ 
			$('#leave_match').show();
			$('#leave_decide').hide();
			response_controller.leaveMatchTransition();
			this.leaveMatchOnServer();
		}
		else if(objectId === "cancel_leave")
		{
			$('#leave_match').show();
			$('#leave_decide').hide();
		}
		else if(objectId === "game_lobby")
			response_controller.goToGameLobby();
		else if(objectId === "trophy_room")
			response_controller.goToTrophyRoom();
		else if(objectId === "join_offline")
			this.submitOfflineMatchRequest();
		else if(objectId === "throw_dice")
			this.handleDiceClick();
		else if(objectId === "game_chat_entry")
			this.submitChatToServer($('#game_chat_entry [type="text"]'));
		else if(objectId === "lobby_chat_entry")
			this.submitChatToServer($('#lobby_chat_entry [type="text"]'));
		else if(objectId === "add_wait_entry")
			this.submitNewWaitingEntry();
		else if(objectId === "double_offer")
			this.submitDoublingOffer();
	},

	handleDOMEventsByElement: function(element)
	{
		var elemClass = element.attr('class');

		if(elemClass.indexOf("btn_join") > -1)
			this.tryToJoinOnlineMatch(element.attr('id'));
		else if(elemClass.indexOf("btn_cancel") > -1)
			this.submitWaitlistRemoval(element.attr('id'));
		else if(elemClass.indexOf("btn_spec") > -1)
			this.joinMatchAsObserver(element.attr('id'));
		
	},

	handleCanvasEvents: function(cx, cy)
	{
		var squarePos = this.translateInputCoords(cx, cy);
		if(squarePos >= 0)
			this.handleSquareClick(squarePos);
	},

	joinMatchAsObserver: function(idstr)
	{
		var url = "http://localhost:9090/observeMatch?name="+this.username+"&id="+idstr;
		console.log(url);
		$.ajax({
            	'url': url,
            	'type' : 'POST',
            	'dataType':'json',
            	'success' : messageDispatcher,
            	'error' : function()
            	{
                	console.log("error");
            	}
        	});
	},

	submitWaitlistRemoval: function(idstr)
	{
		var url = "http://localhost:9090/removeWaitEntry?name="+this.username+"&id="+idstr;
		console.log(url);
		$.ajax({
            	'url': url,
            	'type' : 'POST',
            	'dataType':'json',
            	'success' : messageDispatcher,
            	'error' : function()
            	{
                	console.log("error");
            	}
        	});
	},

	submitNewWaitingEntry: function()
	{
		dom_manipulator.explainSuccess("A new game was created and put on the waiting list", 4000);
		console.log(dom_manipulator.createWaitEntryString(this.username));
		var url = "http://localhost:9090/addWaitEntry?entry="+dom_manipulator.createWaitEntryString(this.username);
		$.ajax({
            	'url': url,
            	'type' : 'POST',
            	'dataType':'json',
            	'success' : messageDispatcher,
            	'error' : function()
            	{
                	console.log("error");
            	}
        	});
	},

	submitOfflineMatchRequest: function()
	{
		console.log(dom_manipulator.createOfflineMatchEntryString(this.username));

		var url = "http://localhost:9090/joinOfflineMatch?diff=1&entry="+
				dom_manipulator.createOfflineMatchEntryString(this.username);
		console.log(url);
		$.ajax({
            	'url': url,
            	'type' : 'POST',
            	'dataType':'json',
            	'success' : messageDispatcher,
            	'error' : function()
            	{
                	console.log("error");
            	}
        	});
	},

	submitDoublingOffer: function()
	{
		return;
		if(this.currentlyAnimating)
			dom_manipulator.explainNonResponse("You must wait for the animation to finish", 4000);
		else if(!(this.clientGameAccess === CUBE_DICE || this.clientGameAccess === CUBE))
			dom_manipulator.explainNonResponse("You cannot use the doubling cube right now", 4000);
		else
		{
			dom_manipulator.hideCubeButton();
			dom_manipulator.hideDiceButton();
			$.ajax({
            	'url': "http://localhost:9090/cube",
            	'type' : 'POST',
            	'dataType':'json',
            	'success' : messageDispatcher,
            	'error' : function()
            	{
                	console.log("error");
            	}
        	});
		}
	},

	handleDiceClick: function()
	{
		if(this.currentlyAnimating)
			dom_manipulator.explainNonResponse("You must wait for the animation to finish", 4000);
		else if(this.clientGameAccess !== CUBE_DICE)
			dom_manipulator.explainNonResponse("You cannot use the dice right now",4000);
		else
		{
			dom_manipulator.hideDiceButton();
			dom_manipulator.hideCubeButton();

			var url = "http://localhost:9090/diceThrow?name="+this.username;
			console.log(url);
			$.ajax({
            	'url': url,
            	'type' : 'POST',
            	'dataType':'json',
            	'success' : messageDispatcher,
            	'error' : function()
            	{
                	console.log("error");
            	}
        	});
		}
	},

	handleSquareClick: function(squarePos)
	{
		if(this.currentlyAnimating)
			dom_manipulator.explainNonResponse("You must wait for the animation to finish",4000);
		else if(this.clientGameAccess !== SQUARES)
			dom_manipulator.explainNonResponse("You cannot move any pawns right now",3000);
		else  //legal request
		{
			var queryStr = "pos="+squarePos+"&name="+this.username;
			var url = "http://localhost:9090/square?"+queryStr;
			console.log(url);
			$.ajax({
            	'url': url,
            	'type' : 'POST',
            	'dataType':'json',
            	'success' : messageDispatcher,
            	'error' : function()
            	{
                	console.log("error");
            	}
        	});
		}
	},

	submitChatToServer: function(inputElement)
	{
		var chatText = inputElement.val();
		inputElement.val("");

		var url = "http://localhost:9090/submitChat?name="+this.username+"&chat="+chatText;
		console.log(url);
		$.ajax({
            	'url': url,
            	'type' : 'POST',
            	'dataType':'json',
            	'success' : messageDispatcher,
            	'error' : function()
            	{
                	console.log("error");
            	}
        	});
	},

	joinOfflineMatch: function()
	{
		var url = "http://localhost:9090/joinOfflineMatch?id="+gameId;
		$.ajax({
            	'url': url,
            	'type' : 'POST',
            	'dataType':'json',
            	'success' : messageDispatcher,
            	'error' : function()
            	{
                	console.log("error");
            	}
        	});
	},

	tryToJoinOnlineMatch: function(waitId)
	{
		var url = "http://localhost:9090/joinOnlineMatch?id="+waitId+"&name="+this.username;
		console.log(url);
		$.ajax({
            	'url': url,
            	'type' : 'POST',
            	'dataType':'json',
            	'success' : messageDispatcher,
            	'error' : function()
            	{
                	console.log("error");
            	}
        	});
	},

	leaveMatchOnServer: function()
	{
		var url = "http://localhost:9090/leaveMatch?name="+this.username;
		console.log(url);
		$.ajax({
            	'url': url,
            	'type' : 'POST',
            	'dataType':'json',
            	'success' : messageDispatcher,
            	'error' : function()
            	{
                	console.log("error");
            	}
        	});
	},

	finishUserTurn: function()
	{
		if(this.currentlyAnimating) return;


		g_animator.unHighlightAll();
		response_controller.hideEndTurn();
		$.ajax({
            	'url': "http://localhost:9090/endTurn?name="+this.username,
            	'type' : 'GET',
            	'dataType':'json',
            	'success' : messageDispatcher,
            	'error' : function()
            	{
                	console.log("error");
            	}
        	});
	},

	signUpAttempt: function()
	{
		var username = $('#signup [name="username"]').val();
		var password = $('#signup [name="pw1"]').val();
		var password2 = $('#signup [name="pw2"]').val();

		var valid = /^[a-z0-9 ]+$/i.test(username);
		if(!valid)
			dom_manipulator.explainNonResponse("Only alphanumeric characters allowed in a username",5500);
		else if(password2 !== password)
			dom_manipulator.explainNonResponse("The passwords given do not match", 5500);
		else
		{
			var queryStr = "name="+username+"&pw="+password;
			var url = "http://localhost:9090/signup?" + queryStr;
			console.log(url); 

			$.ajax({
            	'url': url,
            	'type' : 'POST',
            	'dataType':'json',
            	'success' : messageDispatcher,
            	'error' : function()
            	{
                	console.log("error");
            	}
        	});
		}
	},

	logInAttempt: function()
	{
		var username = $('#login [name="username"]').val();
		var password = $('#login [name="pw"]').val();

		var valid = /^[a-z0-9 ]+$/i.test(username);
		if(!valid)
			dom_manipulator.explainNonResponse("Only alphanumeric characters allowed in a username", 5500);
		else
		{
			var queryStr = "name="+username+"&pw="+password;
			var url = "http://localhost:9090/login?" + queryStr;
			console.log(url); 

			$.ajax({
            	'url': url,
            	'type' : 'GET',
            	'dataType':'json',
            	'success' : messageDispatcher,
            	'error' : function()
            	{
                	console.log("error");
            	}
        	});
		}
	},

	setBoundingBoxes: function(squares, squareW, squareH)
	{
		for(var i = 0; i < squares.length; i++)
		{
			var sq = squares[i];
			var bottom = sq.bottomY;
			var top = bottom - squareH;
			if(sq.pointsDown)
			{
				top = sq.bottomY;
				bottom = top + squareH;
			}
	
			var left = sq.cx - squareW*0.5;
			var right = left + squareW;
			this.boundingBoxes.push({top: top, bottom: bottom, right: right, left: left, id: sq.id});
		}
	},

	translateInputCoords: function(cx, cy) 
	{
		var objectId = this.translatePosToId(cx, cy);
		return objectId;
	},

	translatePosToId: function(cx, cy)
	{
		for(var i = 0; i < this.boundingBoxes.length; i++)
		{
			var b = this.boundingBoxes[i];
			if(cx >= b.left && cx <= b.right && cy >= b.top && cy <= b.bottom)
				return b.id;			
		}
		return -1;
	},

	wasCubeClicked: function(cx, cy)
	{
		return cx < 50 && cy < 50;
	},

}