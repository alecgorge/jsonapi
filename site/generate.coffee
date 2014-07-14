#!/usr/bin/env coffee

fs = require 'fs'

wintersmith = require 'wintersmith'
request		= require 'request'

url = "http://localhost:25565/api/2/call?json=%5B%7B%22name%22%3A%22jsonapi.methods%22%2C%22key%22%3A%22059d32c621cee00c7990857d17cbd5f9730c77735dbf905654f90124d2cbaa9f%22%2C%22username%22%3A%22admin%22%2C%22arguments%22%3A%5B%5D%2C%22tag%22%3A%221%22%7D%5D"

cmd = process.argv.pop()

if ['build', 'preview', 'build_locals'].indexOf(cmd) is -1
	cmd = 'build'

env = null

if cmd isnt "build_locals"
	env = wintersmith __dirname + '/config.json'

# perform basic setup (pull in new readme, etc)

if cmd is "build"
	env.build (err) ->
		throw err if err
		console.log 'Done!'
else if cmd is "preview"
	env.preview (err, server) ->
		throw err if err

		console.log 'Server Running!'
else if cmd is "build_locals"
	console.log "build_locals"
	request url, (err, response, body) ->
		fs.writeFileSync 'locals.json', JSON.stringify(methods: JSON.parse(body)[0].success)