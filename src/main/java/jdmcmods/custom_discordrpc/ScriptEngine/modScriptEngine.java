package jdmcmods.custom_discordrpc.ScriptEngine;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import jdmcmods.custom_discordrpc.CDRPCmod;
import jdmcmods.custom_discordrpc.ModConfigManager;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

import javax.script.*;
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
                )
        );
        engine.put("Helper",new ScriptHelper());
    }

    public void eval(String script, DiscordRichPresence currentRP, String currentProfileName, boolean hasProfileJustChanged)throws ScriptException
    {
        engine.put("RichPresence", currentRP);
        engine.put("CurrentProfileName", currentProfileName);
        engine.put("hasProfileChanged",hasProfileJustChanged);
        engine.put("LatestActivationEvent",ModConfigManager.latestEvent.toString());
        engine.put("log",(Consumer<String>)CDRPCmod.LOGGER::info);
        engine.eval(script);
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
        public String getConfigDir()
        {
            return CDRPCmod.CONFIG_DIR.getAbsolutePath();
        }
    }
}
