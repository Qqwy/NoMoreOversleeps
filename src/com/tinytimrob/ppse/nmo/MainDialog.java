package com.tinytimrob.ppse.nmo;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import com.tinytimrob.common.CommonUtils;
import com.tinytimrob.common.Configuration;
import com.tinytimrob.common.LogWrapper;
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
import com.tinytimrob.ppse.nmo.utils.DesktopHelper;
import com.tinytimrob.ppse.nmo.utils.JavaFxHelper;
import com.tinytimrob.ppse.nmo.ws.WebcamWebSocketHandler;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

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
	public static volatile SimpleBooleanProperty isCurrentlyPaused = new SimpleBooleanProperty(false);
	public static volatile SimpleObjectProperty<Image> lastWebcamImage = new SimpleObjectProperty<Image>();
	public static volatile SleepEntry lastSleepBlockWarning = null;
	public static volatile SleepEntry nextSleepBlock = null;
	public static volatile String scheduleStatus = "No schedule configured";
	public static volatile WritableImage writableImage = null;
	public static ObservableList<String> events = FXCollections.observableArrayList();
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
						if (location.startsWith(Main.CLIENT_CALLBACK) && location.contains("?code"))
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
			jfxButton.setMinWidth(250);
			jfxButton.setMaxWidth(250);
			jfxButton.setAlignment(Pos.BASELINE_LEFT);
			jfxButton.setContentDisplay(ContentDisplay.RIGHT);
			jfxButton.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent arg0)
				{
					try
					{
						clickableButton.onAction();
						triggerEvent("<" + clickableButton.getName() + "> from frontend", null);
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
		pane.setPadding(new Insets(10, 10, 10, 10));
		pane.setHgap(10);
		pane.setVgap(10);
		ColumnConstraints none = new ColumnConstraints();
		none.setHgrow(Priority.ALWAYS);
		ColumnConstraints c300 = new ColumnConstraints();
		c300.setMinWidth(340);
		c300.setMaxWidth(340);
		pane.getColumnConstraints().addAll(none, none, none, c300);
		innerPane.setCenter(pane);

		// schedule
		{
			HBox hbox = new HBox(6);
			hbox.setPadding(new Insets(6));
			hbox.setAlignment(Pos.TOP_CENTER);

			VBox statusBox = new VBox(6);
			statusBox.setSpacing(1);
			statusBox.setAlignment(Pos.TOP_LEFT);
			hbox.getChildren().add(statusBox);
			HBox.setHgrow(statusBox, Priority.ALWAYS);

			statusBox.getChildren().add(JavaFxHelper.createLabel(NMOConfiguration.instance.scheduleName, Color.WHITE, "-fx-font-weight: bold;"));

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
		}

		// monitoring control
		{
			HBox hbox = new HBox(6);
			hbox.setPadding(new Insets(6));
			hbox.setAlignment(Pos.TOP_CENTER);

			VBox statusBox = new VBox(6);
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

			final Label webMonitoringLabel = JavaFxHelper.createLabel("", Color.WHITE);
			webMonitoringLabel.textProperty().bind(webMonitoringString);
			statusBox.getChildren().add(webMonitoringLabel);

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
						TextInputDialog dialog = new TextInputDialog("");
						dialog.setTitle("Pause for " + hm);
						dialog.setContentText("Please input why you are pausing:");

						// Traditional way to get the response value.
						Optional<String> result = dialog.showAndWait();
						if (result.isPresent() && !result.get().isEmpty())
						{
							long now = System.currentTimeMillis();
							pausedUntil = now + (pp * 60000);
							pauseReason = result.get();
							triggerEvent("Paused for " + hm + " (until " + CommonUtils.dateFormatter.format(pausedUntil) + ") for \"" + pauseReason + "\"", NMOConfiguration.instance.events.pauseInitiated);
						}
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
						String hostname = NMOConfiguration.instance.hostname.isEmpty() ? PortForwarding.getExternalIP() : NMOConfiguration.instance.hostname;
						DesktopHelper.browse("http://" + hostname + ":" + NMOConfiguration.instance.jettyPort + "/");
					}
					catch (Throwable e)
					{
						e.printStackTrace();
					}
				}
			});
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
			heading.getChildren().add(jfxButtonPortForward);
			final Button jfxButtonConfigure = JavaFxHelper.createButton("Configure", JavaFxHelper.createIcon(FontAwesomeIcon.COGS, "11", Color.BLACK));
			jfxButtonConfigure.setPadding(new Insets(2, 4, 2, 4));
			jfxButtonConfigure.setDisable(true); // temporary
			heading.getChildren().add(jfxButtonConfigure);

			final BorderPane frame = new BorderPane();
			frame.setTop(heading);
			frame.setCenter(hbox);
			frame.setStyle("-fx-border-width: 1px; -fx-border-color: #6D81A3; -fx-background-color: #333;");
			pane.add(frame, 1, 0, 2, 1);
		}

		// build the webcam frame
		{
			VBox vbox = new VBox(6);
			vbox.setPadding(new Insets(6));
			Label l = JavaFxHelper.createLabel("", Color.WHITE, "-fx-font-weight: bold;");
			l.setPadding(new Insets(0, 0, 0, 2));
			l.textProperty().bind(webcamName);
			vbox.getChildren().add(l);

			ImageView webcamImageView = new ImageView();
			webcamImageView.imageProperty().bind(lastWebcamImage);
			webcamImageView.setPreserveRatio(true);
			vbox.getChildren().add(webcamImageView);

			final HBox heading = JavaFxHelper.createHorizontalBox(Control.USE_COMPUTED_SIZE, 24);
			heading.setStyle("-fx-background-color: #A36D75;");
			heading.setPadding(new Insets(2));
			heading.setSpacing(2);
			final Label label = JavaFxHelper.createLabel("Webcam", Color.BLACK, "-fx-font-size: 11pt;");
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
			frame.setCenter(vbox);
			frame.setStyle("-fx-border-width: 1px; -fx-border-color: #A36D75; -fx-background-color: #333;");
			pane.add(frame, 3, 0, 1, 1);
		}

		// PAVLOK
		{
			HBox hbox = new HBox(6);
			hbox.setPadding(new Insets(6));
			hbox.setAlignment(Pos.TOP_CENTER);

			VBox statusBox = new VBox(6);
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
		}

		// LIGHTING
		{
			HBox hbox = new HBox(6);
			hbox.setPadding(new Insets(6));
			hbox.setAlignment(Pos.TOP_CENTER);

			VBox statusBox = new VBox(6);
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
		}

		// ALARM SOUNDS
		{
			HBox hbox = new HBox(6);
			hbox.setPadding(new Insets(6));
			hbox.setAlignment(Pos.TOP_CENTER);

			VBox statusBox = new VBox(6);
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
			pane.add(frame, 2, 1, 1, 2);
		}

		// EVENT CONTROL
		{
			HBox hbox = new HBox(6);
			hbox.setPadding(new Insets(6));
			hbox.setAlignment(Pos.TOP_CENTER);

			VBox statusBox = new VBox(6);
			statusBox.setSpacing(1);
			statusBox.setAlignment(Pos.TOP_LEFT);
			hbox.getChildren().add(statusBox);
			HBox.setHgrow(statusBox, Priority.ALWAYS);

			this.addEventSummaryToStatusBox(statusBox, "When sleep block is approaching", NMOConfiguration.instance.events.sleepBlockApproaching);
			this.addEventSummaryToStatusBox(statusBox, "When sleep block starts", NMOConfiguration.instance.events.sleepBlockStarted);
			this.addEventSummaryToStatusBox(statusBox, "When sleep block ends", NMOConfiguration.instance.events.sleepBlockEnded);
			this.addEventSummaryToStatusBox(statusBox, "On first activity warning", NMOConfiguration.instance.events.activityWarning1);
			this.addEventSummaryToStatusBox(statusBox, "On subsequent warnings", NMOConfiguration.instance.events.activityWarning2);
			this.addEventSummaryToStatusBox(statusBox, "When manually pausing", NMOConfiguration.instance.events.pauseInitiated);
			this.addEventSummaryToStatusBox(statusBox, "When manually unpausing", NMOConfiguration.instance.events.pauseCancelled);
			this.addEventSummaryToStatusBox(statusBox, "When pause auto-expires", NMOConfiguration.instance.events.pauseExpired);

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
			pane.add(frame, 3, 1, 1, 2);
		}

		// TWILIO
		{
			HBox hbox = new HBox(6);
			hbox.setPadding(new Insets(6));
			hbox.setAlignment(Pos.TOP_CENTER);

			VBox statusBox = new VBox(6);
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
		}

		// CUSTOM COMMANDS
		{
			HBox hbox = new HBox(6);
			hbox.setPadding(new Insets(6));
			hbox.setAlignment(Pos.TOP_CENTER);

			VBox statusBox = new VBox(6);
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

	private void addEventSummaryToStatusBox(VBox statusBox, String description, String[] eventTriggers)
	{
		statusBox.getChildren().add(JavaFxHelper.createLabel(description + ":", Color.WHITE, "-fx-font-weight: bold;"));
		if (eventTriggers.length == 0)
		{
			statusBox.getChildren().add(JavaFxHelper.createLabel("do nothing", Color.GRAY, "", new Insets(0, 0, 0, 16)));
		}
		else
		{
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
		if (tick >= 180)
		{
			tick -= 180;
			//System.gc();
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
				scheduleStatus = "SLEEPING [" + nextSleepBlockDetected.name + "] UNTIL " + CommonUtils.convertTimestamp(tims);
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
				scheduleStatus = "AWAKE [" + nextSleepBlockDetected.name + " STARTS IN " + minutesRemaining + " MINUTE" + (minutesRemaining == 1 ? "" : "S") + "]";
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
			resetActivityTimer();
		}
		if (NMOConfiguration.instance.integrations.pavlok.enabled)
		{
			loginTokenValidUntilString.set("Login expires: " + CommonUtils.dateFormatter.format(1000 * (NMOConfiguration.instance.integrations.pavlok.auth.created_at + NMOConfiguration.instance.integrations.pavlok.auth.expires_in)));
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
			lastActivityTimeString.set("Last input activity: " + CommonUtils.dateFormatter.format(lastActivityTime));
			long nawtd = getNextActivityWarningTimeDiff(nextActivityWarningID);
			if (timeDiff > (1000 * nawtd))
			{
				try
				{
					// the first time, you get an alternative lighter warning, just in case you forgot to pause
					if (nextActivityWarningID == 0)
					{
						triggerEvent("No activity detected for " + nawtd + " seconds", NMOConfiguration.instance.events.activityWarning1);
					}
					else
					{
						triggerEvent("No activity detected for " + nawtd + " seconds", NMOConfiguration.instance.events.activityWarning2);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				this.setNextActivityWarningForTimer(timer, timeDiff);
			}
			timeDiffString.set("Time difference: " + timeDiff + " (next warning: " + nawtd + "s)");
		}

		webMonitoringString.set(WebcamWebSocketHandler.connectionCounter.get() + " active web sockets");
		activeTimerString.set("Active timer:   " + timer.name + " (" + timer.secondsForFirstWarning + "s/" + timer.secondsForSubsequentWarnings + "s)");
		lightingStateString.set("LIGHTING: " + (IntegrationPhilipsHue.INSTANCE.lightState > -1 ? "ON, LIGHT LEVEL " + IntegrationPhilipsHue.INSTANCE.lightState : "OFF"));

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

	public static String formatTimeElapsedWithDays(long now, long time)
	{
		now = (now / 1000) * 1000;
		time = (time / 1000) * 1000;
		long elapsed = now - time;
		long days = elapsed / 86400000;
		elapsed = elapsed - (days * 86400000);
		long hours = elapsed / 3600000;
		elapsed = elapsed - (hours * 3600000);
		long minutes = elapsed / 60000;
		elapsed = elapsed - (minutes * 60000);
		long seconds = elapsed / 1000;

		return String.format("%01dd %01dh %01dm", days, hours, minutes);
	}

	public static String formatTimeElapsedWithoutDays(long now, long time)
	{
		now = (now / 1000) * 1000;
		time = (time / 1000) * 1000;
		long elapsed = now - time;
		long days = elapsed / 86400000;
		elapsed = elapsed - (days * 86400000);
		long hours = elapsed / 3600000;
		elapsed = elapsed - (hours * 3600000);
		long minutes = elapsed / 60000;
		elapsed = elapsed - (minutes * 60000);
		long seconds = elapsed / 1000;

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

	public static void resetActivityTimer()
	{
		lastActivityTime = System.currentTimeMillis();
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
	}
}
