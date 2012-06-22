# requires the following gems:
# json, digest/sha2, net/http

# written on:
# ruby 1.8.7 (2011-12-28 patchlevel 357) [universal-darwin11.0]

require 'json'
require 'digest/sha2'
require 'net/http'

class JSONAPI
	attr_accessor :host
	attr_accessor :port
	attr_accessor :username
	attr_accessor :password
	attr_accessor :salt

	def initialize(host, port, username, password, salt)
		@host = host
		@port = port
		@username = username
		@password = password
		@salt = salt
	end

	def call(method, args)
		
	end