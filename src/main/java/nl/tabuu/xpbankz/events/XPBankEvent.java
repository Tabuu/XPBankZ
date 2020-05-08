package nl.tabuu.xpbankz.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class XPBankEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean _canceled;
    private long _balance;
    private OfflinePlayer _accountHolder;

    public XPBankEvent(OfflinePlayer accountHolder, long balance) {
        _balance = balance;
        _accountHolder = accountHolder;
    }

    public long getBalance() {
        return _balance;
    }

    public OfflinePlayer getAccountHolder() {
        return _accountHolder;
    }

    @Override
    public boolean isCancelled() {
        return _canceled;
    }

    @Override
    public void setCancelled(boolean canceled) {
        _canceled = canceled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
