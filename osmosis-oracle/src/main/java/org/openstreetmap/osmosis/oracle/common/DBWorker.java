package org.openstreetmap.osmosis.oracle.common;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleDataSource;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.openstreetmap.osmosis.core.domain.v0_6.*;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lqian on 9/20/17.
 */
public class DBWorker {
    private DataSource dataSource;

    //OSM user id when unknown
    private static final int USER_ID_NONE = -1;
    private final String userInsertSql = "insert /*+ append */ into users (id, name) values(?, ?)";
    private final String nodeInsertSql = "insert /*+ append */ into nodes (id, version, user_id, tstamp, changeset_id, point, tags) values(?, ?, ?, ?, ?, ?, ?)";
    private final String wayInsertSql = "insert /*+ append */ into ways (id, version, user_id, tstamp, changeset_id, tags) values(?, ?, ?, ?, ?, ?)";
    private final String wayNodeInsertSql = "insert /*+ append */ into way_nodes (way_id, node_id, sequence_id) values(?, ?, ?)";
    private final String relationInsertSql = "insert /*+ append */ into relations (id, version, user_id, tstamp, changeset_id, tags) values(?, ?, ?, ?, ?, ?)";
    private final String relationMemberInsertSql = "insert /*+ append */ into relation_members (relation_id, member_id, member_type, member_role, sequence_id) values(?, ?, ?, ?, ?)";

    private OracleConnection conn = null;
    private PreparedStatement pstmt=null;
    private DBEntityType previousType = null;
    private DBEntityType currentType = null;

    public DBWorker(DataSource ds){
        this.dataSource = ds;
    }
    
    private PreparedStatement prepareInsert(OracleConnection oraConn, String sql) throws SQLException {
        PreparedStatement pstmt = oraConn.prepareStatement(sql);
        return pstmt;
    }

    private void ensureConnection() throws SQLException {
        if(conn!=null)
            return;

        conn = (OracleConnection) dataSource.getConnection();
        conn.setAutoCommit(false);
    }

    private void closeConnection() throws SQLException {

        pstmt.close();
        pstmt = null;

        conn.close();
        conn = null;

    }
    
    public boolean beginBatchInserts(DBEntityType dataType) throws SQLException {
    	//System.out.println("BEGIN BATCH INSERT");
    	boolean batchCommitted = false;
    	if(pstmt != null) {
    		if (dataType == currentType) {
        		//do nothing, as the statement has been initialized
        	}
    		else {
    			//throw new IllegalStateException("Previous PreparedStatement still alive!");
    			//commit changes?
    			endBatchInsert(true);
    			batchCommitted = true;
    		}
    	}
    	
    	//Set active EntityType
    	previousType = currentType;
    	currentType = dataType;
    	
        ensureConnection();

		switch (dataType) {
		case User:
			pstmt = prepareInsert(conn, userInsertSql);
			break;
		case Node:
			pstmt = prepareInsert(conn, nodeInsertSql);
			break;
		case Way:
			pstmt = prepareInsert(conn, wayInsertSql);
			break;
		case WayNode:
			pstmt = prepareInsert(conn, wayNodeInsertSql);
			break;
		case Relation:
			pstmt = prepareInsert(conn, relationInsertSql);
			break;
		case RelationMember:
			pstmt = prepareInsert(conn, relationMemberInsertSql);
		default:

		}
		return batchCommitted;
    }

    public void endBatchInsert(boolean commit) throws SQLException {
    	//System.out.println("END BATCH INSERT");
        if(commit) {
            conn.commit();
        }
        else {
            conn.rollback();
        }

        closeConnection();
    }
    
    public void batchInsertUsers(Map<Integer, String> userMap) throws SQLException {
    	//System.out.println("BATCH INSERT USERS");
        for(Integer id : userMap.keySet()){
        	//System.out.println("ID=" + id + ", NAME="+userMap.get(id));
        	pstmt.setInt(1, id);
        	if (userMap.get(id) == null || userMap.get(id).isEmpty()) {
        		System.out.println ("Invalid user name! " + id);
        		pstmt.setString(2, "NO_NAME_FOUND");
        	}
        	else {
        		pstmt.setString(2, userMap.get(id));
        	}

            pstmt.addBatch();
        }

        pstmt.executeBatch();

    }

    public void batchInsertNodes(List<Node> nodeList) throws SQLException {
    	//System.out.println("BATCH INSERT NODES");
    	for(Node node : nodeList){
            long id = node.getId();
            int version = node.getVersion();
            int userId = node.getUser().getId();
            Date timestamp = node.getTimestamp();
            long changeset_id = node.getChangesetId();
            STRUCT geom = JGeometry.store(new JGeometry(node.getLongitude(), node.getLatitude(), 8307), pstmt.getConnection());

            String tagJson = TagJson.getJson(node.getTags());

            pstmt.setLong(1, id);
            pstmt.setInt(2, version);
            pstmt.setInt(3, userId);
            pstmt.setDate(4,  new java.sql.Date(timestamp.getTime()));
            pstmt.setLong(5, changeset_id);
            pstmt.setObject(6, geom);
            pstmt.setString(7, tagJson);

            pstmt.addBatch();
        }

        pstmt.executeBatch();
    }
    
