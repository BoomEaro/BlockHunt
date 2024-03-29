package ru.boomearo.blockhunt.objects.playertype;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ru.boomearo.blockhunt.board.BHPLGame;
import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.objects.BHArena;
import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.blockhunt.objects.ItemButton;
import ru.boomearo.blockhunt.objects.state.RunningState;
import ru.boomearo.board.Board;
import ru.boomearo.serverutils.utils.other.DateUtil;
import ru.boomearo.serverutils.utils.other.ExpFix;

public class SeekerPlayer implements IPlayerType {

    private SeekerRespawn respawn = null;

    @Override
    public void preparePlayer(BHPlayer player) {
        Player pl = player.getPlayer();

        for (PotionEffect effect : pl.getActivePotionEffects()) {
            pl.removePotionEffect(effect.getType());
        }

        //Ослепляем сикера
        pl.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20*1200, 1));

        pl.setFoodLevel(20);

        pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);

        pl.setHealth(pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

        pl.setGameMode(GameMode.SURVIVAL);
        pl.setFlying(false);
        pl.setAllowFlight(false);

        pl.closeInventory();

        ExpFix.setTotalExperience(player.getPlayer(), 0);

        PlayerInventory inv = pl.getInventory();
        inv.clear();

        ItemButton leave = ItemButton.Leave;
        inv.setItem(leave.getSlot(), leave.getItem());

        inv.setItem(EquipmentSlot.HEAD, new ItemStack(Material.IRON_HELMET, 1));
        inv.setItem(EquipmentSlot.CHEST, new ItemStack(Material.IRON_CHESTPLATE, 1));
        inv.setItem(EquipmentSlot.LEGS, new ItemStack(Material.IRON_LEGGINGS, 1));
        inv.setItem(EquipmentSlot.FEET, new ItemStack(Material.IRON_BOOTS, 1));

        ItemButton sword = ItemButton.SeekerSword;
        inv.setItem(sword.getSlot(), sword.getItem());

        inv.setHeldItemSlot(0);

        Board.getInstance().getBoardManager().sendBoardToPlayer(player.getName(), (playerBoard -> new BHPLGame(playerBoard, player)));

        Location loc = player.getArena().getSeekersLocation();
        if (loc != null) {
            pl.teleport(loc);
        }
    }

    public SeekerRespawn getSeekerRespawn() {
        return this.respawn;
    }

    public void setSeekerRespawn(SeekerRespawn respawn) {
        this.respawn = respawn;
    }

    public static class SeekerRespawn {

        //сколько ждать секунд перед телепортацией
        private int count = RunningState.seekerSpawnTime;

        private int cd = 20;

        private final SeekerPlayer sp;

        public SeekerRespawn(SeekerPlayer sp) {
            this.sp = sp;
        }

        public void autoHandle(BHPlayer player) {
            if (this.cd <= 0) {
                this.cd = 20;

                Player pl = player.getPlayer();

                if (this.count <= 0) {
                    this.sp.setSeekerRespawn(null);

                    //Очищаем все эффекты
                    for (PotionEffect effect : pl.getActivePotionEffects()) {
                        pl.removePotionEffect(effect.getType());
                    }

                    //Делаем сикера быстрее
                    pl.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20*1200, 1));

                    BHArena arena = player.getArena();
                    Location loc = player.getArena().getHidersLocation();
                    if (loc != null) {
                        pl.teleport(loc);
                    }
                    pl.sendMessage(BlockHuntManager.prefix + "Вы были заспавнены!");
                    arena.sendMessages(BlockHuntManager.prefix + BlockHuntManager.seekerColor + "Охотник " + player.getName() + BlockHuntManager.mainColor + " был заспавнен!", player.getName());

                    arena.sendTitle("", BlockHuntManager.seekerColor + "Охотник " + player.getPlayer().getDisplayName() + BlockHuntManager.mainColor + " был заспавнен!", 20, 40, 20);
                    return;
                }

                if (this.count <= 5) {
                    pl.sendMessage(BlockHuntManager.prefix + "Вы будете заспавнены через " + BlockHuntManager.variableColor + DateUtil.formatedTime(this.count, false));
                }
                else {
                    if ((this.count % 5) == 0) {
                        pl.sendMessage(BlockHuntManager.prefix + "Вы будете заспавнены через " + BlockHuntManager.variableColor + DateUtil.formatedTime(this.count, false));
                    }
                }

                this.count--;
            }
            this.cd--;
        }
    }

}