package com.panpan.erm2text.meta;

/**
 * 
 * @author liuhs
 *
 */
public class MappingInfo {
	
	private String content = null;
	
	private  String fileName = null;
	
	public MappingInfo() {
		super();
	}
	
	public MappingInfo(String fileName,String content) {
		this.fileName = fileName;
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
