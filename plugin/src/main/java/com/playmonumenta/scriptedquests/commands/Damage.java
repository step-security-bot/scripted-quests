package com.playmonumenta.scriptedquests.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import org.bukkit.entity.Damageable;

import java.util.Collection;
import java.util.LinkedHashMap;

public class Damage {
	public static void register() {
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();

		arguments.put("entities", new EntitySelectorArgument(EntitySelectorArgument.EntitySelector.MANY_ENTITIES));
		arguments.put("amount", new DoubleArgument());
		arguments.put("respect_armor", new BooleanArgument());

		new CommandAPICommand("damage")
			.withPermission(CommandPermission.fromString("scriptedquests.damage"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				for (Object e : (Collection<?>)args[0]) {
					if (e instanceof Damageable) {
						Double amount = (Double)args[1];
						Damageable entity = (Damageable)e;
						if (!((boolean) args[2])) {
							if (entity.getHealth() - amount <= 0.0) {
								entity.setHealth(0);
							} else {
								entity.setHealth(entity.getHealth() - amount);
							}
							entity.damage(0); //fake damage animation
						} else {
							entity.damage(amount);
						}
					}
				}
			})
			.register();

	}
}
