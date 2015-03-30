package org.prom5.importing.coloredcontrolflownet;

import java.io.Reader;

import org.prom5.framework.models.coloredcontrolflownet.ColoredControlFlowNet;
import org.prom5.framework.models.coloredcontrolflownet.PlaceType;
import org.prom5.framework.models.coloredcontrolflownet.inscription.ArcInscription;
import org.prom5.importing.coloredcontrolflownet.arcinscriptionparser.Scanner;
import org.prom5.importing.coloredcontrolflownet.arcinscriptionparser.parser;
import org.prom5.importing.simplecpn.SimpleCPNParser;

public class ColoredControlFlowNetParser
		extends
		SimpleCPNParser<parser, PlaceType, Object, ArcInscription, Object, ColoredControlFlowNet> {

	@Override
	public parser getArcParser(Reader reader) {
		return new parser(new Scanner(reader));
	}

	@Override
	public ColoredControlFlowNet getNewEmptyNet() {
		return new ColoredControlFlowNet();
	}

	@Override
	public PlaceType getPlaceAnnotation(String type) {
		if ("PID".equals(type))
			return PlaceType.PID;
		else if ("STATE".equals(type))
			return PlaceType.STATE;
		return null;
	}
}