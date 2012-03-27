(function () {
	var go = function () {
		var dls = $('body > dl').slice(1)
		,	i = 0
		,	a = [0,1,2,3,4,5,6,7,8,9,10]
		,	raw = []
		;

		$('pre').slice(1).map(function () {
			var txt = $(this).text().replace(/\s+/gm, ' ').trim();
			var parts = [txt.substring(0, txt.indexOf(' ')), txt.substring(txt.indexOf(' ') + 1)];

			var x = ''
			,	responseType = parts[0]
			,	open = parts[1].indexOf('(')
			,	close = parts[1].indexOf(')')
			,	args = open+1 == close ? [] : parts[1].substring(open+1, close).split(', ').map(function (v, k) {
				var parts = v.split(' ');
				return [parts[0], $($(dls[i]).find('dd dd').slice(0, -1)[k]).text().trim()]
			})
			,	methodName = parts[1].substring(0, open)
			;

			var r = {
				name: methodName,
				"call": "this.econ." + methodName + "(" + a.slice(0, args.length).join(', ') + ")",
				"args": args,
				"desc": $(dls[i]).find('dd:first-child').text().replace(/\s+/gm, ' ').trim(),
				"returns": [responseType, $(dls[i]).find('dd dd:last-child').text().replace(/\s+/gm, ' ').trim()]
			};

			i++;

			raw.push(r);
		});

		console.log(raw);
		console.log('[' + raw.map(JSON.stringify).join(', ') + ']');

		return raw;
	};

	if(!window.jQuery) {
		var j = document.createElement('script');
		j.src = "file://localhost/Users/alecgorge/Documents/js/jquery/jquery.js";
		j.onload = go;

		document.body.appendChild(j);
	}
	else {
		go();
	}
})();
