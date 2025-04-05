package org.adsc.adsc_bot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.HashMap;
import java.util.Map;

public interface ICommand
{
	void commandProcess(SlashCommandInteractionEvent event);
}

class HasSubcommands implements ICommand
{
	protected final Map<String, ICommand> subcommands; //子指令們

	protected HasSubcommands(int subcommandsCount)
	{
		subcommands = HashMap.newHashMap(subcommandsCount);
	}

	@Override
	public void commandProcess(SlashCommandInteractionEvent event)
	{
		subcommands.get(event.getSubcommandName()).commandProcess(event); //透過HashMap選擇子指令
	}
}