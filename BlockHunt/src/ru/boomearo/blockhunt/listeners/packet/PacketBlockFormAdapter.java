package ru.boomearo.blockhunt.listeners.packet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;

import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.blockhunt.objects.state.RunningState;
import ru.boomearo.blockhunt.objects.state.RunningState.SolidBlock;
import ru.boomearo.gamecontrol.objects.states.IGameState;

public class PacketBlockFormAdapter extends PacketAdapter {

    public PacketBlockFormAdapter() {
        super(BlockHunt.getInstance(), ListenerPriority.LOWEST, PacketType.Play.Server.BLOCK_CHANGE);
    }
    

    @Override
    public void onPacketSending(PacketEvent e) {
        Player pl = e.getPlayer();
        if (pl == null) {
            return;
        }
        
        BHPlayer bh = BlockHunt.getInstance().getBlockHuntManager().getGamePlayer(pl.getName());
        if (bh == null) {
            return;
        }
        
        IGameState state = bh.getArena().getState();
        if (!(state instanceof RunningState)) {
            return;
        }
        
        RunningState rs = (RunningState) state;

        PacketContainer pc = e.getPacket();
        //Material mat = pc.getBlockData().readSafely(0).getType();
        BlockPosition bp = e.getPacket().getBlockPositionModifier().readSafely(0);
        Location loc = bp.toLocation(pl.getWorld());
        
        SolidBlock sb = rs.getSolidBlockByLocation(loc);
        if (sb == null) {
            return;
        }
        
        if (sb.getPlayer().getName().equals(pl.getName())) {
            return;
        }
        
        pc.getBlockData().writeSafely(0, WrappedBlockData.createData(Material.STONE));
        //BlockHunt.getInstance().getLogger().info("test " + pl.getName() + " " + loc + " " + mat);
    }
}
