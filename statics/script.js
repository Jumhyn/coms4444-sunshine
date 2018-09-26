// ctx: drawing board.
// skill: list of skills of players.
// type: home/away
// name: group #
// text_pos: -1/+1 - text is above/below.
// x_start: x starting position of the drawing.
// y_start: y starting position of the drawing.
function drawPlayers(ctx, x_start, y_start, skills, type, name, score, text_pos, fill, isWin) {
    var x_off = 10;
    var y_off = 20;

    ctx.font = '20px Arial';
    ctx.fillStyle = 'black';

    for (var i=0; i<skills.length; ++i) {
        ctx.beginPath();
        ctx.arc(x_start + 5 + x_off, y_start-7, 5, 0*Math.PI, 2*Math.PI);

        if (fill[i]) {
            ctx.fill();
        }

        ctx.stroke();
        ctx.fillText(skills[i], x_start + x_off - 2, y_start + text_pos*y_off);
    
        x_off += 40;
    }

    if (isWin) {
        ctx.font = 'bold 20px Arial';
    }

    ctx.fillText(type + '(' + name + '): ' + score, x_start + x_off, y_start);
    ctx.stroke();
}

function drawBorder(ctx)
{
    ctx.beginPath();
    ctx.lineWidth="4";
    ctx.strokeStyle="black";
    ctx.rect(0,0,600,600);
    ctx.stroke();
}

function drawBales(ctx, bales, m) {
    for (var i = 0; i < bales.length; i++) {
        var bale = bales[i];
        //console.log(bale);
        var drawX = (bale.x / m) * 600;
        var drawY = (bale.y / m) * 600;
        //console.log(drawX);
        //console.log(drawY);
        ctx.beginPath();
        ctx.fillStyle="green";
        ctx.rect(drawX-2, drawY-2, 4, 4);
        ctx.fill();
    }
}

function drawTrailers(ctx, trailers, m) {
    for (var i = 0; i < trailers.length; i++) {
        var trailer = trailers[i];
        //console.log(bale);
        var drawX = (trailer.x / m) * 600;
        var drawY = (trailer.y / m) * 600;
        //console.log(drawX);
        //console.log(drawY);
        ctx.beginPath();
        ctx.fillStyle="blue";
        ctx.rect(drawX-5, drawY-2.5, 10, 5);
        ctx.fill();
    }
}

function drawTractors(ctx, tractors, m) {
    for (var i = 0; i < tractors.length; i++) {
        var tractor = tractors[i];
        //console.log(bale);
        var drawX = (tractor.x / m) * 600;
        var drawY = (tractor.y / m) * 600;
        //console.log(drawX);
        //console.log(drawY);
        
        if (tractor.dest != null)
        {
            var x_start = (tractor.x / m) * 600;
            var y_start = (tractor.y / m) * 600;
            
            var x_end = (tractor.dest.x / m) * 600;
            var y_end = (tractor.dest.y / m) * 600;
            
            ctx.strokeStyle="black";
            ctx.lineWidth="1";
            drawLine(ctx, x_start, y_start, x_end, y_end)
        }
        
        
        ctx.beginPath();
        ctx.fillStyle="red";
        ctx.rect(drawX-5, drawY-5, 10, 10);
        ctx.fill();
        
        if (tractor.trailer) {
            ctx.beginPath();
            ctx.fillStyle="blue";
            ctx.rect(drawX-5-10.0, drawY, 10, 5);
            ctx.fill();
        }
        
        if (tractor.bale) {
            ctx.beginPath();
            ctx.fillStyle="green";
            ctx.rect(drawX-2, drawY-2, 4, 4);
            ctx.fill();
        }
    }
}

function drawLine(ctx, x_start, y_start, x_end, y_end) {
    ctx.beginPath();
    ctx.moveTo(x_start, y_start);
    ctx.lineTo(x_end, y_end);
    ctx.stroke();
}

var y_pos = 40;

function process(data) {
    var result = JSON.parse(data)

    console.log(result);
    var refresh = parseFloat(result.refresh);
    var bales = result.bales;
    var trailers = result.trailers;
    var tractors = result.tractors;
    var m = result.m;
    var t = result.t;
    var elapsed = result.elapsed;
    
    canvas = document.getElementById('canvas');
    ctx = canvas.getContext('2d');
    
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    drawBorder(ctx);
    drawBales(ctx, bales, m);
    drawTrailers(ctx, trailers, m);
    drawTractors(ctx, tractors, m);
    
    timeElement = document.getElementById('time');
    timeElement.innerHTML = "Time: " + elapsed.toFixed(2) + "/" + t + " - Remaining bales: " + result.remaining_bales;

    return refresh;
}

var latest_version = -1;

function ajax(version, retries, timeout) {
    console.log("Version " + version);
    var xhttp = new XMLHttpRequest();
    xhttp.onload = (function() {
            var refresh = -1;
            try {
                if (xhttp.readyState != 4)
                    throw "Incomplete HTTP request: " + xhttp.readyState;
                if (xhttp.status != 200)
                    throw "Invalid HTTP status: " + xhttp.status;
                //console.log(xhttp.responseText);
                refresh = process(xhttp.responseText);
                if (latest_version < version)
                    latest_version = version;
                else refresh = -1;
            } catch (message) {
                alert(message);
            }

            console.log(refresh);
            if (refresh >= 0)
                setTimeout(function() { ajax(version + 1, 10, 100); }, refresh);
        });
    xhttp.onabort = (function() { location.reload(true); });
    xhttp.onerror = (function() { location.reload(true); });
    xhttp.ontimeout = (function() {
            if (version <= latest_version)
                console.log("AJAX timeout (version " + version + " <= " + latest_version + ")");
            else if (retries == 0)
                location.reload(true);
            else {
                console.log("AJAX timeout (version " + version + ", retries: " + retries + ")");
                ajax(version, retries - 1, timeout * 2);
            }
        });
    xhttp.open("GET", "data.txt", true);
    xhttp.responseType = "text";
    xhttp.timeout = timeout;
    xhttp.send();
}

ajax(1, 10, 100);

// process('{"refresh":0, "grp_a":"g1", "grp_b":"g2", "grp_a_round":"1,2,3,4,5", "grp_b_round":"4,5,6,7,8",' +
//    '"grp_a_skills":"4,3,2,5,6,7,3", "grp_b_skills":"2,3,5,7,5,4,3", "grp_a_dist":"1,2;4,3;6,7", "grp_b_dist":"3,4;5,1;8,7", ' +
//    '"grp_a_score":"3", "grp_b_score":"0"}');

