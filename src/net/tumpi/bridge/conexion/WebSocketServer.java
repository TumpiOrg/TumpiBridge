package net.tumpi.bridge.conexion;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import net.tumpi.bridge.log.Log;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

public class WebSocketServer extends WebSocketServlet {
    
    private static final long serialVersionUID = 1L;
    private final Map<String, Set<ClienteWebSocket>> connectedClients = new ConcurrentHashMap();
    
    @Override
    public void init() throws ServletException {
        super.init();
        String[] channels = {"chat", "counter"};
        
        for (String channel : channels) {
            this.connectedClients.put(channel, new CopyOnWriteArraySet());
        }
    }
    
    @Override
    public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        String channel = request.getRequestURI().split("/prime-push/")[1];
        Log.$.info("Canal:" + channel);
        return new ClienteWebSocket(channel);
    }
    
    private class ClienteWebSocket implements WebSocket, WebSocket.OnTextMessage {
        
        Connection connection;
        String channel;
        
        public ClienteWebSocket(String channel) {
            this.channel = channel;
        }
        
        @Override
        public void onClose(int closeCode, String message) {
            connectedClients.get(this.channel).remove(this);
        }
        
        @Override
        public void onOpen(Connection connection) {
            this.connection = connection;
            connectedClients.get(this.channel).add(this);
        }
        
        @Override
        public void onMessage(String message) {
            Log.$.info(message);
            try {
                for (ClienteWebSocket ws : connectedClients.get(this.channel)) {
                    ws.connection.sendMessage(message);
                    ws.connection.sendMessage("<html><body>FJIJFDJFDJIFDJIFD</body></html>");
                }
            } catch (IOException e) {
                Log.$.error("", e);
            }
        }
    }
}