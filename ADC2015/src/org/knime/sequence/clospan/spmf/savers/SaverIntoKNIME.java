package org.knime.sequence.clospan.spmf.savers;

import org.knime.sequence.clospan.spmf.items.patterns.Pattern;

/**
 * This is an implementation of a class implementing the Saver interface. By
 * means of these lines, the user choose to keep his patterns in a KNIME DataTale.
 * 
 * 
**/
public class SaverIntoKNIME implements Saver {

	@Override
	public void savePattern(Pattern p) {
		// TODO Auto-generated method stub

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
