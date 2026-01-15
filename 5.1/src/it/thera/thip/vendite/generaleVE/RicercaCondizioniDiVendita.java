package it.thera.thip.vendite.generaleVE;

import java.math.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;

import com.thera.thermfw.base.*;
import com.thera.thermfw.cbs.*;
import com.thera.thermfw.common.BusinessObject;
import com.thera.thermfw.persist.*;
import com.thera.thermfw.type.*;

import it.thera.thip.base.agentiProvv.*;
import it.thera.thip.base.articolo.*;
import it.thera.thip.base.azienda.*;
import it.thera.thip.base.cliente.*;
import it.thera.thip.base.comuniVenAcq.*;
import it.thera.thip.base.generale.*;
import it.thera.thip.base.listini.*;
import it.thera.thip.base.partner.*;
import it.thera.thip.cs.DatiComuniEstesi;
import it.thera.thip.datiTecnici.configuratore.*;
import it.thera.thip.magazzino.documenti.*;

/**
 * RicercaCondizioniDiVendita
 *
 * <br></br><b>Copyright (C) : Thera S.p.A.</b>
 * @author Daniela Battistoni
 */
/*
 * Revision:
 * Number  Date         Owner   Description
 *         17/07/2003   DZ      Modificato ricercaCondizioniVendita per gestire il listinoCampagna sul listino corrente.
 *         23/07/2003   DZ      Aggiunti metodi calcolaPrezzoAlNettoSconti() e getSconti(), utilizzati in ricercaCondizioniVendita
 *                              per impostare sull'oggetto CondizioniDiVendita il prezzo al netto degli sconti.
 * 01126   28/11/2003   PM
 * 01296   20/01/2004   PM      La percentuale provvigione deve venire ricercata sulla tabella sconti
 *                              provvigioni anche se da anagrafico provvigioni, listini e articolo ho un valore
 *                              null a patto che sia abilitata la ricerca in pers dati vendita
 * 01340   28/01/2004   DB      Correzione metodo per anagrafico provvigioni.
 * 01504   24/02/2004   DB
 * 01621   15/03/2004   ME
 * 01760   01/04/2004   DB
 * 01761   01/04/2004   DB
 * 01797   08/04/2004   DB
 * 01948   11/05/2004   DZ      ricercaCondizioniVendita: tolto flag attivolistinoCampagna e
 *                              tolta gestione del listinoCampagna da PersDatiVen.
 *                              Aggiunto metodo ricercaCondizioniVendita senza il parametro attivoListinoCampagna.
 *                              Recuperato persDatiVen tramite opportuno metodo statico.
 * 01974   13/05/2004   DZ      Corretta fix 1948 (aggiunto test listino != null).
 * 01971   13/05/2004   DZ      In tutti gli statement aggiunta condizione di where su STATO VALIDO.
 * 01990   17/05/2004   DZ      Corretto STATEMENT_SCONTI, commentato persDatiVen.getListinoCampagna in
 *                                , corretto setAttivoListinoCampagna in ricercaCondAcq.
 * 02182   30/06/2004   DZ      Aggiunta configurazione nel processo di ricerca delle condizioni di vendita.
 *                              Spostati da RecuperaDatiVendita i metodi statici getCondizioniVendita.
 * 02261   22/07/2004   DZ      Aggiunta gestione delle autorizzazioni sui listini nella ricerca del prezzo.
 * 02343   27/08/2004   DB
 * 02341   27/08/2004   PM      Non veniva gestito correttamente il caso in cui l'articolo era null.
 * 02419   13/09/2004   DZ      ricercaCond...: cambiato nome variabile ricercaCampagna --> listinoIndividuato
 *                              individuaListino: sul listinoCampagna ora si prosegue la ricerca
 *                              sull'eventuale listino sconti associato.
 * 02456   20/09/2004   ME      Modificata gestione del campo tipoDataPrezziSconti
 *                              nel caso in cui il cliente sia null.
 * 02333   27/08/2004   DB
 * 02607   12/10/2004   DB      Quando agente o subAgente sono null non calcolo la provvigione
 * 02752   02/11/2004   DB
 * 02787   08/11/2004   DZ      Corretto individuaRiga per ricercaConConfigurazione.
 * 02806   11/11/2004   Crosa   Modificato metodo calcolaScontoProvvigioni()
 * 02880   23/11/2004   DZ      calcolaPrezzoAlNettoSconti: scale numDecUnitari anzichè numDec.
 * 02888   24/11/2004   MG      in calcolaPrezzi condizionato set Prezzo Extra al settaggio del flag corrispondente
                                in PersDatiVen
 * 02911   29/11/2004   DZ      calcolaSconti: aggiunto test che gli sconti sian diversi da null.
 * 03007   17/12/2004   DB
 * 03085   11/01/2005   DZ      individuaScaglione: valorizzato attributo AzzeraScontiCliFor su condVen.
 *                              calcolaPrezzi: impostato UMPrezzo a VENDITA se non viene trovato il prezzo.
 * 03197   25/01/2005   ME      Aggiunto metodo calcoloScontoDaScontiRiga
 * 03223   02/02/2005   ME      Istanze con Factory
 * 03674   27/04/2005   PJ      sincronizzazione metodo di ricerca condizioni di vendita
 * 03834   25/05/2005   MN      Nel metodo getCondizioniVendita() utilizzato il metodo
 *                              recuperaConfigurazione della classe ConfigurazioneRicEnh.
 * 03910   14/06/2005   ME      Metodo individuaListino: impostata quantità minima nelle
 *                              condizioni di vendita
 * 04003   23/06/2005   ME      Aggiunti due metodi getCondizioniVendita paralleli
 *                              a quelli già esistenti che nella loro segnatura
 *                              hanno in più il parametro rifDataPrezzoSconti,
 *                              in quanto questo parametro viene passato dalla
 *                              testata.
 *                              I vecchi metodi richiamano i metodi paralleli
 *                              passando come valore del parametro quello
 *                              recuperato da cliente o da pers. dati vendita
 * 04140   18/07/2005   ME      Modifica fix 4003: aggiunto un controllo su null
 * 04103   18/07/2005   DZ      Aggiunto valorizzaDettagliArtCfg per calcolo prezzi
 *                              con articoli "secondari" su configurazione.
 * 04343   19/09/2005   DZ      individuaListino: aggiunto test schemaCfg != null
 * 04348   20/09/2005   MG      implementazione gestione sconti di testata per calcolo provvigione agente su scala sconti
 * 04663   21/11/2005   DZ      valorizzaDettagliArtCfg: chiamata a getUMDefaultVendita anziché getUMPrimariaVendita.
 * 04745   06/12/2005   DZ      corretto calcolaPrezziSecondari per gestire sconti su articoli secondari.
 * 04632   21/11/2005   DZ      Aggiunto getQuantitaForScaglioni().
 * 04781   16/12/2005   PM      Resi protected i metodi individuaAgentiScontiProvv(...) e caricaMap(...),
 *                              per gestire la ridefinizione nella personalizzazione.
 * 05141   30/03/2006   DZ      calcolaPrezziSecondari: modifiche per gestire la "maggiorazione" sul lordo
 *                              necessaria per annullare gli sconti se 'azzeraScontiCliente'.
 * 05135   26/04/2006   DZ      calcolaPrezzi: la valorizzazione del prezzo dei dettagliCfg avviene anche se il prz
 *                              sull'articolo principale non è valorizzato.
 * 05330   10/04/2006   DBot    Utilizzo readOnlyElementWithKey per gestione corretta classi Cacheable
 * 05706   18/07/2006   ME      Aggiunto controllo su articolo nullo
 * 05765   28/07/2006   DZ      calcolaPrezziSecondari: aggiunta valorizzazione a 0 del prezzoBase se è null
 *                              altrimenti dava eccezione e non riportava in GUI della riga ordine/doc la provvigione
 * 05767   31/07/2006   EP      Aggiunto metodo isRicScontiProvv(String idAzienda) per calcolo provvigione
 * 06160   31/10/2006   DZ      calcolaPrezziSecondari: aggiunto test che dettaglio.getPrezzoAlNettoSconti
 *                              sia diverso da null
 * 06204   13/11/2006   MG      ricercaCondizioniDiVendita: recupero provv. agente anche nel caso in cui listino null
 *                              e valuta doc. vendita diversa da valuta aziendale
 * 06617   30/01/2007   LP      Reso protected alcuni metodi privati (per personalizzazioni)
 * 07024   29/03/2007   ME      Sistemato un controllo
 * 08858   25/03/2008   BML     calcolaScontoProvvigioni :rivolta il provigione agente se è valorizzata
 * 10149   26/11/2008   EP      Reso protetto il metodo caricaScontoProvvigioni(...)
 * 10533   30/03/2009   FM      update on case cliente null the method ricercaPerChiavi
 * 10750   23/04/2009   MG      corretto calcolaScontoProvvigioni con ripristino cond.provv.minima
                                oltre che quella prioritaria imposta dalla fix 8858
 * 11156   21/07/2009   DB
 * 11304   14/09/2009   GSCARTA
 * 11344   21/09/2009   GSCARTA
 * 11766   11/12/2009   GScarta correzione fix 11156 per formato numero imballo.
 * 11779   14/12/2009   GScarta migliorata gestione numeroImballo della fix 11156
 * 11951   15/01/2010   GScarta Riporto in 3.0.8 delle fix 11766 e 11779
 * 12321   09/03/2010   Mekki   valorizzaDettagliArtCfg : Introdurre il concetto di calcolo del prezzo attraverso macro
                                e poter indicare un coefficiente a livello di schema.
 * 12331   22/03/2010   GN      Modifica al metodo calcolaQuantita aggiunto dalla fix precedente
 * 12442   31/03/2010   GN      In calcolaPrezziSecondari corretto errore sul prezzoExtra
 * 12635   14/05/2010   LC      Aggiunto l'uso della Factory in vari punti
 * 13063   09/09/2010  Ichrak   aggiornare del STATEMENT_PROVV
 *                     Elouni
 * 13211   01/10/2010   OC      Aggiunto due paramatri umSecMag e qtaSecMag del metodo ricercaCondizioniDiVendita
 * 13589   30/11/2010  Amara    Aggiunto la parametro configurazione al metodo ricercaCondizioniDiVendita
 * 13515   13/12/2010  TF       calcolo provvigioni su prezzo extra
 * 12639   24/05/2010   GScarta Nuova gestione numero imballo. Completamento fix 12572.
 * 12767   07/06/2010   GScarta Gestione Anagrafica Provvigioni su Scelta
 * 12951   22/07/2010   GScarta Corretto reperimento listino scaglione sconti.
 * 13130   13/09/2010   GScarta Introdotta Factory
 * 14738   29/06/2011  DBot     Integrazione a standard fix 12639 12767 12951 13130
 * 15647   15/02/2012  TF       Quando un prezzo manca sul listino nel campo prezzo viene lasciato NULL
                                e quindi se è attiva l'obbligatorietà del prezzo viene segnalato errore.
 * 17534   05/10/2013  HBT      Considerare gli scaglioni di sconto relativi ad articoli legati a valori di variabili di configurazione.
 * 21767   01/06/2015  AA       Corretto la valore di tipo testata seguende la valori degli dettagli (anche il calcolo prezzo)
 * 22073   28/08/2015  Linda    Aggiungere alla Tipologia listino ricerca prezzi l'opzione "Articolo" da valorizzare nel caso in cui il prezzo venga identificato nella Ricerca prezzi dall'anagrafico articoli.
 * 22229   29/09/2015  Linda    Aggiungere gestione Provvigioni /scala sconti per agente/Cliente.
 * 23738   02/09/2014  PM       Nel cruscotto articolo quando vengono calcolate le condizioni di vendita non viene gestito correttamente il listino alternativo del cliente.
 * 24273   06/09/2016  LTB      Modifiche per agevolare le personalizzazioni.  
 * 24634   29/12/2016  Jackal   Aggiunti ganci per personalizzaioni in valorizzaDettagliArtCfg
 * 25185   15/03/2017  PM       Fix correzione della 24273
 * 25658   04/10/2017  GScarta  Aggiornamenti e adeguamento 4.6.x
 * 26981   14/02/2018  GScarta  Migliorie su gestione sconti
 * 27616   21/06/2018  Linda    Valorizza correttamente l'idAzienda.
 * 27900   19/09/2018  RA		Modifica per migliorare la gestione della memoria nel giro delle configurazioni.
 * 29197   21/05/2019  GScarta  Migliorie gestione sconti per provvigioni
 * 30330   10/12/2019  TJ	    Modifica la chiamata del metodo esegue(...)
 * 30785   22/02/2020  PM	    Se sullo scaglione del listino di vendita è impostata la condizione di azzerare gli sconti cliente il sistema sbaglia a calcolare le provvigioni su scala sconti.
 * 30871   06/03/2020  SZ		6 Decimale
 * 27561   06/05/2020  GScarta  Modifica WebService Recupera prezzo per la gestione dell'azzera sconti cliente sulla riga scaglione
 * 33484   06/05/2021  SZ		Gestione del listino alternativa del divisione per il recuperazione del prezzo
 * 34667   18/11/2021  Mekki    Valutare l'UMPrezzo nel calcolo della qtaVen
 * 35308   24/02/2022  TJ		Valuta la data di inizio validità della riga.
 * 36857   25/10/2022  YBA      Modificato metodo getProvvigioneDaSconto().
 * 39206   05/07/2023  Mekki    Considerare il valore della combo "Rif. data per prezzo sconti" definito sull'ordine di vendita
 * 40311   10/11/2023  RA		Modifica usa del metodo equals per tipi BigDecimal con il metodo areEqual del classe Utils
 * 41247   13/02/2024  HG       Gestione del attributo idZona
 * 42529   04/06/2024  SZ	    Dovrebbe trovare lo sconto specifico per configurazione nel caso di altri testata ordine.
 * 43127   26/08/2024  TA       Gestione del attributo idCategoriaVendita	                         
 */

public class RicercaCondizioniDiVendita
{

   //fix 12767 >
   protected TreeMap iParamsTest = null;
   protected String  iSqlStatementParamsTest = null;
   protected boolean iOkTest = false;
  //fix 12767 <

  public CondizioniDiVendita iCondizioniDiVendita;

  protected static CachedStatement STATEMENT = new CachedStatement(
    "SELECT "  +
    SystemParam.getSchema("THIP")+ "LST_VEN_RIG.ID_TESTATA, " +
    SystemParam.getSchema("THIP")+"LST_VEN_RIG.ID_LST_RIGA" +
    " FROM " +
    SystemParam.getSchema("THIP")+"LISTINI_VEN, " +
    SystemParam.getSchema("THIP")+"LST_VEN_TES, " +
    SystemParam.getSchema("THIP")+"LST_VEN_RIG " +
    "WHERE (" +
    SystemParam.getSchema("THIP")+"LST_VEN_RIG.ID_TESTATA = "+
  SystemParam.getSchema("THIP")+"LST_VEN_TES.ID_TESTATA AND " +
  SystemParam.getSchema("THIP")+"LST_VEN_RIG.ID_LISTINO = "+
  SystemParam.getSchema("THIP")+"LST_VEN_TES.ID_LISTINO AND " +
    SystemParam.getSchema("THIP")+"LST_VEN_TES.ID_LISTINO = "+
    SystemParam.getSchema("THIP")+"LISTINI_VEN.ID_LISTINO AND " +
  SystemParam.getSchema("THIP")+"LST_VEN_RIG.ID_AZIENDA = "+
  SystemParam.getSchema("THIP")+"LST_VEN_TES.ID_AZIENDA AND "+
    SystemParam.getSchema("THIP")+"LST_VEN_TES.ID_AZIENDA = "+
    SystemParam.getSchema("THIP")+"LISTINI_VEN.ID_AZIENDA AND " +
    SystemParam.getSchema("THIP")+"LISTINI_VEN.STATO='V' AND " +
    SystemParam.getSchema("THIP")+"LST_VEN_TES.STATO='V' AND " +
    SystemParam.getSchema("THIP")+"LST_VEN_RIG.STATO='V' AND " +
  SystemParam.getSchema("THIP")+"LST_VEN_RIG.ID_LISTINO=? AND TIPO_LST_TES=? AND TIPO_LST_RIG=? AND "+
  "((0=? AND R_CLIENTE IS NULL) OR (R_CLIENTE=? AND 1=?)) AND "+
  "((0=? AND R_ZONA IS NULL) OR (R_ZONA=? AND 1=?)) AND "+
  "((0=? AND R_CAT_VEN IS NULL) OR (R_CAT_VEN=? AND 1=?)) AND "+
  "((? BETWEEN "+SystemParam.getSchema("THIP")+"LST_VEN_TES.DATA_INZ_VALID AND "+
  SystemParam.getSchema("THIP")+"LST_VEN_TES.DATA_FIN_VALID) OR (? > "+
  SystemParam.getSchema("THIP")+"LST_VEN_TES.DATA_INZ_VALID AND "+
  SystemParam.getSchema("THIP")+"LST_VEN_TES.DATA_FIN_VALID IS NULL)) AND " +
  "((0=? AND R_ARTICOLO IS NULL) OR (R_ARTICOLO=? AND 1=?)) AND " +
  "((0=? AND R_MICROFAMIGLIA IS NULL) OR (R_MICROFAMIGLIA=? AND 1=?)) AND "+
  "((0=? AND R_SUBFAMIGLIA IS NULL) OR (R_SUBFAMIGLIA=? AND 1=?)) AND "+
  "((0=? AND R_MACROFAMIGLIA IS NULL) OR (R_MACROFAMIGLIA=? AND 1=?)) AND "+
  "((0=? AND R_LINEA_PRODOTTO IS NULL) OR (R_LINEA_PRODOTTO=? AND 1=?)) AND " +
  "((0=? AND R_CAT_PRZ IS NULL) OR (R_CAT_PRZ=? AND 1=?)) AND " +
  "((0=? AND R_UNITA_MISURA IS NULL) OR (R_UNITA_MISURA=? AND 1=?)) AND "+
    "((0=? AND R_CONFIGURAZIONE IS NULL) OR (R_CONFIGURAZIONE=? AND 1=?)) AND "+  //...FIX02182 - DZ
  "((? BETWEEN "+SystemParam.getSchema("THIP")+"LST_VEN_RIG.DATA_INZ_VALID AND "+
  SystemParam.getSchema("THIP")+"LST_VEN_RIG.DATA_FIN_VALID) OR (? > " +
  SystemParam.getSchema("THIP")+"LST_VEN_RIG.DATA_INZ_VALID AND "+
  SystemParam.getSchema("THIP")+"LST_VEN_RIG.DATA_FIN_VALID IS NULL)) AND "+
  SystemParam.getSchema("THIP")+"LST_VEN_TES.ID_AZIENDA=?) " +
    "ORDER BY " +
    SystemParam.getSchema("THIP")+"LST_VEN_TES.DATA_INZ_VALID DESC, "+
  SystemParam.getSchema("THIP")+"LST_VEN_RIG.DATA_INZ_VALID DESC" );

    protected static CachedStatement STATEMENT_SCONTI = new CachedStatement(
    "SELECT "  +
    SystemParam.getSchema("THIP")+ "LSTVEN_SCN_RIG.ID_TESTATA, " +
    SystemParam.getSchema("THIP")+"LSTVEN_SCN_RIG.ID_LST_RIGA" +
    " FROM " +
  SystemParam.getSchema("THIP")+"LSTVEN_SCN_TES, " +
    SystemParam.getSchema("THIP")+"LSTVEN_SCN_RIG, " +
    SystemParam.getSchema("THIP")+"LSTVEN_SCN " +
    "WHERE (" +
    SystemParam.getSchema("THIP")+"LSTVEN_SCN_RIG.ID_TESTATA = "+
  SystemParam.getSchema("THIP")+"LSTVEN_SCN_TES.ID_TESTATA AND " +
  SystemParam.getSchema("THIP")+"LSTVEN_SCN_RIG.ID_LISTINO = "+
  SystemParam.getSchema("THIP")+"LSTVEN_SCN_TES.ID_LISTINO AND " +
    SystemParam.getSchema("THIP")+"LSTVEN_SCN_TES.ID_LISTINO = "+
    SystemParam.getSchema("THIP")+"LSTVEN_SCN.ID_LISTINO AND " +
  SystemParam.getSchema("THIP")+"LSTVEN_SCN_RIG.ID_AZIENDA = "+
  SystemParam.getSchema("THIP")+"LSTVEN_SCN_TES.ID_AZIENDA AND "+
    SystemParam.getSchema("THIP")+"LSTVEN_SCN_TES.ID_AZIENDA = "+
    SystemParam.getSchema("THIP")+"LSTVEN_SCN.ID_AZIENDA AND "+
    SystemParam.getSchema("THIP")+"LSTVEN_SCN.STATO='V' AND "+
    SystemParam.getSchema("THIP")+"LSTVEN_SCN_TES.STATO='V' AND "+
    SystemParam.getSchema("THIP")+"LSTVEN_SCN_RIG.STATO='V' AND "+
  SystemParam.getSchema("THIP")+"LSTVEN_SCN_RIG.ID_LISTINO=? AND TIPO_LST_TES=? AND TIPO_LST_RIG=? AND "+
  "((0=? AND R_CLIENTE IS NULL) OR (R_CLIENTE=? AND 1=?)) AND "+
  "((0=? AND R_ZONA IS NULL) OR (R_ZONA=? AND 1=?)) AND "+
  "((0=? AND R_CAT_VEN IS NULL) OR (R_CAT_VEN=? AND 1=?)) AND "+
  "((? BETWEEN "+SystemParam.getSchema("THIP")+"LSTVEN_SCN_TES.DATA_INZ_VALID AND "+
  SystemParam.getSchema("THIP")+"LSTVEN_SCN_TES.DATA_FIN_VALID) OR " +
  "(? >"+SystemParam.getSchema("THIP")+"LSTVEN_SCN_TES.DATA_INZ_VALID AND "+
  SystemParam.getSchema("THIP")+"LSTVEN_SCN_TES.DATA_FIN_VALID IS NULL)) AND " +
  "((0=? AND R_ARTICOLO IS NULL) OR (R_ARTICOLO=? AND 1=?)) AND " +
  "((0=? AND R_MICROFAMIGLIA IS NULL) OR (R_MICROFAMIGLIA=? AND 1=?)) AND "+
  "((0=? AND R_SUBFAMIGLIA IS NULL) OR (R_SUBFAMIGLIA=? AND 1=?)) AND "+
  "((0=? AND R_MACROFAMIGLIA IS NULL) OR (R_MACROFAMIGLIA=? AND 1=?)) AND "+
  "((0=? AND R_LINEA_PRODOTTO IS NULL) OR (R_LINEA_PRODOTTO=? AND 1=?)) AND " +
  "((0=? AND R_CAT_PRZ IS NULL) OR (R_CAT_PRZ=? AND 1=?)) AND " +
  "((0=? AND R_UNITA_MISURA IS NULL) OR (R_UNITA_MISURA=? AND 1=?)) AND "+
    "((0=? AND R_CONFIGURAZIONE IS NULL) OR (R_CONFIGURAZIONE=? AND 1=?)) AND "+  //...FIX02182 - DZ
  "((? BETWEEN "+SystemParam.getSchema("THIP")+"LSTVEN_SCN_RIG.DATA_INZ_VALID AND "+
  SystemParam.getSchema("THIP")+"LSTVEN_SCN_RIG.DATA_FIN_VALID) OR ( ? > "+
  SystemParam.getSchema("THIP")+"LSTVEN_SCN_RIG.DATA_INZ_VALID AND " +
  SystemParam.getSchema("THIP")+"LSTVEN_SCN_RIG.DATA_FIN_VALID IS NULL)) AND "+
  SystemParam.getSchema("THIP")+"LSTVEN_SCN_TES.ID_AZIENDA=?) " +
    "ORDER BY " +
    SystemParam.getSchema("THIP")+"LSTVEN_SCN_TES.DATA_INZ_VALID DESC, "+
  SystemParam.getSchema("THIP")+"LSTVEN_SCN_RIG.DATA_INZ_VALID DESC" );


    private static CachedStatement STATEMENT_PROVV = new CachedStatement(
  "SELECT ID_ANA_PVG FROM "+ SystemParam.getSchema("THIP")+"ANAGRA_PVG "+
  "WHERE ID_AZIENDA = ? AND " +
    SystemParam.getSchema("THIP")+"ANAGRA_PVG.STATO='V' AND "+
  "((? BETWEEN "+SystemParam.getSchema("THIP")+"ANAGRA_PVG.DATA_INZ_VALID AND "+
  //Fix 13063 Inizio
  //SystemParam.getSchema("THIP")+"ANAGRA_PVG.DATA_FIN_VALID) OR (? > "+
  SystemParam.getSchema("THIP") + "ANAGRA_PVG.DATA_FIN_VALID) OR (? >= " +
  //Fix 13063 Fine
  SystemParam.getSchema("THIP")+"ANAGRA_PVG.DATA_INZ_VALID AND "+
  SystemParam.getSchema("THIP")+"ANAGRA_PVG.DATA_FIN_VALID IS NULL)) AND " +
  "((R_AGENTE IS NULL AND 0 = ?) OR (R_AGENTE = ? AND 1 = ?)) AND " +
  "((R_CLIENTE IS NULL AND 0 = ?) OR (R_CLIENTE = ? AND 1 = ?)) AND " +
  "((R_CAT_VEN_CLI IS NULL AND 0 = ?) OR (R_CAT_VEN_CLI = ? AND 1 = ?)) AND " +
  "((R_MOD_PAGAMENTO IS NULL AND 0 = ?) OR (R_MOD_PAGAMENTO = ? AND 1 = ?)) AND " +
  "((R_ARTICOLO IS NULL AND 0 = ?) OR (R_ARTICOLO = ? AND 1 = ?)) AND " +
  "((R_LINEA_PRODOTTO IS NULL AND 0 = ?) OR (R_LINEA_PRODOTTO = ? AND 1 = ?)) AND " +
  "((R_CAT_PREZZO IS NULL AND 0 = ?) OR (R_CAT_PREZZO = ? AND 1 = ?)) AND " +
  "((R_UNITA_MISURA IS NULL AND 0 = ?) OR (R_UNITA_MISURA = ? AND 1 = ?)) AND " +
  "((R_MACROFAMIGLIA IS NULL AND 0 = ?) OR (R_MACROFAMIGLIA = ? AND 1 = ?)) AND " +
  "((R_SUBFAMIGLIA IS NULL AND 0 = ?) OR (R_SUBFAMIGLIA = ? AND 1 = ?)) AND " +
  "((R_MICROFAMIGLIA IS NULL AND 0 = ?) OR (R_MICROFAMIGLIA = ? AND 1 = ?)) AND " +  // fix 12767
  "((ID_SCELTA IS NULL AND 0 = ?) OR (ID_SCELTA = ? AND 1 = ?)) " + // fix 12767
  "ORDER BY "+SystemParam.getSchema("THIP")+"ANAGRA_PVG.DATA_INZ_VALID DESC");


  private static CachedStatement STATEMENT_SCN_PROVV= new CachedStatement(
  "SELECT ID_SCN_PVG FROM " + SystemParam.getSchema("THIP") + "AGE_SCN_PVG " +
  "WHERE  ID_AZIENDA = ? AND R_AGENTE = ? AND "+
    SystemParam.getSchema("THIP")+"AGE_SCN_PVG.STATO='V' AND "+
  "((R_LINEA_PRODOTTO IS NULL AND 0 = ?) OR (R_LINEA_PRODOTTO = ? AND 1 = ?)) AND "+
  "((R_MACROFAMIGLIA IS NULL AND 0 = ?) OR (R_MACROFAMIGLIA = ? AND 1 = ?)) AND " +
  "((R_SUBFAMIGLIA IS NULL AND 0 = ?) OR (R_SUBFAMIGLIA = ? AND 1 = ?)) AND "+
  "((R_MICROFAMIGLIA IS NULL AND 0 = ?) OR (R_MICROFAMIGLIA = ? AND 1 = ?)) AND "+
  "((R_CLIENTE IS NULL AND 0 = ?) OR (R_CLIENTE = ? AND 1 = ?))  AND "+ // ");//Fix 22229
  "((R_ARTICOLO IS NULL AND 0 = ?) OR (R_ARTICOLO = ? AND 1 = ?))");//Fix 36857

  private boolean iIsDettaglioArtCfg = false;

  /**
   * Valorizza l'attributo.
   * @param condizioniDiVendita CondizioniDiVendita
   */
  /*
   * Revisions:
   * Date          Owner      Description
   * 09/05/2002    Wizard     Codice generato da Wizard
   *
   */
  public void setCondizioniDiVendita(CondizioniDiVendita condizioniDiVendita)
  {
    if (!(condizioniDiVendita == null))
      this.iCondizioniDiVendita = condizioniDiVendita;
    else
      this.iCondizioniDiVendita = null;
  }

  /**
   * Restituisce l'attributo.
   * @return CondizioniDiVendita
   */
  /*
   * Revisions:
   * Date          Owner      Description
   * 09/05/2002    Wizard     Codice generato da Wizard
   *
   */
  public CondizioniDiVendita getCondizioniDiVendita()
  {
    return iCondizioniDiVendita;
  }

    public RicercaCondizioniDiVendita()
    {}

//MG FIX 4348: per mantenere compatibilita con codice precedente
    public CondizioniDiVendita ricercaCondizioniDiVendita(String idAzienda, ListinoVendita listino,
                                                          ClienteVendita cliente,
                                                          Articolo articolo,
                                                          Configurazione configurazione,
                                                          UnitaMisura unita,
                                                          BigDecimal quantita,
                                                          BigDecimal importo,
                                                          ModalitaPagamento modalita,
                                                          java.sql.Date dataValidita,
                                                          Agente agente,
                                                          Agente subagente,
                                                          UnitaMisura unitaMag,
                                                          BigDecimal quantitaMag,
                                                          Valuta valuta,
                                                          // Fix 13211 inzio
                                                          UnitaMisura umSecMag,
                                                          BigDecimal qtaSecMag
                                                          // Fix 13211 fine
                                                          ) throws SQLException
    {
      return ricercaCondizioniDiVendita(idAzienda,listino,cliente,articolo,
                                        configurazione,unita,quantita,importo,
                                        modalita,dataValidita,agente,subagente,unitaMag,
                                        quantitaMag,valuta,null,null,null,
                                        umSecMag, qtaSecMag // Fix 13211
                                        );
    }
//MG FIX 4348

