package sunshine.g5;

import java.util.List;
import java.util.Collections;
import java.util.*;
import java.util.Random;
import java.util.HashMap;
import java.lang.Math;
import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.Trailer;
import sunshine.sim.CommandType;
import sunshine.sim.Point;
import java.util.PriorityQueue;

public class Player implements sunshine.sim.Player {
    // Random seed of 42.
    private int seed = 42;
    private Random rand;
    int CollectBales;
    int CarNum;
    double len;
    double range;
    int layers;
    List<Point> bales;
    List<Point> inRangeBales;
    List<Point> outRangeBales;
    List<Double> degres;
    int[] balesdist;
    int[] CarDist;
    int[] CarRDist;
    double[] bound;
    MutableTrailer[] tlist;
    int outRangeSize;
    int outRangeSize2;
    int start;
    int Stcount;
    Map<Integer, List<Integer>> map;
    private static class MutableTrailer implements Trailer
    {
        public Point location;
        public int numBales;

        public MutableTrailer()
        {
            this.location = new Point(0, 0);
            this.numBales = 0;
        }

        public Point getLocation()
        {
            return new Point(location.x, location.y);
        }

        public int getNumBales()
        {
            return numBales;
        }
    }
    public Player() {
        rand = new Random(seed);
    }

    public void init(List<Point> bales, int n, double m, double t)
    {
        range = 320.0;
        Stcount = 1;
        CarNum  = n ;
        len = m;
        System.out.println(CarNum);
        outRangeBales = new ArrayList<>();
        inRangeBales = new ArrayList<>();
        tlist = new MutableTrailer[CarNum];
        layers = (int)(Math.sqrt(2)*m/range);
        balesdist = new int[layers];
        bound = new double[layers];
        degres = new ArrayList<>();
        for(int i=0;i<layers;i++){
            Double d = range*(i+1)>m?m:range*(i+1)/Math.sqrt(2);
            degres.add(Math.acos(d/(range*(i+1))));
            bound[i] = range*(i+1);
        }

        for(int i=0;i<CarNum ;i++){
            tlist[i] = new MutableTrailer();
        }

        for(int i=0;i<CarNum;i++){

                tlist[i].location.x = Math.cos(Math.abs((i+1)*Math.PI/(CarNum*2)))*range*(i%layers+1)+1;
                tlist[i].location.y = Math.sin(Math.abs((i+1)*Math.PI/(CarNum*2)))*range*(i%layers+1)+1;


        }
        start = 0;

        for(Point p: bales){
            if(calDistance(p)>=range){
                outRangeBales.add(p);
            }else{
                inRangeBales.add(p);
            }
            for(int i=0;i<layers;i++){
                Double r =range*(i+1);
                Double r2 =range*(i+2);
                int countR=0;
                if(calDistance(p)>r && calDistance(p)<=r2){
                    balesdist[i] = balesdist[i]+1;
                    break;
                }
            }
        }
        int min = Integer.MAX_VALUE;
        for(int i=0;i<balesdist.length;i++){
            if(min>balesdist[i])
                min = balesdist[i];
        }
        CarDist = new int[layers];
        //for(int i=0;i<layers;i++) CarDist[i] = (balesdist[i]*(1+(i+1)/4*layers))/min;
        for(int i=0;i<layers;i++) CarDist[i] = balesdist[i]/min;
        map = new HashMap<>();
        CarRDist = new int[layers];
        int index = 0;
        for(int i=0;i<CarNum/layers+1;i++){
            for(int j=0;j<layers;j++){
                for(int k=0;k<CarDist[j];k++){
                    if(index<CarNum){
                         List<Integer> l = new ArrayList<>();
                         l.add(j);
                         l.add(i*CarDist[j]+k);
                         CarRDist[j] = CarRDist[j]+1;
                         map.put(index, l);
                         index  = index+1;
                    }
                }
            }

        }
        outRangeSize = outRangeBales.size();
        outRangeSize2 = outRangeBales.size();
        this.bales = bales;
    }
    private double calDistance(Point p){
        return Math.sqrt(p.x*p.x + p.y*p.y);
    }

