$(function() {
HighchartsHelper.add({
	plotOptions: {
	      chart: {
	          type: 'spline',
	          renderTo: 'waiting-time-by-pos'
	      },
	      title: {
	          text: 'Wartezeit auf der Strecke'
	      },
	      xAxis: {
	          type: 'datetime',
	          labels: {
	              formatter: function() {
	                  return Highcharts.numberFormat(this.value,0) + ' km';
	              }
	          }
	      },
	      yAxis: {
	          title: {
	              text: 'Wartezeit'
	          },
              labels: {
    			format: '{value:2.0f} min'
		      },
	          min: 0,
	          max: 30,
	      },
	      tooltip: {
	    	    formatter: function() {
	    	        return this.x + 'm <br/>' + '<span style="color:'+this.series.color+'">'+ this.series.name +'</span>: ' + this.y + ' min';
	  			}
	      }
	  },
	
	sources : [
		           	{
		           		url: "waiting-time-by-pos.json",
		           		serieOptions: {
		    				name : 'Wartezeit',
		    				color : 'firebrick',
    				        marker: {
					            enabled: false
							}
		    			},
		           	},
				]
})


});
