package com.tinytimrob.ppse.nmo.integration.discord;

import com.google.gson.annotations.Expose;

public class SendableMessage
{
	@Expose
	public String name;
	
	@Expose
	public String description;

	@Expose
	public DiscordTargetType targetType;

	@Expose
	public long targetID;

	@Expose
	public String message;
}