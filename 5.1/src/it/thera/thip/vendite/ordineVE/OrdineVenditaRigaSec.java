package it.thera.thip.vendite.ordineVE;

import it.cope.thip.vendite.ordineVE.YOrdineVenditaRigaPrm;
import it.thera.thip.base.agentiProvv.Agente;
import it.thera.thip.base.articolo.Articolo;
import it.thera.thip.base.articolo.ArticoloDatiIdent;
import it.thera.thip.base.articolo.ArticoloDatiVendita;
import it.thera.thip.base.cliente.Sconto;
import it.thera.thip.base.comuniVenAcq.*;
import it.thera.thip.base.comuniVenAcq.web.DatiArticoloRigaVendita;
import it.thera.thip.base.documenti.DocumentoBase;
import it.thera.thip.base.documenti.DocumentoBaseRiga;
import it.thera.thip.base.documenti.StatoAvanzamento;
import it.thera.thip.cs.DatiComuniEstesi;
import it.thera.thip.vendite.documentoVE.DocumentoVenRigaSec;
import it.thera.thip.vendite.documentoVE.DocumentoVenditaRiga;
import it.thera.thip.vendite.offerteCliente.OffertaClienteRigaSec;
import it.thera.thip.magazzino.generalemag.*;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import java.util.List;

import com.thera.thermfw.base.TimeUtils;
import com.thera.thermfw.base.Trace;
import com.thera.thermfw.common.BaseComponentsCollection;
import com.thera.thermfw.common.ErrorMessage;
import com.thera.thermfw.common.Numerator;
import com.thera.thermfw.persist.CachedStatement;
import com.thera.thermfw.persist.ConnectionManager;
import com.thera.thermfw.persist.CopyException;
import com.thera.thermfw.persist.Copyable;
import com.thera.thermfw.persist.Database;
import com.thera.thermfw.persist.Factory;
import com.thera.thermfw.persist.KeyHelper;
import com.thera.thermfw.persist.OneToMany;
import com.thera.thermfw.persist.PersistentObject;
import com.thera.thermfw.persist.Proxy;
import com.thera.thermfw.persist.TableManager;
import com.thera.thermfw.cbs.CommentHandler;
import com.thera.thermfw.security.Entity;
import it.thera.thip.datiTecnici.configuratore.RigaConDettaglioConf;
import it.thera.thip.datiTecnici.configuratore.DettRigaConfigurazione;
import it.thera.thip.base.listini.ListinoVendita;
import it.thera.thip.datiTecnici.configuratore.DettRigaConfigurazioneOrdVen;
import it.thera.thip.datiTecnici.configuratore.SchemaCfg;
import it.thera.thip.base.generale.ParametroPsn;
import it.thera.thip.base.generale.PersDatiGen;
import it.thera.thip.datiTecnici.configuratore.DettRigaConfigurazioneOffCli;
import com.thera.thermfw.base.Utils;

/**
 * OrdineVenditaRigaSec.<br>
 *
 * <br><br><b>Copyright (c): Thera SpA</b>
 *
 * @author Enrico Masserdotti 13/09/2002
 */
/*
 * Revisions:
 * Number Date        Owner   Description
 *        13/09/2002  ME      Prima stesura
 *        25/02/2003  ME      Eliminato metodo salvaRiga
 *        09/05/2003  ME      Reso pubblico metodo getSequenzaNuovaRiga
 *                            in seguito a introduzione righe copiate
 *        30/05/2003  ME      Inseriti metodi checkAll e checkArticoloNoKit
 *        18/06/2003  DB      Ridefinito il metodo impostaStatoAvanzamento
 *        16/07/2003  ME      Modificato metodo completaBO
 * 03592  13/04/2005  ME      Sistemazione calcolo settimane in completaBO
 * 03230  28/04/2005  ME      Aggiunto metodo getRigaDestinazionePerCopia
 * 03929  16/06/2005  ME      Aggiunti metodi calcolaDatiVendita e verificaAzzeraPrezzo
 * 04060  05/07/2005  ME      Modificato metodo recuperoDatiVenditaArticoloPrm
 * 04166  29/07/2005  ME      Spostato richiamo super.completaBO per evitare
 *                            eccezioni in caso di creazione manuale di riga sec
 * 04453  21/09/2005  DBot    Aggiunto ritorno qta in um primaria di riga primaria per ricalcolo coeff. impiego
 * 04749  07/12/2005  ME      Aggiunta logica per propagazione dati
 * 04814  22/12/2005  DZ      checkArticoloNoKit: aggiunto test articolo != null per CM.
 * 05110  01/03/2006  ME      Aggiunto metodo setRigaPrimariaPerCopia e
 *                            controllo su richiamo completaBO
 * 05617  28/06/2006  ME      Modificato completaBO: aggiunta importazione della
 *                            commessa dalla riga primaria
 * 06754  23/02/2007  MG      gestione righe secondarie da fatturare
 * 07024  29/03/2007  ME      Modificato metodo recuperoDatiVenditaArticoloPrm
 * 07627  31/07/2007  PM      Inizializzazione nel costruttore dell'attributo coefficenteImpiego
 * 07779  10/09/2007  C&A     Togliere l'obbligatorietaà all'attributo AssogettamentoIVA
 * 10075  18/11/2008  FR      Modificata gestione campi data e settimana nel caso di valori null.
 * 10719   15/04/2009 PM      gestione offerte cliente
 * 11123  13/07/2009  DB
 * 11707  19/01/2010  MN      Per agevolare personalizzazioni, resi alcuni metodi publici.
 * 12078  29/01/2010  PM      gestione offerte cliente
 * 12148  08/02/2010  RH      modificato commenti
 * 12508  20/04/2010  DBot    Aggiunta la gestione dei pesi e del volume
 * 13110  30/08/2010  DBot    Aggiunto test su attivazione calcolo pesi e volume
 * 14931  30/08/2011  DBot    Aggiunta gestione pesi/volume ceramiche
 * 17490  27/03/2013  Linda   Modificato il metodo checkArticoloNoKit().
 * 18753  17/06/2015  Linda   Redefinire metodo checkIdEsternoConfig().
 * 22729  18/12/2015  Linda   Nella riga secondaria inserire il listino di prezzi indicato sulla riga primaria.
 * 22839  15/01/2016  Linda   Redefine metodo getEntityRiga().
 * 24613  07/12/2016  Linda   Gestione il salvataggio del dettaglio riga valore configurazione.
 * 27649   02/07/2018  LTB     Aggiunta un controllo bloccante che impedisca di inserire una riga con quantità non intera se l'um è gestita a quantità intera. 
 * 28305  26/11/2018  SZ		In caso delle righe secondarie con caricamento manuale impostare il prezzo a zero in caso non sia trovato su listino.
 * 30193  13/12/2019  SZ	  Gestione della configurazione nel caso di articoli di tipo ceramico. 
 * 33905  02/07/2021  SZ	  Nel caso di kit non gestito a magazzino il costo deve essere la somma dei costi delle righe secondarie 
 * 37217  03/12/2022  YBA     La copia di un ordine di vendita con articoli che gestiscono i lotti unitari non copiare i lotti unitari dall'ordine di origine
 * 37244  08/12/2022  YBA     Corregere il problemea perché del copia un ordine cambiando la causale e le codizioni di vendita sono impostate Da documento gli sconti testata e la provvigione 1 delle righe non deveno essere riprese dalla testata.
 * 37556  18/01/2023  YBA     Corregere il problemea perché in copia ordine lo sconto intestatario presente sul cliente non viene messo su tutte le righe le secondarie.
 * 38150  21/03/2023  YBA     Nella riga secondaria inserire il centro costo indicato sulla riga primaria.
 * 40694  13/12/2023  SBR	  Varie modifiche intellimag
 * 41393  20/02/2024  SZ	  i dati relativi a peso lordo e netto di riga vengono reperiti sempre dai dati tecnici dell'articolo, pur in presenza di dati differenti specificati nella versione indicata nella riga del documento.
 * 41316  09/02/2024  SBR     Riallineamento Intellimag al 5.0.2
 * 44499  13/01/2025  KD      Correggere passaggi valiso /annullato e sospeso e viceversa per gestire correttamente lo stato delle righe secondarie
 * 45124  07/03/2025  SZ	  recalcola il pesi volume si lo stato è cambiati
 * 44784  02/05/2025  RA	  Rendi la ConfigArticoloPrezzo persistent
 */
