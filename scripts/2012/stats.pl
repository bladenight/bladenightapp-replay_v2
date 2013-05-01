#!/usr/bin/env perl

use strict;
use JSON;
use Digest::MD5 qw(md5_base64);
use POSIX;
use Data::Dumper;

my $salt = rand(10000000000);

# 1343070493221

sub filterOnTime {
  my ($sec, $min, $hour, $dayOfMonth, $month, $yearOffset, $dayOfWeek, $dayOfYear, $daylightSavings) = @_;
  $month++;
  return 0 if $hour < 20;
  return 0 if $hour == 20 && $min < 50;
  return 0 if $hour == 23 && $min > 15;
  return 0 if $dayOfWeek != 1;
  return 1;
}

sub avg {
  my $a = shift;
  my $sum = 0;
  my $i;
  return 0 if scalar(@$a) <= 0;
  foreach $i (@$a) {
    $sum += $i ; 
  }
  return $sum * 1.0 / scalar(@$a);
}

sub max {
  my $array = shift;
  return (sort { $b <=> $a } @$array)[0];
}

sub min {
  my $array = shift;
  return (sort { $a <=> $b } @$array)[0];
}

sub diffArray {
  my $array = shift;
  my $result = [ ];
  foreach my $i (0..scalar(@$array)-2) {
    my $diff = ( $array->[$i+1] - $array->[$i] );
    die "here" if ! $diff;
    push @$result, $diff;
  }
  return $result;
}

my %stats;

my $separator = "";
while (my $line = <>) {
  my ($timestamp, $json) = split(/\s+/, $line,2);

  my @timearray = localtime($timestamp /1000);

  next if ! filterOnTime(@timearray);

  my $data = decode_json($json);
  next if ! $data->{longitude} || ! $data->{latitude};

  my $id = $data->{deviceId} || $data->{userName};
  die Dumper($data) if ! $id;
  if ( ! $data->{userName} ) {
    $data->{longitude} /= 1000000;
    $data->{latitude} /= 1000000;
    $data->{accuracy} /= 10;
  }
  else {
    delete $data->{userName};
  }
  $data->{deviceId} = md5_base64($salt . $id);

  delete $data->{checksum};
  delete $data->{password};
  
  my $msec = sprintf "%03d", $timestamp % 1000;
  $data->{serverTime} = int($timestamp);
  $data->{serverTimeString} = POSIX::strftime("%Y-%m-%dT%H:%M:%S.$msec", @timearray);
  
  $data->{clientTime} = 1000 * $data->{timestamp};
  delete $data->{timestamp};
  
  $stats{$id} ||= { };
  my $stat = $stats{$id};
  $stat->{count}++;
  $stat->{lastTimestamp}++;
  push @{$stat->{serverTimes}}, $data->{serverTime};
}

foreach my $id (sort(keys(%stats))) {
  my $s = $stats{$id};
  print $id . "\n";
  printf "  %-20s : %5d\n", "Count", $s->{count};
  my $diffArray = diffArray($s->{serverTimes});
  printf "  %-20s : %5d\n", "Avg diff", int(avg($diffArray)/1000);
  printf "  %-20s : %5d\n", "Min diff", int(min($diffArray)/1000);
  printf "  %-20s : %5d\n", "Max diff", int(max($diffArray)/1000);
  # printf "  %-20s : %s\n",  "Times", join(" ", @{$s->{serverTimes}});
}