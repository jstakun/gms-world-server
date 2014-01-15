package osm.api;

import osm.primitive.changeset.Changeset;
import osm.primitive.changeset.osc.DiffResult;
import osm.primitive.changeset.osc.OSMChangeFile;

public class Session {

    /**
     * Attempts to start a changeset. Returns the id of the changeset, if
     * successful.
     * 
     * @param changeset
     *            The changeset to start.
     * @return If the creation of the changeset was successful, the id of the
     *         changeset.
     */
    public int createChangeset(Changeset changeset) {
        return 0;

    }

    /**
     * Closes the changeset with the given id number.
     * 
     * @param changesetId
     *            The id number of the changeset to close.
     */
    public void closeChangeset(int changesetId) {

    }

    public DiffResult upload(int changesetId, OSMChangeFile osmChange) {
        // Open a POST connection
        // Write out the osc file to a buffer
        // Upload the buffer as the POST data

        // Read back the result
        // Parse it into a DiffResult
        return null;
    }
}
