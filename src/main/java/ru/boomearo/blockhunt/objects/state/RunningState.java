package ru.boomearo.blockhunt.objects.state;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.disguise.DisguiseAPI;

import ru.boomearo.adveco.AdvEco;
import ru.boomearo.adveco.exceptions.EcoException;
import ru.boomearo.adveco.managers.EcoManager;
import ru.boomearo.adveco.objects.EcoType;
import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.objects.BHArena;
import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.blockhunt.objects.SolidPlayer;
import ru.boomearo.blockhunt.objects.playertype.HiderPlayer;
import ru.boomearo.blockhunt.objects.playertype.IPlayerType;
import ru.boomearo.blockhunt.objects.playertype.SeekerPlayer;
import ru.boomearo.blockhunt.objects.playertype.SeekerPlayer.SeekerRespawn;
import ru.boomearo.blockhunt.objects.playertype.WaitingPlayer;
import ru.boomearo.blockhunt.objects.BHStatsType;

import ru.boomearo.gamecontrol.objects.states.game.ICountable;
import ru.boomearo.gamecontrol.objects.states.game.IRunningState;
import ru.boomearo.gamecontrol.objects.statistics.DefaultStatsManager;
import ru.boomearo.langhelper.LangHelper;
import ru.boomearo.langhelper.versions.LangType;
import ru.boomearo.serverutils.utils.other.DateUtil;
import ru.boomearo.serverutils.utils.other.RandomUtils;

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
            Material chos = tp.getChosenBlock();
            if (chos == null) {
                chos = this.arena.getRandomHideBlock();
            }

            tp.setChosenBlock(null);

            HiderPlayer hp = new HiderPlayer();
            hp.setHideBlock(chos);

            tp.setPlayerType(hp);
        }

        for (BHPlayer tp : this.arena.getAllPlayers()) {
            if (tp.getPlayerType() instanceof HiderPlayer hp) {
                hp.preparePlayer(tp);
                tp.getPlayer().sendMessage(BlockHuntManager.prefix + "Вы были замаскированы под " + BlockHuntManager.variableColor + LangHelper.getInstance().getItemTranslate(new ItemStack(hp.getHideBlock(), 1), LangType.RU_RU));
            }
        }
    }

    @Override
    public void autoUpdateHandler() {
        //Играть одним низя
        if (this.arena.getAllPlayers().size() <= 1) {
            this.arena.sendMessages(BlockHuntManager.prefix + "Не достаточно игроков для игры! " + BlockHuntManager.otherColor + "Игра прервана.");
            this.arena.setState(new EndingState(this.arena));
            return;
        }

        //Игра сразу закончится если вдруг хайдеров не окажется
        if (this.arena.getAllPlayersType(HiderPlayer.class).size() <= 0) {
            this.arena.sendMessages(BlockHuntManager.prefix + "Последний §3Хайдер §bпокинул игру. " + BlockHuntManager.otherColor + "Игра прервана.");
            this.arena.setState(new EndingState(this.arena));
            return;
        }

        //Если обнаружится что отсутствуют сикеры, мы сделаем нового из текущих людей
        if (this.arena.getAllPlayersType(SeekerPlayer.class).size() <= 0) {
            this.arena.sendMessages(BlockHuntManager.prefix + "§cПоследний Охотник покинул игру, выбираем случайного нового Охотника..");
            choseSeeker(true);
        }

        for (BHPlayer tp : this.arena.getAllPlayers()) {
            tp.getPlayer().spigot().respawn();

            IPlayerType type = tp.getPlayerType();
            if (type instanceof SeekerPlayer sp) {
                SeekerRespawn respawn = sp.getSeekerRespawn();
                if (respawn != null) {
                    respawn.autoHandle(tp);
                }
            }
            else if (type instanceof HiderPlayer hp) {
                hp.autoHandle(tp);
            }

            if (!this.arena.getArenaRegion().isInRegionPoint(tp.getPlayer().getLocation())) {

                handleDeath(tp, null);
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
                arena.sendMessages(BlockHuntManager.prefix + "Время вышло! " + BlockHuntManager.hiderColor + "Хайдеры победили!");

                arena.sendSounds(Sound.ENTITY_PLAYER_LEVELUP, 999, 2);

                arena.sendTitle(BlockHuntManager.hiderColor + "Хайдеры победили!", "", 20, 20 * 15, 20);

                //Награждаем всех хайдеров
                DefaultStatsManager bhs = BlockHunt.getInstance().getBlockHuntManager().getStatisticManager();
                for (BHPlayer bh : this.arena.getAllPlayersType(HiderPlayer.class)) {
                    bhs.addStatsToPlayer(BHStatsType.HidersWin, bh.getName());

                    try {
                        AdvEco.getInstance().getEcoManager().addPlayerEco(bh.getName(), bh.getPlayer(), EcoType.Credit, BlockHuntManager.hiderWinReward);
                    }
                    catch (EcoException e) {
                        e.printStackTrace();
                    }

                    bh.getPlayer().sendMessage(BlockHuntManager.prefix + "Вы получили награду за победу: " + EcoManager.getFormatedEco(BlockHuntManager.hiderWinReward, EcoType.Credit));
                }

                arena.setState(new EndingState(this.arena));
                return;
            }

            arena.sendLevels(this.count);

            if (this.count <= 10) {
                arena.sendMessages(BlockHuntManager.prefix + "Игра закончится через " + BlockHuntManager.variableColor + DateUtil.formatedTime(this.count, false));
            }
            else {
                if ((this.count % 30) == 0) {
                    arena.sendMessages(BlockHuntManager.prefix + "Игра закончится через " + BlockHuntManager.variableColor + DateUtil.formatedTime(this.count, false));
                }
            }

            updateFakeSolidsBlocks();

            this.count--;

            return;

        }
        this.cd--;
    }

    private void updateFakeSolidsBlocks() {
        for (SolidPlayer sp : this.arena.getAllSolidPlayers()) {
            Material mat = sp.getHiderPlayer().getHideBlock();
            if (mat != null) {

                for (BHPlayer pla : this.arena.getAllPlayers()) {
                    if (sp.getPlayer().getName().equals(pla.getName())) {
                        continue;
                    }
                    pla.getPlayer().sendBlockChange(sp.getLocation(), Bukkit.createBlockData(mat));
                }
            }
        }
    }

    public void choseSeeker(boolean onlyHiders) {
        List<BHPlayer> players = new ArrayList<>((onlyHiders ? this.arena.getAllPlayersType(HiderPlayer.class) : this.arena.getAllPlayers()));

        //Выбираем случайно одного сикера
        BHPlayer seeker = players.get(RandomUtils.getRandomNumberRange(0, (players.size() - 1)));

        //При выборе если оказалоось что выбрали хайдера, то сбрасываем все обличии
        IPlayerType type = seeker.getPlayerType();
        if (type instanceof HiderPlayer hp) {

            //Если был твердым то убираем твердость
            seeker.getArena().unmakeSolid(seeker, hp);

            Player pl = seeker.getPlayer();

            //Если была маскировка то сносим ее
            if (DisguiseAPI.isDisguised(pl)) {
                DisguiseAPI.undisguiseToAll(pl);
            }
        }

        SeekerPlayer sp = new SeekerPlayer();

        seeker.setPlayerType(sp);

        sp.setSeekerRespawn(new SeekerRespawn(sp));

        seeker.getPlayerType().preparePlayer(seeker);

        seeker.getPlayer().sendMessage(BlockHuntManager.prefix + "Вас выбрали " + BlockHuntManager.seekerColor + "Охотником " + BlockHuntManager.mainColor + "!");

        this.arena.sendMessages(BlockHuntManager.prefix + seeker.getPlayer().getDisplayName() + BlockHuntManager.mainColor + " выбран " + BlockHuntManager.seekerColor + "Охотником" + BlockHuntManager.mainColor + "!", seeker.getName());

        if (!onlyHiders) {
            this.arena.sendMessages(BlockHuntManager.prefix + "Через " + BlockHuntManager.seekerColor + seekerSpawnTime + BlockHuntManager.mainColor + " секунд " + BlockHuntManager.seekerColor + "Охотник начнет вас искать" + BlockHuntManager.mainColor + "!");
        }
    }

    public void handleDeath(BHPlayer player, BHPlayer killer) {
        DefaultStatsManager bhs = BlockHunt.getInstance().getBlockHuntManager().getStatisticManager();

        IPlayerType type = player.getPlayerType();
        if (type instanceof HiderPlayer hp) {

            if (killer != null) {
                bhs.addStatsToPlayer(BHStatsType.HidersKills, killer.getName());

                try {
                    AdvEco.getInstance().getEcoManager().addPlayerEco(killer.getName(), killer.getPlayer(), EcoType.Credit, BlockHuntManager.hiderKillReward);
                }
                catch (EcoException e) {
                    e.printStackTrace();
                }

                killer.getPlayer().sendMessage(BlockHuntManager.prefix + "Вы получили " + EcoManager.getFormatedEco(BlockHuntManager.hiderKillReward, EcoType.Credit) + BlockHuntManager.mainColor + " за убийство " + BlockHuntManager.hiderColor + "Хайдера");
            }

            //Если был твердым то убираем твердость
            player.getArena().unmakeSolid(player, hp);

            Player pl = player.getPlayer();

            //Если была маскировка то сносим ее
            if (DisguiseAPI.isDisguised(pl)) {
                DisguiseAPI.undisguiseToAll(pl);
            }

            SeekerPlayer sp = new SeekerPlayer();

            player.setPlayerType(sp);

            //Если умирает хайдер и это оказывается последний
            if (this.arena.getAllPlayersType(HiderPlayer.class).size() <= 0) {
                this.arena.sendMessages(BlockHuntManager.prefix + "Последний " + BlockHuntManager.hiderColor + "Хайдер " + player.getPlayer().getDisplayName() + BlockHuntManager.mainColor + " мертв! " + BlockHuntManager.seekerColor + "Охотники победили!");

                this.arena.sendTitle(BlockHuntManager.seekerColor + "Охотники победили!", "", 20, 20 * 15, 20);

                this.arena.sendSounds(Sound.ENTITY_PLAYER_LEVELUP, 999, 2);

                //Награждаем всех сикеров
                for (BHPlayer bh : this.arena.getAllPlayersType(SeekerPlayer.class)) {
                    //Проигравший хайдер становится сикером и в этот момент всем сикерам выдается награда.
                    //Так как игрок просто не успел поиграть за сикера, не даем ему никакой награды.
                    //TODO Справедливо ли это?
                    if (bh == player) {
                        continue;
                    }
                    bhs.addStatsToPlayer(BHStatsType.SeekersWin, bh.getName());

                    //Vault.addMoney(bh.getName(), BlockHuntManager.hiderWinReward);
                    //bh.getPlayer().sendMessage(BlockHuntManager.prefix + "Вы получили награду за победу: " + GameControl.getFormatedEco(BlockHuntManager.hiderWinReward));
                    //TODO награждать жетонами сикеров?
                }

                this.arena.setState(new EndingState(this.arena));
            }
            else {
                sp.setSeekerRespawn(new SeekerRespawn(sp));

                player.getPlayer().sendMessage(BlockHuntManager.prefix + BlockHuntManager.seekerColor + "Вы были убиты! " + BlockHuntManager.mainColor + "Теперь вы стали " + BlockHuntManager.seekerColor + "Охотником" + BlockHuntManager.mainColor + "!");

                this.arena.sendMessages(BlockHuntManager.prefix + BlockHuntManager.hiderColor + "Хайдер " + player.getPlayer().getDisplayName() + BlockHuntManager.mainColor + " мертв! Осталось " + BlockHuntManager.hiderColor + this.arena.getAllPlayersType(HiderPlayer.class).size() + " Хайдеров", player.getName());
            }
        }
        else if (type instanceof SeekerPlayer sp) {
            if (killer != null) {
                bhs.addStatsToPlayer(BHStatsType.SeekersKills, killer.getName());
            }

            sp.setSeekerRespawn(new SeekerRespawn(sp));

            player.getPlayer().sendMessage(BlockHuntManager.prefix + "Вы были убиты!");


            this.arena.sendMessages(BlockHuntManager.prefix + BlockHuntManager.seekerColor + "Охотник " + player.getPlayer().getDisplayName() + BlockHuntManager.mainColor + " мертв!", player.getName());
        }

        this.arena.sendSounds(Sound.ENTITY_WITHER_HURT, 999, 2);

        player.getPlayerType().preparePlayer(player);
    }
}
