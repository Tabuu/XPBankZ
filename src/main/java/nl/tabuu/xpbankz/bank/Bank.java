package nl.tabuu.xpbankz.bank;

import nl.tabuu.tabuucore.configuration.IConfiguration;
import nl.tabuu.tabuucore.debug.Debug;
import nl.tabuu.tabuucore.serialization.string.Serializer;
import nl.tabuu.xpbankz.events.XPBankBalanceChangeEvent;
import nl.tabuu.xpbankz.util.ExperienceUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Bank {

    private Map<UUID, Long> _accounts;

    public Bank() {
        _accounts = new HashMap<>();
    }

    public void load(IConfiguration file) {
        _accounts = file.getMap("Accounts", Serializer.UUID, Serializer.LONG);

        if(_accounts == null) _accounts = new HashMap<>();
    }

    public void save(IConfiguration file) {
        file.setMap("Accounts", _accounts, Serializer.UUID, Serializer.LONG);
    }

    public long getBalance(OfflinePlayer player) {
        return _accounts.getOrDefault(player.getUniqueId(), 0L);
    }

    public long getMaxBalance(OfflinePlayer player) {
        // TODO: configurable max.
        return Long.MAX_VALUE;
    }

    public boolean canSet(OfflinePlayer player, long amount) {
        return amount < getMaxBalance(player) && amount > 0;
    }

    public boolean set(OfflinePlayer player, long amount) {
        if(!canSet(player, amount)) return false;

        XPBankBalanceChangeEvent event = new XPBankBalanceChangeEvent(player, getBalance(player), amount);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) return false;

        _accounts.put(player.getUniqueId(), amount);
        return true;
    }

    public boolean withdraw(OfflinePlayer player, long amount) {
        if(amount <= 0) return false;
        return set(player, getBalance(player) - amount);
    }

    public boolean deposit(OfflinePlayer player, long amount) {
        if(amount <= 0) return false;
        return set(player, getBalance(player) + amount);
    }

    public boolean has(OfflinePlayer player, long amount) {
        return getBalance(player) >= amount;
    }
}
