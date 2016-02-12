var TEAM_WH = 0, TEAM_BL = 1, TEAM_NONE = 2;
function BoardManager()   
{
	this.squares = [];   
	this.homelessPawns = [];
	for(var i = 0; i <= 27; i++)
		this.homelessPawns[i] = null;

	this.pawnMovers = [new PawnMover(), new PawnMover(), new PawnMover(), new PawnMover()];
	this.doublingCube = new Cube(1);	
	this.dicePairOne = new DicePair(TEAM_WH);
	this.dicePairTwo = new DicePair(TEAM_BL); 
	this.batchMode = false;

	this.addSquares();
	this.installPawns();
}

BoardManager.prototype.updateOthersIfNeeded = function(delta)
{
	if(this.dicePairOne.animating) this.dicePairOne.update(delta);
	if(this.dicePairTwo.animating) this.dicePairTwo.update(delta);

	return !this.dicePairTwo.animating && !this.dicePairOne.animating;
};

BoardManager.prototype.customizeBoard = function(countList, teamList, diceVals, doubCube)
{
	this.dicePairOne = new DicePair(TEAM_WH);
	this.dicePairOne.setBoth(diceVals[0], diceVals[1]);
	this.dicePairTwo = new DicePair(TEAM_BL);
	this.dicePairTwo.setBoth(diceVals[2], diceVals[3]);

	this.doublingCube = new Cube(1);
	this.squares = null;
	this.squares = [];
	this.addSquares();
	this.installCustomPawns(countList, teamList);
};

BoardManager.prototype.installCustomPawns = function(countList, teamList)
{
	
	for(var i = 0; i < countList.length; i++)
	{
		var count = countList[i];
		if(count === 0)	continue;

		var currentSquare = this.squares[i];
		var team = teamList[i];

		for(var j = 0; j < count; j++)
			currentSquare.addPawn(new Pawn(team));
	}
};

BoardManager.prototype.updateMovers = function(delta)
{
	var pMoverStatuses = this.updateActivePawnMovers(delta);
	return pMoverStatuses;
};

BoardManager.prototype.flipCube = function(newStakes)
{
	this.doublingCube = newStakes;
};

BoardManager.prototype.animateCube = function(newStakes)
{

};

BoardManager.prototype.prepareDiceAnimation = function(valOne, valTwo, isWhite)
{
	if(isWhite) this.dicePairOne.prepareForAnimation(valOne, valTwo);
	else		this.dicePairTwo.prepareForAnimation(valOne, valTwo);
};

BoardManager.prototype.updateActivePawnMovers = function(delta)
{
	var statusMessages = [];
	for(var i = 0; i < this.pawnMovers.length; i++)
		if(this.pawnMovers[i].isItActive())
			statusMessages.push(this.pawnMovers[i].update(delta));

	return statusMessages;
};

BoardManager.prototype.getMoverById = function(id)
{
	for(var i = 0; i < this.pawnMovers.length; i++)
		if(this.pawnMovers[i].id === id)
			return this.pawnMovers[i];
};

BoardManager.prototype.finishMove = function(id, to)
{
	var parkingPawn = this.getMoverById(id).shutDownAndReleasePawn();
	this.getSquare(to).addPawn(parkingPawn); 
};

BoardManager.prototype.getSquare = function(id)
{
	return this.squares[id];
};

BoardManager.prototype.unHighlightAll = function()
{
	for(var i = 0; i < 28; i++) this.getSquare(i).unHighlight();
};
BoardManager.prototype.highlightSquare = function(sqPos)
{
	this.getSquare(sqPos).highlight();
};


BoardManager.prototype.findNextAvailableMover = function()
{
	if(!this.batchMode) return this.pawnMovers[0];
	for(var i = 0; i < this.pawnMovers.length; i++)
		if(!this.pawnMovers[i].isItActive())
			return this.pawnMovers[i];
};

BoardManager.prototype.setUpNextMove = function(from, to, killed, moveId)
{
	var removedPawn = this.pickUpDepartingPawn(from);
	if(killed)
		this.homelessPawns[to] = this.getSquare(to).referenceOnlyPawn();

	if(!removedPawn)
	{
		console.log("was this killed?: "+ killed);
		console.log("from = "+from+" to = "+to);
	}
	var landingCoords = this.getSquare(to).getNextParkingCoords(removedPawn.team);

	this.findNextAvailableMover().receiveDirections(removedPawn, landingCoords, killed, moveId);
};

