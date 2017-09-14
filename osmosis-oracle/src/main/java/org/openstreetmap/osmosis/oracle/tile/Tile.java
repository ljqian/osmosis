package org.openstreetmap.osmosis.oracle.tile;


import java.awt.geom.Rectangle2D;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * This class represents a Mapbox vector tile in a specific tile scheme. Every
 * vector tile is uniquely identified by its Z/X/Y numbers.
 *
 * Default tile scheme is the Google tile scheme, where tile index Y starts as 0
 * at the top and increases toward the bottom,
 *
 * @author lqian
 */
public class Tile {
    private final TileScheme tileScheme;

    //local cached coverage to save repeated computing
    private Rectangle2D.Double coverage;
    private Rectangle2D.Double coverageLL;
    private Rectangle2D.Double coverageBuffer; //a buffer of 5 percent on each edge

    private int zoom = 0;
    private int x = 0;
    private int y = 0;

    public Tile(int zoom, int x, int y){
        this(zoom, x, y, "google");
    }

    public Tile(int zoom, int x, int y, String tileSchemeName){
        this.zoom = zoom;
        this.x = x;
        this.y = y;

        tileScheme = TileSchemeFactory.getTileScheme(tileSchemeName);
        if(tileScheme == null)
            throw new IllegalArgumentException("Unknow tile scheme name: "+tileSchemeName);

        this.coverage = this.tileScheme.getTileCoverage(zoom, x, y);
        this.coverageLL = this.tileScheme.getTileCoverageLL(zoom, x, y);
        computeBuffer();
    }

    private void computeBuffer(){
        //computes the buffered coverage area.
        double xSpan = this.coverage.getWidth();
        double xDelta = this.coverage.getWidth() * 0.025;
        double yDelta = this.coverage.getHeight() * 0.025;
        double x = this.coverage.getMinX() - xDelta;
        double y = this.coverage.getMinY() - yDelta;
        double x2 = this.coverage.getMaxX() + xDelta;
        double y2 = this.coverage.getMaxY() + yDelta;

        this.coverageBuffer = new Rectangle2D.Double(x, y, x2-x, y2-y);
    }
    /**
     * Sets the new ZXY values of this tile.
     */
    public void setZXY(int zoom, int x, int y){
        this.zoom = zoom;
        this.x = x;
        this.y = y;

        this.coverage = this.tileScheme.getTileCoverage(zoom, x, y);
        this.coverageLL = this.tileScheme.getTileCoverageLL(zoom, x, y);
        computeBuffer();
    }

    public int getZoom(){
        return this.zoom;
    }

    public int getX(){
        return this.x;
    }

    public int getY(){
        return this.y;
    }

    public TileScheme getTileScheme(){
        return this.tileScheme;
    }

    /**
     * Returns the size of the tile in pixels. Defined in the associated
     * tile scheme.
     * This is same as calling getTileScheme().getTileSize();
     */
    public int getSize(){
        return tileScheme.getTileSize();
    }

    /**
     * Returns the integer coordinate system extent of the tile. Defined in the
     * associated tile scheme.
     * This is same as calling getTileScheme().getTileExtent().
     *
     * @return
     */
    public int getExtent(){
        return tileScheme.getTileExtent();
    }

    /**
     * Returns the square area covered by this tile in lon/lat.
     *
     * @return
     */
    public Rectangle2D.Double getCoverageLL(){
        return this.coverageLL;
    }

    /**
     * Returns the square area covered by this tile in the map projection unit,
     * such as meters for Web Mercator tiles.
     * @return
     */
    public Rectangle2D.Double getCoverage(){
        return this.coverage;
    }

    /**
     * Returns the buffered coverage area in the tile scheme's map projection
     * coordinates.
     *
     * @return
     */
    public Rectangle2D.Double getCoverageBuffer(){
        return this.coverageBuffer;
    }

    /**
     * Checks if this tile has any interaction with the provided feature's
     * bounding box which is in the tile scheme's map projection coordinates.
     * The check is done using the buffered coverage area fo this tile.
     * @param featureBBox
     * @return
     */
    public boolean anyinteract(double[] featureBBox){
        if(coverageBuffer.getMaxX() < featureBBox[0] ||
                featureBBox[2] < coverageBuffer.getMinX() ||
                coverageBuffer.getMaxY() < featureBBox[1] ||
                featureBBox[3] < coverageBuffer.getMinY())
            return false;
        else
            return true;
    }

    public String getInfo(){
        return "Tile["+getZoom()+"/"+getX()+"/"+getY()+"]";
    }
}
