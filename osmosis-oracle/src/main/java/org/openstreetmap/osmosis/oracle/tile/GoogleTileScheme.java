package org.openstreetmap.osmosis.oracle.tile;


import java.awt.geom.Rectangle2D;

/**
 * Google tiling scheme.
 *
 *
 * @author lqian
 */
public class GoogleTileScheme implements TileScheme
{

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
     * Returns the bounds of the tile referenced using the google (x,y)
     * scheme. The bounds will be in meters.
     * @param zoom tile zoom level
     * @param x  tile index x in the google tiling scheme
     * @param y  tile index y in the google tiling scheme
     * @return
     */
    @Override
    public Rectangle2D.Double getTileCoverage(int zoom, int x, int y) {
        //convert into TMS tile y
        y = (1 << zoom) - 1 - y;

        //lower left corner of the tile in Web Mercator projection
        double[] ll = WorldMercatorUtils.pixelsToMeters(zoom, x*256, y*256);
        //upper right corner of the tile in Web Mercator projection
        double[] ur = WorldMercatorUtils.pixelsToMeters(zoom, (x+1)*256, (y+1)*256);
        return new Rectangle2D.Double(ll[0], ll[1], ur[0]-ll[0], ur[1]-ll[1]);
    }

    /**
     * Returns the bounds fo the tile reverenced using the google (x,y)
     * scheme. The bounds will be in lon/lat.
     * @param zoom tile zoom level
     * @param x tile index x in the google tiling scheme
     * @param y tile index y in the google tiling scheme
     * @return
     */
    @Override
    public Rectangle2D.Double getTileCoverageLL(int zoom, int x, int y) {
        Rectangle2D.Double bboxMeters = getTileCoverage(zoom, x, y); // in meters

        double[] ll = WorldMercatorUtils.metersToLonLat(bboxMeters.getMinX(), bboxMeters.getMinY());
        double[] ur = WorldMercatorUtils.metersToLonLat(bboxMeters.getMaxX(), bboxMeters.getMaxY());

        return new Rectangle2D.Double(ll[0], ll[1], ur[0]-ll[0], ur[1]-ll[1]);
    }


    public static void main(String[] args){
        GoogleTileScheme ts = new GoogleTileScheme();

        Rectangle2D.Double bboxWorld = ts.getTileCoverageLL(0, 0, 0);
        System.out.println("world bbox: "+bboxWorld.getMinX()+", "+bboxWorld.getMinY()+" "+bboxWorld.getMaxX()+", "+bboxWorld.getMaxY());

        bboxWorld = ts.getTileCoverageLL(1, 0, 1);
        System.out.println("1,0,1 world bbox: "+bboxWorld.getMinX()+", "+bboxWorld.getMinY()+" "+bboxWorld.getMaxX()+", "+bboxWorld.getMaxY());

        bboxWorld = ts.getTileCoverageLL(1, 1, 1);
        System.out.println("1,1,1 world bbox: "+bboxWorld.getMinX()+", "+bboxWorld.getMinY()+" "+bboxWorld.getMaxX()+", "+bboxWorld.getMaxY());

        bboxWorld = ts.getTileCoverageLL(2, 1, 1);
        System.out.println("2, 1,1 world bbox: "+bboxWorld.getMinX()+", "+bboxWorld.getMinY()+" "+bboxWorld.getMaxX()+", "+bboxWorld.getMaxY());

        bboxWorld = ts.getTileCoverageLL(2, 3, 2);
        System.out.println("2, 2,3 world bbox: "+bboxWorld.getMinX()+", "+bboxWorld.getMinY()+" "+bboxWorld.getMaxX()+", "+bboxWorld.getMaxY());

        bboxWorld = ts.getTileCoverageLL(3, 5, 6);
        System.out.println("3, 5,6 world bbox: "+bboxWorld.getMinX()+", "+bboxWorld.getMinY()+" "+bboxWorld.getMaxX()+", "+bboxWorld.getMaxY());

        bboxWorld = ts.getTileCoverageLL(6, 13, 22);
        System.out.println("6, 13,22 world bbox: "+bboxWorld.getMinX()+", "+bboxWorld.getMinY()+" "+bboxWorld.getMaxX()+", "+bboxWorld.getMaxY());

    }


}
