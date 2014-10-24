package eu.matejkormuth.staticmc;

import java.security.Key;

import org.spacehq.packetlib.Session;

public interface MinecraftProtocol {
    
    /**
     * @return
     */
    ProtocolMode getMode();
    
    /**
     * @param status
     * @param b
     * @param session
     */
    void setMode(ProtocolMode status, boolean b, Session session);
    
    /**
     * @param key
     */
    void enableEncryption(Key key);
    
}