BoardManager.prototype.pickUpDepartingPawn = function(departingFrom)
{
	if(this.homelessPawns[departingFrom] !== null)
	{
		var homelessPawn = this.homelessPawns[departingFrom];
		this.homelessPawns[departingFrom] = null;
		return homelessPawn;
	}
	else
		return this.getSquare(departingFrom).removePawn();
};

BoardManager.prototype.setToInGameMoves = function()
{
	this.batchMode = false;
	this.pawnMovers[0].setAllProtocolsToTeleport();
};

BoardManager.prototype.resetAllMovementSettings = function(msg)
{
	this.batchMode = !msg.seq;
	if(!this.batchMode)
		this.pawnMovers[0].resetAllMovementSettings();
	else
	{
		var settingArray = this.pawnMovers[0].resetAllMovementSettings();
		for(var i = 1; i < this.pawnMovers.length; i++)
			this.pawnMovers[i].setProtocols(settingArray);
	}
};

BoardManager.prototype.render = function(ctx)
{
	ctx.drawImage(gameBoard.image, 0,0, WIDTH, HEIGHT);
	for(var i = 0; i < 28; i++)
	{
		//this.squares[i].highlight();
		this.squares[i].render(ctx);
	}

	this.dicePairOne.render(ctx);
	this.dicePairTwo.render(ctx);
	this.doublingCube.render(ctx);
	this.renderActiveMovers(ctx);

};

BoardManager.prototype.renderActiveMovers = function(ctx)
{
	for(var i = 0; i < this.pawnMovers.length; i++)
		if(this.pawnMovers[i].isItActive())
			this.pawnMovers[i].render(ctx);
};

BoardManager.prototype.addSquares = function()
{
	var j, cx;
	for(var i = 1; i< 25; i++)
	{
		if(i < 7 || i > 18)
		{
			j = (i < 7)? i - 1: 24 - i;
			cx = WIDTH- j*SQ_W - 0.133*WIDTH;
		}
		else
		{
			j = (i <= 12)? 12 - i: Math.abs(13-i);
			cx = 0.102*WIDTH+ j*SQ_W + SQ_W*0.5;
		}
		this.squares[i] = new Square(i,cx);
	}

	var whiteDead = new Square(25, WIDTH*0.5);
	var blackDead = new Square(0, WIDTH*0.5);
	whiteDead.setBottom(HEIGHT*0.8);
	blackDead.setBottom(HEIGHT*0.2);

	this.squares[25] = whiteDead;
	this.squares[0] = blackDead;

	var whiteEnd = new Square(26, WIDTH*0.94);
	whiteEnd.pointDown();
	whiteEnd.setBottom(HEIGHT*0.039);
	var blackEnd = new Square(27, WIDTH*0.94);

	this.squares[26] = whiteEnd;
	this.squares[27] = blackEnd;
	
	request_controller.setBoundingBoxes(this.squares, SQ_W, SQ_H);
};


BoardManager.prototype.installPawns = function()
{
	var blackTower1 = this.squares[19];
	var blackTower2 = this.squares[12];
	var whiteTower1 = this.squares[6];
	var whiteTower2 = this.squares[13];
	for(var i = 0; i< 5; i++)
	{
		blackTower1.addPawn(new Pawn(TEAM_BL));
		blackTower2.addPawn(new Pawn(TEAM_BL));
		whiteTower1.addPawn(new Pawn(TEAM_WH));
		whiteTower2.addPawn(new Pawn(TEAM_WH));
	}

	var blackTrips = this.squares[17];
	var whiteTrips = this.squares[8];
	for(var i = 0; i < 3; i++)
	{
		blackTrips.addPawn(new Pawn(TEAM_BL));
		whiteTrips.addPawn(new Pawn(TEAM_WH));
	}
	var blackRunners = this.squares[1];
	var whiteRunners = this.squares[24];
	for(var i = 0; i< 2; i++)
	{
		blackRunners.addPawn(new Pawn(TEAM_BL));
		whiteRunners.addPawn(new Pawn(TEAM_WH));
	}
};