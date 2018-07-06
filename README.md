# Custom discordRPC
This MinecraftForge mod allows you to use custom Rich Presense Text for the Discord client.

A JSON config file named `discordRPconfig.json` is automatically generated by the mod in the `config` folder and this file stores all the mod's configs.

<details>
  <summary>Sample config file</summary>
  <p>
  
```json
{
  "Discord APP ID": "462280508068331522",
  "Rich Presence Profiles": {
    "default": {
      "Activation Event": "JUST_STARTED",
      "Game State": "Just Started",
      "Details": "Waiting for Mod to init.",
      "Start Time Delay": 0,
      "Big Image Key": "juststrtedbig",
      "Big Image Hover Text": "Starting up",
      "Small Image Key": "juststartedsmall",
      "Small Image Hover Text": "Hi",
      "Modification JavaScript": "RichPresence.smallImageText = Helper.getUserName();RichPresence.state += ' Minecraft v'+Helper.cancellUpdateIfMatch(Helper.getMCVERSION(),null);"
    },
    "preInit": {
      "Activation Event": "PRE_INIT",
      "Game State": "Pre-Init.",
      "Details": "Forge Loading in pre-init.",
      "Start Time Delay": 0,
      "Big Image Key": "preinitbig",
      "Big Image Hover Text": "This won't take long",
      "Small Image Key": "preinitsmall",
      "Small Image Hover Text": "pre-init",
      "Modification JavaScript": "RichPresence.smallImageText = Helper.cancellUpdateIfMatch(Helper.getUserName(),null);"
    },
    "init": {
      "Activation Event": "INIT",
      "Game State": "Init.",
      "Details": "Forge Loading in init.",
      "Start Time Delay": 0,
      "Big Image Key": "initbig",
      "Big Image Hover Text": "Working on it",
      "Small Image Key": "initsmall",
      "Small Image Hover Text": "init",
      "Modification JavaScript": "RichPresence.smallImageText = Helper.cancellUpdateIfMatch(Helper.getUserName(),null);"
    },
    "postInit": {
      "Activation Event": "POST_INIT",
      "Game State": "Post-Init.",
      "Details": "Forge Loading in post-init.",
      "Start Time Delay": 0,
      "Big Image Key": "postinitbig",
      "Big Image Hover Text": "Almost there",
      "Small Image Key": "postinitsmall",
      "Small Image Hover Text": "post-init",
      "Modification JavaScript": "RichPresence.smallImageText = Helper.cancellUpdateIfMatch(Helper.getUserName(),null);"
    },
    "mainMenu": {
      "Activation Event": "MAIN_MENU_REACHED",
      "Game State": "In main menu.",
      "Details": "Idle",
      "Start Time Delay": 0,
      "Big Image Key": "mainmenu",
      "Big Image Hover Text": "Surfing the menus",
      "Small Image Key": "mainmenu",
      "Small Image Hover Text": "main-menu",
      "Modification JavaScript": "RichPresence.smallImageText = Helper.cancellUpdateIfMatch(Helper.getUserName(),null);RichPresence.details = ''+Helper.cancellUpdateIfMatch(Helper.getUserName(),null)+' has nothing to do.';"
    },
    "serverAboutToStart": {
      "Activation Event": "SERVER_ABOUT_TO_START",
      "Game State": "Loading Screen",
      "Details": "Loading Single Player Server",
      "Start Time Delay": 0,
      "Big Image Key": "serverabouttostartbig",
      "Big Image Hover Text": "Getting ready to roll",
      "Small Image Key": "serverabouttostartsmall",
      "Small Image Hover Text": "server-about-to-start",
      "Modification JavaScript": "RichPresence.smallImageText = Helper.cancellUpdateIfMatch(Helper.getUserName(),null);RichPresence.details = 'Loading World \\''+Helper.cancellUpdateIfMatch(Helper.getWorldName(),null)+'\\'';"
    },
    "serverStarted": {
      "Activation Event": "SERVER_STARTED",
      "Game State": "In-game",
      "Details": "Playing Single Player",
      "Start Time Delay": 0,
      "Big Image Key": "serverstartedbig",
      "Big Image Hover Text": "Currently causing havoc ;)",
      "Small Image Key": "serverstartedsmall",
      "Small Image Hover Text": "in-game",
      "Modification JavaScript": "RichPresence.smallImageText = Helper.cancellUpdateIfMatch(Helper.getUserName(),null);RichPresence.details = 'In the \\''+Helper.cancellUpdateIfMatch(Helper.getDimensionName(),null)+'\\'';RichPresence.state += '('+Helper.cancellUpdateIfMatch(Helper.getWorldName(),null)+')'"
    },
    "connectedToMultiplayerServer": {
      "Activation Event": "CONNECTED_TO_MULTIPLAYER_SERVER",
      "Game State": "Online",
      "Details": "Playing MultiPlayer",
      "Start Time Delay": 0,
      "Big Image Key": "connectedtomultiplayerserverbig",
      "Big Image Hover Text": "Currently causing havoc ;)",
      "Small Image Key": "connectedtomultiplayerserversmall",
      "Small Image Hover Text": "in-game",
      "Modification JavaScript": "RichPresence.smallImageText = Helper.cancellUpdateIfMatch(Helper.getUserName(),null);RichPresence.details = 'In the \\''+Helper.cancellUpdateIfMatch(Helper.getDimensionName(),null)+'\\'';RichPresence.state += ' @'+Helper.cancellUpdateIfMatch(Helper.getServerIP(),null)"
    },
    "disconnectedFromMultiplayerServer": {
      "Activation Event": "DISCONNECTED_FROM_MULTIPLAYER_SERVER",
      "Game State": "MultiplayerGUI",
      "Details": "Was just playing MultiPlayer",
      "Start Time Delay": 0,
      "Big Image Key": "disconnectedfrommultiplayerserverbig",
      "Big Image Hover Text": "Currently causing havoc ;)",
      "Small Image Key": "disconnectedfrommultiplayerserversmall",
      "Small Image Hover Text": "in-game",
      "Modification JavaScript": "RichPresence.smallImageText = Helper.cancellUpdateIfMatch(Helper.getUserName(),null);"
    }
  },
  "Advanced Config": {
    "Treat First Post-Load Screen As Main-Menu": false,
    "Main Menu Full ClassName": "lumien.custommainmenu.gui.GuiCustom",
    "Discord Update Interval": 15,
    "Script Update Interval Millis": 2000
  }
}
```

