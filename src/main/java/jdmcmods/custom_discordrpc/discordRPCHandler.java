package jdmcmods.custom_discordrpc;

import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.Level;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static jdmcmods.custom_discordrpc.CDRPCmod.LOGGER;

public class discordRPCHandler {
    private static DiscordEventHandlers eventHandlers = discordEventHandlers.getEventHandlers();
    private static long lastRPCUpdateTime=-1L;
    private static Thread RPCWatchdogThread = null;

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

            // register callback executor to rpc events
            MinecraftForge.EVENT_BUS.register(discordCallbackExecutor);

            isRPCRunning.set(true);

            // launch rich text updater thread
            launchRPCWatchdogThread();
        }
    }

    private static void launchRPCWatchdogThread()
    {
        RPCWatchdogThread = new Thread(()->{
            // set discord min update interval
            long discordMinUpdateInterval = 15;
            long delay = 15;
            ModConfigManager.ModConfig config = ModConfigManager.getModConfig();
            ModConfigManager.RichTextProfile textProfile = null;
            String prevProfileName = null;
            boolean profileChanged = true;
            AtomicBoolean isRunning = isRPCRunning;

            // run while isRPCRunning is true
            while(isRunning.get()) {

                // calculate delay
                delay = System.currentTimeMillis()-lastRPCUpdateTime;

                // wait 15 sec. before update
                try {
                    delay = discordMinUpdateInterval*1000 - delay;
                    Thread.sleep(delay>0?delay:0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    LOGGER.log(Level.ERROR,"RPCWatchdogThread interrupted... This shouldn't happen.");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    LOGGER.log(Level.ERROR,"RPCWatchdogThread had an exception... This shouldn't happen.");
                }

                // Select the profile to use
                for(Map.Entry<String,ModConfigManager.RichTextProfile> entry:config.RTProfileList.entrySet())
                {
                    // Check if current profile is the one to use
                    if(entry.getValue().shouldActivate() && !entry.getKey().equals(prevProfileName))
                    {
                        // Only update if profile changed
                        prevProfileName = entry.getKey();
                        textProfile = entry.getValue();
                        profileChanged = true;
                        break;
                    }
                }

                if(textProfile!=null) {
                    // Update Rich presence
                    DiscordRPC.discordUpdatePresence(textProfile.getRichPresence(profileChanged));
                    profileChanged = false;
                    LOGGER.log(Level.DEBUG,"RPCWatchdogThread: RPC updated.[Event: "+ModConfigManager.latestEvent+"]"
                            +"(+"+ (System.currentTimeMillis() - lastRPCUpdateTime) + " sec.)"
                    );
                    lastRPCUpdateTime = System.currentTimeMillis();
                }

            }
        },"DiscordRPCWatchdogThread["+ModConfigManager.getModConfig().discordAppID+"]");
        RPCWatchdogThread.start();
    }

    public static void stopRPC()
    {
        if(isRPCRunning.getAndSet(false)) {
            MinecraftForge.EVENT_BUS.unregister(discordCallbackExecutor);
            DiscordRPC.discordShutdown();
            isRPCRunning = new AtomicBoolean(false);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        stopRPC();
    }
}
