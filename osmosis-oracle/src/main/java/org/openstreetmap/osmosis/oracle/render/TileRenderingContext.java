package org.openstreetmap.osmosis.oracle.render;

import org.openstreetmap.osmosis.oracle.tile.Tile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Logger;

public class TileRenderingContext implements RenderingContext {
    private static Logger log = Logger.getLogger(TileRenderingContext.class.getName());

    private static final int CANVAS_MAX_WIDTH = 4000,
                             CANVAS_MAX_HEIGHT = 4000;

    private static final int CANVAS_DEFAULT_WIDTH = 256,
                             CANVAS_DEFAULT_HEIGHT = 256;

    private Tile tile;
    private Graphics2D graphics;
    private BufferedImage canvas;
    private int canvasWidth, canvasHeight; //in pixel
    private boolean initialized = false;
    private StyleSheet styleSheet;

    /**
     * Creates a new rendering context that's suitable for drawing geospatial data
     * for the given tile. The drawing will be done to a blank canvas (image) with
     * the specified dimension.
     *
     * @param tile The Tile object for which this rendering context is for.
     * @param canvasWidthPx The width of the canvas in pixel
     * @param canvasHeightPx The height of the canvas in pixel
     */
    public TileRenderingContext(Tile tile, int canvasWidthPx, int canvasHeightPx){
        this.tile = tile;

        if(canvasWidthPx<0 || canvasWidthPx> CANVAS_MAX_WIDTH){
            log.warning("Invalid canvas width: "+ canvasWidthPx);
            canvasWidthPx = CANVAS_DEFAULT_WIDTH;
        }

        if(canvasHeightPx<0 || canvasHeightPx > CANVAS_MAX_HEIGHT){
            log.warning("Invalid canvas height: "+ canvasHeightPx);
            canvasHeightPx = CANVAS_DEFAULT_HEIGHT;
        }

        canvasWidth = canvasWidthPx;
        canvasHeight = canvasHeightPx;
        canvas = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
        graphics = canvas.createGraphics();
    }

    /**
     * Creates a new rendering context that will draw geospatial contents to the
     * supplied canvas image for the given tile.
     *
     * @param tile
     * @param canvasImage
     */
    public TileRenderingContext(Tile tile, BufferedImage canvasImage){
        this.tile = tile;

        canvas = canvasImage;
        canvasWidth = canvasImage.getWidth();
        canvasHeight = canvasImage.getHeight();
        graphics = canvas.createGraphics();
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
    }

    /**
     * User of this RenderingContext must call this method to initialize it.
     *
     * A rendering context cannot be passed into any rendering operation before it's
     * initialized using this method.
     */
    @Override
    public void initialize(){
        if(graphics==null){
            log.warning("No Graphics2D object found. This RenderingContext cannot be initialized.");
            return;
        }

        if(styleSheet==null){
            log.warning("No StyleSheet object found. This RenderingContext cannot be initialized.");
            return;
        }

        //set affine transform

        initialized = true;
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

    private void setAffineTransform(){
        //this method ensures correct geometry transformation from the tile-scheme's coordinate space
        // to the canvas image's pixel space.


    }

    public static void main(String[] args){
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
    }
}
