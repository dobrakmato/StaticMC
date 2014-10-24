package org.spacehq.mc.protocol1_8;

import org.spacehq.packetlib.Session;

public interface ServerLoginHandler {

	public void loggedIn(Session session);

}
