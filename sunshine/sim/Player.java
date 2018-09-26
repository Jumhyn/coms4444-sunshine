package sunshine.sim;

import java.util.List;

import sunshine.sim.Point;
import sunshine.sim.Command;
import sunshine.sim.Tractor;

public interface Player {
    // Initialization function.
    // bales: Location of the bales of hay.
    // n: Number of tractors available.
    // m: Size of the field.
    // t: Total time available.
    public void init(List<Point> bales, int n, double m, double t);

    // Gets the command for the specified tractor.
    public Command getCommand(Tractor tractor);
}
