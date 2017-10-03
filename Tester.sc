Tester {
	var gui, view;
	var color;
	var server;
	var timeText;
	var curText;
	var pad;
	var targetNotes;
	var targetSynth;
	var userNotes;
	var target;
	var notes;
	var numGen;
	var hit, miss;
	var hitText, missText, trialText;
	var targVals;
	var dif, difString;
	var useSlider, slide;
	var file;
	var hitBuf, missBuf;
	var name;
	var cur = 1;
	var targetNum = 0;
	var correct = 0;
	var thisNum;
	var trial = -1;
	var flag = false;
	var oscFunc, oscdef;
	var slider;
	var window;
	var key, stopBtn;
	var trial;
	var folder;
	var nameFlag, folderFlag;

	*new {arg server, gui, window;
		^super.new.init(server, gui, window);
	}

	init {arg thisServer, thisGUI, thisWindow;
		gui = thisGUI;
		window = thisWindow;
		color = Color.white;
		notes = Scale.major.degrees ++ 12;
		dif = Dictionary.new;
		useSlider = false;
		slider = Dictionary.new;
		nameFlag = false;
		folderFlag = false;
		[
			[\target, 0.5],
			[\user, 0.3],
			[\hit, 0.3],
			[\miss, 0.3]
		];
		[
			["EASY", [1, 8]],
			["MEDIUM", [1, 5, 8]],
			["HARD", [1, 3, 5, 8]]
		].do({arg thisData;
			dif.put(thisData[0], thisData[1]);
		});
		name = "default";
		targVals = dif["EASY"];
		difString = "EASY";
		server = thisServer;
		hit = 0;
		miss = 0;
		key = 36;
		this.initSynths;
		this.makeGUI;
	}

	initSynths {
		SynthDef.new(\curSynth, {arg freq = 440, amp = 0.4, dur = 0.4;
			var sig, env, out;

			sig = amp *  MdaPiano.ar(freq, decay: 0.1, release: dur);
			DetectSilence.ar(sig, 0.01, doneAction:2);

			Out.ar(0, sig);

		}).add;

		SynthDef.new(\targetSynth, {arg freq = 440, amp = 0.5;
			var sig, env, out;
			var lagVal;

			lagVal = 0.01;

			freq = freq.lag(lagVal);

			sig = Saw.ar([freq  + LFDNoise3.kr(0.89, 0.029), freq + LFDNoise3.kr(0.1, 0.03)]);

			sig = LPF.ar(sig, freq + (0.4 * freq * LFNoise2.kr([0.3, 0.27])));

			// apply the amplitude envelope and Ring Modulate
			sig = amp.lag(0.05) * sig;


			DetectSilence.ar(sig, 0.001, doneAction:2);

			Out.ar(0, sig);
		}).add;

	}

	makeGUI {
		var guiFont = Font(size: 30);
		var width = 1200;
		var height = 300;
		var makeText;
		var button, buttonSlider;
		var startBox;
		var sliderBox;
		var nameField;
		var folderField;
		var checkFlags;
/*
		gui = Window("Tester",  Rect.new(400, 800, 360, 180)).background_(Color.black).alwaysOnTop_(true).front.onClose_(
			{
				server.freeAll;
				SystemClock.clear;
				server.quit;
				oscdef.free;
			}
		);*/

		view = CompositeView(gui, Rect.new(250, 50, 820, 200)).background_(Color.grey.alpha_(0.3));
		view.decorator_(FlowLayout(view.bounds,10@10, 0@0));

		startBox = CompositeView(gui, Rect(0, 0, 360, 200));

/*		sliderBox = CompositeView(gui, Rect.new(50, 100, 200, 200)).background_(Color.grey.alpha_(0.2));
		sliderBox.decorator_(FlowLayout(view.bounds,10@10, 0@0));

		makeSlider = {arg symbol, value;


		};*/

		targetNotes = Array.fill(8, {
			TargetVis(100, Color.white, view);
		});

		userNotes = Array.fill(8, {
			NoteVis(100, Color.white, view).animate;
		});

		view.visible_(false);

		makeText = {arg x = 1100, y = 50, mul = 0, text = "", size = 30;
			StaticText(gui, Rect(x, y + mul, 100, 50)).string_(text).stringColor_(color).font_(Font().pixelSize_(size));
		};

		timeText = makeText.value();
		curText = makeText.value(mul: 40);
		trialText = makeText.value(mul: 80, size: 15);
		hitText = makeText.value(mul: 120, size: 15);
		missText = makeText.value(mul: 160, size: 15);

		StaticText(startBox, Rect(50, 60, 70, 20)).string_("difficulty:").stringColor_(Color.white).font_(Font().pixelSize_(15));

		PopUpMenu(startBox, Rect(120, 60, 180, 25)).items_(["EASY", "MEDIUM", "HARD"]).background_(Color.black).stringColor_(Color.white)
		.action_({arg menu;
			targVals = dif[menu.item];
			difString = menu.item;
		});

		button = Button(startBox, Rect(50, 140, 250, 40))
		.states_([
			["START SESSION", Color.white, Color.black],
			["", Color.black, Color.white]
		])
		.font_(guiFont.pixelSize_(15))
		.action_({arg btn;
			if ( btn.value == 1,
				{
					button.visible_(false);
					window.setInnerExtent(width, (height + 50));
					gui.resizeTo(width, height);
					view.visible_(true);
					startBox.visible_(false);
					this.playTester;
					stopBtn = Button(gui, Rect(50, 50, 150, 40))
					.states_([
						["STOP", Color.black, Color.white],
						["", Color.black, Color.white]
					])
					.font_(guiFont.pixelSize_(15))
					.action_({arg thisBtn;
						if (thisBtn.value == 1,
							{
								timeText.string_("");
								curText.string_("");
						 		hitText.string_("");
								missText.string_("");
								trialText.string_("");
								this.stopTarget;
								SystemClock.clear;
								if (target != 0,
									{
										target.makeDefault;
									}
								);
								oscdef.free;
								numGen.free;
								file.write("\n \n" ++ "hits: " ++ hit ++ " misses: " ++ miss ++ " trials: " ++ (trial) ++ " difficulty: " ++ difString ++ "\n");
								file.close;
								window.setInnerExtent(360, 200 + 50);
								gui.resizeTo(360, 200);
								view.visible_(false);
								startBox.visible_(true);
								thisBtn.visible_(false);
							}
						);
						thisBtn.value_(0);
					});

					btn.value_(0);

					// window = Window.new("Session Set-Up", Rect.new(400, 400 + height - (0.25 * height), width * 0.5, height * 0.25)).background_(Color.black).alwaysOnTop_(true).front.onClose_(
					// 	{
					// 		this.playTester;
					// 		btn.value_(1);
					// 	}
					// );
				}
			);
		})
		.visible_(false);

		checkFlags = {
			if (folderFlag && nameFlag, {
				button.visible_(true);
				nameFlag = false;
				folderFlag = false;
			});
		};

		nameField = StaticText(startBox, Rect(50, 20, 40, 20)).string_("name:").stringColor_(Color.white).font_(Font().pixelSize_(15));

		TextField(startBox, Rect(100, 20, 200, 25)).background_(Color.black).stringColor_(Color.white).action_({arg text;
			if (text != "",
				{
					name = text.value;
					nameFlag = true;
					checkFlags.value();
				}
			)
		});

		folderField = StaticText(startBox, Rect(50, 100, 40, 20)).string_("path:").stringColor_(Color.white).font_(Font().pixelSize_(15));

		TextField(startBox, Rect(100, 100, 200, 25)).background_(Color.black).stringColor_(Color.white).action_({arg text;
			if (text != "",
				{
					folder = text.value;
					folderFlag = true;
					checkFlags.value();
				}
			)
		});
	}

	playTester {
		var rout = Routine({arg time;
			var cur = 1;
			var targetNum = 0;
			var correct = 0;
			var thisNum = 9.xrand - 1;
			var flag = false;
			var date;
    		 trial = -1;
			hit = 0;
			miss = 0;
			targetSynth = 0;
			target = 0;

			// oscFunc = { |msg, time, addr|
			// 	if(msg[0] != '/status.reply') {
			// 		("getting - " + msg[1]).postln;
			// 		thisNum = 9.xrand - 1;
			// 	}
			// };
			// thisProcess.addOSCRecvFunc(oscFunc);
			oscFunc = { |msg, time, addr|
				    thisNum = msg[1] - 1;
			};
			oscdef = OSCdef(\testerDef, oscFunc, '/fred');

			date = " " ++ Date.getDate.asString.replace(":", "-");

			file = File(folder
				++ Platform.case(
					    \osx,       { "".postln },
					    \windows,   { "/".postln }
				)
				++ name ++ date ++ ".txt", "w");

			600.do({ arg i;
					if (cur == 1,
						{
							if ((target == 0).not, {
								target.makeDefault;
							});
							targetNum = targVals.choose - 1;
							target = targetNotes[targetNum];
						{target.makeTarget}.defer;
							trial = trial + 1;
							file.write("Trial Number: " + trial + "Target: " + (targetNum  + 1) + "\n");
							if (targetSynth == 0,
								{this.startTarget(targetNum)},
								{this.setTarget(targetNum)}
							);
						}
					);
					file.write("octile: " + thisNum + " " + "num: " + (i + 1) + " ");
					("target: " + (targetNum + 1)).postln;
					("thisNum " + (thisNum + 1)).postln;
					("cur: " + cur).postln;
					("correct: " + correct).postln;
					{
					timeText.string_(i + 1);
					curText.string_(cur);
					trialText.string_("trial: " + trial);
					hitText.string_("hit: " + hit);
					missText.string_("miss " + miss);

					userNotes[thisNum].animate;
					}.defer;

					this.play(thisNum);
					file.write(" result: ");
					if (targetNum != 7,
						{
							if ((thisNum == targetNum).or(thisNum == (targetNum + 1)),
								{
									correct = correct + 1;
									file.write("hit")
								},
								{
									correct = 0;
									file.write("miss")
								}
							)
						},
						{
							if ((thisNum == 7).or(thisNum == 6),
								{
									correct = correct + 1;
									file.write("hit")
								},
								{
									correct = 0;
									file.write("miss")
								}
							)
						}
					);
					0.5.yield;
					file.write("\n");
					if (cur == 19,
						{
						this.stopTarget;
						this.playDim();
						1.0.yield;
							cur = 1;
							correct = 0;
							miss = miss + 1;
							file.write("\n");
						},
						{
							if(correct > 2,
								{
								this.stopTarget;
								this.playTriad();
								1.0.yield;
									cur = 1;
									correct = 0;
									hit = hit + 1;
									file.write("\n");
								},
								{cur = cur + 1}
							);
						}
					);
			});


			{stopBtn.valueAction_(1)}.defer;

		});
		SystemClock.play(rout);

	}

	getOscDef {
		^oscdef;
	}

	play {arg deg = 1, oct = 2, synth = \curSynth;
		var freq = Scale.major.degreeToFreq(notes[deg], key.midicps, 1);
		Synth.new(synth, [\freq, freq, \dur, rrand(0.15, 0.5)]);
	}

	playDim {
		var play = Task({
			[0, 2, 4, 6].do({arg thisNote;
				var dimNotes = Scale.diminished.degrees;
				var freq = Scale.diminished.degreeToFreq(dimNotes[thisNote], key.midicps, 1);
				Synth.new(\curSynth, [\freq, freq]);
			});
		});
		play.start;
	}

	playTriad {
		var play = Task({
			2.do({
				[0, 2, 4].do({arg thisNote;
					var freq = Scale.major.degreeToFreq(notes[thisNote], key.midicps, 1);
					Synth.new(\curSynth, [\freq, freq, \dur, 0.2]);
				});
				(0.20).wait;
			});
		});
		play.start;
	}

	startTarget {arg deg = 1, amp = 0.5;
		var freq = Scale.major.degreeToFreq(notes[deg], key.midicps, 1);
		targetSynth = Synth.new(\targetSynth, [\freq, freq, \amp, amp]);
	}

	stopTarget {
		targetSynth.set(\amp, 0);
		targetSynth = 0;
	}

	setTarget {arg deg, oct = 2;
		var freq = Scale.major.degreeToFreq(notes[deg], key.midicps, 1);
		targetSynth.set(\freq, freq);
	}

}