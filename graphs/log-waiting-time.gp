set timefmt '%Y-%m-%dT%H:%M:%S'

set datafile sep '\t'

set term pngcairo size 1200,900
set output "log-waiting-time.png"

# set yrange [0:5000]
set ylabel "Wartezeit auf der Strecke (min)"

plot 'log-waiting-time.log' u 1:2 with line

