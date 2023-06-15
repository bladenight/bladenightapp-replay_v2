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
  # return 0 if ! ( $dayOfMonth == 10 && $month == 9 );
  return 1;
}

print "[\n";
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
  $data->{deviceId} = md5_base64($salt . $id);

  delete $data->{checksum};
  delete $data->{password};
  
  my $msec = sprintf "%03d", $timestamp % 1000;
  $data->{serverTime} = int($timestamp);
  $data->{serverTimeString} = POSIX::strftime("%Y-%m-%dT%H:%M:%S.$msec", @timearray);
  
  $data->{clientTime} = 1000 * $data->{timestamp};
  delete $data->{timestamp};
  print $separator . to_json($data, {utf8 => 1, pretty => 1, canonical=>1});
  $separator ||= ",\n";
}
print "]\n";
