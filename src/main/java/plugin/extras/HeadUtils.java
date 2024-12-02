package plugin.extras;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;

public final class HeadUtils {

	public static String normalizedNameFromMHFName(String mhfName) {
		mhfName = mhfName.substring(4);
		String mhfCompact = mhfName.replace("_", "").replace(" ", "").toLowerCase();
		if (mhfCompact.equals("lavaslime"))
			return "Magma Cube";
		else if (mhfCompact.equals("golem"))
			return "Iron Golem";
		else if (mhfCompact.equals("pigzombie"))
			return "Zombie Pigman";
		else if (mhfCompact.equals("mushroomcow"))
			return "Mooshroom";
		else if (mhfName.isEmpty())
			return "";
		else {
			char[] chars = mhfName.toCharArray();
			StringBuilder name = new StringBuilder("").append(chars[0]);
			for (int i = 1; i < chars.length; ++i) {
				if (Character.isUpperCase(chars[i]) && chars[i - 1] != ' ')
					name.append(' ');
				name.append(chars[i]);
			}
			return name.toString();
		}
	}

	public static ItemStack makeCustomHead(String base64TextureValue, boolean setOwner) {
		ItemStack head = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta meta = (SkullMeta) head.getItemMeta();

		// Optionally set the owner if you have a player UUID
		if (setOwner && base64TextureValue != null) {
			OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.nameUUIDFromBytes(base64TextureValue.getBytes()));
			if (player != null) {
				meta.setOwningPlayer(player);
			}
		}

		head.setItemMeta(meta);

		// Use NBT API to set the texture
		NBTItem nbti = new NBTItem(head);
		NBTCompound skullOwner = nbti.addCompound("SkullOwner");
		skullOwner.setString("Name", meta.displayName() != null ? meta.displayName().examinableName() : "Custom Head");
		NBTCompound properties = skullOwner.addCompound("Properties");
		NBTCompound textures = properties.addCompound("textures");
		textures.setString("Value", base64TextureValue);

		return nbti.getItem();
	}

	public static boolean isHead(Material type) {
		switch (type) {
		case CREEPER_HEAD:
		case CREEPER_WALL_HEAD:
		case DRAGON_HEAD:
		case DRAGON_WALL_HEAD:
		case PLAYER_HEAD:
		case PLAYER_WALL_HEAD:
		case ZOMBIE_HEAD:
		case ZOMBIE_WALL_HEAD:
		case SKELETON_SKULL:
		case SKELETON_WALL_SKULL:
		case WITHER_SKELETON_SKULL:
		case WITHER_SKELETON_WALL_SKULL:
			return true;
		default:
			if (type.name().equals("PIGLIN_HEAD") || type.name().equals("PIGLIN_WALL_HEAD"))
				return true;
			return false;
		}
	}

	public static boolean isPlayerHead(Material type) {
		return type == Material.PLAYER_HEAD || type == Material.PLAYER_WALL_HEAD;
	}

	public static EntityType getEntityFromHead(Material type) {
		switch (type) {
		case CREEPER_HEAD:
		case CREEPER_WALL_HEAD:
			return EntityType.CREEPER;
		case DRAGON_HEAD:
		case DRAGON_WALL_HEAD:
			return EntityType.ENDER_DRAGON;
		case PLAYER_HEAD:
		case PLAYER_WALL_HEAD:
			return EntityType.PLAYER;
		case ZOMBIE_HEAD:
		case ZOMBIE_WALL_HEAD:
			return EntityType.ZOMBIE;
		case SKELETON_SKULL:
		case SKELETON_WALL_SKULL:
			return EntityType.SKELETON;
		case WITHER_SKELETON_SKULL:
		case WITHER_SKELETON_WALL_SKULL:
			return EntityType.WITHER_SKELETON;
		default:
			if (type.name().equals("PIGLIN_HEAD") || type.name().equals("PIGLIN_WALL_HEAD"))
				return EntityType.valueOf("PIGLIN");
			throw new IllegalArgumentException("Unknown head type: " + type);
		}
	}

	public enum HeadType {
		HEAD, SKULL, TOE
	}

	public static HeadType getDroppedHeadType(EntityType eType) {
		if (eType == null)
			return null;
		switch (eType) {
		case SKELETON:
		case SKELETON_HORSE:
		case WITHER_SKELETON:
		case STRAY:
			return HeadType.SKULL;
		case GIANT:
			return HeadType.TOE;
		default:
			return HeadType.HEAD;
		}
	}

	public static boolean dropsHeadFromChargedCreeper(EntityType eType) {
		switch (eType) {
		case ZOMBIE:
		case CREEPER:
		case SKELETON:
		case WITHER_SKELETON:
			return true;
		default:
			return eType.name().equals("PIGLIN");
		}
	}

	public static boolean hasGrummName(Nameable e) {
		return e.customName() != null
				&& (e.customName().examinableName().equals("Dinnerbone") || e.customName().examinableName().equals("Grumm"));
	}
}