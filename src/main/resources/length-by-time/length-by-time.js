$(function() {
HighchartsHelper.add({
	plotOptions: {
	      chart: {
	          type: 'spline',
	          renderTo: 'length-by-time'
	      },
	      title: {
	          text: 'Zuglänge in der Zeit'
	      },
	      xAxis: {
	          type: 'datetime',
	          labels: {
	              formatter: function() {
	                  return Highcharts.dateFormat('%H:%M', this.value);
	              }
	          }
	      },
	      yAxis: {
	          title: {
	              text: 'Zuglänge (km)'
	          },
	          min:0,
	          max:6000,
	      },
	      tooltip: {
	    	    formatter: function() {
	    	    	var dateStr = Highcharts.dateFormat('%H:%M', new Date(this.x));
	    	        return dateStr + '<br/>' + '<span style="color:'+this.series.color+'">'+ this.series.name +'</span>: ' + this.y;
	  			}
	      }
	  },
	
	sources : [
		           	{
		           		url: "length-by-time.json",
		           		serieOptions: {
		    				name : 'Zuglänge',
		    				color : 'green',
    				        marker: {
					            enabled: false
							}
		    			},
		    			entryConverter: function(data) { return [[HighchartsHelper.stringToTime(data[0]),data[1]]] } 
		           	},
	]
});

})