set timefmt '%Y-%m-%dT%H:%M:%S'
set xdata time

set datafile sep '\t'

# set format x controls the way that gnuplot displays the dates on the x axis.
set xlabel "Uhrzeit"
set format x "%H:%M"

set ylabel "Position auf der Strecke (km)"

set term pngcairo size 1200,900
set output "%BASE_FILENAME%-speed.png"

set palette defined ( -1 "white", 0 "red", 10 "yellow", 20 "green", 25 "green", 35 "purple")
set cbrange [-1:35]
set cblabel "km / h"

pos_only(x) = x > 0 ? x : 1/0

%EVENT_INFO_LABELS%

plot '%DATA_FILE%' u 1:2:3 with image notitle


set output "%BASE_FILENAME%-density.png"

set palette defined ( 0 "white", 1 "blue", 3 "yellow", 5 "red", 10 "purple")
set cbrange [0:10]
set cblabel "Anzahl Benutzer"

%EVENT_INFO_LABELS%

plot '%DATA_FILE%' u 1:2:4 with image notitle

