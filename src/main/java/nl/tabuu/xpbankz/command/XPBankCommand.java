package nl.tabuu.xpbankz.command;

import nl.tabuu.tabuucore.command.Command;
import nl.tabuu.tabuucore.command.CommandResult;
import nl.tabuu.tabuucore.command.SenderType;
import nl.tabuu.tabuucore.command.argument.ArgumentConverter;
import nl.tabuu.tabuucore.command.argument.ArgumentType;
import nl.tabuu.tabuucore.command.argument.converter.OrderedArgumentConverter;
import nl.tabuu.tabuucore.text.ComponentBuilder;
import nl.tabuu.tabuucore.util.Dictionary;
import nl.tabuu.xpbankz.XPBankZ;
import nl.tabuu.xpbankz.bank.Bank;
import nl.tabuu.xpbankz.util.ExperienceUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class XPBankCommand extends Command {

    private Bank _bank;
    private Dictionary _local;

    public XPBankCommand() {
        super("xpbank");

        _bank = XPBankZ.getInstance().getBank();
        _local = XPBankZ.getInstance().getLocal();

        addSubCommand("set", new XPBankSetCommand(this));
        addSubCommand("give", new XPBankGiveCommand(this));
        addSubCommand("take", new XPBankTakeCommand(this));
        addSubCommand("balance", new XPBankBalanceCommand(this));
        addSubCommand("deposit", new XPBankDepositCommand(this));
        addSubCommand("withdraw", new XPBankWithdrawCommand(this));
        addSubCommand("transfer", new XPBankTransferCommand(this));
    }

    @Override
    protected CommandResult onCommand(CommandSender sender, List<Optional<?>> arguments) {
        return ((XPBankBalanceCommand) getSubCommand("balance")).onCommand(sender, arguments);
    }

    class XPBankBalanceCommand extends Command {

        protected XPBankBalanceCommand(Command parent) {
            super("xpbank balance", parent);

            setRequiredSenderType(SenderType.PLAYER);
        }

        @Override
        public CommandResult onCommand(CommandSender sender, List<Optional<?>> arguments) {
            Player player = (Player) sender;

            String message = _local.translate("COMMAND_BALANCE", getReplacements(player));

            player.spigot().sendMessage(ComponentBuilder.parse(message).build());

            return CommandResult.SUCCESS;
        }

        protected Object[] getReplacements(Player player) {
            long balance = _bank.getBalance(player);
            long balanceLevel = ExperienceUtil.pointToLevel((int) balance);
            long balanceLevelYield = balanceLevel - player.getLevel();
            int currentPoints = ExperienceUtil.getPoints(player);

            if(balanceLevelYield < 0) balanceLevelYield = 0;

            return new Object[]{
                    "{BALANCE_POINTS}", balance,
                    "{BALANCE_LEVEL}", balanceLevel,
                    "{BALANCE_LEVEL_YIELD}", balanceLevelYield,
                    "{CURRENT_POINTS}", currentPoints
            };
        }
    }

    class XPBankDepositCommand extends XPBankEditSelfCommand {

        protected XPBankDepositCommand(Command parent) {
            super("xpbank deposit", parent);

            setRequiredSenderType(SenderType.PLAYER);

            ArgumentConverter converter = new OrderedArgumentConverter().setSequence(ArgumentType.LONG);
            setArgumentConverter(converter);
        }

        @Override
        protected BalanceManipulator getBalanceManipulator() {
            return Long::sum;
        }

        @Override
        protected boolean onPreBalanceEdit(CommandSender sender, OfflinePlayer target, long oldBalance, long delta) {
            return ExperienceUtil.getPoints((Player) sender) >= delta;
        }

        @Override
        protected void onPostBalanceEdit(CommandSender sender, OfflinePlayer target, long oldBalance, long delta) {
            Player player = (Player) sender;
            int points = ExperienceUtil.getPoints(player);
            points -= delta;
            ExperienceUtil.setPoints(player, points);
        }

        @Override
        protected String getLocalizedSuccessMessage() {
            return "COMMAND_DEPOSIT";
        }

        @Override
        protected String getLocalizedErrorMessage() {
            return "COMMAND_DEPOSIT_ERROR";
        }
    }

    class XPBankWithdrawCommand extends XPBankEditSelfCommand {

        protected XPBankWithdrawCommand(Command parent) {
            super("xpbank withdraw", parent);
        }

        @Override
        protected BalanceManipulator getBalanceManipulator() {
            return (balance, delta) -> balance - delta;
        }

        @Override
        protected void onPostBalanceEdit(CommandSender sender, OfflinePlayer target, long oldBalance, long delta) {
            Player player = (Player) sender;
            int points = ExperienceUtil.getPoints(player);
            points += delta;
            ExperienceUtil.setPoints(player, points);
        }

        @Override
        protected String getLocalizedSuccessMessage() {
            return "COMMAND_WITHDRAW";
        }

        @Override
        protected String getLocalizedErrorMessage() {
            return "COMMAND_WITHDRAW_ERROR";
        }
    }

    class XPBankTransferCommand extends XPBankGiveCommand {

        protected XPBankTransferCommand(Command parent) {
            super("xpbank transfer", parent);

            setRequiredSenderType(SenderType.PLAYER);
        }

        @Override
        protected boolean onPreBalanceEdit(CommandSender sender, OfflinePlayer target, long oldBalance, long delta) {
            Player player = (Player) sender;
            return _bank.has(player, delta);
        }

        @Override
        protected void onPostBalanceEdit(CommandSender sender, OfflinePlayer target, long oldBalance, long delta) {
            Player player = (Player) sender;
            _bank.withdraw(player, delta);

            if(target.isOnline()) {
                String message = "COMMAND_TRANSFER_RECEIVED";
                sendMessage((Player) target, message, oldBalance, oldBalance + delta, delta, target);
            }
        }

        @Override
        protected String getLocalizedErrorMessage() {
            return "COMMAND_TRANSFER_ERROR";
        }

        @Override
        protected String getLocalizedSuccessMessage() {
            return "COMMAND_TRANSFER";
        }
    }

    class XPBankSetCommand extends XPBankEditOtherCommand {

        protected XPBankSetCommand(String name, Command parent) {
            super(name, parent);
        }

        protected XPBankSetCommand(Command parent) {
            super("xpbank set", parent);
        }

        @Override
        protected BalanceManipulator getBalanceManipulator() {
            return (balance, delta) -> delta;
        }
    }

    class XPBankGiveCommand extends XPBankEditOtherCommand {

        protected XPBankGiveCommand(String name, Command parent) {
            super(name, parent);
        }

        protected XPBankGiveCommand(Command parent) {
            super("xpbank give", parent);
        }

        @Override
        protected BalanceManipulator getBalanceManipulator() {
            return Long::sum;
        }
    }

    class XPBankTakeCommand extends XPBankEditOtherCommand {

        protected XPBankTakeCommand(String name, Command parent) {
            super(name, parent);
        }

        protected XPBankTakeCommand(Command parent) {
            super("xpbank take", parent);
        }

        @Override
        protected BalanceManipulator getBalanceManipulator() {
            return (balance, delta) -> balance - delta;
        }
    }

    abstract class XPBankEditSelfCommand extends XPBankEditCommand {

        protected XPBankEditSelfCommand(String name, Command parent) {
            super(name, parent);

            setRequiredSenderType(SenderType.PLAYER);
            ArgumentConverter converter = new OrderedArgumentConverter().setSequence(ArgumentType.LONG);
            setArgumentConverter(converter);
        }

        @Override
        protected CommandResult onCommand(CommandSender sender, List<Optional<?>> arguments) {
            if (!arguments.get(0).isPresent()) return CommandResult.WRONG_SYNTAX;

            long delta = (Long) arguments.get(0).get();
            OfflinePlayer target = (OfflinePlayer) sender;
            long oldBalance = _bank.getBalance(target);

            onBalanceEdit(sender, target, oldBalance, delta);

            return CommandResult.SUCCESS;
        }

    }

    abstract class XPBankEditOtherCommand extends XPBankEditCommand {

        protected XPBankEditOtherCommand(String name, Command parent) {
            super(name, parent);

            ArgumentConverter converter = new OrderedArgumentConverter().setSequence(ArgumentType.OFFLINE_PLAYER, ArgumentType.LONG);
            setArgumentConverter(converter);
        }

        @Override
        protected CommandResult onCommand(CommandSender sender, List<Optional<?>> arguments) {
            if (!arguments.stream().allMatch(Optional::isPresent)) return CommandResult.WRONG_SYNTAX;

            OfflinePlayer target = (OfflinePlayer) arguments.get(0).get();
            long delta = (Long) arguments.get(1).get();
            long oldBalance = _bank.getBalance(target);

            onBalanceEdit(sender, target, oldBalance, delta);

            return CommandResult.SUCCESS;
        }

    }

    abstract class XPBankEditCommand extends Command {

        protected XPBankEditCommand(String name, Command parent) {
            super(name, parent);

            ArgumentConverter converter = new OrderedArgumentConverter().setSequence(ArgumentType.OFFLINE_PLAYER, ArgumentType.LONG);
            setArgumentConverter(converter);
        }

        protected abstract BalanceManipulator getBalanceManipulator();

        protected boolean onPreBalanceEdit(CommandSender sender, OfflinePlayer target, long oldBalance, long delta) { return true; }

        protected void onPostBalanceEdit(CommandSender sender, OfflinePlayer target, long oldBalance, long delta) { }

        protected boolean onBalanceEdit(CommandSender sender, OfflinePlayer target, long oldBalance, long delta) {
            long newBalance = getBalanceManipulator().manipulate(oldBalance, delta);

            boolean success = onPreBalanceEdit(sender, target, oldBalance, delta) && _bank.set(target, newBalance);

            String message = success ? getLocalizedSuccessMessage() : getLocalizedErrorMessage();
            sendMessage(sender, message, oldBalance, newBalance, delta, target);

            if(success) onPostBalanceEdit(sender, target, oldBalance, delta);

            return success;
        }

        protected void sendMessage(CommandSender receiver, String localizedMessage, long oldBalance, long newBalance, long delta, OfflinePlayer player) {
            Object[] replacements = new Object[] {
                    "{BALANCE_OLD}", oldBalance,
                    "{BALANCE_NEW}", newBalance,
                    "{BALANCE_DELTA}", delta,
                    "{PLAYER_NAME}", player.getName()
            };

            String message = _local.translate(localizedMessage, replacements);
            receiver.spigot().sendMessage(ComponentBuilder.parse(message).build());
        }

        protected String getLocalizedErrorMessage() {
            return "COMMAND_BALANCE_EDIT_ERROR";
        }

        protected String getLocalizedSuccessMessage() {
            return "COMMAND_BALANCE_EDIT";
        }
    }

    protected interface BalanceManipulator {
        Long manipulate(Long currentBalance, Long delta);
    }
}