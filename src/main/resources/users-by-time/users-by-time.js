$(function() {
HighchartsHelper.add({
	plotOptions: {
	      chart: {
	          type: 'spline',
	          renderTo: 'users-by-time'
	      },
	      title: {
	          text: 'Anzahl von Benutzern in der Zeit'
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
	              text: 'Anzahl Benutzer'
	          },
	          min:0
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
		           		url: "users-by-time.json",
		           		serieOptions: {
		    				name : 'Anzahl Benutzer',
		    				color : 'blue',
    				        marker: {
					            enabled: false
							}
		    			},
		    			entryConverter: function(data) { return [[HighchartsHelper.stringToTime(data[0]),data[1]]] } 
		           	},
	]
});

})