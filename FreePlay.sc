FreePlay {
	var gui, server;
	var oscdef;
	var userNotes;
	var btn;
	var instrument;
	var major, minor;
	var keys, curKey;
	var octaves, curOctave;
	var userPattern, tempo;
	var noteData, cur, midiNote;
	var curInstrument, note, keys, curRatio, scales, curScale;
	var instrumentNames, instrumentControls;
	var clock, userClock, pattern, bar, beat;
	var songs, curSong, curPlayingSong;
	var schedFunc;
	//var <isSustaining = false;

	*new {arg server, gui;
		^super.new.init(server, gui);
	}

	init {arg thisServer, thisGUI;
		note = 0;
		gui = thisGUI;
		server = thisServer;
		tempo = 120;
		curOctave = 4;
		curKey = 'C';
		curScale = \major;
		instrument = Dictionary.new;
		keys = Dictionary.new;
		scales = Dictionary.new;
		songs = Dictionary.new;
		scales.putPairs([\major, Scale.major.degrees ++ 12, \minor, Scale.minor.degrees ++ 12]);
		scales.postln;
		keys.putPairs(['C', 0, 'C#', 1, 'Db', 1, 'D', 2, 'D#', 3, 'Eb', 3, 'E', 4, 'F', 5, 'F#', 6, 'Gb', 6, 'G', 7, 'G#', 8, 'Ab', 8, 'A', 9, 'A#', 10, 'Bb', 10, 'B', 11]);
		this.makeDict;
		this.addSongs;
		//cur = Dictionary.with(*[\tempo, ->120, \instrument -> \Violin, \note -> 60]);
		this.initSynths;
		this.makeGUI;
	}

	addSongs {
		var makeSongBufs;

		makeSongBufs = {arg thisFile;
			var noteDict = Dictionary.new;
				var fileArray = thisFile.fileName.split($.);
				noteDict.putPairs([\Name, fileArray[0].asSymbol, \Buffer, Buffer.read(server, thisFile.fullPath), \Key, fileArray[1], \Scale, fileArray[2], \Tempo, fileArray[3]]);
			noteDict;
		};

		PathName(Platform.userAppSupportDir ++ "/downloaded-quarks/EncephalophoneGUI/Songs/").entries.do({arg thisEntry;
			songs.put(thisEntry.fileName.split($.)[0].asSymbol, makeSongBufs.value(thisEntry));
		});
		curSong = songs.asSortedArray[0][0];
	    songs.postln;
		curSong.postln;
	}

	makeDict {
		var makeBufs, cond, loading;
		var list;
		list = SortedList.new;
		cond = Condition.new;


		makeBufs = {arg entries;
			var noteDict = Dictionary.new;
			var minimum = 128;
			var maximum = 0;
			var startView;

			entries.filesDo({arg thisFile;
				var fileArray = thisFile.fileName.split($.);
				var midiNum = fileArray[fileArray.size - 3].namemidi;
				minimum = min(midiNum, minimum);
				maximum = max(midiNum, maximum);
				noteDict.put(midiNum, Buffer.read(server, thisFile.fullPath/*,action:{cond.test_(true).signal}*/));
				//cond.wait;
				//cond.test_(false);
			});
			noteDict.put(\min, minimum);
			noteDict.put(\max, maximum);
			noteDict;
		};

		PathName(Platform.userAppSupportDir ++ "/downloaded-quarks/EncephalophoneGUI/Musical_Instruments_midi/").entries.do({arg thisEntry;
			list.add(thisEntry.folderName);
			instrument.put(thisEntry.folderName.asSymbol, makeBufs.value(thisEntry));
		});
		instrumentNames = list.asArray;
	}

/*	isSustaining_ {arg bool;
		if(bool, {
			// nowPlayingSynth.set(\sustaining, 1);
			isSustaining = true;
		}, {
			// nowPlayingSynth.set(\gate, 0);
			this.releaseLast;
			isSustaining = false;
		})
	}*/

	makeGUI {
		var makeBox, makeView;
		var noteBox, controlBox, soundBox, backingBox;
		var instrumentBox, nameArray, controlDict;
		var startView;

		"Got to GUI".postln;
		controlDict = Dictionary.new;

		makeBox = {
			View().background_(Color.grey.alpha_(0.2));
		};

		/*makeView = {arg parent, size, string;
			var gap;
			var thisBox;
			gap = 5;
			thisBox = CompositeView(parent).background_(Color.clear);
			thisBox.decorator_(FlowLayout(thisBox.bounds, 0@0, 0@gap));
			StaticText(thisBox, (thisBox.bounds.width / 2)@thisBox.bounds.height).string_(string).stringColor_(Color.white);
			thisBox;
		};*/

/*		makeView = {arg parent, string, control;
			controlDict.put(string, control);
			HLayout(StaticText(parent).string_(string), control);
		};*/

		noteBox = makeBox.value().fixedHeight_(gui.bounds.height - 300);
		controlBox = makeBox.value();
		instrumentBox = makeBox.value();
		backingBox = makeBox.value().fixedWidth_(600);

		userNotes = Array.fill(8, {
			var thisNote, thisView;
			thisNote = NoteVis(95, Color.white, thisView).animate;
			thisView = thisNote.getView;
			[thisNote, thisView];
		});

		userNotes = userNotes.flop;

		startView = View().background_(Color.black);
		gui.layout = VLayout (
			noteBox, HLayout(controlBox, backingBox)
		);

		noteBox.layout_(HLayout(*userNotes[1]));

		btn = Button()
			.states_([
				["START", Color.white, Color.black],
				["STOP", Color.black, Color.white]
			])
			.font_(Font.new().pixelSize_(15))
			.action_({arg button;
				if(button.value == 1,
					{
					clock ?? {this.startClock; this.showInfo};
					    this.startSequence;
					    this.quantNotes;
					},
					{
					    this.stopUserClock;
						this.stopSequence;
					}
				)
			});

		controlBox.layout = VLayout(btn, instrumentBox);


		instrumentControls = [
			[
				"Tempo",                                                   // name
				TextField(),                                               // control
				{arg field; (userClock !? {userClock.tempo_(field.value.asInteger/60)}); tempo =  field.value.asInteger}, // action
				120                                                          // valueAction
			],
			[
				"Instrument",
				PopUpMenu(),
				{arg field;	curInstrument = field.item.asSymbol;},
				instrumentNames[0],
				instrumentNames
			],
			[
				"Key",
				TextField(),
				{arg field;	curKey = field.value.asSymbol;},
				"C"
			],
			[
				"Octave",
				TextField(),
				{arg field;curOctave = field.value.asInteger;},
				3
			],
			[
				"Scale",
				PopUpMenu(),
				{arg field; curScale = field.item.asSymbol; curScale.postln},
				"major",
				scales.asSortedArray.flop[0];
			]
		].collect({arg thisData;
			var control, layout;
			control = thisData[1];
			thisData.postln;
			if (thisData.size > 4, {
				control.items_(thisData[4]);
			});
			control.action_(thisData[2]);
			control.valueAction_(thisData[3]);
			[control, HLayout(*[StaticText(instrumentBox).string_(thisData[0] + ": ").stringColor_(Color.white), control.fixedWidth_(100)]), thisData[0].asSymbol];
		});

		instrumentControls = instrumentControls.flop;
	    instrumentControls.postln;

	instrumentBox.layout = VLayout(*instrumentControls[1]);
		backingBox.layout = HLayout(
			VLayout(
				Button().states_([
					["START SONG", Color.white, Color.black],
					["STOP SONG", Color.black, Color.white]
				]).action_({arg button;
					if(button.value == 1,
						{
							this.startSong
						},
						{
							this.stopSong
						}
					)
				});
			),
			VLayout(View().fixedWidth_(400))
		);
/*		instrumentControls.do({arg thisLayout;
			instrumentBox.layout.add(thisLayout);
		});*/

		//instrumentBox.layout = VLayout(*instrumentControls);
		/*
		instrumentBox.layout_(
			Vlayout(
				HLayout(T)
TextField(makeView.value(instrumentBox, "Tempo:")
				.action_({arg field;
					tempo = field.value.asInteger;
				})
					.valueAction_(120)],

				[PopUpMenu(makeView.value(instrumentBox, 160@20, "Instrument:"), 80@20)
				.items_(instrumentNames)
				.action_({arg field;
					curInstrument = field.item.asSymbol;
				}).valueAction_(instrumentNames[0])],

				[TextField(makeView.value(instrumentBox, 160@20, "Key:"), 80@20)
				.action_({arg field;
					curKey = field.value.asSymbol;
				})
					.valueAction_("C")],

				[TextField(makeView.value(instrumentBox, 160@20, "Octave:"), 80@20)
				.action_({arg field;
					curOctave = field.value.asInteger;
				})
					.valueAction_("4")],

				[TextField(makeView.value(instrumentBox, 160@20, "Scale:"), 80@20)
				.action_({arg field;
				})
					.valueAction_("major")],
			)
		)
		*/
	}



	initSynths {
		SynthDef.new(\curSynth, {arg freq = 440, amp = 0.4, dur = 0.4;
			var sig, env, out;

			sig = amp *  MdaPiano.ar(freq, decay: 0.1, release: dur);
			DetectSilence.ar(sig, 0.01, doneAction:2);

			Out.ar(0, sig);

		}).add;

		SynthDef.new(\playSound,
			{arg buffer, gain = -6, att = 0.05, sus = 0.2, rel = 0.3, dura = 0.2, ratio = 1;
				var in, out, env, amp;
				amp = gain.dbamp;
				//env = EnvGen.kr(Env.adsr(a, d, s, r), levelScale: amp, timeScale: dur, doneAction: 2);
				in = PlayBuf.ar(1, buffer, BufRateScale.kr(buffer) * ratio);
				out = in!2;
				out = out *  EnvGen.kr(Env([0,1, 1, 0], [att, sus, rel]),  timeScale: BufDur.kr(buffer) * dura, doneAction: 2);
				// out = out *  EnvGen.kr(Env.adsr(att, dec, susL, rel), Select.kr(sustaining, [Trig1.kr(1, BufDur.kr(buffer)).neg + 1, gate]),  doneAction: 2);
				//out = out *  EnvGen.kr(Env.adsr(a, d, s, r), gate,  doneAction: 2);
				Out.ar(0, out);
			}).add;

		SynthDef.new(\playSong,
			{arg buffer, gain = -6, turnOff = 1;
				var in, out, env, amp;
				amp = gain.dbamp;
				//env = EnvGen.kr(Env.adsr(a, d, s, r), levelScale: amp, timeScale: dur, doneAction: 2);
				in = PlayBuf.ar(2, buffer, BufRateScale.kr(buffer));

				out = in * turnOff;
				DetectSilence.ar(out, doneAction: 2);
				out = out * amp;
				Out.ar(0, out);
			}).add;

	}

	startSequence {
		oscdef = OSCdef(
			\playOsc,
			{ |msg, time, addr|
				msg.postln;
				note = msg[1] - 1;
				this.setRatio();
			},
			'/fred'
		)
	}

	startClock {
		clock = TempoClock.new(tempo/60);
	}

	startSong {
		var playSong, startUser;
        instrumentControls[2].do({arg thisSymbol, index;
			if (songs[curSong].includesKey(thisSymbol), {
				if (thisSymbol != \Scale, {
					instrumentControls[0][index].valueAction_(songs[curSong][thisSymbol]);
				}, {
					instrumentControls[0][index].valueAction_(	scales.asSortedArray.flop[0].indexOf(songs[curSong][thisSymbol].asSymbol));
				});
			})
	    });

		if (btn.value == 1, {
			btn.valueAction_(0);
		});

		this.stopClock;
		this.startClock;
		this.showInfo;

		playSong = {
			curPlayingSong = Synth(\playSong, [\buffer: songs[curSong][\Buffer]]);
			NodeWatcher(server).register(curPlayingSong);
		};

		startUser = {
			{btn.valueAction_(1)}.defer;
		};

		clock.schedAbs(clock.nextBar, playSong);
		clock.schedAbs(8, startUser);
	}

	stopSong {
		btn.valueAction_(0);
		curPlayingSong.free;
		this.stopClock;
	}

	showInfo {
		var postInfo;
			postInfo = {
				//userNotes.postln;
				("beats :" + (clock.beats.floor%clock.beatsPerBar + 1)).postln;
				("bar :" + (clock.bar + 1)).postln;
				"".postln;
				//note.postln;
				1
			};
			clock.schedAbs(clock.nextBar, postInfo);
	}

	scheduleNotes {
		userClock ?? {userClock = TempoClock.new(tempo/60)};
		schedFunc = {
			{userNotes[0][note].animate}.defer;
			Synth(\playSound, [\buffer: instrument[curInstrument][midiNote], \ratio, curRatio]);
			1
		};
		userClock.schedAbs(userClock.nextTimeOnGrid, schedFunc);
	}

	quantNotes {
		clock !? {clock.schedAbs(clock.beats.floor + 1, {this.scheduleNotes})};
	}

	stopClock {
		clock !? {clock.clear};
		clock = nil;
		clock.postln;
	}

	stopUserClock {
		userClock.clear;
		userClock = nil;
		userClock.postln;
	}

	stopSequence {
		oscdef.free;
	}

	getOscDef {
		^oscdef;
	}

	getBtn {
		^btn;
	}

	setRatio {
		var rout, thisBuf, min, max, midiDif, major, thisNote;
		    thisNote = keys[curKey] + scales[curScale][note] + (12 * curOctave) + 24;
			min = instrument[curInstrument][\min];
			max = instrument[curInstrument][\max];
			midiDif = 0;
			case
			{thisNote < min} {
				midiDif = (thisNote - min);
				thisNote = min;
			}
			{thisNote > max} {
				midiDif = (thisNote - max);
				thisNote = max;
			};
			curRatio = midiDif.midiratio;
			midiNote = thisNote;
		    curRatio.postln;
			instrument[curInstrument][thisNote].postln;
	}
}