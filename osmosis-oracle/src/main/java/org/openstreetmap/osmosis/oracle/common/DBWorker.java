package org.openstreetmap.osmosis.oracle.common;

import oracle.jdbc.OracleConnection;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.openstreetmap.osmosis.core.domain.v0_6.*;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by lqian on 9/20/17.
 */
public class DBWorker {
	private static Logger LOG = Logger.getLogger(DBWorker.class.getName());
    private DataSource dataSource;

    //OSM user id when unknown
    //private static final int USER_ID_NONE = -1;
    
    //append_values
//    private final String userInsertSql = "insert /*+ append_values */ into users (id, name) values(?, ?)";
//    private final String nodeInsertSql = "insert /*+ append_values */ into nodes (id, version, user_id, tstamp, changeset_id, point, tags) values(?, ?, ?, ?, ?, ?, ?)";
//    private final String wayInsertSql = "insert /*+ append_values */ into ways (id, version, user_id, tstamp, changeset_id, tags) values(?, ?, ?, ?, ?, ?)";
//    private final String wayNodeInsertSql = "insert /*+ append_values */ into way_nodes (way_id, node_id, sequence_id) values(?, ?, ?)";
//    private final String relationInsertSql = "insert /*+ append_values */ into relations (id, version, user_id, tstamp, changeset_id, tags) values(?, ?, ?, ?, ?, ?)";
//    private final String relationMemberInsertSql = "insert /*+ append_values */ into relation_members (relation_id, member_id, member_type, member_role, sequence_id) values(?, ?, ?, ?, ?)";
    
    //append
    private final String userInsertSql = "insert /*+ append */ into users (id, name) values(?, ?)";
    private final String nodeInsertSql = "insert /*+ append */ into nodes (id, version, user_id, tstamp, changeset_id, point, tags) values(?, ?, ?, ?, ?, ?, ?)";
    private final String wayInsertSql = "insert /*+ append */ into ways (id, version, user_id, tstamp, changeset_id, tags) values(?, ?, ?, ?, ?, ?)";
    private final String wayNodeInsertSql = "insert /*+ append */ into way_nodes (way_id, node_id, sequence_id) values(?, ?, ?)";
    private final String relationInsertSql = "insert /*+ append */ into relations (id, version, user_id, tstamp, changeset_id, tags) values(?, ?, ?, ?, ?, ?)";
    private final String relationMemberInsertSql = "insert /*+ append */ into relation_members (relation_id, member_id, member_type, member_role, sequence_id) values(?, ?, ?, ?, ?)";

    private OracleConnection conn = null;
    private PreparedStatement pstmt=null;
    private DBEntityType currentType = null;

    public DBWorker(DataSource ds){
        this.dataSource = ds;
    }
    
    private PreparedStatement prepareInsert(OracleConnection oraConn, String sql) throws SQLException {
        PreparedStatement pstmt = oraConn.prepareStatement(sql);
        return pstmt;
    }
    
    private void initStatement(DBEntityType dataType)  throws SQLException {
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
    	LOG.fine("Begin Batch Insert for " + dataType.name());
    	boolean batchCommitted = false;
    	if(pstmt != null) {
    		if (dataType == currentType) {
        		//do nothing, as the statement has been initialized
        	}
    		else {
    			endBatchInsert(true);
    			batchCommitted = true;
    			initStatement(dataType);
    		}
    	}
    	else {
    		initStatement(dataType);
    	}
    	
		return batchCommitted;
    }

    public void endBatchInsert(boolean commit) throws SQLException {
    	LOG.fine("End Batch Insert");
        if(commit) {
        	pstmt.executeBatch();
            conn.commit();
        }
        else {
            conn.rollback();
        }

        closeConnection();
    }
    
    public void batchInsertUsers(Map<Integer, String> userMap) throws SQLException {
    	LOG.fine("Batch Insert Users.");
        for(Integer id : userMap.keySet()){
        	pstmt.setInt(1, id);
        	if (userMap.get(id) == null || userMap.get(id).isEmpty()) {
        		LOG.warning("Invalid user name: " + id + ", using default name instead.");
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
    	LOG.fine("Batch Insert Nodes");
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
    	LOG.fine("Batch Insert Ways");
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
    	LOG.fine("Batch Insert Way Nodes");
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
    	LOG.fine("Batch Insert Relations");
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
    	LOG.fine("Batch Insert Relation Members");
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
    
    /**
     * Whether there is a Prepared Statement
     * @return
     * @throws SQLException 
     */
    public boolean isDataPending() {
    	boolean pending = false;
    	try {
			pending = pstmt != null && !pstmt.isClosed();
		} catch (SQLException e) {
			pending = true;
		}
    	return pending;
    }
}
