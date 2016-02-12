function Cube(value) //value = 1-6
{
	this.value = value;
	this.sprite = doubCube;
}

Cube.prototype.render = function(ctx) //3184 * 592
{
	var midHeight = HEIGHT*0.5;
	var leftImageStart = 50;
	var imageStartGap = 400; 
	var valueOffset = (this.value-1)*imageStartGap;
	var imageTop = 50;
	var cubeSideLength = 480;
	var onCanvasSideLength = HEIGHT*0.088;

	ctx.drawImage(this.sprite.image, leftImageStart+valueOffset, imageTop, cubeSideLength, cubeSideLength, 
			(WIDTH*0.028), (HEIGHT*0.5)-(onCanvasSideLength*0.5), onCanvasSideLength ,onCanvasSideLength);
	
};