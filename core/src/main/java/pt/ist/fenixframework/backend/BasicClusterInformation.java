package pt.ist.fenixframework.backend;

/**
 * Default implementation of {@link ClusterInformation} with th basic the basic information, i.e., the number of nodes
 * available and tis node index.
 *
 * @author Pedro Ruivo
 * @since 2.5
 */
public class BasicClusterInformation implements ClusterInformation {

    private final int numberOfNodes;
    private final int nodeIndex;

    public BasicClusterInformation(int numberOfNodes, int nodeIndex) {
        this.numberOfNodes = numberOfNodes;
        this.nodeIndex = nodeIndex;
    }

    @Override
    public final int getNumberOfNodes() {
        return numberOfNodes;
    }

    @Override
    public final int getNodeIndex() {
        return nodeIndex;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicClusterInformation that = (BasicClusterInformation) o;

        if (nodeIndex != that.nodeIndex) return false;
        if (numberOfNodes != that.numberOfNodes) return false;

        return true;
    }

    @Override
    public final int hashCode() {
        int result = numberOfNodes;
        result = 31 * result + nodeIndex;
        return result;
    }
}
