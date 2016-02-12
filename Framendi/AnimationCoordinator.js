var g_ID = 0;
var g_anim_flag = false;
/*
function AnimationCallback()
{
	var isAnimDone = g_animator.update();
	g_animator.render();
	
	if(isAnimDone)
	{
		clearInterval(g_ID);
		request_controller.animationHasStopped();
		g_animator.cleanUpAfterFinish();
		g_anim_flag = false;
	}
} */

function AnimationCoordinator()
{
	this.board = new BoardManager();
	this.moveList = [];
	this.currentAnimationId = 0;
	this.sequential = true;
	this.pawnsAreMoving = false;
	this.optionsAreWaiting = false;
}

AnimationCoordinator.prototype.buildCustomBoard = function(countList, teamList, diceVals, doubcube)
{
	this.board = new BoardManager();
	this.board.customizeBoard(countList, teamList, diceVals, doubcube);
	this.render();
};

AnimationCoordinator.prototype.switchToFreshBoard = function()
{
	this.board = new BoardManager();
};


AnimationCoordinator.prototype.userDoubling = function(newStakes)
{
	this.board.flipCube(newStakes);
	this.render();
};

AnimationCoordinator.prototype.opponentDoubling = function(newStakes)
{
	this.board.flipCube(newStakes);
	this.render();
}

AnimationCoordinator.prototype.highlightSquares = function(sqPos1, sqPos2)  
{
	this.board.unHighlightAll();
	if(sqPos1 >= 0)
		this.board.highlightSquare(sqPos1);
	if(sqPos2 >= 0) 
		this.board.highlightSquare(sqPos2);
	this.render();
};

AnimationCoordinator.prototype.unHighlightAll = function()
{
	this.board.unHighlightAll();
};

AnimationCoordinator.prototype.inTurnMove = function(from, to)
{
	this.pawnsAreMoving = true;
	this.board.unHighlightAll();
	this.board.highlightSquare(sqPos1);
	if(sqPos2 >= 0) 
		this.board.highlightSquare(sqPos2);
	this.render();
};

AnimationCoordinator.prototype.whiteDiceAnimation = function(valOne, valTwo)
{
	this.board.prepareDiceAnimation(valOne, valTwo, true);
	g_anim_flag = true;
};

AnimationCoordinator.prototype.blackDiceAnimation = function(valOne, valTwo)
{
	this.board.prepareDiceAnimation(valOne, valTwo, false);
	g_anim_flag = true;
};

AnimationCoordinator.prototype.receiveMovePackage = function(moveList) //moveList alltaf með length > 0?
{
	this.pawnsAreMoving = true;
	this.moveList = moveList;
	this.sequential = this.isSequenceNeeded();

	if(this.sequential)
	{
		this.board.resetAllMovementSettings({seq:true});
		this.setUpSequentialAnimation();
	}
	else
	{
		this.board.resetAllMovementSettings({seq:false});
		this.setUpBatchAnimation();
	}

	g_anim_flag = true;
};

AnimationCoordinator.prototype.setUpSequentialAnimation = function() //gerir ráð fyrir 1+ move í movelist
{
	this.currentAnimationId = 0;
	var move = this.getCurrentMove();
	this.board.setUpNextMove(move.from, move.to, move.killed, this.currentAnimationId);
	//það þarf annaðhvort að uppfæra þetta í hvert skipti í sequential í update eða láta .to bara duga 
	//í gegnum aðra aðferð en batch notar
};

AnimationCoordinator.prototype.setUpBatchAnimation = function()
{
	for(var i = 0; i < this.moveList.length; i++)
	{
		var move = this.moveList[i];
		if(!move.killed)
			this.board.setUpNextMove(move.from, move.to, move.killed, i);
	}
};

AnimationCoordinator.prototype.isSequenceNeeded = function()
{
	for(var i = 0; i < this.moveList.length; i++)
	{
		for(var j = 0; j < this.moveList.length; j++)
		{
			if(this.moveList[i].to === this.moveList[j].from)//óþarfi?
				return true;
		}
	}
	return false;
};

