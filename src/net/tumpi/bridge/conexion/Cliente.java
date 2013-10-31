/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.tumpi.bridge.conexion;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * @author 66785270
 */
public class Cliente extends Object {

    private Socket socketCliente = null;
    private String ipCliente = null;
    private int puertoCliente = 0;
    private SocketServidor servidor;
    private String id = "";
    private InputStream input = null;
    private OutputStream output = null;
    private DataInputStream inputStream = null;
    private DataOutputStream outputStream = null;

    public Cliente(Socket socket, SocketServidor server) throws IOException {
        socketCliente = socket;
        ipCliente = socket.getInetAddress().getHostAddress();
        puertoCliente = socket.getPort();
        id = ipCliente + ":" + puertoCliente;
        input = socketCliente.getInputStream();
        output = socketCliente.getOutputStream();
        inputStream = new DataInputStream(input);
        outputStream = new DataOutputStream(output);
        servidor = server;
    }

    public void startListeningClient() {
        new Thread() {
            @Override
            public void run() {
                String textoRecibido;
                try {
                    do {
                        textoRecibido = inputStream.readUTF();
                        if (!"exit".equals(textoRecibido)) {
                            servidor.fireMessageReceivedEvent(id, textoRecibido);
                        }
                    } while (!"exit".equals(textoRecibido) && inputStream != null);
                } catch (IOException ex) {
                } finally {
                    try {
                        close();
                    } catch (IOException ex) {
                    }
                }
            }
        }.start();
    }

    public void finishListeningClient() throws IOException {
        if (inputStream != null) {
            inputStream.close();
            inputStream = null;
        }
    }

    public void enviarMensaje(String mensaje) throws IOException {
        if (socketCliente.isConnected() && !socketCliente.isClosed()) {
            outputStream.writeUTF(mensaje);
            outputStream.flush();
        }
    }

    public void close() throws IOException {
        finishListeningClient();
        socketCliente.close();
        servidor.clientes.remove(this);
        servidor.fireClientDisconnectedEvent(id);
    }
    
    public String getId(){
        return this.id;
    }
}
