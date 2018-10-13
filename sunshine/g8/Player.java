package sunshine.g8;

import java.lang.Math;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.Comparator;

import sunshine.sim.Command;
import sunshine.sim.Tractor;
import sunshine.sim.CommandType;
import sunshine.sim.Point;
import sunshine.sim.Trailer;


public class Player implements sunshine.sim.Player 
{
	// Random seed of 42.
	private int seed = 42;
	private Random rand;
	private final static Point BARN= new Point(0.0, 0.0);

	// List of Bales, one batch for trailers and the other for tractor ONLY
	private List<Point> tractor_bales;
	private List<Point> trailer_bales;

	// Make list of centroids, Deposit points for the trailer
	private List<Point> centroids = new ArrayList<Point>();

	// Map the TractorID (matched with trailer it is matched to...to Location of Trailer
	private HashMap<Integer, Point> trailer_map = new HashMap<Integer, Point>(); 
	private HashMap<Integer, Integer> trailer_num = new HashMap<Integer, Integer>();

	private HashMap<Integer, List<Point>> taskList = new HashMap<Integer, List<Point>>();

	private boolean is_not_removed = true;


	// Get number of tractors, change stategy as needed
	private int n_tractors = 0;
	private double dimensions = 0;
	private int haystack = 0;

	public Player() 
	{
		rand = new Random(seed);
	}

	private double distance(Point p1, Point p2) 
	{
		return Math.sqrt(Math.pow(p1.x-p2.x,2)+Math.pow(p1.y-p2.y,2));
	}

	// Purpose: Sort location of bales. Closest to furthest bale
	public Comparator<Point> pointComparator = new Comparator<Point>() 
	{
		public int compare(Point p1, Point p2) 
		{
			double d1 = distance(BARN, p1);
			double d2 = distance(BARN, p2);
			if (d1 < d2) 
			{
				return -1;
			} 
			else if (d1 > d2) 
			{
				return 1;
			} 
			else 
			{
				return 0;
			}
		}
	};

	// Will break a list into a list<list>, maxmimize all lists up to n.
	// The last list may be size n or less
	public ArrayList<List<Point>> split_list_n(List<Point> full, int n)
	{
		ArrayList<List<Point>> split = new ArrayList<List<Point>>();
		for (int i = 0; i < full.size();i+=n)
		{
			List<Point> chunk = new ArrayList<Point>();
			for (int j = i; j < n; j++)
			{
				// Edge case: last batch has less than 11 bales
				if(j >= full.size())
				{
					return split;
				}
				chunk.add(full.get(j));
			}
			split.add(chunk);
		}
		return split;
	}

	// Split the Tractor and Trailer bale depending on the index.
	public void split_trailer_tractor_batch(List<Point> bales, double split)
	{
		if(split == 1)
		{
			this.tractor_bales = bales;
			this.trailer_bales = new ArrayList<Point>();
			return;
		}
		else if(split == 0)
		{
			this.tractor_bales = new ArrayList<Point>();
			this.trailer_bales = bales;
			return;
		}
		// split determines the fraction going to tractor.
		// e.g 1/2 means 1/2 tractor 1/2 trailer
		// e.g 1/3 means 1/3 tractor and 2/3 trailer
		int split_idx = (int) (bales.size() * split);
		this.trailer_bales = bales.subList(0, split_idx);
		this.tractor_bales = bales.subList(split_idx, bales.size());
	}

	public void init(List<Point> bales, int n, double m, double t)
	{
		// Organize how bales will be selected by tractors
		// Min to max distance
		Collections.sort(bales, pointComparator);

		// 1- 1/2 closest to tractor
		// 2- 1/2 closest to trailer
		// For now, all of them will go to trailer...
		split_trailer_tractor_batch(bales, 1);
		
		for (int i=0;i<n;i++) 
		{
			trailer_map.put(i,new Point(0.0,0.0));
			trailer_num.put(i,0);

			taskList.put(i, new ArrayList<Point>());
		}
		this.n_tractors = n;
		this.dimensions = m;
	}

