
package org.openstreetmap.osmosis.oracle.common;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.*;
import org.openstreetmap.osmosis.core.task.v0_6.*;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlReader;

import oracle.jdbc.pool.OracleDataSource;

public class PBFToDB {
	DBWorker worker;
    private static final int ENTITY_THRESHOLD = 10000;//Threshold to insert in batch
	private static final long COMMIT_THRESHOLD = 1000000;//Threshold to commit changes
	private static Logger LOG = Logger.getLogger(PBFReader.class.getName());
    private HashMap<Integer, String> userMap = new HashMap<>();
    private List<Node> nodeList = new ArrayList<>();
    private List<Way> wayList = new ArrayList<>();
    private Map<Long, List<Long>> wayNodeMap = new HashMap<>();
    private List<Relation> relationList = new ArrayList<>();
    private Map<Long, List<RelationMember>> relationMemberMap = new HashMap<>();
    
    long userCount = 0;
    long nodeCount = 0;
    long commitNodeCount = 0;
    long wayCount = 0;
    long commitWayCount = 0;
    long wayNodeCount = 0;
    long commitWayNodeCount = 0;
    long relationCount = 0;
    long commitRelationCount = 0;
    long relationMemberCount = 0;
    long commitRelationMemberCount = 0;
    long boundCount = 0;
    
    public boolean initDBWorker() {
		String jdbcUrl = "jdbc:oracle:thin:@//adc01dtw.us.oracle.com:1521/orcl.us.oracle.com";
		OracleDataSource ds;
		try {
			ds = new OracleDataSource();
		} catch (SQLException e) {
			System.out.println("Error creating DataSource");
			return false;
		}
		ds.setUser("osm");
		ds.setPassword("oracle");
		ds.setURL(jdbcUrl);
		worker = new DBWorker(ds);
		return true;
    }

