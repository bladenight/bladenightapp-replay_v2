#!/usr/bin/env perl

use strict;
use JSON;
use Digest::MD5 qw(md5_base64);
use POSIX;
use Data::Dumper;

my $wampCommand = "http://www.greencity.de/bladenight/app/rpc/getRealtimeUpdate";
# 1343070493221

my %newIds;

sub anonymizeId {
  my $oldId = shift;
  die "oldId is not set" if ! $oldId;
  $newIds{$oldId} = "anonymized-" . (keys(%newIds)+1) if ! $newIds{$oldId};
  return $newIds{$oldId};
}

sub filterOnTime {
  my ($sec, $min, $hour, $dayOfMonth, $month, $yearOffset, $dayOfWeek, $dayOfYear, $daylightSavings) = @_;
  $month++;
  return 0 if $hour < 20;
  return 0 if $hour == 20 && $min < 50;
  return 0 if $hour == 23 && $min > 15;
  return 0 if $dayOfWeek != 1;
  # return 0 if ! ( $dayOfMonth == 10 && $month == 9 );
  return 1;
}

# 2013-06-09T21:17:49.504+02:00	WAMPIN	273dab9acfa54bd0bc464ae9683f6d4c	[2,"289d47d3-276e-403a-93c7-42c6699694c2","http://www.greencity.de/bladenight/app/rpc/getRealtimeUpdate",{"did":"ddcd2ceaf2ba20e66790","coo":{"la":48.0113,"lo":11.5923},"acc":50,"par":true}] 
# ts=2013-06-14T22:34:07.357+02:00	hp=2109.0701475331616	tp=108.85526632129692
my $separator = "";
while (my $line = <>) {
  my ($timestamp, $json) = split(/\s+/, $line,2);

  my @timearray = localtime($timestamp /1000);

  next if ! filterOnTime(@timearray);

  my $data = decode_json($json);
  next if ! $data->{longitude} || ! $data->{latitude};

  my $id = $data->{deviceId} || $data->{userName};
  if ( ! $data->{userName} ) {
    $data->{longitude} /= 1000000;
    $data->{latitude} /= 1000000;
    $data->{accuracy} /= 10;
  }
  else {
    delete $data->{userName};
  }
  $data->{deviceId} = anonymizeId($id);

  delete $data->{checksum};
  delete $data->{password};
  
  my $msec = sprintf "%03d", $timestamp % 1000;
  $data->{serverTime} = int($timestamp);
  my $timeStr = POSIX::strftime("%Y-%m-%dT%H:%M:%S.$msec", @timearray);
  
  $data->{clientTime} = 1000 * $data->{timestamp};
  delete $data->{timestamp};
  
  # print $timeStr . $separator . to_json($data, {utf8 => 1, pretty => 1, canonical=>1}) . "\n";
  
  
  print join("\t", "ts=$timeStr", "did=$data->{deviceId}", "la=$data->{latitude}", "lo=$data->{longitude}", "ac=$data->{accuracy}") . "\n";
}
