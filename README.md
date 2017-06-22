JSONAPI is a plugin for Bukkit that allows you to access data and other information about your server and your players through a simple, yet secure, HTTP API. This allows you to make **awesome websites**, **iPhone apps**, and a way for your **players to purchase goods online and automatically receive them in game**.

However, this plugin won't do all of that by itself. It is simply an API that allows you to assemble the features in a way that makes sense for your needs.

## Contributing/dev setup

This project relies on Java Comment Preprocessing to handle multiple versions of Minecraft.

The files in src/ will only compile for one version but anything that has a dependency on a specific version of Minecraft with have specialized
imports like this:

```
//#ifdefined mcversion
//$import net.minecraft.server./*$mcversion$*/.EntityPlayer;
//$import net.minecraft.server./*$mcversion$*/.*;
//$import org.bukkit.craftbukkit./*$mcversion$*/.*;		
//#else		
import net.minecraft.server.v1_11_R1.EntityPlayer;		
import net.minecraft.server.v1_11_R1.*;		
import org.bukkit.craftbukkit.v1_11_R1.*;		
//#endif
```

The only proper way to build JSONAPI is with `./complete_build`. You can edit `./jsonapi` to add new versions.

## Download

[Download JSONAPI](https://github.com/alecgorge/jsonapi/releases)

You can find the source code on GitHub at [alecgorge/jsonapi](https://github.com/alecgorge/jsonapi). I accept pull requests!

## JSONAPI is well documented

Read the documentation at [mcjsonapi.com](http://mcjsonapi.com) or on [GitHub](site/contents/index.markdown).

# A big thanks to YourKit!

YourKit is kindly supporting JSONAPI open source project with its full-featured Java Profiler.
YourKit, LLC is the creator of innovative and intelligent tools for profiling
Java and .NET applications. Take a look at YourKit's leading software products:
[YourKit Java Profiler](http://www.yourkit.com/java/profiler/index.jsp) and
[YourKit .NET Profiler](http://www.yourkit.com/.net/profiler/index.jsp)</a>.

[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/alecgorge/jsonapi/trend.png)](https://bitdeli.com/free "Bitdeli Badge")

