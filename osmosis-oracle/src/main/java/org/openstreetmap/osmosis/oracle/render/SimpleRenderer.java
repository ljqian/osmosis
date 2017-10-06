package org.openstreetmap.osmosis.oracle.render;

import oracle.spatial.spark.vector.index.SpatialPartition;
import oracle.spatial.spark.vector.oow17.writable.WayWritable;

import java.awt.*;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * A simple renderer to be used as an example.
 * It creates one OOWTile then renders its features.
 *
 * Created by lqian on 9/20/17.
 */
public class SimpleRenderer implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SimpleRenderer(){

    }
    
    public ImageInfo render(SpatialPartition partition, Iterator<WayWritable> records) {
		System.out.println("Rendering " + Arrays.toString(partition.getMbr()));
		double []mbr = partition.getMbr();
		OOWTile tile = new OOWTile(mbr[0], mbr[1]);

        TileRenderingContext tc = new TileRenderingContext(tile);
        tc.initialize();

        tc.setLineStyle(Color.black, 1.0f);
		
		ImageInfo image = new ImageInfo(partition.index(), partition.getMbr());
		image.setX(tile.getFullImageXPosition());
		image.setY(tile.getFullImageYPosition());
		image.setX(tile.getTileX());
		image.setY(tile.getTileY());
		while (records.hasNext()) {
			WayWritable info = records.next();
			Map<String, String> tags  = info.getTagMap();

            //check and filter the record based on its tags.
            if(tc.getStyleSheet().applicable(tags)){
            	image.incrementCount();
            	double[] xys = info.getGeom().getOrdinatesArray();
            	if (xys != null) {
            		WorldMercatorUtils.lonLatToMeters(xys);
                	
                    tc.renderLineString(xys, tile.getMbrMercator());
            	}
            	else {
            		System.out.println("Ordinate array is null!");
            	}
            }
		}
		//we are done iterating the records, now is time to generate the PNG image
        byte[] imageBytes = tc.saveToBuffer();
        image.setImageBytes(imageBytes);

        tc.destroy();
		return image;
	}

	public ImageInfo aggregateImages(ImageInfo aggrImageInfo, ImageInfo tileImageInfo) {
		// add tileImage to aggrImage
		/*BufferedImage aggrImage = ImageFileUtil.loadImageFromBytes(aggrImageInfo.getImageBytes());
		Graphics2D g2 = aggrImage.createGraphics();
		BufferedImage tileImage = ImageFileUtil.loadImageFromBytes(tileImageInfo.getImageBytes());
		g2.drawImage(tileImage, null, tileImageInfo.getX(), tileImageInfo.getY());
		aggrImageInfo.incrementCount(tileImageInfo.getRecordCount());
		aggrImageInfo.setImageBytes(ImageFileUtil.saveImageToBytes(aggrImage, "PNG"));
		aggrImage.flush();
		aggrImage = null;
		tileImage.flush();
		tileImage = null;*/
		aggrImageInfo.aggregateImageInfo(tileImageInfo);
		return aggrImageInfo;
	}

	public ImageInfo combineImages(ImageInfo aggrImageInfo1, ImageInfo aggrImageInfo2) {
		/*BufferedImage aggrImage1 = ImageFileUtil.loadImageFromBytes(aggrImageInfo1.getImageBytes());
		Graphics2D g2 = aggrImage1.createGraphics();
		BufferedImage aggrImage2 = ImageFileUtil.loadImageFromBytes(aggrImageInfo2.getImageBytes());
		g2.drawImage(aggrImage2, null, 0, 0);
		aggrImageInfo1.incrementCount(aggrImageInfo2.getRecordCount());
		aggrImageInfo1.setImageBytes(ImageFileUtil.saveImageToBytes(aggrImage1, "PNG"));
		aggrImage1.flush();
		aggrImage1 = null;
		aggrImage2.flush();
		aggrImage2 = null;*/
		aggrImageInfo1.getAggregatedImages().addAll(aggrImageInfo2.getAggregatedImages());
		return aggrImageInfo1;
	}
}
