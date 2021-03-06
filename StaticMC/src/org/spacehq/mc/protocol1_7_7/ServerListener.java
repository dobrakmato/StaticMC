package org.spacehq.mc.protocol1_7_7;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.spacehq.mc.auth.GameProfile;
import org.spacehq.mc.auth.SessionService;
import org.spacehq.mc.auth.exception.AuthenticationUnavailableException;
import org.spacehq.mc.protocol1_7_7.data.status.ServerStatusInfo;
import org.spacehq.mc.protocol1_7_7.data.status.handler.ServerInfoBuilder;
import org.spacehq.mc.protocol1_7_7.packet.handshake.client.HandshakePacket;
import org.spacehq.mc.protocol1_7_7.packet.ingame.client.ClientKeepAlivePacket;
import org.spacehq.mc.protocol1_7_7.packet.ingame.server.ServerDisconnectPacket;
import org.spacehq.mc.protocol1_7_7.packet.ingame.server.ServerKeepAlivePacket;
import org.spacehq.mc.protocol1_7_7.packet.login.client.EncryptionResponsePacket;
import org.spacehq.mc.protocol1_7_7.packet.login.client.LoginStartPacket;
import org.spacehq.mc.protocol1_7_7.packet.login.server.EncryptionRequestPacket;
import org.spacehq.mc.protocol1_7_7.packet.login.server.LoginDisconnectPacket;
import org.spacehq.mc.protocol1_7_7.packet.login.server.LoginSuccessPacket;
import org.spacehq.mc.protocol1_7_7.packet.status.client.StatusPingPacket;
import org.spacehq.mc.protocol1_7_7.packet.status.client.StatusQueryPacket;
import org.spacehq.mc.protocol1_7_7.packet.status.server.StatusPongPacket;
import org.spacehq.mc.protocol1_7_7.packet.status.server.StatusResponsePacket;
import org.spacehq.mc.protocol1_7_7.util.CryptUtil;
import org.spacehq.packetlib.Session;
import org.spacehq.packetlib.event.session.ConnectedEvent;
import org.spacehq.packetlib.event.session.DisconnectingEvent;
import org.spacehq.packetlib.event.session.PacketReceivedEvent;
import org.spacehq.packetlib.event.session.SessionAdapter;

import eu.matejkormuth.staticmc.ProtocolMode;

public class ServerListener extends SessionAdapter {
    
    private static KeyPair pair          = CryptUtil.generateKeyPair();
    
    private final byte     verifyToken[] = new byte[4];
    private final String   serverId      = "";
    private String         username      = "";
    
    private long           lastPingTime  = 0;
    private int            lastPingId    = 0;
    
    public ServerListener() {
        new Random().nextBytes(this.verifyToken);
    }
    
    @Override
    public void connected(final ConnectedEvent event) {
        event.getSession().setFlag(ProtocolConstants.PING_KEY, 0);
    }
    
