package sunshine.g4;

import sunshine.sim.Point;

import java.io.*;
import java.util.List;

public class Cluster {
    /*******  DEBUG MODE******/
    private final boolean DEBUG_MODE = false;

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

    private static final double alpha = 0.000000001;
    private static final int iters = 1000000000;

    //Euclidean Partial Derivative over Px
    private double EPDx(Point tmpCenter, Point p) {
        return (tmpCenter.x - p.x) / Math.sqrt(Math.pow((p.x - tmpCenter.x), 2) + Math.pow((p.y - tmpCenter.y), 2)) / 5;
    }

    //Euclidean Partial Derivative over Py
    private double EPDy(Point tmpCenter, Point p) {
        return (tmpCenter.y - p.y) / Math.sqrt(Math.pow((p.x - tmpCenter.x), 2) + Math.pow((p.y - tmpCenter.y), 2)) / 5;
    }

    //Compensation for moving back to origin to Unload bales Partial Derivative x
    private double CPDx(Point tmpCenter) {
        return tmpCenter.x * Math.sqrt(Math.pow(tmpCenter.x, 2) + Math.pow(tmpCenter.y, 2)) / 2;
    }

    //Compensation for moving back to origin to Unload bales Partial Derivative y
    private double CPDy(Point tmpCenter) {
        return tmpCenter.y * Math.sqrt(Math.pow(tmpCenter.x, 2) + Math.pow(tmpCenter.y, 2)) / 2;
    }

    //Overall Partial Derivative over Px
    private double dPx(Point tmpCenter) {
        double result = 0.0d;
        for (Point p : nodes) {
            result += EPDx(tmpCenter, p) + CPDx(tmpCenter);
        }
        return result;
    }

    //Overall Partial Derivative over Py
    private double dPy(Point tmpCenter) {
        double result = 0.0d;
        for (Point p : nodes) {
            result += EPDy(tmpCenter, p) + CPDy(tmpCenter);
        }
        return result;
    }

    private Point updateCenter(double dPx, double dPy, Point tmpCenter) {
        return new Point(tmpCenter.x - alpha * dPx, tmpCenter.y - alpha * dPy);
    }

    private boolean isIn(Point target, Point origin, double radius) {
        return Euclidean(target, origin) < radius;
    }

    private double objective(Point tmpCenter) {
        double result = 0.0d;
        for (Point p : nodes) {
            result += Euclidean(p, tmpCenter);
        }
        result /= 5;
        result += Euclidean(tmpCenter, new Point(0.0d, 0.0d)) / 2;
        return result;
    }

    public Point SGDCenter(PrintWriter writer) {

        Point p = naiveCenter();
        Point firstP = new Point(p.x, p.y);
        Point lastP;
        double originObj = objective(firstP);
        double maxRadius = Double.MIN_VALUE;
        for (Point x : nodes) {
            maxRadius = Math.max(maxRadius, Euclidean(x, p));
        }
        int currentStep = 0;
        double obj = Double.MAX_VALUE;
        while (isIn(p, firstP, maxRadius) && currentStep < iters) {
            currentStep++;
            if (DEBUG_MODE) {
                if (currentStep % 100 == 0) {
                    writer.println(obj);
                }
            }
            lastP = p;
            p = updateCenter(dPx(p), dPy(p), p);
            double newObj = objective(p);
//            if (newObj > obj) {
//                return lastP;
//            }
            obj = newObj;
        }
        if (originObj > obj) {
            if (DEBUG_MODE) {
                writer.print("SGD is better:");
                writer.println(obj + "  " + originObj);
            }
            return p;
        } else {
            if (DEBUG_MODE) {
                writer.print("Naive is better:");
                writer.println(obj + "  " + originObj);
            }
            return firstP;
        }
    }

    public Cluster(List<Point> nodes, Point center) {
        this.nodes = nodes;

        if (center != null)
            this.center = center;
        else {
            //calculate center
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(new FileOutputStream(
                        new File("objective-series.txt"),
                        true /* append = true */));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
//            this.center = naiveCenter();
            this.center = SGDCenter(writer);
            writer.close();
        }
    }

    private double Euclidean(Point p1, Point p2) {
        return (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y);
    }
}
