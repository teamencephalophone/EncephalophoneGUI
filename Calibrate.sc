Calibrate {
	var server, gui;

	*new {arg server, gui;
		^super.new.init(server, gui);
	}

	init {arg thisServer, thisGUI;
		server = thisServer;
		gui = thisGUI;
	}

}