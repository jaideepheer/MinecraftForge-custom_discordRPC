package jdmcmods.custom_discordrpc;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(
        modid = CDRPCmod.MOD_ID,
        name = CDRPCmod.MOD_NAME,
        version = CDRPCmod.VERSION,
        canBeDeactivated = true,
        acceptedMinecraftVersions = "[1.11,)",
        dependencies = "before:*;",
        updateJSON = CDRPCmod.UPDATE_JSON,
        clientSideOnly = true
)
public class CDRPCmod {

    public static final String MOD_ID = "customdiscordrpc";
    public static final String MOD_NAME = "Custom Discord RPC";
    public static final String VERSION = "2.21";
    public static final String UPDATE_JSON = "https://raw.githubusercontent.com/jaideepheer/MinecraftForge-custom_discordRPC/master/src/main/resources/update.json";

    public static Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static File CONFIG_DIR;

    private String firstDetectedScreen = null;
    private boolean connectedToMultiplayer = false;

    // Ask forge to point this variable to the ModMetaData object of this mod.
    @Mod.Metadata(value = MOD_ID)
    public static ModMetadata metadata;

    static {
        discordRPCHandler.startRPC();
        // TODO: this crashes with jar, NullPointerException at "URI uri = CDRPCmod.class.getResource("/").toURI();"
        /*try {
            URI uri = CDRPCmod.class.getResource("/").toURI();
            if (uri.getScheme().equals("jar")) {
                FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
                executionRoot = fileSystem.getPath("/");
            } else {
                executionRoot = Paths.get(uri);
            }
            Stream<Path> walk = Files.walk(executionRoot, 1);
            for (Iterator<Path> it = walk.iterator(); it.hasNext(); ) {
                System.out.println(it.next());
            }
        }catch (Exception e){e.printStackTrace(); executionRoot = null;}*/
    }

    /**
     * This is the instance of your mod as created by Forge. It will never be null.
     */
    @Mod.Instance(MOD_ID)
    public static CDRPCmod INSTANCE;

    /**
     * This is the first initialization event. Register tile entities here.
     * The registry events below will have fired prior to entry to this method.
     */
    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        // Init. metadata
        metadata = event.getModMetadata();
        // Set the metadata
        if(!metadata.authorList.contains("Jaideep Singh Heer"))
            metadata.authorList.add(0,"Jaideep Singh Heer");
        metadata.version = VERSION;
        metadata.description = "This mod allows you to use custom Rich Presence Text for the Discord client.";
        metadata.updateJSON = UPDATE_JSON;
        metadata.logoFile = "icon.png";
        metadata.modId = MOD_ID;

        MinecraftForge.EVENT_BUS.register(this);
        ModConfigManager.setLatestEvent(ModConfigManager.Event.PRE_INIT);
        CONFIG_DIR = event.getModConfigurationDirectory();
        ModConfigManager.init(CONFIG_DIR);
    }

    /**
     * This is the second initialization event. Register custom recipes
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ModConfigManager.setLatestEvent(ModConfigManager.Event.INIT);
    }

    /**
     * This is the final initialization event. Register actions from other mods here
     */
    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {
        ModConfigManager.setLatestEvent(ModConfigManager.Event.POST_INIT);
    }
    
    @Mod.EventHandler
    public void serverAboutTostart(FMLServerAboutToStartEvent e)
    {
        ModConfigManager.setLatestEvent(ModConfigManager.Event.SERVER_ABOUT_TO_START);
    }
    
    @Mod.EventHandler
    public void severStarting(FMLServerStartingEvent e)
    {
        ModConfigManager.setLatestEvent(ModConfigManager.Event.SERVER_STARTING);
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent e)
    {
        ModConfigManager.setLatestEvent(ModConfigManager.Event.SERVER_STOPPING);
    }

    @Mod.EventHandler
    public void serverStopped(FMLServerStoppedEvent e)
    {
        ModConfigManager.setLatestEvent(ModConfigManager.Event.SERVER_STOPPED);
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent e)
    {
        ModConfigManager.setLatestEvent(ModConfigManager.Event.SERVER_STARTED);
    }

    @SubscribeEvent
    public void guiScreenDetect(GuiScreenEvent.InitGuiEvent.Pre e)
    {
        String screenClass = e.getGui().getClass().getName();
        ModConfigManager.ModConfig config = ModConfigManager.getModConfig();

        // Check if we need to use custom class name to detect main menu screen
        if(screenClass.equals(config.advancedConfig.mainMenuClassName))
        {
            LOGGER.log(Level.INFO,"Custom main menu screen detected. ["+screenClass+"]");
            ModConfigManager.setLatestEvent(ModConfigManager.Event.MAIN_MENU_REACHED);
        }
        // Check if we need to treat first screen as main menu screen
        else if(config.advancedConfig.firstPostLoadScreenAsMainMenu)
        {
            if(firstDetectedScreen == null)
            {
                firstDetectedScreen = screenClass;
                LOGGER.log(Level.INFO,"Treating first detected GUI screen as main menu screen. ["+firstDetectedScreen+"]");
            }
            if(screenClass.equals(firstDetectedScreen))
            {
                ModConfigManager.setLatestEvent(ModConfigManager.Event.MAIN_MENU_REACHED);
            }
        }
        // else use vanilla detection
        else if(e.getGui() instanceof GuiMainMenu)
        {
            ModConfigManager.setLatestEvent(ModConfigManager.Event.MAIN_MENU_REACHED);
        }
    }

    @SubscribeEvent
    public void connectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent e)
    {
        if(!e.isLocal())
        {
            connectedToMultiplayer = true;
            ModConfigManager.setLatestEvent(ModConfigManager.Event.CONNECTED_TO_MULTIPLAYER_SERVER);
        }
        else
        {
            ModConfigManager.setLatestEvent(ModConfigManager.Event.CONNECTED_TO_SINGLEPLAYER_SERVER);
        }
    }

    @SubscribeEvent
    public void disconnectedFromServer(FMLNetworkEvent.ClientDisconnectionFromServerEvent e)
    {
        if(connectedToMultiplayer){
            connectedToMultiplayer = false;
            ModConfigManager.setLatestEvent(ModConfigManager.Event.DISCONNECTED_FROM_MULTIPLAYER_SERVER);
        }
        else
        {
            ModConfigManager.setLatestEvent(ModConfigManager.Event.DISCONNECTED_FROM_SINGLEPLAYER_SERVER);
        }
    }
}
