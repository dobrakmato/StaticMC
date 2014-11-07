package eu.matejkormuth.staticmc;

import java.io.File;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.spacehq.mc.auth.GameProfile;
import org.spacehq.mc.protocol1_8.MinecraftProtocol_18;
import org.spacehq.mc.protocol1_8.ProtocolConstants;
import org.spacehq.mc.protocol1_8.ServerLoginHandler;
import org.spacehq.mc.protocol1_8.data.game.EntityMetadata;
import org.spacehq.mc.protocol1_8.data.game.Position;
import org.spacehq.mc.protocol1_8.data.game.values.PlayerListEntry;
import org.spacehq.mc.protocol1_8.data.game.values.PlayerListEntryAction;
import org.spacehq.mc.protocol1_8.data.game.values.entity.MobType;
import org.spacehq.mc.protocol1_8.data.game.values.entity.player.GameMode;
import org.spacehq.mc.protocol1_8.data.game.values.setting.Difficulty;
import org.spacehq.mc.protocol1_8.data.game.values.world.WorldType;
import org.spacehq.mc.protocol1_8.data.message.ChatColor;
import org.spacehq.mc.protocol1_8.data.message.MessageStyle;
import org.spacehq.mc.protocol1_8.data.message.TextMessage;
import org.spacehq.mc.protocol1_8.data.status.PlayerInfo;
import org.spacehq.mc.protocol1_8.data.status.ServerStatusInfo;
import org.spacehq.mc.protocol1_8.data.status.VersionInfo;
import org.spacehq.mc.protocol1_8.data.status.handler.ServerInfoBuilder;
import org.spacehq.mc.protocol1_8.packet.ingame.client.ClientChatPacket;
import org.spacehq.mc.protocol1_8.packet.ingame.client.player.ClientPlayerMovementPacket;
import org.spacehq.mc.protocol1_8.packet.ingame.server.ServerChatPacket;
import org.spacehq.mc.protocol1_8.packet.ingame.server.ServerJoinGamePacket;
import org.spacehq.mc.protocol1_8.packet.ingame.server.ServerPlayerListDataPacket;
import org.spacehq.mc.protocol1_8.packet.ingame.server.ServerPlayerListEntryPacket;
import org.spacehq.mc.protocol1_8.packet.ingame.server.entity.ServerEntityPositionRotationPacket;
import org.spacehq.mc.protocol1_8.packet.ingame.server.entity.player.ServerPlayerAbilitiesPacket;
import org.spacehq.mc.protocol1_8.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import org.spacehq.mc.protocol1_8.packet.ingame.server.entity.spawn.ServerSpawnMobPacket;
import org.spacehq.mc.protocol1_8.packet.ingame.server.world.ServerSpawnPositionPacket;
import org.spacehq.mc.protocol1_8.packet.ingame.server.world.ServerUpdateTimePacket;
import org.spacehq.packetlib.Server;
import org.spacehq.packetlib.Session;
import org.spacehq.packetlib.event.server.ServerAdapter;
import org.spacehq.packetlib.event.server.SessionAddedEvent;
import org.spacehq.packetlib.event.server.SessionRemovedEvent;
import org.spacehq.packetlib.event.session.PacketReceivedEvent;
import org.spacehq.packetlib.event.session.SessionAdapter;
import org.spacehq.packetlib.tcp.TcpSessionFactory;

public class StaticMC {
    private Server        server;
    private final Logger  log;
    private final int     port;
    private World         world;
    private Config        config;
    
    private static Logger logger;
    
    public static Logger getLogger() {
        return StaticMC.logger;
    }
    
    public StaticMC(final int port) {
        this.port = port;
        this.log = Logger.getLogger("StaticMC");
        this.log.setLevel(Level.ALL);
        StaticMC.logger = this.log;
        this.log.info("StaticMC v 1.0");
    }
    
