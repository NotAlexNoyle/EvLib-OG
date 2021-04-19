package net.evmodder.EvLib.extras;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import net.evmodder.EvLib.extras.EntityUtils.CCP;

public class TellrawUtils{
	public enum ClickEvent{// Descriptions below are from https://minecraft.gamepedia.com/Raw_JSON_text_format
		OPEN_URL,// opens value as a URL in the player's default web browser
		OPEN_FILE,// opens the value file on the user's computer
		//NOTE: "open_file" is  used in messages generated by the game (e.g. on taking a  screenshot) and cannot be used in commands or signs.
		RUN_COMMAND,// has value entered in chat as though the player typed it themselves.
					// This can be used to run commands, provided the player has the  required permissions
		CHANGE_PAGE,// can be used only in written books
		SUGGEST_COMMAND,// similar to "run_command" but it cannot be used in a written book, the text appears only in the player's chat
						// input and it is not automatically entered. Unlike insertion, this replaces the existing contents of the chat input
		COPY_TO_CLIPBOARD;// copy the value to the clipboard

		@Override public String toString(){return name().toLowerCase();}
	}
	public enum HoverEvent{
		SHOW_TEXT,// shows raw JSON text
		SHOW_ITEM,// shows the tooltip of an item that can have NBT tags
		SHOW_ENTITY;// shows an entity's name, possibly its type, and its UUID
//		SHOW_ACHIEVEMENT;// shows advancement or statistic
//		//tellraw @a {"text":"test","hoverEvent":{"action":"show_achievement","value":"minecraft:adventure/arbalistic"}}//CURRENT DOESNT WORK

		@Override public String toString(){return name().toLowerCase();}
	}
	public final static class TextClickAction{
		final ClickEvent event;
		final String value;
		public TextClickAction(@Nonnull ClickEvent event, @Nonnull String value){this.event = event; this.value = value;}
		@Override public boolean equals(Object other){
			return other != null && other instanceof TextClickAction
					&& ((TextClickAction)other).event.equals(event) && ((TextClickAction)other).value.equals(value);
		}
	}
	public final static class TextHoverAction{
		final HoverEvent event;
		final String value;
		public TextHoverAction(@Nonnull HoverEvent event, @Nonnull String value){this.event = event; this.value = value;}
		@Override public boolean equals(Object other){
			return other != null && other instanceof TextHoverAction
					&& ((TextHoverAction)other).event.equals(event) && ((TextHoverAction)other).value.equals(value);
		}
	}

	public enum Keybind{
		ATTACK("key.attack"), USE("key.use"),
		FORWARD("key.forward"), BACK("key.back"), LEFT("key.left"), RIGHT("key.right"), JUMP("key.jump"), SNEAK("key.sneak"), SPRINT("key.sprint"),
		OPEN_INVENTORY("key.inventory"), PICK_ITEM("key.pickItem"), DROP("key.drop"), SWAP_HANDS("key.swapHands"), OPEN_ADVANCEMENTS("key.advancements"),
		HOTBAR_1("key.hotbar.1"), HOTBAR_2("key.hotbar.2"), HOTBAR_3("key.hotbar.3"), HOTBAR_4("key.hotbar.4"), HOTBAR_5("key.hotbar.5"),
		HOTBAR_6("key.hotbar.6"), HOTBAR_7("key.hotbar.7"), HOTBAR_8("key.hotbar.8"), HOTBAR_9("key.hotbar.9"),
		CHAT("key.chat"), PLAYERLIST("key.playerlist"), COMMAND("key.command"),
		SCREENSHOT("key.screenshot"), FULLSCREEN("key.fullscreen"),
		TOGGLE_PERSPECTIVE("key.togglePerspective"), SMOOTH_CAMERA("key.smoothCamera"), SPECTATOR_OUTLINES("key.spectatorOutlines"),
		SAVE_TOOLBAR("key.saveToolbarActivator"), LOAD_TOOLBAR("key.loadToolbarActivator"),
		OPTIFINE_ZOOM("of.key.zoom");

		final String toString;
		Keybind(String toString){this.toString = toString;}
		@Override public String toString(){return toString;}
	};

