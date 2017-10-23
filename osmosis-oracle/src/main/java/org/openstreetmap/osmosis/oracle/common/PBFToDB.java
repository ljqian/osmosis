
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
	private static Logger LOG = Logger.getLogger(PBFToDB.class.getName());
	private DBWorker worker;
    private static final int ENTITY_THRESHOLD = 10000;//Threshold to insert in batch
	private static final long COMMIT_THRESHOLD = 1000000;//Threshold to commit changes
    private HashMap<Integer, String> userMap = new HashMap<>();
    private List<Node> nodeList = new ArrayList<>();
    private List<Way> wayList = new ArrayList<>();
    private Map<Long, List<Long>> wayNodeMap = new HashMap<>();
    private List<Relation> relationList = new ArrayList<>();
    private Map<Long, List<RelationMember>> relationMemberMap = new HashMap<>();
    
    long boundCount = 0;
    long userCount = 0;
    long nodeCount = 0;
    long wayCount = 0;
    long wayNodeCount = 0;
    long relationCount = 0;
    long relationMemberCount = 0;
    
    long commitNodeCount = 0;
    long commitWayCount = 0;
    long commitWayNodeCount = 0;
    long commitRelationCount = 0;
    long commitRelationMemberCount = 0;
        
    long skippedNodes = 0;
    long skippedWays = 0;
    long skippedRelations = 0;
    
    public PBFToDB(DBWorker worker) {
    	this.worker = worker;
    }
    
    //TEST METHOD
    public boolean initDBWorker() {
		String jdbcUrl = "jdbc:oracle:thin:@//adc01dtw.us.oracle.com:1521/orcl.us.oracle.com";
		OracleDataSource ds;
		try {
			ds = new OracleDataSource();
		} catch (SQLException e) {
			LOG.severe("Error creating DataSource.");
			return false;
		}
		ds.setUser("osm");
		ds.setPassword("oracle");
		ds.setURL(jdbcUrl);
		worker = new DBWorker(ds);
		return true;
    }

    public void readFile(PBFConfiguration config) {
    	File file = config.getPBFFile();

        Sink sinkImplementation = new Sink() {
        	

            @Override
            public void process(EntityContainer entityContainer) {
                Entity entity = entityContainer.getEntity();
                EntityType type = entity.getType();
                switch(type) {
                case Node:
                	if (config.isInsertable(type)) {
                		addNode((Node)entity);
                	}
                	else {
                		//LOG.finest("Skipping node.");
                		skippedNodes++;
                	}
                	break;
                case Way:
                	if (config.isInsertable(type)) {
                		addWay((Way)entity);
                	}
                	else {
                		//LOG.finest("Skipping way.");
                		skippedWays++;
                	}
                	break;
                case Relation:
                	if (config.isInsertable(type)) {
                		addRelation((Relation)entity);
                	}
                	else {
                		//LOG.finest("Skipping relation.");
                		skippedRelations++;
                	}
                	break;
                case Bound: //ignore Bound objects
                	boundCount++;
                	break;
                }
            }

            @Override
            public void close() {
            	LOG.info("Release!");

            }

            @Override
            public void complete() {
            	LOG.info("TOTALS: users=" + userCount + ",nodes=" + nodeCount + ", ways=" + wayCount + ", way nodes=" + wayNodeCount + ", relations=" + relationCount + ", relation members=" + relationMemberCount + ", bounds=" + boundCount);
            }

            @Override
            public void initialize(Map<String, Object> map) {
            	LOG.info("Initialize!");
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
        LOG.info("Processing started...");

        while (readerThread.isAlive()) {
            try {
                readerThread.join();
            }
            catch (InterruptedException e) {
                /* do nothing */
            }
        }
        
        //Insert everything
        insertUsers();
        insertNodes();
        insertWays();
        insertWayNodes();
        insertRelations();
        insertRelationMembers();
        commitBatchInsert();

        long t2 = System.currentTimeMillis();
        LOG.info("Time spent: "+ (t2-t1)+" ms.");

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
				LOG.fine("USERS: " + userCount);
				if (worker.beginBatchInserts(DBEntityType.User)) {
					//Reset counter
					resetCommitCounters();
				}
				worker.batchInsertUsers(userMap);
				userMap.clear();
			} catch (SQLException e) {
				LOG.severe("Error inserting users! " + e.getMessage());
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
				resetCommitCounters();//Reset counter
			}
		}
	}

	private void insertNodes() {
		if (nodeList.size() > 0) {
			try {
				LOG.fine("NODES: " + nodeCount);
				if (worker.beginBatchInserts(DBEntityType.Node)) {
					//Reset counters
					resetCommitCounters();
				}
				worker.batchInsertNodes(nodeList);
				
				nodeList.clear();
			} catch (SQLException e) {
				LOG.severe("Error inserting nodes! " + e.getMessage());
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
				resetCommitCounters();//Reset counter
			}
		}
	}
	
	private void insertWays() {
		if (wayList.size() > 0) {
			try {
				LOG.fine("WAYS: " + wayCount);
				if (worker.beginBatchInserts(DBEntityType.Way)) {
					//Reset counters
					resetCommitCounters();
				}
				worker.batchInsertWays(wayList);
				
				wayList.clear();
			} catch (SQLException e) {
				LOG.severe("Error inserting ways! " + e.getMessage());
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
					resetCommitCounters();//Reset counter
				}
			}
		}
	}

	private void insertWayNodes() {
		if (wayNodeMap.size() > 0) {
			try {
				LOG.fine("WAY NODES: " + wayNodeCount);
				if (worker.beginBatchInserts(DBEntityType.WayNode)) {
					//Reset counters
					resetCommitCounters();
				}
				worker.batchInsertWayNodes(wayNodeMap);
				
				wayNodeMap.clear();
			} catch (SQLException e) {
				LOG.severe("Error inserting way nodes! " + e.getMessage());
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
				resetCommitCounters();//Reset counter
			}
		}
	}
	
	private void insertRelations() {
		if (relationList.size() > 0) {
			try {
				LOG.fine("RELATIONS: " + relationCount);
				if (worker.beginBatchInserts(DBEntityType.Relation)) {
					//Reset counters
					resetCommitCounters();
				}
				worker.batchInsertRelations(relationList);
				
				relationList.clear();
			} catch (SQLException e) {
				LOG.severe("Error inserting relations! " + e.getMessage());
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
					resetCommitCounters();//Reset counters
				}
			}
		}
	}
	
	private void insertRelationMembers() {
		if (relationMemberMap.size() > 0) {
			try {
				LOG.fine("RELATION MEMBERS: " + relationMemberCount);
				if (worker.beginBatchInserts(DBEntityType.RelationMember)) {
					//Reset counters
					resetCommitCounters();
				}
				worker.batchInsertRelationMembers(relationMemberMap);
				
				relationMemberMap.clear();
			} catch (SQLException e) {
				LOG.severe("Error inserting relation members! " + e.getMessage());
			}
		}
	}
	
	private void commitBatchInsert() {
		try {
			worker.endBatchInsert(true);
		} catch (SQLException e) {
			LOG.severe("Error committing changes! " + e.getMessage());
		}
	}
	
	private void resetCommitCounters() {
		commitNodeCount = 0;
		commitWayCount = 0;
		commitWayNodeCount = 0;
		commitRelationCount = 0;
		commitRelationMemberCount = 0;
	}
    
    public static void main(String... args) {
    	PBFToDB sample = new PBFToDB(null);
    	if (sample.initDBWorker()) {
    		//PBFConfiguration config = new PBFConfiguration(new File("D:\\osm\\x_1_y_4.osm.pbf"), EntityType.Bound, EntityType.Node, EntityType.Way, EntityType.Relation);
    		PBFConfiguration config = new PBFConfiguration(new File("D:\\osm\\x_2_y_4.osm.pbf"), EntityType.Bound, EntityType.Way);
    		sample.readFile(config);
    	}
    }
}
