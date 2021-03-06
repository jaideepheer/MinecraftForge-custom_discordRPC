package jdmcmods.custom_discordrpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import net.arikia.dev.drpc.DiscordRichPresence;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

import static jdmcmods.custom_discordrpc.CDRPCmod.LOGGER;

public class ModConfigManager
{
    private static File configFile;
    private static Gson gson;
    private static ModConfig config = new ModConfig();

    // This stores the latest event that has occoured
    public static Event latestEvent;
    // This stores the latest event that activates at least one profile.
    public static Event validEvent;
    public static EnumSet<Event> validEventsSet = EnumSet.noneOf(Event.class);

    static {
        gson = new GsonBuilder().setPrettyPrinting().create();
        setDefaults();
        ModConfigManager.setLatestEvent(Event.JUST_STARTED);
    }

    public enum Event {
        JUST_STARTED,
        PRE_INIT,
        INIT,
        POST_INIT,
        SERVER_ABOUT_TO_START,
        SERVER_STARTING,
        SERVER_STOPPING,
        SERVER_STOPPED,
        SERVER_STARTED,
        MAIN_MENU_REACHED,
        CONNECTED_TO_SINGLEPLAYER_SERVER,
        DISCONNECTED_FROM_SINGLEPLAYER_SERVER,
        CONNECTED_TO_MULTIPLAYER_SERVER,
        DISCONNECTED_FROM_MULTIPLAYER_SERVER
    }

    static class ModConfig
    {
        @SerializedName("Discord APP ID")
        public String discordAppID;
        // Map<profileName,RichTextProfile>
        @SerializedName("Rich Presence Profiles")
        public Map<String,RichTextProfile> RTProfileList = new LinkedHashMap<>();

        @SerializedName("Advanced Config")
        public AdvancedConfig advancedConfig = new AdvancedConfig();
    }

    public static class AdvancedConfig
    {
        @SerializedName("Treat First Post-Load Screen As Main-Menu")
        boolean firstPostLoadScreenAsMainMenu = true;

        @SerializedName("Main Menu Full ClassName")
        String mainMenuClassName = null;

        @SerializedName("Update Interval Millis")
        long updateintervalMillis = 2000L;
    }

    public static class RichTextProfile
    {
        @SerializedName("Activation Event")
        Event activationEvent;
        @SerializedName("Game State")
        String gameState = null;
        @SerializedName("Details")
        String details = null;
        @SerializedName("Party ID")
        String partyID = null;
        @SerializedName("Party Current Size")
        Integer partySize;
        @SerializedName("Party Max Size")
        Integer partySizeMax;
        @SerializedName("Start Timestamp")
        Long startTimestamp;
        @SerializedName("End Timestamp")
        Long endTimestamp;
        @SerializedName("Start Time Delay")
        Long startTimedelay;
        @SerializedName("End Time Delay")
        Long endTimedealy;
        @SerializedName("Big Image Key")
        String bigImageKey = null;
        @SerializedName("Big Image Hover Text")
        String bigImageHover = null;
        @SerializedName("Small Image Key")
        String smallImagekey = null;
        @SerializedName("Small Image Hover Text")
        String smallImageHover = null;
        @SerializedName("Join Secret")
        String joinSecret = null;
        @SerializedName("Spectate Secret")
        String spectateSecret = null;
        @SerializedName("Modification JavaScript")
        String modifyScript = null;

        private DiscordRichPresence richPresence;

        public RichTextProfile setGameState(String gameState) {
            this.gameState = gameState;
            return this;
        }

        public RichTextProfile setDetails(String details) {
            this.details = details;
            return this;
        }

        public RichTextProfile setParty(String partyID, int partySize, int partySizeMax) {
            this.partyID = partyID;
            this.partySize = partySize;
            this.partySizeMax = partySizeMax;
            return this;
        }

        public RichTextProfile setBigImage(String bigImageKey, String bigImageHover) {
            this.bigImageKey = bigImageKey;
            this.bigImageHover = bigImageHover;
            return this;
        }

