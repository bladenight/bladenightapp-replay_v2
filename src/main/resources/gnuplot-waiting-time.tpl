set timefmt '%Y-%m-%dT%H:%M:%S'

set datafile sep '\t'

set term pngcairo size 1200,900
set output "%BASE_FILENAME%.png"

set xlabel "Position auf der Strecke (km)"

# set yrange [0:5000]
set ylabel "Wartezeit auf der Strecke (min)"

plot '%DATA_FILE%' u 1:2 with line