//public class OrdineVenditaRigaSec extends OrdineVenditaRiga {//Fix 24613
public class OrdineVenditaRigaSec extends OrdineVenditaRiga implements RigaConDettaglioConf {//Fix 24613

  //Query che seleziona il numero di riga più alto tra le righe di un ordine.
  //A questo valore verrà aggiunto il passo e così si otterrà il numero della
  //nuova riga secondaria.
  protected static final String SELECT_MAX_SEQUENZA_RIGHE_SEC =
    "SELECT MAX(" +
      OrdineVenditaRigaSecTM.SEQUENZA_RIGA +
    ") FROM " +
      OrdineVenditaRigaSecTM.TABLE_NAME + " " +
    "WHERE " +
      OrdineVenditaRigaSecTM.ID_AZIENDA + "=? AND " +
      OrdineVenditaRigaSecTM.ID_ANNO_ORD + "=? AND " +
      OrdineVenditaRigaSecTM.ID_NUMERO_ORD + "=? AND " +
      OrdineVenditaRigaSecTM.ID_RIGA_ORD + "=?";
  protected static CachedStatement cSelectMaxSequenzaRigheSec =
    new CachedStatement(SELECT_MAX_SEQUENZA_RIGHE_SEC);


  boolean iSalvaRigaPrimaria = true;

  //Attributi
  protected Proxy iRigaPrimaria = new Proxy(OrdineVenditaRigaPrm.class);


  /**
   * Costruttore
   */
  public OrdineVenditaRigaSec() {
    super();
    setSpecializzazioneRiga(RIGA_SECONDARIA_PER_COMPONENTE);
    iRigheLotto = new OneToMany(OrdineVenditaRigaLottoSec.class, this, 31, true);
    iRigaCollegata = new Proxy(OrdineVenditaRigaSec.class);
    iNonFatturare = true;   //MG FIX 6754;
    //Fix 7627 PM Inizio
    setCoefficienteImpiego(new BigDecimal(1.0));
    //Fix 7627 PM Fine
    datiArticolo = (DatiArticoloRigaVendita)Factory.createObject(DatiArticoloRigaVendita.class);//Fix 33905
    iOffertaClienteRiga = new Proxy(OffertaClienteRigaSec.class); //Fix 10719 PM

  }

  //--------------------------------------------------------//

  //Metodi get/set attributi

  /**
   * Restituisce la Proxy Riga Primaria.
   */
  public OrdineVenditaRigaPrm getRigaPrimaria() {
    return (OrdineVenditaRigaPrm)iRigaPrimaria.getObject();
  }


  /**
   * Valorizza la Proxy Riga Primaria.
   */
  public void setRigaPrimaria(OrdineVenditaRigaPrm rigaPrimaria){
    iRigaPrimaria.setObject(rigaPrimaria);
    setDirty();
    setOnDB(false);
  }


  /**
   * Restituisce la chiave della Proxy Riga Primaria.
   */
  public String getRigaPrimariaKey() {
    return iRigaPrimaria.getKey();
  }


  /**
   * Valorizza la chiave della Proxy Riga Primaria.
   */
  public void setRigaPrimariaKey(String key) {
    iRigaPrimaria.setKey(key);
    setDirty();
  }



  /**
   * Attributo di servizio
   */
  public BigDecimal getServizioQtaVenditaPrm() {
    try {
      return getRigaPrimaria().getQtaInUMRif();
    }
    catch (Exception ex) {
      return new BigDecimal(0.0);
    }
  }

