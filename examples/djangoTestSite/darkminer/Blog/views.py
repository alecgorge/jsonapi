# Create your views here.
import json
from socket import gethostbyname

from django.shortcuts import render_to_response
from django.template import RequestContext

import MinecraftAPI

JSONAPIhost = 'localhost'
JSONAPIusername = 'admin'
JSONAPIpassword = 'demo'
JSONAPIsalt = ''


def home(request, method = ''):
	if 'ServerConnection' not in request.session.keys():
		request.session['host']='localhost'
		request.session['username']='admin'
		request.session['password']='demo'
		request.session['salt']=''
		
	if request.method == 'POST' and request.POST['action'] == 'Connect':
		request.session['host']=request.POST['host']
		request.session['username']=request.POST['username']
		request.session['password']=request.POST['password']
		request.session['salt']=request.POST['salt']
	
	MinecraftJsonApi = None
	try:
		MinecraftJsonApi = MinecraftAPI.MinecraftJsonApi(
			host = request.session['host'],
			username = request.session['username'],
			password = request.session['password'],
			salt = request.session['salt'],
		)
	except:
		pass
	
	if MinecraftJsonApi:
		methods = MinecraftJsonApi.getLoadedMethods()
		server = MinecraftJsonApi.getServer()
		result = ''
		try:
			selectedMethod = [m for m in methods if m['method_name'] == method][0]
		except:
			selectedMethod = {}
	
		if request.method == 'POST' and request.POST['action'] != 'Connect':
			args = []
	
			# build the parameter list for the call
			for i in range(len(selectedMethod['params'])):
				args.append(request.POST['param%d'%(i+1)])
		
			# make the call.
			# this is equivilent to writing:
			#	server.foo(a,b,...)
			result = selectedMethod['method'](MinecraftJsonApi, *args)
	
			# Pretty print the resultant object for the web
			result = json.dumps(result, skipkeys=True, indent=4)
	else:
		methods = []
		server = None
		selectedMethod = None
		result = ''
	return render_to_response('index.html', {
		'host': request.session['host'],
		'hostIP': gethostbyname(request.session['host']),
		'server': server,
		'methods': methods,
		'selectedMethod': selectedMethod,
		'result': result,
	}, RequestContext(request))
