package eu.matejkormuth.staticmc;

import java.util.Random;

public interface WorldPopulator {
    public void populate(Chunk chunk, Random random);
}
