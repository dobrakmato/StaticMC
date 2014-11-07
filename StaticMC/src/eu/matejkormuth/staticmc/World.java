package eu.matejkormuth.staticmc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.spacehq.mc.auth.GameProfile;
import org.spacehq.mc.protocol1_8.data.game.NibbleArray3d;
import org.spacehq.mc.protocol1_8.data.game.ShortArray3d;
import org.spacehq.mc.protocol1_8.data.game.values.PlayerListEntry;
import org.spacehq.mc.protocol1_8.data.game.values.PlayerListEntryAction;
import org.spacehq.mc.protocol1_8.packet.ingame.server.ServerPlayerListEntryPacket;
import org.spacehq.packetlib.packet.Packet;

public class World {
    protected Location                     spawn;
    protected Map<ChunkCoordinates, Chunk> chunks     = new HashMap<ChunkCoordinates, Chunk>();
    protected WorldGenerator               generator;
    protected final List<WorldPopulator>   populators = new ArrayList<WorldPopulator>();
    protected final List<Player>           players    = new ArrayList<Player>();
    protected final AtomicInteger          eidCounter = new AtomicInteger();
    protected final Random                 random     = new Random();
    private int                            spawnSize  = 10;
    protected UUID                         worldUID   = UUID.randomUUID();
    private LightProcessor                 lightProcessor;
    
    public void loadChunk(final int x, final int z) {
        this.loadChunk(new ChunkCoordinates(x, z));
    }
    
    public void loadChunk(final ChunkCoordinates coordinates) {
        if (this.chunks.get(coordinates) == null) {
            // Load or generate.
            this.regenerateChunk(coordinates);
        }
    }
    
    public void regenerateChunk(final ChunkCoordinates coordinates) {
        this.chunks.put(coordinates, this.generator.generate(coordinates, this.random));
        for (WorldPopulator populator : this.populators) {
            populator.populate(this.chunks.get(coordinates), this.random);
        }
        this.updateLight(coordinates);
    }
    
    private void updateLight(final ChunkCoordinates coordinates) {
        this.lightProcessor.compute(this.chunks.get(coordinates));
    }
    
    public void prepareSpawn(final int size) {
        this.spawnSize = size;
        this.prepareSpawn();
    }
    
    public void prepareSpawn() {
        StaticMC.getLogger().info(
                "Generating spawn of size " + this.spawnSize + " chunks...");
        int spawnChunkX = this.spawn.getChunkX();
        int spawnChunkZ = this.spawn.getChunkZ();
        for (int x = -1 * this.spawnSize; x < this.spawnSize; x++) {
            for (int z = -1 * this.spawnSize; z < this.spawnSize; z++) {
                this.regenerateChunk(new ChunkCoordinates(spawnChunkX + x, spawnChunkZ
                        + z));
            }
        }
        StaticMC.getLogger().info("Generated " + this.chunks.size() + " chunks!");
    }
    
    public void unloadChunk(final int x, final int z) {
        
    }
    
    public void setGenerator(final WorldGenerator generator) {
        this.generator = generator;
        StaticMC.getLogger().info(
                "World's " + this.worldUID.toString() + " chunk generator set to "
                        + generator.getClass().getSimpleName());
    }
    
    public void setLightProcessor(final LightProcessor lightProcessor) {
        this.lightProcessor = lightProcessor;
        StaticMC.getLogger().info(
                "Using " + lightProcessor.getClass().getSimpleName()
                        + " as light processor for world " + this.worldUID.toString());
    }
    
    public void addPopulator(final WorldPopulator populator) {
        this.populators.add(populator);
        StaticMC.getLogger().info(
                "Added populator " + populator.getClass().getSimpleName() + " to world "
                        + this.worldUID.toString());
    }
    
    public void setChunk(final int x, final int z, final Chunk chunk) {
        this.chunks.put(new ChunkCoordinates(x, z), chunk);
    }
    
    public Chunk getChunk(final int x, final int z) {
        return this.chunks.get(new ChunkCoordinates(x, z));
    }
    
    public Location getSpawn() {
        return this.spawn;
    }
    
    public void setSpawn(final Location spawn) {
        this.spawn = spawn;
    }
    
