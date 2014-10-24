package org.spacehq.mc.protocol1_7_7;

import org.spacehq.packetlib.Session;

public interface ServerLoginHandler {
	
	public void loggedIn(Session session);
	
}