    public void start() {
        this.log.info("Loading config...");
        try {
            javax.xml.bind.Unmarshaller dese = JAXBContext.newInstance(Config.class).createUnmarshaller();
            Config c = (Config) dese.unmarshal(new File("./config.xml"));
            this.config = c;
        } catch (JAXBException e) {
            this.log.severe("Can't load config.xml, using default config...");
            this.config = new Config();
        }
        
        this.log.info("Loading world...");
        
        this.world = new World();
        
        this.world.setGenerator(new World.FlatGenerator());
        this.world.setLightProcessor(new FastLight_0());
        this.world.addPopulator(new World.LakePopulator());
        this.world.addPopulator(new World.SmallTreePopulator());
        this.world.addPopulator(new World.BigTreePopulator());
        this.world.addPopulator(new World.GrassPopulator());
        this.world.addPopulator(new World.HousePopulator());
        
        this.world.setSpawn(this.config.spawnLocation);
        this.world.prepareSpawn(this.config.spawnSize);
        
        this.log.info("Initializing server...");
        this.server = new Server("0.0.0.0", this.port, MinecraftProtocol_18.class,
                new TcpSessionFactory());
        this.log.info("Starting server on port " + this.port + "...");
        this.server.bind();
        
        this.server.setGlobalFlag(ProtocolConstants.SERVER_COMPRESSION_THRESHOLD, 100);
        this.server.setGlobalFlag(ProtocolConstants.VERIFY_USERS_KEY, false);
        
        this.server.setGlobalFlag(ProtocolConstants.SERVER_LOGIN_HANDLER_KEY,
                new ServerLoginHandler() {
                    @Override
                    public void loggedIn(final Session session) {
                        int pid = StaticMC.this.world.nextEid();
                        session.setFlag("pexel-player-id", pid);
                        
                        // Send join packet.
                        session.send(new ServerJoinGamePacket(pid, false,
                                GameMode.SURVIVAL, 0, Difficulty.PEACEFUL, 1000,
                                WorldType.FLAT, false));
                        
                        // Add to world.
                        Player added = StaticMC.this.world.addPlayer(new Player(session,
                                pid));
                        
                        StaticMC.this.log.info("Client " + added.profile.getName() + "/"
                                + added.profile.getId() + "(" + pid + ") - "
                                + session.getHost() + ":" + session.getPort()
                                + " has logged in!");
                        
                        // Send world data.
                        /*
                         * for (Chunk chunk : StaticMC.this.world.getChunks()) { session.send(new ServerChunkDataPacket(
                         * chunk.coordinates.getChunkX(), chunk.coordinates.getChunkZ(), chunk.chunks, chunk.biomes)); }
                         */
                        
                        // Send spawn position.
                        session.send(new ServerSpawnPositionPacket(new Position(
                                StaticMC.this.world.getSpawn().getBlockX(),
                                StaticMC.this.world.getSpawn().getBlockY(),
                                StaticMC.this.world.getSpawn().getBlockZ())));
                        // Send player abilities.
                        session.send(new ServerPlayerAbilitiesPacket(false, false,
                                false, false, .1F, .1F));
                        // Send helt item.
                        session.send(new org.spacehq.mc.protocol1_8.packet.ingame.server.entity.player.ServerChangeHeldItemPacket(
                                0)); //TODO :Config
                        // Send world time.
                        session.send(new ServerUpdateTimePacket(
                                StaticMC.this.world.getAge(),
                                StaticMC.this.world.getTime()));
                        session.send(new ServerChatPacket(new TextMessage(
                                "Welcome to sIMPLEmc")));
                        
                        // Send player position and look.
                        session.send(new ServerPlayerPositionRotationPacket(
                                StaticMC.this.world.getSpawn().getX(),
                                StaticMC.this.world.getSpawn().getY(),
                                StaticMC.this.world.getSpawn().getZ(),
                                StaticMC.this.world.getSpawn().getYaw(),
                                StaticMC.this.world.getSpawn().getPitch()));
                        
                        // Send world data.
                        int num = 96;
                        int c = 0;
                        int[] x = new int[num];
                        int[] z = new int[num];
                        org.spacehq.mc.protocol1_8.data.game.Chunk[][] chunks = new org.spacehq.mc.protocol1_8.data.game.Chunk[num][];
                        byte[][] biomeData = new byte[num][];
                        for (Chunk chunk : StaticMC.this.world.getChunks()) {
                            if (c == num) {
                                
                                session.send(new org.spacehq.mc.protocol1_8.packet.ingame.server.world.ServerMultiChunkDataPacket(
                                        x, z, chunks, biomeData));
                                
                                x = new int[num];
                                z = new int[num];
                                chunks = new org.spacehq.mc.protocol1_8.data.game.Chunk[num][];
                                biomeData = new byte[num][];
                                c = 0;
                                
                                x[c] = chunk.coordinates.getChunkX();
                                z[c] = chunk.coordinates.getChunkZ();
                                chunks[c] = chunk.chunks;
                                biomeData[c] = chunk.biomes;
                                c++;
                            }
                            else {
                                x[c] = chunk.coordinates.getChunkX();
                                z[c] = chunk.coordinates.getChunkZ();
                                chunks[c] = chunk.chunks;
                                biomeData[c] = chunk.biomes;
                                c++;
                            }
                            /*
                             * session.send(new ServerChunkDataPacket( chunk.coordinates.getChunkX(),
                             * chunk.coordinates.getChunkZ(), chunk.chunks, chunk.biomes));
                             */
                        }
                        
                        session.send(new ServerPlayerListDataPacket(new TextMessage(
                                "SimpleMC server test"), new TextMessage(
                                "everything is broken! yey!")));
                        
                        // Spawn other players.
                        for (Player p : StaticMC.this.world.getPlayers()) {
                            session.send(new ServerEntityPositionRotationPacket(
                                    p.getId(), 0, 0, 0, (float) p.yaw, (float) p.pitch,
                                    p.isOnGround));
                            session.send(new ServerPlayerListEntryPacket(
                                    PlayerListEntryAction.ADD_PLAYER,
                                    (PlayerListEntry[]) Arrays.asList(
                                            new PlayerListEntry(p.profile,
                                                    GameMode.SURVIVAL, 10,
                                                    new TextMessage(p.getDisplayName()))).toArray()));
                            session.send(p.getSpawnPacket());
                        }
                        
                        EntityMetadata[] metadata = new EntityMetadata[0];
                        session.send(new ServerSpawnMobPacket(1, MobType.CREEPER, 17,
                                10, 17, 0, 0, 0, 0, 0, 0, metadata));
                    }
                });
        
        this.server.setGlobalFlag(ProtocolConstants.SERVER_INFO_BUILDER_KEY,
                new ServerInfoBuilder() {
                    @Override
                    public ServerStatusInfo buildInfo(final Session session) {
                        return new ServerStatusInfo(new VersionInfo("StaticMC", 47),
                                new PlayerInfo(80000, 0,
                                        StaticMC.this.world.getPlayerProfiles()),
                                new TextMessage("StaticMC - Pexel.EU"), null);
                    }
                });
        
        this.server.addListener(new ServerAdapter() {
            @Override
            public void sessionAdded(final SessionAddedEvent event) {
                event.getSession().addListener(new SessionAdapter() {
                    @Override
                    public void packetReceived(final PacketReceivedEvent event) {
                        if (event.getPacket() instanceof ClientChatPacket) {
                            // Chat.
                            ClientChatPacket packet = event.getPacket();
                            GameProfile profile = event.getSession().getFlag(
                                    ProtocolConstants.PROFILE_KEY);
                            System.out.println("[CHAT]" + profile.getName() + ": "
                                    + packet.getMessage());
                            StaticMC.this.world.sendGlobalPacket(new ServerChatPacket(
                                    new TextMessage(profile.getName() + ": ").addExtra(new TextMessage(
                                            packet.getMessage()).setStyle(new MessageStyle().setColor(ChatColor.GRAY)))));
                        }
                        
                        //StaticMC.logger.info("Packet/"
                        //       + event.getPacket().getClass().getSimpleName());
                        
                        // Pohyb hracov.
                        if (event.getPacket() instanceof ClientPlayerMovementPacket) {
                            ClientPlayerMovementPacket packet = event.getPacket();
                            Player p = StaticMC.this.world.getPlayer((int) event.getSession().getFlag(
                                    "pexel-player-id"));
                            
                            if (packet.getX() != 0 && packet.getZ() != 0) {
                                p.x = packet.getX();
                                p.y = packet.getY();
                                p.z = packet.getZ();
                                p.pitch = packet.getPitch();
                                p.yaw = packet.getYaw();
                                p.isOnGround = packet.isOnGround();
                                
                                StaticMC.this.world.sendGlobalPacketExceptOne(
                                        new ServerEntityPositionRotationPacket(p.id,
                                                packet.getX() - p.x,
                                                packet.getY() - p.y,
                                                packet.getZ() - p.z,
                                                (float) packet.getYaw(),
                                                (float) packet.getPitch(),
                                                packet.isOnGround()), p);
                            }
                        }
                    }
                });
            }
            
            @Override
            public void sessionRemoved(final SessionRemovedEvent event) {
                Player p = StaticMC.this.world.getPlayer((int) event.getSession().getFlag(
                        "pexel-player-id"));
                StaticMC.this.log.info("Client " + p.profile.getName() + "/"
                        + p.profile.getId() + "(" + p.getId() + ") - "
                        + event.getSession().getHost() + ":"
                        + event.getSession().getPort() + " has disconnected in!");
                StaticMC.this.world.removePlayer(p);
            }
        });
        
        this.handleConsole();
    }
    
    private void handleConsole() {
        this.log.info("Console enabled!");
        
        Scanner scanner = new Scanner(System.in);
        String line = "";
        while ((line = scanner.nextLine()) != null) {
            if (line.equalsIgnoreCase("stop")) {
                this.log.info("Stopping server...");
                for (Session s : this.server.getSessions()) {
                    s.disconnect("Server closed");
                }
                this.server.close();
            }
            else if (line.equalsIgnoreCase("help")) {
                this.log.info("Valid commands: stop, help");
            }
            else {
                this.log.info("Unknown command!");
            }
        }
        scanner.close();
    }
}
