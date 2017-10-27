package com.tinytimrob.ppse.nmo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.logging.log4j.Logger;
import com.tinytimrob.common.Configuration;
import com.tinytimrob.common.LogWrapper;
import com.tinytimrob.common.PlatformData;
import com.tinytimrob.common.PlatformType;
import com.tinytimrob.ppse.nmo.config.NMOConfiguration;
import com.tinytimrob.ppse.nmo.config.NMOStatistics;
import com.tinytimrob.ppse.nmo.integration.cmd.IntegrationCommandLine;
import com.tinytimrob.ppse.nmo.integration.discord.IntegrationDiscord;
import com.tinytimrob.ppse.nmo.integration.filewriter.IntegrationFileWriter;
import com.tinytimrob.ppse.nmo.integration.input.IntegrationKeyboard;
import com.tinytimrob.ppse.nmo.integration.input.IntegrationMidiTransmitter;
import com.tinytimrob.ppse.nmo.integration.input.IntegrationMouse;
import com.tinytimrob.ppse.nmo.integration.input.IntegrationXboxController;
import com.tinytimrob.ppse.nmo.integration.noise.IntegrationNoise;
import com.tinytimrob.ppse.nmo.integration.pavlok.IntegrationPavlok;
import com.tinytimrob.ppse.nmo.integration.philipshue.IntegrationPhilipsHue;
import com.tinytimrob.ppse.nmo.integration.randomizer.IntegrationRandomizer;
import com.tinytimrob.ppse.nmo.integration.tplink.IntegrationTPLink;
import com.tinytimrob.ppse.nmo.integration.twilio.IntegrationTwilio;
import com.tinytimrob.ppse.nmo.integration.webui.IntegrationWebUI;
import com.tinytimrob.ppse.nmo.integration.wemo.IntegrationWemo;
import com.tinytimrob.ppse.nmo.utils.AppleHelper;
import com.tinytimrob.ppse.nmo.utils.JavaFxHelper;
import com.tinytimrob.ppse.nmo.utils.Logging;
import com.tinytimrob.ppse.nmo.utils.MasterLock;
import javafx.scene.control.Dialog;
import javafx.scene.text.Font;

public class Main
{
	private static final Logger log = LogWrapper.getLogger();

	//-------------------------------------------
	public static String VERSION = "0.13";
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
		integrations.add(IntegrationTPLink.INSTANCE);
		integrations.add(IntegrationWemo.INSTANCE);
		integrations.add(IntegrationTwilio.INSTANCE);
		integrations.add(IntegrationCommandLine.INSTANCE);
		integrations.add(IntegrationFileWriter.INSTANCE);
		integrations.add(IntegrationDiscord.INSTANCE);
		integrations.add(IntegrationRandomizer.INSTANCE);
		integrations.add(ActivityTimerFakeIntegration.INSTANCE);
		integrations.add(ScheduleFakeIntegration.INSTANCE);
		integrations.add(IntegrationWebUI.INSTANCE);
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
					NMOConfiguration.load();
				}
				catch (Throwable t)
				{
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					JOptionPane.showMessageDialog(null, "Parsing of config.json failed; please check for JSON syntax errors", "NoMoreOversleeps", JOptionPane.ERROR_MESSAGE);
					return;
				}
				try
				{
					NMOStatistics.load();
				}
				catch (Throwable t)
				{
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					JOptionPane.showMessageDialog(null, "Parsing of stats.json failed; please check for JSON syntax errors", "NoMoreOversleeps", JOptionPane.ERROR_MESSAGE);
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
				for (Integration integration : integrations)
				{
					if (integration.isEnabled())
					{
						log.info("Initializing integration module : " + integration.id);
						integration.init();
					}
				}
				MainDialog.launch(MainDialog.class, args);
				Collections.reverse(integrations);
				for (Integration integration : integrations)
				{
					if (integration.isEnabled())
					{
						log.info("Shutting down integration module : " + integration.id);
						integration.shutdown();
					}
				}
				Logging.shutdown();
				try
				{
					Configuration.save(NMOStatistics.instance, "stats.json");
				}
				catch (Throwable t)
				{
					t.printStackTrace(); // damn
				}
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
