package ru.boomearo.blockhunt.menu.sessions;

import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.menuinv.api.session.InventorySession;

public class BHPlayerSession extends InventorySession {

    private final BHPlayer bhPlayer;

    public BHPlayerSession(BHPlayer bhPlayer) {
        this.bhPlayer = bhPlayer;
    }

    public BHPlayer getBHPlayer() {
        return this.bhPlayer;
    }

}
