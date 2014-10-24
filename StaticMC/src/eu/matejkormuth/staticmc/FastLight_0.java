package eu.matejkormuth.staticmc;

/**
 * @author Mato Kormuth
 * 
 */
public class FastLight_0 implements LightProcessor {
    @Override
    public void compute(final Chunk ch) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int bll = 15;
                for (int y = 255; y > 0; y--) {
                    ch.setSkyLight(x, y % 16, z, bll);
                    if (ch.getBlock(x, y, z) != 0) {
                        bll = 10;
                    }
                }
            }
        }
    }
}
