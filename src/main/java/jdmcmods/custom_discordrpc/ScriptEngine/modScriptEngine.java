package jdmcmods.custom_discordrpc.ScriptEngine;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import jdmcmods.custom_discordrpc.CDRPCmod;
import jdmcmods.custom_discordrpc.ModConfigManager;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Level;

import javax.script.*;
import java.io.File;
import java.util.function.Consumer;

public class modScriptEngine {
    private final ScriptEngine engine;

    public modScriptEngine()
    {
        // prevent access to java.* , javax.* and jdmcmods.*
        engine = new NashornScriptEngineFactory().getScriptEngine((className)->
                !(className.startsWith("java")
                        || className.startsWith("jdmcmods")
                        || className.startsWith("javax")
                        || className.equals("net.minecraft.util.Session")
                )
        );
        Bindings b = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        b.remove("exit");
        b.remove("quit");
        engine.put("Helper",new ScriptHelper());
    }

    public void eval(String script, DiscordRichPresence currentRP, String currentProfileName, boolean hasProfileJustChanged)throws ScriptException
    {
        engine.put("RichPresence", currentRP);
        engine.put("CurrentProfileName", currentProfileName);
        engine.put("hasProfileChanged",hasProfileJustChanged);
        engine.put("log",(Consumer<String>)CDRPCmod.LOGGER::info);
        try{engine.eval(script);}
        catch (Exception e)
        {
            e.printStackTrace();
            CDRPCmod.LOGGER.log(Level.ERROR,"Script threw an exception.[Profile: "+currentProfileName+"]");
        }
    }

    public static class ScriptHelper
    {
        public Minecraft getMinecraft()
        {
            return Minecraft.getMinecraft();
        }
        public String getMCVERSION()
        {
            return MinecraftForge.MC_VERSION;
        }
        public File getConfigDir()
        {
            return CDRPCmod.CONFIG_DIR;
        }
        public String getLatestActivationEvent()
        {
            return ModConfigManager.latestEvent.toString();
        }

        //======================
        // Game Related Helpers
        //======================
        public long getMinecraftStartTimeMillis(){
            return getMinecraft().getPlayerUsageSnooper().getMinecraftStartTimeMillis();
        }

        //=======================
        // World Related Helpers
        //=======================
        public World getWorld()
        {
            return getMinecraft().world;
        }
        public WorldInfo getWorldInfo() {
            try{return getWorld().getWorldInfo();}catch (Exception e){return null;}
        }
        public File getWorldDirectory(){
            return DimensionManager.getCurrentSaveRootDirectory();
        }
        public WorldProvider getWorldProvider(){
            if(getWorld()!=null)return getWorld().provider;
            else return null;
        }
        public long getWorldTime(){return getWorldInfo().getWorldTime();}
        public long getTotalWorldTime(){return getWorldInfo().getWorldTotalTime();}

        //===========================
        // Dimension Related Helpers
        //===========================
        public String getDimensionName(){
            return getWorldProvider().getDimensionType().getName();
        }
        public int getDimensionID(){
            return getWorldProvider().getDimension();
        }

        //======================
        // User Related Helpers
        //======================
        public String getUserName(){
            return getMinecraft().getSession().getUsername();
        }
    }
}
