$(function() {
HighchartsHelper.add({
	plotOptions: {
	      chart: {
	          type: 'spline',
	          renderTo: 'head-and-tail-by-time'
	      },
	      title: {
	          text: 'Entwicklung der Spitze und des Schlusses mit der Zeit'
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
	              text: 'Position'
	          },
              labels: {
    			format: '{value:0.3f} km'
		      },
		      min:0,
	      },
	      tooltip: {
	    	    formatter: function() {
	    	    	var dateStr = Highcharts.dateFormat('%H:%M', new Date(this.x));
	    	        return dateStr + '<br/>' + '<span style="color:'+this.series.color+'">'+ this.series.name +'</span>: ' + Highcharts.numberFormat(this.y,3) + ' km';
	  			}
	      }
	  },
	
	sources : [
		           	{
		           		url: "head-pos-by-time.json",
		           		serieOptions: {
		    				name : 'Zugspitze',
		    				color : 'green',
    				        marker: {
					            enabled: false
							}
		    			},
		    			entryConverter: function(data) { return [[HighchartsHelper.stringToTime(data[0]),data[1] / 1000.0]] } 
		           	},
		           	{
		           		url: "tail-pos-by-time.json",
		           		serieOptions: {
		    				name : 'Zugschluss',
		    				color : 'red',
    				        marker: {
					            enabled: false
							}
		    			},
		    			entryConverter: function(data) { return [[HighchartsHelper.stringToTime(data[0]),data[1]  / 1000.0]] } 
		           	}
				]

})


});
