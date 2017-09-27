EncephalophoneGUI {
	var server;
	var gui;
	var freeplay;

	*new {arg server;
		^super.new.init(server);
	}

	init {arg thisServer;
		server = thisServer;
		server.boot;
		server.waitForBoot({
			this.makeGUI;
		});
	}

	makeGUI {
		var modeBox, selectMode, modes;
		var space;
		var modeTypes, modeDim;
		var colors = [Color.white.alpha_(0.1), Color.white.alpha_(0.08), Color.white.alpha_(0.06)];
		var tester;

		space = 50;

		// Dictionary of filter boxes
		modeBox = Dictionary.new;

		// Dictionary of filters for button selection
		selectMode = Dictionary.new;

		modeTypes = [\Calibrate, \FreePlay, \Tester];

		modeDim = [[800, 300], [840, 400], [360, 200]];

		gui = Window.new("EncGUI", Rect(200, 600, modeDim[0][0], modeDim[0][1] + space))
		.visible_(true)
		.alwaysOnTop_(true)
		.background_(Color.black)
		.onClose_(
			{
				server.freeAll;
				SystemClock.clear;
				server.quit;
				tester.getOscDef.free;
			}
		);

		// Creates views for filters
		[modeTypes, modeDim].flop.do({arg thisKey, i;
			var width = thisKey[1][0];
			var height = thisKey[1][1];
			var modeView = CompositeView(gui, Rect.new(0, space, width, height)).background_(colors[i]).visible_(false);
			modeView.visible_(false);
			modeBox.put(thisKey[0], modeView);
			if (thisKey[0] == \Calibrate,
				{
					Calibrate(server, modeView);
				},
				{
					if (thisKey[0] == \Tester,
						{tester = Tester(server, modeView, gui)},
						{freeplay = FreePlay(server, modeView)}
					)
				}
			);
		});

		// Creates buttons for filter selection
		modeBox.put(\buttons, CompositeView(gui, Rect.new(0, 0, (350), space)));
		modeBox[\buttons].decorator_(FlowLayout(modeBox[\buttons].bounds, 5@5, 5@5));
		modeTypes.do({arg item;
			selectMode.put(item, Button(modeBox[\buttons], 100@40)
				.states_([
					[item.asString, Color.white.alpha_(0.7), Color.black.alpha_(0.7)],
					[item.asString, Color.black.alpha_(0.7), Color.white.alpha_(0.7)]]
				)
				.font_(Font.new.pixelSize_(20))
		)});

		modeBox[\Calibrate].visible_(true);
		selectMode[\Calibrate].value_(1);

		selectMode.keysDo({arg key;
			selectMode[key].action_({arg button;
				if (button.value == 1,
					{
						if (key != \FreePlay && modeBox[\FreePlay].visible,
							{
								if(freeplay.getBtn.value == 1,
									{freeplay.getBtn.valueAction_(0)}
								)
							}
						);
						modeBox.values.do({arg value; value.visible_(false)});
						modeBox[key].visible_(true);
						modeBox[\buttons].visible_(true);
						selectMode.values.do({arg thisButton; thisButton.value_(0)});
						button.value_(1);
						gui.setInnerExtent(modeBox[key].bounds.width, modeBox[key].bounds.height + space)
					},
					{
						button.value_(1);
					}
				)
			})
		});
	}

}