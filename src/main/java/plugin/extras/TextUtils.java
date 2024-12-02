package plugin.extras;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.trueog.utilitiesog.UtilitiesOG;

public class TextUtils {

	public static final char[] COLOR_CHARS = new char[]
			{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', /*r*/};
	public static final char[] FORMAT_CHARS = new char[]{'k', 'l', 'm', 'n', 'o', /*r*/};
	public static boolean isSimpleColor(char ch){
		switch(ch){
		case '0': case '1': case '2': case '3': case '4':
		case '5': case '6': case '7': case '8': case '9':
		case 'a': case 'b': case 'c': case 'd': case 'e':
		case 'f': case 'r': return true;
		default: return false;
		}
	}
	public static boolean isFormat(char ch){
		switch(ch){
		case 'k': case 'l': case 'm': case 'n': case 'o': return true;
		default: return false;
		}
	}
	public static boolean isSimpleColorOrFormat(char ch){
		switch(ch){
		case '0': case '1': case '2': case '3': case '4':
		case '5': case '6': case '7': case '8': case '9':
		case 'a': case 'b': case 'c': case 'd': case 'e':
		case 'k': case 'l': case 'm': case 'n': case 'o':
		case 'f': case 'r': return true;
		default: return false;
		}
	}

	/*public static String generateRandomASCII(int desiredLength){//TODO: currently unused
		StringBuilder builder = new StringBuilder();
		Random rand = new Random();
		for(int i=0; i<desiredLength; ++i){
			int randC = 33 + rand.nextInt(223);
			switch(randC){
				// These are all spaces; we avoid using them just generate a new randC
				case 127: case 129: case 141: case 143: case 144: case 157: case 160: case 173: --i; continue;
				default: break;
			}
			builder.append((char)randC);
		}
		return builder.toString();
	}*/

	public static Component translateAlternateColorCodes(char altColorChar, String textToTranslate) {
		// Replace the alternate color character with the MiniMessage supported `<` or legacy codes like `&` to `<`.
		String convertedText = textToTranslate.replace(altColorChar, '<')
				.replaceAll("(?<!\\\\)#([0-9a-fA-F]{6})", "<#$1>"); // Handle hex colors

		// Use MiniMessage to parse the text into a Component
		return MiniMessage.miniMessage().deserialize(convertedText);
	}

	public static String translateAlternateColorCodes(char altColorChar, String textToTranslate, String resetColor) {
		// Replace the alternate color character with `<` for MiniMessage
		String convertedText = textToTranslate.replace(altColorChar, '<')
				.replaceAll("(?<!\\\\)#([0-9a-fA-F]{6})", "<#$1>"); // Handle hex colors

		// Use MiniMessage to parse and serialize back, ensuring the string output.
		String deserializedText = MiniMessage.miniMessage().deserialize(convertedText).toString();

		// Replace the reset color tag (`<reset>`) with the provided resetColor
		return deserializedText.replace("<reset>", resetColor);
	}


	//TODO: fix bug: "&l " is different from " &l", so we need special handling for it
	/*public static String minimizeColorCodes(String legacyStr){// TODO: unused
		// Find (<color>)(\s|<color>)+
		Pattern colorsAndSpacesPattern = Pattern.compile(
				"(?:(?:§x(?:§[0-9a-fA-F]){6})|(?:§[0-9a-fA-FrRk-oK-O]))(?:(?:§x(?:§[0-9a-fA-F]){6})|(?:§[0-9a-fA-FrRk-oK-O])|\\s)*");
		Pattern lastColorPattern = Pattern.compile("((?:§x(?:§[0-9a-fA-F]){6})|(?:§[0-9a-fA-FrR]))\\s*$", Pattern.MULTILINE);
		Matcher colorMatcher = colorsAndSpacesPattern.matcher(legacyStr);
		StringBuilder builder = new StringBuilder();
		int lastEnd = 0;
		while(colorMatcher.find()){
			builder.append(legacyStr.substring(lastEnd, colorMatcher.start()));
			String colorsAndSpaces = colorMatcher.group();
			Matcher lastColorFinder = lastColorPattern.matcher(colorsAndSpaces);
			lastColorFinder.find();
			builder.append(lastColorFinder.group(1)).append(TextUtils.stripColorsOnly(colorsAndSpaces));
			lastEnd = colorMatcher.end();
		}
		builder.append(legacyStr.substring(lastEnd));
		return builder.toString();
	}*/

	// TODO: implement "remove useless trailing colors/formats", which removes colors/formats at the end of a string that have no visual effect

	/**
	 * Removes color codes from a string by interpreting it as MiniMessage content
	 * and serializing it back to plain text.
	 *
	 * @param str The input string containing MiniMessage or color codes.
	 * @return A plain-text string without any color or decorations.
	 */
	public static String stripColorsOnly(String str) {
		Component parsed = MiniMessage.miniMessage().deserialize(str);
		// Strip all decorations, leaving plain text
		return PlainTextComponentSerializer.plainText().serialize(parsed);
	}

	/**
	 * Removes formatting codes (like bold, italic) while preserving color information.
	 *
	 * @param str The input string containing MiniMessage or color codes.
	 * @return A string with only color information preserved.
	 */
	public static String stripFormatsOnly(String str) {
		Component parsed = MiniMessage.miniMessage().deserialize(str);
		// Strip formats, but preserve color using specific serialization logic.
		Component stripped = parsed.style(style -> style.decorations(Collections.emptyMap()));
		return PlainTextComponentSerializer.plainText().serialize(stripped);
	}

	public static String getLeadingColorAndFormats(String str) {
		StringBuilder leadingColor = new StringBuilder();
		StringBuilder formats = new StringBuilder();

		boolean inTag = false;

		for (int i = 0; i < str.length(); i++) {
			char currentChar = str.charAt(i);

			if (currentChar == '<') {
				inTag = true; // Start of a MiniMessage tag
			} else if (inTag && currentChar == '>') {
				inTag = false; // End of a MiniMessage tag
				String tagContent = leadingColor.toString();

				if (tagContent.startsWith("#") && tagContent.length() == 7) {
					// Hex color, e.g., <#RRGGBB>
					leadingColor.setLength(0); // Reset
					leadingColor.append(tagContent);
				} else if (isNamedColor(tagContent)) {
					// Named color, e.g., <red>
					leadingColor.setLength(0); // Reset
					leadingColor.append(tagContent);
				} else if (isFormatTag(tagContent)) {
					// Formatting tag, e.g., <bold>
					if (!formats.toString().contains(tagContent)) {
						formats.append(tagContent).append(",");
					}
				}

				leadingColor.setLength(0); // Reset the builder to parse next tag
			} else if (inTag) {
				// Collect the content of the tag
				leadingColor.append(currentChar);
			} else if (!Character.isWhitespace(currentChar) && !inTag) {
				// Stop processing after the leading color/formats if we encounter text
				break;
			}
		}

		// Remove trailing comma from formats
		if (formats.length() > 0 && formats.charAt(formats.length() - 1) == ',') {
			formats.setLength(formats.length() - 1);
		}

		return leadingColor.toString() + (formats.length() > 0 ? "," + formats : "");
	}

	private static boolean isNamedColor(String tag) {
		// List of MiniMessage named colors (can be extended)
		return tag.matches("red|blue|green|yellow|aqua|black|white|gray|gold|dark_red|dark_blue|dark_green|dark_aqua|dark_gray|dark_purple|light_purple");
	}

	public static String getCurrentColor(String str) {
		// Regex to match the last color or format code in MiniMessage syntax
		String colorPattern = "(<#[0-9a-fA-F]{6}>|<\\w+>)$";
		Pattern pattern = Pattern.compile(colorPattern);
		Matcher matcher = pattern.matcher(str);

		// If a match is found, return the last color code
		if (matcher.find()) {
			return matcher.group();
		}

		return ""; // Return empty string if no color is present
	}


	public static String getCurrentFormats(String str) {
		StringBuilder formats = new StringBuilder();
		boolean inTag = false;
		StringBuilder currentTag = new StringBuilder();

		for (int i = str.length() - 1; i >= 0; i--) {
			char c = str.charAt(i);

			if (c == '>') {
				inTag = true; // End of a tag
				currentTag.setLength(0);
			} else if (inTag) {
				if (c == '<') {
					// Start of a tag
					inTag = false;
					String tag = currentTag.reverse().toString(); // Reverse because we parsed backward

					if (isFormatTag(tag) && formats.indexOf(tag) == -1) {
						// Add the tag if it's a format and not already present
						if (formats.length() > 0) {
							formats.insert(0, ",");
						}
						formats.insert(0, tag);
					}
				} else {
					currentTag.append(c);
				}
			}
		}

		return formats.toString();
	}

	// Helper to check if a tag represents a format
	private static boolean isFormatTag(String tag) {
		// List of valid MiniMessage formatting tags
		return tag.equals("bold") || tag.equals("italic") || tag.equals("underlined") ||
				tag.equals("strikethrough") || tag.equals("obfuscated");
	}


	// Returns empty-string if no color or format is present at end of string
	// Works identically to ChatColor.getLastColors()
	/*	public static String getCurrentColorAndFormats(String str){
		StringBuilder builder = new StringBuilder();
		final char[] msg = str.toCharArray();
		int i=msg.length-1;
		for(; i>0; --i) if(msg[i-1] == ChatColor.COLOR_CHAR){
			if(i >= 13 && msg[i-13] == ChatColor.COLOR_CHAR && msg[i-12] == 'x' 
					&& msg[i-11] == ChatColor.COLOR_CHAR && isHex(msg[i-10])
					&& msg[i-9] == ChatColor.COLOR_CHAR && isHex(msg[i-8])
					&& msg[i-7] == ChatColor.COLOR_CHAR && isHex(msg[i-6])
					&& msg[i-5] == ChatColor.COLOR_CHAR && isHex(msg[i-4])
					&& msg[i-3] == ChatColor.COLOR_CHAR && isHex(msg[i-2])
					&& isHex(msg[i])){builder.insert(0, str.substring(i-13, i+1)); ++i; break;}
			if(isSimpleColor(msg[i])){builder.insert(0, str.substring(i-1, i+1)); ++i; break;}
		}
		for(++i; i<msg.length; ++i) if(msg[i-1] == ChatColor.COLOR_CHAR && isFormat(msg[i])){
			final String formatStr = str.substring(i-1, i+1);
			if(builder.indexOf(formatStr) == -1) builder.append(formatStr);
		}
		return builder.toString();
	}*/

	public static boolean isEscaped(char[] str, int x){
		boolean escaped = false;
		while(x != 0 && str[--x] == '\\') escaped = !escaped;
		return escaped;
	}
	public static boolean isEscaped(String str, int x){
		boolean escaped = false;
		while(x != 0 && str.charAt(--x) == '\\') escaped = !escaped;
		return escaped;
	}

	public static String unescapeString(String str){
		StringBuilder builder = new StringBuilder("");
		boolean unescaped = true;
		for(char c : str.toCharArray()){
			if(c == '\\' && unescaped) unescaped = false;
			else{
				builder.append(c);
				unescaped = true;
			}
		}
		return builder.toString();
	}

	public static String escape(String str, String... thingsToEscape){
		str = str.replace("\\", "\\\\");//Escape escapes first!
		for(String item : thingsToEscape){
			//TODO: special handling for more special characters
			if(item.equals("\n")) str = str.replace("\n", "\\n");
			else str = str.replace(item, "\\"+item);
		}
		return str;
	}

	/**
	 * Converts a Location to a string without any colors.
	 *
	 * @param loc The Location to convert.
	 * @return The formatted location string.
	 */
	public static String locationToString(Location loc) {
		return locationToString(loc, null, null);
	}

	/**
	 * Converts a Location to a string with optional colors for coordinates and commas.
	 *
	 * @param loc          The Location to convert.
	 * @param coordColor   The MiniMessage color for coordinates.
	 * @param commaColor   The MiniMessage color for commas.
	 * @return The formatted location string.
	 */
	public static String locationToString(Location loc, String coordColor, String commaColor) {
		return locationToString(loc, coordColor, commaColor, 2);
	}

	/**
	 * Converts a Location to a string with specified precision and optional colors.
	 *
	 * @param loc          The Location to convert.
	 * @param coordColor   The MiniMessage color for coordinates.
	 * @param commaColor   The MiniMessage color for commas.
	 * @param precision    The number of decimal places for coordinates.
	 * @return The formatted location string.
	 */
	public static String locationToString(Location loc, String coordColor, String commaColor, int precision) {
		String coordPrefix = coordColor == null ? "" : coordColor;
		String commaPrefix = commaColor == null ? "" : commaColor;

		if (precision < 1) {
			return new StringBuilder()
					.append(coordPrefix).append(loc.getBlockX()).append(commaPrefix).append(',')
					.append(coordPrefix).append(loc.getBlockY()).append(commaPrefix).append(',')
					.append(coordPrefix).append(loc.getBlockZ())
					.toString();
		}

		String formatP = "%." + precision + "f";
		return new StringBuilder()
				.append(coordPrefix).append(String.format(formatP, loc.getX())).append(commaPrefix).append(',')
				.append(coordPrefix).append(String.format(formatP, loc.getY())).append(commaPrefix).append(',')
				.append(coordPrefix).append(String.format(formatP, loc.getZ()))
				.toString();
	}

	/**
	 * Parses a string into a Location, assuming the string was formatted by locationToString.
	 *
	 * @param s The formatted location string.
	 * @return A Location object or null if parsing fails.
	 */
	public static Location getLocationFromString(String s) {
		String[] data = s.split(",");
		World world = org.bukkit.Bukkit.getWorld(data[0]);
		if (world != null) {
			try {
				return new Location(world, Double.parseDouble(data[1]), Double.parseDouble(data[2]), Double.parseDouble(data[3]));
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Wraps text with a MiniMessage color tag.
	 *
	 * @param text  The text to wrap.
	 * @param color The MiniMessage color.
	 * @return The formatted text.
	 */
	public static String colorize(String text, String color) {
		return color == null ? text : "<" + color + ">" + text + "</" + color + ">";
	}

	public static Location getLocationFromString(World w, String s){
		String[] data = UtilitiesOG.stripFormatting(s).split(",");
		try{return new Location(w,
				Double.parseDouble(data[data.length-3]),
				Double.parseDouble(data[data.length-2]),
				Double.parseDouble(data[data.length-1]));}
		catch(ArrayIndexOutOfBoundsException | NumberFormatException ex){return null;}
	}

	static long[] scale = new long[]{31536000000L, /*2628000000L,*/ 604800000L, 86400000L, 3600000L, 60000L, 1000L};
	static char[] units = new char[]{'y', /*'m',*/ 'w', 'd', 'h', 'm', 's'};

	public static String formatTime(long millisecond){
		return formatTime(millisecond, /*show0s=*/true, "", "", ", ", units.length, scale, units);
	}
	/**
	 * Formats time with the default units and scale.
	 *
	 * @param millisecond The time in milliseconds.
	 * @param show0s      Whether to show zero values.
	 * @param timeColor   The MiniMessage color for time values.
	 * @param unitColor   The MiniMessage color for units.
	 * @return A formatted time string.
	 */
	public static String formatTime(long millisecond, boolean show0s, String timeColor, String unitColor) {
		return formatTime(millisecond, show0s, timeColor, unitColor, null, units.length);
	}

	/**
	 * Formats time with a specified number of significant units.
	 *
	 * @param millisecond The time in milliseconds.
	 * @param show0s      Whether to show zero values.
	 * @param timeColor   The MiniMessage color for time values.
	 * @param unitColor   The MiniMessage color for units.
	 * @param sigUnits    The number of significant units to include.
	 * @return A formatted time string.
	 */
	public static String formatTime(long millisecond, boolean show0s, String timeColor, String unitColor, int sigUnits) {
		return formatTime(millisecond, show0s, timeColor, unitColor, null, sigUnits);
	}

	public static String formatTime(long millisecond, boolean show0s, String timePrefix, String unitPrefix, String sep){
		return formatTime(millisecond, show0s, timePrefix, unitPrefix, sep, units.length, scale, units);
	}
	public static String formatTime(long millisecond, boolean show0s, String timePrefix, String unitPrefix, String sep, int sigUnits){
		return formatTime(millisecond, show0s, timePrefix, unitPrefix, sep, sigUnits, scale, units);
	}
	public static String formatTime(long millis, boolean show0s, String timePrefix, String unitPrefix, String sep, int sigUnits, long[] scale, char[] units){
		if(millis / scale[scale.length-1] == 0){
			return new StringBuilder(timePrefix).append("0").append(unitPrefix).append(units[units.length-1]).toString();
		}
		int i = 0, unitsShown = 0;
		while(millis < scale[i]) ++i;
		StringBuilder builder = new StringBuilder("");
		for(; i < scale.length-1; ++i){
			if(show0s || millis / scale[i] != 0){
				long scaledTime = millis / scale[i];
				builder.append(timePrefix).append(scaledTime).append(unitPrefix).append(units[i]).append(sep);
				if(++unitsShown == sigUnits) break;
			}
			millis %= scale[i];
		}
		if((show0s || (millis / scale[scale.length-1]) != 0) && unitsShown < sigUnits)
			return builder
					.append(timePrefix).append(millis / scale[scale.length-1])
					.append(unitPrefix).append(units[units.length-1]).toString();
		else return builder.substring(0, builder.length()-sep.length()); // cut off trailing sep
	}
	/**
	 * Takes a string such as 30w6d11h55m33s or 1y and returns a value in milliseconds
	 *
	 * @param formattedTime an Ev-style formatted time string
	 * @return a {@code long} representing the time interval in milliseconds.
	 */
	public static long parseTimeInMillis(String formattedTime, long defaultScale){
		formattedTime = formattedTime.toLowerCase();
		//formattedTime.matches("(?:y[1-9][0-9]*)?(?:ew[1-9][0-9]*)?(?:d[1-9][0-9]*)?(?:h[1-9][0-9]*)?(?:m[1-9][0-9]*)?(?:s[1-9][0-9]*)?");
		long time = 0;
		// Note: 'units' is sorted largest -> smallest
		for(int i=0; i<units.length && !formattedTime.isEmpty(); ++i){
			int idx = formattedTime.indexOf(units[i]);
			if(idx != -1){
				time += Long.parseLong(formattedTime.substring(0, idx))*scale[i];
				formattedTime = formattedTime.substring(idx+1);
			}
		}
		// If there is a leftover number (with no unit specified), assume Milliseconds (if defaultScale is 1)
		if(!formattedTime.isEmpty()) time += Long.parseLong(formattedTime)*defaultScale;
		return time;
	}
	public static long parseTimeInMillis(String formattedTime){return parseTimeInMillis(formattedTime, /*defaultScale=*/1);}


	public static String capitalizeAndSpacify(String str, char toSpace){
		StringBuilder builder = new StringBuilder("");
		boolean lower = false;
		for(char c : str.toCharArray()){
			if(c == toSpace){builder.append(' '); lower = false;}
			else if(lower){builder.append(Character.toLowerCase(c));}
			else{builder.append(Character.toUpperCase(c)); lower = true;}
		}
		return builder.toString();
	}

	@Deprecated public static String getNormalizedName(Material material){
		switch(material.name()){
		case "CREEPER_BANNER_PATTERN":
		case "FLOWER_BANNER_PATTERN":
		case "GLOBE_BANNER_PATTERN":
		case "MOJANG_BANNER_PATTERN":
		case "SKULL_BANNER_PATTERN":
			return "Banner Pattern";
		default:
			//				return capitalizeWords(material.name().toLowerCase().replace("_", " "));
			return capitalizeAndSpacify(material.name(), '_');
		}
	}
	@Deprecated public static String getNormalizedName(EntityType eType){
		//TODO: improve this algorithm / test for errors
		switch(eType.name()){
		case "PIG_ZOMBIE":
			return "Zombie Pigman";
		case "MUSHROOM_COW":
			return "Mooshroom";
		case "TROPICAL_FISH"://TODO: 22 varieties (already implemented in TextureKeyLookup.java)
		default:
			//					return capitalizeWords(eType.toLowerCase().replace("_", " "));
			return capitalizeAndSpacify(eType.name(), '_');
		}
	}

	/**
	 * Returns a MiniMessage-compatible color string representing the rarity color of an item.
	 *
	 * @param item The ItemStack to determine the rarity color.
	 * @return A MiniMessage color string (e.g., "aqua", "yellow").
	 */
	public static String getRarityColor(ItemStack item) {
		final int version = Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]);

		if (version >= 14) {
			switch (item.getType().name()) {
			case "MOJANG_BANNER_PATTERN":
				return Bukkit.getBukkitVersion().compareTo("1.21.2") < 0 ? "light_purple" : "aqua";
			case "SKULL_BANNER_PATTERN":
				return Bukkit.getBukkitVersion().compareTo("1.21.2") < 0 ? enchAquaOr(item, "yellow") : "aqua";
			case "JIGSAW":
			case "LIGHT":
			case "MACE":
			case "HEAVY_CORE":
				return "light_purple";
			case "SNOUT_BANNER_PATTERN":
			case "CREEPER_BANNER_PATTERN":
			case "PIGLIN_HEAD":
				return enchAquaOr(item, "yellow");
			default:
				// Fallthrough intended
			}
		}

		if (Bukkit.getBukkitVersion().compareTo("1.21.2") >= 0) {
			switch (item.getType().name()) {
			case "RECOVERY_COMPASS":
			case "GOAT_HORN":
			case "SNIFFER_EGG":
			case "DISC_FRAGMENT_5":
			case "ECHO_SHARD":
			case "OMINOUS_BOTTLE":
			case "NETHERITE_UPGRADE_SMITHING_TEMPLATE":
			case "SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE":
			case "DUNE_ARMOR_TRIM_SMITHING_TEMPLATE":
			case "COAST_ARMOR_TRIM_SMITHING_TEMPLATE":
			case "WILD_ARMOR_TRIM_SMITHING_TEMPLATE":
			case "TIDE_ARMOR_TRIM_SMITHING_TEMPLATE":
			case "SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE":
			case "RIB_ARMOR_TRIM_SMITHING_TEMPLATE":
			case "WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE":
			case "SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE":
			case "RAISER_ARMOR_TRIM_SMITHING_TEMPLATE":
			case "HOST_ARMOR_TRIM_SMITHING_TEMPLATE":
			case "FLOW_ARMOR_TRIM_SMITHING_TEMPLATE":
			case "BOLT_ARMOR_TRIM_SMITHING_TEMPLATE":
				return enchAquaOr(item, "yellow");
			case "FLOW_BANNER_PATTERN":
			case "GUSTER_BANNER_PATTERN":
			case "WARD_ARMOR_TRIM_SMITHING_TEMPLATE":
			case "EYE_ARMOR_TRIM_SMITHING_TEMPLATE":
			case "VEX_ARMOR_TRIM_SMITHING_TEMPLATE":
			case "SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE":
				return "aqua";
			case "SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE":
				return "light_purple";
			default:
				if (item.getType().name().endsWith("_POTTERY_SHERD")) return enchAquaOr(item, "yellow");
				// Fallthrough intended
			}
		}

		switch (item.getType()) {
		case CHAINMAIL_HELMET:
		case CHAINMAIL_CHESTPLATE:
		case CHAINMAIL_LEGGINGS:
		case CHAINMAIL_BOOTS:
		case NAUTILUS_SHELL:
			return enchAquaOr(item, Bukkit.getBukkitVersion().compareTo("1.21.2") < 0 ? "white" : "yellow");
		case TRIDENT:
			return Bukkit.getBukkitVersion().compareTo("1.21") >= 0
			? (Bukkit.getBukkitVersion().compareTo("1.21.2") < 0 ? "light_purple" : "aqua")
					: enchAquaOr(item, "white");
		case ENCHANTED_GOLDEN_APPLE:
			return Bukkit.getBukkitVersion().compareTo("1.21.2") < 0 ? "light_purple" : "aqua";
		case CONDUIT:
			return Bukkit.getBukkitVersion().compareTo("1.21.2") < 0 ? "aqua" : enchAquaOr(item, "yellow");
		case MUSIC_DISC_11:
		case MUSIC_DISC_13:
		case MUSIC_DISC_BLOCKS:
		case MUSIC_DISC_CAT:
		case MUSIC_DISC_CHIRP:
		case MUSIC_DISC_FAR:
		case MUSIC_DISC_MALL:
		case MUSIC_DISC_MELLOHI:
		case MUSIC_DISC_STAL:
		case MUSIC_DISC_WAIT:
		case MUSIC_DISC_WARD:
			return Bukkit.getBukkitVersion().compareTo("1.21.2") < 0 ? "aqua" : enchAquaOr(item, "yellow");
		case NETHER_STAR:
		case WITHER_SKELETON_SKULL:
			return enchAquaOr(item, Bukkit.getBukkitVersion().compareTo("1.21.2") < 0 ? "yellow" : "aqua");
		case ELYTRA:
		case DRAGON_HEAD:
			return Bukkit.getBukkitVersion().compareTo("1.21.2") < 0 ? enchAquaOr(item, "yellow") : "light_purple";
		case DRAGON_EGG:
		case DEBUG_STICK:
		case KNOWLEDGE_BOOK:
		case COMMAND_BLOCK:
		case CHAIN_COMMAND_BLOCK:
		case REPEATING_COMMAND_BLOCK:
		case COMMAND_BLOCK_MINECART:
		case STRUCTURE_BLOCK:
		case STRUCTURE_VOID:
			return "light_purple";
		case SPAWNER:
			return Bukkit.getBukkitVersion().compareTo("1.19.3") < 0 ? "light_purple" : enchAquaOr(item, "white");
		case BEACON:
		case END_CRYSTAL:
		case GOLDEN_APPLE:
			return "aqua";
		case EXPERIENCE_BOTTLE:
		case DRAGON_BREATH:
		case ENCHANTED_BOOK:
		case PLAYER_HEAD:
		case CREEPER_HEAD:
		case ZOMBIE_HEAD:
		case SKELETON_SKULL:
		case HEART_OF_THE_SEA:
		case TOTEM_OF_UNDYING:
			return enchAquaOr(item, "yellow");
		default:
			if (item.getType().name().equals("LIGHT")) return "light_purple";
			if (item.getType().name().equals("MUSIC_DISC_PIGSTEP")
					|| item.getType().name().equals("MUSIC_DISC_OTHERSIDE")
					|| item.getType().name().equals("MUSIC_DISC_CREATOR")) {
				return "aqua";
			}
			if (item.getType().name().equals("MUSIC_DISC_5")
					|| item.getType().name().equals("MUSIC_DISC_RELIC")
					|| item.getType().name().equals("MUSIC_DISC_CREATOR_MUSIC_BOX")) {
				return Bukkit.getBukkitVersion().compareTo("1.21.2") < 0 ? "aqua" : enchAquaOr(item, "yellow");
			}
			return enchAquaOr(item, "white");
		}
	}

	/**
	 * Returns a MiniMessage color string based on whether the item has enchantments.
	 *
	 * @param item          The ItemStack to check.
	 * @param fallbackColor The fallback MiniMessage color.
	 * @return A MiniMessage color string.
	 */
	private static String enchAquaOr(ItemStack item, String fallbackColor) {
		return item.hasItemMeta() && item.getItemMeta().hasEnchants() ? "aqua" : fallbackColor;
	}


	// TODO: Move this to TabText (once TabText is cleaned up)?
	/* ----------==========---------- PIXEL WIDTH CALCULATION METHODS ----------==========---------- */
	//https://minecraft.gamepedia.com/Resource_pack#Fonts, DefaultAsstes/assets/minecraft/textures/font/ascii.png
	final public static int MAX_PIXEL_WIDTH = 320, MAX_MONO_WIDTH = 80, MAX_PLAYERNAME_MONO_WIDTH=16, MAX_PLAYERNAME_PIXEL_WIDTH = 96/*6*16*/;
	// Supports ASCII + Extended codes (32-255), and currently just assumes width=6 for all others
	// Note: Returns 1 more than actual width, since all characters are separated by a pixel
	/**
	 * returns character pixel-width, NOT safe with format codes
	 * @param ch the character to check
	 * @return character width in pixels
	 */
	private static int pxLen(char ch){
		switch(ch){
		case '§':
			return 0; // Actual width is 5
		case '΄':
			return 1;
		case '.': case ','://comma44
		case ':': case ';':
		case 'i': case '!': case '|': case '\'':
		case '¡': case '¦': case '´': case '¸':
		case '·':
			return 2;
		case '`':
		case 'l':
		case '‚'://comma130
		case 'ˆ':
		case '‘': case '’':
		case '•':
		case '¨':
		case 'ì': case 'í':
			return 3;
		case 'I': case 't':
		case 'Ì': case 'Í': case 'Î': case 'Ï': case 'î': case 'ï':
		case '[': case ']': case '(': case ')': case '{': case '}':
		case ' '://space!
		case '‹': case '›':
		case '˜':
		case '°': case '¹':
			return 4;
		case '"': case '*':
		case '<': case '>':
		case 'f': case 'k':
		case '„': case '“': case '”':
		case 'ª': case '²': case '³': case 'º':
			return 5;
			//			case '-':
			//				return 6;
		case '@': case '~': case '–':
		case '«': case '»':
		case '¶':
			return 7;
		case '…':
		case '‰':
		case '¤':
		case '©': case '®':
		case '¼': case '½': case '¾':
			return 8;
		case '—':
		case '™':
			return 9;
		case 'Œ': case 'œ': case 'Æ': case 'æ':
			return 10;
		}
		//for(int px : charList.keySet()) if(charList.get(px).indexOf(ch) >= 0) return px;
		return 6;
	}
	/**
	 * returns true if the character uses half-pixels when bold
	 * @param ch the Character to check
	 * @return boolean whether to add .5px if inside bold formatting
	 */
	public static boolean isHalfPixel(char ch) {
		switch(ch){
		case '´': case '¸'://2
		case 'ˆ': case '¨'://3
		case '˜'://4
			return true;
		default:
			return false;
		}
	}

	/**
	 * returns String pixel-width, considering format codes
	 * @param str the String to check
	 * @return String width in pixels
	 */
	public static int strLen(String str, boolean mono) {
		if(mono) {
			return UtilitiesOG.stripFormatting(str).length();
		}
		int len = 0;
		boolean bold = false, colorPick = false, halfPixel = false;
		for(char ch : str.toCharArray()){
			if(colorPick){
				colorPick = false;
				switch(ch){
				//Note: Italicizing can make chars "half-pixel-y", but won't ever change their width, unlike bolding.
				case '0': case '1': case '2': case '3': case '4':
				case '5': case '6': case '7': case '8': case '9':
				case 'a': case 'b': case 'c': case 'd': case 'e':
				case 'f': case 'r': bold = false; continue;
				case 'l': bold = true; continue;
				case 'k': case 'm': case 'n': case 'o': continue;
				default: /**/continue; // Apparently, "§x" => ""
				}
			}
			if(ch == '§'){colorPick = true; continue;}
			len += pxLen(ch);
			if(bold){
				if(isHalfPixel(ch)){
					if(!halfPixel) ++len; // Round up
					halfPixel = !halfPixel;
				}
				else ++len;
			}
		}
		return len;// - (halfPixel ? .5 : 0);
	}

	public static double strLenExact(String str, boolean mono) {
		if(mono) {
			return UtilitiesOG.stripFormatting(str).length();
		}
		double len = 0;
		boolean bold = false, colorPick = false;
		for(char ch : str.toCharArray()){
			if(colorPick){
				colorPick = false;
				switch(Character.toLowerCase(ch)){
				case '0': case '1': case '2': case '3': case '4':
				case '5': case '6': case '7': case '8': case '9':
				case 'a': case 'b': case 'c': case 'd': case 'e':
				case 'f': case 'r': case 'x': bold = false; continue;
				case 'l': bold = true; continue;
				case 'k': case 'm': case 'n': case 'o': continue;
				default: /**/continue; // Apparently, "§w" => ""
				}
			}
			if(ch == '§'){colorPick = true; continue;}
			len += pxLen(ch);
			if(bold) len += isHalfPixel(ch) ? .5 : 1;
		}
		return len;
	}

	public static class StrAndPxLen{
		public String str;
		public double pxLen;
		public StrAndPxLen(String s, double l){str = s; pxLen = l;}
	}
	/**
	 * returns substring, in chars(mono) or pixels, ignoring color and format
	 * @param str input string
	 * @param maxLen desired string maximum length
	 * @param mono true if length will be in chars (for console) or false if will be in pixels (for chat area)
	 * @return object array with stripped string [0] and integer length in pixels or chars depending of mono
	 */
	public static StrAndPxLen pxSubstring(String str, double maxLen, boolean mono) {
		if(mono){
			int len = 0, subStrLen = 0;
			for(char ch : str.toCharArray()){
				len += (ch == '§' ? -1 : 1);
				if(len > maxLen) break;
				++subStrLen;
			}
			return new StrAndPxLen(str.substring(0, subStrLen), len);
		}
		else{
			double pxLen = 0, subStrPxLen = 0;
			int subStrLen = 0;
			boolean bold = false, colorPick = false;
			for(char ch : str.toCharArray()){
				if(colorPick){
					colorPick = false;
					subStrLen += 2;
					switch(Character.toLowerCase(ch)){
					case '0': case '1': case '2': case '3': case '4':
					case '5': case '6': case '7': case '8': case '9':
					case 'a': case 'b': case 'c': case 'd': case 'e':
					case 'f': case 'r': case 'x': bold = false; continue;
					case 'l': bold = true; continue;
					case 'k': case 'm': case 'n': case 'o': continue;
					default: /**/continue; // Apparently, "§W" => ""
					}
				}
				if(ch == '§'){colorPick = true; continue;}
				subStrPxLen = pxLen;
				pxLen += TextUtils.pxLen(ch);
				if(bold) pxLen += isHalfPixel(ch) ? .5 : 1;
				if(pxLen > maxLen) break;
				++subStrLen;
			}
			if(subStrLen == str.length()) subStrPxLen =pxLen;
			return new StrAndPxLen(str.substring(0, subStrLen), subStrPxLen);
		}
	}

}