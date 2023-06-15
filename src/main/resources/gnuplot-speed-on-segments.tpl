set timefmt '%Y-%m-%dT%H:%M:%S'

set datafile sep '\t'

set term pngcairo size 1200,900
set output "%PNG_FILE%"

set xlabel "Position auf der Strecke (km)"

set ylabel "Geschwindigkeit (km/h)"
set yrange [0:30]

%EVENT_INFO_LABELS%

plot '%DATA_FILE%' u 1:2 with line notitle

