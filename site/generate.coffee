#!/usr/bin/env coffee

fs = require 'fs'

wintersmith = require 'wintersmith'
$p 			= require 'procstreams'

env = wintersmith __dirname + '/config.json'

cmd = process.argv.pop()

if ['build', 'preview'].indexOf(cmd) is -1
	cmd = 'build'

# perform basic setup (pull in new readme, etc)

if not fs.existsSync 'contents/apidocs'
	fs.mkdirSync 'contents/apidocs'

$p('php -f generate_docs.php', [], cwd: __dirname + '/../docs/jsonapi4_docs').data (stderr, stdout) ->
	fs.writeFileSync 'contents/apidocs/index.html', stdout?.toString()

	if cmd is "build"
		env.build (err) ->
			throw err if err
			console.log 'Done!'
	else if cmd is "preview"
		env.preview (err, server) ->
			throw err if err

			console.log 'Server Running!'

