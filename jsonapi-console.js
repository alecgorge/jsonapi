$(function () {
	var api = null,
		status = $("#status"),
		methods = null,
		argb = $("#arg-builder"),
		$res = $("#res"),
		activeMethod = ""
		socket = null;
		
	
	window.unload = function () {
		socket.close();
	};
	function set_status(text) {
		status.html(text).slideDown();
	}
	function hide_status(text) {
		status.slideUp();
	}
	
	function loadArgs(k, kk) {
		var method = methods[k]["methods"][kk];
		var activeMethod = method["name"];
		
		$('#arg-title').text('Arguments for '+method['name']);
		var thtml = "<form id='argbb'>";
		if(method.args) {
			$.each(method.args, function(k,v) {
				thtml += "<p><label>"+v[1]+" ("+v[0]+")</label> <input type='text' class='arg-val' name='arg-"+k+"'/></p>";
			});
		}
		else {
			thtml += "<p>No arguments. Just hit \"Call method\".</p>";
		}
		thtml += "<p class='clear'><input id='argbb-submit' type='submit' value='Call method' /></p></form>";
		argb.html(thtml);
		
		$('#argbb').submit(function (e) {
			e.preventDefault();
			e.stopPropagation();
		
			set_status('Calling method...');
			
			var args = [];
			$('input.arg-val').each(function (k,v) {
				var val = $(v).val();
				if(parseInt(val) == val) {
					val = parseInt(val);
				}
				
				args.push(val);
			});
			
			var url = api.makeURL(activeMethod, args);
			$res.html("URL: "+url.substring(0, url.length - 11)+"\n\n\n");
			api.call(activeMethod, args, function (data) {
				$res.append(JSON.stringify(data, null, 4).replace(/\\r/g, "\r").replace(/\\n/g, "\n").replace(/\\t/g, "\t"));
				set_status("Ready...");
			});
			
			return false;
		});
	}
	
	$('#navigation a').click(function (e) {
		e.preventDefault();
		
		$(this).parent().parent().find('li').removeClass('active');
		
		$(this).parent().addClass('active');
		
		$('.content').hide();
		
		$('#'+$(this).attr('id').split('-')[0]).show();
	});
	$('#navigation').hide();
	
	$('#cmd').keyup(function (e) {
		if(e.keyCode == 13) {
			send_cmd($(this).val());
			$(this).val('');
		}
	});
	
	$('#send').click(function () {
		send_cmd($('#cmd').val());
		$('#cmd').val('');
	});
	
	var $display = $("#display");
	function cmd_log(e) {
		$display.val($display.val() + e);
		$display.scrollTop(9999);
		$display.scrollTop($display.scrollTop()*12);
	}
	
	function send_cmd(e) {
		var url = api.makeURL("runConsoleCommand", [e]);
		url = "/api"+url.substr(url.lastIndexOf("/"));
		url = url.substr(0, url.indexOf("&callback=?"));
		socket.send(url);
		cmd_log("INPUT: "+e+"\n");
	};
	
	if(!window.WebSocket) {
		alert('WebSocket not detected, console will not work! Get a cooler browser!');
	}

	$("#jsonapi-login").submit(function (e) {
		api = new JSONAPI({
			host: $("#jsonapi-host").val(),
			port: parseInt($("#jsonapi-port").val()),
			username: $("#jsonapi-user").val(),
			password: $("#jsonapi-pass").val(),
			salt: $("#jsonapi-salt").val(),
		});
		
		var $this = $(this);
		$this.slideUp();
		
		set_status("Testing connection...");
		
		api.call("getPlayerCount", function (data) {
			if(data.result == "success") {
				set_status("Connected!");
				set_status("Loading API methods...");
				
				socket = new WebSocket('ws://'+api.host+':'+(api.port+2)+'/');
				
				socket.onopen = function (e) {
					cmd_log("Connected...\n");
					socket.send("/api/subscribe?source=console&key="+api.createKey("console"));
				};
				
				socket.onmessage = function (e) {
					var data = JSON.parse(e.data);
					
					if(data.source == "console") {
						cmd_log(data.result == "success" ? data.success.line : data.error);
					}
				};
				
				socket.oncolse = function (e) {
					cmd_log("Connection lost...\n");
				};
				
				$.getJSON("http://ramblingwood.com/minecraft/jsonapi/console/serve-json.php?callback=?", function(data) {
					$(".content, #navigation").show();
					set_status("Processing API methods...");
					methods = data;
					
					var $methods = $("#methods-list");
					$.each(methods, function (k, v) {
						var thehtml = "";
						
						$.each(v["methods"], function (kk, vv) {
							thehtml += "<li><a class='active-link' href='#method-"+k.toString()+"-"+kk.toString()+"'>"+vv["name"]+"("+ (!vv["args"] ? "0" : vv["args"].length.toString()) +")</a></li>";
						});
						
						$methods.append("<li>"+v["name"]+"<ul>"+thehtml+"</ul></li>");
					});
					
					$('a.active-link').click(function () {
						var parts = $(this).attr('href').match(/#method-([0-9]+)-([0-9]+)/);
						loadArgs(parseInt(parts[1]), parseInt(parts[2]));
					});
					
					set_status("Ready. Select a method from the sidebar.");
				});
			}
			else {
				$this.slideDown();
				hide_status();
			}
		});
		
		return false;
	});
});
