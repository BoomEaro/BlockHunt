package ru.boomearo.blockhunt.objects;

import org.bukkit.Location;
import org.bukkit.Material;

public class SolidPlayer {

    private final BHPlayer player;
    private final Location loc;
    private final Material old;
    
    public SolidPlayer(BHPlayer player, Location loc, Material old) {
        this.player = player;
        this.loc = loc;
        this.old = old;
    }
    
    public BHPlayer getPlayer() {
        return this.player;
    }
    
    public Location getLocation() {
        return this.loc;
    }

    public Material getOldMaterial() {
        return this.old;
    }
    
}