    /**
     * FIX04103 - DZ.
     */
    public CondizioniDiVendita ricercaCondizioniDiVendita(String idAzienda, ListinoVendita listino,
                                                          ClienteVendita cliente,
                                                          Articolo articolo,
                                                          Configurazione configurazione,
                                                          UnitaMisura unita,
                                                          BigDecimal quantita,
                                                          BigDecimal importo,
                                                          ModalitaPagamento modalita,
                                                          java.sql.Date dataValidita,
                                                          Agente agente,
                                                          Agente subagente,
                                                          UnitaMisura unitaMag,
                                                          BigDecimal quantitaMag,
                                                          Valuta valuta,
                                                          //MG FIX 4348
                                                          BigDecimal scontoIntestatario,
                                                          BigDecimal scontoModalita,
                                                          String idScontoModalita,
                                                          //MG FIX 4348
                                                          // fix 11156
                                                          ArticoloVersione idVersione,
                                                          BigDecimal numeroImballo,
                                                          // fine fix 11156
                                                          // Fix 13211 inzio
                                                          UnitaMisura umSecMag,
                                                          BigDecimal qtaSecMag
                                                          // Fix 13211 fine
                                                          ) throws SQLException
    {//Fix 13589 Inizio
     /*return ricercaCondizioniDiVendita(idAzienda, listino, cliente, articolo, null, unita, quantita,
                                        importo, modalita, dataValidita, agente, subagente,
                                        unitaMag, quantitaMag, valuta, false,
                                        //MG FIX 4348
                                        scontoIntestatario,
                                        scontoModalita,
                                        idScontoModalita,
                                        //MG FIX 4348
                                        // fix 11156
                                        idVersione,
                                        numeroImballo,
                                        // fine fix 11156
                                        // Fix 13211 inzio
                                        umSecMag,
                                        qtaSecMag
                                        // Fix 13211 fine
                                        );*/
     return ricercaCondizioniDiVendita(idAzienda, listino, cliente, articolo, configurazione, unita, quantita,
                                      importo, modalita, dataValidita, agente, subagente,
                                      unitaMag, quantitaMag, valuta, false,
                                      //MG FIX 4348
                                      scontoIntestatario,
                                      scontoModalita,
                                      idScontoModalita,
                                      //MG FIX 4348
                                      // fix 11156
                                      idVersione,
                                      numeroImballo,
                                      // fine fix 11156
                                      umSecMag, qtaSecMag // fix 13211
      );
    //Fix 13589 Fine
    }


    /**
     * Aggiunto con FIX02182 - DZ
     * Parametri: senza configurazione (e senza attivoListinoCampagna).
     */

    public CondizioniDiVendita ricercaCondizioniDiVendita(String idAzienda, ListinoVendita listino,
           ClienteVendita cliente, Articolo articolo, UnitaMisura unita, BigDecimal quantita,
           BigDecimal importo, ModalitaPagamento modalita, java.sql.Date dataValidita,
           Agente agente, Agente subagente, UnitaMisura unitaMag, BigDecimal quantitaMag,
           Valuta valuta,
           // Fix 13211 inzio
           UnitaMisura umSecMag,
           BigDecimal qtaSecMag
           // Fix 13211 fine
           ) throws SQLException
    {
      return ricercaCondizioniDiVendita(idAzienda, listino, cliente, articolo, null, unita, quantita,
                                        importo, modalita, dataValidita, agente, subagente,
                                        unitaMag, quantitaMag, valuta,
                                        umSecMag, qtaSecMag // Fix 13211
        );
    }

    /**
   * Questo metodo istanzia un oggetto Condizioni di vendita con i parametri passati
   * al metodo e avvia la determinazione delle informazioni utili caricando gli
   * attributi di Condizione di vendita.
   * la ricerca viene effettuata sul listino campagna e poi sul listino passato
   * al metodo.
   */
    /*
    * Revisions:
    * Number  Date         Owner   Description
    *         18/06/2002   DB
    *         17/09/2002   DB
    * 02001   20/05/2004   DZ      Aggiunti nuovi totali/importi/imposte.
    * 02182   30/06/2004   DZ      Aggiunto parametro configurazione.
    * 02261   22/07/2004   DZ      Gestione autorizzazioni dei listini.
    */


//MG FIX 4348 : per mantenere compatibilità con il codice precedente
  public CondizioniDiVendita ricercaCondizioniDiVendita(String idAzienda, ListinoVendita listino,
                                                        ClienteVendita cliente,
                                                        Articolo articolo,
                                                        Configurazione configurazione, //...FIX02182 - DZ
                                                        UnitaMisura unita,
                                                        BigDecimal quantita,
                                                        BigDecimal importo,
                                                        ModalitaPagamento modalita,
                                                        java.sql.Date dataValidita,
                                                        Agente agente,
                                                        Agente subagente,
                                                        UnitaMisura unitaMag,
                                                        BigDecimal quantitaMag,
                                                        Valuta valuta,
                                                        boolean visualizzaDettagli,
                                                        // gic 11156
                                                        ArticoloVersione idVersione,
                                                        BigDecimal numeroImballo,
                                                        // fine fix 11156
                                                        // Fix 13211 inzio
                                                        UnitaMisura umSecMag,
                                                        BigDecimal qtaSecMag
                                                        // Fix 13211 fine
                                                        ) throws SQLException
  {
    return ricercaCondizioniDiVendita(idAzienda,listino,cliente,articolo,configurazione,
                                      unita,quantita,importo,modalita,dataValidita,agente,subagente,
                                      unitaMag,quantitaMag,valuta,visualizzaDettagli,
                                      null,null,null,
                                      // fix 11156
                                      idVersione,
                                      numeroImballo,
                                      // fine fix 11156
                                      umSecMag, qtaSecMag // Fix 13211
        );
  }
//MG FIX 4348

  //added: 03674
  protected static String cSyncRicercaCondizioniDiVendita = new String("javascript:allert('querry senza nessun risultato')");

  //fixed: 03674
  /**
   * FIX04103 - DZ.
   * Aggiunto parametro visualizzaDettagli.
   */
  public CondizioniDiVendita ricercaCondizioniDiVendita(String idAzienda, ListinoVendita listino,
                                                        ClienteVendita cliente,
                                                        Articolo articolo,
                                                        Configurazione configurazione, //...FIX02182 - DZ
                                                        UnitaMisura unita,
                                                        BigDecimal quantita,
                                                        BigDecimal importo,
                                                        ModalitaPagamento modalita,
                                                        java.sql.Date dataValidita,
                                                        Agente agente,
                                                        Agente subagente,
                                                        UnitaMisura unitaMag,
                                                        BigDecimal quantitaMag,
                                                        Valuta valuta,
                                                        boolean visualizzaDettagli,
                                                        //MG FIX 4348
                                                        BigDecimal prcScontoIntestatario,
                                                        BigDecimal prcScontoModalita,
                                                        String idScontoModalita,
                                                        //MG FIX 4348
                                                        // fix 11156
                                                        ArticoloVersione idVersione,
                                                        BigDecimal numeroImballo,
                                                        // fine fix 11156
                                                        // Fix 13211 inzio
                                                        UnitaMisura umSecMag,
                                                        BigDecimal quantSecMag
                                                        //Fix 13211 fine
                                                        ) throws SQLException
  {
  	
  	//Fix 24273
    /*synchronized (cSyncRicercaCondizioniDiVendita) {

        boolean ricercaProvv = false;  //MG FIX 6204

        boolean ricercaListino = false;
        boolean listinoIndividuato = false; //...FIX02419 - DZ
        // attributo utile per verificare se è necessario effettuare
        // la ricerca anche per articolo e unita di misura di magazzino
        boolean doppiaRicerca = false;
        // fix 1797
        if (idAzienda == null || valuta == null) {
            //if(idAzienda == null)
            // fine fix 1797
            return null;
        }
        //Fix 3223 - inizio
        CondizioniDiVendita cdv = (CondizioniDiVendita) Factory.
            createObject(CondizioniDiVendita.class);
        //Fix 3223 - fine

        PersDatiGen iPersDatiGen = PersDatiGen.getCurrentPersDatiGen();

        // Fix 2343
        //if (!RicercaCondizioniDiVendita.isListinoAuthorized(listino)) //...FIX02261 - DZ
        //return null;
        if (!RicercaCondizioniDiVendita.isListinoAuthorized(listino)) { //...FIX02261 - DZ
            listino = null;
        }
        // Fine fix 2343

        // inanzitutto si controlla la corrispondenza tra la valuta del listino
        // e quella passata al metodo che siano la stessa.
        if (listino != null) {
            if (iPersDatiGen.getValutaPrimaria() != null) {
                if (!valuta.equals(listino.getValuta()) &&
                    !valuta.
                    getIdValuta().equals(iPersDatiGen.getIdValutaPrimaria()))
                    return null;
            }
            else {
                if (!valuta.equals(listino.getValuta()))
                    return null;
            }
        }
        else if ( (iPersDatiGen.getValutaPrimaria() != null &&
                   !valuta.
                   getIdValuta().equals(iPersDatiGen.getIdValutaPrimaria())) ||
                 iPersDatiGen.getValutaPrimaria() == null)
//MG FIX 6204 inizio
          //            return null;
          ricercaProvv = true;
//MG FIX 6204 fine

        cdv.setListinoVendita(listino);
        cdv.setCliente(cliente);
        cdv.setArticolo(articolo);
        cdv.setConfigurazione(configurazione); //...FIX02182 - DZ
        cdv.setUnitaMisura(unita);
        cdv.setUMVen(unita);//Fix 17534
        cdv.setQuantita(quantita);
        cdv.setImporto(importo);
        cdv.setModalitaPagamento(modalita);
        cdv.setDataValidita(dataValidita);
        //...inizio FIX01990 - DZ
        if (listino != null)
            cdv.setAttivoListinoCampagna(listino.getListinoCampagna() != null); //...FIX01948 - DZ - FIX01974
        else
            cdv.setAttivoListinoCampagna(false);
            //...fine FIX01990 - DZ
        cdv.setAgente(agente);
        cdv.setSubAgente(subagente);
        cdv.setValuta(valuta);
        // fix 11156
        cdv.setVersione(idVersione);
        cdv.setNumeroImballo(numeroImballo);
        // fix 11779 >
        
        //if (numeroImballo!=null && numeroImballo.compareTo(new BigDecimal("0"))>0){
         // cdv.setSempreManuale(true);
        //}
        
        // fix 11779 <
        // fine fix 11156

        // se l'unita di misura di magazzino è diversa da quella di vendita
        // nel caso in cui la ricerca per unita di misura di vendita
        // non abbia dato risultato positivo allora si effettua anche una
        // ricerca per unita di misura di magazzino.

        //fix 13211 inzio
        cdv.setUMPrmMag(unitaMag);
        cdv.setQuantitaPrmMag(quantitaMag);
        if (umSecMag != null)
          cdv.setUMSecMag(umSecMag);
        if (quantSecMag != null)
          cdv.setQuantitaSecMag(quantSecMag);
        //fix 13211 fine

        if (unitaMag != null && quantitaMag != null) {
            if (!unitaMag.equals(unita))
                doppiaRicerca = true;
        }

        cdv.setAziendaKey(idAzienda);

//MG FIX 4348 - inizio
        if (prcScontoIntestatario != null)
          cdv.setPrcScontoIntestatario(prcScontoIntestatario);
        if (prcScontoModalita != null)
          cdv.setPrcScontoModalita(prcScontoModalita);
        if (idScontoModalita != null) {
          String key = KeyHelper.buildObjectKey(
              new String[] {idAzienda, idScontoModalita}
              );
              cdv.setScontoModalitaKey(key);
        }
//MG FIX 4348 - fine

        this.setCondizioniDiVendita(cdv);

        PersDatiVen psnDatiVen = PersDatiVen.getPersDatiVen(idAzienda);
        boolean personalizzati = psnDatiVen != null;

        // Fix 2343
        
             //if(listino==null && personalizzati && psnDatiVen.getListinoVendita()!=null && psnDatiVen.getListinoVendita().getValuta().equals(valuta))
               //cdv.setListinoVendita(psnDatiVen.getListinoVendita());
         
        if (listino == null && personalizzati &&
            psnDatiVen.getListinoVendita() != null &&
            psnDatiVen.getListinoVendita().getValuta().equals(valuta)) {
            if (RicercaCondizioniDiVendita.isListinoAuthorized(psnDatiVen.
                getListinoVendita())) {
                listino = psnDatiVen.getListinoVendita();
                cdv.setListinoVendita(listino);
            }
        }
        // Fine fix 2343

        //...inizio FIX01948 - DZ
        ListinoVendita lv = null;
        if (listino != null) { //...FIX01974 - DZ
            lv = listino.getListinoCampagna();
            if (!RicercaCondizioniDiVendita.isListinoAuthorized(lv)) { //...FIX02261 - DZ
                lv = null;
                cdv.setAttivoListinoCampagna(false);
            }
        }

//MG FIX 6204
        if (!ricercaProvv) {

        if (lv != null) {
            // Ricerca nel listino campagna con UM di vendita
            listinoIndividuato = this.individuaListino(lv, visualizzaDettagli);
            this.getCondizioniDiVendita().setUMPrezzo(CondizioniDiVendita.
                VENDITA);
            // Ricerca nel Listino campagna con UM di magazzino
            if (lv != null && !listinoIndividuato && doppiaRicerca) {
                this.getCondizioniDiVendita().setQuantita(quantitaMag);
                this.getCondizioniDiVendita().setUnitaMisura(unitaMag);
                listinoIndividuato = this.individuaListino(lv, visualizzaDettagli);
                this.getCondizioniDiVendita().setUMPrezzo(
                    CondizioniDiVendita.MAGAZZINO);
            }
            if (!listinoIndividuato) { //...FIX02419 - DZ
                this.getCondizioniDiVendita().setQuantita(quantita);
                this.getCondizioniDiVendita().setUnitaMisura(unita);
                lv = this.iCondizioniDiVendita.getListinoVendita();
                ricercaListino = this.individuaListino(lv, visualizzaDettagli);
                this.getCondizioniDiVendita().setUMPrezzo(
                    CondizioniDiVendita.VENDITA);
                if (!ricercaListino && doppiaRicerca) {
                    this.getCondizioniDiVendita().setQuantita(quantitaMag);
                    this.getCondizioniDiVendita().setUnitaMisura(unitaMag);
                    lv = this.iCondizioniDiVendita.getListinoVendita();
                    ricercaListino = this.individuaListino(lv, visualizzaDettagli);
                    this.getCondizioniDiVendita().setUMPrezzo(
                        CondizioniDiVendita.MAGAZZINO);
                }
            }
            else
                this.getCondizioniDiVendita().setListinoVendita(lv);
        }
        else {
            lv = this.iCondizioniDiVendita.getListinoVendita();
            this.getCondizioniDiVendita().setQuantita(quantita);
            this.getCondizioniDiVendita().setUnitaMisura(unita);
            ricercaListino = this.individuaListino(lv, visualizzaDettagli);
            this.getCondizioniDiVendita().setUMPrezzo(CondizioniDiVendita.
                VENDITA);
            if (!ricercaListino) {
                this.getCondizioniDiVendita().setQuantita(quantitaMag);
                this.getCondizioniDiVendita().setUnitaMisura(unitaMag);
                ricercaListino = this.individuaListino(lv, visualizzaDettagli);
                this.getCondizioniDiVendita().setUMPrezzo(
                    CondizioniDiVendita.MAGAZZINO);
            }
        }
        }  //MG FIX 6204

        this.calcolaPrezzi(unita, unitaMag, visualizzaDettagli);
        // Prima di passare al calcolo delle provvigioni è bene rivedere il calcolo
        // dell'importo in funzione del risultato ottenuto dal calcolo dei prezzi.
        this.getCondizioniDiVendita().setImporto(calcoloImporto());
        this.calcolaSconti();
        // E' importante che sia chiamato il metodo degli sconti prima di quello delle
        // provvigioni in quanto viene effettuato un controllo sulle provvigioni in base
        // agli sconti.
        // fix 2607
        if (this.getCondizioniDiVendita().getAgente() != null ||
            this.getCondizioniDiVendita().getSubAgente() != null) {
            // fine fix 2607
            this.calcolaProvvigioni();
            // fix 2607
        }
        // fine fix 2607
        this.calcolaPrezzoAlNettoSconti();
        
        // fix 25658 >
        this.calcolaPrezzoAlNettoScontiTotali();
        // fix 25658 <

        return this.getCondizioniDiVendita();
    }*/
  	CondizioniDiVenditaParams condVenParams = (CondizioniDiVenditaParams)Factory.createObject(CondizioniDiVenditaParams.class);
  	//Fix 25185 PM >
     Azienda aziendaObj = (Azienda)PersistentObject.elementWithKey(Azienda.class, KeyHelper.buildObjectKey(new String[] {idAzienda}), PersistentObject.NO_LOCK);
     condVenParams.setAzienda(aziendaObj);
  	//Fix 25185 PM <
    condVenParams.setIdAziendaParam(idAzienda);
  	condVenParams.setListinoVendita(listino);
  	condVenParams.setCliente(cliente);  	
  	condVenParams.setArticolo(articolo);  	
  	condVenParams.setConfigurazione(configurazione);  	
  	condVenParams.setUMVendita(unita);  	
  	condVenParams.setQuantitaInUMRif(quantita);
  	condVenParams.setImporto(importo);  	
  	condVenParams.setModalitaPagamento(modalita);  	
  	condVenParams.setDataValidita(dataValidita);  	
  	condVenParams.setAgente(agente);
  	condVenParams.setSubagente(subagente);  	
  	condVenParams.setUMMagazzino(unitaMag);  	
  	condVenParams.setQuantitaInUMPrm(quantitaMag);  	
  	condVenParams.setValuta(valuta);  	
  	condVenParams.setVisualizzaDettagliParam(visualizzaDettagli);  	
  	condVenParams.setPrcScontoIntestatario(prcScontoIntestatario);  	
  	condVenParams.setPrcScontoModalita(prcScontoModalita);  	
  	condVenParams.setIdScontoModalitaParam(idScontoModalita);  	
  	condVenParams.setArtVersione(idVersione);  	
  	condVenParams.setNumeroImballo(numeroImballo);  	
  	condVenParams.setUMSecMagazzino(umSecMag);  	
  	condVenParams.setQuantitaInUMSec(quantSecMag);  	
  	
    return getCondizioniVenditaInternal(condVenParams);
    //Fix 24273
  }

    /**
     * Aggiunto con FIX02182 - DZ.
     * Spostato dalla servlet RecuperaDatiVendita.
     */
    public static CondizioniDiVendita getCondizioniVendita(
                                        String idListino,
                                        String idCliente,
                                        String idArticolo,
                                        Integer idConfigurazione,  //...FIX02182 - DZ
                                        String idUMVendita,
                                        String qtaVendita,
                                        String qtaMagazzino,
                                        String idModPagamento,
                                        String dtOrdine,
                                        String dtConsegna,
                                        String idAgente,
                                        String idSubagente,
                                        String idUMMagazzino,
                                        String idValuta,
                                        String idUMSecMag, // Fix 13211
                                        String qtaSecMag // Fix 13211
                                      ) {
       //Fix 4003 - inizio
       String key = KeyHelper.buildObjectKey(
        new String[] {Azienda.getAziendaCorrente(), idCliente}
      );

       ClienteVendita cliente = null;
      try {
        cliente = (ClienteVendita)
           ClienteVendita.elementWithKey(
             ClienteVendita.class, key, PersistentObject.NO_LOCK
           );
         }
      catch (SQLException e) {
        Trace.printStackTrace();
      }

      char tipoDataPrezziSconti = '\0';
         if (cliente == null) {
        tipoDataPrezziSconti = PersDatiVen.getCurrentPersDatiVen().getTipoDataPrezziSconti();
      }
      else {
        tipoDataPrezziSconti = cliente.getRifDataPerPrezzoSconti();
      }

      return getCondizioniVendita(idListino, idCliente, idArticolo, idConfigurazione,
          idUMVendita, qtaVendita, qtaMagazzino, idModPagamento,
          dtOrdine, dtConsegna, idAgente, idSubagente, idUMMagazzino, idValuta,
          new Character(tipoDataPrezziSconti).toString(),
          idUMSecMag, qtaSecMag //Fix 13211
        );
       //Fix 4003 - fine
    }


    //Fix 4003 - inizio
    public static CondizioniDiVendita getCondizioniVendita(String idListino,
                                                           String idCliente,
                                                           String idArticolo,
                                                           Integer idConfigurazione,  //...FIX02182 - DZ
                                                           String idUMVendita,
                                                           String qtaVendita,
                                                           String qtaMagazzino,
                                                           String idModPagamento,
                                                           String dtOrdine,
                                                           String dtConsegna,
                                                           String idAgente,
                                                           String idSubagente,
                                                           String idUMMagazzino,
                                                           String idValuta,
                                                           String rifDataPrezzoSconti,
                                                           // Fix 13211 inzio
                                                           String umSecMag,
                                                           String qtaSecMag
                                                           // Fix 13211 fine
                                                           ) {
      String idEsternoConfigurazione = null;
      if (idConfigurazione != null){
        String key =
           KeyHelper.buildObjectKey(
              new Object[]{Azienda.getAziendaCorrente(), idConfigurazione}
           );
        try {
          Configurazione config =
             Configurazione.elementWithKey(key, PersistentObject.NO_LOCK);
          if (config != null)
            idEsternoConfigurazione = config.getIdEsternoConfig();
        }
        catch (SQLException e) {
          Trace.printStackTrace();
        }

      }
      return
            getCondizioniVendita(
               idListino, idCliente, idArticolo, idEsternoConfigurazione,
          idUMVendita, qtaVendita, qtaMagazzino, idModPagamento,
          dtOrdine, dtConsegna, idAgente, idSubagente, idUMMagazzino, idValuta,
               rifDataPrezzoSconti,
          umSecMag, qtaSecMag // Fix 13211
            );
    }
    //Fix 4003 - fine


    public static CondizioniDiVendita getCondizioniVendita(
                                        String idListino,
                                        String idCliente,
                                        String idArticolo,
                                        String idEsternoConfigurazione,  //...FIX02182 - DZ
                                        String idUMVendita,
                                        String qtaVendita,
                                        String qtaMagazzino,
                                        String idModPagamento,
                                        String dtOrdine,
                                        String dtConsegna,
                                        String idAgente,
                                        String idSubagente,
                                        String idUMMagazzino,
                                        String idValuta,
                                        String idUMSecMag, // Fix 13211
                                        String qtaSecMag // Fix 13211
                                      ) {
      return getCondizioniVendita(idListino, idCliente, idArticolo,
                                  idEsternoConfigurazione,  //...FIX02182 - DZ
                                  idUMVendita, qtaVendita, qtaMagazzino,
                                  idModPagamento, dtOrdine, dtConsegna, idAgente,
                                  idSubagente, idUMMagazzino, idValuta, false,
                                  idUMSecMag, qtaSecMag // Fix 13211
        );
    }

    /**
     * Aggiunto con FIX02182 - DZ.
     * Spostato dalla servlet RecuperaDatiVendita.
     */
    public static CondizioniDiVendita getCondizioniVendita(
                                        String idListino,
                                        String idCliente,
                                        String idArticolo,
                                        String idEsternoConfigurazione,  //...FIX02182 - DZ
                                        String idUMVendita,
                                        String qtaVendita,
                                        String qtaMagazzino,
                                        String idModPagamento,
                                        String dtOrdine,
                                        String dtConsegna,
                                        String idAgente,
                                        String idSubagente,
                                        String idUMMagazzino,
                                        String idValuta,
                                        boolean visualizzaDettagli,  //...FIX04103 - DZ
                                        String idUMSecMag, // Fix 13211
                                        String qtaSecMag // Fix 13211
                                      ) {
       //Fix 4003 - inizio
      String key = KeyHelper.buildObjectKey(new String[] {Azienda.getAziendaCorrente(), idCliente});

      ClienteVendita cliente = null;
      try {
        cliente = (ClienteVendita)ClienteVendita.elementWithKey(ClienteVendita.class, key, PersistentObject.NO_LOCK);
      }
      catch (SQLException e) {
        Trace.printStackTrace();
      }

      char tipoDataPrezziSconti = '\0';
      if (cliente == null) {
        tipoDataPrezziSconti = PersDatiVen.getCurrentPersDatiVen().getTipoDataPrezziSconti();
      }
      else {
        tipoDataPrezziSconti = cliente.getRifDataPerPrezzoSconti();
      }

      return getCondizioniVendita(idListino, idCliente, idArticolo, idEsternoConfigurazione,
          idUMVendita, qtaVendita, qtaMagazzino, idModPagamento,
          dtOrdine, dtConsegna, idAgente, idSubagente, idUMMagazzino, idValuta,
          new Character(tipoDataPrezziSconti).toString(), visualizzaDettagli,
          idUMSecMag, qtaSecMag // Fix 13211
        );
       //Fix 4003 - fine
    }

/*    public static CondizioniDiVendita getCondizioniVendita(
                                        String idListino,
                                        String idCliente,
                                        String idArticolo,
                                        String idEsternoConfigurazione,  //...FIX02182 - DZ
                                        String idUMVendita,
                                        String qtaVendita,
                                        String qtaMagazzino,
                                        String idModPagamento,
                                        String dtOrdine,
                                        String dtConsegna,
                                        String idAgente,
                                        String idSubagente,
                                        String idUMMagazzino,
                                        String idValuta
                                      ) {
            //Fix 4003 - inizio
            String key = KeyHelper.buildObjectKey(
        new String[] {Azienda.getAziendaCorrente(), idCliente}
      );

            ClienteVendita cliente = null;
      try {
        cliente = (ClienteVendita)
                ClienteVendita.elementWithKey(
                  ClienteVendita.class, key, PersistentObject.NO_LOCK
                );
                        }
      catch (SQLException e) {
        Trace.printStackTrace();
      }

      char tipoDataPrezziSconti = '\0';
                        if (cliente == null) {
        tipoDataPrezziSconti = PersDatiVen.getCurrentPersDatiVen().getTipoDataPrezziSconti();
      }
      else {
        tipoDataPrezziSconti = cliente.getRifDataPerPrezzoSconti();
      }

      return
                                getCondizioniVendita(
                                        idListino, idCliente, idArticolo, idEsternoConfigurazione,
          idUMVendita, qtaVendita, qtaMagazzino, idModPagamento,
          dtOrdine, dtConsegna, idAgente, idSubagente, idUMMagazzino, idValuta,
                                        new Character(tipoDataPrezziSconti).toString()
                                );
            //Fix 4003 - fine
    }*/

//MG FIX 4348 : per mantenere compatibilita con codice precedente
    public static CondizioniDiVendita getCondizioniVendita(
                                        String idListino,
                                        String idCliente,
                                        String idArticolo,
                                        String idEsternoConfigurazione,  //...FIX02182 - DZ
                                        String idUMVendita,
                                        String qtaVendita,
                                        String qtaMagazzino,
                                        String idModPagamento,
                                        String dtOrdine,
                                        String dtConsegna,
                                        String idAgente,
                                        String idSubagente,
                                        String idUMMagazzino,
                                        String idValuta,
                                        String rifDataPrezzoSconti,
                                        String idUMSecMag, // Fix 13211
                                        String qtaSecMag // Fix 13211
                                        ) {
      return getCondizioniVendita(idListino,idCliente,
                                  idArticolo,idEsternoConfigurazione,  //...FIX02182 - DZ
                                  idUMVendita,qtaVendita,
                                  qtaMagazzino,idModPagamento,
                                  dtOrdine,dtConsegna,
                                  idAgente,idSubagente,
                                  idUMMagazzino,idValuta,
                                  rifDataPrezzoSconti,null,null,null, "1", null,
                                  idUMSecMag, qtaSecMag //Fix 13211
        );
    }
//MG FIX 4348

    public static CondizioniDiVendita getCondizioniVendita(
                                        String idListino,
                                        String idCliente,
                                        String idArticolo,
                                        String idEsternoConfigurazione,  //...FIX02182 - DZ
                                        String idUMVendita,
                                        String qtaVendita,
                                        String qtaMagazzino,
                                        String idModPagamento,
                                        String dtOrdine,
                                        String dtConsegna,
                                        String idAgente,
                                        String idSubagente,
                                        String idUMMagazzino,
                                        String idValuta,
                                        String rifDataPrezzoSconti,
                                        //MG FIX 4348
                                        String prcScontoIntestatario,
                                        String prcScontoModalita,
                                        String idScontoModalita,
                                        //MG FIX 4348
                                        // fix 11156
                                        String idVersione,
                                        String numeroImballo,
                                        // fine fix 11156
                                        // Fix 13211 inzio
                                        String idUMSecMag,
                                        String quantSecMag
                                        // Fix 13211 fine
                                        ) {
      return getCondizioniVendita(idListino, idCliente, idArticolo, idEsternoConfigurazione,
                                  idUMVendita, qtaVendita, qtaMagazzino, idModPagamento,
                                  dtOrdine, dtConsegna, idAgente, idSubagente, idUMMagazzino,
                                  idValuta, rifDataPrezzoSconti, false,
                                  //MG FIX 4348
                                  prcScontoIntestatario,prcScontoModalita,idScontoModalita,
                                  //MG FIX 4348
                                  // fix 11156
                                  idVersione,
                                  numeroImballo,
                                  // fine fix 11156
                                  idUMSecMag, quantSecMag // Fix 13211
                                  );
    }

//MG FIX 4348 : per mantenere compatibilita con docice precedente
    public static CondizioniDiVendita getCondizioniVendita(
                                        String idListino,
                                        String idCliente,
                                        String idArticolo,
                                        String idEsternoConfigurazione,  //...FIX02182 - DZ
                                        String idUMVendita,
                                        String qtaVendita,
                                        String qtaMagazzino,
                                        String idModPagamento,
                                        String dtOrdine,
                                        String dtConsegna,
                                        String idAgente,
                                        String idSubagente,
                                        String idUMMagazzino,
                                        String idValuta,
                                        String rifDataPrezzoSconti,
                                        boolean visualizzaDettagli,
                                        // Fix 13211 inzio
                                        String idUMSecMag,
                                        String qtaSecMag
                                        // Fix 13211 fine
                                        ) {
      return getCondizioniVendita(
                                        idListino,
                                        idCliente,
                                        idArticolo,
                                        idEsternoConfigurazione,  //...FIX02182 - DZ
                                        idUMVendita,
                                        qtaVendita,
                                        qtaMagazzino,
                                        idModPagamento,
                                        dtOrdine,
                                        dtConsegna,
                                        idAgente,
                                        idSubagente,
                                        idUMMagazzino,
                                        idValuta,
                                        rifDataPrezzoSconti,
                                        visualizzaDettagli,  //...FIX04103 - DZ
                                        null,null,null,
                                        idUMSecMag, qtaSecMag // Fix 13211
                                        );
    }
//MG FIX 4348

