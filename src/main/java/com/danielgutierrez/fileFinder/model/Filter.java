package com.danielgutierrez.fileFinder.model;

public enum Filter {
	NONE(""), 
	VIDEO("AVI|WMV|FLV|3GP|MPG|MP4|VOB|AVI"), 
	MUSIC("MP2|MP3|WAV"), 
	IMAGE("JPG|JPEG|PNG|GIF");
	
	String regex;
	
	private Filter(String regex){
		this.regex = regex;
	}
	
	public String getRegex() {
		return regex;
	}
}
