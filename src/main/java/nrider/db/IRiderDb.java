package nrider.db;

import nrider.core.Rider;

import java.util.List;

/**
 * Simple interface to a persistent rider store.
 */
public interface IRiderDb {
    List<String> getRiderIds();

    Rider getRider(String riderId);

    List<String> getGroupIds();

    List<String> getGroupRiderIds(String groupId);

    void addRider(Rider rider);

    void updateRider(Rider rider);

    void removeRider(String riderId);

    void createGroup(String groupId);

    void removeGroup(String groupId);

    void addRiderToGroup(String groupId, String riderId);

    void removeRiderFromGroup(String groupId, String riderId);
}
