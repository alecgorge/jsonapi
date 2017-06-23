<?php
require('../sdk/php/JSONAPI.php');
$json = new JSONAPI('localhost', '20060', 'admin', 'demo', '');
$key = $json->createKey("console");

$tcp = fsockopen('localhost',20060);
stream_set_blocking ($tcp,1);

var_dump("/api/subscribe?source=console&key=".$key."\n");
if (!$tcp) {
    echo "$errstr ($errno)<br />\n";
} else {
    echo fwrite($tcp, "/api/subscribe?source=console&key=".$key."\n");

    while (!feof($tcp)) {
        echo fgets($tcp, 128);
    }
    fclose($tcp);
}