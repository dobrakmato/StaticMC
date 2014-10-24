package eu.matejkormuth.staticmc;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "config")
public class Config {
    public int      spawnSize     = 10;
    public Location spawnLocation = new Location(256, 25, 256, 0, 0);
}
