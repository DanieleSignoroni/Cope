package it.thera.thip.vendite.documentoVE;

import java.math.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;

import com.thera.thermfw.base.*;
import com.thera.thermfw.common.*;
import com.thera.thermfw.persist.*;

import it.cope.thip.vendite.documentoVE.YDocumentoVenRigaPrm;
import it.thera.thip.base.agentiProvv.Agente;
import it.thera.thip.base.articolo.*;
import it.thera.thip.base.cliente.ClienteVendita;
import it.thera.thip.base.cliente.Sconto;
import it.thera.thip.base.comuniVenAcq.*;
import it.thera.thip.base.comuniVenAcq.web.DatiArticoloRigaVendita;
import it.thera.thip.base.documenti.*;
import it.thera.thip.base.generale.*;
import it.thera.thip.base.interfca.RiferimentoVociCA;
import it.thera.thip.base.interfca.SottogruppoContiCA;
import it.thera.thip.base.partner.Indirizzo;
import it.thera.thip.cs.DatiComuniEstesi;
import it.thera.thip.cs.ThipRuntimeException;
import it.thera.thip.magazzino.generalemag.*;
import it.thera.thip.magazzino.matricole.StoricoMatricola;
import it.thera.thip.servizi.anagraficiBase.BeneLocazione;
import it.thera.thip.vendite.generaleVE.CausaleDocumentoVendita;
import it.thera.thip.vendite.generaleVE.TipoRigaDocumentoVendita;
import it.thera.thip.vendite.ordineVE.*;
import it.thera.thip.vendite.generaleVE.CausaleRigaDocVen;
import it.thera.thip.vendite.generaleVE.TipoBolla;

import com.thera.thermfw.security.Entity;

import it.thera.thip.datiTecnici.configuratore.RigaConDettaglioConf;
import it.thera.thip.datiTecnici.configuratore.SchemaCfg;
import it.thera.thip.datiTecnici.configuratore.DettRigaConfigurazione;
import it.thera.thip.base.listini.ListinoVendita;
import it.thera.thip.datiTecnici.configuratore.DettRigaConfigurazioneDocVen;
import it.thera.thip.datiTecnici.configuratore.DettRigaConfigurazioneOrdVen;
import it.thera.thip.datiTecnici.configuratore.DettRigaConfigurazioneOffCli;

/**
 * DocumentoVenRigaSec.<br>
 * <br><br><b>Copyright (c): Thera SpA</b>
 * @author ????? ??/??/????
 */
/*
 * Revisions:
 * Number  Date         Owner   Description
 *         ??/??/????   ??      Prima stesura
 *         30/05/2003   ME      Inseriti metodi checkAll e checkArticoloNoKit
 * 03187   21/01/2005   LP      Aggiunto generazione automatica dei lotti
 * 03373   11/03/2005   MN      Modificato il metodo ricalcoloQuantita(), settato
 *                              correttamente lo scale.
 * 03498   04/04/2005   MN      Migrazione della fix 3373 in 2.0
 * 03592   13/04/2005   ME      Sistemazione calcolo settimane in completaBO
 * 03688   28/04/2005   PM      Evasione ordini: ordine con un articolo gestito
 *                              a lotti con proposizione automatica dei lotti
 *                              "multipla", al salvataggio della riga documento
 *                              la quantità totale dell'articolo viene riportata
 *                              uguale a quella dell'ultimo lotto individuato
 * 03230   28/04/2005   ME      Aggiunto metodo getRigaDestinazionePerCopia
 * 03929   16/06/2005   ME      Aggiunti metodi calcolaDatiVendita e verificaAzzeraPrezzo
 * 04060   05/07/2005   ME      Modificato metodo recuperoDatiVenditaArticoloPrm
 * 04166  29/07/2005    ME      Spostato richiamo super.completaBO per evitare
 *                              eccezioni in caso di creazione manuale di riga sec
 * 04453  21/09/2005  DBot      Aggiunto ritorno qta in um primaria di riga primaria per ricalcolo coeff. impiego
 *                              Modificato ricalcolo delle QTA per riga sec
 * 04670   02/12/2005  MN       Gestione Unità Misura con flag Quantità intera.
 * 04800   02/01/2006  ME       Metodo getSequenzaNuovaRiga da protected a public
 * 05110   01/03/2006  ME       Aggiunto metodo setRigaPrimariaPerCopia e
 *                              controllo su richiamo completaBO
 * 05117   14/03/2006  GN       Correzione nella gestione delle unità di misura con flag Quantità intera
 * 05350   26/04/2006  MN       Modificato il metodo proponiLotti(..), per la gestione dei lotti automatici.
 * 05617   28/06/2006  ME       Modificato completaBO: aggiunta importazione della
 *                              commessa dalla riga primaria
 * 05501  05/07/2006   GM       aggiunto trasmissione a Logis
 * 06754  28/02/2007   MG       gestione righe secondarie da fatturare
 * 06439  20/03/2007   ME       Aggiunta logica per gestione servizi/noleggi
 * 07024  29/03/2007   ME       Modificato metodo recuperoDatiVenditaArticoloPrm
 * 07627  31/07/2007   PM       Inizializzazione nel costruttore dell'attributo coefficenteImpiego
 * 07779  10/09/2007   C&A      Togliere l'obbligatorietaà all'attributo AssogettamentoIVA
 * 08707  06/03/2008   ME       Aggiunto metodo regressioneMatricole
 * 09454  27/06/2008   MG       Se riga primaria ha flag nonFatturare = true, e non è una riga di servizio, forzo il flag a non fatturare sulla riga secondaria
 * 09671  25/08/2008   PM       Se una riga sec ha lo stesso articolo della riga prm e
 *                              il coefficente è 1 allora la sua quantita deve
 *                              essere uguale a quella della riga prm.
 * 09745  10/09/2008   PM	     Fix completamento della fix 9671: aggiunto metodo ricalcoloQuantita(DocumentoOrdineRiga rigaPrm, QuantitaInUMRif qtaRigaPrimaria)
 * 10042  05/11/2008   ME	     Ridefinito il metodo checkCoerenzaBeneArticolo
 * 10075  18/11/2008  FR         Modificata gestione campi data e settimana nel caso di valori null.
 * 10955   17/06/2009  Gscarta  modificate chiamate a convertiUM dell'articolo per passare la versione
 * 10987  30/06/2009   DB
 * 11123  15/07/2009   DB
 * 11239  05/08/2009   DB
 * 11084  05/08/2009   PM       Gestione picking e packing
 * 11414  08/10/2009   MG       Escamotage introdotto in metodo EliminaRiga
 * 12437  18/05/2010   LTB      Add checkDelete() method
 * 12508  20/04/2010   DBot     Aggiunta la gestione dei pesi e del volume
 * 13110  30/08/2011   DBot     Aggiunto test su attivazione calcolo pesi e volume
 * 13466   02/11/2011  PM       Errato ricalcolo della quantità della UM di magazzino
 *                              nelle righe secondarie
 * 13831  03/03/2011  Amara     Aggiunto idCliente nel creaProposizioneAutLotto quando magazzino e di conto lavoro interno
 * 14931  30/08/2011  DBot      Aggiunta gestione pesi/volume ceramiche
 * 14459  16/05/2011   TF       Traferimenti Bene in tabella addebiti/sospensioni
 * 16032  23/04/2012  AYM       In caso di ResoMerce da Cliente AND Articolo.Matricolato = Y AND (CodificaAutomaticaLottiAcquisti=ND
                                OR CodificaAutomaticaLottiProduzione=ND OR CodificaAutomaticaLottiLavEsterna=ND) proporre Lotto ND su DocVenLot.
 * 18309  24/07/2013  AA        Ridefinito il metodo checkPresenzaLottoDummy(per potere passare il doc definitivo con righeSec hanni lottoDummy)
 * 19215  12/02/2014  AYM       Gestione il flag "Quantità attesa entrata disponibilié" nella modello  di "Proposte evasione"
 * 17490  27/03/2013  Linda     Modificato il metodo checkArticoloNoKit().
 * 20304  03/09/2014  AYM       Correga il methodo proponiLotti.
 * 17984  03/12/2013  Linda     Aggiunto gancio per personalizzazioni : metodo getQtaLottoCorretta e impostaQtaLotto.
 * 18753  17/06/2015  Linda     Redefinire metodo checkIdEsternoConfig().
 * 22729  18/12/2015  Linda     Nella riga secondaria inserire il listino di prezzi indicato sulla riga primaria.
 * 22839  15/01/2016  Linda     Redifine metodo getEntityRiga().
 * 24613  07/12/2016  Linda     Gestione il salvataggio del dettaglio riga valore configurazione.
 * 25818  25/05/2017  OCH       Ridefinizione propagaDatiTestata 
 * 26807  24/01/2018  Linda     Aggiunto deleteOwnedRigheDdt().
 * 27337  19/04/2018  Linda    Gestione caso di note di accredito con nessun movimento di magazzino e un articolo gestito a lotti.
 * 27649   02/07/2018  LTB     Aggiunta un controllo bloccante che impedisca di inserire una riga con quantità non intera se l'um è gestita a quantità intera. 
 * 28305  26/11/2018  SZ		In caso delle righe secondarie con caricamento manuale impostare il prezzo a zero in caso non sia trovato su listino.
 * 28189  24/04/2019  SZ		Aggiungere il controllo della Data Consegna Confermata e Richiesta in caso di aggiornamento di massa.
 * 29240  25/04/2019  LTB    Se salvo la riga primaria del kit ottengo un optimistick lock per oggetto modificato da altro utente dovuto a doppio salvataggio della riga sec
 * 29396  24/05/2019  SZ		NullPointerException nel metodo checkQtaInUM in caso di QtaInUM(Rif , Prm ,Sec) contiene un valore errato il CM documenti vendita
 * 30193  13/12/2019  SZ	    Gestione della configurazione nel caso di articoli di tipo ceramico.
 * 30871  06/03/2020  SZ		6 Decimale 
 * 33304  08/04/2021  SZ		Se viene variata la quantità della riga kit , aggiornare le quantità
 * 33719  04/06/2021  SZ		Corretto calcolo disponibilità in evasione.
 * 33905  02/07/2021  SZ	  Nel caso di kit non gestito a magazzino il costo deve essere la somma dei costi delle righe secondarie
 * 34113  01/10/2021  SZ		Nel copia riga sec nn deve cambiata el causale.
 * 35639  02/05/2022  LTB       Gestione assegnazione dei lotti (con proposizione automatica o manuale dei lotti) 
 * 								che consideri quanto già assegnato nello stesso documento
 * 36381  29/07/2022  LTB       Non è necessario chiamare il checkQuadraturaLotti se c'è la proposizione lotti,  
 * 								Anomalia nella gestione della messagistica sulla disponibilità lotti in evasione.
 * 37244  08/12/2022  YBA      Corregere il problemea perché del copia un ordine cambiando la causale e le codizioni di vendita sono impostate Da documento gli sconti testata e la provvigione 1 delle righe non deveno essere riprese dalla testata.
 * 37556  18/01/2023  YBA      Corregere il problemea perché in copia ordine lo sconto intestatario presente sul cliente non viene messo su tutte le righe le secondarie.
 * 38150  21/03/2023  YBA     Nella riga secondaria inserire il centro costo indicato sulla riga primaria.
 * 38908  05/06/2023  LTB       In alcune condizioni di la proposizione atomatica dei lotti occupa in modo abnorme la memoria 
 * 39402  24/07/2023  SZ      Scale errato se il database ha le quantità a 6 decimali
 * 41393  20/02/2024  SZ	  i dati relativi a peso lordo e netto di riga vengono reperiti sempre dai dati tecnici dell'articolo, pur in presenza di dati differenti specificati nella versione indicata nella riga del documento.
 * 41966  05/04/2024  SBR      Modifica in metodo isControlloLottoDummyDaEscludere()
 * 42148  24/04/2024  SBR      Varie modifiche intellimag
 * 43330  20/09/2024  TA       Correggi il metodo di ricolcoloQuantitaDaRicalcolare se l'articolo non esiste 
 * 44499  13/01/2025  KD      Correggere passaggi valiso /annullato e sospeso e viceversa per gestire correttamente lo stato delle righe secondarie
 * 45124  07/03/2025  SZ	  recalcola il pesi volume si lo stato è cambiati 
 * 44784  02/05/2025  RA	  Rendi la ConfigArticoloPrezzo persistent
*/

