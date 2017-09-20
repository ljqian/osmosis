package org.openstreetmap.osmosis.oracle.render;


import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.io.Serializable;

/**
 * A class that implements the viewport transformation.
 * A viewport transformation transfroms the coordinates
 * from user-space (cartisian) into device space.
 *
 * The basic usage of this class is to setup the screen/window area for
 * actual drawing (the view port), the portion of the data set
 * to be displayed (by specifying a view onto the data set), and then
 * simply use the forward() and reverse() for the conversion between the
 * two coordinate systems.
 *
 * @author lj.qian
 * @version 1.0
 */
public class XFViewPort implements Serializable, Cloneable
{
    boolean  strict = false;  // strictly honor user bbox?

    //// section 1:  information about the canvas real-estate

    int	can_xl,     //actual drawing area (the view port)
            can_yl,
            can_xh,
            can_yh;

    int	can_bits;   //plane depth in bits

    int	can_xoff,   //where drawing starts
            can_yoff;

    int scale_axis;  //which axis is scaled to the full extent. 0:X, 1:Y

    //// section 2: transformation information
    double zoom_level;  //current zoom level
    double x_scalef;    //current x-axis scale factor
    //screen_x = data_x*x_scalef,
    double y_scalef;    //current y-axis scale factor
    //screen_y=data_y*y_scalef;
    double rotation;    //current rotation angle (in radians)

  /*
  //// section 3: information about the  data sets
  //// the values are in original unit used by the data set.

  double data_xl, data_yl, //extent of the entire data set
	       data_xh, data_yh;
  */

    double dview_xl, dview_yl, //current view into the data set
            dview_xh, dview_yh;

    boolean    	selecting;	//current user selection information
    int    	select_sx, select_sy, select_ex, select_ey;

    double  scale_data_x=1, scale_data_y=1; //scaling the original data

    AffineTransform  affXF;

    public XFViewPort()
    {
        can_xl = can_yl = 0;
        can_xh = can_yh = 0;
        can_bits = 8;
        can_xoff = can_yoff = 0;
        zoom_level = 1;
        x_scalef = y_scalef = 1;
        rotation = 0;
        selecting = false;
        select_sx = select_sy = select_ex = select_ey = -1;
    }

    public Object clone() throws CloneNotSupportedException
    {
        XFViewPort obj = (XFViewPort) super.clone();
        if(affXF!=null)
            obj.affXF = (AffineTransform) affXF.clone();

        return obj;
    }

    public void   setStrict(boolean v)
    {
        strict = v;
    }

    public boolean  getStrict()
    {
        return strict;
    }

    /**
     * convert a native data coordinate (source point) into device
     * space (dest point).
     * @param src	source point (in native space)
     * @param dst	dest point (in device space)
     * @return  returns true if the conversion is success;
     *		otherwise return false (such as when the native coordinates
     *		are out of the current data view).
     */

    public boolean   forward(Point2D src, Point2D dst)
    {
        dst.setLocation( (src.getX() - dview_xl) * x_scalef + can_xoff,
                (dview_yh - src.getY()) * y_scalef + can_yoff  );
        return true;
    }


    /**
     * convert a source (device) point back into native data point
     * @param src	source point in device space
     * @param dst	dest point in user/native space
     * @return 		returns true if the conversion is success;
     *		otherwise return false
     */

    public boolean   inverse(Point2D src, Point2D dst)
    {
        dst.setLocation((double)(src.getX()-can_xoff) / x_scalef + dview_xl,
                dview_yh - (double)(src.getY()-can_yoff) / y_scalef );
        return true;
    }

    public boolean   inverse(int[] src, double[] dst)
    {
        dst[0] = (double)(src[0]-can_xoff) / x_scalef + dview_xl;
        dst[1] = dview_yh -  (double)(src[1]-can_yoff) / y_scalef;
        return true;
    }



    /**
     *
     */
    public double  getScaleFactor()
    {
        return x_scalef;
    }

    /**
     * set the rendering area or viewport on the device;
     */
    public void   setDeviceView(int xlow, int ylow, int xhi, int yhi)
    {
        can_xl = xlow; can_yl = ylow;
        can_xh = xhi;  can_yh = yhi;
        if(strict)
            setupXForm2();
        else
            setupXForm();
    }

    public void   setDeviceView(Rectangle2D env)
    {
        setDeviceView((int)env.getMinX(), (int)env.getMinY(),
                (int)env.getMaxX(), (int)env.getMaxY());
    }

    /**
     * set up the scale related transformation. Mainly the x and y-axis
     * scale factors and transitions. Essentially this sets up the
     * transformation from the data view (2D real-world coordindates)
     * to the view-port (screen coordinates).
     */
    public void   setupXForm()
    {
        double x_inc = (dview_xh-dview_xl)*scale_data_x, xf;
        double y_inc = dview_yh-dview_yl, yf;

        xf = (can_xh-can_xl-2)/x_inc;  //2 is for minimum gap
        yf = (can_yh-can_yl-2)/y_inc;

        if (xf <= yf ) {
            x_scalef = y_scalef = xf;
            scale_axis = 0;
            can_xoff = can_xl+1;
            can_yoff = (int)(((can_yh-can_yl+1) - (y_inc)*xf)/2) + can_yl+1;
        } else {
            x_scalef = y_scalef = yf;
            scale_axis = 1;
            can_xoff = (int)(((can_xh-can_xl+1) - (x_inc)*yf)/2) + can_xl+1;
            can_yoff = can_yl+1;
        }

        setupAffineTransform();
    }

