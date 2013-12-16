set timefmt '%Y-%m-%dT%H:%M:%S'
set xdata time

set datafile sep '\t'

# set format x controls the way that gnuplot displays the dates on the x axis.
set format x "%H:%M"

set term pngcairo size 1200,900
set output "%BASE_FILENAME%.png"

set ylabel "Position auf der Strecke (km)"

set palette defined ( -1 "white", 0 "red", 10 "yellow", 20 "green", 25 "green", 35 "purple")
set cbrange [-1:35]
set cblabel "km / h"

pos_only(x) = x > 0 ? x : 1/0

plot '%DATA_FILE%' u 1:2:3 with image

