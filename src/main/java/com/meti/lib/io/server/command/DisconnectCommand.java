package com.meti.lib.io.server.command;

import com.meti.lib.io.client.Client;
import com.meti.lib.io.client.ClientLoop;
import com.meti.lib.io.server.ServerState;

/**
 * @author SirMathhman
 * @version 0.0.0
 * @since 12/20/2017
 */
public class DisconnectCommand extends Command {
    @Override
    public void perform(ServerState state, Client client) {
        if (state.getClientLoops().size() == 0) {
            throw new UnsupportedOperationException("Number of clients in server cannot be zero");
        } else {
            for (ClientLoop loop : state.getClientLoops()) {
                if (loop.getClient().equals(client) && loop.isRunning()) {
                    loop.setRunning(false);
                    return;
                }
            }

            throw new IllegalStateException("Server contains no clients with specified client");
        }
    }
}