    public static final class SmallTreePopulator implements WorldPopulator {
        @Override
        public void populate(final Chunk chunk, final Random random) {
            if (random.nextInt(7) == 0) {
                byte leaves = (byte) random.nextInt(16);
                //build tree
                int x = 4, z = 5;
                int y = chunk.getHighestBlock(x, z);
                chunk.setBlock(x, y, z, 17, (byte) 1);
                chunk.setBlock(x, y + 1, z, 17, (byte) 1);
                chunk.setBlock(x, y + 2, z, 17, (byte) 1);
                chunk.setBlock(x, y + 3, z, 17, (byte) 1);
                chunk.setBlock(x, y + 4, z, 17, (byte) 1);
                chunk.setBlock(x, y + 5, z, 17, (byte) 1);
                chunk.setBlock(x, y + 6, z, 17, (byte) 1);
                
                for (int a = -2; a < 3; a++) {
                    for (int b = -2; b < 3; b++) {
                        for (int c = 0; c < 4; c++) {
                            chunk.setBlock(x + a, y + 4 + c, z + b, 18, leaves);
                            
                            if (c != 4) {
                                chunk.setBlock(x, y + 4, z, 17, (byte) 1);
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static final class GrassPopulator implements WorldPopulator {
        @Override
        public void populate(final Chunk chunk, final Random random) {
            int y = chunk.getHighestBlock(0, 0) + 1;
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    if (chunk.getBlock(x, y, z) == 0 && random.nextInt(50) == 0) {
                        chunk.setBlock(x, y, z, 31, (byte) 1);
                        int m = random.nextInt(8);
                        for (int i = 0; i < m; i++) {
                            chunk.setBlock(x - 3 + random.nextInt(6), y,
                                    z - 3 + random.nextInt(6), 31, (byte) 1);
                        }
                    }
                }
            }
        }
    }
    
    public static final class HousePopulator implements WorldPopulator {
        @Override
        public void populate(final Chunk chunk, final Random random) {
            int y = chunk.getHighestBlock(0, 0) + 1;
            if (random.nextInt(40) == 0) {
                int sx = random.nextInt(12);
                int sz = random.nextInt(12);
                for (int x = sx; x < sx + 4; x++) {
                    for (int w = y; w < y + 5; w++) {
                        for (int z = sz; z < sz + 4; z++) {
                            if (w < y + 4) {
                                if (x == sx || x == sx + 3 || z == sz || z == sz + 3) {
                                    chunk.setBlock(x, w, z, 5, (byte) 0);
                                }
                            }
                            else {
                                chunk.setBlock(x, w, z, 45, (byte) 0);
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static final class BigTreePopulator implements WorldPopulator {
        @Override
        public void populate(final Chunk chunk, final Random random) {
            if (random.nextInt(7) == 0) {
                byte leaves = (byte) random.nextInt(16);
                //build tree
                int x = random.nextInt(12) + 2, z = random.nextInt(12) + 2;
                int y = chunk.getHighestBlock(x, z);
                int oldMaterial = chunk.getBlock(x, y, z);
                
                if (oldMaterial != 17 && oldMaterial != 18) {
                    for (int k = x - 1; k < x + 1; k++) {
                        for (int j = z - 1; j < z + 1; j++) {
                            for (int h = y + 1; h < y + 8; h++) {
                                chunk.setBlock(k, h, j, 17, (byte) 0);
                            }
                        }
                    }
                    
                    for (int a = -5; a < 5; a++) {
                        for (int b = -5; b < 5; b++) {
                            for (int c = 0; c < 8; c++) {
                                chunk.setBlock(x + a, y + 8 + c, z + b, 18, leaves);
                                
                                if (random.nextBoolean()) {
                                    chunk.setBlock(x + a - 1 + random.nextInt(2), y + 8,
                                            z + b - 1 + random.nextInt(2), 18, leaves);
                                }
                                
                                if (c != 4) {
                                    chunk.setBlock(x, y + 8, z, 17, (byte) 1);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static final class LakePopulator implements WorldPopulator {
        @Override
        public void populate(final Chunk chunk, final Random random) {
            if (random.nextInt(9) == 0) {
                int y = chunk.getHighestBlock(2, 2);
                int h = random.nextInt(9) + 2;
                int w = random.nextInt(9) + 2;
                int u = random.nextInt(5);
                int o = random.nextInt(5);
                for (int x = u; x < (u + h); x++) {
                    for (int z = o; z < (o + w); z++) {
                        chunk.setBlock(x, y, z, 9, (byte) 0);
                        if (random.nextBoolean()) {
                            chunk.setBlock(x - 2 + random.nextInt(4), y,
                                    z - 2 + random.nextInt(4), 9, (byte) 0);
                            chunk.setBlock(x - 2 + random.nextInt(5), y,
                                    z - 2 + random.nextInt(5), 9, (byte) 0);
                        }
                    }
                }
            }
        }
    }
    
    public static final class FlatGenerator implements WorldGenerator {
        @Override
        public Chunk generate(final ChunkCoordinates coordintes, final Random random) {
            org.spacehq.mc.protocol1_8.data.game.Chunk[] chunks = new org.spacehq.mc.protocol1_8.data.game.Chunk[16];
            byte[] biomes = new byte[256];
            Arrays.fill(
                    biomes,
                    (byte) ((Math.sin(coordintes.getChunkX() * coordintes.getChunkZ()) + 1) * 10));
            
            for (int i = 0; i < 16; i++) {
                ShortArray3d blocks = new ShortArray3d(16 * 16 * 16);
                NibbleArray3d blocklight = new NibbleArray3d(16 * 16 * 16);
                NibbleArray3d skylight = new NibbleArray3d(16 * 16 * 16);
                
                for (int a = 0; a < 16; a++) {
                    for (int b = 0; b < 16; b++) {
                        for (int c = 0; c < 16; c++) {
                            if (i == 0 && b < 5) {
                                blocks.setBlockAndData(a, b, c, 2, 0);
                            }
                            blocklight.set(a, b, c, 15);
                            skylight.set(a, b, c, 15);
                        }
                    }
                }
                chunks[i] = new org.spacehq.mc.protocol1_8.data.game.Chunk(blocks,
                        blocklight, skylight);
            }
            return new Chunk(coordintes, chunks, biomes);
        }
    }
    
    public Collection<Chunk> getChunks() {
        return this.chunks.values();
    }
    
    public Player addPlayer(final Player player) {
        this.players.add(player);
        this.sendGlobalPacket(player.getSpawnPacket());
        //this.sendGlobalPacket(new ServerPlayerListEntryPacket(
        //        PlayerListEntryAction.ADD_PLAYER,
        //        new PlayerListEntry[] { new PlayerListEntry(player.profile,
        //               GameMode.SURVIVAL) }));
        return player;
    }
    
    public void sendGlobalPacket(final Packet packet) {
        for (Player p : this.players) {
            p.session.send(packet);
        }
    }
    
    public void removePlayer(final int id) {
        this.removePlayer(this.getPlayer(id));
    }
    
    public Player getPlayer(final int id) {
        for (Player p : this.players) {
            if (p.id == id) { return p; }
        }
        return null;
    }
    
    public List<Player> getPlayers() {
        return this.players;
    }
    
    public int nextEid() {
        return this.eidCounter.getAndIncrement();
    }
    
    public void removePlayer(final Player p) {
        PlayerListEntry[] entries = new PlayerListEntry[1];
        entries[0] = new PlayerListEntry(p.profile);
        
        this.sendGlobalPacket(new ServerPlayerListEntryPacket(
                PlayerListEntryAction.REMOVE_PLAYER, entries));
        this.players.remove(p);
    }
    
    public GameProfile[] getPlayerProfiles() {
        List<Player> players2 = this.players;
        GameProfile[] profiles = new GameProfile[players2.size()];
        for (int i = 0; i < players2.size(); i++) {
            Player p = players2.get(i);
            profiles[i] = p.profile;
        }
        return profiles;
    }
    
    /**
     * @return
     */
    public long getAge() {
        return 20 * 10;
    }
    
    /**
     * @return
     */
    public long getTime() {
        return 4000L;
    }
    
    /**
     * @param serverEntityPositionRotationPacket
     */
    public void sendGlobalPacketExceptOne(final Packet packet, final Player exception) {
        for (Player p : this.players) {
            if (p != exception) {
                p.session.send(packet);
            }
        }
    }
}
