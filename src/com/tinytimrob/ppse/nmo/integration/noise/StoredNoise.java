package com.tinytimrob.ppse.nmo.integration.noise;

import com.google.gson.annotations.Expose;

public class StoredNoise
{
	@Expose
	public String name;
	
	@Expose
	public String description = "";

	@Expose
	public String path;

	@Expose
	public boolean hidden;

	@Expose
	public boolean secret;
}