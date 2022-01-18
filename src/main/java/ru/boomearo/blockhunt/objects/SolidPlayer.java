package ru.boomearo.blockhunt.objects;

import org.bukkit.Location;
import org.bukkit.Material;

import ru.boomearo.blockhunt.objects.playertype.HiderPlayer;

public class SolidPlayer {

    private final BHPlayer player;
    private final HiderPlayer hp;
    private final Location loc;
    private final Material old;
    
    public SolidPlayer(BHPlayer player, HiderPlayer hp, Location loc, Material old) {
        this.player = player;
        this.hp = hp;
        this.loc = loc;
        this.old = old;
    }
    
    public BHPlayer getPlayer() {
        return this.player;
    }
    
    public HiderPlayer getHiderPlayer() {
        return this.hp;
    }
    
    public Location getLocation() {
        return this.loc;
    }

    public Material getOldMaterial() {
        return this.old;
    }
    
}
