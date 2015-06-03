package org.knime.sequence.clospan;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
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
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.sequence.clospan.spmf.AlgoCloSpan;
import org.knime.sequence.clospan.spmf.items.Item;
import org.knime.sequence.clospan.spmf.items.ItemFactory;
import org.knime.sequence.clospan.spmf.items.Sequence;
import org.knime.sequence.clospan.spmf.items.SequenceDatabase;
import org.knime.sequence.clospan.spmf.items.creators.AbstractionCreator;
import org.knime.sequence.clospan.spmf.items.creators.AbstractionCreator_Qualitative;


/**
 * This is the model implementation of CloSpan.
 * 
 *
 * @author Michael Hundt
 */
public class CloSpanNodeModel extends NodeModel {
	
	 // the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(CloSpanNodeModel.class);
	
	/**
	 * The container for building the output table.
	 */
	static BufferedDataContainer container;
	
	/**
	 * The settings models for the dialog components to handle user settings.
	 */
	static final String MIN_SUP = "min_sup_selection";
	static final String SEQ_COL = "seq_column_selection";
	

    /** initial default count value. */
    static final double DEFAULT_MIN_SUPP = 0.5 ;
    static final String DEFAULT_SEQ_COL = "POLYLINE";
    static final int MAX_MIN_SUP = 1;
    static final int MIN_MIN_SUP = 0;
	
	private double minSup = 0.5;
	private int seqColPos = 0;
	
	private SettingsModelDoubleBounded m_minSupSelection = createMinSupModel();
	private SettingsModelString m_SeqColumnSelection = createSeqColumnModel();
	
	 boolean keepPatterns = true;
     boolean verbose = false;
     boolean findClosedPatterns = true;
     boolean executePruningMethods = true;
     
     // if you set the following parameter to true, the sequence ids of the sequences where
     // each pattern appears will be shown in the result
     boolean outputSequenceIdentifiers = false;
     
     private Map<Item, BitSet> frequentItems = new HashMap<Item, BitSet>();
     private List<Sequence> sequences = new LinkedList<Sequence>();
     private ItemFactory<Integer> itemFactory = new ItemFactory<Integer>();
    

    /**
     * Constructor for the node model.
     */
    protected CloSpanNodeModel() {
    
        // TODO one incoming port and one outgoing port is assumed
        super(1, 1);
    }

    /**
	 * Creation of the different Settings Models to communicate with the node
	 * dialog
	 */
	protected static SettingsModelString createSeqColumnModel() {
		return new SettingsModelString(CloSpanNodeModel.SEQ_COL, 
				CloSpanNodeModel.DEFAULT_SEQ_COL);
	}

