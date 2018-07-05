package jdmcmods.custom_discordrpc;

import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.callbacks.*;
import org.apache.logging.log4j.Level;

import static jdmcmods.custom_discordrpc.CDRPCmod.LOGGER;

class discordEventHandlers {
    private static ReadyCallback readyCallback = (discordUser)->{
        LOGGER.log(Level.INFO,"Discord ready, sending update NOW...");
        discordRPCHandler.sendUpdateNOW();
    };
    private static JoinRequestCallback joinRequestCallback = (discordUser)->{};
    private static DisconnectedCallback disconnectedCallback = (errCode,message)->{
        LOGGER.log(Level.INFO,"OMG OoO, Discord gone.(Discord Disconnected)");
    };
    private static ErroredCallback erroredCallback = (errCode,message)->{};
    private static JoinGameCallback joinGameCallback = (joinSecret)->{};
    private static SpectateGameCallback spectateGameCallback = (spectateSecret)->{};

    static DiscordEventHandlers getEventHandlers()
    {
        return new DiscordEventHandlers.Builder()
                .setDisconnectedEventHandler(disconnectedCallback)
                .setErroredEventHandler(erroredCallback)
                .setJoinGameEventHandler(joinGameCallback)
                .setJoinRequestEventHandler(joinRequestCallback)
                .setReadyEventHandler(readyCallback)
                .setSpectateGameEventHandler(spectateGameCallback)
                .build();
    }
}
