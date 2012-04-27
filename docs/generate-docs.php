<?php

error_reporting(E_ALL ^ E_NOTICE);

$input_files = array_merge(array(realpath("../test/plugins/JSONAPI/methods.json")), glob("../test/plugins/JSONAPI/methods/*.json"));
foreach($input_files as $input_file) {
	$docs .= jsonapi_docs(json_decode(file_get_contents($input_file), true));
}

?><!DOCTYPE html>
<html>
	<head>
		<title>JSONAPI API Docs</title>
		<link type="text/css" rel="stylesheet" name="stylesheet" href="bootstrap.min.css" />
		<link type="text/css" rel="stylesheet" name="stylesheet" href="styles.css" />
	</head>
	<body>
	        <div class="nav-right well">
	          <ul class="nav nav-list">
		        <li class="nav-header">JSONAPI Documentation</a>
	          	<?php
	foreach($input_files as $input_file) {
		$json = json_decode(file_get_contents($input_file), true);
		printf('<li><a href="#package-%s">%s</a></li>'."\n", rawurlencode($json['name']), $json['name'], (empty($json['namespace']) ? "default" : $json['namespace']));
	}
	          	?>
	          </ul>
	        </div><!-- /.nav-collapse -->
	<div class="container">
		<div class="jumbotron subhead">
			<h1>API Documentation for JSONAPI</h1>
			<p class="lead">This page documents all the methods that ship with JSONAPI. This includes the base method that work as long as JSONAPI is installed and enabled, along with the methods that require another plugin to be installed and enabled for them to work.
			</p>
			<p class="lead well"><em>The methods that require another plugin to be installed simply do not work if that plugin is not installed and enabled.</em></p>
			<h2>Available method packages</h2>
		</div>
		<ul>
	<?php

	foreach($input_files as $input_file) {
		$json = json_decode(file_get_contents($input_file), true);
		printf('<li><a href="#package-%s">%s</a> (%s namespace)</li>'."\n", rawurlencode($json['name']), $json['name'], (empty($json['namespace']) ? "default" : $json['namespace']));
	}
	echo "</ul>".$docs; ?>
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

function name_for_method_json($input_json, $namespace) {
	return (empty($namespace) ? "" : $namespace.".").$input_json['name'];
}

function generate_token($call) {
	return rawurlencode($call);
}

function jsonapi_docs_method ($method, $namespace) {
	$format = <<<EOT
	<div class="method-wrapper" id="%s">
		<dl class="dl-horizontal method-info">
			<dt>Name</dt>
			<dd class="method-info-name">%s</dd>

			<dt>Description</dt>
			<dd class="method-info-desc">%s</dd>

			<dt>Arguments</dt>
			<dd class="method-info-args clearfix">%s</dd>

			<dt>Returns</dt>
			<dd class="method-info-return">%s</dd>
		</dl>
	</div>

EOT;
	return sprintf($format, generate_token($method['call']), name_for_method_json($method, $namespace), $method['desc'], jsonapi_docs_args($method['args']), jsonapi_docs_returns($method['returns']));
}

function jsonapi_docs_args ($args) {
	if(empty($args)) {
		return "None";
	}
	
	$format = "<dl class='dl-horizontal clearfix method-info-args-ul'>%s</dl>";
	$format_i = "\n<dt>%s</dt><dd>%s</dd>";
	foreach($args as $arg) {
		$r .= sprintf($format_i, $arg[0], $arg[1]);
	}
	
	return sprintf($format, $r);
}

function jsonapi_docs_returns ($returns) {
	if(empty($returns)) {
		return "void";
	}
	
	return sprintf("<strong>%s</strong> %s", $returns[0], $returns[1]);
}