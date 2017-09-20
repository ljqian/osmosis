/*
 * @author lj.qian
 */
package org.openstreetmap.osmosis.oracle.render;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides the rendering context suitable for
 * drawing geometries to a specific Tile.
 */
public class TileRenderingContext implements RenderingContext, Serializable {
    private static Logger log = Logger.getLogger(TileRenderingContext.class.getName());

    private OOWTile tile;
    private Graphics2D graphics;
    private BufferedImage canvas;
    private int canvasWidth, canvasHeight; //in pixel
    private boolean initialized = false;
    private StyleSheet styleSheet = new HighwayStyleSheet();

    private AffineTransform xfm = null;

    public TileRenderingContext(){

    }

    /**
     * Creates a new rendering context that's suitable for drawing geospatial data
     * for the given tile. The drawing will be done to a blank canvas (image) with
     * the specified dimension.
     *
     * @param tile The OOWTile object for which this rendering context is for.
     */
    public TileRenderingContext(OOWTile tile){
        this.tile = tile;

        canvasWidth = tile.TILE_SIZE;
        canvasHeight = tile.TILE_SIZE;

        canvas = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
        graphics = canvas.createGraphics();
    }

    public TileRenderingContext(OOWTile tile, byte[] pngImage){
        this.tile = tile;

        if(pngImage==null)
            throw new IllegalArgumentException("Null PNG image data found!");

        try {
            BufferedImage _canvas = ImageIO.read(new ByteArrayInputStream(pngImage));

            setCanvas(_canvas);
        } catch (IOException e) {
            log.warning("Unable to load PNG image from byte array.");
            throw new IllegalArgumentException("Unable to load PNG image from byte array (size="+pngImage.length+").");
        }

    }

    @Override
    public Graphics2D getGraphics() {
        return graphics;
    }

    @Override
    public void destroy(){
        if(graphics!=null){
            graphics.dispose();
            graphics = null;
        }
        initialized = false;
    }

    /**
     * User of this RenderingContext must call this method to initialize it.
     *
     * A rendering context cannot be passed into any rendering operation before it's
     * initialized using this method.
     */
    @Override
    public void initialize(){
        if(initialized)
            return;

        if(graphics==null){
            log.warning("No Graphics2D object found. This RenderingContext cannot be initialized.");
            return;
        }

        if(styleSheet==null){
            log.warning("No StyleSheet object found. This RenderingContext cannot be initialized.");
            return;
        }

        //clear image background to be transparent
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
        graphics.fillRect(0,0,canvasWidth,canvasHeight);

        //reset composite
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

        setAffineTransform();

        //all future rendering on this tile is now subject to this viewport transformation.
        graphics.setTransform(xfm);


        initialized = true;
    }

    public AffineTransform getViewPortTransform() {
        return xfm;
    }

    @Override
    public StyleSheet getStyleSheet(){
        return styleSheet;
    }

    public void setStyleSheet(StyleSheet sheet){
        styleSheet = sheet;
    }

    public BufferedImage getCanvasImage(){
        return canvas;
    }


    //
    // Sets the affine transform to render the geometries to the canvas.
    // The geometry coordinates must be in mercator projection
    //
    private void setAffineTransform(){
        //this method ensures correct geometry transformation from the tile-scheme's coordinate space
        // to the canvas image's pixel space.

        XFViewPort vp = new XFViewPort();
        vp.setStrict(true);
        vp.setDataView(tile.getMbrMercator());
        vp.setDeviceView(0,0, tile.TILE_SIZE, tile.TILE_SIZE);

        xfm = vp.getAffineTransform();
    }

    public OOWTile getTile() {
        return tile;
    }

    public BufferedImage getCanvas() {
        return canvas;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setTile(OOWTile tile) {
        this.tile = tile;
    }

    public void setCanvas(BufferedImage canvas) {
        if(this.isInitialized()){
            this.destroy();
        }

        this.canvas = canvas;

        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();

        if(canvasWidth!= tile.TILE_SIZE || canvasHeight!= tile.TILE_SIZE)
            throw new IllegalArgumentException("Invalid image size: "+canvasWidth+" x "+ canvasHeight+". Must be "+tile.TILE_SIZE+" pixel.");
        graphics = canvas.createGraphics();
    }

    public void loadFromFile(File pngFile){
        if(this.isInitialized())
            this.destroy();

        try {
            BufferedImage _canvas = ImageIO.read(pngFile);

            setCanvas(_canvas);

        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to load png file: "+ pngFile, e);
            throw new IllegalArgumentException("Unable to load PNG file.");
        }
    }

    public void saveToFile(File pngFile){
        if(!this.initialized || this.graphics==null)
            throw new IllegalStateException("No canvas image found or rendering context not initialized.");

        ImageFileUtil.saveImageToFile(pngFile, canvas, "PNG");
    }

    public byte[] saveToBuffer(){
        if(!this.initialized || this.graphics==null)
            throw new IllegalStateException("No canvas image found or rendering context not initialized.");

        return ImageFileUtil.saveImageToBytes(canvas, "PNG");
    }

    public void setLineStyle(Color strokeColor, float strokeWidth){
        graphics.setColor(strokeColor);
        graphics.setStroke(new BasicStroke(strokeWidth));
    }

    /**
     *
     * @param strokeColor
     * @param strokeWidth
     * @param xys
     */
    public void renderLineString(Color strokeColor, float strokeWidth, double[] xys){
        graphics.setColor(strokeColor);
        graphics.setStroke(new BasicStroke(strokeWidth));

        renderLineString(xys);
    }

    public void renderLineString(double[] xys){
        int i=0;
        double x= xys[i];
        double y = xys[i+1];
        GeneralPath linestring = new GeneralPath(GeneralPath.WIND_EVEN_ODD, xys.length/2);
        linestring.moveTo(x,y);
        for(i = 1; i < xys.length/2; i++){
            x = xys[i*2];
            y = xys[i*2+1];
            linestring.lineTo(x,y);
        }

        graphics.draw(linestring);
    }

    public static void main(String[] args){
        /*
        int w = 64 * 500;
        int h = 30 * 500;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(10f));
        g2.drawRect(5, 5, w-10, h-10);
        g2.setColor(Color.blue);
        g2.drawLine(5,5, w-10, h-10);
        g2.drawLine(5, h-10, w-10, 5);


        ImageFileUtil.saveImageToFile(new File("D:\\downloads\\big.png"), image, "PNG");
        */

        OOWTile t00 = new OOWTile(0,0);
        t00.print();

        //suitable for testing NorthEast region data
        OOWTile t1 = new OOWTile(14,5);
        t1.print();

        TileRenderingContext tc = new TileRenderingContext(t00);
        tc.setStyleSheet(new HighwayStyleSheet());

        tc.initialize();

        AffineTransform xf = tc.getViewPortTransform();
        Point2D.Double pt1, pt2;
        pt1 = new Point2D.Double(-1.4471533803125564E7,2154935.9150858927);
        pt2 = new Point2D.Double();
        xf.transform(pt1, pt2);
        System.out.println("Transformed point: "+ pt2.getX()+", "+pt2.getY());

        pt1 = new Point2D.Double(-1.4471533803125564E7+2500,2630003);
        pt2 = new Point2D.Double();
        xf.transform(pt1, pt2);
        System.out.println("Transformed point: "+ pt2.getX()+", "+pt2.getY());

    }
}
