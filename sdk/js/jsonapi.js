var JSON;JSON||(JSON={}),function(){function str(a,b){var c,d,e,f,g=gap,h,i=b[a];i&&typeof i=="object"&&typeof i.toJSON=="function"&&(i=i.toJSON(a)),typeof rep=="function"&&(i=rep.call(b,a,i));switch(typeof i){case"string":return quote(i);case"number":return isFinite(i)?String(i):"null";case"boolean":case"null":return String(i);case"object":if(!i)return"null";gap+=indent,h=[];if(Object.prototype.toString.apply(i)==="[object Array]"){f=i.length;for(c=0;c<f;c+=1)h[c]=str(c,i)||"null";e=h.length===0?"[]":gap?"[\n"+gap+h.join(",\n"+gap)+"\n"+g+"]":"["+h.join(",")+"]",gap=g;return e}if(rep&&typeof rep=="object"){f=rep.length;for(c=0;c<f;c+=1)typeof rep[c]=="string"&&(d=rep[c],e=str(d,i),e&&h.push(quote(d)+(gap?": ":":")+e))}else for(d in i)Object.prototype.hasOwnProperty.call(i,d)&&(e=str(d,i),e&&h.push(quote(d)+(gap?": ":":")+e));e=h.length===0?"{}":gap?"{\n"+gap+h.join(",\n"+gap)+"\n"+g+"}":"{"+h.join(",")+"}",gap=g;return e}}function quote(a){escapable.lastIndex=0;return escapable.test(a)?'"'+a.replace(escapable,function(a){var b=meta[a];return typeof b=="string"?b:"\\u"+("0000"+a.charCodeAt(0).toString(16)).slice(-4)})+'"':'"'+a+'"'}function f(a){return a<10?"0"+a:a}"use strict",typeof Date.prototype.toJSON!="function"&&(Date.prototype.toJSON=function(a){return isFinite(this.valueOf())?this.getUTCFullYear()+"-"+f(this.getUTCMonth()+1)+"-"+f(this.getUTCDate())+"T"+f(this.getUTCHours())+":"+f(this.getUTCMinutes())+":"+f(this.getUTCSeconds())+"Z":null},String.prototype.toJSON=Number.prototype.toJSON=Boolean.prototype.toJSON=function(a){return this.valueOf()});var cx=/[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,escapable=/[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,gap,indent,meta={"\b":"\\b","\t":"\\t","\n":"\\n","\f":"\\f","\r":"\\r",'"':'\\"',"\\":"\\\\"},rep;typeof JSON.stringify!="function"&&(JSON.stringify=function(a,b,c){var d;gap="",indent="";if(typeof c=="number")for(d=0;d<c;d+=1)indent+=" ";else typeof c=="string"&&(indent=c);rep=b;if(b&&typeof b!="function"&&(typeof b!="object"||typeof b.length!="number"))throw new Error("JSON.stringify");return str("",{"":a})}),typeof JSON.parse!="function"&&(JSON.parse=function(text,reviver){function walk(a,b){var c,d,e=a[b];if(e&&typeof e=="object")for(c in e)Object.prototype.hasOwnProperty.call(e,c)&&(d=walk(e,c),d!==undefined?e[c]=d:delete e[c]);return reviver.call(a,b,e)}var j;text=String(text),cx.lastIndex=0,cx.test(text)&&(text=text.replace(cx,function(a){return"\\u"+("0000"+a.charCodeAt(0).toString(16)).slice(-4)}));if(/^[\],:{}\s]*$/.test(text.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g,"@").replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g,"]").replace(/(?:^|:|,)(?:\s*\[)+/g,""))){j=eval("("+text+")");return typeof reviver=="function"?walk({"":j},""):j}throw new SyntaxError("JSON.parse")})}();

