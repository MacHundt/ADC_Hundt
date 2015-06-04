package org.knime.sequence.prefixspan;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.sequence.prefixspan.spmf.AlgoPrefixSpan;
import org.knime.sequence.prefixspan.spmf.SequentialPatterns;
import org.knime.sequence.prefixspan.spmf.input.SequenceDatabase;


/**
 * This is the model implementation of CloSpan.
 * 
 * 
 * @author Michael Hundt
 */
public class PrefixSpanNodeModel extends NodeModel {

	// the logger instance
	private static final NodeLogger logger = NodeLogger
			.getLogger(PrefixSpanNodeModel.class);

	/**
	 * The container for building the output table.
	 */
	static BufferedDataContainer container;

	/**
	 * The settings models for the dialog components to handle user settings.
	 */
	static final String MIN_SUP = "min_sup_selection";
	static final String SEQ_COL = "seq_column_selection";
	static final String OUTPUT_SEQ_ID = "output_Sequence_Identifiers";

	/** initial default count value. */
	static final double DEFAULT_MIN_SUPP = 0.05;
	static final int MAX_MIN_SUP = 1;
	static final int MIN_MIN_SUP = 0;
	static final String DEFAULT_SEQ_COL = "POLYLINE";
	static final boolean DEFAAULT_OUTPUT_SEQ_ID = false;

	private double minSup = 0.5;
	private int seqColPos = 0;
	private int rowNum = 0;

	private SettingsModelDoubleBounded m_minSupSelection = createMinSupModel();
	private SettingsModelString m_SeqColumnSelection = createSeqColumnModel();
	private SettingsModelBoolean m_output_seq_id = createOutSeqID();

	boolean keepPatterns = true;
	boolean verbose = false;
	boolean findClosedPatterns = true;
	boolean executePruningMethods = true;

	// if you set the following parameter to true, the sequence ids of the
	// sequences where
	// each pattern appears will be shown in the result
	boolean outputSequenceIdentifiers = false;

	/**
	 * Constructor for the node model.
	 */
	protected PrefixSpanNodeModel() {

		// TODO one incoming port and one outgoing port is assumed
		super(1, 1);
	}

	private SettingsModelBoolean createOutSeqID() {
		return new SettingsModelBoolean(OUTPUT_SEQ_ID, DEFAAULT_OUTPUT_SEQ_ID);
	}

	/**
	 * Creation of the different Settings Models to communicate with the node
	 * dialog
	 */
	protected static SettingsModelString createSeqColumnModel() {
		return new SettingsModelString(PrefixSpanNodeModel.SEQ_COL,
				PrefixSpanNodeModel.DEFAULT_SEQ_COL);
	}

	protected static SettingsModelDoubleBounded createMinSupModel() {
		return new SettingsModelDoubleBounded(PrefixSpanNodeModel.MIN_SUP,
				PrefixSpanNodeModel.DEFAULT_MIN_SUPP,
				PrefixSpanNodeModel.MIN_MIN_SUP, PrefixSpanNodeModel.MAX_MIN_SUP);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		if (inData == null || inData[0] == null) {
			return inData;
		}

		// stores meta data about the table
		DataTableSpec inDataSpec = inData[0].getDataTableSpec();
		rowNum = inData[0].getRowCount();

		/*
		 * read values form Dialog
		 */
		seqColPos = inDataSpec.findColumnIndex(m_SeqColumnSelection
				.getStringValue());
		//relative in %
		minSup = m_minSupSelection.getDoubleValue();
		// absolut minsup
		int absminSup =  (int) java.lang.Math.ceil((minSup * rowNum));
		outputSequenceIdentifiers = m_output_seq_id.getBooleanValue();

		SequenceDatabase sequenceDatabase = new SequenceDatabase();

		/*
		 * Read in to sequential database
		 */
		loadFromDataTable(inData[0], sequenceDatabase);

		// Create an instance of the algorithm with minsup = 50 %
		AlgoPrefixSpan algo = new AlgoPrefixSpan(); 
		
        // if you set the following parameter to true, the sequence ids of the sequences where
        // each pattern appears will be shown in the result
        algo.setShowSequenceIdentifiers(outputSequenceIdentifiers);
		

		// execute the algorithm
        SequentialPatterns patterns = algo.runAlgorithm(sequenceDatabase, minSup, null); 
		algo.printStatistics(sequenceDatabase.size());
		
		patterns.printFrequentPatterns(100, outputSequenceIdentifiers);
		
		
		// once we are done, we close the container and return its table
		container.close();

		BufferedDataTable out = container.getTable();
		return new BufferedDataTable[] { out };
	}

	/**
	 * From a KNIME DataTable, we create a database composed of a list of
	 * sequences
	 * 
	 * @param inData
	 *            File a KNIME DataTable
	 * @param sequenceDatabase
	 * @param minSupRelative
	 *            relative Minimum support
	 * @throws IOException
	 */
	public void loadFromDataTable(BufferedDataTable inData,
			SequenceDatabase sequenceDatabase) throws IOException {
		String thisLine; // variable to read each line.
		// For each line
		RowIterator rowIter = inData.iterator();
		while (rowIter.hasNext()) {
			thisLine = ((StringCell) (rowIter.next().getCell(seqColPos)))
					.getStringValue();
			// If the line is not a comment line
			if (thisLine.isEmpty() == false && thisLine.charAt(0) != '#'
					&& thisLine.charAt(0) != '%' && thisLine.charAt(0) != '@') {
				// we read it and add it as a sequence
				sequenceDatabase.addSequence(thisLine.split(" "));
			}
		}
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
		return new DataTableSpec[] { null };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_minSupSelection.saveSettingsTo(settings);
		m_SeqColumnSelection.saveSettingsTo(settings);
		m_output_seq_id.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_minSupSelection.loadSettingsFrom(settings);
		m_SeqColumnSelection.loadSettingsFrom(settings);
		m_output_seq_id.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_minSupSelection.validateSettings(settings);
		m_SeqColumnSelection.validateSettings(settings);
		m_output_seq_id.validateSettings(settings);
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

}
