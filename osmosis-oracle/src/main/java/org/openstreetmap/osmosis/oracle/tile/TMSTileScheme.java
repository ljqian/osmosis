package org.openstreetmap.osmosis.oracle.tile;


import java.awt.geom.Rectangle2D;

/**
 *
 * @author lqian
 */
public class TMSTileScheme implements TileScheme {

/*
    What is the difference between TMS and Google Maps/QuadTree tile name convention?

    The tile raster itself is the same (equal extent, projection, pixel size),
    there is just different identification of the same raster tile.
    Tiles in TMS are counted from [0,0] in the bottom-left corner, id is XYZ.
    Google placed the origin [0,0] to the top-left corner, reference is XYZ.
*/

    @Override
    public int getSrid() {
        return 3857;
    }

    @Override
    public int getTileSize() {
        return 256;
    }

    @Override
    public int getTileExtent() {
        return 4096;
    }

    @Override
    public Rectangle2D.Double getUniverseCoverageLL() {
        return WorldMercatorUtils.UNIVERSE_COVERAGE_LL;
    }

    @Override
    public Rectangle2D.Double getUniverseCoverage() {
        return WorldMercatorUtils.UNIVERSE_COVERAGE;
    }


    /**
     * Returns the bounds of the given tile in lon/lat.
     * @param x tile index on the x axis
     * @param y tile index on the y axis
     * @param zoom zoom level
     * @return
     */
    @Override
    public Rectangle2D.Double getTileCoverageLL(int zoom, int x, int y) {
        Rectangle2D.Double bboxMeters = getTileCoverage(zoom, x, y); // in meters

        double[] ll = WorldMercatorUtils.metersToLonLat(bboxMeters.getMinX(), bboxMeters.getMinY());
        double[] ur = WorldMercatorUtils.metersToLonLat(bboxMeters.getMaxX(), bboxMeters.getMaxY());

        return new Rectangle2D.Double(ll[0], ll[1], ur[0]-ll[0], ur[1]-ll[1]);
    }

    /**
     * Returns bounds of the given tile in Web Mercator coordinates
     * @param zoom zoom level
     * @param x tile index on the x axis
     * @param y tile index on the y axis
     * @return
     */
    @Override
    public Rectangle2D.Double getTileCoverage(int zoom, int x, int y) {
        //lower left corner of the tile in Web Mercator projection
        double[] ll = WorldMercatorUtils.pixelsToMeters(zoom, x*256, y*256);
        //upper right corner of the tile in Web Mercator projection
        double[] ur = WorldMercatorUtils.pixelsToMeters(zoom, (x+1)*256, (y+1)*256);
        return new Rectangle2D.Double(ll[0], ll[1], ur[0]-ll[0], ur[1]-ll[1]);
    }

/*
    public static void main(String[] args){
        TMSTileScheme ts = new TMSTileScheme();

        double[] bboxWorld = ts.getTileCoverageLL(0, 0, 0);
        System.out.println("world bbox: "+bboxWorld[0]+", "+bboxWorld[1]+" "+bboxWorld[2]+", "+bboxWorld[3]);

        bboxWorld = ts.getTileCoverageLL(1, 0, 1);
        System.out.println("1,0,1 world bbox: "+bboxWorld[0]+", "+bboxWorld[1]+" "+bboxWorld[2]+", "+bboxWorld[3]);

        bboxWorld = ts.getTileCoverageLL(1, 1, 1);
        System.out.println("1,1,1 world bbox: "+bboxWorld[0]+", "+bboxWorld[1]+" "+bboxWorld[2]+", "+bboxWorld[3]);

        bboxWorld = ts.getTileCoverageLL(2, 1, 1);
        System.out.println("2, 1,1 world bbox: "+bboxWorld[0]+", "+bboxWorld[1]+" "+bboxWorld[2]+", "+bboxWorld[3]);

        bboxWorld = ts.getTileCoverageLL(2, 3, 2);
        System.out.println("2, 3,2 world bbox: "+bboxWorld[0]+", "+bboxWorld[1]+" "+bboxWorld[2]+", "+bboxWorld[3]);

        bboxWorld = ts.getTileCoverageLL(3, 5, 6);
        System.out.println("3, 5,6 world bbox: "+bboxWorld[0]+", "+bboxWorld[1]+" "+bboxWorld[2]+", "+bboxWorld[3]);
    }
*/

}