        public RichTextProfile setSmallImage(String smallImagekey, String smallImageHover) {
            this.smallImagekey = smallImagekey;
            this.smallImageHover = smallImageHover;
            return this;
        }

        public RichTextProfile setJoinSecret(String joinSecret) {
            this.joinSecret = joinSecret;
            return this;
        }

        public RichTextProfile setSpectateSecret(String spectateSecret) {
            this.spectateSecret = spectateSecret;
            return this;
        }

        public RichTextProfile setModifyScript(String modifyScript) {
            this.modifyScript = modifyScript;
            return this;
        }

        /**
         * Takes timestamps in seconds, i.e. System.currentTimeMillis()/1000 + T
         * @param startTimestamp timestamp for time time left to start, shows as elapsed
         * @param endTimestamp timestamp for time left to end, shows as remaining
         * @return this object
         */
        public RichTextProfile setTimestamps(long startTimestamp, long endTimestamp) {
            this.startTimestamp = startTimestamp;
            this.endTimestamp = endTimestamp;
            return this;
        }

        /**
         * Takes time delay in seconds and adds it to current system time.
         * @param startTimedelay time delay for time time left to start, shows as elapsed
         * @return this object
         */
        public RichTextProfile setStartTimedealy(long startTimedelay) {
            this.startTimedelay = startTimedelay;
            return this;
        }
        /**
         * Takes time delay in seconds and adds it to current system time.
         * @param endTimedealy time delay for time time left to end, shows as remaining
         * @return this object
         */
        public RichTextProfile setEndTimeDelay(long endTimedealy)
        {
            this.endTimedealy = endTimedealy;
            return this;
        }

        public RichTextProfile setActivationEvent(Event activationEvent) {
            this.activationEvent = activationEvent;
            return this;
        }

        public boolean shouldActivate()
        {
            return activationEvent == validEvent;
        }

        /**
         * Returns the {@link DiscordRichPresence} object to be used by this profile.
         * @param createNewRP if true creates a new {@link DiscordRichPresence} object, send true to change gameState
         *                    if false it only updates the {@link DiscordRichPresence} object preserving any timestamps
         * @return {@link DiscordRichPresence} object to be used
         */
        public DiscordRichPresence getRichPresence(boolean createNewRP)
        {
            if(createNewRP || richPresence==null) {
                long curTime = System.currentTimeMillis();
                DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder(gameState)
                        .setDetails(details)
                        .setSecrets(joinSecret, spectateSecret)
                        // time stamp logic
                        .setTimestamps(
                                (startTimedelay != null) ? (curTime / 1000 + startTimedelay) : startTimestamp != null ? startTimestamp : 0L,
                                (endTimedealy != null) ? (curTime / 1000 + endTimedealy) : endTimestamp != null ? endTimestamp : 0L
                        );

                if (partyID != null && partySize != null && partySizeMax != null) {
                    builder.setParty(partyID, partySize, partySizeMax);
                }
                if (bigImageKey != null) {
                    builder.setBigImage(bigImageKey, bigImageHover);
                }
                if (smallImagekey != null) {
                    builder.setSmallImage(smallImagekey, smallImageHover);
                }
                richPresence = builder.build();
            }
            else
            {
                richPresence.details = details;
                if(partySizeMax!=null && partySize!=null){
                    richPresence.partySize = partySize;
                    richPresence.partyMax = partySizeMax;
                }
                if(partyID!=null)richPresence.partyId = partyID;
                richPresence.state = gameState;
                richPresence.smallImageKey = smallImagekey;
                richPresence.smallImageText = smallImageHover;
                richPresence.largeImageKey = bigImageKey;
                richPresence.largeImageText = bigImageHover;
            }
            return richPresence;
        }