    //Fix 4003
    //Spostato nei metodi paralleli (senza parametro rifDataPrezzoSconti) il
    //codice relativo al controllo del cliente sul recupero del riferimento
    //data prezzi sconti
    public static CondizioniDiVendita getCondizioniVendita(
                                        String idListino,
                                        String idCliente,
                                        String idArticolo,
                                        String idEsternoConfigurazione,  //...FIX02182 - DZ
                                        String idUMVendita,
                                        String qtaVendita,
                                        String qtaMagazzino,
                                        String idModPagamento,
                                        String dtOrdine,
                                        String dtConsegna,
                                        String idAgente,
                                        String idSubagente,
                                        String idUMMagazzino,
                                        String idValuta,
                                        String rifDataPrezzoSconti,
                                        boolean visualizzaDettagli,  //...FIX04103 - DZ
                                        //MG FIX 4348
                                        String prcScontoIntestatario,
                                        String prcScontoModalita,
                                        String idScontoModalita,
                                        //MG FIX 4348
                                        // fix 11156
                                        String idVersione,
                                        String numeroImballo,
                                        // fine fix 11156
                                        // Fix 13211 inzio
                                        String idUMSecMag,
                                        String qtaSecMag
                                        // Fix 13211 fine
                                        ) {
    	 CondizioniDiVenditaParams condVenParams = (CondizioniDiVenditaParams)Factory.createObject(CondizioniDiVenditaParams.class); 
       condVenParams = condVenParams.impostaParamsCondizioniDiVendita(null,
       		Azienda.getAziendaCorrente(), // LTB to be verified
       		idListino,
           idCliente,
           idArticolo,
           idEsternoConfigurazione,
           idUMVendita,
           qtaVendita,
           qtaMagazzino,
           idModPagamento,
           dtOrdine,
           dtConsegna,
           idAgente,
           idSubagente,
           idUMMagazzino,
           idValuta,
           rifDataPrezzoSconti,
           visualizzaDettagli,
           prcScontoIntestatario,
           prcScontoModalita,
           idScontoModalita,
           idVersione,
           numeroImballo,
           idUMSecMag,
           qtaSecMag
       		); 
       
       CondizioniDiVendita condVen = RicercaCondizioniDiVendita.getCondizioniVendita(condVenParams);
       return condVen;
    	
    	
    	
    	/*
      CondizioniDiVendita condVen = null;
      try {
        DecimalType decType = new DecimalType();
        DateType dateType = new DateType();

        String azienda = Azienda.getAziendaCorrente();

        //Recupera l'oggetto ListinoVendita
        String key = KeyHelper.buildObjectKey(
                       new String[] {azienda, idListino}
                     );
        ListinoVendita listino =
          ListinoVendita.elementWithKey(key, PersistentObject.NO_LOCK);
        Trace.println(">>>>>>>>>>>>>>listino="+listino);

        //Recupera l'oggetto Articolo
        key = KeyHelper.buildObjectKey(
          new String[] {azienda, idArticolo}
        );
        Articolo articolo =
          Articolo.elementWithKey(key, PersistentObject.NO_LOCK);
        Trace.println(">>>>>>>>>>>>>>articolo="+articolo);

        if (articolo != null) {		//Fix 5706

          //...FIX02182 - DZ
          // Inizio 3834
          Configurazione configurazione = ConfigurazioneRicEnh.recuperaConfigurazione(azienda, articolo.getIdArticolo(), idEsternoConfigurazione);
          //Configurazione configurazione = ConfigurazioneRic.recuperaConfigurazione(azienda, idEsternoConfigurazione);
          // Fine 3834
          Trace.println(">>>>>>>>>>>>>>configurazione=" + configurazione);
          //...fine FIX02182 - DZ

          //Recupera l'oggetto ClienteVendita
          key = KeyHelper.buildObjectKey(
            new String[] {azienda, idCliente}
          );
          ClienteVendita cliente = (ClienteVendita)
            ClienteVendita.elementWithKey(
              ClienteVendita.class, key, PersistentObject.NO_LOCK
            );
          Trace.println(">>>>>>>>>>>>>>cliente="+cliente);

          //Recupera l'oggetto UnitaMisura (vendita)
          //fix 5330 inizio
          UnitaMisura umVendita = UnitaMisura.getUM(idUMVendita);
          
          //key = KeyHelper.buildObjectKey(        new String[] {azienda, idUMVendita});
          //UnitaMisura umVendita = UnitaMisura.elementWithKey(key, PersistentObject.NO_LOCK);
          
          //fix 5330 fine
          Trace.println(">>>>>>>>>>>>>>um vendita="+umVendita);

          //Recupera la quantità di vendita
          qtaVendita = decType.unFormat(qtaVendita);  //Fix 1010
          // fix 12639 >
          BigDecimal  quantitaVendita  = null;
          try {
          quantitaVendita =
            new BigDecimal(
              ((Double)(decType.stringToObject(qtaVendita))).doubleValue()
            );
          }
          catch (Throwable t){}
          // fix 12639 <
          // Fix 1791
          quantitaVendita = quantitaVendita.setScale(dammiLaScala(qtaVendita), BigDecimal.ROUND_HALF_UP);
          // Fine fix 1791
          Trace.println(">>>>>>>>>>>>>>quantVendita="+quantitaVendita);

          //Recupera la quantità di magazzino
          qtaMagazzino = decType.unFormat(qtaMagazzino);  //Fix 1010
          BigDecimal quantitaMagazzino =
            new BigDecimal(
              ((Double)(decType.stringToObject(qtaMagazzino))).doubleValue()
            );
          // Fix 1791
          quantitaMagazzino = quantitaMagazzino.setScale(dammiLaScala(qtaMagazzino), BigDecimal.ROUND_HALF_UP);
          // Fine fix 1791
          Trace.println(">>>>>>>>>>>>>>quantMagazzino="+qtaMagazzino);

          //Recupera l'oggetto ModalitaPagamento
          key = KeyHelper.buildObjectKey(
            new String[] {azienda, idModPagamento}
          );
          //fix 5330 inizio
          ModalitaPagamento modPagamento = (ModalitaPagamento)PersistentObject.readOnlyElementWithKey(ModalitaPagamento.class, key);
          
          //ModalitaPagamento modPagamento =
            //ModalitaPagamento.elementWithKey(key, PersistentObject.NO_LOCK);
          
          //fix 5330 fine
          Trace.println(">>>>>>>>>>>>>>modPagamento="+modPagamento);

          //Recupera la data di validità
          Date dataValid = null;
          //Fix 4003 - inizio
          //Fix 4140 - inizio
          //Fix 7024 - inizio: aggiunta seconda condizione
          if (rifDataPrezzoSconti == null || rifDataPrezzoSconti.length() == 0) {
          //Fix 7024 - fine
               if (cliente == null) {
                  rifDataPrezzoSconti = new Character(PersDatiVen.getCurrentPersDatiVen().getTipoDataPrezziSconti()).toString();
            }
            else {
               rifDataPrezzoSconti = new Character(cliente.getRifDataPerPrezzoSconti()).toString();
            }
          }
          //Fix 4140 - fine
          char cRif = rifDataPrezzoSconti.charAt(0);
          if (cRif == RifDataPrzScn.DA_CONDIZIONI_GENERALI) {
             cRif = PersDatiVen.getCurrentPersDatiVen().getTipoDataPrezziSconti();
          }
          //Fix 4003 - fine
          switch (cRif) {
            case RifDataPrzScn.DATA_ORDINE:
              dataValid = (Date)dateType.stringToObject(dtOrdine);
              break;
            case RifDataPrzScn.DATA_CONSEGNA:
              dataValid = (Date)dateType.stringToObject(dtConsegna);
              break;
          }
          Trace.println(">>>>>>>>>>>>>>dataValid="+dataValid);

  //MG FIX 4348
          //Recupera lo sconto intestatario
          BigDecimal prcScontoIntestatarioDec = new BigDecimal(0);
          if (prcScontoIntestatario != null && !prcScontoIntestatario.equals("")) {
            prcScontoIntestatario = decType.unFormat(prcScontoIntestatario);  //Fix 1010
            prcScontoIntestatarioDec =
                new BigDecimal(
                ((Double)(decType.stringToObject(prcScontoIntestatario))).doubleValue()
                );

            prcScontoIntestatarioDec = prcScontoIntestatarioDec.setScale(dammiLaScala(prcScontoIntestatario), BigDecimal.ROUND_HALF_UP);
          }
          //Recupera lo sconto modalita
          BigDecimal prcScontoModalitaDec = new BigDecimal(0);
          if (prcScontoModalita != null && !prcScontoModalita.equals("")) {
            prcScontoModalita = decType.unFormat(prcScontoModalita);  //Fix 1010
            prcScontoModalitaDec =
                new BigDecimal(
                ((Double)(decType.stringToObject(prcScontoModalita))).doubleValue()
                );

            prcScontoModalitaDec = prcScontoModalitaDec.setScale(dammiLaScala(prcScontoModalita), BigDecimal.ROUND_HALF_UP);
          }
  //MG FIX 4348


          //Recupera il flag di attivazione ListinoCampagna
          boolean attivaListinoCampagna =
             PersDatiVen.getCurrentPersDatiVen().getListinoCampagna() != null;
          Trace.println(">>>>>>>>>>>>>>attivaListinoCampagna="+attivaListinoCampagna);

          //Recupera l'oggetto Agente
          key = KeyHelper.buildObjectKey(
            new String[] {azienda, idAgente}
          );
          //fix 5330 inizio
          Agente agente = (Agente)PersistentObject.readOnlyElementWithKey(Agente.class, key);
          //Agente agente = Agente.elementWithKey(key, PersistentObject.NO_LOCK);
          //fix 5330 fine
          Trace.println(">>>>>>>>>>>>>>agente="+agente);

          //Recupera l'oggetto Subagente
          key = KeyHelper.buildObjectKey(
            new String[] {azienda, idSubagente}
          );
          //fix 5330 inizio
          Agente subagente = (Agente)PersistentObject.readOnlyElementWithKey(Agente.class, key);
          //Agente subagente = Agente.elementWithKey(key, PersistentObject.NO_LOCK);
          //fix 5330 fine
          Trace.println(">>>>>>>>>>>>>>subagente="+subagente);

          //Recupera l'oggetto UnitaMisura (primaria magazzino)
          //fix 5330 inizio
          UnitaMisura umMagazzino = UnitaMisura.getUM(idUMMagazzino);
          
          //key = KeyHelper.buildObjectKey(new String[] {azienda, idUMMagazzino});
          //UnitaMisura umMagazzino = UnitaMisura.elementWithKey(key, PersistentObject.NO_LOCK);
          
          //fix 5330 fine
          Trace.println(">>>>>>>>>>>>>>um magazzino="+umMagazzino);

          //Recupera l'oggetto Valuta
          key = KeyHelper.buildObjectKey(
            new String[] {idValuta}
          );
          //fix 5330 inizio
          Valuta valuta = (Valuta)PersistentObject.readOnlyElementWithKey(Valuta.class, key);
          
          //Valuta valuta =
            //Valuta.elementWithKey(key, PersistentObject.NO_LOCK);
          
          //fix 5330 fine
          Trace.println(">>>>>>>>>>>>>>valuta="+valuta);

          // fix 11156
          ArticoloVersione versione = ArticoloVersione.elementWithKey(KeyHelper.buildObjectKey(new String[]{azienda,idArticolo,idVersione}),PersistentObject.NO_LOCK);
          BigDecimal numImballo=null;
          // fix 11766 >
          if (numeroImballo != null && !numeroImballo.trim().equals("") && !(numeroImballo.trim().equals("undefined"))) { // fix 11951 // fix 12639
             try {
               numImballo = new BigDecimal(numeroImballo);
               if (numImballo == null) {
                if (numeroImballo!=null && !(numeroImballo.trim().equals("") && !(numeroImballo.trim().equals("undefined")))) { // fix 12639
                  numeroImballo = decType.unFormat(numeroImballo);
                  Object obj = decType.stringToObject(numeroImballo);
                  if (obj != null) {
                     numImballo = new BigDecimal(((Double)obj).doubleValue());
                     numImballo = numImballo.setScale(dammiLaScala(numeroImballo), BigDecimal.ROUND_HALF_UP);
                  }
                }
              }
             }
             catch (NumberFormatException ex) {
               
             }
          }
          
          //if (numImballo == null) {
            // if (numeroImballo!=null && !(numeroImballo.trim().equals(""))){
              // numeroImballo = decType.unFormat(numeroImballo);
               // numImballo =
                  // new BigDecimal(
                    //   ( (Double) (decType.stringToObject(numeroImballo))).
                      // doubleValue()
                   //);
               //numImballo = numImballo.setScale(dammiLaScala(numeroImballo),
                 //  BigDecimal.ROUND_HALF_UP);
             //}
             // fine fix 11156
             // }
           
          // fix 11766 <
          // fix 12639 <

          //Fix 13211 inzio
          UnitaMisura umSecMag = null;
          if (idUMSecMag!=null)
            umSecMag = UnitaMisura.getUM(idUMSecMag);
          if (umSecMag != null)
            qtaSecMag = decType.unFormat(qtaSecMag);
          BigDecimal quantSecMagazzino = null;
          if (qtaSecMag != null && !qtaSecMag.equals("")) {
            quantSecMagazzino =
              new BigDecimal(
                ((Double) (decType.stringToObject(qtaSecMag))).doubleValue()
              );
          }
          //Fix 13211 fine

          //Istanzia l'oggetto RicercaCondizioniDiVendita e recupera l'oggetto
          //CondizioniDiVendita
              //Fix 3223 - inizio
          RicercaCondizioniDiVendita ricerca =
            (RicercaCondizioniDiVendita)Factory.createObject(RicercaCondizioniDiVendita.class);
              //Fix 3223 - fine
          condVen =
              ricerca.ricercaCondizioniDiVendita(azienda,  //...FIX02182 - DZ
                listino, cliente, articolo, configurazione, umVendita, quantitaVendita, new BigDecimal(0.0),
                modPagamento, dataValid, agente, subagente,
                umMagazzino, quantitaMagazzino, valuta, visualizzaDettagli,
  //MG FIX 4348
                prcScontoIntestatarioDec, prcScontoModalitaDec, idScontoModalita,
  //MG FIX 4348
  // fix 11156
                versione, numImballo,
  // fine fix 11156
                umSecMag, quantSecMagazzino // fix 13211
              );
          //Fix 2138 - inizio
          if (condVen != null && cliente != null) { //...FIX04103 - DZ
          //Fix 2138 - fine
            //Fix 1328 - inizio
            BigDecimal prezzo = condVen.getPrezzo();
            if (prezzo == null || prezzo.equals(new BigDecimal(0.0))) {
              ListinoVendita listinoCliente = cliente.getListino();
              ListinoVendita listinoAlternativo = cliente.getListinoAlternativo();
              if (listinoCliente != null && listinoAlternativo != null &&
                  //listinoCliente.equals(listinoAlternativo)) {  //Fix 23738 PM
                  !listinoCliente.equals(listinoAlternativo)) { //Fix 23738 PM
                condVen = ricerca.ricercaCondizioniDiVendita(azienda,  //...FIX02182 - DZ
                  listinoAlternativo, cliente, articolo, configurazione, umVendita, quantitaVendita,
                  new BigDecimal(0.0), modPagamento, dataValid, agente, subagente,
                  umMagazzino, quantitaMagazzino, valuta,
  //MG FIX 4348
                prcScontoIntestatarioDec, prcScontoModalitaDec, idScontoModalita,
  //MG FIX 4348
  // fix 11156
                versione, numImballo,
  // fine fix 11156
                umSecMag, quantSecMagazzino // Fix 13211
                );
              }
            }
            //Fix 1328 - fine
          }  //Fix 2138

          Trace.println(">>>>>>>>>>>>>>CondizioniDiVendita="+condVen);
        }
      }
      catch (Exception ex) {
        ex.printStackTrace(Trace.excStream); // fix 12767
      }

      return condVen;*/
    }


    /**
     * Aggiunto con FIX02182 - DZ.
     * Spostato dalla servlet RecuperaDatiVendita.
     */
    //private static int dammiLaScala(String num){ //24273
    public static int dammiLaScala(String num){    //24273
      int scala = 2;
      int virgola = num.indexOf(DecimalType.getDecimalSeparator()) + 1;
      if (virgola>0){
        int newScala = num.length()- virgola;
        if (newScala>2)
          scala = newScala;
      }
      return scala;
    }

  /*
   * Revisions:
   * Date          Owner
   * 18/06/2002    Daniela Battistoni
   * carica i parametri allo statement in base al tipo di testata. Lo statement
   * viene passato al metodo in quanto questo metodo viene utilizzato sia per la
   * ricerca nel listino vendita che nel listino sconti.
   */
  protected boolean caricaParametriTestata(CachedStatement CS, char testata) throws SQLException
  {
    if (testata=='0') return false;

    PreparedStatement ps = CS.getStatement();
    Database db = ConnectionManager.getCurrentDatabase();
    String rCliente=new String();
    String rZona=new String();
    String rCatVen=new String();

    // sto lavorando per voi
    if (this.getCondizioniDiVendita().getCliente()!=null){
      rCliente = KeyHelper.getTokenObjectKey(this.getCondizioniDiVendita().getClienteKey(), 2);
      rZona =  this.getCondizioniDiVendita().getCliente().getCliente().getIdZona(); 
      rCatVen = this.getCondizioniDiVendita().getCliente().getIdCategoriaVenditaCliente();
    }
    if(this.getCondizioniDiVendita().getIdZona() != null)// || !this.getCondizioniDiVendita().getIdZona().equals(""))//fix 41247 In caso del parametro IdZona == "" vale a dire che non è stato passato come parametro allora usare la zona del cliente
    	// Normalmente questo caso non è possibie a standard (except una dimenticanza) ma possibile in una personalizazione che usa un vechio metodo deprecated 
    	rZona = this.getCondizioniDiVendita().getIdZona();//fix 41247 
    if( this.getCondizioniDiVendita().getIdCategoriaVendita()!=null) //|| !this.getCondizioniDiVendita().getIdCategoriaVendita().equals(""))//Fix 43127 
        rCatVen = this.getCondizioniDiVendita().getIdCategoriaVendita();//Fix 43127
    Map m = new HashMap();
    m.put("2", new Oggettino(0,rCliente));
    m.put("3", new Oggettino(0,rZona));
    m.put("4", new Oggettino(0,rCatVen));

    if (testata!='1'){
      Oggettino o = (Oggettino) m.get(new String(new char[]{testata}));
      o.carico = 1;
    }
    boolean boo = this.caricaMap(0,m,ps);
    return boo;
  }

    /**
     * Carica i parametri allo statement in base al tipo di riga. Lo statement
     * viene passato al metodo in quanto questo metodo viene utilizzato sia per la
     * ricerca nel listino vendita che nel listino sconti.
     */
  /*
   * Revisions:
    * Number  Date         Owner   Description
    *         18/06/2002   DB
    * 02182   30/06/2004   DZ      Aggiunto parametro ricercaConCOnfigurazione.
   */
    protected boolean caricaParametriRiga(CachedStatement CS, char riga, boolean ricercaConConfigurazione) throws SQLException
  {
        if (riga =='0')
            return false;

    PreparedStatement ps = CS.getStatement();
    Database db = ConnectionManager.getCurrentDatabase();

        String rArticolo       = new String();
        String rConfigurazione = new String();   //...FIX02182 - DZ
        String rUnitaMisura    = new String();
        String rCatPrz         = new String();
        String rLineaProdotto  = new String();
        String rMacrofamiglia  = new String();
        String rSubfamiglia    = new String();
        String rMicrofamiglia  = new String();

        if ( this.getCondizioniDiVendita().getArticolo()!=null)
        {
            rArticolo = getCondizioniDiVendita().getRArticolo();
            Integer idConfig = getCondizioniDiVendita().getRConfigurazione();   //...FIX02182 - DZ
            rConfigurazione = idConfig == null ? null : idConfig.toString();    //...FIX02182 - DZ
            rUnitaMisura = getCondizioniDiVendita().getRUnitaMisura();
            rCatPrz = KeyHelper.getTokenObjectKey(getCondizioniDiVendita().getArticolo().getCategoriaPrezzoKey(),2);
            rLineaProdotto = KeyHelper.getTokenObjectKey(getCondizioniDiVendita().getArticolo().getLineaProdottoKey(),2);
            rMacrofamiglia = KeyHelper.getTokenObjectKey(getCondizioniDiVendita().getArticolo().getMacroFamigliaKey(),2);
            rSubfamiglia = KeyHelper.getTokenObjectKey(getCondizioniDiVendita().getArticolo().getSubFamigliaKey(),2);
            rMicrofamiglia = KeyHelper.getTokenObjectKey(getCondizioniDiVendita().getArticolo().getMicroFamigliaKey(),2);
    }

    Map m = new HashMap();
        m.put("1", new Oggettino(0, rArticolo));
        m.put("2", new Oggettino(0, rMicrofamiglia));
        m.put("3", new Oggettino(0, rSubfamiglia));
        m.put("4", new Oggettino(0, rMacrofamiglia));
        m.put("5", new Oggettino(0, rLineaProdotto));
        m.put("6", new Oggettino(0, rCatPrz));
        m.put("7", new Oggettino(0, rUnitaMisura));
        m.put("8", new Oggettino(0, rConfigurazione));   //...FIX02182 - DZ

    Oggettino o = (Oggettino) m.get(new String(new char[]{riga}));
    o.carico = 1;

        if (riga == TipologieTestateRighe.ARTICOLO)
        {
      Oggettino o11 = (Oggettino) m.get("7");
      o11.carico = 1;
            //...FIX02182 - DZ
            if (ricercaConConfigurazione)
            {
                o11 = (Oggettino) m.get("8");
                o11.carico = 1;
            }
            //...fine FIX02182 - DZ
    }

        switch (riga)
        {
      case TipologieTestateRighe.LINEAMACROSUBMICRO:
        Oggettino o1 = (Oggettino) m.get("3");
        o1.carico = 1;
      case TipologieTestateRighe.LINEAMACROSUB:
        Oggettino o2 = (Oggettino) m.get("4");
        o2.carico = 1;
      case TipologieTestateRighe.LINEAMACRO:
        Oggettino o3 = (Oggettino) m.get("5");
        o3.carico = 1;
        break;
    }

        //boolean boo = this.caricaMap(14, m, ps); //24273
        boolean boo = this.caricaMap(getPartoParametriRiga(), m, ps);//24273
    return boo;
  }

  /*
   * Revisions:
   * Date          Owner
   * 18/06/2002    Daniela Battistoni
   * carica i parametri generali. Lo statement
   * viene passato al metodo in quanto questo metodo viene utilizzato sia per la
   * ricerca nel listino vendita che nel listino sconti.
   */
  protected boolean caricaParametriGenerali(CachedStatement CS, String RListino, String IdAzienda, char testata, char riga, Date data) throws SQLException
  {
    PreparedStatement ps = CS.getStatement();
    Database db = ConnectionManager.getCurrentDatabase();
    if (RListino==null || data==null || IdAzienda==null) return false;

    db.setString(ps,1,RListino);

    db.setString(ps,2,String.valueOf(testata));
    db.setString(ps,3,String.valueOf(riga));

    ps.setDate(13,data);
    ps.setDate(14,data);
        ps.setDate(39,data);  //...FIX02182 - DZ
        ps.setDate(40,data);  //...FIX02182 - DZ

        db.setString(ps,41,IdAzienda);  //...FIX02182 - DZ

    return true;
  }

    /**
   * il metodo ricerca la riga del listino vendita passato al metodo.
   * La ricerca la fa attraverso un ciclo sulle tipologie testate e le tipologie
   * riga nell'ordine di priorità indicate nel listino.
   * Per ogni tipologia testata e tipologia riga, fino a che non viene ritrovata
   * una riga, carica dei parametri lo statement e lo esegue.
   */
    /*
    * Revisions:
    * Number  Date         Owner   Description
    *         18/06/2002   DB
    * 02182   30/06/2004   DZ      Aggiunto flag ricercaConConfigurazione.
    */
    protected ListinoVenditaRiga individuaRiga(ListinoVendita lv) throws SQLException
  {
      //PreparedStatement ps = STATEMENT.getStatement(); //24273
    	PreparedStatement ps = getStatementRicRigaListino().getStatement(); //24273

    if (lv == null)
      return null;
    String az = lv.getIdAzienda();
    String ls = lv.getIdListino();

    //Iterator i =(lv.ordinaTestate()).iterator(); 72296 Remmare
    //72296 Softre --> 
    Iterator i = null;
    List yte = lv.ordinaTestate();
    yOrdinaTestateSelezionate(yte.iterator());
    i = yte.iterator();
    //72296 Softre <--
    ArrayList al =(ArrayList) lv.ordinaRighe();
    
    //...FIX02182 - DZ
    boolean ricercaConConfigurazione = false;
    boolean originalRicercaConConfigurazione = false;  //...FIX02787 - DZ
    //Se la configurazione non è null devo fare due volte la ricerca con tipo riga = Articolo, prima
    //usando la configurazione, poi se la ricerca non ha dato esito positivo ricerco senza configurazione = null.
    if (!al.isEmpty() && iCondizioniDiVendita.getConfigurazione() != null &&
        al.contains(String.valueOf(TipologieTestateRighe.ARTICOLO)))
    {
        String s = String.valueOf(TipologieTestateRighe.ARTICOLO);
        al.add(al.indexOf(s), s);
        originalRicercaConConfigurazione = true;  //...FIX02787 - DZ
    }
    //...fine FIX02182 - DZ

    ResultSet r;

    boolean isPrezzoArticoloCfg = false;
    while (i.hasNext())
    {
      char testata =((String)i.next()).toCharArray()[0];
      ricercaConConfigurazione = originalRicercaConConfigurazione;  //...FIX02787 - DZ
      Iterator j= al.iterator();
      while (j.hasNext())
      {
        isPrezzoArticoloCfg = ricercaConConfigurazione;             //...FIX05141 - DZ
        ps.clearParameters();
        char riga = ((String)j.next()).toCharArray()[0];
        //...FIX02182 - DZ
        //boolean result = this.caricaParametriRiga(STATEMENT, riga, ricercaConConfigurazione); //24273
        boolean result = this.caricaParametriRiga(getStatementRicRigaListino(), riga, ricercaConConfigurazione); //24273

        if (riga == TipologieTestateRighe.ARTICOLO)
          ricercaConConfigurazione = false;
        //...fine FIX02182 - DZ
        if (result)
          //result = this.caricaParametriTestata(STATEMENT, testata); //24273
        	result = this.caricaParametriTestata(getStatementRicRigaListino(), testata);   //24273

        if (result)
          //result = this.caricaParametriGenerali(STATEMENT, ls, az, testata,riga,this.getCondizioniDiVendita().getDataValidita());//24273
        	result = this.caricaParametriGenerali(getStatementRicRigaListino(), ls, az, testata,riga,this.getCondizioniDiVendita().getDataValidita());     //24273   

        if (result)
        {
          r = ps.executeQuery();
          String k ;
          String ky ;
          while(r.next())
          {
            k = Integer.toString(r.getInt("ID_TESTATA"));
            ky = Integer.toString(r.getInt("ID_LST_RIGA"));
            if ( k != null && ky != null)
            {
              String key = KeyHelper.buildObjectKey(new String[]{az, ls, k, ky});

              this.getCondizioniDiVendita().setTipoTestata(testata);
              this.getCondizioniDiVendita().setTipologiaRiga(riga);
              // fix 11779 >
              /*
              // fix 11156
              if (this.getCondizioniDiVendita().isSempreManuale()){
                this.getCondizioniDiVendita().setTipoTestata(TipologieTestateRighe.NESSUNARIGA);
              }
              // fine fix 11156
              */
              // fix 11779 <
              this.getCondizioniDiVendita().setIsPrezzoEsattoArtCfg(isPrezzoArticoloCfg); //...FIX05141 - DZ);
              r.close();
              return ListinoVenditaRiga.elementWithKey(key, PersistentObject.NO_LOCK);
            }
          }
          r.close();
        }
      }
    }
    return null;
  }

