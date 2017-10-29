package com.tinytimrob.ppse.nmo.integration.cmd;

import java.io.File;
import com.tinytimrob.ppse.nmo.Action;
import com.tinytimrob.ppse.nmo.Integration;
import com.tinytimrob.ppse.nmo.config.NMOConfiguration;

public class IntegrationCommandLine extends Integration
{
	public IntegrationCommandLine()
	{
		super("cmd");
	}

	public static final IntegrationCommandLine INSTANCE = new IntegrationCommandLine();

	@Override
	public boolean isEnabled()
	{
		return NMOConfiguration.INSTANCE.integrations.cmd.enabled;
	}

	@Override
	public void init()
	{
		for (int i = 0; i < NMOConfiguration.INSTANCE.integrations.cmd.commands.length; i++)
		{
			final StoredCommand command = NMOConfiguration.INSTANCE.integrations.cmd.commands[i];
			this.actions.put("/cmd/" + i, new Action()
			{
				@Override
				public void onAction() throws Exception
				{
					Process process = new ProcessBuilder(command.command).directory(new File(command.workingDir)).start();
				}

				@Override
				public String getName()
				{
					return command.name;
				}
				
				public String getDescription()
				{
					return command.description;
				}

				@Override
				public boolean isHiddenFromFrontend()
				{
					return command.hidden;
				}

				@Override
				public boolean isHiddenFromWebUI()
				{
					return command.secret;
				}

				@Override
				public boolean isBlockedFromWebUI()
				{
					return command.secret;
				}
			});
		}
	}

	@Override
	public void update() throws Exception
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void shutdown()
	{
		// TODO Auto-generated method stub
	}
}
