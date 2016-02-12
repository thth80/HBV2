function goingSouth(pawnCoords, targetCoords)
{
	return pawnCoords.cy >= targetCoords.cy;
}
function goingNorth(pawnCoords, targetCoords)
{
	return pawnCoords.cy <= targetCoords.cy;
}
function goingWest(pawnCoords, targetCoords)
{
	return pawnCoords.cx <= targetCoords.cx;
}
function goingEast(pawnCoords, targetCoords)
{
	return pawnCoords.cx >= targetCoords.cx;
}
function movingInBothAxes(pawnCoords, targetCoords)
{
	var xDistSquared = Math.pow(Math.abs(pawnCoords.cx - targetCoords.cx),2);
	var yDistSquare = Math.pow(Math.abs(pawnCoords.cy - targetCoords.cy),2);
	var realDistSquared = xDistSquared + yDistSquare;
	return realDistSquared < 100;
}

var STANDARD_LINE = 0, JUMPER = 1, TELEPORT = 2, STANDARD_CHARGER = 3, LAUNCHER = 4;
function PawnMover()
{
	this.id = -1;
	this.movingPawn = null;
	this.currentTargetId = -1;
	this.updateProtocols = [JUMPER, JUMPER, JUMPER, JUMPER];

	this.assessFunction  = null;
	this.targetsOnTheWay = null;
	this.isActive = false;
}

PawnMover.prototype.setAllProtocolsToTeleport = function()
{
	for(var i = 0; i < this.updateProtocols.length; i++)
		this.updateProtocols[i] = TELEPORT;
};

PawnMover.prototype.resetAllMovementSettings = function()
{
	this.updateProtocols[0] = this.selectRegProtocol();
	this.updateProtocols[1] = this.selectKillProtocol();
	this.updateProtocols[2] = this.selectKilledProtocol();
	this.updateProtocols[3] = this.selectHomeProtocol();

	//$('#outputter').remove('#derp').append($('<p></p>').text("SELECTED Protocol is: "+ this.updateProtocols[0]));

	return this.updateProtocols;
};

PawnMover.prototype.selectRegProtocol = function()
{
	var random = Math.random();
	return LAUNCHER;
	if(random < 0.2)
		return STANDARD_LINE;
	else if(random < 0.4)
		return JUMPER;
	else if(random < 0.55)
		return TELEPORT; 
	else
		return LAUNCHER;
};
PawnMover.prototype.selectKillProtocol = function()
{
	var random = Math.random();
	return JUMPER;
};
PawnMover.prototype.selectKilledProtocol = function()
{
	var random = Math.random();
	return JUMPER;
};
PawnMover.prototype.selectHomeProtocol = function()
{
	var random = Math.random();
	return JUMPER;
};

PawnMover.prototype.setProtocols = function(settingArray)
{
	for(var i = 0; i < settingArray.length; i++)
		this.updateProtocols[i] = settingArray[i];
};

PawnMover.prototype.receiveDirections = function(pawn, target, killed, id)
{
	this.activate();
	this.currentTargetId = 0;
	this.movingPawn = pawn;
	this.id = id;
	this.assessFunction = this.getAssessFunc(target);
	this.initTargetList(target);
};

PawnMover.prototype.initTargetList = function(targetCoords)
{
	if(this.updateProtocols[0] === STANDARD_LINE)
		this.initStandardLineMove(targetCoords);
	else if(this.updateProtocols[0] === JUMPER)
		this.initStandardJumper(targetCoords);
	else if(this.updateProtocols[0] === TELEPORT)
		this.initTeleport(targetCoords);
	else if(this.updateProtocols[0] === STANDARD_CHARGER)
		this.initStandardCharger(targetCoords);
	else if(this.updateProtocols[0] === LAUNCHER)
		this.initVariableLauncher(targetCoords);
};

PawnMover.prototype.update = function(delta)
{
	if(this.updateProtocols[0] === STANDARD_LINE)
		return this.updateStandardLineMove(delta);
	else if(this.updateProtocols[0] === JUMPER)
		return this.updateStandardJumper(delta);
	else if(this.updateProtocols[0] === TELEPORT)
		return this.updateTeleport(delta);
	else if(this.updateProtocols[0] === STANDARD_CHARGER)
		return this.updateStandardCharger(delta);
	else if(this.updateProtocols[0] === LAUNCHER)
		return this.updateVariableLauncher(delta);
};

PawnMover.prototype.initTeleport = function(t)
{
	this.movingPawn.placeAt(t);
};

PawnMover.prototype.updateTeleport = function(delta)
{
	return {id: this.id};
};

PawnMover.prototype.initVariableLauncher = function(t)
{
	this.gravity = -0.00003;
	var zLaunchVel = 0.011 + Math.random()*0.003;
	var timeToLandAgain = -2*(zLaunchVel/this.gravity);
	
	var xVector = t.cx - this.movingPawn.cx;
	var yVector = t.cy - this.movingPawn.cy;
	var neededXVel = xVector/timeToLandAgain;
	var neededYVel = yVector/timeToLandAgain;
	this.movingPawn.applyForces({x: neededXVel, y: neededYVel});
	this.movingPawn.verticalForce(zLaunchVel);
	this.targetsOnTheWay = [];
	this.targetsOnTheWay[0] = t;
};

