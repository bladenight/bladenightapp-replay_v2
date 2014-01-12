package de.greencity.bladenightapp.replay.log.local;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

public class Utils {
	public static void copyResourceTo(String resourceName, File destination) throws IOException {
		URL inputUrl = Utils.class.getResource(resourceName);
		FileUtils.copyURLToFile(inputUrl, destination);
	}
}
