 /* @(#)OffertaClienteRiga.java

 */

/**
 * OffertaClienteRiga
 *
 * <br></br><b>Copyright (C) : Thera s.p.a.</b>
 * @author MARZOUK FATTOUMA 12/02/2009 at 10:04:36
 */
/*
 * Revisions:
 * Number      Date          Owner      Description
 *             13/02/2009    Wizard     Codice generato da Wizard
 * 10685       10/04/2009    PM
 * 10719       15/04/2009    PM	        ...
 * 10761       30/04/2009    PM	        ...
 * 10750       29/04/2009    MG         doppia gestione provvigione scala sconti
 * 10955       17/06/2009    Gscarta    modificate chiamate a convertiUM dell'articolo per passare la versione
 * 11176       24/08/2009    FM         implementazione copia offerta cliente
 * 11529       26/10/2009    ME         Propagazione commessa su righe secondarie al salvataggio
 * 11685       08/12/2009    FM         prezzo calculato da kit in caso da generazione riga secondario
 * 11976       22/01/2010    PM         Ridefinito metodo impostaStatoEvasioneTestata()
 * 12584       04/05/2010    PM         Nelle offerte non deve essere attivatà la creazione dei lotti automatici
 * 12969       30/07/2010    Linda      modificare l'errore del metodo checkEsistenzaRigaOrdineCollegata
 * 13423       21/10/2010    FM         Correct copiaRiga definition to copy riga sec
 * 13515       06/12/2010    TF         Calcolo provvigioni su prezzo extra
 * 13494       07/01/2011    OC         Corretto totale dal offerta di cliente
 * 13494       04/02/2011    MBH        trasferisci del metodo stornaImportiRigaDaTestata ha DocumentoVenRiga
 * 14225       31/03/2011    OC         Corretto la generazione di righe OffertaSec a partire di una distinta per copiare il campo Nota e DacumentoMM
 * 14765       19/08/2011    Linda      inizializzato IdConcorrenteOfferta nel metodo completaBO()
 * 14727       13/07/2011    RA         Gestione DescrizioneExtArticolo
 * 14670       20/07/2011    Linda      Modificare il metodo generaRigheKit()
 * 16754       11/09/2012    AYM        Corretto il problema “Entità non trovata” nella  elimina  di offerta cliente in  caso di presenza di riga omaggio collegata.
 * 16840       13/09/2012    Linda      Ridefinire il metodo isUtenteAutorizzatoForzaturaPrelLotto() e controllaDispUnicoLottoEffettivo().
 * 16893       22/03/2013    Ichrak     Gancio per personalizzazioni
 * 18900       25/12/2013    MA         Aggiunto i collezioni "ListCausaliRiga" e "ListUnitaMisura"
 * 19167       10/02/2014    LTB        Modifica getListCausaliRiga
 * 18703       14/11/2013    Ichrak     Aggiungere il metodo per calcolo importi di righe spese percentuale
 * 19757       22/05/2014    Ichrak     Correzione del cast su EspNodoArticoloBase
 * 18156       25/06/2013    MA         L'autorizzazione alla "forzatura lotto" in verde chiaro equivale alla non autorizzazione
 * 17639       14/05/2013    TF         Agevolazioni per personalizzazioni : metodo da protected a public
 * 20387       26/12/2014    Linda      Se in copia di una riga primaria che possiede righe secondarie l'utente cambia la configurazione,
 *                                      al salvataggio il sitema deve emettere un warning.
 * 18753       18/11/2013    Linda      Modificato il metodo beforeSaveRigaPrm(...).
 * 22229       29/09/2015    Linda      Modificato metodo getProvvigioneDaSconto().
 * 22839       15/01/2016    Linda      Redefine metodo getEntityRiga().
 * 23345       04/04/2016    Linda      Aggiunto metodo controlloRicalcoloCondizioniVen().
 * 23709       03/06/2016    OCH        Se in modifica di una riga primaria che possiede righe secondarie l'utente cambia la configurazione,
 *                                      al salvataggio il sitema deve emettere un warning.
 * 23743       07/06/2016    OCH        Imposta null al prezzo concordato                                      
 * 24190       20/09/2016    OCH        Nella generazione delle righe kit se l'attributo KitRecuperaMagDaMod della causale e a true deve impostare valore del magazzino con il valore di magazzino della riga di esplosione                                   
 * 24299       07/10/2016    Jackal     Gancio per consentire personalizzazioni in 
 *                                      calcolo sconto su scala sconti
 * 24493       11/11/2016    OCH        Correzzione Fix 24190
 * 24613       05/12/2016    Linda      Gestione il salvataggio del dettaglio riga valore configurazione.
 * 25004       21/02/2017    OCH        Recuperato assoggIva da ArticoloFornitore se valorizzato 
 * 25682       08/06/2017    ME         Aggiunti ganci per personalizzazioni
 * 26145       17/07/2017    Jackal     Gancio per consentire personalizzazioni in 
 *                                      calcolo sconto su scala sconti
 * 27649       02/07/2018    LTB        Aggiunta un controllo bloccante che impedisca di inserire una riga con quantità non intera se l'um è gestita a quantità intera.
 * 27616       03/09/2018    LTB        Se effettuo una riga d'ordine di vendita con un articolo kit con "tipo calcolo prezzi = dal costo dei componenti per markup", 
 * 																			deve valorizzare il campo "provvigione 2 agente", nemmeno al salvataggio. 
 * 29108       02/04/2019    EP		      Modifica per attivare il ricalcolo dei dati di vendita in base al parametro presente nel metodo cambiaArticolo(...)
 * 30716       13/02/2020    LP         Integrazione CONAI
 * 30871       06/03/2020    SZ			6 Decimale.
 * 31790       28/08/2020    SZ			Se saldo manuale le righe secondarie sono da saldare.
 * 33218       13/04/2021    LP         Integrazione CONAI
 * 33762       08/06/2021    YBA        Correggere il problema che durante la  copia una riga offerta di vendita, contenente un kit, e modificando la data della riga primaria la modifica non viene trasmessa alle righe secondarie
 * 33905       02/07/2021    SZ			Nel caso di kit non gestito a magazzino il costo deve essere la somma dei costi delle righe secondarie  
 * 36857       25/10/2022    YBA        Modificato metodo getProvvigioneDaSconto().
 * 37244  	   08/12/2022  	 YBA     	Corregere il problemea perché del copia un ordine cambiando la causale e le codizioni di vendita sono impostate Da documento gli sconti testata e la provvigione 1 delle righe non deveno essere riprese dalla testata.
 * 39363  	   20/07/2023  	 SZ			Evitare il nullPointerException nel check del Um.
 * 41868       26/03/2024    TA         Aggiunto il metodo calcolaDateRigheSecondarie(rigaSec)
 * 42285       13/05/2024	 KD 	    In righe l'inserimento della chiusura offerta viene salvata senza saldare l'offerta.
 * 43795       06/11/2024    KD         redifine serveRicalProvv 
 * 44166       05/12/2024    SZ			Aggiunto il metodo checkStatoSospeso()
 * 44409       25/12/2024    TA         Corretto l'anomalia si duplica una riga d'ordine che è in stato "definitiva", si imposta a "Provvisoria", le righe secondarie rimangono con stato "Definitiva"
 * 44522       15/01/2025    TA         Recupera i dati di Agente, Sub-Agente e Responsabile vendite.
 * 44784  	   02/05/2025    RA	  		Rendi la ConfigArticoloPrezzo persistent
 * 45246       16/05/2025    SZ			Attributi Da Usare nel batch ricalcolaPrezzi 
 * 46088       09/06/2025    TA         Togliere le seguenti righe
 */
package it.thera.thip.vendite.offerteCliente;

import java.math.*;
import java.sql.*;
import java.util.*;

import com.thera.thermfw.base.*;
import com.thera.thermfw.common.*;
import com.thera.thermfw.persist.*;
import com.thera.thermfw.security.*;

import it.thera.thip.acquisti.generaleAC.*;
import it.thera.thip.acquisti.offerteFornitore.*;
import it.thera.thip.base.agentiProvv.*;
import it.thera.thip.base.articolo.*;
import it.thera.thip.base.azienda.*;
import it.thera.thip.base.catalogo.*;
import it.thera.thip.base.cliente.*;
import it.thera.thip.base.comuniVenAcq.*;
import it.thera.thip.base.comuniVenAcq.web.*;
import it.thera.thip.base.documenti.*;
import it.thera.thip.base.generale.*;
import it.thera.thip.base.interfca.*;
import it.thera.thip.base.listini.*;
import it.thera.thip.cs.*;
import it.thera.thip.datiTecnici.configuratore.*;
import it.thera.thip.datiTecnici.distinta.*;
import it.thera.thip.datiTecnici.modpro.*;
import it.thera.thip.magazzino.generalemag.*;
import it.thera.thip.magazzino.saldi.*;
import it.thera.thip.vendite.generaleVE.*;
import it.thera.thip.vendite.offerteCliente.web.*;
import it.thera.thip.vendite.ordineVE.*;
import it.thera.thip.vendite.prezziExtra.*;

public class OffertaClienteRigaPrm
    extends OffertaClienteRiga
    //implements RigaPrimaria//Fix 24613
    implements RigaPrimaria,RigaConDettaglioConf//Fix 24613

