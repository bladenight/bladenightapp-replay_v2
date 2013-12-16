set timefmt '%Y-%m-%dT%H:%M:%S'
set xdata time

set datafile sep '\t'

# set format x controls the way that gnuplot displays the dates on the x axis.
set format x "%H:%M"

set term png
set output "%BASE_FILENAME%.png"

set ylabel "Position auf der Strecke (km)"

plot '%DATA_FILE%' u 1:2 w lines, \
     '%DATA_FILE%' u 1:4 w lines
