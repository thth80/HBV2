var dom_manipulator = {

	trophyAnnouncements: [],
	isUserDataLoaded: false,

	initialLoadingIsDone: function()
	{
		this.isUserDataLoaded = true;
	},

	explainNonResponse: function(reason, ms)
	{
		$('#success_banner p').text(reason);
		$('#success_banner').fadeIn(500).fadeOut(ms);
	},

	explainSuccess: function(reason, ms)
	{
		$('#success_banner p').text(reason);
		$('#success_banner').fadeIn(500).fadeOut(ms);
	},

	showCubeAndDice: function()
	{
		this.showCubeButton();
		this.showDiceButton();
	},
	
	showCubeButton: function()
	{
		$('#rammi2 #double_offer').show();
	},

	hideCubeButton: function()
	{
		$('#rammi2 #double_offer').hide();
	},

	showDiceButton: function()
	{
		$('#rammi2 #throw_dice').show();
	},

	hideDiceButton: function()
	{
		$('#rammi2 #throw_dice').hide();
	},

	updateTrophyEntry: function(trophyName, descript ,imageUrl, percent) 
	{
		var percentNum = Number(percent);
		if(percentNum > 100) percent = "100";
		var styleVal = "width:"+percent+"%";

		var trophyEntry = $('#rammi4 #trophies [name="'+trophyName+'"]');
		if(trophyEntry.length) 
		{
			trophyEntry.find('[role="progressbar"]').attr('style',styleVal).attr('aria-valuenow',percent).html(percent+"%");
		} 
		else 
		{
			var rand = Math.random();
			if(rand > 0.76) imageUrl = "http://api.adorable.io/avatars/80";
			else if(rand > 0.5) imageUrl = "http://api.adorable.io/avatars/134";
			else if(rand > 0.25)  imageUrl = "http://api.adorable.io/avatars/120";
			else                         imageUrl = "./images/trophy_icon.jpg";

			var html = 
				'<div class="well well-sm clearfix" name="'+trophyName+'">'
				+	'<div class="well-content">'
				+		'<h3 class="media-heading">'+trophyName+'</h3>'
				+		'<div class="progress">'
				+		   '<div class="progress-bar progress-bar-success" role="progressbar" aria-valuenow="'+percent+'"'
				+			' aria-valuemin="0" aria-valuemax="100" style="'+styleVal+'">' +percent+"%" 
				+			'</div>'
				+		'</div>'
				+		'<p>'+descript+'</p>'
				+	'</div>'
				+	'<img src="'+imageUrl+'">'
				+ '</div>';

			$('#rammi4 #trophies .scroll').append($(html));
		}
	},

	updateVersusStats: function(user, oppo, userPoints, oppoPoints, userPawns, oppoPawns)
	{
		var versusEntry = $('#rammi4 #stats [name="'+oppo+'"]');
		var winPercentage = Math.floor(100*Number(userPoints)/(Number(userPoints)+Number(oppoPoints)));
		var styleVal = "width:"+winPercentage+"%";

		if(versusEntry.length)
		{
			versusEntry.find('[role = "progressbar"]').html(winPercentage+"%").attr('aria-valuenow', winPercentage+"").attr('style', styleVal);
			
		}
		else
		{
			var html = 
				'<div class="well well-lg clearfix" name="'+oppo+'">'
				+	'<div class="well-content">'
				+		'<h5 class="media-heading">'+"VS "+oppo+'</h5>'
				+		'<div class="progress">'
				+			'<div class="progress-bar progress-bar-info progress-bar-striped" role="progressbar" aria-valuenow="'+winPercentage
				+			'" aria-valuemin="0" aria-valuemax="100" style="'+styleVal+'">'+winPercentage+"%"
				+			'</div>'
				+		'</div>'
				+	'</div>'
				+ '</div>';

			var wellWrapper = $(html);

			$('#rammi4 #stats .scroll').append(wellWrapper);
		}
	},

	updateOverallStats: function(winCountStr, lossCountStr) 
	{
		var winPercentage = Math.floor(100*Number(winCountStr)/(Number(lossCountStr)+Number(winCountStr)));
		var overallEntry = $('#rammi4 #stats [name="over_all"]');
		var styleVal = "width:"+winPercentage+"%";

		if(overallEntry.length)
		{
			overallEntry.find('[role = "progressbar"]').html(winPercentage+"%").attr('aria-valuenow',winPercentage+"").attr('style', styleVal);
		}
		else
		{
			var html = 
			'<div class="well well-lg clearfix" name="'+"over_all"+'">'
			+	'<div class="well-content">'
			+	   '<h4 class="media-heading">Overall win statistics</h4>'
			+		'<div class="progress">'
			+		   '<div class="progress-bar progress-bar-info progress-bar-striped" role="progressbar" ' 
			+			'aria-valuenow="'+winPercentage+'" aria-valuemin="0" aria-valuemax="100" style="'+styleVal+'">'
			+			winPercentage+"%"
			+			'</div>'
			+		'</div>'
			+		'<p>Pawns who made it: </p>'
			+		'<p class="text-center">1972</p>'
			+		'<p>Opponents defeated: </p>'
			+		'<p class="text-center">17</p>'
			+	'</div>'
			+ '</div>';

			var wholeWindow = $(html);
			$('#rammi4 #stats .scroll').prepend(wholeWindow);
		}
	}, 

	storeTrophyAnnouncement: function(trophyName, descript, imageUrl)
	{
		var wholeWindowStr ='<div class="announce">'
						    + '<div class="announcement-trophy centered">'
						    	+  '<div class="announcement-pres">'
						    		+ '<img src="http://api.adorable.io/avatars/80">'
						    		+ '<h4 class = "media-heading">'+trophyName+'</h4>'
						    		+ '<p><b>'+descript+'</b></p>'
						    	+  '</div>'
						    	+  '<button class="btn btn-primary btn-lg pull-right">Next</button>'
						    +  '</div>'
						 +  '</div>';

		var wholeWindow = $(wholeWindowStr);
		wholeWindow.find('button').click(function(event){
			$(this).parent().parent().remove();
		});
		
		//$('#lobby_container').append(wholeWindow);
		this.trophyAnnouncements.push(wholeWindow);
	},

	showTrophyAnnouncements: function() 
	{
		if(this.trophyAnnouncements.length === 0) return;

		this.explainSuccess("Congrats, you earned another trophy!", 5000);

		for(var i = 0; i < this.trophyAnnouncements.length; i++)
			$('#rammi3 #lobby_container').append(this.trophyAnnouncements[i]);
		
		this.trophyAnnouncements = null;
		this.trophyAnnouncements = [];
	},

	displayPreMatchPresentation: function(playerOne, playerTwo)
	{
		//var randomOne = Math.floor(Math.random()*100);
		//var randomTwo = Math.floor(Math.random()*100);
		var imageUrlOne = "http://api.adorable.io/avatars/kalliderp";
		var imageUrlTwo = "http://api.adorable.io/avatars/42";
		
		var html =
				'<div class="announce">'
				+	'<div class="announcement-players centered">'
				+		'<div class="announcement-vs pull-left">'
				+			'<img src="'+imageUrlOne+'">'
				+			'<h4 class="media-heading text-center">'+playerOne+'</h4>'
				+		'</div>'
				+		'<div class="announcement-vs pull-right">'
				+			'<img src="'+imageUrlTwo+'">'
				+			'<h4 class="media-heading text-center">'+playerTwo+'</h4>'
				+		'</div>'
				+	'</div>'
				+ '</div>';   

		$('#rammi2 #game_container').append($(html));
	},

	displayEndGameWindow: function(winner, endedHow, regMult, cube)
	{
		var total = Number(regMult)*Number(cube);

		var winnerText = winner+" won the game";
		var bonusPoints = "Backgammon bonus multiplier: "+regMult;
		var cubeBonus = "The final value of the cube: "+cube;
		var pointsEarned = "Total points earned: "+ total;

		var html =
			'<div class="announce">'
			+	'<div class="announcement centered">'
			+		'<div class="centered">' 
			+			'<p>'+winnerText+'</p>'
			+			'<p>'+bonusPoints+'</p>'
			+			'<p>'+cubeBonus+'</p>'
			+			'<p>'+pointsEarned+'</p>'
			+		'</div>'
			+	'</div>'
			+ '</div>';

		$('#rammi2 #game_container').append($(html));
	},
	
	displayEndMatchWindow: function(winner, loser, winPoints, lossPoints)
	{
		var html = '<div class="announce">'
					+ '<div class="announcement centered">'
					   +  '<div class="centered">'
					   		+  '<p>'+"And the winner is: "+winner+'</p>'
					   		+  '<p>'+"The biggest loser is: "+loser+'</p>'
					   		+  '<p>'+"Total points for the winner: "+winPoints+'</p>'
					   		+  '<p>'+"Total points for the loser: "+lossPoints+'</p>'
						+  '</div>'
					+ '</div>'
				+  '</div>';

		var wholeWindow = $(html);
		$('#rammi2 #game_container').append(wholeWindow);
	},

	renderNewGameChatEntry: function(chatstring) 
	{
		var height = $('#lobby_chat_frame').height();
		$('#game_chat_frame').append($('<p>').text(chatstring).fadeIn(250).fadeOut(250).fadeIn(250)).scrollTop(2000);
	},

	clearMatchChat: function()
	{
		$('#game_chat_frame').empty();
	},

	//CHAT frá framenda er ÖFUGSNÚIÐ, bæði í nýjum skömmtum til ný logged in og ef einhver missir af einföldum skammti

	renderNewLobbyChatEntry: function(chatstring)
	{
		var height = $('#lobby_chat_frame').height();
		$('#lobby_chat_frame').append($('<p>').text(chatstring).fadeIn(250).fadeOut(250).fadeIn(250)).scrollTop(2000);
	},

	createWaitEntryString: function(username)  //idStr, playername, pointsToWin, clock
	{
		var clockSelection = $('#rammi3 #clock_select_online input:checked').val();
		var pointSelection = $('#rammi3 #point_select_online input:checked').val();
		return username+"_"+pointSelection+"_"+clockSelection;
	},

	createOfflineMatchEntryString: function(username)
	{
		var clockSelection = $('#rammi3 #clock_select_offline input:checked').val();
		var pointSelection = $('#rammi3 #point_select_offline input:checked').val();
		var botSelection =  $('#rammi3 #offline_diff input:checked').val();

		return username+"_"+botSelection+"_"+pointSelection+"_"+clockSelection;
	},
							
	//Tímasetningin á hvenær DELETE og CREATE fyrir sama waitinglist/ongoing entry lendir skiptir máli!

	addToWaitingList: function(idStr, playerName, points, clock) 
	{
		var belongsToUser = (playerName === request_controller.username);
		var buttonClass = (belongsToUser)? "btn btn-warning pull-right btn_cancel":"btn btn-success pull-right btn_join";
		var buttonText = (belongsToUser)? "Cancel": "Join" ;
		var settingsStr = "Points to win: "+points+"  "+clock;

		var html = 
			'<div class="well well-sm clearfix">'
			+	'<div class="well-content">'
			+  		'<h3 class="media-heading">'+playerName+'</h3>'
			+  		'<p>'+settingsStr+'</p>'
			+	'</div>'
			+	'<button class="'+buttonClass+'" id="'+idStr+'">'+buttonText+'</button>'
			+ '</div>';
					
		var waitingEntry = $(html);
		var button = waitingEntry.find('button');
		this.connectElemByClassToClickEvent(button);
		$('#rammi3 #waiting_list').append(waitingEntry);
	},

	addToOngoingList: function(idStr, pOne, pTwo, points, clock) 
	{
		var versusString = pOne+" Vs. "+pTwo;
		var settingsStr = "Points to win: "+points+"  "+clock;

		var html = 
			'<div class="well well-sm clearfix">'
			+	'<div class="well-content">'
			+		'<h3 class="media-heading">'+versusString+'</h3>'
			+		'<p>'+settingsStr+'</p>'
			+	'</div>'
			+	'<button class="btn btn-success pull-right btn_spec" id="'+idStr+'">Spectate</button>'
			+ '</div>'; 

		var ongoingEntry = $(html);
		var button = ongoingEntry.find('button');
		this.connectElemByClassToClickEvent(button);

		$('#rammi3 #ongoing_games').append(ongoingEntry);
	},

	connectElemByClassToClickEvent: function(element)
	{
		$(element).click(function(event){
        	request_controller.handleDOMEventsByElement($(this));
        });
	},

	switchToFrame: function(frameNum) 
	{
		for(var i = 1; i <= 4; i++)
			$("#rammi"+i).hide();
		$("#rammi"+frameNum).show();
	},

	deleteEntry: function(strId)
	{
		var buttonClicked = $('#'+strId);
		var wrapper = buttonClicked.parent();
		wrapper.remove();
	},
}