//public class DocumentoVenRigaSec extends DocumentoVenditaRiga {//Fix 24613
public class DocumentoVenRigaSec extends DocumentoVenditaRiga implements RigaConDettaglioConf {//Fix 24613
    //Attributi
  protected Proxy iRigaPrimaria = new Proxy(DocumentoVenRigaPrm.class);

  boolean iSalvaRigaPrimaria = true;

  private final static String NUMERATORE = "DocVenRigaSec";


  //Query che seleziona la sequenza più alta tra le righe secondarie di una
  //riga primaria.
  //A questo valore verrà aggiunto 1 e si otterrà la sequenza della nuova riga
  //secondaria.
  protected static final String SELECT_MAX_SEQUENZA_RIGHE_SEC =
    "SELECT MAX(" +
      DocumentoVenRigaSecTM.SEQUENZA_RIGA +
    ") FROM " +
      DocumentoVenRigaSecTM.TABLE_NAME_PRINCIPALE + " " +
    "WHERE " +
      DocumentoVenRigaSecTM.ID_AZIENDA + "=? AND " +
      DocumentoVenRigaSecTM.ID_ANNO_DOC + "=? AND " +
      DocumentoVenRigaSecTM.ID_NUMERO_DOC + "=? AND " +
      DocumentoVenRigaSecTM.ID_RIGA_DOC + "=?";

  protected static CachedStatement cSelectMaxSequenzaRigheSec =
    new CachedStatement(SELECT_MAX_SEQUENZA_RIGHE_SEC);

  public DocumentoVenRigaSec() {
    super();
    setSpecializzazioneRiga(RIGA_SECONDARIA_PER_COMPONENTE);
    this.iRigaCollegata = new Proxy(DocumentoVenRigaSec.class);
    this.iRigheLotto =  new OneToMany(DocumentoVenRigaLottoSec.class, this, 31, true);
    this.iRigheContenitore = new OneToMany(ContenitoreVenRigaSec.class, this, 31, true);
    this.iRigaOrdine =  new Proxy(it.thera.thip.vendite.ordineVE.OrdineVenditaRigaSec.class);
    iNonFatturare = true;   //MG FIX 6754;
    //Fix 7627 PM Inizio
    setCoefficienteImpiego(new BigDecimal(1.0));
    //Fix 7627 PM Fine
    datiArticolo = (DatiArticoloRigaVendita)Factory.createObject(DatiArticoloRigaVendita.class);//Fix 33905

  }

  //Metodi get/set attributi

  /**
   * Restituisce l'attributo Proxy RigaCollegata.
   */
  public DocumentoVenRigaPrm getRigaPrimaria() {
    return (DocumentoVenRigaPrm)iRigaPrimaria.getObject();
  }


  /**
   * Valorizza l'attributo Proxy RigaCollegata.
   */
  public void setRigaPrimaria(DocumentoVenRigaPrm rigaPrimaria){
    iRigaPrimaria.setObject(rigaPrimaria);
    setDirty();
    setOnDB(false);
    getCommentHandlerManager().setOwnerKeyChanged();
    this.getRigheContenitoreInternal().setFatherKeyChanged();
    this.getRigheLottoInternal().setFatherKeyChanged();
   }


  /**
   * Restituisce l'attributo Proxy RigaCollegata.
   */
  public String getRigaPrimariaKey() {
    return iRigaPrimaria.getKey();
  }


