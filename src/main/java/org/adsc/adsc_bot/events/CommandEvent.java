package org.adsc.adsc_bot.events;

import lombok.NonNull;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.adsc.adsc_bot.commands.ClearMessageCommand;
import org.adsc.adsc_bot.commands.PointsCommand;
import org.adsc.adsc_bot.commands.ICommand;
import org.adsc.adsc_bot.utilties.Constants;

import java.util.HashMap;
import java.util.Map;

public class CommandEvent extends ListenerAdapter
{
	private final Map<String, ICommand> commandMap = new HashMap<>();

	public CommandEvent()
	{
		commandMap.put(PointsCommand.POINTS, new PointsCommand());
		commandMap.put(ClearMessageCommand.CLEAR_MESSAGE, new ClearMessageCommand());

		commandMap.put("shutdown", event ->
		{
			long userID = event.getUser().getIdLong();
			if (userID == Constants.AC_USER_ID || userID == Constants.DARK_NIGHT_USER_ID)
				event.reply("關閉中").queue(hook -> event.getJDA().shutdownNow());
			else
				event.reply("Hey! 你不能這樣做!").queue();
		});
	}

	@Override
	public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent event)
	{
		ICommand command = commandMap.get(event.getName());
		if (command == null)
			return;

		User user = event.getUser();
		if (user.isBot() || user.isSystem())
		{
			event.reply("你不能這樣做！").setEphemeral(true).queue();
			return;
		}

		command.commandProcess(event);
	}
}