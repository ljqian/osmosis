package org.openstreetmap.osmosis.oracle.common;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by lqian on 9/20/17.
 */
public class DBWorker {
    private DataSource dataSource;

    public DBWorker(DataSource ds){
        this.dataSource = ds;
    }


    public void batchInsertNodes(List<Node> nodeList) throws SQLException {
        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(false);

        OracleConnection oraConn = (OracleConnection) conn;

    }

    public void batchInsertWays(List<Way> wayList){

    }

    public void batchInsertRelations(List<Relation> relationList){

    }


}
