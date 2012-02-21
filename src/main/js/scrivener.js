var nano = require('nano')('http://localhost:5984/'), server = require('express').createServer();

server.post( '/log', function (req, res) { insert( 'log', req, res); });
server.post('/stat', function (req, res) { insert('stat', req, res); });

server.get( '/log', function (req, res) { list( 'log', req, res); });
server.get('/stat', function (req, res) { list('stat', req, res); });

function insert(method, req, res) {
    var appId = req.query['appid'], entry = req.query['entry'], dbName = appId + '_' + method;
    nano.db.create(dbName, function () {
        nano.use(dbName).insert(JSON.parse(entry), null,
            function (e, b, h) {
                if (e) { console.error(e); }
                else { console.log(appId + ': ' + entry); }
            }).pipe(res);
    });
}

function list(method, req, res) {
    var appId = req.query['appid'], dbName = appId + '_' + method;
    nano.db.create(dbName, function () { nano.use(dbName).list({'include_docs':true}).pipe(res); });
}

server.get('/', function (req, res) {
    res.writeHead(200, {'Content-Type':'text/plain'});
    res.end('Hello World\n');
});

server.listen(8124);

