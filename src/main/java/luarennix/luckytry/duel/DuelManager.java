package luarennix.luckytry.duel;

import luarennix.luckytry.Luckytry;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DuelManager {

    private final Map<Player, DuelInvitation> duelInvitations = new HashMap<>();
    private Economy economy;

    public DuelManager() {
        setupEconomy();
    }

    private boolean setupEconomy() {
        if (Luckytry.getInstance().getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Luckytry.getInstance().getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public void invitePlayer(Player inviter, Player target, double amount) {
        // Проверка баланса приглашающего игрока
        if (economy.getBalance(inviter) < amount) {
            inviter.sendMessage("§cУ вас недостаточно средств для ставки в размере " + amount + ".");
            return;
        }

        // Проверка баланса целевого игрока
        if (economy.getBalance(target) < amount) {
            inviter.sendMessage("§cУ игрока " + target.getName() + " недостаточно средств для принятия ставки.");
            return;
        }

        duelInvitations.put(target, new DuelInvitation(inviter, amount));
        target.sendMessage("§aИгрок " + inviter.getName() + " пригласил вас на Lucky Duel на сумму " + amount + "!");
        target.sendMessage("§aИспользуйте §e/luckyduel accept §aдля принятия или §e/luckyduel cancel §aдля отмены.");
        inviter.sendMessage("§aВы пригласили игрока " + target.getName() + " на Lucky Duel на сумму " + amount + ".");
    }

    public void acceptDuel(Player player) {
        DuelInvitation invitation = duelInvitations.get(player);
        if (invitation == null) {
            player.sendMessage("§cУ вас нет активных приглашений на дуэль.");
            return;
        }

        Player inviter = invitation.getInviter();
        double amount = invitation.getAmount();

        // Проверка баланса игрока, который принимает дуэль
        if (economy.getBalance(player) < amount) {
            player.sendMessage("§cУ вас недостаточно средств для принятия ставки.");
            return;
        }

        // Проверка баланса приглашающего игрока
        if (economy.getBalance(inviter) < amount) {
            player.sendMessage("§cУ игрока " + inviter.getName() + " недостаточно средств для дуэли.");
            return;
        }

        duelInvitations.remove(player);
        startDuel(inviter, player, amount);
    }

    public void cancelDuel(Player player) {
        DuelInvitation invitation = duelInvitations.get(player);
        if (invitation == null) {
            player.sendMessage("§cУ вас нет активных приглашений на дуэль.");
            return;
        }

        Player inviter = invitation.getInviter();
        duelInvitations.remove(player);
        inviter.sendMessage("§cИгрок " + player.getName() + " отклонил ваше приглашение на дуэль.");
        player.sendMessage("§cВы отклонили приглашение на дуэль.");
    }

    private void startDuel(Player player1, Player player2, double amount) {
        Random random = new Random();
        int num1 = random.nextInt(6) + 1; // Число для первого игрока (от 1 до 6)
        int num2 = random.nextInt(6) + 1; // Число для второго игрока (от 1 до 6)

        // Счетчик завершенных анимаций
        int[] completedAnimations = {0};

        // Анимация для первого игрока
        new BukkitRunnable() {
            int current = 0;

            @Override
            public void run() {
                if (current < num1) {
                    current++;
                    showTitle(player1, "§e" + current); // Показываем число как Title
                } else {
                    this.cancel(); // Останавливаем анимацию
                    completedAnimations[0]++;
                    checkIfAnimationsCompleted(player1, player2, num1, num2, amount, completedAnimations);
                }
            }
        }.runTaskTimer(Luckytry.getInstance(), 0L, 10L); // Задержка 0.5 секунды (10 тиков)

        // Анимация для второго игрока
        new BukkitRunnable() {
            int current = 0;

            @Override
            public void run() {
                if (current < num2) {
                    current++;
                    showTitle(player2, "§e" + current); // Показываем число как Title
                } else {
                    this.cancel(); // Останавливаем анимацию
                    completedAnimations[0]++;
                    checkIfAnimationsCompleted(player1, player2, num1, num2, amount, completedAnimations);
                }
            }
        }.runTaskTimer(Luckytry.getInstance(), 0L, 10L); // Задержка 0.5 секунды (10 тиков)
    }

    private void checkIfAnimationsCompleted(Player player1, Player player2, int num1, int num2, double amount, int[] completedAnimations) {
        // Если оба игрока завершили анимацию
        if (completedAnimations[0] == 2) {
            checkResults(player1, player2, num1, num2, amount);
        }
    }

    private void showTitle(Player player, String text) {
        player.sendTitle(text, "", 5, 20, 5); // Показываем Title на 1 секунду (20 тиков)
    }

    private void checkResults(Player player1, Player player2, int num1, int num2, double amount) {
        if (num1 > num2) {
            // Игрок 1 выиграл, игрок 2 проиграл
            economy.withdrawPlayer(player2, amount); // Снимаем деньги у проигравшего
            economy.depositPlayer(player1, amount);  // Добавляем деньги победителю
            player1.sendTitle("§aПобеда!", "Вы получили " + amount + "!", 10, 70, 20);
            player2.sendTitle("§cПоражение", "Вы потеряли " + amount + ".", 10, 70, 20);
        } else if (num2 > num1) {
            // Игрок 2 выиграл, игрок 1 проиграл
            economy.withdrawPlayer(player1, amount); // Снимаем деньги у проигравшего
            economy.depositPlayer(player2, amount);  // Добавляем деньги победителю
            player2.sendTitle("§aПобеда!", "Вы получили " + amount + "!", 10, 70, 20);
            player1.sendTitle("§cПоражение", "Вы потеряли " + amount + ".", 10, 70, 20);
        } else {
            // Ничья
            player1.sendTitle("§fНичья!", "Оба игрока выбили " + num1 + ".", 10, 70, 20);
            player2.sendTitle("§fНичья!", "Оба игрока выбили " + num2 + ".", 10, 70, 20);
        }
    }

    private static class DuelInvitation {
        private final Player inviter;
        private final double amount;

        public DuelInvitation(Player inviter, double amount) {
            this.inviter = inviter;
            this.amount = amount;
        }

        public Player getInviter() {
            return inviter;
        }

        public double getAmount() {
            return amount;
        }
    }
}