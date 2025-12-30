# Developer Setup and Installation Guide
It is highly recommended you already have had aurora installed before. If not familiarize yourself with this process first.

## Eclipse IDE
Aurora is primarily developed in Eclipse. Download and install eclipse for your platform.

## Clone the repo
Head over to Aurora github repo and clone the repo. There are no submodules included in aurora. All libraries Aurora requires are shipped in the repo.

## Create Project
Launch eclipse and select the Java 25 installation on your system. Once eclipse boots. Import the existing aurora project files. Select the `java` folder in the repo.

Double check the window. If an option is given to create `module-info.java` **UNCHECK THIS**. Aurora DOES NOT support modules at this time.

## Launch Arguments
When you launch Aurora in eclipse `F11` you may see some warnings in your console. Edit your runtime configuration and add the following VM arguments.

```
--enable-native-access=ALL-UNNAMED
--sun-misc-unsafe-memory-access=allow
```

## Testing in Integrated Mode
To test in integrated mode. You must put the generated class files into the Thumper directory. However this is a very annoying an manual process.

A much better solution is to create symlinks. These are special folders that you can create that point to existing folders. So we will create 3 symlinks in the Thumper directory.

* `aurora_bin`
* `aurora_res`
* `aurora_lib`

If you launch Thumper and a `.jar` file cannot be found. It'll attempt to use these folders to load Aurora if they are present.

Note that this means the `Aurora.jar` file must either be removed or renamed by changing the `.jar` extension. If this isn't done then the `.jar` file is used and not the IDE generated code.

## Generating SymLinks
This demonstrates how to do this on Windows and with a shell extension. The same can be done on linux. List lookup how to create symlinks for your platform.

I use [this shell extension](https://schinagl.priv.at/nt/hardlinkshellext/linkshellextension.html). This adds a new option to your context menu. `Pick Link Source` and `Drop Link As`. This allows you to simply right click the source folder in aurora and select them as source. Then in the thumper directory, you simply drop them as a symbolic link.

Repeat this process for the 3 folders listed above. After this is done, remove or rename the .jar and thumper will run aurora in dev mode. In this situtation. A debug socket is opened to allow you to attach eclipse to the runtime.

## Attaching to the Socket
In eclipse you can add a debug configuration pretty simply. Create the configuration. Specify `localhost` for the host and `5005` for the port.

When aurora is running in integrated mode, you can attach this debugger and debug.

## Ignoring Build Targets
When testing code in standalone mode you may want to actually launch thumper after debugging in standalone. Typically you dont want aurora to regenerate or overwrite existing work that was done. To simply tell aurora to ignore targets. Use the `Advanced`->`Ignore Targets` option in the `Mod Loader`. Aurora will simply not build the levels and launch thumper as is.