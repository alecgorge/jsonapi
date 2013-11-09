#!/usr/bin/env php
<?php

$host = "localhost";
$port = 20059;
$endpoint = "api/2/call";
$host = "localhost";
$endpoint = "api/2/call";

$username = "admin";
$password = "changeme";

function gen_key($name) {
	global $username, $password, $salt;
	return hash('sha256', $username . $name . $password);
}


$methodName = "server.version";

$payload = array(
	array(
		'name' => $methodName,
		'key' => gen_key($methodName),
		'username' => $username,
		'arguments' => [],
		'tag' => '1'
	),
	array(
		'name' => 'players.name',
		'key' => gen_key('players.name'),
		'username' => $username,
		'arguments' => ['alecgorge'],
		'tag' => '2'
	),
	// array(
	// 	'name' => 'players.name',
	// 	'key' => gen_key('players.name'),
	// 	'arguments' => ["alecgorge"],
	// 	'username' => $username,
	// ),
	// array(
	// 	'name' => 'server.performance.tick_health',
	// 	'key' => gen_key('server.performance.tick_health'),
	// 	'username' => $username,
	// ),
	// array(
	// 	'name' => 'server.performance.memory.used',
	// 	'key' => gen_key('server.performance.memory.used'),
	// 	'username' => $username,
	// ),
	// array(
	// 	'name' => 'server.performance.memory.total',
	// 	'key' => gen_key('server.performance.memory.total'),
	// 	'username' => $username,i
	// ),
	// array(
	// 	'name' => 'server.performance.disk.used',
	// 	'key' => gen_key('server.performance.disk.used'),
	// 	'username' => $username,
	// ),
	// array(
	// 	'name' => 'server.performance.disk.size',
	// 	'key' => gen_key('server.performance.disk.size'),
	// 	'username' => $username,
	// )
);

$streamPayload = array(
	array(
		'name' => 'performance',
		'key' => gen_key('performance'),
		'username' => $username,
		'tag' => 'performance',
		'show_previous' => true,
	)
);

if(!(php_sapi_name() == 'cli' && empty($_SERVER['REMOTE_ADDR']))) {
	echo "<pre>";
}

$stream = false;
if($stream) {
	$url = sprintf("http://%s:%d/api/2/subscribe?json=%s", $host, $port, rawurlencode(json_encode($streamPayload)));
	echo $url ."\n";
	exit();	
}

$url = sprintf("http://%s:%d/%s?json=%s", $host, $port, $endpoint, rawurlencode(json_encode($payload)));
echo $url . "\n"; // "\n\n\n";
exit();

$c = curl_init($url);
curl_setopt($c, CURLOPT_PORT, $port);
curl_setopt($c, CURLOPT_HEADER, true);
curl_setopt($c, CURLOPT_RETURNTRANSFER, true);
curl_setopt($c, CURLOPT_TIMEOUT, 10);		

curl_setopt($c, CURLOPT_VERBOSE, 0);

$response = curl_exec($c);

// Then, after your curl_exec call:
$header_size = curl_getinfo($c, CURLINFO_HEADER_SIZE);
$header = substr($response, 0, $header_size);
$body = json_decode(substr($response, $header_size), true);

curl_close($c);

echo $header;

print_r($body);