	protected static SettingsModelDoubleBounded createMinSupModel() {
		return new SettingsModelDoubleBounded(CloSpanNodeModel.MIN_SUP, 
				CloSpanNodeModel.DEFAULT_MIN_SUPP, 
				CloSpanNodeModel.MIN_MIN_SUP, 
				CloSpanNodeModel.MAX_MIN_SUP);
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
		int rowNum = inData[0].getRowCount();
		
		/*
		 * store the positions of needed columns.
		 */
		seqColPos = inDataSpec.findColumnIndex(m_SeqColumnSelection.getStringValue());
		
		minSup = m_minSupSelection.getDoubleValue();
		
		System.out.println("MinSupp: "+minSup);
		System.out.println("SEQ_Col: "+seqColPos);
		
		AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();

	    SequenceDatabase sequenceDatabase = new SequenceDatabase();
	    
	    /*
	     * Read in to sequential database 
	     */
	    loadFromDataTable(inData[0], sequenceDatabase, minSup);
	    
        AlgoCloSpan algorithm = new AlgoCloSpan(minSup, abstractionCreator, findClosedPatterns,executePruningMethods);

        algorithm.runAlgorithm(sequenceDatabase, keepPatterns, verbose, ".//output.txt", outputSequenceIdentifiers);
		

        
        // the data table spec of the single output table, 
        // the table will have three columns:
        DataColumnSpec[] allColSpecs = new DataColumnSpec[3];
        allColSpecs[0] = 
            new DataColumnSpecCreator("Column 0", StringCell.TYPE).createSpec();
        allColSpecs[1] = 
            new DataColumnSpecCreator("Column 1", DoubleCell.TYPE).createSpec();
        allColSpecs[2] = 
            new DataColumnSpecCreator("Column 2", IntCell.TYPE).createSpec();
        DataTableSpec outputSpec = new DataTableSpec(allColSpecs);
        // the execution context will provide us with storage capacity, in this
        // case a data container to which we will add rows sequentially
        // Note, this container can also handle arbitrary big data tables, it
        // will buffer to disc if necessary.
        BufferedDataContainer container = exec.createDataContainer(outputSpec);
        // let's add m_count rows to it
        
        RowIterator rowIter = inData[0].iterator();
        int i = 0;
        while (rowIter.hasNext()) {
            RowKey key = new RowKey("Row" + i);
            // the cells of the current row, the types of the cells must match
            // the column spec (see above)
            DataCell[] cells = new DataCell[3];
            
            String[] inputTokens = ((StringCell) (rowIter.next()
					.getCell(seqColPos))).getStringValue().split(" ");
            
            System.out.println(((StringCell) (rowIter.next()
					.getCell(seqColPos))).getStringValue());
            
            
            cells[0] = new StringCell("String_" + i); 
            cells[1] = new DoubleCell(0.5 * i); 
            cells[2] = new IntCell(i);
            DataRow row = new DefaultRow(key, cells);
            container.addRowToTable(row);
            
            // check if the execution monitor was canceled
            exec.checkCanceled();
            exec.setProgress(i / (double)rowNum, 
                "Adding row " + i);
            i++;
        }
        // once we are done, we close the container and return its table
        container.close();
        BufferedDataTable out = container.getTable();
        return new BufferedDataTable[]{out};
    }
    
    
    /**
     * From a  KNIME DataTable, we create a database
     * composed of a list of sequences
     *
     * @param inData File a KNIME DataTable
     * @param sequenceDatabase 
     * @param minSupRelative relative Minimum support
     * @throws IOException
     */
    public void loadFromDataTable(BufferedDataTable inData, SequenceDatabase sequenceDatabase, double minSupRelative) throws IOException {
        String thisLine;
        try {
            int sequenceID=1;
            //For each line
            RowIterator rowIter = inData.iterator();
            int i = 0;
            while (rowIter.hasNext()) {
            	thisLine = ((StringCell) (rowIter.next().getCell(seqColPos))).getStringValue();
                // If the line is not a comment line
                if (thisLine.charAt(0) != '#') {
                    // we read it and add it as a sequence
                	sequenceDatabase.addSequence(thisLine.split(" "),sequenceID);
                    sequenceID++;
                }
            }
            double minSupAbsolute = (int) Math.ceil(minSupRelative * sequences.size());
            //We get the set of items
            Set<Item> frequent = frequentItems.keySet();
            //And prepare a list to keep the non-frequent ones
            Set<Item> toRemove = new HashSet<Item>();
            for (Item frecuente : frequent) {
                if ((frequentItems.get(frecuente)).cardinality() < minSupAbsolute) {
                    toRemove.add(frecuente);
                }
            }
            //We remove from the original set those non frequent items
            for(Item removedItem:toRemove){
                frequentItems.remove(removedItem);
            }
        } catch (Exception e) {
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
        return new DataTableSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	m_minSupSelection.saveSettingsTo(settings);
		m_SeqColumnSelection.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	m_minSupSelection.loadSettingsFrom(settings);
		m_SeqColumnSelection.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	m_minSupSelection.validateSettings(settings);
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

}

