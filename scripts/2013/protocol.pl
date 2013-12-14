#!/usr/bin/env perl

use strict;
no strict 'refs';
use POSIX;
use Data::Dumper;
use JSON;
use feature 'state';
use Getopt::Long;

my %args = ();

sub text_init {
}

sub text_line {
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
  eval {
    $did = $item->{wampdata}->[3]->{did};
  };
  if ( $did ) {
    my $la = $item->{wampdata}->[3]->{coo}->{la};
    return if ! $la;
    my $lo = $item->{wampdata}->[3]->{coo}->{lo};
    my $hd = sqrt($item->{wampdata}->[3]->{acc});
    # print join("\t", $item->{ts_str}, $did, $la, $lo) . "\n";
    printf "<trkpt lat=\"%f\" lon=\"%f\"><ele>0</ele><time>%s</time><hdop>%f</hdop></trkpt>\n", $la, $lo, $item->{ts_str}, $hd;
	}	
}

sub gpx_finish {
  print '	</trkseg>
    </trk>
  </gpx>';
}

GetOptions (\%args, 'file=s', 'format=s', 'date=s', 'deviceid=s') || die "Failed to parse arguments: $!";

my $format = $args{format} || die "Please provide the output format";

&{$format."_init"}();

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
  
  next if $args{date} && $item->{ts_str} !~ /$args{date}/;
  my $did;
  eval {
    $did = $item->{wampdata}->[3]->{did};
  };
  next if $args{deviceid} && ($did ne $args{deviceid});

#   print join("\t", $item->{ts_str}, $item->{type}, $item->{session}, $item->{wampdata_str}) . "\n";
  &{$format."_line"}($item);
}

&{$format."_finish"}();
