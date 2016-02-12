function DicePair(team)
{
	this.diceSprite = diceSprite;
	this.isWhite = (team === 0);
	this.first = 1;
	this.second = 1;
	this.firstCopy = this.first;
	this.secondCopy = this.second;

	this.animating = false;
	this.remainingTimeInMs = 0;
	this.timeBetweenFlips = 80;
}

DicePair.prototype.setBoth = function(vOne, vTwo)
{
	this.first = vOne;
	this.second = vTwo;
};

DicePair.prototype.setFirst = function(val)
{
	this.first = val;
};

DicePair.prototype.setSecond = function(val)
{
	this.second = val;
};

DicePair.prototype.update = function(delta)
{
	var oldFlipsLeft = Math.floor(this.remainingTimeInMs/this.timeBetweenFlips);
	this.remainingTimeInMs -= delta;
	var currentFlipsLeft = Math.floor(this.remainingTimeInMs/this.timeBetweenFlips);

	if(oldFlipsLeft > currentFlipsLeft)
	{
		var oldFirst = this.first;
		var oldSecond = this.second;
		while(oldFirst === this.first)
			this.first = 1 + Math.floor(Math.random()*6);
		while(oldSecond === this.second)
			this.second = 1 + Math.floor(Math.random()*6); 
	}

	if(this.remainingTimeInMs <= 0)
	{
		this.animating = false;
		this.first = this.firstCopy;
		this.second = this.secondCopy;
	}

};

DicePair.prototype.prepareForAnimation = function(first, second)
{
	this.animating = true;
	this.remainingTimeInMs = 1350;
	this.firstCopy = first;
	this.secondCopy = second;
	this.first = 1 + Math.floor(Math.random()*6);
	this.second = 1 + Math.floor(Math.random()*6);
};

DicePair.prototype.render = function(ctx)
{
	var LEFT_START = 29;
	var SPR_W = 59;
	var OFFSET = 63;
	var HOFF = 79;
	var SIDE_LEN = WIDTH*0.1;

	var offset1 = (this.first-1)*OFFSET;
	var offset2 = (this.second-1)*OFFSET;

	if(this.isWhite)
	{
		ctx.drawImage(this.diceSprite.image, LEFT_START+offset1, HOFF, SPR_W, SPR_W, 
			(WIDTH*0.2), (HEIGHT*0.5)-(SIDE_LEN*0.5), SIDE_LEN,SIDE_LEN);
		ctx.drawImage(this.diceSprite.image, LEFT_START+offset2, HOFF, SPR_W, SPR_W, 
			(WIDTH*0.2)+SIDE_LEN, (HEIGHT*0.5)-(SIDE_LEN*0.5), SIDE_LEN,SIDE_LEN);
	}
	else
	{
		ctx.drawImage(this.diceSprite.image, LEFT_START+offset1, HOFF+OFFSET, SPR_W, SPR_W, 
			(WIDTH*0.625), (HEIGHT*0.5)-(SIDE_LEN*0.5), SIDE_LEN,SIDE_LEN);
		ctx.drawImage(this.diceSprite.image, LEFT_START+offset2, HOFF+OFFSET, SPR_W, SPR_W,
		 (WIDTH*0.625)+SIDE_LEN, (HEIGHT*0.5)-(SIDE_LEN*0.5), SIDE_LEN,SIDE_LEN);
	}
};