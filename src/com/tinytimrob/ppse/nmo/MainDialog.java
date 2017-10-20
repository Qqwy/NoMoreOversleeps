package com.tinytimrob.ppse.nmo;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import com.tinytimrob.common.CommonUtils;
import com.tinytimrob.common.Configuration;
import com.tinytimrob.common.LogWrapper;
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
import com.tinytimrob.ppse.nmo.integration.twilio.IntegrationTwilio;
import com.tinytimrob.ppse.nmo.integration.webui.PortForwarding;
import com.tinytimrob.ppse.nmo.integration.webui.WebcamCapture;
import com.tinytimrob.ppse.nmo.utils.DesktopHelper;
import com.tinytimrob.ppse.nmo.utils.JavaFxHelper;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import nl.captcha.Captcha;
import nl.captcha.backgrounds.GradiatedBackgroundProducer;
import nl.captcha.gimpy.FishEyeGimpyRenderer;

public class MainDialog extends Application
{
	private static final Logger log = LogWrapper.getLogger();
	public static Scene scene;

	public static volatile ActivityTimer pendingTimer = null;
	public static volatile ActivityTimer timer = null;
	public static volatile String pauseReason = "";
	public static volatile long pausedUntil = 0;
	public static volatile long nextActivityWarningID;
	public static volatile long lastActivityTime = System.currentTimeMillis();
	public static volatile String lastActivitySource = "system";
	public static volatile SimpleStringProperty loginTokenValidUntilString = new SimpleStringProperty("");
	public static volatile SimpleStringProperty webMonitoringString = new SimpleStringProperty("");
	public static volatile SimpleStringProperty activeTimerString = new SimpleStringProperty("");
	public static volatile SimpleStringProperty lastActivityTimeString = new SimpleStringProperty("");
	public static volatile SimpleStringProperty timeDiffString = new SimpleStringProperty("");
	public static volatile SimpleStringProperty webcamName = new SimpleStringProperty("");
	public static volatile SimpleStringProperty lightingStateString = new SimpleStringProperty("");
	public static volatile SimpleStringProperty startedString = new SimpleStringProperty("");
	public static volatile SimpleStringProperty startedString2 = new SimpleStringProperty("");
	public static volatile SimpleStringProperty lastOversleepString = new SimpleStringProperty("");
	public static volatile SimpleStringProperty lastOversleepString2 = new SimpleStringProperty("");
	public static volatile SimpleStringProperty personalBestString = new SimpleStringProperty("");
	public static volatile SimpleBooleanProperty isCurrentlyPaused = new SimpleBooleanProperty(false);
	public static volatile SimpleObjectProperty<Image> lastWebcamImage = new SimpleObjectProperty<Image>();
	public static volatile SleepEntry lastSleepBlockWarning = null;
	public static volatile SleepEntry nextSleepBlock = null;
	public static volatile String scheduleStatus = "No schedule configured";
	public static volatile String scheduleStatusShort = "UNCONFIGURED";
	public static volatile WritableImage writableImage = null;
	public static ObservableList<String> events = FXCollections.observableArrayList();
	public static ArrayList<CustomEventAction> customActions = new ArrayList<CustomEventAction>();
	public static volatile int tick = 0;

