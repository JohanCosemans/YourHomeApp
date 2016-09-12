package net.yourhome.app.net;

/**
 * Created by Johan on 10-2-2016.
 * This is a modified version of the de.tavendo.autobahn.WebSocketConnection class.
 * It adds a 'forcedDisconnect' method to reset the connection without waiting until it is actually closed.
 */

import de.tavendo.autobahn.WebSocket;

public class WebSocketConnection extends de.tavendo.autobahn.WebSocketConnection {
    public void forcedDisconnect() {
        failConnection(WebSocket.ConnectionHandler.CLOSE_CONNECTION_LOST,"Forcing connection lost");
    }
}
