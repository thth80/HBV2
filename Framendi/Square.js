
function Square(pos, cx)
{
	this.pawns = [];
	this.cx = cx;
	this.pointsDown = (pos < 13)? true : false;
	this.bottomY = (this.pointsDown)? HEIGHT*0.039 : HEIGHT*0.961;
	this.highlighted = false;
	this.id = pos;
}

Square.prototype.getCount = function()
{
	return this.pawns.length;
};
Square.prototype.setBottom = function(bot)
{
	this.bottomY = bot;
};
Square.prototype.pointDown = function()
{
	this.pointsDown = true;
};

Square.prototype.referenceOnlyPawn = function()
{
	return this.pawns[0];
};

Square.prototype.removePawn = function()
{
	var oldPawn = this.pawns[this.getCount()-1];
	this.pawns.splice(this.getCount()-1, 1);
	return oldPawn;
};

Square.prototype.addPawn = function(pawn) 
{										
	if(this.pawns.length == 1 && pawn.team != this.pawns[0].team)
	{
		pawn.placeAt(this.getNextParkingCoords(pawn.team));
		this.pawns.splice(0,1);
		this.pawns.push(pawn);
	}
	else
	{
		pawn.placeAt(this.getNextParkingCoords(pawn.team));
		this.pawns.push(pawn);
	}
};

Square.prototype.getNextParkingCoords = function(team) //Ath töfrafastar sem eiga að tákna peð radíus
{
	var offset = (this.willKillHappen(team))?  0 : this.getCount();
	var startingSpot = (this.pointsDown)? this.bottomY+30: this.bottomY-30;

	if(offset >= 8)
	{
		startingSpot = (this.pointsDown)? 90 : HEIGHT - 90; 
		offset -= 8;
	}
	else if(offset >= 5)
	{
		startingSpot = (this.pointsDown)? 75: HEIGHT - 75 ;
		offset -= 5;
	}

	var offsetMovement = (this.pointsDown)? offset*60 : offset*-60 ;
	return {cx: this.cx, cy: startingSpot + offsetMovement};
};

Square.prototype.willKillHappen = function(team)
{
	return this.getCount() === 1 && team !== this.pawns[0].team;
};

Square.prototype.highlight = function()
{
	this.highlighted = true;
};
Square.prototype.unHighlight = function()
{
	this.highlighted = false;
};
	
Square.prototype.render = function(ctx)
{
	for(var i = 0; i< this.pawns.length; i++)
		this.pawns[i].render(ctx);

	if(this.highlighted)
	{
		ctx.beginPath();
		var oldstyle = ctx.fillStyle;
		ctx.fillStyle = 'rgba(183, 247, 99, 0.47)';
		var top = (this.pointsDown)? this.bottomY: this.bottomY - SQ_H;
		ctx.fillRect(this.cx-SQ_W/2, top, SQ_W, SQ_H);

		ctx.fillStyle = oldstyle;

		ctx.beginPath();
		ctx.rect(this.cx-SQ_W/2, top, SQ_W, SQ_H);
		ctx.stroke();
	}
};