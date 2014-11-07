package org.spacehq.mc.protocol1_8.packet.ingame.server;

import java.io.IOException;

import org.spacehq.mc.protocol1_8.data.message.Message;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.packetlib.io.NetOutput;
import org.spacehq.packetlib.packet.Packet;

public class ServerPlayerListDataPacket implements Packet {
    private Message header;
    private Message footer;
    
    @SuppressWarnings("unused")
    private ServerPlayerListDataPacket() {
    }
    
    public ServerPlayerListDataPacket(final Message header, final Message footer) {
        this.header = header;
        this.footer = footer;
    }
    
    public Message getHeader() {
        return this.header;
    }
    
    public Message getFooter() {
        return this.footer;
    }
    
    @Override
    public void read(final NetInput in) throws IOException {
        this.header = Message.fromString(in.readString());
        this.footer = Message.fromString(in.readString());
    }
    
    @Override
    public void write(final NetOutput out) throws IOException {
        out.writeString(this.header.toJsonString());
        out.writeString(this.footer.toJsonString());
    }
    
    @Override
    public boolean isPriority() {
        return false;
    }
}
