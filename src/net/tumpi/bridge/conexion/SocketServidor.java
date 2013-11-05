/*
 * Clase de comunicacion por sockets en un servidor para varios clientes.
 * Tambien se define la subclase Cliente.
 * Resumen SocketServidor:
 * - Constructor: unicamente crea y 'conecta' el socket.
 * - isBound: devuelve si el socket esta o no 'conectado'.
 * - startSearchClients: arranca un thread que busca clientes poniendose en un
 *                       modo de espera. Cuando se conecta alguien lo añade a
 *                       la lista de clientes y dispara el evento. Despues
 *                       vuelve a esperar.
 * - finishSearchClients: detiene la busqueda de clientes.
 * - enviarMensajeServer: envia un mensaje a un destino en concreto o a todos
 *                        los clientes de la lista si en la direccion de destino
 *                        se pone un '*'.
 * - getClientsCount: devuelve el numero de clientes conectados.
 * - closeSocket: Cierra la comunicación. No se puede volver a conectar.
 * - [add/remove]ServerSocketListener: añade o remueve un listener concreto de 
 *                                     la lista de listeners.
 * - fire[evento]: dispara el evento que sea.
 * 
 * Resumen Cliente:
 * - Constructor: Dandole el socket ya conectado guarda los datos (ip) del
 *                cliente y inicia los streams de comunicacion.
 * - [start/stop]ListenClient: inicia o detiene el thread que escucha los
 *                             mensajes del cliente en concreto.
 * - enviarMensaje: envia un mensaje al cliente.
 * - close: Cierra la conexion con el cliente.
 */
package net.tumpi.bridge.conexion;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import net.tumpi.bridge.log.Log;

/**
 *
 * @author 66785270
 */
public class SocketServidor {

    private ServerSocket serverSocket = null;
    private Thread thread_buscar_clientes = null;
    public HashMap<String, Cliente> clientes = new HashMap<>();
    private ArrayList<ServerSocketListener> listeners = new ArrayList();

    public SocketServidor(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public boolean isBound() {
        return serverSocket.isBound();
    }

    public void startSearchClients() {
        thread_buscar_clientes = new Thread() {
            @Override
            public void run() {
                while (!serverSocket.isClosed()) {
                    try {
                        Cliente cliente;
                        Socket socket = serverSocket.accept();
                        cliente = new Cliente(socket, SocketServidor.this);
                        clientes.put(cliente.getId(), cliente);
                        cliente.startListeningClient();
                        fireClientConnectedEvent(cliente.getId());
                    } catch (IOException ex) {
                    }
                }
            }
        };
        thread_buscar_clientes.start();
    }

    public void finishSearchClients() {
        thread_buscar_clientes.interrupt();
    }

    public void enviarMensajeServer(String clientId, String mensaje) throws IOException {
        clientes.get(clientId).enviarMensaje(mensaje);
        Log.$.info("Mensaje enviado a " + clientId + " : " + mensaje);
    }

    public int getClientsCount() {
        return clientes.size();
    }

    public void closeSocket() throws IOException {
        finishSearchClients();
        for (Cliente cliente : clientes.values()) {
            cliente.enviarMensaje("exit");
            cliente.finishListeningClient();
        }
        serverSocket.close();
    }

    public void addServerSocketListener(ServerSocketListener listener) {
        listeners.add(listener);
    }

    public void removeServerSocketListener(ServerSocketListener listener) {
        listeners.remove(listener);
    }

    public void fireClientConnectedEvent(String ip) {
        for (ServerSocketListener listener : listeners) {
            listener.onClientConnected(ip);
        }
    }

    public void fireClientDisconnectedEvent(String ip) {
        for (ServerSocketListener listener : listeners) {
            listener.onClientDisconnected(ip);
        }
    }

    public void fireMessageReceivedEvent(String ip, String message) {
        for (ServerSocketListener listener : listeners) {
            listener.onMessageReceived(ip, message);
        }
    }
}
