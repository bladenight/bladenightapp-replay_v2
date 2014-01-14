
HighchartsHelper = {
	init : function () {
	    Highcharts.setOptions({
	        lang: {
	                loading: 'Laden...',
	                months: ['Januar', 'Februar', 'März', 'April', 'Mai', 'Juni', 'Juli', 'August', 'September', 'October', 'November', 'Dezember'],
	                weekdays: ['Sonntag', 'Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag', 'Samstag'],
	                shortMonths: ['Jan', 'Feb', 'Mär', 'Apr', 'Mai', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dez'],
	                exportButtonTitle: "Exportieren",
	                printButtonTitle: "Ausdrucken",
	                rangeSelectorFrom: "Von:",
	                rangeSelectorTo: "An:",
	                rangeSelectorZoom: "Zeitrahmen:",
	                downloadPNG: 'Als PNG herunterladen',
	                downloadJPEG: 'Als JPEG herunterladen',
	                downloadPDF: 'Als PDF herunterladen',
	                downloadSVG: 'Als SVG herunterladen',
	                resetZoom: "Zoom zurücksetzen",
	                resetZoomTitle: "Zoom zurücksetzen",
	                thousandsSep: ".",
	                decimalPoint: ','
	        	},
	    	
	        global: {
		        	useUTC: false
		        }
	        }
	    );
	},
	
	add : function(config) {
		var plotOptions = config.plotOptions;
		if ( plotOptions.chart.animation == null)
			plotOptions.chart.animation = false;

		var chart = new Highcharts.Chart(config.plotOptions);
		$.each(config.sources, function(index, source) {
			$.getJSON( source.url, function( data ) {
				console.log("Got " + source.url);
				var serieOptions = source.serieOptions; 
				if (source.entryConverter == null) {
					serieOptions.data = data;
				}
				else {
					serieOptions.data = $.map(data, source.entryConverter);
				}
				if ( serieOptions.animation == null)
					serieOptions.animation = false;
				chart.addSeries(serieOptions)
			})
			.fail(function( jqxhr, textStatus, error ) {
				var err = textStatus + ", " + error;
				console.log( "Request for " + source.url + " failed: " + err )
			});
		});
	},

	stringToTime : function(str) {
		return new Date(str).getTime();
	}
};

