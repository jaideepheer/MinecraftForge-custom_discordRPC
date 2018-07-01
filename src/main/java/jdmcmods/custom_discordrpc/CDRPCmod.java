package jdmcmods.custom_discordrpc;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = CDRPCmod.MOD_ID,
        name = CDRPCmod.MOD_NAME,
        version = CDRPCmod.VERSION,
        acceptedMinecraftVersions = "*",
        canBeDeactivated = true,
        updateJSON = CDRPCmod.UPDATE_JSON,
        clientSideOnly = true
)
public class CDRPCmod {

    public static final String MOD_ID = "customdiscordrpc";
    public static final String MOD_NAME = "Custom Discord RPC";
    public static final String VERSION = "0.91";
    public static final String UPDATE_JSON = "https://raw.githubusercontent.com/jaideepheer/MinecraftForge-custom_discordRPC/master/src/main/resources/update.json";

    public static Logger LOGGER = LogManager.getLogger();

    // Ask forge to point this variable to the ModMetaData object of this mod.
    @Mod.Metadata(value = MOD_ID)
    public static ModMetadata metadata;

    private final ModConfigManager configManager = new ModConfigManager();

    static {
        discordRPCHandler.startRPC();
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
        metadata.authorList.add(0,"Jaideep Singh Heer");
        metadata.version = VERSION;
        metadata.description = "This mod allows you to use custom Rich Presence Text for the Discord client.";
        metadata.updateJSON = UPDATE_JSON;
        metadata.logoFile = "icon.png";
        metadata.modId = MOD_ID;

        MinecraftForge.EVENT_BUS.register(this);
        ModConfigManager.setLatestEvent(ModConfigManager.LatestEvent.PRE_INIT);
        ModConfigManager.init(event.getModConfigurationDirectory());
    }

    /**
     * This is the second initialization event. Register custom recipes
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ModConfigManager.setLatestEvent(ModConfigManager.LatestEvent.INIT);
    }

    /**
     * This is the final initialization event. Register actions from other mods here
     */
    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {
        ModConfigManager.setLatestEvent(ModConfigManager.LatestEvent.POST_INIT);
    }
    
    @Mod.EventHandler
    public void serverAboutTostart(FMLServerAboutToStartEvent e)
    {
        ModConfigManager.setLatestEvent(ModConfigManager.LatestEvent.SERVER_ABOUT_TO_START);
    }
    
    @Mod.EventHandler
    public void severStarting(FMLServerStartingEvent e)
    {
        ModConfigManager.setLatestEvent(ModConfigManager.LatestEvent.SERVER_STARTING);
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent e)
    {
        ModConfigManager.setLatestEvent(ModConfigManager.LatestEvent.SERVER_STOPPING);
    }

    @Mod.EventHandler
    public void serverStopped(FMLServerStoppedEvent e)
    {
        ModConfigManager.setLatestEvent(ModConfigManager.LatestEvent.SERVER_STOPPED);
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent e)
    {
        ModConfigManager.setLatestEvent(ModConfigManager.LatestEvent.SERVER_STARTED);
    }

    @SubscribeEvent
    public void guiScreenDetect(GuiScreenEvent.InitGuiEvent.Pre e)
    {
        if(e.getGui() instanceof GuiMainMenu)
        {
            ModConfigManager.setLatestEvent(ModConfigManager.LatestEvent.MAIN_MENU_REACHED);
        }
    }
}
