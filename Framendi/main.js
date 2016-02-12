
var main = 
{
    frameTimeMs : null,
    frameTimeDeltaMs : null,
    timeUntilNextRefresh: 2000,
};

main.updateClocks = function (frameTime) {
    
    if (this.frameTimeMs === null) this.frameTimeMs = frameTime;
    
    this.frameTimeDeltaMs = frameTime - this.frameTimeMs;
    this.frameTimeMs = frameTime;
};

main.oneIteration = function (frameTime) 
{
    this.updateClocks(frameTime);
    this.timeUntilNextRefresh -= this.frameTimeDeltaMs;
    if(this.timeUntilNextRefresh <= 0)
    {
        this.timeUntilNextRefresh = 2000;
        refreshCallback();
    }

    if(this.frameTimeDeltaMs > 50) 
    {
        this.frameTimeDeltaMs = 16;
        console.log("Delta is: "+this.frameTimeDeltaMs);
    }

    if(g_anim_flag)
    {
        var didAnimJustFinish = this.animIteration(this.frameTimeDeltaMs);
        if (didAnimJustFinish) 
        { 
            g_anim_flag = false;
            request_controller.animationHasStopped();
            g_animator.cleanUpAfterFinish();
        }
    }

    this.requestNextIteration();
};

main.animIteration = function (dt) 
{       
    var isAnimDone = g_animator.update(dt);
    g_animator.render();
    return isAnimDone;
    
};

window.requestAnimationFrame = 
    window.requestAnimationFrame ||        // Chrome
    window.mozRequestAnimationFrame ||     // Firefox
    window.webkitRequestAnimationFrame;    // Safari

function mainIterFrame(frameTime) {
    main.oneIteration(frameTime);
}

main.requestNextIteration = function () {
    window.requestAnimationFrame(mainIterFrame);
};

main.init = function () 
{
    this.requestNextIteration();
};
