package sunshine.sim;

import sunshine.sim.Point;
import sunshine.sim.Trailer;

public interface Tractor {
    public int getId();
    public Point getLocation();
    public boolean getHasBale();
    public Trailer getAttachedTrailer();
}
