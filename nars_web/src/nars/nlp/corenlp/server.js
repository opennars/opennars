/* Simple Sanford CoreNLP Server
 * 
 * To setup a CoreNLP Server:
 *  1. extract corenlp to a directory
 *  2. edit nlp.sh setting the appropriate path to the 'java' binary and corenlp
 *  3. run nlp.sh to make sure it works, reading from stdin and output to stdout
 *  4. run this script which will pipe incoming socket connection to corenlp
 *          nohup server.js &
 *  
 */

var port = 9100;

var net = require('net');
var readline = require('readline');

var spawn = require('child_process').spawn;
var nlp = spawn('./nlp.sh');

nlp.stdout.on('data', function(data) {
    console.log('stdout: ' + data);
});

nlp.stderr.on('data', function(data) {
    console.log('stderr: ' + data);
});

nlp.on('close', function(code) {
    console.log('child process exited with code ' + code);
});

var server = net.createServer();
//server.maxConnections = 1;


server.listen(port);

server.once('listening', function() {
    console.log('Server listening on port %d', port);
});

var pending = [];
var connected = false;

server.on('connection', function(stream) {
    var p = processConnection(stream);
    if ((pending.length == 0) && (!connected))
        p();
    else {
        pending.push(p);
    }
});

//handle client's own disconnect?
/*server.on('close', function(stream) {
    if ((connected) && (pending.length > 0)) {
        pending.shift()();
    }
});*/

function processConnection(stream) {

    return function() {
        connected = true;
        stream.pipe(nlp.stdin);
        nlp.stdout.pipe(stream);
        
        var rl = readline.createInterface({
            input: nlp.stdout,
            output: stream
        });
        
        function switchNext() {
            stream.unpipe(nlp.stdin);
            stream.end();
            connected = false;
            rl.close();
            if (pending.length > 0) {
                var r = pending.shift();
                r();
            }            
        }
        
        rl.on('line', function(cmd) {
            if (cmd == '') {
                switchNext();
            }
        });
        
        rl.on('close', switchNext);

    }
}