    public void batchInsertWays(List<Way> wayList) throws SQLException {
    	//System.out.println("BATCH INSERT WAYS");
    	for(Way way : wayList){
            long id = way.getId();
            int version = way.getVersion();
            int userId = way.getUser().getId();
            Date timestamp = way.getTimestamp();
            long changeset_id = way.getChangesetId();
            //linestring
            //bbox
            String tagJson = TagJson.getJson(way.getTags());
            //nodes
            
            
            /*StringBuilder nodes = new StringBuilder();
            String separator = "";
            for (int nodeCount=0; nodeCount<way.getWayNodes().size(); nodeCount++) {
            	nodes.append(separator);
            	nodes.append(String.valueOf(way.getWayNodes().get(nodeCount).getNodeId()));
            	separator = ",";
            }
            java.sql.Array sqlArray = conn.createArrayOf("number", nodes);*/
            
            //STRUCT geom = JGeometry.store(new JGeometry(way.getLongitude(), way.getLatitude(), 8307), pstmt.getConnection());

            pstmt.setLong(1, id);
            pstmt.setInt(2, version);
            pstmt.setInt(3, userId);
            pstmt.setDate(4,  new java.sql.Date(timestamp.getTime()));
            pstmt.setLong(5, changeset_id);
            pstmt.setString(6, tagJson);

            pstmt.addBatch();
        }

        pstmt.executeBatch();
    }
    
    public void batchInsertWayNodes(Map<Long, List<Long>> wayNodeMap) throws SQLException {
    	//System.out.println("BATCH INSERT WAY NODES");
    	for(Long wayId : wayNodeMap.keySet()){
            List<Long> nodeIds = wayNodeMap.get(wayId);
            for (int nodeCount = 0; nodeCount < nodeIds.size(); nodeCount++) {
            	pstmt.setLong(1, wayId);
                pstmt.setLong(2, nodeIds.get(nodeCount));
                pstmt.setInt(3, nodeCount);

                pstmt.addBatch();
            }
        }

        pstmt.executeBatch();
    }
    
    

    public void batchInsertRelations(List<Relation> relationList) throws SQLException {
    	//System.out.println("BATCH INSERT RELATIONS");
    	for(Relation relation : relationList){
            long id = relation.getId();
            int version = relation.getVersion();
            int userId = relation.getUser().getId();
            Date timestamp = relation.getTimestamp();
            long changeset_id = relation.getChangesetId();
            String tagJson = TagJson.getJson(relation.getTags());

            pstmt.setLong(1, id);
            pstmt.setInt(2, version);
            pstmt.setInt(3, userId);
            pstmt.setDate(4,  new java.sql.Date(timestamp.getTime()));
            pstmt.setLong(5, changeset_id);
            pstmt.setString(6, tagJson);

            pstmt.addBatch();
        }

        pstmt.executeBatch();
    }
    
    public void batchInsertRelationMembers(Map<Long, List<RelationMember>> relationMemberMap) throws SQLException {
    	//System.out.println("BATCH INSERT RELATION MEMBERS");
    	for(Long relationId : relationMemberMap.keySet()) {
    		List<RelationMember> members = relationMemberMap.get(relationId);
    		for (int memberCounter = 0; memberCounter < members.size(); memberCounter++) {
    			RelationMember member = members.get(memberCounter);
    			long memberId = member.getMemberId();
    			String memberType = member.getMemberType().name();
    			String memberRole = member.getMemberRole();
    			
    			pstmt.setLong(1, relationId);
                pstmt.setLong(2, memberId);
                pstmt.setString(3, memberType);
                pstmt.setString(4,  memberRole);
                pstmt.setLong(5, memberCounter);
                
                pstmt.addBatch();
    		}
        }

        pstmt.executeBatch();
    }
    
    public void testInsertNode() {
    	try {
    		beginBatchInserts(DBEntityType.Node);
    		List<Tag> tags = new ArrayList<>();
    		tags.add(new Tag("key", "value"));
    		List<Node> nodeList = new ArrayList<>();
    		Node node = new Node(1, 1, new Date(), new OsmUser(444, "osm_user"), 555, tags, 22,33);
    		nodeList.add(node);
    		batchInsertNodes(nodeList);
    		endBatchInsert(true);
    	}
    	catch(Exception e) {
    		System.out.println("Error!");
    	}
    }
    
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
    		beginBatchInserts(DBEntityType.Way);
    		batchInsertWays(wayList);
    		endBatchInsert(true);
    		//insert wayNodes
    		beginBatchInserts(DBEntityType.WayNode);
    		batchInsertWayNodes(wayNodeMap);
    		endBatchInsert(true);
    	}
    	catch(Exception e) {
    		System.out.println("Error!");
    	}
    }
    
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
    		beginBatchInserts(DBEntityType.Relation);
    		batchInsertRelations(relationList);
    		endBatchInsert(true);
    		//insert relationMembers
    		beginBatchInserts(DBEntityType.RelationMember);
    		batchInsertRelationMembers(relationMemberMap);
    		endBatchInsert(true);
    	}
    	catch(Exception e) {
    		System.out.println("Error!");
    	}
    }

    public static void main(String ...args) {
		try {
			String jdbcUrl = "jdbc:oracle:thin:@//adc01dtw.us.oracle.com:1521/orcl.us.oracle.com";
	    	OracleDataSource ds;
	    	//OracleDataSource rds = OracleDataSourceFactory.getOracleDataSource();
	    	ds = new OracleDataSource();
	    	ds.setUser("osm");
	    	ds.setPassword("oracle");
			ds.setURL(jdbcUrl);
			DBWorker worker = new DBWorker(ds);
			worker.testInsertRelation();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
}
