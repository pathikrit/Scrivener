var server = require('express').createServer();

server.post('/log', function(req, res){
    res.writeHead(200, {'Content-Type': 'text/plain'});
    res.end('Hello World\n');
    console.log(req.query["appid"] + ": " + req.query["entry"]);
});

server.listen(8124);
