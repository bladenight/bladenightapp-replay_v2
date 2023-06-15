set timefmt '%Y-%m-%dT%H:%M:%S'
set xdata time

set datafile sep '\t'

# set format x controls the way that gnuplot displays the dates on the x axis.
set format x "%H:%M"

set term pngcairo size 1200,900
set output "2013-07-22.png"

plot '2013-07-22.log' u 2:3 w lines, \
     '2013-07-22.log' u 2:4 w lines

