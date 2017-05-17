# NoMoreOversleeps #

NoMoreOversleeps is a very simple JavaFX application which is designed to help you adapt to a new polyphasic sleeping schedule.

This application was created only for my own use and is largely a work in progress, so the implementation is currently very crude and makes lots of assumptions (e.g. if you 
don't own a Pavlok you can't even use the application right now). In the future it would be nice to turn this into a general-purpose tool to help people adapt to and track
their sleeping schedules.

### Features ###

#### Activity tracking ####

After setting up your sleep schedule in the configuration file, NoMoreOversleeps monitors activity of input devices on your computer (currently supporting keyboard, mouse and
Xbox360-compatible controllers) and will try to get your attention every 10 seconds if it fails to detect activity in any of those devices within a 5 minute interval. This
is useful in case you fall asleep at the wrong time or fail to wake up on time.

Supported attention methods are as follows:

* Send vibrate (on first warning) and then shock (on subsequent warnings) to a Pavlok
* Play a noise of your choice out of computer speakers/headphones

In the event you are deliberatily going to be away from your computer for a certain amount of time, e.g. you're going shopping or going to work, you can pause the activity detection
feature of NoMoreOversleeps for a choosable length of time. This will prevent NoMoreOversleeps from trying to get your attention. The pause function is also automatically activated
at the start of each sleep block, lasting for the duration of that block, in order to avoid a scenario where you go to sleep on time but get woken up mid-nap because you forgot to
pause the activity tracking.

#### Audio warning of upcoming sleep block ####

An audio warning of your choice will be played out of computer speakers/headphones 5 minutes before each sleep block so that you remember to go to sleep on time.

#### Web frontend ####

NoMoreOversleeps features a port-forwardable password-protected web UI with a live webcam feed, along with buttons to get your attention in various ways:

* Send beep/vibrate/shock to a Pavlok
* Call your phone numbers using Twilio
* Play a noise out of computer speakers/headphones
* Turn Philips Hue smart light bulbs on and off

This gives people the ability attempt to wake you up via whatever means they feel is necessary if you appear to be oversleeping.

### Best usage ###

NoMoreOversleeps is designed for continuous monitoring. It works best if you mostly sit at your computer, have access to most/all of the integration features, and have a reliable buddy to
watch your feed.

### Features that might be added in the future ###

Some ideas I've had include:

* Better GUI to allow for more flexible configuration of the software
* Load the sleep schedule from a Napchart link rather than having to configure it by hand
* Plugin/extensibility system so that people can add their own integrations into the software
* GPS tracking of phone so you can see where someone is if they aren't at their computer
* Tracking function so you can record your sleeping (and possible oversleeping) along with how good each sleep/nap felt
* Automate the phone call and lighting control if you fail to wake up
* Send a message to the polysleeping Discord if you fail to wake up so that you can be really embarrased for being such a huge failure
* Food tracker so you can monitor what you're eating in case this affects your sleep schedule
* Productivity tracker so you can input what you're doing in each sleep block to avoid wasting time
* Integration with EEG to track SWS/REM acquisition

I probably won't implement most of these, but we will see.

### Known issues ###

* The RAM usage is too high. There may be a possible memory leak somewhere I haven't found. It appears to be related to the webcam code, as it worked fine prior to that :/
* Some features only work on Windows. For other platforms YMMV. It is possible that right now the application even would just crash out on other platforms or be largely unusable (because I haven't tested it).
* I designed this only for my own usage, so the application is currently not very configurable.
* If you don't have a Pavlok you can't access the application frontend at all because you get stuck on the Pavlok login screen.
* When your login key to Pavlok API expires it isn't renewed. This doesn't seem to make any difference because it doesn't appear to matter that it's expired and is accepted by the API anyway :/
* The Pavlok integration is not very useful due to the glitchy and unreliable nature of the Pavlok's bluetooth connection and push notifications.
* The webcam feed isn't visible on iOS devices for some reason.
* The log in the web UI flashes because it was implemented as a refreshing iframe. It should eventually be replaced with Ajax.
* Pressing buttons in the web UI is done as a form action, causing the entire page to reload, including a reset of the webcam socket. It should eventually be replaced with Ajax.

### Contribution guidelines ###

This program is open source and completely free. If you want to contribute then you can fork the project, patch it, and submit a pull request. As long as your patch works and your code is not awful, I'll probably accept it.

### Who do I talk to? ###

If you have any queries about this program please contact Robert Dennington (Tinytimrob) over Discord or via e-mail, tinytimrob [at] googlemail [dot] com