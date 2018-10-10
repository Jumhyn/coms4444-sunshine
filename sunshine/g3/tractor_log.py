#!/usr/bin/env python3


import os
import sys


LOG = 'log.txt'
TRACTOR_LOG = 'tractor_log.txt'

tractor_log = {}
with open(LOG, 'r') as infile:
    for line in infile:
        if line != '\n':
            split = line.split()
            if split[0] == 'COMMAND:':
                Id = int(split[2])

                if Id not in tractor_log:
                    tractor_log[Id] = []

                command = split[3]
                if command == 'MOVE_TO':
                    action = split[3] + ' ' + split[4]
                else:
                    action = split[3]

                tractor_log[Id].append(action)

if os.path.exists(TRACTOR_LOG):
    os.remove(TRACTOR_LOG)

with open(TRACTOR_LOG, 'w+') as outfile:
    for i in range(len(tractor_log)):
        outfile.write('TRACTOR ' + str(i) + ':\n')
        outfile.write('\t')
        outfile.write('\n\t'.join(tractor_log[i]))
        outfile.write('\n\n')
