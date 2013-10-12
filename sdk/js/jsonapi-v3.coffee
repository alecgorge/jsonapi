
###
opts = {
	hostname: 'localhost',
	port: 25565,
	username: 'admin',
	password: 'demo',
	onConnection: function(err, jsonapi){}
}
###

class JSONAPI
	constructor: (opts) ->
		opts = opts || {}
		@host = opts.hostname || 'localhost'
		@port = opts.port || 25565
		@username = opts.username || 'admin'
		@password = opts.password || 'changeme'
		@queue = []
		@handlers = {}

	connect: () ->
		@socket = new WebSocket "ws://#{@host}:#{@port}/api/2/websocket"

		@socket.onopen = () =>
			if @queue.length > 0
				for item in @queue
					# @handlers[item.tag] = item.callback

					@send item.line

				@queue = []

		@socket.onerror = (e) -> throw e
		@socket.onmessage = (data) =>
			return console.log data
			data = data.data

			for line in data.toString().trim().split("\r\n")
				json = JSON.parse line
				if typeof json.tag isnt "undefined" and @handlers[json.tag]
					@handlers[json.tag](json)
				else
					throw "JSONAPI is out of date. JSONAPI 5.3.0+ is required."

	send: (data) ->
		if @socket.readyState is WebSocket.OPEN
			@socket.send(data)
		else
			@queue.push line: data

j = new JSONAPI

j.connect()

setInterval () ->
	j.send('test')
, 15000

# if exports or module or not window
# 	module.exports = exports = JSONAPI