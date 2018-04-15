Calibrate {
	var server, gui, oscdef, pupilOSCDef;

	*new {arg server, gui;
		^super.new.init(server, gui);
	}

	init {arg thisServer, thisGUI;
		server = thisServer;
		gui = thisGUI;
		this.makeGUI;
	}

	makeGUI {
		var makeBox;
		var controlBox, plotBox;
		var btn, plotView, plot;
		var data, cur, eyeVal;
		var plotDim;

		plotDim = [400, 200];

		controlBox = View().background_(Color.black.alpha_(0.1));
		plotBox = View().background_(Color.black.alpha_(0.12));

		data = Array.fill(16, {0});
		cur = 0;
		data.postln;

		btn = Button()
		.states_([
			["CALIBRATE", Color.white, Color.black],
			["", Color.black, Color.white]
		])
		.font_(Font.new().pixelSize_(15))
		.action_({arg button;
			if(button.value == 1,
				{
					button.value_(0);
				},
			)
		});

		plotView = UserView();

		plot = Plotter("data-plot", Rect(0, 0, plotDim[0], plotDim[1]), plotView).value_(data)
		.setProperties(
			 \fontColor, Color.white,
			\plotColor, Color.white,
			\backgroundColor, Color.black,
			\gridColorX, Color.white,
			\gridColorY, Color.white,
		);

		cur = StaticText().string_("Current Val: " ++ cur).stringColor_(Color.white);

		eyeVal = StaticText().string_("No Eyetracker").stringColor_(Color.white);

		plotBox.layout_(
			VLayout(
				plotView,
				HLayout(
					View().background_(Color.black).layout_(HLayout(cur)).fixedHeight_(40),
					View().background_(Color.black).layout_(HLayout(eyeVal)).fixedHeight_(40)
				)
			)
		);

		gui.layout_(
			HLayout(
				controlBox, plotBox
			)
		);

		oscdef = OSCdef(
			\calibrateDef,
			{ |msg, time, addr|
				var note;
				note = msg[1];
				data.removeAt(0);
				data.add(note);
				{
					plot.value_(data);
					cur.string_("Current Value: " + note);
				}.defer;

			},
			'/fred'
		);

		pupilOSCDef = OSCdef(
			\checkPupil,
			{ |msg|
				var string;
				var val = msg[1];
				{
					if (val > 0.5, {
						eyeVal.string_("Eye is: " ++ "OPEN")
					}, {
						eyeVal.string_("Eye is: " ++ "CLOSED")
					});
				}.defer
			},
			'/pupil'
		);
	}

	getOscDef {
		^oscdef;
	}

	getPupilDef {
		^pupilOSCDef;
	}
}