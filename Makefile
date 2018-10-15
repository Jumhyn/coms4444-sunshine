all:
	java sunshine.sim.Simulator --time_step 1.0 -t 10000 --num_tractors 30 --player random -m 500 --gui --fps 50

compile:
	javac sunshine/sim/*.java

clean:
	rm sunshine/*/*.class

