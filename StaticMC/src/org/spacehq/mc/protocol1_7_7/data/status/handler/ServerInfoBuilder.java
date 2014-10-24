package org.spacehq.mc.protocol1_7_7.data.status.handler;

import org.spacehq.mc.protocol1_7_7.data.status.ServerStatusInfo;
import org.spacehq.packetlib.Session;

public interface ServerInfoBuilder {

	public ServerStatusInfo buildInfo(Session session);
	
}
