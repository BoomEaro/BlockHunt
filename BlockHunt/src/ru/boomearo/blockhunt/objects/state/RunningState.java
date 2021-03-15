package ru.boomearo.blockhunt.objects.state;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;

import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.managers.BlockHuntStatistics;
import ru.boomearo.blockhunt.objects.BHArena;
import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.blockhunt.objects.playertype.LosePlayer;
import ru.boomearo.blockhunt.objects.playertype.PlayingPlayer;
import ru.boomearo.blockhunt.objects.statistics.BHStatsType;
import ru.boomearo.gamecontrol.GameControl;
import ru.boomearo.gamecontrol.objects.states.ICountable;
import ru.boomearo.gamecontrol.objects.states.IRunningState;
import ru.boomearo.gamecontrol.utils.DateUtil;
import ru.boomearo.gamecontrol.utils.Vault;

public class RunningState implements IRunningState, ICountable, SpectatorFirst {

    private final BHArena arena;
    private int count;
    private int deathPlayers = 0;

    private int cd = 20;
    
    private final Map<String, BlockOwner> removedBlocks = new HashMap<String, BlockOwner>();
    
    public RunningState(BHArena arena, int count) {
        this.arena = arena;
        this.count = count;
    }
    
    @Override
    public String getName() {
        return "§aИдет игра";
    }

    @Override
    public BHArena getArena() {
        return this.arena;
    }
    
    @Override
    public void initState() {
        //Подготавливаем всех игроков (например тп на точку возрождения)
        for (BHPlayer tp : this.arena.getAllPlayers()) {
            tp.getPlayerType().preparePlayer(tp);
        }
        
        this.arena.sendMessages(BlockHuntManager.prefix + "Игра началась. Удачи!");
        this.arena.sendSounds(Sound.BLOCK_NOTE_BLOCK_PLING, 999, 2);
    }
    
    @Override
    public void autoUpdateHandler() {
        //Играть одним низя
        if (this.arena.getAllPlayersType(PlayingPlayer.class).size() <= 1) {
            this.arena.sendMessages(BlockHuntManager.prefix + "Не достаточно игроков для игры! Игра прервана.");
            this.arena.setState(new EndingState(this.arena));
            return;
        }
        
        for (BHPlayer tp : this.arena.getAllPlayers()) {
            tp.getPlayer().spigot().respawn();
            
            if (!this.arena.getArenaRegion().isInRegion(tp.getPlayer().getLocation())) {
                if (tp.getPlayerType() instanceof PlayingPlayer) {
                    PlayingPlayer pp = (PlayingPlayer) tp.getPlayerType();
                    tp.setPlayerType(new LosePlayer());
                    
                    this.deathPlayers++;
                    
                    //Добавляем единицу в статистику поражений
                    BlockHuntStatistics trs = BlockHunt.getInstance().getBlockHuntManager().getStatisticManager();
                    trs.addStats(BHStatsType.Defeat, tp.getName());
                    
                    this.arena.sendSounds(Sound.ENTITY_WITHER_HURT, 999, 2);
                    
                    if (pp.getKiller() != null) {
                        if (tp.getName().equals(pp.getKiller())) {
                            this.arena.sendMessages(BlockHuntManager.prefix + "Игрок §b" + tp.getName() + " §7проиграл, свалившись в свою же яму! " + BlockHuntManager.getRemainPlayersArena(this.arena));
                        }
                        else {
                            this.arena.sendMessages(BlockHuntManager.prefix + "Игрок §b" + tp.getName() + " §7проиграл, свалившись в яму игрока §b" + pp.getKiller() + " " + BlockHuntManager.getRemainPlayersArena(this.arena));
                        }
                    }
                    else {
                        this.arena.sendMessages(BlockHuntManager.prefix + "Игрок §b" + tp.getName() + " §7проиграл, зайдя за границы игры. " + BlockHuntManager.getRemainPlayersArena(this.arena));
                    }
                    
                    Collection<BHPlayer> win = this.arena.getAllPlayersType(PlayingPlayer.class);
                    if (win.size() == 1) {
                        BHPlayer winner = null;
                        for (BHPlayer w : win) {
                            winner = w;
                            break;
                        }
                        if (winner != null) {
                            winner.setPlayerType(new LosePlayer());
                            this.arena.sendMessages(BlockHuntManager.prefix + "Игрок §b" + winner.getName() + " §7победил!");
                            
                            this.arena.sendSounds(Sound.ENTITY_PLAYER_LEVELUP, 999, 2);
                            
                            //Добавляем единицу в статистику побед
                            trs.addStats(BHStatsType.Wins, winner.getName());
                            
                            //В зависимости от того сколько игроков ПРОИГРАЛО мы получим награду.
                            double reward = BlockHuntManager.winReward + (this.deathPlayers * BlockHuntManager.winReward);
                            
                            Vault.addMoney(winner.getName(), reward);
                            
                            winner.getPlayer().sendMessage(BlockHuntManager.prefix + "Ваша награда за победу: " + GameControl.getFormatedEco(reward));
                            
                            this.arena.setState(new EndingState(this.arena));
                            return;
                        }
                    }
                }
                
                tp.getPlayerType().preparePlayer(tp);
            }
            

            if (tp.getPlayerType() instanceof PlayingPlayer) {
                PlayingPlayer pp = (PlayingPlayer) tp.getPlayerType();
                
                BlockOwner bo = this.removedBlocks.get(convertLocToString(tp.getPlayer().getLocation()));
                if (bo != null) {
                    pp.setKiller(bo.getName());
                }
            }
        }
        
        
        handleCount(this.arena);
    }
    
    @Override
    public int getCount() {
        return this.count;
    }
    
    @Override
    public void setCount(int count) {
        this.count = count;
    }
    
    private void handleCount(BHArena arena) {
        if (this.cd <= 0) {
            this.cd = 20;
            
            if (this.count <= 0) {
                arena.sendMessages(BlockHuntManager.prefix + "Время вышло! §bНичья!");
                arena.setState(new EndingState(this.arena));
                return;
            }
            
            arena.sendLevels(this.count);
            
            if (this.count <= 10) {
                arena.sendMessages(BlockHuntManager.prefix + "Игра закончится через §b" + DateUtil.formatedTime(this.count, false));
            }
            else {
                if ((this.count % 30) == 0){
                    arena.sendMessages(BlockHuntManager.prefix + "Игра закончится через §b" + DateUtil.formatedTime(this.count, false));
                }
            }
            
            this.count--;
            
            return;
            
        }
        this.cd--;
    }

    public BlockOwner getBlockByLocation(Location loc) {
        return this.removedBlocks.get(convertLocToString(loc));
    }
    
    public void addBlock(Block block, String owner) {
        this.removedBlocks.put(convertLocToString(block.getLocation()), new BlockOwner(block.getType(), owner));
    }
    
    public static String convertLocToString(Location loc) {
        return loc.getBlockX() + "|" + loc.getBlockY() + "|" +  loc.getBlockZ();
    }

    public static class BlockOwner {
        private final Material mat;
        private final String owner;
        
        public BlockOwner(Material mat, String owner) {
            this.mat = mat;
            this.owner = owner;
        }
        
        public Material getMaterial() {
            return this.mat;
        }
        
        public String getName() {
            return this.owner;
        }
    }
}
