#!/usr/bin/env perl

use File::Basename;
use Data::Dumper;
use strict;

my %generators = (
  "waiting-time" => \&generateWaitingTime,
  "procession-length" => \&generateProcessionLength,
);

opendir(my $dh, ".") || die "$!";
while(readdir $dh) {
  if ( ! ( $_ =~ /([^\\]*)\.log/ ) ) {
    next;
  }
  my $base = $1;
  my $match = (grep { $base =~ /$_/ } keys(%generators))[0];
  next if ! $match;
  print "$base $match\n";
  my $sub = $generators{$match};
  print "$base $sub\n";
  my $filename = "$base.gp";
  print "$filename\n";
  open(my $fh, ">", $filename) || die "Failed to open $filename: $!";
  &$sub($fh, $base);
  close $fh;
}
closedir $dh;


sub generateWaitingTime {
  my $fh = shift;
  my $base = shift;
  my $heredoc = <<END;
set timefmt '%Y-%m-%dT%H:%M:%S'
set datafile sep '\\t'
set term pngcairo size 1200,900
set output "$base.png"
set ylabel "Wartezeit auf der Strecke (min)"
plot '$base.log' u 1:2 with line
END
 print $fh $heredoc;
}

sub generateProcessionLength {
  my $fh = shift;
  my $base = shift;
  my $heredoc = <<END;
set timefmt '%Y-%m-%dT%H:%M:%S'
set xdata time

set datafile sep '\\t'

# set format x controls the way that gnuplot displays the dates on the x axis.
set format x "%H:%M"

set term pngcairo size 1200,900
set output "$base.png"

set yrange [0:5000]

set palette defined ( -1 "white", 0 "red", 10 "yellow", 20 "green", 25 "green", 35 "purple")
set cbrange [-1:35]
set cblabel "km / h"

pos_only(x) = x > 0 ? x : 1/0

plot '$base.log' u 1:2:3 with image
END
 print $fh $heredoc;
}
