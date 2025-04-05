package org.adsc.adsc_bot.events;

import lombok.NonNull;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.adsc.adsc_bot.utilties.Constants;

public class ReactionEvent extends ListenerAdapter
{
	@Override
	public void onMessageReactionAdd(@NonNull MessageReactionAddEvent event)
	{
		if (isMinecraftOwO(event)) //是minecraft_owo
		{
			GuildRoleMember data = getGuildRoleMember(event);
			if (data.invalid())
				return; //結束

			data.guild.addRoleToMember(data.member, data.minecraftRole).queue(); //增加身分組
			data.member.getUser()
					.openPrivateChannel()
					.flatMap(dm -> dm.sendMessage("恭喜你！現在你可以看到專屬Minecraft伺服器頻道了"))
					.queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
		}
	}

	@Override
	public void onMessageReactionRemove(@NonNull MessageReactionRemoveEvent event)
	{
		if (isMinecraftOwO(event)) //是minecraft_owo
		{
			GuildRoleMember data = getGuildRoleMember(event);
			if (data.invalid()) //沒找到身分組或成員
				return; //結束

			data.guild.removeRoleFromMember(data.member, data.minecraftRole).queue(); //移除身分組
			data.member.getUser()
					.openPrivateChannel()
					.flatMap(dm -> dm.sendMessage("你再也看不到專屬Minecraft伺服器頻道了…"))
					.queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
		}
	}

	private boolean isMinecraftOwO(GenericMessageReactionEvent event)
	{
		return event.getMessageIdLong() == Constants.GET_ROLE_MESSAGE_ID && //必須是公告的那則訊息
				event.getEmoji() instanceof CustomEmoji customEmoji && //必須是custom emoji
				customEmoji.getIdLong() == Constants.MINECRAFT_OWO_EMOJI_ID; //必須是minecraft_owo
	}

	private GuildRoleMember getGuildRoleMember(GenericMessageReactionEvent event)
	{
		Guild guild = event.getGuild();
		return new GuildRoleMember(guild, guild.getRoleById(Constants.MINECRAFT_ROLE_ID), event.getMember());
	}

	private record GuildRoleMember(Guild guild, Role minecraftRole, Member member)
	{
		boolean invalid()
		{
			return minecraftRole == null || member == null;
		}
	}
}