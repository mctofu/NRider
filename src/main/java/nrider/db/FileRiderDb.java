package nrider.db;

import nrider.core.Rider;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple file based impl of rider db
 */
public class FileRiderDb implements IRiderDb {
    private final static Logger LOG = Logger.getLogger(FileRiderDb.class);
    private static final String RIDERS_DB = "riders.db";
    private static final String GROUPS_DB = "groups.db";

    public List<String> getRiderIds() {
        return readLines(RIDERS_DB);
    }

    public Rider getRider(String riderId) {
        Rider rider = null;
        try {
            File riderDb = new File(riderDb(riderId));
            String line;
            if (riderDb.exists()) {
                BufferedReader input = new BufferedReader(new FileReader(riderDb));
                try {
                    line = input.readLine();
                    rider = new Rider();
                    rider.setName(riderId);
                    rider.setThresholdPower(Integer.parseInt(line));
                    while ((line = input.readLine()) != null) {
                        rider.addDevice(line);
                    }
                } finally {
                    input.close();
                }
            }
        } catch (IOException ex) {
            LOG.error(ex);
        }
        return rider;
    }

    private String riderDb(String riderId) {
        return "rider-" + riderId + ".db";
    }

    public List<String> getGroupIds() {
        return readLines(GROUPS_DB);
    }

    public List<String> getGroupRiderIds(String groupId) {
        return readLines(groupDb(groupId));
    }

    private String groupDb(String groupId) {
        return "group-" + groupId + ".db";
    }

    public void addRider(Rider rider) {
        List<String> riderIds = getRiderIds();
        if (riderIds.contains(rider.getIdentifier())) {
            throw new Error("Rider already exists");
        }
        updateRider(rider);
        riderIds.add(rider.getIdentifier());
        writeLines(RIDERS_DB, riderIds);
    }

    public void updateRider(Rider rider) {
        List<String> lines = new ArrayList<String>();

        lines.add(Integer.toString(rider.getThresholdPower()));
        for (String device : rider.getDevices()) {
            lines.add(device);
        }
        writeLines(riderDb(rider.getIdentifier()), lines);
    }

    public void removeRider(String riderId) {
        List<String> riderIds = getRiderIds();
        riderIds.remove(riderId);
        writeLines(RIDERS_DB, riderIds);
    }

    public void createGroup(String groupId) {
        List<String> groupIds = getGroupIds();
        if (groupIds.contains(groupId)) {
            throw new Error("Group already exists");
        }
        groupIds.add(groupId);
        writeLines(GROUPS_DB, groupIds);
    }

    public void removeGroup(String groupId) {
        List<String> groupIds = getGroupIds();
        groupIds.remove(groupId);
        writeLines(GROUPS_DB, groupIds);
    }

    public void addRiderToGroup(String groupId, String riderId) {
        List<String> riderIds = getGroupRiderIds(groupId);
        if (riderIds.contains(riderId)) {
            return;
        }
        riderIds.add(riderId);
        writeLines(groupDb(groupId), riderIds);
    }

    public void removeRiderFromGroup(String groupId, String riderId) {
        List<String> riderIds = getGroupRiderIds(groupId);
        riderIds.remove(riderId);
        writeLines(groupDb(groupId), riderIds);
    }

    private List<String> readLines(String file) {
        List<String> riders = new ArrayList<>();

        try {
            File riderDb = new File(file);
            if (riderDb.exists()) {
                BufferedReader input = new BufferedReader(new FileReader(riderDb));
                try {
                    String line;
                    while ((line = input.readLine()) != null) {
                        riders.add(line);
                    }
                } finally {
                    input.close();
                }
            }
        } catch (IOException ex) {
            LOG.error(ex);
        }
        return riders;
    }

    private void writeLines(String file, List<String> lines) {
        try {
            File riderDb = new File(file + ".tmp");

            PrintWriter output = new PrintWriter(new FileWriter(riderDb));
            try {
                for (String line : lines) {
                    output.println(line);
                }
            } finally {
                output.close();
            }

            File targetPath = new File(file);
            if (targetPath.exists()) {
                targetPath.delete();
            }

            riderDb.renameTo(targetPath);
        } catch (IOException ex) {
            LOG.error(ex);
        }
    }
}