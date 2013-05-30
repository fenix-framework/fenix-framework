package pt.ist.fenixframework.backend;

/**
 * Contains the cluster information related to this node when Fenix Framework has used in a clustering environment.
 *
 * @author Pedro Ruivo
 * @since 2.5
 */
public interface ClusterInformation {

    /**
     * Instance that represents a local-mode Fénix Framework usage.
     */
    public static final ClusterInformation LOCAL_MODE = new ClusterInformation() {

        @Override
        public final int getNumberOfNodes() {
            return 1;
        }

        @Override
        public final int getNodeIndex() {
            return 0;
        }

        @Override
        public final int hashCode() {
            return 1;
        }

        @Override
        public final boolean equals(Object obj) {
            return this == obj;

        }
    };

    /**
     * Instance that represents the cases where if it not possible to know if we are in a clustered environment or
     * local mode.
     */
    public static final ClusterInformation NOT_AVAILABLE = new ClusterInformation() {
        @Override
        public final int getNumberOfNodes() {
            return -1;
        }

        @Override
        public final int getNodeIndex() {
            return -1;
        }

        @Override
        public final int hashCode() {
            return 2;
        }

        @Override
        public final boolean equals(Object obj) {
            return this == obj;
        }
    };

    /**
     * @return the number of nodes running Fenix Framework in the cluster.
     */
    public int getNumberOfNodes();

    /**
     * @return the node index in the cluster. The index is between 0 (inclusive) and {@link #getNumberOfNodes()} (exclusive)
     */
    public int getNodeIndex();

}