  /**
   * Valorizza la chiave dell'attributo Proxy RigaCollegata.
   */
  public void setRigaPrimariaKey(String key) {
    iRigaPrimaria.setKey(key);
    setDirty();
    setOnDB(false);
    getCommentHandlerManager().setOwnerKeyChanged();
    this.getRigheContenitoreInternal().setFatherKeyChanged();
    this.getRigheLottoInternal().setFatherKeyChanged();
  }

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
    //Il seguente return è provvisorio
    return super.getOrderByClause();
  }

  protected TableManager getTableManager() throws java.sql.SQLException {
       return DocumentoVenRigaSecTM.getInstance();
  }

  public void setEqual(Copyable obj) throws CopyException {
    super.setEqual(obj);
    DocumentoVenRigaSec doc = (DocumentoVenRigaSec)obj;
    this.iRigaPrimaria.setEqual(doc.iRigaPrimaria);
  }

  /**
   * Ridefinizione del metodo getNumeroRiga della classe
   * OrdineVenditaRiga
   */
  protected void componiChiave() {
    try {
      int dett = Numerator.getNextInt(NUMERATORE);
      if (dett == 0)
        dett = Numerator.getNextInt(NUMERATORE);

      setDettaglioRigaDocumento(new Integer(dett));
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Ridefinizione del metodo eliminaRigaOmaggioCollegata della classe
   * DocumentoVenditaRiga
   */
  protected int eliminaRigaOmaggioCollegata(String key) throws SQLException {
    int rc = 0;

    DocumentoVenRigaSec rigaOmaggio =
      (DocumentoVenRigaSec)Factory.createObject(DocumentoVenRigaSec.class);
    rigaOmaggio.setKey(key);
    if (rigaOmaggio.retrieve()) {
      rc = rigaOmaggio.delete();
    }

    return rc;
  }

  public DocumentoBase getTestata()
  {
    DocumentoBase ret = null;
    DocumentoBaseRiga riga = (DocumentoBaseRiga)getRigaPrimaria();
    if (riga != null)
        ret = riga.getTestata();
    return ret;
  }

  public String getTestataKey()
  {
    String ret = null;
    DocumentoBaseRiga riga = (DocumentoBaseRiga)getRigaPrimaria();
    if (riga != null)
        ret = riga.getTestataKey();
    return ret;
  }


  protected void setSalvaRigaPrimaria(boolean salvaRigaPrimaria)
  {
    iSalvaRigaPrimaria = salvaRigaPrimaria;
  }

  protected boolean isSalvaRigaPrimaria()
  {
    return iSalvaRigaPrimaria;
  }

  protected int eliminaRiga() throws SQLException
  {
     //Fix 12508 inizio
     if(isSalvaRigaPrimaria())
        aggiornaPesiEVolumeRigaPrm(true);
     //Fix 12508 fine

    int rc = super.eliminaRiga();
    if (isSalvaRigaPrimaria())
        rc = salvaRigaPrimaria(rc);
//MG FIX 11414 inizio : escamotage per impedire errore -6 a seguito della cancellazione
// di più righe secondarie contemporaneamente su selezione multipla
    String key = this.getRigaPrimariaKey();
    iRigaPrimaria.setKey(null);
    iRigaPrimaria.setKey(key);
//MG FIX 11414 fine

return rc;
  }

//MG FIX 9454 inizio
  protected void getNonFatturareDaRigaPrm() {
    char tipoRigaPrm = getRigaPrimaria().getTipoRiga();

    if (tipoRigaPrm == TipoRigaDocumentoVendita.SERVIZIO_ASSIST_MANUT ||
        tipoRigaPrm == TipoRigaDocumentoVendita.SERVIZIO_CANONE ||
        tipoRigaPrm == TipoRigaDocumentoVendita.SERVIZIO_NOLEGGIO)
      return;
    setNonFatturare(getRigaPrimaria().isNonFatturare());
  }
//MG FIX 9454 fine

  protected int salvaRiga(DocumentoVendita testata, boolean newRiga) throws SQLException
  {
     //Fix 12508 inizio
     boolean aggiornaPesiEVolumeTes = false;
     if(isSalvaRigaPrimaria())
     {
        //if(isQuantitaCambiata() || !isOnDB() || isPesiVolumiCambiati())//Fix 41393
    	//if(isQuantitaCambiata() || !isOnDB() || isPesiVolumiCambiati() || isVersioneCambiata())//Fix 41393 //Fix 445124
    	 if(isServeRecalcoloPesiVolumiInCambiamentoStato() || isQuantitaCambiata() || !isOnDB() || isPesiVolumiCambiati() || isVersioneCambiata())//Fix 45124
        {
           calcolaPesiEVolume();
           aggiornaPesiEVolumeRigaPrm(false);
           //aggiornaPesiEVolumeTes = isRicalcolaPesiEVolume() && getRigaPrimaria().isRicalcolaPesiEVolume();
        }
     }
     //Fix 12508 fine

    //fix 5501 - inizio
    boolean passaALogis = false;
    if (isTrasmissioneALogisDaEffettuare()){
      passaALogis = testTrasmissioneDoc();
      setStatoTrasmissioneALogis(passaALogis);
    }
    //Fix  11084 PM Inizio
    if (!passaALogis)
    	setRigaTrasmittibilePPL(testTrasmissionePPL());
    //Fix  11084 PM Fine
    //fix 5501 - fine

//MG FIX 9454 inizio
    if (getRigaPrimaria().isNonFatturare())
      getNonFatturareDaRigaPrm();
//MG FIX 9454 fine

    int rc = super.salvaRiga(testata,newRiga);

    //fix 5501 - inizio
    if (rc >= ErrorCodes.NO_ROWS_UPDATED && passaALogis &&  isTrasmissioneALogisDaEffettuare())
     trasmettiALogis();
    //fix 5501 - fine

    //Fix 3929 - inizio
    verificaAzzeraPrezzo();
    //Fix 3929 - fine
    if (isSalvaRigaPrimaria()) {

        //MG FIX 6754 inizio
      boolean totaliDaRicalcolare = isDaRicalcolare();
      //72296 Softre <
      DocumentoOrdineRiga rigaPrm = getRigaPrimaria();
		char tipoParte = rigaPrm.getArticolo().getTipoParte();
		char tipoCalcoloPrezzo = rigaPrm.getArticolo().getTipoCalcPrzKit();
		if (!totaliDaRicalcolare && (tipoParte == ArticoloDatiIdent.KIT_NON_GEST ||
				tipoParte == ArticoloDatiIdent.KIT_GEST)
				&& tipoCalcoloPrezzo == ArticoloDatiVendita.SUL_PRODOTTO_FINITO
				&& newRiga) {
			totaliDaRicalcolare = true;
			if(getRigaPrimaria() instanceof YDocumentoVenRigaPrm) {
				((YDocumentoVenRigaPrm)getRigaPrimaria()).setAbilitaCalcoloTotRigheSecConReset(true);
			}
		}
      //72296 Softre >
      if (totaliDaRicalcolare)
        getRigaPrimaria().calcolaPrezzoDaRigheSecondarieSenzaReset();
        //MG FIX 6754 fine


        rc = salvaRigaPrimaria(rc);

        //MG FIX 6754 inizio
        //Fix 12508 inizio
        if (totaliDaRicalcolare)
          ((DocumentoVendita)this.getTestata()).calcolaCostiValoriOrdine(false);

        if(totaliDaRicalcolare || aggiornaPesiEVolumeTes)
          rc = salvaTestata(rc);
        /*
        if (totaliDaRicalcolare) {
           ((DocumentoVendita)this.getTestata()).calcolaCostiValoriOrdine(false);
           rc = salvaTestata(rc);
         }
         */
        //Fix 12508 fine
        //MG FIX 6754 fine
    }
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


  /**
   * Ridefinizione del metodo recuperoDatiVenditaSave della classe
   * DocumentoVenditaRiga
   */
  protected boolean recuperoDatiVenditaSave() {
    return recuperoDatiVenditaArticoloPrm() &&
           isServizioCalcDatiVendita() &&
           ! isRigaOfferta() &&
           getTipoRiga() != TipoRiga.SPESE_MOV_VALORE;
  }


  /**
   * Restituisce un flag che capisce se deve essere effettuatoil calcolo dei
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

  protected int salvaRigaPrimaria(int rc) throws SQLException
  {
      if (rc >= 0)
      {
          DocumentoVenRigaPrm rigaPrm = (DocumentoVenRigaPrm)getRigaPrimaria();
          rigaPrm.setSalvaRigheSecondarie(false);
//          rigaPrm.disabilitaTrasmissionePPL(); //11084 PM
          int rc1 = rigaPrm.save();
          rigaPrm.setSalvaRigheSecondarie(true);
          rc =  rc1 >= 0 ? rc + rc1 : rc1;
      }
      return rc;
  }

  public void aggiornaQuantitaSuiSaldi()
  {
    Articolo articoloRigaPrm = getRigaPrimaria().getArticolo();
    if (articoloRigaPrm.getTipoParte() == ArticoloDatiIdent.KIT_GEST) {
      // Nel caso di convalida e regressione non deve essere ricreato il lotto dummy
      // il controllo è doppio perchè può essere nel caso della riga seconaria
      // stessa o della riga secondaria in quanto secondaria di una primaria
      if (!(this.iApplicoMovimenti || getRigaPrimaria().isApplicoMovimenti()))
         this.controllaPresenzaLottoDummy();
      disabilitaAggiornamentoSaldiSuiLotti();
      //this.sistemoLeQuantita();
    }
    else {
      super.aggiornaQuantitaSuiSaldi();
    }
  }

  /**
   * Overwrite da OrdinevenditaRiga
   */
  public void stornaQuantitaSuiSaldi()
  {
    Articolo articoloRigaPrm = getRigaPrimaria().getArticolo();
    if (articoloRigaPrm.getTipoParte() == ArticoloDatiIdent.KIT_GEST) {
      disabilitaAggiornamentoSaldiSuiLotti();
      //this.sistemoLeQuantita();
    }
    else {
      super.stornaQuantitaSuiSaldi();
    }
  }
  /**
   * Ricalcola la quantità delle righe secondarie
   * @deprecated
   */
  public void ricalcoloQuantita(BigDecimal qtaRigaPrm) {

     //Fix 9671 PM Inizio
     /*
     //fix 4453 inizio (modifica ricalcolo qta)
     // Inizio modifica Mz001
      BigDecimal qtaRicalcolata = new BigDecimal("0.00");
      if(!isBloccoRicalcoloQtaComp())
      {
         //Ricalcolo qta in base al coefficente
         qtaRicalcolata = qtaRigaPrm.multiply(getCoefficienteImpiego());
      }
      else
      {
         //RigaSec a qta fissa
         char statoAvanzamento = getStatoAvanzamento();
         if(statoAvanzamento == StatoAvanzamento.DEFINITIVO)
            qtaRicalcolata = getQtaAttesaEvasione().getQuantitaInUMPrm();
         else
            qtaRicalcolata = getQtaPropostaEvasione().getQuantitaInUMPrm();
      }
      setQtaInUMPrm(qtaRicalcolata);
      Articolo articolo = getArticolo();
      UnitaMisura umPrm = getUMPrm();
      UnitaMisura umRif = getUMRif();
      if(umRif != null)
      {
         BigDecimal qtaRif = articolo.convertiUM(qtaRicalcolata, umPrm, umRif);
         qtaRif = qtaRif.setScale(2, 4);
         setQtaInUMVen(qtaRif);
      }
      UnitaMisura umSec = getUMSec();
      if(umSec != null)
      {
         BigDecimal qtaSec = articolo.convertiUM(qtaRicalcolata, umPrm, umSec);
         qtaSec = qtaSec.setScale(2, 4);
         setQtaInUMSec(qtaSec);
      }
      // Inizio 4670
      if (UnitaMisura.isPresentUMQtaIntera(umRif, umPrm, umSec, articolo)){ //Fix 5117
        QuantitaInUMRif qta = articolo.calcolaQuantitaArrotondate(qtaRicalcolata, umRif, umPrm, getUMSec(),Articolo.UM_PRM);
        setQtaInUMVen(qta.getQuantitaInUMRif());
        setQtaInUMPrm(qta.getQuantitaInUMPrm());
        if (umSec != null)
          setQtaInUMSec(qta.getQuantitaInUMSec());
      }
      // Fine 4670

/*
    if (! isBloccoRicalcoloQtaComp()) {
      BigDecimal qtaRicalcolata = qtaRigaPrm.multiply(getCoefficienteImpiego());
      setQtaInUMPrm(qtaRicalcolata);

      Articolo articolo = getArticolo();
      UnitaMisura umPrm = getUMPrm();
      UnitaMisura umRif = getUMRif();
      //Inizio 3373
      BigDecimal qta = null;
      //Fine 3373
      if (umRif != null) {
        // Inizio 3373
//        this.setQtaInUMVen(articolo.convertiUM(qtaRicalcolata, umPrm, umRif));
        qta = articolo.convertiUM(qtaRicalcolata, umPrm, umRif);
        qta = qta.setScale(2, BigDecimal.ROUND_HALF_UP);
        this.setQtaInUMVen(qta);
        //Fine 3373
      }

      UnitaMisura umSec = getUMSec();
      if (umSec != null) {
        // Inizio 3373
//        setQtaInUMSec(articolo.convertiUM(qtaRicalcolata, umPrm, umSec));
        qta = articolo.convertiUM(qtaRicalcolata, umPrm, umSec);
        qta = qta.setScale(2, BigDecimal.ROUND_HALF_UP);
        setQtaInUMSec(qta);
        // Fine 3373
      }
    }
 */
    //fix 4453 fine (modifica ricalcolo qta)
     ricalcoloQuantita(this.getRigaPrimaria());
    // Fix 9671 PM Fine
  }

  //Fix 9745 PM Inizio
     public void ricalcoloQuantita(DocumentoOrdineRiga rigaPrm)
     {
         DocumentoVenRigaPrm rigaDocPrm = (DocumentoVenRigaPrm)rigaPrm;
         ricalcoloQuantita(rigaDocPrm, rigaDocPrm.getServizioQta());
     }
//Fix 9745 PM Inizio


  // Fix 9671 PM Inizio
  //public void ricalcoloQuantita(DocumentoOrdineRiga rigaPrm) //Fix 9745 PM
  public void ricalcoloQuantita(DocumentoOrdineRiga rigaPrm, QuantitaInUMRif qtaRigaPrimaria)  //Fix 9745 PM
  {
       // fix 10987
       QuantitaInUMRif qtaRigaSec = ricolcoloQuantitaDaRicalcolare(rigaPrm, qtaRigaPrimaria);
       this.setQtaInUMPrm(qtaRigaSec.getQuantitaInUMPrm());
       this.setQtaInUMSec(qtaRigaSec.getQuantitaInUMSec());
       this.setQtaInUMVen(qtaRigaSec.getQuantitaInUMRif());
       //Fix 33304 Inizio
	      if(this.getStatoAvanzamento() == StatoAvanzamento.PROVVISORIO) {
	          this.setQtaPropostaEvasione(qtaRigaSec);
	        }
	        else if(this.getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO) {
	          if(this.isCollegataAMagazzino()) {
	        	  this.setQtaSpedita(qtaRigaSec);
	          }
	          else {
	        	  this.setQtaAttesaEvasione(qtaRigaSec);
	          }
	        }
       //Fix 33304 Fine
       /*
       DocumentoVenRigaPrm rigaDocPrm = (DocumentoVenRigaPrm)rigaPrm;
       Articolo articoloRigaPrm = rigaDocPrm.getArticolo();
       Articolo articolo = getArticolo();
       BigDecimal coffImpiego = getCoefficienteImpiego();
       if (articoloRigaPrm.equals(articolo)  && coffImpiego.compareTo(new BigDecimal("1.00")) == 0)
       {
           //Se l'articolo della riga sec è uguale all'articolo della riga prm
           //e il coefficente di impiego è 1, allora la quantità della riga secondaria
           //deve essere uguale a quella della riga primaria
           if (!isBloccoRicalcoloQtaComp())
           {
               //Fix 9745 PM Inizio
               //QuantitaInUMRif qta = rigaDocPrm.getServizioQta();
               QuantitaInUMRif qta = qtaRigaPrimaria;
               //Fix 9745 PM Fine

               UnitaMisura umPrm = getUMPrm();
               UnitaMisura umRif = getUMRif();
               UnitaMisura umSec = getUMSec();

               setQtaInUMPrm(qta.getQuantitaInUMPrm());
               if (umSec != null)
                   setQtaInUMSec(qta.getQuantitaInUMSec());

               if (umRif.equals(rigaDocPrm.getUMRif()))
                   setQtaInUMVen(qta.getQuantitaInUMRif());
               else if (umRif.equals(rigaDocPrm.getUMPrm()))
                   setQtaInUMVen(qta.getQuantitaInUMPrm());
               else if (umSec != null && umRif.equals(rigaDocPrm.getUMSec()))
                   setQtaInUMVen(qta.getQuantitaInUMSec());
               else
               {
                   if (UnitaMisura.isPresentUMQtaIntera(umRif, umPrm, umSec, articolo))
                   {
                       QuantitaInUMRif qta1 = articolo.calcolaQuantitaArrotondate(qta.getQuantitaInUMPrm(), umRif, umPrm, umSec, this.getArticoloVersRichiesta(), Articolo.UM_PRM); // fix 10955
                       setQtaInUMVen(qta1.getQuantitaInUMRif());
                   }
                   else
                       setQtaInUMVen(articolo.convertiUM(qta.getQuantitaInUMPrm(), umPrm, umRif, this.getArticoloVersRichiesta())); // fix 10955
               }

           }

       }
       else
       {
           //Fix 9745 PM Inizio
           //BigDecimal qtaRigaPrm = rigaDocPrm.getServizioQta().getQuantitaInUMPrm();
           BigDecimal qtaRigaPrm = qtaRigaPrimaria.getQuantitaInUMPrm();
           //Fix 9745 PM Fine
           BigDecimal qtaRicalcolata = new BigDecimal("0.00");
           if (!isBloccoRicalcoloQtaComp())
           {
               //Ricalcolo qta in base al coefficente
               qtaRicalcolata = qtaRigaPrm.multiply(coffImpiego);
           }
           else
           {
               //RigaSec a qta fissa
               char statoAvanzamento = getStatoAvanzamento();
               if (statoAvanzamento == StatoAvanzamento.DEFINITIVO)
                   qtaRicalcolata = getQtaAttesaEvasione().getQuantitaInUMPrm();
               else
                   qtaRicalcolata = getQtaPropostaEvasione().getQuantitaInUMPrm();
           }
           setQtaInUMPrm(qtaRicalcolata);
           UnitaMisura umPrm = getUMPrm();
           UnitaMisura umRif = getUMRif();
           if (umRif != null)
           {
               BigDecimal qtaRif = articolo.convertiUM(qtaRicalcolata, umPrm, umRif, this.getArticoloVersRichiesta()); // fix 10955
               qtaRif = qtaRif.setScale(2, 4);
               setQtaInUMVen(qtaRif);
           }
           UnitaMisura umSec = getUMSec();
           if (umSec != null)
           {
               BigDecimal qtaSec = articolo.convertiUM(qtaRicalcolata, umPrm, umSec, this.getArticoloVersRichiesta()); // fix 10955
               qtaSec = qtaSec.setScale(2, 4);
               setQtaInUMSec(qtaSec);
           }
           if (UnitaMisura.isPresentUMQtaIntera(umRif, umPrm, umSec, articolo))
           {
               QuantitaInUMRif qta = articolo.calcolaQuantitaArrotondate(qtaRicalcolata, umRif, umPrm, getUMSec(), this.getArticoloVersRichiesta(), Articolo.UM_PRM); // fix 10955
               setQtaInUMVen(qta.getQuantitaInUMRif());
               setQtaInUMPrm(qta.getQuantitaInUMPrm());
               if (umSec != null)
                   setQtaInUMSec(qta.getQuantitaInUMSec());
           }
       }
       */
  }

  // fix 10987
  public QuantitaInUMRif ricolcoloQuantitaDaRicalcolare(DocumentoOrdineRiga rigaPrm, QuantitaInUMRif qtaRigaPrimaria){
    QuantitaInUMRif qtaRigaSec = new QuantitaInUMRif(this.getServizioQta().getQuantitaInUMPrm(),
        this.getServizioQta().getQuantitaInUMSec(),this.getServizioQta().getQuantitaInUMRif());
    BigDecimal coffImpiego = new BigDecimal(1);//43330
    DocumentoVenRigaPrm rigaDocPrm = (DocumentoVenRigaPrm)rigaPrm;
    Articolo articoloRigaPrm = rigaDocPrm.getArticolo();
    Articolo articolo = getArticolo();
    //Fix Inizio 43330
    if (articolo == null)
    	return qtaRigaSec;
    if (getCoefficienteImpiego() != null )
    	 coffImpiego = getCoefficienteImpiego();
    //Fix Fine 43330 
    if (articoloRigaPrm.equals(articolo)  && coffImpiego.compareTo(new BigDecimal("1.00")) == 0)
    {
        //Se l'articolo della riga sec è uguale all'articolo della riga prm
        //e il coefficente di impiego è 1, allora la quantità della riga secondaria
        //deve essere uguale a quella della riga primaria
        if (!isBloccoRicalcoloQtaComp())
        {
            //Fix 9745 PM Inizio
            //QuantitaInUMRif qta = rigaDocPrm.getServizioQta();
            QuantitaInUMRif qta = qtaRigaPrimaria;
            //Fix 9745 PM Fine

            UnitaMisura umPrm = getUMPrm();
            UnitaMisura umRif = getUMRif();
            UnitaMisura umSec = getUMSec();

            qtaRigaSec.setQuantitaInUMPrm(qta.getQuantitaInUMPrm());
            if (umSec != null)
                qtaRigaSec.setQuantitaInUMSec(qta.getQuantitaInUMSec());

            if (umRif.equals(rigaDocPrm.getUMRif()))
                qtaRigaSec.setQuantitaInUMRif(qta.getQuantitaInUMRif());
            else if (umRif.equals(rigaDocPrm.getUMPrm()))
                qtaRigaSec.setQuantitaInUMRif(qta.getQuantitaInUMPrm());
            else if (umSec != null && umRif.equals(rigaDocPrm.getUMSec()))
                qtaRigaSec.setQuantitaInUMRif(qta.getQuantitaInUMSec());
            else
            {
                if (UnitaMisura.isPresentUMQtaIntera(umRif, umPrm, umSec, articolo))
                {
                    QuantitaInUMRif qta1 = articolo.calcolaQuantitaArrotondate(qta.getQuantitaInUMPrm(), umRif, umPrm, umSec,  this.getArticoloVersRichiesta(), Articolo.UM_PRM);
                    qtaRigaSec.setQuantitaInUMRif(qta1.getQuantitaInUMRif());
                }
                else
                    qtaRigaSec.setQuantitaInUMRif(articolo.convertiUM(qta.getQuantitaInUMPrm(), umPrm, umRif, this.getArticoloVersRichiesta()));
            }  
        }
    }
    else
    {
        //Fix 9745 PM Inizio
        //BigDecimal qtaRigaPrm = rigaDocPrm.getServizioQta().getQuantitaInUMPrm();
        BigDecimal qtaRigaPrm = qtaRigaPrimaria.getQuantitaInUMPrm();
        //Fix 9745 PM Fine
        BigDecimal qtaRicalcolata = new BigDecimal("0.00");
		qtaRicalcolata = Q6Calc.get().setScale(qtaRicalcolata,2);//Fix 30871
        if (!isBloccoRicalcoloQtaComp())
        {
            //Ricalcolo qta in base al coefficente
            qtaRicalcolata = qtaRigaPrm.multiply(coffImpiego);
        }
        else
        {
            //RigaSec a qta fissa
            char statoAvanzamento = getStatoAvanzamento();
            if (statoAvanzamento == StatoAvanzamento.DEFINITIVO)
                qtaRicalcolata = getQtaAttesaEvasione().getQuantitaInUMPrm();
            else
                qtaRicalcolata = getQtaPropostaEvasione().getQuantitaInUMPrm();
        }
        //qtaRicalcolata = qtaRicalcolata.setScale(qtaRigaPrm.scale(), BigDecimal.ROUND_HALF_UP);//Fix 13466 PM //Fix 30871
		//qtaRicalcolata = Q6Calc.get().setScale(qtaRicalcolata,qtaRigaPrm.scale(), BigDecimal.ROUND_HALF_UP);//Fix 30871 //Fix 39402
        qtaRicalcolata = Q6Calc.get().setScale(qtaRicalcolata,2, BigDecimal.ROUND_HALF_UP);//Fix 39402
        qtaRigaSec.setQuantitaInUMPrm(qtaRicalcolata);
        UnitaMisura umPrm = getUMPrm();
        UnitaMisura umRif = getUMRif();
        if (umRif != null)
        {
            BigDecimal qtaRif = articolo.convertiUM(qtaRicalcolata, umPrm, umRif, this.getArticoloVersRichiesta());
            //qtaRif = qtaRif.setScale(2, 4); //Fix 30871
			qtaRif = Q6Calc.get().setScale(qtaRif,2, 4); //Fix 30871
            qtaRigaSec.setQuantitaInUMRif(qtaRif);
        }
        UnitaMisura umSec = getUMSec();
        if (umSec != null)
        {
            BigDecimal qtaSec = articolo.convertiUM(qtaRicalcolata, umPrm, umSec, this.getArticoloVersRichiesta());
            //qtaSec = qtaSec.setScale(2, 4); //Fix 30871
			qtaSec = Q6Calc.get().setScale(qtaSec,2, 4); //Fix 30871
            qtaRigaSec.setQuantitaInUMSec(qtaSec);
        }
        if (UnitaMisura.isPresentUMQtaIntera(umRif, umPrm, umSec, articolo))
        {
            QuantitaInUMRif qta = articolo.calcolaQuantitaArrotondate(qtaRicalcolata, umRif, umPrm, getUMSec(), this.getArticoloVersRichiesta(),Articolo.UM_PRM);
            qtaRigaSec.setQuantitaInUMRif(qta.getQuantitaInUMRif());
            qtaRigaSec.setQuantitaInUMPrm(qta.getQuantitaInUMPrm());
            if (umSec != null)
                qtaRigaSec.setQuantitaInUMSec(qta.getQuantitaInUMSec());
        }
    }
    return qtaRigaSec;

  }
  // fine fix 10987

  // Fix 9671 PM Fine

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
      iRigheContenitore.setFatherKeyChanged();
      setIdAziendaInternal(idAzienda);
  }
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
    iRigheContenitore.setFatherKeyChanged();

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
    iRigheContenitore.setFatherKeyChanged();
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
    iRigheContenitore.setFatherKeyChanged();
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

    DocumentoVenRigaPrm rigaPrm = getRigaPrimaria();
    if(rigaPrm != null && rigaPrm.rigaDaCopiareKey == null)//Fix 34113
    	setCausaleRiga(rigaPrm.getCausaleRiga());

    //Fix 4166 - inizio
     super.completaBO();
    //Fix 4166 - fine

    //Fix 3592 - inizio
    Date dataConsRich = rigaPrm.getDataConsegnaRichiesta();
    setDataConsegnaRichiesta(dataConsRich);
    Date dataConsConf = rigaPrm.getDataConsegnaConfermata();
    setDataConsegnaConfermata(dataConsConf);

    if (dataConsRich != null){//Fix 10075 FR
    	int[] datiSett = TimeUtils.getISOWeek(dataConsRich);
    	String sett =
    		DocumentoOrdineTestata.getSettimanaFormattata(
    				datiSett[0], datiSett[1]
    		);
    	setSettConsegnaRichiesta(sett);
    } else {
    	setSettConsegnaRichiesta(null);
    }//Fix 10075 FR

    if (dataConsConf != null){//Fix 10075 FR
    	int [] datiSett = TimeUtils.getISOWeek(dataConsConf);
    	String sett =
    		DocumentoOrdineTestata.getSettimanaFormattata(
    				datiSett[0], datiSett[1]
    		);
    	setSettConsegnaConfermata(sett);
    }else {
    	setSettConsegnaConfermata(null);
    }//Fix 10075 FR
    //Fix 3592 - fine

    //Fix 5617 - inizio
    setCommessa(rigaPrm.getCommessa());
    //Fix 5617 - fine
    setListinoPrezzi(rigaPrm.getListinoPrezzi());//Fix 22729
    setCentroCosto(rigaPrm.getCentroCosto());//Fix 38150
  }



    protected DocumentoRigaLotto creaLottoDummy(){
        DocumentoVenRigaLottoSec lottoD;
        lottoD = (DocumentoVenRigaLottoSec)Factory.createObject(DocumentoVenRigaLottoSec.class);
        lottoD.setFather(this);
        lottoD.setIdLotto(LOTTO_DUMMY);
        lottoD.setIdArticolo(getIdArticolo());
        return (DocumentoVenRigaLotto)lottoD;
    }

  /**
   * Attributo di servizio
   */
  public BigDecimal getServizioQtaVenditaPrm() {
    try {
       //fix 4453 inizio
          return getRigaPrimaria().getServizioQta().getQuantitaInUMRif();
          //return getRigaPrimaria().getServizioQtaInUMVen();
       //fix 4453 fine
    }
    catch (Exception ex) {
      return new BigDecimal(0.0);
    }
  }

  //fix 4453 inizio
  public BigDecimal getServizioQtaUMPrmPrm() {
    try {
      return getRigaPrimaria().getServizioQta().getQuantitaInUMPrm();
    }
    catch (Exception ex) {
      return new BigDecimal(0.0);
    }
  }
  //fix 4453 fine

  /**
   * Restituisce la sequenza da assegnare ad una nuova riga secondaria
   */
  public static synchronized int getSequenzaNuovaRiga(DocumentoVenRigaPrm rigaPrm) {
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
    
    em = checkPerMassUpdate();
    if(em != null)
    	errors.addElement(em);
    
    return errors;
  }

  /**
   * Ridefinizione (DocumentoVenditaRiga)
   */
  public ErrorMessage checkCoerenzaBeneArticolo() {
  	return null;
  }

  /**
   * Viene verificato che non venga inserito un articolo che è a sua volta un
   * kit.
   */
  public ErrorMessage checkArticoloNoKit() {
    Articolo articolo = getArticolo();
    Trace.print("=============>>>>>>>>>>>>>>>>>"+getKey()+">>>>>>>>"+getCausaleRigaKey());
    //if (articolo.getTipoParte() == ArticoloDatiIdent.KIT_GEST ||//Fix 17490
    if (articolo != null &&//Fix 17490

        articolo.getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST) {
      return new ErrorMessage("THIP_BS203");
    }

    return null;
  }

  protected void impostaStatoAvanzamento(){
    super.impostaStatoAvanzamento();
    if (this.getStatoAvanzamento()== StatoAvanzamento.PROVVISORIO){
        try{
            this.getRigaPrimaria().rendiProvvisorio();
        }
        catch (CopyException e){
            e.printStackTrace(Trace.excStream);
        }
    }
  }
  /**
   * Nel caso in cui le righe secondarie siano di kit gestito a magazzino non deve
   * essere effettuato il controllo sulla quadratura dei lotti.
   */
  public ErrorMessage checkQuadraturaLotti() {
    char tipoDoc = ((DocumentoVendita)this.getTestata()).getTipoDocVenPerGestMM();//Fix 16032
    DocumentoVenRigaPrm rigaPrm = this.getRigaPrimaria();
    if (rigaPrm.getArticolo().getTipoParte()==ArticoloDatiIdent.KIT_GEST){
        return null;
    }
    //Fix 27337 inizio
    if(!isOnDB())
    {
    	//36381 inizio
    	if(tipoDoc == DocumentoVendita.TD_VENDITA || tipoDoc == DocumentoVendita.TD_SPE_CTO_TRASF) {
    		//...Controllo che la creazione automatica sia impostata
    		boolean ok = identificaLotto();
    		if(ok)
    			return null;
    	}
    	//36381 fine
    	
        if((getRigheLotto() == null || getRigheLotto().isEmpty()) && isControlloLottoDummyDaEscludere())
        	 return null;  
    }
    //Fix 27337 fine
    //Fix 16032 inizio
    if (tipoDoc == DocumentoVendita.TD_VENDITA_RESO) {
      boolean ok = identificazioneAutomaticaND(getArticolo());
       if(ok)
      return null;
      else return super.checkQuadraturaLotti();

    }
    //Fix 16032 fine
    else {
        return super.checkQuadraturaLotti();
    }
  }


  //Fix 2844 - inizio
  /**
   * Ridefinizione
   */
  public ContenitoreRiga istanziaContenitore(){
    return (ContenitoreVenRigaSec)Factory.createObject(ContenitoreVenRigaSec.class);
  }
  //Fix 2844 - fine

  //...FIX 3187 inizio

  /**
   * creaLottiAutomatici (da ridefinire negli eredi)
   */
  protected void creaLottiAutomatici(){
    //...Controllo se sono in un documento non di reso
    char tipoDoc = ((DocumentoVendita)this.getTestata()).getTipoDocVenPerGestMM();
    if(tipoDoc == DocumentoVendita.TD_VENDITA || tipoDoc == DocumentoVendita.TD_SPE_CTO_TRASF) {
      //...Controllo che la creazione automatica sia impostata
      boolean ok = identificaLotto();
      if(ok)
        proponiLotti(PersDatiMagazzino.TIPO_VEN, ProposizioneAutLotto.CREA_DA_DOCUMENTO, getIdMagazzino());
    }
    //Fix 16032 inizio
    else if(tipoDoc == DocumentoVendita.TD_VENDITA_RESO)
    {
      boolean ok = identificazioneAutomaticaND(getArticolo());
      if(ok){
        creaLottoND();
      }
    }
    //Fix 16032 fine
  }

  /**
   * proponiLotto
   * @param tipo char
   * @return boolean
   */
  public boolean identificaLotto() {
    return ProposizioneAutLotto.identificazioneAutomaticaAttiva(getArticolo());
  }

  /**
   * proponiLotti
   * @param tipo char
   * @param ambito char
   */
  public void proponiLotti(char tipo, char ambito, String idMagazzino) {
    List lottiOrig = new ArrayList();
    List lottiOrdine = new ArrayList();
    if(getRigaOrdine() != null) {
      List lottiRig = getRigaOrdine().getRigheLotto();
      for (int i = 0; i < lottiRig.size(); i++) {
        OrdineVenditaRigaLotto lt = (OrdineVenditaRigaLotto)lottiRig.get(i);
        if(!lt.getIdLotto().equals(Lotto.LOTTO_DUMMY)) {
          lottiOrig.add(lt.getLotto());
          //lottiOrdine.add(lt);//35639
        }
      }
      lottiOrdine = getImpegniLottiOrdine(true); //35639
    }

    BigDecimal qta = new BigDecimal(0);
    boolean isPropostaEva = true;
    if (getStatoAvanzamento() == StatoAvanzamento.PROVVISORIO && getQtaPropostaEvasione() != null){
      qta = getQtaPropostaEvasione().getQuantitaInUMPrm();
    }
    if (getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO && getQtaAttesaEvasione() != null){
      qta = getQtaAttesaEvasione().getQuantitaInUMPrm();
      isPropostaEva = false;
    }

    //35639 inizio
    /*ProposizioneAutLotto pal = ProposizioneAutLotto.creaProposizioneAutLotto(tipo,
    pal = pal.creaProposizioneAutLotto(*/
    
    List lottiAuto = new ArrayList();
	ProposizioneAutLotto pal = getProposizioneAutLotto();
	if(pal == null) {
		pal = creaProposizioneAutLotto();
		pal.inizializzaProposizioneAutLotto(tipo,
				//35639 fine
				getNumeroDocumento(),
				getAnnoDocumento(),
				getTestata().getDataDocumento(),
				getNumeroRigaDocumento(),
				null,
				getIdArticolo(),
				getIdVersioneSal(),
				getIdEsternoConfig(),
				idMagazzino,
				getIdCommessa(),
				getIdClientePerLotti(idMagazzino),//getIdFornitore(), //Fix 13831
				//PersDatiMagazzino.CREA_DA_DOCUMENTO,
				ambito,
				lottiOrig,
				lottiOrdine,
				null, //...Se alla data passo null allora considero la data corrente
				qta);
		pal.setQtaAttesaEntrataDisp(isQtaAttesaEntrataDisp()); //Fix 19215 AYM
		caricaLottiGiaAssegnati(pal); //35639
		lottiAuto = pal.proponiLottiAutomatici();
		pal.setSaldiLottiProposati(lottiAuto); //35639
	}
	else {
		lottiAuto = pal.getSaldiLottiProposati();
	}
	//35639 fine   

    BigDecimal controlloQta = qta;
    getRigheLotto().clear();
    HashMap giacenzaResiduaSulLotti = new HashMap(); //35639

    //...Se è stato creato un lotto automatico genero una riga lotto con quel lotto
    if(lottiAuto != null && !lottiAuto.isEmpty()) {
      List lottiPropostiDaOrdine = new ArrayList();	//35639
      for (int j = 0; j < lottiAuto.size(); j++) {
        LottiSaldi lt = (LottiSaldi)lottiAuto.get(j);
        //35639 inizio
        OrdineVenditaRigaLotto rigaOrdineLotto = getCorrispondanteRigaOrdLotto(lt);
        if(rigaOrdineLotto == null && !lottiPropostiDaOrdine.isEmpty())
        	controlloQta = completaQtaDaLottiDisponibile(lottiPropostiDaOrdine, giacenzaResiduaSulLotti, controlloQta);
        else if (rigaOrdineLotto != null)
        	lottiPropostiDaOrdine.add(lt);
        
        if(controlloQta.compareTo(ZERO_DEC) <= 0)
        	break;
        //35639 fine
        
        DocumentoVenRigaLottoSec lotto = (DocumentoVenRigaLottoSec)Factory.createObject(DocumentoVenRigaLottoSec.class);
        lotto.setFather(this);
        lotto.setIdArticolo(lt.getIdArticolo());
        lotto.setIdLotto(lt.getIdLotto());
        //BigDecimal qtaLotto = pal.calcolaQtaDisponibileLotto(tipo, lt, !lottiOrig.isEmpty(), lottiOrdine); //Fix 19215
        //BigDecimal qtaLotto = pal.calcolaQtaDisponibileLotto(tipo, lt, !lottiOrig.isEmpty(), lottiOrdine, isQtaAttesaEntrataDisp()); //Fix 19215 //Fix 20304 AYM
        
        BigDecimal qtaLotto =  pal.calcolaQtaGiacenzaNetta(tipo, lt, !lottiOrig.isEmpty(), lottiOrdine); //Fix 20304 AYM
        //Fix 17984 - inizio
        qtaLotto = getQtaLottoCorretta(controlloQta, qtaLotto, lottiAuto);
        //Fix 17984 - fine
        
        //35639 inizio      
        BigDecimal qtaLottoAssegnato = getQtaLottoGiaAssegnato(lt);  
        qtaLotto = qtaLotto.subtract(qtaLottoAssegnato);
        
        if(qtaLotto.compareTo(ZERO_DEC) <= 0)
        	continue;
        //35639 fine  

        BigDecimal qtaRigaOrdineLotto = getQuantitaResiduoOrdineLotto(lt); //35639
        if(controlloQta.compareTo(qtaLotto) >= 0) {
        	//35639 inizio
        	BigDecimal qtaDaUsare = qtaLotto;
        	if(qtaRigaOrdineLotto != null)
        		qtaDaUsare = (qtaRigaOrdineLotto.compareTo(qtaLotto) > 0) ? qtaLotto : qtaRigaOrdineLotto;
        	//35639 fine
        	
          controlloQta = controlloQta.subtract(qtaDaUsare);
          if(isPropostaEva) {
//Fix 03688 PM Inizio
/*            getQtaPropostaEvasione().setQuantitaInUMPrm(qtaLotto);
            getQtaPropostaEvasione().setQuantitaInUMRif(calcolaQtaUmRif(qtaLotto));
            getQtaPropostaEvasione().setQuantitaInUMSec(calcolaQtaUmSec(qtaLotto));
            lotto.setQtaPropostaEvasione(getQtaPropostaEvasione());
*/
             //Fix 17984 inizio
            //lotto.getQtaPropostaEvasione().setQuantitaInUMPrm(qtaLotto);
            //lotto.getQtaPropostaEvasione().setQuantitaInUMRif(calcolaQtaUmRif(qtaLotto));
            //lotto.getQtaPropostaEvasione().setQuantitaInUMSec(calcolaQtaUmSec(qtaLotto));
            impostaQtaLotto(lotto.getQtaPropostaEvasione(), qtaDaUsare);
            //Fix 17984 fine
//Fix 03688 PM Fine
          }
          else {
//Fix 03688 PM Inizio
/*            getQtaAttesaEvasione().setQuantitaInUMPrm(qtaLotto);
            getQtaAttesaEvasione().setQuantitaInUMRif(calcolaQtaUmRif(qtaLotto));
            getQtaAttesaEvasione().setQuantitaInUMSec(calcolaQtaUmSec(qtaLotto));
            lotto.setQtaAttesaEvasione(getQtaAttesaEvasione());
*/
            //Fix 17984 inizio
            //lotto.getQtaAttesaEvasione().setQuantitaInUMPrm(qtaLotto);
            //lotto.getQtaAttesaEvasione().setQuantitaInUMRif(calcolaQtaUmRif(qtaLotto));
            //lotto.getQtaAttesaEvasione().setQuantitaInUMSec(calcolaQtaUmSec(qtaLotto));
            impostaQtaLotto(lotto.getQtaAttesaEvasione(), qtaDaUsare);
            //Fix 17984 fine
//Fix 03688 PM Fine
          }
          assegnaQtaLotto(lt, qtaDaUsare); //35639
          giacenzaResiduaSulLotti.put(lt, qtaLotto.subtract(qtaDaUsare)); //35639 
          
        }
        else {
        	//35639 inizio
        	BigDecimal qtaDaUsare = controlloQta;
        	if(qtaRigaOrdineLotto != null)
        		qtaDaUsare = (qtaRigaOrdineLotto.compareTo(controlloQta) > 0) ? controlloQta : qtaRigaOrdineLotto;
        	//35639 fine
          if(isPropostaEva) {
//Fix 03688 PM Inizio
/*            getQtaPropostaEvasione().setQuantitaInUMPrm(controlloQta);
            getQtaPropostaEvasione().setQuantitaInUMRif(calcolaQtaUmRif(controlloQta));
            getQtaPropostaEvasione().setQuantitaInUMSec(calcolaQtaUmSec(controlloQta));
            lotto.setQtaPropostaEvasione(getQtaPropostaEvasione());
*/
            //Fix 17984 inizio
            //lotto.getQtaPropostaEvasione().setQuantitaInUMPrm(controlloQta);
            //lotto.getQtaPropostaEvasione().setQuantitaInUMRif(calcolaQtaUmRif(controlloQta));
            //lotto.getQtaPropostaEvasione().setQuantitaInUMSec(calcolaQtaUmSec(controlloQta));
            impostaQtaLotto(lotto.getQtaPropostaEvasione(), qtaDaUsare);
            //Fix 17984 fine
//Fix 03688 PM Fine

          }
          else {
//Fix 03688 PM Inizio
/*
            getQtaAttesaEvasione().setQuantitaInUMPrm(controlloQta);
            getQtaAttesaEvasione().setQuantitaInUMRif(calcolaQtaUmRif(controlloQta));
            getQtaAttesaEvasione().setQuantitaInUMSec(calcolaQtaUmSec(controlloQta));
            lotto.setQtaAttesaEvasione(getQtaAttesaEvasione());
*/
            //Fix 17984 inizio
            //lotto.getQtaAttesaEvasione().setQuantitaInUMPrm(controlloQta);
            //lotto.getQtaAttesaEvasione().setQuantitaInUMRif(calcolaQtaUmRif(controlloQta));
            //lotto.getQtaAttesaEvasione().setQuantitaInUMSec(calcolaQtaUmSec(controlloQta));
            impostaQtaLotto(lotto.getQtaAttesaEvasione(), qtaDaUsare);
            //Fix 17984 fine
//Fix 03688 PM Fine
          }
          assegnaQtaLotto(lt, qtaDaUsare); //35639
          giacenzaResiduaSulLotti.put(lt, qtaLotto.subtract(qtaDaUsare)); //35639 
          controlloQta = controlloQta.subtract(qtaDaUsare);//35639 

        }
        getRigheLotto().add(lotto);
        //35639 inizio
        if(controlloQta.compareTo(ZERO_DEC) <= 0)
        	break;
        //35639 fine        
      }
      //35639 inizio
      if (controlloQta.compareTo(ZERO_DEC)>0) {
      	controlloQta = completaQtaDaLottiDisponibile(lottiPropostiDaOrdine, giacenzaResiduaSulLotti, controlloQta);
      }
      //35639 fine
    }
    // Inizio 5350
    else{
      //if (getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO){//Fix 27337
    	if (getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO && !isControlloLottoDummyDaEscludere()){//Fix 27337
      	throw new ThipRuntimeException(new ErrorMessage("THIP_BS213",false));
      }
    }
    // Fine 5350
    setProposizioneAutLotto(null); //38908
  }
  //...FIX 3187 fine

  //Fix 3230 - inizio
  /**
   * Ridefinizione.
   */
  protected DocumentoOrdineRiga getRigaDestinazionePerCopia() {
    return (DocumentoVenRigaSec)Factory.createObject(DocumentoVenRigaSec.class);
  }
  //Fix 3230 - fine


  //Fix 3929 - inizio
  /**
   * Ridefinizione
   */
  protected void calcolaDatiVendita(DocumentoVendita testata) throws SQLException {
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
  //Fix 3929 - fine


  //Fix 5110 - inizio
  /**
   * Ridefinizione
   */
  protected void setRigaPrimariaPerCopia(DocumentoOrdineRiga rigaPrm) {
    setRigaPrimaria((DocumentoVenRigaPrm)rigaPrm);
  }
  //Fix 5110 - fine


  //Fix 6439 - inizio
  /**
   * Ridefinizione
   */
  public ErrorMessage checkIdBene() {
  	return null;
  }


  /**
   * Ridefinizione
   */
  protected List convalidaNlgSrvDDTOp1(List errorList) throws SQLException {
  	return errorList;
  }


  /**
   * Ridefinizione
   */
  protected List convalidaNlgSrvDDTOp2(List errorList) throws SQLException {
  	DocumentoVendita testata = (DocumentoVendita)getTestata();
  	ClienteVendita cliente = testata.getCliente();
  	Indirizzo indirizzo = testata.getIndirizzo();
  	DocumentoVenRigaPrm rigaPrm = getRigaPrimaria();

  	//Crea nuova locazione ATTUALE
  	BeneLocazione loc = (BeneLocazione)Factory.createObject(BeneLocazione.class);
  	loc.setBeneAnagr(getBene());
  	loc.setTipoLocazione(indirizzo == null ? BeneLocazione.CLIENTE : BeneLocazione.CLIENTE_IND);
  	loc.setDataInizioLocaz(rigaPrm.getDataInizioAttivContratto());
  	loc.setOraInizioLocaz(rigaPrm.getOraInizioAttivContratto());
  	loc.setStatoLocazione(BeneLocazione.INIZIO_LOCAZ);
  	loc.setCliente(cliente);
  	if (indirizzo != null) {
  		loc.setIndirizzoCliente(indirizzo);
  	}

  	//Chiude la vecchia locazione ATTUALE
  	int rc = 0;

  	BeneLocazione locAtt = loc.getBeneLocazioneAttuale();
  	if (locAtt != null) {
	  	locAtt.setDataFineLocaz(rigaPrm.getDataInizioAttivContratto());
	  	locAtt.setOraFineLocaz(rigaPrm.getOraInizioAttivContratto());
	  	locAtt.setStatoLocazione(BeneLocazione.STORICO);
	  	rc = locAtt.save();
  	}
  	if (rc < 0) {
  		errorList.add(new ErrorMessage("THIP_BS108", new String[]{getKey()}));
  	}
  	else {
	  	rc = loc.save();
	  	if (rc < 0) {
	  		errorList.add(new ErrorMessage("THIP_BS108", new String[]{getKey()}));
	  	}
  	}

	  return errorList;
  }


  /**
   * Ridefinizione
   */
  protected List convalidaNlgSrvDDTOp3(List errorList) throws SQLException {
  	return errorList;
  }


  /**
   * Ridefinizione
   */
  protected List convalidaNlgSrvBEMOp1(List errorList) throws SQLException {
  	return errorList;
  }


  /**
   * Ridefinizione
   */
  protected List convalidaNlgSrvBEMOp2(List errorList) throws SQLException {
  	DocumentoVenRigaPrm rigaPrm = getRigaPrimaria();

  	//Crea la nuova locazione di tipo AZIENDA e in stato ATTUALE
  	BeneLocazione loc = (BeneLocazione)Factory.createObject(BeneLocazione.class);
  	loc.setBeneAnagr(getBene());
  	loc.setStatoLocazione(BeneLocazione.ATTUALE);
  	loc.setDataInizioLocaz(rigaPrm.getDataFineAttivContratto());
  	loc.setOraInizioLocaz(rigaPrm.getOraFineAttivContratto());
  	loc.setStatoLocazione(BeneLocazione.INIZIO_LOCAZ);
  	loc.setTipoLocazione(BeneLocazione.AZIENDA);
  	loc.setSede(getOrdineServizio().getStabilimento().getSede());

  	//Chiude la vecchia locazione ATTUALE
  	int rc = 0;

  	BeneLocazione locAtt = loc.getBeneLocazioneAttuale();
  	if (locAtt != null) {
	  	locAtt.setDataFineLocaz(rigaPrm.getDataFineAttivContratto());
	  	locAtt.setOraFineLocaz(rigaPrm.getOraFineAttivContratto());
	  	locAtt.setStatoLocazione(BeneLocazione.STORICO);
	  	rc = locAtt.save();
  	}
  	if (rc < 0) {
  		errorList.add(new ErrorMessage("THIP_BS108", new String[]{getKey()}));
  	}
  	else {
	  	rc = loc.save();
	  	if (rc < 0) {
	  		errorList.add(new ErrorMessage("THIP_BS108", new String[]{getKey()}));
	  	}
  	}

  	return errorList;
  }



  /**
   * Ridefinizione
   */
  protected List convalidaNlgSrvBEMOp3(List errorList) throws SQLException {
  	return errorList;
  }


  /**
   * Ridefinizione
   */
  protected int regressioneNlgSrvDDTOp1() throws SQLException {
  	return 0;
  }


  /**
   * Ridefinizione
   */
  protected int regressioneNlgSrvDDTOp3() throws SQLException {
  	return 0;
  }


  /**
   * Ridefinizione
   */
  protected int regressioneNlgSrvBEMOp1() throws SQLException {
  	return 0;
  }


  /**
   * Ridefinizione
   */
  protected int regressioneNlgSrvBEMOp3() throws SQLException {
  	return 0;
  }
  //Fix 6439 - fine
// Fix 07779 inizio
  public ErrorMessage checkIdAssogIVA() {
    if (!isNonFatturare() && (getIdAssogIVA() == null))
      return new ErrorMessage("BAS0000000");
    return null;
  }

// Fix 07779 fine

  //Fix 8707 - inizio
  /**
   * Richiamato in fase di cancellazione interattiva (solo da GUI) della riga.
   * Se associati alla riga esistono dei movimenti di storico matricole che
   * fanno riferimento a determinate matricole, queste ultime devono essere
   * 'regredite' in stato da A MAGAZZINO a DEFINITA e si deve eliminare in esse
   * qualsiasi riferimento a questo documento.
   * Le movimentazioni di storico devono essere eliminate.
   * @see Metodo deleteObject della classe DocumentoVenditaRigaSecGridActionAdapter
   */
  public static synchronized ErrorMessage regressioneMatricole(DocumentoVenRigaSec riga) throws SQLException {
  	ErrorMessage em = null;

  	if (riga.getArticolo().isArticoloMatric()) {

	  	List lotti = riga.getRigheLotto();
	  	Iterator iterLotti = lotti.iterator();
	  	while (em == null && iterLotti.hasNext()) {
	  		DocumentoVenRigaLottoSec lotto = (DocumentoVenRigaLottoSec)iterLotti.next();
	  		List movStorMat =
	  			StoricoMatricola.getMovimentiStoricoMatricolaRigaLotto(
	  				lotto.getKey(), true
	  			);
	  		em = DocumentoVenditaRiga.regressioneMatricole(riga, movStorMat);
	  	}

	  	if (em == null || em.getSeverity() == ErrorMessage.WARNING) {
	  		em = new ErrorMessage("THIP300044", new String[] {riga.getKey(), "A MAGAZZINO"});
	  	}
  	}

  	return em;
  }
  //Fix 8707 - fine

  // fix 10987
  // fix Fix 10987
  public boolean effettuareIlControllo(List lista){
    return effettuareIlControllo(lista, null);
  }

  public boolean effettuareIlControllo(List lista, QuantitaInUM qta){
    boolean ritorno = false;
    //ho messo l'and tra l'aggiornametno saldi e il moviemtno di magazzino perchè
    // ad esempio se la riga è merce a valore non deve essere effettuato il controllo
    // è solo la and tra le due che mi può garantire, se le cose sono fatte bene che
    // potenzialmente la causale mov di magazzino prevederà un aggiornamento della giacenza
    // ma non nei casi speciali
    //Fix 30193 Inizio
    boolean controlla = true ;
    if(this.isConfigurazioneNeutra()) {
  	  controlla = false ;
    }
    if(controlla && this.isDaAggiornare(qta)) {
    //Fix 30193 Fine
    OggCalcoloGiaDisp ogg = null;
    // fix 11123
    //if (this.isAbilitatoAggiornamentoSaldi() && this.isAbilitatoMovimentiMagazzino()) {
    if (this.isDaAggiornare(qta)&& this.isAbilitatoAggiornamentoSaldi() && this.isAbilitatoMovimentiMagazzino()) {
    // fine fix 11123
      CausaleRigaDocVen cau = this.getCausaleRiga();
      boolean rigaSpesa = cau.getTipoRiga() == TipoRiga.SPESE_MOV_VALORE;
      Articolo articoloPrm = this.getRigaPrimaria().getArticolo();
      boolean kit = false;
      if (articoloPrm != null) {
        kit = articoloPrm.getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST;
      }
      if (kit && !rigaSpesa) {
        this.getRigaPrimaria().getArticolo().getTipoParte();
        Articolo articolo = this.getArticolo();
        char tipoArticolo = articolo.getTipoArticolo();
        boolean artContenitore =
            tipoArticolo == Articolo.ART_CONTENITORE ||
            tipoArticolo == Articolo.CONTENITORE_TERZI;
        char azione = cau.getAzioneMagazzino();
        if (artContenitore) {
          if (azione == AzioneMagazzino.USCITA && cau.getCauMagContenitori1() != null) {
            ritorno = true;
            ogg = (OggCalcoloGiaDisp) Factory.createObject(OggCalcoloGiaDisp.class);
            ogg.caricati(this, null, qta);
            //Fix 33719 Inizio
            if (this.getArticolo()!=null && this.getArticolo().isArticLotto() && ogg.getIdLotto() != null && ogg.getIdLotto().equals(Lotto.LOTTO_DUMMY)){
            	ogg.setIdLotto(null);
            }
            //Fix 33719 Fine
            ogg.setIdCliente(this.getMagazzino().getIdCodiceClienteAzienda());
            ogg.setTipoControllo(OggCalcoloGiaDisp.TP_CTL_GIACENZA);
          }
          // fix 11239
          //else if (azione == AzioneMagazzino.ENTRATA &&
          //         cau.getCauMagContenitori2() != null) {
          //  ritorno = true;
          //}
          // fine fix 11239
        }
        else if (azione == AzioneMagazzino.USCITA) {
          ritorno = true;
        }
      }
      if (ritorno) {
        if (ogg == null) {
          ogg = (OggCalcoloGiaDisp) Factory.createObject(OggCalcoloGiaDisp.class);
          ogg.caricati(this, null, qta);
          //Fix 33719 Inizio
          if (this.getArticolo()!=null && this.getArticolo().isArticLotto() && ogg.getIdLotto() != null && ogg.getIdLotto().equals(Lotto.LOTTO_DUMMY)){
          	ogg.setIdLotto(null);
          }
          //Fix 33719 Fine
          ogg.setTipoControllo(OggCalcoloGiaDisp.TP_CTL_GIACENZA);
        }
        lista.add(ogg);
      }
    }
    }//Fix 30193 
    return ritorno;
  }
  // fine fix 10987
  // fix 11123
  // Se la qta è valorizzata vuol dire che la chiamata viene dalla riga primaria
  // e se chiama il controllo sulle righe secondarie è perchè è in update
  // e sono cambiati dei dati, che poi aggiornerà sulla riga secondaria, per i
  // quali deve essere rinnovato il controllo sulla riga secondaria.
  public boolean isDaAggiornare(QuantitaInUM qta){
    if (qta==null)
      return super.isDaAggiornare();
    else
      return true;
  }
  // fine fix 11123


  //Fix 12437 inizio
  public ErrorMessage checkDelete() {
    if (this.getAttivaCheckCancellazione())
    {
      DocumentoVendita docVenTestata = ((DocumentoVendita)this.getTestata());
      if(docVenTestata.getTipoDocumento() == it.thera.thip.vendite.generaleVE.TipoDocumento.DOCUM_DA_NON_FATTURARE
         && (docVenTestata.getTipoBolla() == TipoBolla.BEM || ((DocumentoVendita)this.getTestata()).getTipoBolla() == TipoBolla.DDT)
         && (getCausaleRiga().getAzioneMagazzino() == AzioneMagazzino.USCITA || getCausaleRiga().getAzioneMagazzino() == AzioneMagazzino.ENTRATA)
         && getRigaPrimaria().getCausaleRiga().getTipoRiga() == TipoRigaDocumentoVendita.SERVIZIO_ASSIST_MANUT
         && this.getTipoRiga() == TipoRiga.MERCE)
      {
        if(getRigaPrimaria().getCollegatoAMagazzino()== StatoAttivita.ESEGUITO || getRigaPrimaria().getCollegatoAMagazzino()== StatoAttivita.NON_RICHIESTO) {
          return new ErrorMessage("THIP_BS162",new String[]{this.getKey()});
        }
        this.setAttivaCheckCancellazione(false);
      }
    }
    return super.checkDelete();
  }
  //Fix 12437 fine

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
        QuantitaInUMRif srvQta = getServizioQta();
        BigDecimal[] pesiEVolume = Articolo.getPesiEVolumeTotali(getArticolo(),
                                                              srvQta.getQuantitaInUMRif(), srvQta.getQuantitaInUMPrm(), srvQta.getQuantitaInUMSec(),
                                                              getUMRif(), getUMPrm(), getUMSec());
        setPesoNetto(pesiEVolume[0]);
        setPesoLordo(pesiEVolume[1]);
        setVolume(pesiEVolume[2]);
        */
        //Fix 14931 fine
        //System.out.println("DOC_SEC:calcolaPesiEVolume " + pesiEVolume[0]);
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
     DocumentoVenRigaPrm rigaPrm = getRigaPrimaria();
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
        DocumentoDocRiga oldRiga = (DocumentoDocRiga)getOldRiga();
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
        //System.out.println("DOC_AGGRIGA:aggiornaPesiEVolumeRigaPrm " + pesiEVolume[0]);
     }
     */
     //Fix 14931 fine
  }
  //Fix 12508 fine

  //Fix 14459 inizio
  protected List convalidaNlgSrvDDTOp4(List errorList) throws SQLException {
    return errorList;
  }

  protected List convalidaNlgSrvBEMOp4(List errorList) throws SQLException {
    return errorList;
  }

  protected int regressioneNlgSrvBEMOp4() throws SQLException {
    return 0;
  }

  protected int regressioneNlgSrvDDTOp4() throws SQLException {
    return 0;
  }
  //Fix 14459 fine
  //Fix 16032 inizio
  public  boolean identificazioneAutomaticaND(Articolo art) {
    if (!getRigheLotto().isEmpty())
     return false;
    if (!art.isArticoloMatric())
     return false;
    PersDatiMagazzino pdm = PersDatiMagazzino.getCurrentPersDatiMagazzino();
    ArticoloDatiMagaz artDatiMag = art.getArticoloDatiMagaz();
    boolean isCodAutLotAcq= artDatiMag.getCodAutLotAcq()!=ArticoloDatiMagaz.DEFAULT ? artDatiMag.getCodAutLotAcq()==ArticoloDatiMagaz.ND
                                        :pdm.getCodAutLotAcq()==ArticoloDatiMagaz.ND;
    boolean isCodAutLotCl= artDatiMag.getCodAutLotCl()!=ArticoloDatiMagaz.DEFAULT ? artDatiMag.getCodAutLotCl()==ArticoloDatiMagaz.ND
                                        :pdm.getCodAutLotCl()==ArticoloDatiMagaz.ND;
    boolean isCodAutLotProd= artDatiMag.getCodAutLotProd()!=ArticoloDatiMagaz.DEFAULT ? artDatiMag.getCodAutLotProd()==ArticoloDatiMagaz.ND
                                        :pdm.getCodAutLotProd()==ArticoloDatiMagaz.ND;

   if (isCodAutLotAcq ||isCodAutLotCl||isCodAutLotProd)
     return true;

   return false;
 }
 public void creaLottoND(){


   QuantitaInUMRif qtaProp = new QuantitaInUMRif();
   QuantitaInUMRif qtaAttesa =new QuantitaInUMRif();
   qtaProp.azzera();
   qtaAttesa.azzera();
   if(getStatoAvanzamento() == StatoAvanzamento.PROVVISORIO &&getQtaPropostaEvasione() != null)
   {
         qtaProp = getQtaPropostaEvasione();
   }
   if(getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO && getQtaAttesaEvasione() != null)
   {
     qtaAttesa = getQtaAttesaEvasione();

   }

   DocumentoVenRigaLottoSec lotto = (DocumentoVenRigaLottoSec)Factory.createObject(DocumentoVenRigaLottoSec.class);
   lotto.setFather(this);
   lotto.setIdAzienda(getIdAzienda());
   lotto.setIdArticolo(this.getIdArticolo());
   lotto.setIdLotto(Lotto.LOTTO_ND);
   lotto.setQtaAttesaEvasione(lotto.getQtaAttesaEvasione().add(qtaAttesa));
   lotto.setQtaPropostaEvasione(lotto.getQtaPropostaEvasione().add(qtaProp));
   getRigheLotto().add(lotto);


 }