    public Command getCommand(Tractor tractor)
    {
        Double trax = new Double(tractor.getLocation().x);
        Double tray = new Double(tractor.getLocation().y);
        if(trax.equals(0.0) && tray.equals(0.0)){
              if(tractor.getHasBale())  return new Command(CommandType.UNLOAD);

              if(Math.abs(tlist[tractor.getId()].getLocation().x-0)<0.001 && Math.abs(tlist[tractor.getId()].getLocation().y-0)<0.001 && tlist[tractor.getId()].getNumBales()>0){
                  tlist[tractor.getId()].numBales = tlist[tractor.getId()].numBales-1;
                  return new Command(CommandType.UNSTACK);
              }


              if(outRangeSize>0){
                  List<Integer> l = map.get(tractor.getId());
                  Double Degr = degres.get(l.get(0));
                  if(Math.abs(Degr-Math.PI/4)<1e-5){
                      Double ad = l.get(1)*((Math.PI/2)/CarRDist[l.get(0)]);
                      Double layerbound = range*(l.get(0)+1);
                      Point cloest = outRangeBales.get(0);
                      for (Point c : outRangeBales) {
                          if (c.x * c.x + c.y * c.y  >= layerbound*layerbound) {
                                  cloest = c;
                                  break;
                          }
                      }
                      for (Point c : outRangeBales) {
                          if ((c.x * c.x + c.y * c.y >= layerbound*layerbound)  && (c.x * c.x + c.y * c.y<=cloest.x * cloest.x + cloest.y * cloest.y)) {
                              cloest = c;
                          }
                      }
                      /*
                      * Solve function: bale    s*(k^2+2*k*a)*pi/m^2 = 1/3*CarNum*10
                      * to get k
                      * */
                      double dis = Math.sqrt(cloest.x*cloest.x+cloest.y*cloest.y);
                      double pa_c = 4*10*CarRDist[l.get(0)]*len*len/(2.5*bales.size());
                      double pa_b  = 2*Math.PI*dis;
                      double pa_a = Math.PI;
                      double delta = Math.sqrt(pa_b*pa_b+4*pa_a*pa_c);
                      double dynamic_adjust = (-pa_b+delta)/(2*pa_a);
                      if(dis>len || (dis + dynamic_adjust)>len) {
                          if(dis<=len) {
                              ad = l.get(1)*((Math.PI/2)/CarRDist[l.get(0)]);
                              tlist[tractor.getId()].location.x = Math.cos(ad) * (dis);
                              tlist[tractor.getId()].location.y = Math.sin(ad) * (dis);

                          }else{
                              Degr = Math.acos(len / dis);
                              ad = l.get(1) * ((Math.PI / 2 - 2 * Degr) / CarRDist[l.get(0)]);
                              dis = Math.sqrt(cloest.x * cloest.x + cloest.y * cloest.y);
                              pa_c = 10 * CarRDist[l.get(0)] * len * len / (2.5 * bales.size());
                              pa_b = -Math.sqrt(2) * (len - Math.sqrt(dis * dis - len * len));
                              pa_a = 2;
                              delta = Math.sqrt(pa_b * pa_b + 4 * pa_a * pa_c);
                              dynamic_adjust = (-pa_b + delta) / (2 * pa_a);
                              if ((dis + dynamic_adjust) <= Math.sqrt(2) * len) {
                                  dis = dis + dynamic_adjust;
                                  Degr = Math.acos(len / dis);
                                  ad = l.get(1) * ((Math.PI / 2 - 2 * Degr) / CarRDist[l.get(0)]);
                                  tlist[tractor.getId()].location.x = Math.cos(Degr + ad) * (dis + dynamic_adjust);
                                  tlist[tractor.getId()].location.y = Math.sin(Degr + ad) * (dis + dynamic_adjust);
                              } else {
                                  dis = dis + 25;
                                  Degr = Math.acos(len / dis);
                                  ad = l.get(1) * ((Math.PI / 2 - 2 * Degr) / CarRDist[l.get(0)]);
                                  tlist[tractor.getId()].location.x = Math.cos(Degr + ad) * (dis);
                                  tlist[tractor.getId()].location.y = Math.sin(Degr + ad) * (dis);
                              }
                          }
                      }else{
                          tlist[tractor.getId()].location.x = Math.cos(ad) * (dis + dynamic_adjust) ;
                          tlist[tractor.getId()].location.y = Math.sin(ad) * (dis + dynamic_adjust) ;

                      }

                  }else{
                      Double layerbound = range*(l.get(0)+1);
                      Point cloest = outRangeBales.get(0);
                      for (Point c : outRangeBales) {
                          if (c.x * c.x + c.y * c.y  >= layerbound*layerbound) {
                              cloest = c;
                              break;
                          }
                      }
                      for (Point c : outRangeBales) {
                          if ((c.x * c.x + c.y * c.y >= layerbound*layerbound)  && (c.x * c.x + c.y * c.y<=cloest.x * cloest.x + cloest.y * cloest.y)) {
                              cloest = c;
                          }
                      }
                      Double ad = l.get(1)*((Math.PI/2-2*Degr)/CarRDist[l.get(0)]);
                      double dis = Math.sqrt(cloest.x*cloest.x+cloest.y*cloest.y);
                      double pa_c = 10*CarRDist[l.get(0)]*len*len/(3*bales.size());
                      double pa_b = -Math.sqrt(2)*(len-Math.sqrt(dis*dis-len*len));
                      double pa_a = 2;
                      double delta = Math.sqrt(pa_b*pa_b+4*pa_a*pa_c);
                      double dynamic_adjust = (-pa_b+delta)/(2*pa_a);
                      if ((dis + dynamic_adjust) <= Math.sqrt(2) * len) {
                          System.out.println(dis + dynamic_adjust+" "+Math.sqrt(2) * len);
                          dis = dis + dynamic_adjust;
                          Degr = Math.acos(len / dis);
                          ad = l.get(1) * ((Math.PI / 2 - 2 * Degr) / CarRDist[l.get(0)]);
                          tlist[tractor.getId()].location.x = Math.cos(Degr + ad) * (dis + dynamic_adjust);
                          tlist[tractor.getId()].location.y = Math.sin(Degr + ad) * (dis + dynamic_adjust);
                      } else {
                          dis = dis + 25;
                          Degr = Math.acos(len / dis);
                          ad = l.get(1) * ((Math.PI / 2 - 2 * Degr) / CarRDist[l.get(0)]);
                          tlist[tractor.getId()].location.x = Math.cos(Degr + ad) * (dis);
                          tlist[tractor.getId()].location.y = Math.sin(Degr + ad) * (dis);
                      }


                  }


                  if(tractor.getAttachedTrailer()!=null){
                      if(tractor.getAttachedTrailer().getNumBales()==0){
                          Point p = tlist[tractor.getId()].location;
                          outRangeSize=outRangeSize-10;
                          return Command.createMoveCommand(p);
                      }else{

                          tlist[tractor.getId()].location.x = 0;
                          tlist[tractor.getId()].location.y = 0;
                          return new Command(CommandType.DETATCH);
                      }
                  }else{

                      return new Command(CommandType.ATTACH);
                  }

              }else{
                  if(inRangeBales.size()>0){
                      if(tractor.getAttachedTrailer()!=null){

                          return new Command(CommandType.DETATCH);

                      }else{
                          Point p = inRangeBales.get(0);
                          inRangeBales.remove(inRangeBales.get(0));
                          return Command.createMoveCommand(p);
                      }

                  }

                  if(tractor.getAttachedTrailer()!=null){
                      if(tractor.getAttachedTrailer().getNumBales()==0){
                          Point p = tlist[tractor.getId()].location;
                          return Command.createMoveCommand(p);
                      }else{

                          tlist[tractor.getId()].location.x = 0;
                          tlist[tractor.getId()].location.y = 0;
                          return new Command(CommandType.DETATCH);
                      }
                  }else{
                      if(outRangeBales.size()!=0){
                          Point p = outRangeBales.get(0);
                          outRangeBales.remove(p);
                          tlist[tractor.getId()].location.x = 0 ;
                          tlist[tractor.getId()].location.y = 0 ;
                          return Command.createMoveCommand(p);
                      }
                      return new Command(CommandType.UNSTACK);

                  }
              }

        }
        if(trax.equals(tlist[tractor.getId()].location.x) && tray.equals(tlist[tractor.getId()].location.y) ) {
            if (tractor.getAttachedTrailer() != null) {
                if (tlist[tractor.getId()].getNumBales() == 10 || outRangeBales.size() == 0) {
                    Point p = new Point(0, 0);
                    return Command.createMoveCommand(p);

                }
                return new Command(CommandType.DETATCH);


            } else {
                if (!tractor.getHasBale()) {

                    if (tlist[tractor.getId()].getNumBales() == 10 || outRangeBales.size() == 0) {
                        return new Command(CommandType.ATTACH);

                    } else {
                        Point cloest = outRangeBales.get(0);
                        boolean flag = false;
                        for (Point c : outRangeBales) {
                            double x = tlist[tractor.getId()].location.x;
                            double y = tlist[tractor.getId()].location.y;
                            if (((c.x - x ) * (c.x - x) + (c.y - y) * (c.y -y))<= ((cloest.x-x) * (cloest.x-x)+(cloest.y-y)*(cloest.y-y))){
                                if(c.x*c.x+c.y*c.y>=bound[map.get(tractor.getId()).get(0)]*bound[map.get(tractor.getId()).get(0)]){
                                    cloest = c;
                                    flag = true;
                                }
                            }
                        }
                        if(!flag){
                            for (Point c : outRangeBales) {
                                double x = tlist[tractor.getId()].location.x;
                                double y = tlist[tractor.getId()].location.y;
                                if (((c.x - x ) * (c.x - x) + (c.y - y) * (c.y -y))<= ((cloest.x-x) * (cloest.x-x)+(cloest.y-y)*(cloest.y-y))){
                                        cloest = c;
                                }
                            }
                        }
                        Point p = cloest;
                        outRangeBales.remove(cloest);
                        return Command.createMoveCommand(p);
                    }


                } else {
                    if (tlist[tractor.getId()].getNumBales() < 10) {
                        tlist[tractor.getId()].numBales = tlist[tractor.getId()].numBales + 1;
                        return new Command(CommandType.STACK);
                    }

                }
            }
        }
            if (trax * trax + tray * tray < range * range) {
                if (tractor.getHasBale()) {
                    Point p = new Point(0, 0);
                    return Command.createMoveCommand(p);
                } else {

                    return new Command(CommandType.LOAD);
                }
            } else {
                if (tractor.getHasBale()) {
                    Point p = new Point(tlist[tractor.getId()].location.x, tlist[tractor.getId()].location.y);
                    return Command.createMoveCommand(p);
                } else {

                    return new Command(CommandType.LOAD);
                }
            }



    }
}