  //fix 4453 inizio
  public BigDecimal getServizioQtaUMPrmPrm() {
    try {
      return getRigaPrimaria().getQtaInUMPrmMag();
    }
    catch (Exception ex) {
      return new BigDecimal(0.0);
    }
  }
  //fix 4453 fine

  //--------------------------------------------------------//

  //Implementazione metodi interfaccia Child

  public String getFatherKey() {
    return iRigaPrimaria.getKey();
  }


  public void setFatherKey(String key) {
    iRigaPrimaria.setKey(key);
  }


  public void setFather(PersistentObject father) {
    iRigaPrimaria.setObject(father);
  }


  public String getOrderByClause() {
    return super.getOrderByClause();
  }

  //--------------------------------------------------------//

  //Implementazione metodi interfaccia BusinessObject

  public Vector checkAll(BaseComponentsCollection components) {
    Vector errors = new Vector();
    //Fix 28305 inizio 
    verificaAzzeraPrezzo();
    if (getPrezzo()!= null && getPrezzo().equals(new BigDecimal(0.0)))
    	components.getComponent("Prezzo").setValue("0");
    //Fix 28305 inizio
    
    errors = super.checkAll(components);

    //Vector otherErrors = new Vector();
    ErrorMessage em = null;

    //Verifica coerenza dati per riga di tipo spesa
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


  /**
   * Viene verificato che non venga inserito un articolo che è a sua volta un
   * kit.
   */
  public ErrorMessage checkArticoloNoKit() {
    Articolo articolo = getArticolo();
    Trace.print("=============>>>>>>>>>>>>>>>>>"+getKey()+">>>>>>>>"+getCausaleRigaKey());
    if (articolo != null && //...FIX04814 - DZ
        //(articolo.getTipoParte() == ArticoloDatiIdent.KIT_GEST ||//Fix 17490
        articolo.getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST) {
      return new ErrorMessage("THIP_BS203");
    }

    return null;
  }

  //--------------------------------------------------------//

  //Implementazione metodi astratti di PersistentObject

  protected TableManager getTableManager() throws java.sql.SQLException
  {
    return OrdineVenditaRigaSecTM.getInstance();
  }


  public int save() throws SQLException {
     boolean newRow = !isOnDB();//Fix 24613
     //40694
     if(PersDatiGen.isGestitioIntellimag()) {
     	cambioTestataStatoIM(newRow);
     }
     //40694
     //Fix 12508 inizio
     boolean aggiornaPesiEVolumeTes = false;
     if(isSalvaRigaPrimaria())
     {
        //if(isQuantitaCambiata() || !isOnDB() || isPesiVolumiCambiati() || isVersioneCambiata())//Fix 41393
    	//if(isQuantitaCambiata() || !isOnDB() || isPesiVolumiCambiati() || isVersioneCambiata())//Fix 41393 //Fix 45124
    	 if(isServeRecalcoloPesiVolumiInCambiamentoStato() ||isQuantitaCambiata() || !isOnDB() || isPesiVolumiCambiati() || isVersioneCambiata()) //Fix 45124
        {
           calcolaPesiEVolume();
           aggiornaPesiEVolumeRigaPrm(false);
           //aggiornaPesiEVolumeTes = isRicalcolaPesiEVolume() && getRigaPrimaria().isRicalcolaPesiEVolume();
        }
     }
     //Fix 12508 fine
		//Fix 37217 inizio
		if((isInCopiaRiga || ( this.getTestata() !=null &&  ((OrdineVendita)this.getTestata()).isInCopia()) ) 
				&& getArticolo() != null  && (getArticolo().isArticLotto()) && (getArticolo().getArticoloDatiMagaz().isLottoUnitario()))
		{	if(getRigheLotto() != null)
				getRigheLotto().clear();
		}
		//Fix 37217 fine
    int rc = super.save();
    //Fix 3929 - inizio
    verificaAzzeraPrezzo();
    //Fix 3929 - fine
    if (isSalvaRigaPrimaria()) {

//MG FIX 6754 inizio
      boolean totaliDaRicalcolare = isDaRicalcolare();
      //72296 Softre -->
      DocumentoOrdineRiga rigaPrm = getRigaPrimaria();
		char tipoParte = rigaPrm.getArticolo().getTipoParte();
		char tipoCalcoloPrezzo = rigaPrm.getArticolo().getTipoCalcPrzKit();
		if (!totaliDaRicalcolare && (tipoParte == ArticoloDatiIdent.KIT_NON_GEST ||
				tipoParte == ArticoloDatiIdent.KIT_GEST)
				&& tipoCalcoloPrezzo == ArticoloDatiVendita.SUL_PRODOTTO_FINITO
				&& newRow) {
			totaliDaRicalcolare = true;
			if(getRigaPrimaria() instanceof YOrdineVenditaRigaPrm) {
				((YOrdineVenditaRigaPrm)getRigaPrimaria()).setAbilitaCalcoloTotRigheSecConReset(true);
			}
		}
      //72296 Softre <--
      if (totaliDaRicalcolare)
        getRigaPrimaria().calcolaPrezzoDaRigheSecondarieSenzaReset();
//MG FIX 6754 fine
      getRigaPrimaria().setAggiornaRigaOfferta(false);//Fix 12078 PM
      rc = salvaRigaPrimaria(rc);
      getRigaPrimaria().setAggiornaRigaOfferta(true);//Fix 12078 PM
//MG FIX 6754 inizio
      //Fix 12508 inizio
      if (totaliDaRicalcolare)
         ((OrdineVendita)this.getTestata()).calcolaCostiValoriOrdine(false);
      if(totaliDaRicalcolare || aggiornaPesiEVolumeTes)
         rc = salvaTestata(rc);
      /*
      if (totaliDaRicalcolare) {
        ((OrdineVendita)this.getTestata()).calcolaCostiValoriOrdine(false);
        rc = salvaTestata(rc);
      }
      */
      //Fix 12508 fine
//MG FIX 6754 fine
    }
    //Fix 10719 PM Inizio
    if (rc > 0 && getOffertaClienteRiga() != null && isAggiornaRigaOfferta())
    {
    	int rc1 = getOffertaClienteRiga().aggiornaDopoEvasione(this, OrdineRiga.MANUTENZIONE);
    	rc = rc1 >= 0 ? rc + rc1 : rc1;
    }
    //Fix 10719 PM Fine

    //Fix 24613 inizio
    if(rc>=0){
      int dettCfgRett = salvaDettRigaConf(newRow);
      if(dettCfgRett < 0)
        rc = dettCfgRett;
      else
        rc = rc + dettCfgRett;
    }
    //Fix 24613 fine
    salvaConfigArticoloPrezzoList(newRow);//44784
    return rc;
  }


//MG FIX 6754 inizio
  public boolean isDaRicalcolare() {
    DocumentoOrdineRiga rigaPrm = getRigaPrimaria();
    char tipoParte = rigaPrm.getArticolo().getTipoParte();
    char tipoCalcoloPrezzo = rigaPrm.getArticolo().getTipoCalcPrzKit();
    if ( (tipoParte == ArticoloDatiIdent.KIT_NON_GEST ||
          tipoParte == ArticoloDatiIdent.KIT_GEST)
          && tipoCalcoloPrezzo == ArticoloDatiVendita.DA_COMPONENTI)
        return confrontaValoriOldRiga();
    return false;
  }
//MG FIX 6754 fine


  //--------------------------------------------------------//
  //Ridefinizione di altri metodi

  /**
   * Ridefinizione del metodo recuperoDatiVenditaSave della classe
   * OrdineVenditaRiga
   */
  protected boolean recuperoDatiVenditaSave() {
    return recuperoDatiVenditaArticoloPrm() &&
           isServizioCalcDatiVendita() &&
           ! isRigaOfferta() &&
           getTipoRiga() != TipoRiga.SPESE_MOV_VALORE;
  }


  /**
   * Ridefinizione del metodo getNumeroRiga della classe OrdineRiga
   */
  protected void componiChiave() {
    try {
      int dett = Numerator.getNextInt("OrdVenRigaPrm");
      if (dett == 0)
        dett = Numerator.getNextInt("OrdVenRigaPrm");

      setDettaglioRigaDocumento(new Integer(dett));
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }


  /**
   * Ridefinizione del metodo eliminaRigaOmaggioCollegata della classe
   * OrdineVenditaRiga
   */
  protected int eliminaRigaOmaggioCollegata(String key) throws SQLException {
    int rc = 0;

    OrdineVenditaRigaSec rigaOmaggio =
      (OrdineVenditaRigaSec)Factory.createObject(OrdineVenditaRigaSec.class);
    rigaOmaggio.setKey(key);
    if (rigaOmaggio.retrieve()) {
//      rigaOmaggio.setFather(getTestata());
//      rigaOmaggio.setSalvaTestata(false);
      rc = rigaOmaggio.delete();
    }

    return rc;
  }


  //Pietro
  public DocumentoBase getTestata()
  {
    DocumentoBase ret = null;
    DocumentoBaseRiga riga = (DocumentoBaseRiga)getRigaPrimaria();
    if (riga != null)
        ret = riga.getTestata();
    return ret;
  }

  //Pietro
  public String getTestataKey()
  {
    String ret = null;
    DocumentoBaseRiga riga = (DocumentoBaseRiga)getRigaPrimaria();
    if (riga != null)
        ret = riga.getTestataKey();
    return ret;
  }

    //Pietro
  public void setSalvaRigaPrimaria(boolean salvaRigaPrimaria) // Fix 11707
  {
    iSalvaRigaPrimaria = salvaRigaPrimaria;
  }

    //Pietro
  protected boolean isSalvaRigaPrimaria()
  {
    return iSalvaRigaPrimaria;
  }

  //Pietro
  protected int eliminaRiga() throws SQLException
  {
     //Fix 12508 inizio
     if(isSalvaRigaPrimaria())
        aggiornaPesiEVolumeRigaPrm(true);
     //Fix 12508 fine
    int rc = super.eliminaRiga();
    if (isSalvaRigaPrimaria())
        rc = salvaRigaPrimaria(rc);
    return rc;
  }

  //Pietro
  protected int salvaRigaPrimaria(int rc) throws SQLException
  {
      if (rc >= 0)
      {
          OrdineVenditaRigaPrm rigaPrm = (OrdineVenditaRigaPrm)getRigaPrimaria();
          rigaPrm.setSalvaRigheSecondarie(false);
          int rc1 = rigaPrm.save();
          rigaPrm.setSalvaRigheSecondarie(true);
          rc =  rc1 >= 0 ? rc + rc1 : rc1;
      }
      return rc;
  }


//  protected void disabilitaAggiornamentoSaldiSuiLotti() {
//    Iterator i = getRigheLotto().iterator();
//    while (i.hasNext())
//    {
//        OrdineVenditaRigaLotto rigaLotto = (OrdineVenditaRigaLotto)i.next();
//        rigaLotto.setMovimentaSaldi(false);
//    }
//  }


  /**
   * Overwrite da OrdinevenditaRiga
   */
  protected void aggiornaQuantitaOrdinataSuiSaldi()
  {
    Articolo articoloRigaPrm = getRigaPrimaria().getArticolo();
    if (articoloRigaPrm.getTipoParte() == ArticoloDatiIdent.KIT_GEST)
    {
      this.controllaPresenzaLottoDummy();
      iApplicaMovimentiSuiSaldi = false;
      disabilitaCalcoloMovimentiPortafoglioSuiLotti();
    }
    else
      super.aggiornaQuantitaOrdinataSuiSaldi();

  }


  /**
   * Overwrite da OrdinevenditaRiga
   */
  protected void stornaQuantitaOrdinataDaiSaldi()
  {
      Articolo articoloRigaPrm = getRigaPrimaria().getArticolo();
      if (articoloRigaPrm.getTipoParte() == ArticoloDatiIdent.KIT_GEST)
          disabilitaCalcoloMovimentiPortafoglioSuiLotti();
      else
          super.stornaQuantitaOrdinataDaiSaldi();
  }

   /**
   * Restituisce l'attributo relativo al Proxy Testata.
   */
  public String getIdAzienda() {
    String key = iRigaPrimaria.getKey();
    String idAzienda = KeyHelper.getTokenObjectKey(key,1);
    return idAzienda;
  }


  /**
   * Valorizza l'attributo relativo al Proxy Testata.
   */
  public void setIdAzienda(String idAzienda) {
    String key = iRigaPrimaria.getKey();
    iRigaPrimaria.setKey(KeyHelper.replaceTokenObjectKey(key , 1, idAzienda));
    setDirty();
    setOnDB(false);

    getCommentHandlerManager().setOwnerKeyChanged();
    iRigheLotto.setFatherKeyChanged();
    setIdAziendaInternal(idAzienda);
  }

  /**
   * Valorizza l'attributo.
   * Ridefinisco il metodo per chiamare il metodo setFatherKeyChanged()
   */
  /*
   * Revisions:
   * Date          Owner      Description
   * 09/05/2002    Wizard     Codice generato da Wizard
   *
   */
   public void setAziendaKey(String a)
   {
     setIdAzienda(a);
    }


  /**
   * Restituisce l'attributo relativo al Proxy Testata.
   */
  public String getAnnoDocumento() {
    String key = iRigaPrimaria.getKey();
    String annoDocumento = KeyHelper.getTokenObjectKey(key,2);
    return annoDocumento;
  }


  /**
   * Valorizza l'attributo relativo al Proxy Testata.
   */
  public void setAnnoDocumento(String annoDocumento) {
    String key = iRigaPrimaria.getKey();
    iRigaPrimaria.setKey(KeyHelper.replaceTokenObjectKey(key , 2, annoDocumento));
    setDirty();
    setOnDB(false);

    getCommentHandlerManager().setOwnerKeyChanged();
    iRigheLotto.setFatherKeyChanged();
  }


  /**
   * Restituisce l'attributo relativo al Proxy Testata.
   */
  public String getNumeroDocumento() {
    String key = iRigaPrimaria.getKey();
    String numeroDocumento = KeyHelper.getTokenObjectKey(key,3);
    return numeroDocumento;
  }


  /**
   * Valorizza l'attributo relativo al Proxy Testata.
   */
  public void setNumeroDocumento(String numeroDocumento) {
    String key = iRigaPrimaria.getKey();
    iRigaPrimaria.setKey(KeyHelper.replaceTokenObjectKey(key , 3, numeroDocumento));
    setDirty();
    setOnDB(false);

    getCommentHandlerManager().setOwnerKeyChanged();
    iRigheLotto.setFatherKeyChanged();
  }

  //--------------

  /**
   * Restituisce l'attributo NumeroRigaDocumento.
   */
  public Integer getNumeroRigaDocumento() {

    String key = iRigaPrimaria.getKey();
    String nrd = KeyHelper.getTokenObjectKey(key,4);
    return (nrd == null) ? null : new Integer(nrd);
  }


  /**
   * Valorizza l'attributo NumeroRigaDocumento.
   */
  public void setNumeroRigaDocumento(Integer numeroRigaDocumento) {
    String key = iRigaPrimaria.getKey();
    iRigaPrimaria.setKey(KeyHelper.replaceTokenObjectKey(key , 4, numeroRigaDocumento));
    setDirty();
    setOnDB(false);

    getCommentHandlerManager().setOwnerKeyChanged();
    iRigheLotto.setFatherKeyChanged();
  }


  /**
   * Completamento dati form in NEW
   */
  public void completaBO() {
    //Fix 4166 - inizio
     //super.completaBO();
    //Fix 4166 - fine

    if (getRigaPrimaria().getNumeroRigaDocumento() != null) {		//Fix 5110
    	setSequenzaRiga(getSequenzaNuovaRiga(getRigaPrimaria()));
    }
    //Fix 7627 PM Inizio
    //setCoefficienteImpiego(new BigDecimal(1.0));
    //Fix 7627 PM Fine

    OrdineVenditaRigaPrm rigaPrm = getRigaPrimaria();
    setCausaleRiga(rigaPrm.getCausaleRiga());

    //Fix 4166 - inizio
     super.completaBO();
    //Fix 4166 - fine

     //Fix 3592 - inizio
    Date dataConsRich = rigaPrm.getDataConsegnaRichiesta();
    setDataConsegnaRichiesta(rigaPrm.getDataConsegnaRichiesta());
    Date dataConsConf = rigaPrm.getDataConsegnaConfermata();
    setDataConsegnaConfermata(dataConsConf);
    Date dataConsProd = rigaPrm.getDataConsegnaProduzione();
    setDataConsegnaProduzione(dataConsProd);
    if (dataConsRich != null){//Fix 10075 FR
    	int[] datiSett = TimeUtils.getISOWeek(dataConsRich);
    	String sett =
    		DocumentoOrdineTestata.getSettimanaFormattata(
    				datiSett[0], datiSett[1]
    		);
    	setSettConsegnaRichiesta(sett);
    }

    if (dataConsConf != null){//Fix 10075 FR
    	int[] datiSett = TimeUtils.getISOWeek(dataConsConf);
    	String sett =
    		DocumentoOrdineTestata.getSettimanaFormattata(
    				datiSett[0], datiSett[1]
    		);
    	setSettConsegnaConfermata(sett);
    }

    if (dataConsProd != null){//Fix 10075 FR
    	int[] datiSett = TimeUtils.getISOWeek(dataConsProd);
    	String sett =
    		DocumentoOrdineTestata.getSettimanaFormattata(
    				datiSett[0], datiSett[1]
    		);
    	setSettConsegnaProduzione(sett);
    }
    //Fix 3592 - fine

    //Differenza tra KIT_GESTITO e KIT_NON_GESTITO
    OrdineVendita testata = (OrdineVendita)getTestata();
    Articolo articoloRigaPrm = rigaPrm.getArticolo();
    //Fix 7024 - inizio
//    if (articoloRigaPrm.getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST) {
    //Fix 7024 - fine
      //Listino
      //setListinoVendita(testata.getListinoPrezzi());//Fix 22729
      setListinoVendita(rigaPrm.getListinoVendita());//Fix 22729
      //Sconti
      
      setPrcScontoIntestatario(testata.getPrcScontoIntestatario());
      setPrcScontoModalita(testata.getPrcScontoModalita());
      setScontoModalita(testata.getScontoTabellare());
      //Fix 7024 - inizio
//    }
      //Fix 7024 - fine
    //Fix 5617 - inizio
    setCommessa(rigaPrm.getCommessa());
    //Fix 5617 - fine
    setCentroCosto(rigaPrm.getCentroCosto());//Fix 38150
  }


  /**
   * Restituisce la sequenza da assegnare ad una nuova riga secondaria
   */
  public static synchronized int getSequenzaNuovaRiga(OrdineVenditaRigaPrm rigaPrm) {
    try {
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


  /**
   * Restituisce un flag che capisce se deve essere effettuato il calcolo dei
   * dati di vendita in base al tipo parte dell'articolo della riga primaria
   */
  protected boolean recuperoDatiVenditaArticoloPrm() {
  	//Fix 4060 - inizio
  	/*
    boolean calcoloDatiVendita = false;
    int tipoParte = getRigaPrimaria().getArticolo().getTipoParte();
    switch (tipoParte) {
      case ArticoloDatiIdent.KIT_GEST:
        calcoloDatiVendita = false;
        break;
      case ArticoloDatiIdent.KIT_NON_GEST:
        calcoloDatiVendita = true;
        break;
    }
    return calcoloDatiVendita;
    */

  	Articolo articoloPrm = getRigaPrimaria().getArticolo();
  	//Fix 7024 - inizio
    //char tipoParte = articoloPrm.getTipoParte();
  	//Fix 7024 - fine
  	char spclRiga = getSpecializzazioneRiga();
    char tipoCalcoloPrezzo = articoloPrm.getTipoCalcPrzKit();
  	//Fix 7024 - inizio
    return
    	(spclRiga == RIGA_SECONDARIA_DA_FATTURARE) ||
    	(spclRiga == RIGA_SECONDARIA_PER_COMPONENTE && tipoCalcoloPrezzo == ArticoloDatiVendita.DA_COMPONENTI);
    /*
    return
    	tipoParte == ArticoloDatiIdent.KIT_NON_GEST || tipoParte == ArticoloDatiIdent.KIT_GEST
      &&
      tipoCalcoloPrezzo == ArticoloDatiVendita.DA_COMPONENTI;
    */
  	//Fix 7024 - fine
    //Fix 4060 - fine
  }


  protected OrdineRigaLotto creaLotto()
  {
     return (OrdineVenditaRigaLotto)Factory.createObject(OrdineVenditaRigaLottoSec.class);
  }


  protected int salvaParteIntestatario(int rc) throws SQLException
  {
      return rc;
  }

  protected void inizializzaParteIntestatario()
  {
  }

  protected void calcolaMovimentiPortafoglio()
  {
      super.calcolaMovimentiPortafoglio();
  }


  protected void creaOldRiga(){
    iOldRiga =(OrdineVenditaRigaSec)Factory.createObject(OrdineVenditaRigaSec.class);
  }

  protected void impostaStatoAvanzamento(){
    super.impostaStatoAvanzamento();
    if (this.getStatoAvanzamento()== StatoAvanzamento.PROVVISORIO){
        this.getRigaPrimaria().setStatoAvanzamento(StatoAvanzamento.PROVVISORIO);
    }
  }


  //Fix 3230 - inizio
  /**
   * Ridefinizione.
   */
  protected DocumentoOrdineRiga getRigaDestinazionePerCopia() {
    return (OrdineVenditaRigaSec)Factory.createObject(OrdineVenditaRigaSec.class);
  }
  //Fix 3230 - fine


  //Fix 3929 - inizio
  /**
   * Ridefinizione
   */
  public void calcolaDatiVendita(OrdineVenditaPO testata) throws SQLException { // Fix 11707
     super.calcolaDatiVendita(testata);
     verificaAzzeraPrezzo();
  }


  /**
   * Se l'articolo della riga primaria ha tipo calcolo prezzo non legato ai
   * componenti è necessario azzerare il prezzo se non è stato trovato da
   * listino.
   * Questo perchè in tale situazione nella GUI delle righe secondarie la
   * cartella 'Prezzi/Sconti' è disabilitata e se il prezzo non fosse stato
   * valorizzato e fosse obbligatorio al salvataggio segnalerebbe 'Campo obbligatorio'
   * e non consentirebbe più di uscire dalla GUI.
   */
  public void verificaAzzeraPrezzo() {
     //Fix 4060 - inizio
     if (getRigaPrimaria() != null) {
     //Fix 4060 - fine
        if (getRigaPrimaria().getArticolo().getTipoCalcPrzKit() != ArticoloDatiVendita.DA_COMPONENTI) {
           BigDecimal prezzo = getPrezzo();
           //Se non ha trovato alcun prezzo dal listino allora lo forza a zero per
           //evitare che al salvataggio venga segnalato un errore di campo obbligatorio
           //sul prezzo (per righe nuove)
           if (prezzo == null) {
              setPrezzo(new BigDecimal(0.0));
           }
        }
     }
  }
  //Fix 3929 - fine


  //Fix 4749 - inizio
  /**
   * Ridefinizione
   */
  public void propagaDatiTestata(SpecificheModificheRigheOrd spec) {
  	if (spec.isSezioneEvasione()) {
	  	if (spec.getAzioneCommessa() != SpecificheModificheRigheOrd.AZ_GENER_NESSUNA) {
	  		propagaCommessa(spec);
	  	}
	  	if (spec.getAzioneCentroCosto() != SpecificheModificheRigheOrd.AZ_GENER_NESSUNA) {
	  		propagaCentroCosto(spec);
	  	}
  	}
  }
  //Fix 4749 - fine


  //Fix 5110 - inizio
  /**
   * Ridefinizione
   */
  protected void setRigaPrimariaPerCopia(DocumentoOrdineRiga rigaPrm) {
  	setRigaPrimaria((OrdineVenditaRigaPrm)rigaPrm);
  }
  //Fix 5110 - fine

// Fix 07779 inizio
  public ErrorMessage checkIdAssogIVA() {
    if (!isNonFatturare() && getIdAssogIVA() == null)
      return new ErrorMessage("BAS0000000");
    return null;
  }

// Fix 07779 fine

//Fix 10719 PM Inizio
	public Integer getDettRigaBozza()
	{
		String key = iOffertaClienteRiga.getKey();
		Integer ret = KeyHelper.stringToIntegerObj(KeyHelper.getTokenObjectKey(key, 5));
		if (ret == null)
			ret = new Integer(0);
		return ret;
	}
//Fix 10719 PM Fine

        // fix Fix 11123
        public boolean effettuareIlControllo(List lista){
          return effettuareIlControllo(lista, null);
        }

        public boolean effettuareIlControllo(List lista, QuantitaInUM qta){
          boolean ritorno = false;
          //Fix 30193 Inizio
          boolean controlla = true ;
          if(this.isConfigurazioneNeutra() ) {
        	  controlla = false ;
          }
          if(controlla && this.isDaAggiornare(qta)) {
          //Fix 30193 Fine
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
          }//Fix 30193 
          return ritorno;
        }

        public boolean isDaAggiornare(QuantitaInUM qta){
          if (qta==null)
            return super.isDaAggiornare();
          else
            return true;
        }

        // fine fix 11123

        //Fix 12148
        protected CommentHandler getCommentiIntestatario() {
          if (this.getOffertaClienteRiga() != null)
            return this.getOffertaClienteRiga().getCommentHandler();
          return super.getCommentiIntestatario();
        }

        protected CommentHandler getCommentiParteIntestatario() {
          if (getOffertaClienteRiga() != null)
            return null;
          return super.getCommentiParteIntestatario();
        }

      //Fine 12148

   //Fix 12508 inizio
   public void calcolaPesiEVolume()
   {
      if(!isAttivoCalcoloPesiVolumi()) //Fix 13110
         return;

      if(isRicalcolaPesiEVolume())
      {
         //Fix 14931 inizio
         CalcolatorePesiVolume calc = CalcolatorePesiVolume.getInstance();
         calc.aggiornaPesiVolumeRiga(this);
         /*
         BigDecimal[] pesiEVolume = Articolo.getPesiEVolumeTotali(getArticolo(),
                                                                  getQtaInUMRif(), getQtaInUMPrmMag(), getQtaInUMSecMag(),
                                                                  getUMRif(), getUMPrm(), getUMSec());
         setPesoNetto(pesiEVolume[0]);
         setPesoLordo(pesiEVolume[1]);
         setVolume(pesiEVolume[2]);
         */
         //Fix 14931 fine
         //System.out.println("ORD_SEC:calcolaPesiEVolume " + pesiEVolume[0]);
      }
   }

   public void aggiornaPesiEVolumeRigaPrm(boolean rigaInCancellazione)
   {
      //Fix 14931 inizio
      CalcolatorePesiVolume calc = CalcolatorePesiVolume.getInstance();
      calc.aggiornaPesiVolumeRigaDaSecondarie(this, getRigaPrimaria(), rigaInCancellazione);
      /*
      if(!isAttivoCalcoloPesiVolumi()) //Fix 13110
         return;

      //aggiornamento dei pesi e volume di testata per delta rispetto a preceente
      OrdineVenditaRigaPrm rigaPrm = getRigaPrimaria();
      if(!rigaPrm.isRicalcolaPesiEVolume())
         return;

      BigDecimal pesoNettoRP = getNotNullValue(rigaPrm.getPesoNetto());
      BigDecimal pesoLordoRP = getNotNullValue(rigaPrm.getPesoLordo());
      BigDecimal volumeRP = getNotNullValue(rigaPrm.getVolume());

      boolean aggiornaRP = false;
      //controllo per eliminare valroe vecchio
      boolean oldRigaNonAnnullata = getOldRiga() != null && getOldRiga().getDatiComuniEstesi().getStato() != DatiComuniEstesi.ANNULLATO;
      boolean storno = isOnDB() && oldRigaNonAnnullata && (rigaInCancellazione || isQuantitaCambiata() || isPesiVolumiCambiati());
      if(storno && getOldRiga().isPesiVolumeValorizzati())
      {
         RigaVendita oldRiga = (RigaVendita)getOldRiga();
         pesoNettoRP = pesoNettoRP.subtract(getNotNullValue(oldRiga.getPesoNetto()));
         pesoLordoRP = pesoLordoRP.subtract(getNotNullValue(oldRiga.getPesoLordo()));
         volumeRP = volumeRP.subtract(getNotNullValue(oldRiga.getVolume()));
         aggiornaRP = true;
      }

      //controllo per aggiungere valore nuovo
      boolean valida = getDatiComuniEstesi().getStato() != DatiComuniEstesi.ANNULLATO;
      boolean applica = valida && !rigaInCancellazione && (isQuantitaCambiata() || !isOnDB() || isPesiVolumiCambiati());
      if(applica && isPesiVolumeValorizzati())
      {
         pesoNettoRP = pesoNettoRP.add(getNotNullValue(getPesoNetto()));
         pesoLordoRP = pesoLordoRP.add(getNotNullValue(getPesoLordo()));
         volumeRP = volumeRP.add(getNotNullValue(getVolume()));
         aggiornaRP = true;
      }

      if(aggiornaRP)
      {
         BigDecimal[] pesiEVolume = new BigDecimal[] {pesoNettoRP, pesoLordoRP, volumeRP};
         pesiEVolume = Articolo.sistemaScalePesiEVolumePerRighe(pesiEVolume);
         rigaPrm.setPesoNetto(pesiEVolume[0]);
         rigaPrm.setPesoLordo(pesiEVolume[1]);
         rigaPrm.setVolume(pesiEVolume[2]);
         //System.out.println("ORD_AGGRIGA:aggiornaPesiEVolumeRigaPrm " + pesiEVolume[0]);
      }
      */
      //Fix 14931 fine
   }
   //Fix 12508 fine

   //Fix 18753 inizio
   public ErrorMessage checkIdEsternoConfig(){
     ErrorMessage error = super.checkIdEsternoConfig();
     if(error != null && !error.equals("")) return error;

     if (isOnDB() && getArticolo() != null && getArticolo().isConfigurato() &&
         getConfigurazione() == null && iOldRiga!=null && iOldRiga.getStatoAvanzamento() != StatoAvanzamento.DEFINITIVO
         && getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO) {
       return new ErrorMessage("THIP40T398", getIdArticolo());
     }

     return null;
   }
   //Fix 18753 fine

   //Fix 22839 inizio
   protected Entity getEntityRiga() {
     try {
       return Entity.elementWithKey("OrdVenRigaSec", Entity.NO_LOCK);
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
      if(schemaCfg != null)
        isValorizzaConf = schemaCfg.getValorizzaConfig();
    }
    DettRigaConfigurazione dettRigaCfg = null;
    if (getConfigurazione() != null && isValorizzaConf) {

      if(isDaEvasioneOfferta())
        return salvaDettRigaConfDaEvasione();

      char provPrezzo = getProvenienzaPrezzo();
      if (newRow) {
        if (provPrezzo != TipoRigaRicerca.MANUALE){
          dettRigaCfg = dammiOggettoGestione();
          if((isInCopiaRiga && !iControlloRicalVlrDettCfg) || (((DocumentoOrdineTestata)this.getTestata()).isInCopia() && !isCondVenCopiaDaRicalcolare))
            yrit = dettRigaCfg.copiaDettRigaCfg(this,rigaDaCopiareKey);
          else
            yrit = dettRigaCfg.recuperaDettRigaConfigurazione(this);
        }
      }
      else if(isConfigurazioneCambiata() || isListinoCambiato() || provPrezzo == TipoRigaRicerca.MANUALE || iControlloRicalVlrDettCfg){
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

  protected int salvaDettRigaConfDaEvasione() throws SQLException {
    int ret = 0;
    if (getOffertaClienteRiga() != null) {
      DettRigaConfigurazione dettRigaCfg = (DettRigaConfigurazioneOffCli) Factory.createObject(DettRigaConfigurazioneOffCli.class);
      String offRigaKey = getOffertaClienteRiga().getKey();
      ret = dettRigaCfg.copiaDettRigaCfgDaEvasione(this, offRigaKey);
    }
    return ret;
  }

  public boolean isListinoCambiato() {
    OrdineVenditaRiga ovr = (OrdineVenditaRiga) iOldRiga;
    if (ovr == null)
      return false;
    //return!(this.getIdListino().equals(ovr.getIdListino()));//Fix 24705
    return!(Utils.compare(this.getIdListino(),ovr.getIdListino()) == 0);//Fix 24705
  }

  public DettRigaConfigurazione dammiOggettoGestione() {
    DettRigaConfigurazioneOrdVen dett = (DettRigaConfigurazioneOrdVen) Factory.createObject(DettRigaConfigurazioneOrdVen.class);
    return dett;
  }

  public Object getOggettoTestata() {
    return getTestata();
  }

  public ListinoVendita getListino() {
    return getListinoVendita();
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
	 
	 Agente agente = ((RigaVendita) riga).getAgente();
	 BigDecimal provvigione1Agente=((RigaVendita) riga).getProvvigione1Agente();

	 Agente subagente= ((RigaVendita) riga).getSubagente();
	 BigDecimal provvigione1Subagente=((RigaVendita) riga).getProvvigione1Subagente();
	 boolean differenzaPrezzoAgente = ((RigaVendita) riga).hasDifferenzaPrezzoAgente();
	 boolean differenzaPrezzoSubagente = ((RigaVendita) riga).hasDifferenzaPrezzoSubagente();

	  copiaRigaCompletaBO( riga);
	  
	  if (spec.getCondizTestataDocumento() == SpecificheCopiaDocumento.CTD_DA_DOCUMENTO)
	        {
			  riga.setPrcScontoIntestatario(prcScontoIntestatario);
			  riga.setPrcScontoModalita(prcScontoModalita);
			  riga.setScontoModalita(scontoModalita);
			   ((RigaVendita) riga).setAgente(agente);
			   ((RigaVendita) riga).setProvvigione1Agente(provvigione1Agente);			  
			   ((RigaVendita) riga).setSubagente(subagente);
			   ((RigaVendita) riga).setProvvigione1Subagente(provvigione1Subagente);
			   ((RigaVendita) riga).setDifferenzaPrezzoAgente(differenzaPrezzoAgente);
			   ((RigaVendita) riga).setDifferenzaPrezzoSubagente(differenzaPrezzoSubagente);
		     }
 	  //Fix 37556 inizio	  
 	  else 
 	  {
 		 OrdineVenditaRigaPrm rigaPrm = getRigaPrimaria();
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
  
//40694
public void cambioTestataStatoIM(boolean newRow) {
	if(newRow) {
		OrdineVendita testata = ((OrdineVendita) getTestata());
		if(testata != null) {
			if((testata.getStatoIntellimag() == OrdineVendita.TRASMESSO || testata.getStatoIntellimag() == OrdineVendita.PRELEVATO)&& isTipoRigaDaTrassmeso()) {
				testata.setStatoIntellimag(OrdineVendita.RITRASMETTERE);
			}
		}
	}
}
//40694

//Fix 44499 Inizio
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
	           ((DettRigaConfigurazioneOrdVen) dettRigaCfg).recuperaDettRigaConfigPrezzo(this); 
	        }
	      }
	  }
}  
//44784 fine
}
