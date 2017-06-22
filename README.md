JSONAPI is a plugin for Bukkit that allows you to access data and other information about your server and your players through a simple, yet secure, HTTP API. This allows you to make **awesome websites**, **iPhone apps**, and a way for your **players to purchase goods online and automatically receive them in game**.

However, this plugin won't do all of that by itself. It is simply an API that allows you to assemble the features in a way that makes sense for your needs.

**Note** I (@alecgorge) don't main this very much anymore. I accept pull requests and will fix issues with PRs or new versions of Minecraft, but development is more or less stalled.
I haven't played Minecraft in years so I don't know when a new version comes out. Please open issues. I read every one but I don't have time to debug every configuration issue because every issue that isn't related to a new version of Minecraft has been answered in some way. Configurations have not changed in years.

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

The only proper way to build JSONAPI is with `./complete_build`. You can edit `./jsonapi` to add new versions and run `./jsonapi build > ./complete_build` then `chmod +x ./complete_build` to update the build script..

If your build hangs for a bit on this part:

```
[WARNING] The POM for net.ess3:Essentials:jar:2.14-20140906.162642-94 is invalid, transitive dependencies (if any) will not be available, enable debug logging for more details
Downloading: http://dev.escapecraft.com/maven/org/bukkit/bukkit/1.8-R0.1-SNAPSHOT/maven-metadata.xml
Downloading: http://repo.bukkit.org/content/groups/public/org/bukkit/bukkit/1.8-R0.1-SNAPSHOT/maven-metadata.xml
```

Let it finish once (to download everything else) and then you can run `./complete_build -o` to build offline and not get hung up on that. Unfortunately this is an upstream bug
with Vault and it would require a change in the Vault build to fix it.

## Download

[Download JSONAPI](https://github.com/alecgorge/jsonapi/releases)

You can find the source code on GitHub at [alecgorge/jsonapi](https://github.com/alecgorge/jsonapi). I accept pull requests!

## JSONAPI is well documented

Read the documentation at [mcjsonapi.com](http://mcjsonapi.com) or on [GitHub](site/contents/index.markdown).
