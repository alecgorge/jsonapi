var jsonapi = require('../jsonapi-v2').JSONAPI;

var j = new jsonapi({
	hostname: "localhost",
	port: 20059,
	username: "usernameGoesHere",
	password: "passwordGoesHere",
	salt: "salt goes here"
});

setInterval(function () {
	j.call('getPlayers', function (json) {
		console.log(json.success);
	});
}, 5000);

j.stream('console', function(json) {
	console.log(json.success.line.trim());
});

//j.close();
