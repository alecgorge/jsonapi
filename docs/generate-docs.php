<?php

require '../sdk/php/JSONAPI.php';

$input_files = array_merge(array("../src/methods.json"), glob("../test/plugins/JSONAPI/methods/*.json"));
foreach($input_files as $input_file) {
	$docs .= jsonapi_docs(json_decode(file_get_contents($input_file), true));
}

?><!DOCTYPE html>
<html>
	<head>
		<title>JSONAPI API Docs</title>
		<link type="text/css" rel="stylesheet" name="stylesheet" href="styles.css" />
	</head>
	<body>
	<h1>API Documentation for JSONAPI</h1>
	<p>This page documents all the methods that ship with JSONAPI. This includes the base method that work as long as JSONAPI is installed and enabled, along with the methods that require another plugin to be installed and enabled for them to work.
	The methods that require another plugin to be installed simply do not work if that plugin is not installed and enabled.</p>
	<h2>Available method packages</h2>
	<ul>
<?php

foreach($input_files as $input_file) {
	$json = json_decode(file_get_contents($input_file), true);
	printf('<li><a href="#package-%s">%s</a> (%s namespace)</li>', rawurlencode($json['name']), $json['name'], (empty($json['namespace']) ? "default" : $json['namespace']));
}
echo "</ul>".$docs; ?>
	</body>
</html>
<?php

function jsonapi_docs($input_json) {
	$format = '<h2 id="package-%s">%s</h2><h3>Depends on the following plugins:</h3><ul>%s</ul><h3>Methods</h3><ul>%s</ul><div id="jsonapi-docs" class="doc-wrapper">%s</div>';
	
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
		<ul class="method-info">
			<li class="method-info-name"><strong>Name: </strong>%s</li>
			<li class="method-info-desc"><strong>Description: </strong>%s</li>
			<li class="method-info-args"><strong>Arguments: </strong>%s</li>
			<li class="method-info-return"><strong>Returns: </strong>%s</li>
		</ul>
	</div>

EOT;
	return sprintf($format, generate_token($method['call']), name_for_method_json($method, $namespace), $method['desc'], jsonapi_docs_args($method['args']), jsonapi_docs_returns($method['returns']));
}

function jsonapi_docs_args ($args) {
	if(empty($args)) {
		return "None";
	}
	
	$format = "<ul class='method-info-args-ul'>%s</ul>";
	$format_i = "<li><strong>%s</strong> %s</li>";
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