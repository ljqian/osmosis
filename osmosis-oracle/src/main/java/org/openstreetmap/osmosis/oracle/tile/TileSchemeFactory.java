package org.openstreetmap.osmosis.oracle.tile;


import java.util.HashMap;
import java.util.Map;

/**
 * A factory class that returns an instance of TileScheme based on the given
 * name.
 *
 * @author lqian
 */
public class TileSchemeFactory {
    private static Map<String, TileScheme> schemes = new HashMap<>();
    static{
        registerBuiltinTileSchemes();
    }

    private static void registerBuiltinTileSchemes(){
        GoogleTileScheme gs = new GoogleTileScheme();
        schemes.put("google", gs);
        schemes.put("openstreetmap", gs);

        TMSTileScheme ts = new TMSTileScheme();
        schemes.put("tms", ts);
    }

    /**
     * Factory method that returns a specific TileScheme
     * sub-class instance using a name. Currently supported names are:
     * <UL>
     *  <LI> "google"
     *  <LI> "tms"
     *  <LI> "openstreetmap"
     * </UL>
     *
     * @param name
     * @return
     */
    public static TileScheme getTileScheme(String name){
        return schemes.get(name);
    }

    /**
     * Method for registering a custom tile scheme.
     *
     * @param name
     * @param customTileScheme
     */
    public static void registerTileScheme(String name, TileScheme customTileScheme){
        schemes.put(name, customTileScheme);
    }

}
