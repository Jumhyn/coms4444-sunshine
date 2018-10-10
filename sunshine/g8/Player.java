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
		for (int i=0;i<n;i++) {
			trailer_map.put(i,new Point(0.0,0.0));
			trailer_num.put(i,0);
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


	public Command getCommand(Tractor tractor)
	{
		// if empty, have tractor return to base now!
		// Extra Consideration. If last tractor has a trailer attached...
		// We need to see if it is empty
		// if empty, we must compute if it is faster to detach and run to barn or just go now
		//if(trailer_bales.isEmpty() && tractor_bales.isEmpty())
		//{
		//	return Command.createMoveCommand(BARN);
		//}
		
		// Andrew will be testing some stuff with just 1 tractor
		if(n_tractors == 1)
		{
			return one_trailer_greedy(tractor);


			// Option 2: Use Trailer for everything
			// If at origin
			/*
			if (tractor.getLocation().equals(new Point(0.0, 0.0)))
			{
				if(tractor.getAttachedTrailer() != null)
				{
					// It is time to empty everything
					if(tractor.getHasBale())
					{
						haystack = tractor.getAttachedTrailer().getNumBales();
						return new Command(CommandType.DETATCH);
					}
					// Everything is empty, Go now!
					else
					{
						Point p = tractor_bales.remove(tractor_bales.size()-1);
						return Command.createMoveCommand(p);
					}
				}
				// It is detached! Empty EVERYTHING!
				else
				{
					if(tractor.getHasBale())
					{
						return new Command(CommandType.UNLOAD);
					}
					// BEWARE OF EMPTY TRAILER!
					else
					{
						if(haystack > 0)
						{
							--haystack;
							return new Command(CommandType.UNSTACK);
						}
						else
						{
							return new Command(CommandType.ATTACH);
						}
					}
				}

				// If not empty, GO TO LOCATION
			}
			// Farming for Bale!
			else
			{
				// 2- unattach trailer
				// 3- Stack 10 times, 1 more for hay
				// 4- Attach
				// 5- go to barn
				// BEWARE OF LIST OF POINTS FOR TRAILER BEING EMPTY!

				if(tractor.getAttachedTrailer() != null)
				{
					// Just in case the bails for trailer are done! Go Back NOW!

					//if(trailer_bales == null || trailer_bales.isEmpty())
					//{
					//	return Command.createMoveCommand(new Point(0.0, 0.0));
					//}


					// Do you have 11th bale? If so, time to go?
					if(tractor.getHasBale())
					{
						return Command.createMoveCommand(new Point(0.0, 0.0));
					}
					// Everything is empty, Go now!
					else
					{
						this.haystack = tractor.getAttachedTrailer().getNumBales();
						return new Command(CommandType.DETATCH);
					}
				}
				else
				{
					// It is detached, 
					// In this case, the trailer is full! Move out!
					if(haystack == 10)
					{
						return new Command(CommandType.ATTACH);
					}
					// Keep farming!
					else
					{
						if(tractor.getHasBale())
						{
							++haystack;
							return new Command(CommandType.STACK);
						}
						else
						{
							// If you are at Trailer...Go get Bale!

							// If you are NOT at Trailer...LOAD IT!
							Point p = tractor_bales.remove(tractor_bales.size()-1);
							return Command.createMoveCommand(p);
						}
					}
				}
			}
			 */

			// Option 3: Use Trailer ONLY ON QUADRAN 1 (Closest to Barn)

			// Option 4: Use the Trailer ONLY ON QUADTRANT 5 (Furthest from Barn)
		}
		// Leaving room open for different tractor strategy
		else if(n_tractors > 1)
		{
			if (tractor.getLocation().equals(BARN)) 
			{
				if (tractor.getAttachedTrailer() != null) //trailer
				{
					if(tractor.getAttachedTrailer().getNumBales() == 0) {
						if (tractor.getHasBale()) {
							return new Command(CommandType.UNLOAD);
						}
						else {
							Point p = tractor_bales.get(tractor_bales.size()-1);
							return Command.createMoveCommand(p);
						}
					} else { //has bales
						return new Command(CommandType.DETATCH);
					}
				}
				else //no trailer
				{
					if(trailer_num.get(tractor.getId()) != 0) { //detached trailer has bales
						if (tractor.getHasBale()) {
							return new Command(CommandType.UNLOAD);
						} else {
							trailer_num.put(tractor.getId(),trailer_num.get(tractor.getId())-1);
							return new Command(CommandType.UNSTACK);
						}
					} else {
						if (tractor.getHasBale()) {
							return new Command(CommandType.UNLOAD);
						}
						else {
							return new Command(CommandType.ATTACH);
						}
					}
				}
			}
			// There is no bale!
			else // not at barn
			{
				if (tractor.getAttachedTrailer() != null) //trailer attached, only attach with bale in forklift
				{
					if (tractor.getAttachedTrailer().getNumBales() == 0) { //nothing in trailer
						trailer_map.put(tractor.getId(),tractor.getLocation());
						return new Command(CommandType.DETATCH);
					}
					else { //something in trailer, ready to move (should be 10)
						Point p = new Point(0.0,0.0);
						trailer_map.put(tractor.getId(),p);
						return Command.createMoveCommand(BARN);
					}
				} 
				else // no trailer attached
				{
					Point trail_loc = trailer_map.get(tractor.getId());
					if (trailer_num.get(tractor.getId()) < 10) { //can still stack more
						if (tractor.getHasBale()) {
							if (tractor.getLocation().equals(trail_loc)) {
								trailer_num.put(tractor.getId(),trailer_num.get(tractor.getId())+1);
								return new Command(CommandType.STACK);
							}
							else { //move to trailer
								return Command.createMoveCommand(trail_loc);
							}
						} else { // no bale, need to go to next bale to 
							Point p = tractor_bales.get(tractor_bales.size()-1);
							if (tractor.getLocation().equals(p)) {
								tractor_bales.remove(tractor_bales.size()-1);
								return new Command(CommandType.LOAD);
							}
							else {
								return Command.createMoveCommand(p);
							}
						}
					} else { //trailer is full
						if (tractor.getHasBale()) {
							if (tractor.getLocation().equals(trail_loc)) {
								return new Command(CommandType.ATTACH);
							}
							else {
								return Command.createMoveCommand(trail_loc);
							}
						} else {
							Point p = tractor_bales.get(tractor_bales.size()-1);
							if (tractor.getLocation().equals(p)) {
								tractor_bales.remove(tractor_bales.size()-1);
								return new Command(CommandType.LOAD);
							}
							else {
								return Command.createMoveCommand(p);
							}
						}
					}
				}
			}
		}
		else
		{
			// INVALID, Just random code...
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
				if (tractor.getLocation().equals(BARN)) 
				{
					if (tractor.getAttachedTrailer() == null) 
					{
						Point p = tractor_bales.remove(tractor_bales.size()-1);
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
}
