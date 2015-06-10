package org.knime.sequence.ruleGrowth;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "RuleGrowth" Node.
 * Mining sequential rules using the RuleGrowth algorithm from the spmf-library (http://www.philippe-fournier-viger.com/spmf)
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Michael Hundt
 */
public class RuleGrowthNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the ERMiner node.
     */
    protected RuleGrowthNodeDialog() {
super();
        
addDialogComponent(new DialogComponentColumnNameSelection(
		new SettingsModelString(RuleGrowthNodeModel.SEQ_COL, 
				RuleGrowthNodeModel.DEFAULT_SEQ_COL),
				"Column containing the sequences: ", 
				0, 
				true, 
				StringValue.class));
        
        addDialogComponent(new DialogComponentNumber(RuleGrowthNodeModel.createMinSupModel(), "Choose minSup", 0.05));
        addDialogComponent(new DialogComponentNumber(RuleGrowthNodeModel.createMinConfModel(), "Choose minConf", 0.05));
    }
}

