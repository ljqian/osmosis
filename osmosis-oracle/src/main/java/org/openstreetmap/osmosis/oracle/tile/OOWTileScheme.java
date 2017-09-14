package org.openstreetmap.osmosis.oracle.tile;

import java.awt.geom.Rectangle2D;

public class OOWTileScheme implements TileScheme {

    // our main area of interest is continental US with a bounding box of 64 x 32 degrees.
    private final Rectangle2D.Double universeLL; // = new Rectangle2D.Double(-130,19, 64, 32);

    //this is the same area of interest but in world mercator (3857) coordinate system; unit is meter.
    private final Rectangle2D.Double universeMercatorMeter;

    //size of our desired wall poster (in inches).
    private final int posterWidthInch ;
    private final int posterHeightInch;
    private final int posterZoomLevel; //poster shows the target area at this zoom level. Typically is 4, so each axis is divided into 16 tiles

    //the DPI, or PPI, for print.
    private final int DPI = 300;

    //the pixel universe of our desired wall poster.
    private final Rectangle2D.Double universePixel;

    //size of each tile, in pixel.
    private final int tileSizePixel;
    private final double initialResolutionMeters;

    //resolution by zoom level.
    private final double[] METERS_PER_PIXEL = new double[10];

    /**
     * This is a custom tiling scheme. A few assumptions are made, such as:
     *   - Individual tile size is not 256x256 pixel; rather it will be determined by wall poster size and the target area.
     *   - zoom level 0 is a single tile, and it should cover the entire target area.
     *   - Only USA data will be rendered (is this restriction necessary?)
     *   - The wall poster should comfortably cover the entire target area (as space-efficient as possible).
     *   - The wall poster scale (in terms of meters-per-dot or meters-per-pixel) is easily determined.
     *   - The poster image is stitched from tiles that each covers about 4 degrees. This ensures continental US
     *     can be divided into 16 x 8 "tiles" (at zoom level 3).
     *   - If the poster and the target area have different aspect ratio, then the map will be strechted (or shrinked).
     *   - only support 10 zoom levels
     *
     * @param targetAreaLL
     * @param posterWidthInch
     * @param posterHeightInch
     */
    public OOWTileScheme(Rectangle2D.Double targetAreaLL, int posterWidthInch, int posterHeightInch, int posterZoomLevel){
        universeLL = targetAreaLL;
        this.posterWidthInch = posterWidthInch;
        this.posterHeightInch = posterHeightInch;
        this.posterZoomLevel = posterZoomLevel;

        universePixel = new Rectangle2D.Double(0, 0, posterWidthInch*DPI, posterHeightInch*DPI);

        double[] lowerLeftPt = WorldMercatorUtils.lonLatToMeters(universeLL.getMinX(), universeLL.getMinY());
        double[] upperRightPt = WorldMercatorUtils.lonLatToMeters(universeLL.getMaxX(), universeLL.getMaxY());

        double w = upperRightPt[0] - lowerLeftPt[0];
        double h = upperRightPt[1] - lowerLeftPt[1];
        universeMercatorMeter = new Rectangle2D.Double(lowerLeftPt[0], lowerLeftPt[1], w, h);
        System.out.println("universe width in meters = "+ universeMercatorMeter.getWidth());
        System.out.println("universe height in meters = "+ universeMercatorMeter.getHeight());

        int numTilesOnShortSide =  1 << posterZoomLevel;

        System.out.println("at poster zoom level "+ posterZoomLevel+", there will be "+numTilesOnShortSide +" tiles on the short side");

        //we can now determine size of each tile (in pixel).
        tileSizePixel = (int) (Math.min(universePixel.getWidth(), universePixel.getHeight()) / numTilesOnShortSide);

        System.out.println("tile size: "+ tileSizePixel+" px.");

        //at zoom level 0, only 1 tile covers the whole universe (which is our target area).
        initialResolutionMeters = Math.min(universeMercatorMeter.getWidth(), universeMercatorMeter.getHeight()) / tileSizePixel;

        for(int i=0; i<10; i++){
            METERS_PER_PIXEL[i] = initialResolutionMeters / (1 << i);
            System.out.println("\t resolution at zoom level ["+i+"]= "+ METERS_PER_PIXEL[i]);
        }
    }

    @Override
    public int getSrid() {
        return 9000001;
    }

    @Override
    public int getTileSize() {
        return tileSizePixel;
    }

    //not really used here.
    @Override
    public int getTileExtent() {
        return 4096;
    }

    @Override
    public Rectangle2D.Double getUniverseCoverageLL() {
        return universeLL;
    }

    @Override
    public Rectangle2D.Double getUniverseCoverage() {
        return universeMercatorMeter;
    }

    @Override
    public Rectangle2D.Double getTileCoverageLL(int zoom, int x, int y) {
        Rectangle2D.Double bboxMeters = getTileCoverage(zoom, x, y); // in meters

        double[] ll = WorldMercatorUtils.metersToLonLat(bboxMeters.getMinX(), bboxMeters.getMinY());
        double[] ur = WorldMercatorUtils.metersToLonLat(bboxMeters.getMaxX(), bboxMeters.getMaxY());

        return new Rectangle2D.Double(ll[0], ll[1], ur[0]-ll[0], ur[1]-ll[1]);
    }

    private double[] pixelsToMeters (int zoom, int px, int py){
        double res = METERS_PER_PIXEL[zoom];

        double mx = px * res + universeMercatorMeter.getMinX();
        double my = py * res + universeMercatorMeter.getMinY();
        return new double[]{mx, my};
    }

    @Override
    public Rectangle2D.Double getTileCoverage(int zoom, int x, int y) {
        //convert into TMS tile y
        y = (1 << zoom) - 1 - y;

        //lower left corner of the tile in Web Mercator projection
        double[] ll = pixelsToMeters(zoom, x*tileSizePixel, y*tileSizePixel);
        //upper right corner of the tile in Web Mercator projection
        double[] ur = pixelsToMeters(zoom, (x+1)*tileSizePixel, (y+1)*tileSizePixel);

        return new Rectangle2D.Double(ll[0], ll[1], ur[0]-ll[0], ur[1]-ll[1]);
    }

    public static void main(String[] args){
        //use http://boundingbox.klokantech.com/ to figure out what bbox to use as target area.
        Rectangle2D.Double targetArea = new Rectangle2D.Double(-130,19, 64, 32);

        int posterZoom = 4;
        OOWTileScheme ots = new OOWTileScheme(targetArea, 96, 72, posterZoom);

        TileSchemeFactory.registerTileScheme("oow", ots);

        Tile t00 = new Tile(posterZoom, 0, 0, "oow");
        Rectangle2D.Double mbr = t00.getCoverageLL();
        System.out.println("Tile lat/lon bbox: "+ mbr.toString());

        mbr = t00.getCoverage();
        System.out.println("Tile mercator bbox: "+ mbr.toString());

        int tileSize = t00.getSize();
        System.out.println("tile size: "+ tileSize+" px.");
    }
}
