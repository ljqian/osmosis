/*
 * @author lj.qian
 */
package org.openstreetmap.osmosis.oracle.render;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides the rendering context suitable for
 * drawing geometries to a specific Tile.
 */
public class TileRenderingContext implements RenderingContext, Serializable {
    private static final long serialVersionUID = 1L;

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

        canvasWidth = OOWTile.TILE_SIZE;
        canvasHeight = OOWTile.TILE_SIZE;

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
        vp.setDeviceView(0,0, OOWTile.TILE_SIZE, OOWTile.TILE_SIZE);

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

        if(canvasWidth!= OOWTile.TILE_SIZE || canvasHeight!= OOWTile.TILE_SIZE)
            throw new IllegalArgumentException("Invalid image size: "+canvasWidth+" x "+ canvasHeight+". Must be "+OOWTile.TILE_SIZE+" pixel.");
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
    public boolean renderLineString(Color strokeColor, float strokeWidth, double[] xys, Rectangle2D.Double bbox){
        graphics.setColor(strokeColor);
        graphics.setStroke(new BasicStroke(strokeWidth));

        return renderLineString(xys, bbox);
    }

    /**
     * Renders a linestring whose coordinates are defined in the double array (coordinates stored as
     * x0,y0,x1,y1, ... xn,yn).
     *
     * @param xys  The coordinates of the linestring
     * @param bbox The bounding box of current rendering context (clip window).
     * @return true if the linestring is actually rendered to the canvas, false otherwise.
     */
    public boolean renderLineString(double[] xys, Rectangle2D.Double bbox){
        if(xys==null || xys.length<4)
            return false;

        int i=0;
        //double xMin, yMin ,
        //       xMax, yMax ;

        double x= xys[i];
        double y = xys[i+1];

        GeneralPath linestring = new GeneralPath(GeneralPath.WIND_EVEN_ODD, xys.length/2);
        linestring.moveTo(x,y);

        //xMin = x;
        //yMin = y;
        //xMax = x;
        //yMax = y;

        for(i = 1; i < xys.length/2; i++){
            x = xys[i*2];
            y = xys[i*2+1];
            linestring.lineTo(x,y);

           /* if(xMin > x)
                xMin = x;
            if(yMin > y)
                yMin = y;
            if(xMax < x)
                xMax = x;
            if(yMax < y)
                yMax = y;*/

        }

        //Rectangle2D.Double myBBox = new Rectangle2D.Double(xMin, yMin, xMax - xMin, yMax - yMin);

        //TODO: when the linestring overlaps tile bbox, clip the linestring and render (instead of skipping it).
        Shape drawShape = clipLineString(bbox, linestring, linestring.getBounds2D());
        if(bbox.contains(drawShape.getBounds2D())) {
            graphics.draw(drawShape);
            return true;
        }
        else
            return false; //did not render
    }
    
