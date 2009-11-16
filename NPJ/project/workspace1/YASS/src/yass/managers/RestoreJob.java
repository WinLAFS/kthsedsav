package yass.managers;

import java.io.Serializable;


import dks.niche.ids.GroupId;

class RestoreJob implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8398694795317271216L;
		String failedNodeId;
		GroupId groupToRestore;
		RestoreJob(String failedNodeId, GroupId groupToRestore) {
			this.failedNodeId = failedNodeId;
			this.groupToRestore = groupToRestore;
		}
		String getKey() {
			return groupToRestore.getId().toString()+failedNodeId;
		}
}