    /**
   * il metodo ricerca la riga del listino sconti passato al metodo.
   * La ricerca la fa attraverso un ciclo sulle tipologie testate e le tipologie
   * riga nell'ordine di priorità indicate nel listino.
   * Per ogni tipologia testata e tipologia riga, fino a che non viene ritrovata
   * una riga, carica dei parametri lo statement e lo esegue.
   */
    /*
    * Revisions:
    * Number  Date         Owner   Description
    *         18/06/2002   DB
    * 02182   30/06/2004   DZ      Aggiunto flag ricercaConConfigurazione.
    */
    protected ListinoVenditaScontiRiga individuaRigaSconti(ListinoVenditaSconti lv) throws SQLException
  {
        if (lv == null)
          return null;

    //PreparedStatement ps = STATEMENT_SCONTI.getStatement(); //24273
    PreparedStatement ps = getStatementRicRigaScontiListino().getStatement();  //24273        

    String az = lv.getIdAzienda();
    String ls = lv.getIdListino();

    Iterator i =(lv.ordinaTestate()).iterator();
    ArrayList al =(ArrayList) lv.ordinaRighe();

    	
        //...FIX02182 - DZ
        boolean ricercaConConfigurazione = false;
        //Se la configurazione è null devo fare due volte la ricerca con tipo riga = Articolo, prima
        //usando la configurazione, poi se la ricerca non ha dato esito positivo ricerco con configurazione = null.
        if (!al.isEmpty() && iCondizioniDiVendita.getConfigurazione() != null &&
            al.contains(String.valueOf(TipologieTestateRighe.ARTICOLO)))
        {
            String s = String.valueOf(TipologieTestateRighe.ARTICOLO);
            al.add(al.indexOf(s), s);
            ricercaConConfigurazione = true;
        }
        //...fine FIX02182 - DZ 
        boolean ricercaConConfigurazioneInitialValue = ricercaConConfigurazione;//Fix 42529 
       

    ResultSet r;

        while (i.hasNext())
        {
            ricercaConConfigurazione = ricercaConConfigurazioneInitialValue;//Fix 42529 
        
            
      char testata =((String)i.next()).toCharArray()[0];
      Iterator j = al.iterator();
            while (j.hasNext())
            {
        ps.clearParameters();
        char riga = ((String)j.next()).toCharArray()[0];
                //...FIX02182 - DZ
        				//boolean result = this.caricaParametriRiga(STATEMENT_SCONTI,riga,ricercaConConfigurazione); //24273
								boolean result = this.caricaParametriRiga(getStatementRicRigaScontiListino(), riga, ricercaConConfigurazione); //24273

                if (riga == TipologieTestateRighe.ARTICOLO)
                    ricercaConConfigurazione = false;
                //...fine FIX02182 - DZ
                if (result)
                  //result = this.caricaParametriTestata(STATEMENT_SCONTI,testata); //24273
              		result = this.caricaParametriTestata(getStatementRicRigaScontiListino(), testata); //24273                

                if (result)
                  //result = this.caricaParametriGenerali(STATEMENT_SCONTI,ls, az, testata,riga,this.getCondizioniDiVendita().getDataValidita());//24273
              		result = this.caricaParametriGenerali(getStatementRicRigaScontiListino(), ls, az, testata,riga,this.getCondizioniDiVendita().getDataValidita());//24273                

                if (result)
                {
          r = ps.executeQuery();
          String k ;
          String ky ;
                    while(r.next())
                    {
            k = Integer.toString(r.getInt("ID_TESTATA"));
            ky = Integer.toString(r.getInt("ID_LST_RIGA"));
                        if (k!=null&&ky!=null)
                        {
              String key = KeyHelper.buildObjectKey(new String[]{az, ls, k, ky});

              this.getCondizioniDiVendita().setTipoTestataSconto(testata);
              this.getCondizioniDiVendita().setTipologiaRigaSconto(riga);
              r.close();
              return ListinoVenditaScontiRiga.elementWithKey(key, PersistentObject.NO_LOCK);
            }
          }
          r.close();

        }
      }
    }
    return null;
  }

  /**
   * FIX04632 - DZ.
   * @return BigDecimal
   */
  protected BigDecimal getQuantitaForScaglioni(){
    // fix 11156
    boolean perNumeroImballi = this.getCondizioniDiVendita().getArticolo().hasVersioneEstesa();
    // fine fix 11156
    ClienteVendita clienteVendita = getCondizioniDiVendita().getCliente();
    String idCliente = clienteVendita != null ? clienteVendita.getIdCliente() : "";
    String where = ArticoloClienteTM.ID_AZIENDA + "='" + getCondizioniDiVendita().getIdAzienda() + "' AND " +
                   ArticoloClienteTM.ID_CLIENTE + "='" + idCliente + "' AND " +
                   ArticoloClienteTM.ID_ARTICOLO + "='" + getCondizioniDiVendita().getArticolo().getIdArticolo() + "'";

    // fix 11344 >
        where += " AND " + ArticoloClienteTM.STATO + "='" + DatiComuniEstesi.VALIDO + "'";
    ArticoloCliente articoloCliente = null;
    char UMRifScaglioni = ArticoloIntestatario.UM_SCG_VEN_ACQ;

    if (perNumeroImballi && PersDatiVen.getCurrentPersDatiVen().hasNumeroImballiUMScg()) {
      UMRifScaglioni = ArticoloIntestatario.UM_SCG_NUM_IMB;
    }
    // fix 11304 >
    if (perNumeroImballi && getCondizioniDiVendita().getConfigurazione()!=null ) {
        String whereCfg = where + " AND " + ArticoloClienteTM.ID_CONFIGURAZIONE + "=" + getCondizioniDiVendita().getConfigurazione().getIdConfigurazione() + "";
        try{
        List articoliCliente = ArticoloCliente.retrieveList(whereCfg, "", false);
        if (articoliCliente != null && !articoliCliente.isEmpty()){
          articoloCliente = (ArticoloCliente)articoliCliente.get(0);
          UMRifScaglioni = articoloCliente.getUMRiferimentoScaglioni();
        }
      }
      catch(Throwable t){
        t.printStackTrace(Trace.excStream);
      }
    }
    // fix 11304 <
    if (articoloCliente == null) {
        try{
                List articoliCliente = ArticoloCliente.retrieveList(where, "", false);
                if (articoliCliente != null && !articoliCliente.isEmpty()){
                        articoloCliente = (ArticoloCliente)articoliCliente.get(0);
                        UMRifScaglioni = articoloCliente.getUMRiferimentoScaglioni();
                }
        }
        catch(SQLException ex){
                ex.printStackTrace(Trace.excStream);
        }
        catch(IllegalAccessException ex){
                ex.printStackTrace(Trace.excStream);
        }
        catch(InstantiationException ex){
                ex.printStackTrace(Trace.excStream);
        }
        catch(ClassNotFoundException ex){
                ex.printStackTrace(Trace.excStream);
        }
    }
    // fix 11344 <

    if (UMRifScaglioni == ArticoloIntestatario.UM_SCG_PRM_MAG){
      // Fix 13211 inzio
//      Articolo articolo = getCondizioniDiVendita().getArticolo();
//      return articolo.convertiUM(getCondizioniDiVendita().getQuantita(),
//                                 getCondizioniDiVendita().getUnitaMisura(),
//                                 articolo.getUMPrmMag());
      return getCondizioniDiVendita().getQuantitaPrmMag();
      // Fix 13211 fine
    }
    if (UMRifScaglioni == ArticoloIntestatario.UM_SCG_SEC_MAG){
      // Fix 13211 fine
      Articolo articolo = getCondizioniDiVendita().getArticolo();
//      return articolo.convertiUM(getCondizioniDiVendita().getQuantita(),
//                                 getCondizioniDiVendita().getUnitaMisura(),
//                                 articolo.getUMSecMag());
      if (articolo.getUMSecMag()!= null)
        return getCondizioniDiVendita().getQuantitaSecMag();
      else
        return getCondizioniDiVendita().getQuantita();
      // Fix 13211 inzio
    }
    // fix 11156
    if (perNumeroImballi && UMRifScaglioni == ArticoloIntestatario.UM_SCG_NUM_IMB && this.getCondizioniDiVendita().getVersione()!=null){
      if (this.getCondizioniDiVendita().getNumeroImballo()!=null){
         return this.getCondizioniDiVendita().getNumeroImballo();
      }
      // fix 11779 >
      BigDecimal ritorno = this.getCondizioniDiVendita().getVersione().calcolaNumeroImballo(getCondizioniDiVendita().getQuantita(), getCondizioniDiVendita().getUnitaMisura());
      this.getCondizioniDiVendita().setNumeroImballo(ritorno);
      return ritorno;
      /*
      BigDecimal fattore = this.getCondizioniDiVendita().getVersione().getFattoreConvNI();
      char UMRiferimento = this.getCondizioniDiVendita().getVersione().getUMRifFattoreConvNI();
      BigDecimal quantita = new BigDecimal("0");
      if (UMRiferimento==ArticoloVersionePO.UM_SECONDARIA ){
        Articolo articolo = getCondizioniDiVendita().getArticolo();
        quantita = articolo.convertiUM(getCondizioniDiVendita().getQuantita(),
                                 getCondizioniDiVendita().getUnitaMisura(),
                                 articolo.getUMSecMag(), this.getCondizioniDiVendita().getVersione());
      }
      else {
        Articolo articolo = getCondizioniDiVendita().getArticolo();
        quantita = articolo.convertiUM(getCondizioniDiVendita().getQuantita(),
                                 getCondizioniDiVendita().getUnitaMisura(),
                                 articolo.getUMPrmMag(), this.getCondizioniDiVendita().getVersione());
      }
      if (fattore!=null && fattore.compareTo(new BigDecimal("0"))!=0 && quantita!=null){
        BigDecimal ritorno = quantita.divide(fattore,2,BigDecimal.ROUND_HALF_UP);
        this.getCondizioniDiVendita().setNumeroImballo(ritorno);
        return ritorno;
      }
      */
      // fix 11779 <
    }
    // fine fix 11156
    return getCondizioniDiVendita().getQuantita();
  }

  /*
   * Revisions:
   * Date          Owner
   * 18/06/2002    Daniela Battistoni
   * il metodo ricerca lo scaglione della riga.
   * La ricerca viene eseguita ricercando lo scaglione tra quelli della riga il base
   * ad un metodo della riga.
   * sucessivamente viene verificato se esiste un omaggio.
   */
  protected ListinoVenditaScaglione individuaScaglione(ListinoVenditaRiga lvr)
  {
    ListinoVenditaScaglione lvs;
    if (lvr!=null) {
      lvs = lvr.caricaScaglione(getQuantitaForScaglioni());  //...FIX04632 - DZ
      getCondizioniDiVendita().setListinoVenditaScaglione(lvs);
    }
    else
      return null;

    if (lvs != null){
      getCondizioniDiVendita().setOmaggioOfferta(lvs.getOffertaOmaggio());
      getCondizioniDiVendita().setAzzeraScontiCliFor(lvs.getAzzeraScontiCliFor());  //...FIX03085 - DZ
    }

    return lvs;
  }

  /**
   * FIX04103 - DZ.
   * @param lvs ListinoVenditaScaglione
   * @return ListinoVenditaScaglione
   */
  
  protected void valorizzaDettagliArtCfg (ListinoVenditaScaglione lvs, boolean visualizzaDettagli){
	  valorizzaDettagliArtCfg (lvs, visualizzaDettagli, null); //Fix 39206
  }

  //protected void valorizzaDettagliArtCfg (ListinoVenditaScaglione lvs, boolean visualizzaDettagli){ //Fix 39206
  protected void valorizzaDettagliArtCfg (ListinoVenditaScaglione lvs, boolean visualizzaDettagli, String rifDataPrezzoSconti){ //Fix 39206
    Configurazione cfg = iCondizioniDiVendita.getConfigurazione();
    if (cfg != null){
      HashMap variabili = cfg.getVariabiliValori();
      for (Iterator i = variabili.keySet().iterator(); i.hasNext(); ){
        VariabileSchemaCfg variabile = (VariabileSchemaCfg)i.next();
        ValoreVariabileCfg valore = (ValoreVariabileCfg)variabili.get(variabile);
        if (valore.getArticolo() != null){
          ListinoVenditaRiga riga = null;
          ListinoVenditaTestata testata = null;
          ListinoVendita listino = null;
          String idListino = null;
          ClienteVendita clienteVen = null;
          if (lvs != null){
            riga = lvs.getListinoVenditaRiga();
            testata = riga.getListinoVenditaTestata();
            listino = testata.getListino();
            idListino = lvs.getIdListino();
            clienteVen = testata.getCliente();
          }
          else{
            listino = getCondizioniDiVendita().getListinoVendita();
            if (listino != null) //Fix 15647
              idListino = listino.getIdListino();
            clienteVen = getCondizioniDiVendita().getCliente();
          }
          //Fix 12321 --inizio
          DecimalType decType = new DecimalType();
          decType.setScale(6);
          BigDecimal quantita = calcolaQuantita(cfg, variabile, valore);
          //Fix 12321 --fine
          //Fix 24634 - inizio
          //Articolo articolo = valore.getArticolo();
          Articolo articolo = getArticoloDettagliArtCfg(cfg, valore);
          //Fix 24634 - fine
          UnitaMisura umVen = articolo.getUMDefaultVendita();  //...FIX04663 - DZ
          UnitaMisura umMag = articolo.getUMPrmMag();
          //Fix 12321 --inizio
          //BigDecimal qtaMag = articolo.convertiUM(new BigDecimal("1.00"), umVen, umMag);
          BigDecimal qtaMag = articolo.convertiUM(quantita, umVen, umMag);
          //Fix 12321 --fine
          Integer idConfig = valore.getIdConfigurazione();
          // fix 13211 inzio
          UnitaMisura umSecMag = articolo.getUMSecMag();
          BigDecimal qtaSecMag = null;
          if (umSecMag!=null )
            qtaSecMag = articolo.convertiUM(quantita, umVen, umSecMag);
          // fix 13211 fine
          CondizioniDiVendita cdvDettaglio = RicercaCondizioniDiVendita.getCondizioniVendita(
              idListino, iCondizioniDiVendita.getRCliente(),
              //Fix 24634 - inizio
//            valore.getIdArticolo(), idConfig == null ? Configurazione.CONFIGURAZIONE_DUMMY : idConfig,
              articolo.getIdArticolo(), idConfig == null ? Configurazione.CONFIGURAZIONE_DUMMY : idConfig,
              //Fix 24634 - fine
              //Fix 12321 --inizio
              //umVen.getIdUnitaMisura(), "1", qtaMag == null ? "0" : qtaMag.toString(), null,
              umVen.getIdUnitaMisura(), decType.objectToString(quantita), qtaMag == null ? "0" : decType.objectToString(qtaMag), null,
              //Fix 12321 --fine
              iCondizioniDiVendita.getDataValidita().toString(),
              iCondizioniDiVendita.getDataValidita().toString(),
              null, null, umMag.getIdUnitaMisura(),
              //Fix 15647 inizio
              //(clienteVen != null && clienteVen.getCliente().getIdValuta() != null) ? clienteVen.getCliente().getIdValuta() : listino.getIdValuta(),
              (clienteVen != null && clienteVen.getCliente().getIdValuta() != null) ? clienteVen.getCliente().getIdValuta() : (listino==null)?null:listino.getIdValuta(),
              //Fix 15647 fine
              // Fix 13211 inzio
              rifDataPrezzoSconti, //Fix 39206
              (umSecMag==null)?null:umSecMag.getIdUnitaMisura(), qtaSecMag == null ? "0" : decType.objectToString(qtaSecMag)
              // Fix 13211 fine
              );
          if (cdvDettaglio != null){
//            if (visualizzaDettagli)
              cdvDettaglio.setVariabileKey(variabile.getKey()); //Fix 12321
              iCondizioniDiVendita.setDettaglio(cdvDettaglio);
          }
        }
      }
      cfg.resetSchemaVariabili(); //27900
    }
  }

  /*
   * Revisions:
   * Date          Owner
   * 18/06/2002    Daniela Battistoni
   * il metodo ricerca lo scaglione della riga.
   * La ricerca viene eseguita ricercando lo scaglione tra quelli della riga il base
   * ad un metodo della riga.
   */
  protected ListinoVenditaScontiScaglione individuaScaglioneSconti(ListinoVenditaScontiRiga lvr)
  {
    ListinoVenditaScontiScaglione lvs;
    if (lvr!=null) {
       // fix 12951 >
       //lvs = lvr.caricaScaglione(this.iCondizioniDiVendita.getQuantita());
       lvs = lvr.caricaScaglione(getQuantitaForScaglioni());
       // fix 12951 <
      this.getCondizioniDiVendita().setListinoVenditaScontiScaglione(lvs);
     }
    else
      return null;
    return lvs;

  }

  /*
   * Revisions:
   * Date          Owner
   * 18/06/2002    Daniela Battistoni

   * 17/09/2002    Daniela Battistoni
   * (introduce il controllo sull'unità di misura associata al prezzo.
   * Ho modificato la signature del metodo in quanto vengono passati
   * due parametri che sono le due unità di misura)
   * Viene effettuato anche il controllo di uniformità tra la valuta primaria
   * la valuta della ricerca.

   * il metodo calcola i prezzi in base a quanto indicato nella riga
   * di scaglione individuata. Se non è stata individuata nessuna riga di
   * scaglione viene caricato quanto indicato nell'anagrafica articolo.
   * Verificare l'unità di misura e la valuta.
  */
  protected boolean calcolaPrezzi(UnitaMisura unita, UnitaMisura unitaMag, boolean visualizzaDettagli) {
      return calcolaPrezzi(unita, unitaMag, visualizzaDettagli, null); //Fix 39206
  }
  
  //protected boolean calcolaPrezzi(UnitaMisura unita, UnitaMisura unitaMag, boolean visualizzaDettagli) { //Fix 39206
  protected boolean calcolaPrezzi(UnitaMisura unita, UnitaMisura unitaMag, boolean visualizzaDettagli, String rifDataPrezzoSconti) { //Fix 39206
    ListinoVenditaScaglione lvs = this.getCondizioniDiVendita().getListinoVenditaScaglione();
    if (lvs!=null)
    {
      this.getCondizioniDiVendita().setPrezzo(lvs.getPrezzo());
      //Fix 35308 -- Inizio
      if(lvs.getListinoVenditaRiga() != null) {
        this.getCondizioniDiVendita().setDataValiditaLstVenRig(lvs.getListinoVenditaRiga().getDataInizioValidita());
      }
      //Fix 35308 -- Fine
//MG FIX 2888
//            if (lvs.getPrezzoExtra() != null)
      if (PersDatiVen.getCurrentPersDatiVen().isGestionePrezzoExtra() && lvs.getPrezzoExtra() != null)
//MG FIX 2888
        this.getCondizioniDiVendita().setPrezzoExtra(lvs.getPrezzoExtra());

      calcolaPrezziSecondari(visualizzaDettagli);  //...FIX04103 - DZ
      return true;
    }
    else
    {
      this.getCondizioniDiVendita().setUMPrezzo(CondizioniDiVendita.VENDITA);  //...FIX03085 - DZ
      Articolo art = this.getCondizioniDiVendita().getArticolo();
       // Fix 2341 PM Inizio
//       UnitaMisura umPrimaria = art.getUMDefaultVendita();//Fix 1126
//       BigDecimal prezzoStdVen = art.getArticoloDatiVendita().getPrezzoStdVen();
//       if (art!=null  && prezzoStdVen != null )
       if (art!=null) //...FIX05135 - DZ
       // Fix 2341 PM Fine
       {
         if (art.getArticoloDatiVendita().getPrezzoStdVen() != null){  //...FIX05135 - DZ
          // Fix 2341 PM Inizio
           UnitaMisura umPrimaria = art.getUMDefaultVendita();//Fix 1126
           BigDecimal prezzoStdVen = art.getArticoloDatiVendita().getPrezzoStdVen();
          // Fix 2341 PM Fine

           // Fix 3007
           PersDatiGen pdg = PersDatiGen.getCurrentPersDatiGen();
           String valPrm = pdg.getIdValutaPrimaria();
           String valSec = pdg.getIdValutaSecondaria();
           String valRif = null;
           if (pdg.getValutaRifCambi() == PersDatiGen.PRIMARIA){
             valRif = valPrm;
           }
           else if (pdg.getValutaRifCambi() == PersDatiGen.SECONDARIA){
             valRif = valSec;
           }
           // Fine fix 3007

           if( umPrimaria != null && umPrimaria.equals(unita))
           {
               // fix 3007
               if (this.getCondizioniDiVendita().getValuta()!=null && valRif!=null && this.getCondizioniDiVendita().getValuta().getIdValuta().equals(valRif)){
               // fine fix 3007
                 this.getCondizioniDiVendita().setPrezzo(prezzoStdVen);
                 this.getCondizioniDiVendita().setUMPrezzo(CondizioniDiVendita.VENDITA);
                 this.getCondizioniDiVendita().setTipoTestata(TipologieTestateRighe.PREZ_STD_ARTICOLO);//Fix 22073
               // Fix 3007
               }
               // fine fix 3007
           }
           else if (umPrimaria != null && umPrimaria.equals(unitaMag))
           {
               // Fix 3007
               if (this.getCondizioniDiVendita().getValuta()!=null && valRif!=null && this.getCondizioniDiVendita().getValuta().getIdValuta().equals(valRif)){
               // fine fix 3007
                 this.getCondizioniDiVendita().setPrezzo(prezzoStdVen);
                 this.getCondizioniDiVendita().setUMPrezzo(CondizioniDiVendita.MAGAZZINO);
                 this.getCondizioniDiVendita().setTipoTestata(TipologieTestateRighe.PREZ_STD_ARTICOLO);//Fix 22073
               // Fix 3007
               }
               // fine fix 3007
           }
           else{
             return false;
           }
        }

        //...FIX04103 - DZ
        Configurazione config = iCondizioniDiVendita.getConfigurazione();
        if (config != null && config.getSchemaCfg() != null){
          iIsDettaglioArtCfg = iCondizioniDiVendita.getConfigurazione().getSchemaCfg().getValorizzaConfig();
          if (iIsDettaglioArtCfg)
            //valorizzaDettagliArtCfg(lvs, visualizzaDettagli); //Fix 39206
        	valorizzaDettagliArtCfg(lvs, visualizzaDettagli, rifDataPrezzoSconti); //Fix 39206
        }
        calcolaPrezziSecondari(visualizzaDettagli);

        return true;
      }
      else
        return false;
    }
  }

  /**
   * FIX04103 + FIX04745 + FIX05141 - DZ.
   * Modifiche per gestire la "maggiorazione" sul lordo
   * necessaria per annullare gli sconti se checkato 'azzeraScontiCliente'
   * nel caso !visualizzaDettagli (ovvero quando l'utente preme il bottone "Ricalcola dati vendita").
   */
  protected void calcolaPrezziSecondari(boolean visualizzaDettagli){
    if (!iCondizioniDiVendita.getIsPrezzoEsattoArtCfg()){
      if (iIsDettaglioArtCfg && iCondizioniDiVendita.getPrezzo() != null){
        List dettagli = iCondizioniDiVendita.getCdvDettagli();
        for (int i = 0; i < dettagli.size(); i++){
          CondizioniDiVendita dettaglio = (CondizioniDiVendita)dettagli.get(i);
          if (dettaglio.getPrezzo() != null  && dettaglio.getPrezzoAlNettoSconti() != null) //...FIX06160 - DZ
            iCondizioniDiVendita.setPrezzo(iCondizioniDiVendita.getPrezzo().add(dettaglio.getPrezzoAlNettoSconti())); //...FIX04745 - DZ
          //Fix 12321 --inizio
          if (dettaglio.getPrezzoExtra() != null  && dettaglio.getPrezzoExtraAlNettoSconti() != null) {
            BigDecimal prezzoExtra = iCondizioniDiVendita.getPrezzoExtra();
            if (prezzoExtra == null) //Fix 12442
              prezzoExtra = new BigDecimal("0"); //Fix 12442
            iCondizioniDiVendita.setPrezzoExtra(prezzoExtra.add(dettaglio.getPrezzoExtraAlNettoSconti()));
          }
          //Fix 12321 --fine
        }
      }
      if (!visualizzaDettagli){
        if (iIsDettaglioArtCfg && iCondizioniDiVendita.getConfigurazione() != null){
          calcolaSconti();
          // 12635 - LC - inizio
          CfgArticoloPrezzoList cfgArticoloPrezzoList = (CfgArticoloPrezzoList)Factory.createObject(CfgArticoloPrezzoList.class);
          // 12635 - LC - fine
          cfgArticoloPrezzoList.inizializza(iCondizioniDiVendita);
          //...FIX05765 - DZ
          BigDecimal prezzoBase = iCondizioniDiVendita.getPrezzo() == null ? new BigDecimal("0") : iCondizioniDiVendita.getPrezzo();
          //Fix 15647 inizio
          BigDecimal prz = prezzoBase.add(cfgArticoloPrezzoList.getPrezzoLordo().subtract(prezzoBase));
          //iCondizioniDiVendita.setPrezzo(
          //    prezzoBase.add(//...FIX05765 - DZ
          //    cfgArticoloPrezzoList.getPrezzoLordo().subtract(prezzoBase)));
          iCondizioniDiVendita.setPrezzo(prz.compareTo(new BigDecimal("0")) == 0 ? null : prz);
          //Fix 15647 fine
          //Fix 12321 --inizio
          if(PersDatiVen.getCurrentPersDatiVen().isGestionePrezzoExtra() && iCondizioniDiVendita.getPrezzoExtra() != null){
            BigDecimal prezzoExtraBase = iCondizioniDiVendita.getPrezzoExtra() == null ? new BigDecimal("0") : iCondizioniDiVendita.getPrezzoExtra();
            iCondizioniDiVendita.setPrezzoExtra(
                prezzoExtraBase.add( //...FIX05765 - DZ
                cfgArticoloPrezzoList.getPrezzoExtraLordo().subtract(prezzoExtraBase)));
          }
          //Fix 12321 --fine
        }
        ricalcolaTipoTestata(); //Fix 21767
        iCondizioniDiVendita.getCdvDettagli().clear();
      }
    }
  }

  /*
   * Revisions:
   * Date          Owner
   * 18/06/2002    Daniela Battistoni

   * 17/09/2002    Daniela Battistoni
   * (la modifica ha introdotto un controllo sulla determinazione delle provvigioni
   * dall'anagrafico articolo. Questa ricerca deve essere effettuata solo se
   * il prezzo è stato individuato ed è >0)

   * il metodo calcola le provvigioni2 indicate nella riga del documento.
   * viene lanciata la ricerca in anagraficoprovvigioni. Se l'esito della ricerca
   * è positivo allora vengono caricate le provvigioni dell'anagrafica in caso contrario
   * è come avviene per gli sconti.
  */
  protected boolean calcolaProvvigioni() throws SQLException  {
    CondizioniDiVendita cdv = this.getCondizioniDiVendita();
    if (cdv==null) return false;

    Agente age = cdv.getAgente();
    Agente subage = cdv.getSubAgente();
    Articolo art = cdv.getArticolo();
    ClienteVendita cli = cdv.getCliente();
    ModalitaPagamento moda = cdv.getModalitaPagamento();
    UnitaMisura unita = cdv.getUnitaMisura();
    BigDecimal qua = cdv.getQuantita();
    java.sql.Date d = cdv.getDataValidita();
    BigDecimal importo = cdv.getImporto();

    int intero;
    BigDecimal imp = importo;

    if (imp == null)
      intero=8;
    else{
      int posPunto = imp.toString().indexOf(".");
      if (posPunto>0)
        intero = imp.toString().substring(posPunto).length() + 4;
      else
        intero = imp.toString().length() + 4;
    }

    // Fix 2607
    BigDecimal a = null;
    BigDecimal a1 = null;

    // Fix 05767 ini
    boolean ricScontiProvvioni = isRicScontiProvv(cdv.getIdAzienda());
    /**
    boolean ricScontiProvvioni = false;
    PersDatiVen pdv = PersDatiVen.getPersDatiVen(cdv.getIdAzienda());
    if (pdv != null)
      ricScontiProvvioni = pdv.getGestionePvgSuScalaSconti();
    **/
    // Fix 05767 fin

    if (age!=null){
    // fine fix 2607

    CondizioneProvvigioni cp = this.ricercaAnagraficoProvvigioni(age,art,cli,moda,unita,d,qua, importo);

    // Fix 1761
    //BigDecimal a =  new BigDecimal("0");
    // fix 2607
    //BigDecimal a = null;
    // fine fix 2607
    // Fine fix 1761

    if (cp!=null && cp.getProvvigioneAgente()!=null)
      a = cp.getProvvigioneAgente();
    // Fix 1504
    else if (cdv.getListinoVenditaScaglione() != null)
    {
      // Fix 1761
      //BigDecimal b = new BigDecimal("0");
      BigDecimal b = null;
      // Fine fix 1761
      if (cdv.getListinoVenditaScaglione().getProvvigioneAgente() != null)
        b = cdv.getListinoVenditaScaglione().getProvvigioneAgente();
      BigDecimal c;
      char ch;
      if (cdv.getListinoVenditaScontiScaglione()!=null && cdv.getListinoVenditaScontiScaglione().getProvvigioneAgente()!=null){
        c = cdv.getListinoVenditaScontiScaglione().getProvvigioneAgente();
        ch = cdv.getListinoVenditaScontiScaglione().getProvvigioneAggiuntivaSost();
        if (ch == ListinoVenditaScontiScaglione.AGGIUNTIVO)
          if (c!=null)
              // Fix 1761
              //a = b.add(c);
              if (b!=null){
               a = b.add(c);
              }
              else {
                a = c;
              }
              // Fine fix 1761
          else
            a = b;
        else if (ch == ListinoVenditaScontiScaglione.SOSTITUTIVO)
          a = c;
      }
      else
        a = b;
      // Fix 1760
      if (cp!=null){
        cp.setProvvigioneAgente(a);
      }
      // fine fix 1760
    }
    // Fine fix 1504
    else
   {
      if (this.getCondizioniDiVendita().getPrezzo()!=null && this.getCondizioniDiVendita().getPrezzo().compareTo(new BigDecimal("0"))>0){
         a = art.getProvvigione();
        // Fix 1760
        if (cp!=null){
          cp.setProvvigioneAgente(a);
        }
        // fine fix 1760
      }
//MG FIX 4348
/*
      else
        return false;
*/
//MG FIX 4348
    }

    // fix 2607
    /*
    boolean ricScontiProvvioni = false;
        PersDatiVen pdv = PersDatiVen.getPersDatiVen(cdv.getIdAzienda());
        if (pdv != null)
      ricScontiProvvioni = pdv.getGestionePvgSuScalaSconti();
    */
    // fine fix 2607

    if (cp!=null && ricScontiProvvioni) {
      AgentiScontiProvv asp = caricaScontoProvvigioni(cp);
      a = calcolaScontoProvvigioni(asp, a, intero);
    }

    // Fix 2607
    }
    if (subage!=null){
    // Fine fix 2607

    CondizioneProvvigioni cpsa = this.ricercaAnagraficoProvvigioni(subage,art,cli,moda,unita,d,qua, importo);
    // Fix 1761
    //BigDecimal a1 =  new BigDecimal("0");
    // fix 2607
    //BigDecimal a1 = null;
    // fin fix 2607
    // Fine Fix 1761

    if (cpsa!=null && cpsa.getProvvigioneSubagente()!=null)
      a1 = cpsa.getProvvigioneSubagente();
    // fix 1504
    else if (cdv.getListinoVenditaScaglione()!=null)
    {
      // Fix 1761
      //BigDecimal b1 = new BigDecimal("0");
      BigDecimal b1 = null;
      // fine fix 1761
      if (cdv.getListinoVenditaScaglione().getProvvigioneSubagente()!=null)
        b1 = cdv.getListinoVenditaScaglione().getProvvigioneSubagente();
      // Fine Fix 1504
      BigDecimal c1;
      char ch1;
      if (cdv.getListinoVenditaScontiScaglione()!=null && cdv.getListinoVenditaScontiScaglione().getProvvigioneSubagente()!=null){
        c1 = cdv.getListinoVenditaScontiScaglione().getProvvigioneSubagente();
        ch1 = cdv.getListinoVenditaScontiScaglione().getProvvigioneAggiuntivaSost();
        if (ch1 == ListinoVenditaScontiScaglione.AGGIUNTIVO)
        // fix 1761
           if (b1!=null){
             a1 = b1.add(c1);
           }
           else {
             a1 = c1;
           }
        // fine fix 1761
        else if (ch1 == ListinoVenditaScontiScaglione.SOSTITUTIVO)
          a1= c1;
      }
      else
        a1 = b1;
      // Fix 1760
      if (cpsa!=null){
        cpsa.setProvvigioneAgente(a1);
      }
      // Fine fix 1760
    }
    else {
      a1 = art.getProvvigione();
      // Fix 1760
      if (cpsa!=null){
        cpsa.setProvvigioneAgente(a1);
      }
      // fine fix 1760
    }

    if (cpsa!=null && ricScontiProvvioni){
      AgentiScontiProvv aspsa = caricaScontoProvvigioni(cpsa);
      a1 = calcolaScontoProvvigioni(aspsa, a1, intero);
    }

    // fix 2607
    }
    if (age!=null){
    // fine fix 2607
      cdv.setProvvigioneAgente2(a);
    // fix 2607
    }
    if (subage!=null){
    // fine fix 2607
      cdv.setProvvigioneSubagente2(a1);
    // fix 2607
    }
    // fine fix 2607
    return true;
  }



