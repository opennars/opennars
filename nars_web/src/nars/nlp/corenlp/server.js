var net = require('net');
var readline = require('readline');

var spawn = require('child_process').spawn,
    ls    = spawn('./nlp.sh');

ls.stdout.on('data', function (data) {
  console.log('stdout: ' + data);
});

ls.stderr.on('data', function (data) {
  console.log('stderr: ' + data);
});

ls.on('close', function (code) {
  console.log('child process exited with code ' + code);
});

var server = net.createServer();
//server.maxConnections = 1;
 
var port = 9100;
 
server.listen(port);
 
server.once('listening', function() {
  console.log('Server listening on port %d', port);
});

var pending = [];
var connected = false;

server.on('connection', function(stream) {
	var p = processConnection(stream);
	if ((pending.length == 0) && (!connected)) p();
	else {
		pending.push(p);
	}
});

//handle client's own disconnect
server.on('close', function(stream) {
	if ((connected) && (pending.length > 0)) {
		pending.shift()();	
	}
});

function processConnection(stream) {
return function() {
  connected = true;
  stream.pipe(ls.stdin);
  ls.stdout.pipe(stream);

 var rl = readline.createInterface({
  input: ls.stdout,
  output: stream
 });
  rl.on('line', function (cmd) {
  if (cmd == '') {
	stream.unpipe(ls.stdin);
	  stream.end();
	connected = false;
	rl.close();
 if (pending.length > 0) {
        var r = pending.shift();
        r();
 }

  }
 });

}
}

