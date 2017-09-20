package org.openstreetmap.osmosis.oracle.render;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.*;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.oracle.common.MapPoint;
import org.openstreetmap.osmosis.oracle.common.PBFReader;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlReader;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PBFRenderer {
    private static Logger LOG = Logger.getLogger(PBFRenderer.class.getName());

    private static int DEFAULT_INIT_CAPACITY = 5000000;

    private HashMap<Long, MapPoint> nodeMap = new HashMap<>(DEFAULT_INIT_CAPACITY);
    private TileRenderingContext tc;

    public PBFRenderer(OOWTile tile, File file){
        tc = new TileRenderingContext(tile);
        tc.initialize();

        tc.setLineStyle(Color.white, 1.0f);

        readFile(tc.getStyleSheet(), file);
    }

    public void readFile(StyleSheet sheet, File file) {
        StyleSheet highwayStyleSheet = sheet;

        Sink sinkImplementation = new Sink() {
            long nodeCount = 0;
            long wayCount = 0;
            long wayRenderCount = 0;
            long relationCount = 0;
            long wayNodeLookupCount = 0;
            long missingWayNodeCount = 0;

            @Override
            public void process(EntityContainer entityContainer) {
                Entity entity = entityContainer.getEntity();
                if (entity instanceof Node) {
                    //do something with the node
                    nodeCount++;

                    Node nd = (Node) entity;
                    MapPoint pt = new MapPoint(nd.getLongitude(), nd.getLatitude());
                    nodeMap.put(nd.getId(), pt);
                }
                else if (entity instanceof Way) {
                    //do something with the way
                    wayCount++;

                    Way way = (Way) entity;
                    Collection<Tag> tagSet = way.getTags();

                    long wayId = way.getId();
                    List<WayNode> nodes = way.getWayNodes();
                    double[] xys = new double[nodes.size()*2];
                    int i=0;
                    for(WayNode wn : nodes){
                        long nid = wn.getNodeId();
                        MapPoint pt = nodeMap.get(nid);
                        if(pt==null){
                            missingWayNodeCount ++;
                        } else{
                            double[] mercatorPt = WorldMercatorUtils.lonLatToMeters(pt.getX(), pt.getY());
                            xys[i*2] = mercatorPt[0];
                            xys[i*2+1] = mercatorPt[1];
                        }
                        i++;
                    }

                    //for image read-ability, we are going to show only  10% of the streets ...
                    boolean shouldRender =highwayStyleSheet.applicable(tagSet);
                    if(shouldRender /* && wayCount % 2 ==0*/){
                        tc.renderLineString(xys);
                        wayRenderCount++;
                    }
                }
                else if (entity instanceof Relation) {
                    //do something with the relation
                    relationCount++;
                }
            }

            @Override
            public void close() {
                System.out.println("Release!");

                System.out.println("Final nodeMap size: "+ nodeMap.size());

                //release memory
                nodeMap.clear();
                nodeMap = null;

                String parentFolder = file.getParent();
                String saveFileName = parentFolder+File.separator+"output.png";

                tc.saveToFile(new File(saveFileName));
                System.out.println("Image saved: "+ saveFileName);
            }

            @Override
            public void complete() {
                System.out.println("TOTALS: nodes=" + nodeCount + ", ways=" + wayCount + ", relations=" + relationCount +
                        " wayNodeLookups="+ wayNodeLookupCount + ", missingWayNodes="+ missingWayNodeCount+", rendered way="+wayRenderCount);
            }

            @Override
            public void initialize(Map<String, Object> map) {
                System.out.println("Initialize!");
            }
        };

        boolean pbf = false;
        CompressionMethod compression = CompressionMethod.None;

        if (file.getName().endsWith(".pbf")) {
            pbf = true;
        }
        else if (file.getName().endsWith(".gz")) {
            compression = CompressionMethod.GZip;
        }
        else if (file.getName().endsWith(".bz2")) {
            compression = CompressionMethod.BZip2;
        }

        RunnableSource reader = null;

        long t1 = System.currentTimeMillis();

        if (pbf) {
            try {
                reader = new crosby.binary.osmosis.OsmosisReader(
                        new FileInputStream(file));
            }
            catch (FileNotFoundException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        else {
            reader = new XmlReader(file, false, compression);
        }

        reader.setSink(sinkImplementation);

        Thread readerThread = new Thread(reader);
        readerThread.start();
        System.out.println("Processing started...");

        while (readerThread.isAlive()) {
            try {
                readerThread.join();
            }
            catch (InterruptedException e) {
                /* do nothing */
            }
        }

        long t2 = System.currentTimeMillis();

        System.out.println("Time spent: "+ (t2-t1)+" ms.");

    }

    public static void main(String... args) {
        int tileX = 14, tileY = 5; //for boston area
        String fileName = "/Users/lqian/oracle/data/boston_massachusetts.osm.pbf";
        File file = null;

        for(String arg : args){
            if(arg.startsWith("x=")){
                tileX = Integer.valueOf(arg.substring(2));
                System.out.println("Input tile index x="+ tileX);
            }

            if(arg.startsWith("y=")){
                tileY = Integer.valueOf(arg.substring(2));
                System.out.println("Input tile index y="+ tileY);
            }

            if(arg.startsWith("file=")){
                fileName = arg.substring(5);
                System.out.println("Input file="+ fileName);
            }
        }

        //suitable for testing NorthEast region data
        OOWTile tile = new OOWTile(tileX,tileY);
        tile.print();

        file = new File(fileName);

        PBFRenderer sample = new PBFRenderer(tile, file);
    }
}