AnimationCoordinator.prototype.update = function(delta)
{
	if(this.sequential)
		this.sequentialUpdate(delta);
	else
		this.batchUpdate(delta);

	var othersFinished = this.board.updateOthersIfNeeded(delta);
	//console.log("Are Pawns Moving? "+ this.pawnsAreMoving);

	return !this.pawnsAreMoving && othersFinished;
};

AnimationCoordinator.prototype.sequentialUpdate = function(delta)
{
	var statusMessage = this.board.updateMovers(delta);
	if(!this.noMovesToFinish(statusMessage))
	{
		this.finishMove(this.currentAnimationId);
		this.currentAnimationId++;
		if(this.areMoreAnims())
		{
			var nextMove = this.getCurrentMove();
			this.board.setUpNextMove(nextMove.from, nextMove.to, nextMove.killed, this.currentAnimationId);
		}
		else 
			this.pawnsAreMoving = false;
	}
};

AnimationCoordinator.prototype.batchUpdate = function(delta)
{
	var statusArray = this.board.updateMovers(delta);
	if(this.noMovesToFinish(statusArray)) return;
	else 								  statusArray = this.extractStatusMessages(statusArray);

	for(var i = 0; i < statusArray.length; i++)
		this.finishMove(statusArray[i].id);

	for(var i = 0; i < statusArray.length; i++)
	{
		this.startKilledMoveIfPossible(statusArray[i].id); 
		this.markMoveAsFinished(statusArray[i].id);
	}

	if(!this.areMoreAnims())
		this.pawnsAreMoving = false;;
};

AnimationCoordinator.prototype.extractStatusMessages = function(statusArray)
{
	var realStatuses = [];
	for(var i = 0; i < statusArray.length; i++)
		if(statusArray[i])
			realStatuses.push(statusArray[i]);
	return realStatuses;
};

AnimationCoordinator.prototype.noMovesToFinish = function(statusArray)
{
	for(var i = 0; i < statusArray.length; i++)
		if(statusArray[i])
			return false;
	return true;
};

AnimationCoordinator.prototype.startKilledMoveIfPossible = function(id)
{
	var finMove = this.moveList[id];
	for(var i = 0; i < this.moveList.length; i++)
	{
		var move = this.moveList[i];
		if(move !== null && move.killed && move.from === finMove.to && !move.active)
		{
			move.active = true;
			this.board.setUpNextMove(move.from, move.to, move.killed, id);
			return;
		}
	}
};

AnimationCoordinator.prototype.showOptionsAfterAnimation = function()
{
	this.optionsAreWaiting = true;
};

AnimationCoordinator.prototype.cleanUpAfterFinish = function()
{
	this.moveList = null;
	this.currentAnimationId = 0;
	if(this.optionsAreWaiting)
	{
		dom_manipulator.showCubeAndDice();
		this.optionsAreWaiting = false;
	}
};

AnimationCoordinator.prototype.markMoveAsFinished = function(id)
{
	this.moveList[id] = null;
};

AnimationCoordinator.prototype.render = function()
{
	this.board.render(g_ctx);
};

AnimationCoordinator.prototype.finishMove = function(id)
{
	var move = this.getMoveWithId(id);
	this.board.finishMove(id, move.to);
};

AnimationCoordinator.prototype.getMoveWithId = function(id)
{
	return this.moveList[id];
};

AnimationCoordinator.prototype.areMoreAnims = function()
{
	return this.currentAnimationId < this.moveList.length && !this.isMoveListEmpty();
};
AnimationCoordinator.prototype.isMoveListEmpty = function()
{
	for(var i = 0; i < this.moveList.length; i++)
		if(this.moveList[i] !== null)
			return false;
	return true;
};
AnimationCoordinator.prototype.getCurrentMove = function()
{
	return this.moveList[this.currentAnimationId];
}
