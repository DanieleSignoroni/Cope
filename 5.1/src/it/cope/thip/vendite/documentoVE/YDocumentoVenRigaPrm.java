package it.cope.thip.vendite.documentoVE;

import java.math.BigDecimal;
import java.sql.SQLException;

import it.thera.thip.base.articolo.Articolo;
import it.thera.thip.base.articolo.ArticoloDatiIdent;
import it.thera.thip.base.articolo.ArticoloDatiVendita;
import it.thera.thip.vendite.documentoVE.DocumentoVenRigaPrm;
import it.thera.thip.vendite.documentoVE.DocumentoVendita;
import it.thera.thip.vendite.generaleVE.CondizioniDiVendita;

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
 * 72269    15/01/2026  DSSOF3   Prima stesura
 */

public class YDocumentoVenRigaPrm extends DocumentoVenRigaPrm {

	protected boolean iAbilitaCalcoloTotRigheSecConReset = false;

	public boolean isAbilitaCalcoloTotRigheSecConReset() {
		return iAbilitaCalcoloTotRigheSecConReset;
	}

	public void setAbilitaCalcoloTotRigheSecConReset(boolean iAbilitaCalcoloTotRigheSecConReset) {
		this.iAbilitaCalcoloTotRigheSecConReset = iAbilitaCalcoloTotRigheSecConReset;
	}

	@Override
	public void calcolaPrezzoDaRigheSecondarieConReset(boolean reset) {
		Articolo articolo = getArticolo();
		if (articolo != null) {
			char tipoParte = articolo.getTipoParte();
			char tipoCalcoloPrezzo = articolo.getTipoCalcPrzKit();
			if ((tipoParte == ArticoloDatiIdent.KIT_NON_GEST || tipoParte == ArticoloDatiIdent.KIT_GEST)
					&&
					tipoCalcoloPrezzo == ArticoloDatiVendita.SUL_PRODOTTO_FINITO) {
				if(isAbilitaCalcoloTotRigheSecConReset())
					super.calcolaPrezzoDaRigheSecondarieConReset(reset);
				else
					return;
			}
			if ((tipoParte == ArticoloDatiIdent.KIT_NON_GEST || tipoParte == ArticoloDatiIdent.KIT_GEST)
					&&
					tipoCalcoloPrezzo == ArticoloDatiVendita.DA_COMPONENTI) {
				reset = false;
			}
		}
		super.calcolaPrezzoDaRigheSecondarieConReset(reset);
	}

	@Override
	protected boolean recuperoDatiVenditaSave() {
		boolean sup = super.recuperoDatiVenditaSave();
		//72296
		Articolo articolo = getArticolo();
		if (articolo != null) {
			char tipoParte = articolo.getTipoParte();
			char tipoCalcoloPrezzo = articolo.getTipoCalcPrzKit();
			if ((tipoParte == ArticoloDatiIdent.KIT_NON_GEST || tipoParte == ArticoloDatiIdent.KIT_GEST)
					&&
					tipoCalcoloPrezzo == ArticoloDatiVendita.DA_COMPONENTI) {
				return true;
			}
		}
		//72296
		return sup;
	}

	@Override
	protected void calcolaDatiVendita(DocumentoVendita testata) throws SQLException {
		//72296
		Articolo articolo = getArticolo();
		if (articolo != null) {
			char tipoParte = articolo.getTipoParte();
			char tipoCalcoloPrezzo = articolo.getTipoCalcPrzKit();
			if ((tipoParte == ArticoloDatiIdent.KIT_NON_GEST || tipoParte == ArticoloDatiIdent.KIT_GEST)
					&&
					tipoCalcoloPrezzo == ArticoloDatiVendita.DA_COMPONENTI) {
				CondizioniDiVendita condVen = recuperaCondizioniVendita(testata);
				if(!isOnDB() && condVen != null) {
					setScontoArticolo1(condVen.getScontoArticolo1());
					setScontoArticolo2(condVen.getScontoArticolo2());
					setMaggiorazione(condVen.getMaggiorazione());
					setSconto(condVen.getSconto());
					if (condVen.getAzzeraScontiCliFor()){
						setPrcScontoIntestatario(new BigDecimal("0"));
						setPrcScontoModalita(new BigDecimal("0"));
						setScontoModalita(null);
					}else {
						setPrcScontoIntestatario(condVen.getPrcScontoIntestatario());
						setPrcScontoModalita(condVen.getPrcScontoModalita());
						setScontoModalita(condVen.getScontoModalita());
					}
				}
			}else {
				super.calcolaDatiVendita(testata);
			}
		}
		//72296
	}
}
