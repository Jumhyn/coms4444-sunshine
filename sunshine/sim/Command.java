package sunshine.sim;

import sunshine.sim.Point;
import sunshine.sim.Tractor;
import sunshine.sim.CommandType;

public class Command {
    private CommandType type;
    private Point location;
    
    private Command(CommandType type, Point location)
    {
        this.type = type;
        this.location = location;
    }
    
    public Command(CommandType type)
    {
        this.type = type;
        this.location = null;
    }
    
    public static Command createMoveCommand(Point location)
    {
        return new Command(CommandType.MOVE_TO, new Point(location.x, location.y));
    }
    
    public Point getLocation() {
        return location;
    }
    
    public CommandType getType() {
        return type;
    }
}