PawnMover.prototype.updateVariableLauncher = function(delta)
{
	this.movingPawn.updatePos(delta);
	this.movingPawn.verticalForce(this.gravity*delta);

	if(this.assessFunction(this.movingPawn.getCoords(), this.getCurrentTarget()))
	{
		this.movingPawn.ground();
		return {id: this.id};
	}
	else 
		return false;
};

PawnMover.prototype.initStandardCharger = function(t)
{
	var xVector = t.cx - this.movingPawn.cx;
	var yVector = t.cy - this.movingPawn.cy;
	var stopForceSpot = {cx: this.movingPawn.cx + xVector/8, cy: this.movingPawn.cy + yVector/8};
	this.targetsOnTheWay = [];
	this.targetsOnTheWay.push(stopForceSpot);
	this.targetsOnTheWay.push(t);


	this.movingPawn.applyForces({y: -yVector/300, x: -xVector/300});
	this.force = {y: yVector/30000, x: xVector/30000};
};

PawnMover.prototype.updateStandardCharger = function(delta)// MUNA AÐ nota alltaf deltuna til að fá smooth teikningar
{
	this.movingPawn.updatePos(delta);

	if(this.assessFunction(this.movingPawn.getCoords(), this.getCurrentTarget()))
	{
		this.currentTargetId++;
		if(this.currentTargetId === 1)
			this.force = {x: -this.force.x/3.2, y: -this.force.y/3.2};
		else
			return {id: this.id};
	}
	else
	{	
		this.movingPawn.applyForces({x: this.force.x*delta, y: this.force.y*delta});
		return false;
	}

};

PawnMover.prototype.initStandardJumper = function(t)
{
	var p = this.movingPawn;
	var apexZ = 4.0;
	var zVel = 0.003;
	var timeToApex = (apexZ-p.z)/zVel;
	var midXVector = (t.cx - p.cx)/2;
	var midYVector = (t.cy - p.cy)/2;

	var xForce = midXVector/timeToApex;
	var yForce = midYVector/timeToApex;
	this.targetsOnTheWay = [];
	this.targetsOnTheWay[0] = {cx: p.cx + midXVector, cy: p.cy + midYVector};
	this.targetsOnTheWay[1] = t;
	this.movingPawn.applyForces({x: xForce, y: yForce});
	this.movingPawn.verticalForce(zVel);
};

PawnMover.prototype.updateStandardJumper = function(delta)
{
	this.movingPawn.updatePos(delta);
	if(this.assessFunction(this.movingPawn.getCoords(), this.getCurrentTarget()))
	{
		this.currentTargetId++;
		if(this.noMoreTargets())
		{
			this.movingPawn.ground();
			return {id: this.id};
		}
		else 
		{
			this.movingPawn.apex();
			return false;
		}
	}
	else
		return false;
};

PawnMover.prototype.initStandardLineMove = function(targetCoords)
{
	this.targetsOnTheWay = [];
	this.targetsOnTheWay[0] = targetCoords;
	
	var xVector = targetCoords.cx - this.movingPawn.cx;
	var yVector = targetCoords.cy - this.movingPawn.cy;
	this.movingPawn.applyYForce(yVector/1000);
	this.movingPawn.applyXForce(xVector/1000);
};

PawnMover.prototype.updateStandardLineMove = function(delta)
{
	this.movingPawn.updatePos(delta);
	if(this.assessFunction(this.movingPawn.getCoords(), this.getCurrentTarget()))
		return {id: this.id};
	else
		return false;
};


PawnMover.prototype.shutDownAndReleasePawn = function()
{
	this.movingPawn.halt();
	this.isActive = false;
	this.currentTargetId = 0
	this.assessFunction  = null;
	this.targetsOnTheWay = null;
	this.id = -1;

	var pawnCopy = this.movingPawn;
	this.movingPawn = null;
	return pawnCopy;
};

PawnMover.prototype.isItActive = function()
{
	return this.isActive;
};
PawnMover.prototype.activate = function()
{
	this.isActive = true;
};

PawnMover.prototype.getCurrentTarget = function()
{
	return this.targetsOnTheWay[this.currentTargetId];
};

PawnMover.prototype.noMoreTargets = function()
{
	return this.currentTargetId >= this.targetsOnTheWay.length || this.isTargetListEmpty();
};
PawnMover.prototype.isTargetListEmpty = function()
{
	for(var i = 0; i < this.targetsOnTheWay.length; i++)
		if(this.targetsOnTheWay[i] !== null)
			return false;
	return true;
};

PawnMover.prototype.getAssessFunc = function(targetCoords)
{
	var xVector = targetCoords.cx - this.movingPawn.cx;
	var yVector = targetCoords.cy - this.movingPawn.cy;

	if(Math.abs(xVector) < 3 && yVector > 0)
		return goingSouth;
	else if(Math.abs(xVector) < 3 && yVector < 0)
		return goingNorth;
	else if(xVector > 0 && Math.abs(yVector) < 3)
		return goingEast;
	else if(xVector < 0 && Math.abs(yVector) < 3)
		return goingWest;
	else 
		return movingInBothAxes;
};

PawnMover.prototype.render = function(ctx)
{
	var p = this.movingPawn;
	ctx.save();
	ctx.translate(p.cx, p.cy);
	ctx.scale(p.z, p.z)

	ctx.drawImage(this.movingPawn.sprite.image,-29.5,-29.5, 59,59);
	ctx.drawImage(shine.image, -29.5, -29.5, 59, 59);
	ctx.restore();
}