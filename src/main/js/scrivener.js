var bee = require("beeline");

var router = bee.route({
    "/log":{
        "GET":function (req, res) {
            res.writeHead(200, {'Content-Type':'text/plain'});
            res.end('Hello World\n');
        },
        "POST":function (req, res) {

        }
    },

    "/stat":{
        "GET":function (req, res) {

        },
        "POST":function (req, res) {

        }
    },

    "/":function (req, res) {
    }
});

require("http").createServer(router).listen(8124);
