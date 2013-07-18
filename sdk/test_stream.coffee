net = require 'net'
readline = require 'readline'

s = new net.Socket

keepAlive = ->
	setTimeout ->
		console.log "writing keep alive"
		s.write "GSA\n"

		keepAlive()
	, 15000

s.setTimeout 0
s.on 'connect', ->
	# s.write "GS /api/2/call?json=%5B%7B%22name%22%3A%22jsonapi.users.user.permissions%22%2C%22key%22%3A%22e1c3bab67cd33d4e6aa2df0c69a9c0b80b2020b25ac74a55677482385cbd5c76%22%2C%22username%22%3A%22admin%22%2C%22arguments%22%3A%5B%22admin%22%5D%2C%22tag%22%3A%22test%22%7D%2C%7B%22name%22%3A%22plugins.name.version%22%2C%22key%22%3A%22acd93946b70334bcbef1aed2b98486efaac1091b2bf98583b9be8cc8c04daa60%22%2C%22arguments%22%3A%5B%22JSONAPI%22%5D%2C%22username%22%3A%22admin%22%7D%5D\n"
	s.write "GS /api/2/subscribe?json=%7B%22name%22:%22console%22,%22key%22:%228919e84b99035b2a2257645b0c83f3ebfb4f045881b5d6f5b174d89be7d5415a%22,%22username%22:%22admin%22,%22tag%22:%22console%22,%22show_previous%22:true%7D\n"
	# s.write "GET /api/2/subscribe?json=%5B%7B%22name%22%3A%22console%22%2C%22key%22%3A%223de68435513cf0226aafe5f7c8949f18106dede688da19b63663a524ad865fa2%22%2C%22username%22%3A%22usernameGoesHere%22%2C%22tag%22%3A%22console%22%2C%22show_previous%22%3Atrue%7D%5D HTTP/1.1"

	keepAlive()

r = readline.createInterface s, s
r.on 'line', (line) ->
	buf = new Buffer(line);
	if line.trim() != ''
		console.log line

s.connect 25565, 'localhost'