/*
	<div class="announce">
		<div class="announcement-trophy centered">
			<div class="announcement-pres">
				<img src="http://api.adorable.io/avatars/80">
				<h4 class="media-heading">First Point Earned!</h4>
				<p><b>Congrats, you earned a point!</b></p>
			</div>
			<button class="btn btn-primary btn-lg pull-right">Next</button>
		</div>
	</div>
  */

/*
	<div class="well well-lg clearfix">
		<div class="well-content">
			<h5 class="media-heading">VS HardBot</h5>
			<div class="progress">
				<div class="progress-bar progress-bar-info progress-bar-striped" role="progressbar" aria-valuenow="40"
				aria-valuemin="0" aria-valuemax="100" style="width:40%">
				40% 
				</div>
			</div>
		</div>
	</div> */
/*
	<div class="well well-lg clearfix">
		<div class="well-content">
			<h4 class="media-heading">Overall win statistics</h4>
			<div class="progress">
				<div class="progress-bar progress-bar-info progress-bar-striped" role="progressbar" aria-valuenow="40"
				aria-valuemin="0" aria-valuemax="100" style="width:40%">
				40% 
				</div>
			</div>
			<p>Pawns who made it: </p>
			<p class="text-center">1972</p>
			<p>Opponents defeated: </p>
			<p class="text-center">17</p>
		</div>
	</div> */




		
	