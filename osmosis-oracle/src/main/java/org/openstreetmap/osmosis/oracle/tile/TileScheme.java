package org.openstreetmap.osmosis.oracle.tile;


import java.awt.geom.Rectangle2D;

/**
 * Defines the interface of common tile schemes such as
 * the ones used in Google Maps, OpenStreetMaps, TMS, and Bing Maps etc.
 *
 * Can also be implemented by custom tile schemes such as those for indoor
 * floor plans.
 *
 * All tile schemes must meet the following pyramiding rules.
 * <UL>
 *  <LI> All tiles are square and of equal size (typically 256 pixels)
 *  <LI> Always starts at zoom level 0, which has a single square tile covering
 *       the universe of the tile scheme.
 *  <LI> Each tile at zoom level N is always divided into 4 equal sized squares
 *       at zoom level N+1.
 * </UL>
 *
 * @author lqian
 */
public interface TileScheme {
    /**
     * Returns the SRID for the map projection used by the
     * tiling scheme.
     * @return the SRID of the associated map projection; 3857 in most cases.
     */
    public int getSrid();

    /**
     * Returns the size of a tile, in pixels.
     * Default value is typically 256.
     * @return the size of the square tile in pixels.
     */
    public int getTileSize();

    /**
     * Returns the extent of a tile in integer. This typically defines an
     * integer screen coordinate space for the tile, with (0,0) at the top left
     * corner of the tile, and (extent-1, extent-1) at the lower right corner.
     * Default value is 4096 per MapBox convention.
     *
     * @return The extent of a tile in integer
     */
    public int getTileExtent();

    /**
     * Returns the bounds of the entire universe covered by a specific
     * tiling scheme in lon/lat.
     *
     * @return an array of longitudes and latitudes. First pair is the (longitude,
     * latitude) of the lower left corner, and the second pair is that of the
     * upper right corner.
     */
    public Rectangle2D.Double getUniverseCoverageLL();

    /**
     * Returns the bounds of the entire universe in the associated map
     * projection coordinates.
     *
     * @return an array of projected map coordinates. First pair is the (x,y)
     * of the lower left corner, and the second pair is that of the
     * upper right corner.
     */
    public Rectangle2D.Double getUniverseCoverage();

    /**
     * Returns the tile extent expressed in longitude/latitude.
     * @param zoom  The zoom level of the tile
     * @param x  the x-index of the tile
     * @param y  the y-index of the tile
     * @return an array of longitudes and latitudes. First pair is the (longitude,
     * latitude) of the lower left corner of the tile, and the second pair is
     * that of the upper right corner of the tile.
     */
    public Rectangle2D.Double getTileCoverageLL(int zoom, int x, int y);

    /**
     * Returns the tile extent expressed in the associated map
     * projection unit (meters).
     * @param zoom the zoom level of the tile
     * @param x  the x-index of the tile
     * @param y  the y-index of the tile
     * @return an array of projected coordinates. First pair is the (x,y) of
     * the lower left corner of the tile, and the second pair is that of
     * the upper right corner of the tile.
     */
    public Rectangle2D.Double getTileCoverage(int zoom, int x, int y);

}
