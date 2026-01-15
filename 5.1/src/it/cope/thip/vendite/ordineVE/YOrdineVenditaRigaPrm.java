package it.cope.thip.vendite.ordineVE;

import java.math.BigDecimal;
import java.sql.SQLException;

import com.thera.thermfw.persist.*;
import it.thera.thip.vendite.ordineVE.*;
import it.thera.thip.base.articolo.Articolo;
import it.thera.thip.base.articolo.ArticoloDatiIdent;
import it.thera.thip.base.articolo.ArticoloDatiVendita;
import it.thera.thip.base.azienda.Azienda;

/**
 * <p>
 * Company: Softre Solutions<br>
 * Author: Giovanni Lumini<br>
 * Date: 15/12/2025
 * </p>
 */

/*
 * Revisions:
 * Number   Date        Owner    Description
 * 72254    15/12/2025  GLSOF3   Prima stesura
 * 72296	15/01/2025	DSSOF3	 Gestione prezzi KIT
 */

public class YOrdineVenditaRigaPrm extends OrdineVenditaRigaPrm {

	protected String iDescrizioneEtichetta;

	protected boolean iAbilitaCalcoloTotRigheSecConReset = false;

	public YOrdineVenditaRigaPrm() {
		setIdAzienda(Azienda.getAziendaCorrente());
	}

	public void setDescrizioneEtichetta(String descrizioneEtichetta) {
		this.iDescrizioneEtichetta = descrizioneEtichetta;
		setDirty();
	}

	public String getDescrizioneEtichetta() {
		return iDescrizioneEtichetta;
	}

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
					tipoCalcoloPrezzo == ArticoloDatiVendita.SUL_PRODOTTO_FINITO
					&& isAbilitaCalcoloTotRigheSecConReset()) {
				super.calcolaPrezzoDaRigheSecondarieConReset(reset);
			}else {
				return;
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
	protected void calcolaDatiVendita(OrdineVenditaPO testata) throws SQLException {
		//72296
		Articolo articolo = getArticolo();
		if (articolo != null) {
			char tipoParte = articolo.getTipoParte();
			char tipoCalcoloPrezzo = articolo.getTipoCalcPrzKit();
			if ((tipoParte == ArticoloDatiIdent.KIT_NON_GEST || tipoParte == ArticoloDatiIdent.KIT_GEST)
					&&
					tipoCalcoloPrezzo == ArticoloDatiVendita.DA_COMPONENTI) {
				recuperaCondizioniVendita(testata);
				if(!isOnDB()) {
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

	public void setEqual(Copyable obj) throws CopyException {
		super.setEqual(obj);
	}
}