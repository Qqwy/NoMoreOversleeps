package com.tinytimrob.ppse.nmo;

import java.awt.MouseInfo;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.tinytimrob.ppse.nmo.utils.JavaFxHelper;
import com.tinytimrob.ppse.nmo.utils.Utils;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class MainDialog extends Application
{
	private static final Logger log = LogManager.getLogger();
	public static Scene scene;

	// zap every 10 seconds after 5 minutes has passed without the mouse moving 
	public static volatile String pauseReason = "";
	public static volatile long pausedUntil = 0;
	public static volatile long initialZapTimeDiff = 300000;
	public static volatile long nextZapTimeDiff = initialZapTimeDiff;
	public static volatile long incrementZapTimeDiff = 10000;
	public static volatile long lastCursorTime = System.currentTimeMillis();
	public static volatile Point lastCursorPoint = MouseInfo.getPointerInfo().getLocation();
	public static volatile SimpleStringProperty loginTokenValidUntilString = new SimpleStringProperty("");
	public static volatile SimpleStringProperty lastCursorTimeString = new SimpleStringProperty("");
	public static volatile SimpleStringProperty lastCursorPositionString = new SimpleStringProperty("");
	public static volatile SimpleStringProperty timeDiffString = new SimpleStringProperty("");
	public static volatile SimpleBooleanProperty isCurrentlyPaused = new SimpleBooleanProperty(false);
	public static ObservableList<String> events = FXCollections.observableArrayList();

	public static void addEvent(String event)
	{
		log.info("APPEVENT: " + event);
		events.add(Utils.dateFormatter.format(System.currentTimeMillis()) + ": " + event);
	}

	@Override
	public void start(Stage stage) throws Exception
	{
		log.info("JavaFX application start");
		addEvent("Application started");

		Configuration.load();
		try
		{
			Pavlok.vibration(255, "Connection test");
			addEvent("Sent connection test vibration");
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			Configuration.instance.pavlokAuth = null;
		}

		//==================================================================
		// CONFIGURE THE STAGE
		//==================================================================
		stage.setTitle("NoMoreOversleeps v" + Main.VERSION);
		stage.getIcons().add(new Image(JavaFxHelper.buildResourcePath("icon.png")));
		stage.setResizable(false);
		stage.setMinWidth(800);
		stage.setMinHeight(600);

		//==================================================================
		// CONFIGURE ANIMATION TIMER
		//==================================================================
		// this is an absurd workaround
		final AnimationTimer at = new AnimationTimer()
		{
			@Override
			public void handle(long now)
			{
				now = System.currentTimeMillis();
				boolean paused = pausedUntil > now;
				boolean wasPaused = isCurrentlyPaused.get();
				isCurrentlyPaused.set(paused);

				if (!paused && wasPaused)
				{
					addEvent("Unpaused automatically - time alotted for \"" + pauseReason + "\" has expired");
				}

				Point epoint = MouseInfo.getPointerInfo().getLocation();
				if (!epoint.equals(lastCursorPoint) || paused)
				{
					lastCursorTime = now;
					lastCursorPoint = epoint;
					nextZapTimeDiff = initialZapTimeDiff;
				}
				loginTokenValidUntilString.set("Login token to Pavlok API expires on " + Utils.dateFormatter.format(1000 * (Configuration.instance.pavlokAuth.created_at + Configuration.instance.pavlokAuth.expires_in)));
				if (paused)
				{
					lastCursorTimeString.set("PAUSED for \"" + pauseReason + "\" until " + Utils.dateFormatter.format(pausedUntil));
					lastCursorPositionString.set("");
					timeDiffString.set("");
				}
				else
				{
					lastCursorTimeString.set("Last cursor movement: " + Utils.dateFormatter.format(lastCursorTime));
					lastCursorPositionString.set("Last cursor position: " + lastCursorPoint.getX() + ", " + lastCursorPoint.getY());
					long timeDiff = paused ? 0 : (now - lastCursorTime);
					if (timeDiff > nextZapTimeDiff)
					{
						try
						{
							// the first time, you get a vibration instead of a zap, just in case you forgot to pause
							if (nextZapTimeDiff == initialZapTimeDiff)
							{
								addEvent("Sending VIBRATION: Mouse hasn't moved in " + (nextZapTimeDiff / 1000) + " seconds");
								Pavlok.vibration(255, "Mouse hasn't moved in " + (nextZapTimeDiff / 1000) + " seconds");
							}
							else
							{
								addEvent("Sending SHOCK: Mouse hasn't moved in " + (nextZapTimeDiff / 1000) + " seconds");
								Pavlok.shock(255, "Mouse hasn't moved in " + (nextZapTimeDiff / 1000) + " seconds");
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
						nextZapTimeDiff += incrementZapTimeDiff;
					}
					timeDiffString.set("Time difference: " + timeDiff + " (next zap: " + nextZapTimeDiff + ")");
				}
			}
		};

		//==================================================================
		// CONFIGURE THE SCENE
		//==================================================================
		final StackPane outerPane = new StackPane();
		outerPane.setId("root");
		scene = new Scene(outerPane, 800, 600, Color.WHITE);
		scene.getStylesheets().add(JavaFxHelper.buildResourcePath("application.css"));

		//==================================================================
		// BUILD THE PRIMARY PANE
		//==================================================================
		final BorderPane innerPane = new BorderPane();
		{ // Main container pane
			innerPane.setStyle("-fx-background-color: #222;");
		}

		final HBox innerTopPane = JavaFxHelper.createHorizontalBox(Control.USE_COMPUTED_SIZE, 35, new Insets(8, 4, 8, 4));
		{ // Top section
			innerTopPane.setStyle("-fx-background-color: #414C7D;");
			final Label label = JavaFxHelper.createLabel(" NoMoreOversleeps", Color.WHITE, "-fx-font-weight: bold; -fx-font-size:16;", new Insets(0, 0, 0, 3), 320, Control.USE_COMPUTED_SIZE);
			label.setGraphic(JavaFxHelper.createIcon(FontAwesomeIcon.BED, "16", Color.WHITE));
			innerTopPane.getChildren().add(label);
			innerPane.setTop(innerTopPane);
		}

		final BorderPane centerPaneI = new BorderPane();
		{ // Real center pane
			innerPane.setCenter(centerPaneI);
		}

		final GridPane centerPane = new GridPane();
		{ // Center pane
			final Label loginTokenValidUntil = JavaFxHelper.createLabel("", Color.WHITE, "-fx-font-weight: bold;");
			loginTokenValidUntil.textProperty().bind(loginTokenValidUntilString);
			loginTokenValidUntil.setPadding(new Insets(6, 0, 10, 8));
			centerPane.addRow(0, loginTokenValidUntil);

			final Label lastCursorTime = JavaFxHelper.createLabel("", Color.WHITE, "-fx-font-weight: bold;");
			lastCursorTime.textProperty().bind(lastCursorTimeString);
			lastCursorTime.setPadding(new Insets(6, 0, 0, 8));
			centerPane.addRow(1, lastCursorTime);

			final Label lastCursorPosition = JavaFxHelper.createLabel("", Color.WHITE, "-fx-font-weight: bold;");
			lastCursorPosition.textProperty().bind(lastCursorPositionString);
			lastCursorPosition.setPadding(new Insets(3, 0, 0, 8));
			centerPane.addRow(2, lastCursorPosition);

			final Label timeDiff = JavaFxHelper.createLabel("", Color.WHITE, "-fx-font-weight: bold;");
			timeDiff.textProperty().bind(timeDiffString);
			timeDiff.setPadding(new Insets(3, 0, 0, 8));
			centerPane.addRow(3, timeDiff);

			centerPaneI.setCenter(centerPane);
		}

		final ListView<String> listView = new ListView<String>(events);
		listView.getItems().addListener(new ListChangeListener<String>()
		{
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends String> c)
			{
				listView.scrollTo(c.getList().size() - 1);
			}
		});
		centerPaneI.setBottom(listView);

		final GridPane innerRightPane = new GridPane();
		{ // Manual Pavlok controls
			int row = 0;
			innerRightPane.setMinWidth(200);
			innerRightPane.setMaxWidth(200);
			innerRightPane.setStyle("-fx-background-color: #444;");
			innerRightPane.setVgap(10);
			innerRightPane.setPadding(new Insets(10, 10, 10, 10));
			final Label label = JavaFxHelper.createLabel("Manual controls", Color.WHITE, "", new Insets(0, 0, 0, 3), 160, Control.USE_COMPUTED_SIZE);
			innerRightPane.addRow(row++, label);
			final Button beepButton = JavaFxHelper.createButton("BEEP", JavaFxHelper.createIcon(FontAwesomeIcon.VOLUME_UP, "12", Color.BLACK));
			beepButton.setMinWidth(180);
			beepButton.setMaxWidth(180);
			beepButton.setAlignment(Pos.BASELINE_LEFT);
			beepButton.setContentDisplay(ContentDisplay.RIGHT);
			beepButton.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent arg0)
				{
					try
					{
						Pavlok.beep(255, "Manually triggered beep");
						addEvent("Manually triggered beep");
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});
			innerRightPane.addRow(row++, beepButton);
			final Button vibrateButton = new Button("VIBRATE");
			vibrateButton.setMinWidth(180);
			vibrateButton.setMaxWidth(180);
			vibrateButton.setAlignment(Pos.BASELINE_LEFT);
			vibrateButton.setContentDisplay(ContentDisplay.RIGHT);
			vibrateButton.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent arg0)
				{
					try
					{
						Pavlok.vibration(255, "Manually triggered vibration");
						addEvent("Manually triggered vibration");
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});
			innerRightPane.addRow(row++, vibrateButton);
			final Button shockButton = new Button("SHOCK");
			shockButton.setMinWidth(180);
			shockButton.setMaxWidth(180);
			shockButton.setAlignment(Pos.BASELINE_LEFT);
			shockButton.setContentDisplay(ContentDisplay.RIGHT);
			shockButton.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent arg0)
				{
					try
					{
						Pavlok.shock(255, "Manually triggered shock");
						addEvent("Manually triggered shock");
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});
			innerRightPane.addRow(row++, shockButton);

			// Pause controls
			final Label label2 = JavaFxHelper.createLabel("Pause/Resume", Color.WHITE, "", new Insets(0, 0, 0, 3), 160, Control.USE_COMPUTED_SIZE);
			innerRightPane.addRow(row++, label2);

			int[] periods = new int[] { 15, 20, 25, 30, 45, 60, 90, 120, 480, 720, 1440 };
			for (int p = 0; p < periods.length; p++)
			{
				final int pp = periods[p];
				int hours = pp / 60;
				int minutes = pp % 60;
				final String hm = (((hours > 0) ? hours + "h" : "") + ((minutes > 0) ? minutes + "m" : ""));
				final Button pauseButton = JavaFxHelper.createButton("Pause for " + hm);
				pauseButton.setMinWidth(180);
				pauseButton.setMaxWidth(180);
				pauseButton.setAlignment(Pos.BASELINE_LEFT);
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
							addEvent("Paused for " + hm + " (until " + Utils.dateFormatter.format(pausedUntil) + ") for \"" + pauseReason + "\"");
						}
					}
				});
				pauseButton.disableProperty().bind(isCurrentlyPaused);
				innerRightPane.addRow(row++, pauseButton);
			}
			final Button unpauseButton = JavaFxHelper.createButton("Unpause");
			unpauseButton.setMinWidth(180);
			unpauseButton.setMaxWidth(180);
			unpauseButton.setAlignment(Pos.BASELINE_LEFT);
			unpauseButton.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent arg0)
				{
					pausedUntil = 0;
					isCurrentlyPaused.set(false);
					addEvent("Unpaused manually");
				}
			});
			unpauseButton.disableProperty().bind(isCurrentlyPaused.not());
			innerRightPane.addRow(row++, unpauseButton);

			innerPane.setRight(innerRightPane);
		}

		//==================================================================
		// PAVLOK CRAP
		//==================================================================

		if (Configuration.instance.pavlokAuth == null)
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
								Pavlok.postAuthToken(map.get("code"));
								Configuration.instance.pavlokAuth = Pavlok.RESPONSE;
								Configuration.save();
								Pavlok.vibration(255, "Connection test");
								addEvent("Sent connection test vibration");
								outerPane.getChildren().clear();
								outerPane.getChildren().add(innerPane);
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
			outerPane.getChildren().add(authPane);
		}
		else
		{
			outerPane.getChildren().add(innerPane);
			at.start();
		}

		//==================================================================
		// SHOW STAGE
		//==================================================================
		stage.setScene(scene);
		stage.show();
	}
}