	@Override
	public void start(Stage stage) throws Exception
	{
		log.info("JavaFX application start");
		triggerEvent("Application started", null);

		nextActivityWarningID = 0;

		Collections.sort(NMOConfiguration.instance.schedule);
		for (SleepEntry entry : NMOConfiguration.instance.schedule)
		{
			triggerEvent("Adding sleep block: " + entry.describe(), null);
		}
		for (ActivityTimer entry : NMOConfiguration.instance.timers)
		{
			triggerEvent("Adding activity timer: " + entry.name + " " + entry.secondsForFirstWarning + "s/" + entry.secondsForSubsequentWarnings + "s", null);
		}
		int q = 0;
		for (CustomEventAction action : NMOConfiguration.instance.events.custom)
		{
			action.originalOrder = q;
			q++;
			action.updateNextTriggerTime();
			triggerEvent("Adding custom event trigger " + action.name + " triggering " + action.describe() + " and next on " + CommonUtils.convertTimestamp(action.nextTriggerTime), null);
			customActions.add(action);
		}
		Collections.sort(customActions);

		if (NMOConfiguration.instance.integrations.pavlok.enabled)
		{
			try
			{
				IntegrationPavlok.INSTANCE.vibration(255, "Connection test");
				triggerEvent("<VIBRATE PAVLOK> Connection test", null);
			}
			catch (Throwable t)
			{
				t.printStackTrace();
				NMOConfiguration.instance.integrations.pavlok.auth = null;
			}
		}

		// fix bad "last oversleep" value
		if (NMOConfiguration.instance.scheduleLastOversleep < NMOConfiguration.instance.scheduleStartedOn)
		{
			NMOConfiguration.instance.scheduleLastOversleep = NMOConfiguration.instance.scheduleStartedOn;
		}

		//==================================================================
		// CONFIGURE THE STAGE
		//==================================================================
		stage.setTitle("NoMoreOversleeps v" + Main.VERSION);
		stage.getIcons().add(new Image(JavaFxHelper.buildResourcePath("icon.png")));
		stage.setResizable(true);
		stage.setMinWidth(1210);
		stage.setMaxWidth(1210);

		//==================================================================
		// CONFIGURE ANIMATION TIMER
		//==================================================================
		// this is an absurd workaround
		final AnimationTimer at = new AnimationTimer()
		{
			@Override
			public void handle(long now)
			{
				MainDialog.this.tick();
			}
		};

		//==================================================================
		// CONFIGURE THE SCENE
		//==================================================================
		final ScrollPane outerPane = new ScrollPane();
		outerPane.setId("root");
		outerPane.setFitToHeight(true);
		outerPane.setFitToWidth(true);
		scene = new Scene(outerPane, 1210, 910, Color.WHITE);
		scene.getStylesheets().add(JavaFxHelper.buildResourcePath("application.css"));

		//==================================================================
		// INTERCEPT CLOSING OF WINDOW BEHAVIOUR
		//==================================================================
		Platform.setImplicitExit(false);
		stage.setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent event)
			{
				event.consume();
				MainDialog.this.openAppCloseDialog();
			}
		});

		//==================================================================
		// BUILD THE PRIMARY PANE
		//==================================================================
		final BorderPane innerPane = new BorderPane();
		innerPane.setStyle("-fx-background-color: #222;");
		this.loadFrames(innerPane);

		//==================================================================
		// PAVLOK CRAP
		//==================================================================
		if (NMOConfiguration.instance.integrations.pavlok.enabled && NMOConfiguration.instance.integrations.pavlok.auth == null)
		{
			final String url = "https://pavlok-mvp.herokuapp.com/oauth/authorize?client_id=" + Main.CLIENT_ID + "&redirect_uri=" + Main.CLIENT_CALLBACK + "&response_type=code";
			BorderPane authPane = new BorderPane();
			WebView browser = new WebView();
			WebEngine webEngine = browser.getEngine();
			webEngine.load(url);
			authPane.setCenter(browser);
			webEngine.setOnStatusChanged(new EventHandler<WebEvent<String>>()
			{
				boolean handledCallbackYet = false;

				@Override
				public void handle(WebEvent<String> event)
				{
					if (!this.handledCallbackYet && event.getSource() instanceof WebEngine)
					{
						WebEngine we = (WebEngine) event.getSource();
						String location = we.getLocation();
						System.out.println(location);
						if (location.equals("https://pavlok-mvp.herokuapp.com/"))
						{
							we.load(url);
						}
						else if (location.startsWith(Main.CLIENT_CALLBACK) && location.contains("?code"))
						{
							this.handledCallbackYet = true;
							System.out.println("Detected location: " + location);
							try
							{
								String[] params = location.split("\\Q?\\E")[1].split("\\Q&\\E");
								Map<String, String> map = new HashMap<String, String>();
								for (String param : params)
								{
									String name = param.split("=")[0];
									String value = param.split("=")[1];
									map.put(name, value);
								}
								NMOConfiguration.instance.integrations.pavlok.auth = IntegrationPavlok.postAuthToken(map.get("code"));
								Configuration.save();
								IntegrationPavlok.INSTANCE.vibration(255, "Connection test");
								triggerEvent("<VIBRATE PAVLOK> Connection test", null);
								outerPane.setContent(innerPane);
								outerPane.requestFocus();
								outerPane.requestLayout();
								outerPane.setVvalue(0);
								at.start();
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					}
				}
			});
			outerPane.setContent(authPane);
		}
		else
		{
			outerPane.setContent(innerPane);
			at.start();
		}

		//==================================================================
		// SHOW STAGE
		//==================================================================
		stage.setScene(scene);
		stage.show();

		outerPane.requestFocus();
		outerPane.requestLayout();
		outerPane.setVvalue(0);
	}

	private void addIntegrationButtonsToVbox(Integration integration, VBox vbox)
	{
		for (String buttonKey : integration.getActions().keySet())
		{
			System.out.println("*" + buttonKey);
			final Action clickableButton = integration.getActions().get(buttonKey);
			final Button jfxButton = new Button(clickableButton.getName());
			jfxButton.setPadding(new Insets(2, 4, 2, 4));
			jfxButton.setMinWidth(256);
			jfxButton.setMaxWidth(256);
			jfxButton.setAlignment(Pos.BASELINE_LEFT);
			jfxButton.setContentDisplay(ContentDisplay.RIGHT);
			jfxButton.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent arg0)
				{
					try
					{
						triggerEvent("<" + clickableButton.getName() + "> from frontend", null);
						clickableButton.onAction();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});
			vbox.getChildren().add(jfxButton);
		}
	}

	private void loadFrames(BorderPane innerPane)
	{
		// use a grid pane layout
		GridPane pane = new GridPane();
		pane.setPadding(new Insets(8, 8, 8, 8));
		pane.setHgap(8);
		pane.setVgap(8);
		ColumnConstraints none = new ColumnConstraints();
		none.setHgrow(Priority.ALWAYS);
		ColumnConstraints c300 = new ColumnConstraints();
		c300.setMinWidth(340);
		c300.setMaxWidth(340);
		pane.getColumnConstraints().addAll(none, none, none, c300);
		RowConstraints rcSometimes = new RowConstraints();
		rcSometimes.setVgrow(Priority.NEVER);
		RowConstraints rcAlways = new RowConstraints();
		rcAlways.setVgrow(Priority.ALWAYS);
		pane.getRowConstraints().addAll(rcSometimes, rcSometimes, rcSometimes, rcSometimes, rcAlways);
		innerPane.setCenter(pane);

		// schedule
		{
			HBox hbox = new HBox(4);
			hbox.setPadding(new Insets(4));
			hbox.setAlignment(Pos.TOP_CENTER);

			VBox statusBox = new VBox(4);
			statusBox.setSpacing(0);
			statusBox.setAlignment(Pos.TOP_LEFT);
			hbox.getChildren().add(statusBox);
			HBox.setHgrow(statusBox, Priority.ALWAYS);

			String sn = NMOConfiguration.instance.scheduleName;
			if (sn == null || sn.isEmpty())
			{
				sn = "No schedule name configured";
			}
			statusBox.getChildren().add(JavaFxHelper.createLabel(sn, Color.WHITE, "-fx-font-weight: bold;"));

			if (NMOConfiguration.instance.scheduleStartedOn > 0)
			{
				final Label started = JavaFxHelper.createLabel("Started: ", Color.WHITE, "-fx-font-weight: bold;");
				final Label startedG = JavaFxHelper.createLabel("", Color.WHITE, "-fx-font-weight: normal;");
				startedG.textProperty().bind(startedString);
				started.setGraphic(startedG);
				started.setContentDisplay(ContentDisplay.RIGHT);
				statusBox.getChildren().add(started);

				final Label started2 = JavaFxHelper.createLabel("", Color.WHITE, "");
				started2.textProperty().bind(startedString2);
				started2.setPadding(new Insets(0, 0, 0, 10));
				statusBox.getChildren().add(started2);

				final Label lastOversleep = JavaFxHelper.createLabel("Last oversleep: ", Color.WHITE, "-fx-font-weight: bold;");
				final Label lastOversleepG = JavaFxHelper.createLabel("", Color.WHITE, "-fx-font-weight: normal;");
				lastOversleepG.textProperty().bind(lastOversleepString);
				lastOversleep.setGraphic(lastOversleepG);
				lastOversleep.setContentDisplay(ContentDisplay.RIGHT);
				statusBox.getChildren().add(lastOversleep);

				final Label lastOversleep2 = JavaFxHelper.createLabel("", Color.WHITE, "");
				lastOversleep2.textProperty().bind(lastOversleepString2);
				lastOversleep2.setPadding(new Insets(0, 0, 0, 10));
				statusBox.getChildren().add(lastOversleep2);

				final Label personalBest = JavaFxHelper.createLabel("Personal best: ", Color.WHITE, "-fx-font-weight: bold;");
				final Label personalBestG = JavaFxHelper.createLabel("", Color.WHITE, "-fx-font-weight: normal;");
				personalBestG.textProperty().bind(personalBestString);
				personalBest.setGraphic(personalBestG);
				personalBest.setContentDisplay(ContentDisplay.RIGHT);
				statusBox.getChildren().add(personalBest);
				personalBest.setPadding(new Insets(0, 0, 4, 0));

				this.addIntegrationButtonsToVbox(ScheduleFakeIntegration.INSTANCE, statusBox);
			}

			Separator s = new Separator(Orientation.HORIZONTAL);
			s.setPadding(new Insets(6, 0, 2, 0));
			statusBox.getChildren().add(s);

			for (SleepEntry entry : NMOConfiguration.instance.schedule)
			{
				statusBox.getChildren().add(JavaFxHelper.createLabel(entry.name, Color.WHITE, "-fx-font-weight: bold;"));
				statusBox.getChildren().add(JavaFxHelper.createLabel(entry.describeTime() + "    (" + entry.approachWarning + "m approach warning)", Color.WHITE, "", new Insets(0, 0, 0, 8)));
			}

			final HBox heading = JavaFxHelper.createHorizontalBox(Control.USE_COMPUTED_SIZE, 24);
			heading.setStyle("-fx-background-color: #26DE42;");
			heading.setPadding(new Insets(2));
			heading.setSpacing(2);
			final Label label = JavaFxHelper.createLabel("Current Schedule", Color.BLACK, "-fx-font-size: 11pt;");
			heading.getChildren().add(label);
			final StackPane spt = new StackPane();
			heading.getChildren().add(spt);
			HBox.setHgrow(spt, Priority.ALWAYS);
			heading.setAlignment(Pos.TOP_LEFT);
			final Button jfxButtonConfigure = JavaFxHelper.createButton("Configure", JavaFxHelper.createIcon(FontAwesomeIcon.COGS, "11", Color.BLACK));
			jfxButtonConfigure.setPadding(new Insets(2, 4, 2, 4));
			jfxButtonConfigure.setDisable(true); // temporary
			heading.getChildren().add(jfxButtonConfigure);

			final BorderPane frame = new BorderPane();
			frame.setTop(heading);
			frame.setCenter(hbox);
			frame.setStyle("-fx-border-width: 1px; -fx-border-color: #26DE42; -fx-background-color: #333;");
			pane.add(frame, 0, 0, 1, 1);
			GridPane.setVgrow(pane, Priority.ALWAYS);
		}

		// monitoring control
		{
			HBox hbox = new HBox(4);
			hbox.setPadding(new Insets(4));
			hbox.setAlignment(Pos.TOP_CENTER);

			VBox statusBox = new VBox(4);
			statusBox.setSpacing(3);
			statusBox.setAlignment(Pos.TOP_LEFT);
			hbox.getChildren().add(statusBox);
			HBox.setHgrow(statusBox, Priority.ALWAYS);

			final Label keyboard = JavaFxHelper.createLabel("Keyboard: ", Color.WHITE, "-fx-font-weight: bold;");
			if (IntegrationKeyboard.INSTANCE.isEnabled())
			{
				keyboard.setGraphic(JavaFxHelper.createLabel("ENABLED", Color.LIME));
				keyboard.setContentDisplay(ContentDisplay.RIGHT);
			}
			else
			{
				keyboard.setGraphic(JavaFxHelper.createLabel("DISABLED", Color.RED));
				keyboard.setContentDisplay(ContentDisplay.RIGHT);
			}
			statusBox.getChildren().add(keyboard);

			final Label mouse = JavaFxHelper.createLabel("Mouse: ", Color.WHITE, "-fx-font-weight: bold;");
			if (IntegrationMouse.INSTANCE.isEnabled())
			{
				mouse.setGraphic(JavaFxHelper.createLabel("ENABLED", Color.LIME));
				mouse.setContentDisplay(ContentDisplay.RIGHT);
			}
			else
			{
				mouse.setGraphic(JavaFxHelper.createLabel("DISABLED", Color.RED));
				mouse.setContentDisplay(ContentDisplay.RIGHT);
			}
			statusBox.getChildren().add(mouse);

			final Label xbox = JavaFxHelper.createLabel("Controller: ", Color.WHITE, "-fx-font-weight: bold;");
			if (IntegrationXboxController.INSTANCE.isEnabled())
			{
				xbox.setGraphic(JavaFxHelper.createLabel("ENABLED", Color.LIME));
				xbox.setContentDisplay(ContentDisplay.RIGHT);
			}
			else
			{
				xbox.setGraphic(JavaFxHelper.createLabel("DISABLED", Color.RED));
				xbox.setContentDisplay(ContentDisplay.RIGHT);
			}
			statusBox.getChildren().add(xbox);

			final Label midi = JavaFxHelper.createLabel("MIDI: ", Color.WHITE, "-fx-font-weight: bold;");
			if (IntegrationMidiTransmitter.INSTANCE.isEnabled())
			{
				midi.setGraphic(JavaFxHelper.createLabel("ENABLED", Color.LIME));
				midi.setContentDisplay(ContentDisplay.RIGHT);
			}
			else
			{
				midi.setGraphic(JavaFxHelper.createLabel("DISABLED", Color.RED));
				midi.setContentDisplay(ContentDisplay.RIGHT);
			}
			statusBox.getChildren().add(midi);
			if (IntegrationMidiTransmitter.INSTANCE.isEnabled())
			{
				for (String transmitter : NMOConfiguration.instance.integrations.midiTransmitter.transmitters)
				{
					final Label transmitterlabel = JavaFxHelper.createLabel("> " + transmitter, Color.YELLOW, "", new Insets(0, 0, 0, 8));
					statusBox.getChildren().add(transmitterlabel);
				}
			}

			statusBox.getChildren().add(new Separator(Orientation.HORIZONTAL));

			final Label lastCursorTime = JavaFxHelper.createLabel("", Color.WHITE, "-fx-font-weight: bold;");
			lastCursorTime.textProperty().bind(lastActivityTimeString);
			statusBox.getChildren().add(lastCursorTime);

			final Label timeDiff = JavaFxHelper.createLabel("", Color.WHITE, "-fx-font-weight: bold;");
			timeDiff.textProperty().bind(timeDiffString);
			statusBox.getChildren().add(timeDiff);

			final Label activeTimerLabel = JavaFxHelper.createLabel("", Color.WHITE);
			activeTimerLabel.textProperty().bind(activeTimerString);
			statusBox.getChildren().add(activeTimerLabel);

			statusBox.getChildren().add(new Separator(Orientation.HORIZONTAL));
			this.addIntegrationButtonsToVbox(ActivityTimerFakeIntegration.INSTANCE, statusBox);

			hbox.getChildren().add(new Separator(Orientation.VERTICAL));

			VBox pauseControlBox = new VBox(6);
			pauseControlBox.setAlignment(Pos.TOP_LEFT);
			final Label label2 = JavaFxHelper.createLabel("Pause/Resume", Color.WHITE);
			pauseControlBox.getChildren().add(label2);
			int[] periods = new int[] { 5, 10, 15, 20, 25, 30, 45, 60, 90, 105, 120, 180, 240, 300, 360, 420, 480, 540, 600, 660, 720 };
			GridPane btnGridPane = null;
			for (int p = 0; p < periods.length; p++)
			{
				if (p % 3 == 0)
				{
					if (btnGridPane != null)
					{
						pauseControlBox.getChildren().add(btnGridPane);
					}
					btnGridPane = new GridPane();
					btnGridPane.setHgap(6);
				}
				final int pp = periods[p];
				int hours = pp / 60;
				int minutes = pp % 60;
				final String hm = (((hours > 0) ? hours + "h" : "") + ((minutes > 0) ? minutes + "m" : ""));
				final Button pauseButton = JavaFxHelper.createButton(hm);
				pauseButton.setMinWidth(64);
				pauseButton.setMaxWidth(64);
				pauseButton.setAlignment(Pos.BASELINE_LEFT);
				pauseButton.setPadding(new Insets(5, 4, 5, 4));
				pauseButton.setOnAction(new EventHandler<ActionEvent>()
				{
					@Override
					public void handle(ActionEvent arg0)
					{
						MainDialog.this.openPauseDialog(hm, pp);
					}
				});
				//pauseButton.disableProperty().bind(isCurrentlyPaused);
				btnGridPane.addColumn(p % 3, pauseButton);
			}
			if (btnGridPane != null)
			{
				pauseControlBox.getChildren().add(btnGridPane);
			}
			final Button unpauseButton = JavaFxHelper.createButton("Unpause");
			unpauseButton.setMinWidth(204);
			unpauseButton.setMaxWidth(204);
			unpauseButton.setPadding(new Insets(5, 4, 5, 4));
			unpauseButton.setAlignment(Pos.BASELINE_LEFT);
			unpauseButton.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent arg0)
				{
					pausedUntil = 0;
					isCurrentlyPaused.set(false);
					triggerEvent("Unpaused manually", NMOConfiguration.instance.events.pauseCancelled);
				}
			});
			unpauseButton.disableProperty().bind(isCurrentlyPaused.not());
			pauseControlBox.getChildren().add(unpauseButton);
			hbox.getChildren().add(pauseControlBox);

			hbox.getChildren().add(new Separator(Orientation.VERTICAL));

			VBox webcamBox = new VBox(6);
			webcamBox.setMinWidth(326);
			webcamBox.setMaxWidth(326);
			if (NMOConfiguration.instance.integrations.webUI.enabled)
			{
				Label l = JavaFxHelper.createLabel("", Color.WHITE, "-fx-font-weight: bold;");
				l.setPadding(new Insets(0, 0, 0, 2));
				l.textProperty().bind(webcamName);
				webcamBox.getChildren().add(l);
				ImageView webcamImageView = new ImageView();
				webcamImageView.imageProperty().bind(lastWebcamImage);
				webcamImageView.setPreserveRatio(true);
				webcamBox.getChildren().add(webcamImageView);
				webcamBox.getChildren().add(new Separator(Orientation.HORIZONTAL));
				final Label webMonitoringLabel = JavaFxHelper.createLabel("", Color.WHITE);
				webMonitoringLabel.textProperty().bind(webMonitoringString);
				webcamBox.getChildren().add(webMonitoringLabel);
			}
			else
			{
				webcamBox.getChildren().add(JavaFxHelper.createLabel("Remote monitoring is disabled", Color.GRAY, "-fx-font-weight: bold;"));
			}
			hbox.getChildren().add(webcamBox);

			final HBox heading = JavaFxHelper.createHorizontalBox(Control.USE_COMPUTED_SIZE, 24);
			heading.setStyle("-fx-background-color: #6D81A3;");
			heading.setPadding(new Insets(2));
			heading.setSpacing(2);
			final Label label = JavaFxHelper.createLabel("Monitoring Control", Color.BLACK, "-fx-font-size: 11pt;");
			heading.getChildren().add(label);
			final StackPane spt = new StackPane();
			heading.getChildren().add(spt);
			HBox.setHgrow(spt, Priority.ALWAYS);
			heading.setAlignment(Pos.TOP_LEFT);

			final Button jfxButtonWebUI = JavaFxHelper.createButton("Launch web UI", JavaFxHelper.createIcon(FontAwesomeIcon.LINK, "11", Color.BLACK));
			jfxButtonWebUI.setPadding(new Insets(2, 4, 2, 4));
			jfxButtonWebUI.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent arg0)
				{
					try
					{
						String hostname = NMOConfiguration.instance.integrations.webUI.hostname.isEmpty() ? PortForwarding.getExternalIP() : NMOConfiguration.instance.integrations.webUI.hostname;
						DesktopHelper.browse("http://" + hostname + ":" + NMOConfiguration.instance.integrations.webUI.jettyPort + "/");
					}
					catch (Throwable e)
					{
						e.printStackTrace();
					}
				}
			});
			jfxButtonWebUI.setDisable(!NMOConfiguration.instance.integrations.webUI.enabled);
			heading.getChildren().add(jfxButtonWebUI);

			final Button jfxButtonPortForward = JavaFxHelper.createButton("Attempt port auto-forward", JavaFxHelper.createIcon(FontAwesomeIcon.PLUG, "11", Color.BLACK));
			jfxButtonPortForward.setPadding(new Insets(2, 4, 2, 4));
			jfxButtonPortForward.setOnAction(new EventHandler<ActionEvent>()
			{
				@SuppressWarnings("unchecked")
				@Override
				public void handle(ActionEvent arg0)
				{
					try
					{
						ArrayList<String> messages = new ArrayList<String>();
						PortForwarding.attemptAutomaticPortForwarding(messages);
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle("Port forwarding");
						alert.setHeaderText(null);
						alert.setContentText(StringUtils.join(messages, "\n"));
						alert.setResizable(true);
						alert.getDialogPane().setPrefSize(600, Region.USE_COMPUTED_SIZE);
						alert.showAndWait();
					}
					catch (Throwable e)
					{
						e.printStackTrace();
					}
				}
			});
			jfxButtonPortForward.setDisable(!NMOConfiguration.instance.integrations.webUI.enabled);
			heading.getChildren().add(jfxButtonPortForward);

			final Button jfxButtonConfigure = JavaFxHelper.createButton("Configure", JavaFxHelper.createIcon(FontAwesomeIcon.COGS, "11", Color.BLACK));
			jfxButtonConfigure.setPadding(new Insets(2, 4, 2, 4));
			jfxButtonConfigure.setDisable(true); // temporary
			heading.getChildren().add(jfxButtonConfigure);

			final BorderPane frame = new BorderPane();
			frame.setTop(heading);
			frame.setCenter(hbox);
			frame.setStyle("-fx-border-width: 1px; -fx-border-color: #6D81A3; -fx-background-color: #333;");
			pane.add(frame, 1, 0, 3, 1);
			GridPane.setVgrow(pane, Priority.ALWAYS);
		}

		// PAVLOK
		{
			HBox hbox = new HBox(4);
			hbox.setPadding(new Insets(4));
			hbox.setAlignment(Pos.TOP_CENTER);

			VBox statusBox = new VBox(4);
			statusBox.setAlignment(Pos.TOP_LEFT);
			hbox.getChildren().add(statusBox);
			HBox.setHgrow(statusBox, Priority.ALWAYS);

			if (IntegrationPavlok.INSTANCE.isEnabled())
			{
				final Label loginTokenValidUntil = JavaFxHelper.createLabel("", Color.WHITE, "-fx-font-weight: bold;");
				loginTokenValidUntil.textProperty().bind(loginTokenValidUntilString);
				statusBox.getChildren().add(loginTokenValidUntil);
				statusBox.getChildren().add(new Separator(Orientation.HORIZONTAL));
				this.addIntegrationButtonsToVbox(IntegrationPavlok.INSTANCE, statusBox);
			}
			else
			{
				statusBox.getChildren().add(JavaFxHelper.createLabel("Integration disabled", Color.GRAY, "-fx-font-weight: bold;"));
			}

			final HBox heading = JavaFxHelper.createHorizontalBox(Control.USE_COMPUTED_SIZE, 24);
			heading.setStyle("-fx-background-color: #DEB026;");
			heading.setPadding(new Insets(2));
			heading.setSpacing(2);
			final Label label = JavaFxHelper.createLabel("Pavlok", Color.BLACK, "-fx-font-size: 11pt;");
			heading.getChildren().add(label);
			final StackPane spt = new StackPane();
			heading.getChildren().add(spt);
			HBox.setHgrow(spt, Priority.ALWAYS);
			heading.setAlignment(Pos.TOP_LEFT);
			final Button jfxButtonConfigure = JavaFxHelper.createButton("Configure", JavaFxHelper.createIcon(FontAwesomeIcon.COGS, "11", Color.BLACK));
			jfxButtonConfigure.setPadding(new Insets(2, 4, 2, 4));
			jfxButtonConfigure.setDisable(true); // temporary
			heading.getChildren().add(jfxButtonConfigure);

			final BorderPane frame = new BorderPane();
			frame.setTop(heading);
			frame.setCenter(hbox);
			frame.setStyle("-fx-border-width: 1px; -fx-border-color: #DEB026; -fx-background-color: #333;");
			pane.add(frame, 0, 1, 1, 1);
			GridPane.setVgrow(pane, Priority.ALWAYS);
		}

		// LIGHTING
		{
			HBox hbox = new HBox(4);
			hbox.setPadding(new Insets(4));
			hbox.setAlignment(Pos.TOP_CENTER);

			VBox statusBox = new VBox(4);
			statusBox.setAlignment(Pos.TOP_LEFT);
			hbox.getChildren().add(statusBox);
			HBox.setHgrow(statusBox, Priority.ALWAYS);

			if (IntegrationPhilipsHue.INSTANCE.isEnabled())
			{
				final Label lightingStateLabel = JavaFxHelper.createLabel("", Color.WHITE, "-fx-font-weight: bold;");
				lightingStateLabel.textProperty().bind(lightingStateString);
				statusBox.getChildren().add(lightingStateLabel);
				statusBox.getChildren().add(new Separator(Orientation.HORIZONTAL));
				this.addIntegrationButtonsToVbox(IntegrationPhilipsHue.INSTANCE, statusBox);
			}
			else
			{
				statusBox.getChildren().add(JavaFxHelper.createLabel("Integration disabled", Color.GRAY, "-fx-font-weight: bold;"));
			}

			final HBox heading = JavaFxHelper.createHorizontalBox(Control.USE_COMPUTED_SIZE, 24);
			heading.setStyle("-fx-background-color: #839CA0;");
			heading.setPadding(new Insets(2));
			heading.setSpacing(2);
			final Label label = JavaFxHelper.createLabel("Lighting", Color.BLACK, "-fx-font-size: 11pt;");
			heading.getChildren().add(label);
			final StackPane spt = new StackPane();
			heading.getChildren().add(spt);
			HBox.setHgrow(spt, Priority.ALWAYS);
			heading.setAlignment(Pos.TOP_LEFT);
			final Button jfxButtonConfigure = JavaFxHelper.createButton("Configure", JavaFxHelper.createIcon(FontAwesomeIcon.COGS, "11", Color.BLACK));
			jfxButtonConfigure.setPadding(new Insets(2, 4, 2, 4));
			jfxButtonConfigure.setDisable(true); // temporary
			heading.getChildren().add(jfxButtonConfigure);

			final BorderPane frame = new BorderPane();
			frame.setTop(heading);
			frame.setCenter(hbox);
			frame.setStyle("-fx-border-width: 1px; -fx-border-color: #839CA0; -fx-background-color: #333;");
			pane.add(frame, 1, 1, 1, 1);
			GridPane.setVgrow(pane, Priority.ALWAYS);
		}

		// ALARM SOUNDS
		{
			HBox hbox = new HBox(4);
			hbox.setPadding(new Insets(4));
			hbox.setAlignment(Pos.TOP_CENTER);

			VBox statusBox = new VBox(4);
			statusBox.setAlignment(Pos.TOP_LEFT);
			hbox.getChildren().add(statusBox);
			HBox.setHgrow(statusBox, Priority.ALWAYS);

			if (IntegrationNoise.INSTANCE.isEnabled())
			{
				this.addIntegrationButtonsToVbox(IntegrationNoise.INSTANCE, statusBox);
			}
			else
			{
				statusBox.getChildren().add(JavaFxHelper.createLabel("Integration disabled", Color.GRAY, "-fx-font-weight: bold;"));
			}

			final HBox heading = JavaFxHelper.createHorizontalBox(Control.USE_COMPUTED_SIZE, 24);
			heading.setStyle("-fx-background-color: #B649C6;");
			heading.setPadding(new Insets(2));
			heading.setSpacing(2);
			final Label label = JavaFxHelper.createLabel("Noise", Color.BLACK, "-fx-font-size: 11pt;");
			heading.getChildren().add(label);
			final StackPane spt = new StackPane();
			heading.getChildren().add(spt);
			HBox.setHgrow(spt, Priority.ALWAYS);
			heading.setAlignment(Pos.TOP_LEFT);
			final Button jfxButtonConfigure = JavaFxHelper.createButton("Configure", JavaFxHelper.createIcon(FontAwesomeIcon.COGS, "11", Color.BLACK));
			jfxButtonConfigure.setPadding(new Insets(2, 4, 2, 4));
			jfxButtonConfigure.setDisable(true); // temporary
			heading.getChildren().add(jfxButtonConfigure);

			final BorderPane frame = new BorderPane();
			frame.setTop(heading);
			frame.setCenter(hbox);
			frame.setStyle("-fx-border-width: 1px; -fx-border-color: #B649C6; -fx-background-color: #333;");
			pane.add(frame, 2, 1, 1, 4);
			GridPane.setVgrow(pane, Priority.ALWAYS);
		}

		// EVENT CONTROL
		{
			HBox hbox = new HBox(4);
			hbox.setPadding(new Insets(4));
			hbox.setAlignment(Pos.TOP_CENTER);

			VBox statusBox = new VBox(4);
			statusBox.setSpacing(0);
			statusBox.setAlignment(Pos.TOP_LEFT);
			hbox.getChildren().add(statusBox);
			HBox.setHgrow(statusBox, Priority.ALWAYS);

			this.addEventSummaryToStatusBox(statusBox, "When sleep block is approaching", NMOConfiguration.instance.events.sleepBlockApproaching);
			this.addEventSummaryToStatusBox(statusBox, "When sleep block starts", NMOConfiguration.instance.events.sleepBlockStarted);
			this.addEventSummaryToStatusBox(statusBox, "When sleep block ends", NMOConfiguration.instance.events.sleepBlockEnded);
			this.addEventSummaryToStatusBox(statusBox, "On first activity warning", NMOConfiguration.instance.events.activityWarning1);
			this.addEventSummaryToStatusBox(statusBox, "On oversleep warning (activity warning " + NMOConfiguration.instance.oversleepWarningThreshold + ")", NMOConfiguration.instance.events.oversleepWarning);
			this.addEventSummaryToStatusBox(statusBox, "On all other warnings", NMOConfiguration.instance.events.activityWarning2);
			this.addEventSummaryToStatusBox(statusBox, "When manually pausing", NMOConfiguration.instance.events.pauseInitiated);
			this.addEventSummaryToStatusBox(statusBox, "When manually unpausing", NMOConfiguration.instance.events.pauseCancelled);
			this.addEventSummaryToStatusBox(statusBox, "When pause auto-expires", NMOConfiguration.instance.events.pauseExpired);
			for (CustomEventAction action : NMOConfiguration.instance.events.custom)
			{
				this.addEventSummaryToStatusBox(statusBox, action.name + " (" + action.describe() + ")", action.actions);
			}

			final HBox heading = JavaFxHelper.createHorizontalBox(Control.USE_COMPUTED_SIZE, 24);
			heading.setStyle("-fx-background-color: #6BA4A5;");
			heading.setPadding(new Insets(2));
			heading.setSpacing(2);
			final Label label = JavaFxHelper.createLabel("Event Control", Color.BLACK, "-fx-font-size: 11pt;");
			heading.getChildren().add(label);
			final StackPane spt = new StackPane();
			heading.getChildren().add(spt);
			HBox.setHgrow(spt, Priority.ALWAYS);
			heading.setAlignment(Pos.TOP_LEFT);
			final Button jfxButtonConfigure = JavaFxHelper.createButton("Configure", JavaFxHelper.createIcon(FontAwesomeIcon.COGS, "11", Color.BLACK));
			jfxButtonConfigure.setPadding(new Insets(2, 4, 2, 4));
			jfxButtonConfigure.setDisable(true); // temporary
			heading.getChildren().add(jfxButtonConfigure);

			final BorderPane frame = new BorderPane();
			frame.setTop(heading);
			frame.setCenter(hbox);
			frame.setStyle("-fx-border-width: 1px; -fx-border-color: #6BA4A5; -fx-background-color: #333;");
			pane.add(frame, 3, 1, 1, 4);
			GridPane.setVgrow(pane, Priority.ALWAYS);
		}

		// TWILIO
		{
			HBox hbox = new HBox(4);
			hbox.setPadding(new Insets(4));
			hbox.setAlignment(Pos.TOP_CENTER);

			VBox statusBox = new VBox(4);
			statusBox.setAlignment(Pos.TOP_LEFT);
			hbox.getChildren().add(statusBox);
			HBox.setHgrow(statusBox, Priority.ALWAYS);

			if (IntegrationTwilio.INSTANCE.isEnabled())
			{
				this.addIntegrationButtonsToVbox(IntegrationTwilio.INSTANCE, statusBox);
			}
			else
			{
				statusBox.getChildren().add(JavaFxHelper.createLabel("Integration disabled", Color.GRAY, "-fx-font-weight: bold;"));
			}

			final HBox heading = JavaFxHelper.createHorizontalBox(Control.USE_COMPUTED_SIZE, 24);
			heading.setStyle("-fx-background-color: #A36E6D;");
			heading.setPadding(new Insets(2));
			heading.setSpacing(2);
			final Label label = JavaFxHelper.createLabel("Twilio", Color.BLACK, "-fx-font-size: 11pt;");
			heading.getChildren().add(label);
			final StackPane spt = new StackPane();
			heading.getChildren().add(spt);
			HBox.setHgrow(spt, Priority.ALWAYS);
			heading.setAlignment(Pos.TOP_LEFT);
			final Button jfxButtonConfigure = JavaFxHelper.createButton("Configure", JavaFxHelper.createIcon(FontAwesomeIcon.COGS, "11", Color.BLACK));
			jfxButtonConfigure.setPadding(new Insets(2, 4, 2, 4));
			jfxButtonConfigure.setDisable(true); // temporary
			heading.getChildren().add(jfxButtonConfigure);

			final BorderPane frame = new BorderPane();
			frame.setTop(heading);
			frame.setCenter(hbox);
			frame.setStyle("-fx-border-width: 1px; -fx-border-color: #A36E6D; -fx-background-color: #333;");
			pane.add(frame, 0, 2, 1, 1);
			GridPane.setVgrow(pane, Priority.ALWAYS);
		}

		// CUSTOM COMMANDS
		{
			HBox hbox = new HBox(4);
			hbox.setPadding(new Insets(4));
			hbox.setAlignment(Pos.TOP_CENTER);

			VBox statusBox = new VBox(4);
			statusBox.setAlignment(Pos.TOP_LEFT);
			hbox.getChildren().add(statusBox);
			HBox.setHgrow(statusBox, Priority.ALWAYS);

			if (IntegrationCommandLine.INSTANCE.isEnabled())
			{
				this.addIntegrationButtonsToVbox(IntegrationCommandLine.INSTANCE, statusBox);
			}
			else
			{
				statusBox.getChildren().add(JavaFxHelper.createLabel("Integration disabled", Color.GRAY, "-fx-font-weight: bold;"));
			}

			final HBox heading = JavaFxHelper.createHorizontalBox(Control.USE_COMPUTED_SIZE, 24);
			heading.setStyle("-fx-background-color: #7BAD58;");
			heading.setPadding(new Insets(2));
			heading.setSpacing(2);
			final Label label = JavaFxHelper.createLabel("Custom Commands", Color.BLACK, "-fx-font-size: 11pt;");
			heading.getChildren().add(label);
			final StackPane spt = new StackPane();
			heading.getChildren().add(spt);
			HBox.setHgrow(spt, Priority.ALWAYS);
			heading.setAlignment(Pos.TOP_LEFT);
			final Button jfxButtonConfigure = JavaFxHelper.createButton("Configure", JavaFxHelper.createIcon(FontAwesomeIcon.COGS, "11", Color.BLACK));
			jfxButtonConfigure.setPadding(new Insets(2, 4, 2, 4));
			jfxButtonConfigure.setDisable(true); // temporary
			heading.getChildren().add(jfxButtonConfigure);

			final BorderPane frame = new BorderPane();
			frame.setTop(heading);
			frame.setCenter(hbox);
			frame.setStyle("-fx-border-width: 1px; -fx-border-color: #7BAD58; -fx-background-color: #333;");
			pane.add(frame, 1, 2, 1, 1);
			GridPane.setVgrow(pane, Priority.ALWAYS);
		}

		// DISCORD
		{
			HBox hbox = new HBox(4);
			hbox.setPadding(new Insets(4));
			hbox.setAlignment(Pos.TOP_CENTER);

			VBox statusBox = new VBox(4);
			statusBox.setAlignment(Pos.TOP_LEFT);
			hbox.getChildren().add(statusBox);
			HBox.setHgrow(statusBox, Priority.ALWAYS);

			if (IntegrationDiscord.INSTANCE.isEnabled())
			{
				this.addIntegrationButtonsToVbox(IntegrationDiscord.INSTANCE, statusBox);
			}
			else
			{
				statusBox.getChildren().add(JavaFxHelper.createLabel("Integration disabled", Color.GRAY, "-fx-font-weight: bold;"));
			}

			final HBox heading = JavaFxHelper.createHorizontalBox(Control.USE_COMPUTED_SIZE, 24);
			heading.setStyle("-fx-background-color: #7289DA;");
			heading.setPadding(new Insets(2));
			heading.setSpacing(2);
			final Label label = JavaFxHelper.createLabel("Discord", Color.BLACK, "-fx-font-size: 11pt;");
			heading.getChildren().add(label);
			final StackPane spt = new StackPane();
			heading.getChildren().add(spt);
			HBox.setHgrow(spt, Priority.ALWAYS);
			heading.setAlignment(Pos.TOP_LEFT);
			final Button jfxButtonConfigure = JavaFxHelper.createButton("Configure", JavaFxHelper.createIcon(FontAwesomeIcon.COGS, "11", Color.BLACK));
			jfxButtonConfigure.setPadding(new Insets(2, 4, 2, 4));
			jfxButtonConfigure.setDisable(true); // temporary
			heading.getChildren().add(jfxButtonConfigure);

			final BorderPane frame = new BorderPane();
			frame.setTop(heading);
			frame.setCenter(hbox);
			frame.setStyle("-fx-border-width: 1px; -fx-border-color: #7289DA; -fx-background-color: #333;");
			pane.add(frame, 0, 3, 1, 1);
			GridPane.setVgrow(pane, Priority.ALWAYS);
		}

		// FILEWRITER
		{
			HBox hbox = new HBox(4);
			hbox.setPadding(new Insets(4));
			hbox.setAlignment(Pos.TOP_CENTER);

			VBox statusBox = new VBox(4);
			statusBox.setAlignment(Pos.TOP_LEFT);
			hbox.getChildren().add(statusBox);
			HBox.setHgrow(statusBox, Priority.ALWAYS);

			if (IntegrationFileWriter.INSTANCE.isEnabled())
			{
				final Label scheduleName = JavaFxHelper.createLabel("scheduleName: ", Color.WHITE, "-fx-font-weight: bold;");
				if (NMOConfiguration.instance.integrations.fileWriter.scheduleName)
				{
					scheduleName.setGraphic(JavaFxHelper.createLabel("ENABLED", Color.LIME));
					scheduleName.setContentDisplay(ContentDisplay.RIGHT);
				}
				else
				{
					scheduleName.setGraphic(JavaFxHelper.createLabel("DISABLED", Color.RED));
					scheduleName.setContentDisplay(ContentDisplay.RIGHT);
				}
				statusBox.getChildren().add(scheduleName);

				final Label scheduleStartedOn = JavaFxHelper.createLabel("scheduleStartedOn: ", Color.WHITE, "-fx-font-weight: bold;");
				if (NMOConfiguration.instance.integrations.fileWriter.scheduleStartedOn)
				{
					scheduleStartedOn.setGraphic(JavaFxHelper.createLabel("ENABLED", Color.LIME));
					scheduleStartedOn.setContentDisplay(ContentDisplay.RIGHT);
				}
				else
				{
					scheduleStartedOn.setGraphic(JavaFxHelper.createLabel("DISABLED", Color.RED));
					scheduleStartedOn.setContentDisplay(ContentDisplay.RIGHT);
				}
				statusBox.getChildren().add(scheduleStartedOn);

				final Label scheduleLastOversleep = JavaFxHelper.createLabel("scheduleLastOversleep: ", Color.WHITE, "-fx-font-weight: bold;");
				if (NMOConfiguration.instance.integrations.fileWriter.scheduleLastOversleep)
				{
					scheduleLastOversleep.setGraphic(JavaFxHelper.createLabel("ENABLED", Color.LIME));
					scheduleLastOversleep.setContentDisplay(ContentDisplay.RIGHT);
				}
				else
				{
					scheduleLastOversleep.setGraphic(JavaFxHelper.createLabel("DISABLED", Color.RED));
					scheduleLastOversleep.setContentDisplay(ContentDisplay.RIGHT);
				}
				statusBox.getChildren().add(scheduleLastOversleep);

				final Label schedulePersonalBest = JavaFxHelper.createLabel("schedulePersonalBest: ", Color.WHITE, "-fx-font-weight: bold;");
				if (NMOConfiguration.instance.integrations.fileWriter.schedulePersonalBest)
				{
					schedulePersonalBest.setGraphic(JavaFxHelper.createLabel("ENABLED", Color.LIME));
					schedulePersonalBest.setContentDisplay(ContentDisplay.RIGHT);
				}
				else
				{
					schedulePersonalBest.setGraphic(JavaFxHelper.createLabel("DISABLED", Color.RED));
					schedulePersonalBest.setContentDisplay(ContentDisplay.RIGHT);
				}
				statusBox.getChildren().add(schedulePersonalBest);

				final Label timeToNextSleepBlock = JavaFxHelper.createLabel("timeToNextSleepBlock: ", Color.WHITE, "-fx-font-weight: bold;");
				if (NMOConfiguration.instance.integrations.fileWriter.timeToNextSleepBlock)
				{
					timeToNextSleepBlock.setGraphic(JavaFxHelper.createLabel("ENABLED", Color.LIME));
					timeToNextSleepBlock.setContentDisplay(ContentDisplay.RIGHT);
				}
				else
				{
					timeToNextSleepBlock.setGraphic(JavaFxHelper.createLabel("DISABLED", Color.RED));
					timeToNextSleepBlock.setContentDisplay(ContentDisplay.RIGHT);
				}
				statusBox.getChildren().add(timeToNextSleepBlock);
			}
			else
			{
				statusBox.getChildren().add(JavaFxHelper.createLabel("Integration disabled", Color.GRAY, "-fx-font-weight: bold;"));
			}

			final HBox heading = JavaFxHelper.createHorizontalBox(Control.USE_COMPUTED_SIZE, 24);
			heading.setStyle("-fx-background-color: #AA3456;");
			heading.setPadding(new Insets(2));
			heading.setSpacing(2);
			final Label label = JavaFxHelper.createLabel("File Writer", Color.BLACK, "-fx-font-size: 11pt;");
			heading.getChildren().add(label);
			final StackPane spt = new StackPane();
			heading.getChildren().add(spt);
			HBox.setHgrow(spt, Priority.ALWAYS);
			heading.setAlignment(Pos.TOP_LEFT);
			final Button jfxButtonConfigure = JavaFxHelper.createButton("Configure", JavaFxHelper.createIcon(FontAwesomeIcon.COGS, "11", Color.BLACK));
			jfxButtonConfigure.setPadding(new Insets(2, 4, 2, 4));
			jfxButtonConfigure.setDisable(true); // temporary
			heading.getChildren().add(jfxButtonConfigure);

			final BorderPane frame = new BorderPane();
			frame.setTop(heading);
			frame.setCenter(hbox);
			frame.setStyle("-fx-border-width: 1px; -fx-border-color: #AA3456; -fx-background-color: #333;");
			pane.add(frame, 0, 4, 1, 1);
			GridPane.setVgrow(pane, Priority.ALWAYS);
		}

		// RANDOMIZER
		{
			HBox hbox = new HBox(4);
			hbox.setPadding(new Insets(4));
			hbox.setAlignment(Pos.TOP_CENTER);

			VBox statusBox = new VBox(4);
			statusBox.setAlignment(Pos.TOP_LEFT);
			hbox.getChildren().add(statusBox);
			HBox.setHgrow(statusBox, Priority.ALWAYS);

			if (IntegrationRandomizer.INSTANCE.isEnabled())
			{
				this.addIntegrationButtonsToVbox(IntegrationRandomizer.INSTANCE, statusBox);
			}
			else
			{
				statusBox.getChildren().add(JavaFxHelper.createLabel("Integration disabled", Color.GRAY, "-fx-font-weight: bold;"));
			}

			final HBox heading = JavaFxHelper.createHorizontalBox(Control.USE_COMPUTED_SIZE, 24);
			heading.setStyle("-fx-background-color: #D88B43;");
			heading.setPadding(new Insets(2));
			heading.setSpacing(2);
			final Label label = JavaFxHelper.createLabel("Randomizer", Color.BLACK, "-fx-font-size: 11pt;");
			heading.getChildren().add(label);
			final StackPane spt = new StackPane();
			heading.getChildren().add(spt);
			HBox.setHgrow(spt, Priority.ALWAYS);
			heading.setAlignment(Pos.TOP_LEFT);
			final Button jfxButtonConfigure = JavaFxHelper.createButton("Configure", JavaFxHelper.createIcon(FontAwesomeIcon.COGS, "11", Color.BLACK));
			jfxButtonConfigure.setPadding(new Insets(2, 4, 2, 4));
			jfxButtonConfigure.setDisable(true); // temporary
			heading.getChildren().add(jfxButtonConfigure);

			final BorderPane frame = new BorderPane();
			frame.setTop(heading);
			frame.setCenter(hbox);
			frame.setStyle("-fx-border-width: 1px; -fx-border-color: #D88B43; -fx-background-color: #333;");
			pane.add(frame, 1, 3, 1, 2);
			GridPane.setVgrow(pane, Priority.ALWAYS);
		}

		// build the log frame
		{
			final ListView<String> listView = new ListView<String>(events);
			listView.getItems().addListener(new ListChangeListener<String>()
			{
				@Override
				public void onChanged(javafx.collections.ListChangeListener.Change<? extends String> c)
				{
					listView.scrollTo(c.getList().size() - 1);
				}
			});
			listView.setMinHeight(216);
			listView.setMaxHeight(216);
			innerPane.setBottom(listView);
		}
	}

	protected void openPauseDialog(final String hm, final int pp)
	{
		final Stage dialog = new Stage();
		dialog.setTitle("Pause for " + hm);
		dialog.getIcons().add(new Image(JavaFxHelper.buildResourcePath("icon.png")));
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.initOwner(scene.getWindow());
		dialog.setResizable(false);
		BorderPane outerPane = new BorderPane();
		outerPane.setId("root");
		outerPane.setStyle("-fx-background-color: #222;");
		StackPane stopBg = new StackPane();
		stopBg.setPadding(new Insets(10));
		StackPane stopBg2 = new StackPane();
		stopBg2.setStyle("-fx-background-color: #990000; -fx-border-size: 2px; -fx-border-color: white;");
		stopBg2.setMinHeight(200);
		stopBg2.setMaxHeight(200);
		stopBg.getChildren().add(stopBg2);
		Label stopLabel = JavaFxHelper.createIconLabel(FontAwesomeIcon.EXCLAMATION_TRIANGLE, "72", " STOP AND THINK !!", ContentDisplay.LEFT, Color.WHITE, "-fx-font-size: 72px;");
		stopLabel.setPadding(new Insets(0, 0, 70, 0));
		Label stopLabel2 = JavaFxHelper.createLabel("DO YOU REALLY NEED TO PAUSE FOR " + hm + "?", Color.WHITE, "-fx-font-size: 24px;");
		stopLabel2.setPadding(new Insets(52, 0, 0, 0));
		Label stopLabel3 = JavaFxHelper.createLabel("*** FAILING YOUR SCHEDULE IS JUST ONE STUPID PAUSE AWAY ***", Color.WHITE, "-fx-font-size: 24px; -fx-font-weight: bold;");
		stopLabel3.setPadding(new Insets(130, 0, 0, 0));
		stopBg2.getChildren().add(stopLabel);
		stopBg2.getChildren().add(stopLabel2);
		stopBg2.getChildren().add(stopLabel3);
		outerPane.setTop(stopBg);
		final Captcha captcha = new Captcha.Builder(200, 50).addText().addBackground(new GradiatedBackgroundProducer()).addNoise().gimp(new FishEyeGimpyRenderer()).addBorder().build();
		WritableImage wimg = new WritableImage(200, 50);
		SwingFXUtils.toFXImage(captcha.getImage(), wimg);
		ImageView captchaImageView = new ImageView();
		captchaImageView.setImage(wimg);
		captchaImageView.setPreserveRatio(true);
		GridPane center = new GridPane();
		center.setPadding(new Insets(6, 16, 6, 16));
		center.setAlignment(Pos.TOP_LEFT);
		center.setStyle("-fx-font-size: 16px");
		center.setVgap(16);
		center.getColumnConstraints().add(new ColumnConstraints(150));
		center.getColumnConstraints().add(new ColumnConstraints(220));
		center.getColumnConstraints().add(new ColumnConstraints(410));
		Label a = JavaFxHelper.createLabel("If you are ABSOLUTELY SURE you need to pause...", Color.WHITE, "-fx-font-weight: bold;");
		center.addRow(0, a);
		GridPane.setColumnSpan(a, 2);
		final TextField reason = new TextField();
		Label b = JavaFxHelper.createLabel("Input pause reason:", Color.WHITE);
		GridPane.setColumnSpan(reason, 2);
		center.addRow(1, b, reason);
		Label c = JavaFxHelper.createLabel("Solve this captcha:", Color.WHITE);
		final TextField captchaField = new TextField();
		center.addRow(2, c, captchaImageView, captchaField);
		outerPane.setCenter(center);
		Separator s = new Separator(Orientation.HORIZONTAL);
		GridPane.setColumnSpan(s, 3);
		center.addRow(3, s);
		BooleanBinding bb = new BooleanBinding()
		{
			{
				super.bind(reason.textProperty(), captchaField.textProperty());
			}

			@Override
			protected boolean computeValue()
			{
				return (reason.getText().isEmpty() || !captchaField.getText().equals(captcha.getAnswer()));
			}
		};
		ButtonBar buttonBar = new ButtonBar();
		final Button okButton = new Button("Confirm pause");
		okButton.disableProperty().bind(bb);
		ButtonBar.setButtonData(okButton, ButtonData.OK_DONE);
		buttonBar.getButtons().addAll(okButton);
		buttonBar.setPadding(new Insets(0, 16, 16, 0));
		buttonBar.setStyle("-fx-font-size: 16px;");
		okButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				long now = System.currentTimeMillis();
				pausedUntil = now + (pp * 60000);
				pauseReason = reason.getText();
				triggerEvent("Paused for " + hm + " (until " + CommonUtils.dateFormatter.format(pausedUntil) + ") for \"" + pauseReason + "\"", NMOConfiguration.instance.events.pauseInitiated);
				dialog.close();
			}
		});
		reason.setOnKeyPressed(new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent ke)
			{
				if (ke.getCode().equals(KeyCode.ENTER))
				{
					if (!okButton.isDisabled())
					{
						okButton.fire();
					}
				}
			}
		});
		captchaField.setOnKeyPressed(new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent ke)
			{
				if (ke.getCode().equals(KeyCode.ENTER))
				{
					if (!okButton.isDisabled())
					{
						okButton.fire();
					}
				}
			}
		});
		outerPane.setBottom(buttonBar);
		Scene dialogScene = new Scene(outerPane, 800, 430, Color.WHITE);
		dialogScene.getStylesheets().add(JavaFxHelper.buildResourcePath("application.css"));
		dialog.setScene(dialogScene);
		dialog.showAndWait();
	}

	protected void openAppCloseDialog()
	{
		final Stage dialog = new Stage();
		dialog.setTitle("Close NMO");
		dialog.getIcons().add(new Image(JavaFxHelper.buildResourcePath("icon.png")));
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.initOwner(scene.getWindow());
		dialog.setResizable(false);
		BorderPane outerPane = new BorderPane();
		outerPane.setId("root");
		outerPane.setStyle("-fx-background-color: #222;");
		StackPane stopBg = new StackPane();
		stopBg.setPadding(new Insets(10));
		StackPane stopBg2 = new StackPane();
		stopBg2.setStyle("-fx-background-color: #990000; -fx-border-size: 2px; -fx-border-color: white;");
		stopBg2.setMinHeight(200);
		stopBg2.setMaxHeight(200);
		stopBg.getChildren().add(stopBg2);
		Label stopLabel = JavaFxHelper.createIconLabel(FontAwesomeIcon.EXCLAMATION_TRIANGLE, "72", " STOP AND THINK !!", ContentDisplay.LEFT, Color.WHITE, "-fx-font-size: 72px;");
		stopLabel.setPadding(new Insets(0, 0, 70, 0));
		Label stopLabel2 = JavaFxHelper.createLabel("IS CLOSING NMO A GOOD IDEA?", Color.WHITE, "-fx-font-size: 24px;");
		stopLabel2.setPadding(new Insets(52, 0, 0, 0));
		Label stopLabel3 = JavaFxHelper.createLabel("*** FAILING YOUR SCHEDULE IS VERY LIKELY IF NMO IS CLOSED ***", Color.WHITE, "-fx-font-size: 24px; -fx-font-weight: bold;");
		stopLabel3.setPadding(new Insets(130, 0, 0, 0));
		stopBg2.getChildren().add(stopLabel);
		stopBg2.getChildren().add(stopLabel2);
		stopBg2.getChildren().add(stopLabel3);
		outerPane.setTop(stopBg);
		final Captcha captcha = new Captcha.Builder(200, 50).addText().addBackground(new GradiatedBackgroundProducer()).addNoise().gimp(new FishEyeGimpyRenderer()).addBorder().build();
		WritableImage wimg = new WritableImage(200, 50);
		SwingFXUtils.toFXImage(captcha.getImage(), wimg);
		ImageView captchaImageView = new ImageView();
		captchaImageView.setImage(wimg);
		captchaImageView.setPreserveRatio(true);
		GridPane center = new GridPane();
		center.setPadding(new Insets(6, 16, 6, 16));
		center.setAlignment(Pos.TOP_LEFT);
		center.setStyle("-fx-font-size: 16px");
		center.setVgap(16);
		center.getColumnConstraints().add(new ColumnConstraints(150));
		center.getColumnConstraints().add(new ColumnConstraints(220));
		center.getColumnConstraints().add(new ColumnConstraints(410));
		Label a = JavaFxHelper.createLabel("If you are ABSOLUTELY SURE you need to close NMO...", Color.WHITE, "-fx-font-weight: bold;");
		center.addRow(0, a);
		GridPane.setColumnSpan(a, 2);
		Label c = JavaFxHelper.createLabel("Solve this captcha:", Color.WHITE);
		final TextField captchaField = new TextField();
		center.addRow(1, c, captchaImageView, captchaField);
		outerPane.setCenter(center);
		Separator s = new Separator(Orientation.HORIZONTAL);
		GridPane.setColumnSpan(s, 3);
		center.addRow(2, s);
		BooleanBinding bb = new BooleanBinding()
		{
			{
				super.bind(captchaField.textProperty());
			}

			@Override
			protected boolean computeValue()
			{
				return !captchaField.getText().equals(captcha.getAnswer());
			}
		};
		ButtonBar buttonBar = new ButtonBar();
		final Button okButton = new Button("Confirm close");
		okButton.disableProperty().bind(bb);
		ButtonBar.setButtonData(okButton, ButtonData.OK_DONE);
		buttonBar.getButtons().addAll(okButton);
		buttonBar.setPadding(new Insets(0, 16, 16, 0));
		buttonBar.setStyle("-fx-font-size: 16px;");
		okButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				Platform.exit();
			}
		});
		captchaField.setOnKeyPressed(new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent ke)
			{
				if (ke.getCode().equals(KeyCode.ENTER))
				{
					if (!okButton.isDisabled())
					{
						okButton.fire();
					}
				}
			}
		});
		outerPane.setBottom(buttonBar);
		Scene dialogScene = new Scene(outerPane, 800, 380, Color.WHITE);
		dialogScene.getStylesheets().add(JavaFxHelper.buildResourcePath("application.css"));
		dialog.setScene(dialogScene);
		dialog.showAndWait();
	}

	private void addEventSummaryToStatusBox(VBox statusBox, String description, String[] eventTriggers)
	{
		if (eventTriggers.length != 0)
		{
			statusBox.getChildren().add(JavaFxHelper.createLabel(description + ":", Color.WHITE, "-fx-font-weight: bold;"));
			for (int i = 0; i < eventTriggers.length; i++)
			{
				// get the description
				String desc = null;
				for (Integration integration : Main.integrations)
				{
					Action action = integration.getActions().get(eventTriggers[i]);
					if (action != null)
					{
						desc = action.getName();
						break;
					}
				}
				if (desc == null)
				{
					statusBox.getChildren().add(JavaFxHelper.createLabel(eventTriggers[i], Color.RED, "", new Insets(0, 0, 0, 16)));
				}
				else
				{
					statusBox.getChildren().add(JavaFxHelper.createLabel(desc, Color.LIME, "", new Insets(0, 0, 0, 16)));
				}
			}
		}
	}

	protected void tick()
	{
		tick++;
		if (tick >= NMOConfiguration.instance.garbageCollectionFrequency)
		{
			tick -= NMOConfiguration.instance.garbageCollectionFrequency;
			System.gc();
		}
		if (tick % 2 == 1)
		{
			return;
		}

		long now = System.currentTimeMillis();
		boolean paused = pausedUntil > now;
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int currentMinuteOfDay = ((hour * 60) + minute);

		if (NMOConfiguration.instance.scheduleStartedOn > 0)
		{
			startedString.set(CommonUtils.dateFormatter2.format(NMOConfiguration.instance.scheduleStartedOn));
			startedString2.set("(" + MainDialog.formatTimeElapsedWithDays(now, NMOConfiguration.instance.scheduleStartedOn) + " ago)");
			lastOversleepString.set(CommonUtils.dateFormatter2.format(NMOConfiguration.instance.scheduleLastOversleep));
			lastOversleepString2.set("(" + MainDialog.formatTimeElapsedWithDays(now, NMOConfiguration.instance.scheduleLastOversleep) + " ago)");
			if ((now - NMOConfiguration.instance.scheduleLastOversleep) > NMOConfiguration.instance.schedulePersonalBest)
			{
				NMOConfiguration.instance.schedulePersonalBest = now - NMOConfiguration.instance.scheduleLastOversleep;
			}
			personalBestString.set(MainDialog.formatTimeElapsedWithDays(NMOConfiguration.instance.schedulePersonalBest, 0));
		}

		SleepEntry nextSleepBlockDetected = null;
		for (SleepEntry entry : NMOConfiguration.instance.schedule)
		{
			if (entry.containsTime(currentMinuteOfDay) || entry.start >= currentMinuteOfDay)
			{
				nextSleepBlockDetected = entry;
				break;
			}
		}
		if (nextSleepBlockDetected == null && !NMOConfiguration.instance.schedule.isEmpty())
		{
			nextSleepBlockDetected = NMOConfiguration.instance.schedule.get(0);
		}
		if (nextSleepBlockDetected != null)
		{
			if (nextSleepBlockDetected.containsTime(currentMinuteOfDay))
			{
				Calendar calendar2 = Calendar.getInstance();
				calendar2.set(Calendar.HOUR_OF_DAY, nextSleepBlockDetected.end / 60);
				calendar2.set(Calendar.MINUTE, nextSleepBlockDetected.end % 60);
				calendar2.set(Calendar.SECOND, 0);
				calendar2.set(Calendar.MILLISECOND, 0);
				long tims = calendar2.getTimeInMillis();
				if (nextSleepBlockDetected.end < currentMinuteOfDay)
				{
					tims += 86400000L; // nap loops over to next day. add 1 day.
				}
				if (!scheduleStatus.startsWith("SLEEPING"))
				{
					triggerEvent("Entering sleep block: " + nextSleepBlockDetected.name, NMOConfiguration.instance.events.sleepBlockStarted);
				}
				long minutesRemaining = (((tims + 59999) - System.currentTimeMillis()) / 60000);
				scheduleStatus = "SLEEPING [" + nextSleepBlockDetected.name + "] UNTIL " + CommonUtils.convertTimestamp(tims);
				scheduleStatusShort = "SLEEPING [" + minutesRemaining + "m LEFT]";
				nextSleepBlock = nextSleepBlockDetected;
				if (!paused)
				{
					triggerEvent("Automatically pausing until " + CommonUtils.convertTimestamp(tims) + " due to sleep block '" + nextSleepBlockDetected.name + "' having started", null);
					paused = true;
					pausedUntil = tims;
					pauseReason = "Sleep block: " + nextSleepBlockDetected.name;
				}
			}
			else
			{
				Calendar calendar2 = Calendar.getInstance();
				calendar2.set(Calendar.HOUR_OF_DAY, nextSleepBlockDetected.start / 60);
				calendar2.set(Calendar.MINUTE, nextSleepBlockDetected.start % 60);
				calendar2.set(Calendar.SECOND, 0);
				calendar2.set(Calendar.MILLISECOND, 0);
				long tims = calendar2.getTimeInMillis();
				if (nextSleepBlockDetected.start < currentMinuteOfDay)
				{
					tims += 86400000L; // nap loops over to next day. add 1 day.
				}
				long minutesRemaining = (((tims + 59999) - System.currentTimeMillis()) / 60000);
				String pros = nextActivityWarningID >= NMOConfiguration.instance.oversleepWarningThreshold ? "OVERSLEEPING" : nextActivityWarningID > 0 ? "MISSING" : "AWAKE";
				scheduleStatus = pros + " [" + nextSleepBlockDetected.name + " STARTS IN " + minutesRemaining + " MINUTE" + (minutesRemaining == 1 ? "" : "S") + "]";
				scheduleStatusShort = pros.equals("AWAKE") ? pros + " [" + minutesRemaining + "m LEFT]" : pros;
				if (minutesRemaining <= nextSleepBlockDetected.approachWarning && lastSleepBlockWarning != nextSleepBlockDetected)
				{
					if (nextSleepBlockDetected.approachWarning != -1)
					{
						triggerEvent(minutesRemaining + " minute" + (minutesRemaining == 1 ? "" : "s") + " until next sleep block", NMOConfiguration.instance.events.sleepBlockApproaching);
					}
					lastSleepBlockWarning = nextSleepBlockDetected;
				}
			}
		}
		else
		{
			scheduleStatusShort = "UNCONFIGURED";
		}
		if (nextSleepBlock != null && nextSleepBlock != nextSleepBlockDetected)
		{
			triggerEvent("Exiting sleep block: " + nextSleepBlock.name, NMOConfiguration.instance.events.sleepBlockEnded);
		}
		boolean wasPaused = isCurrentlyPaused.get();
		isCurrentlyPaused.set(paused);
		if (!paused && wasPaused)
		{
			triggerEvent("Unpaused automatically - time alotted for \"" + pauseReason + "\" has expired", NMOConfiguration.instance.events.pauseExpired);
		}
		if (nextSleepBlockDetected != null && nextSleepBlock != nextSleepBlockDetected)
		{
			nextSleepBlock = nextSleepBlockDetected;

			Calendar calendar3 = Calendar.getInstance();
			calendar3.set(Calendar.HOUR_OF_DAY, nextSleepBlockDetected.start / 60);
			calendar3.set(Calendar.MINUTE, nextSleepBlockDetected.start % 60);
			calendar3.set(Calendar.SECOND, 0);
			calendar3.set(Calendar.MILLISECOND, 0);
			long tims = calendar3.getTimeInMillis();
			if (nextSleepBlockDetected.start < currentMinuteOfDay)
			{
				tims += 86400000L; // nap loops over to next day. add 1 day.
			}
			long minutesRemaining = (((tims + 59999) - System.currentTimeMillis()) / 60000);
			triggerEvent("The next sleep block is " + nextSleepBlockDetected.name + " which starts in " + minutesRemaining + " minute" + (minutesRemaining == 1 ? "" : "s"), null);
		}
		if (paused)
		{
			if (!(scheduleStatusShort.startsWith("SLEEPING ") && pauseReason.startsWith("Sleep block: ")))
			{
				long minutesRemaining = (((pausedUntil + 59999) - System.currentTimeMillis()) / 60000);
				scheduleStatusShort = "\"" + pauseReason + "\" [" + minutesRemaining + "m LEFT]";
			}
		}

		for (Integration integration : Main.integrations)
		{
			if (integration.isEnabled())
			{
				try
				{
					integration.update();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		if (paused)
		{
			resetActivityTimer("pause");
		}
		long timeDiff = paused ? 0 : (now - lastActivityTime);
		if (pendingTimer != null)
		{
			this.setNextActivityWarningForTimer(pendingTimer, timeDiff);
			pendingTimer = null;
		}
		if (paused)
		{
			lastActivityTimeString.set("PAUSED for \"" + pauseReason + "\"");
			timeDiffString.set("   until " + CommonUtils.dateFormatter.format(pausedUntil));
		}
		else
		{
			lastActivityTimeString.set("Last: " + CommonUtils.dateFormatter.format(lastActivityTime) + " (" + lastActivitySource + ")");
			long nawtd = getNextActivityWarningTimeDiff(nextActivityWarningID);
			if (timeDiff > (1000 * nawtd))
			{
				this.setNextActivityWarningForTimer(timer, timeDiff);
				try
				{
					String pros = nextActivityWarningID >= NMOConfiguration.instance.oversleepWarningThreshold ? "OVERSLEEPING" : nextActivityWarningID >= 0 ? "MISSING" : "AWAKE";
					// the first time, you get an alternative lighter warning, just in case you forgot to pause
					if (nextActivityWarningID == 1)
					{
						triggerEvent(pros + "(" + nextActivityWarningID + "): No activity detected for " + nawtd + " seconds", NMOConfiguration.instance.events.activityWarning1);
					}
					else if (nextActivityWarningID == NMOConfiguration.instance.oversleepWarningThreshold)
					{
						triggerEvent(pros + "(" + nextActivityWarningID + "): No activity detected for " + nawtd + " seconds", NMOConfiguration.instance.events.oversleepWarning);
					}
					else
					{
						triggerEvent(pros + "(" + nextActivityWarningID + "): No activity detected for " + nawtd + " seconds", NMOConfiguration.instance.events.activityWarning2);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			timeDiffString.set("Time difference: " + timeDiff + " (next warning: " + nawtd + "s)");
		}
		activeTimerString.set("Active timer:   " + timer.name + " (" + timer.secondsForFirstWarning + "s/" + timer.secondsForSubsequentWarnings + "s)");

		if (NMOConfiguration.instance.integrations.pavlok.enabled)
		{
			loginTokenValidUntilString.set("Login expires: " + CommonUtils.dateFormatter.format(1000 * (NMOConfiguration.instance.integrations.pavlok.auth.created_at + NMOConfiguration.instance.integrations.pavlok.auth.expires_in)));
		}

		if (NMOConfiguration.instance.integrations.philipsHue.enabled)
		{
			lightingStateString.set("LIGHTING: " + (IntegrationPhilipsHue.INSTANCE.lightState > -1 ? "ON, LIGHT LEVEL " + IntegrationPhilipsHue.INSTANCE.lightState : "OFF"));
		}

		if (NMOConfiguration.instance.integrations.webUI.enabled)
		{
			webMonitoringString.set(WebcamCapture.count() + " active web sockets");
			try
			{
				BufferedImage img = WebcamCapture.getImage();
				if (img != null && tick % 4 < 2)
				{
					writableImage = SwingFXUtils.toFXImage(img, writableImage);
					img.flush();
					lastWebcamImage.set(writableImage);
				}
				webcamName.set(WebcamCapture.getCameraName());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		// trigger custom actions
		if (!customActions.isEmpty())
		{
			CustomEventAction cea = customActions.get(0);
			while (cea.nextTriggerTime < now)
			{
				triggerEvent("Custom action: " + cea.name, cea.actions);
				cea.updateNextTriggerTime();
				triggerEvent("Action will next occur on " + CommonUtils.convertTimestamp(cea.nextTriggerTime), null);
				Collections.sort(customActions);
				cea = customActions.get(0);
			}
		}
	}

	public static String formatTimeElapsedWithDays(long now, long time)
	{
		now = (now / 1000) * 1000;
		time = (time / 1000) * 1000;
		long elapsed = Math.max(0, now - time);
		long days = elapsed / 86400000;
		elapsed = elapsed - (days * 86400000);
		long hours = elapsed / 3600000;
		elapsed = elapsed - (hours * 3600000);
		long minutes = elapsed / 60000;
		elapsed = elapsed - (minutes * 60000);
		//long seconds = elapsed / 1000;

		return String.format("%01dd %01dh %01dm", days, hours, minutes);
	}

	public static String formatTimeElapsedWithoutDays(long now, long time)
	{
		now = (now / 1000) * 1000;
		time = (time / 1000) * 1000;
		long elapsed = Math.max(0, now - time);
		//long days = elapsed / 86400000;
		//elapsed = elapsed - (days * 86400000);
		long hours = elapsed / 3600000;
		elapsed = elapsed - (hours * 3600000);
		long minutes = elapsed / 60000;
		elapsed = elapsed - (minutes * 60000);
		//long seconds = elapsed / 1000;

		return String.format("%01dh %01dm", hours, minutes);
	}

	private void setNextActivityWarningForTimer(ActivityTimer activityWarningTimer, long timeDiff)
	{
		MainDialog.timer = activityWarningTimer;
		if (timeDiff == 0)
		{
			nextActivityWarningID = 0;
		}
		else if (timeDiff == -1)
		{
			timeDiff = System.currentTimeMillis() - lastActivityTime;
		}
		long awid = 0;
		while (timeDiff > (1000 * getNextActivityWarningTimeDiff(awid))) // fixes shortening the gap in the middle of inactivity causing massive warning spam
		{
			awid++;
		}
		nextActivityWarningID = awid;
	}

	public static long getNextActivityWarningTimeDiff(long awid)
	{
		return timer.secondsForFirstWarning + (awid * timer.secondsForSubsequentWarnings);
	}

	public static void resetActivityTimer(String source)
	{
		lastActivityTime = System.currentTimeMillis();
		lastActivitySource = source;
		nextActivityWarningID = 0;
	}

	public static void triggerEvent(String eventDescription, String[] actionArray)
	{
		ArrayList<Action> actionsByLookup = new ArrayList<Action>();
		String actionString = "";
		if (actionArray != null)
		{
			actionLoop: for (String action : actionArray)
			{
				for (Integration integration : Main.integrations)
				{
					LinkedHashMap<String, Action> iactions = integration.getActions();
					Action aaction = iactions.get(action);
					if (aaction != null)
					{
						actionsByLookup.add(aaction);
						actionString += (actionString.isEmpty() ? "" : ", ") + aaction.getName();
						continue actionLoop;
					}
				}
			}
		}
		final String eventString = (actionString.isEmpty() ? "" : "<" + actionString + "> ") + eventDescription;
		log.info("APPEVENT: " + eventString);
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				events.add(CommonUtils.dateFormatter.format(System.currentTimeMillis()) + ": " + eventString);
			}
		});
		if (actionArray != null)
		{
			for (Action aaction : actionsByLookup)
			{
				try
				{
					aaction.onAction();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
