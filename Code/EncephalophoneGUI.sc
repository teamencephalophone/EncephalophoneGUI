EncephalophoneGUI {
	var server;
	var gui;
	var freeplay, tester, calibrate;

	*new {arg server;
		^super.new.init(server);
	}

	init {arg thisServer;
		server = thisServer;
		server.options.numBuffers = 2**16;
		server.waitForBoot({
			this.makeGUI;
		});
	}

	makeGUI {
		var modeBox, selectMode, modes;
		var space;
		var modeTypes, modeDim;
		var colors = Array.fill(3, {Color.white.alpha_(0.06)});

		space = 50;

		// Dictionary of filter boxes
		modeBox = Dictionary.new;

		// Dictionary of filters for button selection
		selectMode = Dictionary.new;

		modeTypes = [\Calibrate, \FreePlay, \Tester];

		modeDim = [[800, 300], [850, 550], [360, 200]];

		gui = Window.new("EncGUI", Rect(200, 600, modeDim[0][0], modeDim[0][1] + space))
		.visible_(true)
		.front
		.background_(Color.black)
		.onClose_(
			{
				server.freeAll;
				SystemClock.clear;
				server.quit;
				freeplay.stopClock;
				[tester, freeplay, calibrate].do({arg item;
					item.getOscDef.free;
				});
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
					calibrate = Calibrate(server, modeView);
				},
				{
					if (thisKey[0] == \Tester,
						{tester = Tester(server, modeView, gui)},
						{freeplay = FreePlay(server, modeView, gui)}
					)
				}
			);
		});

		// Creates buttons for filter selection
		modeBox.put(\buttons, CompositeView(gui, Rect.new(0, 0, (350), space)));
		// modeBox.put(\buttons, View(gui));
		// modeBox[\buttons].decorator_(FlowLayout(modeBox[\buttons].bounds, 5@5, 5@5));
		modeBox[\buttons].layout_(HLayout());
		modeTypes.do({arg item;
			// selectMode.put(item, Button(modeBox[\buttons], 100@40)
			selectMode.put(item, Button(modeBox[\buttons]).fixedWidth_(100)
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
								freeplay.getBtn.valueAction_(1);
/*								if(freeplay.getBtn.value == 1,
									{freeplay.getBtn.valueAction_(0)}
								);		*/
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