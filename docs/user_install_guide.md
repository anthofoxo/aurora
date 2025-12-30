# User Installation Guide

## Prerequisites
### Put Thumper in a Vanilla State
Thumper should be in an entirely vanilla state before installing Aurora. To do this perform the following actions:

1. Open your Steam Library, right click on `Thumper`, click `Manage`, then click `Browse local files`. Remember this folder location. We will refer to this as the `Thumper Directory`.
2. Delete the `cache` folder in the `Thumper Directory`.
3. Open your Steam Library, right click on `Thumper`, click `Properties...`, click `Installed Files`, then click `Verify integrety of game files`. Steam will then proceed to redownload the `cache` directory and put the game into a vanilla state.

### Installing Java
Aurora requires Java 25 to be installed. Java *must* be installed via an installer. Aurora relies on certain registry keys to exist. Using an installer will ensure this happens properly. You can visit the [Oracle website](https://www.oracle.com/java/technologies/downloads) to find the download links.

Install Java using the [Windows x64 MSI Installer](https://download.oracle.com/java/25/latest/jdk-25_windows-x64_bin.msi). Use this same installer even if you are on Linux. Just install it via Wine.

### Shared Library Configuration
Aurora expects `steam_api64.dll.bak` to exist in the `Thumper Directory`. This is simply a copy of `steam_api64.dll`. If this `.bak` file doesn't already exist then copy/pase `steam_api64.dll` as `steam_api64.dll.bak`.

## Linux and Proton Configuration
If you are using Linux, two additional changes are required:

* Add the following launch flag: `WINEDLLOVERRIDES="steam_api64.dll=n,b" %command%`
* Make sure Thumper is ran using the experimental version of proton.

## Aurora Install
To finally proceed with the install. Download the latest `.zip` file from [GitHub](https://github.com/anthofoxo/aurora/releases). Once downloaded you want to *directly* extract this into the `Thumper Directory`. Confirm any overwrites if needed.

## Testing and Aurora Configuration
At this point Aurora should be installed and ready to go. To validate this simply launch Thumper. If all went well the Aurora application should appear.

* You'll be prompted to select a Thumper Executable. Select and of the `.exe` files.
* The Aurora GUI will have a small guide on adding mod paths.
* Once ready. Click the `Launch Thumper` button. Aurora will proceed to build the targets and begin the game.

## Standalone Mode
Aurora can also run standalone mode. This may be useful in a few situation. The `.jar` file that is downloaded is executable. If Java is installed correctly you can simply open this file.

When in standalone mode, Aurora will be unable to directly launch Thumper. Instead you are given the option to `Build Mods`.

## Uninstalling Aurora
If you want to uninstall Aurora. You can simply delete `steam_api64.dll` and rename `steam_api64.dll.bak` to `steam_api64.dll`. This will prevent Aurora from running on game startup. You may delete the `.jar` file if you want.