package org.spacehq.mc.protocol1_8.data.status.handler;

import org.spacehq.packetlib.Session;

public interface ServerPingTimeHandler {

	public void handle(Session session, long pingTime);

}
