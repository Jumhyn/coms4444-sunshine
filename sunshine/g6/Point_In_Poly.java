package sunshine.g6;

/*
 * Class for determining whether a given point is within a polygon defined by a set of vertices
 * Adapted from the code on this website: https://www.sanfoundry.com/java-program-check-whether-given-point-lies-given-polygon/
 */

import java.util.List;

import sunshine.sim.Point;

public class Point_In_Poly
{
 
    public static boolean onSegment(Point p, Point q, Point r)
    {
        if (q.x <= Math.max(p.x, r.x) && q.x >= Math.min(p.x, r.x)
                && q.y <= Math.max(p.y, r.y) && q.y >= Math.min(p.y, r.y))
            return true;
        return false;
    }
 
    public static int orientation(Point p, Point q, Point r)
    {
        double val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);
 
        Double val_d = new Double(val);
        if (val_d.equals(0.0))
            return 0;
        return (val > 0) ? 1 : 2;
    }
 
    public static boolean doIntersect(Point p1, Point q1, Point p2, Point q2)
    {
 
        int o1 = orientation(p1, q1, p2);
        int o2 = orientation(p1, q1, q2);
        int o3 = orientation(p2, q2, p1);
        int o4 = orientation(p2, q2, q1);
 
        if (o1 != o2 && o3 != o4)
            return true;
 
        if (o1 == 0 && onSegment(p1, p2, q1))
            return true;
 
        if (o2 == 0 && onSegment(p1, q2, q1))
            return true;
 
        if (o3 == 0 && onSegment(p2, p1, q2))
            return true;
 
        if (o4 == 0 && onSegment(p2, q1, q2))
            return true;
 
        return false;
    }
 
    public static boolean isInside(List<Point> polygon, int n, Point p)
    {
        int INF = 10000;
        if (n < 3)
            return false;
 
        Point extreme = new Point(INF, p.y);
 
        int count = 0, i = 0;
        do
        {
            int next = (i + 1) % n;
            if (doIntersect(polygon.get(i), polygon.get(next), p, extreme))
            {
                if (orientation(polygon.get(i), p, polygon.get(next)) == 0)
                    return onSegment(polygon.get(i), p, polygon.get(next));
 
                count++;
            }
            i = next;
        } while (i != 0);
 
        return (count & 1) == 1 ? true : false;
    }
 
}