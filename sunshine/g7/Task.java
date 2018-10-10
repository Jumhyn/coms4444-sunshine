package sunshine.g7;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import sunshine.sim.Command;
import sunshine.sim.CommandType;
import sunshine.sim.Point;

class Task extends ArrayList<Command> {
	static Point barn = new Point(0,0);

	public Task() {
		super();
	}

	public void addRetrieveBale(Point bale) {
		Command[] commands = {
			Command.createMoveCommand(bale),
			new Command(CommandType.LOAD),
			Command.createMoveCommand(barn),
			new Command(CommandType.UNLOAD)
		};

		addAll(Arrays.asList(commands));
	}

	public void addBaleToTrailer(Point bale, Point dropoff) {
		Command[] commands = {
			Command.createMoveCommand(bale),
			new Command(CommandType.LOAD),
			Command.createMoveCommand(dropoff),
			new Command(CommandType.STACK)
		};

		addAll(Arrays.asList(commands));
	}

	public void addTrailerPickup(PointClump clump) {
		Point dropoff = clump.dropPoint;
		Point last = clump.get(clump.size()-1);
		ensureCapacity(70);

		//add(new Command(CommandType.ATTACH));
		add(Command.createMoveCommand(dropoff));
		add(new Command(CommandType.DETATCH));
		
		for (Point bale: clump) {
			if (bale == last) {
				add(Command.createMoveCommand(bale));
				add(new Command(CommandType.LOAD));
				add(Command.createMoveCommand(dropoff));
			} else {
				addBaleToTrailer(bale, dropoff);
			}
		}

		add(new Command(CommandType.ATTACH));
		add(Command.createMoveCommand(barn));
		add(new Command(CommandType.DETATCH));

		add(new Command(CommandType.UNLOAD));
		for (Point bale: clump) {
			if (bale != last) {
				add(new Command(CommandType.UNSTACK));
				add(new Command(CommandType.UNLOAD));
			}
		}
	}
}