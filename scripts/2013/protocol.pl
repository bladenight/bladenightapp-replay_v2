#!/usr/bin/env perl

use strict;
no strict 'refs';
use POSIX;
use Data::Dumper;
use JSON;
use feature 'state';

sub text_init {
}

sub text {
  my ($item) = @_;
  my $did;
  eval {
    $did = $item->{wampdata}->[3]->{did};
  };
  if ( $did ) {
    my $la = $item->{wampdata}->[3]->{coo}->{la};
    my $lo = $item->{wampdata}->[3]->{coo}->{lo};
    print join("\t", $item->{ts_str}, $did, $la, $lo) . "\n";
  }
}

sub text_finish {
}

sub gpx_init {
    print '<?xml version="1.0" encoding="UTF-8"?>
<gpx version="1.0">
	<name>Example gpx</name>
	<trk><name>Example gpx</name><number>1</number>
	<trkseg>
';
}

sub gpx_line {
  my ($item) = @_;
	my $did;
  return if $item->{ts_str} !~ /2013-04-30/;
  eval {
    $did = $item->{wampdata}->[3]->{did};
  };
  if ( $did ) {
    my $la = $item->{wampdata}->[3]->{coo}->{la};
    my $lo = $item->{wampdata}->[3]->{coo}->{lo};
    return if ! $la;
    # print join("\t", $item->{ts_str}, $did, $la, $lo) . "\n";
    printf "<trkpt lat=\"%f\" lon=\"%f\"><ele>0</ele><time>%s</time></trkpt>\n", $la, $lo, $item->{ts_str};
	}	
}

sub gpx_finish {
  print '	</trkseg>
    </trk>
  </gpx>';
}

my $output = "gpx";

&{$output."_init"}();

while(my $line = <>) {
  chomp $line;
  my @fields = split(/\t/, $line);
  
  my $item = {
    ts_ms        => $fields[0],
    ts_str       => POSIX::strftime("%Y-%m-%dT%H:%M:%SZ", localtime($fields[0]/1000)),
    type         => $fields[1],
    session      => $fields[2],
    wampdata_str => $fields[3],
    wampdata     => decode_json($fields[3])
  };
  
#   print join("\t", $item->{ts_str}, $item->{type}, $item->{session}, $item->{wampdata_str}) . "\n";
  gpx_line($item);
}

&{$output."_finish"}();
