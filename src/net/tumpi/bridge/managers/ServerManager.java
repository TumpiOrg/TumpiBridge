package net.tumpi.bridge.managers;

import net.tumpi.bridge.modelos.TumpiClient;
import net.tumpi.bridge.modelos.TumpiServer;
import net.tumpi.bridge.conexion.Cliente;
import net.tumpi.bridge.conexion.ServerSocketListener;
import net.tumpi.bridge.conexion.SocketServidor;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.tumpi.bridge.config.Config;
import net.tumpi.bridge.log.Log;

/**
 *
 * @author 66785270
 */
public class ServerManager implements ServerSocketListener {

    private ConcurrentHashMap<String, TumpiServer> servidores;
    private SocketServidor socket;
    private Config config;

    public ServerManager() {
        servidores = new ConcurrentHashMap<>();
        config = Config.instance();
    }

    public boolean arrancarBridge() {
        try {
            socket = new SocketServidor(config.getPuerto());
            socket.addServerSocketListener(this);
            socket.startSearchClients();

            return socket.isBound();
        } catch (Exception ex) {
            Log.$.error("Error al arrancar el BridgeSocialDj", ex);
            return false;
        }
    }

    @Override
    public void onMessageReceived(String id, String data) {
        Log.$.info("Mensaje recibido de " + id + " : " + data);
        String tipo = getType(data);
        String idDest = getId(data);
        switch (idDest) {
            case "log":
                procesarLogIn(id, tipo, getMessage(data));
                break;
            case "exit":
                procesarExit(id);
                break;
            default:
                procesarMessage(id, tipo, idDest, getMessage(data));
                break;
        }
    }

    private void procesarLogIn(String id, String tipo, String message) {
        final String nick = message.substring(0, message.lastIndexOf("|"));
        final String uuid = message.substring(message.lastIndexOf("|") + 1);
        switch (tipo) {
            case "c":
                String idServer = getServerIdByNick(nick);
                if (idServer != null) {
                    TumpiServer server = getServer(idServer);
                    if (!server.isClient(id)) {
                        server.putClient(new TumpiClient(id));
                        sendClientNotification(server.id, uuid, "on");
                        sendLoginResponse(id, 1);
                    }
                } else {
                    sendLoginResponse(id, 0);
                }
                break;
            case "s":
                String serverId = getServerIdByNick(nick);
                if (serverId == null) {
                    servidores.put(id, new TumpiServer(nick, id));
                    Log.$.info("Registrado servidor con nick: " + nick + " y id: " + id);
                    sendLoginResponse(id, 1);
                } else {
                    sendLoginResponse(id, 0);
                }
                break;
        }
    }

    private void procesarExit(String id) {
        try {
            socket.clientes.get(id).close();
        } catch (IOException ex) {
            Log.$.error("Error al desconectar al cliente", ex);
        }
    }

    private void procesarMessage(String origen, String tipo, String destino, String message) {
        try {
            switch (tipo) {
                case "c":
                    if (serverAlreadyExists(origen)) {
                        TumpiServer server = getServer(origen);
                        if ("*".equals(destino)) {
                            for (TumpiClient cliente : server) {
                                socket.enviarMensajeServer(cliente.id, message);
                            }
                        } else if (server.isClient(destino)) {
                            socket.enviarMensajeServer(destino, message);
                        }
                    }
                    break;
                case "s":
                    String id_server = getServerIdByNick(destino);
                    if (id_server != null) {
                        if (getServer(id_server).isClient(origen)) {
                            socket.enviarMensajeServer(getServer(id_server).id, origen + "|" + message);
                        }
                    }
                    break;
            }
        } catch (Exception ex) {
            Log.$.error("Error al procesar el mensaje recibido", ex);
        }
    }

    @Override
    public void onClientConnected(String id) {
        Log.$.info("Conexion abierta >> " + id);
    }

    @Override
    public void onClientDisconnected(String id) {
        if (serverAlreadyExists(id)) {
            TumpiServer server = getServer(id);
            try {
                Map<String, Cliente> clientes_socket = socket.clientes;
                for (TumpiClient cliente_tumpi : server) {
                    Cliente cliente_socket = clientes_socket.get(cliente_tumpi.id);
                    cliente_socket.close();
                }
                server.removeAllClients();

            } catch (IOException ex) {
                Log.$.error("Error al desconectar al cliente");
            }
            servidores.remove(id);
        } else {
            Collection<TumpiServer> values = servidores.values();
            for (TumpiServer server : values) {
                if (server.isClient(id)) {
                    server.removeCliente(id);
                    sendClientNotification(server.id, id, "off");
                }
            }
        }
        Log.$.info("Cliente desconectado >> " + id);
    }

    private void sendClientNotification(String idServer, String uuid, String estado) {
        try {
            socket.enviarMensajeServer(idServer, "b:client_" + estado + "|" + uuid);
        } catch (IOException ex) {
            Log.$.error("Error al enviar la notificacion de nuevo cliente", ex);
        }
    }

    private void sendLoginResponse(String id, int tipo) {
        try {
            socket.enviarMensajeServer(id, "b:log|" + tipo);
        } catch (IOException ex) {
            Log.$.error("Error al enviar respuesta al Log-In", ex);
        }
    }

    private String getServerIdByNick(String nick) {
        Collection<TumpiServer> values = servidores.values();
        for (TumpiServer server : values) {
            if (server.nombre.equals(nick)) {
                return server.id;
            }
        }
        return null;
    }

    private boolean serverAlreadyExists(String id) {
        return servidores.containsKey(id);
    }

    private TumpiServer getServer(String id) {
        return servidores.get(id);
    }

    private String getMessage(String men) {
        return men.substring(men.indexOf("|") + 1);
    }

    private String getId(String men) {
        return men.substring(men.indexOf(":") + 1, men.indexOf("|"));
    }

    private String getType(String men) {
        return men.substring(0, men.indexOf(":"));
    }

}
