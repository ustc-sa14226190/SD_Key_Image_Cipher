package com.nui.multiphotopicker.model;

import com.nui.multiphotopicker.tool.RSACipher;

public class ImageEncryptItem {
	private String encryptFilename;
	private String unencryptFilename;
	private String sourcePath;
	private boolean isSelected;
	private String key;
	public ImageEncryptItem(String encryptFilename,String unencryptFilename,
			String sourcePath, String key,boolean isSelected){
		this.encryptFilename = encryptFilename;
		this.unencryptFilename = unencryptFilename;
		this.sourcePath = sourcePath;
		this.key = RSACipher.encrypt(key);
		this.isSelected = isSelected;
	}
	public String getEncryptFilename() {
		return encryptFilename;
	}
	public void setEncryptFilename(String encryptFilename) {
		this.encryptFilename = encryptFilename;
	}
	public String getUnencryptFilename() {
		return unencryptFilename;
	}
	public void setUnencryptFilename(String unencryptFilename) {
		this.unencryptFilename = unencryptFilename;
	}
	public String getSourcePath() {
		return sourcePath;
	}
	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}
	public boolean isSelected() {
		return isSelected;
	}
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	public String getKey() {
		return RSACipher.decrypt(key);
	}
	public void setKey(String key) {
		this.key = key;
	}
}
