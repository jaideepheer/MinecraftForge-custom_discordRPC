package jdmcmods.custom_discordrpc;

import jdmcmods.custom_discordrpc.ScriptEngine.modScriptEngine;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.Level;

import javax.script.ScriptException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static jdmcmods.custom_discordrpc.CDRPCmod.LOGGER;

public class discordRPCHandler {
    private static DiscordEventHandlers eventHandlers = discordEventHandlers.getEventHandlers();
    private static long lastRPCUpdateTime=-1L;
    private static boolean sendUpdateNowMarker = false;
    private static Thread RPCWatchdogThread = null;
    private static final Object ThreadSyncLock = new Object();

    private static final Object discordCallbackExecutor = new Object(){
        @SubscribeEvent
        public void tickEvent(TickEvent.ClientTickEvent e)
        {
            DiscordRPC.discordRunCallbacks();
        }
    };

    private static AtomicBoolean isRPCRunning = new AtomicBoolean(false);

    public static void startRPC()
    {
        if(!isRPCRunning.get()) {
            ModConfigManager.ModConfig config = ModConfigManager.getModConfig();

            // Initialize discordRPC
            DiscordRPC.discordInitialize(config.discordAppID, eventHandlers, true);

            // Reset the script engine
            modScriptEngine.resetEngine();

            // register callback executor to rpc events
            MinecraftForge.EVENT_BUS.register(discordCallbackExecutor);

            isRPCRunning.set(true);

            // launch rich text updater thread
            launchRPCWatchdogThread();
        }
    }

    public static void sendUpdateNOW()
    {
        if(!isRPCRunning.get())return;
        sendUpdateNowMarker = true;
        if(RPCWatchdogThread.isAlive())
        {
            RPCWatchdogThread.interrupt();
        }
        else {
            LOGGER.log(Level.ERROR,"RPCWatchdogThread isn't alive, can't sendUpdateNOW(). :P");
        }
    }

    private static void launchRPCWatchdogThread()
    {
        RPCWatchdogThread = new Thread(()->{
            // set discord min update interval
            ModConfigManager.ModConfig config = ModConfigManager.getModConfig();
            long updateIntervalMilli = config.advancedConfig.updateintervalMillis;
            long delay;
            ModConfigManager.RichTextProfile textProfile = null;
            String prevProfileName = null;
            boolean profileChanged = true;
            boolean isUpdateCancelled = false;
            AtomicBoolean isRunning = isRPCRunning;

            // run while isRPCRunning is true
            while(isRunning.get()) {

                // calculate time elapsed since last update
                delay = System.currentTimeMillis()-lastRPCUpdateTime;
                // calculate time to wait
                delay = updateIntervalMilli - delay;

                // reset isUpdateCancelled
                isUpdateCancelled = false;

                // wait update interval before update
                if(!sendUpdateNowMarker && delay>0) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.ERROR, "Zzz...Puff! What!? RPCWatchdogThread interrupted... Somebody wants me to send update NOW.");
                    } catch (Exception e) {
                        e.printStackTrace();
                        LOGGER.log(Level.ERROR, "RPCWatchdogThread had an exception while sleeping...! wtf!? This shouldn't happen.");
                    }
                }
                sendUpdateNowMarker = false;

                // Select the profile to use
                for(Map.Entry<String,ModConfigManager.RichTextProfile> entry:config.RTProfileList.entrySet())
                {
                    // Check if current profile is the one to use
                    if(entry.getValue().shouldActivate())
                    {
                        textProfile = entry.getValue();
                        if(!entry.getKey().equals(prevProfileName)) {
                            // Only update if profile changed
                            prevProfileName = entry.getKey();
                            profileChanged = true;
                            LOGGER.log(Level.DEBUG, "Profile changed to '" + prevProfileName + "'");
                        }
                        break;
                    }
                }

                if(textProfile!=null && isRunning.get()) {
                    // Get RichPresence
                    DiscordRichPresence presence = textProfile.getRichPresence(profileChanged);
                    if(textProfile.modifyScript != null) {
                        long timestamp = System.currentTimeMillis();
                        // Run script engine to allow modifications
                        try {
                            modScriptEngine.eval(textProfile.modifyScript, presence, prevProfileName, profileChanged);
                        }
                        catch (ScriptException e) {
                            e.printStackTrace();
                            LOGGER.log(Level.ERROR, "Script execution failed for profile '" + prevProfileName + "'");
                        }
                        catch (RuntimeException e)
                        {
                            if(e.getCause() instanceof modScriptEngine.CancellScriptUpdateException)
                            {
                                LOGGER.log(Level.INFO, "Script cancelled RPC update."
                                        +"[Profile: "+prevProfileName+"]"
                                        +"[ValidEvent: "+ModConfigManager.validEvent+"]"
                                        +"[LatestEvent: "+ModConfigManager.latestEvent+"]");
                                isUpdateCancelled = true;
                            }
                            else {
                                e.printStackTrace();
                                LOGGER.log(Level.ERROR, "Script threw an exception [Profile: " + prevProfileName + "]");
                            }
                        }
                        // Log the script execution time
                        LOGGER.log(Level.DEBUG, "Script execution for profile '"+prevProfileName+"' took: " + (System.currentTimeMillis() - timestamp) + "ms.");
                    }
                    if(!isUpdateCancelled) {
                        // Validate Rich Presence Data
                        if(ModConfigManager.RichTextProfile.validateRichPresence(presence)) {
                            // Update Rich presence
                            synchronized (ThreadSyncLock) {
                                if (isRunning.get()) DiscordRPC.discordUpdatePresence(presence);
                            }
                            LOGGER.log(Level.DEBUG, "RPCWatchdogThread: RPC updated.[LatestEvent: " + ModConfigManager.latestEvent + "]"
                                    + "[ValidEvent: " + ModConfigManager.validEvent + "]"
                                    + "[Profile: " + prevProfileName + "]"
                                    + "(+" + (System.currentTimeMillis() - lastRPCUpdateTime) + " sec.)"
                            );
                            lastRPCUpdateTime = System.currentTimeMillis();
                        }
                        else {
                            LOGGER.log(Level.ERROR,"RichPresence was invalid, can't send update to Discord.");
                        }
                    }
                    profileChanged = false;
                }

            }
            LOGGER.log(Level.INFO,Thread.currentThread().getName()+" has stopped.");
        },"DiscordRPCWatchdogThread[APPID: "+ModConfigManager.getModConfig().discordAppID+"]");
        RPCWatchdogThread.setDaemon(true);
        RPCWatchdogThread.start();
    }

    public static void stopRPC()
    {
        if(isRPCRunning.getAndSet(false)) {
            LOGGER.log(Level.INFO, "Stopping RPC Watchdog Thread...");
            MinecraftForge.EVENT_BUS.unregister(discordCallbackExecutor);
            synchronized (ThreadSyncLock) {
                DiscordRPC.discordClearPresence();
                DiscordRPC.discordShutdown();
            }
            isRPCRunning = new AtomicBoolean(false);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        stopRPC();
    }
}
