package org.knime.sequence.clospan.spmf.items;

import org.knime.sequence.clospan.spmf.items.abstractions.Abstraction_Qualitative;
import org.knime.sequence.clospan.spmf.items.creators.ItemAbstractionPairCreator;
import org.knime.sequence.clospan.spmf.items.patterns.PatternCreator;



/**
 *
 * @author antonio
 */
public class RemoveStatics {

    public static void clear() {
        ItemAbstractionPairCreator.sclear();
        Abstraction_Qualitative.clear();
        PatternCreator.sclear();
    }
}
