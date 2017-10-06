package org.openstreetmap.osmosis.oracle.render;

import javax.imageio.ImageIO;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
/*
 * @author lj.qian
 */
public class ImageFileUtil implements Serializable{
    private static final long serialVersionUID = 1L;
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
    
    public static boolean saveBytesToFile(File dest, byte[] imageBytes) {
    	try {
    		BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
    		ImageIO.write(image, "PNG", dest);
    		return true;
    	}
    	catch(Exception ex) {
    		log.log(Level.WARNING, "Unable to write the image to disk.", ex);
            return false;
    	}
    }

    public static byte[] saveImageToBytes(BufferedImage input, String format){
        if(format==null)
            format = "PNG";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            ImageIO.write(input, format, baos);
            baos.flush();
            byte[] res = baos.toByteArray();
            baos.close();

            return res;
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to write the image to byte array.", e);
            return new byte[0];
        }
    }
    
    public static BufferedImage loadImageFromBytes(byte []imageBytes) {
    	try {
            return ImageIO.read(new ByteArrayInputStream(imageBytes));
        } catch (IOException ex) {
            log.log(Level.WARNING, "Unable to read the image from byte array.", ex);
            return null;
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
    
    public static byte[] createFullImage() {
    	int width = OOWTile.NUM_TILES_X*OOWTile.TILE_SIZE;
    	int height = OOWTile.NUM_TILES_Y*OOWTile.TILE_SIZE;
    	BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = newImage.createGraphics();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
        g2.fillRect(0,0,width,height);
		return saveImageToBytes(newImage, "PNG");
    }
}
