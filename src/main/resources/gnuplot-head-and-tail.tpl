set timefmt '%Y-%m-%dT%H:%M:%S'
set xdata time

set datafile sep '\t'

# set format x controls the way that gnuplot displays the dates on the x axis.
set xlabel "Uhrzeit"
set format x "%H:%M"

set ylabel "Position auf der Strecke (km)"

set term pngcairo size 1200,900
set output "%PNG_FILE%"

%EVENT_INFO_LABELS%

plot '%DATA_FILE%' using 1:2 with lines title "Zugspitze", \
     '%DATA_FILE%' using 1:4 with lines title "Zugschluss"