        /**
         * Validates the RichPresence data to make sure there isn't anything invalid in there.
         */
        public static boolean validateRichPresence(DiscordRichPresence presence){
            if(presence.largeImageKey != null && presence.largeImageKey.length()>32)
            {
                presence.largeImageKey = presence.largeImageKey.substring(0,32);
                LOGGER.log(Level.ERROR,"WARNING! \"Big Image Key\" was more that 32 chars. It is trimmed to: "+presence.largeImageKey);
            }
            if(presence.smallImageKey != null && presence.smallImageKey.length()>32)
            {
                presence.smallImageKey = presence.smallImageKey.substring(0,32);
                LOGGER.log(Level.ERROR,"WARNING! \"Small Image Key\" was more that 32 chars. It is trimmed to:  "+presence.smallImageKey);
            }
            if(presence.largeImageText != null && presence.largeImageText.length()>128)
            {
                presence.largeImageText = presence.largeImageText.substring(0,128);
                LOGGER.log(Level.ERROR,"WARNING! \"Big Image Hover Text\" was more that 128 chars. It is trimmed to fit 128 chars.");
            }
            if(presence.smallImageText != null && presence.smallImageText.length()>128)
            {
                presence.smallImageText = presence.smallImageText.substring(0,128);
                LOGGER.log(Level.ERROR,"WARNING! \"Small Image Hover Text\" was more that 128 chars. It is trimmed to fit 128 chars.");
            }
            if(presence.partyId != null && presence.partyId.length()>128)
            {
                presence.partyId = presence.partyId.substring(0,128);
                LOGGER.log(Level.ERROR,"WARNING! \"Party ID\" was more that 128 chars. It is trimmed to fit 128 chars.");
            }
            if(presence.state != null && presence.state.length()>128)
            {
                presence.state = presence.state.substring(0,128);
                LOGGER.log(Level.ERROR,"WARNING! \"Game State\" was more that 128 chars. It is trimmed to fit 128 chars.");
            }
            if(presence.details != null && presence.details.length()>128)
            {
                presence.details = presence.details.substring(0,128);
                LOGGER.log(Level.ERROR,"WARNING! \"Details\" was more that 128 chars. It is trimmed to fit 128 chars.");
            }
            if(presence.joinSecret != null && presence.joinSecret.length()>128)
            {
                presence.joinSecret = presence.joinSecret.substring(0,128);
                LOGGER.log(Level.ERROR,"WARNING! \"Details\" was more that 128 chars. It is trimmed to fit 128 chars.");
            }
            if(presence.spectateSecret != null && presence.spectateSecret.length()>128)
            {
                presence.spectateSecret = presence.spectateSecret.substring(0,32);
                LOGGER.log(Level.ERROR,"WARNING! \"Details\" was more that 128 chars. It is trimmed to fit 128 chars.");
            }
            if(presence.matchSecret != null)
            {
                presence.matchSecret = null;
                LOGGER.log(Level.WARN,"WARNING! \'matchSecret\' was not null. It is set to null as it is deprecated.");
            }
            return true;
        }
    }

    public static ModConfig getModConfig()
    {
        return config;
    }

