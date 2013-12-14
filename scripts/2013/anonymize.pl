#!/usr/bin/env perl

use strict;
use POSIX qw(strftime);

my $countAnonymized = 0;
my $countLines = 0;
my %newIds;

sub anonymizeId {
  my $oldId = shift;
  die "oldId is not set" if ! $oldId;
  $newIds{$oldId} = "anonymized-" . (keys(%newIds)+1) if ! $newIds{$oldId};
  return $newIds{$oldId};
}

sub epoch2Iso {
  my $s = shift;
  my $tz = strftime("%z", localtime($s));
  $tz =~ s/(\d{2})(\d{2})/$1:$2/;
  return strftime("%Y-%m-%dT%H:%M:%S", localtime($s)) . $tz;
}
  
while (my $line = <> ) {
  # 2013-06-09T21:17:49.504+02:00	WAMPIN	273dab9acfa54bd0bc464ae9683f6d4c	[2,"289d47d3-276e-403a-93c7-42c6699694c2","http://www.greencity.de/bladenight/app/rpc/getRealtimeUpdate",{"did":"ddcd2ceaf2ba20e66790","coo":{"la":48.0113,"lo":11.5923},"acc":50,"par":true}] 
  my @fields = split(/\t/, $line, 4);
  my $wampMessage = $fields[3];
  next if $wampMessage !~ /getRealtimeUpdate/;
  $countLines++;

  my $timeStr = $fields[0];

  if ( $timeStr =~ /^\d+$/ ) {
    $timeStr = epoch2Iso($timeStr / 1000);
  }
  
  my $did;
  if ( $wampMessage =~ /"did":"([^"]+)"/ ) {
    $did = $1;
  }

  my $latitude;
  if ( $wampMessage =~ /"la":([-\d.]+)/ ) {
    $latitude = $1;
  }

  my $longitude;
  if ( $wampMessage =~ /"lo":([-\d.]+)/ ) {
    $longitude = $1;
  }

  my $accuracy;
  if ( $wampMessage =~ /"acc":([-\d.]+)/ ) {
    $accuracy = $1;
  }

  die "Failed to find the expected info in $line" if ! defined($did) || ! defined($latitude) || ! defined($longitude) || ! defined($accuracy);
  next if $latitude <= 0.0;
  next if $longitude <= 0.0;

  $countAnonymized ++;
  $did = anonymizeId($did);

  print join("\t", "ts=$timeStr", "did=$did", "la=$latitude", "lo=$longitude", "ac=$accuracy") . "\n";
}

print STDERR "Input: $countLines lines   Output: $countAnonymized lines   Found " . keys(%newIds) . " different device ids\n";
