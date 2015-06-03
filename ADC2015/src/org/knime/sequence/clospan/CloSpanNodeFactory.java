package org.knime.sequence.clospan;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "CloSpan" Node.
 * 
 *
 * @author Michael
 */
public class CloSpanNodeFactory 
        extends NodeFactory<CloSpanNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public CloSpanNodeModel createNodeModel() {
        return new CloSpanNodeModel();
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
    public NodeView<CloSpanNodeModel> createNodeView(final int viewIndex,
            final CloSpanNodeModel nodeModel) {
        return new CloSpanNodeView(nodeModel);
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
        return new CloSpanNodeDialog();
    }

}

