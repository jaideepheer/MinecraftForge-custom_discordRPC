package jdmcmods.custom_discordrpc.ScriptEngine;

import jdmcmods.custom_discordrpc.CDRPCmod;
import jdmcmods.custom_discordrpc.ModConfigManager;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Level;

import javax.script.*;
import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.Consumer;

import static jdmcmods.custom_discordrpc.CDRPCmod.LOGGER;

public class modScriptEngine {
    private static final ScriptEngine engine;

    static {
        engine = makeScriptEngine();
        resetEngine();
    }

    public static ScriptEngine getEngine() {
        return engine;
    }

    public static void resetEngine()
    {
        Bindings b = engine.createBindings();
        b.remove("exit");
        b.remove("quit");
        b.put("Helper", new ScriptHelper());
        engine.setBindings(b,ScriptContext.ENGINE_SCOPE);
    }

    private static ScriptEngine makeScriptEngine()
    {
        ScriptEngine engine = null;

        HashMap<String,Class<?>> nashornUsedClasses = new HashMap<>();
        ScriptEngineFactory nashornFactory = getNashornScriptEngineFactory(nashornUsedClasses);

        // Get the ClassFilter class object
        Class classFilter = nashornUsedClasses.get("jdk.nashorn.api.scripting.ClassFilter");

        if(nashornFactory !=null && classFilter != null) {
            try {
                // Use Dynamic Proxy to create an instance of ClassFilter(interface)
                Object classFilterInstance = Proxy.newProxyInstance(classFilter.getClassLoader(), new Class<?>[]{classFilter}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if(args!=null && method !=null && method.getName().equals("exposeToScripts") && args.length>0 && args[0] != null)
                        {
                            String className = (String) args[0];
                            return !(className.startsWith("java")
                                    || className.startsWith("jdmcmods")
                                    || className.startsWith("javax")
                                    || className.equals("net.minecraft.util.Session")
                            );
                        }
                        else return false;
                    }
                });
                Method getScriptEngine = nashornFactory.getClass().getMethod("getScriptEngine",classFilter);
                engine = (ScriptEngine) getScriptEngine.invoke(nashornFactory,classFilterInstance);
            } catch (NoSuchMethodException e) {
                LOGGER.log(Level.ERROR, "Couldn't find getScriptEngine(ClassLoader) method in nashornFactory.");
                e.printStackTrace();
            }
            catch (IllegalArgumentException e){
                LOGGER.log(Level.ERROR,"Probably couldn't create ClassFilter instance.");
                e.printStackTrace();
            }
            catch (IllegalAccessException e){
                LOGGER.log(Level.ERROR,"Couldn't access NashornScriptEngineFacroty's getScriptEngine(ClassFilter) function.");
                e.printStackTrace();
            }
            catch (InvocationTargetException e){
                LOGGER.log(Level.ERROR,"Couldn't create NashornScriptEngine from factory using custom ClassLoader.");
                e.printStackTrace();
            }
        }
        else {
            LOGGER.log(Level.ERROR,"Something didn't go right, got a null.");
            LOGGER.log(Level.ERROR,"nashornFactory = "+nashornFactory);
            LOGGER.log(Level.ERROR,"classFilter = "+classFilter);
        }
        if (engine == null) {
            LOGGER.log(Level.FATAL,"engine was still null after all that hack");
            LOGGER.log(Level.FATAL,"engine="+engine);
        }

        return engine;
    }

    /**
     * Uses hack to get a NashornScriptEngineFactory object.
     * @param nashornUsedClasses all classes detected in the NashornScriptEngineFactory class are added to this map where the key is the full name of the class.
     * @return the NashornScriptEngineFactory object
     */
    private static ScriptEngineFactory getNashornScriptEngineFactory(Map<String,Class<?>> nashornUsedClasses)
    {
        // First create a new ScriptEngineManager with null class loader to force it to load classes automatically.
        LOGGER.log(Level.INFO,"Attempting hack to get NashornScriptEngineFactory object.");
        ScriptEngineManager s = new ScriptEngineManager(null);
        LOGGER.log(Level.DEBUG,"New ScriptEngineManager with 'null' classloader created.");

        // DEBUG
        LOGGER.log(Level.DEBUG,"Loaded ScriptEngineFactory count = "+s.getEngineFactories().size());
        LOGGER.log(Level.DEBUG,"getEngineByName(\"nashorn\") = "+s.getEngineByName("nashorn"));

        // Loop through list of ScriptEngineFactory to find NashornScriptEngineFactory
        for(ScriptEngineFactory f :s.getEngineFactories())
        {
            // Check name
            if(f.getEngineName().endsWith("Nashorn"))
            {
                LOGGER.log(Level.INFO,"Found Nashorn ScriptEngineFactory.");
                LOGGER.log(Level.INFO,"Factory engine name = "+f.getEngineName());
                LOGGER.log(Level.INFO,"Factory class = "+f.getClass());

                // List of methods to check for obfuscation
                LOGGER.log(Level.DEBUG,"Factory Method list:-");
                for (Method method : f.getClass().getDeclaredMethods()) {
                    LOGGER.log(Level.DEBUG,method.getName()+", Parameter Types:-");
                    for(Class<?> c:method.getParameterTypes())
                    {
                        LOGGER.log(Level.DEBUG,c.getName());
                        nashornUsedClasses.put(c.getName(),c);
                    }
                }
                LOGGER.log(Level.INFO,"Successfully retrieved NashornScriptEngineFactory object.");
                return f;
            }
        }
        LOGGER.log(Level.INFO,"Couldn't get NashornScriptEngineFactory object.");
        return null;
    }

    public static void eval(String script, DiscordRichPresence currentRP, String currentProfileName, boolean hasProfileJustChanged)throws ScriptException
    {
        engine.put("RichPresence", currentRP);
        engine.put("CurrentProfileName", currentProfileName);
        engine.put("hasProfileChanged",hasProfileJustChanged);
        engine.put("log",(Consumer<String>)CDRPCmod.LOGGER::info);
        engine.eval(script);
    }

    public static class ScriptHelper
    {
        //=====================
        // Mod Related Helpers
        //=====================
        public File getConfigDir()
        {
            return CDRPCmod.CONFIG_DIR;
        }
        public String getLatestActivationEvent()
        {
            return ModConfigManager.latestEvent.toString();
        }
        public void cancellUpdate()throws CancellScriptUpdateException{throw new CancellScriptUpdateException("Script update cancelled");}
        public Object cancellUpdateIfMatch(Object o, Object toMatch) throws CancellScriptUpdateException {
            if((o == toMatch)||(o!=null && o.equals(toMatch))){
                cancellUpdate();
            }
            return o;
        }

        //======================
        // Game Related Helpers
        //======================
        public long getMinecraftStartTimeMillis(){
            return getMinecraft().getPlayerUsageSnooper().getMinecraftStartTimeMillis();
        }
        public Minecraft getMinecraft()
        {
            return Minecraft.getMinecraft();
        }
        public String getMCVERSION()
        {
            return MinecraftForge.MC_VERSION;
        }
        public boolean isSinglePlayer(){return getMinecraft().isSingleplayer();}
        public boolean inGameHasFocus(){return getMinecraft().inGameHasFocus;}
        public boolean isGamePaused(){return getMinecraft().isGamePaused();}

        //========================
        // Server Related Helpers
        //========================
        public ServerData getServerData(){return getMinecraft().getCurrentServerData();}
        public String getServerName(){ServerData s = getServerData();if(s==null)return null; else return s.serverName;}
        public String getServerIP(){ServerData s = getServerData();if(s==null)return null; else return s.serverIP;}
        public String getServerMOTD(){ServerData s = getServerData();if(s==null)return null; else return s.serverMOTD;}
        public String getServerGameVersion(){ServerData s = getServerData();if(s==null)return null; else return s.gameVersion;}
        public boolean getServerIsOnLAN(){ServerData s = getServerData();if(s==null)return false; else return s.isOnLAN();}
        public long getServerPing(){ServerData s = getServerData();if(s==null)return -1; else return s.pingToServer;}
        public String getServerPopulationInfo(){ServerData s = getServerData();if(s==null)return null; else return s.populationInfo;}

        //=======================
        // World Related Helpers
        //=======================
        public World getWorld()
        {
            return getMinecraft().world;
        }
        public String getWorldName(){try{return getMinecraft().getIntegratedServer().getWorldName();}catch (Exception e){return null;}}
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
            try{return getWorldProvider().getDimensionType().getName();}catch (Exception e){return null;}
        }
        public int getDimensionID(){
            return getWorldProvider().getDimension();
        }

        //======================
        // User Related Helpers
        //======================
        public String getUserName(){
            try{return getMinecraft().getSession().getUsername();}catch (Exception e){e.printStackTrace();return null;}
        }
    }
    public static class CancellScriptUpdateException extends ScriptException {
        public CancellScriptUpdateException(String s) {
            super(s);
        }
    }
}