    public static void init(File configDir)
    {
        try {
            configFile = new File(configDir.getAbsolutePath(),"discordRPconfig.json");
            if(configFile.createNewFile())
            {
                LOGGER.log(Level.INFO,"No config file detected.");
                LOGGER.log(Level.INFO,"New config file created at ["+configFile.getAbsolutePath()+"]");
                setDefaults();
                saveConfig();
            }
            else {
                LOGGER.log(Level.INFO,"Config file found at ["+configFile.getAbsolutePath()+"]");
                String prevAPPID = config.discordAppID;
                loadConfig();
                if(!config.discordAppID.equals(prevAPPID))
                {
                    // APP ID changed
                    LOGGER.log(Level.INFO,"APP ID changed from \'"+prevAPPID+"\' to \'"+config.discordAppID+"\'");
                }
                // restart daemon after config load
                discordRPCHandler.stopRPC();
                discordRPCHandler.startRPC();
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.log(Level.INFO,"Mod couldn't initialize.");
            LOGGER.log(Level.INFO,"Tried to create config file at ["+configFile.getAbsolutePath()+"]");
        }
    }

    public static void setLatestEvent(Event event) {
        ModConfigManager.latestEvent = event;
        LOGGER.log(Level.INFO,"Latest event: "+ event);

        if(validEventsSet.contains(event))
        {
            validEvent = latestEvent;
            LOGGER.log(Level.INFO,"Valid Event changed to: "+validEvent+".");
        }
        else {
            LOGGER.log(Level.DEBUG,"Latest Event activates no profiles, hence Valid Event["+validEvent+"] remains unchanged.");
        }
    }

    private static void loadConfig()
    {
        // Read config
        try {
            BufferedReader reader = new BufferedReader(new FileReader(configFile));
            config = gson.fromJson(reader,ModConfig.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LOGGER.log(Level.ERROR,"Config file not found while loading... ["+configFile.getAbsolutePath()+"]");
        }
        catch (JsonIOException e)
        {
            e.printStackTrace();
            LOGGER.log(Level.ERROR,"Config file JSON IO error ["+configFile.getAbsolutePath()+"]");
        }
        catch (JsonSyntaxException e)
        {
            e.printStackTrace();
            LOGGER.log(Level.ERROR,"Config file has invalid JSON syntax ["+configFile.getAbsolutePath()+"]");
        }
        LOGGER.log(Level.INFO,"Config loaded.");

        refreshValidEvents();
    }

    private static void refreshValidEvents()
    {
        // Reset validEventsSet
        validEventsSet = EnumSet.noneOf(Event.class);

        // Detect valid events, i.e. events used for profile activation
        for(RichTextProfile e: config.RTProfileList.values())
        {
            validEventsSet.add(e.activationEvent);
        }

        LOGGER.log(Level.INFO,"Valid events refreshed.");
        LOGGER.log(Level.INFO,"These are the events that activate profiles: "+validEventsSet);
    }

    private static void saveConfig()
    {
        try {
            FileWriter file = new FileWriter(configFile);
            file.write(gson.toJson(config));
            file.close();
            LOGGER.log(Level.INFO,"Config saved to file.");
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.log(Level.INFO,"Couldn't save config to file.");
        }
    }

    private static void setDefaults()
    {
        // Set defaults
        config.discordAppID = "462280508068331522";
        config.RTProfileList.put("default",new RichTextProfile()
                .setActivationEvent(Event.JUST_STARTED)
                .setGameState("Just Started")
                .setDetails("Waiting for Mod to init.")
                .setStartTimedealy(0)
                .setBigImage("juststartedbig","Starting up")
                .setSmallImage("juststartedsmall","Hi")
                .setModifyScript("RichPresence.smallImageText = Helper.getUserName();"
                                +"RichPresence.state += ' Minecraft v'+Helper.cancellUpdateIfMatch(Helper.getMCVERSION(),null);")
        );
        config.RTProfileList.put("preInit",new RichTextProfile()
                .setActivationEvent(Event.PRE_INIT)
                .setGameState("Pre-Init.")
                .setDetails("Forge Loading in pre-init.")
                .setStartTimedealy(0)
                .setBigImage("preinitbig","This won't take long")
                .setSmallImage("preinitsmall","pre-init")
                .setModifyScript("RichPresence.smallImageText = Helper.cancellUpdateIfMatch(Helper.getUserName(),null);")
        );
        config.RTProfileList.put("init",new RichTextProfile()
                .setActivationEvent(Event.INIT)
                .setGameState("Init.")
                .setDetails("Forge Loading in init.")
                .setStartTimedealy(0)
                .setBigImage("initbig","Working on it")
                .setSmallImage("initsmall","init")
                .setModifyScript("RichPresence.smallImageText = Helper.cancellUpdateIfMatch(Helper.getUserName(),null);")
        );
        config.RTProfileList.put("postInit",new RichTextProfile()
                .setActivationEvent(Event.POST_INIT)
                .setGameState("Post-Init.")
                .setDetails("Forge Loading in post-init.")
                .setStartTimedealy(0)
                .setBigImage("postinitbig","Almost there")
                .setSmallImage("postinitsmall","post-init")
                .setModifyScript("RichPresence.smallImageText = Helper.cancellUpdateIfMatch(Helper.getUserName(),null);")
        );
        config.RTProfileList.put("mainMenu",new RichTextProfile()
                .setActivationEvent(Event.MAIN_MENU_REACHED)
                .setGameState("In main menu.")
                .setDetails("Idle")
                .setStartTimedealy(0)
                .setBigImage("mainmenu","Surfing the menus")
                .setSmallImage("mainmenu","main-menu")
                .setModifyScript("RichPresence.smallImageText = Helper.cancellUpdateIfMatch(Helper.getUserName(),null);"
                                +"RichPresence.details = ''+Helper.cancellUpdateIfMatch(Helper.getUserName(),null)+' has nothing to do.';")
        );
        config.RTProfileList.put("serverAboutToStart",new RichTextProfile()
                .setActivationEvent(Event.SERVER_ABOUT_TO_START)
                .setGameState("Loading Screen")
                .setDetails("Loading Single Player Server")
                .setStartTimedealy(0)
                .setBigImage("serverabouttostartbig","Getting ready to roll")
                .setSmallImage("serverabouttostartsmall","server-about-to-start")
                .setModifyScript("RichPresence.smallImageText = Helper.cancellUpdateIfMatch(Helper.getUserName(),null);" +
                        "RichPresence.details = 'Loading World \\''+Helper.cancellUpdateIfMatch(Helper.getWorldName(),null)+'\\'';")
        );
        config.RTProfileList.put("serverStarted", new RichTextProfile()
                .setActivationEvent(Event.SERVER_STARTED)
                .setGameState("In-game")
                .setDetails("Playing Single Player")
                .setStartTimedealy(0)
                .setBigImage("serverstartedbig","Currently causing havoc ;)")
                .setSmallImage("serverstartedsmall","in-game")
                .setModifyScript("RichPresence.smallImageText = Helper.cancellUpdateIfMatch(Helper.getUserName(),null);" +
                        "RichPresence.details = 'In the \\''+Helper.cancellUpdateIfMatch(Helper.getDimensionName(),null)+'\\'';" +
                        "RichPresence.state += '('+Helper.cancellUpdateIfMatch(Helper.getWorldName(),null)+')'")
        );
        config.RTProfileList.put("connectedToMultiplayerServer", new RichTextProfile()
                .setActivationEvent(Event.CONNECTED_TO_MULTIPLAYER_SERVER)
                .setGameState("Online")
                .setDetails("Playing MultiPlayer")
                .setStartTimedealy(0)
                .setBigImage("connectedmultiplayerbig","Currently causing havoc ;)")
                .setSmallImage("connectedmultiplayersmall","in-game")
                .setModifyScript("RichPresence.smallImageText = Helper.cancellUpdateIfMatch(Helper.getUserName(),null);" +
                        "RichPresence.details = 'In the \\''+Helper.cancellUpdateIfMatch(Helper.getDimensionName(),null)+'\\'';" +
                        "var serverName = Helper.cancellUpdateIfMatch(Helper.getServerIP(),null);" +
                        "RichPresence.state += ' @'+serverName;")
        );
        config.RTProfileList.put("disconnectedFromMultiplayerServer", new RichTextProfile()
                .setActivationEvent(Event.DISCONNECTED_FROM_MULTIPLAYER_SERVER)
                .setGameState("Multiplayer Servers List")
                .setDetails("Was online")
                .setStartTimedealy(0)
                .setBigImage("disconnectedmultiplayerbig","Currently causing havoc ;)")
                .setSmallImage("disconnectedmultiplayersmall","in-game")
                .setModifyScript("RichPresence.smallImageText = Helper.cancellUpdateIfMatch(Helper.getUserName(),null);" +
                        "if(serverName!=undefined) RichPresence.details += ' @'+serverName;")
        );

        // Advanced config
        config.advancedConfig.firstPostLoadScreenAsMainMenu = true;
        config.advancedConfig.mainMenuClassName = null;
        config.advancedConfig.updateintervalMillis = 2000L;

        LOGGER.log(Level.INFO,"Config defaults set.");

        refreshValidEvents();
    }
}