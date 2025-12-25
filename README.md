# Aurora v0.1.x
Aurora is the modern Thumper modding toolchain. Custom levels, binary introspection, custom scoring tables, multiple language support and more.

## Languages
Aurora is written in C++ and Java. C++ is mostly only used to hook into the Thumper executable and to pass control off to Java. Everything else is written in Java.

## Why Java
Java is a powerful language with amazing debugging features. This allows us to develop fast while spending less time fighting with the language.

## Operating Modes
Aurora can run standalone or integrated. Standalone simply means Aurora runs as its own application.

When Aurora is run in integrated mode, it is directly in Thumpers memory space and has control over thumper. Enabling a lot of advanced features.

## Java Installation
Aurora uses Java 25. You *must* one of the installers as they properly setup registry keys. Aurora relies on these registry keys being setup properly.

You should use the [Oracle JVM](https://www.oracle.com/java/technologies/downloads/) since we rely on some of their implementation specific behavior currently.

## Proton / Linux
When running on linux its recommended to install Java nativly on linux *and* via wine. Integrated running mode must be ran via wine on linux and this Java needs to be installed on wine.

When running thumper via proton, use the experimental build and this launch flag `WINEDLLOVERRIDES="steam_api64.dll=n,b" %command%`. This is required to allow our custom dll to load.

## Integrated Mode Building
C++ is used for the integrated layer which must on windows or proton. If you are on windows then installing [Visual Studio](https://visualstudio.microsoft.com/) is highly recommended.

If you are on linux this will be harder since you must target windows when building. An up to date `.dll` is shipped with Aurora to faciliate easier development setup on linux.

## Tooling
You'll want a Java IDE to write and maintain Aurora code. I personally recommend [Eclipse](https://eclipseide.org/). It's simply what I'm familiar with. Eclipse may be installed anywhere.

### Setting up Eclipse
If you're using eclipse you can simply create a new project and point it to the `java` directory of the repo.
* DO NOT CREATE `module-info`. Aurora isn't written with module support yet
* Double check your project classpath. Make sure you are using `JDK25`

## Building the DLL
Building the DLL is only required if you want to make edits to the C++ code. Otherwise its easier to use the provided .dll file in the root.

If you want to build from source, a solution file is provided and should build out of the box (assuming Java 25 is installed).

## Preparing Thumper
All of these steps are relative to the thumper directory.

* First rename `steam_api64.dll` to `steam_api64.dll.bak`. This only needs done once.

* Copy the `.dll` to the thumper directory as `steam_api64.dll`. This will make Thumper load aurora isntead of the steam api. Aurora will then forward and hook the calls into the .bak file we made. If you built the dll from source, then use that one.

* Within the java folder, there are three directories prefixed with `aurora_` the thumper integrated launcher will expect these to exist relative to the thumper exe. You should create symlinks from these into the thumper source so when thumper runs in integrated mode, it can still find the assets.

* If you're on windows then [hardlinkshellext](https://schinagl.priv.at/nt/hardlinkshellext/linkshellextension.html) is recommended. If gives you context options to create symlinks directly from explorer.

## Remote Debugging
If you're running in integrated mode a java debug socket will be opened to allow you to attach eclipse to it to debug in integrated mode. The port is 5005. You should *NOT* port forward or expose this port publically. You cam even block network access if you wish.