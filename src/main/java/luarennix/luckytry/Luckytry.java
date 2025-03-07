package luarennix.luckytry;

import luarennix.luckytry.commands.*;
import luarennix.luckytry.duel.DuelManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Luckytry extends JavaPlugin {

    private static Luckytry instance; // Статическая переменная для хранения экземпляра плагина

    @Override
    public void onEnable() {
        instance = this; // Сохраняем текущий экземпляр плагина
        DuelManager duelManager = new DuelManager();

        // Регистрация команд
        Objects.requireNonNull(this.getCommand("luckyduel")).setExecutor(new LuckyDuelCommand(duelManager));
    }

    // Статический метод для получения экземпляра плагина
    public static Luckytry getInstance() {
        return instance;
    }
}