package eu.matejkormuth.staticmc;

import java.util.Random;

public interface WorldGenerator {
    public Chunk generate(ChunkCoordinates coordinates, Random random);
}
