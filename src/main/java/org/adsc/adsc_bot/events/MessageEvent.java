package org.adsc.adsc_bot.events;

import lombok.NonNull;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.TimeUnit;

public class MessageEvent extends ListenerAdapter
{
	@Override
	public void onMessageReceived(@NonNull MessageReceivedEvent event)
	{
		Channel channel = event.getChannel();
		if (channel.getIdLong() != 1357058196816003135L)
			return;

		//第歐根尼俱樂部

		Member member = event.getMember();
		if (member == null)
			return;

		Guild guild = event.getGuild();
		if (!guild.getSelfMember().canInteract(member)) //權限不足
			return;

		ThreadChannel strangersRoom = guild.getThreadChannelById(1357061969336864970L);
		if (strangersRoom != null)
		{
			String mention = member.getAsMention();
			strangersRoom.sendMessage(mention + "被ban了。\n" + mention + " was banned.").queue();
		}

		member.ban(1, TimeUnit.HOURS).reason("在" + channel.getName() + "傳訊息").queue();
	}
}