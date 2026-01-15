/*
 * @(#)OffertaClienteRiga.java
 */

/**
 * OffertaClienteRiga
 *
 * <br></br><b>Copyright (C) : Thera s.p.a.</b>
 * @author MARZOUK FATTOUMA 12/02/2009 at 10:04:36
 */
/*
 * Revisions:
 * Date          Owner      Description
 * 13/02/2009    Wizard     Codice generato da Wizard
 * Number      Date          Owner      Description
 * 10690       06/04/2009    FM         Updates for prezzo
 * 11685       08/12/2009    FM         Prezzo prm calculato da kit in caso da generazione riga secondario
 * 16893       22/03/2013    Ichrak     Gancio per personalizzazioni
 * 17490       27/03/2013    Linda      Modificato il metodo checkArticoloNoKit().
 * 18753       17/06/2015    Linda      Redefinire metodo checkIdEsternoConfig().
 * 22729       18/12/2015    Linda      Nella riga secondaria inserire il listino di prezzi indicato sulla riga primaria.
 * 22839       15/01/2016    Linda      Redefine metodo getEntityRiga().
 * 24613       07/12/2016    Linda      Gestione il salvataggio del dettaglio riga valore configurazione.
 * 27649       02/07/2018    LTB     Aggiunta un controllo bloccante che impedisca di inserire una riga con quantità non intera se l'um è gestita a quantità intera. 
 * 28305  	   26/11/2018	 SZ		    In caso delle righe secondarie con caricamento manuale impostare il prezzo a zero in caso non sia trovato su listino.
 * 29096	   02/04/2019	 SZ			Commentare il metodo che assegna l'attributo Rif UM nella copia della riga secondaria .
 * 33905       02/07/2021    SZ			Nel caso di kit non gestito a magazzino il costo deve essere la somma dei costi delle righe secondarie 
 * 37244  	   08/12/2022    YBA        Corregere il problemea perché del copia un ordine cambiando la causale e le codizioni di vendita sono impostate Da documento gli sconti testata e la provvigione 1 delle righe non deveno essere riprese dalla testata.
 * 37556       18/01/2023    YBA        Corregere il problemea perché in copia ordine lo sconto intestatario presente sul cliente non viene messo su tutte le righe le secondarie. 
 * 38150       21/03/2023    YBA        Nella riga secondaria inserire il centro costo indicato sulla riga primaria.
 * 44499       13/01/2025    KD         Correggere passaggi valiso /annullato e sospeso e viceversa per gestire correttamente lo stato delle righe secondarie
 * 44784  	   02/05/2025  	 RA	  		Rendi la ConfigArticoloPrezzo persistent
 */

package it.thera.thip.vendite.offerteCliente;

import java.math.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;

import com.thera.thermfw.base.*;
import com.thera.thermfw.common.*;
import com.thera.thermfw.persist.*;

import it.cope.thip.vendite.offerteCliente.YOffertaClienteRigaPrm;
import it.thera.thip.base.agentiProvv.Agente;
import it.thera.thip.base.articolo.*;
import it.thera.thip.base.cliente.Sconto;
import it.thera.thip.base.comuniVenAcq.*;
import it.thera.thip.base.comuniVenAcq.web.DatiArticoloRigaVendita;
import it.thera.thip.base.documenti.*;
import it.thera.thip.magazzino.generalemag.OggCalcoloGiaDisp;
import it.thera.thip.vendite.ordineVE.OrdineVenditaRigaPrm;
import it.thera.thip.vendite.ordineVE.OrdineVenditaRigaSec;

import com.thera.thermfw.security.Entity;

import it.thera.thip.datiTecnici.configuratore.RigaConDettaglioConf;
import it.thera.thip.datiTecnici.configuratore.SchemaCfg;
import it.thera.thip.datiTecnici.configuratore.DettRigaConfigurazione;
import it.thera.thip.base.generale.ParametroPsn;
import it.thera.thip.base.interfca.RiferimentoVociCA;
import it.thera.thip.base.interfca.SottogruppoContiCA;
import it.thera.thip.base.listini.ListinoVendita;
import it.thera.thip.datiTecnici.configuratore.DettRigaConfigurazioneOffCli;

