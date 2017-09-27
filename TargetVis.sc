TargetVis {
	var color, default, gui, uv, target;

	*new {arg size, color, gui;
		^super.new.init(size, color, gui);
	}

	init {arg size, thisColor, thisGUI;
		var uvSize = 20;
		default = thisColor;
		color = default;
		gui = thisGUI;
		uv = UserView(gui, size@size);
		this.update();
		uv.animate_(true);
	}

	update {
		uv.drawFunc={|uview|
		Pen.fillColor_(color);
		Pen.fillOval( Rect.aboutPoint( Point(50, 50), 20 , 20))};
	}


    makeTarget {
		color = Color.blue;
		this.update();
	}

	makeDefault {
		color = default;
	}
}
