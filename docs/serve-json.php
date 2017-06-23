<?php

if($_GET['callback']) {
	echo $_GET['callback']."(".file_get_contents("console-info.json").")";
}