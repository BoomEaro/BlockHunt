package ru.boomearo.blockhunt.menu.icons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.nuop.sf.MenuMaker.inventory.control.ControlHandler;
import com.gmail.nuop.sf.MenuMaker.inventory.control.HandlerHeader;

import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.menu.sessions.PlayerSession;
import ru.boomearo.langhelper.LangHelper;
import ru.boomearo.langhelper.versions.LangType;

public class BlockIcon implements ControlHandler {

    private final Material mat;
    
    public BlockIcon(Material mat) {
        this.mat = mat;
    }
    
    @Override
    public void click(HandlerHeader handler) {
        PlayerSession ps = (PlayerSession) handler.getSession();
        
        Player pl = handler.getPlayerClicked();
        
        pl.playSound(pl.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 15, 1f);
        
        ps.getBHPlayer().setChoosenBlock(this.mat);
        ps.getPlayer().sendMessage(BlockHuntManager.prefix + "Вы выбрали блок " + BlockHuntManager.variableColor + LangHelper.getInstance().getItemTranslate(new ItemStack(this.mat, 1), LangType.RU));
        
        handler.getData().close();
    }

}