public class OffertaClienteRigaSec
    //extends OffertaClienteRiga//Fix 24613
    extends OffertaClienteRiga implements RigaConDettaglioConf//Fix 24613
{
  protected Proxy iRigaPrimaria = new Proxy(OffertaClienteRigaPrm.class);

  protected static final String SELECT_MAX_SEQUENZA_RIGHE_SEC =
      "SELECT MAX(" +
      OffertaClienteRigaSecTM.SEQUENZA_RIGA +
      ") FROM " +
      OffertaClienteRigaSecTM.TABLE_NAME + " " +
      "WHERE " +
      OffertaClienteRigaSecTM.ID_AZIENDA + "=? AND " +
      OffertaClienteRigaSecTM.ID_ANNO_OFF + "=? AND " +
      OffertaClienteRigaSecTM.ID_NUMERO_OFF + "=? AND " +
      OffertaClienteRigaSecTM.ID_RIGA_OFF + "=?";
  protected static CachedStatement cSelectMaxSequenzaRigheSec =
      new CachedStatement(SELECT_MAX_SEQUENZA_RIGHE_SEC);

  boolean iSalvaRigaPrimaria = true;

  public OffertaClienteRigaSec()
  {
    super();
    setSpecializzazioneRiga(RIGA_SECONDARIA_PER_COMPONENTE);
    iRigheLotto = new OneToMany(OffertaClienteRigaLottoSec.class, this, 31, true);
    iRigaCollegata = new Proxy(OffertaClienteRigaSec.class);
    iNonFatturare = true;
    setCoefficienteImpiego(new BigDecimal(1.0));
    iOrdineVenditaRiga = new Proxy(it.thera.thip.vendite.ordineVE.OrdineVenditaRigaSec.class); //Fix 10685 PM
    datiArticolo = (DatiArticoloRigaVendita)Factory.createObject(DatiArticoloRigaVendita.class);//Fix 33905
    //setIdAzienda(Azienda.getAziendaCorrente());
  }

  protected TableManager getTableManager() throws java.sql.SQLException
  {
    return OffertaClienteRigaSecTM.getInstance();
  }

  protected void creaOldRiga()
  {
    iOldRiga = (OffertaClienteRigaSec)Factory.createObject(OffertaClienteRigaSec.class);
  }

  protected int eliminaRiga() throws SQLException
  {
    int rc = super.eliminaRiga();
    if (isSalvaRigaPrimaria())
      rc = salvaRigaPrimaria(rc);
    return rc;
  }

  protected DocumentoOrdineRiga getRigaDestinazionePerCopia()
  {
    return (OffertaClienteRigaSec)Factory.createObject(OffertaClienteRigaSec.class);
  }

  protected void impostaStatoAvanzamento()
  {
    super.impostaStatoAvanzamento();
    if (this.getStatoAvanzamento() == StatoAvanzamento.PROVVISORIO)
    {
      this.getRigaPrimaria().setStatoAvanzamento(StatoAvanzamento.PROVVISORIO);
    }
  }

  protected int eliminaRigaOmaggioCollegata(String key) throws SQLException
  {
    int rc = 0;

    OffertaClienteRigaSec rigaOmaggio =
        (OffertaClienteRigaSec)Factory.createObject(OffertaClienteRigaSec.class);
    rigaOmaggio.setKey(key);
    if (rigaOmaggio.retrieve())
    {

      rc = rigaOmaggio.delete();
    }

    return rc;
  }

  public OffertaClienteRigaPrm getRigaPrimaria()
  {
    return (OffertaClienteRigaPrm)iRigaPrimaria.getObject();
  }

  public void setRigaPrimaria(OffertaClienteRigaPrm rigaPrimaria)
  {
    iRigaPrimaria.setObject(rigaPrimaria);
    setDirty();
    setOnDB(false);
  }

  public String getRigaPrimariaKey()
  {
    return iRigaPrimaria.getKey();
  }

  public void setRigaPrimariaKey(String key)
  {
    iRigaPrimaria.setKey(key);
    setDirty();
  }

// father
  public String getFatherKey()
  {
    return iRigaPrimaria.getKey();
  }

  public void setFatherKey(String key)
  {
    iRigaPrimaria.setKey(key);
  }

  public void setFather(PersistentObject father)
  {
    iRigaPrimaria.setObject(father);
  }

  public String getOrderByClause()
  {
    return super.getOrderByClause();
  }

  public String getIdAzienda()
  {
    String key = iRigaPrimaria.getKey();
    String idAzienda = KeyHelper.getTokenObjectKey(key, 1);
    return idAzienda;
  }

  public void setIdAzienda(String idAzienda)
  {
    String key = iRigaPrimaria.getKey();
    iRigaPrimaria.setKey(KeyHelper.replaceTokenObjectKey(key, 1, idAzienda));
    setDirty();
    setOnDB(false);

    getCommentHandlerManager().setOwnerKeyChanged();
    iRigheLotto.setFatherKeyChanged();
    setIdAziendaInternal(idAzienda);
  }

  public void setAziendaKey(String azienda)
  {
    setIdAzienda(azienda);
  }

  public String getAnnoDocumento()
  {
    String key = iRigaPrimaria.getKey();
    String annoDocumento = KeyHelper.getTokenObjectKey(key, 2);
    return annoDocumento;
  }

  public void setAnnoDocumento(String annoDocumento)
  {
    String key = iRigaPrimaria.getKey();
    iRigaPrimaria.setKey(KeyHelper.replaceTokenObjectKey(key, 2, annoDocumento));
    setDirty();
    setOnDB(false);
    getCommentHandlerManager().setOwnerKeyChanged();
    iRigheLotto.setFatherKeyChanged();
  }

  public String getNumeroDocumento()
  {
    String key = iRigaPrimaria.getKey();
    String numeroDocumento = KeyHelper.getTokenObjectKey(key, 3);
    return numeroDocumento;
  }

  public void setNumeroDocumento(String numeroDocumento)
  {
    String key = iRigaPrimaria.getKey();
    iRigaPrimaria.setKey(KeyHelper.replaceTokenObjectKey(key, 3, numeroDocumento));
    setDirty();
    setOnDB(false);

    getCommentHandlerManager().setOwnerKeyChanged();
    iRigheLotto.setFatherKeyChanged();
  }

  public Integer getNumeroRigaDocumento()
  {

    String key = iRigaPrimaria.getKey();
    String nrd = KeyHelper.getTokenObjectKey(key, 4);
    return (nrd == null) ? null : new Integer(nrd);
  }

  public void setNumeroRigaDocumento(Integer numeroRigaDocumento)
  {
    String key = iRigaPrimaria.getKey();
    iRigaPrimaria.setKey(KeyHelper.replaceTokenObjectKey(key, 4, numeroRigaDocumento));
    setDirty();
    setOnDB(false);
    getCommentHandlerManager().setOwnerKeyChanged();
    iRigheLotto.setFatherKeyChanged();
  }

  protected void componiChiave()
  {
    try
    {
      int dett = Numerator.getNextInt("OffCliRigaPrm");
      if (dett == 0)
      {
        dett = Numerator.getNextInt("OffCliRigaPrm");

      }
      setDettaglioRigaDocumento(new Integer(dett));
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public void completaBO()
   {
     if (getRigaPrimaria() != null && getRigaPrimaria().getNumeroRigaDocumento() != null)
     {
       setSequenzaRiga(getSequenzaNuovaRiga(getRigaPrimaria()));
     }

     OffertaClienteRigaPrm rigaPrm = getRigaPrimaria();
     setCausaleRiga(rigaPrm.getCausaleRiga());
     super.completaBO();

     Date dataConsRich = rigaPrm.getDataConsegnaRichiesta();
     setDataConsegnaRichiesta(rigaPrm.getDataConsegnaRichiesta());
     Date dataConsConf = rigaPrm.getDataConsegnaConfermata();
     setDataConsegnaConfermata(dataConsConf);
     //Fix 10685 PM Inizio
     //Date dataConsProd = rigaPrm.getDataConsegnaProduzione();
     //setDataConsegnaProduzione(dataConsProd);
   //Fix 10685 PM Fine
     if (dataConsRich != null)
     {
       int[] datiSett = TimeUtils.getISOWeek(dataConsRich);
       String sett =
           DocumentoOrdineTestata.getSettimanaFormattata(
           datiSett[0], datiSett[1]
           );
       setSettConsegnaRichiesta(sett);
     }

     if (dataConsConf != null)
     {
       int[] datiSett = TimeUtils.getISOWeek(dataConsConf);
       String sett =
           DocumentoOrdineTestata.getSettimanaFormattata(
           datiSett[0], datiSett[1]
           );
       setSettConsegnaConfermata(sett);
     }

   //Fix 10685 PM Inizio
     /*if (dataConsProd != null)
     {
       int[] datiSett = TimeUtils.getISOWeek(dataConsProd);
       String sett =
           DocumentoOrdineTestata.getSettimanaFormattata(
           datiSett[0], datiSett[1]
           );
       setSettConsegnaProduzione(sett);
     }*/
   //Fix 10685 PM Fine

     OffertaCliente testata = (OffertaCliente)getTestata();
     Articolo articoloRigaPrm = rigaPrm.getArticolo();

     //setListinoPrezzi(testata.getListinoPrezzi());//Fix 22729
     setListinoPrezzi(rigaPrm.getListinoPrezzi());//Fix 22729
     setPrcScontoIntestatario(testata.getPrcScontoIntestatario());
     setPrcScontoModalita(testata.getPrcScontoModalita());
     setScontoModalita(testata.getScontoTabellare());
     setCommessa(rigaPrm.getCommessa());
     //setUMRif(articoloRigaPrm.getUMDefaultVendita());/Fix 29096
     setCentroCosto(rigaPrm.getCentroCosto());//Fix 38150

   }


  public BigDecimal getServizioQtaVenditaPrm()
  {
    try
    {
      return getRigaPrimaria().getQtaInUMRif();
    }
    catch (Exception ex)
    {
      return new BigDecimal(0.0);
    }
  }

  public BigDecimal getServizioQtaUMPrmPrm()
  {
    try
    {
      return getRigaPrimaria().getQtaInUMPrmMag();
    }
    catch (Exception ex)
    {
      return new BigDecimal(0.0);
    }
  }

  public void propagaDatiTestata(SpecificheModificheRigheOrd spec)
  {
    if (spec.isSezioneEvasione())
    {
      if (spec.getAzioneCommessa() != SpecificheModificheRigheOrd.AZ_GENER_NESSUNA)
      {
        propagaCommessa(spec);
      }
      if (spec.getAzioneCentroCosto() != SpecificheModificheRigheOrd.AZ_GENER_NESSUNA)
      {
        propagaCentroCosto(spec);
      }
    }
  }

  public static synchronized int getSequenzaNuovaRiga(OffertaClienteRigaPrm rigaPrm)
  {
    try
    {
      Database db = ConnectionManager.getCurrentDatabase();
      db.setString(
          cSelectMaxSequenzaRigheSec.getStatement(), 1, rigaPrm.getIdAzienda()
          );
      db.setString(
          cSelectMaxSequenzaRigheSec.getStatement(), 2, rigaPrm.getAnnoDocumento()
          );
      db.setString(
          cSelectMaxSequenzaRigheSec.getStatement(), 3, rigaPrm.getNumeroDocumento()
          );
      db.setString(
          cSelectMaxSequenzaRigheSec.getStatement(),
          4,
          rigaPrm.getNumeroRigaDocumento().toString()
          );

      ResultSet rs = cSelectMaxSequenzaRigheSec.executeQuery();
      int ret = (rs.next()) ? rs.getInt(1) + 1 : 0;
      rs.close();

      return ret;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return 0;
    }
  }

  public DocumentoBase getTestata()
  {
    DocumentoBase ret = null;
    DocumentoBaseRiga riga = (DocumentoBaseRiga)getRigaPrimaria();
    if (riga != null)
    {
      ret = riga.getTestata();
    }
    return ret;
  }

  public String getTestataKey()
  {
    String ret = null;
    DocumentoBaseRiga riga = (DocumentoBaseRiga)getRigaPrimaria();
    if (riga != null)
    {
      ret = riga.getTestataKey();
    }
    return ret;
  }

  protected OrdineRigaLotto creaLotto()
  {
    return (OffertaClienteRigaLotto)Factory.createObject(OffertaClienteRigaLottoSec.class);
  }

  //...FIX  16893 (resi public)
  //protected void setSalvaRigaPrimaria(boolean salvaRigaPrimaria)
  public void setSalvaRigaPrimaria(boolean salvaRigaPrimaria)
  {
    iSalvaRigaPrimaria = salvaRigaPrimaria;
  }

  //protected boolean isSalvaRigaPrimaria()
  public boolean isSalvaRigaPrimaria()
  {
    return iSalvaRigaPrimaria;
  }
  //...FIX  16893 (resi public) fine

  protected void calcolaMovimentiPortafoglio()
  {
    super.calcolaMovimentiPortafoglio();
  }

  public Vector checkAll(BaseComponentsCollection components)
  {
    Vector errors = new Vector();
   //Fix 28305 inizio 
    verificaAzzeraPrezzo();
    if (getPrezzoConcordato() != null &&  getPrezzoConcordato().equals(new BigDecimal(0.0))) 
    	components.getComponent("Prezzo").setValue("0");
    
    //Fix 28305 inizio
    errors = super.checkAll(components);

    ErrorMessage em = null;

    em = checkArticoloNoKit();
    if (em != null)
      errors.addElement(em);
    
    //27649 inizio
    em = checkQtaInUMVen();
    if (em != null)
      errors.addElement(em);

    em = checkQtaInUMPrmMag();
    if (em != null)
      errors.addElement(em);

    em = checkQtaInUMSecMag();
    if (em != null)
      errors.addElement(em);
    //27649 fine     

    return errors;
  }

  public ErrorMessage checkArticoloNoKit()
  {
    Articolo articolo = getArticolo();
    if (articolo != null &&
        //(articolo.getTipoParte() == ArticoloDatiIdent.KIT_GEST ||//Fix 17490
         articolo.getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST)
    {
      return new ErrorMessage("THIP_BS203");
    }

    return null;
  }

  public ErrorMessage checkIdAssogIVA()
  {
    if (!isNonFatturare() && getIdAssogIVA() == null)
      return new ErrorMessage("BAS0000000");
    return null;
  }

  public boolean isDaRicalcolare()
  {
    DocumentoOrdineRiga rigaPrm = getRigaPrimaria();
    char tipoParte = rigaPrm.getArticolo().getTipoParte();
    char tipoCalcoloPrezzo = rigaPrm.getArticolo().getTipoCalcPrzKit();
    if ((tipoParte == ArticoloDatiIdent.KIT_NON_GEST ||
         tipoParte == ArticoloDatiIdent.KIT_GEST)
        && tipoCalcoloPrezzo == ArticoloDatiVendita.DA_COMPONENTI)
      return confrontaValoriOldRiga();
    return false;
  }

  public int save() throws SQLException
  {
    boolean newRow = !isOnDB(); //Fix 24613
    
    int rc = super.save();

    verificaAzzeraPrezzo();

    if (isSalvaRigaPrimaria())
    {
      boolean totaliDaRicalcolare = isDaRicalcolare();
      //72296 Softre <
      DocumentoOrdineRiga rigaPrm = getRigaPrimaria();
		char tipoParte = rigaPrm.getArticolo().getTipoParte();
		char tipoCalcoloPrezzo = rigaPrm.getArticolo().getTipoCalcPrzKit();
		if (!totaliDaRicalcolare && (tipoParte == ArticoloDatiIdent.KIT_NON_GEST ||
				tipoParte == ArticoloDatiIdent.KIT_GEST)
				&& tipoCalcoloPrezzo == ArticoloDatiVendita.SUL_PRODOTTO_FINITO
				&& newRow) {
			totaliDaRicalcolare = true;
			if(getRigaPrimaria() instanceof YOffertaClienteRigaPrm) {
				((YOffertaClienteRigaPrm)getRigaPrimaria()).setAbilitaCalcoloTotRigheSecConReset(true);
			}
		}
      //72296 Softre >
      if (totaliDaRicalcolare)
        getRigaPrimaria().calcolaPrezzoDaRigheSecondarieSenzaReset();

      rc = salvaRigaPrimaria(rc);

      if (totaliDaRicalcolare)
      {
        ((OffertaCliente)this.getTestata()).calcolaCostiValoriOrdine(false);
        rc = salvaTestata(rc);
      }

    }
        //Fix 24613 inizio
        if (rc >= 0) {
          int dettCfgRett = salvaDettRigaConf(newRow);
          if (dettCfgRett < 0)
            rc = dettCfgRett;
          else
            rc = rc + dettCfgRett;
        }
        //Fix 24613 fine
        
    return rc;
  }
//10690
  public void verificaAzzeraPrezzo()
  {
    if (getRigaPrimaria() != null)
    {
      if (getRigaPrimaria().getArticolo().getTipoCalcPrzKit() != ArticoloDatiVendita.DA_COMPONENTI)
      {
        BigDecimal prezzo = getPrezzoConcordato();

        if (prezzo == null)
        {
          setPrezzoConcordato(new BigDecimal(0.0));
        }
      }
    }
  }

  protected int salvaRigaPrimaria(int rc) throws SQLException
  {
    if (rc >= 0)
    {
      OffertaClienteRigaPrm rigaPrm = (OffertaClienteRigaPrm)getRigaPrimaria();
      rigaPrm.setSalvaRigheSecondarie(false);
      int rc1 = rigaPrm.save();
      rigaPrm.setSalvaRigheSecondarie(true);
      rc = rc1 >= 0 ? rc + rc1 : rc1;
    }
    return rc;
  }

  protected void inizializzaParteIntestatario()
  {
  }

  protected boolean recuperoDatiVenditaArticoloPrm()
  {

    Articolo articoloPrm = getRigaPrimaria().getArticolo();

    char spclRiga = getSpecializzazioneRiga();
    char tipoCalcoloPrezzo = articoloPrm.getTipoCalcPrzKit();

    return
        (spclRiga == RIGA_SECONDARIA_DA_FATTURARE) ||
        (spclRiga == RIGA_SECONDARIA_PER_COMPONENTE && tipoCalcoloPrezzo == ArticoloDatiVendita.DA_COMPONENTI);

  }

  protected boolean recuperoDatiVenditaSave()
  {
	  
    return recuperoDatiVenditaArticoloPrm() &&
        isServizioCalcDatiVendita() &&
        !isRigaOfferta() &&
        getTipoRiga() != TipoRiga.SPESE_MOV_VALORE;
  }

 /* protected int salvaParteIntestatario(int rc) throws SQLException
  {
    return rc;
  }*/

  protected void setRigaPrimariaPerCopia(DocumentoOrdineRiga rigaPrm)
  {
    setRigaPrimaria((OffertaClienteRigaPrm)rigaPrm);
  }


  //  Fix 11685 begin
        public boolean effettuareIlControllo(List lista){
          return effettuareIlControllo(lista, null);
        }

        public boolean effettuareIlControllo(List lista, QuantitaInUM qta){
          boolean ritorno = false;
          OggCalcoloGiaDisp ogg = null;
          if (getTipoRiga() != TipoRiga.SPESE_MOV_VALORE && !isRigaMerceValore() && this.isDaAggiornare(qta)){
            Articolo articoloPrm = this.getRigaPrimaria().getArticolo();
            boolean kit = false;
            if (articoloPrm != null) {
              kit = articoloPrm.getTipoParte() ==
                  ArticoloDatiIdent.KIT_NON_GEST;
            }
            if (kit) {
              ogg = (OggCalcoloGiaDisp) Factory.createObject(
                  OggCalcoloGiaDisp.class);
              ogg.caricati(this, null, qta);
              ogg.setTipoControllo(OggCalcoloGiaDisp.TP_CTL_DISPONIBILITA);
              ritorno = true;
              lista.add(ogg);
            }
          }
          return ritorno;
        }

        public boolean isDaAggiornare(QuantitaInUM qta){
          if (qta==null)
            return super.isDaAggiornare();
          else
            return true;
        }

        // Fix 11685 end

        //Fix 18753 inizio
        public ErrorMessage checkIdEsternoConfig() {
          ErrorMessage error = super.checkIdEsternoConfig();
          if (error != null && !error.equals(""))
            return error;

          if (isOnDB() && getArticolo() != null && getArticolo().isConfigurato() &&
              getConfigurazione() == null && iOldRiga != null && iOldRiga.getStatoAvanzamento() != StatoAvanzamento.DEFINITIVO
              && getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO) {
            return new ErrorMessage("THIP40T398", getIdArticolo());
          }

          return null;
        }
      //Fix 18753 fine

      //Fix 22839 inizio
      protected Entity getEntityRiga() {
        try {
          return Entity.elementWithKey("OffCliRigaSec", Entity.NO_LOCK);
        }
        catch (Exception ex) {
          return null;
        }
      }
      //Fix 22839 fine
      //Fix 24613 inizio
      public int salvaDettRigaConf(boolean newRow) throws SQLException {
        String psnSalvaDettRigaCfg = ParametroPsn.getValoreParametroPsn("Std.vendite", "SalvaDettaglioRigaCfg");
        if (psnSalvaDettRigaCfg == null || psnSalvaDettRigaCfg.equals("") || psnSalvaDettRigaCfg.equals("N"))
          return 0;

        int yrit = 0;
        Articolo art = getArticolo();
        boolean isValorizzaConf = false;
        if (art != null) {
          SchemaCfg schemaCfg = art.getSchemaCfg();
          if (schemaCfg != null)
            isValorizzaConf = schemaCfg.getValorizzaConfig();
        }
        DettRigaConfigurazione dettRigaCfg = null;
        if (getConfigurazione() != null && isValorizzaConf) {
          char provPrezzo = getProvenienzaPrezzo();
          if (newRow) {
            if (provPrezzo != TipoRigaRicerca.MANUALE) {
              dettRigaCfg = dammiOggettoGestione();
              if ((isInCopiaRiga && !iControlloRicalVlrDettCfg) || (((DocumentoOrdineTestata)this.getTestata()).isInCopia() && !isCondVenCopiaDaRicalcolare))
                yrit = dettRigaCfg.copiaDettRigaCfg(this, rigaDaCopiareKey);
              else
                yrit = dettRigaCfg.recuperaDettRigaConfigurazione(this);
            }
          }
          else if (isConfigurazioneCambiata() || isListinoCambiato() || provPrezzo == TipoRigaRicerca.MANUALE || iControlloRicalVlrDettCfg) {
            dettRigaCfg = dammiOggettoGestione();
            int rit = dettRigaCfg.cancellaDettagliCfg(this);
            if (rit >= 0 && provPrezzo != TipoRigaRicerca.MANUALE) {
              rit = dettRigaCfg.recuperaDettRigaConfigurazione(this);
              if (rit >= 0)
                yrit = yrit + rit;
            }
          }
        }
        return yrit;
      }

      public boolean isListinoCambiato() {
        OffertaClienteRiga ocr = (OffertaClienteRiga) iOldRiga;
        if (ocr == null)
          return false;
        //return!(this.getIdListinoPrezzi().equals(ocr.getIdListinoPrezzi()));//Fix 24705
        return!(Utils.compare(this.getIdListinoPrezzi(),ocr.getIdListinoPrezzi()) == 0);//Fix 24705
      }

      public DettRigaConfigurazione dammiOggettoGestione() {
        DettRigaConfigurazioneOffCli dett = (DettRigaConfigurazioneOffCli) Factory.createObject(DettRigaConfigurazioneOffCli.class);
        return dett;
      }

      public Object getOggettoTestata() {
        return getTestata();
      }

      public ListinoVendita getListino() {
        return getListinoPrezzi();
      }
      //Fix 24613 fine
      
    //27649 inizio
      public ErrorMessage checkQtaInUMPrmMag() {
      	if((getIdUMPrm() != null && getUMPrm().getQtaIntera()) || (getArticolo() != null && getArticolo().getArticoloDatiMagaz().isQtaIntera())) {		
      		if(getQtaInUMPrmMag() != null) {
      			BigDecimal qta = getQtaInUMPrmMag();
      			qta = qta.setScale(0, BigDecimal.ROUND_DOWN);
      			if(qta.compareTo(getQtaInUMPrmMag()) != 0)
      				return new ErrorMessage("THIP40T616");
      		}
      	}
      	return null;
      }

      public ErrorMessage checkQtaInUMVen() {
      	if(getIdUMRif() != null && getUMRif().getQtaIntera()) {
      		if(getQtaInUMRif() != null) {
      			BigDecimal qta = getQtaInUMRif();
      			qta = qta.setScale(0, BigDecimal.ROUND_DOWN);
      			if(qta.compareTo(getQtaInUMRif()) != 0)
      				return new ErrorMessage("THIP40T617");
      		}
      	}
      	return null;
      }

      public ErrorMessage checkQtaInUMSecMag() {
      	if(getIdUMSec() != null && getUMSec().getQtaIntera()) {
      		if(getQtaInUMSecMag() != null) {
      			BigDecimal qta = getQtaInUMSecMag();
      			qta = qta.setScale(0, BigDecimal.ROUND_DOWN);
      			if(qta.compareTo(getQtaInUMSecMag()) != 0)
      				return new ErrorMessage("THIP40T617");
      		}
      	}
      	return null;
      }
      //27649 fine    
      
      //Fix 37244 inizio
      protected void copiaRigaCompletaBO(DocumentoOrdineRiga riga,SpecificheCopiaDocumento spec) {
     	 BigDecimal prcScontoIntestatario= riga.getPrcScontoIntestatario();
     	 BigDecimal prcScontoModalita=riga.getPrcScontoModalita();
     	 Sconto scontoModalita= riga.getScontoModalita();
     	 
     	 Agente agente =((OffertaClienteRigaPO) riga).getAgente();
     	 BigDecimal provvigione1Agente=((OffertaClienteRigaPO)riga).getProvvigione1Agente();

     	 Agente subagente= ((OffertaClienteRigaPO)riga).getSubagente();
     	 BigDecimal provvigione1Subagente=((OffertaClienteRigaPO)riga).getProvvigione1Subagente();


     	  copiaRigaCompletaBO( riga);
     	  
     	 if (spec.getCondizTestataDocumento() == SpecificheCopiaDocumento.CTD_DA_DOCUMENTO)
     	        {

     			 riga.setPrcScontoIntestatario(prcScontoIntestatario);
     			 riga.setPrcScontoModalita(prcScontoModalita);
     			 riga.setScontoModalita(scontoModalita);
     			 ((OffertaClienteRigaPO) riga).setAgente(agente);
     			 ((OffertaClienteRigaPO) riga).setProvvigione1Agente(provvigione1Agente);
     			 ((OffertaClienteRigaPO)riga).setProvvigione1Subagente(provvigione1Subagente);
     			 ((OffertaClienteRigaPO)riga).setSubagente(subagente);

     		     }
     	 //Fix 37556 inizio	  
    	  else 
    	  {
    		  OffertaClienteRigaPrm rigaPrm = getRigaPrimaria();
    		  Articolo artPrm = null;
    		  if (rigaPrm != null) artPrm = rigaPrm.getArticolo();
    		  if ( artPrm != null && ( artPrm.getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST ||artPrm.getTipoParte() == ArticoloDatiIdent.KIT_GEST ) )
    		  {
    			  //In questo caso di righe componante, le sconti intestatario devono essere sempre copiate dalla riga originale
    			  riga.setPrcScontoIntestatario(prcScontoIntestatario);
    			  riga.setPrcScontoModalita(prcScontoModalita);
    			  riga.setScontoModalita(scontoModalita);
   			  }
    	  }
    	  //Fix 37556 Fine
     	  
      }
      //Fix 37244 fine
      
 //Fix 44499 inizio
  public boolean isSalvaDaPadre() {
	  return isSalvaRigaPrimaria();
   }
  //Fix 44499 Fine
  //44784 inizio
  public void salvaConfigArticoloPrezzoList(boolean newRow) throws SQLException {
	  Articolo art = getArticolo();
	  boolean isValorizzaConf = false;
	  if(art != null){
		  SchemaCfg schemaCfg = art.getSchemaCfg();
		  if(schemaCfg != null)
			  isValorizzaConf = schemaCfg.getValorizzaConfig();
	  }
	  DettRigaConfigurazione dettRigaCfg = null;
	  if (getConfigurazione() != null && isValorizzaConf) {
		  char provPrezzo = getProvenienzaPrezzo();
		  if (newRow || isConfigurazioneCambiata() || isListinoCambiato() || provPrezzo == TipoRigaRicerca.MANUALE || iControlloRicalVlrDettCfg) {
			  dettRigaCfg = dammiOggettoGestione();
			  if (provPrezzo != TipoRigaRicerca.MANUALE) {
				  ((DettRigaConfigurazioneOffCli) dettRigaCfg).recuperaDettRigaConfigPrezzo(this); 
			  }
		  }
	  }
  }  
  //44784 fine
}



