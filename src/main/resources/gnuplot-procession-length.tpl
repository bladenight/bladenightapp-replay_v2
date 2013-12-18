set timefmt '%Y-%m-%dT%H:%M:%S'
set xdata time

set datafile sep '\t'

# set format x controls the way that gnuplot displays the dates on the x axis.
set format x "%H:%M"

set term pngcairo size 1200,900
set output "%BASE_FILENAME%.png"

set ylabel "Entfernung des Schluss von der Spitze (km)"
set yrange [0:%MAX_PROCESSION_LENGTH%]

set palette defined ( -1 "white", 0 "red", 10 "yellow", 20 "green", 25 "green", 35 "purple")
set cbrange [-1:35]
set cblabel "km / h"

plot '%DATA_FILE%' u 1:2:3 with image

