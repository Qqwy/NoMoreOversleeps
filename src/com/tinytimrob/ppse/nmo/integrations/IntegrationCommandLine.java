package com.tinytimrob.ppse.nmo.integrations;

import java.io.File;
import com.google.gson.annotations.Expose;
import com.tinytimrob.ppse.nmo.Action;
import com.tinytimrob.ppse.nmo.NMOConfiguration;

public class IntegrationCommandLine extends Integration
{
	public static class StoredCommand
	{
		@Expose
		public String name;

		@Expose
		public String[] command;

		@Expose
		public String workingDir;

		@Expose
		public boolean secret;
	}

	public static class CommandLineConfiguration
	{
		@Expose
		public boolean enabled;

		@Expose
		public StoredCommand[] commands = new StoredCommand[0];
	}

	@Override
	public boolean isEnabled()
	{
		return NMOConfiguration.instance.integrations.cmd.enabled;
	}

	@Override
	public void init()
	{
		for (int i = 0; i < NMOConfiguration.instance.integrations.cmd.commands.length; i++)
		{
			final StoredCommand command = NMOConfiguration.instance.integrations.cmd.commands[i];
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

				@Override
				public boolean isSecret()
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
