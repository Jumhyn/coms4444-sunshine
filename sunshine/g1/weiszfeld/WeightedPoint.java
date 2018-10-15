/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sunshine.g1.weiszfeld;

/**
 * Point with weight
 */
public class WeightedPoint {
    private Point point;
    private double weight;

    public WeightedPoint(double weight, Point p){
    	point = p;
    	this.weight = weight;
    }
    
    public WeightedPoint(double weight, double... x) {
    	this(weight, new Point(x));
    }
    
    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
    
}
