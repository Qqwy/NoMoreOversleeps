package com.tinytimrob.ppse.nmo;

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
import com.tinytimrob.common.Configuration;
import com.tinytimrob.common.LogWrapper;
import com.tinytimrob.common.PlatformData;

public class Lighting
{
	private static final Logger log = LogWrapper.getLogger();
	public static PHHueSDK SDK;
	public static PHBridge BRIDGE;
	public static PHSDKListener listener;
	public static volatile int LIGHT_STATE;

	public static void init()
	{
		SDK = PHHueSDK.getInstance();
		SDK.setAppName("NoMoreOversleeps");
		SDK.setDeviceName(PlatformData.computerName);
		SDK.getNotificationManager().registerSDKListener(listener = new PHSDKListener()
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
					PHBridgeSearchManager sm = (PHBridgeSearchManager) SDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
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
						LIGHT_STATE = phls.isOn() ? phls.getBrightness() : -1;
					}
				}
			}

			@Override
			public void onBridgeConnected(PHBridge bridge, String username)
			{
				log.info("Connection to Hue Bridge at " + NMOConfiguration.instance.hueBridgeIP + " has been established");
				log.info("Bridge API authorization username: " + username);
				SDK.setSelectedBridge(bridge);
				SDK.enableHeartbeat(bridge, 1000);
				BRIDGE = bridge;
				NMOConfiguration.instance.hueBridgeUsername = username;
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
					LIGHT_STATE = phls.isOn() ? phls.getBrightness() : -1;
				}
			}

			@Override
			public void onAuthenticationRequired(PHAccessPoint accessPoint)
			{
				log.info("Authentication required. Please push the authentication button on the Hue Bridge!");
				NMOConfiguration.instance.hueBridgeIP = accessPoint.getIpAddress();
				SDK.startPushlinkAuthentication(accessPoint);
			}

			@Override
			public void onAccessPointsFound(List<PHAccessPoint> accessPointList)
			{
				log.info(accessPointList.size() + " access points found");
				if (!accessPointList.isEmpty())
				{
					PHAccessPoint accessPoint = accessPointList.get(0);
					log.info("Attempting connection to " + accessPoint.getIpAddress());
					SDK.connect(accessPoint);
				}
			}
		});

		log.info("Attempting to reconnect to Hue Bridge...");
		PHAccessPoint accessPoint = new PHAccessPoint();
		accessPoint.setIpAddress(NMOConfiguration.instance.hueBridgeIP);
		accessPoint.setUsername(NMOConfiguration.instance.hueBridgeUsername);
		SDK.connect(accessPoint);
	}

	public static void toggle(boolean state)
	{
		PHLightState lightState = new PHLightState();
		lightState.setOn(state);
		if (state)
		{
			lightState.setBrightness(Integer.MAX_VALUE, true);
		}
		BRIDGE.setLightStateForDefaultGroup(lightState);
	}

	public static void shutdown()
	{
		if (SDK != null)
		{
			SDK.disableAllHeartbeat();
			if (BRIDGE != null)
			{
				SDK.disconnect(BRIDGE);
				BRIDGE = null;
			}
			SDK.destroySDK();
			SDK = null;
		}
	}
}
