package sunshine.g4;

import sunshine.sim.Point;

import java.util.List;

public class Cluster {

    public List<Point> nodes;
    public Point center;

    public Point naiveCenter() {
        double x = 0.0d, y = 0.0d;
        for (Point p : nodes) {
            x += p.x;
            y += p.y;
        }
        x /= nodes.size();
        y /= nodes.size();
        return new Point(x, y);
    }

    public Cluster(List<Point> nodes, Point center) {
        this.nodes = nodes;

        if (center != null)
            this.center = center;
        else {
            //calculate center
            this.center = naiveCenter();
        }
    }
}
