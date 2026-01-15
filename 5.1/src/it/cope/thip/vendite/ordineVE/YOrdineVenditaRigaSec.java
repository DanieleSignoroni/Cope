package it.cope.thip.vendite.ordineVE;

import it.cope.thip.base.comuniVenAcq.YCondizioniDiVenditaParams;
import it.thera.thip.base.articolo.Articolo;
import it.thera.thip.base.articolo.ArticoloDatiIdent;
import it.thera.thip.base.articolo.ArticoloDatiVendita;
import it.thera.thip.base.comuniVenAcq.CondizioniDiVenditaParams;
import it.thera.thip.vendite.ordineVE.OrdineVenditaRigaPrm;
import it.thera.thip.vendite.ordineVE.OrdineVenditaRigaSec;

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
 * 72XXX    15/01/2026  DSSOF3   Prima stesura
 */

public class YOrdineVenditaRigaSec extends OrdineVenditaRigaSec {

	@Override
	public void impostaParamsCVPers(CondizioniDiVenditaParams cdvParams) {
		super.impostaParamsCVPers(cdvParams);
		OrdineVenditaRigaPrm rigaPrm = getRigaPrimaria();
		if(rigaPrm != null) {
			Articolo articolo = rigaPrm.getArticolo();
			if (articolo != null) {
				char tipoParte = articolo.getTipoParte();
				char tipoCalcoloPrezzo = articolo.getTipoCalcPrzKit();
				if ((tipoParte == ArticoloDatiIdent.KIT_NON_GEST || tipoParte == ArticoloDatiIdent.KIT_GEST)
						&&
						tipoCalcoloPrezzo == ArticoloDatiVendita.DA_COMPONENTI) {
					((YCondizioniDiVenditaParams)cdvParams).setConsideraSoloListinoGenericoDaComp(true);
				}
			}
		}
	}

}
