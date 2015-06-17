package test;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "TT" Node.
 * asdfadf
 *
 * @author MH
 */
public class TTNodeFactory 
        extends NodeFactory<TTNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public TTNodeModel createNodeModel() {
        return new TTNodeModel();
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
    public NodeView<TTNodeModel> createNodeView(final int viewIndex,
            final TTNodeModel nodeModel) {
        return new TTNodeView(nodeModel);
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
        return new TTNodeDialog();
    }

}

