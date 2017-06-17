package com.tinytimrob.ppse.nmo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import com.tinytimrob.common.Configuration;
import com.tinytimrob.common.PlatformData;
import com.tinytimrob.common.PlatformType;
import com.tinytimrob.ppse.nmo.integrations.Integration;
import com.tinytimrob.ppse.nmo.integrations.IntegrationCommandLine;
import com.tinytimrob.ppse.nmo.integrations.IntegrationKeyboard;
import com.tinytimrob.ppse.nmo.integrations.IntegrationMidiTransmitter;
import com.tinytimrob.ppse.nmo.integrations.IntegrationMouse;
import com.tinytimrob.ppse.nmo.integrations.IntegrationNoise;
import com.tinytimrob.ppse.nmo.integrations.IntegrationPavlok;
import com.tinytimrob.ppse.nmo.integrations.IntegrationPhilipsHue;
import com.tinytimrob.ppse.nmo.integrations.IntegrationTwilio;
import com.tinytimrob.ppse.nmo.integrations.IntegrationXboxController;
import com.tinytimrob.ppse.nmo.utils.AppleHelper;
import com.tinytimrob.ppse.nmo.utils.JavaFxHelper;
import com.tinytimrob.ppse.nmo.utils.Logging;
import com.tinytimrob.ppse.nmo.utils.MasterLock;
import com.tinytimrob.ppse.nmo.ws.WebServer;
import javafx.scene.control.Dialog;
import javafx.scene.text.Font;

public class Main
{
	//-------------------------------------------
	public static String VERSION = "0.9.1";
	public static String JAVA_UPDATE_URL = "https://launcher.ginever.net/javaupdate";

	//-------------------------------------------
	public static String CLIENT_ID = "daf97645073a9cb9e0deefdc9fc9d6a4ac7ebeabd67e65268e1cbdfd6165eb85";
	public static String CLIENT_SECRET = "48cd03c31a0f1df56403bf5368ea2245df11887c8289079ce02b1025e43c82c0";
	public static String CLIENT_CALLBACK = "https://www.tinytimrob.com/nmo-oauth-intercept";

	//-------------------------------------------

	public static final ArrayList<Integration> integrations = new ArrayList<Integration>();
	static
	{
		integrations.add(IntegrationKeyboard.INSTANCE);
		integrations.add(IntegrationMouse.INSTANCE);
		integrations.add(IntegrationXboxController.INSTANCE);
		integrations.add(IntegrationMidiTransmitter.INSTANCE);
		integrations.add(IntegrationPavlok.INSTANCE);
		integrations.add(IntegrationNoise.INSTANCE);
		integrations.add(IntegrationPhilipsHue.INSTANCE);
		integrations.add(IntegrationTwilio.INSTANCE);
		integrations.add(IntegrationCommandLine.INSTANCE);
		integrations.add(ActivityTimerFakeIntegration.INSTANCE);
	}

	//-------------------------------------------

	@SuppressWarnings({ "rawtypes", "unused" })
	public static void main(String[] args)
	{
		try
		{
			try
			{
				Class<Dialog> dialogClass = Dialog.class;
				if ((!PlatformData.installationDirectory.isDirectory()) && (!PlatformData.installationDirectory.mkdirs()))
				{
					throw new RuntimeException("The installation directory could not be created: " + PlatformData.installationDirectory.getAbsolutePath());
				}
				if (!MasterLock.obtain())
				{
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					JOptionPane.showMessageDialog(null, "NoMoreOversleeps is already running.", "NoMoreOversleeps", JOptionPane.ERROR_MESSAGE);
					return;
				}
				Logging.initialize();
				try
				{
					NMOConfiguration.instance = Configuration.load(NMOConfiguration.class);
				}
				catch (Throwable t)
				{
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					JOptionPane.showMessageDialog(null, "Parsing of config.json failed; please check for JSON syntax errors", "NoMoreOversleeps", JOptionPane.ERROR_MESSAGE);
					return;
				}
				//===========================================================================================
				// include these fonts so that the software looks the same on every platform
				//===========================================================================================
				Font.loadFont(JavaFxHelper.buildResourcePath("Roboto-Bold.ttf"), 10);
				Font.loadFont(JavaFxHelper.buildResourcePath("Roboto-Regular.ttf"), 10);
				Font.loadFont(JavaFxHelper.buildResourcePath("Inconsolata-Regular.ttf"), 10);
				//===========================================================================================
				if (PlatformData.platformType == PlatformType.MAC)
				{
					AppleHelper.integrate();
				}
				WebcamCapture.init();
				for (Integration integration : integrations)
				{
					if (integration.isEnabled())
					{
						integration.init();
					}
				}
				WebServer.initialize();
				MainDialog.launch(MainDialog.class, args);
				WebServer.shutdown();
				Collections.reverse(integrations);
				for (Integration integration : integrations)
				{
					if (integration.isEnabled())
					{
						integration.shutdown();
					}
				}
				WebcamCapture.shutdown();
				Logging.shutdown();
				MasterLock.release();
				System.exit(0);
			}
			catch (NoClassDefFoundError e1)
			{
				e1.printStackTrace();
				String javaString = PlatformData.platformType == PlatformType.LINUX ? "JDK 8 and JavaFX 8u40 or later are" : "Java 8u40 or later is";
				complain("<html>" + javaString + " required to run this application. <b>You must update Java in order to continue.</b><br>Would you like to update Java now? (Clicking 'Yes' will open your web browser to the Java download page.)</html>");
			}
		}
		catch (Throwable e2)
		{
			String message = e2.getClass().getName() + ": " + e2.getMessage();
			e2.printStackTrace();
			JOptionPane.showMessageDialog(null, "Unrecoverable error: " + message + "\nNoMoreOversleeps will now close.", "NoMoreOversleeps", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
	}

	static void complain(String message) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, IOException, URISyntaxException, InterruptedException
	{
		// Resort to a Swing Y/N dialog asking if the user wants to update Java.
		// If they click yes, their default browser will open to the JAVA_UPDATE_URL
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		int reply = JOptionPane.showConfirmDialog(null, message, "NoMoreOversleeps", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
		if (reply == 0)
		{
			String platformCode = "";
			if (PlatformData.platformType == PlatformType.WINDOWS)
			{
				platformCode = PlatformData.is64bitOS ? "?platform=win64" : "?platform=win32";
			}
			else if (PlatformData.platformType == PlatformType.MAC)
			{
				platformCode = "?platform=mac";
			}
			else
			{
				platformCode = "?platform=linux";
			}
			java.awt.Desktop.getDesktop().browse(new URI(Main.JAVA_UPDATE_URL + platformCode));
			Thread.sleep(100);
		}
	}
}
