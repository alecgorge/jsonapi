#!/bin/bash

php -f generate-docs.php > html/index.html
php -f generate-json.php > json/console-info.json

