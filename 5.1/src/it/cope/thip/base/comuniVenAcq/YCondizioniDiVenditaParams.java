package it.cope.thip.base.comuniVenAcq;

import it.thera.thip.base.comuniVenAcq.CondizioniDiVenditaParams;

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

public class YCondizioniDiVenditaParams extends CondizioniDiVenditaParams {

	protected boolean iConsideraSoloListinoGenericoDaComp = false;

	public boolean isConsideraSoloListinoGenericoDaComp() {
		return iConsideraSoloListinoGenericoDaComp;
	}

	public void setConsideraSoloListinoGenericoDaComp(boolean iConsideraSoloListinoGenericoDaComp) {
		this.iConsideraSoloListinoGenericoDaComp = iConsideraSoloListinoGenericoDaComp;
	}
	
	
}
