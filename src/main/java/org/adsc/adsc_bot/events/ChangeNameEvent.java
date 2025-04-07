package org.adsc.adsc_bot.events;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.adsc.adsc_bot.utilties.GuildPointsHandle;
import org.jetbrains.annotations.NotNull;

public class ChangeNameEvent extends ListenerAdapter
{
	@Override
	public void onUserUpdateName(@NotNull UserUpdateNameEvent event)
	{
		User user = event.getUser();
		if (user.isBot() || user.isSystem())
			return;
		//更新名字
		GuildPointsHandle.getPointsData(user.getIdLong()).setName(user.getEffectiveName());
	}
}