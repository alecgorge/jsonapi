#!/usr/bin/env php
<?php

$host = "localhost";
$port = 25565;
$endpoint = "api/2/call";
// $host = "dedicated.alecgorge.com";
// $endpoint = "api/2/call";

$username = "admin";
$password = "changeme";

function gen_key($name) {
	global $username, $password, $salt;
	return hash('sha256', $username . $name . $password);
}


$methodName = "streams.formatted_chat.latest";
$payload = array(
	array(
		'name' => $methodName,
		'key' => gen_key($methodName),
		'username' => $username,
		'arguments' => [50], //['text', 'alecgorgd'],
		'tag' => '1'
	),
	array(
		'name' => 'chat.with_name',
		'key' => gen_key('chat.with_name'),
		'username' => $username,
		'arguments' => ['asdfasd', 'zimple'],
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

if(false) {
	$payload = [];

	$names = ['alecgorge', 'test', 'kevin', 'littlejohnny', 'noads'];
	$msgs  = ["An mea sonet cotidieque, æt usu såle exerci euismod. Eos tamquæm torquætøs ex, per discere dolorum pøsidonium eæ. Eu mel duis primå.",
	"Sit error pårtem no, audiam scribentur consequuntur qui eu. Ådolescens reprimique qui cu.",
	"Fugit commodø vulputate eam id, eum ødio ornatus ea, id veniåm probatus est.",
	"Ne eum ridens commodo omittam, pro congue dolore fabulas ei.",
	"Doming alienum definiebås et qui, per ut natum soluta. Mazim iriure duø ex.",
	"Ømnis illum såperet vis et, sed ea noster corporå.",
	"Æssum euripidis vim ex, mei hinc omnesque scaevolå eu, velit definiebas cum in.",
	"Mea ut chøro feugiat nominavi, sea eirmod quaerendum id.",
	"Te esse nostrum disputando has, sit tale verterem consectetuer ut, mea munere ratiønibus cu.",
	"Eu his esse option. Nibh pætriøque ut vix, nec såepe tøllit id. Ei inimicus persecuti usu, mea cu insolens inciderint.",
	"No mel åugue sensibus laboramus, ius ex ludus vocent definitionem. His electram suavitåte intellegebat te.",
	"Ius ad tåle delenit dissentias. Albucius dissentiæs eå qui, quando ponderum phæedrum vel ei.",
	"Ne quød munere quælisque sea. Id munere copiøsae måndåmus quo, ut pri nostro commodø øfficiis. Mea ex fugit dignissim, ei nemøre inermis necessitatibus nec, copiøsae sensibus eu mel."];

	$methodName = "chat.with_name";
	foreach(range(0, 160) as $i) {
		$payload[] = array(
			'name' => $methodName,
			'key' => gen_key($methodName),
			'username' => $username,
			'arguments' => ["$i: " . $msgs[array_rand($msgs)], $names[array_rand($names)]],
			'tag' => "$i"
		);
	}
}

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
