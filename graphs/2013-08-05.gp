set timefmt '%Y-%m-%dT%H:%M:%S'
set xdata time

set datafile sep '\t'

# set format x controls the way that gnuplot displays the dates on the x axis.
set format x "%H:%M"

set term png
set output "2013-08-05.png"

plot '2013-08-05.log' u 2:3 w lines, \
     '2013-08-05.log' u 2:4 w lines