	public enum Format{BOLD, ITALIC, UNDERLINED, STRIKETHROUGH, OBFUSCATED}
	public final static class FormatFlag{
		final Format format;
		final boolean value;
		public FormatFlag(@Nonnull Format format, boolean value){this.format = format; this.value = value;}
		@Override public String toString(){
			return new StringBuilder().append('"').append(format.toString().toLowerCase()).append("\":").append(value).toString();
		}
		@Override public boolean equals(Object other){
			return other != null && other instanceof FormatFlag && ((FormatFlag)other).format.equals(format) && ((FormatFlag)other).value == value;
		}
	}

	// From wiki: Content tags are checked in the order: text, translate, score, selector, keybind, nbt.
	public static abstract class Component{
		final private String insertion; // When the text is shift-clicked by a player, this string is inserted in their chat input.
		final private TextClickAction clickAction;
		final private TextHoverAction hoverAction;
		final private String color;
		final private FormatFlag[] formats;
		final boolean hasProperties;

		String getInsertion(){return insertion;}
		TextClickAction getClickAction(){return clickAction;}
		TextHoverAction getHoverAction(){return hoverAction;}
		String getColor(){return color;}
		FormatFlag[] getFormats(){return formats;}

		private Component(String insertion, TextClickAction clickAction, TextHoverAction hoverAction, String color, FormatFlag... formats){
			this.insertion = insertion; this.clickAction = clickAction; this.hoverAction = hoverAction; this.color = color; this.formats = formats;
			hasProperties = insertion != null || clickAction != null || hoverAction != null || color != null || (formats != null && formats.length > 0);
			if(formats != null && Arrays.stream(formats).map(formatFlag -> formatFlag.format).distinct().count() < formats.length){
				throw new IllegalArgumentException("Multiple FormatFlags in a Component cannot reference the same Format type");
			}
		}
		private Component(){this(/*insertion=*/null, /*clickAction=*/null, /*hoverAction=*/null, /*color=*/null, /*formats=*/null);}

		String getProperties(){
			if(!hasProperties) return "";
			StringBuilder builder = new StringBuilder();
			if(insertion != null) builder.append(",\"insertion\":\"").append(TextUtils.escape(insertion, "\"","\n")).append('"');
			if(color != null) builder.append(",\"color\":\"").append(color).append('"');
			if(formats != null && formats.length > 0) builder.append(',')
									.append(Arrays.stream(formats).map(FormatFlag::toString).collect(Collectors.joining(",")));
			if(clickAction != null) builder.append(",\"clickEvent\":{\"action\":\"").append(clickAction.event)
									.append("\",\"value\":\"").append(TextUtils.escape(clickAction.value, "\"","\n")).append("\"}");
			if(hoverAction != null) builder.append(",\"hoverEvent\":{\"action\":\"").append(hoverAction.event)
									.append("\",\"value\":\"").append(TextUtils.escape(hoverAction.value, "\"","\n")).append("\"}");
			return builder.toString();// Starts with a comma, formerly was builder.substring(1);
		}
		// Returns null if this component could NOT possibly be a Selector matching exactly 1 target
		UUID potentialSingleMatchSelector(){
			if(this instanceof SelectorComponent == false) return null;
			Object selector = ((SelectorComponent)this).selector;
			try{
				UUID uuid = selector instanceof UUID ? (UUID)selector : UUID.fromString(selector.toString());
				if(Bukkit.getEntity(uuid) != null) return uuid;
			}
			catch(IllegalArgumentException ex){};
			try{
				Class<?> clazz = Class.forName("net.evmodder.EvLib.extras.SelectorUtils.Selector");
				@SuppressWarnings("unchecked")
				Collection<Entity> entities = (Collection<Entity>)clazz.getMethod("resolve").invoke(selector);
				entities.removeIf(e -> e == null);
				if(entities.size() == 1) return entities.iterator().next().getUniqueId();
			}
			catch(Exception ex){return UUID.randomUUID();}// assume this matches an unknown single entity
			return null;
		}
		boolean samePropertiesAs(Component other){
			return (getInsertion() == null ? other.getInsertion() == null : getInsertion().equals(other.getInsertion())) &&
					(getClickAction() == null ? other.getClickAction() == null : getClickAction().equals(other.getClickAction())) &&
					(getHoverAction() == null ? other.getHoverAction() == null : getHoverAction().equals(other.getHoverAction())) &&
					(getColor() == null ? other.getColor() == null : getColor().equals(other.getColor())) &&
					(getFormats() == null ? other.getFormats() == null : (other.getFormats() != null && Arrays.equals(getFormats(), other.getFormats()))) &&
					(potentialSingleMatchSelector() == null ? other.potentialSingleMatchSelector() == null
						: potentialSingleMatchSelector().equals(other.potentialSingleMatchSelector()));
		}
		// True if @other doesn't override any of the properties of this component
		boolean noOverridingProperties(@Nonnull Component other){
			return (other.getInsertion() == null || other.getInsertion().equals(getInsertion())) &&
					(other.getClickAction() == null || other.getClickAction().equals(getClickAction())) &&
					(other.getHoverAction() == null || other.getHoverAction().equals(getHoverAction())) &&
					(other.getColor() == null || other.getColor().equals(getColor())) &&
					(other.getFormats() == null || other.getFormats().length == 0 ||
						(getFormats() != null && Arrays.asList(getFormats()).containsAll(Arrays.asList(other.getFormats())))) &&
					(other.potentialSingleMatchSelector() == null || other.potentialSingleMatchSelector().equals(potentialSingleMatchSelector()));
		}

