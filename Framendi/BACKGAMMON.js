var g_canvas = document.getElementById('mycanvas');
var g_ctx = g_canvas.getContext("2d");
var HEIGHT = 768, WIDTH = 1024;
var SQ_H = HEIGHT*0.37, SQ_W = WIDTH*0.061;

var g_animator = null;

function connectInputs()
{
    dom_manipulator.switchToFrame(1);

    g_animator = new AnimationCoordinator();
    var toHide = ["#throw_dice","#end_turn","#double_offer","#accept_offer", "#reject_offer","#leave_decide","#info_banner","#success_banner"];
    for(var i = 0; i < toHide.length; i++)
        $(toHide[i]).hide();

    $(window).on('beforeunload', function()
    {
        var url = "http://localhost:9090/leaveProgram?name="+request_controller.username;
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
    })

    $('#mycanvas').click(function(event){
        var mouseX = event.clientX - g_canvas.offsetLeft;
        var mouseY = event.clientY - g_canvas.offsetTop;
        request_controller.handleCanvasEvents(mouseX, mouseY);
    });

    $('#leave_match').click(function(event){
        event.preventDefault();
        request_controller.handleDOMEventsById("leave_match");
    });
    $('#confirm_leave').click(function(event){
        event.preventDefault();
        request_controller.handleDOMEventsById("confirm_leave");
    });
    $('#cancel_leave').click(function(event){
        event.preventDefault();
        request_controller.handleDOMEventsById("cancel_leave");
    });

    $('#signup').submit(function(event){
        event.preventDefault();
        request_controller.handleDOMEventsById("signup");
    });
     $('#login').submit(function(event){
        event.preventDefault();
        request_controller.handleDOMEventsById("login");
    });
     $('#end_turn').click(function(event){
        event.preventDefault();
        request_controller.handleDOMEventsById("end_turn");
    });
    $('#throw_dice').click(function(event){
        event.preventDefault();
        request_controller.handleDOMEventsById("throw_dice");
    });
     $('#double_offer').click(function(event){
        event.preventDefault();
        request_controller.handleDOMEventsById("double_offer");
    });

    $('#trophy_room').click(function(event){
        event.preventDefault();
        request_controller.handleDOMEventsById("trophy_room");
    });

    $('#game_lobby').click(function(event){
        event.preventDefault();
        request_controller.handleDOMEventsById("game_lobby");
    });

    $('#add_wait_entry').click(function(event){
        event.preventDefault();
        request_controller.handleDOMEventsById("add_wait_entry");
    });

    $('#lobby_chat_entry').submit(function(event){
        event.preventDefault();
        request_controller.handleDOMEventsById("lobby_chat_entry");
    })

    $('#game_chat_entry').submit(function(event){
        event.preventDefault();
        request_controller.handleDOMEventsById("game_chat_entry");
    })

    $('.join_online').click(function(event){
        event.preventDefault();
        request_controller.handleDOMEventsByElement($(this));
    });
    $('.observe_match').click(function(event){
        event.preventDefault();
        request_controller.handleDOMEventsByElement($(this));
    });

    $('.cancel_wait').click(function(event){
        event.preventDefault();
        request_controller.handleDOMEventsByElement($(this));
    });

    $('#join_offline').click(function(event){
            event.preventDefault();
            request_controller.handleDOMEventsById("join_offline");
        });


     $('#end_turn').hide();
     $('#throw_dice').hide();

    function handleMouseDown(evt)
    {
        var mouseX = evt.clientX - g_canvas.offsetLeft;
        var mouseY = evt.clientY - g_canvas.offsetTop;
        request_controller.translateInputCoords(mouseX, mouseY);
    }

    g_animator.render();
}

// =============
// PRELOAD STUFF
// =============

var g_images = {};

function requestPreloads() 
{
    var requiredImages = {

        gameBoard: "images/board1.png",
        clockim: "images/flip_clock.png",
        nums: "images/numbers.png",
        black: "images/black.jpg",
        white: "images/white.jpg",
        diceSprite: "images/dice.jpg",
        shine: "images/shine.png",
        doubCube: "images/doubleCubev3.png",
    };

    imagesPreload(requiredImages, g_images, preloadDone);
}

var g_sprites = {};

function preloadDone() {

    gameBoard = new Sprite(g_images.gameBoard);
    clockim = new Sprite(g_images.clockim);
    nums = new Sprite(g_images.nums);
    blackPawn = new Sprite(g_images.black);
    whitePawn = new Sprite(g_images.white);
    diceSprite = new Sprite(g_images.diceSprite);
    shine = new Sprite(g_images.shine);
    doubCube = new Sprite(g_images.doubCube);
    connectInputs();
}

requestPreloads();