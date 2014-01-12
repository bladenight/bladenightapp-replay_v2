var speedOnRouteData =
[
 	<#list segments as segment>
	{
		nodes: [
		        <#list segment.nodes as node>
		        	new google.maps.LatLng(${node.latitude?string("00.000000")}, ${node.longitude?string("00.000000")}),
		        </#list>
		],
		speed: [
		        ${segment.speed}
		],
	};
]