  /*
   * Revisions:
   * Date          Owner
   * 18/06/2002    Daniela Battistoni

   * 17/09/2002    Daniela Battistoni
   * (la modifica ha introdotto un controllo sulla determinazione degli sconti
   * dall'anagrafico articolo. Questa ricerca deve essere effettuata solo se
   * il prezzo è stato individuato ed è >0)

   * il metodo calcola gli sconti in base a quanto indicato nella riga
   * di scaglione individuata. Viene verificato se ci si trova in presenza
   * di una riga di scaglione di sconto.
  */
   protected boolean calcolaSconti(){
    ListinoVenditaScaglione lvs = this.getCondizioniDiVendita().getListinoVenditaScaglione();
    BigDecimal sconto1 = new BigDecimal("0");
    BigDecimal sconto2 = new BigDecimal("0");
    BigDecimal maggiorazione = new BigDecimal("0");
    //Sconto sconto = new Sconti();
    Sconto sconto = null;
    // 12635 - LC - inizio
    ListinoVenditaScontiScaglione lvss = (ListinoVenditaScontiScaglione)Factory.createObject(ListinoVenditaScontiScaglione.class);
    // 12635 - LC - fine
    if (lvs!=null){
      //...FIX02911 - DZ
      if (lvs.getScontoArticolo1() != null)
        sconto1 =  lvs.getScontoArticolo1();
      if (lvs.getScontoArticolo2() != null)
        sconto2 = lvs.getScontoArticolo2();
      if (lvs.getMaggiorazione() != null)
        maggiorazione = lvs.getMaggiorazione();
      if (lvs.getSconto() != null)
        sconto = lvs.getSconto();
      //...fine FIX02911 - DZ
      lvss = this.getCondizioniDiVendita().getListinoVenditaScontiScaglione();
      if (lvss!=null){
        switch (lvss.getScontoAggiuntivoSost()){
          case ListinoVenditaScontiScaglione.AGGIUNTIVO:
            if (lvss.getScontoArticolo1()!=null)
              sconto1 = sconto1.add(lvss.getScontoArticolo1());
            if (lvss.getScontoArticolo2()!=null)
              sconto2 = sconto2.add(lvss.getScontoArticolo2());
            if (lvss.getMaggiorazione()!=null)
              maggiorazione = maggiorazione.add(lvss.getMaggiorazione());
            break;
          case ListinoVenditaScontiScaglione.SOSTITUTIVO:
              sconto1 = lvss.getScontoArticolo1();
              sconto2 = lvss.getScontoArticolo2();
              maggiorazione = lvss.getMaggiorazione();
            break;
        }
        switch (lvss.getSostituzioneScnInLstVen()){
          case ListinoVenditaScontiScaglione.SEMPRE:
            sconto = lvss.getSconto();
            break;
          case ListinoVenditaScontiScaglione.SE_NON_DEFINITO:
            if (sconto==null) sconto=lvss.getSconto();
            break;
        }
      }
    }
    else{
      Articolo art = this.getCondizioniDiVendita().getArticolo();
      if (art!=null && this.getCondizioniDiVendita().getPrezzo()!=null &&  this.getCondizioniDiVendita().getPrezzo().compareTo(new BigDecimal("0"))> 0){
        sconto1 = art.getArticoloDatiVendita().getSconto();
      }
      else
        return false;
    }
    aggiornaCondizioniDiVenditaPers();//24273
    this.getCondizioniDiVendita().setScontoArticolo1(sconto1);
    this.getCondizioniDiVendita().setScontoArticolo2(sconto2);
    this.getCondizioniDiVendita().setSconto(sconto);
    this.getCondizioniDiVendita().setMaggiorazione(maggiorazione);
    return true;
   }

   public void aggiornaCondizioniDiVenditaPers() {  //24273	 
   }
   
   
  /*
   * Revisions:
   * Date          Owner
   * 1/07/2002    Daniela Battistoni
   * il metodo calcola lo sconto totale frutto del calcolo in cascata dei vari sconti  .
  */

  protected BigDecimal scontoDegliSconti(int dec){
    BigDecimal sconto1 = this.getCondizioniDiVendita().getScontoArticolo1();
	//Fix 13515 Inizio
    sconto1 = getValue(sconto1,calcoloProvvigioniSuPrezzoExtra());
    //Fix 13515 Fine
    BigDecimal sconto2 = this.getCondizioniDiVendita().getScontoArticolo2();
    BigDecimal magg = this.getCondizioniDiVendita().getMaggiorazione();
    Sconto scontoTab = this.getCondizioniDiVendita().getSconto();
//MG FIX 4348
    BigDecimal scontoIntestatario = this.getCondizioniDiVendita().getPrcScontoIntestatario();
    BigDecimal scontoModalita = this.getCondizioniDiVendita().getPrcScontoModalita();
    Sconto scontoModalitaTab = this.getCondizioniDiVendita().getScontoModalita();
    // Fix 30785 PM >
    if (this.getCondizioniDiVendita().getAzzeraScontiCliFor())
    {
    	scontoIntestatario = new BigDecimal(0);
        scontoModalita = new BigDecimal(0);
        scontoModalitaTab = null;
    }
    // Fix 30785 PM <
//MG FIX 4348

    //Fix 3197 - inizio
//MG FIX 4348

 // fix 29197 Inizio
    BigDecimal prcSconto = new BigDecimal(0.0);
    
    if (PersDatiVen.getCurrentPersDatiVen().getScontiEsaminati() == PersDatiVen.SCONTI_TESTATA_RIGA) {// fix 29197 
      //return calcoloScontoDaScontiRiga(scontoIntestatario, scontoModalita, scontoModalitaTab, sconto1, sconto2, magg, scontoTab, dec);// fix 29197 Inizio
    	prcSconto = calcoloScontoDaScontiRiga(scontoIntestatario, scontoModalita, scontoModalitaTab, sconto1, sconto2, magg, scontoTab, dec);// fix 29197 Inizio
    }// fix 29197 
	else {// fix 29197 
//MG FIX 4348
    //return calcoloScontoDaScontiRiga(sconto1, sconto2, magg, scontoTab, dec);// fix 29197 
    //Fix 3197 - fine
		prcSconto = calcoloScontoDaScontiRiga(sconto1, sconto2, magg, scontoTab, dec);// fix 29197 
	}// fix 29197
    ArrayList arr = new ArrayList();
    boolean ricalcolaPrcSconto = this.getTipoScontoRiga().equals("A") || this.getTipoScontoRiga().equals("I") || this.getTipoScontoRiga().equals("E");
    if (ricalcolaPrcSconto) {
    	if (this.getTipoScontoRiga().equals("A")) {
    		prcSconto = new BigDecimal(0);
    		if (PersDatiVen.getCurrentPersDatiVen().getScontiEsaminati() == PersDatiVen.SCONTI_TESTATA_RIGA) {
      		arr.addAll(this.getScontiCliente());    			
    		}
    		arr.addAll(this.getScontiSoloRiga());
    		arr.addAll(this.getScontiInternal());
    	}
    	else if (this.getTipoScontoRiga().equals("I")) {
    		prcSconto = new BigDecimal(0);
    		arr.addAll(this.getScontiSoloRiga());
    		arr.addAll(this.getScontiInternal());
    	}
    	else if (this.getTipoScontoRiga().equals("E")) {
    		prcSconto = new BigDecimal(0);
    		arr.addAll(this.getScontiInternal());
    	}

    	BigDecimal cento = new BigDecimal(100);
    	BigDecimal b = cento;
    	Iterator it = arr.iterator();
    	while(it.hasNext()){
    		BigDecimal sc = (BigDecimal) it.next();
    		b = b.multiply((cento.multiply(cento.add(sc.negate()).divide(cento, dec, BigDecimal.ROUND_HALF_UP)))).divide(cento, dec, BigDecimal.ROUND_HALF_UP);
    	}
    	BigDecimal nprcSconto = cento.add(b.negate());
    	prcSconto = nprcSconto;
    }
    return prcSconto ;
    // Fix 29197 Fine
  }

    /**
     * Aggiunto con FIX02261 - DZ.
     * Restituisce true se il listino (o listinoSconti) risulta autorizzato per l'utente corrente.
     * @param obj Object
     * @return boolean
     */
    public static boolean isListinoAuthorized(Object obj){
      boolean ret = false;
      if (obj != null){
        try{
          ret = ObjectAuthorizationManager.isAuthorized(obj);
        }
        catch(NoSuchMethodException e){
          e.printStackTrace();
        }
      }
      return ret;
    }

    /**
     * FIX04103 - DZ.
     * @param lv ListinoVendita
     * @return boolean
     * @throws SQLException
     */
    protected boolean individuaListino(ListinoVendita lv) throws SQLException {
      return individuaListino(lv, false);
    }

  /*
   * Revisions:
   * Date          Owner
   * 18/06/2002    Daniela Battistoni
   * E' il metodo attivato dalla ricerca che lanciando individuaRiga() ricerca lo scaglion
   * attraverso individuaScaglione(). Verifica poi se è attivata la ricerca nel
   * listino sconti: EsisteLisitnoSconti(). In caso affermativo lancia la ricerca della
   * riga di sconto (indiduaRigaSconti())e quindi dello scalgione (individuaScaglioneSconti())
  */
  protected boolean individuaListino(ListinoVendita lv, boolean visualizzaDettagli) throws SQLException {
        return individuaListino(lv, visualizzaDettagli, null); //Fix 39206
  }
  
  //protected boolean individuaListino(ListinoVendita lv, boolean visualizzaDettagli) throws SQLException { //Fix 39206
  protected boolean individuaListino(ListinoVendita lv, boolean visualizzaDettagli, String rifDataPrezzoSconti) throws SQLException { //Fix 39206
    ListinoVenditaRiga lvr = individuaRiga(lv);
    if (lvr != null)
    {
      //Fix 3910 - inizio
      getCondizioniDiVendita().setQuantitaMin(lvr.getQuantitaMin());
      //Fix 3910 - fine
      ListinoVenditaScaglione lvs = individuaScaglione(lvr);

      //...FIX04103 - DZ
      SchemaCfg schemaCfg = null;  //...FIX04343 - DZ
      if (iCondizioniDiVendita.getConfigurazione() != null)
          schemaCfg = iCondizioniDiVendita.getConfigurazione().getSchemaCfg();
      iIsDettaglioArtCfg = schemaCfg != null ? schemaCfg.getValorizzaConfig() : false;
      if (iIsDettaglioArtCfg)
        //valorizzaDettagliArtCfg(lvs, visualizzaDettagli); //Fix 39206
        valorizzaDettagliArtCfg(lvs, visualizzaDettagli, rifDataPrezzoSconti); //Fix 39206

      // nel caso in cui si stia facendo la ricerca nel listino campagna si deve ignorare il riferimento
      // al listino sconto (BAU_StrutturaListiniVendita)
      //...FIX02419 - DZ
      //if (!(getCondizioniDiVendita().getAttivoListinoCampagna()/* && lv.equals(lvc)*/)) //...FIX01990 - DZ
      //{
      ListinoVenditaSconti lvss = esisteListinoSconti();
      if (lvss != null && RicercaCondizioniDiVendita.isListinoAuthorized(lvss)) //...FIX02261 - DZ
      {
        ListinoVenditaScontiRiga lvrs = individuaRigaSconti(lvss);
        if (lvrs != null)
        {
          ListinoVenditaScontiScaglione lvsss = individuaScaglioneSconti(lvrs);
        }
      }
      //}
    }
    else {
      return false;
    }
    return true;
  }

  /*
   * Revisions:
   * Date          Owner
   * 18/06/2002    Daniela Battistoni
   * verifica che sia attiva la ricerca nel listino sconti in base a quanto indicato
   * nella riga di listino. In caso affermativo va a chiedere alla testata il listino
   * sconti sul quale effettuare la ricerca.
  */
  protected ListinoVenditaSconti esisteListinoSconti() throws SQLException {
    ListinoVenditaScaglione lvs = this.getCondizioniDiVendita().getListinoVenditaScaglione();
    // 12635 - LC - inizio
    ListinoVenditaSconti lvss = (ListinoVenditaSconti)Factory.createObject(ListinoVenditaSconti.class);
    ListinoVenditaTestata lvt  = (ListinoVenditaTestata)Factory.createObject(ListinoVenditaTestata.class);
    // 12635 - LC - fine
    if(lvs!=null){
      if(lvs.getContinuaRcrSconto()){
        String key = this.getCondizioniDiVendita().getListinoVenditaScaglione().getKey();
        String idListino = KeyHelper.getTokenObjectKey(key,2);
        String idTestata = KeyHelper.getTokenObjectKey(key,3);
        String idAzienda = KeyHelper.getTokenObjectKey(key,1);

        key = KeyHelper.buildObjectKey(new String[]{idAzienda, idListino, idTestata});

        lvt = (ListinoVenditaTestata) ListinoVenditaTestata.elementWithKey(ListinoVenditaTestata.class, key, PersistentObject.NO_LOCK);
        if (lvt!= null) {
          lvss = lvt.getListinoSconti();
        }
        else
          return null;
      }
      return lvss;
    }
    else
      return null;
  }

   /*
   * Revisions:
   * Date          Owner
   * 26/06/2002    Daniela Battistoni
   * Dopo aver verificato se una delle ricerche indicate in personalizzazione dei dati di vendita è
   * attiva lancia la ricerca dell'anagrafico provvigioni.
  */

  public CondizioneProvvigioni ricercaAnagraficoProvvigioni(Agente a,
                                                            Articolo art,
                                                            ClienteVendita cli,
                                                            ModalitaPagamento modalita,
                                                            UnitaMisura unita,
                                                            java.sql.Date data,
                                                            BigDecimal quantita,
                                                            BigDecimal importo)  throws SQLException {
    //Fix 3223 - inizio
    CondizioneProvvigioni cp =
      (CondizioneProvvigioni)Factory.createObject(CondizioneProvvigioni.class);
    //Fix 3223 - fine
    cp.setAgente(a);
    cp.setCliente(cli);
    cp.setArticolo(art);
    cp.setModalitaPagamento(modalita);
    cp.setUnitaMisura(unita);
    cp.setDataValidita(data);
    cp.setQuantita(quantita);
    cp.setImporto(importo);

    if (art!=null)
      cp.setAzienda(art.getAzienda());
    else if(cli!=null)
      cp.setAzienda(cli.getAzienda());
     else if(modalita!=null)
      cp.setAzienda(modalita.getAzienda());
    else if(a !=null)
      cp.setAzienda(a.getAzienda());
    else
      return cp;

        PersDatiVen pdv = PersDatiVen.getPersDatiVen(cp.getIdAzienda());
        if (pdv == null)
      return cp;

    if (!pdv.getGestioneAnagraPvg()) return cp;

    if (pdv.getRicerca1().getAttiva()) {
      if (ricercaPerChiavi(cp,pdv.getRicerca1())){
        cp.setRicerca(pdv.getRicerca1());
        return cp;
      }
    }

    if (pdv.getRicerca2().getAttiva()) {
      if (ricercaPerChiavi(cp,pdv.getRicerca2())){
         cp.setRicerca(pdv.getRicerca2());
        return cp;
      }
    }

    if (pdv.getRicerca3().getAttiva()) {
      if(ricercaPerChiavi(cp,pdv.getRicerca3())){
        cp.setRicerca(pdv.getRicerca3());
        return cp;
      }
    }
    return cp;
  }

  /*
   * Revisions:
   * Date          Owner
   * 26/06/2002    Daniela Battistoni
   * costruisce il map per il caricamento degli altri valori.
   * il map ha una chiave che poi corrisponde al parametro chiave per i parametri
   * chiave, e come valore un array che ha al primo parametro un intero che mi dice, 0 o 1,
   * che mi dice se il gruppo di parametri per la chiave deve essere con Null oppure
   * passando il valore.
  */
  protected boolean ricercaPerChiavi(CondizioneProvvigioni cpp, Ricerca ric) throws SQLException{
    if (ric==null || cpp==null)
      return false;

    // 12635 - LC - inizio
    AnagraficoProvvigioni ap = (AnagraficoProvvigioni)Factory.createObject(AnagraficoProvvigioni.class);
    // 12635 - LC - fine
    PreparedStatement ps = STATEMENT_PROVV.getStatement();
    Database db = ConnectionManager.getCurrentDatabase();
    String az = cpp.getIdAzienda();


    java.sql.Date d = cpp.getDataValidita();



    String RLineaProdotto=new String();
    String RMacrofamiglia=new String();
    String RMicrofamiglia=new String();
    String RSubfamiglia=new String();
    String RCatPrezzo=new String();
    String IdScelta = new String(); // fix 12767

    if (cpp.getArticolo()!=null){
      RLineaProdotto = KeyHelper.getTokenObjectKey(cpp.getArticolo().getLineaProdottoKey(),2);
      RMacrofamiglia = KeyHelper.getTokenObjectKey(cpp.getArticolo().getMacroFamigliaKey(),2);
      RMicrofamiglia = KeyHelper.getTokenObjectKey(cpp.getArticolo().getMicroFamigliaKey(),2);
      RSubfamiglia = KeyHelper.getTokenObjectKey(cpp.getArticolo().getSubFamigliaKey(),2);
      RCatPrezzo = KeyHelper.getTokenObjectKey(cpp.getArticolo().getCategoriaPrezzoKey(),2);
      IdScelta = cpp.getArticolo().getIdScelta(); // fix 12767
    }

    Map m = new HashMap();
    m.put("1", new Oggettino(0,cpp.getRAgente()));
    m.put("2", new Oggettino(0,cpp.getRCliente()));
    //  m.put("3", new Oggettino(0,cpp.getCliente().getIdCategoriaVenditaCliente())); commented on fix 10533
    m.put("3", new Oggettino(0,cpp.getCliente()==null?null:cpp.getCliente().getIdCategoriaVenditaCliente()));//fix 10533
    m.put("4", new Oggettino(0,cpp.getRModalitaPagamento()));
    m.put("5", new Oggettino(0,cpp.getRArticolo()));
    m.put("6", new Oggettino(0,RLineaProdotto));
    m.put("7", new Oggettino(0,RCatPrezzo));

    m.put("8", new Oggettino(0,cpp.getRUnitaMisura()));

    m.put("9", new Oggettino(0, RMacrofamiglia));
    m.put("10", new Oggettino(0, RSubfamiglia));
    m.put("11", new Oggettino(0, RMicrofamiglia));

    // fix 12767 >
    m.put("12", new Oggettino(0, IdScelta));

    if (ric.getChiave1()!='0'){
      //Oggettino o = (Oggettino) m.get(new String(new char[]{ric.getChiave1()}));
      char cRic = ric.getChiave1();
      String key = new String(new char[]{cRic});
      if (cRic == Ricerca.SCELTA) {
         key = "12";
      }
      Oggettino o = (Oggettino) m.get(key);
      o.carico=1;
    }
    if (ric.getChiave2()!='0'){
      //Oggettino o = (Oggettino) m.get(new String(new char[]{ric.getChiave2()}));
      char cRic = ric.getChiave2();
      String key = new String(new char[]{cRic});
      if (cRic == Ricerca.SCELTA) {
         key = "12";
      }
      Oggettino o = (Oggettino) m.get(key);
      o.carico=1;
    }
    if (ric.getChiave3()!='0'){
      //Oggettino o = (Oggettino) m.get(new String(new char[]{ric.getChiave3()}));
      char cRic = ric.getChiave3();
      String key = new String(new char[]{cRic});
      if (cRic == Ricerca.SCELTA) {
         key = "12";
      }
      Oggettino o = (Oggettino) m.get(key);
    // fix 12767 <
      o.carico=1;
    }
    if (ric.getChiave3()=='5'||ric.getChiave2()=='5' ||ric.getChiave1()=='5'){
      Oggettino o = (Oggettino) m.get("8");
      o.carico=1;
    }
    if (ric.getChiave3()=='6'||ric.getChiave2()=='6' ||ric.getChiave1()=='6'){
      Oggettino o = (Oggettino) m.get("9");
      o.carico=1;
      Oggettino o1 = (Oggettino) m.get("10");
      o1.carico=1;
      Oggettino o2 = (Oggettino) m.get("11");
      o2.carico=1;
      ap = individuaAnagraProvv(d, az, m);
      if (ap==null){
        Oggettino o3 = (Oggettino) m.get("11");
        o3.carico=0;
        ap = individuaAnagraProvv(d, az, m);
      }
      if (ap==null){
        Oggettino o4 = (Oggettino) m.get("10");
        o4.carico=0;
        ap = individuaAnagraProvv(d, az, m);
      }
      // Fix 1340
      if (ap==null)
      {
          Oggettino o4 = (Oggettino) m.get("9");
          o4.carico=0;
          ap = individuaAnagraProvv(d, az, m);
      }
      // Fine fix 1340
    }
    else
   {
      ap = individuaAnagraProvv(d, az, m);
    }

    if(ap!=null){
      AnagraProvvScaglione aps = ap.caricaScaglione(cpp.getQuantita());
      cpp.setProvvigioneAgente(aps.getProvvigioneAgente());
      cpp.setProvvigioneSubagente(aps.getProvvigioneSubagente());
      return true;
    }
    return false;
  }

  /*
   * Revisions:
   * Date          Owner
   * 26/06/2002    Daniela Battistoni
   * carica i parametri il map nei parametri dello STATEMENT_PROVV e poi esegue la query.
  */
  private AnagraficoProvvigioni individuaAnagraProvv(java.sql.Date d, String az, Map m)throws SQLException {
    PreparedStatement ps = STATEMENT_PROVV.getStatement();
    Database db = ConnectionManager.getCurrentDatabase();


    if (m!=null && d!=null){
      // fix 12767 >
      this.iParamsTest = new TreeMap();
      this.iSqlStatementParamsTest = STATEMENT_PROVV.getStmtString();
      // fix 12767 <
      db.setString(ps,1,az);
      this.appendParams(this.iParamsTest, new Integer(1), az); // fix 12767
      ps.setDate(2,d);
      this.appendParams(this.iParamsTest, new Integer(2), d); // fix 12767
      ps.setDate(3,d);
      this.appendParams(this.iParamsTest, new Integer(3), d); // fix 12767

      boolean iscaricato = caricaMap(3, m, ps);

      this.iParamsTest = null; // fix 12767

      if(iscaricato){
         try { // fix 12767
            ResultSet r = ps.executeQuery();
            int k;
            String tes = "";
            String key = "";
            while(r.next()){
              k = r.getInt("ID_ANA_PVG");
              tes = String.valueOf(k);
              key = KeyHelper.buildObjectKey(new String[]{az, tes});
              return AnagraficoProvvigioni.elementWithKey(key, PersistentObject.NO_LOCK);
            }
          }
         // fix 12767 >
          catch (Throwable t) {
             t.printStackTrace(Trace.excStream);
             return null;
          }
         // fix 12767 <
       }
      else
        return null;
    }
    else
      return null;
     return null;
  }

  /*
   * Revisions:
   * Date          Owner
   * 1/07/2002    Daniela Battistoni
   * Individua la riga AggentiScontiProvvigioni
  */

 // private AgentiScontiProvv individuaAgentiScontiProvv(String az, String age, Map m)throws SQLException { // Fix 4781
  protected AgentiScontiProvv individuaAgentiScontiProvv(String az, String age, Map m)throws SQLException { // Fix 4781
    PreparedStatement ps = STATEMENT_SCN_PROVV.getStatement();
    Database db = ConnectionManager.getCurrentDatabase();

    if (m!=null){

      db.setString(ps,1,az);

      db.setString(ps,2,age);

      boolean iscaricato = caricaMap(2, m, ps );

      if(iscaricato){
        ResultSet r = ps.executeQuery();
        int k;
        String tes = "";
        String key = "";
        while(r.next()){
          k = r.getInt("ID_SCN_PVG");
          tes = String.valueOf(k);
          key = KeyHelper.buildObjectKey(new String[]{az, tes});
          return AgentiScontiProvv.elementWithKey(key, PersistentObject.NO_LOCK);
        }
      }
      else
        return null;
    }
    else
      return null;
    return null;
  }
   /*
   * Revisions:
   * Date          Owner
   * 27/06/2002    Daniela Battistoni
   * carica la riga di AgentiScontiProvvigioni associata.
   *
  */

 // Fix 10149 ini
 //private AgentiScontiProvv caricaScontoProvvigioni(CondizioneProvvigioni cpp) throws SQLException {
 protected AgentiScontiProvv caricaScontoProvvigioni(CondizioneProvvigioni cpp) throws SQLException {
 // Fix 10149 fin
    //AgentiScontiProvv asp;//Fix 22229
    AgentiScontiProvv asp = null;//Fix 22229

    PreparedStatement ps = STATEMENT_SCN_PROVV.getStatement();
    Database db = ConnectionManager.getCurrentDatabase();

    String RLineaProdotto=new String();
    String RMacrofamiglia=new String();
    String RMicrofamiglia=new String();
    String RSubfamiglia =new String();

    if (cpp.getArticolo()!=null){
      RLineaProdotto = KeyHelper.getTokenObjectKey(cpp.getArticolo().getLineaProdottoKey(),2);
      RMacrofamiglia = KeyHelper.getTokenObjectKey(cpp.getArticolo().getMacroFamigliaKey(),2);
      RMicrofamiglia = KeyHelper.getTokenObjectKey(cpp.getArticolo().getMicroFamigliaKey(),2);
      RSubfamiglia = KeyHelper.getTokenObjectKey(cpp.getArticolo().getSubFamigliaKey(),2);
    }

    String RAgente = cpp.getRAgente();
    String RAzienda = cpp.getIdAzienda();
    String RCliente = cpp.getRCliente();//Fix 22229
    String RArticolo = cpp.getRArticolo();//Fix 36857

    Map m = new HashMap();

    //Fix 36857 inizio
    /*
    m.put("1", new Oggettino(1,RLineaProdotto));
    m.put("2", new Oggettino(1, RMacrofamiglia));
    m.put("3", new Oggettino(1, RSubfamiglia));
    m.put("4", new Oggettino(1, RMicrofamiglia));
    m.put("5", new Oggettino(1, RCliente));//Fix 22229
    */
    //Primo step ricerca con Cliente e Articolo
    m.put("1", new Oggettino(0,RLineaProdotto));
    m.put("2", new Oggettino(0, RMacrofamiglia));
    m.put("3", new Oggettino(0, RSubfamiglia));
    m.put("4", new Oggettino(0, RMicrofamiglia));
    m.put("5", new Oggettino(1, RCliente));//Fix 22229
    m.put("6", new Oggettino(1, RArticolo ));//Fix 36857
    asp = caricaScontoProvvigioniInternal(asp, RAzienda, RAgente, m);
    if (asp != null) 
    	return asp;

    //Second step ricerca con Cliente e classificazione articolo
    ((Oggettino) m.get("6")).carico = 0;//Fix 36857
    ((Oggettino) m.get("4")).carico = 1;
    ((Oggettino) m.get("3")).carico = 1;
    ((Oggettino) m.get("2")).carico = 1;
    ((Oggettino) m.get("1")).carico = 1;
    asp = caricaScontoProvvigioniInternal(asp, RAzienda, RAgente, m);
    if (asp != null) 
    	return asp;
    //Ricerca con Cliete null e IdArticolo
    ((Oggettino) m.get("6")).carico = 1;//Fix 36857 id Articolo
    ((Oggettino) m.get("5")).carico = 0;
    ((Oggettino) m.get("4")).carico = 0;
    ((Oggettino) m.get("3")).carico = 0;
    ((Oggettino) m.get("2")).carico = 0;
    ((Oggettino) m.get("1")).carico = 0;
    
    //Fix 36857 Fine

    //Fix 22229 inizio
    /*
     asp = individuaAgentiScontiProvv(RAzienda,RAgente,m);
     if (asp==null){
      Oggettino o = (Oggettino) m.get("4");
      o.carico=0;
       asp= individuaAgentiScontiProvv(RAzienda,RAgente,m);

      if (asp==null){
        Oggettino o1 = (Oggettino) m.get("3");
        o1.carico=0;
        asp= individuaAgentiScontiProvv(RAzienda,RAgente,m);
        if (asp==null){
          Oggettino o2 = (Oggettino) m.get("2");
          o2.carico=0;
          asp= individuaAgentiScontiProvv(RAzienda,RAgente,m);
          if (asp==null){
            Oggettino o3 = (Oggettino) m.get("1");
            o3.carico=0;
            asp= individuaAgentiScontiProvv(RAzienda,RAgente,m);
          }
         }
      }
    }*/
    asp = caricaScontoProvvigioniInternal(asp, RAzienda, RAgente, m);
    if (asp == null) {
    	//Ricerca con idCliente Null e classificazione Articolo
      ((Oggettino) m.get("6")).carico = 0;//Fix 36857
      ((Oggettino) m.get("5")).carico = 0;
      ((Oggettino) m.get("4")).carico = 1;
      ((Oggettino) m.get("3")).carico = 1;
      ((Oggettino) m.get("2")).carico = 1;
      ((Oggettino) m.get("1")).carico = 1;
      asp = caricaScontoProvvigioniInternal(asp, RAzienda, RAgente, m);
    }
    //Fix 22229 fine
    return asp;
 }

