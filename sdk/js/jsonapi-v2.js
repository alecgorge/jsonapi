/**
 * @author Alec Gorge
 * 
 * This file works both in the browser and on Node.js. It uses net.Socket on Node.js and WebSocket in the browser.
 * It has no other dependencies.
 *
 * The only methods to be aware of as an end user are call(methods, arguments, callback), stream(sources, callback)
 * and close()
 *
 * If the first argument to call is an array, a multiple call will automatically used. If the second argument is
 * a function, it will automatically be used.
 *
 * You can subscribe to multiple soruces by passing an array as the first argument to stream.
 *
 * If you use a stream, don't forget to close the connection with close(). Examples:

var jsonapi = require('../jsonapi').JSONAPI;

var j = new jsonapi({
	hostname: "localhost",
	port: 20059,
	username: "usernameGoesHere",
	password: "passwordGoesHere",
	salt: "salt goes here"
});

// or

var j = new jsonapi({
	hostname: "localhost",
	username: "usernameGoesHere"
});

j.port = 20059;
j.password = "passwordGoesHere";
j.salt = "salt goes here";

j.call('getPlayers', console.log);
j.call(['getPlayers', 'getPlayer'], [[], ['alecgorge']], console.log);

j.stream('console', function(json) {
	console.log(json.success.line.trim());
});

 */


