package net.tumpi.bridge.modelos;

import java.util.Objects;

/**
 *
 * @author 66785270
 */
public class TumpiClient {

    public final String id;

    public TumpiClient(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TumpiClient other = (TumpiClient) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.id);
        return hash;
    }
}
