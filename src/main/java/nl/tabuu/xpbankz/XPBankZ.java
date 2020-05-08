package nl.tabuu.xpbankz;

import nl.tabuu.tabuucore.configuration.IConfiguration;
import nl.tabuu.tabuucore.plugin.TabuuCorePlugin;
import nl.tabuu.tabuucore.util.Dictionary;
import nl.tabuu.xpbankz.bank.Bank;
import nl.tabuu.xpbankz.command.XPBankCommand;

public class XPBankZ extends TabuuCorePlugin {
    private static XPBankZ INSTANCE;

    private Bank _bank;
    private IConfiguration _data;
    private Dictionary _local;

    @Override
    public void onEnable() {
        INSTANCE = this;

        _data = getConfigurationManager().addConfiguration("data");
        _local = getConfigurationManager().addConfiguration("lang").getDictionary("");

        _bank = new Bank();
        getBank().load(_data);

        getCommand("xpbank").setExecutor(new XPBankCommand());

        getInstance().getLogger().info("XPBankZ is now enabled.");
    }

    @Override
    public void onDisable() {
        _bank.save(_data);
        _data.save();
        getInstance().getLogger().info("XPBankZ is now disabled.");
    }

    public Dictionary getLocal() {
        return _local;
    }

    public Bank getBank() {
        return _bank;
    }

    public static XPBankZ getInstance() {
        return INSTANCE;
    }
}
