$(function () {
	local = {
		not_connected : "Not connected...",
		reload : "Reload the page to reconnect...",
		connected : "Connected & chatting...",
		connecting : "Connecting..."
	};
	
	app = {
		fqn : "",
		username : "",
		password : "",
		pub : ""
	};
	
	$users = $("#users");
	
	function makeUrl(method, args) {
		if(typeof(args) == "undefined") args = [];
		return "/api/call?method="+encodeURIComponent(method)+"&args="+encodeURIComponent(JSON.stringify(args))+"&username="+encodeURIComponent(app.user)+"&password="+encodeURIComponent(app.pass);
	}
	function subscribe(to) {
		return "/api/subscribe?source="+encodeURIComponent(to)+"&username="+encodeURIComponent(app.user)+"&password="+encodeURIComponent(app.pass);
	}
	
	function putLine (user,msg) {
		var line = $("<li></li>");
		line.addClass("chat");
		
		var date = $("<span class='date'></span>");
		d = new Date();
		date.text(d.format("h:MM:ss TT"));
		
		var userS = $("<span class='user'></span>");
		if(typeof(users[user]) != "undefined" && users[user].isAdmin) {
			userS.addClass("admin");
		}
		userS.text("<"+user+">");
		
		var message = $("<span class='chat-message'></span>");
		message.text(msg);
		
		line.append(date).append(userS).append(message);
		
		$("#responses").append(line);
		window.scrollBy(0,900);
	}
	
	function putConnLine(user,msg) {
		var line = $("<li></li>");
		line.addClass("chat");
		
		var date = $("<span class='date'></span>");
		d = new Date();
		date.text(d.format("h:MM:ss TT"));
		
		var userS = $("<span class='user'></span>");
		if(typeof(users[user]) != "undefined" && users[user].isAdmin) {
			userS.addClass("admin");
		}
		userS.text("Player "+user+"");
		
		var message = $("<span class='chat-message'></span>");
		message.text(msg);
		
		line.append(date).append(userS).append(message);
		
		$("#responses").append(line);
		window.scrollBy(0,900);
	}
	
	users = {};
	
	function say (msg) {
		app.socket.send(makeUrl("player.broadcastMessage", ["<"+app.pub+" (webui)> "+msg]));
		putLine(app.pub, msg);
	}
	
	function process () {
		app.socket = new WebSocket("ws://"+app.fqn);
		var socket = app.socket;
		
		socket.onopen = function(evt) {
			$("#status").removeClass().addClass("message message-success").children("p").text(local.connected);
			socket.send(makeUrl("player.getPlayers"));
			socket.send(subscribe("chat"));
			socket.send(subscribe("connections"));
		};
		
		socket.onerror = function(evt) {
			alert("error");
			alert(evt);
		};
		
		socket.onmessage = function(evt) {
			var data = JSON.parse(evt.data);
			if(data.source == "player.getPlayers") {
				users = {};
				$users.html("");
				if(data.success.length == 0) {
					$users.append("<li id='noone'>No one is here!</li>");
				}
				else {
					for(var i in data.success) {
						var name = data.success[i]["name"];
						users[name] = data.success[i];									
						var ip = data.success[i]["ip"];
						var admin = (data.success[i]["isAdmin"] ? "admin" : "");
						$users.append("<li class='"+admin+"'>"+name+" "+"("+ip+")"+"</li>");
					}
				}
			}
			else if(data.source == "chat") {
				putLine(data.data["player"], data.data["message"]);
			}
			else if(data.source == "connections") {
				putConnLine(data.data.player, (data.data.action == "connect" ? "connected." : "disconnected."));
				socket.send(makeUrl("player.getPlayers"));
			}
		};
		
		socket.onclose = function(evt) {
			$("#status").removeClass().addClass("message message-error").children("p").text(local.reload);
			$("#users").html("");
			$("#responses").append("<li class='message'>Disconnected.</li>");
		};
	};
	
	var ip = $( "#ip" ),
		port = $( "#port" ),
		user = $("#user"),
		pass = $("#pass"),
		pub = $("#pub");
	
	$( "#server-connect" ).dialog({
		autoOpen: false,
		height: 430,
		width: 420,
		modal: true,
		buttons: {
			"Connect": function() {
				app.fqn = ip.val() + ":" + port.val();
				app.user = user.val();
				app.pass = pass.val();
				app.pub = pub.val();
				$( this ).dialog( "close" );
				$("#status").removeClass().addClass("message message-notice").children("p").text(local.connecting);
				$("#input").focus();
				process();
			},
			Cancel: function() {
				alert('Nope. You need to fill the entire form.');
				//$( this ).dialog( "close" );
			}
		},
		close: function() {
			
		}
	});
	
	$("#submit, #disconnect").button();
	
	$("#disconnect").click(function () {
		app.socket.close();
	});
	
	$("#submit").click(function () {
		say($("#input").val());
		$("#input").val("").focus();
	});
	
	$("#input").keyup(function (e) {
		if(e.which == 13) {
			$("#submit").click();
		}
	});
	
	$("#server-connect").dialog("open");

	$("#status").removeClass().addClass("message message-error").children("p").text(local.not_connected);

});
