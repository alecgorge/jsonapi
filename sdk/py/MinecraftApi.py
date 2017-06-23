#!/usr/bin/python

import json
import socket
from hashlib import sha256
from urllib2 import urlopen
from urllib import quote

def urlencode(query):
	if isinstance(query, dict):
		query = query.items()
	pairs = []
	for item in query:
		pairs.append("%s=%s" % (item[0], quote(str(item[1]))))
	return "&".join(pairs)

class MinecraftStream(object):
	#	Extends the basic stream object and adds a readjson method to the object
	def __getattribute__(self, name):
		if name not in ['readjson', '_original_stream']:
			return getattr(
				object.__getattribute__(self, '_original_stream'), 
				name
			)
		else:
			return object.__getattribute__(self, name)
		
	def __init__(self, stream):
		self._original_stream = stream
		
	def readjson(self, *args, **kwargs):
		ret = self._original_stream.readline(*args, **kwargs)
		return json.loads(ret)
	
class MinecraftJsonApi (object):
	'''
	Python Interface to JSONAPI for Bukkit (Minecraft)
	
	Based off of the PHP interface by Alec Gorge <alecgorge@gmail.com>
		https://github.com/alecgorge/jsonapi/raw/master/sdk/php/JSONAPI.php
	
	(c) 2011 Accalia.de.Elementia <Accalia.de.Elementia@gmail.com>
	
	This work is licensed under a Creative Commons Attribution
	3.0 Unported License <http://creativecommons.org/licenses/by/3.0/>
	
	JSONAPI homepage:
		http://ramblingwood.com/minecraft/jsonapi/
	'''
	
	__basic_url = 'http://{host}:{port}/api/call?{query}'
	__multi_url = 'http://{host}:{port}/api/call-multiple?{query}'
	__subscribe_url = '/api/subscribe?{query}'
	__letters = list('abcdefghijklmnopqrstuvwxyz')
		
	def __createkey(self, method):
		'''
		Create an authentication hash for the given method.
		'''
		return sha256('{username}{method}{password}{salt}'.format( 
				username = self.username, 
				method = method,
				password = self.password,
				salt = self.salt
			)
		).hexdigest()
	
	def __createURL(self, method, args):
		'''
		Create the full URL for calling a method.
		'''			
		key = self.__createkey(method)
		
		return self.__basic_url.format(
			host = self.host,
			port = self.port,
			query = urlencode([
				('method', method),
				('args', json.dumps(args)),
				('key', key),
			])
		)
	
	def __createStreamURL(self, source):
		'''
		Create the full URL for subscribing to a stream.
		'''			
		key = self.__createkey(source)
		
		return self.__subscribe_url.format(
			query = urlencode([
				('source', source),
				('key', key),
			])
		)
	

	def __createMultiCallURL(self, methodlist, arglist):
		'''
		Create the full URL for calling multiple methods.
		'''
		methodlist = json.dumps(methodlist)
		arglist = json.dumps(arglist)
		key = self.__createkey(methodlist)
		return self.__multi_url.format(
			host = self.host,
			port = self.port,
			query = urlencode([
				('method', methodlist),
				('args', arglist),
				('key', key),
			])
		)

	def __createsocket(self):
		'''
		Setup a socket connection to the server and return a file like 
		object for reading and writing.
		
		Copied with minor edits from examples on: 
			http://docs.python.org/library/socket.html
		'''
		'''try:
			flags = socket.AI_ADDRCONFIG
		except AttributeError:
			flags = 0
		for res in socket.getaddrinfo(self.host, (self.port+1), 
				socket.AF_UNSPEC, socket.SOCK_STREAM, 
				socket.IPPROTO_TCP,	flags):
			af, socktype, proto, canonname, sa = res'''
		try:
			sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
			port = self.port +1
			sock.connect((self.host, port))
		except socket.error:
			if sock:
				sock.close()
				sock = None
			#continue
		#break
			if not sock:
				raise Exception('Connect failed') 
			
		return MinecraftStream(sock.makefile('rwb'))

	def __createMethodAttributes(self, method):
		'''
		Yet another translation method. 
		
		Transform the method definition JSON into a dictionary 
		containing only the attributes needed for the wrapper. 
		'''
		attrs = {}
		attrs['name'] = method.get('name', '')
		if attrs['name'] < 0:
			raise Exception('Malformed method definition in JSON')
		
		attrs['description'] = method.get('desc','')
		attrs['namespace'] = method.get('namespace','')
		attrs['enabled'] = method.get('enabled',False)
		if attrs['namespace']:
			attrs['method_name'] = attrs['namespace']+ '_'+attrs['name']
			attrs['call_name'] = attrs['namespace'] + '.'+attrs['name']
		else:
			attrs['method_name'] = attrs['name']
			attrs['call_name'] = attrs['name']
		attrs['returns'] = method.get('returns',
			[None,'Unspecified return type.'])[1]
		args = method.get('args',[])
		num_args = len(args)
		alpha = self.__letters
		attrs['args'] = str(alpha[:num_args]).replace('\'','')[1:-1]
		attrs['params'] = '\n'.join([
			'{1} ({0})'.format(a[0], a[1]) for a in args
		])
		return attrs
	
	def __createMethod (self, method):
		'''
		Create a dynamic method based on provided definition.
		
		TODO: Is there a better way to do this? Possibly via closure to
		avoid exec
		'''
		def makeMethod (method):
			call_name = method['call_name']
			def _method (self, *args):
				return self.call(call_name,*args)				
			
			_method.__name__ = str(method['method_name'])
			_method.__doc__ = """{description}
	
			{returns}
	
			Parameters:
			{params}
			""".format(**method)
			
			return _method
		
		attributes = self.__createMethodAttributes(method)
		if method['enabled']:
			rv_method = makeMethod(attributes)
		else:
			rv_method = None
		attributes['method'] = rv_method
		del attributes['call_name']
		del attributes['args']
		return attributes
	
	
	def __init__(self, host='localhost', port=20059, username='admin', 
		password='demo', salt=''):
		self.host = host
		self.username = username
		self.password = password
		self.port = int(port)
		self.salt = salt
		self.__methods = []
				
	def rawCall (self, method, *args):
		'''
		Make a remote call and return the raw response.
		'''
		url = self.__createURL(method, args)
		result = urlopen(url).read()
		return result
				
				
	def call (self, method, *args):
		'''
		Make a remote call and return the JSON response.
		'''
		data = self.rawCall(method, *args)
		result = json.loads(data)
		if result['result'] =='success':
			return result['success']	
		else:
			raise Exception('(%s) %s' %(result['result'], result[result['result']]))
	

	def call_multiple(self, methodlist, arglist):
		'''
		Make multiple calls and return multiple responses
		'''
		url = self.__createMultiCallURL(methodlist, arglist)
		result = urlopen(url).read()
		return result

	def subscribe (self, feed):
		'''
		Subscribe to the remote stream.
		
		Return a file like object for reading responses from. Use 
		read/readline for raw values, use readjson for parsed values.
		'''
		# This doesn't work right, I don't know why.... yet.
		#raise NotImplementedError()
		
		if feed not in ['console', 'chat', 'connections']:
			raise NotImplementedError(
				'Subscribing to feed \'%s\' is not supported.' % feed)
	
		url = self.__createStreamURL(feed)
		stream = self.__createsocket()
	
		stream.write(url)
		stream.write('\n')
		stream.flush()
		
		return stream
	
	def getLoadedMethods(self, active_only=True):
		'''
		Get all methods recognized by the remote server.
		'''
		if active_only:
			test = lambda x: x.get('enabled', False)
		else:
			test = lambda x: True
		return [a for a in self.__methods if test(a)]
	
	def getMethod(self, name):
		'''
		get method definition for the provided method name.
		
		If the method is in a name space the namespace must be provided
		too, the name having the form "{namespace}_{name}"
		'''
		method = [m for m in self.__methods if m['method_name'] == name]
		if len(method):
			return method[0]
		else:
			return None

if __name__ == '__main__':
	# Some basic test code
	# Read params
 	paramDefaults = {'host': 'localhost', 'port':20059, 'username':'admin', 'password':'demo', 'salt':''}
 	filterFuncs = {'host': str, 'port': int, 'username': str, 'password': str, 'salt': str}
 	params = {}
 	for k in paramDefaults.keys():
 		value = raw_input("%s (%s): " % (k.capitalize(), str(paramDefaults[k])))
 		if len(value):
	 		params[k] = filterFuncs[k](value)
	 	else:
			params[k] = paramDefaults[k]
 	
 	api = MinecraftJsonApi(
 		host = params['host'], 
 		port = params['port'], 
 		username = params['username'], 
 		password = params['password'], 
 		salt = params['salt']
 	)
	
	print([m['method_name'] for m in api.getLoadedMethods()])
	print (api.getMethod('kickPlayer'))	
	x = True
	while x:
		method = raw_input('>')
		print (api.getMethod(method))
		method = raw_input('->')
		print api.call(method)
