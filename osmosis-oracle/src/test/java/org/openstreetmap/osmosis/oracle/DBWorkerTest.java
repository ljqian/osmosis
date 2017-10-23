package org.openstreetmap.osmosis.oracle;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.oracle.common.DBEntityType;
import org.openstreetmap.osmosis.oracle.common.DBWorker;

import oracle.jdbc.pool.OracleDataSource;
/**
 * Created by romercad on 10/19/17.
 */
public class DBWorkerTest {
	private static DBWorker worker;
	
	@Before
	public void setUp() {
		try {
			String jdbcUrl = "jdbc:oracle:thin:@//adc01dtw.us.oracle.com:1521/orcl.us.oracle.com";
	    	OracleDataSource ds;
	    	//OracleDataSource rds = OracleDataSourceFactory.getOracleDataSource();
	    	ds = new OracleDataSource();
	    	ds.setUser("osm");
	    	ds.setPassword("oracle");
			ds.setURL(jdbcUrl);
			worker = new DBWorker(ds);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    @Test
    public void testInsertNode() {
    	try {
    		Assert.assertFalse("No data should be commited before inserting.", worker.beginBatchInserts(DBEntityType.Node));
    		List<Tag> tags = new ArrayList<>();
    		tags.add(new Tag("key", "value"));
    		List<Node> nodeList = new ArrayList<>();
    		Node node = new Node(1, 1, new Date(), new OsmUser(444, "osm_user"), 555, tags, 22,33);
    		nodeList.add(node);
    		worker.batchInsertNodes(nodeList);
    		worker.endBatchInsert(true);
    		Assert.assertFalse("There shouldn't be any data pending.", worker.isDataPending());
    	}
    	catch(Exception e) {
    		Assert.fail("Error inserting a node! " + e.getMessage());
    	}
    }
    
    @Test
    public void testInsertWay() {
    	try {
    		
    		List<Tag> tags = new ArrayList<>();
    		tags.add(new Tag("key", "value"));
    		List<WayNode> wayNodes = new ArrayList<>();
    		wayNodes.add(new WayNode(1122334455));
    		wayNodes.add(new WayNode(66778899));
    		wayNodes.add(new WayNode(159785320));
    		List<Way> wayList = new ArrayList<>();
    		Way way = new Way(1, 1, new Date(), new OsmUser(444, "osm_user"), 555, tags, wayNodes);
    		wayList.add(way);
    		Map<Long, List<Long>> wayNodeMap = new HashMap<>();
    		List<Long> nodeList = new ArrayList<>();
			for (WayNode node:way.getWayNodes()) {
				nodeList.add(node.getNodeId());
			}
    		wayNodeMap.put(way.getId(), nodeList);
    		//insert ways
    		Assert.assertFalse("No data should be commited before inserting.", worker.beginBatchInserts(DBEntityType.Way));
    		worker.batchInsertWays(wayList);
    		worker.endBatchInsert(true);
    		//insert wayNodes
    		Assert.assertFalse("No data should be commited before inserting.", worker.beginBatchInserts(DBEntityType.WayNode));
    		worker.batchInsertWayNodes(wayNodeMap);
    		worker.endBatchInsert(true);
    	}
    	catch(Exception e) {
    		Assert.fail("Error inserting a way! " + e.getMessage());
    	}
    }
    
    @Test
    public void testInsertRelation() {
    	try {
    		List<Relation> relationList = new ArrayList<>();
    		List<Tag> tags = new ArrayList<>();
    		tags.add(new Tag("key", "value"));
    		List<RelationMember> memberList = new ArrayList<>();
    		memberList.add(new RelationMember(552345,EntityType.Node,"point"));
    		memberList.add(new RelationMember(667781,EntityType.Way,"forward"));
    		memberList.add(new RelationMember(309546,EntityType.Way,"backward"));
    		Relation relation = new Relation(33,44, new Date(), new OsmUser(222, "New User"), 999, tags, memberList);
    		relationList.add(relation);
    		Map<Long, List<RelationMember>> relationMemberMap = new HashMap<>();
    		relationMemberMap.put(relation.getId(), relation.getMembers());
    		//insert relations
    		Assert.assertFalse("No data should be commited before inserting.", worker.beginBatchInserts(DBEntityType.Relation));
    		worker.batchInsertRelations(relationList);
    		worker.endBatchInsert(true);
    		//insert relationMembers
    		Assert.assertFalse("No data should be commited before inserting.", worker.beginBatchInserts(DBEntityType.RelationMember));
    		worker.batchInsertRelationMembers(relationMemberMap);
    		worker.endBatchInsert(true);
    	}
    	catch(Exception e) {
    		Assert.fail("Error inserting a relation! " + e.getMessage());
    	}
    }
}
