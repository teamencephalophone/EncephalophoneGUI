Search {
	var initData;

	*new {arg data;
		^super.new.init(data);
	}

	init {arg thisData;
		initData = thisData;
		initData.postln;
	}

	findData {arg query;
		var result = Array.new;
		"finding-data".postln;
		if (query != "", {
			initData.do({arg thisVal;
				thisVal.postln;
				if (thisVal.containsi(query),
					{
						query.postln;
						result.postln;
						result = result.add(thisVal)
					}
				);
			});
		}, {
			^initData;
		});
		^result;
	}
}

