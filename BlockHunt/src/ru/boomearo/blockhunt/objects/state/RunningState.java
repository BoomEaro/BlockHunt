package ru.boomearo.blockhunt.objects.state;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Sound;

import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.objects.BHArena;
import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.blockhunt.objects.playertype.HiderPlayer;
import ru.boomearo.blockhunt.objects.playertype.IPlayerType;
import ru.boomearo.blockhunt.objects.playertype.SeekerPlayer;
import ru.boomearo.blockhunt.objects.playertype.WaitingPlayer;
import ru.boomearo.blockhunt.utils.RandomUtil;
import ru.boomearo.gamecontrol.objects.states.ICountable;
import ru.boomearo.gamecontrol.objects.states.IRunningState;
import ru.boomearo.gamecontrol.utils.DateUtil;

public class RunningState implements IRunningState, ICountable {

    private final BHArena arena;
    private int count;

    private int cd = 20;
    
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
        this.arena.sendMessages(BlockHuntManager.prefix + "Игра началась. Удачи!");
        this.arena.sendSounds(Sound.BLOCK_NOTE_BLOCK_PLING, 999, 2);
        
        List<BHPlayer> players = new ArrayList<BHPlayer>(this.arena.getAllPlayers());
        
        //Выбираем случайно одного сикера
        BHPlayer seeker = players.get(RandomUtil.getRandomNumberRange(0, (players.size() - 1)));
        
        seeker.setPlayerType(new SeekerPlayer());
        
        seeker.getPlayerType().preparePlayer(seeker);
        
        seeker.getPlayer().sendMessage("Вы сикер!");
        
        this.arena.sendMessages("Игрок " + seeker.getName() + " выбран сикером!");
        
        
        //Подготавливаем оставшихся игроков, делая их хайдераи
        for (BHPlayer tp : this.arena.getAllPlayersType(WaitingPlayer.class)) {
            
            tp.setPlayerType(new HiderPlayer());
            tp.getPlayerType().preparePlayer(tp);
            
            tp.getPlayer().sendMessage("Вы хайдер!");
        }
    }
    
    @Override
    public void autoUpdateHandler() {
        //Играть одним низя
        if (this.arena.getAllPlayers().size() <= 1) {
            this.arena.sendMessages(BlockHuntManager.prefix + "Не достаточно игроков для игры! Игра прервана.");
            this.arena.setState(new EndingState(this.arena));
            return;
        }
        
        for (BHPlayer tp : this.arena.getAllPlayers()) {
            tp.getPlayer().spigot().respawn();
            
            if (!this.arena.getArenaRegion().isInRegion(tp.getPlayer().getLocation())) {

                handleDeath(tp);
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
                arena.sendMessages(BlockHuntManager.prefix + "Время вышло! Хайдеры победили!");
                //TODO награда всем хайдерам
                arena.setState(new EndingState(this.arena));
                return;
            }
            
            arena.sendLevels(this.count);
            
            if (this.count <= 10) {
                arena.sendMessages(BlockHuntManager.prefix + "Игра закончится через §9" + DateUtil.formatedTime(this.count, false));
            }
            else {
                if ((this.count % 30) == 0){
                    arena.sendMessages(BlockHuntManager.prefix + "Игра закончится через §9" + DateUtil.formatedTime(this.count, false));
                }
            }
            
            this.count--;
            
            return;
            
        }
        this.cd--;
    }
    
    public void handleDeath(BHPlayer tp) {
        IPlayerType type = tp.getPlayerType();
        if (type instanceof HiderPlayer) {
            tp.setPlayerType(new SeekerPlayer());
            tp.getPlayer().sendMessage("Вы погибли! Теперь вы сикер!");
            
            this.arena.sendMessages("Хайдер " + tp.getName() + " мертв!");
        }
        else if (type instanceof SeekerPlayer) {
            tp.getPlayer().sendMessage("Вы зашли за границу мира и умерли!");
        }
        tp.getPlayerType().preparePlayer(tp);
    }
}
