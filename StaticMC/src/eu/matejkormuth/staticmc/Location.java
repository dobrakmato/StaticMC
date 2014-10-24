package eu.matejkormuth.staticmc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "location")
@XmlAccessorType(XmlAccessType.FIELD)
public class Location {
    private double   x;
    private double   y;
    private double   z;
    private float    yaw;
    private float    pitch;
    protected String world;
    
    /**
     * @param x
     * @param y
     * @param z
     * @param yaw
     * @param pitch
     */
    public Location(final double x, final double y, final double z, final float yaw,
            final float pitch) {
        super();
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }
    
    /**
     * @return the x
     */
    public double getX() {
        return this.x;
    }
    
    /**
     * @param x
     *            the x to set
     */
    public void setX(final double x) {
        this.x = x;
    }
    
    /**
     * @return the y
     */
    public double getY() {
        return this.y;
    }
    
    /**
     * @param y
     *            the y to set
     */
    public void setY(final double y) {
        this.y = y;
    }
    
    /**
     * @return the z
     */
    public double getZ() {
        return this.z;
    }
    
    /**
     * @param z
     *            the z to set
     */
    public void setZ(final double z) {
        this.z = z;
    }
    
    /**
     * @return the yaw
     */
    public float getYaw() {
        return this.yaw;
    }
    
    /**
     * @param yaw
     *            the yaw to set
     */
    public void setYaw(final float yaw) {
        this.yaw = yaw;
    }
    
    /**
     * @return the pitch
     */
    public float getPitch() {
        return this.pitch;
    }
    
    /**
     * @param pitch
     *            the pitch to set
     */
    public void setPitch(final float pitch) {
        this.pitch = pitch;
    }
    
    public int getBlockX() {
        return (int) this.x;
    }
    
    public int getBlockY() {
        return (int) this.y;
    }
    
    public int getBlockZ() {
        return (int) this.z;
    }
    
    public int getChunkX() {
        return (int) this.x / 16;
    }
    
    public int getChunkZ() {
        return (int) this.z / 16;
    }
}
