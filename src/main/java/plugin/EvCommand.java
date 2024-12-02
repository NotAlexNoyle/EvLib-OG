package plugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.trueog.utilitiesog.UtilitiesOG;

/** Base class for implementing plugin commands */
public abstract class EvCommand implements TabExecutor {

	//	protected EvPlugin plugin;
	final String commandName;
	final PluginCommand command;
	final static CommandExecutor disabledCmdExecutor = new CommandExecutor() {

		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {

			UtilitiesOG.trueogMessage((Player) sender, "&cERROR: That command is currently unavailable");

			return true;

		}

	};


	//	@Override
	//	abstract public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args);

	public EvCommand(JavaPlugin pl, boolean enabled) {

		//		plugin = p;
		commandName = getClass().getSimpleName().substring(7).toLowerCase();

		command = pl.getCommand(commandName);

		if(enabled) {

			command.setExecutor(this); command.setTabCompleter(this);

		}

		else command.setExecutor(disabledCmdExecutor);

	}

	public EvCommand(JavaPlugin pl){

		this(pl, true);

	}

	public PluginCommand getCommand() {

		return command;

	}

}