package com.tinytimrob.ppse.nmo.integration.philipshue;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.logging.log4j.Logger;
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
import com.tinytimrob.common.LogWrapper;
import com.tinytimrob.common.PlatformData;
import com.tinytimrob.ppse.nmo.Action;
import com.tinytimrob.ppse.nmo.Integration;
import com.tinytimrob.ppse.nmo.config.NMOConfiguration;

public class IntegrationPhilipsHue extends Integration
{
	public IntegrationPhilipsHue()
	{
		super("philipsHue");
	}

	public static final IntegrationPhilipsHue INSTANCE = new IntegrationPhilipsHue();
	private static final Logger log = LogWrapper.getLogger();
	public PHHueSDK sdk;
	public PHBridge activeBridge;
	public PHSDKListener listener;
	//public volatile int lightState = -1;
	public volatile LinkedHashMap<String, Integer> lightStates = new LinkedHashMap<String, Integer>();
	public volatile LinkedHashMap<String, PHLight> lights = new LinkedHashMap<String, PHLight>();

	@Override
	public boolean isEnabled()
	{
		return NMOConfiguration.INSTANCE.integrations.philipsHue.enabled;
	}

	@Override
	public void init()
	{
		for (int i = 0; i < NMOConfiguration.INSTANCE.integrations.philipsHue.lights.length; i++)
		{
			final String bulbName = NMOConfiguration.INSTANCE.integrations.philipsHue.lights[i];
			this.lightStates.put(bulbName, -1);
			this.actions.put("/philipshue/" + i + "/on", new Action()
			{
				@Override
				public void onAction() throws Exception
				{
					IntegrationPhilipsHue.this.toggle(bulbName, true);
				}

				@Override
				public String getName()
				{
					return "TURN ON " + bulbName;
				}
				
				
				@Override
				public String getDescription()
				{
					return "Will turn the light called" + bulbName + " on.";
				}

				@Override
				public boolean isHiddenFromFrontend()
				{
					return false;
				}

				@Override
				public boolean isHiddenFromWebUI()
				{
					return false;
				}

				@Override
				public boolean isBlockedFromWebUI()
				{
					return false;
				}
			});
			this.actions.put("/philipshue/" + i + "/off", new Action()
			{
				@Override
				public void onAction() throws Exception
				{
					IntegrationPhilipsHue.this.toggle(bulbName, false);
				}

				@Override
				public String getName()
				{
					return "TURN OFF " + bulbName;
				}
				
				@Override
				public String getDescription()
				{
					return "Will turn the light called" + bulbName + " off.";
				}

				@Override
				public boolean isHiddenFromFrontend()
				{
					return false;
				}

				@Override
				public boolean isHiddenFromWebUI()
				{
					return false;
				}

				@Override
				public boolean isBlockedFromWebUI()
				{
					return false;
				}
			});
			this.actions.put("/philipshue/" + i + "/toggle", new Action()
			{
				@Override
				public void onAction() throws Exception
				{
					IntegrationPhilipsHue.this.toggle(bulbName, IntegrationPhilipsHue.this.lightStates.get(bulbName) == -1);
				}

				@Override
				public String getName()
				{
					return "TOGGLE " + bulbName;
				}
				
				@Override
				public String getDescription()
				{
					return "Will toggle the state (on/off) of the light called" + bulbName + ".";
				}


				@Override
				public boolean isHiddenFromFrontend()
				{
					return false;
				}

				@Override
				public boolean isHiddenFromWebUI()
				{
					return true;
				}

				@Override
				public boolean isBlockedFromWebUI()
				{
					return true;
				}
			});
		}

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
						PHLight light = u.get(0);
						PHLightState phls = light.getLastKnownLightState();
						String bulbName = light.getName();
						int state = phls.isOn() ? phls.getBrightness() : -1;
						IntegrationPhilipsHue.this.lights.put(bulbName, light);
						IntegrationPhilipsHue.this.lightStates.put(bulbName, state);
						log.info("Updating light state: " + bulbName + " = " + state);
					}
				}
			}

			@Override
			public void onBridgeConnected(PHBridge bridge, String username)
			{
				log.info("Connection to Hue Bridge at " + NMOConfiguration.INSTANCE.integrations.philipsHue.bridgeIP + " has been established");
				log.info("Bridge API authorization username: " + username);
				IntegrationPhilipsHue.this.sdk.setSelectedBridge(bridge);
				IntegrationPhilipsHue.this.sdk.enableHeartbeat(bridge, 1000);
				IntegrationPhilipsHue.this.activeBridge = bridge;
				NMOConfiguration.INSTANCE.integrations.philipsHue.bridgeUsername = username;
				try
				{
					NMOConfiguration.save();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				List<PHLight> u = bridge.getResourceCache().getAllLights();
				if (!u.isEmpty())
				{
					PHLight light = u.get(0);
					PHLightState phls = light.getLastKnownLightState();
					String bulbName = light.getName();
					int state = phls.isOn() ? phls.getBrightness() : -1;
					IntegrationPhilipsHue.this.lights.put(bulbName, light);
					IntegrationPhilipsHue.this.lightStates.put(bulbName, state);
					log.info("Updating light state: " + bulbName + " = " + state);
				}
			}

			@Override
			public void onAuthenticationRequired(PHAccessPoint accessPoint)
			{
				log.info("Authentication required. Please push the authentication button on the Hue Bridge!");
				NMOConfiguration.INSTANCE.integrations.philipsHue.bridgeIP = accessPoint.getIpAddress();
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
		accessPoint.setIpAddress(NMOConfiguration.INSTANCE.integrations.philipsHue.bridgeIP);
		accessPoint.setUsername(NMOConfiguration.INSTANCE.integrations.philipsHue.bridgeUsername);
		this.sdk.connect(accessPoint);
	}

	@Override
	public void update() throws Exception
	{
		// TODO Auto-generated method stub
	}

	public void toggle(String name, boolean state) throws IOException
	{
		PHLight light = this.lights.get(name);
		if (light == null)
			throw new IOException("No such light: " + name);
		PHLightState lightState = new PHLightState();
		lightState.setOn(state);
		if (state)
		{
			lightState.setBrightness(Integer.MAX_VALUE, true);
		}
		this.activeBridge.updateLightState(light, lightState);
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
