package org.knime.sequence.prefixspan;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "PrefixSpan" Node.
 * 
 *
 * @author Michael Hundt
 */
public class PrefixSpanNodeFactory 
        extends NodeFactory<PrefixSpanNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public PrefixSpanNodeModel createNodeModel() {
        return new PrefixSpanNodeModel();
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
    public NodeView<PrefixSpanNodeModel> createNodeView(final int viewIndex,
            final PrefixSpanNodeModel nodeModel) {
        return new PrefixSpanNodeView(nodeModel);
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
        return new PrefixSpanNodeDialog();
    }

}

