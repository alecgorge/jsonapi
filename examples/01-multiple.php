<?php

require('../sdk/php/JSONAPI.php');

$api = new JSONAPI("localhost", 20059, "admin", "demo", ""); // host/ip, port, username, password, salt
var_dump($api->callMultiple(array(
	"getPlayerLimit",
	"getPlayerCount"
	), array(
	array(),
	array(),
)));