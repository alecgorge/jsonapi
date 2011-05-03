$(function () {
	var api = null,
		status = $("#status"),
		methods = null,
		argb = $("#arg-builder"),
		$res = $("#res"),
		activeMethod = "";
		
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
				$res.append(js_beautify(JSON.stringify(data)).replace(/\\r/g, "\r").replace(/\\n/g, "\n").replace(/\\t/g, "\t"));
				set_status("Ready...");
			});
			
			return false;
		});
	}

	$("#jsonapi-login").submit(function (e) {
		api = new JSONAPI({
			host: $("#jsonapi-host").val(),
			port: $("#jsonapi-port").val(),
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
				
				$.getJSON("http://ramblingwood.com/minecraft/jsonapi/console/serve-json.php?callback=?", function(data) {
					$("#wrapper").show();
					set_status("Processing API methods...");
					methods = data;
					
					var $methods = $("#methods");
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