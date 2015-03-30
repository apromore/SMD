package org.prom5.exporting.fsm;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.TreeSet;

import org.prom5.exporting.Exporter;
import org.prom5.framework.models.fsm.AcceptFSM;
import org.prom5.framework.models.fsm.FSMState;
import org.prom5.framework.models.fsm.FSMTransition;
import org.prom5.framework.models.transitionsystem.PetrifyConstants;
import org.prom5.framework.util.StringNormalizer;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class FSMPetrifyExport {

	@Exporter(
			name = "Petrify file",
			help = "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:export:fsm2pfy",
			extension = "g"
	)

	public void FSMPetrifyExport(AcceptFSM fsm, OutputStream out) throws IOException {
		TreeSet<String> conditions = new TreeSet<String>();
		for (Object object: fsm.getEdges()) {
			FSMTransition transition = (FSMTransition) object;
			conditions.add(transition.getCondition());
		}
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
		String model = fsm.getName();
		if (model.length() == 0) {
			model = "unknown";
		}
		bw.write(".model " + replaceBadSymbols(model) );
                bw.newLine();
		bw.write(".dummy ");
		for (String condition: conditions) {
			if (condition.length() > 0) {
                            String normalized = StringNormalizer.normalize(condition);
				bw.write(replaceBadSymbols(normalized) + " ");
			} else {
				bw.write("_ ");
			}
		}
                bw.newLine();
		bw.write(".state graph");
                bw.newLine();
		for (Object object: fsm.getEdges()) {
			FSMTransition transition = (FSMTransition) object;
			FSMState fromState = (FSMState) transition.getSource();
			FSMState toState  = (FSMState) transition.getDest();
			bw.write("s" + fromState.getId() + " ");
			if (transition.getCondition().length() > 0) {
                            String normalized = StringNormalizer.normalize(transition.getCondition());
				bw.write(replaceBadSymbols(normalized));
			} else {
				bw.write("_");
			}
			bw.write(" s" + toState.getId());
                        bw.newLine();
		}
		bw.write(".marking {s" + fsm.getStartState().getId() + "}");
                bw.newLine();
		bw.write(".end");
                bw.newLine();
		bw.close();
	}

	public static String replaceBadSymbols(String st) {
		int newline = st.indexOf("\\n");
		if (newline != -1) {
			st = st.substring(0, newline);
		}

		for (String badSymbol: PetrifyConstants.BadSymbolsMap.keySet()) {
			st = st.replace(badSymbol, PetrifyConstants.BadSymbolsMap.get(badSymbol));
		}
		return st;
	}
}