</p></details>

* The config file contains the `Discord APP ID` key whose value tells the mod what __APP ID__ must be sent to the Discord client.
This allows you to use a custom Discord App so that you can use custom images or a custom APP Name.

* Then there is the `Rich Presence Profiles` key which defines all the profiles that the mod should use.
> A profile is simply a key specifying the profile name with the value as a JSON block that defines certain properties which tell the mod exactly what to show in the Discord Rich Presence whenever the profile is activated.

* Each profile has a `name` followed by a JSON block containing its properties.
<details>
<summary>Example</summary>
<p>

```json
{
  "Discord APP ID": "462280508068331522",
  "Rich Presence Profiles": {
    "default": {
      "Activation Event": "JUST_STARTED",
      "Game State": "Just Started",
      "Details": "Waiting for Mod to init.",
      "Start Time Delay": 0,
      "Big Image Key": "juststrtedbig",
      "Big Image Hover Text": "Starting up",
      "Small Image Key": "juststartedsmall",
      "Small Image Hover Text": "Hi"
    }
  }
  "Advanced Config": {
    "Treat First Post-Load Screen As Main-Menu": true,
    "Discord Update Interval": 15,
    "Script Update Interval Millis": 2000
  }
}
```
> The above config file has only one profile named `default` and its `Activation Event` property is set to `JUST_STARTED`.

</p></details>
<br>

Each profile has a `Activation Event` property whose value tells the mod when to activate that profile. 
If this property is not present in a profile then that profile will never be activated. 
<details>
  <summary>The <code>Activation Event</code> property can currently have the following values</summary>
  <p>
  
