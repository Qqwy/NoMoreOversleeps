package com.tinytimrob.ppse.nmo.integration.webui;

import com.tinytimrob.common.CommonUtils;
import com.tinytimrob.ppse.nmo.Integration;
import com.tinytimrob.ppse.nmo.config.NMOConfiguration;

public class IntegrationWebUI extends Integration
{
	public static final IntegrationWebUI INSTANCE = new IntegrationWebUI();

	private IntegrationWebUI()
	{
		super("webUI");
	}

	@Override
	public boolean isEnabled()
	{
		return NMOConfiguration.instance.integrations.webUI.enabled;
	}

	@Override
	public void init() throws Exception
	{
		if (CommonUtils.isNullOrEmpty(NMOConfiguration.instance.integrations.webUI.webcamSecurityKey))
		{
			NMOConfiguration.instance.integrations.webUI.webcamSecurityKey = CommonUtils.generateAsciiCryptoKey(64);
			NMOConfiguration.save();
		}
		WebcamCapture.init();
		WebServer.initialize();
	}

	@Override
	public void update() throws Exception
	{
		WebcamCapture.update();
	}

	@Override
	public void shutdown() throws Exception
	{
		WebServer.shutdown();
		WebcamCapture.shutdown();
	}
}
