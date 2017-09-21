package org.openstreetmap.osmosis.oracle.common;

import oracle.jdbc.OracleConnection;
import org.openstreetmap.osmosis.core.domain.v0_6.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by lqian on 9/20/17.
 */
public class DBWorker {
    private DataSource dataSource;

    //OSM user id when unknown
    private static final int USER_ID_NONE = -1;
    private final String nodeInsertSql = "insert /*+ append */ into nodes (id, version, user_id, tstamp, changeset_id, tags) values(?, ?, ?, ?, ?, ?)";
    private final String wayInsertSql = "";
    private final String relationInsertSql = "";

    private OracleConnection conn = null;
    private PreparedStatement pstmt=null;

    public DBWorker(DataSource ds){
        this.dataSource = ds;
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
    public void beginBatchInserts(EntityType dataType) throws SQLException {
        if(pstmt!=null)
            throw new IllegalStateException("Previous PreparedStatement still alive!");

        ensureConnection();

        switch(dataType){
            case Way:
                break;
            case Node:
                pstmt = prepareInsert(conn, nodeInsertSql);
                break;
            case Relation:
            default:

        }

    }

    public void endBatchInsert(boolean commit) throws SQLException {
        if(commit)
            conn.commit();
        else
            conn.rollback();

        closeConnection();
    }

    public void batchInsertNodes(List<Node> nodeList) throws SQLException {

        for(Node node : nodeList){

            long id = node.getId();
            int version = node.getVersion();
            int userId = node.getUser().getId();
            Date timestamp = node.getTimestamp();
            long changeset_id = node.getChangesetId();

            String tagJson = TagJson.getJson(node.getTags());

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

    public void batchInsertWays(List<Way> wayList){

    }

    public void batchInsertRelations(List<Relation> relationList){

    }

    private PreparedStatement prepareInsert(OracleConnection oraConn, String sql) throws SQLException {
        PreparedStatement pstmt = oraConn.prepareStatement(sql);
        return pstmt;
    }

}