//Fix 16032 fine

 //Fix 18309 Inizio
 public ErrorMessage checkPresenzaLottoDummy(){
   if(getRigaPrimaria() != null && getRigaPrimaria().getArticolo() != null &&
      getRigaPrimaria().getArticolo().getTipoParte() == ArticoloDatiIdent.KIT_GEST){
     return null;
   }
   else{
     return super.checkPresenzaLottoDummy();
   }
 }
 //Fix 18309 Fine

//Fix 17984 - inizio
 protected BigDecimal getQtaLottoCorretta(BigDecimal qtaRiga, BigDecimal qtaLotto, List lottiAuto) {
   return qtaLotto;
 }

 protected void impostaQtaLotto(QuantitaInUMRif q, BigDecimal qtaDaImpostare) {
  q.setQuantitaInUMPrm(qtaDaImpostare);
  q.setQuantitaInUMRif(calcolaQtaUmRif(qtaDaImpostare));
  q.setQuantitaInUMSec(calcolaQtaUmSec(qtaDaImpostare));
 }
 //Fix 17984 - fine

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
     return Entity.elementWithKey("DocVenRigaSec", Entity.NO_LOCK);
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

     if (this instanceof DocEvaVenRigaSec)
       if (newRow)
         return salvaDettRigaConfDaEvasione();
       else
         return 0;

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

 protected int salvaDettRigaConfDaEvasione() throws SQLException {
   int ret = 0;
   if (getRigaOrdine() != null) {
     DettRigaConfigurazione dettRigaCfg = (DettRigaConfigurazioneOrdVen) Factory.createObject(DettRigaConfigurazioneOrdVen.class);
     String ordRigaKey = getRigaOrdine().getKey();
     ret = dettRigaCfg.copiaDettRigaCfgDaEvasione(this, ordRigaKey);
   }
   return ret;
 }

 public boolean isListinoCambiato() {
   DocumentoVenditaRiga dvr = (DocumentoVenditaRiga) iOldRiga;
   if (dvr == null)
     return false;
   //return!(this.getIdListino().equals(dvr.getIdListino()));//Fix 24705
   return!(Utils.compare(this.getIdListino(),dvr.getIdListino()) == 0);//Fix 24705
 }

 public DettRigaConfigurazione dammiOggettoGestione() {
   DettRigaConfigurazioneDocVen dett = (DettRigaConfigurazioneDocVen) Factory.createObject(DettRigaConfigurazioneDocVen.class);
   return dett;
 }

 public Object getOggettoTestata() {
   return getTestata();
 }

 public ListinoVendita getListino() {
   return getListinoPrezzi();
 }

  public BigDecimal getQtaInUMRif() {
    return getQtaInUMVen();
  }
  //Fix 24613 fine
  
  // Fix 25818 inizio
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
  // Fix 25818 fine
  
  //Fix 26807 inizio
  public int deleteOwnedRigheDdt(){
 	int retDdtRig = 0;
 	String where = DdtRigTM.ID_AZIENDA + "='" + getIdAzienda() + "' AND " + DdtRigTM.ID_ANNO_DOC_VEN + "='" + getAnnoDocumento() + "' AND " + 
 	               DdtRigTM.ID_NUMERO_DOC_VEN + "='" + getNumeroDocumento() + "' AND " +
 		           DdtRigTM.ID_RIGA_DOC_VEN + "=" + getNumeroRigaDocumento() + " AND " + DdtRigTM.ID_DET_RIGA_DOCVEN + "=" + getDettaglioRigaDocumento(); 
 	try {
 		List l = DdtRigSec.retrieveList(where, "", true);
 		if(l != null && !l.isEmpty()){
 			DdtRigSec ddtRigSec = (DdtRigSec)l.get(0);
 			retDdtRig = ddtRigSec.delete();
 		}
 	} catch (Exception e) {
 		e.printStackTrace();
 	}
 	return retDdtRig;
  }
  //Fix 26807 fine
  //Fix 27337 inizio
  public boolean isControlloLottoDummyDaEscludere(){
	   DocumentoVendita doc = (DocumentoVendita)getTestata();	
	   //42148
	   /*
	   if(doc.getCausale().getTipoDocumento() != CausaleDocumentoVendita.NOTA_ACCREDITO)
		   return false;
	   */
	   //42148
	   if(!getArticolo().isArticLotto())
	   	   return false;
	   Articolo articoloPrm = this.getRigaPrimaria().getArticolo();
	   if (articoloPrm != null && articoloPrm.getTipoParte() != ArticoloDatiIdent.KIT_NON_GEST)
		   return true;
	   if(isAbilitatoMovimentiMagazzino() && isAbilitatoAggiornamentoSaldi())
		   return false;
	   return true;
  }
  //Fix 27337 fine
  

