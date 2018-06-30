package jdmcmods.custom_discordrpc;

import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.callbacks.*;

class discordEventHandlers {
    private static ReadyCallback readyCallback = (discordUser)->{};
    private static JoinRequestCallback joinRequestCallback = (discordUser)->{};
    private static DisconnectedCallback disconnectedCallback = (errCode,message)->{};
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
