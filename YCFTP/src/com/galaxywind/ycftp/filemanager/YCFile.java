package com.galaxywind.ycftp.filemanager;

public class YCFile {
	private String path;
	private String name;
	private double size;
	private boolean isFile;
	
	public YCFile() {
		super();
	}
	
	public YCFile(String path) {
		super();
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public boolean isFile() {
		return isFile;
	}

	public void setFile(boolean isFile) {
		this.isFile = isFile;
	}

	@Override
	public String toString() {
		return "File [path=" + path + ", name=" + name + ", size=" + size
				+ ", isFile=" + isFile + "]";
	}
}
