package org.openstreetmap.osmosis.oracle.render;

import java.awt.geom.Rectangle2D;
import java.io.Serializable;
/*
 * @author lj.qian
 */
/**
 * This class defines how the TARGET_AREA is divided into square tiles to be processed/rendered
 * separately.
 *
 * The TARGET_AREA is a rectangle in longitude/latitude.
 */
public class OOWTile implements Serializable {

    //This is the area we are trying to render collectively into a wall poster
    public final static Rectangle2D.Double TARGET_AREA = new Rectangle2D.Double(-130,19, 64, 32);

    public final static int NUM_TILES_X = 16;  // the target area will be covered by this many tiles horizontally
    public final static int NUM_TILES_Y = 8;  // and this many tiles vertically;  must be in same aspect ratio as target area

    public final static double DEGREE_PER_TILE = TARGET_AREA.getWidth() / NUM_TILES_X;
    //Dot/Pixel Per Inch
    public final static int DPI = 300;

    //We are trying to draw everything within the TARGET_AREA to a poster image with these dimensions.
    public final static int POSTER_WIDTH_INCH = 96*2; //must be in same aspect ratio as target area
    public final static int POSTER_HEIGHT_INCH = 48*2;

    //This is the size of a tile (in pixel)
    public final static int TILE_SIZE = (int) ( POSTER_WIDTH_INCH * DPI / NUM_TILES_X );

    private int tileX, tileY; //tile index along x and y. Starts 0 from left and bottom.

    //world MBR of this tile (in lat/lon)
    private Rectangle2D.Double mbrLonLat = null;

    //mercator projected MBR of this tile (in meters).
    private Rectangle2D.Double mbrMercator = null;

    //the resolution
    private double metersPerPixel = 0;

    /**
     * Creates a new OOWTile instance corresponding to the tile index x and y.
     *
     * Note that we follow TMS tile scheme so the tile (0,0) is at the lower left corner of the target area.
     */
    public OOWTile(int x, int y){
        this.tileX = x;
        this.tileY = y;

        double[] mercatorLL = WorldMercatorUtils.lonLatToMeters(TARGET_AREA.getMinX(), TARGET_AREA.getMinY());
        double[] mercatorUR = WorldMercatorUtils.lonLatToMeters(TARGET_AREA.getMaxX(), TARGET_AREA.getMaxY());

        double widthMeters = mercatorUR[0] - mercatorLL[0];

        metersPerPixel = widthMeters / (POSTER_WIDTH_INCH * DPI) ;

        mbrLonLat = new Rectangle2D.Double(TARGET_AREA.getMinX()+x*DEGREE_PER_TILE,
                                       TARGET_AREA.getMinY()+y*DEGREE_PER_TILE,
                                        DEGREE_PER_TILE,
                                        DEGREE_PER_TILE);

        double[] meterLL = WorldMercatorUtils.lonLatToMeters(mbrLonLat.getMinX(), mbrLonLat.getMinY());
        double[] meterUR = WorldMercatorUtils.lonLatToMeters(mbrLonLat.getMaxX(), mbrLonLat.getMaxY());

        mbrMercator = new Rectangle2D.Double(meterLL[0], meterLL[1], meterUR[0]-meterLL[0], meterUR[1]-meterLL[1]);

    }

    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public Rectangle2D.Double getMbrLonLat() {
        return mbrLonLat;
    }

    public Rectangle2D.Double getMbrMercator() {
        return mbrMercator;
    }

    public void print(){

        System.out.println("=====> tile X="+tileX+", tile Y="+tileY);
        System.out.println("tile size: "+ TILE_SIZE);
        System.out.println("tile size in degree: "+ mbrLonLat.getWidth()+" X "+ mbrLonLat.getHeight());
        System.out.println("tile size in meter: "+ mbrMercator.getWidth()+" X "+ mbrMercator.getHeight());
        System.out.println("tile resolution: "+ metersPerPixel +" metersPerPixel");
        System.out.println("tile mbrLonLat="+mbrLonLat.getMinX()+","+mbrLonLat.getMinY()+" "+mbrLonLat.getMaxX()+","+mbrLonLat.getMaxY());
        System.out.println("tile mbrMercator="+mbrMercator.getMinX()+","+mbrMercator.getMinY()+" "+mbrMercator.getMaxX()+","+mbrMercator.getMaxY());

    }

    public static void main(String[] args){
        OOWTile tile = new OOWTile(0, 0);
        tile.print();

        tile = new OOWTile(0, 1);
        tile.print();
    }

}
