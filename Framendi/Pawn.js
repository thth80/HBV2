function Pawn(team)
{
	this.sprite = (team === TEAM_BL)? blackPawn: whitePawn;
	this.team = team;
	this.cx = 0;
	this.cy = 0;
	this.z = 1.0;
	this.xVel = 0;
	this.yVel = 0;
	this.zVel = 0;
}

Pawn.prototype.updatePos = function(timedelta)
{
	this.cx += timedelta*this.xVel;
	this.cy += timedelta*this.yVel;
	this.z += timedelta*this.zVel;
};

Pawn.prototype.ground = function()
{
	this.z = 1.0;
};

Pawn.prototype.magnify = function(percentage)
{
	this.xVel*= 1.1;
	this.yVel*= 1.1;
};
Pawn.prototype.halt = function()
{
	this.xVel = this.yVel = this.zVel = 0;
};

Pawn.prototype.apex = function()
{
	this.zVel *= -1;
};

Pawn.prototype.placeAt = function(coords)
{
	this.cy = coords.cy;
	this.cx = coords.cx;
};

Pawn.prototype.getCoords = function()
{
	return {cx: this.cx, cy: this.cy};
};

Pawn.prototype.setStartVel = function(velX, velY)
{
	this.yVel = velY;
	this.xVel = velX;
};

Pawn.prototype.verticalForce = function(z)
{
	this.zVel += z;	
};

Pawn.prototype.applyForces = function(forces)
{
	this.xVel += forces.x;
	this.yVel += forces.y;
};

Pawn.prototype.applyXForce = function(force)
{
	this.xVel += force;
};
Pawn.prototype.applyYForce = function(force)
{
	this.yVel += force;
};

Pawn.prototype.render = function(ctx) //áður var teiknað frá px3 - px 197
{
	ctx.drawImage(this.sprite.image,this.cx-29.5,this.cy-29.5, 59,59);
	ctx.drawImage(shine.image, this.cx-29.5, this.cy-29.5, 59, 59 );
};