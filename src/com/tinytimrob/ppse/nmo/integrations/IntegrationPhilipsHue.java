package com.tinytimrob.ppse.nmo.integrations;

import java.util.List;
import org.apache.logging.log4j.Logger;
import com.google.gson.annotations.Expose;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHHueParsingError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.tinytimrob.common.Configuration;
import com.tinytimrob.common.LogWrapper;
import com.tinytimrob.common.PlatformData;
import com.tinytimrob.ppse.nmo.Action;
import com.tinytimrob.ppse.nmo.NMOConfiguration;

public class IntegrationPhilipsHue extends Integration
{
	public static IntegrationPhilipsHue INSTANCE = new IntegrationPhilipsHue();
	private static final Logger log = LogWrapper.getLogger();
	public PHHueSDK sdk;
	public PHBridge activeBridge;
	public PHSDKListener listener;
	public volatile int lightState = -1;

	public static class PhilipsHueConfiguration
	{
		@Expose
		public boolean enabled;

		@Expose
		public String bridgeIP = "";

		@Expose
		public String bridgeUsername = "";
	}

	@Override
	public boolean isEnabled()
	{
		return NMOConfiguration.instance.integrations.philipsHue.enabled;
	}

	@Override
	public void init()
	{
		this.actions.put("/philipshue/on", new Action()
		{
			@Override
			public void onAction() throws Exception
			{
				IntegrationPhilipsHue.this.toggle(true);
			}

			@Override
			public String getName()
			{
				return "LIGHT ON";
			}

			@Override
			public boolean isSecret()
			{
				return false;
			}
		});
		this.actions.put("/philipshue/off", new Action()
		{
			@Override
			public void onAction() throws Exception
			{
				IntegrationPhilipsHue.this.toggle(false);
			}

			@Override
			public String getName()
			{
				return "LIGHT OFF";
			}

			@Override
			public boolean isSecret()
			{
				return false;
			}
		});

		this.sdk = PHHueSDK.getInstance();
		this.sdk.setAppName("NoMoreOversleeps");
		this.sdk.setDeviceName(PlatformData.computerName);
		this.sdk.getNotificationManager().registerSDKListener(this.listener = new PHSDKListener()
		{
			@Override
			public void onParsingErrors(List<PHHueParsingError> arg0)
			{
				// TODO
			}

			@Override
			public void onError(int code, String message)
			{
				log.info("Hue SDK error " + code + ": " + message);
				if (code == PHHueError.BRIDGE_NOT_RESPONDING)
				{
					PHBridgeSearchManager sm = (PHBridgeSearchManager) IntegrationPhilipsHue.this.sdk.getSDKService(PHHueSDK.SEARCH_BRIDGE);
					sm.search(true, true);
				}
			}

			@Override
			public void onConnectionResumed(PHBridge bridge)
			{
				// Don't do anything. This happens so frequently that printing anything causes massive log spam.
			}

			@Override
			public void onConnectionLost(PHAccessPoint accessPoint)
			{
				log.info("Connection to Hue Bridge at " + accessPoint.getIpAddress() + " has been lost");
			}

			@Override
			public void onCacheUpdated(List<Integer> cacheNotificationsList, PHBridge bridge)
			{
				if (cacheNotificationsList.contains(PHMessageType.LIGHTS_CACHE_UPDATED))
				{
					List<PHLight> u = bridge.getResourceCache().getAllLights();
					if (!u.isEmpty())
					{
						PHLightState phls = u.get(0).getLastKnownLightState();
						IntegrationPhilipsHue.this.lightState = phls.isOn() ? phls.getBrightness() : -1;
					}
				}
			}

			@Override
			public void onBridgeConnected(PHBridge bridge, String username)
			{
				log.info("Connection to Hue Bridge at " + NMOConfiguration.instance.integrations.philipsHue.bridgeIP + " has been established");
				log.info("Bridge API authorization username: " + username);
				IntegrationPhilipsHue.this.sdk.setSelectedBridge(bridge);
				IntegrationPhilipsHue.this.sdk.enableHeartbeat(bridge, 1000);
				IntegrationPhilipsHue.this.activeBridge = bridge;
				NMOConfiguration.instance.integrations.philipsHue.bridgeUsername = username;
				try
				{
					Configuration.save();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				List<PHLight> u = bridge.getResourceCache().getAllLights();
				if (!u.isEmpty())
				{
					PHLightState phls = u.get(0).getLastKnownLightState();
					IntegrationPhilipsHue.this.lightState = phls.isOn() ? phls.getBrightness() : -1;
				}
			}

			@Override
			public void onAuthenticationRequired(PHAccessPoint accessPoint)
			{
				log.info("Authentication required. Please push the authentication button on the Hue Bridge!");
				NMOConfiguration.instance.integrations.philipsHue.bridgeIP = accessPoint.getIpAddress();
				IntegrationPhilipsHue.this.sdk.startPushlinkAuthentication(accessPoint);
			}

			@Override
			public void onAccessPointsFound(List<PHAccessPoint> accessPointList)
			{
				log.info(accessPointList.size() + " access points found");
				if (!accessPointList.isEmpty())
				{
					PHAccessPoint accessPoint = accessPointList.get(0);
					log.info("Attempting connection to " + accessPoint.getIpAddress());
					IntegrationPhilipsHue.this.sdk.connect(accessPoint);
				}
			}
		});

		log.info("Attempting to reconnect to Hue Bridge...");
		PHAccessPoint accessPoint = new PHAccessPoint();
		accessPoint.setIpAddress(NMOConfiguration.instance.integrations.philipsHue.bridgeIP);
		accessPoint.setUsername(NMOConfiguration.instance.integrations.philipsHue.bridgeUsername);
		this.sdk.connect(accessPoint);
	}

	@Override
	public void update() throws Exception
	{
		// TODO Auto-generated method stub
	}

	public void toggle(boolean state)
	{
		PHLightState lightState = new PHLightState();
		lightState.setOn(state);
		if (state)
		{
			lightState.setBrightness(Integer.MAX_VALUE, true);
		}
		this.activeBridge.setLightStateForDefaultGroup(lightState);
	}

	@Override
	public void shutdown()
	{
		if (this.sdk != null)
		{
			this.sdk.disableAllHeartbeat();
			if (this.activeBridge != null)
			{
				this.sdk.disconnect(this.activeBridge);
				this.activeBridge = null;
			}
			this.sdk.destroySDK();
			this.sdk = null;
		}
	}
}