 //Fix 22229 inizio
 public AgentiScontiProvv caricaScontoProvvigioniInternal(AgentiScontiProvv asp,String RAzienda,String RAgente,Map m)throws SQLException {
   asp = individuaAgentiScontiProvv(RAzienda,RAgente,m);
   if (asp==null){
      Oggettino o = (Oggettino) m.get("4");
      o.carico=0;
       asp= individuaAgentiScontiProvv(RAzienda,RAgente,m);
      if (asp==null){
        Oggettino o1 = (Oggettino) m.get("3");
        o1.carico=0;
        asp= individuaAgentiScontiProvv(RAzienda,RAgente,m);
        if (asp==null){
          Oggettino o2 = (Oggettino) m.get("2");
          o2.carico=0;
          asp= individuaAgentiScontiProvv(RAzienda,RAgente,m);
          if (asp==null){
            Oggettino o3 = (Oggettino) m.get("1");
            o3.carico=0;
            asp= individuaAgentiScontiProvv(RAzienda,RAgente,m);
          }
         }
      }
    }
   return asp;
 }
 //Fix 22229 fine

    /**
   * carica i parametri di un statement a partire da una matrice.
   * In particolare in base al valore assunto in una posizione verrà passato il valore
   * oppure viene effettuata la ricerca del NULL.
  */
    /*
    * Revisions:
    * Number  Date         Owner   Description
    *         01/07/2002   DB
    * 02182   01/07/2004   DZ      Aggiunto setInt per configurazione.
    */
//    private boolean caricaMap(int parto, Map m, PreparedStatement ps ) throws SQLException // Fix 4781
 protected boolean caricaMap(int parto, Map m, PreparedStatement ps ) throws SQLException // Fix 4781
 {
    if (m == null || ps == null)
       return false;

    Database db = ConnectionManager.getCurrentDatabase();

    Iterator it = m.entrySet().iterator();

    // fix 12767 >
    // se parto == 3 significa che è in corso il caticamento per le provvigioni
    //int max = parto == 3 ? 40 : 37; //24273
    int max = getIntegerIndex(parto);   //24273

    while (it.hasNext())
    {
       Map.Entry me = (Map.Entry) it.next();
       int j = ((Oggettino) me.getValue()).carico;
       int k = KeyHelper.stringToInt((String)me.getKey());
       int t = parto + k + (k - 1) * 2;
       String str = ((Oggettino) me.getValue()).valore;
       if (j == 1)
       {
          if (str == null || str.equals(""))
             return false;
          appendParams(this.iParamsTest, new Integer(t), "1");
          ps.setInt(t, 1);
          if (t + 1 != max)  {
             appendParams(this.iParamsTest, new Integer(t+1), str);
             // fix 25658 > 
             //db.setString(ps, t + 1, str);  
             ps.setString(t + 1, str);             
             // fix 25658 <
          }
          else {
             appendParams(this.iParamsTest, new Integer(t+1), str);
             ps.setInt(t + 1, new Integer(str).intValue());  //...FIX02182 - DZ
          }
          appendParams(this.iParamsTest, new Integer(t+2), "1");
          ps.setInt(t + 2, 1);
       }
       else
       {
          appendParams(this.iParamsTest, new Integer(t), "0");
          ps.setInt(t, 0);
          if (t + 1 != max) {
             appendParams(this.iParamsTest, new Integer(t+1), "X");
             db.setString(ps, t + 1, "X");
          }
          else {
             appendParams(this.iParamsTest, new Integer(t+1), "0");
             ps.setInt(t + 1, 0);  //...FIX02182 - DZ
          }
          appendParams(this.iParamsTest, new Integer(t+2), "0");
          ps.setInt(t + 2, 0);
       }
    }

    printParams(m, this.iParamsTest, this.iSqlStatementParamsTest);
    // fix 12767 <
    return true;

 }

  /*
   * Revisions:
   * Date          Owner
   * 27/06/2002    Daniela Battistoni
   * Verifica la conformità della provvigione calcolata allo sconto in base a quanto riportato
   * in AgentiScontiProvv.
  */
 protected BigDecimal calcolaScontoProvvigioni(AgentiScontiProvv asp, BigDecimal vecchiaProvvigione, int intero){

    BigDecimal scn = this.scontoDegliSconti(intero);

    if (asp == null)
        return vecchiaProvvigione;

    if (scn == null)
        return vecchiaProvvigione;

    // Federico Crosa fix 2806
    BigDecimal nuovaProvvigione = asp.getProvvigioneDaSconto(scn);

    /* Parte sottostante commentata non più usata!!!

    List ar = new ArrayList();
    if (asp.getLimiteSconto01()!=null)
      ar.add(new ScontoProvvigione(asp.getLimiteSconto01(),asp.getProvvigione01()));
    if (asp.getLimiteSconto02()!=null)
      ar.add(new ScontoProvvigione(asp.getLimiteSconto02(),asp.getProvvigione02()));
    if (asp.getLimiteSconto03()!=null)
      ar.add(new ScontoProvvigione(asp.getLimiteSconto03(),asp.getProvvigione03()));
    if (asp.getLimiteSconto04()!=null)
      ar.add(new ScontoProvvigione(asp.getLimiteSconto04(),asp.getProvvigione04()));
    if (asp.getLimiteSconto05()!=null)
      ar.add(new ScontoProvvigione(asp.getLimiteSconto05(),asp.getProvvigione05()));
    if (asp.getLimiteSconto06()!=null)
      ar.add(new ScontoProvvigione(asp.getLimiteSconto06(),asp.getProvvigione06()));
    if (asp.getLimiteSconto07()!=null)
      ar.add(new ScontoProvvigione(asp.getLimiteSconto07(),asp.getProvvigione07()));
    if (asp.getLimiteSconto08()!=null)
      ar.add(new ScontoProvvigione(asp.getLimiteSconto08(),asp.getProvvigione08()));
    if (asp.getLimiteSconto09()!=null)
      ar.add(new ScontoProvvigione(asp.getLimiteSconto09(),asp.getProvvigione09()));
    if (asp.getLimiteSconto10()!=null)
      ar.add(new ScontoProvvigione(asp.getLimiteSconto10(),asp.getProvvigione10()));

   Collections.sort(ar , new ScontoProvvComparator());


   ScontoProvvigione scnprovv = new ScontoProvvigione();
   Iterator i = ar.iterator();
   while (i.hasNext()){
    ScontoProvvigione sp = (ScontoProvvigione)i.next();
    BigDecimal bd = sp.sconto;
    if (scn.compareTo(bd)<=0){
       scnprovv=sp;
     }
   }

    //Fix 1296 Inizio
    if (scnprovv.provv != null && (provvigione == null || provvigione.compareTo(scnprovv.provv) > 0))
        provvigione = scnprovv.provv;
//        else if (scnprovv.provv == null)
//            provvigione = new BigDecimal("0");
    //Fix 1296 Fine
*/

  if (vecchiaProvvigione == null)
    return nuovaProvvigione;

//MG FIX 10750 inizio : gestione provv. scala sconti con doppia modalità (provv. minima, prioritaria)
  char condPvgScalaSconti = PersDatiVen.getCurrentPersDatiVen().getCondizPvgScalaSconti();
  if (condPvgScalaSconti == PersDatiVen.PVG_SCALA_SCONTI_MINIMA) {
    if ( (nuovaProvvigione == null) || (nuovaProvvigione.compareTo(vecchiaProvvigione) == 1) )
      return vecchiaProvvigione;
    return nuovaProvvigione;
  }
  else if (condPvgScalaSconti == PersDatiVen.PVG_SCALA_SCONTI_PRIOR) {
    if (nuovaProvvigione == null)
      return vecchiaProvvigione;
    return nuovaProvvigione;
  }
/*  sostituito da codice sopra FIX 10750
  //Fix 8858
  //if ( (nuovaProvvigione == null) || (nuovaProvvigione.compareTo(vecchiaProvvigione) == 1) )
  if(nuovaProvvigione == null)
  //End Fix 8858
    return vecchiaProvvigione;
*/
//MG FIX 10750 fine

  return nuovaProvvigione;

  // Fine fix 2806
 }

  /*
   * Questa metodo è utile per rivedere l'importo, utili per l'approssimazione degli sconti,
   * una volta che è stato calcolato il prezzo.
   */
 //private BigDecimal calcoloImporto(){ // 24273
 public BigDecimal calcoloImporto() {	 // 24273
	 
    BigDecimal prezzo =this.getCondizioniDiVendita().getPrezzo();
    BigDecimal newImporto = new BigDecimal("0");
    if (prezzo!=null)
      newImporto = prezzo.multiply(this.getCondizioniDiVendita().getQuantita());

    BigDecimal oldImporto =  this.getCondizioniDiVendita().getImporto();
    if (oldImporto!=null && oldImporto.compareTo(newImporto)>0)
      return oldImporto;
    else if (oldImporto!=null && oldImporto.compareTo(newImporto)<=0)
      return newImporto;
    else if(oldImporto==null && newImporto.compareTo(new BigDecimal("0"))>0)
      return newImporto;
    else if(oldImporto==null && newImporto.compareTo(new BigDecimal("0"))<=0)
      return oldImporto;
    return oldImporto;
 }

 // fix 25658 >
 protected void calcolaPrezzoAlNettoScontiTotali() {
	 Vector sconti = getSconti();
	 Vector scontiApplicati = new Vector(sconti);
	 if (this.getTipoScontoRiga().equals("I")) {
		 scontiApplicati.addAll(this.getScontiCliente());
	 }
	 calcolaPrezzoAlNettoSconti(scontiApplicati, true);	
 }

 protected void calcolaPrezzoAlNettoSconti() {
	 Vector sconti = getSconti();
	 Vector scontiApplicati = new Vector(sconti);
	 calcolaPrezzoAlNettoSconti(scontiApplicati, false);	 
	 this.getCondizioniDiVendita().setScontiApplicati(scontiApplicati);	 
 }
 // fix 25658 <
 
  /**
   * Calcola e imposta sull'oggetto CondizioniDiVendita l'attributo PrezzoAbbattutoSconti.
   */
  protected void calcolaPrezzoAlNettoSconti(Vector scontiAgg, boolean scontiTotali)  // fix 25658 
  {
    // fix 25658 >
    //Vector sconti    = getSconti();
	Vector sconti    = scontiAgg; 
    // fix 25658 <

    Valuta valuta    = getCondizioniDiVendita().getValuta();
    int numDecUnitari = valuta.getNumDecUnitari().intValue(); //...FIX02880 - DZ
    
    // fix 25658 >
    if (isApplicaArrotondamentoPrezzoNetto()) {
    	numDecUnitari = 10;
    }
    // fix 25658 <
    
    
//        int numDec       = valuta.getNumDec().intValue();
    BigDecimal prezzoScontato = null;
    if (getCondizioniDiVendita().getPrezzo() != null)
    {
        prezzoScontato = ImportiDocumentoOrdineUtil.calcolaValore(getCondizioniDiVendita().getPrezzo(), new BigDecimal("1"), sconti);
        // fix 25658 >
        if (!isApplicaArrotondamentoPrezzoNetto()) {
        	// no scale
        }
        else {
        	prezzoScontato = prezzoScontato.setScale(numDecUnitari, BigDecimal.ROUND_HALF_UP); //...FIX02880 - DZ
        }
        // fix 25658 <
    }
 
    // fix 25658 >
    if (scontiTotali) {
    	getCondizioniDiVendita().setPrezzoAlNettoScontiTotali(prezzoScontato);
    }
    else {
    	getCondizioniDiVendita().setPrezzoAlNettoSconti(prezzoScontato);
    }
    // fix 25658 <
    //Fix 12321 --inizio
    BigDecimal prezzoExtraScontato = null;
    if (getCondizioniDiVendita().getPrezzoExtra() != null)
    {
       prezzoExtraScontato = ImportiDocumentoOrdineUtil.calcolaValore(getCondizioniDiVendita().getPrezzoExtra(), new BigDecimal("1"), sconti);
       // fix 25658 >
       if (!isApplicaArrotondamentoPrezzoNetto()) {
       	// no scale
       }
       else {
      	 prezzoExtraScontato = prezzoExtraScontato.setScale(numDecUnitari, BigDecimal.ROUND_HALF_UP); //...FIX02880 - DZ
       }
       // fix 25658 <
    }
    // fix 25658 >
    if (!isApplicaArrotondamentoPrezzoNetto()) {
    	getCondizioniDiVendita().setPrezzoExtraAlNettoScontiTotali(prezzoExtraScontato);    	
    }
    else {
    	getCondizioniDiVendita().setPrezzoExtraAlNettoSconti(prezzoExtraScontato);
    }
    // fix 25658 <

    //Fix 12321 --fine

  }

  // fix 25658 >
	private String iTipoScontoRiga = "";
	
  public String getTipoScontoRiga() {
		return iTipoScontoRiga;
	}

	public void setTipoScontoRiga(String iTipoScontoRiga) {
		this.iTipoScontoRiga = iTipoScontoRiga;
	}

  private Vector iScontiInternal = new Vector();

  
  protected Vector getScontiInternal() {
		return iScontiInternal;
	}

	protected void setScontiInternal(Vector iScontiInternal) {
		this.iScontiInternal = iScontiInternal;
	}

	public void setSconti(Vector sconti) {
  	this.setScontiInternal(sconti);
  }
  // fix 25658 <
  
  
  
  // fix 25658 >
  /**
   * Restituisce il vettore degli sconti da applicare ad un dato prezzo.
   * @return Vector
   */
	public Vector getSconti() {
		Vector sconti = new Vector();

		if (this.getTipoScontoRiga().equals("A") || this.getTipoScontoRiga().equals("I") || this.getTipoScontoRiga().equals("")) {

			sconti = this.getScontiRiga();
			/*
			// STD
			BigDecimal sconto = getCondizioniDiVendita().getScontoArticolo1();
			if (sconto != null && sconto.compareTo(new BigDecimal(0)) != 0)
				sconti.add(sconto);

			sconto = getCondizioniDiVendita().getScontoArticolo2();
			if (sconto != null && sconto.compareTo(new BigDecimal(0)) != 0)
				sconti.add(sconto);

			sconto = getCondizioniDiVendita().getMaggiorazione();
			if (sconto != null && sconto.compareTo(new BigDecimal(0)) != 0){
				// Fix 2752
				//sconto.negate();
				sconto = sconto.negate();
				// fine fix 2752
				sconti.add(sconto);
			}

			Sconto scontoR = getCondizioniDiVendita().getSconto();
			if (scontoR != null){
				BigDecimal[] percSconto = ImportiDocumentoOrdineUtil.getArrayPrcSconto(scontoR);
				for (int i = 0; i < percSconto.length; i++){
					sconto = percSconto[i];
					if (sconto != null && sconto.compareTo(new BigDecimal(0)) != 0)
						sconti.add(sconto);
				}
			}
			*/

			if (this.getTipoScontoRiga().equals("I")) {    		
				sconti.addAll(this.getScontiInternal());
			}
			else if (this.getTipoScontoRiga().equals("A")) {
				//Fix 29197
				if (getCondizioniDiVendita() != null && getCondizioniDiVendita().getListinoVenditaScaglione() != null 
						&& !getCondizioniDiVendita().getListinoVenditaScaglione().getAzzeraScontiCliFor()) {
					sconti.addAll(this.getScontiCliente());					
				}
				//Fix 29197
				sconti.addAll(this.getScontiInternal());

			}
		}
		else if (this.getTipoScontoRiga().equals("E")) {
			sconti = this.getScontiInternal();
		}

		return sconti;
	}

	protected Vector getScontiRiga() {
		//Fix 29197 Inizio
		/*
		Vector sconti = new Vector();
		BigDecimal sconto = getCondizioniDiVendita().getScontoArticolo1();
		if (sconto != null && sconto.compareTo(new BigDecimal(0)) != 0)
			sconti.add(sconto);

		sconto = getCondizioniDiVendita().getScontoArticolo2();
		if (sconto != null && sconto.compareTo(new BigDecimal(0)) != 0)
			sconti.add(sconto);

		sconto = getCondizioniDiVendita().getMaggiorazione();
		if (sconto != null && sconto.compareTo(new BigDecimal(0)) != 0){
			// Fix 2752
			//sconto.negate();
			sconto = sconto.negate();
			// fine fix 2752
			sconti.add(sconto);
		}*/
		Vector sconti = this.getScontiSoloRiga();
		BigDecimal sconto = null;
		//Fix 29197 Fine
		Sconto scontoR = getCondizioniDiVendita().getSconto();
		if (scontoR != null){
			BigDecimal[] percSconto = ImportiDocumentoOrdineUtil.getArrayPrcSconto(scontoR);
			for (int i = 0; i < percSconto.length; i++){
				sconto = percSconto[i];
				if (sconto != null && sconto.compareTo(new BigDecimal(0)) != 0)
					sconti.add(sconto);
			}
		}
		return sconti;		
	}
	
    // fix 29197 Inizio
	protected Vector getScontiSoloRiga() {
		Vector sconti = new Vector();
		BigDecimal sconto = getCondizioniDiVendita().getScontoArticolo1();
		if (sconto != null && sconto.compareTo(new BigDecimal(0)) != 0)
			sconti.add(sconto);

		sconto = getCondizioniDiVendita().getScontoArticolo2();
		if (sconto != null && sconto.compareTo(new BigDecimal(0)) != 0)
			sconti.add(sconto);

		sconto = getCondizioniDiVendita().getMaggiorazione();
		if (sconto != null && sconto.compareTo(new BigDecimal(0)) != 0){
			sconto = sconto.negate();
			sconti.add(sconto);
		}
		return sconti;		
	}
    // fix 29197 Fine
	
	protected Vector getScontiCliente() {
		Vector sconti = new Vector();
		BigDecimal scontoT = getCondizioniDiVendita().getPrcScontoIntestatario();
		if (scontoT != null && scontoT.compareTo(new BigDecimal(0)) != 0) {
			sconti.add(scontoT);
		}	
		scontoT = getCondizioniDiVendita().getPrcScontoModalita();
		if (scontoT != null && scontoT.compareTo(new BigDecimal(0)) != 0) {
			sconti.add(scontoT);
		}	

		ClienteVendita cv = this.getCondizioniDiVendita().getCliente();
		String  idSconto = cv == null ? null : cv.getIdSconto();
		List  scontiCli = new Vector();
		if (idSconto != null) {
			scontiCli = cv == null ? null : cv.getSconto().getPrcSconti();
			sconti.addAll(scontiCli);
		}
		// fix 27561 >
		if (this.getCondizioniDiVendita().getAzzeraScontiCliFor()) {
			sconti.clear();
		}
		// fix 27561 <
		return sconti;
	}
	
	// fix 25658 <

  /*
   * Questa classe è utile per la definizione del Map.
   */
  public class Oggettino {
    public String valore;
    public int carico;
    public Oggettino(int i, String v){
      this.carico=i;
      this.valore=v;
    }
  }


  /*
   * Questa classe è utile per la definizione del Map.
   */
  class ScontoProvvigione {
    public BigDecimal sconto;
    public BigDecimal provv;

    public ScontoProvvigione(){
    }

    public ScontoProvvigione(BigDecimal i, BigDecimal v){
      this.sconto=i;
      this.provv=v;
    }
  }


  /*
   * Questa classe è utile per la definizione dell'ordinamento degli scaglioni
   * nella OneToMany.
   */
  class ScontoProvvComparator implements Comparator {
    public int compare(Object o1, Object o2){
      return -((ScontoProvvigione)o1).sconto.compareTo(((ScontoProvvigione)o2).sconto)  ;
    }
  }


  public static void main (String [] args) throws Exception
  {
    //ConnectionManager.openMainConnection("THIPDAN", "server", "visual", new DB2Database());
    //com.thera.thermfw.security.Security.setCurrentDatabase("THIPDAN",  new DB2Database());
    //com.thera.thermfw.security.Security.openSession("ADMIN", "ADMIN");

    //Fix 3223 - inizio
    RicercaCondizioniDiVendita rcdv =
      (RicercaCondizioniDiVendita)Factory.createObject(RicercaCondizioniDiVendita.class);
    CondizioniDiVendita cdv =
      (CondizioniDiVendita)Factory.createObject(CondizioniDiVendita.class);
    //Fix 3223 - fine


    cdv.setScontoArticolo1(new BigDecimal("5"));
    cdv.setScontoArticolo2(new BigDecimal("3.5"));
    cdv.setMaggiorazione(new BigDecimal("2.3"));
    // 12635 - LC - inizio
    Sconto sconto = (Sconto)Factory.createObject(Sconto.class);
    // 12635 - LC - fine
    sconto.setSconto01(new BigDecimal("2"));
    sconto.setSconto02(new BigDecimal("3.5"));
    sconto.setSconto03(new BigDecimal("4.23"));
    sconto.setSconto04(new BigDecimal("5"));
    cdv.setSconto(sconto);
    rcdv.setCondizioniDiVendita(cdv);

    BigDecimal bb = rcdv.scontoDegliSconti(10);
    BigDecimal vedo = (new BigDecimal("990000")).multiply(bb).divide(new BigDecimal("100"),2,BigDecimal.ROUND_HALF_UP);
    //System.out.println("Sconto da mio calcolo: " + vedo);
    //System.out.println("Sconto da mio calcolo intero: " + (new BigDecimal("990000")).multiply(bb).divide(new BigDecimal("100"),10,BigDecimal.ROUND_HALF_UP));

    BigDecimal rivedo = (new BigDecimal("990000")).multiply(new BigDecimal("5")).divide(new BigDecimal("100"),6,BigDecimal.ROUND_HALF_UP);
    BigDecimal rivedo1 = (new BigDecimal("990000")).subtract(rivedo).multiply(new BigDecimal("3.5")).divide(new BigDecimal("100"),6,BigDecimal.ROUND_HALF_UP);
    BigDecimal rivedo2 = (new BigDecimal("990000")).subtract(rivedo.add(rivedo1)).multiply(new BigDecimal("2.3")).divide(new BigDecimal("100"),6,BigDecimal.ROUND_HALF_UP);
    BigDecimal rivedo21 = (new BigDecimal("990000")).subtract(rivedo.add(rivedo1).subtract(rivedo2)).multiply(new BigDecimal("2")).divide(new BigDecimal("100"),6,BigDecimal.ROUND_HALF_UP);
    BigDecimal rivedo3 = (new BigDecimal("990000")).subtract(rivedo.add(rivedo1).add(rivedo21).subtract(rivedo2)).multiply(new BigDecimal("3.5")).divide(new BigDecimal("100"),6,BigDecimal.ROUND_HALF_UP);
    BigDecimal rivedo4 = (new BigDecimal("990000")).subtract(rivedo.add(rivedo1).add(rivedo3).add(rivedo21).subtract(rivedo2)).multiply(new BigDecimal("4.23")).divide(new BigDecimal("100"),6,BigDecimal.ROUND_HALF_UP);
    BigDecimal rivedo5 = (new BigDecimal("990000")).subtract(rivedo.add(rivedo1).add(rivedo3).add(rivedo4).add(rivedo21).subtract(rivedo2)).multiply(new BigDecimal("5")).divide(new BigDecimal("100"),6,BigDecimal.ROUND_HALF_UP);
    BigDecimal totsconti = (rivedo.add(rivedo1).add(rivedo3).add(rivedo4).add(rivedo5).add(rivedo21).subtract(rivedo2)).setScale(2,BigDecimal.ROUND_HALF_UP);
    //System.out.println("Sconto calcolato: " + totsconti);



  }

  // Fix 2343
  public void aggiornaProvvigioni()throws SQLException{
    if (this.getCondizioniDiVendita()!=null){
      this.calcolaProvvigioni();
    }
  }
  // Fine fix 2343


//MG FIX 4348
  public static BigDecimal calcoloScontoDaScontiRiga(
                             BigDecimal sconto1,
                             BigDecimal sconto2,
                             BigDecimal magg,
                             Sconto scontoTab,
                             int dec) {
    return calcoloScontoDaScontiRiga(null,null,null,
                                 sconto1,sconto2,magg,scontoTab,dec);
  }
//MG FIX 4348


  //Fix 3197 - inizio
  public static BigDecimal calcoloScontoDaScontiRiga(
      BigDecimal scontoIntestatario,
      BigDecimal scontoPrcModalita,
      Sconto scontoModalita,
                             BigDecimal sconto1,
                             BigDecimal sconto2,
                             BigDecimal magg,
                             Sconto scontoTab,
                             int dec) {
    BigDecimal b = new BigDecimal("0");

    if (dec==0) dec = 8;

//MG FIX 4348
    // sconti di testata
    if (scontoIntestatario!=null && !(scontoIntestatario.compareTo(new BigDecimal("0"))==0))
      b = scontoIntestatario;
    if (scontoPrcModalita!=null && !(scontoPrcModalita.compareTo(new BigDecimal("0"))==0)){
      b = ((new BigDecimal("100").multiply(b)).add((new BigDecimal("100").multiply(scontoPrcModalita)))).add((b.multiply(scontoPrcModalita)).negate()).divide(new BigDecimal("100"),dec,BigDecimal.ROUND_HALF_UP);
    }
    if (scontoModalita!=null){
      List arr = new ArrayList();
      if (scontoModalita.getSconto01()!=null && !(scontoModalita.getSconto01().compareTo(new BigDecimal("0"))==0)){
        if (scontoModalita.getMagiorazzione01())
          arr.add(scontoModalita.getSconto01().negate());
        else
          arr.add(scontoModalita.getSconto01());
      }
      if (scontoModalita.getSconto02()!=null && !(scontoModalita.getSconto02().compareTo(new BigDecimal("0"))==0)){
        if (scontoModalita.getMagiorazzione02())
          arr.add(scontoModalita.getSconto02().negate());
        else
          arr.add(scontoModalita.getSconto02());
      }
      if (scontoModalita.getSconto03()!=null && !(scontoModalita.getSconto03().compareTo(new BigDecimal("0"))==0)){
        if (scontoModalita.getMagiorazzione03())
          arr.add(scontoModalita.getSconto03().negate());
        else
          arr.add(scontoModalita.getSconto03());
      }
      if (scontoModalita.getSconto04()!=null && !(scontoModalita.getSconto04().compareTo(new BigDecimal("0"))==0)){
        if (scontoModalita.getMagiorazzione04())
          arr.add(scontoModalita.getSconto04().negate());
        else
          arr.add(scontoModalita.getSconto04());
      }
       if (scontoModalita.getSconto05()!=null && !(scontoModalita.getSconto05().compareTo(new BigDecimal("0"))==0)){
        if (scontoModalita.getMagiorazzione05())
          arr.add(scontoModalita.getSconto05().negate());
        else
          arr.add(scontoModalita.getSconto05());
      }
      if (scontoModalita.getSconto06()!=null && !(scontoModalita.getSconto06().compareTo(new BigDecimal("0"))==0)){
        if (scontoModalita.getMagiorazzione06())
          arr.add(scontoModalita.getSconto06().negate());
        else
          arr.add(scontoModalita.getSconto06());
      }
       if (scontoModalita.getSconto07()!=null && !(scontoModalita.getSconto07().compareTo(new BigDecimal("0"))==0)){
        if (scontoModalita.getMagiorazzione07())
          arr.add(scontoModalita.getSconto07().negate());
        else
          arr.add(scontoModalita.getSconto07());
      }
       if (scontoModalita.getSconto08()!=null && !(scontoModalita.getSconto08().compareTo(new BigDecimal("0"))==0)){
        if (scontoModalita.getMagiorazzione08())
          arr.add(scontoModalita.getSconto08().negate());
        else
          arr.add(scontoModalita.getSconto08());
      }
       if (scontoModalita.getSconto09()!=null && !(scontoModalita.getSconto09().compareTo(new BigDecimal("0"))==0)){
        if (scontoModalita.getMagiorazzione09())
          arr.add(scontoModalita.getSconto09().negate());
        else
          arr.add(scontoModalita.getSconto09());
      }
      if (scontoModalita.getSconto10()!=null && !(scontoModalita.getSconto10().compareTo(new BigDecimal("0"))==0)){
        if (scontoModalita.getMagiorazzione10())
          arr.add(scontoModalita.getSconto10().negate());
        else
          arr.add(scontoModalita.getSconto10());
      }

      Iterator it = arr.iterator();
      while(it.hasNext()){
        BigDecimal sc = (BigDecimal) it.next();
        b = (((new BigDecimal("100").multiply(b)).add((new BigDecimal("100").multiply(sc)))).add((b.multiply(sc)).negate()).divide(new BigDecimal("100"),dec,BigDecimal.ROUND_HALF_UP));
      }
    }

/*
    if (sconto1!=null && !(sconto1.compareTo(new BigDecimal("0"))==0))
      b = sconto1;
*/
    // sconti di riga
    if (b == null || b.compareTo(new BigDecimal(0))==0) {
      if (sconto1!=null && !(sconto1.compareTo(new BigDecimal("0"))==0))
        b = sconto1;
    }
    else {
      if (sconto1!=null && !(sconto1.compareTo(new BigDecimal("0"))==0)){
        b = ((new BigDecimal("100").multiply(b)).add((new BigDecimal("100").multiply(sconto1)))).add((b.multiply(sconto1)).negate()).divide(new BigDecimal("100"),dec,BigDecimal.ROUND_HALF_UP);
      }
    }
//MG FIX 4348

    if (sconto2!=null && !(sconto2.compareTo(new BigDecimal("0"))==0)){
      //(100*b - 100*sconto2-b*sconto2)/100
      b = ((new BigDecimal("100").multiply(b)).add((new BigDecimal("100").multiply(sconto2)))).add((b.multiply(sconto2)).negate()).divide(new BigDecimal("100"),dec,BigDecimal.ROUND_HALF_UP);
    }
    if (magg!=null && !(magg.compareTo(new BigDecimal("0"))==0)){
      b = ((new BigDecimal("100").multiply(b)).add((new BigDecimal("100").multiply(magg)).negate())).add((b.multiply(magg))).divide(new BigDecimal("100"),dec,BigDecimal.ROUND_HALF_UP);
    }
    if (scontoTab!=null){
      List arr = new ArrayList();
      if (scontoTab.getSconto01()!=null && !(scontoTab.getSconto01().compareTo(new BigDecimal("0"))==0)){
        if (scontoTab.getMagiorazzione01())
          arr.add(scontoTab.getSconto01().negate());
        else
          arr.add(scontoTab.getSconto01());
      }
      if (scontoTab.getSconto02()!=null && !(scontoTab.getSconto02().compareTo(new BigDecimal("0"))==0)){
        if (scontoTab.getMagiorazzione02())
          arr.add(scontoTab.getSconto02().negate());
        else
          arr.add(scontoTab.getSconto02());
      }
      if (scontoTab.getSconto03()!=null && !(scontoTab.getSconto03().compareTo(new BigDecimal("0"))==0)){
        if (scontoTab.getMagiorazzione03())
          arr.add(scontoTab.getSconto03().negate());
        else
          arr.add(scontoTab.getSconto03());
      }
      if (scontoTab.getSconto04()!=null && !(scontoTab.getSconto04().compareTo(new BigDecimal("0"))==0)){
        if (scontoTab.getMagiorazzione04())
          arr.add(scontoTab.getSconto04().negate());
        else
          arr.add(scontoTab.getSconto04());
      }
       if (scontoTab.getSconto05()!=null && !(scontoTab.getSconto05().compareTo(new BigDecimal("0"))==0)){
        if (scontoTab.getMagiorazzione05())
          arr.add(scontoTab.getSconto05().negate());
        else
          arr.add(scontoTab.getSconto05());
      }
      if (scontoTab.getSconto06()!=null && !(scontoTab.getSconto06().compareTo(new BigDecimal("0"))==0)){
        if (scontoTab.getMagiorazzione06())
          arr.add(scontoTab.getSconto06().negate());
        else
          arr.add(scontoTab.getSconto06());
      }
       if (scontoTab.getSconto07()!=null && !(scontoTab.getSconto07().compareTo(new BigDecimal("0"))==0)){
        if (scontoTab.getMagiorazzione07())
          arr.add(scontoTab.getSconto07().negate());
        else
          arr.add(scontoTab.getSconto07());
      }
       if (scontoTab.getSconto08()!=null && !(scontoTab.getSconto08().compareTo(new BigDecimal("0"))==0)){
        if (scontoTab.getMagiorazzione08())
          arr.add(scontoTab.getSconto08().negate());
        else
          arr.add(scontoTab.getSconto08());
      }
       if (scontoTab.getSconto09()!=null && !(scontoTab.getSconto09().compareTo(new BigDecimal("0"))==0)){
        if (scontoTab.getMagiorazzione09())
          arr.add(scontoTab.getSconto09().negate());
        else
          arr.add(scontoTab.getSconto09());
      }
      if (scontoTab.getSconto10()!=null && !(scontoTab.getSconto10().compareTo(new BigDecimal("0"))==0)){
        if (scontoTab.getMagiorazzione10())
          arr.add(scontoTab.getSconto10().negate());
        else
          arr.add(scontoTab.getSconto10());
      }

      Iterator it = arr.iterator();
      while(it.hasNext()){
        BigDecimal sc = (BigDecimal) it.next();
        b = (((new BigDecimal("100").multiply(b)).add((new BigDecimal("100").multiply(sc)))).add((b.multiply(sc)).negate()).divide(new BigDecimal("100"),dec,BigDecimal.ROUND_HALF_UP));
      }
    }

    return b;
  }
  //Fix 3197 - fine

