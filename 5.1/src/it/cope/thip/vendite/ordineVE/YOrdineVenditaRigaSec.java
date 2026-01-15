package it.cope.thip.vendite.ordineVE;

import java.math.BigDecimal;
import java.sql.SQLException;

import it.cope.thip.base.comuniVenAcq.YCondizioniDiVenditaParams;
import it.cope.thip.vendite.generaleVE.YCondizioniDiVendita;
import it.thera.thip.base.articolo.Articolo;
import it.thera.thip.base.articolo.ArticoloDatiIdent;
import it.thera.thip.base.articolo.ArticoloDatiVendita;
import it.thera.thip.base.comuniVenAcq.CondizioniDiVenditaParams;
import it.thera.thip.base.comuniVenAcq.DocumentoOrdineRiga;
import it.thera.thip.vendite.ordineVE.OrdineVenditaPO;
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
 * 72296    15/01/2026  DSSOF3   Prima stesura
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
				if ((tipoParte == ArticoloDatiIdent.KIT_NON_GEST || tipoParte == ArticoloDatiIdent.KIT_GEST)) {
					((YCondizioniDiVenditaParams)cdvParams).setConsideraSoloListinoGenericoDaComp(true);
				}
			}
		}
	}

	@Override
	public void calcolaDatiVendita(OrdineVenditaPO testata) throws SQLException {
		super.calcolaDatiVendita(testata);
		OrdineVenditaRigaPrm rigaPrm = getRigaPrimaria();
		if(rigaPrm != null) {
			Articolo articolo = rigaPrm.getArticolo();
			char tipoCalcoloPrezzo = articolo.getTipoCalcPrzKit();
			if (articolo != null) {
				if (condVen != null 
						&& condVen instanceof YCondizioniDiVendita
						&& ((YCondizioniDiVendita)condVen).isConsideraSoloListinoGenericoDaComp()
						&& !isOnDB()
						&& tipoCalcoloPrezzo == ArticoloDatiVendita.DA_COMPONENTI){
					setScontoArticolo1(new BigDecimal("0"));
					setScontoArticolo2(new BigDecimal("0"));
					setSconto(null);
					setPrcScontoIntestatario(new BigDecimal("0"));
					setPrcScontoModalita(new BigDecimal("0"));
					setScontoModalita(null);
				}
			}
		}
	}

	@Override
	public int save() throws SQLException {
		DocumentoOrdineRiga rigaPrm = getRigaPrimaria();
		char tipoParte = rigaPrm.getArticolo().getTipoParte();
		char tipoCalcoloPrezzo = rigaPrm.getArticolo().getTipoCalcPrzKit();
		if ((tipoParte == ArticoloDatiIdent.KIT_NON_GEST ||
				tipoParte == ArticoloDatiIdent.KIT_GEST)
				&& tipoCalcoloPrezzo == ArticoloDatiVendita.SUL_PRODOTTO_FINITO
				&& !isOnDB()) {
			calcolaDatiVendita((OrdineVenditaPO) rigaPrm.getTestata());
		}
		int rc = super.save();
		return rc;
	}

	@Override
	public void verificaAzzeraPrezzo() {
		if (getRigaPrimaria() != null) {
			if (getRigaPrimaria().getArticolo().getTipoCalcPrzKit() == ArticoloDatiVendita.SUL_PRODOTTO_FINITO) {
				return;
			}
		}
		super.verificaAzzeraPrezzo();
	}
}