	// OPTON 1: USE NO TRAILER, for use with 1 Tractor
	public Command one_trailer_greedy(Tractor tractor)
	{		
		if(is_not_removed)
		{
			is_not_removed = false;
			return new Command(CommandType.DETATCH);
		}

		// Option 1: Abandon Trailer, grab everything!
		if (tractor.getHasBale()) 
		{
			// Unload the bale at the barn
			if (tractor.getLocation().equals(BARN)) 
			{
				return new Command(CommandType.UNLOAD);
			}
			// Move back to the barn!
			else 
			{
				return Command.createMoveCommand(BARN);
			}
		}
		// There is no bale!
		else 
		{
			if(tractor.getLocation().equals(new Point(0.0, 0.0)))
			{
				Point p = tractor_bales.remove(tractor_bales.size()-1);
				return Command.createMoveCommand(p);
			}
			else
			{
				return new Command(CommandType.LOAD);
			}
		}
	}
	private int getMaxIdx(List<Double> ptlist) 
	{
		double max = 0;
		int maxindex = 0;
		for (int i=0;i<ptlist.size();i++) 
		{
			if (ptlist.get(i)>max) 
			{
				max = ptlist.get(i);
				maxindex = i;
			}
		}
		return maxindex;

	}

	private List<Point> closestTen(Point x, List<Point> currBales) 
	{
		List<Point> pos = new ArrayList<Point>();
		List<Double> dist = new ArrayList<Double>();

		int k=0;
		double max = 0;

		for (Point p: currBales) 
		{
			if(k < 10) 
			{
				double temp = distance(x,p);
				pos.add(p);
				dist.add(temp);
				k++;
			}
			else 
			{
				double temp = distance(x,p);
				int i = getMaxIdx(dist);
				max = dist.get(i);
				if (max > temp) 
				{
					dist.remove(i);
					pos.remove(i);
					dist.add(temp);
					pos.add(p);
				}
			}
		}
		return pos;
	}

	//do math to get point within 1 meter
	private Point optimalPoint(Point p)
	{
		Point res = new Point(0,0); 
		double mag = Math.sqrt((p.x * p.x) + (p.y * p.y));
		res.x = p.x - (.90 *(p.x/mag)); 
		res.y = p.y - (.90 *(p.y/mag)); 
		return res; 
	}

