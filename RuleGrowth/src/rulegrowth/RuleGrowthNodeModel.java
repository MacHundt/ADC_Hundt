package rulegrowth;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import rulegrowth.spmf.AlgoRULEGROWTH;
import rulegrowth.spmf.SequenceDatabase;

/**
 * <code>NodeModel</code> for the "RuleGrowth" node.
 * Mining sequential rules using the RuleGrowth algorithm from the spmf-library (http://www.philippe-fournier-viger.com/spmf)
 *
 * @author Michael Hundt
 */
public class RuleGrowthNodeModel extends NodeModel {
    
	/**
	 * The container for building the output table.
	 */
	static BufferedDataContainer container;
	
	static final String DEFAULT_SEQ_COL = "POLYLINE";
	static final String SEQ_COL = "seq_column_selection";
	
	/**
	 * The settings models for the dialog components to handle user settings.
	 */

	private SettingsModelDoubleBounded m_minSupSelection = createMinSupModel();
	private SettingsModelDoubleBounded m_minConfSelection = createMinConfModel();
	private SettingsModelString m_SeqColumnSelection = createSeqColumnModel();
	private double minSup = 0.5;
	private double minConf = 0.6;
	
	private static int rowCounter = 0;
	private static ExecutionContext exec = null;
	private static int rowNum = 0;
	
    /**
     * Constructor for the node model.
     */
    protected RuleGrowthNodeModel() {
    
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
    	
    	this.exec = exec;
    	
		if (inData == null || inData[0] == null) {
			return inData;
		}
		
		ArrayList<String> outt = new ArrayList<String>();
		outt.add("a");
		StringBuilder build = new StringBuilder();
		for ( int i = 0; i< outt.size()-1 ; i++ ) {
			build.append(outt.get(i));
			build.append(" -1 ");
			
		}
		build.toString();
		
		// stores meta data about the table
		DataTableSpec inDataSpec = inData[0].getDataTableSpec();
		rowNum = inData[0].getRowCount();
		
		/*
		 * store the positions of needed columns.
		 */
		int seqColPos = inDataSpec.findColumnIndex(m_SeqColumnSelection.getStringValue());
		
		/*
		 * update k, minconf and delta which is specified by the user
		 */
		minSup = m_minSupSelection.getDoubleValue();
		minConf = m_minConfSelection.getDoubleValue();

		RowIterator rowIter = inData[0].iterator();

		DataColumnSpec[] allColSpecs = new DataColumnSpec[3];
		allColSpecs[0] = new DataColumnSpecCreator("Rule", StringCell.TYPE)
				.createSpec();
		allColSpecs[1] = new DataColumnSpecCreator("Support(absolute)", DoubleCell.TYPE)
				.createSpec();
		allColSpecs[2] = new DataColumnSpecCreator("Confidence", DoubleCell.TYPE)
				.createSpec();
		DataTableSpec outputSpec = new DataTableSpec(allColSpecs);

		SequenceDatabase database = new SequenceDatabase();
		while (rowIter.hasNext()) {
			String[] inputTokens = ((StringCell) (rowIter.next()
					.getCell(seqColPos))).getStringValue().split(" ");
			database.addSequence(inputTokens);
//			for (String s : inputTokens) {
//				System.out.print(s + " ");
//			}
//			System.out.println("");
		}
//		StringBuilder builder = new StringBuilder();
//		while (rowIter.hasNext()) {
//			builder.append(((StringCell) (rowIter.next().getCell(seqColPos))).getStringValue());
//	//		for (String s : inputTokens) {
//	//			System.out.print(s + " ");
//	//		}
//	//		System.out.println("");
//		}
		
		container = exec.createDataContainer(outputSpec);
		
//		AlgoERMinerApplied algoERMiner = new AlgoERMinerApplied();
//		algoERMiner.runAlgorithm(minSup, minConf, database);
//		algoERMiner.printStats();
		
		AlgoRULEGROWTH algo = new AlgoRULEGROWTH();
		algo.runAlgorithm(minSup, minConf, database);
		algo.printStats();
		
		
//		int rowCount = 0;
//		while (rulesIter.hasNext()) {
//			Rule rule = (Rule) rulesIter.next();
//			RowKey key = new RowKey("Row " + rowCount);
//			// the cells of the current row, the types of the cells must
//			// match the column spec (see above)
//			DataCell[] cells = new DataCell[3];
//			cells[0] = new StringCell(rule.toString());
//			cells[1] = new DoubleCell(rule.getAbsoluteSupport());
//			cells[2] = new DoubleCell(rule.getConfidence());
//			DataRow row = new DefaultRow(key, cells);
//			container.addRowToTable(row);
//
//			// check if the execution monitor was canceled
//			exec.checkCanceled();
//			exec.setProgress(rowCount / (double) rowNum, "Adding row "
//					+ rowCount);
//			rowCount++;
//		}
		container.close();
		BufferedDataTable out = container.getTable();
		return new BufferedDataTable[] { out };
    }
    
    public static void addRowToOutput(int rowID, String rule, double absSupport, double confidence) {
    	RowKey key = new RowKey("Row" + rowID);
		DataCell[] cells = new DataCell[3];
		cells[0] = new StringCell(rule);
		cells[1] = new DoubleCell(absSupport);
		cells[2] = new DoubleCell(confidence);
		DataRow row = new DefaultRow(key, cells);
		container.addRowToTable(row);
		monitorProgress(rowCounter++);
    }

    
    private static void monitorProgress(int counter) {
		// check if the execution monitor was canceled
		try {
			exec.checkCanceled();
		} catch (CanceledExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		exec.setProgress(counter / (double) rowNum, "Adding row "
				+ counter);
		
	}

	/**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {

        // TODO: generated method stub
        return new DataTableSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	m_minSupSelection.saveSettingsTo(settings);
		m_minConfSelection.saveSettingsTo(settings);
		m_SeqColumnSelection.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	m_minSupSelection.loadSettingsFrom(settings);
		m_minConfSelection.loadSettingsFrom(settings);
		m_SeqColumnSelection.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	m_minSupSelection.validateSettings(settings);
		m_minConfSelection.validateSettings(settings);
		m_SeqColumnSelection.validateSettings(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }
    
    /**
	 * Creation of the different Settings Models to communicate with the node
	 * dialog
	 */
	protected static SettingsModelString createSeqColumnModel() {
		return new SettingsModelString(SEQ_COL, DEFAULT_SEQ_COL);
	}

	protected static SettingsModelDoubleBounded createMinSupModel() {
		return new SettingsModelDoubleBounded("min_sup_selection", 0.5, 0, 1);
	}
	
	protected static SettingsModelDoubleBounded createMinConfModel() {
		return new SettingsModelDoubleBounded("min_conf_selection", 0.6, 0, 1);
	}
}

