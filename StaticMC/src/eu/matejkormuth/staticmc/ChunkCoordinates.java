package eu.matejkormuth.staticmc;

public class ChunkCoordinates {
    private final int chunkX;
    private final int chunkZ;
    
    /**
     * @param chunkX
     * @param chunkZ
     */
    public ChunkCoordinates(final int chunkX, final int chunkZ) {
        super();
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }
    
    /**
     * @return the chunkX
     */
    public int getChunkX() {
        return this.chunkX;
    }
    
    /**
     * @return the chunkZ
     */
    public int getChunkZ() {
        return this.chunkZ;
    }
    
    @Override
    public String toString() {
        return "ChunkCoordinates [chunkX=" + this.chunkX + ", chunkZ=" + this.chunkZ
                + "]";
    }
}