	public Command getCommand(Tractor tractor)
	{
		/*for(int i=0;i<n_tractors;i++) {
			System.out.println(i+" "+done.get(i));
		}*/
		//System.out.println(tractor_bales.size());
		if (taskList.get(tractor.getId()).size() == 0 && tractor.getLocation().equals(BARN))
		{ 
			//empty list right now
			//pick farthest point
			if (tractor_bales.size() == 0)
			{
				System.out.println("done");
			}
			else if (tractor_bales.size() <= 11) 
			{
				List<Point> tasks = new ArrayList<Point>();
				for(int i=0;i<tractor_bales.size();i++)
				{
					tasks.add(tractor_bales.remove(i));
				}
				Collections.sort(tasks, pointComparator);
				taskList.put(tractor.getId(),tasks);
			}
			else 
			{
				Point p = tractor_bales.remove(tractor_bales.size()-1);
				List<Point> tasks = closestTen(p,tractor_bales);
				for (int i=0;i<tasks.size();i++) 
				{
					tractor_bales.remove(tractor_bales.indexOf(tasks.get(i)));
				}
				tasks.add(p);
				//sort tasks by distance to BARN,
				Collections.sort(tasks, pointComparator);
				taskList.put(tractor.getId(),tasks);
			}
		}

		//when tractor is in barn
		if (tractor.getLocation().equals(BARN)) 
		{
			//if at barn and has bale, always unload
			if (tractor.getHasBale()) 
			{
				return new Command(CommandType.UNLOAD);
			}
			else if (tractor.getAttachedTrailer() != null) //trailer
			{
				//if trailer has nothing
				if(tractor.getAttachedTrailer().getNumBales() == 0) 
				{
					/*if (tractor.getHasBale()) {
						return new Command(CommandType.UNLOAD);
					}*/
					//either move if 
					if ((taskList.get(tractor.getId()).size()) > 0) 
					{ 
						//tractor has tasks
						Point p = taskList.get(tractor.getId()).get(0);
						//TODO
						//do a function here, that optimizes p 
						//closest to barn and one m away from bale

						//Point o = optimalPoint(p); 
						return Command.createMoveCommand(p);
					}
					else 
					{ 
						// tractor has no tasks, just stay at barn, no op
						return new Command(CommandType.UNSTACK);
					}
				} 
				else 
				{ 
					//trailer is attached, trailer has bales
					return new Command(CommandType.DETATCH);
				}
			}
			else //no trailer
			{
				if(trailer_num.get(tractor.getId()) != 0) 
				{ 
					//detached trailer has bales
					if (tractor.getHasBale()) 
					{ 
						//probably won't
						return new Command(CommandType.UNLOAD);
					} 
					else 
					{
						trailer_num.put(tractor.getId(),trailer_num.get(tractor.getId())-1); //update hashmap
						return new Command(CommandType.UNSTACK);
					}
				} 
				else 
				{ 
					//detached trailer has 0 bales
					if (tractor.getHasBale()) 
					{
						return new Command(CommandType.UNLOAD);//no op
					}
					else 
					{
						return new Command(CommandType.ATTACH);
					}
				}
			}
		}
		// There is no bale!
		// not at barn
		else
		{
			if (tractor.getAttachedTrailer() != null) //trailer attached, only attach with bale in forklift
			{
				//nothing in trailer have things to do 
				if (tractor.getAttachedTrailer().getNumBales() == 0 && taskList.get(tractor.getId()).size() != 0) 
				{
					//nothing in trailer
					trailer_map.put(tractor.getId(),tractor.getLocation());
					return new Command(CommandType.DETATCH);
				}
				else 
				{
					// Something in trailer, ready to move (should be 10)  //finished all tasks, ready to move to barn
					Point p = new Point(0.0,0.0);
					trailer_map.put(tractor.getId(), p);
					return Command.createMoveCommand(BARN);
				}
			}
			// no trailer attached
			else 
			{
				Point trail_loc = trailer_map.get(tractor.getId()); //location of the trailer
				if (taskList.get(tractor.getId()).size() > 0) 
				{ 
					//something to do in the tasklist
					//if trailer has less than 10 bales on it 
					if (trailer_num.get(tractor.getId()) < 10) 
					{
						//if forklift has bale
						if (tractor.getHasBale()) 
						{
							//if tractor is by the trailer 
							//TODO: or is in range 
							if (tractor.getLocation().equals(trail_loc)) 
							{
								//updating trailer bales hashmap
								trailer_num.put(tractor.getId(),trailer_num.get(tractor.getId())+1);
								return new Command(CommandType.STACK);
							}
							else 
							{ 
								//move to trailer
								return Command.createMoveCommand(trail_loc);
							}
						} 
						//forklift doesn't have bale, go to next in task list
						else 
						{ 
							// no bale, need to go to next bale to 
							Point p = taskList.get(tractor.getId()).get(0);
							//Point p = tractor_bales.get(tractor_bales.size()-1);
							//if you happen to already be there, load 
							// TODO
							//OR IF LOCATION IS WITHIN ONE METER
							if (tractor.getLocation().equals(p)) 
							{
								taskList.get(tractor.getId()).remove(0);
								//tractor_bales.remove(tractor_bales.size()-1);
								return new Command(CommandType.LOAD);
							}
							else 
							{
								return Command.createMoveCommand(p);
							}
						}
					} 
					else 
					{ 
						//trailer is full, tractor must load
						Point p = taskList.get(tractor.getId()).get(0);
						if (tractor.getLocation().equals(p)) 
						{
							taskList.get(tractor.getId()).remove(0);
							//tractor_bales.remove(tractor_bales.size()-1);
							return new Command(CommandType.LOAD);
						}
						else 
						{
							return Command.createMoveCommand(p);
						}
					}
				} 
				else 
				{ 
					//tasklist done
					//move back to where trailer is 
					if (!tractor.getLocation().equals(trail_loc)) 
					{
						//Point o = optimalPoint(trail_loc); 
						return Command.createMoveCommand(trail_loc);
					}
					//at trailer
					else 
					{
						return new Command(CommandType.ATTACH);
					}
				}
			}
		}
	}
}
