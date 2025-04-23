package org.adsc.adsc_bot.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.regex.Pattern;

public class ClearMessageCommand implements ICommand
{
	public static final String CLEAR_MESSAGE = "clear-message";

	private final Pattern idPattern = Pattern.compile("\\d+");

	@Override
	public void commandProcess(SlashCommandInteractionEvent event)
	{
		int counts = event.getOption("counts", 0, OptionMapping::getAsInt);
		User target = getTarget(event);

		Guild guild = event.getGuild();
		if (guild == null)
		{
			event.reply("請在伺服器中使用！").setEphemeral(true).queue();
			return;
		}

		GuildMessageChannel channel = event.getGuildChannel();

		if (target == null)
		{
			event.reply("刪除 " + counts + " 則訊息中…").setEphemeral(true).queue();
			channel.getIterableHistory()
				.limit(counts)
				.queue(messages -> channel.deleteMessages(messages).queue());
			return;
		}

		event.reply("刪除 " + target.getName() + " 的 " + counts + " 則訊息中…").setEphemeral(true).queue();
		channel.deleteMessages(channel.getIterableHistory()
						.stream()
						.filter(message -> message.getAuthor().equals(target))
						.limit(counts)
						.toList())
				.queue();
	}

	private User getTarget(SlashCommandInteractionEvent event)
	{
		User target = event.getOption("target", OptionMapping::getAsUser);
		if (target != null)
			return target;

		String targetID = event.getOption("target_id", "", OptionMapping::getAsString);
		if (idPattern.matcher(targetID).matches())
			return event.getJDA().retrieveUserById(targetID).complete();

		return null;
	}
}