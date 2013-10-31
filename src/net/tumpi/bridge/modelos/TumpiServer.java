/*
 * La colección usada para encapsular los clientes es HashMap<>,
 * ya que es más eficiente para las funciones 'isClient' y 'getClient'
 * (No es necesario recorrer toda la lista, se indexa por id).
 * 
 * Para encapsular dicho mapa se han creado las funciones de acceso 
 * necesarias de modo que si en el futuro se decide cambiar de colección
 * tan sólo haya que cambiarlo aquí, y no tener que modificar cada referencia
 * a la colección en el resto del código. 
 * 
 * Además se implementa Iterable para poder recorrer los clientes indepen-
 * dientemente de la colección usada.
 * 
 */
package net.tumpi.bridge.modelos;

import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author 66785270
 */
public class TumpiServer implements Iterable<TumpiClient> {

    private final HashMap<String, TumpiClient> clientes;
    public final String nombre;
    public final String id;

    public TumpiServer(String name, String id) {
        clientes = new HashMap<>();
        this.nombre = name;
        this.id = id;
    }

    public boolean isClient(String id) {
        return clientes.containsKey(id);
    }

    public TumpiClient getClient(String id) {
        return clientes.get(id);
    }

    public boolean putClient(TumpiClient cliente) {
        if (!isClient(cliente.id)) {
            clientes.put(cliente.id, cliente);
            return true;
        }
        return false;
    }

    public TumpiClient getCliente(String id) {
        return clientes.get(id);
    }

    public void removeCliente(String id) {
        this.clientes.remove(id);
    }

    public void removeAllClients() {
        this.clientes.clear();
    }

    @Override
    public Iterator<TumpiClient> iterator() {
        return clientes.values().iterator();
    }
}
