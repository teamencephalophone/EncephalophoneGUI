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
	var noteData, cur;
	var curInstrument, note, keys;
	var instrumentNames;

	*new {arg server, gui;
		^super.new.init(server, gui);
	}

	init {arg thisServer, thisGUI;
		note = 0;
		gui = thisGUI;
		server = thisServer;
		instrument = Dictionary.new;
		keys = Dictionary.new;
		keys.putPairs(['C', 0, 'C#', 1, 'Db', 1, 'D', 2, 'D#', 3, 'Eb', 3, 'E', 4, 'F', 5, 'F#', 6, 'Gb', 6, 'G', 7, 'G#', 8, 'Ab', 8, 'A', 9, 'A#', 10, 'Bb', 10, 'B', 11]);
		tempo = 120;
		curOctave = 4;
		curKey = 'C';
		this.makeDict;
		//cur = Dictionary.with(*[\tempo, ->120, \instrument -> \Violin, \note -> 60]);
		this.initSynths;
		this.makeGUI;
		this.initPattern;
	}

	makeDict {
		var makeBufs, cond, loading;
		var list;
		list = SortedList.new;
		cond = Condition.new;

		loading = StaticText(gui, Rect(10, 10, 70, 20)).string_("loading...").stringColor_(Color.white).font_(Font().pixelSize_(20));

		makeBufs = {arg entries;
			var noteDict = Dictionary.new;
			var minimum = 128;
			var maximum = 0;
			entries.filesDo({arg thisFile;
				var fileArray = thisFile.fileName.split($.);
				var midiNum = fileArray[fileArray.size - 3].namemidi;
				minimum = min(midiNum, minimum);
				maximum = max(midiNum, maximum);
				noteDict.put(midiNum, Buffer.read(server, thisFile.fullPath.postln/*,action:{cond.test_(true).signal}*/));
				//cond.wait;
				//cond.test_(false);
			});
			noteDict.put(\min, minimum);
			noteDict.put(\max, maximum);
			noteDict;
		};

		PathName("/Users/dxlocal/Desktop/Musical_Instruments_midi/").entries.do({arg thisEntry;
			list.add(thisEntry.folderName);
			instrument.put(thisEntry.folderName.asSymbol, makeBufs.value(thisEntry));
			loading.visible_(false);
		});
		instrumentNames = list.asArray;
	}

	makeGUI {
		var makeBox, makeView;
		var noteBox, controlBox, soundBox;
		var instrumentBox, nameArray;

		makeBox = {arg rect, color, margin, gap;
			var thisBox = CompositeView(gui, rect).background_(color);
			thisBox.decorator_(FlowLayout(thisBox.bounds, margin, gap));
		};

		makeView = {arg parent, size, string;
			var gap;
			var thisBox;
			gap = 5;
			thisBox = CompositeView(parent, size).background_(Color.clear);
			thisBox.decorator_(FlowLayout(thisBox.bounds, 0@0, 0@gap));
			StaticText(thisBox, (thisBox.bounds.width / 2)@thisBox.bounds.height).string_(string).stringColor_(Color.white);
			thisBox;
		};

		noteBox = makeBox.value(Rect.new(20, 10, gui.bounds.width - 40, gui.bounds.height - 300), Color.grey.alpha_(0.3), 10@0, 0@0);

		controlBox = makeBox.value(Rect.new(20, gui.bounds.height - 260, 380, 300), Color.grey.alpha_(0.2), 10@10, 10@10);

		instrumentBox = makeBox.value(Rect.new(30, gui.bounds.height - 200, 180, 200), Color.grey.alpha_(0.2), 10@10, 10@10);

		userNotes = Array.fill(8, {
			NoteVis(95, Color.white, noteBox).animate;
		});

		btn = Button(controlBox, 180@40)
		.states_([
			["START", Color.white, Color.black],
			["STOP", Color.black, Color.white]
		])
		.font_(Font.new().pixelSize_(15))
		.action_({arg button;
			if(button.value == 1,
				{
					this.startSequence;
					this.startPattern;
				},
				{
					this.stopSequence;
					this.stopPattern;
				}
			)
		});

		TextField(makeView.value(instrumentBox, 160@20, "Tempo:"), 80@20)
		.action_({arg field;
			tempo = field.value.asInteger;
		})
		.valueAction_(120);

		PopUpMenu(makeView.value(instrumentBox, 160@20, "Instrument:"), 80@20)
		.items_(instrumentNames)
		.action_({arg field;
			curInstrument = field.item.asSymbol;
		}).valueAction_(instrumentNames[0]);

		TextField(makeView.value(instrumentBox, 160@20, "Key:"), 80@20)
		.action_({arg field;
			curKey = field.value.asSymbol;
		})
		.valueAction_("C");

		TextField(makeView.value(instrumentBox, 160@20, "Octave:"), 80@20)
		.action_({arg field;
			curOctave = field.value.asInteger;
		})
		.valueAction_("4");

		TextField(makeView.value(instrumentBox, 160@20, "Scale:"), 80@20)
		.action_({arg field;
		})
		.valueAction_("major");
	}



	initSynths {
		SynthDef.new(\curSynth, {arg freq = 440, amp = 0.4, dur = 0.4;
			var sig, env, out;

			sig = amp *  MdaPiano.ar(freq, decay: 0.1, release: dur);
			DetectSilence.ar(sig, 0.01, doneAction:2);

			Out.ar(0, sig);

		}).add;

		SynthDef.new(\playSound,
			{arg buffer, gain = -6, a = 0.05, d = 0.1 , s = 0.1, r = 0.5, dur = 0.3, ratio = 1;
				var in, out, env, amp;
				amp = gain.dbamp;
				//env = EnvGen.kr(Env.adsr(a, d, s, r), levelScale: amp, timeScale: dur, doneAction: 2);
				in = PlayBuf.ar(1, buffer, BufRateScale.kr(buffer) * ratio);
				out = in!2;
				out = out *  EnvGen.kr(Env([0, 1, 0], [0.1, 0.9]),  timeScale: BufDur.kr(buffer) * dur, doneAction: 2);
				Out.ar(0, out);
			}).add;
	}

	startSequence {
		oscdef = OSCdef(
			\playOsc,
			{ |msg, time, addr|
				note = msg[1] - 1;
			},
			'/fred'
		)
	}

	initPattern {
		userPattern = Task ({
			loop {
					{userNotes[note].animate}.defer;
					this.play(note);
					(60.0 / tempo).wait;
			}
		}, TempoClock);
	}

	startPattern {
		userPattern.start;
	}

	stopPattern {
		userPattern.stop;
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

	play {arg note;
		var rout, thisBuf, min, max, ratio, midiDif, major;
		major = Scale.major.degrees ++ 12;
		note = keys[curKey] + major[note] + (12 * curOctave) + 24;
		min = instrument[curInstrument][\min];
		max = instrument[curInstrument][\max];
		midiDif = 0;
		case
		{note < min} {
			midiDif = (note - min);
			note = min;
		}
		{note > max} {
			midiDif = (note - max);
			note = max;
		};
		ratio = midiDif.midiratio;

		note.postln;
		ratio.postln;
		instrument[curInstrument][note].postln;

		Synth.new(\playSound, [\buffer, instrument[curInstrument][note], \dur, 0.3, \ratio, ratio]);
	}
}