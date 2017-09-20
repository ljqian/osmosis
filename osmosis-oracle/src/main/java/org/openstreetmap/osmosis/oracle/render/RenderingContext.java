package org.openstreetmap.osmosis.oracle.render;


import java.awt.*;
/*
 * @author lj.qian
 */
public interface RenderingContext {

    public StyleSheet getStyleSheet();

    public Graphics2D getGraphics();

    public void initialize();

    public void destroy();
}
