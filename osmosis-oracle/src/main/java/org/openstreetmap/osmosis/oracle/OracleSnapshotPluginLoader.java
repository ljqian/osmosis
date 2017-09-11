package org.openstreetmap.osmosis.oracle;

import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lqian on 9/11/17.
 */
public class OracleSnapshotPluginLoader implements PluginLoader {

    @Override
    public Map<String, TaskManagerFactory> loadTaskFactories() {
        Map<String, TaskManagerFactory> factoryMap;
        factoryMap = new HashMap<>();

        return factoryMap;
    }
}
