<?php

error_reporting(E_ALL ^ E_NOTICE);

$input_files = glob("../../src/resources/jsonapi4/methods/*.json");
$router = [];
foreach($input_files as $input_file) {
	$json = json_decode(file_get_contents($input_file), true);
	foreach($json['methods'] as $k => $method) {
		$router[$method['name']] = $method;
	}
}

ksort($router);

$docs = "";

foreach($router as $route => $method) {
	$docs .= jsonapi_docs_method($method);
}

?><!DOCTYPE html>
<html>
	<head>
		<title>JSONAPI API Docs</title>
		<link type="text/css" rel="stylesheet" name="stylesheet" href="bootstrap.min.css" />
		<link type="text/css" rel="stylesheet" name="stylesheet" href="styles.css" />
	</head>
	<body>
	<div class="container">
		<div class="jumbotron subhead">
			<h3>← <a href="/">Back to the complete JSONAPI docs</a></h3>
			<br/>
			<h1>APIv4 Documentation for JSONAPI</h1>
			<p class="lead">This page documents all the methods that ship with JSONAPI. This includes the base method that work as long as JSONAPI is installed and enabled, along with the methods that require another plugin to be installed and enabled for them to work.
			</p>
			<p class="lead well"><em>The methods that require another plugin to be installed simply do not work if that plugin is not installed and enabled.</em></p>
			<h2>Available method packages</h2>
		</div>
		<ul>
	<?php

	foreach($input_files as $input_file) {
		$json = json_decode(file_get_contents($input_file), true);
		printf('<li>%s</li>'."\n", $json['name']);
	}
	?>
		</ul>

		<h2>Methods (<?php echo count($router); ?>)</h2>
		<ul>
		<?php
		foreach($router as $route => $method) {
			printf('<li><a href="#%s" style="font-family:monospace;">%s</a>', generate_token($route), $route);
		}
		?>

		<?php echo $docs; ?>
	</ul>
		</div>
	</body>
</html>
<?php

function jsonapi_docs($input_json) {
	$format = <<<EOT
	<section>
		<div class="page-header">
			<h1 id="package-%s">%s</h1>
		</div>
		<dl class="dl-horizontal">
			<dt>Dependencies</dt>
			<dd><ul>%s</ul></dd>
			<dt>Methods</dt>
			<dd><ul>%s</ul></dd>
		</dl>
		<div id="jsonapi-docs" class="doc-wrapper">%s</div>
	</section>
EOT;
	
	usort($input_json['methods'], function($one, $two) {
		return strcmp($one['name'], $two['name']);
	});
	
	foreach($input_json['methods'] as $method) {
		$r .= jsonapi_docs_method($method, $input_json['namespace']);
	}
	foreach($input_json['depends'] as $dep) {
		$lis .= "<li>$dep</li>";
	}
	
	$method_html = "";
	foreach($input_json['methods'] as $method) {
		$args = "";
		foreach((array)$method['args'] as $arg) {
			$args .= " ".$arg[0].",";
		}
		$args = ltrim(rtrim($args, ","));
		
		$method_html .= sprintf("<li><a href='#%s'>%s %s(%s)</a></li>", 
								generate_token($method['call']),
								(empty($input_json['return']) ? "void" : $input_json['return']),
								(empty($input_json['namespace']) ? "" : $input_json['namespace'].".").$method['name'],
								$args);
	}
	
	return sprintf($format, rawurlencode($input_json['name']), $input_json['name'], $lis, $method_html, $r);
}

function generate_token($call) {
	return rawurlencode($call);
}

function jsonapi_docs_method ($method) {
	$format = <<<EOT
	<div class="method-wrapper" id="%s">
		<dl class="dl-horizontal method-info">
			<dt>Name</dt>
			<dd class="method-info-name"><code>%s</code></dd>

			<dt>Description</dt>
			<dd class="method-info-desc">%s</dd>

			<dt>Arguments</dt>
			<dd class="method-info-args clearfix">%s</dd>

			<dt>Returns</dt>
			<dd class="method-info-return">%s</dd>
		</dl>
	</div>

EOT;
	return sprintf($format, generate_token($method['name']), $method['name'], $method['desc'], jsonapi_docs_args($method['args']), jsonapi_docs_returns($method['returns']));
}

function jsonapi_docs_args ($args) {
	if(empty($args)) {
		return "None";
	}
	
	$format = "<dl class='dl-horizontal clearfix method-info-args-ul'>%s</dl>";
	$format_i = "\n<dt>%s</dt><dd>%s &mdash; %s";
	foreach($args as $arg) {
		$r .= sprintf($format_i, $arg[0], $arg[1], $arg[2]);
		if(count($arg) > 3) {
			$r .= sprintf(" (<em>default is %s</em>)", $arg[3]);
		}
		$r .= "</dd>";
	}
	
	return sprintf($format, $r);
}

function jsonapi_docs_returns ($returns) {
	if(empty($returns)) {
		return "void";
	}
	
	return sprintf("<strong>%s</strong> %s", $returns[0], $returns[1]);
}
