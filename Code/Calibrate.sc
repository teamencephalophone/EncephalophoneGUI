Calibrate {
	var server, gui, oscdef;

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
		var data, cur;
		var plotDim;

		plotDim = [330, 180];

		data = Array.fill(16, {0});
		cur = 0;
		data.postln;

		makeBox = {arg rect, color, margin, gap;
			var thisBox = CompositeView(gui, rect).background_(color);
			thisBox.decorator_(FlowLayout(thisBox.bounds, margin, gap));
		};

		controlBox = makeBox.value(Rect.new(20, gui.bounds.height - 280, 380, 300), Color.grey.alpha_(0.2), 10@10, 10@10);

		plotBox = makeBox.value(Rect.new(430, gui.bounds.height - 280, 350, 300), Color.grey.alpha_(0.2), 10@10, 10@10);

		btn = Button(controlBox, 150@40)
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

		plotView = UserView(plotBox, plotDim[0]@plotDim[1]);

		plot = Plotter("data-plot", Rect(0, 0, plotDim[0], plotDim[1]), plotView).value_(data)
		.setProperties(
			 \fontColor, Color.white,
			\plotColor, Color.white,
			\backgroundColor, Color.black,
			\gridColorX, Color.white,
			\gridColorY, Color.white,
		);
		cur = StaticText(plotBox, 30@30).string_("" + cur).stringColor_(Color.white);


		oscdef = OSCdef(
			\calibrateDef,
			{ |msg, time, addr|
				var note;
				note = msg[1];
				data.removeAt(0);
				data.add(note);
				{
					plot.value_(data);
					cur.string_("" + note);
				}.defer;

			},
			'/fred'
		)

	}

	getOscDef {
		^oscdef;
	}

}