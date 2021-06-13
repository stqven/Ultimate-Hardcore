package inv.me.own.exceptions;

import org.bukkit.Bukkit;

import java.util.logging.Level;

public class ConfigNotFoundException extends Exception {

    private String msg;

    public ConfigNotFoundException(String msg) {
        this.msg = msg;
    }

    @Override
    public void printStackTrace() {
        Bukkit.getLogger().log(Level.WARNING, msg);
    }
}
