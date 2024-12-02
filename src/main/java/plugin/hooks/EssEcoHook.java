package plugin.hooks;

import java.math.BigDecimal;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;

import net.ess3.api.MaxMoneyException;

public class EssEcoHook {

	/**
	 * Gets the balance of a player.
	 *
	 * @param p The OfflinePlayer to check.
	 * @return The balance of the player, or 0 if the player does not exist or an error occurs.
	 */
	public static double getBalance(OfflinePlayer p) {
		if (p == null) {
			Bukkit.getLogger().warning("EssEcoHook: OfflinePlayer is null in getBalance()");
			return 0D;
		}
		try {
			return Economy.getMoneyExact(p.getUniqueId()).doubleValue();
		} catch (UserDoesNotExistException e) {
			Bukkit.getLogger().warning("EssEcoHook: Player does not exist - " + p.getName());
		} catch (Exception e) {
			Bukkit.getLogger().warning("EssEcoHook: Unexpected error in getBalance() - " + e.getMessage());
			e.printStackTrace();
		}
		return 0D;
	}

	/**
	 * Checks if the player has at least the specified amount of money.
	 *
	 * @param p      The OfflinePlayer to check.
	 * @param amount The amount to check for.
	 * @return True if the player has at least the specified amount, false otherwise.
	 */
	public static boolean hasAtLeast(OfflinePlayer p, double amount) {
		if (p == null) {
			Bukkit.getLogger().warning("EssEcoHook: OfflinePlayer is null in hasAtLeast()");
			return false;
		}
		try {
			return Economy.hasEnough(p.getUniqueId(), BigDecimal.valueOf(amount));
		} catch (UserDoesNotExistException e) {
			Bukkit.getLogger().warning("EssEcoHook: Player does not exist - " + p.getName());
		} catch (Exception e) {
			Bukkit.getLogger().warning("EssEcoHook: Unexpected error in hasAtLeast() - " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Gives money to a player.
	 *
	 * @param p      The OfflinePlayer to receive the money.
	 * @param amount The amount to give.
	 * @return True if the transaction was successful, false otherwise.
	 */
	public static boolean giveMoney(OfflinePlayer p, double amount) {
		return giveMoney(p, BigDecimal.valueOf(amount));
	}

	/**
	 * Gives money to a player.
	 *
	 * @param p      The OfflinePlayer to receive the money.
	 * @param amount The BigDecimal amount to give.
	 * @return True if the transaction was successful, false otherwise.
	 */
	public static boolean giveMoney(OfflinePlayer p, BigDecimal amount) {
		if (p == null) {
			Bukkit.getLogger().warning("EssEcoHook: OfflinePlayer is null in giveMoney()");
			return false;
		}
		try {
			Economy.add(p.getUniqueId(), amount);
			return true;
		} catch (NoLoanPermittedException e) {
			Bukkit.getLogger().warning("EssEcoHook: Loans are not permitted for - " + p.getName());
		} catch (MaxMoneyException e) {
			Bukkit.getLogger().warning("EssEcoHook: Max money limit exceeded for - " + p.getName());
		} catch (UserDoesNotExistException e) {
			Bukkit.getLogger().warning("EssEcoHook: Player does not exist - " + p.getName());
		} catch (Exception e) {
			Bukkit.getLogger().warning("EssEcoHook: Unexpected error in giveMoney() - " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Sets the player's money balance to a specific amount.
	 *
	 * @param p      The OfflinePlayer whose balance is to be set.
	 * @param amount The amount to set.
	 * @return True if the operation was successful, false otherwise.
	 */
	public static boolean setMoney(OfflinePlayer p, double amount) {
		if (p == null) {
			Bukkit.getLogger().warning("EssEcoHook: OfflinePlayer is null in setMoney()");
			return false;
		}
		try {
			Economy.setMoney(p.getUniqueId(), BigDecimal.valueOf(amount));
			return true;
		} catch (NoLoanPermittedException e) {
			Bukkit.getLogger().warning("EssEcoHook: Loans are not permitted for - " + p.getName());
		} catch (MaxMoneyException e) {
			Bukkit.getLogger().warning("EssEcoHook: Max money limit exceeded for - " + p.getName());
		} catch (UserDoesNotExistException e) {
			Bukkit.getLogger().warning("EssEcoHook: Player does not exist - " + p.getName());
		} catch (Exception e) {
			Bukkit.getLogger().warning("EssEcoHook: Unexpected error in setMoney() - " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Charges a fee from the player's balance.
	 *
	 * @param p      The OfflinePlayer to charge.
	 * @param amount The amount to charge.
	 * @return True if the operation was successful, false otherwise.
	 */
	public static boolean chargeFee(OfflinePlayer p, double amount) {
		return chargeFee(p, BigDecimal.valueOf(amount));
	}

	/**
	 * Charges a fee from the player's balance.
	 *
	 * @param p      The OfflinePlayer to charge.
	 * @param amount The BigDecimal amount to charge.
	 * @return True if the operation was successful, false otherwise.
	 */
	public static boolean chargeFee(OfflinePlayer p, BigDecimal amount) {
		if (p == null) {
			Bukkit.getLogger().warning("EssEcoHook: OfflinePlayer is null in chargeFee()");
			return false;
		}
		try {
			if (!Economy.hasEnough(p.getUniqueId(), amount)) {
				Bukkit.getLogger().warning("EssEcoHook: Player does not have enough money - " + p.getName());
				return false;
			}
			Economy.subtract(p.getUniqueId(), amount);
			return true;
		} catch (NoLoanPermittedException e) {
			Bukkit.getLogger().warning("EssEcoHook: Loans are not permitted for - " + p.getName());
		} catch (MaxMoneyException e) {
			Bukkit.getLogger().warning("EssEcoHook: Max money limit exceeded during chargeFee() for - " + p.getName());
		} catch (UserDoesNotExistException e) {
			Bukkit.getLogger().warning("EssEcoHook: Player does not exist - " + p.getName());
		} catch (Exception e) {
			Bukkit.getLogger().warning("EssEcoHook: Unexpected error in chargeFee() - " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}
}