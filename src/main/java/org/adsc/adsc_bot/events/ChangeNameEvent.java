package org.adsc.adsc_bot.events;

import lombok.NonNull;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.adsc.adsc_bot.utilties.GuildPointsHandle;

public class ChangeNameEvent extends ListenerAdapter
{
	@Override
	public void onUserUpdateName(@NonNull UserUpdateNameEvent event)
	{
		User user = event.getUser();
		if (user.isBot() || user.isSystem())
			return;
		//更新名字
		GuildPointsHandle.getPointsData(user.getIdLong()).setName(user.getEffectiveName());
	}
}