    public static Shape clipLineString(Rectangle2D window, Shape old, Rectangle2D shapeMBR)
    {
  	  if(old==null)
  		  return old;
  	  
  	  if(shapeMBR==null)
  		  shapeMBR = old.getBounds();

  	  //optimization: if old.mbr is completely within window, do nothing
  	  if(window.contains(shapeMBR))
  	      return old;
  	      
  	    PathIterator pi = old.getPathIterator(null);
  	    if(pi.isDone()) return old;

  	    float sx=0, sy=0;
  	    float ex=0, ey=0;
  	    float mx=0, my=0;
  	    float[] seg = new float[6];
  	    double[] in = new double[4];
  	    double[] out= new double[4];
  	    GeneralPath   newShp = new GeneralPath(pi.getWindingRule());
  	    int segType = 0;
        int lineToCnt = 0 ;
  	    
  	    while(!pi.isDone())
  	    {
  	      segType = pi.currentSegment(seg);
  	      switch(segType)
  	      {
  	        case PathIterator.SEG_MOVETO:
              lineToCnt = 0 ;
  	          sx = seg[0]; sy = seg[1];
  	          mx=sx; my=sy;
  	          //if(window.contains(seg[0], seg[1]/*bug in contains(x,y)*/) )
  	          if(contains(window, seg[0], seg[1]))
  	            newShp.moveTo(seg[0], seg[1]);
  	          break;

  	        case PathIterator.SEG_LINETO:
              lineToCnt ++ ;
  	          ex = seg[0];  ey = seg[1];
  	          in[0] = sx; in[1] = sy; in[2] = ex; in[3] = ey;
  	          if(clipLineSegment(in, window, out))
  	          {
  	            if(out[0]!=sx || out[1]!=sy)
  	              newShp.moveTo((float)out[0], (float)out[1]);
  	            newShp.lineTo((float)out[2], (float)out[3]);
  	          }

  	          sx = ex;   sy = ey;
  	          break;
  	        case PathIterator.SEG_CLOSE:
              if(lineToCnt<=0)
                break ;
              lineToCnt=0 ;
  	        	//treat polygon as polyline.
  	        	sx = ex; sy = ey;
  	        	ex = mx; ey = my;
  	        	if(clipLineSegment(in, window, out))
  	            {
  	              if(out[0]!=sx || out[1]!=sy)
  	                newShp.moveTo((float)out[0], (float)out[1]);
  	              newShp.lineTo((float)out[2], (float)out[3]);
  	            }
  	        	break;
  	        case PathIterator.SEG_CUBICTO:
  	        	/*
  	        	Rectangle2D mbr = curveMBR(seg, 3);
  	        	if(!mbr.intersects(window))
  	        	{
  	        		//simply skip the curve segment
  	        	}
  	        	else
  	        	{
  	        		
  	        		//do not attempt to clip it; just add to the new shape
  	        		log.finer("clipLineString: cubic curve not supported.");
  	        		return old;
  	        	}
  	        	break;
  	        	*/
  	        case PathIterator.SEG_QUADTO:
  	        default:	          
  	          log.finer("clipLineString: quadratic or cubic curve not supported.");
  	          return old;
  	      }
  	      pi.next();
  	    }

  	    return newShp;	  
    }
    
    private final static boolean contains(Rectangle2D win, double x, double y) {
      return  ( x<=win.getMaxX() && y<=win.getMaxY() && x>=win.getMinX() && y>= win.getMinY() );
    }
    
	public final static boolean clipLineSegment(double[] line, Rectangle2D clip, double[] out) {
		// Cohen-Sutherland line-clipping algorithm
		int c0, c1, c;
		double x, y, x0 = line[0], y0 = line[1], x1 = line[2], y1 = line[3];

		c0 = clip.outcode(x0, y0);
		c1 = clip.outcode(x1, y1);

		while (true) {
			/* trivial accept: both ends in rectangle */
			if ((c0 | c1) == 0) {
				out[0] = x0;
				out[1] = y0;
				out[2] = x1;
				out[3] = y1;
				return true;
			}

			/* trivial reject: AND does not equal zero */
			if ((c0 & c1) != 0)
				return false;

			/* normal case: clip from the end outside rectangle */
			c = (c0 != 0) ? c0 : c1;
			if ((c & Rectangle2D.OUT_BOTTOM) != 0) /* BOTTOM has max Y value */
			{
				x = x0 + (x1 - x0) * (clip.getMaxY() - y0) / (y1 - y0);
				y = clip.getMaxY();
			} else if ((c & Rectangle2D.OUT_TOP) != 0) /* TOP has min Y value */
			{
				x = x0 + (x1 - x0) * (clip.getMinY() - y0) / (y1 - y0);
				y = clip.getMinY();
			} else if ((c & Rectangle2D.OUT_RIGHT) != 0) {
				x = clip.getMaxX();
				y = y0 + (y1 - y0) * (clip.getMaxX() - x0) / (x1 - x0);
			} else {
				x = clip.getMinX();
				y = y0 + (y1 - y0) * (clip.getMinX() - x0) / (x1 - x0);
			}

			/* set new end point and iterate */
			if (c == c0) {
				x0 = x;
				y0 = y;
				c0 = clip.outcode(x0, y0);
			} else {
				x1 = x;
				y1 = y;
				c1 = clip.outcode(x1, y1);
			}
		} // while
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
