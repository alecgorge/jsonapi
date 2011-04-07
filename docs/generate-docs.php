<?php

require '../sdk/php/JSONAPI.php';

$input_file = "../src/methods.json";

$docs = jsonapi_docs(json_decode(file_get_contents($input_file), true));

?><!DOCTYPE html>
<html>
	<head>
		<title>JSONAPI API Docs</title>
		<link type="text/css" rel="stylesheet" name="stylesheet" href="styles.css" />
	</head>
	<body>
<?php echo $docs; ?>
	</body>
</html>
<?php

function jsonapi_docs($input_json) {
	$format = '<div id="jsonapi-docs" class="doc-wrapper">%s</div>';
	foreach($input_json as $method) {
		$r .= jsonapi_docs_method($method);
	}
	return sprintf($format, $r);
}

function jsonapi_docs_method ($method) {
	$format = <<<EOT
	<div class="method-wrapper">
		<ul class="method-info">
			<li class="method-info-name"><strong>Name: </strong>%s</li>
			<li class="method-info-desc"><strong>Description: </strong>%s</li>
			<li class="method-info-args"><strong>Arguments: </strong>%s</li>
			<li class="method-info-return"><strong>Returns: </strong>%s</li>
		</ul>
		<br class="clear" />
	</div>

EOT;
	return sprintf($format, $method['name'], $method['desc'], jsonapi_docs_args($method['args']), jsonapi_docs_returns($method['returns']));
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
		return "null";
	}
	
	return sprintf("<strong>%s</strong> %s", $returns[0], $returns[1]);
}