    public void readFile() {

        //File file = new File("D:\\osm\\SanFrancisco.osm.pbf"); // the input file
    	File file = new File("D:\\osm\\x_1_y_4.osm.pbf"); // the input file

        Sink sinkImplementation = new Sink() {
        	

            @Override
            public void process(EntityContainer entityContainer) {
                Entity entity = entityContainer.getEntity();
                switch(entity.getType()) {
                case Node:
                	 addNode((Node)entity);
                	break;
                case Way:
                	addWay((Way)entity);
                	break;
                case Relation:
                	addRelation((Relation)entity);
                	break;
                case Bound: //ignore Bound objects
                	boundCount++;
                	break;
                }
            }

            @Override
            public void close() {
                System.out.println("Release!");

            }

            @Override
            public void complete() {
                System.out.println("TOTALS: users=" + userCount + ",nodes=" + nodeCount + ", ways=" + wayCount + ", way nodes=" + wayNodeCount + ", relations=" + relationCount + ", relation members=" + relationMemberCount + ", bounds=" + boundCount);
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
        insertUsers();
        insertNodes();
        insertWays();
        insertWayNodes();
        insertRelations();
        insertRelationMembers();
        commitBatchInsert();

        long t2 = System.currentTimeMillis();
        System.out.println("Time spent: "+ (t2-t1)+" ms.");

    }
    
    private void addUser(OsmUser user) {
    	//Just add distinct ids
    	if (!userMap.containsKey(user.getId())) {
    		userCount++;
    		String userName = "USER_NOT_FOUND";
    		//Make sure we always insert something as the user name
    		if (user.getName() != null && !user.getName().isEmpty()) {
    			userName = user.getName();
    		}
    		userMap.put(user.getId(), userName);
    		if (userMap.size() >= ENTITY_THRESHOLD) {
    			insertUsers();
    		}
    	}
    }

	private void insertUsers() {
		if (userMap.size() > 0) {
			try {
				System.out.println("USERS: " + userCount);
				if (worker.beginBatchInserts(DBEntityType.User)) {
					commitNodeCount = 0;//Reset counter
					//TODO: also reset ways and relations as needed
				}
				worker.batchInsertUsers(userMap);
				userMap.clear();
			} catch (SQLException e) {
				System.out.println("Error inserting users!");
				e.printStackTrace();
			}
		}
	}
    
    private void addNode(Node entity) {
    	addUser(entity.getUser());
    	nodeCount++;
    	nodeList.add(entity);
		if (nodeList.size() >= ENTITY_THRESHOLD) {
			commitNodeCount += nodeList.size();
			insertNodes();
			
			//Check if data needs to be committed
			if (commitNodeCount >= COMMIT_THRESHOLD) {
				commitBatchInsert();
				commitNodeCount = 0;//Reset counter
			}
		}
	}

	private void insertNodes() {
		if (nodeList.size() > 0) {
			try {
				System.out.println("NODES: " + nodeCount);
				if (worker.beginBatchInserts(DBEntityType.Node)) {
					//Reset counters
					commitNodeCount = 0;
					commitWayCount = 0;
					commitRelationCount = 0;
				}
				worker.batchInsertNodes(nodeList);
				
				nodeList.clear();
			} catch (SQLException e) {
				System.out.println("Error inserting nodes!");
				e.printStackTrace();
			}
		}
	}
	
	private void addWay(Way entity) {
    	addUser(entity.getUser());
    	addWayNodes(entity);
    	wayCount++;
    	wayList.add(entity);
		if (wayList.size() >= ENTITY_THRESHOLD) {
			commitWayCount += wayList.size();
			insertWays();
			
			//Check if data needs to be committed
			if (commitWayCount >= COMMIT_THRESHOLD) {
				commitBatchInsert();
				commitWayCount = 0;//Reset counter
			}
		}
	}
	
	private void insertWays() {
		if (wayList.size() > 0) {
			try {
				System.out.println("WAYS: " + wayCount);
				if (worker.beginBatchInserts(DBEntityType.Way)) {
					//Reset counters
					commitNodeCount = 0;
					commitWayCount = 0;
					commitRelationCount = 0;
				}
				worker.batchInsertWays(wayList);
				
				wayList.clear();
			} catch (SQLException e) {
				System.out.println("Error inserting ways!");
				e.printStackTrace();
			}
		}
	}
	
	private void addWayNodes(Way entity) {
		if (entity.getWayNodes() != null && !entity.getWayNodes().isEmpty()) {
			wayNodeCount += entity.getWayNodes().size();
			List<Long> nodeList = new ArrayList<>();
			for (WayNode node:entity.getWayNodes()) {
				nodeList.add(node.getNodeId());
			}
			wayNodeMap.put(entity.getId(), nodeList);
			if (wayNodeMap.size() >= ENTITY_THRESHOLD) {
				commitWayNodeCount += wayNodeMap.size();
				insertWayNodes();
				
				//Check if data needs to be committed
				if (commitWayNodeCount >= (ENTITY_THRESHOLD*5)) {
					commitBatchInsert();
					commitWayNodeCount = 0;//Reset counter
				}
			}
		}
	}

	private void insertWayNodes() {
		if (wayNodeMap.size() > 0) {
			try {
				System.out.println("WAY NODES: " + wayNodeCount);
				if (worker.beginBatchInserts(DBEntityType.WayNode)) {
					//Reset counters
					commitNodeCount = 0;
					commitWayCount = 0;
					commitRelationCount = 0;
					commitWayNodeCount = 0;
				}
				worker.batchInsertWayNodes(wayNodeMap);
				
				wayNodeMap.clear();
			} catch (SQLException e) {
				System.out.println("Error inserting way nodes!");
				e.printStackTrace();
			}
		}
	}
	
	private void addRelation(Relation entity) {
    	addUser(entity.getUser());
    	addRelationMembers(entity);
    	relationCount++;
    	relationList.add(entity);
		if (relationList.size() >= ENTITY_THRESHOLD) {
			commitRelationCount += relationList.size();
			insertRelations();
			
			//Check if data needs to be committed
			if (commitRelationCount >= COMMIT_THRESHOLD) {
				commitBatchInsert();
				commitRelationCount = 0;//Reset counter
			}
		}
	}
	
	private void insertRelations() {
		if (relationList.size() > 0) {
			try {
				System.out.println("RELATIONS: " + relationCount);
				if (worker.beginBatchInserts(DBEntityType.Relation)) {
					//Reset counters
					commitNodeCount = 0;
					commitWayCount = 0;
					commitRelationCount = 0;
				}
				worker.batchInsertRelations(relationList);
				
				relationList.clear();
			} catch (SQLException e) {
				System.out.println("Error inserting relations!");
				e.printStackTrace();
			}
		}
	}
	
	private void addRelationMembers(Relation relation) {
		if (relation.getMembers() != null && !relation.getMembers().isEmpty()) {
			relationMemberCount += relation.getMembers().size();
			relationMemberMap.put(relation.getId(), relation.getMembers());
			if (relationMemberMap.size() >= ENTITY_THRESHOLD) {
				commitRelationMemberCount += relationMemberMap.size();
				insertRelationMembers();
				
				//Check if data needs to be committed
				if (commitRelationMemberCount >= (ENTITY_THRESHOLD*5)) {
					commitBatchInsert();
					commitRelationMemberCount = 0;//Reset counter
				}
			}
		}
	}
	
	private void insertRelationMembers() {
		if (relationMemberMap.size() > 0) {
			try {
				System.out.println("RELATION MEMBERS: " + relationMemberCount);
				if (worker.beginBatchInserts(DBEntityType.RelationMember)) {
					//Reset counters
					commitNodeCount = 0;
					commitWayCount = 0;
					commitWayNodeCount = 0;
					commitRelationCount = 0;
					commitRelationMemberCount = 0;
				}
				worker.batchInsertRelationMembers(relationMemberMap);
				
				relationMemberMap.clear();
			} catch (SQLException e) {
				System.out.println("Error inserting relation members!");
				e.printStackTrace();
			}
		}
	}
	
	private void commitBatchInsert() {
		try {
			worker.endBatchInsert(true);
		} catch (SQLException e) {
			System.out.println("Error committing changes!");
			e.printStackTrace();
		}
	}
    
    public static void main(String... args) {
    	PBFToDB sample = new PBFToDB();
    	if (sample.initDBWorker()) {
    		sample.readFile();
    	}
    }
}
