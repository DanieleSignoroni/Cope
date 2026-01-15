package it.cope.thip.vendite.generaleVE;

import java.util.Iterator;

import it.cope.thip.base.comuniVenAcq.YCondizioniDiVenditaParams;
import it.thera.thip.base.comuniVenAcq.CondizioniDiVenditaParams;
import it.thera.thip.base.listini.TipologieTestateRighe;
import it.thera.thip.vendite.generaleVE.CondizioniDiVendita;
import it.thera.thip.vendite.generaleVE.RicercaCondizioniDiVendita;

/**
 *
 * <p></p>
 *
 * <p>
 * Company: Softre Solutions<br>
 * Author: Daniele Signoroni<br>
 * Date: 15/01/2026
 * </p>
 */

/*
 * Revisions:
 * Number   Date        Owner    Description
 * 72296    15/01/2026  DSSOF3   Prima stesura
 */

public class YRicercaCondizioniDiVendita extends RicercaCondizioniDiVendita {

	@Override
	public void completaDatiCondVenditaPers(CondizioniDiVenditaParams condVenParams, CondizioniDiVendita cdv) {
		super.completaDatiCondVenditaPers(condVenParams, cdv);
		if(condVenParams instanceof YCondizioniDiVenditaParams && cdv instanceof YCondizioniDiVendita) {
			((YCondizioniDiVendita)cdv).setConsideraSoloListinoGenericoDaComp(((YCondizioniDiVenditaParams) condVenParams).isConsideraSoloListinoGenericoDaComp());
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void yOrdinaTestateSelezionate(Iterator iter) {
		super.yOrdinaTestateSelezionate(iter);
		if(getCondizioniDiVendita() != null 
				&& getCondizioniDiVendita() instanceof YCondizioniDiVendita
				&& ((YCondizioniDiVendita)getCondizioniDiVendita()).isConsideraSoloListinoGenericoDaComp()) {
			while(iter.hasNext()) {
				char testata =((String)iter.next()).toCharArray()[0];
				if(testata != TipologieTestateRighe.GENERICO) {
					iter.remove();
				}
			}
		}
	}
}