JSONAPI = (function() {
	var JSON;JSON||(JSON={}),function(){function str(a,b){var c,d,e,f,g=gap,h,i=b[a];i&&typeof i=="object"&&typeof i.toJSON=="function"&&(i=i.toJSON(a)),typeof rep=="function"&&(i=rep.call(b,a,i));switch(typeof i){case"string":return quote(i);case"number":return isFinite(i)?String(i):"null";case"boolean":case"null":return String(i);case"object":if(!i)return"null";gap+=indent,h=[];if(Object.prototype.toString.apply(i)==="[object Array]"){f=i.length;for(c=0;c<f;c+=1)h[c]=str(c,i)||"null";e=h.length===0?"[]":gap?"[\n"+gap+h.join(",\n"+gap)+"\n"+g+"]":"["+h.join(",")+"]",gap=g;return e}if(rep&&typeof rep=="object"){f=rep.length;for(c=0;c<f;c+=1)typeof rep[c]=="string"&&(d=rep[c],e=str(d,i),e&&h.push(quote(d)+(gap?": ":":")+e))}else for(d in i)Object.prototype.hasOwnProperty.call(i,d)&&(e=str(d,i),e&&h.push(quote(d)+(gap?": ":":")+e));e=h.length===0?"{}":gap?"{\n"+gap+h.join(",\n"+gap)+"\n"+g+"}":"{"+h.join(",")+"}",gap=g;return e}}function quote(a){escapable.lastIndex=0;return escapable.test(a)?'"'+a.replace(escapable,function(a){var b=meta[a];return typeof b=="string"?b:"\\u"+("0000"+a.charCodeAt(0).toString(16)).slice(-4)})+'"':'"'+a+'"'}function f(a){return a<10?"0"+a:a}"use strict",typeof Date.prototype.toJSON!="function"&&(Date.prototype.toJSON=function(a){return isFinite(this.valueOf())?this.getUTCFullYear()+"-"+f(this.getUTCMonth()+1)+"-"+f(this.getUTCDate())+"T"+f(this.getUTCHours())+":"+f(this.getUTCMinutes())+":"+f(this.getUTCSeconds())+"Z":null},String.prototype.toJSON=Number.prototype.toJSON=Boolean.prototype.toJSON=function(a){return this.valueOf()});var cx=/[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,escapable=/[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,gap,indent,meta={"\b":"\\b","\t":"\\t","\n":"\\n","\f":"\\f","\r":"\\r",'"':'\\"',"\\":"\\\\"},rep;typeof JSON.stringify!="function"&&(JSON.stringify=function(a,b,c){var d;gap="",indent="";if(typeof c=="number")for(d=0;d<c;d+=1)indent+=" ";else typeof c=="string"&&(indent=c);rep=b;if(b&&typeof b!="function"&&(typeof b!="object"||typeof b.length!="number"))throw new Error("JSON.stringify");return str("",{"":a})}),typeof JSON.parse!="function"&&(JSON.parse=function(text,reviver){function walk(a,b){var c,d,e=a[b];if(e&&typeof e=="object")for(c in e)Object.prototype.hasOwnProperty.call(e,c)&&(d=walk(e,c),d!==undefined?e[c]=d:delete e[c]);return reviver.call(a,b,e)}var j;text=String(text),cx.lastIndex=0,cx.test(text)&&(text=text.replace(cx,function(a){return"\\u"+("0000"+a.charCodeAt(0).toString(16)).slice(-4)}));if(/^[\],:{}\s]*$/.test(text.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g,"@").replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g,"]").replace(/(?:^|:|,)(?:\s*\[)+/g,""))){j=eval("("+text+")");return typeof reviver=="function"?walk({"":j},""):j}throw new SyntaxError("JSON.parse")})}();
	
	var util, crypto, net;

	if(typeof require != 'undefined' && typeof window === 'undefined') {
		    util = require('util')
		,	crypto = require('crypto')
		,	net = require('net')
		;
	}
	else {
		var sprintf=function(){function b(a,b){for(var c=[];b>0;c[--b]=a);return c.join("")}function a(a){return Object.prototype.toString.call(a).slice(8,-1).toLowerCase()}var c=function(){c.cache.hasOwnProperty(arguments[0])||(c.cache[arguments[0]]=c.parse(arguments[0]));return c.format.call(null,c.cache[arguments[0]],arguments)};c.format=function(c,d){var e=1,f=c.length,g="",h,i=[],j,k,l,m,n,o;for(j=0;j<f;j++){g=a(c[j]);if(g==="string")i.push(c[j]);else if(g==="array"){l=c[j];if(l[2]){h=d[e];for(k=0;k<l[2].length;k++){if(!h.hasOwnProperty(l[2][k]))throw sprintf('[sprintf] property "%s" does not exist',l[2][k]);h=h[l[2][k]]}}else l[1]?h=d[l[1]]:h=d[e++];if(/[^s]/.test(l[8])&&a(h)!="number")throw sprintf("[sprintf] expecting number but found %s",a(h));switch(l[8]){case"b":h=h.toString(2);break;case"c":h=String.fromCharCode(h);break;case"d":h=parseInt(h,10);break;case"e":h=l[7]?h.toExponential(l[7]):h.toExponential();break;case"f":h=l[7]?parseFloat(h).toFixed(l[7]):parseFloat(h);break;case"o":h=h.toString(8);break;case"s":h=(h=String(h))&&l[7]?h.substring(0,l[7]):h;break;case"u":h=Math.abs(h);break;case"x":h=h.toString(16);break;case"X":h=h.toString(16).toUpperCase()}h=/[def]/.test(l[8])&&l[3]&&h>=0?"+"+h:h,n=l[4]?l[4]=="0"?"0":l[4].charAt(1):" ",o=l[6]-String(h).length,m=l[6]?b(n,o):"",i.push(l[5]?h+m:m+h)}}return i.join("")},c.cache={},c.parse=function(a){var b=a,c=[],d=[],e=0;while(b){if((c=/^[^\x25]+/.exec(b))!==null)d.push(c[0]);else if((c=/^\x25{2}/.exec(b))!==null)d.push("%");else{if((c=/^\x25(?:([1-9]\d*)\$|\(([^\)]+)\))?(\+)?(0|'[^$])?(-)?(\d+)?(?:\.(\d+))?([b-fosuxX])/.exec(b))===null)throw"[sprintf] huh?";if(c[2]){e|=1;var f=[],g=c[2],h=[];if((h=/^([a-z_][a-z_\d]*)/i.exec(g))===null)throw"[sprintf] huh?";f.push(h[1]);while((g=g.substring(h[0].length))!=="")if((h=/^\.([a-z_][a-z_\d]*)/i.exec(g))!==null)f.push(h[1]);else if((h=/^\[(\d+)\]/.exec(g))!==null)f.push(h[1]);else throw"[sprintf] huh?";c[2]=f}else e|=2;if(e===3)throw"[sprintf] mixing positional and named placeholders is not (yet) supported";d.push(c)}b=b.substring(c[0].length)}return d};return c}(),vsprintf=function(a,b){b.unshift(a);return sprintf.apply(null,b)}
		util = {
			format: sprintf,
			isArray: function(x) {
				return x.constructor == Array;
			}
		};
		function SHA256(a){function p(a){var b=c?"0123456789ABCDEF":"0123456789abcdef",d="";for(var e=0;e<a.length*4;e++)d+=b.charAt(a[e>>2]>>(3-e%4)*8+4&15)+b.charAt(a[e>>2]>>(3-e%4)*8&15);return d}function o(a){a=a.replace(/\r\n/g,"\n");var b="";for(var c=0;c<a.length;c++){var d=a.charCodeAt(c);d<128?b+=String.fromCharCode(d):d>127&&d<2048?(b+=String.fromCharCode(d>>6|192),b+=String.fromCharCode(d&63|128)):(b+=String.fromCharCode(d>>12|224),b+=String.fromCharCode(d>>6&63|128),b+=String.fromCharCode(d&63|128))}return b}function n(a){var c=[],d=(1<<b)-1;for(var e=0;e<a.length*b;e+=b)c[e>>5]|=(a.charCodeAt(e/b)&d)<<24-e%32;return c}function m(a,b){var c=[1116352408,1899447441,3049323471,3921009573,961987163,1508970993,2453635748,2870763221,3624381080,310598401,607225278,1426881987,1925078388,2162078206,2614888103,3248222580,3835390401,4022224774,264347078,604807628,770255983,1249150122,1555081692,1996064986,2554220882,2821834349,2952996808,3210313671,3336571891,3584528711,113926993,338241895,666307205,773529912,1294757372,1396182291,1695183700,1986661051,2177026350,2456956037,2730485921,2820302411,3259730800,3345764771,3516065817,3600352804,4094571909,275423344,430227734,506948616,659060556,883997877,958139571,1322822218,1537002063,1747873779,1955562222,2024104815,2227730452,2361852424,2428436474,2756734187,3204031479,3329325298],e=[1779033703,3144134277,1013904242,2773480762,1359893119,2600822924,528734635,1541459225],f=Array(64),m,n,o,p,q,r,s,t,u,v,w,x;a[b>>5]|=128<<24-b%32,a[(b+64>>9<<4)+15]=b;for(var u=0;u<a.length;u+=16){m=e[0],n=e[1],o=e[2],p=e[3],q=e[4],r=e[5],s=e[6],t=e[7];for(var v=0;v<64;v++)v<16?f[v]=a[v+u]:f[v]=d(d(d(l(f[v-2]),f[v-7]),k(f[v-15])),f[v-16]),w=d(d(d(d(t,j(q)),g(q,r,s)),c[v]),f[v]),x=d(i(m),h(m,n,o)),t=s,s=r,r=q,q=d(p,w),p=o,o=n,n=m,m=d(w,x);e[0]=d(m,e[0]),e[1]=d(n,e[1]),e[2]=d(o,e[2]),e[3]=d(p,e[3]),e[4]=d(q,e[4]),e[5]=d(r,e[5]),e[6]=d(s,e[6]),e[7]=d(t,e[7])}return e}function l(a){return e(a,17)^e(a,19)^f(a,10)}function k(a){return e(a,7)^e(a,18)^f(a,3)}function j(a){return e(a,6)^e(a,11)^e(a,25)}function i(a){return e(a,2)^e(a,13)^e(a,22)}function h(a,b,c){return a&b^a&c^b&c}function g(a,b,c){return a&b^~a&c}function f(a,b){return a>>>b}function e(a,b){return a>>>b|a<<32-b}function d(a,b){var c=(a&65535)+(b&65535),d=(a>>16)+(b>>16)+(c>>16);return d<<16|c&65535}var b=8,c=0;a=o(a);return p(m(n(a),a.length*b))}
	}

	function jsonapi (args) {
		this.hostname	= "";
		this.port		= 20059;
		this.username	= "";
		this.password	= "";
		this.salt		= "";
		this.debug		= false;
		this.usingWebSocket = false;

		var _socket = false
		,	queue = []
		,	handlers = {}
		,	tagStarter = Math.round(Math.random(Math.random()) * 1000)
		,	that = this
		,	_init_started = false
		;

		(function (args) {
			if(!args) return;
			if(args.hostname) this.hostname = args.hostname;
			if(args.port) this.port = args.port;
			if(args.username) this.username = args.username;
			if(args.password) this.password = args.password;
			if(args.salt) this.salt = args.salt;
		}).call(this, args);

		function dbug() {
			if(that.debug) {
				for(var i = 0; i < arguments.length; i++) {
					console.log(arguments[i]);
				}
			}
		}

		this.socket		= function () {
			if(_socket == false) {
				dbug(that.port + 1, that.hostname);

				if(typeof window != 'undefined' && window.WebSocket) {
					this.usingWebSocket = true;

					_socket =  new WebSocket(util.format('ws://%s:%s', that.hostname, that.port + 2));
					_socket.onopen = openHandler;
					_socket.onerror = errorHandler;
					_socket.onmessage = dataHandler;
				}
				else {
					_socket = net.connect(that.port + 1, that.hostname, openHandler);
					_socket.setNoDelay();

					_socket.on('error', errorHandler);
					_socket.on('data', dataHandler);
				}
			}

			return _socket;
		};

		function openHandler () {
			if(queue.length > 0) {
				queue.forEach(function (v) {
					handlers[v.tag] = v.callback;
					writeLine(v.line);
				});
				queue = [];
			}
		}

		function jsonHandler (json) {
			if(typeof json.tag != "undefined" && handlers[json.tag])
				handlers[json.tag](json);
			else
				throw "JSONAPI is out of date. JSONAPI 3.5.0+ is required.";
		}

		function dataHandler (data) {
			if(that.usingWebSocket) data = data.data;

			dbug(data.toString());
			data.toString().trim().split("\r\n").forEach(function (v) {
				dbug(v);
				jsonHandler(JSON.parse(v));
			});
		}

		function errorHandler (e) {
			throw e;
		}

		var urlFormat = "/api/%s?method=%s&args=%s&key=%s%s"
		,	streamFormat = "/api/subscribe?%s=%s&key=%s&show_previous=%s%s"
		,	fqnFormat = "%s:%s";

		function fqn() {
			return util.format(fqnFormat, that.hostname, that.port);
		}

		function base () {
			return "http://" + fqn();
		}

		function makeURL(method, arguments, tag) {
			arguments = util.isArray(arguments) ? arguments : [];
			isMultiple = util.isArray(method);

			if(isMultiple) {
				method = JSON.stringify(method);
			}

			return util.format(urlFormat, "call",
									   encodeURIComponent(method),
									   encodeURIComponent(JSON.stringify(arguments)),
									   makeKey(method),
									   typeof tag == "undefined" ? "" : "&tag=" + encodeURIComponent(tag)
									   );
		}

		function makeStreamURL(source, showOlder, tag) {
			if(typeof showOlder == "undefined") {
				showOlder = true;
			}

			isMultiple = util.isArray(source);

			if(isMultiple) {
				source = JSON.stringify(source);
			}

			return util.format(streamFormat,
									   isMultiple ? "sources" : "source",
									   encodeURIComponent(source),
									   makeKey(source),
									   showOlder,
									   typeof tag == "undefined" ? "" : "&tag=" + encodeURIComponent(tag)
									   );
		}

		function makeKey(method) {
			var text = util.format('%s%s%s%s', that.username, method, that.password, that.salt);

			method = encodeURIComponent(util.isArray(method) ? JSON.stringify(method) : method);

			return SHA256 ? SHA256(text) : crypto.createHash('sha256')
												 .update(text)
												 .digest('hex');
		}

		function makeTag() {
			tagStarter++;
			return tagStarter;
		}

		function writeLine(line) {
			dbug("writing:"+line);

			if(that.usingWebSocket) {
				that.socket().send(line);
			}
			else {
				that.socket().write(line + "\r\n");
			}
		}

		this.call = function (method, arguments, callback) {
			if(typeof callback == 'undefined' && typeof arguments == "function") {
				callback = arguments;
				arguments = [];
			}

			if(typeof arguments == "undefined") {
				arguments = [];
			}
			else if(!util.isArray(arguments)) {
				arguments = [arguments];
			}

			var tag = makeTag()
			,	url = makeURL(method, arguments, tag)
			;

			if(_socket == false) {
				queue.push({
					"line": url,
					"tag": tag,
					"callback": callback
				});

				if(_init_started == false) {
					that.socket();
					_init_started = true;
				}
			}
			else {
				handlers[tag] = callback;
				writeLine(url);
			}
		}

		this.stream = function(streamName, sendOld, callback) {
			if(typeof callback == 'undefined' && typeof sendOld == "function") {
				callback = sendOld;
				sendOld = true;
			}

			var tag = makeTag()
			,	url = makeStreamURL(streamName, sendOld, tag)
			;

			if(_socket == false) {
				queue.push({
					"line": url,
					"tag": tag,
					"callback": callback
				});

				if(_init_started == false) {
					that.socket();
					_init_started = true;
				}
			}
			else {
				handlers[tag] = callback;
				writeLine(url);
			}
		}

		this.close = function () {
			if(_socket) {
				if(that.usingWebSocket) {
					this.socket().close();
				}
				else {
					this.socket().end();
				}
				handles = {};
			}
		}
	}

	return jsonapi;
})();

if(typeof module != 'undefined') {
	module.exports = {
		"JSONAPI": JSONAPI
	};
}
