package org.openstreetmap.osmosis.oracle.render;

import java.awt.geom.Rectangle2D;

/**
 * A utility class that implements a factory method to return popular
 * web mercator tiling scheme instances. Also provides various convenient
 * methods for conversion between Web Mercator projection and tile coordinates.
 *
 * @author lj.qian
 */
public class WorldMercatorUtils {
    public static final Rectangle2D.Double UNIVERSE_COVERAGE_LL = new Rectangle2D.Double(-180.0, -85.05112877980659, 360, 85.05112877980659*2 );
    public static final Rectangle2D.Double UNIVERSE_COVERAGE = new Rectangle2D.Double(-20037508.342789244, -20037508.342789244, 20037508.342789244*2, 20037508.342789244*2);

    static final double initialResolutionMeters = (2*Math.PI*6378137)/256.0;

    //Resolution of a pixel at each zoom level in meters at the equator.
    //Assumes tile sizes are 256 x 256, and the world is covered by a single
    //tile at zoom level 0.
    public static final double[] PIXEL_RESOLUTION_METERS = {
            initialResolutionMeters,
            initialResolutionMeters / 2,
            initialResolutionMeters / 4,
            initialResolutionMeters / 8,
            initialResolutionMeters / 16,
            initialResolutionMeters / 32,
            initialResolutionMeters / 64,
            initialResolutionMeters / 128,   //zoomlevel = 7
            initialResolutionMeters / 256,
            initialResolutionMeters / 512,
            initialResolutionMeters / 1024,
            initialResolutionMeters / 2048,
            initialResolutionMeters / 4096,
            initialResolutionMeters / 8192,
            initialResolutionMeters / 16384,
            initialResolutionMeters / 32768,
            initialResolutionMeters / 65536,
            initialResolutionMeters / 131072,
            initialResolutionMeters / 262144,
            initialResolutionMeters / 524288, //zoomlevel = 19
            initialResolutionMeters / 1048576,
            initialResolutionMeters / 2097152,
            initialResolutionMeters / 4194304,
            initialResolutionMeters / 8388608 //zoomlevel = 23
    };

    public static final double ORIGIN_SHIFT = 2 * Math.PI * 6378137 / 2.0;

    /**
     * Converts pixel coordinates in given zoom level of pyramid to
     * 3857 coordinates.
     * @param px
     * @param py
     * @param zoom
     * @return 3857 coordinates in meters
     */
    public static final double[] pixelsToMeters (int zoom, int px, int py){
        double res = PIXEL_RESOLUTION_METERS[zoom];

        double mx = px * res - ORIGIN_SHIFT;
        double my = py * res - ORIGIN_SHIFT;
        return new double[]{mx, my};
    }

    /**
     * Converts XY point from 3857 to lon/lat
     * @param mx x ordinate in meters
     * @param my y ordinate in meters
     * @return lon and lat
     */
    public static final double[] metersToLonLat (double mx, double my){
        double lon = (mx / ORIGIN_SHIFT) * 180.0;
        double lat = (my / ORIGIN_SHIFT) * 180.0;

        lat = 180 / Math.PI * (2 * Math.atan( Math.exp( lat * Math.PI / 180.0)) - Math.PI / 2.0);

        return new double[] {lon, lat};
    }

    /**
     * Converts given lon/lat in 8307 to XY in 3857
     * @param lon
     * @param lat
     * @return
     */
    public static final double[] lonLatToMeters(double lon, double lat){
        double mx = lon * ORIGIN_SHIFT / 180.0;
        double my = Math.log( Math.tan((90 + lat) * Math.PI / 360.0 )) / (Math.PI / 180.0) ;

        my = my * ORIGIN_SHIFT / 180.0;
        return new double[]{mx, my};
    }

    /**
     * Converts 3857 XY to pixel coordinates in a given zoom level of
     * the pyramid.
     * @param zoom
     * @param mx
     * @param my
     * @return
     */
    public static final int[] metersToPixels(int zoom, double mx, double my){
        double res = PIXEL_RESOLUTION_METERS[zoom];

        int x = (int) ( (mx + ORIGIN_SHIFT) / res );
        int y = (int) ( (my + ORIGIN_SHIFT) / res );

        return new int[]{x,y};
    }

    /*
    public static void main(String[] args){
        TileScheme ts = WorldMercatorUtils.getTileScheme("tms");

        double[] bboxWorld = ts.getTileCoverageLL(0, 0, 0);
        System.out.println("world bbox: "+bboxWorld[0]+", "+bboxWorld[1]+" "+bboxWorld[2]+", "+bboxWorld[3]);

        bboxWorld = ts.getTileCoverageLL(1, 0, 1);
        System.out.println("1,0,1 world bbox: "+bboxWorld[0]+", "+bboxWorld[1]+" "+bboxWorld[2]+", "+bboxWorld[3]);

        bboxWorld = ts.getTileCoverageLL(1, 1, 1);
        System.out.println("1,1,1 world bbox: "+bboxWorld[0]+", "+bboxWorld[1]+" "+bboxWorld[2]+", "+bboxWorld[3]);

        bboxWorld = ts.getTileCoverageLL(1, 1, 2);
        System.out.println("1,1,2 world bbox: "+bboxWorld[0]+", "+bboxWorld[1]+" "+bboxWorld[2]+", "+bboxWorld[3]);

        bboxWorld = ts.getTileCoverageLL(3, 2, 2);
        System.out.println("3,2,2 world bbox: "+bboxWorld[0]+", "+bboxWorld[1]+" "+bboxWorld[2]+", "+bboxWorld[3]);

        bboxWorld = ts.getTileCoverageLL(5, 6, 3);
        System.out.println("5,6,3 world bbox: "+bboxWorld[0]+", "+bboxWorld[1]+" "+bboxWorld[2]+", "+bboxWorld[3]);

        bboxWorld = ts.getTileCoverageLL(7, 5, 3);
        System.out.println("7,5,3 world bbox: "+bboxWorld[0]+", "+bboxWorld[1]+" "+bboxWorld[2]+", "+bboxWorld[3]);
    }
    */
}