//27649 inizio
public ErrorMessage checkQtaInUMPrmMag() {
	//if((getIdUMPrm() != null && getUMPrm().getQtaIntera()) || (getArticolo() != null && getArticolo().getArticoloDatiMagaz().isQtaIntera())) { Fix 29396
	if((getIdUMPrm() != null && getUMPrm() != null && getUMPrm().getQtaIntera()) || (getArticolo() != null && getArticolo().getArticoloDatiMagaz().isQtaIntera())) { //Fix 29396	
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
	//if(getIdUMRif() != null && getUMRif().getQtaIntera()) { Fix 29396
	if(getIdUMRif() != null && getUMRif() != null && getUMRif().getQtaIntera()) { //Fix 29396
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
	//if(getIdUMSec() != null && getUMSec().getQtaIntera()) { Fix 29396
	if(getIdUMSec() != null && getUMSec() != null  && getUMSec().getQtaIntera()) { //Fix 29396
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
	//Fix 28189 Inizio
	public ErrorMessage checkPerMassUpdate() {
		  String attributeName = getAdNameForMassUpdate();
		  if (attributeName == null || attributeName.equals(""))
			  return null;
		  
		  ErrorMessage e  = checkDataConsegna();
		  if (e != null)
			  return e;
		  
		return super.checkPerMassUpdate() ;
	}
	
	public ErrorMessage checkDataConsegna() {
		  String attributeName = getAdNameForMassUpdate();
		  if (attributeName == null || attributeName.equals(""))
			  return null;
		  
		  if(attributeName.equals("RigDataConsConf") || attributeName.equals("RigDataConsRich"))
			  return new ErrorMessage("THIP_TN554") ;
		  
		  return null ;
	}
	//Fix 28189 Fine
	
	//29240 inizio
  public boolean isDaAggiornaQtaPortafoglioDopoEvasione() {
  	if(!(isSalvaRigaPrimaria() && (getCausaleRiga().getAzioneMagazzino() == AzioneMagazzino.NESSUNA_AZIONE || getCausaleRiga().isRigaMerceAValore())))
  		return false;
  	return super.isDaAggiornaQtaPortafoglioDopoEvasione();
  }
	//29240 fine
  //Fix 37244 inizio
  protected void copiaRigaCompletaBO(DocumentoOrdineRiga riga,SpecificheCopiaDocumento spec) {
 	 BigDecimal prcScontoIntestatario= riga.getPrcScontoIntestatario();
 	 BigDecimal prcScontoModalita=riga.getPrcScontoModalita();
 	 Sconto scontoModalita= riga.getScontoModalita();
 	 
 	 Agente agente = ((DocumentoVenditaRiga) riga).getAgente();
 	 BigDecimal provvigione1Agente=((DocumentoVenditaRiga)riga).getProvvigione1Agente();

 	 Agente subagente= ((DocumentoVenditaRiga) riga).getSubagente();
 	 BigDecimal provvigione1Subagente=((DocumentoVenditaRiga) riga).getProvvigione1Subagente();
 	 boolean differenzaPrezzoAgente = ((DocumentoVenditaRiga) riga).hasDifferenzaPrezzoAgente();
 	 boolean differenzaPrezzoSubagente = ((DocumentoVenditaRiga) riga).hasDifferenzaPrezzoSubagente();

 	  copiaRigaCompletaBO( riga);
 	  
 	  if (spec.getCondizTestataDocumento() == SpecificheCopiaDocumento.CTD_DA_DOCUMENTO)
 	        {

 			  riga.setPrcScontoIntestatario(prcScontoIntestatario);
 			  riga.setPrcScontoModalita(prcScontoModalita);
 			  riga.setScontoModalita(scontoModalita);
 			  ((DocumentoVenditaRiga) riga).setAgente(agente);
 			  ((DocumentoVenditaRiga) riga).setProvvigione1Agente(provvigione1Agente);
 			  ((DocumentoVenditaRiga) riga).setProvvigione1Subagente(provvigione1Subagente);
 			  ((DocumentoVenditaRiga) riga).setSubagente(subagente);
 			  ((DocumentoVenditaRiga) riga).setDifferenzaPrezzoAgente(differenzaPrezzoAgente);
 			  ((DocumentoVenditaRiga) riga).setDifferenzaPrezzoSubagente(differenzaPrezzoSubagente);
 		     }
 	  //Fix 37556 inizio	  
 	  else 
 	  {
 		  DocumentoVenditaRiga rigaPrm = getRigaPrimaria();
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
				  ((DettRigaConfigurazioneDocVen) dettRigaCfg).recuperaDettRigaConfigPrezzo(this); 
			  }
		  }
	  }
  }  
  //44784 fine
}
