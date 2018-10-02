package sunshine.g4;

import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;

import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.CommandType;
import sunshine.sim.Point;


public class Player implements sunshine.sim.Player {
    
    List<Point> bales;

    public Player() {
    }
    
    public void init(List<Point> bales, int n, double m, double t)
    {
        this.bales = bales;
    }
    
    public Command getCommand(Tractor tractor)
    {
        if (tractor.getHasBale())
        {
            if (tractor.getLocation().equals(new Point(0.0, 0.0)))
            {
                return new Command(CommandType.UNLOAD);
            }
            else
            {
                return Command.createMoveCommand(new Point(0.0, 0.0));
            }
        }
        else
        {
            if (tractor.getLocation().equals(new Point(0.0, 0.0)))
            {
                if (tractor.getAttachedTrailer() == null && bales.size() > 0)
                {
                    Point p = bales.remove(0);
                    return Command.createMoveCommand(p);            
                }
                else
                {
                    return new Command(CommandType.DETATCH);
                }
            }
            else
            {
                return new Command(CommandType.LOAD);
            }
        }
    }
}
