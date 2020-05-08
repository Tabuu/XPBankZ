package nl.tabuu.xpbankz.events;

import org.bukkit.OfflinePlayer;

public class XPBankBalanceChangeEvent extends XPBankEvent {

    private long _oldBalance;

    public XPBankBalanceChangeEvent(OfflinePlayer accountHolder, long oldBalance, long newBalance) {
        super(accountHolder, newBalance);
        _oldBalance = oldBalance;
    }

    public long getOldBalance() {
        return _oldBalance;
    }
}
