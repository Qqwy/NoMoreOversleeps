package com.tinytimrob.ppse.nmo.integrations;

import com.google.gson.annotations.Expose;
import com.tinytimrob.common.CommonUtils;
import com.tinytimrob.ppse.nmo.MainDialog;
import com.tinytimrob.ppse.nmo.NMOConfiguration;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;

public class IntegrationDiscord extends Integration
{
	public static final IntegrationDiscord INSTANCE = new IntegrationDiscord();
	static JDA jda = null;
	static String lastMessage = "";

	public static class DiscordConfiguration
	{
		@Expose
		public boolean enabled;

		@Expose
		public String authToken = "";
	}

	public IntegrationDiscord()
	{
		super("discord");
	}

	@Override
	public boolean isEnabled()
	{
		return NMOConfiguration.instance.integrations.discord.enabled;
	}

	@Override
	public void init() throws Exception
	{
		if (CommonUtils.isNullOrEmpty(NMOConfiguration.instance.integrations.discord.authToken))
		{
			throw new Exception("You need to specify discord authToken in the configuration file in order for discord integration to work");
		}
		jda = new JDABuilder(AccountType.CLIENT).setToken(NMOConfiguration.instance.integrations.discord.authToken).buildBlocking();
	}

	@Override
	public void update() throws Exception
	{
		if (!lastMessage.equals(MainDialog.scheduleStatusShort))
		{
			lastMessage = MainDialog.scheduleStatusShort;
			jda.getPresence().setGame(Game.of(MainDialog.scheduleStatusShort));
		}
	}

	@Override
	public void shutdown() throws Exception
	{

	}
}