    @Override
    public void packetReceived(final PacketReceivedEvent event) {
        MinecraftProtocol_177 protocol = (MinecraftProtocol_177) event.getSession().getPacketProtocol();
        if (protocol.getMode() == ProtocolMode.HANDSHAKE) {
            if (event.getPacket() instanceof HandshakePacket) {
                HandshakePacket packet = event.getPacket();
                switch (packet.getIntent()) {
                    case 1:
                        protocol.setMode(ProtocolMode.STATUS, false, event.getSession());
                        break;
                    case 2:
                        protocol.setMode(ProtocolMode.LOGIN, false, event.getSession());
                        if (packet.getProtocolVersion() > ProtocolConstants.PROTOCOL_VERSION) {
                            event.getSession().disconnect(
                                    "Outdated server! I'm still on "
                                            + ProtocolConstants.GAME_VERSION + ".");
                        }
                        else if (packet.getProtocolVersion() < ProtocolConstants.PROTOCOL_VERSION) {
                            event.getSession().disconnect(
                                    "Outdated client! Please use "
                                            + ProtocolConstants.GAME_VERSION + ".");
                        }
                        
                        break;
                    default:
                        throw new UnsupportedOperationException(
                                "Invalid client intent: " + packet.getIntent());
                }
            }
        }
        
        if (protocol.getMode() == ProtocolMode.LOGIN) {
            if (event.getPacket() instanceof LoginStartPacket) {
                this.username = event.<LoginStartPacket> getPacket().getUsername();
                boolean verify = event.getSession().hasFlag(
                        ProtocolConstants.VERIFY_USERS_KEY) ? event.getSession().<Boolean> getFlag(
                        ProtocolConstants.VERIFY_USERS_KEY)
                        : true;
                if (verify) {
                    event.getSession().send(
                            new EncryptionRequestPacket(this.serverId, pair.getPublic(),
                                    this.verifyToken));
                }
                else {
                    GameProfile profile = new GameProfile(
                            UUID.nameUUIDFromBytes(("OfflinePlayer:" + this.username).getBytes()),
                            this.username);
                    event.getSession().send(new LoginSuccessPacket(profile));
                    event.getSession().setFlag(ProtocolConstants.PROFILE_KEY, profile);
                    protocol.setMode(ProtocolMode.GAME, false, event.getSession());
                    ServerLoginHandler handler = event.getSession().getFlag(
                            ProtocolConstants.SERVER_LOGIN_HANDLER_KEY);
                    if (handler != null) {
                        handler.loggedIn(event.getSession());
                    }
                    
                    new KeepAliveThread(event.getSession()).start();
                }
            }
            else if (event.getPacket() instanceof EncryptionResponsePacket) {
                EncryptionResponsePacket packet = event.getPacket();
                PrivateKey privateKey = pair.getPrivate();
                if (!Arrays.equals(this.verifyToken, packet.getVerifyToken(privateKey))) {
                    throw new IllegalStateException("Invalid nonce!");
                }
                else {
                    SecretKey key = packet.getSecretKey(privateKey);
                    protocol.enableEncryption(key);
                    new UserAuthThread(event.getSession(), key).start();
                }
            }
        }
        
        if (protocol.getMode() == ProtocolMode.STATUS) {
            if (event.getPacket() instanceof StatusQueryPacket) {
                ServerInfoBuilder builder = event.getSession().getFlag(
                        ProtocolConstants.SERVER_INFO_BUILDER_KEY);
                if (builder == null) {
                    event.getSession().disconnect("No server info builder set.");
                }
                
                ServerStatusInfo info = builder.buildInfo(event.getSession());
                event.getSession().send(new StatusResponsePacket(info));
            }
            else if (event.getPacket() instanceof StatusPingPacket) {
                event.getSession().send(
                        new StatusPongPacket(
                                event.<StatusPingPacket> getPacket().getPingTime()));
            }
        }
        
        if (protocol.getMode() == ProtocolMode.GAME) {
            if (event.getPacket() instanceof ClientKeepAlivePacket) {
                ClientKeepAlivePacket packet = event.getPacket();
                if (packet.getPingId() == this.lastPingId) {
                    long time = (System.nanoTime() / 1000000L) - this.lastPingTime;
                    event.getSession().setFlag(ProtocolConstants.PING_KEY, time);
                }
            }
        }
    }
    
    @Override
    public void disconnecting(final DisconnectingEvent event) {
        MinecraftProtocol_177 protocol = (MinecraftProtocol_177) event.getSession().getPacketProtocol();
        if (protocol.getMode() == ProtocolMode.LOGIN) {
            event.getSession().send(new LoginDisconnectPacket(event.getReason()));
        }
        else if (protocol.getMode() == ProtocolMode.GAME) {
            event.getSession().send(new ServerDisconnectPacket(event.getReason()));
        }
    }
    
    private class UserAuthThread extends Thread {
        private final Session   session;
        private final SecretKey key;
        
        public UserAuthThread(final Session session, final SecretKey key) {
            this.key = key;
            this.session = session;
        }
        
        @Override
        public void run() {
            MinecraftProtocol_177 protocol = (MinecraftProtocol_177) this.session.getPacketProtocol();
            try {
                String serverHash = new BigInteger(CryptUtil.getServerIdHash(
                        ServerListener.this.serverId, pair.getPublic(), this.key)).toString(16);
                SessionService service = new SessionService();
                GameProfile profile = service.hasJoinedServer(new GameProfile(
                        (UUID) null, ServerListener.this.username), serverHash);
                if (profile != null) {
                    this.session.send(new LoginSuccessPacket(profile));
                    this.session.setFlag(ProtocolConstants.PROFILE_KEY, profile);
                    protocol.setMode(ProtocolMode.GAME, false, this.session);
                    ServerLoginHandler handler = this.session.getFlag(ProtocolConstants.SERVER_LOGIN_HANDLER_KEY);
                    if (handler != null) {
                        handler.loggedIn(this.session);
                    }
                    
                    new KeepAliveThread(this.session).start();
                }
                else {
                    this.session.disconnect("Failed to verify username!");
                }
            } catch (AuthenticationUnavailableException e) {
                this.session.disconnect("Authentication servers are down. Please try again later, sorry!");
            }
        }
    }
    
    private class KeepAliveThread extends Thread {
        private final Session session;
        
        public KeepAliveThread(final Session session) {
            this.session = session;
        }
        
        @Override
        public void run() {
            ServerListener.this.lastPingTime = System.nanoTime() / 1000000L;
            while (this.session.isConnected()) {
                long curr = System.nanoTime() / 1000000L;
                long time = curr - ServerListener.this.lastPingTime;
                if (time > 2000) {
                    ServerListener.this.lastPingTime = curr;
                    ServerListener.this.lastPingId = (int) curr;
                    this.session.send(new ServerKeepAlivePacket(
                            ServerListener.this.lastPingId));
                }
                
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
}
