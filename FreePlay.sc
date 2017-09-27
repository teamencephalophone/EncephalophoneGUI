FreePlay {
	var gui, server;
	var oscdef;
	var userNotes;
	var btn;

	*new {arg server, gui;
		^super.new.init(server, gui);
	}

	init {arg thisServer, thisGUI;
		gui = thisGUI;
		server = thisServer;
		this.initSynths;
		this.makeGUI;
	}

	makeGUI {
		var makeBox;
		var noteBox, controlBox, soundBox;

		makeBox = {arg rect, color, margin, gap;
			var thisBox = CompositeView(gui, rect).background_(color);
			thisBox.decorator_(FlowLayout(thisBox.bounds, margin, gap));
		};

		noteBox = makeBox.value(Rect.new(10, 10, gui.bounds.width - 20, gui.bounds.height - 300), Color.grey.alpha_(0.3), 10@0, 0@0);

		controlBox = makeBox.value(Rect.new(10, gui.bounds.height - 260, 400, 300), Color.grey.alpha_(0.2), 10@10, 10@10);

		userNotes = Array.fill(8, {
			NoteVis(100, Color.white, noteBox).animate;
		});

		btn = Button(controlBox, 150@40)
		.states_([
			["START", Color.white, Color.black],
			["STOP", Color.black, Color.white]
		])
		.font_(Font.new().pixelSize_(15))
		.action_({arg button;
			if(button.value == 1,
				{this.startSequence},
				{this.stopSequence}
			)
		});
	}

	initSynths {
		SynthDef.new(\curSynth, {arg freq = 440, amp = 0.4, dur = 0.4;
			var sig, env, out;

			sig = amp *  MdaPiano.ar(freq, decay: 0.1, release: dur);
			DetectSilence.ar(sig, 0.01, doneAction:2);

			Out.ar(0, sig);

		}).add;
	}

	startSequence {
		oscdef = OSCdef(
			\aName,
			{ |msg, time, addr|
				var note;
				("getting - " + msg[1]).postln;
				note = msg[1] - 1;
				{userNotes[note].animate}.defer;
				this.play(note);
			},
			'/fred'
		)
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

	play {arg deg = 1, oct = 2, dur = 0.4, synth = \curSynth;
		var notes = Scale.major.degrees ++ 12;
		var freq = Scale.major.degreeToFreq(notes[deg], 36.midicps, 1);
		Synth.new(synth, [\freq, freq, \dur, dur]);
	}
}