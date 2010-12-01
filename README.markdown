# JSONApi
JSONApi is a simple, lightweight way to provide a JSON interface to a Minecraft server with hMod installed.

This plugin was created to be an alternative to CraftAPI. However, JSONApi differs from CraftAPI in that
everything in this plugin is JSON instead of XML-RPC. This one doesn't seem to run out of memory either :). But a huge thanks to sk89q for the binding of all the actions to java methods.
All of his work is in the XMLRPC* files.

Also requests are formed from URLs so you only ever have to do GET requests.

## Notes
The default port is 20059.

**_Always remember to encode the values of the GET variables to make them URL safe!_**

## JSONP

For JSONP calls, simply add an GET variable "callback" to be the function that wraps around the response. This works well for jQuery/Javascript and cross-site scripting.

Example

    http://localhost:20059/api/call?method=server.getPlayerCount&username=admin&password=test&callback=myCallbackFunction
    
Will return

    myCallbackFunction(1)

## Standard Request Format
For standard (non-streaming) requests the URL format looks like this:
	
	http://host:port/api/call?method=methodName&args=%5B%22urlEncodedjsonEncodedArrayOfArgs%22%5D&username=username&password=password

You can see the list of available methods below.

Here is a sample request this time, _with args_:

	http://localhost:20059/api/call?method=player.sendMessage&args=%5B%22alecgorge%22,%22hello there%22%5D&username=admin&password=test
	
**Notes:**

* %5B is a url encoded [
* %5D is a url encoded ]
* %22 is a url encoded "

## Standard Response Format	
Standard API calls will return one of the following.

These responses are based off of this request:
	
	http://localhost:20059/api/call?method=server.getPlayerCount&username=admin&password=test

Invalid username/password:

	403 Forbidden
	{"result":"error","error":"Invalid username\/password."}

Method not found or there is no args GET variable present:

	404 Not Found
	{"result":"error","error":"You need to pass a valid method and an array arguments."}
	
Request successful:

	200 OK
	{"result":"success",source:"server.getPlayerCount","success":0}

## Streaming Request Format
To request a stream you have the following options:

* "chat" Every time something is said in chat you get a new line. 
* "console" A mirror of the console output
* "commands" Similar to chat, but for commands
* "connections" A new line for each connection/disconnection

Format:

	http://host:post/api/subscribe?source=sourceOption&username=username&password=password
	
## Streaming Response Format
The response never fully loads, but as more and more events "stream" in another line is added (lines separated by \r\n).

The following responses are based off of this request:

	http://localhost:20059/api/subscribe?source=chat&username=admin&password=test

Response:

	{"source":"chat","data":{"message":"hi","player":"alecgorge"}}
	{"source":"chat","data":{"message":"test","player":"alecgorge"}}
	
### Other Response Formats

**console**

	{"source":"console","data":{"line":"THE REPLY IS YES"}}
	{"source":"console","data":{"line":"2010-11-24 23:02:18 [INFO] alecgorge [\/127.0.0.1:56487] logged in with entity id 7"}}
	{"source":"console","data":{"line":"Player count: 1"}}
	{"source":"console","data":{"line":"2010-11-24 23:02:34 [INFO] CONSOLE: Stopping the server.."}}
	{"source":"console","data":{"line":"2010-11-24 23:02:34 [INFO] Stopping server"}}
	{"source":"console","data":{"line":"2010-11-24 23:02:34 [INFO] Saving chunks"}}
	
	
**commands**

	{"source":"commands","data":{"player":"alecgorge","command":"\/help"}}
	{"source":"commands","data":{"player":"alecgorge","command":"\/test"}}

**connections**

	{"source":"connections","data":{"player":"alecgorge","action":"connect"}}
	{"source":"connections","data":{"player":"alecgorge","action":"disconnect"}}
	{"source":"connections","data":{"player":"alecgorge","action":"connect"}}
	{"source":"connections","data":{"player":"alecgorge","action":"disconnect"}}
	
## Testing	
The easiest way to test your streaming API calls on Linux and Mac is using cURL from the command line. Just run ''curl url'' and you will see the output as it is given.
On Windows, I use cURL through cygwin. However if you have msysgit, you can launch the git bash and use curl there.

For testing standard API calls, just use your favorite browser. 

## Available Methods
These were all shamelessly ripped from CraftAPI (thanks sk89q!). I haven't tested all of them yet so I am not sure how structs and more complex objects are handled.

	int        minecraft.getBlockID(int, int, int)
	base64     minecraft.getCuboidIDs(int, int, int, int, int, int)
	int        minecraft.getHighestBlockY(int, int)
	int        minecraft.getTime()
	boolean    minecraft.setBlockID(int, int, int, int)
	boolean    minecraft.setCuboidIDs(int, int, int, int, int, int, base64)
	boolean    minecraft.setTime(int)
	boolean    player.broadcastMessage(string)
	array      player.getAccessibleKits(string)
	array      player.getAccessibleWarps(string)
	struct     player.getInventory(string)
	struct     player.getPlayerInfo(string)
	array      player.getPlayerNames()
	array      player.getPlayers()
	boolean    player.giveItem(string, int, int)
	boolean    player.giveItemDrop(string, int, int)
	boolean    player.kick(string, string)
	boolean    player.removeInventoryItem(string, int, int)
	boolean    player.removeInventorySlot(string, int)
	boolean    player.sendMessage(string, string)
	boolean    player.teleportTo(string, double, double, double, double, double)
	boolean    player.teleportTo(string, double, double, double)
	boolean    player.toggleMute(string)
	boolean    server.addGroup(string, string, array, array, boolean, boolean, boolean, boolean)
	boolean    server.addKit(string, string, struct, int)
	boolean    server.addToReserveList(string)
	boolean    server.addToWhitelist(string)
	boolean    server.disablePlugin(string)
	boolean    server.enablePlugin(string)
	array      server.getAllowedItems()
	struct     server.getBan(string, string)
	array      server.getDisallowedItems()
	struct     server.getKit(string)
	string     server.getMotd()
	int        server.getPlayerCount()
	int        server.getPlayerLimit()
	struct     server.getPlugin(string)
	array      server.getPlugins()
	struct     server.getSpawnLocation()
	int        server.getSpawnProtectionSize()
	string     server.getWhitelistMessage()
	boolean    server.hasKits()
	boolean    server.hasReserveList()
	boolean    server.hasTimerExpired(string)
	boolean    server.hasWarps()
	boolean    server.hasWhitelist()
	boolean    server.isBanned(string, string)
	boolean    server.isWhitelistEnabled()
	boolean    server.modifyBan(string, string, string, int)
	boolean    server.modifyKit(string, string, string, struct, int)
	boolean    server.reloadBanList()
	boolean    server.reloadGroups()
	boolean    server.reloadHomes()
	boolean    server.reloadKits()
	boolean    server.reloadPlugin(string)
	boolean    server.reloadReserveList()
	boolean    server.reloadWarps()
	boolean    server.reloadWhitelist()
	boolean    server.removeFromReserveList(string)
	boolean    server.removeFromWhitelist(string)
	boolean    server.runConsoleCommand(string, string)
	boolean    server.runConsoleCommand(string)
	boolean    server.setAllowedItems(array)
	boolean    server.setDisallowedItems(array)
	boolean    server.setMotd(string)
	boolean    server.setPlayerLimit(int)
	boolean    server.setSpawnProtectionSize(int)
	boolean    server.setWhitelistEnabled(boolean)
	boolean    server.setWhitelistMessage(string)
