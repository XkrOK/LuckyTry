package luarennix.luckytry.commands;

import luarennix.luckytry.duel.DuelManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LuckyDuelCommand implements CommandExecutor {

    private final DuelManager duelManager;

    public LuckyDuelCommand(DuelManager duelManager) {
        this.duelManager = duelManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§cИспользование: /luckyduel <ник> <сумма>");
            player.sendMessage("§cИли: /luckyduel accept | cancel");
            return true;
        }

        // Обработка команды /luckyduel accept
        if (args[0].equalsIgnoreCase("accept")) {
            duelManager.acceptDuel(player);
            return true;
        }

        // Обработка команды /luckyduel cancel
        if (args[0].equalsIgnoreCase("cancel")) {
            duelManager.cancelDuel(player);
            return true;
        }

        // Обработка команды /luckyduel <ник> <сумма>
        if (args.length < 2) {
            player.sendMessage("§cИспользование: /luckyduel <ник> <сумма>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("§cИгрок " + args[0] + " не найден.");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage("§cВы не можете пригласить себя на дуэль.");
            return true;
        }

        try {
            double amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                player.sendMessage("§cСумма должна быть больше нуля.");
                return true;
            }

            duelManager.invitePlayer(player, target, amount);
        } catch (NumberFormatException e) {
            player.sendMessage("§cНеверный формат суммы.");
        }

        return true;
    }
}