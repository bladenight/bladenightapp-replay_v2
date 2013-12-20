package de.greencity.bladenightapp.replay.log.local;

public class OutputImageFile {
	public OutputImageFile(String fileName, String tag, int order) {
		this.fileName = fileName;
		this.tag = tag;
		this.order = order;
	}
	public String fileName;
	public int order;
	public String tag;
}