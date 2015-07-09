package com.nui.multiphotopicker.model;

import java.io.Serializable;

/**
 * 图像对象数据结构
 *
 */
public class ImageItem implements Serializable
{
	private static final long serialVersionUID = -7188270558443739436L;
	public String imageId;
	public String thumbnailPath;
	public String sourcePath;
	public boolean isSelected = false;
}