{

  //Fix 23345 inizio
  //ProvenienzaPrezzo
  public static final char PROV_PREZZO_LISTINO_GENERICO = '1';
  public static final char PROV_PREZZO_LISTINO_CLIENTE = '2';
  public static final char PROV_PREZZO_LISTINO_ZONA = '3';
  public static final char PROV_PREZZO_LISTINO_CATEG_VEN = '4';
  //Fix 23345 fine
  protected OneToMany iRigheSecondarie = new OneToMany(it.thera.thip.vendite.offerteCliente.OffertaClienteRigaSec.class, this, 15, true);

  protected boolean iGeneraRigheSecondarie = true;
  protected boolean iServeRicalcoloProvvAgente = false;
  protected boolean iServeRicalcoloProvvSubagente = false;
  public boolean isBOCompleted = false;
  protected boolean iSaveFromPDC = false;
  protected boolean iSalvaRigheSecondarie = true;
  private OffertaClienteRigaPrm rigaOmf;
  protected boolean iPropagaCausaleChiusura = false;


  protected static final String SELECT_STATO_EVASIONE_RIGA =
      "SELECT " +
      OffertaClienteRigaTM.ID_RIGA_OFF + ", " +
      OffertaClienteRigaTM.STATO_EVASIONE + " " +
      "FROM " + OffertaClienteRigaPrmTM.TABLE_NAME + " " +
      "WHERE " +
      OffertaClienteRigaTM.ID_AZIENDA + "=? AND " +
      OffertaClienteRigaTM.ID_ANNO_OFF + "=? AND " +
      OffertaClienteRigaTM.ID_NUMERO_OFF + "=? AND " +
      OffertaClienteRigaTM.STATO + "<>'" + DatiComuniEstesi.ANNULLATO + "'";

  protected static CachedStatement cSelectStatoEvasioneRiga =
      new CachedStatement(SELECT_STATO_EVASIONE_RIGA);



//Fix 10761 PM Inizio
  protected static final String SQL_RIC_ORDVEN_RIG =
	  	"SELECT COUNT(*) FROM " + OrdineVenditaRigaPrmTM.TABLE_NAME +
	  	" WHERE "+OrdineVenditaRigaPrmTM.ID_AZIENDA + "=? AND " +
	  	OrdineVenditaRigaPrmTM.R_ANNO_BOZZA+"=? AND " 						 +
	  	OrdineVenditaRigaPrmTM.R_NUMERO_BOZZA+"=? AND "					 +
	  	OrdineVenditaRigaPrmTM.R_RIGA_BOZZA+"=? AND "						 +
	  	OrdineVenditaRigaPrmTM.R_DET_RIGA_BOZZA+"=?";

  protected static final CachedStatement csRicOrdVenRig = new CachedStatement(SQL_RIC_ORDVEN_RIG);

//Fix 10761 PM Fine
  public OffertaClienteRigaPrm()
  {
    super();
    iRigheLotto = new OneToMany(OffertaClienteRigaLottoPrm.class, this, 15, true);
    iRigaCollegata = new Proxy(OffertaClienteRigaPrm.class);
    datiArticolo = (DatiArticoloRigaVendita)Factory.createObject(DatiArticoloRigaVendita.class);
    iOrdineVenditaRiga = new Proxy(it.thera.thip.vendite.ordineVE.OrdineVenditaRigaPrm.class); //Fix 10685 PM

  }

  protected TableManager getTableManager() throws java.sql.SQLException
  {
    return OffertaClienteRigaPrmTM.getInstance();
  }

  public void setKey(String key)
  {
    String objIdAzienda = KeyHelper.getTokenObjectKey(key, 1);
    setIdAzienda(objIdAzienda);

    String objAnnoDocumento = KeyHelper.getTokenObjectKey(key, 2);
    setAnnoDocumento(objAnnoDocumento);

    String objNumeroDocumento = KeyHelper.getTokenObjectKey(key, 3);
    setNumeroDocumento(objNumeroDocumento);

    Integer objNumeroRigaDocumento = KeyHelper.stringToIntegerObj(KeyHelper.
        getTokenObjectKey(key, 4));
    setNumeroRigaDocumento(objNumeroRigaDocumento);
  }

  public String getKey()
  {
    String idAzienda = getIdAzienda();
    String annoDocumento = getAnnoDocumento();
    String numeroDocumento = getNumeroDocumento();
    Integer numeroRigaDocumento = getNumeroRigaDocumento();
    Object[] keyParts =
        {
        idAzienda, annoDocumento, numeroDocumento, numeroRigaDocumento};
    return KeyHelper.buildObjectKey(keyParts);
  }

  //Metodi per gestione OneToMany

  public void setIdAzienda(String idAzienda)
  {
    super.setIdAzienda(idAzienda);
    iRigheSecondarie.setFatherKeyChanged();
  }

  public void setAnnoDocumento(String annoDocumento)
  {
    super.setAnnoDocumento(annoDocumento);
    iRigheSecondarie.setFatherKeyChanged();
  }

  public void setNumeroDocumento(String numeroDocumento)
  {
    super.setNumeroDocumento(numeroDocumento);
    iRigheSecondarie.setFatherKeyChanged();
  }

  public void setNumeroRigaDocumento(Integer numeroRigaDocumento)
  {
    super.setNumeroRigaDocumento(numeroRigaDocumento);
    iRigheSecondarie.setFatherKeyChanged();
  }

  public void setTipoModello(ModproEsplosione esplosione)
  {
    esplosione.setTipiModello(new char[]
                              {ModelloProduttivoPO.PRODUZIONE});
  }

  public void setDettaglioRigaDocumento(Integer dettaglioRigaDocumento)
  {
    this.iDettaglioRigaDocumento = dettaglioRigaDocumento;
    setDirty();
  }

  public List getRigheSecondarie()
  {
    return getRigheSecondarieInternal();
  }

  protected OneToMany getRigheSecondarieInternal()
  {
    if (iRigheSecondarie.isNew())
      iRigheSecondarie.retrieve();
    return iRigheSecondarie;
  }

  public boolean initializeOwnedObjects(boolean result)
  {
    result = super.initializeOwnedObjects(result);
    return iRigheSecondarie.initialize(result);
  }

  public int saveOwnedObjects(int rc) throws SQLException
  {
    rc = super.saveOwnedObjects(rc);
    if (isSalvaRigheSecondarie())
      rc = iRigheSecondarie.save(rc);
    return rc;

  }

  public int deleteOwnedObjects() throws SQLException
  {
    int ret = super.deleteOwnedObjects();
    return getRigheSecondarieInternal().delete(ret);
  }

  public void setEqual(Copyable obj) throws CopyException
  {
    super.setEqual(obj);
    OffertaClienteRigaPrm rigaPrm = (OffertaClienteRigaPrm)obj;
    iRigheSecondarie.setEqual(rigaPrm.iRigheSecondarie);
  }

  public void setGeneraRigheSecondarie(boolean b)
  {
    this.iGeneraRigheSecondarie = b;
  }

  public boolean isGeneraRigheSecondarie()
  {
    return iGeneraRigheSecondarie;
  }

  public void propagaDatiTestata(SpecificheModificheRigheOrd spec)
  {
    super.propagaDatiTestata(spec);
    Iterator righeSec = getRigheSecondarie().iterator();
    while (righeSec.hasNext())
    {
      OffertaClienteRigaSec rigaSec = (OffertaClienteRigaSec)righeSec.next();
      rigaSec.propagaDatiTestata(spec);
    }
  }

  public boolean isArticoloRigaArticoloDefaultCatalogo()
  {
    boolean ret = false;

    PersDatiVen pdv = PersDatiVen.getCurrentPersDatiVen();
    String idArtPdv = pdv.getIdArticoloPerCatalogoEst();
    if (idArtPdv != null)
    {
      String idArtRiga = getIdArticolo();
      ret = idArtRiga.equals(idArtPdv);
    }

    return ret;
  }

  public void completaBO()
  {
    super.completaBO();
    OffertaCliente offertaCliente = (OffertaCliente)getTestata();
    setSequenzaRiga(getNumeroNuovaRiga(offertaCliente));
    CausaleRigaOffertaCliente causale = getCausaleRiga();
    //Fix 10685 PM Inizio
    /*if (causale != null && causale.getTipoRiga() != TipoRiga.SPESE_MOV_VALORE)
    {
      setDataConsegnaProduzione(offertaCliente.getDataConsegnaProduzione());
      setSettConsegnaProduzione(offertaCliente.getSettConsegnaProduzione());
    }*/
  //Fix 10685 PM Fine
    setDataConsegnaRichiesta(offertaCliente.getDataConsegnaRichiesta());
    setDataConsegnaConfermata(offertaCliente.getDataConsegnaConfermata());
    setSettConsegnaRichiesta(offertaCliente.getSettConsegnaRichiesta());
    setSettConsegnaConfermata(offertaCliente.getSettConsegnaConfermata());

    if (causale != null)
    {
      setNonFatturare(causale.getNonFatturare());
    }

    setIdConcorrenteOfferta(offertaCliente.getIdConcorrenteOfferta()); //Fix 14765

    isBOCompleted = true;

  }

  public int save() throws SQLException
  {
	//Fix 45246 inizio
    if(isSalvatagioRigaDaIngorare())
	  return 0;
	//Fix 45246 fine  
    boolean newRow = !isOnDB();
    //Fix 31790 Inizio
  //Fix 42285 Inizio
    /*if (this.getStatoEvasione() != StatoEvasione.SALDATO)
    	//setIdCausaleChiusuraOffVen(null);*/
  //Fix 42285 Fine
    //Fix 31790 Fine
    if (iPropagaCausaleChiusura)
      propagaCausaleChiusura();
    beforeSaveRigaPrm(newRow);

    //Fix 11529 - inizio
    char tipoParte = getArticolo().getTipoParte();
    if (tipoParte == ArticoloDatiIdent.KIT_GEST || tipoParte == ArticoloDatiIdent.KIT_NON_GEST) {
    	propagaCommessaSuRigheSec(getRigheSecondarie());
    }
    //Fix 11529 - fine

    int rc = super.save();

    //Fix 24613 inizio
    if (rc >= 0) {
      int dettCfgRit = salvaDettRigaConf(newRow);
      if (dettCfgRit < 0)
        rc = dettCfgRit;
      else
        rc = rc + dettCfgRit;
    }
    //Fix 24613 fine
    salvaConfigArticoloPrezzoList(newRow);//44784
    if (rc >= 0)
    {

      /*if (rc > 0 && isRigaAContratto() && !getSaveFromPDC())
        aggiornaRigaPianoConsegnaCollegata();*/
      setSalvaRigheSecondarie(false);
      disabilitaCalcolaImportiRiga();
      this.setSalvaTestata(false);
      int rc1 = super.save();
      rc = rc1 >= 0 ? rc + rc1 : rc1;
      setSalvaRigheSecondarie(true);
    }

    if (rc > 0 && newRow && getTipoRiga() == TipoRiga.MERCE &&
        rigaOmf != null)
      rc = gestioneRigheOmaggio((OffertaClientePO)getTestata(), rc);

    //...FIX 30716
    try {
      if (rc > 0 && GestioneConaiHelper.getInstance().getPersDatiConai() != null)
        //if(getIdCliente() != null && !getIdCliente().trim().equals("")) //...33218
          GestioneConaiHelper.getInstance().aggiungiRigheOffCliGestioneConai(this);
    }
    catch(Exception e) {
       Trace.excStream.println("#### ECCEZIONE IN GESTIONE CONAI RIGA OFF CLI ####");
       e.printStackTrace(Trace.excStream);
     } 

    
    return rc;

  }

  protected void setSalvaRigheSecondarie(boolean salvaRigheSecondarie)
  {
    iSalvaRigheSecondarie = salvaRigheSecondarie;
  }

  protected boolean isSalvaRigheSecondarie()
  {
    return iSalvaRigheSecondarie;
  }

  public void setSaveFromPDC(boolean saveFromPDC)
  {
    this.iSaveFromPDC = saveFromPDC;
  }

  public boolean getSaveFromPDC()
  {
    return iSaveFromPDC;
  }

  private void beforeSaveRigaPrm(boolean newRow) throws SQLException
  {

    /*  if (!isOnDB() && getTipoRiga() == TipoRigaDocumentoVendita.OMAGGIO) {
       CausaleRigaOffertaCliente cau = this.getCausaleRiga();
       if (cau.getTpOmaggioScontoArticolo() == ScontoArticolo.SC_ART_ES_ART15 &&
           cau.getIdAssoggIvaEsArt15() != null)
         this.setIdAssogIVA(cau.getIdAssoggIvaEsArt15());
     }
     */
    PersDatiVen pdv = PersDatiVen.getCurrentPersDatiVen();
    if (pdv.getGestionePvgSuScalaSconti() &&
        (isServeRicalProvvAg() || isServeRicalProvvSubag()))
    {
      modificaProvv2Agente();
    }
    //...FIX 16893 inizio
    /*if (newRow && getTipoRiga() != TipoRiga.SPESE_MOV_VALORE &&
        (getArticolo().getTipoParte() == ArticoloDatiIdent.KIT_GEST
         ||
         getArticolo().getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST)
        )
    {

      if (isGeneraRigheSecondarie() && !isDisabilitaRigheSecondarieForCM())
      {
        gestioneKit();

        calcolaPrezzoDaRigheSecondarie();

      }

    }
    else
    {

      if (isOnDB())
      {

        gestioneDateRigheSecondarie();
      }
    }*/
    runGenerazioneRigheSec();
    //...FIX 16893 fine

    impostaSaldoManuale();
    Iterator i = getRigheSecondarie().iterator();
    while (i.hasNext())
    {
      OffertaClienteRigaSec rigaSec = (OffertaClienteRigaSec)i.next();

      //Fix 18753 inizio
      if ((!isOnDB() || (iOldRiga != null && iOldRiga.getStatoAvanzamento() != StatoAvanzamento.DEFINITIVO && getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO))
          && rigaSec.getArticolo() != null && rigaSec.getArticolo().isConfigurato() &&
          rigaSec.getConfigurazione() == null) {
        setStatoAvanzamento(StatoAvanzamento.PROVVISORIO);
        rigaSec.setStatoAvanzamento(StatoAvanzamento.PROVVISORIO);
        getWarningList().add(new ErrorMessage("THIP40T311", rigaSec.getIdArticolo()));
      }
      //Fix 18753 fine

      rigaSec.setSalvaRigaPrimaria(false);

      if ((!newRow && isQuantitaCambiata()) || !isGeneraRigheSecondarie())

        rigaSec.ricalcoloQuantita(this);
    }

    if (!isOnDB())
    {
      CatalEsterno ce = getCatalogoEsterno();
      if (ce != null && ce.getArticolo() == null)
      {
        setStatoAvanzamento(StatoAvanzamento.PROVVISORIO);
      }
    }

    impostaStatoAvanzamentoSecondarie();

    if (newRow && isBOCompleted)
    {
      completaDatiCA();
    }
  }

  public void completaDatiCA()
  {
    OffertaCliente testata = (OffertaCliente)getTestata();
    Articolo articolo = getArticolo();
    String idGruppoContiArticolo = null;
    if (articolo.getArticoloDatiContab() != null &&
        articolo.getArticoloDatiContab().getRiferimVociCA() != null)
    {
      idGruppoContiArticolo = articolo.getArticoloDatiContab().getRiferimVociCA().getIdGruppoConti();
    }
    if (getIdCommessa() == null)
      setIdCommessa(testata.getIdCommessa());
    if (getIdCentroCosto() == null)
      setIdCentroCosto(testata.getIdCentroCosto());
    if (getIdGrpCntCa() == null)
    {
      if (idGruppoContiArticolo != null)
        setIdGrpCntCa(idGruppoContiArticolo);
      else
        setIdGrpCntCa(testata.getIdGrpCntCa());
    }
    PersDatiGen pdg = PersDatiGen.getCurrentPersDatiGen();
    if (pdg.getTipoInterfCA() != PersDatiGen.NON_INTERFACCITO)
    {
      try
      {
        DatiCA datiContabili = getDatiCA();
        if (datiContabili.isIncompleto() ||
            getIdCommessa() == null || getIdCentroCosto() == null)
        {
          SottogruppoContiCA DatiCARecuperati =
              GestoreDatiCA.recuperaDatiCA(GestoreDatiCA.VENDITA,
                                           articolo,
                                           idGruppoContiArticolo,
                                           testata.getIdGrpCntCa(),
                                           testata.getIdCentroCosto(),
                                           testata.getIdCommessa());

          if (DatiCARecuperati != null)
          {
            datiContabili.completaDatiCA(DatiCARecuperati);
            if (getIdCommessa() == null &&
                DatiCARecuperati.getIdCommessa() != null)
            {
              setIdCommessa(DatiCARecuperati.getIdCommessa());
            }
            if (getIdCentroCosto() == null &&
                DatiCARecuperati.getIdCentroCosto() != null)
            {
              setIdCentroCosto(DatiCARecuperati.getIdCentroCosto());
            }
          }
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace(Trace.excStream);
      }
    }
  }

  public void impostaStatoAvanzamentoSecondarie()
  {
    /* Fix 44409 Inizio
    if (isOnDB())
    {
      if (getOldRiga() != null)
      {
        char statoAvanzamentoOld = getOldRiga().getStatoAvanzamento();
        if (getStatoAvanzamento() != statoAvanzamentoOld)
        {
          Iterator iter = getRigheSecondarie().iterator();
          while (iter.hasNext())
          {
            OffertaClienteRigaSec ordRigaSec = (OffertaClienteRigaSec)iter.next();
            ordRigaSec.setStatoAvanzamento(getStatoAvanzamento());
          }
        }
      }
    } */ 
	  //Fix 44409
    boolean propagaStato = isOnDB() && (getOldRiga() != null) && (getStatoAvanzamento() != getOldRiga().getStatoAvanzamento() );
    propagaStato = propagaStato || isInCopiaRiga;		
    if (propagaStato){
	    //propagaStato
	    Iterator iter = getRigheSecondarie().iterator();
	    while (iter.hasNext()){
	      OffertaClienteRigaSec ordRigaSec = (OffertaClienteRigaSec)iter.next();
	      ordRigaSec.setStatoAvanzamento(getStatoAvanzamento());
	    }
    }
  //Fix 44409 Fine
  }

  protected void impostaSaldoManuale()
  {
    if ((!isOnDB() && isSaldoManuale()) ||
        (isOnDB() &&
         (getOldRiga() != null && !getOldRiga().isSaldoManuale() && isSaldoManuale())))
    {
      Iterator righeSecondarie = iRigheSecondarie.iterator();
      while (righeSecondarie.hasNext())
      {
        OffertaClienteRigaSec rigaSec = (OffertaClienteRigaSec)righeSecondarie.
            next();
        rigaSec.setSaldoManuale(true);
      }
    }
  }

  protected void impostaSaldoManualeRigheSec()
  {
    Iterator righeSecondarie = iRigheSecondarie.iterator();
    while (righeSecondarie.hasNext())
    {
      OffertaClienteRigaSec rigaSec = (OffertaClienteRigaSec)righeSecondarie.next();
      rigaSec.setSaldoManuale(true);
    }
  }

  protected List getStatoEvasioneRighe()
  {

    List ret = new ArrayList();

    synchronized (cSelectStatoEvasioneRiga)
    {
      ResultSet rs = null;
      try
      {
        //Verifica se su DB c'è la riga omaggio
        Database db = ConnectionManager.getCurrentDatabase();
        db.setString(cSelectStatoEvasioneRiga.getStatement(), 1, getIdAzienda());
        db.setString(cSelectStatoEvasioneRiga.getStatement(), 2,
                     getAnnoDocumento());
        db.setString(cSelectStatoEvasioneRiga.getStatement(), 3,
                     getNumeroDocumento());

        rs = cSelectStatoEvasioneRiga.getStatement().executeQuery();
        //Verifica se c'è la riga omaggio
        while (rs.next())
        {
          OrdineRiga.StatoEvasioneRiga ser = new OrdineRiga.StatoEvasioneRiga();
          ser.setNumeroRigaDocumento(new Integer(rs.getInt(OffertaClienteRigaTM.
              ID_RIGA_OFF)));
          ser.setStatoEvasione(rs.getString(OffertaClienteRigaTM.STATO_EVASIONE).
                               charAt(0));
          ret.add(ser);
        }
      }
      catch (SQLException ex)
      {
        ex.printStackTrace();
      }
      finally
      {
        if (rs != null)
          try
          {
            rs.close();
          }
          catch (SQLException ex)
          {
            ex.printStackTrace();
          }
      }
    }

    return ret;

  }

  protected boolean recuperoDatiVenditaSave()
  {

    Articolo articolo = getArticolo();

    if (articolo != null)
    {

      char tipoParte = articolo.getTipoParte();
      char tipoCalcoloPrezzo = articolo.getTipoCalcPrzKit();
      if ((tipoParte == ArticoloDatiIdent.KIT_NON_GEST || tipoParte == ArticoloDatiIdent.KIT_GEST)
          &&
          tipoCalcoloPrezzo == ArticoloDatiVendita.DA_COMPONENTI)
      {
        return false;
      }

      else
      {
        return
            isServizioCalcDatiVendita() &&
            !isRigaOfferta() &&
            getTipoRiga() != TipoRiga.SPESE_MOV_VALORE;
      }
    }

    else
    {
      return false;
    }

  }

  protected void modificaProvv2Agente() throws SQLException
  {
    Articolo articolo = getArticolo();
    String idLineaProdotto = articolo.getIdLineaProdotto();
    String idMacroFamiglia = articolo.getIdMacroFamiglia();
    String idSubFamiglia = articolo.getIdSubFamiglia();
    String idMicroFamiglia = articolo.getIdMicroFamiglia();

    PersDatiVen pdv = PersDatiVen.getCurrentPersDatiVen();
    BigDecimal sconto = null;
    
    if(pdv.getGestionePvgSuScalaSconti()) { //27616
    BigDecimal provvSuPrezzoExtra = calcoloProvvigioniSuPrezzoExtra();//Fix 13515

    if (pdv.getScontiEsaminati() == PersDatiVen.SCONTI_TESTATA_RIGA)
      sconto =
          RicercaCondizioniDiVendita.calcoloScontoDaScontiRiga(
          getPrcScontoIntestatario(),
          getPrcScontoModalita(),
          getScontoModalita(),
          //getScontoArticolo1(),
          getValue(getScontoArticolo1(),provvSuPrezzoExtra),//Fix 13515
          //Fix 26145 - inizio
//        getScontoArticolo2(),
          getScontoArticolo2CalcoloScontoScalaSconti(),
          //Fix 26145 - fine
          //Fix 24299 - inizio
//        getMaggiorazione(),
          getMaggiorazioneCalcoloScontoScalaSconti(),
          //Fix 24299 - fine
          getSconto(),
          2
          );
    else
      sconto =
          RicercaCondizioniDiVendita.calcoloScontoDaScontiRiga(
          //getScontoArticolo1(),
          getValue(getScontoArticolo1(),provvSuPrezzoExtra),//Fix 13515
          //Fix 26145 - inizio
//        getScontoArticolo2(),
          getScontoArticolo2CalcoloScontoScalaSconti(),
          //Fix 26145 - fine
          //Fix 24299 - inizio
//        getMaggiorazione(),
          getMaggiorazioneCalcoloScontoScalaSconti(),
          //Fix 24299 - fine
          getSconto(),
          2
          );
    } //27616
//MG FIX 10750 inizio
    //if (isServeRicalProvvAg() || isServeRicalProvvSubag()) { //27616
    if (isServeRicalProvvAg() || isServeRicalProvvSubag() || isRicalProvvAgSubag()) { //27616

      if (condVen == null)
        recuperaCondizioniVendita((OffertaCliente)this.getTestata());
    }
//MG FIX 10750 fine

    //if (isServeRicalProvvAg()) //27616
    if(isServeRicalProvvAg() || isRicalProvvAgSubag()) //Fix 27616    
    {
    	BigDecimal provv2 = null; //27616
    	if(pdv.getGestionePvgSuScalaSconti()) { //27616
      provv2 =
          AgentiScontiProvv.getProvvigioneDaSconto(
          Azienda.getAziendaCorrente(),
          getIdAgente(),
          getIdCliente(),//Fix 22229
          idLineaProdotto,
          idMacroFamiglia,
          idSubFamiglia,
          idMicroFamiglia,
          getIdArticolo(),//Fix 36857
          sconto
          );
//MG FIX 10750 inizio
      BigDecimal nuovaProvvigione = provv2;
      BigDecimal vecchiaProvvigione = null;
      if (condVen != null)
        vecchiaProvvigione = condVen.getProvvigioneAgente2();
      if (vecchiaProvvigione != null) {

        char condPvgScalaSconti = PersDatiVen.getCurrentPersDatiVen().getCondizPvgScalaSconti();
        if (condPvgScalaSconti == PersDatiVen.PVG_SCALA_SCONTI_MINIMA) {
          if ( (nuovaProvvigione == null) || (nuovaProvvigione.compareTo(vecchiaProvvigione) == 1) )
            provv2 = vecchiaProvvigione;
          else
            provv2 = nuovaProvvigione;
        }
        else if (condPvgScalaSconti == PersDatiVen.PVG_SCALA_SCONTI_PRIOR) {
          if (nuovaProvvigione == null)
            provv2 = vecchiaProvvigione;
          else
            provv2 = nuovaProvvigione;
        }
      }
//MG FIX 10750 fine
//27616 inizio
    	} 
    	else {
    		provv2 = condVen.getProvvigioneAgente2();
    	}
    	//27616 fine
      if (provv2 != null)
      {
        setProvvigione2Agente(provv2);
      }
    }

    //if (isServeRicalProvvSubag())  //27616
    if(isServeRicalProvvSubag() || isRicalProvvAgSubag()) //Fix 27616
    {
    	BigDecimal provv2 = null; //27616
    	if(pdv.getGestionePvgSuScalaSconti()) { //27616
    		provv2 =
          AgentiScontiProvv.getProvvigioneDaSconto(
          Azienda.getAziendaCorrente(),
          getIdSubagente(),
          getIdCliente(),//Fix 22229
          idLineaProdotto,
          idMacroFamiglia,
          idSubFamiglia,
          idMicroFamiglia,
          getIdArticolo(),//Fix 36857
          sconto
          );
//MG FIX 10750 inizio
      BigDecimal nuovaProvvigione = provv2;
      BigDecimal vecchiaProvvigione = null;
      if (condVen != null)
        vecchiaProvvigione = condVen.getProvvigioneSubagente2();
      if (vecchiaProvvigione != null) {
        char condPvgScalaSconti = PersDatiVen.getCurrentPersDatiVen().getCondizPvgScalaSconti();
        if (condPvgScalaSconti == PersDatiVen.PVG_SCALA_SCONTI_MINIMA) {
          if ( (nuovaProvvigione == null) || (nuovaProvvigione.compareTo(vecchiaProvvigione) == 1) )
            provv2 = vecchiaProvvigione;
          else
            provv2 = nuovaProvvigione;
        }
        else if (condPvgScalaSconti == PersDatiVen.PVG_SCALA_SCONTI_PRIOR) {
          if (nuovaProvvigione == null)
            provv2 = vecchiaProvvigione;
          else
            provv2 = nuovaProvvigione;
        }
      }
//MG FIX 10750 fine
		//27616 inizio
    	} 
    	else {
    		provv2 = condVen.getProvvigioneSubagente2();
    	}
    	//27616 fine
      if (provv2 != null)
      {
        setProvvigione2Subagente(provv2);
      }

    }
  }

  protected void gestioneDateRigheSecondarie() throws SQLException
  {
	//Fix 33762 inizio
	boolean propagaDati = true;
	if (isOnDB())
	{
		OrdineRiga oldRiga = getOldRiga();
		if (oldRiga != null)
		{
			if (datiUguali(oldRiga.getDataConsegnaRichiesta(), getDataConsegnaRichiesta()) &&
				datiUguali(oldRiga.getDataConsegnaConfermata(), getDataConsegnaConfermata()) &&
				datiUguali(oldRiga.getDataConsegnaProduzione(),getDataConsegnaProduzione()))
				propagaDati = false;
		}
	}
	if (!propagaDati)
		return;
	//Fix 33762 Fine
    
      List righeSecondarie = getRigheSecondarie();
      //Fix 33762 inizio
      if (righeSecondarie.isEmpty()) 
    	  return;
    //Fix 33762 fine
      
      /* if (!righeSecondarie.isEmpty()) {
       		if (oldRiga.getDataConsegnaRichiesta() != getDataConsegnaRichiesta() ||
            oldRiga.getDataConsegnaConfermata() != getDataConsegnaConfermata() ||
            oldRiga.getDataConsegnaProduzione() != getDataConsegnaProduzione())
        {Fix 33762*/
          Iterator iter = righeSecondarie.iterator();
          while (iter.hasNext())
          {
            OffertaClienteRigaSec rigaSec = (OffertaClienteRigaSec)iter.next();
            rigaSec.setDataConsegnaRichiesta(getDataConsegnaRichiesta());
            rigaSec.setDataConsegnaConfermata(getDataConsegnaConfermata());
            rigaSec.setDataConsegnaProduzione(getDataConsegnaProduzione());
            rigaSec.setSettConsegnaRichiesta(getSettConsegnaRichiesta());
            rigaSec.setSettConsegnaConfermata(getSettConsegnaConfermata());
            rigaSec.setSettConsegnaProduzione(getSettConsegnaProduzione());
          }
        //}
  }

  protected void gestioneKit() throws SQLException
  {
    Articolo articolo = getArticolo();
    //Fix 11685 begin
 /*
    EspNodoArticolo esplosione = null;
    boolean okModello = false;
    try
    {
      esplosione = getEsplosioneNodoModello(false, articolo, ModelloProduttivo.KIT);
      okModello = true;
    }
    catch (ThipException ex)
    {
      okModello = false;
    }

    if (!okModello)
    {
      try
      {
        esplosione = getEsplosioneNodoModello(false, articolo, ModelloProduttivo.PRODUZIONE);
        okModello = true;
      }
      catch (ThipException ex)
      {
        okModello = false;
      }
    }
 */
EspNodoArticolo esplosione = esplosioneModelloDocumento(articolo);
   // if (okModello)
    if (esplosione!=null)
    //Fix 11685 end
      generaRigheSecondarieEsplosioneModello(false, esplosione);
    else
      generaRigheKit(getEsplosioneNodo(articolo));
  }

  protected EsplosioneNodo getEsplosioneNodo(Articolo articolo) throws
      SQLException
  {
    Trace.println("==============>>sono in getEsplosioneNodo");
    Trace.println(articolo.getIdArticolo());
    Trace.println(getDataConsegnaConfermata());
    Trace.println(new Integer(getQtaInUMPrmMag().intValue()).toString());

    Esplosione esplosione = new Esplosione();
    esplosione.setTipoEsplosione(Esplosione.PRODUZIONE);
    esplosione.setTrovaTestataEsatta(false);
    esplosione.setIdArticolo(articolo.getIdArticolo());
    esplosione.getProprietario().setTipoProprietario(ProprietarioDistinta.
        CLIENTE);
    esplosione.getProprietario().setCliente(((OffertaCliente)getTestata()).
                                            getCliente());

    esplosione.setTipoDistinta(DistintaTestata.NORMALE);
    esplosione.setLivelloMassimo(new Integer(1));
    esplosione.setData(getDataConsegnaConfermata());
    esplosione.setQuantita(getQtaInUMPrmMag());

    if (getIdConfigurazione() != null)
      esplosione.setIdConfigurazione(getIdConfigurazione());

    esplosione.run();
    Trace.println(esplosione.getKey());
    Trace.println(esplosione.getNodoRadice());
    return esplosione.getNodoRadice();
  }

  protected void generaRigheKit(EsplosioneNodo nodo) throws SQLException
  {

    boolean calcoloDatiVendita = false;
    Trace.println("==============>>calcoloDatiVendita=" + calcoloDatiVendita);

    List datiRigheKit = nodo.getNodiFigli();
    Trace.println("==============>>datiRigheKit=" + datiRigheKit.size());
    if (datiRigheKit.isEmpty())
    {

      return;
    }
    else
    {
      int sequenza = 0;
      Iterator iter = datiRigheKit.iterator();
      while (iter.hasNext())
      {
        EsplosioneNodo datiRigaKit = (EsplosioneNodo)iter.next();
        Trace.println("==============>>iterazione=" + datiRigaKit);
        Trace.println("\tversione=" + datiRigaKit.getIdVersione());

        OffertaClienteRigaSec rigaKit =
            (OffertaClienteRigaSec)Factory.createObject(OffertaClienteRigaSec.class);

        Articolo articoloKit = datiRigaKit.getArticolo();
        Trace.println("\tversione primaria=" + getIdVersioneSal());

        UnitaMisura umVen = articoloKit.getUMDefaultVendita();
        UnitaMisura umPrm = articoloKit.getUMPrmMag();
        UnitaMisura umSec = articoloKit.getUMSecMag();

        BigDecimal qc = datiRigaKit.getQuantitaCalcolata();
        //BigDecimal qtaCalcolata = qc.setScale(2, BigDecimal.ROUND_HALF_UP);//Fix 30871
		BigDecimal qtaCalcolata = Q6Calc.get().setScale(qc,2, BigDecimal.ROUND_HALF_UP);//Fix 30871

        BigDecimal qtaVendita =
            articoloKit.convertiUM(qtaCalcolata, umPrm, umVen, rigaKit.getArticoloVersRichiesta()); // fix 10955
        BigDecimal qtaSecondaria =
            (umSec == null) ?
            new BigDecimal(0.0) :
            articoloKit.convertiUM(qtaVendita, umVen, umSec, rigaKit.getArticoloVersRichiesta()); // fix 10955

        if (UnitaMisura.isPresentUMQtaIntera(umVen, umPrm, umSec, articoloKit))
        {
          QuantitaInUMRif qta = articoloKit.calcolaQuantitaArrotondate(qtaCalcolata, umVen, umPrm, umSec, rigaKit.getArticoloVersRichiesta(), Articolo.UM_PRM); // fix 10955
          qtaVendita = qta.getQuantitaInUMRif();
          qtaCalcolata = qta.getQuantitaInUMPrm();
          qtaSecondaria = qta.getQuantitaInUMSec();
        }

        rigaKit.setSequenzaRiga(sequenza++);
        rigaKit.setTipoRiga(getTipoRiga());
        rigaKit.setStatoAvanzamento(getStatoAvanzamento());

        rigaKit.setCoefficienteImpiego(datiRigaKit.getCoeffImpiego());
        if (datiRigaKit.getCoeffTotale())
        {
          rigaKit.setBloccoRicalcoloQtaComp(true);
          rigaKit.setCoefficienteImpiego(new BigDecimal("0"));
        }

        rigaKit.getDatiComuniEstesi().setStato(getDatiComuniEstesi().getStato());

        rigaKit.setCausaleRiga(getCausaleRiga());
        rigaKit.setMagazzino(getMagazzino());
        // Fix 24190 inizio
        DistintaLegame distinta = datiRigaKit.getDistintaLegame();
        if (distinta != null) {
	  	  if (getCausaleRiga() != null && getCausaleRiga().isKitRecuperaMagDaMod() && distinta.getMagazzino() != null)
	        rigaKit.setMagazzino(distinta.getMagazzino());
		}
        // Fix 24190 fine
        rigaKit.setArticolo(articoloKit);
        rigaKit.setDescrizioneArticolo(
            articoloKit.getDescrizioneArticoloNLS().getDescrizione()
            );
        Integer idVersioneKit = datiRigaKit.getIdVersione();
        if (idVersioneKit != null)
        {
          rigaKit.setIdVersioneRcs(idVersioneKit);
          ArticoloVersione versioneKit =
              (ArticoloVersione)Factory.createObject(ArticoloVersione.class);
          String versioneKitKey =
              KeyHelper.buildObjectKey(
              new Object[]
              {
              getIdAzienda(),
              articoloKit.getIdArticolo(),
              idVersioneKit
          }
              );
          versioneKit.setKey(versioneKitKey);
          if (versioneKit.retrieve())
          {
            ArticoloVersione versioneSaldiKit = versioneKit.getVersioneSaldi();
            if (versioneSaldiKit == null)
            {
              rigaKit.setIdVersioneSal(idVersioneKit);
            }
            else
            {
              rigaKit.setIdVersioneSal(versioneSaldiKit.getIdVersione());
            }
          }
        }

        rigaKit.setIdCommessa(getIdCommessa());
        rigaKit.setIdCentroCosto(getIdCentroCosto());
        recuperaDatiCA(rigaKit);

        rigaKit.setIdDocumentoMM(getIdDocumentoMM());

        // Fix 14225 inizio
        if (datiRigaKit.getDistintaLegame().getIdDocumentoMM() != null)
          rigaKit.setIdDocumentoMM(datiRigaKit.getDistintaLegame().getIdDocumentoMM());
        rigaKit.setNota(datiRigaKit.getDistintaLegame().getNote());
        // Fix 14225 fine

        rigaKit.setConfigurazione(datiRigaKit.getConfigurazione()); //?????
        rigaKit.setQtaInUMRif(qtaVendita);
        rigaKit.setUMRif(umVen);
        rigaKit.setQtaInUMPrmMag(qtaCalcolata);
        rigaKit.setUMPrm(umPrm);
        rigaKit.setQtaInUMSecMag(qtaSecondaria);
        rigaKit.setUMSec(umSec);
        rigaKit.setDataConsegnaRichiesta(getDataConsegnaRichiesta());
        rigaKit.setSettConsegnaRichiesta(getSettConsegnaRichiesta());
        rigaKit.setDataConsegnaConfermata(getDataConsegnaConfermata());
        rigaKit.setSettConsegnaConfermata(getSettConsegnaConfermata());
        rigaKit.setDataConsegnaProduzione(getDataConsegnaProduzione());
        rigaKit.setSettConsegnaProduzione(getSettConsegnaProduzione());
        //Scheda Prezzi/Sconti
        rigaKit.setListinoPrezzi(getListinoPrezzi());
        rigaKit.setIdResponsabileVendite(getIdResponsabileVendite());
        rigaKit.setRigaPrimaria(this);
        rigaKit.setDescrizioneExtArticolo(recuperaDescExtArticolo(rigaKit.getIdCliente(), rigaKit.getIdArticolo(), rigaKit.getIdConfigurazione()));//Fix14727 RA
        rigaKit.calcolaDatiVendita((OffertaCliente)rigaKit.getTestata());

        //AssoggettamentoIVA assIva = articoloKit.getAssoggettamentoIVA(); // Fix 25004
        AssoggettamentoIVA assIva = getAssoggettamentoIVAArticolo(articoloKit, rigaKit.getIdConfigurazione()); // Fix 25004
        AssoggettamentoIVA assIvaTestata = ((DocumentoOrdineTestata)getTestata()).getAssoggettamentoIVA();//Fix 14670
        if (assIva == null)
        {
          assIva = getAssoggettamentoIVA();//Fix 14670
          rigaKit.setAssoggettamentoIVA(getAssoggettamentoIVA());
        }
        else
        {
          rigaKit.setAssoggettamentoIVA(assIva);
        }
        //Fix 14670 inizio
        if (assIvaTestata != null && assIvaTestata.isIVAAgevolata())
          if (assIva == null ||
              (assIva.getTipoIVA() == AssoggettamentoIVA.SOGGETTO_A_CALCOLO_IVA
               && !assIva.isIVAAgevolata()
               && assIva.getAliquotaIVA().compareTo(assIvaTestata.getAliquotaIVA()) > 0))
            rigaKit.setAssoggettamentoIVA(assIvaTestata);
        //Fix 14670 fine
        rigaKit.setSalvaRigaPrimaria(false);
        //Fix 33905 Inizio
        OffertaClienteRigaSec rigaSecTmp = (OffertaClienteRigaSec)Factory.createObject(OffertaClienteRigaSec.class);
        try {
			rigaSecTmp.setEqual(rigaKit);
			rigaSecTmp.setRigaPrimaria(this);
			rigaSecTmp.cambiaArticolo(rigaSecTmp.getArticolo(),rigaSecTmp.getConfigurazione(),false);
			BigDecimal costoUnitario = rigaSecTmp.getCostoUnitario();
			if(costoUnitario != null)
				rigaKit.setCostoUnitario(costoUnitario);
		} catch (CopyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //Fix 33905 Fine
        getRigheSecondarie().add(rigaKit);
      }
    }
  }

  protected void componiChiave()
  {

    setNumeroRigaDocumento(
        new Integer(
        OffertaClienteRiga.getNumeroNuovaRiga(getTestata())
        )
        );

  }

  protected void creaOldRiga()
  {
    iOldRiga = (OffertaClienteRigaPrm)Factory.createObject(OffertaClienteRigaPrm.class);
  }

  protected OrdineRigaLotto creaLotto()
  {
    return (OffertaClienteRigaLotto)Factory.createObject(
        OffertaClienteRigaLottoPrm.class);
  }

  /** Temporary method **/
  public void creaLottiAutomatici()
  {
	 //Fix 12584 PM >
     /*
	  List lottiOrig = new ArrayList();

    char tipo = PersDatiMagazzino.TIPO_VEN_CTO_TRASF;

    ProposizioneAutLotto pal = ProposizioneAutLotto.creaProposizioneAutLotto(tipo,
        getNumeroDocumento(),
        getAnnoDocumento(),
        getTestata().getDataDocumento(),
        getNumeroRigaDocumento(),
        null,
        getIdArticolo(),
        getIdVersioneSal(),
        getIdEsternoConfig(),
        getIdMagazzino(),
        getIdCommessa(),
        getIdFornitore(),
        PersDatiMagazzino.CREA_DA_ORDINE,
        lottiOrig,
        null,
        null,
        getQuantitaResiduo().getQuantitaInUMPrm()); //...Se alla data passo null allora considero la data corrente
    List lottiAuto = pal.creaLottiAutomatici();

    if (lottiAuto != null && !lottiAuto.isEmpty())
    {
      getRigheLotto().clear();
      for (int j = 0; j < lottiAuto.size(); j++)
      {
        Lotto lt = (Lotto)lottiAuto.get(j);
        OffertaClienteRigaLotto lotto = (OffertaClienteRigaLotto)creaLotto();
        lotto.setFather(this);
        lotto.setIdArticolo(lt.getCodiceArticolo());
        lotto.setIdLotto(lt.getCodiceLotto());
        //lotto.setQtaAttesaEvasione(getQtaAttesaEvasione());
        //lotto.setQtaPropostaEvasione(getQtaPropostaEvasione());
        lotto.getQuantitaOfferta().setQuantitaInUMRif(getQuantitaOffertaVen().getQuantitaInUMRif());
        lotto.getQuantitaOfferta().setQuantitaInUMPrm(getQuantitaOffertaVen().getQuantitaInUMPrm());
        lotto.getQuantitaOfferta().setQuantitaInUMSec(getQuantitaOffertaVen().getQuantitaInUMSec());
        getRigheLotto().add(lotto);
      }
    }
   */
	//Fix 12584 PM <

  }

  public Vector checkAll(BaseComponentsCollection components)
  {
     //Fix 45246 inizio  
  	Vector errors = new Vector();
    if(isCheckRigaDaIngorare())
    	return errors;
  	//Vector errors = super.checkAll(components);
     errors = super.checkAll(components);
    //Fix 45246 fine

    Vector otherErrors = new Vector();
    otherErrors.addElement(checkRigheSecondarie());
    for (int i = 0; i < otherErrors.size(); i++)
    {
      ErrorMessage err = (ErrorMessage)otherErrors.elementAt(i);
      if (err != null)
        errors.addElement(err);
    }
    ErrorMessage em = checkStatoAnnullato();
    if (em != null)
      errors.addElement(em);
    //Fix 20387 inizio
    em = checkIdEsternoConfigInCopia();
    if (em != null)
      errors.addElement(em);
    //Fix 20387 fine
    //Fix 23345 inizio
    em = controlloRicalcoloCondizioniVen();
    if (em != null)
      errors.addElement(em);
    //Fix 23345 fine
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

  protected ErrorMessage checkRigheSecondarie()
  {
    Articolo articolo = getArticolo();

    if (articolo != null &&
        (!isOnDB() && getTipoRiga() != TipoRiga.SPESE_MOV_VALORE &&
         (articolo.getTipoParte() == ArticoloDatiIdent.KIT_GEST ||
          articolo.getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST))
        && !isDisabilitaRigheSecondarieForCM())
    {

      ModelloProduttivo modpro = null;
      boolean okModello = false;
      Stabilimento stab = null;
      if (getMagazzino() != null)
        stab = getMagazzino().getStabilimento();
      stab = (stab == null) ? PersDatiGen.getCurrentPersDatiGen().getStabilimento() : stab;
      if (stab == null)
        return new ErrorMessage("THIP110305");
      try
      {
        modpro = ModproEsplosione.trovaModelloProduttivo(getIdAzienda(), articolo.getIdArticolo(),
            stab.getIdStabilimento(), getDataConsegnaConfermata(), getIdCommessa(),
            ModelloProduttivo.GENERICO, new char[]
            {ModelloProduttivo.KIT});
        okModello = modpro != null;
      }
      catch (SQLException ex)
      {
        okModello = false;
      }

      if (!okModello)
      {
        try
        {
          modpro = ModproEsplosione.trovaModelloProduttivo(getIdAzienda(), articolo.getIdArticolo(),
              stab.getIdStabilimento(), getDataConsegnaConfermata(), getIdCommessa(),
              ModelloProduttivo.GENERICO, new char[]
              {ModelloProduttivo.PRODUZIONE});
          okModello = modpro != null;
        }
        catch (SQLException ex)
        {
          okModello = false;
        }
      }

      if (!okModello)
      {
        try
        {
          List datiRigheKit = getEsplosioneNodo(articolo).getNodiFigli();
          if (datiRigheKit.isEmpty())
          {
            setGeneraRigheSecondarie(false);
            return new ErrorMessage("THIP_BS151");
          }
        }
        catch (SQLException ex)
        {
          ex.printStackTrace();
        }

      }
    }
    return null;
  }

  public void calcolaPrezzoDaRigheSecondarie()
  {
    calcolaPrezzoDaRigheSecondarieConReset(true);
  }

  public void calcolaPrezzoDaRigheSecondarieSenzaReset()
  {
    calcolaPrezzoDaRigheSecondarieConReset(false);
  }

  public void calcolaPrezzoDaRigheSecondarieConReset(boolean reset)
  {
    try
    {
      char tipoParte = getArticolo().getTipoParte();
      char tipoCalcoloPrezzo = getArticolo().getTipoCalcPrzKit();
      if ((tipoParte == ArticoloDatiIdent.KIT_NON_GEST ||
           tipoParte == ArticoloDatiIdent.KIT_GEST)
          &&
          tipoCalcoloPrezzo == ArticoloDatiVendita.DA_COMPONENTI || (tipoCalcoloPrezzo == ArticoloDatiVendita.SUL_PRODOTTO_FINITO)) { //72269 Softre anche su pf
//      {
        BigDecimal zero = new BigDecimal(0.0);
        BigDecimal prezzoRigaPrimaria = zero;
        BigDecimal costoRigaPrimaria = zero ;//Fix 33905
        ValorizzatoreImportiOffertaVendita viov =
            new ValorizzatoreImportiOffertaVendita();
        ImportiRigaOffertaVendita importi = viov.calcolaImportiRiga(this);

        Iterator righeSecondarie = importi.getValoriRigheSecondarie().iterator();
        while (righeSecondarie.hasNext())
        {
          ImportiRigaOffertaVendita importoRigaSec =
              (ImportiRigaOffertaVendita)righeSecondarie.next();
          if (importoRigaSec.getSpecializzazioneRiga() == RIGA_SECONDARIA_PER_COMPONENTE)
          {//Fix 33905
            	//prezzoRigaPrimaria = prezzoRigaPrimaria.add(importoRigaSec.getValoreOrdinato()); //Fix 33905
            	prezzoRigaPrimaria = prezzoRigaPrimaria.add(importoRigaSec.getValoreOfferto()); //Fix 33905
            	costoRigaPrimaria = costoRigaPrimaria.add(importoRigaSec.getCostoOfferto());// Fix 33905
          }//Fix 33905
        }
        if (this.getQtaInUMRif() != null && this.getQtaInUMRif().compareTo(new BigDecimal(0.0)) != 0)//Fix 33905
        {//Fix 33905
        	prezzoRigaPrimaria = prezzoRigaPrimaria.divide(getQtaInUMRif(), BigDecimal.ROUND_HALF_UP);
        	costoRigaPrimaria = costoRigaPrimaria.divide(getQtaInUMRif(), BigDecimal.ROUND_HALF_UP);//Fix 33905
        }//Fix 33905
        BigDecimal markup = getArticolo().getMarkupKit();
        if (markup != null && markup != zero)
        {
          BigDecimal perc = markup.divide(new BigDecimal(100.0), 6, BigDecimal.ROUND_HALF_UP);
          prezzoRigaPrimaria = prezzoRigaPrimaria.add(prezzoRigaPrimaria.multiply(perc));
        }
        setPrezzo(prezzoRigaPrimaria);
        //Fix 33905 Inizio
        if(tipoParte == ArticoloDatiIdent.KIT_NON_GEST)
        	setCostoUnitario(costoRigaPrimaria);
        //Fix 33905 Fine
        if (reset)
        {
          setScontoArticolo1(new BigDecimal(0.0));
          setScontoArticolo2(new BigDecimal(0.0));
          setMaggiorazione(new BigDecimal(0.0));
          setSconto(null);
          setTipoPrezzo(TipoPrezzo.LORDO);
          setProvenienzaPrezzo(TipoRigaRicerca.MANUALE);
        }

        aggiornaProvvigioni();
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public void aggiornaValoriNuovi()
  {
    if (this.getOldRiga() != null)
    {
      this.stornaImportiRigaSpesa((OffertaCliente)getTestata(), (OffertaClienteRiga)this.getOldRiga());
      stornaImportiImpostaRiga((OffertaCliente)getTestata(), (OffertaClienteRiga)this.getOldRiga());
    }
  }

  protected void stornaImportiImpostaRiga(OffertaCliente testata, OffertaClienteRiga oldRiga)
  {
    //Valore offerto
    BigDecimal valoreOfferto =
        getNotNullValue(testata.getValoreImposta()).
        subtract(
        getNotNullValue(oldRiga.getValoreImposta())
        );
    testata.setValoreImposta(valoreOfferto);

    //Valore ordianto
    BigDecimal valoreOrdinato =
        getNotNullValue(testata.getValoreImpostaOrd()).
        subtract(
        getNotNullValue(oldRiga.getValoreImpostaOrdinato())
        );
    testata.setValoreImpostaOrd(valoreOrdinato);

  }

  public void annullaOldRiga()
  {
    super.annullaOldRiga();
    List righeSec = getRigheSecondarie();
    Iterator iter = righeSec.iterator();
    while (iter.hasNext())
    {
      DocumentoOrdineRiga rigaSec = (DocumentoOrdineRiga)iter.next();
      rigaSec.annullaOldRiga();
    }
  }

  public void aggiornaProvvigioni()
  {
    try
    {
      /*String idAgente = this.getIdAgente();
      String idSubAgente = this.getIdSubagente();
      OffertaCliente testata = ((OffertaCliente)this.getTestata());
      if ((idAgente != null && !idAgente.trim().equals("")) || (idSubAgente != null && !idSubAgente.trim().equals("")))
      {
        CondizioniDiVendita cV = (CondizioniDiVendita)Factory.createObject(CondizioniDiVendita.class);
        cV.setRArticolo(getIdArticolo());
        cV.setRSubAgente(idSubAgente);
        cV.setRAgente(idAgente);
        cV.setRValuta(testata.getIdValuta());
        cV.setRUnitaMisura(this.getIdUMRif());
        //cV.setRCliente(this.getIdCliente());
        cV.setIdAzienda(this.getIdAzienda());
        cV.setRConfigurazione(this.getIdConfigurazione());

        java.sql.Date dataValid = null;
        PersDatiVen pda = PersDatiVen.getCurrentPersDatiVen();
        char tipoDataPrezziSconti = testata.getCliente().getRifDataPerPrezzoSconti();
        if (tipoDataPrezziSconti == RifDataPrzScn.DA_CONDIZIONI_GENERALI)
          tipoDataPrezziSconti = pda.getTipoDataPrezziSconti();
        switch (tipoDataPrezziSconti)
        {
          case RifDataPrzScn.DATA_ORDINE:
            dataValid = TimeUtils.getDate(testata.getDataDocumento());
            break;
          case RifDataPrzScn.DATA_CONSEGNA:
            dataValid = TimeUtils.getDate(this.getDataConsegnaConfermata());
            break;
        }
        cV.setDataValidita(dataValid);

        cV.setMaggiorazione(this.getMaggiorazione());
        cV.setSconto(this.getSconto());
        cV.setScontoArticolo1(this.getScontoArticolo1());
        cV.setScontoArticolo2(this.getScontoArticolo2());
        cV.setProvvigioneAgente1(this.getProvvigione1Agente());
        cV.setProvvigioneSubagente1(this.getProvvigione1Subagente());
        cV.setQuantita(this.getQtaInUMRif());
        cV.setRModalitaPagamento(testata.getIdModPagamento());

        RicercaCondizioniDiVendita ric = (RicercaCondizioniDiVendita)Factory.createObject(RicercaCondizioniDiVendita.class);
        ric.setCondizioniDiVendita(cV);
        ric.aggiornaProvvigioni();
        setProvvigione2Agente(ric.getCondizioniDiVendita().getProvvigioneAgente2());
        setProvvigione2Subagente(ric.getCondizioniDiVendita().getProvvigioneSubagente2());
  }*/
    	//27616 inizio
      PersDatiVen pdv = PersDatiVen.getCurrentPersDatiVen();
      if ((pdv.getGestionePvgSuScalaSconti() || ( pdv.getGestioneAnagraPvg() && isRicalProvvAgSubag()))  && 
    		  (isServeRicalProvvAg() || isServeRicalProvvSubag() || isRicalProvvAgSubag())) {
        modificaProvv2Agente();
      }
    	//27616 fine 
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  //Fix 27616 inizio
  protected boolean isRicalProvvAgSubag(){
  	if(isOnDB()) 
  		return false;
	  Articolo art = getArticolo();
	  if(art != null && art.getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST && art.getTipoCalcPrzKit() == ArticoloDatiVendita.DA_COMPONENTI)
	  	return true;
	  return false;
  }
  //Fix 27616 fine
  public void setServeRicalProvvAg(boolean b)
  {
    this.iServeRicalcoloProvvAgente = b;
  }

  public boolean isServeRicalProvvAg()
  {
    return iServeRicalcoloProvvAgente;
  }

  public void setServeRicalProvvSubag(boolean b)
  {
    this.iServeRicalcoloProvvSubagente = b;
  }

  public boolean isServeRicalProvvSubag()
  {
    return iServeRicalcoloProvvSubagente;
  }

  protected int gestioneRigheOmaggio(OffertaClientePO testata, int rc) throws
      SQLException
  {
    rigaOmf.setSalvaTestata(false);
    int rc1 = rigaOmf.save();

    if (rc1 >= 0)
      rc += rc1;
    else
      rc = rc1;

    return rc;
  }

  protected int eliminaRigaOmaggioCollegata(String key) throws SQLException
  {
    int rc = 0;

    OffertaClienteRigaPrm rigaOmf =
        (OffertaClienteRigaPrm)Factory.createObject(OffertaClienteRigaPrm.class);
    rigaOmf.setKey(key);
    if (rigaOmf.retrieve())
    {
      rigaOmf.setSalvaTestata(false);
      rc = rigaOmf.delete();
    }

    return rc;
  }

  protected int eliminaRiga() throws SQLException
  {
    //Fix 16754 inizio
      if(getTipoRiga()==TipoRiga.OMAGGIO &&!getAttivaCheckCancellazione()){
        if(getIdRigaCollegata() != null && getIdDettaglioRigaCollegata() != null) {
           if (!retrieve())
            return 0;
        }
     }
    //Fix 16754 fine
    Iterator i = getRigheSecondarie().iterator();
    while (i.hasNext())
    {
      OffertaClienteRigaSec rigaSec = (OffertaClienteRigaSec)i.next();
      rigaSec.setSalvaRigaPrimaria(false);
    }
    int rc = super.eliminaRiga();
    return rc;
  }

  protected void recuperaDatiCA(OffertaClienteRigaSec rigaSec)
  {
    RiferimentoVociCA rifVociCA = rigaSec.getArticolo().getRiferimVociCA();
    if (rifVociCA != null)
      rigaSec.setIdGrpCntCa(rifVociCA.getIdGruppoConti());

    SottogruppoContiCA datiCA = null;
    datiCA = GestoreDatiCA.recuperaDatiCA(
        GestoreDatiCA.VENDITA,
        rigaSec.getArticolo(),
        rifVociCA != null ? rifVociCA.getIdGruppoConti() : null,
        null,
        rigaSec.getIdCentroCosto(),
        rigaSec.getIdCommessa());
    if (datiCA != null)
    {
      rigaSec.setIdCentroCosto(datiCA.getIdCentroCosto());
      rigaSec.setIdCommessa(datiCA.getIdCommessa());
      rigaSec.getDatiCA().setIdVoceSpesaCA(datiCA.getIdVoceSpesa());
      rigaSec.getDatiCA().setIdVoceCA4(datiCA.getIdVoce4());
      rigaSec.getDatiCA().setIdVoceCA5(datiCA.getIdVoce5());
      rigaSec.getDatiCA().setIdVoceCA6(datiCA.getIdVoce6());
      rigaSec.getDatiCA().setIdVoceCA7(datiCA.getIdVoce7());
      rigaSec.getDatiCA().setIdVoceCA8(datiCA.getIdVoce8());
    }
  }

  //Fix 17639 inizio
  //protected DocumentoOrdineRiga generaRigaSecondariaModello(EspNodoArticoloBase
  //    datiRigaSec, int sequenza) throws SQLException
  //{
  public DocumentoOrdineRiga generaRigaSecondariaModello(EspNodoArticoloBase
      datiRigaSec, int sequenza) throws SQLException
  {
  //Fix 17639 fine

    OffertaClienteRigaSec rigaSec =
        (OffertaClienteRigaSec)super.generaRigaSecondariaModello(datiRigaSec,
        sequenza);
    CausaleRigaOffertaCliente causaleRigaPrm = getCausaleRiga();
    rigaSec.setCausaleRiga(causaleRigaPrm);

    if (rigaSec.getMagazzino() == null)
    {
    	//Fix 25682 - inizio
//    	rigaSec.setMagazzino(getMagazzino());
    	rigaSec.setMagazzino(getMagazzinoRigaSecModello(datiRigaSec));
    	//Fix 25682 - fine
    }
	
    if(datiRigaSec instanceof EspNodoArticolo){  // Fix 24493
    // Fix 24190 inizio
    AttivitaProdMateriale atvMat = ((EspNodoArticolo) datiRigaSec).getAttivitaProdMateriale();
    if(atvMat != null){
      if(causaleRigaPrm != null && causaleRigaPrm.isKitRecuperaMagDaMod() && atvMat.getMagazzinoPrelievo() != null)
	rigaSec.setMagazzino(atvMat.getMagazzinoPrelievo());
    }
    // Fix 24190 fine
    }

    Articolo articoloSec = datiRigaSec.getArticoloUsato().getArticolo();

    UnitaMisura umRif = articoloSec.getUMDefaultVendita();

    UnitaMisura umPrm = articoloSec.getUMPrmMag();
    UnitaMisura umSec = articoloSec.getUMSecMag();

    BigDecimal qc = datiRigaSec.getQuantitaCalcolata();

    //BigDecimal qtaCalcolata = qc.setScale(2, BigDecimal.ROUND_HALF_UP);//Fix 30871
    BigDecimal qtaCalcolata = Q6Calc.get().setScale(qc,2, BigDecimal.ROUND_HALF_UP);//Fix 30871
	//Fix 11685
    /*
    BigDecimal qtaRiferimento = articoloSec.convertiUM(qtaCalcolata, umPrm, umRif, rigaSec.getArticoloVersRichiesta()); // fix 10955
    BigDecimal qtaSecondaria = (umSec == null) ? new BigDecimal(0.0) :
        articoloSec.convertiUM(qtaRiferimento, umRif, umSec, rigaSec.getArticoloVersRichiesta()); // fix 10955

    Trace.println("\tqtaCalcolata=" + qtaCalcolata);
    Trace.println("\tqtaRiferimento=" + qtaRiferimento);
    Trace.println("\tqtaSecondaria=" + qtaSecondaria);
 */ //Fix 11685 end
    rigaSec.setCoefficienteImpiego(datiRigaSec.getCoeffImpiego());
    if (datiRigaSec.getCoeffTotale())
    {
      rigaSec.setBloccoRicalcoloQtaComp(true);
      rigaSec.setCoefficienteImpiego(new BigDecimal("0"));
    }
  //Fix 11685 begin
  /*
    if (UnitaMisura.isPresentUMQtaIntera(umRif, umPrm, umSec, articoloSec))
    {
      QuantitaInUMRif qta = articoloSec.calcolaQuantitaArrotondate(qtaCalcolata, umRif, umPrm, umSec, rigaSec.getArticoloVersRichiesta(), Articolo.UM_PRM); // fix 10955
      qtaRiferimento = qta.getQuantitaInUMRif();
      qtaCalcolata = qta.getQuantitaInUMPrm();
      if (umSec != null)
        qtaSecondaria = qta.getQuantitaInUMSec();
    }
*/
   QuantitaInUMRif qta = this.calcolaSoloQuantitaRigaSec(articoloSec,qtaCalcolata, rigaSec.getArticoloVersRichiesta(), umRif);
   BigDecimal qtaRiferimento = qta.getQuantitaInUMRif();
   qtaCalcolata = qta.getQuantitaInUMPrm();
   BigDecimal qtaSecondaria = qta.getQuantitaInUMSec();
  //Fix 11685 end
    rigaSec.setQtaInUMRif(qtaRiferimento);
    rigaSec.setUMRif(umRif);
    rigaSec.setQtaInUMPrmMag(qtaCalcolata);
    rigaSec.setUMPrm(umPrm);
    rigaSec.setQtaInUMSecMag(qtaSecondaria);
    rigaSec.setUMSec(umSec);
    rigaSec.setDataConsegnaRichiesta(getDataConsegnaRichiesta());
    rigaSec.setSettConsegnaRichiesta(getSettConsegnaRichiesta());
    rigaSec.setDataConsegnaConfermata(getDataConsegnaConfermata());
    rigaSec.setSettConsegnaConfermata(getSettConsegnaConfermata());
    rigaSec.setDataConsegnaProduzione(getDataConsegnaProduzione());
    rigaSec.setSettConsegnaProduzione(getSettConsegnaProduzione());
    rigaSec.setIdResponsabileVendite(getIdResponsabileVendite());
    rigaSec.setListinoPrezzi(getListinoPrezzi());
    rigaSec.setServizioCalcDatiVendita(false);
    rigaSec.setRigaPrimaria(this);
    rigaSec.calcolaDatiVendita((OffertaCliente)rigaSec.getTestata()); // Fix 11685
    rigaSec.setDescrizioneExtArticolo(recuperaDescExtArticolo(rigaSec.getIdCliente(), rigaSec.getIdArticolo(), rigaSec.getIdConfigurazione()));//Fix14727 RA

    rigaSec.setIdCommessa(getIdCommessa());
    rigaSec.setIdCentroCosto(getIdCentroCosto());
    recuperaDatiCA(rigaSec);

    rigaSec.setSalvaRigaPrimaria(false);
    //Fix 33905 Inizio
    OffertaClienteRigaSec rigaSecTmp = (OffertaClienteRigaSec)Factory.createObject(OffertaClienteRigaSec.class);
    try {
		rigaSecTmp.setEqual(rigaSec);
		rigaSecTmp.setRigaPrimaria(this);
		rigaSecTmp.cambiaArticolo(rigaSecTmp.getArticolo(),rigaSecTmp.getConfigurazione(),false);
		BigDecimal costoUnitario = rigaSecTmp.getCostoUnitario();
		if(costoUnitario != null)
			rigaSec.setCostoUnitario(costoUnitario);
	} catch (CopyException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    //Fix 33905 Fine
    return rigaSec;
  }

  public DocumentoOrdineRiga creaRigaSecondaria()
  {
    OffertaClienteRigaSec rigaSec =
        (OffertaClienteRigaSec)Factory.createObject(OffertaClienteRigaSec.class);
    return rigaSec;

  }

  /* Fix 13494 : the method stornaImportiRigaDaTestata is transfered to OffertaClienteRiga

  protected void stornaImportiRigaDaTestata()
  {
    if (getDatiComuniEstesi().getStato() != DatiComuniEstesi.ANNULLATO)
    {

      OffertaCliente testata = (OffertaCliente)getTestata();
      BigDecimal valoreOfferto = null;
      //BigDecimal valoreInSpedizione = null;
      BigDecimal valoreOrdinato = null;
      BigDecimal costoOfferto = null;
      //BigDecimal costoInSpedizione = null;
      BigDecimal costoOrdinato = null;
      BigDecimal imposta = getNotNullValue(testata.getValoreImposta());
      // BigDecimal impostaInSpedizione = getNotNullValue(testata.getValoreImpostaInSped());
      BigDecimal impostaOrdinato = getNotNullValue(testata.getValoreImpostaOrd());

      switch (getTipoRiga())
      {
        case TipoRiga.MERCE:
          valoreOfferto = testata.getValoreOfferto();
          valoreOrdinato = testata.getValoreOrdinato();
          costoOfferto = testata.getCostoOfferto();
          costoOrdinato = testata.getCostoOrdinato();
          break;
        case TipoRiga.OMAGGIO:
          valoreOfferto = testata.getValoreOmaggiOff();
          valoreOrdinato = testata.getValoreOmaggiOrd();
          costoOfferto = testata.getCostoOmaggiOff();
          costoOrdinato = testata.getCostoOmaggiOrd();
          break;
        case TipoRiga.SERVIZIO:
          valoreOfferto = testata.getValoreServiziOff();
          valoreOrdinato = testata.getValoreServiziOrd();
          costoOfferto = testata.getCostoServiziOff();
          costoOrdinato = testata.getCostoServiziOrd();
          break;
        case TipoRiga.SPESE_MOV_VALORE:
          valoreOfferto = testata.getValoreSpeseOff();
          valoreOrdinato = testata.getValoreSpeseOrd();
          costoOfferto = testata.getCostoSpeseOff();
          costoOrdinato = testata.getCostoSpeseOrd();
          break;
      }
      valoreOfferto = getNotNullValue(valoreOfferto).subtract(getNotNullValue(getValoreOfferto()));
      valoreOrdinato = getNotNullValue(valoreOrdinato).subtract(getNotNullValue(getValoreOrdinato()));
      costoOfferto = getNotNullValue(costoOfferto).subtract(getNotNullValue(getCostoOfferto()));
      costoOrdinato = getNotNullValue(costoOrdinato).subtract(getNotNullValue(getCostoOrdinato()));

      imposta = imposta.subtract(getNotNullValue(getValoreImposta()));
      impostaOrdinato = impostaOrdinato.subtract(getNotNullValue(getValoreImpostaOrdinato()));

      switch (getTipoRiga())
      {
        case TipoRiga.MERCE:
          testata.setValoreOfferto(valoreOfferto);
          testata.setValoreOrdinato(valoreOrdinato);
          testata.setCostoOfferto(costoOfferto);
          testata.setCostoOrdinato(costoOrdinato);
          break;
        case TipoRiga.OMAGGIO:
          testata.setValoreOmaggiOff(valoreOfferto);
          testata.setValoreOmaggiOrd(valoreOrdinato);
          testata.setCostoOmaggiOff(costoOfferto);
          testata.setCostoOmaggiOrd(costoOrdinato);
          break;
        case TipoRiga.SERVIZIO:
          testata.setValoreServiziOff(valoreOfferto);
          testata.setValoreServiziOrd(valoreOrdinato);
          testata.setCostoServiziOff(costoOfferto);
          testata.setCostoServiziOrd(costoOrdinato);
          break;
        case TipoRiga.SPESE_MOV_VALORE:
          testata.setValoreSpeseOff(valoreOfferto);
          testata.setValoreSpeseOrd(valoreOrdinato);
          testata.setCostoSpeseOff(costoOfferto);
          testata.setCostoSpeseOrd(costoOrdinato);
          break;
      }

      testata.setValoreImposta(imposta);
      testata.setValoreImpostaOrd(impostaOrdinato);

      if (getTipoRiga() != TipoRiga.OMAGGIO)
      {
        testata.setValoreTotOfferto(getNotNullValue(testata.getValoreTotOfferto()).
                                    subtract(getNotNullValue(getValoreImposta())).
                                    subtract(getNotNullValue(getValoreOfferto())));

        testata.setValoreTotOrd(getNotNullValue(testata.getValoreTotOrdinato()).
                                subtract(getNotNullValue(getValoreImpostaOrdinato())).
                                subtract(getNotNullValue(getValoreOrdinato())));
      }
      else
      {
        testata.setValoreTotOfferto(getNotNullValue(testata.getValoreTotOfferto()).
                                    subtract(getNotNullValue(getValoreImposta())));

        testata.setValoreTotOrd(getNotNullValue(testata.getValoreTotOrd()).
                                subtract(getNotNullValue(getValoreImpostaOrdinato())));
      }

            if (getTipoRiga() != TipoRiga.OMAGGIO && getTipoRiga() != TipoRiga.SPESE_MOV_VALORE)
            {
              if (PersDatiVen.getCurrentPersDatiVen().getContabilizzazioneRicavi() == PersDatiVen.AL_LORDO)
              {
                BigDecimal valoreScFF = testata.calcolaScontoFineFatturaSoloSeAlLordo(getValoreOfferto());
                testata.setValoreTotOfferto(getNotNullValue(testata.getValoreTotOfferto()).add(valoreScFF));
                valoreScFF = testata.calcolaScontoFineFatturaSoloSeAlLordo(getValoreOrdinato());
                testata.setValoreTotOrd(getNotNullValue(testata.getValoreTotOrd()).add(valoreScFF));
              }
            }

    }
  }
  */
  public void saldaRiga()
  {
    super.saldaRiga();
    //Fix 31790 Inizio
    //Iterator righeSecondarie = iRigheSecondarie.iterator();
    Iterator righeSecondarie = getRigheSecondarie().iterator();
    //Fix 31790 Fine
    iPropagaCausaleChiusura = true;
    while (righeSecondarie.hasNext())
    {
      OffertaClienteRigaSec rigaSec =
          (OffertaClienteRigaSec)righeSecondarie.next();
      rigaSec.saldaRiga();
      propagaCausaleChiusura(rigaSec);
    }
  }

  private void propagaCausaleChiusura(OffertaClienteRigaSec rigaSec)
  {
    if (getIdCausaleChiusuraOffVen() != null)
    {
      if (rigaSec.getIdCausaleChiusuraOffVen() == null || rigaSec.getIdCausaleChiusuraOffVen().equals(""))
        rigaSec.setIdCausaleChiusuraOffVen(getIdCausaleChiusuraOffVen());
    }
  }

  private void propagaCausaleChiusura()
  {
    Iterator righeSecondarie = iRigheSecondarie.iterator();
    while (righeSecondarie.hasNext())
    {
      OffertaClienteRigaSec rigaSec =
          (OffertaClienteRigaSec)righeSecondarie.next();
     //if (getIdCausaleChiusuraOffVen() != null &&(rigaSec.getIdCausaleChiusuraOffVen() == null || rigaSec.getIdCausaleChiusuraOffVen().equals("")))//Fix 31790
        rigaSec.setIdCausaleChiusuraOffVen(getIdCausaleChiusuraOffVen());
    }//Fix 31790
  }

  public void riapriRiga()
  {
    super.riapriRiga();
    iPropagaCausaleChiusura = true;
    Iterator righeSecondarie = getRigheSecondarie().iterator();
    while (righeSecondarie.hasNext())
    {
      OffertaClienteRigaSec rigaSec = (OffertaClienteRigaSec)righeSecondarie.next();
      rigaSec.setAzioneManuale(getAzioneManuale());
      rigaSec.riapriRiga();
      rigaSec.setIdCausaleChiusuraOffVen(null);
    }
    setIdCausaleChiusuraOffVen(null);
  }

  public void cambiaArticolo(
      Articolo articolo,
      Configurazione config,
      boolean recuperaDatiVenAcq)
  {
    if (getIdCliente() != null)
      datiArticolo.setParIntestatario(getIdCliente());
    datiArticolo.setParIdListino(getIdListinoPrezzi());
    datiArticolo.setParQtaUMRif(getQtaInUMRif().toString());

    if (getIdAgente() != null)
    {
      datiArticolo.setParIdAgente(getIdAgente());
      if (getProvvigione1Agente() != null)
      {
        datiArticolo.setParProvvigione1Agente(getProvvigione1Agente().toString());
      }
    }
    if (getIdSubagente() != null)
    {
      datiArticolo.setParIdSubagente(getIdSubagente());
      if (getProvvigione1Subagente() != null)
      {
        datiArticolo.setParProvvigione1Subagente(getProvvigione1Subagente().
                                                 toString());
      }
    }
    String idModPag = ((DocumentoOrdineTestata)getTestata()).
        getIdModPagamento();
    if (idModPag != null)
    {
      datiArticolo.setParIdModPagamento(idModPag);
    }
    super.cambiaArticolo(articolo, config, recuperaDatiVenAcq);

    setPrcPerditaResiduo(datiArticolo.getPercentualePerditaResiduoNumerico());
    setIdAgente(((DatiArticoloRigaVendita)datiArticolo).getAgentiProvvigioni().
                getIdAgente());
    setProvvigione1Agente(((DatiArticoloRigaVendita)datiArticolo).
                          getProvvigioneAgenteNumerico());
    setIdSubagente(((DatiArticoloRigaVendita)datiArticolo).
                   getAgentiProvvigioni().getIdSubagente());
    setProvvigione1Subagente(((DatiArticoloRigaVendita)datiArticolo).
                             getProvvigioneSubagenteNumerico());

    try
    {

      UnitaMisura umRif = UnitaMisura.getUM(getIdUMRif());

      String idUMPrmMag = datiArticolo.getIdUMPrimaria();
      setIdUMPrm(idUMPrmMag);

      UnitaMisura umPrm = UnitaMisura.getUM(idUMPrmMag);

      setQtaInUMPrmMag(articolo.convertiUM(getQtaInUMRif(), umRif, umPrm, this.getArticoloVersRichiesta())); // fix 10955

      String idUMSecMag = datiArticolo.getIdUMSecondaria();
      if (idUMSecMag != null && idUMSecMag.length() > 0)
      {
        setIdUMSec(idUMSecMag);

        UnitaMisura umSec = UnitaMisura.getUM(idUMSecMag);

        setQtaInUMSecMag(articolo.convertiUM(getQtaInUMRif(), umRif, umSec, this.getArticoloVersRichiesta())); // fix 10955
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    // Fix 29108 ini
    if (recuperaDatiVenAcq) {
      try {
        calcolaDatiVendita((OffertaCliente)getTestata());
      } catch (Exception ex) {	
      }
    }    
    // Fix 29108 fin
    
  }

  public void creaRigaOmaggio(OffertaCliente testata) throws SQLException
  {

    ListinoVenditaScaglione lvs = null;

    if (isServizioCalcDatiVendita())
    {
      Trace.println("COND DATI VENDITA SI");
      if (condVen == null && getIdCliente() != null)
        recuperaCondizioniVendita(testata);
      if (condVen == null)
      {
        return;
      }
      lvs = condVen.getListinoVenditaScaglione();
    }
    else
    {
      Trace.println("COND DATI VENDITA NO");
      lvs = (ListinoVenditaScaglione)Factory.createObject(
          ListinoVenditaScaglione.class);
      lvs.setKey(getServizioListVendScaglione());
      lvs.retrieve();
    }

    if (lvs != null)
    {
      ListinoVenditaOffertaOmaggio offOmg = lvs.getOffertaOmaggio();

      if (offOmg != null &&
          offOmg.getTipoOmaggioOfferta() !=
          ListinoVenditaOffertaOmaggio.INCOMPLETO)
      {
        Trace.println(getKey() + "----------------------------superato primo controllo Righe Omaggio----------------------------");

        BigDecimal quantRiferimento = offOmg.getQuantitaRiferimento();
        BigDecimal quantMin = offOmg.getQuantitaMin();
        BigDecimal quantMax = offOmg.getQuantitaMax();

        BigDecimal quantOrdinata = new BigDecimal("0.00");
		quantOrdinata = Q6Calc.get().setScale(quantOrdinata,2);//Fix 30871
        char rifUMPrz = getRiferimentoUMPrezzo();
        if (rifUMPrz == RiferimentoUmPrezzo.VENDITA)
        {
          quantOrdinata = getQtaInUMRif();
        }
        else if (rifUMPrz == RiferimentoUmPrezzo.MAGAZZINO)
        {
          quantOrdinata = getQtaInUMPrmMag();
        }

        //Calcola la quantità dovuta di articoli omaggio
        //BigDecimal quantTotOmaggioOfferta = new BigDecimal("0.00");//Fix 30871
		  BigDecimal quantTotOmaggioOfferta = Q6Calc.get().setScale(new BigDecimal("0.00"),2);//Fix 30871
        if (quantOrdinata.compareTo(quantRiferimento) >= 0)
        {
          quantTotOmaggioOfferta = (quantOrdinata.divide(quantRiferimento,
              BigDecimal.ROUND_DOWN)).
              multiply(offOmg.getQuantitaOmaggioOfferta());
          quantTotOmaggioOfferta = quantTotOmaggioOfferta.setScale(0,
              BigDecimal.ROUND_DOWN);
        }
        Trace.println("---->>quantRiferimento=" + quantRiferimento);
        Trace.println("---->>quantMin=" + quantMin);
        Trace.println("---->>quantMax=" + quantMax);
        Trace.println("---->>quantOrdinata=" + quantOrdinata);
        Trace.println("---->>quantTotOmaggioOfferta=" + quantTotOmaggioOfferta);

        if (quantTotOmaggioOfferta.compareTo(quantMin) >= 0)
        {
          Trace.println(getKey() + "----------------------------superato secondo controllo Righe Omaggio----------------------------");

          if (quantTotOmaggioOfferta.compareTo(quantMax) > 0)
            quantTotOmaggioOfferta = quantMax;

            //Prepara i valori che si differenziano tra i due tipi di riga
          CausaleRigaOffertaCliente causale = null;
          char tipoRiga = '\0';

          switch (offOmg.getTipoOmaggioOfferta())
          {
            case ListinoVenditaOffertaOmaggio.OMAGGIO:
              causale = getCausaleRigaOmaggio(testata,
                                              ListinoVenditaOffertaOmaggio.
                                              OMAGGIO);
              tipoRiga = TipoRiga.OMAGGIO;
              break;
            case ListinoVenditaOffertaOmaggio.OFFERTA:
              causale = getCausaleRiga();
              tipoRiga = TipoRiga.MERCE;
              break;
            case ListinoVenditaOffertaOmaggio.V_PER_O:
              causale = getCausaleRigaOmaggio(testata,
                                              ListinoVenditaOffertaOmaggio.
                                              V_PER_O);
              if (causale != null)
              {
                BigDecimal newQta = getQtaInUMRif();
                newQta = newQta.subtract(quantTotOmaggioOfferta);
                this.setQtaInUMRif(newQta);
                if (getUMPrm() != null)
                  setQtaInUMPrmMag(getArticolo().convertiUM(newQta, getUMRif(),
                      getUMPrm(), this.getArticoloVersRichiesta())); // fix 10955
                if (getUMSec() != null)
                  setQtaInUMSecMag(getArticolo().convertiUM(getQtaInUMPrmMag(),
                      getUMPrm(), getUMSec(), this.getArticoloVersRichiesta())); // fix 10955

                tipoRiga = TipoRiga.OMAGGIO;
                if (this.getArticolo().isArticLotto())
                {
                  this.setStatoAvanzamento(StatoAvanzamento.PROVVISORIO);
                  this.getRigheLotto().clear();
                }
              }

              break;
            case ListinoVenditaOffertaOmaggio.V_PIU_O:
              causale = getCausaleRigaOmaggio(testata,
                                              ListinoVenditaOffertaOmaggio.
                                              V_PIU_O);
              tipoRiga = TipoRiga.OMAGGIO;
              break;
          }

          if (causale != null)
          {
            Trace.println(getKey() + "----------------------------superato terzo controllo Righe Omaggio----------------------------");

            //Articolo della riga omaggio/offerta
            Articolo articolo = offOmg.getArticolo();

            //Crea le righe
            rigaOmf = (OffertaClienteRigaPrm)Factory.createObject(
                OffertaClienteRigaPrm.class);

            rigaOmf.setServizioCalcDatiVendita(false);
            rigaOmf.setRigaOfferta(offOmg.getTipoOmaggioOfferta() ==
                                   ListinoVenditaOffertaOmaggio.OFFERTA);

            //Chiave
            rigaOmf.setTestata(testata);

            rigaOmf.setNonFatturare(causale.getNonFatturare());
            //Campi not nullable
            rigaOmf.setRigaCollegata(this);
            rigaOmf.setIdDettaglioRigaCollegata(getDettaglioRigaDocumento());
            rigaOmf.setTipoRiga(tipoRiga);
            rigaOmf.setStatoAvanzamento(getStatoAvanzamento());
            if (offOmg.getArticolo().isArticLotto())
            {
              //rigaOmf.setStatoAvanzamento(StatoAvanzamento.PROVVISORIO);
              rigaOmf.setStatoAvanzamento(getStatoAvanzamento());
            }
            rigaOmf.setPrcPerditaResiduo(this.getPrcPerditaResiduo());
            rigaOmf.setRigaNonFrazionabile(this.isRigaNonFrazionabile());

            //Dati comuni
            rigaOmf.getDatiComuniEstesi().setStato(getDatiComuniEstesi().
                getStato());
            //Scheda Generale
            rigaOmf.setSequenzaRiga(getSequenzaRiga() + 1);
            rigaOmf.setCausaleRiga(causale);
            rigaOmf.setMagazzino(getMagazzino());
            rigaOmf.setArticolo(articolo);
            rigaOmf.setDescrizioneArticolo(articolo.getDescrizioneArticoloNLS().
                                           getDescrizione());
            rigaOmf.setConfigurazione(offOmg.getConfigurazione());

            ArticoloVersione ver = articolo.getVersioneAtDate(
                getDataConsegnaRichiesta());
            if (ver.getIdVersioneSaldi() != null)
              rigaOmf.setArticoloVersSaldi(ver.getVersioneSaldi());
            else
              rigaOmf.setArticoloVersSaldi(ver);
            rigaOmf.setArticoloVersRichiesta(ver);

            // Faccio questo perchè l'unità di misura che proviene dal listino potrebbe
            // non essere di vendita.
            UnitaMisura unitaMisuraVendita = offOmg.getUnitaMisura();
            boolean passato = false;
            List l = articolo.getArticoloDatiVendita().getForcedUMSecondarie();
            Iterator iter = l.iterator();
            while (iter.hasNext())
            {
              UnitaMisura uni = (UnitaMisura)iter.next();

              if (unitaMisuraVendita != null && uni != null &&
                  uni.
                  getIdUnitaMisura().equals(unitaMisuraVendita.getIdUnitaMisura()))
              {

                passato = true;
                break;
              }
            }
            if (unitaMisuraVendita == null || !passato)
            {
              unitaMisuraVendita = articolo.getUMDefaultVendita();
              rigaOmf.setRiferimentoUMPrezzo(RiferimentoUmPrezzo.MAGAZZINO);
              quantTotOmaggioOfferta = articolo.convertiUM(
                  quantTotOmaggioOfferta, offOmg.getUnitaMisura(),
                  unitaMisuraVendita, rigaOmf.getArticoloVersRichiesta()); // fix 10955
            }

            rigaOmf.setUMRif(unitaMisuraVendita);
            rigaOmf.setQtaInUMRif(quantTotOmaggioOfferta);

            //
            UnitaMisura unitaMisuraPrm = articolo.getUMPrmMag();
            if (unitaMisuraPrm != null)
            {
              rigaOmf.setUMPrm(unitaMisuraPrm);
              if (unitaMisuraPrm.equals(unitaMisuraVendita))
                rigaOmf.setQtaInUMPrmMag(quantTotOmaggioOfferta);
              else
                rigaOmf.setQtaInUMPrmMag(articolo.convertiUM(
                    quantTotOmaggioOfferta, unitaMisuraVendita, unitaMisuraPrm, rigaOmf.getArticoloVersRichiesta())); // fix 10955
            }

            UnitaMisura unitaMisuraSec = articolo.getUMSecMag();
            if (unitaMisuraSec != null)
            {
              rigaOmf.setUMSec(unitaMisuraSec);
              if (unitaMisuraSec.equals(unitaMisuraVendita))
                rigaOmf.setQtaInUMSecMag(quantTotOmaggioOfferta);
              else
                rigaOmf.setQtaInUMSecMag(articolo.convertiUM(
                    quantTotOmaggioOfferta, unitaMisuraVendita, unitaMisuraSec, rigaOmf.getArticoloVersRichiesta())); // fix 10955
            }

            rigaOmf.setDataConsegnaRichiesta(getDataConsegnaRichiesta());
            rigaOmf.setSettConsegnaRichiesta(getSettConsegnaRichiesta());
            rigaOmf.setDataConsegnaConfermata(getDataConsegnaConfermata());
            rigaOmf.setSettConsegnaConfermata(getSettConsegnaConfermata());
            rigaOmf.setDataConsegnaProduzione(getDataConsegnaProduzione());
            rigaOmf.setSettConsegnaProduzione(getSettConsegnaProduzione());

            //Scheda Prezzi/Sconti
            rigaOmf.setIdListinoPrezzi(offOmg.getIdListino());
            rigaOmf.setPrezzo(offOmg.getPrezzo());
            if (offOmg.getTipoOmaggioOfferta() != TipoRiga.OMAGGIO)
            {
              rigaOmf.setPrezzoExtra(offOmg.getPrezzoExtra());
              rigaOmf.setScontoArticolo1(lvs.getScontoArticolo1());
              rigaOmf.setScontoArticolo2(lvs.getScontoArticolo2());
              rigaOmf.setMaggiorazione(lvs.getMaggiorazione());
              rigaOmf.setSconto(lvs.getSconto());
            }

            AssoggettamentoIVA assIva = offOmg.getAssoggettamentoIVA();
            if (assIva == null)
              assIva = articolo.getAssoggettamentoIVA();
            if (assIva == null)
              assIva = testata.getAssoggettamentoIVA();
            rigaOmf.setAssoggettamentoIVA(assIva);

            //Scheda Agenti
            rigaOmf.setProvvigione1Agente(lvs.getProvvigioneAgente());
            rigaOmf.setProvvigione1Subagente(lvs.getProvvigioneSubagente());
			//Fix Inizio 44522
          	rigaOmf.setAgente(getAgente());
            rigaOmf.setSubagente(getSubagente());
            rigaOmf.setResponsabileVendite(getResponsabileVendite());
            //Fix Fine 44522 
            rigaOmf.setIdCommessa(getIdCommessa());
            rigaOmf.setIdDocumentoMM(getIdDocumentoMM());
            rigaOmf.setDescrizioneExtArticolo(recuperaDescExtArticolo(rigaOmf.getIdCliente(), rigaOmf.getIdArticolo(), rigaOmf.getIdConfigurazione()));//Fix14727 RA
          }
        }
      }
    }
  }

  public BigDecimal getPrezzoRiferimento()
  {
    OrdineRigaPrezziExtraVendita prezziExtra = (OrdineRigaPrezziExtraVendita)this.getRigaPrezziExtra();
    if (prezziExtra != null)
    {
      return prezziExtra.getPrezzoRiferimento();
    }
    return null;
  }

  public void setPrezzoRiferimento(BigDecimal b)
  {
    OrdineRigaPrezziExtraVendita prezziExtra = (OrdineRigaPrezziExtraVendita)this.getRigaPrezziExtra();
    if (prezziExtra != null)
    {
      prezziExtra.setPrezzoRiferimento(b);
    }
  }

  public ErrorMessage checkStatoAnnullato()
  {
    ErrorMessage em = null;
    if (isOnDB() && getOldRiga() != null && this.getDatiComuniEstesi().getStato() == DatiComuniEstesi.ANNULLATO)
    {
      BigDecimal qtaZero = new BigDecimal(0.0);
      //Fix 10719 PM Inizio
      /*if (getQuantitaOrdinataVen().getQuantitaInUMPrm().compareTo(qtaZero) != 0 ||
          getQuantitaOrdinataVen().getQuantitaInUMSec().compareTo(qtaZero) != 0 ||
          isSaldoManuale() ||
          (this.getStatoEvasione() != StatoEvasione.INEVASO))*/
      if (getQuantitaOrdinata().getQuantitaInUMPrm().compareTo(qtaZero) != 0 ||
          getQuantitaOrdinata().getQuantitaInUMSec().compareTo(qtaZero) != 0 ||
          isSaldoManuale() ||(this.getStatoEvasione() != StatoEvasione.INEVASO)) //Fix 10719 PM Fine
        em = new ErrorMessage("THIP200446", KeyHelper.formatKeyString(this.getKey()));
    }
    return em;
  }
//Fix 44166 inizio
  public ErrorMessage checkStatoSospeso()
  {
    ErrorMessage em = null;
    if (isOnDB() && getOldRiga() != null && this.getDatiComuniEstesi().getStato() == DatiComuniEstesi.SOSPESO)
    {
      BigDecimal qtaZero = new BigDecimal(0.0);
      //Fix 10719 PM Inizio
      /*if (getQuantitaOrdinataVen().getQuantitaInUMPrm().compareTo(qtaZero) != 0 ||
          getQuantitaOrdinataVen().getQuantitaInUMSec().compareTo(qtaZero) != 0 ||
          isSaldoManuale() ||
          (this.getStatoEvasione() != StatoEvasione.INEVASO))*/
      if (getQuantitaOrdinata().getQuantitaInUMPrm().compareTo(qtaZero) != 0 ||
          getQuantitaOrdinata().getQuantitaInUMSec().compareTo(qtaZero) != 0 /* 46088 ini ||
          isSaldoManuale() ||(this.getStatoEvasione() != StatoEvasione.INEVASO) 46088 fine  */) //Fix 10719 PM Fine
        em = new ErrorMessage("THIP200446", KeyHelper.formatKeyString(this.getKey()));
    }
    return em;
  }
//Fix 44166 fine

//Fix 10719 PM Inizio

  public Integer getDettaglioRigaOrdineClg()
  {
    return new Integer(0);
  }

  public void setDettaglioRigaOrdineClg(Integer dettRigaOrdine)
  {

  }

//Fix 10719 PM Fine


//Fix 10761 PM Inizio

  /**
   * Ridefinizione metodo checkDelete()
   * Se esiste almeno una riga ordine collegata , la riga offerta non può
   * essere eliminata
   */
  public ErrorMessage checkDelete()
  {
	  ErrorMessage err = super.checkDelete();
  	  if (err == null)
  		  err = checkEsistenzaRigaOrdineCollegata();
  	  return err;
  }


  public ErrorMessage checkEsistenzaRigaOrdineCollegata()
  {
	  ErrorMessage err = null;
	  if (existRigaOrdineCollegata())
		  //err = new ErrorMessage("THIP300195");//FIX 12969
      err = new ErrorMessage("THIP30T160");//FIX 12969
	  return err;
  }

  /**
   * Controlla se esiste almeno una riga ordine collegata.
  */
  public boolean existRigaOrdineCollegata()
  {
  	boolean exist = false;
  	Database db = ConnectionManager.getCurrentDatabase();
  	try
  	{
  		PreparedStatement ps = csRicOrdVenRig.getStatement();
  		synchronized (ps)
  		{
  			db.setString(ps,1,getIdAzienda());
  			db.setString(ps,2,getAnnoDocumento());
  			db.setString(ps,3,getNumeroDocumento());
  			ps.setInt(4,getNumeroRigaDocumento().intValue());
  			ps.setInt(5,getDettaglioRigaDocumento().intValue());
  		}
    	ResultSet rs = ps.executeQuery();
    	if (rs.next())
    		exist = (rs.getInt(1)>0);
    	rs.close();
  	}
  	catch(SQLException ex)
  	{
  		ex.printStackTrace(Trace.excStream);
  	}
  	return exist;
  }
//Fix 10761 PM Fine
  //Fix 11176 begin
  protected DocumentoOrdineRiga getRigaDestinazionePerCopia()
  {
    return (OffertaClienteRigaPrm)Factory.createObject(OffertaClienteRigaPrm.class);
  }

//public DocumentoOrdineRiga copiaRiga(DocumentoOrdineTestata docDest, SpecificheCopiaOffertaFornitore spec) throws CopyException { // Fix 13423
  public DocumentoOrdineRiga copiaRiga(DocumentoOrdineTestata docDest, SpecificheCopiaDocumento spec) throws CopyException {
   OffertaClienteRigaPrm riga = (OffertaClienteRigaPrm)(super.copiaRiga(docDest, spec));
   if (riga != null) {
     //Copia righe secondarie
     riga.setGeneraRigheSecondarie(false);
     List righe = getRigheSecondarie();
     for (Iterator iter = righe.iterator(); iter.hasNext(); ) {
       OffertaClienteRigaSec rigaSec = (OffertaClienteRigaSec) iter.next();
       spec.setRigaPrimariaDest(riga);
       DocumentoOrdineRiga rigaCopiata = rigaSec.copiaRiga(docDest, spec);
       if (rigaCopiata != null) {
         riga.getRigheSecondarie().add(rigaCopiata);
       }
     }
     if (riga.getTipoRiga() == TipoRiga.SPESE_MOV_VALORE) // Fix 23743
       riga.setPrezzoConcordato(null); // Fix 23743
   }
   return riga;
 }


 public void impostazioniPerCopiaRiga()
 {
	 super.impostazioniPerCopiaRiga();
	 Iterator i = getRigheSecondarie().iterator();
	 while (i.hasNext())
	 {
		  //OrdineVenditaRiga rigaSec = (OrdineVenditaRiga)i.next();
	     OffertaClienteRiga rigaSec = (OffertaClienteRiga)i.next();
	     rigaSec.impostazioniPerCopiaRiga();
	     if(getTestata() != null && !((DocumentoOrdineTestata)getTestata()).isInCopia())//Fix 41868
	     	calcolaDateRigheSecondarie(rigaSec);//Fix 41868
	     if (!Utils.areEqual(getIdOldMagazzino(), getIdMagazzino()))
	       rigaSec.setIdMagazzino(getIdMagazzino());
	 }
 }

 //Fix Inizio 41868
	protected void calcolaDateRigheSecondarie(OffertaClienteRiga rigaSec) {

		if (datiUguali(getOldDataConsegnaRichiesta(), getDataConsegnaRichiesta())
				&& datiUguali(getOldDataConsegnaConfermata(), getDataConsegnaConfermata())
				&& datiUguali(getOldDataConsegnaProduzione(), getDataConsegnaProduzione()))
			return;

		rigaSec.setDataConsegnaRichiesta(getDataConsegnaRichiesta());
		rigaSec.setDataConsegnaConfermata(getDataConsegnaConfermata());
		rigaSec.setDataConsegnaProduzione(getDataConsegnaProduzione());
		rigaSec.setSettConsegnaRichiesta(getSettConsegnaRichiesta());
		rigaSec.setSettConsegnaConfermata(getSettConsegnaConfermata());
		rigaSec.setSettConsegnaProduzione(getSettConsegnaProduzione());

	}
//Fix Fine 41868
 
 
  protected void copiaRigaCompletaBO(DocumentoOrdineRiga riga)
 {
	  riga.completaBO();
	  copiaRigaImpostaProvvigioniAgenti(riga);
 }
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
 	  
  }
  //Fix 37244 fine

 protected void copiaRigaImpostaProvvigioniAgenti(DocumentoOrdineRiga riga)
 {
	 try
	 {
		 OffertaClienteRigaPrm rigaPrm = (OffertaClienteRigaPrm)riga;
		 OffertaCliente testata = (OffertaCliente)rigaPrm.getTestata();
		 if (testata.getTipoIntestatarioOfferta() == OffertaCliente.TIPO_INTESTATARIO_CLIENTE)
		 {
			 DocumentoOrdineRigaVenRecuperaDati recDati = new DocumentoOrdineRigaVenRecuperaDati();
			 recDati.setClassName("OffertaClienteRigaPrm");
			 recDati.setArticolo(rigaPrm.getArticolo());
			 recDati.setIdAgente(rigaPrm.getIdAgente());
			 if (rigaPrm.getProvvigione1Agente() != null)
				 recDati.setProvv1Agente(rigaPrm.getProvvigione1Agente().toString());
			 recDati.setIdSubagente(rigaPrm.getIdSubagente());
			 if (rigaPrm.getProvvigione1Subagente() != null)
				 recDati.setProvv1Subagente(rigaPrm.getProvvigione1Subagente().toString());
			 recDati.setIntestatario(rigaPrm.getIdCliente());
			 recDati.setDivisione(testata.getIdDivisione());
			 recDati.impostaProvvigioniAgente();
			 rigaPrm.setProvvigione1Agente(recDati.getAgentiProvvigioni().getProvvigioniAgente());
			 rigaPrm.setProvvigione1Subagente(recDati.getAgentiProvvigioni().getProvvigioniSubagente());
		 }
	 }
	 catch(Exception e)
	 {
		 e.printStackTrace(Trace.excStream);
	 }
 }

  //Fix 11176 end

// Fix 11685 begin
  public EspNodoArticolo esplosioneModelloDocumento(Articolo articolo)throws SQLException{
    EspNodoArticolo esplosione = null;
    boolean okModello = false;
    try {
      esplosione = getEsplosioneNodoModello(false, articolo, ModelloProduttivo.KIT);
      okModello = true;
    }
    catch (ThipException ex) {
      okModello = false;
      esplosione = null;
    }

    if (!okModello) {
      try {
        esplosione = getEsplosioneNodoModello(false, articolo, ModelloProduttivo.PRODUZIONE);
        okModello = true;
      }
      catch (ThipException ex) {
        okModello = false;
        esplosione = null;
      }
    }
    return esplosione;
  }

   public boolean effettuareIlControllo(List lista){
     boolean ritorno = false;
     OggCalcoloGiaDisp ogg = null;
     if (getTipoRiga() != TipoRiga.SPESE_MOV_VALORE && !isRigaMerceValore()){
       Articolo articolo = this.getArticolo();
       boolean kitNonGestitoAMag = (articolo.getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST);
       if (!kitNonGestitoAMag && this.isDaAggiornare()){
         ogg = (OggCalcoloGiaDisp) Factory.createObject(OggCalcoloGiaDisp.class);
         ogg.caricati(this, null, null);
         ogg.setTipoControllo(ogg.TP_CTL_DISPONIBILITA);
         ritorno = true;
       }
     }
     if (ritorno){
       lista.add(ogg);
     }

     boolean rit = verificaRigheSecondarie(lista);
     if (rit)
       ritorno = rit;
     return ritorno;
   }

   public boolean verificaRigheSecondarie(List lista){
     boolean ritorno = false;
     Articolo articolo = this.getArticolo();
     if (getRigheSecondarie()!=null && !getRigheSecondarie().isEmpty() &&
         (this.isDaAggiornare() ||
              isGeneraRigheSecondarie())){
       Iterator iter = this.getRigheSecondarie().iterator();
       while (iter.hasNext()) {
         OffertaClienteRigaSec rigaSec = (OffertaClienteRigaSec) iter.next();
         QuantitaInUMRif qtaRigaSec = rigaSec.ricalcoloSoloQuantita(this);
         boolean rit = rigaSec.effettuareIlControllo(lista,
             new QuantitaInUM(qtaRigaSec.getQuantitaInUMPrm(), qtaRigaSec.getQuantitaInUMSec()));
         if (rit){
           ritorno = rit;
         }
       }
     }
     else if(!isOnDB() && articolo != null &&
        getTipoRiga() != TipoRiga.SPESE_MOV_VALORE &&
        articolo.getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST &&
        getRigheSecondarie().isEmpty())
     {
       try {
         EspNodoArticolo esplosioneModello = esplosioneModelloDocumento(articolo);
         if (esplosioneModello != null) {
           ritorno = verificoGiaRigheSecondarieModello(esplosioneModello, lista);
         }
         else {
           EsplosioneNodo nodo = getEsplosioneNodo(getArticolo());
           ritorno = verificoGiaRigheSecondarieDistinta(nodo, lista);
         }
       }
       catch(SQLException ex){
         ex.printStackTrace(Trace.excStream);
       }
     }

     return ritorno;
   }

   public boolean verificoGiaRigheSecondarieModello(EspNodoArticolo nodoModello, List lista){
    boolean ritorno =false;
    List datiRigheKit = nodoModello.getNodiMateriali();
    datiRigheKit.addAll(nodoModello.getNodiProdottiNonPrimari());
    if (datiRigheKit.isEmpty())
      return ritorno;
    Iterator iter = datiRigheKit.iterator();
    while (iter.hasNext() ) {
      //EspNodoArticolo datiRigaKit = (EspNodoArticolo)iter.next();//Fix 19757
      EspNodoArticoloBase datiRigaKit = (EspNodoArticoloBase)iter.next();//Fix 19757
      OggCalcoloGiaDisp ogg = (OggCalcoloGiaDisp)Factory.createObject(OggCalcoloGiaDisp.class);
      ogg.caricati(datiRigaKit,this, null);
      ogg.setTipoControllo(ogg.TP_CTL_DISPONIBILITA);
      lista.add(ogg);
      ritorno = true;
    }
    return ritorno;
  }

  public boolean verificoGiaRigheSecondarieDistinta(EsplosioneNodo nodo, List lista){
    boolean ritorno =false;
    List datiRigheKit = nodo.getNodiFigli();
    Iterator iter = datiRigheKit.iterator();
    while (iter.hasNext()) {
      EsplosioneNodo datiRigaKit = (EsplosioneNodo)iter.next();
      OggCalcoloGiaDisp ogg = (OggCalcoloGiaDisp) Factory.createObject(
          OggCalcoloGiaDisp.class);
      ogg.caricati(datiRigaKit, this, null);
      ogg.setTipoControllo(ogg.TP_CTL_DISPONIBILITA);
      lista.add(ogg);
      ritorno = true;
    }
    return ritorno;
  }



//Fix 11685 end



  //Fix 11976 PM >
  public void impostaStatoEvasioneTestata(boolean cancella)
  {
	OffertaCliente testata = (OffertaCliente)getTestata();
	char oldStatoEvasione = testata.getStatoEvasione();
    super.impostaStatoEvasioneTestata(cancella);
    if (testata.getStatoEvasione() == OffertaCliente.SALDATO)
    {
    	if (testata.getCausaleChiusuraOffVen() == null)
    	{
    		CausaliChiusuraOffertaCli cau = getCausaleChiusuraOffVen();
    		if (cau != null)
    		{
    		   if (cau.getTipoChiusuraOfferta() == CausaliChiusuraOffertaCli.ACCETTATA)
    			   testata.setCausaleChiusuraOffVen(cau);
    		   else
    		   {
    			   cau = testata.getCausaleChiusuraOffAccettata(testata);
    			   if (cau != null)
    				   testata.setCausaleChiusuraOffVen(cau);
    			   else
    				   testata.setCausaleChiusuraOffVen(getCausaleChiusuraOffVen());
    		   }
    		}
    	}
    }
	  else
	  {
		  if (oldStatoEvasione == OffertaFornitore.SALDATO)
			  testata.setCausaleChiusuraOffVen(null);
	  }

  }
  //Fix 11976 PM <
  // Fix 13494 inzio
    protected void calcolaImportiRiga(){
      calcolaPrezzoDaRigheSecondarieConReset(false);
      super.calcolaImportiRiga();
    }
   // Fix 13494 fine

//Fix 16840 inizio
public boolean isUtenteAutorizzatoForzaturaPrelLotto(){
  //...Controlla l'autorizzazione sulla forzatura
//Fix 18156 inizio
 try {
   return Security.validateTask("SaldoMagLotto", "FORZA_USO_LOTTO") == null;
 }
 catch (SQLException ex) {
   ex.printStackTrace(Trace.excStream);
   return false;
 }
/*
  boolean isAuth = false;
  AuthorityLoader aLoader = (AuthorityLoader)Factory.createObject(AuthorityLoader.class);
  String tk = "SaldoMagLotto" + PersistentObject.KEY_SEPARATOR + "FORZA_USO_LOTTO";
  Task t = null;
  try{
    t = Task.elementWithKey(tk, PersistentObject.NO_LOCK);
  }
  catch (SQLException ex) {
    ex.printStackTrace(Trace.excStream);
    return false;
  }
  // Non esiste il task;
  if (t == null)
    return false;
  aLoader.reset();
  aLoader.setUser(Security.getCurrentUser());
  isAuth = aLoader.isAuthorized(t);
  if (!isAuth) {
    List gruppi = Security.getCurrentUser().getGroups();
    //...Controllo tutti i gruppi dell'utente e vedo se fa parte di uno di quelli autorizzati
    for(int i = 0; i < gruppi.size(); i++) {
      Group gr = (Group)gruppi.get(i);
      aLoader.reset();
      aLoader.setGroup(gr);
      isAuth = aLoader.isAuthorized(t);
      if(isAuth)
        break;
    }
  }
  return isAuth;
Fix 18156 fine */
}

public BigDecimal controllaDispUnicoLottoEffettivo(DocumentoOrdineRigaLotto rigaLotto) {
  BigDecimal giacCalcLotto = ZERO_DEC;
  try {
    String idAzienda = getIdAzienda();
    String idArticolo = getIdArticolo();
    Integer idVersione = getIdVersioneSal();
    String idEsternoConfig = getIdEsternoConfig();
    String idLotto = rigaLotto.getIdLotto();
    String idMagazzino = getIdMagazzino();
    char tipo = PersDatiMagazzino.TIPO_VEN;
    List lottiOfferta = new ArrayList();

    String whereLtSald = LottiSaldiTM.ID_AZIENDA + " = '" + idAzienda + "' AND " +
                         LottiSaldiTM.ID_MAGAZZINO + " = '" + idMagazzino + "' AND " +
                         LottiSaldiTM.ID_ARTICOLO + " = '" + idArticolo + "' AND " +
                         LottiSaldiTM.ID_VERSIONE + " = " + idVersione + " AND " +
                         LottiSaldiTM.ID_OPERAZIONE + " = '" + SaldoMag.OPERAZIONE_DUMMY + "' AND " +
                         LottiSaldiTM.ID_LOTTO + " = '" + idLotto + "' AND " +
                         LottiSaldiTM.COD_CONFIG + " = '" + ProposizioneAutLotto.calcolaCodConfig(idEsternoConfig) + "'";

    Vector lottiSaldo = LottiSaldi.retrieveList(whereLtSald, "", false);
    LottiSaldi lottoSaldo = null;

    if (!lottiSaldo.isEmpty()) {
      lottoSaldo = (LottiSaldi) lottiSaldo.elementAt(0);
      giacCalcLotto = ProposizioneAutLotto.getInstance().calcolaQtaDisponibileLottoOrdine(tipo, lottoSaldo, !lottiOfferta.isEmpty(), lottiOfferta);

      String keyRigaOrdLotto = KeyHelper.buildObjectKey(new String[] {getIdAzienda(),
        getAnnoDocumento(), getNumeroDocumento(), getNumeroRigaDocumento().toString(), getIdArticolo(), rigaLotto.getIdLotto()});
      BigDecimal qtaRigaLottoPrmOld = getOffertaCliRigaLottoPrm(keyRigaOrdLotto);
      if (qtaRigaLottoPrmOld != null)
        giacCalcLotto = giacCalcLotto.add(qtaRigaLottoPrmOld);
    }
  }
  catch (Exception ex) {
    ex.printStackTrace(Trace.excStream);
  }
  return giacCalcLotto;
}

public BigDecimal getOffertaCliRigaLottoPrm(String key) {
  BigDecimal qtaRigaLottoPrm = null;
  try {
    OffertaClienteRigaLottoPrm rigaOffLotto = (OffertaClienteRigaLottoPrm) Factory.createObject(OffertaClienteRigaLottoPrm.class);
    rigaOffLotto.setKey(key);
    if (rigaOffLotto.retrieve()) {
      qtaRigaLottoPrm = rigaOffLotto.getQuantitaOrdinata().getQuantitaInUMPrm();
    }
  }
  catch (Exception ex) {
    ex.printStackTrace(Trace.excStream);
  }
  return qtaRigaLottoPrm;
}
// Fix 16840 fine

  //...FIX 16893 inizio
  /**
   * Gestione della generazione delle righe secondarie
   */
  public void runGenerazioneRigheSec() throws SQLException {
    boolean newRow = righeSecDaGenerare(); //...FIX 16893
    if (newRow && getTipoRiga() != TipoRiga.SPESE_MOV_VALORE &&
       (getArticolo().getTipoParte() == ArticoloDatiIdent.KIT_GEST ||
       getArticolo().getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST)) {

      if (isGeneraRigheSecondarie() && !isDisabilitaRigheSecondarieForCM()) {
        gestioneKit();
        calcolaPrezzoDaRigheSecondarie();
      }
    }
    //else {//Fix 33762
     // if (isOnDB()) //Fix 33762
        gestioneDateRigheSecondarie();
   // }
  }

  public boolean righeSecDaGenerare() {
    return!isOnDB();
  }
  //...FIX 16893 fine


  //Fix 25682 - inizio
  protected Magazzino getMagazzinoRigaSecModello(EspNodoArticoloBase datiRigaSec) throws SQLException {
	   return getMagazzino();
  }
  //Fix 25682 - fine


// Fix 18900 inizio
	//Fix 18757 fine
	//Methodi per il webAjaxEditGrid
	public List getListCausaliRiga(){
		List causaliRiga = new ArrayList();
		// Fix 19167 inizio
		if(isOnDB() && getCausaleRiga() != null) {
			causaliRiga.add(getCausaleRiga());
			return causaliRiga;
		}
		// Fix 19167 fine
		OffertaCliente testata = (OffertaCliente)getTestata();
		if(getTestata()!=null)
			causaliRiga = testata.getCausale().getCausaliRiga();
		Iterator causaliRigaIt =causaliRiga.iterator();
		while (causaliRigaIt.hasNext()){
		 CausaleRigaOffertaCliente crov = (CausaleRigaOffertaCliente)causaliRigaIt.next();
		 //...Se la causale è di scorporoIva non mostro le causali di spesa a percentuale
			if(testata.getCausale().isScorporoIVAAbilitato() && OffertaClienteRigaPrmNuovoFormModifier.isCausaleRigaSpesaPercentuale(crov.getSpesa())) {
				causaliRigaIt.remove();
			}
		}
		return causaliRiga;
	}

	public List getListUnitaMisura(){
		List unitaMisura = new ArrayList();
		Articolo articolo = getArticolo();
		if (articolo != null)
		 unitaMisura =articolo.getArticoloDatiVendita().getForcedUMSecondarie();
		return unitaMisura;
	}
// Fix 18900 fine

  //Fix 18703 inizio
  protected void calcolaImportiPercRiga() {
    setValoreOfferto(new BigDecimal(0));
    setValoreOrdinato(new BigDecimal(0));
    setCostoOfferto(new BigDecimal(0));
    setCostoOrdinato(new BigDecimal(0));
    setValoreImposta(new BigDecimal(0));
    setValoreImpostaOrdinato(new BigDecimal(0));
    setValoreTotaleRiga(new BigDecimal(0));
    setValoreOrdinatoTotRiga(new BigDecimal(0));
    setPrezzoNetto(new BigDecimal(0));
  }
  //Fix 18703 fine
  //Fix 20387 inizio
  public ErrorMessage checkIdEsternoConfigInCopia() {
    //if (!isOnDB() && !getRigheSecondarie().isEmpty()) { // Fix 23709
    if (!getRigheSecondarie().isEmpty()) { // Fix 23709
      if (!equalsObject(getIdEsternoConfig(), iOldIdEsternoConfig))
        return new ErrorMessage("THIP40T339");
    }
    return null;
  }
  //Fix 20387 fine
  //Fix 22839 inizio
  protected Entity getEntityRiga() {
    try {
      return Entity.elementWithKey("OffCliRigaPrm", Entity.NO_LOCK);
    }
    catch (Exception ex) {
      return null;
    }
  }
  //Fix 22839 fine

  //Fix 23345 inizio
  protected ErrorMessage controlloRicalcoloCondizioniVen() {
    ErrorMessage err = null;
    if (isOnDB()) {
      String psnControlloRicalCondizVen = ParametroPsn.getValoreParametroPsn("std.vendite", "controlloRicalcoloCondizioni");
      if (isControlloRicalCondiz() && psnControlloRicalCondizVen.equals(CONTROLLO_RICAL_COND_AL_SALVATAGGIO) && isCondizVenCambiata()) {
        err = new ErrorMessage("THIP40T401");
      }
    }
    return err;
  }

  public boolean isCondizVenCambiata() {
    OrdineRiga oldRiga = getOldRiga();
    OffertaCliente testata = (OffertaCliente)this.getTestata();
    if (isProvenienzaPrezzoDaListini() && getTipoRiga() != TipoRiga.SPESE_MOV_VALORE) {
      if (oldRiga != null &&
          ((!datiUguali(oldRiga.getDataConsegnaConfermata(), getDataConsegnaConfermata())
            && testata.getRifDataPerPrezzoSconti() == ClienteVendita.DATA_CONSEGNA)
           || oldRiga.getQtaInUMRif().compareTo(this.getQtaInUMRif()) != 0))
        return true;
    }
    return false;
  }

  protected boolean isProvenienzaPrezzoDaListini() {
    if (getProvenienzaPrezzo() == PROV_PREZZO_LISTINO_GENERICO ||
        getProvenienzaPrezzo() == PROV_PREZZO_LISTINO_CLIENTE ||
        getProvenienzaPrezzo() == PROV_PREZZO_LISTINO_ZONA ||
        getProvenienzaPrezzo() == PROV_PREZZO_LISTINO_CATEG_VEN)
      return true;
    return false;
  }

  protected boolean datiUguali(java.sql.Date dSrc, java.sql.Date dTrg) {
    boolean r = false;
    if (dSrc == null) {
      if (dTrg != null) {
        r = false;
      }
      else {
        r = true;
      }
    }
    else {
      dSrc = TimeUtils.getDate(dSrc);
      if (dTrg == null) {
        r = false;
      }
      else {
        dTrg = TimeUtils.getDate(dTrg);
        if (dSrc.compareTo(dTrg) == 0) {
          r = true;
        }
        else {
          r = false;
        }
      }
    }
    return r;
  }
  //Fix 23345 fine
  
  
  //Fix 24299 - inizio
  protected BigDecimal getMaggiorazioneCalcoloScontoScalaSconti() {
	  return getMaggiorazione();
  }
  //Fix 24299 - fine


  //Fix 26145 - inizio
  protected BigDecimal getScontoArticolo2CalcoloScontoScalaSconti() {
	  return getScontoArticolo2();
  }
  //Fix 26145 - fine

  
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

  public ListinoVendita getListino() {
    return getListinoPrezzi();
  }

  public Object getOggettoTestata() {
    return getTestata();
  }
//Fix 24613 fine
 //Fix 25004 inizio
 public AssoggettamentoIVA getAssoggettamentoIVAArticolo(Articolo articolo, Integer idConfigurazione){  
   ArticoloCliente artCli = null;
   try {
     artCli = ArticoloCliente.getArticoloClienteRicorsivo(Azienda.getAziendaCorrente(), getIdIntestatario(), articolo.getIdArticolo(), idConfigurazione);
     if(artCli != null && artCli.getIdAssoggettamentoIVA() != null)
	return artCli.getAssoggettamentoIVA();
   }
   catch (SQLException e) {  
     e.printStackTrace();
   }
   return articolo.getAssoggettamentoIVA();
 }
 // Fix 25004 fine
//27649 inizio
public ErrorMessage checkQtaInUMPrmMag() {
	//if((getIdUMPrm() != null && getUMPrm().getQtaIntera()) || (getArticolo() != null && getArticolo().getArticoloDatiMagaz().isQtaIntera())) {//Fix 39363		
	if((getUMPrm() != null && getUMPrm().getQtaIntera()) || (getArticolo() != null && getArticolo().getArticoloDatiMagaz().isQtaIntera())) {//Fix 39363
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
//	if(getIdUMRif() != null && getUMRif().getQtaIntera()) {//Fix 39363
	if(getUMRif() != null && getUMRif().getQtaIntera()) {//Fix 39363
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
	//if(getIdUMSec() != null && getUMSec().getQtaIntera()) {//Fix 39363
	if(getUMSec() != null && getUMSec().getQtaIntera()) {//Fix 39363
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

//Fix 43795 inizio
public void serveRicalProvv(SpecificheModificheRigheOffCliente specOff)
{
 	if (specOff.isRicalcolareProvvigioni())   
 	{
 		setServeRicalProvvAg(true); 
 		setServeRicalProvvSubag(true);
 	}
}
//Fix 43795 fine
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
