package sunshine.g2;
import java.util.ArrayList;

import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;

import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.CommandType;
import sunshine.sim.Point;



public class Player implements sunshine.sim.Player {
    // Random seed of 42.
    private int seed = 42;
    private Random rand;
    private Point dest;
    List<Point> bales;
    private int counter;
    List<Point> balesList;
    Point balesListCenter;
    public Player() {
        rand = new Random(seed);
    }
    
    public void init(List<Point> bales, int n, double m, double t)
    {
        this.bales = bales;
        balesList = new ArrayList<Point>();
        counter = 0;
    	balesListCenter = this.bales.get(getFurthestBale());
    	buildList();
    }

	public double calcEucDistance(Point origin, Point dest1)
    {
        Point result = new Point(0.0, 0.0);
        result.y = Math.abs(origin.y - dest1.y);
        result.x = Math.abs(origin.x - dest1.x);
        double distance = Math.sqrt((result.y)*(result.y) +(result.x)*(result.x));

        return distance;
    }

    public int getNearestBale()
    {
    	Point target = balesListCenter;
        double minDist = 9999999999.99;
        int minIndex = 0;

        for (int i=0; i<bales.size(); i++) {
            Point bale = bales.get(i);

            Double distance = calcEucDistance(target, bale);
            if (distance < minDist)
            {
                minDist = distance;
                minIndex = i;
            }
        }

        return minIndex;
    }

    public int getFurthestBale()
    {
        Point home = new Point(0.0, 0.0);
        double maxDist = 0.0;
        int maxIndex = 0;

        for (int i=0; i<bales.size(); i++) {
           Point bale = bales.get(i);

           Double distance = calcEucDistance(home, bale);
           if (distance > maxDist)
           {
                maxDist = distance;
                maxIndex = i;
           }
        }

        return maxIndex;
    }

    public Command getCommand(Tractor tractor)
    {	
        if(tractor.getLocation().equals(new Point(0.0,0.0)) && tractor.getAttachedTrailer()!=null && balesList.size()!=0){
			return Command.createMoveCommand(balesListCenter);
        }
        else if(tractor.getLocation().equals(balesListCenter) && tractor.getAttachedTrailer()!=null && balesList.size()!=0){
        	return new Command(CommandType.DETATCH);
        }
        else if(tractor.getLocation().equals(balesListCenter) && tractor.getAttachedTrailer()==null && balesList.size()!=0 && !tractor.getHasBale()){
			dest = balesList.remove(rand.nextInt(balesList.size()));
            return Command.createMoveCommand(dest);
        }
        else if (tractor.getHasBale()&&tractor.getLocation().equals(dest)){
        	return Command.createMoveCommand(balesListCenter);
        }
        else if (tractor.getHasBale()==false&&tractor.getLocation().equals(dest)){
        	return new Command(CommandType.LOAD);
        }
        else if (tractor.getHasBale()&&tractor.getLocation().equals(balesListCenter)&&balesList.size()!=0){
        	counter+=1;
        	return new Command(CommandType.STACK);
        }
        else if (tractor.getHasBale()&&tractor.getLocation().equals(balesListCenter)&&balesList.size()==0 && tractor.getAttachedTrailer()==null){
        	return new Command(CommandType.ATTACH);
        }
        else if (tractor.getHasBale()&&tractor.getLocation().equals(balesListCenter)&&balesList.size()==0 && tractor.getAttachedTrailer()!=null){
		   return Command.createMoveCommand(new Point(0.0,0.0));
		}
        else if(tractor.getLocation().equals(new Point(0.0,0.0)) && tractor.getAttachedTrailer()!=null && balesList.size()==0 && tractor.getHasBale()){
			return new Command(CommandType.UNLOAD);
        }
        else if(tractor.getLocation().equals(new Point(0.0,0.0)) && tractor.getAttachedTrailer()!=null && balesList.size()==0 && !tractor.getHasBale()){
        	return new Command(CommandType.DETATCH);
        }
        else if(tractor.getLocation().equals(new Point(0.0,0.0)) && tractor.getAttachedTrailer()==null && balesList.size()==0 && !tractor.getHasBale() && counter!=0){
        	counter-=1;
        	return new Command(CommandType.UNSTACK);
        }
        else if(tractor.getLocation().equals(new Point(0.0,0.0)) && tractor.getAttachedTrailer()==null && balesList.size()==0 && tractor.getHasBale()){
        	return new Command(CommandType.UNLOAD);
        }
        else if(tractor.getLocation().equals(new Point(0.0,0.0)) && tractor.getAttachedTrailer()==null && balesList.size()==0 && !tractor.getHasBale() && counter==0){
        	balesListCenter = bales.get(getFurthestBale());
        	buildList();
        	return new Command(CommandType.ATTACH);
        }
        else{
        	return null;
        }
    }

    private void buildList() {
    	for (int i = 0; i < 11; i++) {
    		int index = getNearestBale();
    		balesList.add(bales.get(index));
    		bales.remove(index);
    	}
    }



    
}
