package ru.boomearo.blockhunt.objects.state;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.libraryaddict.disguise.DisguiseAPI;
import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.objects.BHArena;
import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.blockhunt.objects.playertype.HiderPlayer;
import ru.boomearo.blockhunt.objects.playertype.IPlayerType;
import ru.boomearo.blockhunt.objects.playertype.SeekerPlayer;
import ru.boomearo.blockhunt.objects.playertype.SeekerPlayer.SeekerRespawn;
import ru.boomearo.blockhunt.objects.playertype.WaitingPlayer;
import ru.boomearo.blockhunt.utils.RandomUtil;
import ru.boomearo.gamecontrol.objects.states.ICountable;
import ru.boomearo.gamecontrol.objects.states.IRunningState;
import ru.boomearo.gamecontrol.utils.DateUtil;

public class RunningState implements IRunningState, ICountable {

    private final BHArena arena;
    private int count;

    private int cd = 20;
    
    public static final int seekerSpawnTime = 20;
    public static final int hiderSwordTime = 30;
    
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
        
        choseSeeker(false);
        
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
        
        //Игра сразу закончится если вдруг хайдеров не окажется
        if (this.arena.getAllPlayersType(HiderPlayer.class).size() <= 0) {
            this.arena.sendMessages(BlockHuntManager.prefix + "Последний хайдер покинул игру. Игра прервана.");
            this.arena.setState(new EndingState(this.arena));
            return;
        }
        
        //Если обнаружится что отсутствуют сикеры, мы сделаем нового из текущих людей
        if (this.arena.getAllPlayersType(SeekerPlayer.class).size() <= 0) {
            this.arena.sendMessages(BlockHuntManager.prefix + "Последний сикер покинул игру, выбираем случайного нового сикера..");
            choseSeeker(true);
        }
        
        for (BHPlayer tp : this.arena.getAllPlayers()) {
            tp.getPlayer().spigot().respawn();
            
            IPlayerType type = tp.getPlayerType();
            if (type instanceof SeekerPlayer) {
                SeekerPlayer sp = (SeekerPlayer) type;
                SeekerRespawn respawn = sp.getSeekerRespawn();
                if (respawn != null) {
                    respawn.autoHandle(tp);
                }
            }
            else if (type instanceof HiderPlayer) {
                HiderPlayer hp = (HiderPlayer) type;
                hp.autoHandle(tp);
            }
            
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
    
    public void choseSeeker(boolean onlyHiders) {
        List<BHPlayer> players = new ArrayList<BHPlayer>((onlyHiders ? this.arena.getAllPlayersType(HiderPlayer.class) : this.arena.getAllPlayers()));
        
        //Выбираем случайно одного сикера
        BHPlayer seeker = players.get(RandomUtil.getRandomNumberRange(0, (players.size() - 1)));
        
        SeekerPlayer sp = new SeekerPlayer();
        
        seeker.setPlayerType(sp);
        
        sp.setSeekerRespawn(new SeekerRespawn(sp));
        
        seeker.getPlayerType().preparePlayer(seeker);
        
        seeker.getPlayer().sendMessage("Вы сикер!");
        
        this.arena.sendMessages("Игрок " + seeker.getName() + " выбран сикером!", seeker.getName());
        
        if (!onlyHiders) {
            this.arena.sendMessages(BlockHuntManager.prefix + "Через " + seekerSpawnTime + " секунд сикер начнет вас искать!");
        }
    }
    
    public void handleDeath(BHPlayer tp) {
        IPlayerType type = tp.getPlayerType();
        if (type instanceof HiderPlayer) {
            HiderPlayer hp = (HiderPlayer) type;
            
            //Если был твердым то убираем твердость
            tp.getArena().unmakeSolid(tp, hp);
            
            Player pl = tp.getPlayer();
            
            //Если была маскировка то сносим ее
            if (DisguiseAPI.isDisguised(pl)) {
                DisguiseAPI.undisguiseToAll(pl);
            }
            
            SeekerPlayer sp = new SeekerPlayer();
            
            tp.setPlayerType(sp);
            
            //Если умирает хайдер и это оказывается последний
            if (this.arena.getAllPlayersType(HiderPlayer.class).size() <= 0) {
                this.arena.sendMessages("Последний хайдер " + tp.getName() + " мертв! Сикеры победили!");
                
                this.arena.setState(new EndingState(this.arena));
            }
            else {
                sp.setSeekerRespawn(new SeekerRespawn(sp));
                
                tp.getPlayer().sendMessage("Вы погибли! Теперь вы сикер!");
                
                this.arena.sendMessages("Хайдер " + tp.getName() + " мертв!", tp.getName());
            }
        }
        else if (type instanceof SeekerPlayer) {
            SeekerPlayer sp = (SeekerPlayer) type;
            sp.setSeekerRespawn(new SeekerRespawn(sp));
            
            tp.getPlayer().sendMessage("Вы были убиты!");
            
            
            this.arena.sendMessages("Сикер " + tp.getName() + " мертв!", tp.getName());
        }
        tp.getPlayerType().preparePlayer(tp);
    }
}