		public abstract String toString();
		public abstract String toPlainText();
	};
	public final static class RawTextComponent extends Component{
		final String text;
		public RawTextComponent(@Nonnull String text){this.text = text;}
		public RawTextComponent(@Nonnull String text, String insert, TextClickAction click, TextHoverAction hover, String color, FormatFlag... formats){
			super(insert, click, hover, color, formats);
			this.text = text;
		}
		public RawTextComponent(@Nonnull String text, @Nonnull TextClickAction click){
			this(text, /*insert=*/null, click, /*hover=*/null, /*color=*/null, /*formats=*/null);
		}
		public RawTextComponent(@Nonnull String text, @Nonnull TextHoverAction hover){
			this(text, /*insert=*/null, /*click=*/null, hover, /*color=*/null, /*formats=*/null);
		}
		//tellraw @a "test"
		//tellraw @a {"text":"test"}
		//tellraw @a {"text":"test","insertion":"hi there"}

		@Override public String toPlainText(){return text;}
		@Override public String toString(){
			String escapedText = TextUtils.escape(text, "\"","\n");
			return !hasProperties
					? new StringBuilder().append('"').append(escapedText).append('"').toString()
					: new StringBuilder("{\"text\":\"").append(escapedText).append('"').append(getProperties()).append('}').toString();
		}
	}
	public final static class TranslationComponent extends Component{
		final String jsonKey;
		final Component[] with; // Used to replace "%s" placeholders in the translation text.
		public TranslationComponent(@Nonnull String jsonKey){this.jsonKey = jsonKey; with = null;}
		public TranslationComponent(@Nonnull String jsonKey, @Nonnull Component... with){this.jsonKey = jsonKey; this.with = with;}
		public TranslationComponent(@Nonnull String jsonKey, Component[] with,
				String insert, TextClickAction click, TextHoverAction hover, String color, FormatFlag... formats){
			super(insert, click, hover, color, formats);
			this.jsonKey = jsonKey;
			this.with = with;
		}
		//tellraw @a {"translate":"multiplayer.player.joined","with":["EvDoc", "unused"]} -> en_us.json: "%s joined the game"

