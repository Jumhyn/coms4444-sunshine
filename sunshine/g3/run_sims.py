#!/usr/bin/python
import sys
import os
import re

maxField = 2000
maxTractors = 200

#maxField = 250
#maxTractors = 20


def varyParams():
	#run a grid search for different parameters of m and t

	for num_tractors in range(1,maxTractors,5):
		for field_size in range(50,maxField,50):
			run(num_tractors,field_size)



def run(t,m):

	os.system("./test1 "+str(t)+ " "+str(m) +" >> test.log 2> stderr")
	
	with open('test.log',"r") as infile:
		lines = infile.readlines()
		print(lines)
		if len(lines) > 0:
			result = lines[-1]


			time = re.findall(r'\b\d+\.\d+\b',result)

		else:
			time = ["NULL"]
			return
	with open("data.log", 'a+') as outfile:
		outfile.write(str(t))
		outfile.write("\t")
		outfile.write(str(m))
		outfile.write("\t")
		outfile.write(str(time[0]))
		outfile.write("\n")


if __name__ == "__main__":
	varyParams()