  // Fix 05767 ini
  public boolean isRicScontiProvv(String idAzienda) {
    boolean ricScontiProvvioni = false;
    PersDatiVen pdv = PersDatiVen.getPersDatiVen(idAzienda);
    if (pdv != null)
      ricScontiProvvioni = pdv.getGestionePvgSuScalaSconti();
    return ricScontiProvvioni;
  }
  // Fix 05767 fin


  // fix 11156
  public static CondizioniDiVendita getCondizioniVendita(
                                      String idListino,
                                      String idCliente,
                                      String idArticolo,
                                      String idEsternoConfigurazione,  //...FIX02182 - DZ
                                      String idUMVendita,
                                      String qtaVendita,
                                      String qtaMagazzino,
                                      String idModPagamento,
                                      String dtOrdine,
                                      String dtConsegna,
                                      String idAgente,
                                      String idSubagente,
                                      String idUMMagazzino,
                                      String idValuta,
                                      String rifDataPrezzoSconti,
                                      boolean visualizzaDettagli,  //...FIX04103 - DZ
                                      //MG FIX 4348
                                      String prcScontoIntestatario,
                                      String prcScontoModalita,
                                      String idScontoModalita,
                                      //MG FIX 4348
                                      String idUMSecMag, // Fix 13211
                                      String qtaSecMag // Fix 13211
                                      ) {
    return getCondizioniVendita(
                                      idListino,
                                      idCliente,
                                      idArticolo,
                                      idEsternoConfigurazione,
                                      idUMVendita,
                                      qtaVendita,
                                      qtaMagazzino,
                                      idModPagamento,
                                      dtOrdine,
                                      dtConsegna,
                                      idAgente,
                                      idSubagente,
                                      idUMMagazzino,
                                      idValuta,
                                      rifDataPrezzoSconti,
                                      visualizzaDettagli,
                                      prcScontoIntestatario,
                                      prcScontoModalita,
                                      idScontoModalita,
                                      "1", null,
                                      idUMSecMag, qtaSecMag // Fix 13211
                                    );

  }




  public CondizioniDiVendita ricercaCondizioniDiVendita(String idAzienda, ListinoVendita listino,
                                                        ClienteVendita cliente,
                                                        Articolo articolo,
                                                        Configurazione configurazione,
                                                        UnitaMisura unita,
                                                        BigDecimal quantita,
                                                        BigDecimal importo,
                                                        ModalitaPagamento modalita,
                                                        java.sql.Date dataValidita,
                                                        Agente agente,
                                                        Agente subagente,
                                                        UnitaMisura unitaMag,
                                                        BigDecimal quantitaMag,
                                                        Valuta valuta,
                                                        //MG FIX 4348
                                                        BigDecimal scontoIntestatario,
                                                        BigDecimal scontoModalita,
                                                        String idScontoModalita,
                                                        //MG FIX 4348
                                                        UnitaMisura idUMSecMag, // Fix 13211
                                                        BigDecimal qtaSecMag // Fix 13211
                                                        ) throws SQLException
    {
      return ricercaCondizioniDiVendita(idAzienda, listino,
                                                        cliente,
                                                        articolo,
                                                        configurazione,
                                                        unita,
                                                        quantita,
                                                        importo,
                                                        modalita,
                                                        dataValidita,
                                                        agente,
                                                        subagente,
                                                        unitaMag,
                                                        quantitaMag,
                                                        valuta,
                                                        //MG FIX 4348
                                                        scontoIntestatario,
                                                        scontoModalita,
                                                        idScontoModalita,
                                                        //MG FIX 4348
                                                        null, null,
                                                        idUMSecMag, qtaSecMag // Fix 13211
                                                        );

    }



    public CondizioniDiVendita ricercaCondizioniDiVendita(String idAzienda, ListinoVendita listino,
                                                      ClienteVendita cliente,
                                                      Articolo articolo,
                                                      Configurazione configurazione, //...FIX02182 - DZ
                                                      UnitaMisura unita,
                                                      BigDecimal quantita,
                                                      BigDecimal importo,
                                                      ModalitaPagamento modalita,
                                                      java.sql.Date dataValidita,
                                                      Agente agente,
                                                      Agente subagente,
                                                      UnitaMisura unitaMag,
                                                      BigDecimal quantitaMag,
                                                      Valuta valuta,
                                                      boolean visualizzaDettagli,
                                                      // Fix 13211 inzio
                                                      UnitaMisura umSecMag,
                                                      BigDecimal qtaSecMag
                                                      // Fix 13211 fine
                                                      ) throws SQLException
{
  return ricercaCondizioniDiVendita(idAzienda,listino,cliente,articolo,configurazione,
                                    unita,quantita,importo,modalita,dataValidita,agente,subagente,
                                    unitaMag,quantitaMag,valuta,visualizzaDettagli,
                                    null,null,null,null, null,
                                    umSecMag, qtaSecMag // Fix 13211
    );
}


public static CondizioniDiVendita getCondizioniVendita(
                                    String idListino,
                                    String idCliente,
                                    String idArticolo,
                                    String idEsternoConfigurazione,  //...FIX02182 - DZ
                                    String idUMVendita,
                                    String qtaVendita,
                                    String qtaMagazzino,
                                    String idModPagamento,
                                    String dtOrdine,
                                    String dtConsegna,
                                    String idAgente,
                                    String idSubagente,
                                    String idUMMagazzino,
                                    String idValuta,
                                    String rifDataPrezzoSconti,
                                    //MG FIX 4348
                                    String prcScontoIntestatario,
                                    String prcScontoModalita,
                                    String idScontoModalita,
                                    //MG FIX 4348
                                    String idUMSecMag, // Fix 13211
                                    String qtaSecMag // Fix 13211
                                  ) {
  return getCondizioniVendita(
                                    idListino,
                                    idCliente,
                                    idArticolo,
                                    idEsternoConfigurazione,  //...FIX02182 - DZ
                                    idUMVendita,
                                    qtaVendita,
                                    qtaMagazzino,
                                    idModPagamento,
                                    dtOrdine,
                                    dtConsegna,
                                    idAgente,
                                    idSubagente,
                                    idUMMagazzino,
                                    idValuta,
                                    rifDataPrezzoSconti,
                                    //MG FIX 4348
                                    prcScontoIntestatario,
                                    prcScontoModalita,
                                    idScontoModalita,
                                    //MG FIX 4348
                                    // fix 11156
                                    "1", null,
                                    // fine fix 11156
                                    idUMSecMag, qtaSecMag // Fix 13211
                                  );


}

public static CondizioniDiVendita getCondizioniVendita(
                                    String idListino,
                                    String idCliente,
                                    String idArticolo,
                                    String idEsternoConfigurazione,  //...FIX02182 - DZ
                                    String idUMVendita,
                                    String qtaVendita,
                                    String qtaMagazzino,
                                    String idModPagamento,
                                    String dtOrdine,
                                    String dtConsegna,
                                    String idAgente,
                                    String idSubagente,
                                    String idUMMagazzino,
                                    String idValuta,
                                    String rifDataPrezzoSconti,
                                    String idVersione,
                                    String numeroImballo,
                                    String idUMSecMag, // Fix 13211
                                    String qtaSecMag // Fix 13211
                                  ) {
  return getCondizioniVendita(idListino,idCliente,
                              idArticolo,idEsternoConfigurazione,  //...FIX02182 - DZ
                              idUMVendita,qtaVendita,
                              qtaMagazzino,idModPagamento,
                              dtOrdine,dtConsegna,
                              idAgente,idSubagente,
                              idUMMagazzino,idValuta,
                              rifDataPrezzoSconti,null,null,null, idVersione, numeroImballo,
                              idUMSecMag, qtaSecMag // Fix 13211
    );
}
public static CondizioniDiVendita getCondizioniVendita(
                                    String idListino,
                                    String idCliente,
                                    String idArticolo,
                                    String idEsternoConfigurazione,  //...FIX02182 - DZ
                                    String idUMVendita,
                                    String qtaVendita,
                                    String qtaMagazzino,
                                    String idModPagamento,
                                    String dtOrdine,
                                    String dtConsegna,
                                    String idAgente,
                                    String idSubagente,
                                    String idUMMagazzino,
                                    String idValuta,
                                    String idVersione,
                                    String numeroImballo,
                                    String idUMSecMag, // Fix 13211
                                    String qtaSecMag // Fix 13211
                                  ) {
  return getCondizioniVendita(idListino, idCliente, idArticolo,
                              idEsternoConfigurazione,  //...FIX02182 - DZ
                              idUMVendita, qtaVendita, qtaMagazzino,
                              idModPagamento, dtOrdine, dtConsegna, idAgente,
                              idSubagente, idUMMagazzino, idValuta, false, idVersione,numeroImballo,
                              idUMSecMag, qtaSecMag // Fix 13211
    );
}
public static CondizioniDiVendita getCondizioniVendita(
                                    String idListino,
                                    String idCliente,
                                    String idArticolo,
                                    String idEsternoConfigurazione,  //...FIX02182 - DZ
                                    String idUMVendita,
                                    String qtaVendita,
                                    String qtaMagazzino,
                                    String idModPagamento,
                                    String dtOrdine,
                                    String dtConsegna,
                                    String idAgente,
                                    String idSubagente,
                                    String idUMMagazzino,
                                    String idValuta,
                                    boolean visualizzaDettagli, //...FIX04103 - DZ
                                    String idVersione,
                                    String numeroImballo,
                                    String idUMSecMag, // Fix 13211
                                    String qtaSecMag // Fix 13211
                                  ) {
   //Fix 4003 - inizio
  String key = KeyHelper.buildObjectKey(new String[] {Azienda.getAziendaCorrente(), idCliente});

  ClienteVendita cliente = null;
  try {
    cliente = (ClienteVendita)ClienteVendita.elementWithKey(ClienteVendita.class, key, PersistentObject.NO_LOCK);
  }
  catch (SQLException e) {
    Trace.printStackTrace();
  }

  char tipoDataPrezziSconti = '\0';
  if (cliente == null) {
    tipoDataPrezziSconti = PersDatiVen.getCurrentPersDatiVen().getTipoDataPrezziSconti();
  }
  else {
    tipoDataPrezziSconti = cliente.getRifDataPerPrezzoSconti();
  }

  return getCondizioniVendita(idListino, idCliente, idArticolo, idEsternoConfigurazione,
      idUMVendita, qtaVendita, qtaMagazzino, idModPagamento,
      dtOrdine, dtConsegna, idAgente, idSubagente, idUMMagazzino, idValuta,
      new Character(tipoDataPrezziSconti).toString(), visualizzaDettagli, null,null,null,idVersione,numeroImballo,
      idUMSecMag, qtaSecMag // Fix 13211
    );
   //Fix 4003 - fine
}


 public CondizioniDiVendita ricercaCondizioniDiVendita(String idAzienda, ListinoVendita listino,
                                                       ClienteVendita cliente,
                                                       Articolo articolo,
                                                       Configurazione configurazione, //...FIX02182 - DZ
                                                       UnitaMisura unita,
                                                       BigDecimal quantita,
                                                       BigDecimal importo,
                                                       ModalitaPagamento modalita,
                                                       java.sql.Date dataValidita,
                                                       Agente agente,
                                                       Agente subagente,
                                                       UnitaMisura unitaMag,
                                                       BigDecimal quantitaMag,
                                                       Valuta valuta,
                                                       boolean visualizzaDettagli,
                                                       //MG FIX 4348
                                                       BigDecimal prcScontoIntestatario,
                                                       BigDecimal prcScontoModalita,
                                                       String idScontoModalita,
                                                       // Fix 13211 inzio
                                                       UnitaMisura umSecMag,
                                                       BigDecimal qtaSecMag
                                                        // Fix 13211 fine
                                                       ) throws SQLException
 {
   ArticoloVersione versione = ArticoloVersione.elementWithKey(KeyHelper.
       buildObjectKey(new String[] {idAzienda, articolo.getIdArticolo(), "1"}),
       PersistentObject.NO_LOCK);

   return ricercaCondizioniDiVendita(idAzienda, listino,
                                     cliente,
                                     articolo,
                                     configurazione, //...FIX02182 - DZ
                                     unita,
                                     quantita,
                                     importo,
                                     modalita,
                                     dataValidita,
                                     agente,
                                     subagente,
                                     unitaMag,
                                     quantitaMag,
                                     valuta,
                                     visualizzaDettagli,
                                     //MG FIX 4348
                                     prcScontoIntestatario,
                                     prcScontoModalita,
                                     idScontoModalita,
                                     //MG FIX 4348
                                     // fix 11156
                                     versione,
                                     null,
                                     // fine fix 11156
                                     umSecMag, qtaSecMag // Fix 13211
                                     );

   // fine fix 11156
 }

 //Fix 12321 --inizio
 public BigDecimal calcolaQuantita(Configurazione cfg, VariabileSchemaCfg variabile, ValoreVariabileCfg valore){
   BigDecimal coefficiente = new BigDecimal(1); //Fix 17534
   if(valore.isUsaValoreInCalPrz()){
     char decSep = DecimalType.getDecimalSeparator();
     Hashtable cfgVars = cfg.getVariables();
     BigDecimal val = null;
     String tmp = (String) cfgVars.get(variabile.getKey());
     if (tmp != null && variabile.getTipoVariabileCfg() == VariabileSchemaCfg.DECIMALE)
       val = new BigDecimal(tmp.replace(decSep, '.'));
     else if (tmp != null && variabile.getTipoVariabileCfg() == VariabileSchemaCfg.INTERO)
       val = new BigDecimal(tmp);
     if (valore.getCoeffCalPrz() != null) //Fix 12331
       val = val.multiply(valore.getCoeffCalPrz()); //Fix 12331
     //return val;//Fix 17534
     coefficiente = val; //Fix 17534
   }
   else if(valore.getCoeffCalPrz() != null && valore.getIdMacroCalPrz() == null){
     //return valore.getCoeffCalPrz();//Fix 17534
     coefficiente = valore.getCoeffCalPrz(); //Fix 17534

   }
   else if(valore.getCoeffCalPrz() != null && valore.getIdMacroCalPrz() != null){
     GestoreMacroConfigurazione gestore = (GestoreMacroConfigurazione) Factory.createObject(GestoreMacroConfigurazione.class);
     //return gestore.esegue(valore.getMacroCalPrz(), iCondizioniDiVendita.getConfigurazione(),
                          // valore.getArticolo(), valore.getCoeffCalPrz())//Fix 12331//Fix 17534
     coefficiente = gestore.esegue(valore.getMacroCalPrz(), iCondizioniDiVendita.getConfigurazione(),
                           valore.getArticolo(), valore.getConfigurazione(), valore.getCoeffCalPrz()); //Fix 17534 //Fix 30330
   }
   //Fix 17534
   /*else{
    return new BigDecimal(1);
   }*/
    Articolo articolo = iCondizioniDiVendita.getArticolo();
    //Fix 34667 inizio
    //BigDecimal qtaVen = articolo.convertiUM(iCondizioniDiVendita.iQuantitaPrmMag,iCondizioniDiVendita.getUMPrmMag(),iCondizioniDiVendita.getUMVen());
    BigDecimal qtaVen = null;
    if (iCondizioniDiVendita.getUMPrezzo() == iCondizioniDiVendita.VENDITA)
      qtaVen = articolo.convertiUM(iCondizioniDiVendita.iQuantitaPrmMag,iCondizioniDiVendita.getUMPrmMag(),iCondizioniDiVendita.getUMVen());
    else {
      qtaVen = iCondizioniDiVendita.iQuantitaPrmMag;
      iCondizioniDiVendita.setUMVen(iCondizioniDiVendita.getUMPrmMag());
    }
    //Fix 34667 fine
    //Fix 21767 Inizio
    if ((qtaVen.compareTo(new BigDecimal(0)) == 0)){
      return coefficiente;
    }
    //Fix 21767 Fine
    return coefficiente.multiply(qtaVen); //Fix 17534
 }
 //Fix 12321 --fine

 //Fix 13515 Inizio
  public BigDecimal calcoloProvvigioniSuPrezzoExtra()
  {
    return calcoloProvvigioniSuPrezzoExtra(getCondizioniDiVendita().getPrezzo(),getCondizioniDiVendita().getPrezzoExtra());
  }

  public static BigDecimal calcoloProvvigioniSuPrezzoExtra(BigDecimal prezzo,BigDecimal prezzoExtra)
  {
    if(PersDatiVen.getCurrentPersDatiVen().getAbilitaCalProvvPrzExtra() && prezzo != null && prezzoExtra != null
        && prezzoExtra.compareTo(new BigDecimal(0.0)) < 0
        && prezzo.compareTo(new BigDecimal(0.0)) > 0 )

      return prezzoExtra.multiply(new BigDecimal(100.0)).divide(prezzo, BigDecimal.ROUND_HALF_UP).negate();

    return new BigDecimal(0.00);
  }

  public BigDecimal getValue(BigDecimal value1, BigDecimal value2)
  {
    if (value1 != null && value2 != null)
      return value1.add(value2);
    else if (value1 == null && value2 != null)
      return value2;
    else if (value1 != null && value2 == null)
      return value1;
    return null;
  }
  //Fix 13515 Fine

  // fix 12767 >
  // x test reperimento anagrafica provvigioni
  private void printOggettini(Map m) {
     if (this.iOkTest && m != null) {
        System.out.println();
        TreeMap tm = new TreeMap();
        Iterator iter = m.keySet().iterator();
        while (iter.hasNext()) {
           String key = (String) iter.next();
           Oggettino v = (Oggettino) m.get(key);
           String sv = v.valore + " - " + v.carico;
           tm.put(new Integer(key), sv);
        }
        System.out.print(tm.toString());
        System.out.println();
     }
  }

  private void printParams(Map m, TreeMap params, String stmtString) {
     if (this.iOkTest && m != null && params != null && stmtString != null) {
        System.out.println("---------------------------------------------------");
        printOggettini(m);
        String stmParams = stmtString;
        StringBuffer out = new StringBuffer();
        String stm = stmtString;
        int iOld = 0;
        int p = 1;
        for (int i = 0; i < stm.length(); i++) {
           char c = stm.charAt(i);
           if (c == '?') {
              String v = params.get(new Integer(p)).toString();
              String ss = stmParams.substring(iOld, i);
              if (v.equals("0") || v.equals("1") && p != 38 ) {
                 out.append(ss).append(" ").append(v).append(" ");
              }
              else {
                 out.append(ss).append(" '").append(v).append("' ");
              }
              iOld = i + 1;
              p++;
           }
        }
        out.append(stmParams.substring(iOld));
        System.out.println(p + ") " + stmtString);
        System.out.println(params.toString());
        System.out.println(out);
        System.out.println("---------------------------------------------------");
     }
  }

  private void appendParams(TreeMap params, Integer pos, Object value) {
     if (this.iOkTest && params != null && pos != null) {
        params.put(pos, value);
     }
  }
  // fix 12767 <

  //Fix 21767 Inizio
  public void ricalcolaTipoTestata(){
    char tipoTestataDettagli = TipologieTestateRighe.NESSUNATESTATA;
    if(iCondizioniDiVendita == null)
      return;
    List dettagli = iCondizioniDiVendita.getCdvDettagli();
    if(dettagli == null || dettagli.isEmpty())
      return;

    for(int i=0; i<dettagli.size(); i++){
      CondizioniDiVendita dettaglio = (CondizioniDiVendita)dettagli.get(i);
      if(tipoTestataDettagli < getIndiceTipoTestata(dettaglio.getTipoTestata()))
        tipoTestataDettagli = dettaglio.getTipoTestata();
    }
    if(tipoTestataDettagli > getIndiceTipoTestata(iCondizioniDiVendita.getTipoTestata()))
      iCondizioniDiVendita.setTipoTestata(tipoTestataDettagli);
  }

  public char getIndiceTipoTestata(char tipoTestata){
    if(tipoTestata == TipologieTestateRighe.CLIENTE)
      return TipologieTestateRighe.CATEGORIAVENDITA;
    else if(tipoTestata == TipologieTestateRighe.CATEGORIAVENDITA)
      return TipologieTestateRighe.CLIENTE;
    else
      return tipoTestata;
  }
  //Fix 21767 Fine
  
  //Fix 24273 inizio
  public CachedStatement getStatementRicRigaListino() {
  	return STATEMENT;
  }

  public CachedStatement getStatementRicRigaScontiListino() {
  	return STATEMENT_SCONTI;
  }
  
  public int getPartoParametriRiga() {
  	return 14;
  }

  public int getIntegerIndex(int parto) {
  	return parto == 3 ? 40 : 37;
  }
  
