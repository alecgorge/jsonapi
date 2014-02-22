---
title: JSONAPI
template: index.jade
---

JSONAPI is a plugin for Bukkit that allows you to access data and other information about your server and your players through a simple, yet secure, HTTP API. This allows you to make **awesome websites**, **iPhone apps**, and a way for your **players to purchase goods online and automatically receive them in game**.

However, this plugin won't do all of that by itself. It is simply an API that allows you to assemble the features in a way that makes sense for your needs.

## Download

[Download JSONAPI on the releases page](https://github.com/alecgorge/jsonapi/releases)

You can find the source code on GitHub at [alecgorge/jsonapi](http://github.com/alecgorge/jsonapi). I accept pull requests!

## What features exist in JSONAPI?

### There are tons of API methods.
There are 146 different API methods in 8 different categories covering a wide range of features:

* [Read the standard API documenation](/apidocs)
* [Get a live stream](https://github.com/alecgorge/jsonapi/wiki/Stream-sources) of chat, connections and disconnections and/or the console.
* ...and many more standard features (ban, unban, inventory management, etc.)

### JSONAPI is well documented.

There are many guides along with an expansive technical reference available on the [wiki](https://github.com/alecgorge/jsonapi/wiki).

Every API method is documented and [viewable online](/apidocs).

You can read a careful documentation of the [the request and response format on the wiki](https://github.com/alecgorge/jsonapi/wiki/Analyzing-the-jsonapi-request-and-response-format) if you are thinking about writing a new SDK or plan on rolling your own solution.

### There are easy to use SDKs.
SDKs allow for easy usage of all of JSONAPI's capabilities currently there are 4 SDKs:

* PHP: [JSONAPI.php](https://raw.github.com/alecgorge/jsonapi/master/sdk/php/JSONAPI.php) ([docs](http://alecgorge.com/minecraft/jsonapi/phpsdkdocs/jsonapi/jsonapi.html))
* .NET: [JSONAPI](https://github.com/alecgorge/jsonapi/tree/master/sdk/DotNet%203.5)
* JavaScript: [jsonapi.js](https://github.com/alecgorge/jsonapi/tree/master/sdk/js)
* Python: [MinecraftApi.py](https://raw.github.com/alecgorge/jsonapi/master/sdk/py/MinecraftApi.py)

### Easily integrate JSONAPI with other plugins.
Using the API for JSONAPI, you can easily **add new methods** or **stream sources**. Check out the section "For plugin developers" at [the bottom of the wiki](https://github.com/alecgorge/jsonapi/wiki).

### There is an interactive test console.

Once you setup Adminium on your server, you can use [this test console](http://alecgorge.github.com/jsonapi/) to easily test all of the available API methods and view the JSON response.

## What else can you do with JSONAPI? *You can...*

* Integrate your website and your Minecraft server
* Control your server with your iPad/iPhone/iPod Touch through [Adminium](http://adminiumapp.com)
* Setup scripts that perform actions on your server, all through an easy to use API!

# JSONAPI API v2 usage

The second major iteration of the API is very different from the original. One of the biggest features of the v2 API is [support for granular permissions](#Granular-Permission-Background) but there are many other improvements such as the HTTP **and** WebSocket API over port `25565`.

Many changes had to be made to properly support permissions. It was important to maintain the ability to call multiple API methods in one HTTP request, but it was also important to prevent the whole request from failing if only one API method call failed due to permissions.

## Connection Options

### HTTP API (JSON and JSONP)

> This is best and simplest option if you don't wish to make use of JSONAPI's streaming data.

It is now recommended to make API requests over the Minecraft join port (`25565` by default). Even though this sounds unusual, over 16 months of research and experimentation has gone into making HTTP API requests work seamlessly over the Minecraft join port without affecting gameplay.

Of course you can still use port `20059` as you always have, but sometimes using port `25565` will be easier. It is one less port to forward!

#### Usage

To consume the HTTP API, simply send [a JSON Request payload](#JSON-Request-Structure) in the `GET` query parameter `json` or in `POST` body as raw JSON. `POST`-ing the data is recommend.

Data is returned as [a JSON Response payload](#JSON-Response-Structure) and is **always** an array&mdash;even when you only called one API method.

##### GET Example

###### cURL

```
curl -v "http://localhost:25565/api/2/call?json=%5B%7B%22name%22%3A%22server.version%22%2C%22key%22%3A%221f33a4a68d95a26782fdb41686f4f01d3ffaff14b9eec5c2ddf367840d1adcfc%22%2C%22username%22%3A%22admin%22%2C%22arguments%22%3A%5B%5D%2C%22tag%22%3A%22sampleTag%22%7D%5D"
```

###### Request

```
GET /api/2/call?json=%5B%7B%22name%22%3A%22server.version%22%2C%22key%22%3A%221f33a4a68d95a26782fdb41686f4f01d3ffaff14b9eec5c2ddf367840d1adcfc%22%2C%22username%22%3A%22admin%22%2C%22arguments%22%3A%5B%5D%2C%22tag%22%3A%22sampleTag%22%7D%5D HTTP/1.1
User-Agent: curl/7.21.4 (x86_64-apple-darwin12.2.0) libcurl/7.21.4 OpenSSL/0.9.8y zlib/1.2.5 libidn/1.20
Host: localhost:25565
Accept: */*
```

###### Response

```
HTTP/1.1 200 OK
Content-Type: application/json
Date: Sat, 12 Oct 2013 07:15:22 GMT
Access-Control-Allow-Origin: *
Content-Length: 140

[{"result":"success","is_success":true,"source":"server.version","tag":"sampleTag","success":"git-Bukkit-1.6.2-R1.0-b2879jnks (MC: 1.6.2)"}]
```

##### POST Example

###### cURL

```
curl -X "POST" "http://localhost:25565/api/2/call" -H "Content-Type: application/json" -d "[{\"name\":\"server.version\",\"key\":\"1f33a4a68d95a26782fdb41686f4f01d3ffaff14b9eec5c2ddf367840d1adcfc\",\"username\":\"admin\",\"arguments\":[],\"tag\":\"sampleTag\"}]"
```

###### Raw Request

```
POST /api/2/call HTTP/1.1
User-Agent: curl/7.21.4 (x86_64-apple-darwin12.2.0) libcurl/7.21.4 OpenSSL/0.9.8y zlib/1.2.5 libidn/1.20
Host: localhost:25565
Accept: */*
Content-Type: application/json
Content-Length: 152

[{"name":"server.version","key":"1f33a4a68d95a26782fdb41686f4f01d3ffaff14b9eec5c2ddf367840d1adcfc","username":"admin","arguments":[],"tag":"sampleTag"}]
```

###### Raw Response

```
HTTP/1.1 200 OK
Content-Type: application/json
Date: Sat, 12 Oct 2013 07:15:22 GMT
Access-Control-Allow-Origin: *
Content-Length: 140

[{"result":"success","is_success":true,"source":"server.version","tag":"sampleTag","success":"git-Bukkit-1.6.2-R1.0-b2879jnks (MC: 1.6.2)"}]
```

#### Using the HTTP API via JavaScript

JSONAPI supports making API calls via JavaScript thanks to the JavaScript SDK. If you are familiar with making AJAX requests in JavaScript, you will know that you can't typically make AJAX requests to other domains. This could pose a problem if your website is running on a different domain than your Minecraft server. Fortunately JSONAPI supports 2 workarounds for this issue: [Cross-origin resource sharing](http://en.wikipedia.org/wiki/Cross-origin_resource_sharing) and [JSONP](http://en.wikipedia.org/wiki/JSONP).

##### Cross-origin resource sharing

Making use of CORS is really easy if you are working [browsers that support CORS](http://caniuse.com/#feat=cors). If you need to support IE 8 and IE 9 you can use jQuery and the [XDomainRequest jQuery Plugin](https://github.com/MoonScript/jQuery-ajaxTransport-XDomainRequest).

Because of JSONAPI's CORS support, you can simply make AJAX calls to your Minecraft the same way you would make an AJAX call to your own script.

##### JSONP

Using JSONP is very simple. Simply generate a `GET` HTTP request as shown above and then add a query parameter called `callback`. The value of `callback` is the name of hte function to wrap the response in.

###### cURL

```
curl -v "http://localhost:25565/api/2/call?callback=myCallbackFunc&json=%5B%7B%22name%22%3A%22server.version%22%2C%22key%22%3A%221f33a4a68d95a26782fdb41686f4f01d3ffaff14b9eec5c2ddf367840d1adcfc%22%2C%22username%22%3A%22admin%22%2C%22arguments%22%3A%5B%5D%2C%22tag%22%3A%22sampleTag%22%7D%5D"
```

###### Raw Request

```
GET /api/2/call?callback=myCallbackFunc&json=%5B%7B%22name%22%3A%22server.version%22%2C%22key%22%3A%221f33a4a68d95a26782fdb41686f4f01d3ffaff14b9eec5c2ddf367840d1adcfc%22%2C%22username%22%3A%22admin%22%2C%22arguments%22%3A%5B%5D%2C%22tag%22%3A%22sampleTag%22%7D%5D HTTP/1.1
User-Agent: curl/7.21.4 (x86_64-apple-darwin12.2.0) libcurl/7.21.4 OpenSSL/0.9.8y zlib/1.2.5 libidn/1.20
Host: localhost:25565
Accept: */*
```

###### Raw Response

```
HTTP/1.1 200 OK
Content-Type: application/json
Date: Sat, 12 Oct 2013 07:27:41 GMT
Access-Control-Allow-Origin: *
Content-Length: 157

myCallbackFunc([{"result":"success","is_success":true,"source":"server.version","tag":"sampleTag","success":"git-Bukkit-1.6.2-R1.0-b2879jnks (MC: 1.6.2)"}]);
```

### Socket API

> *Recommended for advanced users only!*
>
> 95% of people will be perfectly fine using the HTTP API, the Post-hook API and the WebSocket API.

The Socket API is a raw TCP socket that you can use to communicate with JSONAPI. It listens on port `20060` by default (1 higher than the port set in `config.yml`).

The Socket API is very similiar to the WebSocket API below. The only difference is that instead of sending text frames back and forth, the Socket API is line based. Lines are terminated with `\r\n`.

### Stream Pusher API

> This is a useful option when you want to consume JSONAPI's streaming data, but you are using a language such as PHP, where you typically can't or don't want to run a process for an extended period of time.
 
The Stream Pusher API will send a POST request to a URL specify with a payload of JSONAPI stream data every 30 seconds or 500 stream messages, whichever comes first.

**TODO** Document this more and expose the config file in a more useful yay

### WebSocket API

The WebSocket API is actually quite similiar to the Socket API. The connection path for the WebSocket API looks like this:

```
ws://localhost:25565/api/2/websocket
```

or this:

```
ws://localhost:20061
```

Obviously, replace `localhost` with your server's IP address or hostname and replace `25565` with the join port. Replace `20061` with the port in `config.yml` plus 2 (`20059 + 2 == 20061` is the default).

#### Requests

The WebSocket API is frame based. Each text frame sent to JSONAPI should look a HTTP `GET` URL:

```
/api/2/call?json=%5B%7B%22name%22%3A%22server.version%22%2C%22key%22%3A%221f33a4a68d95a26782fdb41686f4f01d3ffaff14b9eec5c2ddf367840d1adcfc%22%2C%22username%22%3A%22admin%22%2C%22arguments%22%3A%5B%5D%2C%22tag%22%3A%22sampleTag%22%7D%5D
```

This format can be simplified to `/api/2/call?json=` suffixed by a `RFC 3986` URL encoded [a JSON Request payload](#JSON-Request-Structure) formatted to be on a single line ending with a `\r\n`.

This odd format is necessary to keep things similiar to the HTTP API and to also allow for streams:

```
/api/2/subscribe?json=%5B%7B%22name%22%3A%22performance%22%2C%22key%22%3A%22059198890c8a68512ca1bef01f57aef81c14fec438caaf9b7cd54a662b638fa7%22%2C%22username%22%3A%22admin%22%2C%22tag%22%3A%22performance%22%2C%22show_previous%22%3Atrue%7D%5D
```

#### Responses

[A JSON Response payload](#JSON-Response-Structure) will be sent in a single frame. Note that for streams, they are not sent in an array but as one response object per line and could appear in any frame.

## JSON Request Structure

The API request structure is very simple and is used in all the different way to access JSONAPI. However, you will not need to even know how this works if you use one Let's start by looking at a simple example of a single API method call formatted for easier reading. Extraneous whitespace is fine to remove, as with any JSON:

```
[
    {
        "name": "server.version",
        "key": "1f33a4a68d95a26782fdb41686f4f01d3ffaff14b9eec5c2ddf367840d1adcfc",
        "username": "admin",
        "arguments": [],
        "tag": "sampleTag"
    }
]
```

The first thing to notice about the request structure is that at the root of each request is a JSON array. This array should contain request objects such as the one in this example. Each request object has up to 5 key-value pairs:

* `name`: This is the name of the JSONAPI API method that you want to call. This comes from the list of [JSONAPI API methods](http://mcjsonapi.com/apidocs/).
* `key`: This is a [`sha256`](http://en.wikipedia.org/wiki/SHA-2) hash of the API method, JSONAPI username and JSONAPI password. [More on this below](#JSONAPI-Key-Format).
* `username`: This is the JSONAPI username of the user that is making this request. It must be the same as the `username` used when generating the `key`.
* `arguments`: This must be an array of all the arguments required the API method. In this example there are no arguments to this API method.
* `tag`: This key-value pair is *optional*. If you set a tag in the request, the corresponding response will also have the same tag in it. This is useful for streaming situations where you may need to callbacks to certain requests complete.

The response to a request like this will look similar to this:

```
[
    {
        "result": "success",
        "is_success": true,
        "source": "server.version",
        "tag": "sampleTag",
        "success": "git-Bukkit-1.6.2-R1.0-b2879jnks (MC: 1.6.2)"
    }
]
```

You can [read more about the response format](#JSON-Response-Structure) below.

### A more complex example

In this example, we call multiple API methods and even pass arguments to some API methods:

```
[
    {
        "name": "players.name",
        "key": "111af5fa82ed1b0c6b23564d491d153d8440f5cae85a1375b8dba6dc307ab8c5",
        "arguments": ["alecgorge"],
        "username": "admin"
    },
    {
        "name": "players.name.inventory.slots.slot.set",
        "key": "f292bb79a3ac1d0efa534d7f06ce63a52ebf91f4258694c285b7882a184544ba",
        "username": "admin",
        "arguments": ["alecgorge", 0, 35, 6, 0, 2],
        "tag": "test"
    },
    {
        "name": "players.name",
        "key": "111af5fa82ed1b0c6b23564d491d153d8440f5cae85a1375b8dba6dc307ab8c5",
        "arguments": ["alecgorge"],
        "username": "admin"
    }
]
```

In this example, we call 3 API methods: [`players.name`](http://mcjsonapi.com/apidocs/#players.name), [`players.name.inventory.slots.slot.set`](http://mcjsonapi.com/apidocs/#players.name.inventory.slots.slot.set) and [`players.name`](http://mcjsonapi.com/apidocs/#players.name) again. The first and last API methods give us information about a player including his/her inventory. The second API method sets the `0`th slot of `alecgorge`'s inventory to 2 pink wools (wool's ID is `35` and `6` is the data value for pink wool).

Here is a sample response, with irrelvant information remove:

```
[
    {
        "result": "success",
        "is_success": true,
        "source": "players.name",
        "success": {
            ... player info ...
            "inventory": {
                ... item in hand info ...
                "inventory": [
                    null, 
                    ... other inventory slots ...
                ],
            ... player info ...
        }
    },
    {
        "result": "success",
        "is_success": true,
        "source": "players.name.inventory.slots.slot.set",
        "tag": "test",
        "success": true
    },
    {
        "result": "success",
        "is_success": true,
        "source": "players.name",
        "success": {
            ... player info ...
            "inventory": {
                ... item in hand info ...
                "inventory": [
                    {
                        "enchantments": {},
                        "amount": 2,
                        "durability": 6,
                        "type": 35,
                        "dataValue": 6
                    },
                    ... other inventory slots ...
                ],
            ... player info ...
       }
    }
]
```

The important things to note here is that `alecgorge` didn't have anything in his `0`th slot initially so it showed as null, but after `players.name.inventory.slots.slot.set` was called, the slot was filled.

API methods are called in order when they are in an array, but no promises are made about API method calls in separate requests.

You can [read more about the response format](#JSON-Response-Structure) below.

### JSONAPI Key Format

You can calulate the `key` necessary for JSONAPI API requests by taking the `sha256` hash of string formed by concatenating the JSONAPI username, API method name or stream name and JSONAPI password. The output should be hex encoded and be entirely in lowercase.

Here is a sample function to calculate a JSONAPI key in `PHP`:

```
function generate_jsonapi_key($username, $password, $api_method_or_stream_name) {
	return hash('sha256', $username . $api_method_or_stream_name . $password);
}
```

This example can easily be carried over to any language.

## JSON Stream Request Structure

Subscribing to streams is very similar to calling API methods. For the `name` key you provide the name of `stream` that you subscribe to.

The stream subscription object also has one extra key: `show_previous`. When `show_previous` is set to `true` in the request payload, the server will automatically send up to the latest 150 messages in the stream. When `show_previous` is set to `false`, only messages that occur in the stream after the subscription request is processed will be sent.

Otherwise everything is the same:



## JSON Response Structure

The reponse structure matches one-to-one with the request structure&mdash;that is for every request object there is exactly one response object.

Each response object will be in the position in the response array as the request object was in the request array.

There are a minimum of 4 key-value pairs in each response object and a maximum of 5:

* `result`: This is a string that will either be "success" or "error". You can the value of this key as the key to find the response data regardless of whether or not the request succeeded.
* `is_success`: This is a boolean that will either be `true` or `false` to represent whether or not the API method call succeeded.
* `source`: This is the API method name in the request object.
* `tag`: This key-value pair is *optional*. If the request object had a `tag` key, the response object will have the same `tag`.
* `success`: This key-value pair only appears if the request was successful. It contains API method specific data. It could be an array, futher objects, a number, a boolean or a string.
* `error`: This key-value pair only appears if the request failed. It has two sub-keys:
  * `message`: This is a English description of the error that occured.
  * `code`: This is an integer that describes the type of failure that occured. There are 10 error codes:
    * `1`: Page not found
    * `2`: Invalid JSON submitted in request
    * `3`: Server offline
    * `4`: API Error
    * `5`: InvocationTargetException
    * `6`: Other caught exception
    * `7`: Method does not exist
    * `8`: The API key is wrong. This indicates that either the username and password combination was not found on the server or that key does not match the provided username and API method name provided in the request payload
    * `9`: Not allowed, but correct API key. This indicates that although the key is valid, the requesting user is not allowed to access the request API method.
    * `10`: Missing username from payload

Here is a sample failed request:

```
[
    {
        "result": "error",
        "is_success": false,
        "error": {
            "message": "Invalid username, password or salt.",
            "code": 8
        },
        "source": "players.name"
    }
]
```

Note that in an array you can have failures and successes:

```
[
    {
        "result": "success",
        "is_success": true,
        "source": "server.version",
        "tag": "1",
        "success": "git-Bukkit-1.6.2-R1.0-b2879jnks (MC: 1.6.2)"
    },
    {
        "result": "error",
        "is_success": false,
        "error": {
            "message": "Invalid username, password or salt.",
            "code": 8
        },
        "source": "server.version",
        "tag": "2"
    }
]
```

## JSON Stream Response Structure

Streams provide slightly different responses. Stream responses are not wrapped in an array and one is sent per line.

Here is an example of a few lines from the `performance` stream:

```
{"result":"success","source":"performance","tag":"performance","success":{"memoryUsage":110.50666046142578,"expectedTicks":600,"time":1381567969,"clockRate":19.999333355554814,"diskMax":238552.5078125,"error":0.0,"players":0,"expectedTime":30000,"expectedClockRate":20.0,"memoryMax":227.5625,"elapsedTicks":600,"diskUsage":233016.75,"elapsedTime":30001}}
{"result":"success","source":"performance","tag":"performance","success":{"memoryUsage":112.44316101074219,"expectedTicks":600,"time":1381567974,"clockRate":19.999333355554814,"diskMax":238552.5078125,"error":0.0,"players":0,"expectedTime":30000,"expectedClockRate":20.0,"memoryMax":227.5625,"elapsedTicks":600,"diskUsage":233016.75,"elapsedTime":30001}}
{"result":"success","source":"performance","tag":"performance","success":{"memoryUsage":104.84185028076172,"expectedTicks":600,"time":1381567979,"clockRate":19.999333355554814,"diskMax":238552.5078125,"error":0.0,"players":0,"expectedTime":30000,"expectedClockRate":20.0,"memoryMax":227.5625,"elapsedTicks":600,"diskUsage":233016.7578125,"elapsedTime":30001}}
{"result":"success","source":"performance","tag":"performance","success":{"memoryUsage":106.78044128417969,"expectedTicks":600,"time":1381567984,"clockRate":19.999333355554814,"diskMax":238552.5078125,"error":0.0,"players":0,"expectedTime":30000,"expectedClockRate":20.0,"memoryMax":227.5625,"elapsedTicks":600,"diskUsage":233017.203125,"elapsedTime":30001}}
{"result":"success","source":"performance","tag":"performance","success":{"memoryUsage":112.02610778808594,"expectedTicks":600,"time":1381567989,"clockRate":19.999333355554814,"diskMax":238552.5078125,"error":0.0,"players":0,"expectedTime":30000,"expectedClockRate":20.0,"memoryMax":227.5625,"elapsedTicks":600,"diskUsage":233017.20703125,"elapsedTime":30001}}
```

# Granular Permissions Background

In this document the `user` is the username accessing JSONAPI. The groups the `user` belongs to determine what features the `user` can access.

All permission nodes are tied to a specific set of JSONAPI methods and streams. Unless a group belongs to the `ALLOW_ALL` group, it is assumed the user only has access to the specific API methods whitelisted by these permission nodes.

You can modify what groups users belong to in `users.yml` and you can modify what permission nodes groups have in `groups.yml`. You cannot modify what methods and streams are allowed by each permission node. However, [if you open up an issue on GitHub](https://github.com/alecgorge/jsonapi/issues/new) it will likely be implemented.

# Configuring JSONAPI for Permissions Usage

Please note that these permissions **only** work `v2` of the API (`/api/2/call` and `/api/2/subscribe`) so it is important to set `use-new-api` to `true` in `config.yml` if you want the permission to be properly enforced.

**If you do not set `use-new-api` to `true`, anyone with a valid JSONAPI username and password will be able to access any API method using the original, deprecated API.**

# Permission Nodes

## view_player_information

### Methods

* `players.name`

This allows the user to view all basic information about a player: health, food, banned?, opped?, etc. Although this API call allows you to view a player's inventory. This permission node alone is not enough to view the inventory in Adminium.

## view_map

### Methods

* `dynmap.host`
* `dynmap.port`

This allows the user to access API methods to automatically determine the dynmap url if dynmap is installed.

In Adminium this is used to determine whether or not the "Map" menu item should be shown.


## view_server_performance

### Methods

* `server.performance.disk.free`
* `server.performance.disk.size`
* `server.performance.disk.used`
* `server.performance.memory.used`
* `server.performance.memory.total`
* `server.performance.tick_health`

### Streams

* `performance`

This allows the user to view graphs and information about the server's performance: disk usage, RAM usage and TPS.


## view_banned_players

### Methods

* `players.banned.names`

This allows the user to view a list of the names of banned players.

## modify_player_inventory

### Methods

* `players.name.inventory.slots.slot.clear`
* `players.name.inventory.slots.slot.set`
* `players.name.inventory.slots.slot.enchant`

This allows the user to give, modify, enchant and take a player's items.

## view_player_world_info

### Methods

* `players.name`
* `players.online`
* `players.online.count`
* `players.online.names`

This allows the user to view the information of online players.

## view_severe_log_history

This allows the user the most recent (up to 150) `SEVERE` messages on the server.

## file_management

### Methods

* `files.read`
* `files.read_binary`
* `files.append`
* `files.create`
* `files.create_folder`
* `files.write`
* `files.write_binary`
* `files.move`

This gives the user **full file system access**. They can read, create, edit and delete any file on the Minecraft server.

## view_push_notification_settings

This allows a user to view his/her push notifications settings for Adminium but not change them.

## power_management

### Methods

* `remotetoolkit.rescheduleServerRestart`
* `remotetoolkit.restartServer`
* `remotetoolkit.startServer`
* `remotetoolkit.stopServer`

Turn the server off and on using Remote Toolkit. Remote Toolkit and JSONAPI_RTK needs to be properly setup.

## view_whitelisted_players

### Methods

* `players.whitelisted.names`

This allows the user to view a list of the names of whitelisted players.

## view_calladmin_history

This allows the user the most recent (up to 150) `/calladmin <reason>` usages by players on the server.

## recieve_push_notifications

This allows a user to recieve push notifications.

## use_player_actions

### Methods

* `players.name.op`
* `players.name.deop`
* `players.name.send_message`
* `players.name.kick`
* `players.name.ban`
* `players.name.pardon`
* `players.name.whitelist`
* `players.name.unwhitelist`
* `players.name.teleport_to.to_name`
* `players.name.teleport_world`

This allows a user to take actions on a player. With this permission the user can op, deop, send a PM, kick ban, pardon, whitelist, unwhitelist and teleport a player.

## view_player_groups

### Methods

* `groups.all`

This allows the user to see a list of all the name of all the players, organized by group.

## view_offline_players

* `players.offline`
* `players.offline.name`
* `players.offline.names`

This allows the user to see offline players' names and their corresponding information.

## modify_player_information

### Methods

* `players.name.set_level`
* `players.name.set_food_level`
* `players.name.set_health`
* `players.name.set_game_mode`

This allows the user to manipulate a player's level, food, health and game mode.

## configure_push_notifications

This allows the user to manipulate his/her push notification settings for Adminium.

## speak_in_chat

### Methods

* `chat.with_name`

This allows the user to speak in chat.

## change_chat_name

This allows the user to use something other than their JSONAPI username when chatting.

## view_online_player_list

* `players.online.names`
* `players.online.count`
* `server.settings.max_players`

This allows the user to view the names and number of players current on the server.

## view_worlds

* `worlds.all`
* `worlds.names`
* `worlds.world`

This allows the user to view a list of worlds on the server and basic information about each world: seed, time of day, time, difficulty, etc.

## modify_worlds

* `worlds.world.set_time`
* `worlds.world.set_difficulty`

This allows the user to change the time and difficulty of a world on the server.

## modify_plugins

* `plugins.name.enable`
* `plugins.name.disable`

This allows the user to enable or disable plugins by name.

## install_plugins

This allows the user to install a plugin from a URL.

## view_player_inventory

This allows the user to view a player's inventory, including details about each item. This doesn't allow the user to change the inventory or any item.

## view_player_enderchest

Same as inventory but for enderchests. This isn't currently used.

## view_chat

This allows the user to subscribe to the chat stream and view the server's chat in realtime.

## whitelisted_player_actions

This allows the user to remove players from the whitelist.

## view_player_permissions_and_groups

This allows the user to see the permission nodes and groups a user belongs to. Information provided by Vault and the Bukkit permission API.

## view_player_bank_information

This allows the user to see a player's bank balance. Information provided by Vault.

## modify_player_bank_information

This allows the user to withdraw or deposit into a player's bank account.

## modify_player_permissions_and_groups

This allows the user to add and remove permission nodes and groups from a player.

## view_jsonapi_users_and_groups

This allows the user to view all the JSONAPI users and groups on the server. This includes passwords and allowed streams, groups and permission nodes.

## modify_jsonapi_users_and_groups

This allows the user to add and remove permissions, methods and streams from groups; add and remove groups from a user and set a user's password.

## modify_group

This allows the user to modify a permission group using Vault.

## view_plugins

This allows the user to view a plugin and its information.

## view_console

This allows the user to view the real time console.

## banned_player_actions

This allows the user to pardon banned players.

## reload_server 

This allows the user to do the equivlent of `/reload`


# A big thanks to YourKit!

YourKit is kindly supporting JSONAPI open source project with its full-featured Java Profiler.
YourKit, LLC is the creator of innovative and intelligent tools for profiling
Java and .NET applications. Take a look at YourKit's leading software products:
[YourKit Java Profiler](http://www.yourkit.com/java/profiler/index.jsp) and
[YourKit .NET Profiler](http://www.yourkit.com/.net/profiler/index.jsp)</a>.

[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/alecgorge/jsonapi/trend.png)](https://bitdeli.com/free "Bitdeli Badge")