		private static Constructor<?> chatMessageConstructor, chatMessageConstructorWith;
		private static Method chatMessageGetString;
		private boolean callNMS = true;
		@Override public String toPlainText(){
			if(callNMS) try{
				if(chatMessageGetString == null){
					try{
						Server server = Bukkit.getServer();
						String nmsVersion  = server.getClass().getDeclaredMethod("getHandle").invoke(server).getClass().getName().split("\\.")[3];
						Class<?> clazz = Class.forName("net.minecraft.server."+nmsVersion+".ChatMessage");
						chatMessageConstructor = clazz.getConstructor(String.class);
						chatMessageConstructorWith = clazz.getConstructor(String.class, Object[].class);
						chatMessageGetString = clazz.getMethod("getString");
					}
					catch(NoSuchMethodException | SecurityException | ClassNotFoundException e){callNMS = false; throw new InstantiationException();}
				}
				return (String)chatMessageGetString.invoke(with == null
						? chatMessageConstructor.newInstance(jsonKey)
						: chatMessageConstructorWith.newInstance(jsonKey, Arrays.stream(with).map(Component::toPlainText).toArray()));
			}
			catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e){}
			// This is ONLY correct when the key is invalid/unknown to the client!
			return with == null ? jsonKey.replace("%s", "") : String.format(jsonKey, Arrays.stream(with).map(Component::toPlainText).toArray());
		}
		@Override public String toString(){
			StringBuilder builder = new StringBuilder().append("{\"translate\":\"").append(jsonKey).append('"');
			if(with != null && with.length > 0) builder.append(",\"with\":[").append(
					Arrays.stream(with).map(Component::toString).collect(Collectors.joining(","))).append(']');
			return builder.append(getProperties()).append('}').toString();
		}
	}

	final static String tropicalFishLocaleCCP = /*custom.tropical_fish.ccp*/"%s-%s %s";
	final static String tropicalFishLocaleCP = /*custom.tropical_fish.cp*/"%s %s";
	public static Component getLocalizedDisplayName(@Nonnull CCP ccp){
		Integer id = EntityUtils.commonTropicalFishIds.get(ccp);
		if(id != null) return new TranslationComponent("entity.minecraft.tropical_fish.predefined."+id);
		return ccp.bodyColor != ccp.patternColor
			? new TranslationComponent(tropicalFishLocaleCCP, new Component[]{
				new TranslationComponent("color.minecraft."+ccp.bodyColor.name().toLowerCase()),
				new TranslationComponent("color.minecraft."+ccp.patternColor.name().toLowerCase()),
				new TranslationComponent("entity.minecraft.tropical_fish.type."+ccp.pattern.name().toLowerCase())})
			: new TranslationComponent(tropicalFishLocaleCP, new Component[]{
					new TranslationComponent("color.minecraft."+ccp.bodyColor.name().toLowerCase()),
					new TranslationComponent("entity.minecraft.tropical_fish.type."+ccp.pattern.name().toLowerCase())});
	}
	public static Component getLocalizedDisplayName(@Nonnull Entity entity, boolean useDisplayName){
		if(entity.getName() != null) return new RawTextComponent(
				(entity instanceof Player && useDisplayName) ? ((Player)entity).getDisplayName() : entity.getName());
		switch(entity.getType()){
			case VILLAGER:
				return new TranslationComponent("entity.minecraft."+entity.getType().name().toLowerCase()+"."
						+((Villager)entity).getProfession().name().toLowerCase());
			case TROPICAL_FISH:
				return getLocalizedDisplayName(EntityUtils.getCCP((TropicalFish)entity));
			default:
				return new TranslationComponent("entity.minecraft."+entity.getType().name().toLowerCase());
		}
	}
	static String getVanillaPotionEffectTypeName(@Nonnull PotionEffectType type){
		switch(type.getName()){
			case "AWKWARD": return "awkward";
			case "FIRE_RESISTANCE": return "fire_resistance";
			case "INSTANT_DAMAGE": return "harming";
			case "INSTANT_HEAL": return "healing";
			case "INVISIBILITY": return "invisibility";
			case "JUMP": return "leaping";
			case "LEVITATION": return "levitation";
			case "LUCK": return "luck";
			case "MUNDANE": return "mundane";
			case "NIGHT_VISION": return "night_vision";
			case "POISON": return "poison";
			case "REGEN": return "regeneration";
			case "SLOWNESS": return "slowness";
			case "SLOW_FALLING": return "slow_falling";
			case "SPEED": return "swiftness";
			case "STRENGTH": return "strength";
			case "THICK": return "thick";
			case "TURTLE_MASTER": return "turtle_master";
			case "WATER": return "water";
			case "WATER_BREATHING": return "water_breathing";
			case "WEAKNESS": return "weakness";
			case "UNCRAFTABLE": default: return "empty";
		}
	}
	public static Component getLocalizedDisplayName(@Nonnull BlockState block){
		switch(block.getType()){
			case PLAYER_HEAD:
			case PLAYER_WALL_HEAD:
				return new TranslationComponent("block.minecraft.player_head.named",
						new RawTextComponent(HeadUtils.getGameProfile((Skull)block).getName()));
			case BLACK_BANNER:
			case BLUE_BANNER:
			case BROWN_BANNER:
			case CYAN_BANNER:
			case GRAY_BANNER:
			case GREEN_BANNER:
			case LIGHT_BLUE_BANNER:
			case LIGHT_GRAY_BANNER:
			case LIME_BANNER:
			case MAGENTA_BANNER:
			case ORANGE_BANNER:
			case PINK_BANNER:
			case PURPLE_BANNER:
			case RED_BANNER:
			case WHITE_BANNER:
			case YELLOW_BANNER:
				return new TranslationComponent("block.minecraft.banner"+((Banner)block).getPattern(0).getPattern().name().toLowerCase()
						+"."+((Banner)block).getBaseColor().name().toLowerCase());
			default:
				return new TranslationComponent("block.minecraft."+block.getType().name().toLowerCase()); 
		}
	}
	public static Component getLocalizedDisplayName(@Nonnull ItemStack item){
		if(item.hasItemMeta() && item.getItemMeta().hasDisplayName()) return new RawTextComponent(item.getItemMeta().getDisplayName());
		if(item.getType().isBlock()){
			if(item.hasItemMeta()) return getLocalizedDisplayName(((BlockStateMeta)item.getItemMeta()).getBlockState());
			return new TranslationComponent("block.minecraft."+item.getType().name().toLowerCase()); 
		}
		switch(item.getType()){
			case POTION:
			case SPLASH_POTION:
			case LINGERING_POTION:
			case TIPPED_ARROW:
				return new TranslationComponent("item.minecraft."+item.getType().name().toLowerCase()+".effect."+(item.hasItemMeta()
								? getVanillaPotionEffectTypeName(((PotionMeta)item.getItemMeta()).getBasePotionData().getType().getEffectType())
								: "empty"
							));
			case SHIELD:
				if(item.hasItemMeta()) return new TranslationComponent("item.minecraft.shield."
						+((Banner)((BlockStateMeta)item.getItemMeta()).getBlockState()).getBaseColor().name().toLowerCase());
			default:
				return new TranslationComponent("item.minecraft."+item.getType().name().toLowerCase()); 
		}
	}

	public final static class ScoreComponent extends Component{
		final Object selector;
		final Objective objective;
		String value; // Optional; overwrites output of score selector
		public ScoreComponent(@Nonnull Object selector, @Nonnull Objective objective){this.selector = selector; this.objective = objective;}
		public ScoreComponent(@Nonnull String name, @Nonnull Objective objective){this.selector = name; this.objective = objective;}
		public ScoreComponent(@Nonnull Object selector, @Nonnull Objective objective,
				String insert, TextClickAction click, TextHoverAction hover, String color, FormatFlag... formats){
			super(insert, click, hover, color, formats);
			this.selector = selector;
			this.objective = objective;
		}
		//tellraw @a {"score":{"name":"@p","objective":"levels","value":"3333"}}

		@Override public String toPlainText(){
			String name = null;
			try{
				UUID uuid = UUID.fromString(selector.toString());
				name = Bukkit.getEntity(uuid).getName();
			}
			catch(IllegalArgumentException ex){};
			try{
				Class<?> clazz = Class.forName("net.evmodder.EvLib.extras.SelectorUtils.Selector");
				@SuppressWarnings("unchecked")
				Collection<Entity> entities = (Collection<Entity>)clazz.getMethod("resolve").invoke(selector);
				if(entities.size() > 1) return "ERROR: more than 1 entity matched with score selector!";
				if(entities.isEmpty()) return "";
				name = entities.iterator().next().getName();
			}
			catch(Exception ex){name = selector.toString();}
			if(name == null) return "";
			return ""+objective.getScore(name).getScore();
		}
		@Override public String toString(){
			StringBuilder builder = new StringBuilder().append("\"score\":{\"name\":\"")
					.append(selector.toString()).append("\",\"objective\":\"").append(objective.getName()).append('"');
			if(value != null) builder.append(",\"value\":\"").append(TextUtils.escape(value, "\"","\n")).append('"');
			return builder.append('}').append(getProperties()).append('}').toString();
		}
	}
	public final static class SelectorComponent extends Component{
//		final Selector selector;
		final Object selector;
		final boolean useDisplayName;
		public SelectorComponent(@Nonnull Object selector){this.selector = selector; this.useDisplayName = true;}
		public SelectorComponent(@Nonnull UUID uuid){this.selector = uuid; this.useDisplayName = true;}
		public SelectorComponent(@Nonnull Object selector, boolean useDisplayName){this.selector = selector; this.useDisplayName = useDisplayName;}
		public SelectorComponent(@Nonnull UUID uuid, boolean useDisplayName){this.selector = uuid; this.useDisplayName = useDisplayName;}
//		public SelectorComponent(@Nonnull SelectorType type, @Nonnull SelectorArgument...arguments){this.selector = new Selector(type, arguments);}
		//tellraw @a {"selector":"@a"}

		@Override public String toPlainText(){
			Collection<String> names = null;
			try{
				UUID uuid = selector instanceof UUID ? (UUID)selector : UUID.fromString(selector.toString());
				Entity entity = Bukkit.getEntity(uuid);
				if(entity != null) names = Arrays.asList(getLocalizedDisplayName(entity, useDisplayName).toPlainText());
			}
			catch(IllegalArgumentException ex){};
			if(names == null) try{
				Class<?> clazz = Class.forName("net.evmodder.EvLib.extras.SelectorUtils.Selector");
				@SuppressWarnings("unchecked")
				Collection<Entity> entities = (Collection<Entity>)clazz.getMethod("resolve").invoke(selector);
				names = entities.stream().filter(e -> e != null)
						.map(e -> getLocalizedDisplayName(e, useDisplayName).toPlainText()).collect(Collectors.toList());
			}
			catch(Exception ex){names = Arrays.asList(selector.toString());}
			return String.join(ChatColor.GRAY+", "+ChatColor.RESET, names);
		}
		@Override public String toString(){
			return new StringBuilder().append("{\"selector\":\"").append(TextUtils.escape(selector.toString(), "\"","\n"))
					.append(getProperties()).append("\"}").toString();
		}
	}
	public final static class KeybindComponent extends Component{
		final Keybind keybind;
		public KeybindComponent(@Nonnull Keybind keybind){this.keybind = keybind;}
		//tellraw @a {"keybind":"of.key.zoom"}
		@Override public String toPlainText(){return keybind.toString();}//TODO: KEY SETTING NAME HERE if possible?
		@Override public String toString(){
			return new StringBuilder().append("{\"keybind\":\"").append(keybind).append('"').append(getProperties()).append('}').toString();
		}
	}

	public final static class ListComponent extends Component{
		@Override String getInsertion(){return components.isEmpty() ? null : components.get(0).getInsertion();}
		@Override TextClickAction getClickAction(){return components.isEmpty() ? null : components.get(0).getClickAction();}
		@Override TextHoverAction getHoverAction(){return components.isEmpty() ? null : components.get(0).getHoverAction();}
		@Override String getColor(){return components.isEmpty() ? null : components.get(0).getColor();}
		@Override FormatFlag[] getFormats(){return components.isEmpty() ? null : components.get(0).getFormats();}
		Component last = null;
		List<Component> components;
		public ListComponent(@Nonnull Component...components){
			this.components = new ArrayList<>();
			for(Component comp : components) addComponent(comp);
		}
		public boolean isEmpty(){return components.isEmpty();}

		private RawTextComponent copyWithNewText(@Nonnull RawTextComponent comp, @Nonnull String text){
			return new RawTextComponent(text, comp.getInsertion(), comp.getClickAction(), comp.getHoverAction(), comp.getColor(), comp.getFormats());
		}
		public boolean addComponent(@Nonnull Component component){
			if(component.toPlainText().isEmpty()) return false;
			if(component instanceof RawTextComponent){
				if(ChatColor.stripColor(component.toPlainText()).isEmpty()) return false;
				if(last != null && last instanceof RawTextComponent && last.noOverridingProperties(component)){
					components.remove(components.size()-1);
					components.add(last = copyWithNewText((RawTextComponent)last, last.toPlainText() + component.toPlainText()));
					return true;
				}
			}
			if(component instanceof ListComponent){
				// We can safely flatten nested TellrawBlobs IFF they don't override any of the parent's "group properties"
				if(((ListComponent)component).components.size() <= 1 || noOverridingProperties(component)){
					for(Component comp : ((ListComponent)component).components) addComponent(comp);
					return true;
				}
			}
			return components.add(last = component);
		}
		public void addComponent(@Nonnull String text){addComponent(new RawTextComponent(text));}

		/**
		 * Loops through all RawTextComponents in this instance and replaces all occurances of @textToReplace with the @replacement component
		 * @param textToReplace The simple text from inside a RawTextComponent to search for
		 * @param replacement The component substituted in place of each instance of matching text
		 * @return true if one or more replacements occurred
		 */
		public boolean replaceRawDisplayTextWithComponent(@Nonnull final String textToReplace, @Nonnull final Component replacement){
			if(textToReplace.isEmpty()) return false;
			boolean updated = false;
			for(int i=0; i<components.size(); ++i){
				Component comp = components.get(i);
				if(comp instanceof ListComponent){
					if(((ListComponent)comp).replaceRawDisplayTextWithComponent(textToReplace, replacement)) updated = true;
				}
				if(comp instanceof RawTextComponent == false) continue;
				RawTextComponent txComp = (RawTextComponent) comp;
				final String text = txComp.toPlainText();
				if(text.contains(textToReplace) == false) continue;
				boolean replacementIsRawText = replacement instanceof RawTextComponent;

				if(replacementIsRawText && txComp.noOverridingProperties(replacement)){
					components.set(i, copyWithNewText(txComp, text.replace(textToReplace, ((RawTextComponent)replacement).toPlainText())));
					continue;
				}
				int matchIdx = text.indexOf(textToReplace);
				String textBefore = text.substring(0, matchIdx);
				String textAfter = text.substring(matchIdx+textToReplace.length());
				boolean emptyBefore = (replacementIsRawText ? ChatColor.stripColor(textBefore) : textBefore).isEmpty();
				boolean emptyAfter = (replacementIsRawText ? ChatColor.stripColor(textAfter) : textAfter).isEmpty();
				// Necessary to prevent overriding this ListComponent's group properties
				if(i == 0 && emptyBefore && !this.samePropertiesAs(replacement)){components.add(0, copyWithNewText(txComp, "")); i = 1;}

				Component replacementInst = replacement;
				if(replacementIsRawText){
					String replacementText = replacement.toPlainText();
					if(emptyBefore) replacementInst = copyWithNewText((RawTextComponent)replacement, textBefore + replacementText);
					if(emptyAfter) replacementInst = copyWithNewText((RawTextComponent)replacement, replacementText + textAfter);
				}
				if(emptyBefore && emptyAfter){
					if(i == components.size()-1) last = replacementInst;
					components.set(i, replacementInst);
				}
				else if(emptyBefore){
					components.set(i, copyWithNewText(txComp, textAfter));
					components.add(i, replacementInst);
				}
				else if(emptyAfter){
					components.set(i, new RawTextComponent(textBefore));
					if(++i == components.size()) components.add(last = replacementInst);
					else components.add(i, replacementInst);
				}
				else{
					components.set(i, copyWithNewText(txComp, textBefore));
					RawTextComponent textAfterComp = copyWithNewText(txComp, textAfter);
					if(++i == components.size()){components.add(replacementInst); components.add(last = textAfterComp);}
					else{components.add(i, textAfterComp); components.add(i, replacementInst);}
				}
				updated = true;
			}
			return updated;
		}

		@Override public String toPlainText(){
			StringBuilder builder = new StringBuilder();
			for(Component comp : components) builder.append(comp.toPlainText());
			return builder.toString();
		}
		@Override public String toString(){
			while(last instanceof RawTextComponent && ChatColor.stripColor(last.toPlainText()).isEmpty()){
				components.remove(components.size()-1);
				last = components.isEmpty() ? null : components.get(components.size()-1);
			}
			switch(components.size()){
				case 0: return "\"\"";
				case 1: return components.get(0).toString();
				default: return new StringBuilder().append('[').append(
							components.stream().map(Component::toString).collect(Collectors.joining(","))
						).append(']').toString();
			}
		}
	}
}