  public static CondizioniDiVendita getCondizioniVendita(CondizioniDiVenditaParams condVenParams) {
  	String idAzienda = condVenParams.getIdAziendaParam();
  	String idListino = condVenParams.getIdListinoParam();
  	String idCliente = condVenParams.getIdClienteParam();
    String idArticolo = condVenParams.getIdArticoloParam();
    String idEsternoConfigurazione = condVenParams.getIdEsternoConfigParam();
    String idUMVendita = condVenParams.getIdUMVenditaParam();
    String qtaVendita  = condVenParams.getQtaVenditaParam();
    String qtaMagazzino  = condVenParams.getQtaMagazzinoParam();
    String idModPagamento  = condVenParams.getIdModPagamentoParam();
    String dtOrdine  = condVenParams.getDtOrdineParam();
    String dtConsegna = condVenParams.getDtConsegnaParam();
    String idAgente = condVenParams.getIdAgenteParam();
    String idSubagente = condVenParams.getIdSubagenteParam();
    String idUMMagazzino = condVenParams.getIdUMMagazzinoParam();
    String idValuta = condVenParams.getIdValutaParam();
    String rifDataPrezzoSconti = condVenParams.getRifDataPrezzoScontiParam();
    boolean visualizzaDettagli = condVenParams.getVisualizzaDettagliParam();
    String prcScontoIntestatario = condVenParams.getPrcScontoIntestatarioParam();
    String prcScontoModalita = condVenParams.getPrcScontoModalitaParam();
    String idScontoModalita = condVenParams.getIdScontoModalitaParam();
    String idVersione = condVenParams.getIdVersioneParam();
    String numeroImballo = condVenParams.getNumeroImballoParam();
    String idUMSecMag = condVenParams.getIdUMSecMagParam();
    String qtaSecMag = condVenParams.getQtaSecMagParam();
    String idDivisione = condVenParams.getIdDivisioneParam();//33484
    String idZona = condVenParams.getIdZona();//41247
    String idCategoriaVendita = condVenParams.getIdCategoriaVendita();//43127
  	CondizioniDiVendita condVen = null;

    try {
       DecimalType decType = new DecimalType();
       DateType dateType = new DateType();

       String azienda = Azienda.getAziendaCorrente();

       //Recupera l'oggetto ListinoVendita
       String key = KeyHelper.buildObjectKey(
                      new String[] {azienda, idListino}
                    );
       ListinoVendita listino =
         ListinoVendita.elementWithKey(key, PersistentObject.NO_LOCK);
       Trace.println(">>>>>>>>>>>>>>listino="+listino);

       //Recupera l'oggetto Articolo
       key = KeyHelper.buildObjectKey(
         new String[] {azienda, idArticolo}
       );
       Articolo articolo =
         Articolo.elementWithKey(key, PersistentObject.NO_LOCK);
       Trace.println(">>>>>>>>>>>>>>articolo="+articolo);

       if (articolo != null) {		//Fix 5706

         //...FIX02182 - DZ
         // Inizio 3834
         Configurazione configurazione = ConfigurazioneRicEnh.recuperaConfigurazione(azienda, articolo.getIdArticolo(), idEsternoConfigurazione);
         //Configurazione configurazione = ConfigurazioneRic.recuperaConfigurazione(azienda, idEsternoConfigurazione);
         // Fine 3834
         Trace.println(">>>>>>>>>>>>>>configurazione=" + configurazione);
         //...fine FIX02182 - DZ

         //Recupera l'oggetto ClienteVendita
         key = KeyHelper.buildObjectKey(
           new String[] {azienda, idCliente}
         );
         ClienteVendita cliente = (ClienteVendita)
           ClienteVendita.elementWithKey(
             ClienteVendita.class, key, PersistentObject.NO_LOCK
           );
         Trace.println(">>>>>>>>>>>>>>cliente="+cliente);

         //Recupera l'oggetto UnitaMisura (vendita)
         //fix 5330 inizio
         UnitaMisura umVendita = UnitaMisura.getUM(idUMVendita);
         /*
         key = KeyHelper.buildObjectKey(
           new String[] {azienda, idUMVendita}
         );
         UnitaMisura umVendita =
           UnitaMisura.elementWithKey(key, PersistentObject.NO_LOCK);
         */
         //fix 5330 fine
         Trace.println(">>>>>>>>>>>>>>um vendita="+umVendita);

         //Recupera la quantità di vendita
         qtaVendita = decType.unFormat(qtaVendita);  //Fix 1010
         // fix 12639 >
         BigDecimal  quantitaVendita  = null;
         try {
         quantitaVendita =
           new BigDecimal(
             ((Double)(decType.stringToObject(qtaVendita))).doubleValue()
           );
         }
         catch (Throwable t){/* nulla */}
         // fix 12639 <
         // Fix 1791
         //quantitaVendita = quantitaVendita.setScale(dammiLaScala(qtaVendita), BigDecimal.ROUND_HALF_UP);//Fix 30871
         quantitaVendita = Q6Calc.get().setScale(quantitaVendita,dammiLaScala(qtaVendita), BigDecimal.ROUND_HALF_UP);//Fix 30871
		 // Fine fix 1791
         Trace.println(">>>>>>>>>>>>>>quantVendita="+quantitaVendita);

         //Recupera la quantità di magazzino
         qtaMagazzino = decType.unFormat(qtaMagazzino);  //Fix 1010
         BigDecimal quantitaMagazzino =
           new BigDecimal(
             ((Double)(decType.stringToObject(qtaMagazzino))).doubleValue()
           );
         // Fix 1791
         //quantitaMagazzino = quantitaMagazzino.setScale(dammiLaScala(qtaMagazzino), BigDecimal.ROUND_HALF_UP);//Fix 30871
         quantitaMagazzino = Q6Calc.get().setScale(quantitaMagazzino,dammiLaScala(qtaMagazzino), BigDecimal.ROUND_HALF_UP);//Fix 30871
		 // Fine fix 1791
         Trace.println(">>>>>>>>>>>>>>quantMagazzino="+qtaMagazzino);

         //Recupera l'oggetto ModalitaPagamento
         key = KeyHelper.buildObjectKey(
           new String[] {azienda, idModPagamento}
         );
         //fix 5330 inizio
         ModalitaPagamento modPagamento = (ModalitaPagamento)PersistentObject.readOnlyElementWithKey(ModalitaPagamento.class, key);
         /*
         ModalitaPagamento modPagamento =
           ModalitaPagamento.elementWithKey(key, PersistentObject.NO_LOCK);
         */
         //fix 5330 fine
         Trace.println(">>>>>>>>>>>>>>modPagamento="+modPagamento);

         //Recupera la data di validità
         Date dataValid = null;
         //Fix 4003 - inizio
         //Fix 4140 - inizio
         //Fix 7024 - inizio: aggiunta seconda condizione
         if (rifDataPrezzoSconti == null || rifDataPrezzoSconti.length() == 0) {
         //Fix 7024 - fine
              if (cliente == null) {
                 rifDataPrezzoSconti = new Character(PersDatiVen.getCurrentPersDatiVen().getTipoDataPrezziSconti()).toString();
           }
           else {
              rifDataPrezzoSconti = new Character(cliente.getRifDataPerPrezzoSconti()).toString();
           }
         }
         //Fix 4140 - fine
         char cRif = rifDataPrezzoSconti.charAt(0);
         if (cRif == RifDataPrzScn.DA_CONDIZIONI_GENERALI) {
            cRif = PersDatiVen.getCurrentPersDatiVen().getTipoDataPrezziSconti();
         }
         //Fix 4003 - fine
         switch (cRif) {
           case RifDataPrzScn.DATA_ORDINE:
             dataValid = (Date)dateType.stringToObject(dtOrdine);
             break;
           case RifDataPrzScn.DATA_CONSEGNA:
             dataValid = (Date)dateType.stringToObject(dtConsegna);
             break;
         }
         Trace.println(">>>>>>>>>>>>>>dataValid="+dataValid);

 //MG FIX 4348
         //Recupera lo sconto intestatario
         BigDecimal prcScontoIntestatarioDec = new BigDecimal(0);
         if (prcScontoIntestatario != null && !prcScontoIntestatario.equals("")) {
           prcScontoIntestatario = decType.unFormat(prcScontoIntestatario);  //Fix 1010
           prcScontoIntestatarioDec =
               new BigDecimal(
               ((Double)(decType.stringToObject(prcScontoIntestatario))).doubleValue()
               );

           prcScontoIntestatarioDec = prcScontoIntestatarioDec.setScale(dammiLaScala(prcScontoIntestatario), BigDecimal.ROUND_HALF_UP);
         }
         //Recupera lo sconto modalita
         BigDecimal prcScontoModalitaDec = new BigDecimal(0);
         if (prcScontoModalita != null && !prcScontoModalita.equals("")) {
           prcScontoModalita = decType.unFormat(prcScontoModalita);  //Fix 1010
           prcScontoModalitaDec =
               new BigDecimal(
               ((Double)(decType.stringToObject(prcScontoModalita))).doubleValue()
               );

           prcScontoModalitaDec = prcScontoModalitaDec.setScale(dammiLaScala(prcScontoModalita), BigDecimal.ROUND_HALF_UP);
         }
 //MG FIX 4348

         //Fix 24273 inizio
         Sconto scontoModalita=null;
         if(idScontoModalita!=null)
         {
        	 key = KeyHelper.buildObjectKey(new String[] {azienda, idScontoModalita});
        	 scontoModalita = (Sconto)PersistentObject.elementWithKey(Sconto.class, key,PersistentObject.NO_LOCK);
        	 Trace.println(">>>>>>>>>>>>>>scontoModalita="+scontoModalita);
         }
         //Fix 24273 fine

         //Recupera il flag di attivazione ListinoCampagna
         boolean attivaListinoCampagna =
            PersDatiVen.getCurrentPersDatiVen().getListinoCampagna() != null;
         Trace.println(">>>>>>>>>>>>>>attivaListinoCampagna="+attivaListinoCampagna);

         //Recupera l'oggetto Agente
         key = KeyHelper.buildObjectKey(
           new String[] {azienda, idAgente}
         );
         //fix 5330 inizio
         Agente agente = (Agente)PersistentObject.readOnlyElementWithKey(Agente.class, key);
         //Agente agente = Agente.elementWithKey(key, PersistentObject.NO_LOCK);
         //fix 5330 fine
         Trace.println(">>>>>>>>>>>>>>agente="+agente);

         //Recupera l'oggetto Subagente
         key = KeyHelper.buildObjectKey(
           new String[] {azienda, idSubagente}
         );
         //fix 5330 inizio
         Agente subagente = (Agente)PersistentObject.readOnlyElementWithKey(Agente.class, key);
         //Agente subagente = Agente.elementWithKey(key, PersistentObject.NO_LOCK);
         //fix 5330 fine
         Trace.println(">>>>>>>>>>>>>>subagente="+subagente);

         //Recupera l'oggetto UnitaMisura (primaria magazzino)
         //fix 5330 inizio
         UnitaMisura umMagazzino = UnitaMisura.getUM(idUMMagazzino);
         /*
         key = KeyHelper.buildObjectKey(
           new String[] {azienda, idUMMagazzino}
         );
         UnitaMisura umMagazzino =
           UnitaMisura.elementWithKey(key, PersistentObject.NO_LOCK);
         */
         //fix 5330 fine
         Trace.println(">>>>>>>>>>>>>>um magazzino="+umMagazzino);

         //Recupera l'oggetto Valuta
         key = KeyHelper.buildObjectKey(
           new String[] {idValuta}
         );
         //fix 5330 inizio
         Valuta valuta = (Valuta)PersistentObject.readOnlyElementWithKey(Valuta.class, key);
         /*
         Valuta valuta =
           Valuta.elementWithKey(key, PersistentObject.NO_LOCK);
         */
         //fix 5330 fine
         Trace.println(">>>>>>>>>>>>>>valuta="+valuta);

         // fix 11156
         ArticoloVersione versione = ArticoloVersione.elementWithKey(KeyHelper.buildObjectKey(new String[]{azienda,idArticolo,idVersione}),PersistentObject.NO_LOCK);
         BigDecimal numImballo=null;
         // fix 11766 >
         if (numeroImballo != null && !numeroImballo.trim().equals("") && !(numeroImballo.trim().equals("undefined"))) { // fix 11951 // fix 12639
            try {
              numImballo = new BigDecimal(numeroImballo);
              if (numImballo == null) {
               if (numeroImballo!=null && !(numeroImballo.trim().equals("") && !(numeroImballo.trim().equals("undefined")))) { // fix 12639
                 numeroImballo = decType.unFormat(numeroImballo);
                 Object obj = decType.stringToObject(numeroImballo);
                 if (obj != null) {
                    numImballo = new BigDecimal(((Double)obj).doubleValue());
                    numImballo = numImballo.setScale(dammiLaScala(numeroImballo), BigDecimal.ROUND_HALF_UP);
                 }
               }
             }
            }
            catch (NumberFormatException ex) {
              /* nulla */
            }
         }
         /*
         if (numImballo == null) {
            if (numeroImballo!=null && !(numeroImballo.trim().equals(""))){
              numeroImballo = decType.unFormat(numeroImballo);
              numImballo =
                  new BigDecimal(
                      ( (Double) (decType.stringToObject(numeroImballo))).
                      doubleValue()
                  );
              numImballo = numImballo.setScale(dammiLaScala(numeroImballo),
                  BigDecimal.ROUND_HALF_UP);
            }
            // fine fix 11156
            }
          */
         // fix 11766 <
         // fix 12639 <

         //Fix 13211 inzio
         UnitaMisura umSecMag = null;
         if (idUMSecMag!=null)
           umSecMag = UnitaMisura.getUM(idUMSecMag);
         if (umSecMag != null)
           qtaSecMag = decType.unFormat(qtaSecMag);
         BigDecimal quantSecMagazzino = null;
         if (qtaSecMag != null && !qtaSecMag.equals("")) {
           quantSecMagazzino =
             new BigDecimal(
               ((Double) (decType.stringToObject(qtaSecMag))).doubleValue()
             );
         }
         //Fix 13211 fine      
         
         //Recupera l'oggetto Azienda
         key = KeyHelper.buildObjectKey(new String[] {azienda});
         Azienda aziendaObj = (Azienda)PersistentObject.elementWithKey(Azienda.class, key, PersistentObject.NO_LOCK);
         condVenParams.setAzienda(aziendaObj);
         condVenParams.setListinoVendita(listino);
         condVenParams.setArticolo(articolo);         
         condVenParams.setConfigurazione(configurazione);         
         condVenParams.setCliente(cliente);
         condVenParams.setUMVendita(umVendita);
         condVenParams.setQuantitaInUMRif(quantitaVendita);
         condVenParams.setQuantitaInUMPrm(quantitaMagazzino);         
         condVenParams.setModalitaPagamento(modPagamento);         
         condVenParams.setDataValidita(dataValid);
         condVenParams.setPrcScontoIntestatario(prcScontoIntestatarioDec);
         condVenParams.setPrcScontoModalita(prcScontoModalitaDec);
         condVenParams.setScontoModalita(scontoModalita);//Fix 24273
         condVenParams.setAgente(agente);
         condVenParams.setSubagente(subagente);
         condVenParams.setUMMagazzino(umMagazzino);
         condVenParams.setValuta(valuta);
         condVenParams.setArtVersione(versione);
         condVenParams.setNumeroImballo(numImballo);
         condVenParams.setUMSecMagazzino(umSecMag);
         condVenParams.setQuantitaInUMSec(quantSecMagazzino);        
         condVenParams.setImporto(new BigDecimal(0.0));
         //Istanzia l'oggetto RicercaCondizioniDiVendita e recupera l'oggetto
         //CondizioniDiVendita 
         RicercaCondizioniDiVendita ricerca = (RicercaCondizioniDiVendita)Factory.createObject(RicercaCondizioniDiVendita.class);

         /*
         ricerca.ricercaCondizioniDiVendita(azienda, 
         listino, cliente, articolo, configurazione, umVendita, quantitaVendita, new BigDecimal(0.0),
         modPagamento, dataValid, agente, subagente,
         umMagazzino, quantitaMagazzino, valuta, visualizzaDettagli,

         prcScontoIntestatarioDec, prcScontoModalitaDec, idScontoModalita,
         versione, numImballo,
         umSecMag, quantSecMagazzino
       );*/
         condVen = ricerca.getCondizioniVenditaInternal(condVenParams);

         
         if (condVen != null && condVenParams.getCliente() != null) {
        	 String divisioneKey = null;
        	 if(idDivisione!=null)
        		 divisioneKey = KeyHelper.buildObjectKey(new String[] {azienda, idCliente,idDivisione});
        	 
           BigDecimal prezzo = condVen.getPrezzo();
           //if (prezzo == null || prezzo.equals(new BigDecimal(0.0))) {//40311
           if (prezzo == null || Utils.areEqual(prezzo, new BigDecimal(0.0))) {//40311

        	   //Fix 33484 inizio
               //ListinoVendita listinoCliente = condVenParams.getCliente().getListino();
        	   ListinoVendita listinoCliente = null;
        	   if (divisioneKey==null)
        		   listinoCliente = condVenParams.getCliente().getListino();
        	   else
        		   listinoCliente = condVenParams.getCliente().getListino(divisioneKey);
        	   
        	   
             //ListinoVendita listinoAlternativo = condVenParams.getCliente().getListinoAlternativo();
        	   ListinoVendita listinoAlternativo = null;
        	   if (divisioneKey==null)
        		   listinoAlternativo = condVenParams.getCliente().getListinoAlternativo();
        	   else
        		   listinoAlternativo = condVenParams.getCliente().getListinoAlternativo(divisioneKey);
        	   //Fix 33484 Fine
        	   
             if (listinoCliente != null && listinoAlternativo != null && !listinoCliente.equals(listinoAlternativo)) {
             	condVenParams.setIdListinoParam(listinoAlternativo.getIdListino());
             	condVenParams.setListinoVendita(listinoAlternativo);
             	condVen = ricerca.getCondizioniVenditaInternal(condVenParams);

               /*condVen = ricerca.ricercaCondizioniDiVendita(azienda,
                 listinoAlternativo, cliente, articolo, configurazione, umVendita, quantitaVendita,
                 new BigDecimal(0.0), modPagamento, dataValid, agente, subagente,
                 umMagazzino, quantitaMagazzino, valuta,
               prcScontoIntestatarioDec, prcScontoModalitaDec, idScontoModalita,
               versione, numImballo,
              umSecMag, quantSecMagazzino
               );*/
             }
           }
         }
       }
     }
     catch (Exception ex) {
       ex.printStackTrace(Trace.excStream);
     }
    return condVen;
  }
  
  public CondizioniDiVendita getCondizioniVenditaInternal(CondizioniDiVenditaParams condVenParams)  throws SQLException {
    synchronized (cSyncRicercaCondizioniDiVendita) {

      // fix 25658 >
    	this.setSconti(condVenParams.getSconti());
    	this.setTipoScontoRiga(condVenParams.getTipoScontoRiga());
      // fix 25658 >


      boolean ricercaProvv = false;
      boolean ricercaListino = false;
      boolean listinoIndividuato = false;

      // attributo utile per verificare se è necessario effettuare
      // la ricerca anche per articolo e unita di misura di magazzino
      boolean doppiaRicerca = false;
      if (condVenParams.getIdAzienda() == null || condVenParams.getIdValuta() == null) {
          return null;
      }

      CondizioniDiVendita cdv = (CondizioniDiVendita) Factory.createObject(CondizioniDiVendita.class);
      PersDatiGen iPersDatiGen = PersDatiGen.getCurrentPersDatiGen();

      if (!RicercaCondizioniDiVendita.isListinoAuthorized(condVenParams.getListinoVendita())) {
      	condVenParams.setListinoVendita(null);
      }

      // inanzitutto si controlla la corrispondenza tra la valuta del listino
      // e quella passata al metodo che siano la stessa.
      if (condVenParams.getListinoVendita() != null) {
      	if (iPersDatiGen.getValutaPrimaria() != null) {
      		if (!condVenParams.getValuta().equals(condVenParams.getListinoVendita().getValuta()) &&   !condVenParams.getIdValuta().equals(iPersDatiGen.getIdValutaPrimaria()))
      			return null;
      	}
      	else {
      		if (!condVenParams.getValuta().equals(condVenParams.getListinoVendita().getValuta()))
      			return null;
      	}
      }
      else if ((iPersDatiGen.getValutaPrimaria() != null && 
      		!condVenParams.getIdValuta().equals(iPersDatiGen.getIdValutaPrimaria())) || iPersDatiGen.getValutaPrimaria() == null)
        ricercaProvv = true;

      cdv.setListinoVendita(condVenParams.getListinoVendita());
      cdv.setIdAzienda(condVenParams.getIdAzienda());//Fix 27616
      cdv.setCliente(condVenParams.getCliente());
      cdv.setIdZona(condVenParams.getIdZona()); //Fix 41247
      cdv.setIdCategoriaVendita(condVenParams.getIdCategoriaVendita()); //Fix 43127
      cdv.setArticolo(condVenParams.getArticolo());
      cdv.setConfigurazione(condVenParams.getConfigurazione());
      cdv.setUnitaMisura(condVenParams.getUMVendita());
      cdv.setUMVen(condVenParams.getUMVendita());
      cdv.setQuantita(condVenParams.getQuantitaInUMRif());
      cdv.setImporto(condVenParams.getImporto());
      cdv.setModalitaPagamento(condVenParams.getModalitaPagamento());
      cdv.setDataValidita(condVenParams.getDataValidita());
      if (condVenParams.getListinoVendita() != null)
          cdv.setAttivoListinoCampagna(condVenParams.getListinoVendita().getListinoCampagna() != null);
      else
          cdv.setAttivoListinoCampagna(false);

      cdv.setAgente(condVenParams.getAgente());
      cdv.setSubAgente(condVenParams.getSubagente());
      cdv.setValuta(condVenParams.getValuta());
      cdv.setVersione(condVenParams.getArtVersione());
      cdv.setNumeroImballo(condVenParams.getNumeroImballo());

      cdv.setUMPrmMag(condVenParams.getUMMagazzino());
      cdv.setQuantitaPrmMag(condVenParams.getQuantitaInUMPrm());
      if (condVenParams.getUMSecMagazzino() != null)
        cdv.setUMSecMag(condVenParams.getUMSecMagazzino());
      if (condVenParams.getQuantitaInUMSec() != null)
        cdv.setQuantitaSecMag(condVenParams.getQuantitaInUMSec());

      if (condVenParams.getUMMagazzino() != null && condVenParams.getQuantitaInUMPrm() != null) {
      	if (!condVenParams.getUMMagazzino().equals(condVenParams.getUMVendita()))
      		doppiaRicerca = true;
      }

      cdv.setAziendaKey(condVenParams.getIdAzienda());
      completaDatiCondVenditaPers(condVenParams, cdv);
      
      if (condVenParams.getPrcScontoIntestatario() != null)
        cdv.setPrcScontoIntestatario(condVenParams.getPrcScontoIntestatario());
      if (condVenParams.getPrcScontoModalita() != null)
        //cdv.setPrcScontoModalita(condVenParams.getPrcScontoIntestatario());//Fix 24273
        cdv.setPrcScontoModalita(condVenParams.getPrcScontoModalita());//Fix 24273

      cdv.setScontoModalita(condVenParams.getScontoModalita());        
      this.setCondizioniDiVendita(cdv);
      
      PersDatiVen psnDatiVen = PersDatiVen.getPersDatiVen(condVenParams.getIdAzienda());
      boolean personalizzati = psnDatiVen != null;

      if (condVenParams.getListinoVendita() == null && 
      		personalizzati &&
          psnDatiVen.getListinoVendita() != null &&
          psnDatiVen.getListinoVendita().getValuta().equals(condVenParams.getValuta())) {
      	if (RicercaCondizioniDiVendita.isListinoAuthorized(psnDatiVen.getListinoVendita())) {
      		condVenParams.setListinoVendita(psnDatiVen.getListinoVendita());
      		cdv.setListinoVendita(condVenParams.getListinoVendita());
      	}
      }
      
      ListinoVendita lv = null;
      if (condVenParams.getListinoVendita() != null) {
          lv = condVenParams.getListinoVendita().getListinoCampagna();
          if (!RicercaCondizioniDiVendita.isListinoAuthorized(lv)) {
              lv = null;
              cdv.setAttivoListinoCampagna(false);
          }
      }

      if (!ricercaProvv) {
      	if (lv != null) {
      		// Ricerca nel listino campagna con UM di vendita
      		//listinoIndividuato = this.individuaListino(lv, condVenParams.getVisualizzaDettagliParam()); //Fix 39206
      		listinoIndividuato = this.individuaListino(lv, condVenParams.getVisualizzaDettagliParam(), condVenParams.getRifDataPrezzoScontiParam()); //Fix 39206

      		this.getCondizioniDiVendita().setUMPrezzo(CondizioniDiVendita.VENDITA);
          // Ricerca nel Listino campagna con UM di magazzino
      		if (lv != null && !listinoIndividuato && doppiaRicerca) {
              this.getCondizioniDiVendita().setQuantita(condVenParams.getQuantitaInUMPrm());
              this.getCondizioniDiVendita().setUnitaMisura(condVenParams.getUMMagazzino());
              this.getCondizioniDiVendita().setUMPrezzo(CondizioniDiVendita.MAGAZZINO); //Fix 34667
              //listinoIndividuato = this.individuaListino(lv, condVenParams.getVisualizzaDettagliParam()); //Fix 39206
              listinoIndividuato = this.individuaListino(lv, condVenParams.getVisualizzaDettagliParam(), condVenParams.getRifDataPrezzoScontiParam()); //Fix 39206
              //this.getCondizioniDiVendita().setUMPrezzo(CondizioniDiVendita.MAGAZZINO); //Fix 34667
          }
          if (!listinoIndividuato) {
          	this.getCondizioniDiVendita().setQuantita(condVenParams.getQuantitaInUMRif());
          	this.getCondizioniDiVendita().setUnitaMisura(condVenParams.getUMVendita());
          	lv = this.iCondizioniDiVendita.getListinoVendita();
          	//ricercaListino = this.individuaListino(lv, condVenParams.getVisualizzaDettagliParam()); //Fix 39206
          	ricercaListino = this.individuaListino(lv, condVenParams.getVisualizzaDettagliParam(), condVenParams.getRifDataPrezzoScontiParam()); //Fix 39206
          	this.getCondizioniDiVendita().setUMPrezzo(CondizioniDiVendita.VENDITA);
          	if (!ricercaListino && doppiaRicerca) {
          		this.getCondizioniDiVendita().setQuantita(condVenParams.getQuantitaInUMPrm());
          		this.getCondizioniDiVendita().setUnitaMisura(condVenParams.getUMMagazzino());
          		lv = this.iCondizioniDiVendita.getListinoVendita();
          		this.getCondizioniDiVendita().setUMPrezzo(CondizioniDiVendita.MAGAZZINO); //Fix 34667
          		//ricercaListino = this.individuaListino(lv, condVenParams.getVisualizzaDettagliParam()); //Fix 39206
          		ricercaListino = this.individuaListino(lv, condVenParams.getVisualizzaDettagliParam(), condVenParams.getRifDataPrezzoScontiParam()); //Fix 39206
          		//this.getCondizioniDiVendita().setUMPrezzo(CondizioniDiVendita.MAGAZZINO); //Fix 34667
          	}
          }
          else
          	this.getCondizioniDiVendita().setListinoVendita(lv);
      	}
      	else {
      		lv = this.iCondizioniDiVendita.getListinoVendita();
          this.getCondizioniDiVendita().setQuantita(condVenParams.getQuantitaInUMRif());
          this.getCondizioniDiVendita().setUnitaMisura(condVenParams.getUMVendita());
          //ricercaListino = this.individuaListino(lv, condVenParams.getVisualizzaDettagliParam()); //Fix 39206
          ricercaListino = this.individuaListino(lv, condVenParams.getVisualizzaDettagliParam(), condVenParams.getRifDataPrezzoScontiParam()); //Fix 39206
          this.getCondizioniDiVendita().setUMPrezzo(CondizioniDiVendita.VENDITA);
          if (!ricercaListino) {
          	this.getCondizioniDiVendita().setQuantita(condVenParams.getQuantitaInUMPrm());
          	this.getCondizioniDiVendita().setUnitaMisura(condVenParams.getUMMagazzino());
          	this.getCondizioniDiVendita().setUMPrezzo(CondizioniDiVendita.MAGAZZINO); //Fix 34667
          	//ricercaListino = this.individuaListino(lv, condVenParams.getVisualizzaDettagliParam()); //Fix 39206
          	ricercaListino = this.individuaListino(lv, condVenParams.getVisualizzaDettagliParam(), condVenParams.getRifDataPrezzoScontiParam()); //Fix 39206
          	//this.getCondizioniDiVendita().setUMPrezzo(CondizioniDiVendita.MAGAZZINO); //Fix 34667
          }
      	}
      } 

      //this.calcolaPrezzi(condVenParams.getUMVendita(), condVenParams.getUMMagazzino(), condVenParams.getVisualizzaDettagliParam()); //Fix 39206
      this.calcolaPrezzi(condVenParams.getUMVendita(), condVenParams.getUMMagazzino(), condVenParams.getVisualizzaDettagliParam(), condVenParams.getRifDataPrezzoScontiParam()); //Fix 39206
      // Prima di passare al calcolo delle provvigioni è bene rivedere il calcolo
      // dell'importo in funzione del risultato ottenuto dal calcolo dei prezzi.
      this.getCondizioniDiVendita().setImporto(calcoloImporto());
      this.calcolaSconti();
      
	  // fix 26981 >
      if (condVenParams.getPrcScontoArticolo1() != null) {
      	if (condVenParams.getPrcScontoArticolo1().equals("0")) {
      		this.getCondizioniDiVendita().setScontoArticolo1(null);
      	}
      	else {
      		if (!condVenParams.getPrcScontoArticolo1().trim().equals("")) {
      			this.getCondizioniDiVendita().setScontoArticolo1(toBigDecimal(condVenParams.getPrcScontoArticolo1()));
      		}
      	}
      }
      if (condVenParams.getPrcScontoArticolo2() != null) {
      	if (condVenParams.getPrcScontoArticolo2().equals("0")) {
      		this.getCondizioniDiVendita().setScontoArticolo2(null);
      	}
      	else {
      		if (!condVenParams.getPrcScontoArticolo2().trim().equals("")) {
      			this.getCondizioniDiVendita().setScontoArticolo2(toBigDecimal(condVenParams.getPrcScontoArticolo2()));
      		}
      	}
      }
      if (condVenParams.getPrcMaggiorazione() != null) {
      	if (condVenParams.getPrcMaggiorazione().equals("0")) {
      		this.getCondizioniDiVendita().setMaggiorazione(null);
      	}
      	else {
      		if (!condVenParams.getPrcMaggiorazione().trim().equals("")) {
      			this.getCondizioniDiVendita().setMaggiorazione(toBigDecimal(condVenParams.getPrcMaggiorazione()));
      		}
      	}
      }
      if (condVenParams.getCodSconto() != null) {
      	if (condVenParams.getCodSconto().equals("-")) {
      		this.getCondizioniDiVendita().setSconto(null);
      	}
      	else {
        	if (!condVenParams.getCodSconto().equals("")) {      		
        		String scontoKey = KeyHelper.buildObjectKey(new String[] {condVenParams.getIdAzienda(), condVenParams.getCodSconto()});
        		this.getCondizioniDiVendita().setScontoKey(scontoKey);
        	}
      	}
      }
	  // fix 26981 <
      
      // E' importante che sia chiamato il metodo degli sconti prima di quello delle
      // provvigioni in quanto viene effettuato un controllo sulle provvigioni in base
      // agli sconti.
      if (this.getCondizioniDiVendita().getAgente() != null ||
          this.getCondizioniDiVendita().getSubAgente() != null) {
          this.calcolaProvvigioni();
      }
      this.calcolaDatiPers(condVenParams, this.getCondizioniDiVendita());
      this.calcolaPrezzoAlNettoSconti();

      // fix 25658 >
      this.calcolaPrezzoAlNettoScontiTotali();
      // fix 25658 <

      return this.getCondizioniDiVendita();
  }
  }
  
  public void completaDatiCondVenditaPers(CondizioniDiVenditaParams condVenParams, CondizioniDiVendita cdv) {  	
  }
  
  public void calcolaDatiPers(CondizioniDiVenditaParams condVenParams, CondizioniDiVendita cdv) {  	
  }
   
  //24273 fine
  
  // fix 26981 <
  private BigDecimal toBigDecimal(String s) {
  	BigDecimal d = null;
  	try {
  		d = new BigDecimal(s);
  	}
  	catch (Throwable t) {}
  	return d;
  }
  // fix 26981 <  

  //Fix 24634 - inizio
  protected Articolo getArticoloDettagliArtCfg(Configurazione cfg, ValoreVariabileCfg valoreVarCfg) {
	  return valoreVarCfg.getArticolo();
  }
  //Fix 24634 - fine


  // fix 25658 >
  private boolean isApplicaArrotondamentoPrezzoNetto = false;

  public boolean isApplicaArrotondamentoPrezzoNetto() {
	  return isApplicaArrotondamentoPrezzoNetto;
  }

  public void setApplicaArrotondamentoPrezzoNetto(
		  boolean isApplicaArrotondamentoPrezzoNetto) {
	  this.isApplicaArrotondamentoPrezzoNetto = isApplicaArrotondamentoPrezzoNetto;
  }
  
  protected static Object getIdObj (Object obj) {
  	Object id = null;
  	if (obj instanceof BusinessObject) {
  		id = ((BusinessObject)obj).getKey();
  	}
  	else {
  		id = obj;
  	}
  	return id;
  }

	// Fix 41247 Inizio
  
  public static CondizioniDiVendita getCondizioniVendita(
          String idListino,
          String idCliente,
          String idArticolo,
          String idEsternoConfigurazione,
          String idUMVendita,
          String qtaVendita,
          String qtaMagazzino,
          String idModPagamento,
          String dtOrdine,
          String dtConsegna,
          String idAgente,
          String idSubagente,
          String idUMMagazzino,
          String idValuta,
          String rifDataPrezzoSconti,
          boolean visualizzaDettagli,  
          String prcScontoIntestatario,
          String prcScontoModalita,
          String idScontoModalita,
          String idVersione,
          String numeroImballo,
          String idUMSecMag,
          String qtaSecMag,
          String idDivisione,
          String idZona
          ) { 
	  			if(idZona== null) idZona= "";
				CondizioniDiVenditaParams condVenParams = (CondizioniDiVenditaParams) Factory
						.createObject(CondizioniDiVenditaParams.class);
				condVenParams = condVenParams.impostaParamsCondizioniDiVendita(null, Azienda.getAziendaCorrente(), 
						idListino, idCliente, idArticolo, idEsternoConfigurazione, idUMVendita, qtaVendita,
						qtaMagazzino, idModPagamento, dtOrdine, dtConsegna, idAgente, idSubagente, idUMMagazzino,
						idValuta, rifDataPrezzoSconti, visualizzaDettagli, prcScontoIntestatario, prcScontoModalita,
						idScontoModalita, idVersione, numeroImballo, idUMSecMag, qtaSecMag, idDivisione, idZona);

				CondizioniDiVendita condVen = RicercaCondizioniDiVendita.getCondizioniVendita(condVenParams);
				return condVen;
			}

//Fix 41247 fine
  
  
//Fix 43127 Inizio
  
 public static CondizioniDiVendita getCondizioniVendita(
         String idListino,
         String idCliente,
         String idArticolo,
         String idEsternoConfigurazione,
         String idUMVendita,
         String qtaVendita,
         String qtaMagazzino,
         String idModPagamento,
         String dtOrdine,
         String dtConsegna,
         String idAgente,
         String idSubagente,
         String idUMMagazzino,
         String idValuta,
         String rifDataPrezzoSconti,
         boolean visualizzaDettagli,  
         String prcScontoIntestatario,
         String prcScontoModalita,
         String idScontoModalita,
         String idVersione,
         String numeroImballo,
         String idUMSecMag,
         String qtaSecMag,
         String idDivisione,
         String idZona,
         String idCategoriaVendita
         ) { 
	  			if(idCategoriaVendita== null) idCategoriaVendita= "";
				CondizioniDiVenditaParams condVenParams = (CondizioniDiVenditaParams) Factory
						.createObject(CondizioniDiVenditaParams.class);
				condVenParams = condVenParams.impostaParamsCondizioniDiVendita(null, Azienda.getAziendaCorrente(), 
						idListino, idCliente, idArticolo, idEsternoConfigurazione, idUMVendita, qtaVendita,
						qtaMagazzino, idModPagamento, dtOrdine, dtConsegna, idAgente, idSubagente, idUMMagazzino,
						idValuta, rifDataPrezzoSconti, visualizzaDettagli, prcScontoIntestatario, prcScontoModalita,
						idScontoModalita, idVersione, numeroImballo, idUMSecMag, qtaSecMag, idDivisione, idZona, idCategoriaVendita);

				CondizioniDiVendita condVen = RicercaCondizioniDiVendita.getCondizioniVendita(condVenParams);
				return condVen;
			}

//Fix 43127 Fine
  
  // fix 25658 <

 //72296 Softre -->
 protected void yOrdinaTestateSelezionate(Iterator iter) {
 }
 //72296 Softr <--
}