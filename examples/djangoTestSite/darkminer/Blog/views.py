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
	if 'MinecraftJsonApi' not in request.session.keys():
		try:
			request.session['MinecraftJsonApi'] = MinecraftAPI.MinecraftJsonApi(
				host = JSONAPIhost,
				username = JSONAPIusername,
				password = JSONAPIpassword,
				salt = JSONAPIsalt,
			)
		except:
			pass
	if 'MinecraftJsonApi' in request.session.keys():
		methods = request.session['MinecraftJsonApi'].getLoadedMethods()
		server = request.session['MinecraftJsonApi'].getServer()
		try:
			selectedMethod = [m for m in methods if m['method_name'] == method][0]
		except:
			selectedMethod = {}
	
			if request.method == 'POST':
				args = []
		
			# build the parameter list for the call
			for i in range(len(selectedMethod['params'])):
				args.append(request.POST['param%d'%(i+1)])
			
			# make the call.
			# this is equivilent to writing:
			#	server.foo(a,b,...)
			result = selectedMethod['method'](request.session['MinecraftJsonApi'], *args)
		
			# Pretty print the resultant object for the web
			result = json.dumps(result, skipkeys=True, indent=4)
		else:	
			result = ''
	else:
		methods = []
		server = None
		selectedMethod = None
		result = ''
	return render_to_response('index.html', {
		'host': JSONAPIhost,
		'hostIP': gethostbyname(JSONAPIhost),
		'server': server,
		'methods': methods,
		'selectedMethod': selectedMethod,
		'result': result,
	}, RequestContext(request))
