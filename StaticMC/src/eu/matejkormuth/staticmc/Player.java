package eu.matejkormuth.staticmc;

import java.util.UUID;

import org.spacehq.mc.auth.GameProfile;
import org.spacehq.mc.protocol1_8.ProtocolConstants;
import org.spacehq.mc.protocol1_8.data.game.EntityMetadata;
import org.spacehq.mc.protocol1_8.data.game.values.entity.MetadataType;
import org.spacehq.mc.protocol1_8.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import org.spacehq.packetlib.Session;

public class Player extends Entity {
    public final Session session;
    public GameProfile   profile;
    public UUID          uuid;
    protected boolean    isOnGround;
    
    public Player(final Session session, final int id) {
        this.id = id;
        this.session = session;
        this.profile = session.getFlag(ProtocolConstants.PROFILE_KEY);
        this.uuid = this.profile.getId();
    }
    
    public UUID getUUID() {
        return this.profile.getId();
    }
    
    public String getName() {
        return this.profile.getName();
    }
    
    public ServerSpawnPlayerPacket getSpawnPacket() {
        return new ServerSpawnPlayerPacket(this.id, this.uuid, this.x, this.y, this.z,
                (float) this.yaw, (float) this.pitch, 0,
                new EntityMetadata[] { new EntityMetadata(2, MetadataType.STRING,
                        this.getName()) });
    }
    
    public String getDisplayName() {
        return this.getName();
    }
}