    public void   setupAffineTransform()
    {
        int sx = can_xl, sy = can_yl, ex = can_xh, ey = can_yh;
        double rw = (dview_xh-dview_xl);
        double rh = (dview_yh-dview_yl);

        double scale = 0.0;
        double scaleX = (ex-sx)/rw;
        double scaleY = (ey-sy)/rh;
        if(scale_data_x!=1.0) scale = scaleY; //need local cosine adjustment
        else
            scale = scaleX>scaleY? scaleY:scaleX;  //use the smaller scale

        //System.out.println("scaleX="+scaleX+", scaleY="+scaleY);

        double tx = (sx - dview_xl*scale*scale_data_x);
        double ty = (sy + dview_yl*scale);
        //adjust the translate so that the transformed shape is
        //in the center. Also note that shape will be flipped along y axis.
        tx += ( (ex-sx-rw*scale*scale_data_x)/2.0f );
        ty += ( ey-sy-(ey-sy-rh*scale)/2.0f );

        //-scale for y axis since the shape needs to be flipped.
        affXF = new AffineTransform(scale*scale_data_x,0,0,-scale,tx,ty);
    }

    /**
     * this method is called if strict = true.
     */
    public void   setupXForm2()
    {
        double x_inc = (dview_xh-dview_xl)*scale_data_x;
        double y_inc = dview_yh-dview_yl, yf;

        x_scalef = (can_xh-can_xl-2)/x_inc;  //2 is for minimum gap
        y_scalef = (can_yh-can_yl-2)/y_inc;
        scale_axis = 0;

        can_xoff = can_xl+1;
        can_yoff = can_yl+1;

        setupAffineTransform2();
    }


    /**
     * this method is called if strict=true.
     */
    public void   setupAffineTransform2()
    {
        int sx = can_xl, sy = can_yl, ex = can_xh, ey = can_yh;
        double rw = (dview_xh-dview_xl);
        double rh = (dview_yh-dview_yl);

        double scaleX = (ex-sx)/rw;
        double scaleY = (ey-sy)/rh;

        //local adjustment is disabled here.
        double tx = (sx - dview_xl*scaleX);
        double ty = (sy + dview_yl*scaleY);

        //adjust the translate so that the transformed shape is
        //in the center. Also note that shape will be flipped along y axis.
        tx += ( (ex-sx-rw*scaleX)/2.0f );
        ty += ( ey-sy-(ey-sy-rh*scaleY)/2.0f );

        //-scale for y axis since the shape needs to be flipped.
        affXF = new AffineTransform(scaleX,0,0,-scaleY,tx,ty);
    }

    public AffineTransform  getAffineTransform()
    {
        return affXF;
    }

    public void    setDataScaleOnX(double f) { scale_data_x = f; }
    public void    setDataScaleOnY(double f) { scale_data_y = f; }

    /**
     * setup a new view into the data set. This will affect the
     * scale factors.  A view is a window inside the universal bounding
     * box of the data set (set by setDataExtent()).
     * @param xl	lower corner x coordinate
     * @param yl	lower corner y coordinate
     * @param xh	higher corner x coordinate
     * @param yh	higher corner y coordinate
     * @return  returns true if success; otherwise returns false.
     */
    public boolean   setDataView(double xl, double yl, double xh, double yh)
    {

        if( (xh-xl)<0 || (yh-yl)<0) {
            //give some warning!!!
            return false;
        }

        if(xl==xh && yl==yh) //a single point
        {
            xl -= Math.abs(xl)*0.005;
            yl -= Math.abs(yl)*0.005;
            xh += Math.abs(xh)*0.005;
            yh += Math.abs(yl)*0.005;
        }

        dview_xl = xl; dview_yl = yl;
        dview_xh = xh; dview_yh = yh;
        //cout<<"new data view: ("<<xl<<","<<yl<<","<<xh<<","<<yh<<")"<<endl;
        if(strict)
            setupXForm2();
        else
            setupXForm();

        return true;
    }

    public void   setDataView(Rectangle2D env)
    {
        setDataView(env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY());
    }


    public int   getDeviceMinX() { return can_xl; }
    public int   getDeviceMinY() { return can_yl; }
    public int   getDeviceMaxX() { return can_xh; }
    public int   getDeviceMaxY() { return can_yh; }
    public int   getDeviceOffsetX() { return can_xoff; }
    public int   getDeviceOffsetY() { return can_yoff; }
    public int   getScaleAxis() { return scale_axis; }

    public void	  printAll()
    {
        System.out.println("XFViewPort:");
        System.out.println("device_view=(" + (int)can_xl + "," + (int)can_yl +
                ", " + (int)can_xh + "," + (int)can_yh + ")");
        //System.out.println("dataextent_view=(" + data_xl + "," + data_yl +
        //	                 ", " + data_xh + "," + data_yh + ")");
        System.out.println("data_view=(" + dview_xl + "," + dview_yl +
                ", " + dview_xh + "," + dview_yh + ")");
        System.out.println("dev_off=(" + can_xoff + "," + can_yoff + ")");
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("XFViewPort:").append("device_view=(" + (int)can_xl + "," + (int)can_yl +
                ", " + (int)can_xh + "," + (int)can_yh + ") ");
        //sb.append("dataextent_view=(" + data_xl + "," + data_yl +
        //          ", " + data_xh + "," + data_yh + ") ");
        sb.append("data_view=(" + dview_xl + "," + dview_yl +
                ", " + dview_xh + "," + dview_yh + ") ");
        sb.append("dev_off=(" + can_xoff + "," + can_yoff + ")\n"+affXF);
        return sb.toString();
    }
}
