package com.playmonumenta.scriptedquests.commands;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Random;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import com.playmonumenta.scriptedquests.utils.InventoryUtils;
import com.playmonumenta.scriptedquests.utils.MessagingUtils;

public class GiveLootTable {
	@SuppressWarnings("unchecked")
	public static void register(Random random) {
		CommandPermission perms = CommandPermission.fromString("scriptedquests.giveloottable");
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();

		arguments.put("players", new EntitySelectorArgument(EntitySelectorArgument.EntitySelector.MANY_PLAYERS));
		arguments.put("lootTablePath", new TextArgument());

		new CommandAPICommand("giveloottable")
			.withPermission(perms)
			.withArguments(arguments)
			.executes((sender, args) -> {
				giveLoot((Collection<Player>)args[0], (String)args[1], 1, random);
			})
			.register();

		arguments.put("count", new IntegerArgument(1, 1000));

		new CommandAPICommand("giveloottable")
			.withPermission(perms)
			.withArguments(arguments)
			.executes((sender, args) -> {
				giveLoot((Collection<Player>)args[0], (String)args[1], (Integer)args[2], random);
			})
			.register();

		/* Rather than take a specified count, take an item entity - and give loot from the loot table once for each item in the stack */
		arguments.clear();
		arguments.put("players", new EntitySelectorArgument(EntitySelectorArgument.EntitySelector.MANY_PLAYERS));
		arguments.put("lootTablePath", new TextArgument());
		arguments.put("countItems", new EntitySelectorArgument(EntitySelectorArgument.EntitySelector.ONE_ENTITY));

		new CommandAPICommand("giveloottable")
			.withPermission(perms)
			.withArguments(arguments)
			.executes((sender, args) -> {
				giveLootCountFromItemEntity((Collection<Player>)args[0], (String)args[1], (Entity)args[2], random);
			})
			.register();
	}

	private static void giveLootCountFromItemEntity(Collection<Player> players, String lootPath, Entity entity, Random random) {
		if ((entity instanceof Item) && ((Item)entity).getItemStack() != null) {
			giveLoot(players, lootPath, ((Item)entity).getItemStack().getAmount(), random);
		} else {
			for (Player player : players) {
				player.sendMessage(ChatColor.RED + "BUG! Server tried to give you loot based on an entity that wasn't an item from the table '" + lootPath + "'");
				player.sendMessage(ChatColor.RED + "Please take a screenshot and report this bug");
			}
		}
	}

	private static void giveLoot(Collection<Player> players, String lootPath, int count, Random random) {
		boolean alreadyDone = false;
		for (Player player : players) {
			try {
				for (int i = 0; i < count; i++) {
					alreadyDone = InventoryUtils.giveLootTableContents(player, lootPath, random, alreadyDone);
				}
			} catch (Exception e) {
				player.sendMessage(ChatColor.RED + "BUG! Server failed to give you loot from the table '" + lootPath + "'");
				player.sendMessage(ChatColor.RED + "Please hover over the following message, take a screenshot, and report this to a moderator");
				MessagingUtils.sendStackTrace(player, e);
			}
		}
	}
}
