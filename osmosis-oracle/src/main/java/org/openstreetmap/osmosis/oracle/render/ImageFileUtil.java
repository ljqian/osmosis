package org.openstreetmap.osmosis.oracle.render;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageFileUtil implements Serializable{
    private static Logger log = Logger.getLogger(ImageFileUtil.class.getName());

    public static boolean saveImageToFile(File dest, BufferedImage input, String format){
        if(format==null)
            format = "PNG";
        try {
            ImageIO.write(input, format, dest);
            return true;
        }catch(Exception ex){
            log.log(Level.WARNING, "Unable to write the image to disk.", ex);
            return false;
        }
    }

    public static BufferedImage loadImageFromFile(File input){
        try {
            return ImageIO.read(input);
        } catch (IOException ex) {
            log.log(Level.WARNING, "Unable to read the image from disk file:"+input.getPath(), ex);
            return null;
        }
    }


}
