JSONAPI is a plugin for Bukkit that allows you to access data and other information about your server and your players through a simple, yet secure, HTTP API. This allows you to make **awesome websites**, **iPhone apps**, and a way for your **players to purchase goods online and automatically receive them in game**.

However, this plugin won't do all of that by itself. It is simply an API that allows you to assemble the features in a way that makes sense for your needs.

## Download

[Download JSONAPI v3.6.0 (for Minecraft 1.1-R6 and Minecraft 1.2)](http://alecgorge.com/minecraft/jsonapi/version/latest/)

You can find the source code on GitHub at [alecgorge/jsonapi](http://github.com/alecgorge/jsonapi). I accept pull requests!

## What features exist in JSONAPI?

### There are tons of API methods.
There are 146 different API methods in 8 different categories covering a wide range of features:

* [Chat](http://alecgorge.com/minecraft/jsonapi/apidocs/#package-Chat) (groups, prefixes, suffixes, etc.)
* [Economy](http://alecgorge.com/minecraft/jsonapi/apidocs/#package-Economy) (give money, create banks, etc.)
* [Permissions](http://alecgorge.com/minecraft/jsonapi/apidocs/#package-Permission%20methods) (control permissions and groups)
* [Edit signs and chests](http://alecgorge.com/minecraft/jsonapi/apidocs/#package-World%20Editing)
* Integration with drdanick's [Remote Toolkit](http://forums.bukkit.org/threads/admn-remotetoolkit-r10-a13-restarts-crash-detection-auto-saves-remote-console-1-2-3.674/) to enable the ability to turn the server on and off using API methods. *Note:* this requires the [JSONAPI_RTK](https://github.com/downloads/alecgorge/jsonapi_rtk/JSONAPI_RTK%20v1.1.zip) Remote Toolkit module.
* [Get a live stream](https://github.com/alecgorge/jsonapi/wiki/Stream-sources) of chat, connections and disconnections and/or the console.
* ...and many more standard features (ban, unban, inventory management, etc.)

### JSONAPI is well documented.

There are many guides along with an expansive technical reference available on the [wiki](https://github.com/alecgorge/jsonapi/wiki).

Every API method is documented and [viewable online](http://alecgorge.com/minecraft/jsonapi/apidocs/).

You can read a careful documentation of the [the request and response format on the wiki](https://github.com/alecgorge/jsonapi/wiki/Analyzing-the-jsonapi-request-and-response-format) if you are thinking about writing a new SDK or plan on rolling your own solution.

### There are easy to use SDKs.
SDKs allow for easy usage of all of JSONAPI's capabilities currently there are 4 SDKs:

* PHP: [JSONAPI.php](https://raw.github.com/alecgorge/jsonapi/master/sdk/php/JSONAPI.php) ([docs](http://alecgorge.com/minecraft/jsonapi/phpsdkdocs/jsonapi/jsonapi.html))
* .NET: [JSONAPI](https://github.com/alecgorge/jsonapi/tree/master/sdk/DotNet%203.5)
* JavaScript: [jsonapi.js](https://github.com/alecgorge/jsonapi/tree/master/sdk/js)
* Python: [MinecraftApi.py](https://raw.github.com/alecgorge/jsonapi/master/sdk/py/MinecraftApi.py)

### Easily integrate JSONAPI with other plugins
Using the API for JSONAPI, you can easily **add new methods** or **stream sources**. Check out the section "For plugin developers" at [the bottom of the wiki](https://github.com/alecgorge/jsonapi/wiki).

### There is an interactive test console.

Once you setup Adminium on your server, you can use [this test console](http://alecgorge.github.com/jsonapi/) to easily test all of the available API methods and view the JSON response.

## What else can you do with JSONAPI? *You can...*

* Integrate your website and your Minecraft server
* Control your server with your iPad/iPhone/iPod Touch through [Adminium](http://adminiumapp.com)
* Setup scripts that perform actions on your server, all through an easy to use API!

## What uses JSONAPI?

* [Adminium](http://adminiumapp.com/)
* [SpaceBukkit](http://spacebukkit.xereo.net/)
* [HeroicDeath](http://dev.bukkit.org/server-mods/heroicdeath/)
* SimpServ