| Activation Event | Description |
|------------------|-------------|
| JUST_STARTED     | This event occours when the mod is loaded into memory. Currently specifying this activation event has no use since it occours before the config file is read. |
| PRE_INIT         | This event occours when Forge calls the `pre-init` function, i.e during the `pre-init` phase of loading. |
| INIT | This event occours when Forge calls the `init` function, i.e during the `init` phase of loading. |
| POST_INIT | This event occours when Forge calls the `post-init` function, i.e during the `post-init` phase of loading. |
| MAIN_MENU_REACHED | This event occours when the main menu screen is displayed. |
| SERVER_ABOUT_TO_START | This event occours __before__ a __Single-Player__ world starts. |
| SERVER_STARTING | This event occours __while__ a __Single-Player__ world is starting. |
| SERVER_STARTED | This event occours __after__ a __Single-Player__ world has started. |
| CONNECTED_TO_SINGLEPLAYER_SERVER | This event occours when the player has connected to a Single-Player server, i.e. the player's Entity has been created. |
| DISCONNECTED_FROM_SINGLEPLAYER_SERVER | This event occours when the player has disconnected from the Single-Player server, i.e. the server is still running but the player entity has disconnected. |
| SERVER_STOPPING | This event occours __while__ a __Single-Player__ world is stopping. |
| SERVER_STOPPED | This event occours __after__ a __Single-Player__ world has stopped. |
| CONNECTED_TO_MULTIPLAYER_SERVER | This event occours when the player has connected to a __Multi-Player/Online__ server, i.e. the player's Entity has been created. |
| DISCONNECTED_FROM_MULTIPLAYER_SERVER | This event occours when the player has disconnected from the __Multi-Player/Onilne__ server, i.e. the player entity has disconnected. |

  </p></details>
  <br>
  
  Each profile also has a few properties that specify exactly what must be displayed in the Discord Rich Presence.
  If a property is not specified for a profile, then that property is disabled for that profile thus allowing you to have small config files by ommiting useless properties.
  <details>
  <summary>Each profile currently has the following properties</summary>
  <p>
  
| Property                 | Datatype | Max Length | Description |
|--------------------------|----------|------------|-------------|
| Activation Event | String | Fixed Values | This stores the activation event of this profile, eg: `MAIN_MENU_REACHED`. |
| Game State | String | 128 Chars. | This is discord's state string. |
| Details | String | 128 Chars. | This is discord's details string. |
| Party ID | String | 128 Chars. | This is discord's party ID string. |
| Party Current Size | Integer | Integer | This is discord's party size string. |
| Party Max Size | Integer | Integer | This is discord's party size max string. |
| Start Timestamp | Long | Long | This is discord's start timestamp string. |
| End Timestamp | Long | Long | This is discord's end timestamp string. |
| Start Time Delay | Long | Long | When this is set, `Start Timestamp` is ignored and discord is given a startTimestamp that is `profileActivationTime + value` where `value` is this field's value. |
| End Time Delay | Long | Long | Similar to `Start Time Delay`. |
| Big Image Key | String | 32 Chars. | This is discord's large image key string. |
| Big Image Hover Text | String | 128 Chars. | This is discord's large image text string. |
| Small Image Key | String | 32 Chars. | This is discord's small image key string. |
| Small Image Hover Text | String | 128 Chars. | This is discord's small image text string. |
| Modification JavaScript | String | Any Size | This is the javascript code that is executed before sending an update to Discord. It uses the Nashorn engine therefore you can use the 'load()' function to load custom js files which will allow you to change the js code without restarting the game. See below to know more. |
| Join Secret | String | 128 Chars. | This is currently unused. |
| Spectate Secret | String | 128 Chars. | This is currently unused. |

  </p></details>
  <br>
  
  # Note
  * You __can__ use your own images by providing a different `Discord APP ID` and your own image key strings in the profiles.
  * You can create your own profiles with different names, however, each profile should have a unique `name` and a unique `Activation Event`.
  * You do __not__ have to create a profile for every `Activation Event`.
  * You do __not__ have to specify all the properties for a profile.
  * If the Main Menu is __not__ detected try to use the `Treat First Post-Load Screen As Main-Menu` and `Main Menu Full ClassName` settings in the `Advanced Config` block of the config file.
  
  # Modification Javascript
  Each profile has a `Modification JavaScript` property that can be set using the config file.
  
  This property's value __must__ be a String containing valid javascript code.
  
  The provided script is executed using the __Nashorn__ engine and is __pre-loaded__ with the following variables that can be used for processing.
  
  <details>
  <summary>Pre-Loaded variables</summary>
  <p>

