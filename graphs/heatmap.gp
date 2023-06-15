set timefmt '%Y-%m-%dT%H:%M:%S'
set xdata time

set datafile sep '\t'

# set format x controls the way that gnuplot displays the dates on the x axis.
set format x "%H:%M"

set term pngcairo size 1200,900
set output "heatmap.png"

set yrange [0:5000]

set palette defined ( -1 "white", 0 "red", 10 "yellow", 20 "green", 25 "green", 35 "purple")
set cbrange [-1:35]
set cblabel "km / h"

pos_only(x) = x > 0 ? x : 1/0

plot 'heatmap.log' u 2:3:4 with image

