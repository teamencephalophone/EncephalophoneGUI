NoteVis {
	var color, gui, uv;

	*new {arg size, color, gui;
		^super.new.init(size, color, gui);
	}

	init {arg size, thisColor, thisGUI;
		var uvSize = 20;
		color = thisColor;
		gui = thisGUI;
		uv = UserView(gui, size@size);
		uv.background_(Color.clear);
		uv.drawFunc={|uview|
			if (uview.frame < 20, {
				Pen.fillColor_( thisColor.alpha_(0.9 / uview.frame));
				Pen.fillOval( Rect.aboutPoint( Point(50, 50), 20 , 20));

			},
			{
				uview.clearDrawing();
				uview.animate_(false)}
			);
		};
	}

	animate {
		uv.animate_(true);
	}

}