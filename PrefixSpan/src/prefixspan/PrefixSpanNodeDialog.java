package prefixspan;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "CloSpan" Node.
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Michael Hundt
 */
public class PrefixSpanNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring CloSpan node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    @SuppressWarnings("unchecked")
	protected PrefixSpanNodeDialog() {
        super();
        addDialogComponent(new DialogComponentColumnNameSelection(
        		new SettingsModelString(PrefixSpanNodeModel.SEQ_COL, 
        				PrefixSpanNodeModel.DEFAULT_SEQ_COL),
						"Column containing the sequences: ", 
						0, 
						true, 
						StringValue.class));
        
        addDialogComponent(new DialogComponentNumber(
        		new SettingsModelDoubleBounded(PrefixSpanNodeModel.MIN_SUP, 
        				PrefixSpanNodeModel.DEFAULT_MIN_SUPP, 
        				PrefixSpanNodeModel.MIN_MIN_SUP, 
        				PrefixSpanNodeModel.MAX_MIN_SUP), 
        		"Choose minSup", 
        		0.002));
        
        addDialogComponent(new DialogComponentBoolean(
        		new SettingsModelBoolean(PrefixSpanNodeModel.OUTPUT_SEQ_ID, 
        				PrefixSpanNodeModel.DEFAAULT_OUTPUT_SEQ_ID), "add sequence identifiers to output"));
    }
}

