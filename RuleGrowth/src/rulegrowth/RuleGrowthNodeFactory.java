package rulegrowth;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "RuleGrowth" Node.
 * 
 *
 * @author Hundt
 */
public class RuleGrowthNodeFactory 
        extends NodeFactory<RuleGrowthNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public RuleGrowthNodeModel createNodeModel() {
        return new RuleGrowthNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<RuleGrowthNodeModel> createNodeView(final int viewIndex,
            final RuleGrowthNodeModel nodeModel) {
        return new RuleGrowthNodeView(nodeModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new RuleGrowthNodeDialog();
    }

}

