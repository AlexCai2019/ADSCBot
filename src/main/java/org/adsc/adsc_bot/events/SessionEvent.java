package org.adsc.adsc_bot.events;

import lombok.NonNull;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.adsc.adsc_bot.utilties.GuildPointsHandle;

public class SessionEvent extends ListenerAdapter
{
	@Override
	public void onReady(@NonNull ReadyEvent event)
	{
		GuildPointsHandle.initialize();
	}

	@Override
	public void onShutdown(@NonNull ShutdownEvent event)
	{
		GuildPointsHandle.writeToDatabase();
	}
}