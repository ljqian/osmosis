package org.openstreetmap.osmosis.oracle.render;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	private int index = 0;
	private double[] mbr = null;
	private int recordCount = 0;
	private byte[] bytes = null;
	private int x;
	private int y;
	private List<ImageInfo> aggregatedImages;
	
	public ImageInfo(int index, double[] mbr){
		this.index = index;
		this.mbr = mbr;
		this.aggregatedImages = new ArrayList<>();
	}
	
	public void aggregateImageInfo(ImageInfo info) {
		aggregatedImages.add(info);
	}
	
	public List<ImageInfo> getAggregatedImages() {
		return aggregatedImages;
	}
	
	public byte[] getImageBytes() {
		return bytes;
	}
	
	public void setImageBytes(byte []imageBytes) {
		this.bytes = imageBytes;
	}
	
	public void incrementCount() {
		recordCount++;
	}
	
	public void incrementCount(int count) {
		recordCount += count;
	}
	
	public int getRecordCount() {
		return recordCount;
	}
	
	@Override
	public String toString() {
		return "Image: { index:"+index+" mbr:"+Arrays.toString(mbr)+" count:"+recordCount+"} ";
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}
	
	public static void main(String []args) {
		/*ImageInfo image1 = new ImageInfo(0, new double[]{0,0,1,1});
		image1.setImageBytes(ImageFileUtil.saveImageToBytes(ImageFileUtil.loadImageFromFile(new File("D:\\osm\\x_1_y_4.png")), "PNG"));
		image1.setFullImageXPosition(0);
		image1.setFullImageYPosition(0);
		
		ImageInfo image2 = new ImageInfo(1, new double[]{1,0,2,1});
		image2.setImageBytes(ImageFileUtil.saveImageToBytes(ImageFileUtil.loadImageFromFile(new File("D:\\osm\\x_2_y_4.png")), "PNG"));
		image2.setFullImageXPosition(3600);
		image2.setFullImageYPosition(0);
		
		BufferedImage newFullImage = new BufferedImage(7200, 3600, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = newFullImage.createGraphics();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
        g2.fillRect(0,0,7200,3600);
		ImageInfo fullImage = new ImageInfo(99, new double[]{0,0,2,1});
		fullImage.setImageBytes(ImageFileUtil.saveImageToBytes(newFullImage, "PNG"));
		
		SimpleRenderer renderer = new SimpleRenderer();
		
		renderer.aggregateImages(fullImage, image1);
		renderer.aggregateImages(fullImage, image2);
		
		ImageFileUtil.saveBytesToFile(new File("D:\\osm\\aggregated.png"), fullImage.getImageBytes());*/
		
		ImageInfo image1 = new ImageInfo(0, new double[]{0,0,2,1});
		image1.setImageBytes(ImageFileUtil.saveImageToBytes(ImageFileUtil.loadImageFromFile(new File("D:\\osm\\x_1_y_4.png")), "PNG"));
		
		ImageInfo image2 = new ImageInfo(1, new double[]{0,0,2,1});
		image2.setImageBytes(ImageFileUtil.saveImageToBytes(ImageFileUtil.loadImageFromFile(new File("D:\\osm\\x_2_y_4.png")), "PNG"));
		
		SimpleRenderer renderer = new SimpleRenderer();
		
		renderer.aggregateImages(image1, image2);
		ImageFileUtil.saveBytesToFile(new File("D:\\osm\\combined.png"), image1.getImageBytes());
	}
}
