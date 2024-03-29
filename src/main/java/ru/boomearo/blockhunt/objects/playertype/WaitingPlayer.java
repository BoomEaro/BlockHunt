package ru.boomearo.blockhunt.objects.playertype;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import ru.boomearo.blockhunt.board.BHPLLobby;
import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.blockhunt.objects.ItemButton;
import ru.boomearo.board.Board;
import ru.boomearo.serverutils.utils.other.ExpFix;

public class WaitingPlayer implements IPlayerType {

    @Override
    public void preparePlayer(BHPlayer player) {
        Player pl = player.getPlayer();

        for (PotionEffect effect : pl.getActivePotionEffects()) {
            pl.removePotionEffect(effect.getType());
        }

        pl.setFoodLevel(20);

        pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);

        pl.setHealth(pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

        pl.setGameMode(GameMode.ADVENTURE);
        pl.setFlying(false);
        pl.setAllowFlight(false);

        pl.closeInventory();

        ExpFix.setTotalExperience(player.getPlayer(), 0);

        PlayerInventory inv = pl.getInventory();
        inv.clear();

        ItemButton leave = ItemButton.Leave;
        inv.setItem(leave.getSlot(), leave.getItem());


        if (pl.hasPermission("blockhunt.blockchoose")) {
            ItemButton chos = ItemButton.BlockChoose;
            inv.setItem(chos.getSlot(), chos.getItem());
        }

        inv.setHeldItemSlot(0);

        Board.getInstance().getBoardManager().sendBoardToPlayer(player.getName(), (playerBoard -> new BHPLLobby(playerBoard, player)));

        Location loc = player.getArena().getLobbyLocation();
        if (loc != null) {
            pl.teleport(loc);
        }
    }

}
