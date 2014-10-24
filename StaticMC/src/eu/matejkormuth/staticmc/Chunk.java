package eu.matejkormuth.staticmc;

public class Chunk {
    public final org.spacehq.mc.protocol1_8.data.game.Chunk[] chunks;
    public final byte[]                                    biomes;
    public final ChunkCoordinates                          coordinates;
    
    public Chunk(final int x, final int z,
            final org.spacehq.mc.protocol1_8.data.game.Chunk[] chunks, final byte[] biomes) {
        this.coordinates = new ChunkCoordinates(x, z);
        this.chunks = chunks;
        this.biomes = biomes;
    }
    
    public Chunk(final ChunkCoordinates coordintes,
            final org.spacehq.mc.protocol1_8.data.game.Chunk[] chunks, final byte[] biomes) {
        this.coordinates = coordintes;
        this.chunks = chunks;
        this.biomes = biomes;
    }
    
    public void setBlock(final int x, final int y, final int z, final int block,
            final byte data) {
        if (x > 0 && x < 16 && z > 0 && z < 16 && y > 0 && y < 256) {
            this.chunks[y / 16].getBlocks().setBlockAndData(x, y % 16, z, block, data);
        }
        else {
            
        }
    }
    
    public void setBlockLight(final int x, final int y, final int z, final int val) {
        if (x > 0 && x < 16 && z > 0 && z < 16 && y > 0 && y < 256) {
            this.chunks[y / 16].getBlockLight().set(x, y, z, val);
        }
        else {
            
        }
    }
    
    public void setSkyLight(final int x, final int y, final int z, final int val) {
        if (x > 0 && x < 16 && z > 0 && z < 16 && y > 0 && y < 256) {
            this.chunks[y / 16].getSkyLight().set(x, y, z, val);
        }
        else {
            
        }
    }
    
    public int getHighestBlock(final int x, final int z) {
        for (int i = 15; i >= 0; i--) {
            for (int j = 15; j >= 0; j--) {
                if (this.chunks[i].getBlocks().get(x, j, z) != 0) { return j; }
            }
        }
        return 0;
    }
    
    public int getBlock(final int x, final int y, final int z) {
        return this.chunks[y / 16].getBlocks().get(x, y % 16, z);
    }
}
