package com.playmonumenta.scriptedquests.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.playmonumenta.scriptedquests.Plugin;

import org.bukkit.Bukkit;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.FunctionArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.wrappers.FunctionWrapper;

public class ScheduleFunction {
	private class DelayedFunction {
		protected int mTicksLeft;
		private FunctionWrapper[] mFunction;

		protected DelayedFunction(int ticksLeftIn, FunctionWrapper[] functionIn) {
			mTicksLeft = ticksLeftIn;
			mFunction = functionIn;
		}

		protected void run() {
			for (FunctionWrapper func : mFunction) {
				func.run();
			}
		}
	}

	private final List<DelayedFunction> mFunctions = new ArrayList<DelayedFunction>();
	private final Plugin mPlugin;
	private Integer mTaskId = null;
	private final List<DelayedFunction> mFunctionsToRun = new ArrayList<DelayedFunction>();
	private final Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			Iterator<DelayedFunction> it = mFunctions.iterator();
			while (it.hasNext()) {
				DelayedFunction entry = it.next();
				entry.mTicksLeft--;

				if (entry.mTicksLeft < 0) {
					mFunctionsToRun.add(entry);

					it.remove();
				}
			}

			for (DelayedFunction entry : mFunctionsToRun) {
				entry.run();
			}
			mFunctionsToRun.clear();

			if (mFunctions.isEmpty()) {
				Bukkit.getScheduler().cancelTask(mTaskId);
				mTaskId = null;
			}
		}
	};

	public ScheduleFunction(Plugin plugin) {
		mPlugin = plugin;
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();

		arguments.put("literal", new LiteralArgument("function"));
		arguments.put("function", new FunctionArgument());
		arguments.put("ticks", new IntegerArgument(0));

		/* Unregister the default /schedule command */
		try {
			CommandAPI.unregister("schedule");
		} catch (Exception e) {
			// Nothing to do here - there is nothing to unregister in 1.13
			plugin.getLogger().info("Failed to unregister /schedule - this is only an error in 1.14+");
		}

		new CommandAPICommand("schedule")
			.withPermission(CommandPermission.fromString("scriptedquests.schedulefunction"))
			.withArguments(arguments)
			.executes((sender, args) -> {
				addDelayedFunction((FunctionWrapper[])args[0], (Integer)args[1]);
			})
			.register();

		/* TODO: Add the other /schedule variants (clear and replace/append variants) */
	}

	private void addDelayedFunction(FunctionWrapper[] function, int ticks) {
		mFunctions.add(new DelayedFunction(ticks, function));

		if (mTaskId == null) {
			mTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(mPlugin, mRunnable, 0, 1);
		}
	}

	/* Run all the remaining commands now, even though they are scheduled for later */
	public void cancel() {
		for (DelayedFunction entry : mFunctions) {
			entry.run();
		}
		mFunctions.clear();

		if (mTaskId != null) {
			Bukkit.getScheduler().cancelTask(mTaskId);
		}
	}
}
