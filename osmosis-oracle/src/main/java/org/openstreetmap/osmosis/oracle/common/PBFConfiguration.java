package org.openstreetmap.osmosis.oracle.common;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;

public class PBFConfiguration {
	private File file;
	private List<EntityType> typesToInsertList;
	
	/**
	 * The configuration when reading from a PBF File for insertion to the Database.
	 * @param file The PBF file to read.
	 * @param typesToInsert What types to insert into the Database. Nodes, Ways or Relations. If this value is not specified, then everything will be inserted.
	 */
	public PBFConfiguration(File file, EntityType ...typesToInsert) {
		this.file = file;
		if (typesToInsert != null && typesToInsert.length > 0) {
			this.typesToInsertList = Arrays.asList(typesToInsert);
		}
	}
	
	public File getPBFFile() {
		return file;
	}
	
	public boolean isInsertable(EntityType type) {
		if (typesToInsertList != null) {
			return typesToInsertList.contains(type);
		}
		//By default, everything goes if not specified otherwise!
		return true;
	}
}