function JSONAPI (obj) {
	this.host = obj.host;
	this.port = obj.port || 20059;
	this.salt = obj.salt;
	this.username = obj.username;
	this.password = obj.password;
	this.urlFormats = {
		"call" : "http://%s:%s/api/call?method=%s&args=%s&key=%s&callback=?",
		"callMultiple" : "http://%s:%s/api/call-multiple?method=%s&args=%s&key=%s&callback=?"
	};
	
	var that = this;
		
	var sprintf=function(){function b(a,b){for(var c=[];b>0;c[--b]=a);return c.join("")}function a(a){return Object.prototype.toString.call(a).slice(8,-1).toLowerCase()}var c=function(){c.cache.hasOwnProperty(arguments[0])||(c.cache[arguments[0]]=c.parse(arguments[0]));return c.format.call(null,c.cache[arguments[0]],arguments)};c.format=function(c,d){var e=1,f=c.length,g="",h,i=[],j,k,l,m,n,o;for(j=0;j<f;j++){g=a(c[j]);if(g==="string")i.push(c[j]);else if(g==="array"){l=c[j];if(l[2]){h=d[e];for(k=0;k<l[2].length;k++){if(!h.hasOwnProperty(l[2][k]))throw sprintf('[sprintf] property "%s" does not exist',l[2][k]);h=h[l[2][k]]}}else l[1]?h=d[l[1]]:h=d[e++];if(/[^s]/.test(l[8])&&a(h)!="number")throw sprintf("[sprintf] expecting number but found %s",a(h));switch(l[8]){case"b":h=h.toString(2);break;case"c":h=String.fromCharCode(h);break;case"d":h=parseInt(h,10);break;case"e":h=l[7]?h.toExponential(l[7]):h.toExponential();break;case"f":h=l[7]?parseFloat(h).toFixed(l[7]):parseFloat(h);break;case"o":h=h.toString(8);break;case"s":h=(h=String(h))&&l[7]?h.substring(0,l[7]):h;break;case"u":h=Math.abs(h);break;case"x":h=h.toString(16);break;case"X":h=h.toString(16).toUpperCase()}h=/[def]/.test(l[8])&&l[3]&&h>=0?"+"+h:h,n=l[4]?l[4]=="0"?"0":l[4].charAt(1):" ",o=l[6]-String(h).length,m=l[6]?b(n,o):"",i.push(l[5]?h+m:m+h)}}return i.join("")},c.cache={},c.parse=function(a){var b=a,c=[],d=[],e=0;while(b){if((c=/^[^\x25]+/.exec(b))!==null)d.push(c[0]);else if((c=/^\x25{2}/.exec(b))!==null)d.push("%");else{if((c=/^\x25(?:([1-9]\d*)\$|\(([^\)]+)\))?(\+)?(0|'[^$])?(-)?(\d+)?(?:\.(\d+))?([b-fosuxX])/.exec(b))===null)throw"[sprintf] huh?";if(c[2]){e|=1;var f=[],g=c[2],h=[];if((h=/^([a-z_][a-z_\d]*)/i.exec(g))===null)throw"[sprintf] huh?";f.push(h[1]);while((g=g.substring(h[0].length))!=="")if((h=/^\.([a-z_][a-z_\d]*)/i.exec(g))!==null)f.push(h[1]);else if((h=/^\[(\d+)\]/.exec(g))!==null)f.push(h[1]);else throw"[sprintf] huh?";c[2]=f}else e|=2;if(e===3)throw"[sprintf] mixing positional and named placeholders is not (yet) supported";d.push(c)}b=b.substring(c[0].length)}return d};return c}(),vsprintf=function(a,b){b.unshift(a);return sprintf.apply(null,b)}
	function SHA256(a){function p(a){var b=c?"0123456789ABCDEF":"0123456789abcdef",d="";for(var e=0;e<a.length*4;e++)d+=b.charAt(a[e>>2]>>(3-e%4)*8+4&15)+b.charAt(a[e>>2]>>(3-e%4)*8&15);return d}function o(a){a=a.replace(/\r\n/g,"\n");var b="";for(var c=0;c<a.length;c++){var d=a.charCodeAt(c);d<128?b+=String.fromCharCode(d):d>127&&d<2048?(b+=String.fromCharCode(d>>6|192),b+=String.fromCharCode(d&63|128)):(b+=String.fromCharCode(d>>12|224),b+=String.fromCharCode(d>>6&63|128),b+=String.fromCharCode(d&63|128))}return b}function n(a){var c=[],d=(1<<b)-1;for(var e=0;e<a.length*b;e+=b)c[e>>5]|=(a.charCodeAt(e/b)&d)<<24-e%32;return c}function m(a,b){var c=[1116352408,1899447441,3049323471,3921009573,961987163,1508970993,2453635748,2870763221,3624381080,310598401,607225278,1426881987,1925078388,2162078206,2614888103,3248222580,3835390401,4022224774,264347078,604807628,770255983,1249150122,1555081692,1996064986,2554220882,2821834349,2952996808,3210313671,3336571891,3584528711,113926993,338241895,666307205,773529912,1294757372,1396182291,1695183700,1986661051,2177026350,2456956037,2730485921,2820302411,3259730800,3345764771,3516065817,3600352804,4094571909,275423344,430227734,506948616,659060556,883997877,958139571,1322822218,1537002063,1747873779,1955562222,2024104815,2227730452,2361852424,2428436474,2756734187,3204031479,3329325298],e=[1779033703,3144134277,1013904242,2773480762,1359893119,2600822924,528734635,1541459225],f=Array(64),m,n,o,p,q,r,s,t,u,v,w,x;a[b>>5]|=128<<24-b%32,a[(b+64>>9<<4)+15]=b;for(var u=0;u<a.length;u+=16){m=e[0],n=e[1],o=e[2],p=e[3],q=e[4],r=e[5],s=e[6],t=e[7];for(var v=0;v<64;v++)v<16?f[v]=a[v+u]:f[v]=d(d(d(l(f[v-2]),f[v-7]),k(f[v-15])),f[v-16]),w=d(d(d(d(t,j(q)),g(q,r,s)),c[v]),f[v]),x=d(i(m),h(m,n,o)),t=s,s=r,r=q,q=d(p,w),p=o,o=n,n=m,m=d(w,x);e[0]=d(m,e[0]),e[1]=d(n,e[1]),e[2]=d(o,e[2]),e[3]=d(p,e[3]),e[4]=d(q,e[4]),e[5]=d(r,e[5]),e[6]=d(s,e[6]),e[7]=d(t,e[7])}return e}function l(a){return e(a,17)^e(a,19)^f(a,10)}function k(a){return e(a,7)^e(a,18)^f(a,3)}function j(a){return e(a,6)^e(a,11)^e(a,25)}function i(a){return e(a,2)^e(a,13)^e(a,22)}function h(a,b,c){return a&b^a&c^b&c}function g(a,b,c){return a&b^~a&c}function f(a,b){return a>>>b}function e(a,b){return a>>>b|a<<32-b}function d(a,b){var c=(a&65535)+(b&65535),d=(a>>16)+(b>>16)+(c>>16);return d<<16|c&65535}var b=8,c=0;a=o(a);return p(m(n(a),a.length*b))}
	function rawurlencode(a){a=(a+"").toString();return encodeURIComponent(a).replace(/!/g,"%21").replace(/'/g,"%27").replace(/\(/g,"%28").replace(/\)/g,"%29").replace(/\*/g,"%2A")}
	
	/**
	 * Generates the proper SHA256 based key from the given method suitable for use as the key GET parameter in a JSONAPI API call.
	 * 
	 * @param string method The name of the JSONAPI API method to generate the key for.
	 * @return string The SHA256 key suitable for use as the key GET parameter in a JSONAPI API call.
	 */
	this.createKey = function (method) {
		if(typeof method == "object") {
			method = JSON.stringify(method);
		}
		return SHA256(that.username + method + that.password + that.salt);
	};
	
	/**
	 * Generates the proper URL for a standard API call the given method and arguments.
	 * 
	 * @param string method The name of the JSONAPI API method to generate the URL for.
	 * @param array args An array of arguments that are to be passed in the URL.
	 * @return string A proper standard JSONAPI API call URL. Example: "http://localhost:20059/api/call?method=methodName&args=jsonEncodedArgsArray&key=validKey".
	 */
	this.makeURL = function (method, args) {
		return sprintf(that.urlFormats["call"], that.host, that.port, rawurlencode(method), rawurlencode(JSON.stringify(args || [])), that.createKey(method));
	};
	
	/**
	 * Generates the proper URL for a multiple API call the given method and arguments.
	 * 
	 * @param array methods An array of strings, where each string is the name of the JSONAPI API method to generate the URL for.
	 * @param array args An array of arrays, where each array contains the arguments that are to be passed in the URL.
	 * @return string A proper multiple JSONAPI API call URL. Example: "http://localhost:20059/api/call-multiple?method=[methodName,methodName2]&args=jsonEncodedArrayOfArgsArrays&key=validKey".
	 */
	this.makeURLMultiple = function (methods, args) {
		return sprintf(that.urlFormats["callMultiple"], that.host, that.port, rawurlencode(JSON.stringify(methods)), rawurlencode(JSON.stringify(args || [])), that.createKey(methods));
	};
	
	/**
	 * Calls the single given JSONAPI API method with the given args.
	 * 
	 * @param string method The name of the JSONAPI API method to call.
	 * @param array args An array of arguments that are to be passed.
	 * @param function onComplete The function to be called when the request is complete. The only argument passed is the returned JSON.
	 * @return array An associative array representing the JSON that was returned.
	 */
	this.call = function (method, args, onComplete) {
		if(typeof args == "function") {
			onComplete = args;
			args = [];
		}
	
		args = args || [];
		if(typeof method == "object") {
			that.callMultiple(method, args);
		}
		
		for(var i = 0; i < args.length; i++) {
			var v = args[i];
			if(!isNaN(parseInt(v))) {
				args[i] = parseInt(v);
			}
		}
		
		var url = that.makeURL(method, args);

		that.curl(url, onComplete);
		
		return that;
	};
	
	this.curl = function (url, cb) {
		jQuery.getJSON(url, cb);
	};

	/**
	 * Calls the given JSONAPI API methods with the given args.
	 * 
	 * @param array methods An array strings, where each string is the name of a JSONAPI API method to call.
	 * @param array args An array of arrays of arguments that are to be passed.
	 * @param function onComplete The function to be called when the request is complete. The only argument passed is the returned JSON.
	 * @throws Exception When the length of the methods array and the args array are different, an exception is thrown.
	 * @return array An array of associative arrays representing the JSON that was returned.
	 */
	this.callMultiple = function (methods, args, onComplete) {
		if(typeof args == "function") {
			onComplete = args;
			args = [];
		}
	
		args = args || [];
		if(methods.length !== args.length) {
			throw "The length of the arrays \$methods and \$args are different! You need an array of arguments for each method!";
		}
		
		for(var i = 0; i < args.length; i++) {
			for(var x = 0; x < args[i].length; x++) {
				var v = args[i][x];
				if(!isNaN(parseInt(v))) {
					args[i][x] = parseInt(v);
				}
			}
		}
		
		var url = that.makeURLMultiple(methods, args);

		that.curl(url, onComplete);
		
		return that;
	};
}