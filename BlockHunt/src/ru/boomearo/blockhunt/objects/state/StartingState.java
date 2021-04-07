package ru.boomearo.blockhunt.objects.state;

import org.bukkit.Sound;

import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.objects.BHArena;
import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.gamecontrol.objects.states.ICountable;
import ru.boomearo.gamecontrol.objects.states.IStartingState;
import ru.boomearo.gamecontrol.utils.DateUtil;

public class StartingState implements IStartingState, ICountable, AllowJoin {

    private final BHArena arena;
    
    private int count = 30;
    
    private int cd = 20;
    
    public StartingState(BHArena arena) {
        this.arena = arena;
    }
    
    @Override
    public String getName() {
        return "§aНачало игры";
    }
    
    @Override
    public BHArena getArena() {
        return this.arena;
    }
    
    @Override
    public void initState() {
        this.arena.sendMessages(BlockHuntManager.prefix + "Начинаем игру!");
    }
    
    @Override
    public void autoUpdateHandler() {
        for (BHPlayer tp : this.arena.getAllPlayers()) {
            tp.getPlayer().spigot().respawn();
            
            if (!this.arena.getLobbyRegion().isInRegionPoint(tp.getPlayer().getLocation())) {
                tp.getPlayerType().preparePlayer(tp);
            }
        }
        
        //Если на арене вообще нет игроков то переходим в ожидание. (малоли)
        if (this.arena.getAllPlayers().size() <= 0) {
            this.arena.setState(new WaitingState(this.arena));
            return;
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
            
            //Если прошло 30 сек
            if (this.count <= 0) {
                
                if (!this.arena.isForceStarted()) {
                    //Если игроков не достаточно для игры, то возвращаемся в ожидание
                    if (this.arena.getAllPlayers().size() < this.arena.getMinPlayers()) {
                        this.arena.sendMessages(BlockHuntManager.prefix + "§cНе достаточно игроков для старта!");
                        this.arena.setState(new WaitingState(this.arena));
                        return;
                    }
                }
                
                
                arena.setState(new RunningState(arena, arena.getTimeLimit()));
                return;
            }
            
            arena.sendLevels(this.count);
            if (this.count <= 5) {
                arena.sendMessages(BlockHuntManager.prefix + "Игра начнется через " + BlockHuntManager.variableColor + DateUtil.formatedTime(this.count, false));
                arena.sendSounds(Sound.BLOCK_NOTE_BLOCK_PLING, 999, 2);
            }
            else {
                if ((this.count % 5) == 0){
                    arena.sendMessages(BlockHuntManager.prefix + "Игра начнется через " + BlockHuntManager.variableColor + DateUtil.formatedTime(this.count, false));
                    arena.sendSounds(Sound.BLOCK_NOTE_BLOCK_PLING, 999, 2);
                }
            }
            
            this.count--;
            
            return;
        }
        
        this.cd--;
    }

    
}