| Variable Name | Data Type | Description |
|---------------|-----------|-------------|
| RichPresence | [`net.arikia.dev.drpc.DiscordRichPresence`](https://github.com/Vatuu/discord-rpc/blob/master/src/main/java/net/arikia/dev/drpc/DiscordRichPresence.java) | This is the `DiscordRichPresence` object that will be passed in the current update to discord and it stores all the information regarding the rich presence. You must change this objects properties to change what is displayed in the rich presence. See [net.arikia.dev.drpc.DiscordRichPresence](https://github.com/Vatuu/discord-rpc/blob/master/src/main/java/net/arikia/dev/drpc/DiscordRichPresence.java)|
| CurrentProfileName | `String` | This is the name of the currently active profile. (Hint: You can use this to make a single js file that behaves differently for different profiles and is executed using the `load()` function) |
| hasProfileChanged | `boolean` | This is a boolean value that is only true when a profile has been changed. __Note__ that after a single execution this value is false even if the RPC update was cancelled using the [ScriptHelper::cancellUpdate()](https://github.com/jaideepheer/MinecraftForge-custom_discordRPC/blob/5f9d87adc33e628df24a4a808ca45aa157231018/src/main/java/jdmcmods/custom_discordrpc/ScriptEngine/modScriptEngine.java#L170). |
| log | org.apache.logging.log4j.Logger::info | This is the logger function used to print output to the log. Just send it an object ant it'll print it to the log. For eg: log('hello') |
| Helper | [`ScriptHelper`](https://github.com/jaideepheer/MinecraftForge-custom_discordRPC/blob/5f9d87adc33e628df24a4a808ca45aa157231018/src/main/java/jdmcmods/custom_discordrpc/ScriptEngine/modScriptEngine.java#L157) | This is perhaps the most usefull variable. It is a custom wrapper object that provides various functions to get data from Minecraft during script execution. For eg. one of its functions is `Helper.getUserName()` which returns a `String`. <br>Eg: `log(Helper.getMCVERSION())` prints the Minecraft version to the log.<br><br> See the [`ScriptHelper`](https://github.com/jaideepheer/MinecraftForge-custom_discordRPC/blob/5f9d87adc33e628df24a4a808ca45aa157231018/src/main/java/jdmcmods/custom_discordrpc/ScriptEngine/modScriptEngine.java#L157) class's doccumentation to know more. |

  </p></details>
<br>

* Since Discord allows the Rich Presence to be updated __only once in 15sec.__, this mod will execute your script once in about 15 seconds. For that matter, you may want to delay an update due to some reason like invalid/unavailable data. This can be accomplished by the `Helper.cancellUpdate()` function. This function prevents the current update from being sent thus allowing you to 'delay' an update till you think it is time. __Note__ that after the update is canceled, the RPC Thread still waits for 2 seconds before attempting another update(and executing your script) to prevent log spamming and save on CPU resources.

* There is also a utility function `Helper.cancellUpdateIfMatch(target, toMatch)` which takes two arguments and cancells the current update if the two match else returns the first argument. This can be used to check for null values.

* Finally, canceling an update causes an exception and therefore __stopps the script execution__ so you shouldn't expect any code to run after the cancell statement.

# Advanced Config
There is an `Advanced Config` block in the config file which provides some settings for compatability and script execution.

It provides the following settings:-

| Key | DataType | Description |
|-----|----------|-------------|
| Treat First Post-Load Screen As Main-Menu | boolean | If set to `true` this will use the first `GuiScreenEvent` to get the `GuiScreen` used for Main Menu detection.<br>Set this to `true` if the mod cannot detect the Main Menu screen due to conflicts with other mods like 'Custom Main Menu' etc. |
| Main Menu Full ClassName | String | If this property is present the mod uses its value to detect the Main Menu. The class name of each `GuiScreen` encountered in `GuiScreenEvent` is matched with this property's value to detect the Main Menu.<br>Use this if `Treat First Post-Load Screen As Main-Menu` setting doesn't cut it for you.<br><br>__Hint:__ For the '__Custom Main Menu__' mod use value '__lumien.custommainmenu.gui.GuiCustom__'. |
| Update Interval Millis | Integer | This is the time period of the updates sent to discord in milli-seconds. Your update script is executed in every update. |
