$(function() {
HighchartsHelper.add({
	plotOptions: {
	      chart: {
	          type: 'spline',
	          renderTo: 'speed-by-pos'
	      },
	      title: {
	          text: 'Durschnitliche Geschwindigkeit auf der Strecke'
	      },
	      xAxis: {
	          type: 'datetime',
	          labels: {
	              formatter: function() {
	                  return Highcharts.numberFormat(this.y,0) + ' km';
	              }
	          }
	      },
	      yAxis: {
	          title: {
	              text: 'Geschwindigkeit'
	          },
              labels: {
    			format: '{value:2.0f} km/h'
		      },
	          min: 0,
	          max: 30,
	      },
	      tooltip: {
	    	    formatter: function() {
	    	        return this.x + '<br/>' + '<span style="color:'+this.series.color+'">'+ this.series.name +'</span>: ' + Highcharts.numberFormat(this.value,0) + ' km/h';
	  			}
	      }
	  },
	
	sources : [
		           	{
		           		url: "speed-by-pos.json",
		           		serieOptions: {
		    				name : 'Geschwindigkeit',
		    				color : 'green',
    				        marker: {
					            enabled: false
							}
		    			},
		    			entryConverter: function(data) { return [[data[0] / 1000.0,data[1]]] } 
		           	},
				]
})


});
