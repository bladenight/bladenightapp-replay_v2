var heatMapData =
[
  <#list entries as entry>
    new google.maps.LatLng(${entry.latitude?string("00.000000")}, ${entry.longitude?string("00.000000")}),
  </#list>
];
