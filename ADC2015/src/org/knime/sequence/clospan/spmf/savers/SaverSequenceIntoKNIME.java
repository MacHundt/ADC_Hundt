package org.knime.sequence.clospan.spmf.savers;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.sequence.clospan.spmf.items.patterns.Pattern;

/**
 * This is an implementation of a class implementing the Saver interface. By
 * means of these lines, the user choose to keep his patterns in a KNIME DataTale.
 * 
 * 
**/
public class SaverSequenceIntoKNIME implements Saver {

	private BufferedDataContainer container;
	private ExecutionContext exec;
	private boolean outputSequenceIdentifiers;
	private static int rowID = 0;
	private int rowNum;
	
	public SaverSequenceIntoKNIME( ExecutionContext exec, 
			boolean outputSequenceIdentifiers, int rowNum) {
		this.exec = exec;
		this.outputSequenceIdentifiers = outputSequenceIdentifiers;
		this.rowNum = rowNum;
        // the data table spec of the single output table, 
        // the table will have three columns:
        DataColumnSpec[] allColSpecs = new DataColumnSpec[3];
        	if (outputSequenceIdentifiers) {
        		allColSpecs = new DataColumnSpec[4];
        	}
        	// the sequence
        allColSpecs[0] = 
            new DataColumnSpecCreator("Sequence", StringCell.TYPE).createSpec();
        // the support
        allColSpecs[1] = 
            new DataColumnSpecCreator("Support", IntCell.TYPE).createSpec();
        allColSpecs[2] = 
            new DataColumnSpecCreator("itemNumber", IntCell.TYPE).createSpec();
        if (outputSequenceIdentifiers) {
        	 allColSpecs[3] = 
        			 new DataColumnSpecCreator("Sequence Identifiers", StringCell.TYPE).createSpec();
        }
        DataTableSpec outputSpec = new DataTableSpec(allColSpecs);
		this.container = exec.createDataContainer(outputSpec);
	}

	public BufferedDataContainer getContainer() {
		return container;
	}

	@Override
	public void savePattern(Pattern p) {
         
        StringBuilder r = new StringBuilder("");
        r.append(p.toStringToFile(outputSequenceIdentifiers));
        
        RowKey key = new RowKey("Row" + rowID++);
		DataCell[] cells = new DataCell[3];
		if (outputSequenceIdentifiers) {
			cells = new DataCell[4];
		}
		String[] tokens = r.toString().split("#SUP:");
		String sequence = tokens[0] + "-2";
		int support;
		String sid = "";
		if (outputSequenceIdentifiers) {
			support = Integer.parseInt(tokens[1].split("#SID: ")[0].trim());
			sid += tokens[1].split("#SID: ")[1].trim();
		}
		else {
			support = Integer.parseInt(tokens[1].trim());
		}
		
		
		cells[0] = new StringCell(sequence);
		cells[1] = new IntCell(support);
		cells[2] = new IntCell((sequence.split(" -1").length)-1);
		if (outputSequenceIdentifiers) {
			cells[3] = new StringCell(sid);
		}
		DataRow row = new DefaultRow(key, cells);
		container.addRowToTable(row);
		
       // check if the execution monitor was canceled
       try {
		exec.checkCanceled();
	} catch (CanceledExecutionException e) {
		e.printStackTrace();
	}
       exec.setProgress(rowID / (double)rowNum, 
           "Adding row " + rowID);
      ;
        
       
        // once we are done, we close the container and return its table
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public String print() {
		 return "Content at KNIME DataTable";
	}

}
