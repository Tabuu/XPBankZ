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
import nl.tabuu.xpbankz.events.XPBankBalanceChangeEvent;
import nl.tabuu.xpbankz.util.ExperienceUtil;
import org.bukkit.Bukkit;
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

        addSubCommand("balance", new XPBankBalanceCommand(this));
        addSubCommand("deposit", new XPBankBalanceDeposit(this));
        addSubCommand("withdraw", new XPBankBalanceWithdraw(this));
        addSubCommand("transfer", new XPBankBalanceTransfer(this));
        addSubCommand("set", new XPBankBalanceSetCommand(this));
        addSubCommand("give", new XPBankBalanceGiveCommand(this));
        addSubCommand("take", new XPBankBalanceTakeCommand(this));
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

        protected String[] getReplacements(Player player) {
            long balance = _bank.getBalance(player);
            long balanceLevel = ExperienceUtil.pointToLevel((int) balance);
            long balanceLevelYield = balanceLevel - player.getLevel();
            int currentPoints = ExperienceUtil.getPoints(player);

            if(balanceLevelYield < 0) balanceLevelYield = 0;

            return new String[]{
                    "{BALANCE_POINTS}", String.format("%s", balance),
                    "{BALANCE_LEVEL}", String.format("%s", balanceLevel),
                    "{BALANCE_LEVEL_YIELD}", String.format("%s", balanceLevelYield),
                    "{CURRENT_POINTS}", String.format("%s", currentPoints)
            };
        }
    }

    class XPBankBalanceDeposit extends Command {

        protected XPBankBalanceDeposit(Command parent) {
            super("xpbank deposit", parent);

            setRequiredSenderType(SenderType.PLAYER);

            ArgumentConverter converter = new OrderedArgumentConverter().setSequence(ArgumentType.LONG);
            setArgumentConverter(converter);
        }

        @Override
        protected CommandResult onCommand(CommandSender sender, List<Optional<?>> arguments) {
            if (!arguments.stream().allMatch(Optional::isPresent)) return CommandResult.WRONG_SYNTAX;

            Player player = (Player) sender;
            long amount = (Long) arguments.get(0).get();

            String message = "COMMAND_DEPOSIT_ERROR";
            if(_bank.deposit(player, amount)) {
                long points = ExperienceUtil.getPoints(player);
                points -= amount;

                if(points > 0) {
                    ExperienceUtil.setPoints(player, (int) points);
                    message = "COMMAND_DEPOSIT";
                }
            }

            message = _local.translate(message, "{AMOUNT}", String.format("%s", amount));
            player.spigot().sendMessage(ComponentBuilder.parse(message).build());

            return CommandResult.SUCCESS;
        }
    }

    class XPBankBalanceWithdraw extends Command {

        protected XPBankBalanceWithdraw(Command parent) {
            super("xpbank withdraw", parent);

            setRequiredSenderType(SenderType.PLAYER);

            ArgumentConverter converter = new OrderedArgumentConverter().setSequence(ArgumentType.LONG);
            setArgumentConverter(converter);
        }

        @Override
        protected CommandResult onCommand(CommandSender sender, List<Optional<?>> arguments) {
            if (!arguments.stream().allMatch(Optional::isPresent)) return CommandResult.WRONG_SYNTAX;

            Player player = (Player) sender;
            long amount = (Long) arguments.get(0).get();

            String message = "COMMAND_WITHDRAW_ERROR";
            if(_bank.withdraw(player, amount)) {
                long points = ExperienceUtil.getPoints(player);
                points += amount;

                if(points <= Integer.MAX_VALUE) {
                    ExperienceUtil.setPoints(player, (int) points);
                    message = "COMMAND_WITHDRAW";
                }
            }

            message = _local.translate(message, "{AMOUNT}", String.format("%s", amount));
            player.spigot().sendMessage(ComponentBuilder.parse(message).build());

            return CommandResult.SUCCESS;
        }
    }

    class XPBankBalanceTransfer extends Command {

        protected XPBankBalanceTransfer(Command parent) {
            super("xpbank transfer", parent);

            setRequiredSenderType(SenderType.PLAYER);

            ArgumentConverter converter = new OrderedArgumentConverter().setSequence(ArgumentType.OFFLINE_PLAYER, ArgumentType.LONG);
            setArgumentConverter(converter);
        }

        @Override
        protected CommandResult onCommand(CommandSender sender, List<Optional<?>> arguments) {
            if (!arguments.stream().allMatch(Optional::isPresent)) return CommandResult.WRONG_SYNTAX;

            Player player = (Player) sender;
            OfflinePlayer receiver = (OfflinePlayer) arguments.get(0).get();
            long amount = (Long) arguments.get(1).get();

            String message = "COMMAND_TRANSFER_ERROR";
            if(_bank.has(player, amount) && _bank.deposit(receiver, amount)) {
                _bank.withdraw(player, amount);
                message = "COMMAND_TRANSFER";
            }

            message = _local.translate(message, "{AMOUNT}", String.format("%s", amount), "{PLAYER}", receiver.getName());
            player.spigot().sendMessage(ComponentBuilder.parse(message).build());

            return CommandResult.SUCCESS;
        }
    }

    class XPBankBalanceSetCommand extends XPBankBalanceEditCommand {

        protected XPBankBalanceSetCommand(Command parent) {
            super("xpbank set", parent);
        }

        @Override
        protected BalanceManipulator getBalanceManipulator() {
            return (balance, delta) -> delta;
        }
    }

    class XPBankBalanceGiveCommand extends XPBankBalanceEditCommand {

        protected XPBankBalanceGiveCommand(Command parent) {
            super("xpbank give", parent);
        }

        @Override
        protected BalanceManipulator getBalanceManipulator() {
            return Long::sum;
        }
    }

    class XPBankBalanceTakeCommand extends XPBankBalanceEditCommand {

        protected XPBankBalanceTakeCommand(Command parent) {
            super("xpbank take", parent);
        }

        @Override
        protected BalanceManipulator getBalanceManipulator() {
            return (balance, delta) -> balance - delta;
        }
    }

    abstract class XPBankBalanceEditCommand extends Command {

        protected XPBankBalanceEditCommand(String name, Command parent) {
            super(name, parent);

            ArgumentConverter converter = new OrderedArgumentConverter().setSequence(ArgumentType.OFFLINE_PLAYER, ArgumentType.LONG);
            setArgumentConverter(converter);
        }

        protected abstract BalanceManipulator getBalanceManipulator();

        @Override
        protected CommandResult onCommand(CommandSender sender, List<Optional<?>> arguments) {
            if (!arguments.stream().allMatch(Optional::isPresent)) return CommandResult.WRONG_SYNTAX;

            OfflinePlayer player = (OfflinePlayer) arguments.get(0).get();
            long delta = (Long) arguments.get(1).get();
            long oldBalance = _bank.getBalance(player);
            long newBalance = getBalanceManipulator().manipulate(oldBalance, delta);

            String message;
            if (_bank.set(player, newBalance))
                message = "COMMAND_BALANCE_EDIT";
            else
                message = "COMMAND_BALANCE_EDIT_ERROR";

            message = _local.translate(message,
                    "{BALANCE_POINTS}", String.format("%s", oldBalance),
                    "{PLAYER}", player.getName());
            sender.spigot().sendMessage(ComponentBuilder.parse(message).build());

            return CommandResult.SUCCESS;
        }
    }

    protected interface BalanceManipulator {
        Long manipulate(Long currentBalance, Long delta);
    }
}
