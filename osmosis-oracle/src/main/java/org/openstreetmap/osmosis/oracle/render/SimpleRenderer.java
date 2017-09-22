package org.openstreetmap.osmosis.oracle.render;

import org.openstreetmap.osmosis.oracle.common.MapFeatureLineString;

import java.awt.*;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

/**
 * A simple renderer to be used as an example.
 * It creates one OOWTile then renders its features.
 *
 * Created by lqian on 9/20/17.
 */
public class SimpleRenderer implements Serializable {
    public SimpleRenderer(){

    }

    /**
     * Returns the rendered contents in a byte array.
     * The byte array represents a PNG-formatted, compressed image.
     * @param tileX tile index on the X (longitude) axis
     * @param tileY tile index on the Y (latitude) axis
     * @param recordIterator Some iterator to get feature records
     * @return a compressed, PNG formatted image
     */
    public static byte[] render(int tileX, int tileY, Iterator recordIterator){
        //creates a specific OOWTile that covers one portion of the TARGET AREA.
        OOWTile tile = new OOWTile(tileX, tileY);

        //Creates a new renderer. It will allocate a buffered image as the
        //rendering canvas. It also provides all the necessary rendering methods.
        TileRenderingContext tc = new TileRenderingContext(tile);

        //default is to render all linestring using this line style.
        tc.setLineStyle(Color.white, 1.0f);

        //go over the records, extract its tags to check if we should render it,
        //then extract its coordinates (and convert them to Mercator projection if needed),
        //finally render the coordinates to the internal canvas buffer.
        while(recordIterator.hasNext()){
            //using MapFeatureLineString as an example here; but the iterator could
            //return any object, as long as we can access its tags and the coordinates.
            MapFeatureLineString lineString = (MapFeatureLineString) recordIterator.next();
            Map<String, String> tags  = lineString.getTags();

            //check and filter the record based on its tags.
            if(tc.getStyleSheet().applicable(tags)){

                //TODO: populate the Mercator coordinates of the feature geometry here.
                //      For mercator projection use the WorldMercatorUtils class which can transform
                //      a lon,lat point to meters in Mercator as required by our renderer.
                double[] xys = new double[0];

                tc.renderLineString(xys, tile.getMbrMercator());
            }
        }

        //we are done iterating the records, now is time to generate the PNG image
        byte[] image = tc.saveToBuffer();

        tc.destroy();

        return image;
    }

}
