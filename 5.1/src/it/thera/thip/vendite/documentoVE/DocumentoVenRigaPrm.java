package it.thera.thip.vendite.documentoVE;

import java.math.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;

import com.thera.thermfw.base.*;
import com.thera.thermfw.collector.*;
import com.thera.thermfw.common.*;
import com.thera.thermfw.persist.*;
import com.thera.thermfw.security.*;
import com.thera.thermfw.security.gui.*;
import it.thera.thip.base.agentiProvv.*;
import it.thera.thip.base.articolo.*;
import it.thera.thip.base.azienda.*;
import it.thera.thip.base.catalogo.*;
import it.thera.thip.base.cliente.*;
import it.thera.thip.base.commessa.Commessa;
import it.thera.thip.base.comuniVenAcq.*;
import it.thera.thip.base.comuniVenAcq.web.*;
import it.thera.thip.base.comuniVenAcq.web.DatiArticolo;
import it.thera.thip.base.documenti.*;
import it.thera.thip.base.generale.*;
import it.thera.thip.base.listini.*;
import it.thera.thip.base.partner.Nazione;
import it.thera.thip.base.prezziExtra.*;
import it.thera.thip.base.qualita.*;
import it.thera.thip.cs.*;
import it.thera.thip.datiTecnici.configuratore.*;
import it.thera.thip.datiTecnici.distinta.*;
import it.thera.thip.datiTecnici.modpro.*;
import it.thera.thip.magazzino.generalemag.*;
import it.thera.thip.magazzino.matricole.StoricoMatricola;
import it.thera.thip.magazzino.movimenti.CausaleMovimentoMagazzino;
import it.thera.thip.magazzino.saldi.*;
import it.thera.thip.produzione.documento.DocumentoProduzione;
import it.thera.thip.servizi.ordsrv.*;
import it.thera.thip.vendite.generaleVE.*;
import it.thera.thip.vendite.ordineVE.*;
import it.thera.thip.vendite.prezziExtra.*;
import it.thera.thip.atp.PersDatiATP;
import it.thera.thip.servizi.comuniNlgCnt.UtilGener;
import it.thera.thip.servizi.comuniNlgCnt.UtilTemp;
import it.thera.thip.servizi.documento.*;  //MG FIX 7949
import it.thera.thip.base.interfca.RiferimentoVociCA;   //MG FIX 8659
import it.thera.thip.base.interfca.SottogruppoContiCA;
import it.thera.thip.acquisti.generaleAC.DatiCA;  //MG FIX 8659
import it.thera.thip.servizi.anagraficiBase.PersDatiServizi;
import it.thera.thip.servizi.anagraficiBase.PersDatiServiziUM;
import it.thera.thip.servizi.anagraficiBase.AnagraficaBeni;
import it.thera.thip.base.generale.Calendario;
import it.thera.thip.servizi.noleggi.CalcoloPeriodoAddebito;


/**
 * DocumentoVenRigaPrm.<br>
 * <br><br><b>Copyright (c): Thera SpA</b>
 * @author Enrico Masserdotti
 */
/*
 * Revisions:
 * Number  Date         Owner    Description
 *         30/09/2003   MSaccoia Modificata gestione riga omaggio/offerta (VxO, V+O)
 * 01136   02/12/2003   PM
 * 01205   17/12/2003   PM
 *         23/12/2003   ME       Prima importazione in THIP 1.1 con nuova generazione
 *                               righe secondarie
 *         23/12/2003   ME       Inserita gestione modelli produttivi per righe kit
 *         13/01/2004   ME       Modificate chiamate a metodi getEsplosioneNodoModello e
 *                               generaRigheSecondarieEsplosioneModello
 * 01336   27/01/2004   DB
 * 01377   04/02/2004   ME
 * 01390   10/02/2004   DB
 * 01425   12/02/2004   DB
 * 01480   20/02/2004   CHAKHARI modification in the method saveOwnedObjects,
 *                               save directly the oneTomany without using
 *                               getOneTomany().save(rc).
 * 01684   25/03/2004   GScarta  introdotto check riga duplicata condizionato
 * 01918   10/05/2004   ME
 * 02029   24/05/2004   ME
 *         17/06/2004   ME       Fix 2105: migrazione da THIP 1.0 a THIP 1.1 delle
 *                               fix 1922-1977-2006-2028-2053
 * 02151   23/06/2004   Mekki    cambiare tipo de Quantita di Esplosione Distinta:int--->BigDecimal.
 * 02001   24/05/2004   DZ       Modificato calcolaImportiRiga in seguito a spostamento di calcolaImposta
 *                               da DocumentoOrdineRiga a ImportiRigaDocumentoOrdine.
 * 02380   07/09/2004   ME       Segnalazione errore per associazione catalogo/articolo
 *                               inesistente
 * 02407   10/09/2004   DZ       Aggiunto metodo di servizio getNotNullValue utilizzato in stornaImporti...
 * 02543   04/10/2004   GScarta  Gestione documenti collaudo
 * 02563   11/10/2004   ME       Aggiunto metodo cambiaArticolo
 * 02614   14/10/2004   DB
 * 02636   15/10/2004   DB
 * 02567   22/10/2004   SR       Spostati metodi in DocumentoVenRigaLottoPrm
 * 02921   14/12/2004   SR       aggiunto metodo isResoFornitoreDaTrasferire()
 * 03212   31/01/2005   MN       Ridefinito il metodo setTipoModello(ModproEsplosione esplosione).
 *                               Modificato il metodo chckQuadraturaLottiRigheSecondarie.
 *                               Settato il listino nmelle righe secondarie.
 * 03197   01/02/2005   ME       Aggiunti attributi iServeRicalcoloProvvAgente e
 *                               iServeRicalcoloProvvSubagente
 * 03187   21/01/2005   LP       Aggiunto generazione automatica dei lotti
 * 03016   20/01/2005   DB
 * 03246   11/02/2005   ME       Aggiunta condizione nel metodo salvaRiga (come
 *                               nelle righe dei documenti di acquisto)
 * 03319   28/02/2005   MN       Modificato il metodo checkQuadraturaLottiRigheSecondarie(),
 *                               il metodo getEsplosioneNodoModello() erstiruisce sempre
 *                               un'istanza della classe EspNodoArticolo che puo' avere
 *                               la lista delle componenti vuota.
 * 03362   08/03/2005   MN       Modificato il metodo generaRigaSecondariaModello(), aggiunto il set
 *                               dell'UM Sec.
 * 03489   04/04/2005   MN       Migrazione 2.0
 * 03368   10/03/2005   MN       Modificato il metodo save().
 * 03611   18/04/2005   ME       Cambiato controllo su ricalcolo qta righe secondarie
 * 03659   22/04/2005   ME       Arrotondamento delle quantità delle righe
 *                               secondarie generate da distinta
 * 03688   28/04/2005   PM       Evasione ordini: ordine con un articolo gestito
 *                               a lotti con proposizione automatica dei lotti
 *                               "multipla", al salvataggio della riga documento
 *                               la quantità totale dell'articolo viene riportata
 *                               uguale a quella dell'ultimo lotto individuato
 * 03700   02/05/2005   ME       Nuova gestione righe secondarie
 * 03738   09/05/2005   ME       Modifiche su recupero provv. agenti su scala sconti
 * 03770   13/05/2005   ME       Modifiche per miglioramento prestazioni
 * 03814   23/05/2005   MN       Modificato il metodo creaRigaOmaggio().
 * 03230   28/04/2005   ME       Aggiunti metodi getRigaDestinazionePerCopia e
 *                               copiaRiga
 * 03929   16/06/2005   ME       Modificato ricalcolo prezzo da righe secondarie
 * 03769   17/05/2005   BP       aggiunta metodi su prezzo di riferimento.
 * 03953   21/06/2005   ME       Modificato metodo copiaRiga: anticipato settaggio
 *                               riga primaria su righe secondarie
 * 03954   21/06/2005   DZ       Modificato metodo gestioneKit
 * 04060   05/07/2005   ME       Nel caso di articolo kit con tipo calcolo prezzo
 *                               da componenti non fa scattare il recupero dati vendita
 *                               alla save. Forzato il calcolo del prezzo da righe
 *                               secondarie nella save.
 * 04191   28/07/2005   ME       Modificato calcolo prezzo anche per esplosione modello
 * 04228   05/08/2005   PM       Aggiunte modifiche per gestire il passaggio a
 *                               Logis dei documenti di vendita
 * 04348   20/09/2005   MG       Implementazione gestione sconti provvigione agente
 * 04356   10/10/2005   DZ       Aggiunto COntoTrasformazione.
 * 04453   10/10/2005   DB       Modificato reperimento coeff. impiego e qta totale
 *                               Modificata UMRif per righe secondarie (impostava DefAcq in espl distinta)
 *                               Passata cfg in esplosione distinta
 *                               Modificato controllo quadr. righe secondarie (checkQuadraturaLottiRigheSecondarie)
 * 04486  19/10/2005    MN
 * 04532  24/10/2005    MG       Correzione a metodo stornaImportiRigaDaTestata (tolta esclusione per righe spesa/omaggio)
 * 04607  11/11/2005    DZ       Generazione righe secondarie per articolo kit: se non esistono modPro o distinta,
 *                               la riga prm deve essere salvata ugualmente deve essere dato un warning NON bloccante
 *                               (modificato generaRigheKit, aggiunti checkAll e checkRigheSecondarie).
 * 04669  22/11/2005    DZ       getEsplosioneNodo: usata non getQtaInUMPrm ma getServizioQta.getQtaInUMPrm
 *                               checkRigheSecondarie: modifiche per ottimizzazione performances.
 * 04670  02/12/2005    MN       Gestione Unità Misura con flag Quantità intera.
 * 04656  23/11/2005    MG       Modificato metodo creaLottiAutomatici per inserire getsione doc.vendita conto trasformazione
 * 04805  21/12/2005    MN       Modificato il metodo generaRigaSecondariaModello(), in quanto durante il calcolo della
 *                               quantità veniva utilizzato l'articolo della primaria
 *                               e non quello della secondaria.
 * 04858  05/01/2006    ME       Omaggi: sistemato calcolo delle quantità da assegnare
 *                               alla riga omaggio
 * 04923  19/01/2006    PM       Commentato metodo testTrasmissioneDoc
 * 04976  30/01/2006    ME       Aggiunti controlli per evitare che cada l'importatore
 * 05110  01/03/2006    ME       Eliminata modifica introdotta da fix 3953:
 *                               alle righe secondarie del documento di origine
 *                               veniva assegnata la riga primaria di destinaz.
 *                               In copia aggiunta gestione particolare per righe
 *                               secondarie.
 * 05102  27/02/2006    ME       Aggiunti controlli su righe annullate per
 *                               calcolo/storno totali testata
 * 05117  14/03/2006    GN       Correzione nella gestione delle unità di misura con flag Quantità intera
 * 05270  05/04/2006    BP       New --> Factory.
 * 05282  06/04/2006    MN       Modificato il metodo proponiLotti(), se non viene trovato
 *                               un lotto e la riga documento di trova in stato definitivo,
 *                               questa viene portata in stato provvisorio in modo da
 *                               scatenare la procedura di creazione del lotto dummy.
 * 05350  26/04/2006    MN       Modificato il metodo proponiLotti(..), per la gestione dei lotti automatici.
 * 05330  10/04/2006    DBot     Utilizzo readOnlyElementWithKey per gestione corretta classi Cacheable
 * 05634  04/07/2006    ME       Aggiunto metodo annullaOldRiga
 * 05749  27/07/2006    MN       Modificato il metodo proponiLotti(), nel caso di riga documento
 *                               in stato provvisorio la qta reperita dal lotto non veniva settata
 *                               nella qta proposta evasione. In questo caso il lotto si trovava
 *                               con una qta prp eva a 0, e in fase di salvataggio del doc, il
 *                               programma faceva quadrare correttamente le qta spostando il
 *                               delta della qta sul lotto dummy.
 *                               Aggiunto , nel caso di stato provv (isPropEva), il settaggio
 *                               delle qta proposte.
 *                               Inoltre la qta del lotto veniva reperita chiamando il metodo
 *                               che restituisce la qta disponibile, mentre deve essere utilizzato il metodo
 *                               che ritorna la giacenza netta.
 * 05501  05/07/2006    GM       aggiunto trasmissione a Logis
 * 06150  30/10/2006    PM       Aggiunto metodo impostazioniPerCopiaRiga
 * 06209  09/11/2006    MN       Aggiunto metodo controllaDispUnicoLottoEffettivo(), per il
 *                               controllo della giacenza sull'unico lotto effettivo inserito.
 * 06204  13/11/2006    MG       Inserire reupero provv. agenti nel caso kit (non gestito a magazzino)
 * 06318  28/11/2006    MN       Nel caso di unico lotto effettivo, la forzatura del prelievo del
 *                               lotto deve essere condizionata all'autorizzazione dell'utente sul relativo
 *                               task di "Forzatura utilizzo lotto".
 * 06332  29/11/2006    GM       il passaggio a Logis viene attivato solo se la super.save() non torna errori
 * 06481  29/01/2007    MG       gestione sconto fine fattura
 * 06920  19/03/2007    MN       Nel caso di unico lotto "effettivo", deve essere controllata
 *                               la giacenza sul lotto.
 * 06754  28/02/2007    MG       gestione righe secondarie da fatturare
 * 06439  07/03/2007    ME       Aggiunta logica per gestione servizi/noleggi
 * 06965  21/03/2007    MN       Modificate le chiamate ai metodi di calcolo
 *                               giecenza/disponibilità su ProposizioneAutLotto.
 * 05518  21/03/2007    LP       Aggiunto calcolo valoreTotaleRiga
 * 07093  20/04/2007    LP       Aggiunto gestione ricerca tramite barcode
 * 07264  09/05/2007    DB
 * 07220  30/04/2007    DBot     Introdotta gestione blocchi evasione per accantonato e prenotato
 * 07375  28/05/2007    ME       Aggiunto controllo su ordine servizio
 * 07587  06/07/2007    DB       Spostato in alto il metodo testTrasmissioneDoc
 * 07187  09/07/2007    DBot     Modificato test per blocchi accprn per documenti non collegati a riga ordine
 * 07053  07/09/2007    LP       Aggiunto reperimento di idConfig e idVersione dal barcode
 * 07858  14/09/2007    ME       Aggiunta descrizione articolo a righe second.
 *                               di tipo DOTAZIONE per ordini di servizio
 * 07825  03/09/2007    MG       Modificato test in metodo SAVE per escludere le righe assist./manutenzione dalla creazione delle righe secondarie
 * 07949  12/10/2007    MG       Modificato metodo eliminaRiga
 * 08393  10/12/2007    MN       Se la riga documento è evasa da una riga ordine gestita a contratto,
 *                               il prezzo deve essere recuperato dalle conedizioni di vendita definite sul contratto.
 * 08520  10/01/2007    ME       Modifica su generazione  righe sec . da ord. srv .
 * 08495  09/01/2008    MG       Modificato metodo save per gestione righe omaggio es.art.15
 * 08597  31/01/2008    MG       Gestione dati contabilità analitica in righe sec. da materiali ord. srv.
 * 08659  05/02/2008    MG       Aggiunta gestione dati CA in generaRigheKit
 * 08640  01/02/2008    ME       Aggiunto metodo generaRigheSecDaRisorseOrdSrv
 * 08886  19/03/2008    GM       la riga omaggio veniva creata senza riga lotto,
 *                               quindi la trasmissione a Logis andava in errore e il doc. non poteva essere salvato
 * 08707  06/03/2008    ME       Aggiunto metodo regressioneMatricole
 * 08913  25/03/2008    OV        Recupero dati di contabilità analitica al momento del salvataggio (per inserimenti batch)
 * 08977  04/04/2008    MN       Modiifcato il metodo generaRigheKit(...) nel calcolo della qta con gestione a qta intere
 *                               veniva considerato l'articolo della riga prm e non l'articolo del kit.
 * 09061  17/04/2008    OV       Rimosso salvataggio dopo recupero dati CA
 * 09221  15/05/2008    ME       Modifiche a metodi di creazione righe
 *                               secondarie per documenti di noleggio
 * 09181  08/05/2008    DBot     Allentati controlli di accPrn quando causale non mov. magazzino
 * 09251  19/05/2008    DBot     Resa possibile evasione provvisoria senza copertura con controlllo acc/prn = ERRORE
 * 09671  25/08/2008    PM       Se una riga sec ha lo stesso articolo della riga prm e
 *                               il coefficente è 1 allora la sua quantita deve
 *                               essere uguale a quella della riga prm.
 * 09867  17/11/2008    DB      Descrizione con PAOLA
 * 10604  24/03/2009    MG       Correzione metodo resetDatiVenditaDaDocSrv() (sostituito 0 con null)
 * 10750  29/04/2009    MG       doppia gestione provvigione scala sconti
 * 10932  12/06/2009    MG       corretto messaggio d'errore in metodo proponiLotti
 * 10955   17/06/2009  Gscarta   modificate chiamate a convertiUM dell'articolo per passare la versione
 * 10987  29/06/2009    DB
 * 10882      30/06/2009    RH          Modificato  per generazione di Documenti DDT caso Ordine != Noleggio
 * 11123  13/07/2009    DB
 * 11009   22/07/2009   MN      Gestione intercompany
 * 11239   05/08/2009   DB
 * 11198   31/08/2009   MN      Intercompany: modifiche per gestione flusso triangolazione vendite
 * 11084   05/08/2009   PM      Gestione  picking e packing
 * 11170  17/09/2009    PM
 * 11931  13/01/2010    GN      Corretto metodo di save affinchè in addebito servizi non vengono generate
 *                              delle righe secondarie doppie (la generazione deve avvenire solo il documento viene
 *                              generato dal batch di nuovo doc. sped./rientro)
 * 11529  26/10/2009    ME      Propagazione commessa su righe secondarie al salvataggio
 * 11420  02/10/2009   MBH      Modifica la condizione di generazione di documento di Collaudo :
 *                              Azione magazzino = Entrata.
 * 11529  26/10/2009    ME      Propagazione commessa su righe secondarie al salvataggio
 * 11924   14/01/2010   GScarta introdotto in save il controllo ed eventuale modifica
 *                              della configurazione sulla riga ordine evasa
 * 11951   15/01/2010   GScarta Riporto in 3.0.8 della fix 11924. Riallineamento alle fix 11931 e 11420
 * 11789  21/01/2010   LTB      elemina generazione rigaSec in caso di copia documentoVendita noleggio
 * 12258  06/04/2010    GM      se la riga deve essere trasmessa a Logis setto tipoCollegamentoLogistica=LOGIS
 * 12046  26/01/2010    MG      Escluse da convalida le righe secondarie in stato annullato
 * 12508  20/04/2010   DBot     Aggiunta la gestione dei pesi e del volume
 *                              Aggiunto setSalvaRigaPrimaria(false) a creazione righe seconfdarie servizi/noleggi
 * 12673  23/06/2010   M.Anis  il metodo getUnicoLottoEffettivo() ridefinito in su erede
 * 12790  13/07/2010   M.Anis  nelle righe secondarie generati da ordini servizio/noeleggi non copiare il commenti dell'ordine
 * 13110   30/08/2011   DBot    Aggiunto test su attivazione calcolo pesi e volume
 * 13493  11/11/2010    RA     Agguinto setIdAzienda() al proponiLotti()
 * 13911  28/01/2011  PM       Migliorata la gestione degli warning nell'intercompany
 * 13494  12/11/2010   OC       corretto totale dal documento di vendita
 * 13494  04/02/2011   MBH      trasferisci del metodo stornaImportiRigaDaTestata ha DocumentoVenRiga
 * 13515  30/11/2010   TF       Calcolo provvigioni su prezzo extra
 * 13831  03/03/2011  Amara     Aggiunto idCliente nel creaProposizioneAutLotto quando magazzino e di conto lavoro interno
 * 13832  09/03/2011   OC       Commento  la condizione relativa alla gestione cali e al monte metalli nel metodo creaLottiAutomatici
 * 14069  10/03/2011   AYM      rendre public il metodo rendiDefinitivoSalva()
 * 14225  31/03/2011   OC       Modificare la generazione di righe DocumentoSec a partire di una distinta per copiare il campo Nota e DacumentoMM
 * 12572  13/05/2010  GScarta   Nuova gestione numero imballo
 * 14738  29/06/2011  DBot      Integrazione a standard fix 12572
 * 14931  30/08/2011  DBot      Aggiunta gestione pesi/volume ceramiche
 * 14727  14/07/2011   RA       Gestione attributi DescrizioneExtArticolo
 * 14670  20/07/2011   Linda    Modificare il metodo generaRigheKit()
 * 15359  28/11/2011  MZ        Per integrazione fix 14534
 * 14922  02/01/2012   TF       Correzione in metodo generaRigheSecDaMaterialiOrdSrv
 * 16267  19/04/2012  MZ       In checkArticoloCatalogoCompat tolto controllo compatibilità su UmPrm e Umsec
 * 16032  23/04/2012  AYM       In caso di ResoMerce da Cliente AND Articolo.Matricolato = Y AND (CodificaAutomaticaLottiAcquisti=ND
                                OR CodificaAutomaticaLottiProduzione=ND OR CodificaAutomaticaLottiLavEsterna=ND) proporre Lotto ND su DocVenLot.
 * 16707  30/07/2012  GN        Ripristinata generazione DDT per i materiali di un ordine di servizio
 * 16754  11/09/2012  AYM       Corretto il problema "Entità non trovata" nella  elimina  di documento vendita in  caso di presenza di riga omaggio collegata.
 * 18127  19/06/2013  RA	Corretto il problema di evasione ordine con articolo di riga primaria kit e articolo di riga secondaria gestito lotti
 * 18309  24/07/2013  AA        Annulamento del fix 18127
 * 19215  12/02/2014  AYM       Gestione il flag "Quantità attesa entrata disponibilié" nella modello  di "Proposte evasione"
 * 19628  23/04/2014  Linda     Modificato metodo aggiornaProvvigioni().
 * 12755  09/11/2013  CO      PERS_STD: hook per personalizzare creazione righe secondarie da ordine servizio
 * 19920  04/09/2014  Ichrak    Riallineamento cum 4.0.4 16242
 * 20230  08/09/2014  DB        Il metodo cambia articolo corregge la scala delle provvigioni prima di settarle
 * 18703  14/11/2013  Ichrak    Aggiungere il metodo per calcolo importi di righe spese percentuale
 * 19757  22/05/2014  Ichrak   Correzione del cast su EspNodoArticoloBase
 * 18156  25/06/2013  MA        L'autorizzazione alla "forzatura lotto" in verde chiaro equivale alla non autorizzazione
 * 17639  14/05/2013  TF        Agevolazioni per personalizzazioni : metodo da protected a public
 * 20387  26/12/2014  Linda     Se in copia di una riga primaria che possiede righe secondarie l'utente cambia la configurazione, al salvataggio il sitema deve emettere un warning.
 * 21094  25/02/2015  Linda     Introdurre un controllo al salvataggio delle Righe documento vendita nel caso di fatturaPA, per avvisare l'utente con un warning se i valori dei
 *                              campi Cig e Cup delle commesse delle righe sono differenti da quelli della commessa della testata.
 * 18753  18/11/2013  Linda     Modificato il metodo save().
 * 22229  29/09/2015  Linda     Modificato metodo getProvvigioneDaSconto().
 * 22850  18/01/2016  PM        Evasione ord vendita:se la proposizione automatica mi individua un solo lotto per evadere la qta prm di magazzino della riga e il flag ricalcolo è spento, la qta della riga lotto è rese uguale alle quella della riga documento
 * 22823  15/01/2016  LTB       Il rendi definivo mette a definitivo lo stato avanzamento delle righe annullate
 * 22839  15/01/2016  Linda     Redefine metodo getEntityRiga().
 * 23345  04/04/2016  Linda     Aggiunto metodo controlloRicalcoloCondizioniVen().
 * 23515  04/05/2016  Linda     Risolvere il problema della fix 22850 anche in presenza di lotti multipli.
 * 23709  03/06/2016  OCH       Se in modifica di una riga primaria che possiede righe secondarie l'utente cambia la configurazione, al salvataggio il sitema deve emettere un warning. 
 * 23896  03/07/2016  PM        In stampa fattura non vengono ripresi i prezzi delle righe secondarie anche nela caso di kit non gestito a magazzino con prezzo dato dalla somma dei componenti con markup
 * 24070  29/08/2016  MBH       redefine metodo disabilitaAggiornamentoSaldiMovimentiSuiLotti per risolvere il problema : La stampa fattura accompagnatoria azzera la quantità spedita sui lotti delle righe secondarie.  
 * 24190  20/09/2016  OCH       Nella generazione delle righe kit se l'attributo KitRecuperaMagDaMod della causale e a true deve impostare valore del magazzino con il valore di magazzino della riga di esplosione 
 * 24299  10/10/2016  Jackal    Gancio per consentire personalizzazioni in 
 *                              calcolo sconto su scala sconti
 * 24493  11/11/2016  OCH       Correzzione Fix 24190  
 * 24569  25/11/2016  OCH       Corretto evasione di un ordine di trasferimento con una causale di reso e di trasferimento
 * 24613  05/12/2016  Linda     Gestione il salvataggio del dettaglio riga valore configurazione.
 * 23604  16/01/2017  AYM       Aggiunto il param articolo nella error "THIP300203".
 * 25004  21/02/2017  OCH       Recuperato assoggIva da ArticoloFornitore se valorizzato
 * 25106  07/03/2017  EP				Controlli per CM
 * 25214  19/03/2017  PM		Se inserisco una riga con un articolo kit non gestito il cui prezzo è dato dalla somma dei componenti al salvataggio della riga non vengono calcolate le provvigioni 2 dell'agente e del subagente.
 * 25818  25/05/2017  OCH       Ridefinizione propagaDatiTestata
 * 26072  11/07/2017  OCH       Ignorato checkIdEsternoConfigInCopia nel caso di CM      
 * 26145  17/07/2017  Jackal    Gancio per consentire personalizzazioni in 
 *                              calcolo sconto su scala sconti
 * 26369  21/09/2017  PM        Migliorata la proposizione dei lotti quando le quantità nelle tre um non rispettano il fattore di conversione 
 * 26488  12/10/2017  LP        Integrazione CONAI con dichiarazione intento.
 * 26492  30/10/2017  Houda     Aggiunto gestione updateFatturaVenditaTipoBene.  
 * 26599  03/11/2017  Linda     La fix 25214 deve valere solo se l'articolo è kit non gestito a magazzino.  
 * 26807  24/01/2018  Linda     Aggiunto deleteOwnedRigheDdt().
 * 26939  07/02/2018  Linda     Gestione EscludiDaDichIntento.
 * 27148  22/03/2018  Linda     Varie modifiche per la fattura elett.
 * 27337  19/04/2018  Linda     Gestione caso di note di accredito con nessun movimento di magazzino e un articolo gestito a lotti.
 * 27649  02/07/2018  LTB     Aggiunta un controllo bloccante che impedisca di inserire una riga con quantità non intera se l'um è gestita a quantità intera. * 
 * 27616       03/09/2018    LTB        Se effettuo una riga d'ordine di vendita con un articolo kit con "tipo calcolo prezzi = dal costo dei componenti per markup", 
 * 28159  30/10/2018  MBH       Sistema NullPointerException nel metodo checkRigheSecondarie in caso di riga prm con Mag Null (esempio riga Kit Merce a valore in un documento NC) deve valorizzare il campo "provvigione 2 agente", nemmeno al salvataggio.
 * 28747  19/02/2019  RA		Sistema NullPointerException nel metodo isDaAggiornareFattVenTpBene  
 * 28653  22/02/2019  Jackal    Aggiunti ganci per personalizzazioni su calcolo provvigione 2 al salvataggio
 * 29396  24/05/2019  SZ		NullPointerException nel metodo checkQtaInUM in caso di QtaInUM(Rif , Prm ,Sec) contiene un valore errato il CM documenti vendita 
 * 30193  13/12/2019  SZ	  Gestione della configurazione nel caso di articoli di tipo ceramico.
 * 30871  06/03/2020  SZ		6 Descimale
 * 31168  30/04/2020  SZ		evitare nullpointerException nel metodo modificaProvv2Agente().
 * 32392  02/12/2020  PM	  Creando una nuova riga ordine/doc vendita se l'articolo è un kit gestito a magazzino e il prezzo è data dalla somma dei componenti, se si salva la riga senza passare dal tab dei prezzi la provvigione 2 non viene recuperata.
 * 32832  28/01/2021  SZ		Aggiunto il flag  isDisattivaPropostaAutoLotti. 
 * 32851  24/02/2021  SZ		Aggiunto il flag  ForsaQtaRigheSec
 * 33309  26/04/2021  Bsondes   Ricevere correttamente l'unità di misura dal materiale.
 * 33663  26/05/2021 YBA       Aggiungi i metodi checkProvvigione1Agente e checkProvvigione1Subagente().
 * 33762  08/06/2021  YBA      Correggere il problema che durante la  copia una riga documento di vendita, contenente un kit, e modificando la data della riga primaria la modifica non viene trasmessa alle righe secondarie
 * 33874  25/06/2021  SZ	    le righe secondarie dei kit non gestiti a magazzino non vengono propagate sul documento di vendita se generato da proposta d'evasione.
 * 33905  02/07/2021  SZ	    Nel caso di kit non gestito a magazzino il costo deve essere la somma dei costi delle righe secondarie
 * 33992  22/07/2021  SZ		gestione origine preferenziale
 * 34503  21/10/2021  SZ		Gestire automaticamente il conto anticipi
 * 34787  02/12/2021  YBA       Modificare il metodo modificaProvv2Agente
 * 35112  25/01/2022  SZ		Il campo nazione deve essere sempre disponibile e diventa obbligatorio nel caso in cui venga messo a true il flag gestione.
 * 35171  07/02/2022  MN        Aggiungi i metodi getQuantitaInUmVen()
 * 35337  24/02/2022  Jackal	Introdotto controllo su causale fix 34503
 * 35639  02/05/2022  LTB       Gestione assegnazione dei lotti (con proposizione automatica o manuale dei lotti) 
 * 								che consideri quanto già assegnato nello stesso documento 
 * 36654  30/09/2022  MR		sistema NullPointerException
 * 36857  25/10/2022  YBA       Modificato metodo getProvvigioneDaSconto().
 * 37023  15/11/2022  SZ		eliminata l'implementazione del fix 33905 per l'ordine di servizio.
 * 37203  30/11/2022  LP        Corretto eliminazione in caso di riga d tipo servizio con riferimento a un ods
 * 37248  07/12/2022  LTB       Annula la modifica del fix 35639 nel metodo controllaDispUnicoLottoEffettivo perchè non serve
 * 37244  08/12/2022  YBA      Corregere il problemea perché del copia un ordine cambiando la causale e le codizioni di vendita sono impostate Da documento gli sconti testata e la provvigione 1 delle righe non deveno essere riprese dalla testata.
 * 37274  14/12/2022  Bsondes   Facciamo un parametro di personalizzazione per decidere quale quantità impostare.
 * 37667  25/01/2023  SZ		Non deve scattare alcun controlo di disponibilità In caso di accantonato prenotato attivo su un documento di vendita di tipo reso
 * 37751  02/02/2023  LP        Migliorato errore save OrdSrv.
 * 38420  14/04/2023  SZ		In caso di fattura il sistema deve verifichare che l'assoggettamento iva della riga sia di tipo soggetto a iva quando l'utente indica manualmente una dichiarazione d'intento.
 * 38908  05/06/2023  LTB       In alcune condizioni di la proposizione atomatica dei lotti occupa in modo abnorme la memoria
 * 38696  07/06/2023  Bsondes   Valorizza DescrizioneExtArticolo.
 * 39456  31/07/2023  YA        Fix tecnica per addebito servizi
 * 39531  25/08/2023  SZ		Aggiunto il generazione Automatica lotto unitario. 
 * 40200  30/10/2023  SZ		Evitare il proposisione auto lotti si l'articolo non gestito il lotti.
 * 40160  03/11/2023  Bsondes   Corregere il calcola della quantità sui riga prm e riga seq con materiali di tipo accessorio con tipo addebito "FORFAIT".
 * 40084  30/10/2023  SZ		gestione matricola intercompany
 * 40598  04/12/2023  SZ		Generazione bene nel generazione Automatica lotto unitario.	
 * 40452  20/11/2023  SBR       Nell'evasione prebolla intellimag non generare le righe kit nella righe secondarie
 * 41393  20/02/2024  SZ	    i dati relativi a peso lordo e netto di riga vengono reperiti sempre dai dati tecnici dell'articolo, pur in presenza di dati differenti specificati nella versione indicata nella riga del documento. 
 * 41769  19/03/2024  SBR       Agg metodo superSalvaRiga() 
 * 41316  09/02/2024  SBR       Riallineamento Intellimag 5.0.2
 * 41132  31/01/2024  SZ		Gestione matricola nel clipboard
 * 41868  26/03/2024  TA        Aggiunto il metodo calcolaDateRigheSecondarie(rigaSec) e modificare il metodo impostazioniPerCopiaRiga()
 * 42238  06/05/2024  SBR       Annulare fix 41769, miglirare tempi di esucessione salda spesa   
 * 43361  23/09/2024  TA        Gli sconti di riga non vengono RIPORTATE nelle righe del documento in fase di ordine e/o doc. di vendita, scegliendo cliente + cantiere
 * 43795  05/11/2024  KD        redifine serveRicalProvv   
 * 43652  22/11/2024  TA        Elimina la metodo getQuantitaInUmVen()
 * 44409  25/12/2024  TA        Corretto l'anomalia si duplica una riga d'ordine che è in stato "definitiva", si imposta a "Provvisoria", le righe secondarie rimangono con stato "Definitiva"
 * 44522  15/01/2025  TA        Recupera i dati di Agente, Sub-Agente e Responsabile vendite.
 * 45131  07/03/2025  SZ	    evitari il nullPointerExecption
 * 44784  02/05/2025  RA	  	Rendi la ConfigArticoloPrezzo persistent
 * 45246  16/05/2025  SZ		Attributi Da Usare nel batch ricalcolaPrezzi 
 */

public class DocumentoVenRigaPrm
    extends DocumentoVenditaRiga implements RigaPrimaria,
    //RigaGestibileInCollaudo {//Fix 24613
    RigaGestibileInCollaudo,RigaConDettaglioConf {//Fix 24613

    //Fix 23345 inizio
    //ProvenienzaPrezzo
    public static final char PROV_PREZZO_LISTINO_GENERICO  = '1';
    public static final char PROV_PREZZO_LISTINO_CLIENTE   = '2';
    public static final char PROV_PREZZO_LISTINO_ZONA      = '3';
    public static final char PROV_PREZZO_LISTINO_CATEG_VEN = '4';
    //Fix 23345 fine
    public static final String RES_EVA_VEN = "it/thera/thip/vendite/documentoVE/resources/DocumentoVenditaRiga"; //fix 7220

    boolean iSalvaRigheSecondarie = true;

    //protected boolean iSalvaTestata = true;

    // ini FIX 1684
    protected boolean iIsCheckRigaDuplicata = Boolean.valueOf((String)
        ParametriDocumentiVen.get().get(ParametriDocumentiVen.
                                        ABILITA_CHECK_RIGHE_DUPLICATE)).
        booleanValue();

    // fine FIX 1684

    protected Integer iIdDettaglioRigaCollegata;
    protected Integer iIdRigaCollegata;

    //Fix 3197 - inizio
    protected boolean iServeRicalcoloProvvAgente = false;
    protected boolean iServeRicalcoloProvvSubagente = false;

    //Fix 3197 - fine

    private DocumentoVenRigaPrm rigaOmf;

    //Fix 3230 - inizio
    protected boolean iGeneraRigheSecondarie = true;

    //Fix 3230 - fine

    protected boolean iDisabilitaRigheSecondarieForCM = false; //...FIX04607 - DZ

    // Fix 8913 - Inizio
    public boolean isBOCompleted = false;
    // Fix 8913 - Inizio

    public boolean iAttivoControlloAccPrn = false; //Fix 9251

    // PAOLA
    // questa è la riga da proposta evasione che deve essere cancellata al salvataggio
    // di questa riga
    protected DocumentoVenditaRiga iRigaDaProposta;
    // fine PAOLA

    protected boolean iDisattivaPropostaAutoLotti = false;//Fix 32832
    
    protected boolean iForsaQtaRigheSec = false;//Fix 32851
    
    public DocumentoVenRigaPrm()
    {
        super();
        //Riferimento alla riga collegata: l'istanziazione avverrà nelle classi eredi
        this.iRigaCollegata = new Proxy(DocumentoVenRigaPrm.class);
        this.iRigheLotto = new OneToMany(DocumentoVenRigaLottoPrm.class, this, 15, true);
        this.iRigheContenitore = new OneToMany(ContenitoreVenRigaPrm.class, this,
                                               15, true);
        this.iRigaOrdine = new Proxy(it.thera.thip.vendite.ordineVE.
                                     OrdineVenditaRigaPrm.class);
        //Fix 2563 - inizio
        datiArticolo =
            (DatiArticoloRigaVendita)Factory.createObject(DatiArticoloRigaVendita.class);
        //Fix 2563 - fine
    }

    protected TableManager getTableManager() throws java.sql.SQLException
    {
        return DocumentoVenRigaPrmTM.getInstance();
    }

    //Attributi
    protected OneToMany iRigheSecondarie = new OneToMany(it.thera.thip.vendite.
        documentoVE.DocumentoVenRigaSec.class,
        this, 15, true);

    //Implementazione metodi astratti di PersistentObject
    //(getTableManager sarà definito nelle classi eredi)
    public String getKey()
    {
        String idAzienda = getIdAzienda();
        String annoDocumento = getAnnoDocumento();
        String numeroDocumento = getNumeroDocumento();
        Integer numeroRigaDocumento = getNumeroRigaDocumento();

        Object[] keyParts =
            {
            idAzienda,
            annoDocumento,
            numeroDocumento,
            numeroRigaDocumento,
        };

        return KeyHelper.buildObjectKey(keyParts);
    }

    public void setKey(String key)
    {

        String objIdAzienda = KeyHelper.getTokenObjectKey(key, 1);
        setIdAzienda(objIdAzienda);

        String objAnnoDocumento = KeyHelper.getTokenObjectKey(key, 2);
        setAnnoDocumento(objAnnoDocumento);

        String objNumeroDocumento = KeyHelper.getTokenObjectKey(key, 3);
        setNumeroDocumento(objNumeroDocumento);

        Integer objNumeroRigaDocumento =
            KeyHelper.stringToIntegerObj(
            KeyHelper.getTokenObjectKey(key, 4)
            );
        setNumeroRigaDocumento(objNumeroRigaDocumento);

    }

    /**
       //--------------------------------------------------------//

       //Metodi get/set attributi

       /**
      * Valorizza l'attributo AnnoDocumento.
      */
     public void setAnnoDocumento(String annoDocumento)
     {

         super.setAnnoDocumento(annoDocumento);
         iRigheSecondarie.setFatherKeyChanged();
     }

    //--------------

    /**
     * Valorizza l'attributo NumeroDocumento.
     */
    public void setNumeroDocumento(String numeroDocumento)
    {
        super.setNumeroDocumento(numeroDocumento);
        iRigheSecondarie.setFatherKeyChanged();
    }

    //--------------

    /**
     * Valorizza l'attributo NumeroRigaDocumento.
     */
    public void setNumeroRigaDocumento(Integer numeroRigaDocumento)
    {
        super.setNumeroRigaDocumento(numeroRigaDocumento);
        iRigheSecondarie.setFatherKeyChanged();
    }

    //--------------

    /**
     * Valorizza l'attributo DettaglioRigaDocumento.
     */
    /**
       public void setDettaglioRigaDocumento(Integer dettaglioRigaDocumento) {
      super.setDettaglioRigaDocumento(dettaglioRigaDocumento);
      iRigheSecondarie.setFatherKeyChanged();
       }
     */
    /**
     * Valorizza l'attributo DettaglioRigaDocumento.
     */
    public void setDettaglioRigaDocumento(Integer dettaglioRigaDocumento)
    {
        this.iDettaglioRigaDocumento = dettaglioRigaDocumento;
        setDirty();
    }


    //Fix 9251 inizio
    public boolean isAttivoControlloAccPrn()
    {
       return iAttivoControlloAccPrn;
    }

    public void setAttivoControlloAccPrn(boolean attivoControlloAccPrn)
    {
       this.iAttivoControlloAccPrn = attivoControlloAccPrn;
    }
    //Fix 9251 fine

    //--------------------------------------------------------//
    protected void setSalvaRigheSecondarie(boolean salvaRigheSecondarie)
    {
        iSalvaRigheSecondarie = salvaRigheSecondarie;
    }

    protected boolean isSalvaRigheSecondarie()
    {
        return iSalvaRigheSecondarie;
    }

    //Metodi per gestione OneToMany

    public List getRigheSecondarie()
    {
        return getRigheSecondarieInternal();
    }

    protected OneToMany getRigheSecondarieInternal()
    {
        if(iRigheSecondarie.isNew())
            iRigheSecondarie.retrieve();
        return iRigheSecondarie;
    }

    public boolean initializeOwnedObjects(boolean result)
    {
        boolean bo = super.initializeOwnedObjects(result);
        // Fix 3016
        if(this.getTestata() != null)
        {
            Cantiere can = ((DocumentoVendita)this.getTestata()).getCantiereTestata();
			//if(can != null && this.getTipoRiga() == TipoRiga.MERCE && //Fix 36654  
            if(can != null && this.getTipoRiga() == TipoRiga.MERCE && getArticolo() != null &&   //Fix 36654 
               getArticolo().getArticoloDatiVendita().getSchemaPrzVen() != null &&
               this.getArticolo().getArticoloDatiVendita().getSchemaPrzVen().
               getTipoSchemaPrz() == SchemaPrezzo.TIPO_SCH_ACQ_VEN)
            {
                this.setConCantiere(true);
            }
        }
        // Fine fix 3016
        return iRigheSecondarie.initialize(bo);
    }

    public int saveOwnedObjects(int rc) throws SQLException
    {
        int rc1 = super.saveOwnedObjects(rc);
        //return getRigheSecondarieInternal().save(rc1);
        return iRigheSecondarie.save(rc1);

        /*
            rc = super.saveOwnedObjects(rc);
            if (isSalvaRigheSecondarie()) {
              rc = iRigheSecondarie.save(rc);
            }
            return rc;
         */
    }

    public int deleteOwnedObjects() throws SQLException
    {
        int rc = super.deleteOwnedObjects();
        return getRigheSecondarieInternal().delete(rc);
    }

    //--------------------------------------------------------//

    /**
     * setEqual
     * @param obj
     * @throws CopyException
     */
    public void setEqual(Copyable obj) throws CopyException
    {
        super.setEqual(obj);
        DocumentoVenRigaPrm doc = (DocumentoVenRigaPrm)obj;
        iRigheSecondarie.setEqual(doc.iRigheSecondarie);
    }

    /**
     * setIdAziendaInternal
     * @param idAzienda
     */
    protected void setIdAziendaInternal(String idAzienda)
    {
        super.setIdAziendaInternal(idAzienda);
        iRigheSecondarie.setFatherKeyChanged();
    }

    protected int salvaRiga(DocumentoVendita testata, boolean newRow) throws
        SQLException
    {
        newRow = !isOnDB();

        /*
                    // FIX 6150 PM
                    Aggiunto controllo su isGeneraRigheSecondarie().
                    Questo controllo serve perchè è l'unico modo di sapere a livello di bo
                    se sono in copia di una riga.
                    Quando si copia una riga e si cambia la qta questa deve essere riportata
                    nelle righe secondarie.
            */

        if(!newRow || !isGeneraRigheSecondarie()) // FIX 6150 PM
        {
            Iterator righeSecondarie = this.getRigheSecondarie().iterator();
            while(righeSecondarie.hasNext())
            {
                DocumentoVenRigaSec rigaSec =
                    (DocumentoVenRigaSec)righeSecondarie.next();
                rigaSec.setSalvaRigaPrimaria(false);
                //Fix 3611 - inizio
                //Fix 3246 - inizio
                //if (this.isDirty()) {
                //Fix 5634 - inizio
                /*
                         Aggiunto controllo su isGeneraRigheSecondarie().
                         Questo controllo serve perchè è l'unico modo di sapere a livello di bo
                         se sono in copia di una riga (vedi relativo datacollector - fix 4800).
                         Quando si copia una riga e si cambia la qta questa deve essere riportata
                         nelle righe secondarie.
                         sulla riga copiata
                 */
                //if(isQuantitaCambiata() || !isGeneraRigheSecondarie())//Fix 32851
                //if( !isForsaQtaRigheSec() && (isQuantitaCambiata() || !isGeneraRigheSecondarie() || isVersioneCambiata()))//Fix 32851 //Fix 41393
                if( !isForsaQtaRigheSec() && (isQuantitaCambiata() || !isGeneraRigheSecondarie() || isVersioneCambiata()))//Fix 41393
                {
                    //Fix 5634 - fine
                    //Fix 3246 - fine
                    //rigaSec.ricalcoloQuantita(this.getServizioQta().getQuantitaInUMPrm()); //Fix 9671
                    rigaSec.ricalcoloQuantita(this);//Fix 9671
                    rigaSec.calcolaPesiEVolume(); //Fix 12508
                }
                //Fix 3611 - fine
                if(this.isRigaSaldata())
                {
                    rigaSec.setRigaSaldata(true);
                }

            }
        } //Fix 12508 inizio
        else if(newRow)
           calcolaPesiEVolumeRigheSec();

        calcolaPesiEVolume(newRow);
        aggiornaPesiEVolumeTestata(false);
        //Fix 12508 fine


        impostaRigaSaldata();

        int rc = super.salvaRiga(testata, newRow);
        /**
             if (isSalvaTestata())
            rc = salvaTestata(rc);
         */
        //Verifica se deve creare delle righe omaggio
        if(rc > 0 && newRow && getTipoRiga() == TipoRiga.MERCE && !isRigaOfferta() &&
           rigaOmf != null)
            rc = gestioneRigheOmaggio(testata, rc);
        else
        {
            if(isRigaOfferta())
                setRigaOfferta(false);
        }

        return rc;

    }

    /**
     * Ridefinizione del metodo recuperoDatiVenditaSave della classe
     * OrdineVenditaRiga
     */
    protected boolean recuperoDatiVenditaSave()
    {
        //Fix 4060 - inizio
        Articolo articolo = getArticolo();
        //Fix 4976 - inizio
        if(articolo != null)
        {
            //Fix 4976 - fine
            char tipoParte = articolo.getTipoParte();
            char tipoCalcoloPrezzo = articolo.getTipoCalcPrzKit();
            if((tipoParte == ArticoloDatiIdent.KIT_NON_GEST || tipoParte == ArticoloDatiIdent.KIT_GEST)
               &&
               tipoCalcoloPrezzo == ArticoloDatiVendita.DA_COMPONENTI)
            {
                return false;
            }
            //Fix 4060 - fine
            else
            {
                return
                    isServizioCalcDatiVendita() &&
                    // Attenzione isRigaOfferta posso utilizzarlo solo perchè questo metodo viene chiamato
                    // in nuovo della riga ossia nel momento in cui la riga offerta viene creata
                    !isRigaOfferta() &&
                    getTipoRiga() != TipoRiga.SPESE_MOV_VALORE;
            }
        }
        //Fix 4976 - inizio
        else
        {
            return false;
        }
        //Fix 4976 - fine
    }

    /**
     * Verifica se sussistono le condizioni per creare le righe di omaggio.
     * In caso affermativo le crea.
     *
     * @param testata Testata relativa alla riga
     */
    protected int gestioneRigheOmaggio(DocumentoVendita testata, int rc) throws
        SQLException
    {
        rigaOmf.setIdRigaCollegata(this.getNumeroRigaDocumento());
        rigaOmf.setIdDettaglioRigaCollegata(this.getDettaglioRigaDocumento());

        rigaOmf.setSalvaTestata(false);
        int rc1 = rigaOmf.save();

        if(rc1 >= 0)
            rc += rc1;
        else
            rc = rc1;

        return rc;
    }

    protected int eliminaRiga() throws SQLException
    {
        //Fix 16754 inizio
        if(getTipoRiga()==TipoRiga.OMAGGIO &&!getAttivaCheckCancellazione()){
           if(getIdRigaCollegata() != null && getIdDettaglioRigaCollegata() != null) {
             if (!retrieve()) return 0;
           }
        }
        //Fix 16754 fine

        Iterator i = this.getRigheSecondarie().iterator();
        while(i.hasNext())
        {
            DocumentoVenRigaSec rigaSec = (DocumentoVenRigaSec)i.next();
            rigaSec.setSalvaRigaPrimaria(false);
        }

//MG FIX 7949 inizio
        int rc = ErrorCodes.OK;
        if (rc >= ErrorCodes.OK && getTipoRiga() == TipoRigaDocumentoVendita.SERVIZIO_ASSIST_MANUT
            && ((DocumentoVendita)getTestata()).getStatoStampaFattura() != StatoAttivita.ESEGUITO) {
          rc = resetDatiVenditaDaDocSrv();
          if (rc < ErrorCodes.OK)
            return rc;
        }
//MG FIX 7949 fine

        aggiornaPesiEVolumeTestata(true); //Fix 12508

        rc = super.eliminaRiga();

        //Fix 15359 inizio
        // richiamo il salvataggio ordineSevizio in modo da ricalcolare il fido ordineServizio
        if (rc < ErrorCodes.OK)
            return rc;
        
        //...FIX 37203 inizio
        //...Il metodo chiama la Save del DC e quindi se va tutto bene restituisce 0 e non va bene mettere rc a 0
        //rc = aggiornaFidoOrdSrv();
        int rcFidoSrv = aggiornaFidoOrdSrv();
        //...Solo se la save dell'ods ha dato errori restituisco un'eccezione e metto rc a 0
        if(rcFidoSrv == BODataCollector.ERROR){
          rc = 0;
          Trace.excStream.println("ELIMINA RIGA DOC VEN " + KeyHelper.formatKeyString(getKey())+ " --> Errore in ricalcolo Fido su Ordine Servizio " + KeyHelper.formatKeyString(getOrdineServizioKey()));//Fix 37751
          throw new ThipException(new ErrorMessage("BAS0000078", "Errore in ricalcolo Fido su Ordine Servizio"));
        }
        
        //Fix 15359 fine

        /*Fix 4501 CM
             //Aggiunta per integrazione Mod. 6 - MC 26-9-05
             if (rc >= ErrorCodes.NO_ROWS_UPDATED &&
         testTrasmissioneDoc()) {  //testare se c'è il collegamento a logis
          TrasmissioneDocumentiLogis tml = (TrasmissioneDocumentiLogis)Factory.createObject(TrasmissioneDocumentiLogis.class);
          this.getTestata().getDatiComuniEstesi().setStato(DatiComuniEstesi.ANNULLATO);
          Vector result = tml.trasmetti(this);
          if (!result.isEmpty())
            return ErrorCodes.GENERIC_ERROR;
             }//Fine aggiunta integrazione Mod. 6 - MC 26-9-05
         */
        //fine fix 4501CM

        /**
             if (isSalvaTestata())
            rc = salvaTestata(rc);
         */
        // Inizio 11009
        if (!((DocumentoVendita)getTestata()).getGenIntercompany()){
        ModelloGenerazioneDocAcq modelloGen = ((DocumentoVendita)getTestata()).getModelloGenDocAcq();
        if (rc >0 && modelloGen != null && modelloGen.getStato() == DatiComuniEstesi.VALIDO && recuperaCausaleDocAcqRig(modelloGen) != null){
          GeneratoreDocumentoAcqRigIC gen = (GeneratoreDocumentoAcqRigIC)Factory.createObject(GeneratoreDocumentoAcqRigIC.class);
          gen.init(modelloGen, this);
          User utenteAziendaOrigine = null;
          try {
            utenteAziendaOrigine = gen.apriNuovaSessione();
            List errors = gen.deleteDocumentoAcquistoRiga();
            if (!errors.isEmpty()){
              rc = 0;
              throw new ThipException(errors);
            }
          }
          catch (Exception ex) {
            ex.printStackTrace(Trace.excStream);
          }
          finally {
            if (utenteAziendaOrigine != null)
              gen.chiudiNuovaSessione(utenteAziendaOrigine);
          }
        }
        }
        // Fine 11009
        //Fix 26492 Inizio
        int ret = 0;
        if (getOldRiga() != null && getOldRiga().getDatiComuniEstesi().getStato() == DatiComuniEstesi.VALIDO)
        {
        	ret = ((DocumentoVendita)getTestata()).updateFatturaVenditaTipoBene();
        	if (ret<0)
        		return ret;
        }
        //Fix 26492 Inizio
        return rc;
    }

//MG FIX 7949 INIZIO
    protected int resetDatiVenditaDaDocSrv() throws SQLException {
      int rc = ErrorCodes.OK;
      String whereCond = DocumentoServizioTM.ID_AZIENDA + " = '" + getIdAzienda() + "'" +
                         " AND " + DocumentoServizioTM.R_ANNO_DOCVEN + " = '" + getAnnoDocumento() + "'" +
                         " AND " + DocumentoServizioTM.R_NUMERO_DOCVEN + " = '" + getNumeroDocumento() + "'" +
                         " AND " + DocumentoServizioTM.R_RIGA_DOCVEN + " = " + getNumeroRigaDocumento();
      try {
        List docSrvList = DocumentoServizio.retrieveList(whereCond, "", true);
        if (docSrvList != null && !docSrvList.isEmpty()) {
          Iterator iter = docSrvList.iterator();
          while (iter.hasNext()) {
            DocumentoServizio docSrv = (DocumentoServizio) iter.next();
            if (docSrv != null) {
              docSrv.setIdAnnoDocVen(null);
              docSrv.setIdNumeroDocVen(null);
              docSrv.setIdRigaDocVen(null);
//MG FIX 10604 inizio
//              docSrv.setIdDetRigaDocVen(new Integer(0));
              docSrv.setIdDetRigaDocVen(null);
//MG FIX 10604 fine
              docSrv.setStatoAvzAddebito(DocumentoServizio.DA_RIESEGUIRE);
              int ret = docSrv.save();
              if (ret < ErrorCodes.OK)
                rc = -1;
            }
          }
        }
      } catch(SQLException e) {
        rc = -1;
        throw e;
      } catch (Exception e){
        e.printStackTrace();
        rc = -1;
      }
      finally {
        return rc;
      }
    }
//MG FIX 7949 FINE

    //Fix 15359 inizio
	protected int aggiornaFidoOrdSrv() throws SQLException {
		int rc = ErrorCodes.OK;
		if (this.getCausaleRiga().getTipoRiga() == TipoRigaDocumentoVendita.SERVIZIO_ASSIST_MANUT) {
			try {
				OrdineServizio ordSrv = this.getOrdineServizio();
				if (ordSrv != null) {
					ordSrv.setFidoDaRicalcolare(true);
					rc = ordSrv.salvaOrdSrv();
				}
			} catch (SQLException e) {
				rc = -1;
				throw e;
			} catch (Exception e) {
				e.printStackTrace(Trace.excStream);
				rc = -1;
			}
		}
		return rc;
	}
    //Fix 15359 fine

    /**
     * Ridefinizione del metodo eliminaRigaOmaggioCollegata della classe
     * OrdineVenditaRiga
     */
    protected int eliminaRigaOmaggioCollegata(String key) throws SQLException
    {
        int rc = 0;

        DocumentoVenRigaPrm rigaOmf = (DocumentoVenRigaPrm)Factory.createObject(
            DocumentoVenRigaPrm.class);
        rigaOmf.setKey(key);
        if(rigaOmf.retrieve())
        {
            rigaOmf.setSalvaTestata(false); //Senza questa riga dà OPTIMISTIC LOCK FALLITO
            rc = rigaOmf.delete();
        }

        return rc;
    }

    //Fix 26492 Inizio
    public boolean isDaAggiornareFattVenTpBene(boolean isNewRow)
    {
  	  boolean updateFattVenTpBene = false;
  	  DocumentoVendita testata = (DocumentoVendita) getTestata();
  	  char V = DatiComuniEstesi.VALIDO ;
  	  if (testata != null && PersDatiVen.getCurrentPersDatiVen().isGestBeniDeteriorabili() )
  	  {
  	  	updateFattVenTpBene = isNewRow;
  	  	if(!updateFattVenTpBene)
  	  	{
  	    	//Fix 28747 Inizio
  	    	if(getOldRiga()==null){ // In caso di salvatagio documento da addebito servizio, la riga è già salvata ma il oLdRiga è NULL
  	    		updateFattVenTpBene = false;
  	    	}
  	    	else
  	    	{
  	    	//Fix 28747 Fine
	  	  		char oldStato = getOldRiga().getDatiComuniEstesi().getStato();
	  	  		char currentStato = getDatiComuniEstesi().getStato();
	  	  		updateFattVenTpBene = (oldStato== V && currentStato != V)  || (oldStato!= V && currentStato == V);
  	    	}//Fix 28747    
  	  	}
  	  }
  	  return updateFattVenTpBene;
    }
  //Fix 26492 Fine
    /**
     * Ridefinizione del metodo save()
     */
    public int save() throws SQLException
    {
	//Fix 45246 inizio
	  if(isSalvatagioRigaDaIngorare())
		return 0;
	//Fix 45246 fine  
        gestioneOriginePreferenziale();//Fix 33992
      boolean updateFattVenTpBene = isDaAggiornareFattVenTpBene(!isOnDB());//Fix 26492
      //Fix 18753 inizio
      if(isOnDB() && getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO)
         verificaConfRigheSec();
      //Fix 18753 fine

    	//Fix 11529 - inizio
      char tipoParte = getArticolo().getTipoParte();
      if (tipoParte == ArticoloDatiIdent.KIT_GEST || tipoParte == ArticoloDatiIdent.KIT_NON_GEST) {
      	propagaCommessaSuRigheSec(getRigheSecondarie());
      }
      //Fix 11529 - fine

    	// PAOLA
      SQLException ex = null;
      boolean hoFattoTutto =false;
      int rc =0;
      try {
        // fine PAOLA
//MG FIX 8495 inizio : per righe omaggio sconto articolo con es.art15, l'assoggettamento
// IVA della riga deve essere forzato a quello assogg.Es.Art.15 definito sulla causale
      if (!isOnDB() && getTipoRiga() == TipoRigaDocumentoVendita.OMAGGIO) {
        CausaleRigaDocVen cau = this.getCausaleRiga();
        if (cau.getTpOmaggioScontoArticolo() == ScontoArticolo.SC_ART_ES_ART15 &&
            cau.getIdAssoggIvaEsArt15() != null)
          this.setIdAssogIVA(cau.getIdAssoggIvaEsArt15());
      }
//MG FIX 8495 fine

        //Fix 3197 - inizio
        //Fix 3738 (aggiunto controllo su pdv) - inizio
        PersDatiVen pdv = PersDatiVen.getCurrentPersDatiVen();
        if(pdv.getGestionePvgSuScalaSconti() &&
           (isServeRicalProvvAg() || isServeRicalProvvSubag()))
        {
            modificaProvv2Agente();
        }
        //Fix 3738 - fine
        //Fix 3197 - fine

        //Fix 6439 - inizio
        char tipoRiga = getTipoRiga();

        //Fix 16242-19920 - inizio
//        boolean noleggiServizi = tipoRiga == TipoRigaDocumentoVendita.SERVIZIO_NOLEGGIO ||
//                                 (tipoRiga == TipoRigaDocumentoVendita.SERVIZIO_ASSIST_MANUT &&
//                                  getTestata() instanceof NuovoDocumentoSpedRientro); //Fix 10882 //Fix 11931
        boolean noleggiServizi = isRigaNoleggiServizi();
        //Fix 16242-19920 - fine

      if (!isOnDB() && noleggiServizi &&
          isGeneraRigheSecondarie() &&       // Fix 11789
          !isDisabilitaRigheSecondarieForCM()
          ) {

          generaRigheSecDaMaterialiOrdSrv(getTipoRigaPerNoleggiServizi());
          //Fix 8640 - inizio
          generaRigheSecDaRisorseOrdSrv(getTipoRigaPerNoleggiServizi());
          //Fix 8640 - fine
          //Fix 12790 inizio
          for (Iterator iter = iRigheSecondarie.iterator(); iter.hasNext(); ) {
            DocumentoVenRigaSec item = (DocumentoVenRigaSec) iter.next();
            item.setAbilitaCopiaCommenti(false);
          }
          //Fix 12790 fine
        }
        //Fix 6439 - fine

        //Verifica se deve generare le righe secondarie di kit
        if(!isOnDB() &&
           !noleggiServizi && 	//Fix 6439
           getTipoRiga() != TipoRiga.SPESE_MOV_VALORE &&
           (getArticolo().getTipoParte() == ArticoloDatiIdent.KIT_GEST
            ||
            getArticolo().getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST
            )
           )
        {
            //System.err.println("==============>>devi richiamare gestioneKit");

            //Fix 3230 - inizio
            if(isGeneraRigheSecondarie() &&
               !isDisabilitaRigheSecondarieForCM() &&
               !noleggiServizi	//Fix 6439
               )
            { //...FIX04607 - DZ
                gestioneKit();
                verificaConfRigheSec();//Fix 18753
                //Fix 4060 - inizio
                calcolaPrezzoDaRigheSecondarie();
                //Fix 4060 - fine
            }
            //Fix 3230 - fine
        }
        //20/02/2003 inizio
        else if(isOnDB())
        {
            //Inizio 3368
            if(getOldRiga() != null &&
               getOldRiga().getStatoAvanzamento() == StatoAvanzamento.PROVVISORIO &&
               getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO)
            {
                rendiDefinitivoRiga();
            }
            //Fine 3368
           //gestioneDateRigheSecondarie();//Fix 33762
        }
        //20/02/2003 fine
		gestioneDateRigheSecondarie();//Fix 33762 

        //fix 5501 - inizio
        boolean passaALogis = testTrasmissioneDoc();
        setStatoTrasmissioneALogis(passaALogis);
        if (passaALogis)                                                                                      //fix 12258
           ((DocumentoVendita)this.getTestata()).setTipoCollegamentoLogistica(TipoCollegamentoLogistica.LOGIS);//fix 12258
        boolean testLogisRigheSec[] = new boolean[getRigheSecondarie().size()];
        int index = 0;
        if(passaALogis)
        {
            Iterator i = getRigheSecondarie().iterator();
            //memorizzo per ciascuna riga sec. se deve essere trasmessa a Logis
            while(i.hasNext())
            {
                DocumentoVenditaRiga rigaSec = (DocumentoVenditaRiga)i.next();
                boolean test = rigaSec.testTrasmissioneDoc();
                testLogisRigheSec[index++] = test;
                rigaSec.setStatoTrasmissioneALogis(test);
                rigaSec.setTrasmissioneALogisDaEffettuare(false);
            }
        }
        //Fix  11084 PM Inizio
        else
        	setRigaTrasmittibilePPL(testTrasmissionePPL());
        //Fix  11084 PM Fine
        //fix 5501 - fine

        // fix 12258
        boolean nonDeveTrasmettereALogis =  ((DocumentoVendita)this.getTestata()).isNonDeveTrasmettereALogis();//45131
        // serve per impedire che, la save() della testata del documento, venga trasmessa a Logis (è inutile trasmetterla)
        ((DocumentoVendita)this.getTestata()).setNonDeveTrasmettereALogis(true);
		impostaStatoAvanzamentoSecondarie(); //Fix 44409
        // Fix 9061 - Inizio
        if (!isOnDB() && isBOCompleted){ // se è una nuova riga inserita in modalità batch
            CompletaDatiCA();
        }
        // PAOLA
        if (rc>=0){
          int riP = 0;
          if (this.getRigaDaProposta() != null) {
            riP = this.getRigaDaProposta().delete();
          }
          if (riP >= 0) {
            rc = rc + riP;
          }
          else {
            rc = riP;
          }
        }
        // fine PAOLA
        // Fix 9061 - Fine
        // PAOLA

        // fix 11924 >
      	if (this.isAbilitaCambioConfigurazioneRigaOrdine() &&
      			this.getRigaOrdine().getIdConfigurazione().compareTo(this.getIdConfigurazione()) != 0) {
      		this.getRigaOrdine().setIdConfigurazione(this.getIdConfigurazione());
      	}
      	// fix 11924 <

        if (rc>=0){
          //int rc = super.save();
          int rc1 = super.save();
          if (rc1<0){
            rc = rc1;
          }
          else {
            rc = rc + rc1;
          }
        }
        // fine PAOLA

        // fix 12258
        // senza la set seguente l'evasione ordini trasmetterebbe solo la prima riga perchè in precedenza
        // l'attributo è stato messo a true per evitare di trasmettere inutilmente la testata nella save() della riga doc
        //((DocumentoVendita)this.getTestata()).setNonDeveTrasmettereALogis(false);//45131
        ((DocumentoVendita)this.getTestata()).setNonDeveTrasmettereALogis(nonDeveTrasmettereALogis); // 45131
        /* Fix 4501 CM 19-10-05
            //Fix 4228 PM Inizio -- 08/07/05 AR  ADRIO
             if (rc >= 0 &&
            testTrasmissioneDoc()) {
          TrasmissioneDocumentiLogis tml = (TrasmissioneDocumentiLogis)Factory.createObject(TrasmissioneDocumentiLogis.class);
          Vector result = tml.trasmetti(this);
          if (!result.isEmpty())
            return ErrorCodes.GENERIC_ERROR;
             }
             //Fix 4228 PM Fine --  AR 08/07/05 fine
         */

        //fix 5501 - inizio
        if(passaALogis && rc >= 0)//fix 06332 - aggiunto rc
        {
            trasmettiALogis(); //trasmissione riga primaria
            Iterator i = getRigheSecondarie().iterator();
            index = 0;
            while(i.hasNext())
            {
                DocumentoVenditaRiga rigaSec = (DocumentoVenditaRiga)i.next();
                if(testLogisRigheSec[index])
                    rigaSec.trasmettiALogis(); //trasmissione riga secondaria
                index++;
            }
        }
        //fix 5501 - fine
        // Fix 8913 - Inizio: Rimosso con fix 9061
        /*
        if (!isOnDB() && isBOCompleted){ // se è una nuova riga inserita in modalità batch
            CompletaDatiCA();
            rc = super.save();
        }*/
        // Fix 8913 - Fine
        // PAOLA
        hoFattoTutto = true;
        // fine PAOLA
      // PAOLA
    }
    catch (SQLException t) {
      if (  (!(t instanceof ThipException)) || ((ThipException)t).getErrorMessage() == null ||  !((ThipException)t).getErrorMessage().getId().equals("THIP_TN560"))//Fix 30871	
      t.printStackTrace(Trace.excStream);
      ex = t;
    }
    finally {
      boolean isAbilitaCommitRiga = ( (DocumentoVendita)this.getTestata()).
          isAbilitaCommitRigaDaProposta();
      // il controllo che segue mi serve perchè se sto salvndo una riga DcoEVaVenRigaPrm
      // il commit o il rollback lo farò al momento del suo save.
      if (isAbilitaCommitRiga){
        isAbilitaCommitRiga = puoEssereCommittata();
      }
      boolean rilancioLeccezione = true;
      if (isAbilitaCommitRiga){
        if (rc >= 0 && ex ==null && hoFattoTutto) {
          DocumentoVendita docTes = (DocumentoVendita)this.getTestata();
          ConnectionManager.commit();
          docTes.setAlmenoUnaRigaCommittata(true);
        }
        else {
          aggiornaLaLista(rc, ex);
          rilancioLeccezione = false;
          rc =0;
          ConnectionManager.rollback();
        }
      }
      if (ex!=null && rilancioLeccezione){
        throw ex;
      }
    }
    // fine PAOLA

      // Inizio 11009
      if (rc >0 && isAttivaGestioneIntercompany() ) {
        GeneratoreDocumentoAcqRigIC gen = (GeneratoreDocumentoAcqRigIC) Factory.createObject(GeneratoreDocumentoAcqRigIC.class);
        DocumentoVendita testata = (DocumentoVendita)getTestata();
        gen.init(testata.getModelloGenDocAcq(), this);
        List errors = gen.generaDocumentoAcquistoRigaIC();
        //Fix 13911 PM >
        //if (!errors.isEmpty())
        if (gen.esistonoErroriBloccanti(errors))
          throw new ThipException(errors);
        else
      	  getWarningList().addAll(errors);
        //Fix 13911 PM <
      }
      // Fine 11009

      //Fix 26492 Inizio
      int ret = 0;
      if (rc>0 && updateFattVenTpBene)
      {
    	DocumentoVendita testata = (DocumentoVendita)getTestata();
      	ret = testata.updateFatturaVenditaTipoBene();
      	if (ret<0 )
      		return ret;
      }
     //Fix 26492 Fine
      
      //...FIX 26488
      try {
        if (rc >0 && GestioneConaiHelper.getInstance().getPersDatiConai() != null)
          GestioneConaiHelper.getInstance().aggiungiRigheDocVenGestioneConai(this);
      }
      catch(Exception e) {
         Trace.excStream.println("#### ECCEZIONE IN GESTIONE CONAI RIGA DOC VEN ####");
         e.printStackTrace(Trace.excStream);
       } 

      return rc;
    }

//Fix 4923 PM Inizio
    //Fix 4228 PM Inizio -- 08/07/05 AR  ADRIO
    /**
     * Test di abilitazione alla trasmissione del documento a PantheraLogistica.
     */
    /*protected boolean testTrasmissioneDoc()
         {
      if (getTipoRiga() == TipoRiga.SPESE_MOV_VALORE)
        return false;
      if (getTipoRiga() == TipoRiga.SERVIZIO)
        return false;
      if (getStatoAvanzamento() != StatoAvanzamento.PROVVISORIO)
        return false;
      if (PersDatiMagazzino.getCurrentPersDatiMagazzino() == null ||
          !PersDatiMagazzino.getCurrentPersDatiMagazzino().getGesUbicazioni())
        return false;
      CfgLogTxDocVen cfgDocVen = null;
      try {
        cfgDocVen = CfgLogTxDocVen.elementWithKey(KeyHelper.buildObjectKey(new Object[] {getIdAzienda(), getTestata().getIdCau(),getTestata().getIdMagazzino()}),PersistentObject.NO_LOCK);
      } catch (Exception ex) {
        ex.printStackTrace(Trace.excStream);
      }
      if (cfgDocVen != null &&
          getArticolo() != null &&
          CfgLogTxArtic.compatibile(getArticolo()))
        return true;
      return false;
         }*/

    //Fix 4228 PM Fine -- 08/07/05 AR  ADRIO
//Fix 4923 PM Inizio

    /**
     protected int salvaTestata(int rc) throws SQLException
     {
         if (rc >= 0)
         {
             DocumentoVendita testata = (DocumentoVendita)getTestata();
             testata.setSalvaRighe(false);
             int rc1 = testata.save();
             testata.setSalvaRighe(true);
             rc =  rc1 >= 0 ? rc + rc1 : rc1;
         }
         return rc;
     }
     */


    /**
       public boolean isSalvaTestata()
       {
          return iSalvaTestata;
       }

       public void setSalvaTestata(boolean salvaTestata)
       {
          iSalvaTestata = salvaTestata;
       }
     */
    /**
     * Restituisce un'istanza della classe EsplosioneNodo
     *
     * @param articolo Articolo In oggetto
     *
     * @return Un'istanza della classe EsplosioneNodo
     */
    protected EsplosioneNodo getEsplosioneNodo(Articolo articolo) throws
        SQLException
    {
        Esplosione esplosione = new Esplosione();
        esplosione.setTipoEsplosione(Esplosione.PRODUZIONE);
        esplosione.setIdArticolo(articolo.getIdArticolo());
        esplosione.setTrovaTestataEsatta(false); //Fix 1136
        esplosione.getProprietario().setTipoProprietario(ProprietarioDistinta.
            CLIENTE);
        esplosione.getProprietario().setCliente(((DocumentoVendita)getTestata()).
                                                getCliente()); //Fix 1136
        esplosione.setTipoDistinta(DistintaTestata.NORMALE);
        esplosione.setLivelloMassimo(new Integer(1));
        esplosione.setData(getDataConsegnaConfermata());
//    esplosione.setQuantita(getQtaInUMPrm());  //...FIX04669 - DZ
        esplosione.setQuantita(this.getServizioQta().getQuantitaInUMPrm()); //PTF02151 //...FIX04669 - DZ
        //fix 4453 inizio
        if(getIdConfigurazione() != null)
            esplosione.setIdConfigurazione(getIdConfigurazione());
            //fix 4453 fine

//    esplosione.setIdConfigurazione(getIdConfigurazione());  //serve ????
//    esplosione.setGesConfigTmp(Esplosione.CREATE_MA_NON_MEMORIZZATE);

        esplosione.run();
        return esplosione.getNodoRadice();
    }

    /**
     * Stabilisce il tipo di righe kit da generare
     * FIX03954 - DZ/GN
     */
    protected void gestioneKit() throws SQLException
    {
    	if(PersDatiGen.isGestitioIntellimag()) {//40452
    		if(isFromEvasionePrebolla()) {
    			return;
    		}
    	}//40452
        Articolo articolo = getArticolo();

        EspNodoArticolo esplosione = null;
        boolean okModello = false;
        try
        {
            esplosione = getEsplosioneNodoModello(false, articolo, ModelloProduttivo.KIT);
            okModello = true;
        }
        catch(ThipException ex)
        {
            okModello = false;
        }

        if(!okModello)
        {
            try
            {
                esplosione = getEsplosioneNodoModello(false, articolo, ModelloProduttivo.PRODUZIONE);
                okModello = true;
            }
            catch(ThipException ex)
            {
                okModello = false;
            }
        }

        if(okModello)
            generaRigheSecondarieEsplosioneModello(false, esplosione);
        else
            generaRigheKit(getEsplosioneNodo(articolo));
    }
    /**
     * Ridefinizione
     */
    public DocumentoOrdineRiga creaRigaSecondaria()
    {
        DocumentoVenRigaSec rigaSec =
            (DocumentoVenRigaSec)Factory.createObject(DocumentoVenRigaSec.class);
        return rigaSec;

    }

    /**
     * Ridefinizione
     */
    //Fix 3700: introdotta nuova interfaccia EspNodoArticoloBase
    //Fix 17639 inizio
//    protected DocumentoOrdineRiga generaRigaSecondariaModello(EspNodoArticoloBase
//        datiRigaSec, int sequenza) throws SQLException
//    {
    public DocumentoOrdineRiga generaRigaSecondariaModello(EspNodoArticoloBase
        datiRigaSec, int sequenza) throws SQLException
    {
    //Fix 17639 fine
        DocumentoVenRigaSec rigaSec = (DocumentoVenRigaSec)super.generaRigaSecondariaModello(datiRigaSec,
            sequenza);
        CausaleRigaDocVen causaleRigaPrm = getCausaleRiga();
        rigaSec.setCausaleRiga(causaleRigaPrm);

        if(rigaSec.getMagazzino() == null)
        {
            rigaSec.setMagazzino(getMagazzino());
        }
		if(datiRigaSec instanceof EspNodoArticolo){  // Fix 24493
        // Fix 24190 inizio
        AttivitaProdMateriale atvMat = ((EspNodoArticolo) datiRigaSec).getAttivitaProdMateriale();
        if (atvMat != null) {
          if (causaleRigaPrm != null && causaleRigaPrm.isKitRecuperaMagDaMod() && atvMat.getMagazzinoPrelievo() != null)
    	    rigaSec.setMagazzino(atvMat.getMagazzinoPrelievo());
        }
    	// Fix 24190 fine
     	}
        Articolo articoloSec = datiRigaSec.getArticoloUsato().getArticolo();

        //fix 4453 inizio
        UnitaMisura umRif = articoloSec.getUMDefaultVendita();
        //UnitaMisura umRif = articoloSec.getUMDefaultAcquisto();
        //fix 4453 fine
        UnitaMisura umPrm = articoloSec.getUMPrmMag();
        UnitaMisura umSec = articoloSec.getUMSecMag();

        //Fix 3659 - inizio
        BigDecimal qc = datiRigaSec.getQuantitaCalcolata(); //Fix 3700
        //BigDecimal qtaCalcolata = qc.setScale(2, BigDecimal.ROUND_HALF_UP); //Fix 30871
		BigDecimal qtaCalcolata = Q6Calc.get().setScale(qc,2, BigDecimal.ROUND_HALF_UP); //Fix 30871
        // fix 11123
        /*
        //Fix 3659 - fine
        BigDecimal qtaRiferimento = articoloSec.convertiUM(qtaCalcolata, umPrm, umRif, rigaSec.getArticoloVersRichiesta()); // fix 10955
        BigDecimal qtaSecondaria = (umSec == null) ? new BigDecimal(0.0) :
            articoloSec.convertiUM(qtaRiferimento, umRif, umSec, rigaSec.getArticoloVersRichiesta()); // fix 10955
        Trace.println("\tqtaCalcolata=" + qtaCalcolata);
        Trace.println("\tqtaRiferimento=" + qtaRiferimento);
        Trace.println("\tqtaSecondaria=" + qtaSecondaria);

        // Inizio 4670
        if(UnitaMisura.isPresentUMQtaIntera(umRif, umPrm, umSec, articoloSec))
        { //Fix 5117
            // Inizio 4805
            //QuantitaInUMRif qta = getArticolo().calcolaQuantitaArrotondate(qtaCalcolata, umRif, umPrm, umSec, Articolo.UM_PRM);
            QuantitaInUMRif qta = articoloSec.calcolaQuantitaArrotondate(qtaCalcolata, umRif, umPrm, umSec, rigaSec.getArticoloVersRichiesta(), Articolo.UM_PRM); // 10955
            // Fine 4805

            qtaRiferimento = qta.getQuantitaInUMRif();
            qtaCalcolata = qta.getQuantitaInUMPrm();
            qtaSecondaria = qta.getQuantitaInUMSec();
        }
        // Fine 4670
        */
        QuantitaInUMRif qta = calcolaSoloQuantitaRigaSec(articoloSec, qtaCalcolata, rigaSec.getArticoloVersRichiesta(), umRif);
        BigDecimal qtaRiferimento = qta.getQuantitaInUMRif();
        qtaCalcolata = qta.getQuantitaInUMPrm();
        BigDecimal qtaSecondaria = qta.getQuantitaInUMSec();
        // fine fix 11123

        //fix 4453 inizio
        rigaSec.setCoefficienteImpiego(datiRigaSec.getCoeffImpiego());
        if(datiRigaSec.getCoeffTotale())
        {
            rigaSec.setBloccoRicalcoloQtaComp(true);
            rigaSec.setCoefficienteImpiego(new BigDecimal("0"));
        }
        /*
             rigaSec.setCoefficienteImpiego(qtaCalcolata.divide(getQtaInUMPrmMag(),
             BigDecimal.ROUND_HALF_UP));
         */
        //fix 4453 fine
        rigaSec.setQtaInUMVen(qtaRiferimento);
        rigaSec.setUMRif(umRif);
        rigaSec.setQtaInUMPrm(qtaCalcolata);
        rigaSec.setUMPrm(umPrm);
        rigaSec.setQtaInUMSec(qtaSecondaria);
        //Inizio Fix 3362
        rigaSec.setUMSec(umSec);
        //Fine Fix 3362

        aggiornaNumeroImballo(rigaSec); // fix 12572

        rigaSec.setDataConsegnaRichiesta(getDataConsegnaRichiesta());
        rigaSec.setSettConsegnaRichiesta(getSettConsegnaRichiesta());
        rigaSec.setDataConsegnaConfermata(getDataConsegnaConfermata());
        rigaSec.setSettConsegnaConfermata(getSettConsegnaConfermata());
        rigaSec.setIdResponVendite(getIdResponVendite());
        //Fix 3212
        rigaSec.setListinoPrezzi(getListinoPrezzi());
        //Fine Fix 3212
        //Fix 4191 - inizio
        rigaSec.sistemoLeQuantita();
        rigaSec.setServizioCalcDatiVendita(false);
        rigaSec.setRigaPrimaria(this);
        rigaSec.calcolaDatiVendita((DocumentoVendita)rigaSec.getTestata());
		rigaSec.setDescrizioneExtArticolo(recuperaDescExtArticolo(rigaSec.getCodiceCliente(), rigaSec.getIdArticolo(), rigaSec.getIdConfigurazione()));//Fix14727 RA

//MG FIX 8659 inizio
    rigaSec.setIdCommessa(getIdCommessa());
    rigaSec.setIdCentroCosto(getIdCentroCosto());
    recuperaDatiCA(rigaSec);
//MG FIX 8659 fine

        //Fix 4191 - fine
        rigaSec.setSalvaRigaPrimaria(false);
        //Fix 33905 Inizio
        DocumentoVenRigaSec rigaSecTmp = (DocumentoVenRigaSec)Factory.createObject(DocumentoVenRigaSec.class);
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
        if (rigaSec.getCausaleRiga() != null) {		//Fix 35337
        	rigaSec.setUtilizzaContoAnticipi(rigaSec.getCausaleRiga().getUtilizzaContoAnticipi());//Fix 34503
        }

        return rigaSec;
    }

    /**
     * Genera righe kit quando il kit è gestito a magazzino
     *
     * @param nodo Classe che contiene tutti i dati delle eventuali righe di kit
     */
    protected void generaRigheKit(EsplosioneNodo nodo) throws SQLException
    {
        //Fix 4060 - inizio
        /*
         boolean calcoloDatiVendita = false;
             int tipoParte = getArticolo().getTipoParte();
             switch (tipoParte) {
          case ArticoloDatiIdent.KIT_GEST:
            calcoloDatiVendita = false;
            break;
          case ArticoloDatiIdent.KIT_NON_GEST:
            calcoloDatiVendita = true;
            break;
             }
         */
        boolean calcoloDatiVendita = false;
        //Fix 4060 - fine
        //System.err.println("==============>>calcoloDatiVendita="+calcoloDatiVendita);

        List datiRigheKit = nodo.getNodiFigli();
        //System.err.println("==============>>datiRigheKit="+datiRigheKit.size());
        Iterator iter = datiRigheKit.iterator();
        int sequenza = 0;
        while(iter.hasNext())
        {
            EsplosioneNodo datiRigaKit = (EsplosioneNodo)iter.next();
            //System.err.println("==============>>iterazione="+datiRigaKit);
            //System.err.println("\tversione="+datiRigaKit.getIdVersione());

            //Istanza della riga secondaria
            DocumentoVenRigaSec rigaKit =
                (DocumentoVenRigaSec)Factory.createObject(DocumentoVenRigaSec.class);

            // Inserisco la sequenza
            rigaKit.setSequenzaRiga(sequenza++);

            //Dati principali
            rigaKit.setServizioCalcDatiVendita(calcoloDatiVendita);

            Articolo articoloKit = datiRigaKit.getArticolo();
            //System.err.println("\tversione primaria="+getIdVersioneSal());

            UnitaMisura umVen = articoloKit.getUMDefaultVendita();
            UnitaMisura umPrm = articoloKit.getUMPrmMag();
            UnitaMisura umSec = articoloKit.getUMSecMag();

            //Fix 3659 - inizio
            BigDecimal qc = datiRigaKit.getQuantitaCalcolata();
            //BigDecimal qtaCalcolata = qc.setScale(2, BigDecimal.ROUND_HALF_UP);//Fix 30871
            BigDecimal qtaCalcolata = Q6Calc.get().setScale(qc,2, BigDecimal.ROUND_HALF_UP);//Fix 30871
			//Fix 3659 - fine
            BigDecimal qtaVendita =
                articoloKit.convertiUM(qtaCalcolata, umPrm, umVen, rigaKit.getArticoloVersRichiesta()); // fix 10955

            BigDecimal qtaSecondaria =
                (umSec == null) ?
                new BigDecimal(0.0) :
                articoloKit.convertiUM(qtaVendita, umVen, umSec, rigaKit.getArticoloVersRichiesta()); // fix 10955

            // Inizio 8977
            // Inizio 4670
            if(UnitaMisura.isPresentUMQtaIntera(umVen, umPrm, umSec, articoloKit))
            { //Fix 5117
                QuantitaInUMRif qta = articoloKit.calcolaQuantitaArrotondate(qtaCalcolata, umVen, umPrm, umSec, rigaKit.getArticoloVersRichiesta(), Articolo.UM_PRM); // 10955
                qtaVendita = qta.getQuantitaInUMRif();
                qtaCalcolata = qta.getQuantitaInUMPrm();
                qtaSecondaria = qta.getQuantitaInUMSec();
            }
            // Fine 4670
            // Fine 8977

            //Campi not nullable
            rigaKit.setTipoRiga(getTipoRiga());
            rigaKit.setStatoAvanzamento(getStatoAvanzamento());

            //fix 4453 inizio
            rigaKit.setCoefficienteImpiego(datiRigaKit.getCoeffImpiego());
            if(datiRigaKit.getCoeffTotale())
            {
                rigaKit.setBloccoRicalcoloQtaComp(true);
                rigaKit.setCoefficienteImpiego(new BigDecimal("0"));
            }
            /*
                   rigaKit.setCoefficienteImpiego(
                qtaCalcolata.divide(getQtaInUMPrm(), BigDecimal.ROUND_HALF_UP)
                );
             */
            //fix 4453 fine

            //Scheda Generale
            rigaKit.setCausaleRiga(getCausaleRiga());
            rigaKit.setMagazzino(getMagazzino());
			// Fix 24190 inizio
            DistintaLegame distinta = datiRigaKit.getDistintaLegame();
            if (distinta != null) {
              if (getCausaleRiga() != null && getCausaleRiga().isKitRecuperaMagDaMod() && distinta.getMagazzino() != null)
        	rigaKit.setMagazzino(distinta.getMagazzino());
            }
            // Fix 24190 fine
            rigaKit.setCommessa(getCommessa());
            rigaKit.setDocumentoMM(getDocumentoMM());

            // Fix 14225 inizio
            if (datiRigaKit.getDistintaLegame().getIdDocumentoMM() != null)
              rigaKit.setIdDocumentoMM(datiRigaKit.getDistintaLegame().getIdDocumentoMM());
            rigaKit.setNota(datiRigaKit.getDistintaLegame().getNote());
            // Fix 14225 fine

            rigaKit.setArticolo(articoloKit);
            rigaKit.setDescrizioneArticolo(
                articoloKit.getDescrizioneArticoloNLS().getDescrizione()
                );
            Integer idVersioneKit = datiRigaKit.getIdVersione();
            if(idVersioneKit != null)
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
                if(versioneKit.retrieve())
                {
                    ArticoloVersione versioneSaldiKit = versioneKit.getVersioneSaldi();
                    if(versioneSaldiKit == null)
                    {
                        rigaKit.setIdVersioneSal(idVersioneKit);
                    }
                    else
                    {
                        rigaKit.setIdVersioneSal(versioneSaldiKit.getIdVersione());
                    }
                }
            }
            rigaKit.setConfigurazione(datiRigaKit.getConfigurazione()); //?????
            rigaKit.setQtaInUMVen(qtaVendita);
            rigaKit.setUMRif(umVen);
            rigaKit.setQtaInUMPrm(qtaCalcolata);
            rigaKit.setUMPrm(umPrm);
            rigaKit.setQtaInUMSec(qtaSecondaria);
            rigaKit.setUMSec(umSec);
            rigaKit.setDataConsegnaRichiesta(getDataConsegnaRichiesta());
            rigaKit.setSettConsegnaRichiesta(getSettConsegnaRichiesta());
            rigaKit.setDataConsegnaConfermata(getDataConsegnaConfermata());
            rigaKit.setSettConsegnaConfermata(getSettConsegnaConfermata());
            rigaKit.setIdResponVendite(getIdResponVendite()); //Fix 1205

            /**
                   rigaKit.setDataBolla(getDataBolla());
                   rigaKit.setDataFattura(getDataFattura());
                   rigaKit.setNumeroBolla(getNumeroBolla());
                   rigaKit.setNumeroFattura(getNumeroFattura());
             */

            //Scheda Prezzi/Sconti
            rigaKit.setListinoPrezzi(getListinoPrezzi());
            //Fix 4060 - inizio
            rigaKit.sistemoLeQuantita();
            rigaKit.setRigaPrimaria(this);
            rigaKit.calcolaDatiVendita((DocumentoVendita)rigaKit.getTestata());
            //Fix 4060 - fine
			rigaKit.setDescrizioneExtArticolo(recuperaDescExtArticolo(rigaKit.getCodiceCliente(), rigaKit.getIdArticolo(), rigaKit.getIdConfigurazione()));//Fix14727 RA
            //AssoggettamentoIVA assIva = articoloKit.getAssoggettamentoIVA();  // Fix 25004
	        AssoggettamentoIVA assIva = getAssoggettamentoIVAArticolo(articoloKit, rigaKit.getIdConfigurazione()); // Fix 25004
            AssoggettamentoIVA assIvaTestata = ((DocumentoOrdineTestata)getTestata()).getAssoggettamentoIVA();//Fix 14670
            if(assIva == null)
            {
                assIva = getAssoggettamentoIVA();//Fix 14670
                rigaKit.setAssoggettamentoIVA(getAssoggettamentoIVA());
            }
            else
            {
                rigaKit.setAssoggettamentoIVA(assIva);
            }

            //Fix 14670 inizio
            // Fix 15359 inizio (ma a standard sono specifiche valide)
            /*
            if (assIvaTestata != null && assIvaTestata.isIVAAgevolata())
              if (assIva == null ||
                  (assIva.getTipoIVA() == AssoggettamentoIVA.SOGGETTO_A_CALCOLO_IVA
                   && !assIva.isIVAAgevolata()
                   && assIva.getAliquotaIVA().compareTo(assIvaTestata.getAliquotaIVA()) > 0))
                rigaKit.setAssoggettamentoIVA(assIvaTestata);
            */
            // Fix 15359 fine
            //Fix 14670 fine
            //Dati Vendita
            /**
                   if (calcoloDatiVendita) {
              condVen = recuperaCondizioniVendita((DocumentoVendita)getTestata());

              DecimalType decType = new DecimalType();
              DateType dateType = new DateType();

              DocumentoVendita testata = (DocumentoVendita)getTestata();
              CondizioniDiVendita cdv =
                RecuperaDatiVendita.getCondizioniVendita(
                  rigaKit.getIdListino(),
                  testata.getIdCliente(),
                  rigaKit.getIdArticolo(),
                  rigaKit.getIdUMRif(),
                  decType.objectToString(rigaKit.getQtaInUMVen()),
                  decType.objectToString(rigaKit.getQtaInUMPrm()),
                  testata.getIdModPagamento(),
                  dateType.objectToString(testata.getDataDocumento()),
                  dateType.objectToString(rigaKit.getDataConsegnaConfermata()),
                  rigaKit.getIdAgente(),
                  rigaKit.getIdAgenteSub(),
                  rigaKit.getIdUMPrm(),
                  testata.getIdValuta()
                );

              if (cdv!=null){
                  rigaKit.setProvvigione2Agente(cdv.getProvvigioneAgente2());
                  rigaKit.setProvvigione2Subagente(cdv.getProvvigioneSubagente2());
                  rigaKit.setScontoArticolo1(cdv.getScontoArticolo1());
                  rigaKit.setScontoArticolo2(cdv.getScontoArticolo2());
                  rigaKit.setMaggiorazione(cdv.getMaggiorazione());
                  rigaKit.setPrezzo(cdv.getPrezzo());
                  rigaKit.setPrezzoExtra(cdv.getPrezzoExtra());
                  rigaKit.setPrezzoListino(cdv.getPrezzo());
                  rigaKit.setPrezzoExtraListino(cdv.getPrezzoExtra());
                  rigaKit.setSconto(cdv.getSconto());
                  rigaKit.setProvenienzaPrezzo(cdv.getTipoTestata());
              }
              else {
                  rigaKit.setProvenienzaPrezzo(TipoRigaRicerca.MANUALE);
              }
                   }
             */

//MG FIX 8659 inizio
            rigaKit.setIdCommessa(getIdCommessa());
            rigaKit.setIdCentroCosto(getIdCentroCosto());
            recuperaDatiCA(rigaKit);
//MG FIX 8659 fine


            rigaKit.setSalvaRigaPrimaria(false);
            rigaKit.getDatiComuniEstesi().setStato(this.getDatiComuniEstesi().
                getStato());
            rigaKit.setEscludiDaDichIntento(isEscludiDaDichIntento());//Fix 26939
            
          //Fix 33905 Inizio
            DocumentoVenRigaSec rigaSecTmp = (DocumentoVenRigaSec)Factory.createObject(DocumentoVenRigaSec.class);
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
            if (rigaKit.getCausaleRiga() != null) {		//Fix 35337
            	rigaKit.setUtilizzaContoAnticipi(rigaKit.getCausaleRiga().getUtilizzaContoAnticipi());//Fix 34503
            }

            getRigheSecondarie().add(rigaKit);
        }
    }

    /**
     * Aggiunto con FIX02407 - DZ. Ritona un BigDecimal 0 se è null.
     * @param importo BigDecimal
     * @return BigDecimal
     */
    protected BigDecimal getNotNullValue(BigDecimal importo)
    {
        if(importo == null)
            return new BigDecimal("0");
        return importo;
    }

    /**
     * Modificato con FIX02407 - DZ.
     * Utilizzato metodo getNotNullValue.
     */
    /*Fix 13494 : Transfered to DocumentoVenditaRiga
    protected void stornaImportiRigaDaTestata()
    {
        if(getTipoRiga() != TipoRiga.OMAGGIO // MG FIX 4532 && getTipoRiga() != TipoRiga.SPESE_MOV_VALORE
           //Fix 5102 - inizio
           &&
           getDatiComuniEstesi().getStato() != DatiComuniEstesi.ANNULLATO)
        {
            //Fix 5102 - fine
            DocumentoVendita testata = (DocumentoVendita)getTestata();
            BigDecimal valoreOrdinato = getNotNullValue(testata.getValoreDocumento());
            BigDecimal costoOrdinato = getNotNullValue(testata.getCostoDocumento());
            BigDecimal imposta = getNotNullValue(testata.getValoreImposta());
            imposta = imposta.subtract(getNotNullValue(getValoreImposta()));
            valoreOrdinato = valoreOrdinato.subtract(getNotNullValue(getValoreRiga()));
            costoOrdinato = costoOrdinato.subtract(getNotNullValue(getCostoRiga()));

            testata.setValoreDocumento(valoreOrdinato);
            testata.setCostoDocumento(costoOrdinato);
            testata.setValoreImposta(imposta);
            testata.setTotaleDocumento(valoreOrdinato.add(imposta));

//MG FIX 6481 inizio: per righe di tipo merce/servizi, se contabilizzazione la lordo dello sconto di fine fattura
//    Aggiungo la quota dello sconto di fine fattura
            if (PersDatiVen.getCurrentPersDatiVen().getContabilizzazioneRicavi() == PersDatiVen.AL_LORDO) {
              if (getTipoRiga() != TipoRiga.OMAGGIO && getTipoRiga() != TipoRiga.SPESE_MOV_VALORE) {
                BigDecimal valoreScontoFF = getNotNullValue(testata.getValoreScontoFF());
                BigDecimal valoreScFFRiga = getNotNullValue(testata.calcolaScontoFineFatturaSoloSeAlLordo(getValoreRiga()));
                valoreScontoFF = valoreScontoFF.subtract(valoreScFFRiga);
                testata.setValoreScontoFF(valoreScontoFF);
              }
              testata.setTotaleDocumento(getNotNullValue(testata.getTotaleDocumento()).subtract(getNotNullValue(testata.getValoreScontoFF())));
            }
//MG FIX 6481 fine
        }
    }
    Fix 13494 Fine*/
    //--------------------------------------------------------//

    //Implementazione metodi astratti di PersistentObject

//  protected TableManager getTableManager() throws java.sql.SQLException {
//    return OrdineVenditaRigaPrmTM.getInstance();
//  }

    //--------------------------------------------------------//

    /**
     * Ridefinizione del metodo getNumeroRiga della classe
     * OrdineVenditaRiga
     */
    protected void componiChiave()
    {
        int n = DocumentoVenditaRiga.getNumeroNuovaRiga(this.getTestata());
        setNumeroRigaDocumento(new Integer(n));
    }


//MG FIX 6754 inizio
/*
    protected void calcolaImportiRiga()
    {
//MG FIX 6481
      boolean contabilizzaAlLordo = (PersDatiVen.getCurrentPersDatiVen().getContabilizzazioneRicavi() == PersDatiVen.AL_LORDO) ? true : false;
        try
        {
            ValorizzatoreImportiDocumentoVendita viov = (
                ValorizzatoreImportiDocumentoVendita)Factory.createObject(
                ValorizzatoreImportiDocumentoVendita.class);
            ImportiRigaDocumentoVendita importi = viov.calcolaImportiRiga(this);
            setValoreRiga(importi.getValoreOrdinato());
            setCostoRiga(importi.getCostoOrdinato());

            //...FIX02001 - DZ
            setValoreImposta(importi.getImpostaValoreOrdinato());
            //...fine FIX02001 - DZ

            //Fix 5102 - inizio
            if(getDatiComuniEstesi().getStato() != DatiComuniEstesi.ANNULLATO)
            {
                //Fix 5102 - fine
                //...FIX02001 - DZ
//          if (getTipoRiga() != TipoRiga.OMAGGIO && getTipoRiga() != TipoRiga.SPESE_MOV_VALORE)
//          {
                //...fine FIX02001 - DZ
                List importiTestata = new ArrayList();
                DocumentoVendita testata = (DocumentoVendita)getTestata();

                BigDecimal valoreOrdinato = new BigDecimal(0);
                BigDecimal costoOrdinato = new BigDecimal(0);
                // Fix 1390
                BigDecimal imposta = new BigDecimal(0);
                // Fine fix 1390
//MG FIX 6481 inizio
                BigDecimal valoreScontoFF = new BigDecimal(0);
//MG FIX 6481 fine

                if(isOnDB() && getOldRiga() != null)
                {
                    //Fix 5102 - inizio
                    //Se la riga ERA in stato ANNULLATO non bisogna stornare altrimenti
                    //il valore stornato si annulla con quello da aggiungere
                    boolean storna = (getOldRiga().getDatiComuniEstesi().getStato() != DatiComuniEstesi.ANNULLATO);
                    if(getTipoRiga() != TipoRiga.OMAGGIO)
                    { //...FIX02001 - DZ
                        valoreOrdinato = getNotNullValue(testata.getValoreDocumento());
                        if(storna)
                        {
                            BigDecimal oldValore = getNotNullValue(getOldRiga().getValoreRiga());
                            valoreOrdinato = valoreOrdinato.subtract(oldValore);
                        }
                        valoreOrdinato = valoreOrdinato.add(getNotNullValue(getValoreRiga()));
                        costoOrdinato = getNotNullValue(testata.getCostoDocumento());
                        if(storna)
                        {
                            BigDecimal oldCosto = getNotNullValue(getOldRiga().getCostoRiga());
                            costoOrdinato = costoOrdinato.subtract(oldCosto);
                        }
                        costoOrdinato = costoOrdinato.add(getNotNullValue(getCostoRiga()));
//MG FIX 6481 inizio:   storno valore sconto di fine fattura
                        if (contabilizzaAlLordo) {
                          if(getTipoRiga() != TipoRiga.SPESE_MOV_VALORE) {
                            valoreScontoFF = getNotNullValue(testata.getValoreScontoFF());
                            if(storna)
                            {
                              BigDecimal oldValore = getNotNullValue(testata.calcolaScontoFineFatturaSoloSeAlLordo(getOldRiga().getValoreRiga()));
                              valoreScontoFF = valoreScontoFF.subtract(oldValore);
                            }
                            valoreScontoFF = valoreScontoFF.add(testata.calcolaScontoFineFatturaSoloSeAlLordo(getValoreRiga()));
                          }
                        }
//MG FIX 6481 fine
                    }
                    imposta = getNotNullValue(testata.getValoreImposta());
                    if(storna)
                    {
                        BigDecimal oldImposta = getNotNullValue(getOldRiga().getValoreImposta());
                        imposta = imposta.subtract(oldImposta);
                    }
                    imposta = imposta.add(getNotNullValue(getValoreImposta()));
                    //Fix 5102 - fine
                }
                else if(isOnDB() && getOldRiga() == null)
                {
                    if(getTipoRiga() != TipoRiga.OMAGGIO)
                    { //...FIX02001 - DZ
                        valoreOrdinato = getNotNullValue(testata.getValoreDocumento()).
                            subtract(getNotNullValue(this.getValoreRiga())).
                            add(getNotNullValue(getValoreRiga()));
                        costoOrdinato = getNotNullValue(testata.getCostoDocumento()).
                            subtract(getNotNullValue(this.getCostoRiga())).
                            add(getNotNullValue(getCostoRiga()));
//MG FIX 6481 inizio
                        if (contabilizzaAlLordo) {
                          if(getTipoRiga() != TipoRiga.SPESE_MOV_VALORE) {
                            BigDecimal valScontoFFRiga = testata.calcolaScontoFineFatturaSoloSeAlLordo(getValoreRiga());
                            valoreScontoFF = getNotNullValue(testata.getValoreScontoFF()).
                                subtract(getNotNullValue(valScontoFFRiga)).
                                add(getNotNullValue(valScontoFFRiga));
                          }
                        }
//MG FIX 6481 fine
                    }
                    // Fix 1390
                    // Fix 1425
                    if(testata.getValoreImposta() != null)
                    {
                        imposta = getNotNullValue(testata.getValoreImposta()).
                            subtract(getNotNullValue(this.getValoreImposta())).
                            add(getNotNullValue(getValoreImposta()));
                    }
                    else
                    {
                        imposta = new BigDecimal(0);
                    }
                    // Fine fix 1425
                    // Fine fix 1390
                }
                else
                {
                    if(getTipoRiga() != TipoRiga.OMAGGIO)
                    { //...FIX02001 - DZ
                        valoreOrdinato = getNotNullValue(testata.getValoreDocumento()).add(
                            getNotNullValue(getValoreRiga()));
                        costoOrdinato = getNotNullValue(testata.getCostoDocumento()).add(
                            getNotNullValue(getCostoRiga()));
//MG FIX 6481 inizio
                        if (contabilizzaAlLordo) {
                          if(getTipoRiga() != TipoRiga.SPESE_MOV_VALORE) {
                            valoreScontoFF = getNotNullValue(testata.getValoreScontoFF()).add(
                                testata.calcolaScontoFineFatturaSoloSeAlLordo(getValoreRiga()));
                          }
                        }
//MG FIX 6481 fine

                    }
                    // Fix 1390
                    // Fix 1425
                    if(testata.getValoreImposta() != null)
                    {
                        imposta = getNotNullValue(testata.getValoreImposta()).add(
                            getNotNullValue(getValoreImposta()));
                    }
                    else
                    {
                        imposta = getNotNullValue(getValoreImposta());
                    }
                    // Fine fix 1425
                    // Fine fix 1390
                }
                if(getTipoRiga() != TipoRiga.OMAGGIO)
                { //...FIX02001 - DZ
                    testata.setValoreDocumento(valoreOrdinato);
                    testata.setCostoDocumento(costoOrdinato);
//MG FIX 6481 inizio
                    if (contabilizzaAlLordo) {
                      if (getTipoRiga() != TipoRiga.SPESE_MOV_VALORE)  {
                        testata.setValoreScontoFF(getNotNullValue(valoreScontoFF));
                      }
                    }
//MG FIX 6481 fine
                }
                // Fix 1390
                testata.setValoreImposta(imposta);
                testata.setTotaleDocumento(getNotNullValue(testata.getValoreDocumento()).
                                           add(imposta));
//MG FIX 6481:  se contabilizzazione al lordo dello sconto di fine fattura, decremento il totale
//              del documento della quota di sconto della riga corrente
                if (contabilizzaAlLordo) {
                  testata.setTotaleDocumento(getNotNullValue(testata.getTotaleDocumento()).subtract(getNotNullValue(testata.getValoreScontoFF())));
                }
//MG FIX 6481 fine
                // Fine fix 1390
                //...FIX02001 - DZ
//          }
                //...fine FIX02001 - DZ
            }
            //Fix 5102 - inizio
            else
            {
                //Se la riga PASSA in stato ANNULLATO è necessario stornare valore/costo
                //dalla testata. Il secondo gruppo di controlli serve per evitare che le
                //stesse operazioni fatte in fase di salvataggio avvengano anche in caso
                //di azione 'Passa a riga', che pure scatena un salvataggio
                if(isOnDB() && (getOldRiga() != null && getOldRiga().getDatiComuniEstesi().getStato() != DatiComuniEstesi.ANNULLATO))
                {
                    DocumentoVendita testata = (DocumentoVendita)getTestata();
                    DocumentoDocRiga oldRiga = getOldRiga();
                    //Valore imponibile
                    BigDecimal valoreImponibile =
                        getNotNullValue(testata.getValoreDocumento()).
                        subtract(
                        getNotNullValue(oldRiga.getValoreRiga())
                        );
                    testata.setValoreDocumento(valoreImponibile);
                    //Valore imposta
                    BigDecimal valoreImposta =
                        getNotNullValue(testata.getValoreImposta()).
                        subtract(
                        getNotNullValue(oldRiga.getValoreImposta())
                        );
                    testata.setValoreImposta(valoreImposta);
//MG FIX 6481 inizio
                    //Valore sconto di fine fattura
                    if (contabilizzaAlLordo) {
                      if (getTipoRiga() != TipoRiga.OMAGGIO && getTipoRiga() != TipoRiga.SPESE_MOV_VALORE) {
                        BigDecimal valoreScontoFF = getNotNullValue(testata.getValoreScontoFF());
                        valoreScontoFF = valoreScontoFF.subtract(testata.calcolaScontoFineFatturaSoloSeAlLordo(oldRiga.getValoreRiga()));
                        testata.setValoreScontoFF(valoreScontoFF);
                      }
                    }
//MG FIX 6481 fine

                    //Totale documento
                    testata.setTotaleDocumento(
                        getNotNullValue(testata.getValoreDocumento()).
                        add(
                        valoreImposta
                        )
                        );
//MG FIX 6481:  se contabilizzazione al lordo dello sconto di fine fattura, decremento il totale
//              del documento della quota di sconto della riga corrente
                    if (contabilizzaAlLordo) {
                        testata.setTotaleDocumento(getNotNullValue(testata.getTotaleDocumento()).subtract(getNotNullValue(testata.getValoreScontoFF())));
                    }
//MG FIX 6481 fine
                    //Costo
                    BigDecimal costoOrdinato =
                        getNotNullValue(testata.getCostoDocumento()).
                        subtract(
                        getNotNullValue(getCostoRiga())
                        );
                    testata.setCostoDocumento(costoOrdinato);
                }
            }
            //Fix 5102 - fine
        }
        catch(Exception e)
        {
            Trace.println("Eccezione nel metodo calcolaImportiRiga() della classe " +
                          getClass().getName() + ": " + e.getMessage());
            e.printStackTrace(Trace.excStream);
        }
    }
*/
//MG FIX 6754 fine

    protected DocumentoRigaLotto creaLottoDummy()
    {
        DocumentoVenRigaLottoPrm lottoD;
        lottoD = (DocumentoVenRigaLottoPrm)Factory.createObject(
            DocumentoVenRigaLottoPrm.class);
        lottoD.setFather(this);
        lottoD.setIdLotto(LOTTO_DUMMY);
        lottoD.setIdArticolo(getIdArticolo());
        return lottoD;
    }

    public String getOrderByClause()
    {
        return "SEQUENZA_RIGA";
    }

    protected void inizializzaIFigli(boolean perConvalida, boolean perRegressione)
    {
        super.inizializzaIFigli(perConvalida, perRegressione);
        Iterator i = getRigheSecondarie().iterator();
        while(i.hasNext())
        {
            DocumentoVenRigaSec ovrs = (DocumentoVenRigaSec)i.next();
            if(ovrs != null)
            {
                ovrs.setMovimentiPortafoglio(iMovimentiPortafoglio);
                ovrs.iApplicaMovimentiSuiSaldi = false;
                if(perConvalida)
                {
                    ovrs.setApplicoMovimenti(true);
                    ovrs.setListaMovimentiMagazzino(iMovimentiMagazzino);
                }
                if(perRegressione)
                {
                    ovrs.setApplicoMovimenti(true);

                }
            }
        }

    }

    /**
     * Restituisce l'attributo relativo al Proxy RigaCollegata
     * (Id dettaglio riga).
     */
    /**
       public Integer getIdDettaglioRigaCollegata() {
      DocumentoBaseRiga docRig = (DocumentoBaseRiga)iRigaCollegata.getObject();
      if (docRig==null)
          return null;
      Integer rDettaglioRigaCollegata = docRig.getDettaglioRigaDocumento();
      return rDettaglioRigaCollegata;
       }
     */
    /**
     * Valorizza l'attributo relativo al Proxy RigaCollegata
     * (Id dettaglio riga).
     */
    /**
       public void setIdDettaglioRigaCollegata(Integer rDettaglioRigaCollegata) {

     DocumentoBaseRiga docRig = (DocumentoBaseRiga)iRigaCollegata.getObject();
     if (docRig==null)
      return;
     docRig.setDettaglioRigaDocumento(rDettaglioRigaCollegata);
     setDirty();

       }
     */
    /**
     * Restituisce l'attributo relativo al Proxy RigaCollegata (Id riga).
     */

    public Integer getIdRigaCollegata()
    {
        return iIdRigaCollegata;
    }

    /**
     * Valorizza l'attributo relativo al Proxy RigaCollegata (Id riga).
     */

    public void setIdRigaCollegata(Integer rRigaCollegata)
    {
        iIdRigaCollegata = rRigaCollegata;
        setDirty();
    }

    /**
     * Restituisce l'attributo relativo al Proxy RigaCollegata
     * (Id dettaglio riga).
     */
    public Integer getIdDettaglioRigaCollegata()
    {
        return iIdDettaglioRigaCollegata;
        /**
             DocumentoVenditaRiga rigacoll = (DocumentoVenditaRiga) iRigaCollegata.getObject();
             if (rigacoll==null)
            return null;
             return rigacoll.getDettaglioRigaDocumento();
         */
    }

    /**
     * Valorizza l'attributo relativo al Proxy RigaCollegata
     * (Id dettaglio riga).
     */
    public void setIdDettaglioRigaCollegata(Integer rDettaglioRigaCollegata)
    {
        iIdDettaglioRigaCollegata = rDettaglioRigaCollegata;
        /**
             DocumentoVenditaRiga rigacoll = (DocumentoVenditaRiga) iRigaCollegata.getObject();
             if (rigacoll==null)
            return;
             rigacoll.setDettaglioRigaDocumento(rDettaglioRigaCollegata);
         */
        setDirty();
    }

    public List preparoConvalida(List errRiga)
    {
        super.preparoConvalida(errRiga);
        //testata.collegaRighe(testata);
        //List movimentiMagazzino = new ArrayList();
        Iterator iter = this.getRigheSecondarie().iterator();
        while(iter.hasNext())
        {
            DocumentoVenditaRiga riga = (DocumentoVenditaRiga)iter.next();
            //riga.setListaMovimentiMagazzino(movimentiMagazzino);
            //Fix 16242-19920 - inizio
//            if(riga != null && riga.isCollegataAMagazzino() == false
//                  && riga.getDatiComuniEstesi().getStato() != DatiComuniEstesi.ANNULLATO)  //MG FIX 12046
            if(riga != null && areCondizPrepConvalidaRigaSec((DocumentoVenRigaSec)riga))
            //Fix 16242-19920 - fine
            {
                errRiga = riga.preparoConvalida(errRiga);
            }
        }
        return errRiga;
    }

    protected int regressioneRiga() throws SQLException
    {
        /**
            int rc = 0;
            int rc1 = 0;
            int rc2 = 0;
            Iterator s = this.getRigheSecondarie().iterator();
            while (s.hasNext())
            {
                DocumentoVenRigaSec rigaSec = (DocumentoVenRigaSec)s.next();
                rc2 = rigaSec.regressione();
                if (rc2<0)
                    rc1 = rc2;
                else
                    rc1 = rc1 + rc2;
            }
            if (rc1>=0){
                rc = super.regressioneRiga();
                if (rc>=0)
                    rc = rc + rc1;
            }
            return rc1;
         */
        int rc = super.regressioneRiga();
        int rc1 = 0;
        Iterator iter = this.getRigheSecondarie().iterator();
        while(iter.hasNext())
        {
            DocumentoVenditaRiga riga = (DocumentoVenditaRiga)iter.next();
            //riga.setListaMovimentiMagazzino(movimentiMagazzino);
            if(riga != null && riga.isCollegataAMagazzino() == true &&
               this.getDatiComuniEstesi().getStato() != DatiComuniEstesi.ANNULLATO)
            {
                rc1 = riga.regressioneRiga();
                if(rc1 > 0)
                {
                    rc = rc + rc1;
                }
            }
        }
        return rc;
    }

    /**
     * Quando su una riga primaria viene cambiata una delle seguenti date:<br>
     * <ul>
     * <li>richiesta consegna
     * <li>confermata consegna
     * </ul>
     * le modifiche devono essere riportate su tutte le righe secondarie
     */
    protected void gestioneDateRigheSecondarie() throws SQLException
    {
    	//Fix 33762 inizio
    	boolean propagaDati = true;
    	if (isOnDB())
    	{
    		if (getOldRiga() != null)
    		{
    			if ((this.datiUguali(getOldRiga().getDataConsegnaRichiesta(),getDataConsegnaRichiesta()) ||
    				this.datiUguali(getOldRiga().getDataConsegnaConfermata(),getDataConsegnaConfermata()) ||
    				this.datiUguali(getOldRiga().getDataBolla(), getDataBolla()) || 
    				datiUguali(getOldRiga().getDataFattura(), getDataFattura()) ||
    				datiUguali(getOldRiga().getNumeroBolla(), getNumeroBolla()) ||
    				datiUguali(getOldRiga().getNumeroFattura(), getNumeroFattura())))
    				propagaDati = false;
    		}
    	}
    	if (!propagaDati)
    		return;
    	//Fix 33762 Fine
    	
        //Fix 3230 - inizio
       // if(getOldRiga() != null) { Fix 33762 
       
            //Fix 3230 - fine
            List righeSecondarie = getRigheSecondarie();
          //Fix 33762 inizio
            if (righeSecondarie.isEmpty()) 
          	  return;
          //Fix 33762 fine
            /* if(!righeSecondarie.isEmpty())
            {
            //Fix 1336
                if(!this.datiUguali(getOldRiga().getDataConsegnaRichiesta(),
                                    getDataConsegnaRichiesta()) ||
                   !this.datiUguali(getOldRiga().getDataConsegnaConfermata(),
                                    getDataConsegnaConfermata()) ||
                   !this.datiUguali(getOldRiga().getDataBolla(), getDataBolla()) ||
                   !datiUguali(getOldRiga().getDataFattura(), getDataFattura()) ||
                   !datiUguali(getOldRiga().getNumeroBolla(), getNumeroBolla()) ||
                   !datiUguali(getOldRiga().getNumeroFattura(), getNumeroFattura())
                   ){ Fix 33762  */
                    //Fine Fix 1336
                    Iterator iter = righeSecondarie.iterator();
                    while(iter.hasNext())
                    {
                        DocumentoVenRigaSec rigaSec = (DocumentoVenRigaSec)iter.next();
                        rigaSec.setDataConsegnaRichiesta(getDataConsegnaRichiesta());
                        rigaSec.setDataConsegnaConfermata(getDataConsegnaConfermata());
                        rigaSec.setSettConsegnaRichiesta(getSettConsegnaRichiesta());
                        rigaSec.setSettConsegnaConfermata(getSettConsegnaConfermata());
                        /**
                                    rigaSec.setDataBolla(getDataBolla());
                                    rigaSec.setDataFattura(getDataFattura());
                                    rigaSec.setNumeroBolla(getNumeroBolla());
                                    rigaSec.setNumeroFattura(getNumeroFattura());
                         */
                    }
                //}
           // }
       // }
    }

    /**
     * Calcola il prezzo di una riga primaria, che ha un articolo di tipo
     * KIT e tipo calcolo prezzo DA_COMPONENTI, sommando i prezzi di tutte le
     * righe secondarie che compongono il kit e tenendo conto di un eventuale
     * percentuale di markup
     */
    public void calcolaPrezzoDaRigheSecondarieConReset(boolean reset)
    {
        try
        {
            //Fix 3929 - inizio
            char tipoParte = getArticolo().getTipoParte();
            char tipoCalcoloPrezzo = getArticolo().getTipoCalcPrzKit();
            if((tipoParte == ArticoloDatiIdent.KIT_NON_GEST ||
                tipoParte == ArticoloDatiIdent.KIT_GEST)
               &&
               tipoCalcoloPrezzo == ArticoloDatiVendita.DA_COMPONENTI || (tipoCalcoloPrezzo == ArticoloDatiVendita.SUL_PRODOTTO_FINITO)) { //72269 Softre anche su pf
//            {
                //Fix 3929 - fine
                BigDecimal zero = new BigDecimal(0.0);
                BigDecimal prezzoRigaPrimaria = zero;
                BigDecimal costoRigaPrimaria = zero ;//Fix 33905	
                ValorizzatoreImportiDocumentoVendita viov =
                    new ValorizzatoreImportiDocumentoVendita();
                ImportiRigaDocumentoVendita importi = viov.calcolaImportiRiga(this);

                Iterator righeSecondarie = importi.getValoriRigheSecondarie().iterator();
                while(righeSecondarie.hasNext())
                {
                    ImportiRigaDocumentoVendita importoRigaSec =
                        (ImportiRigaDocumentoVendita)righeSecondarie.next();
                    if (importoRigaSec.getSpecializzazioneRiga() == RIGA_SECONDARIA_PER_COMPONENTE) //MG FIX 6754 inizio
                    {//Fix 33905
                      prezzoRigaPrimaria = prezzoRigaPrimaria.add(importoRigaSec.getValoreOrdinato());
                      costoRigaPrimaria = costoRigaPrimaria.add(importoRigaSec.getCostoOrdinato());// Fix 33905
                    }//Fix 33905	
                }

                //Fix 3929 - inizio
                if (this.getServizioQtaInUMVen() != null && this.getServizioQtaInUMVen().compareTo(new BigDecimal(0.0)) != 0)//Fix 33874
                {//Fix 33905
                	prezzoRigaPrimaria = prezzoRigaPrimaria.divide(this.getServizioQtaInUMVen(), BigDecimal.ROUND_HALF_UP);
                	costoRigaPrimaria = costoRigaPrimaria.divide(this.getServizioQtaInUMVen(), BigDecimal.ROUND_HALF_UP);
                }//Fix 33905	
                BigDecimal markup = getArticolo().getMarkupKit();
                if(markup != null && markup != zero)
                {
                    BigDecimal perc = markup.divide(new BigDecimal(100.0), 6, BigDecimal.ROUND_HALF_UP);
                    prezzoRigaPrimaria = prezzoRigaPrimaria.add(prezzoRigaPrimaria.multiply(perc));
                }
                setPrezzo(prezzoRigaPrimaria);
                //Fix 3929 - fine
                //Fix 33905 Inizio
                if(tipoParte == ArticoloDatiIdent.KIT_NON_GEST)
                	setCostoUnitario(costoRigaPrimaria);
                //Fix 33905 Fine
//MG FIX 6754 inizio
                if (reset) {
//MG FIX 6754 fine
                  setScontoArticolo1(new BigDecimal(0.0));
                  setScontoArticolo2(new BigDecimal(0.0));
                  setMaggiorazione(new BigDecimal(0.0));
                  setSconto(null);
                  setTipoPrezzo(TipoPrezzo.LORDO);
                  setProvenienzaPrezzo(TipoRigaRicerca.MANUALE);
                }
                //MG FIX 6204
                //recupero provv. agente
                aggiornaProvvigioni();
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace(Trace.excStream);
        }
    }

//MG FIX 6754 inizio
    public void calcolaPrezzoDaRigheSecondarie() {
      calcolaPrezzoDaRigheSecondarieConReset(true);
    }
    public void calcolaPrezzoDaRigheSecondarieSenzaReset() {
      calcolaPrezzoDaRigheSecondarieConReset(false);
    }
//MG FIX 6754 fine


//MG FIX 6204
  public void aggiornaProvvigioni() {
    try {
      //Fix 19628 inizio
      /*
      String idAgente = this.getIdAgente();
      String idSubAgente  = this.getIdAgenteSub();
      DocumentoVendita testata = ((DocumentoVendita)this.getTestata());
      if ((idAgente!=null && !idAgente.trim().equals(""))||(idSubAgente!=null && !idSubAgente.trim().equals(""))) {
        CondizioniDiVendita cV = (CondizioniDiVendita) Factory.createObject(CondizioniDiVendita.class);
        cV.setRArticolo(getIdArticolo());
        cV.setRSubAgente(idSubAgente);
        cV.setRAgente(idAgente);
        cV.setRValuta(testata.getIdValuta());
        cV.setRUnitaMisura(this.getIdUMRif());
        cV.setRCliente(this.getIdIntestatario());
        cV.setIdAzienda(this.getIdAzienda());
        cV.setRConfigurazione(this.getIdConfigurazione());

        java.sql.Date dataValid = null;
        PersDatiVen pda = PersDatiVen.getCurrentPersDatiVen();
        char tipoDataPrezziSconti = testata.getCliente().getRifDataPerPrezzoSconti();
        if (tipoDataPrezziSconti == RifDataPrzScn.DA_CONDIZIONI_GENERALI)
          tipoDataPrezziSconti = pda.getTipoDataPrezziSconti();
        switch (tipoDataPrezziSconti) {
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
        cV.setQuantita(this.getQtaInUMVen());
        cV.setRModalitaPagamento(testata.getIdModPagamento());

        RicercaCondizioniDiVendita ric = (RicercaCondizioniDiVendita)Factory.createObject(RicercaCondizioniDiVendita.class);
        ric.setCondizioniDiVendita(cV);
        ric.aggiornaProvvigioni();
        setProvvigione2Agente(ric.getCondizioniDiVendita().getProvvigioneAgente2());
        setProvvigione2Subagente(ric.getCondizioniDiVendita().getProvvigioneSubagente2());
      }*/
      PersDatiVen pdv = PersDatiVen.getCurrentPersDatiVen();
    //if (pdv.getGestionePvgSuScalaSconti() && //27616      
      if ((pdv.getGestionePvgSuScalaSconti() || ( pdv.getGestioneAnagraPvg() && isRicalProvvAgSubag()))  && //27616

          //(isServeRicalProvvAg() || isServeRicalProvvSubag())) { //Fix 25214 PM
          //(isServeRicalProvvAg() || isServeRicalProvvSubag() || !isOnDB())) { //Fix 25214 PM//Fix 26599
    		(isServeRicalProvvAg() || isServeRicalProvvSubag() || isRicalProvvAgSubag())) { //Fix 26599
        modificaProvv2Agente();
      }
      //Fix 19628 fine
    }
    catch (Exception ex) {
      ex.printStackTrace(Trace.excStream);
    }
  }
//MG FIX 6204


    /**
     * Metodo per sistemare le quantità dei lotti
     */
    protected void rendiDefinitivoIFigli() throws SQLException
    {
        super.rendiDefinitivoIFigli();
        Iterator i = this.getRigheSecondarie().iterator();
        while(i.hasNext())
        {
            DocumentoVenRigaSec riga = (DocumentoVenRigaSec)i.next();
            if(riga != null &&
               riga.getStatoAvanzamento() == StatoAvanzamento.PROVVISORIO /* &&
               riga.getDatiComuniEstesi().getStato() != DatiComuniEstesi.ANNULLATO*/)//Fix 22823 
            {
                riga.rendiDefinitivoRiga();
            }
        }
    }

    public void impostaRigaSaldata()
    {
        if(this.isRigaSaldata())
        {
            Iterator iter = this.getRigheSecondarie().iterator();
            while(iter.hasNext())
            {
                DocumentoDocRiga rigaSec = (DocumentoDocRiga)iter.next();
                if(rigaSec != null)
                {
                    rigaSec.setRigaSaldata(true);
                }
            }
        }
    }

    /**
     * Verifica che nel caso di nuova riga che genererà un kit di tipo non gestito
     * a magazzino, e che quindi aggiorna i saldi, e gli articoli delle righe secondarie
     * siano, anche solo uno gestito a lotti, non sia possibile salvarlo con stato avanzamento
     * definitivo.
     */
    /*
      public ErrorMessage checkQuadraturaLottiRigheSecondarie()  {
       try{
         Articolo articolo = getArticolo();
         //Fix 1377: aggiunto controllo su articolo a null
         if (! isOnDB() && articolo != null && getTipoRiga() != TipoRiga.SPESE_MOV_VALORE &&
             articolo.getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST &&
               getRigheSecondarie().isEmpty() &&
               getStatoAvanzamento()==StatoAvanzamento.DEFINITIVO)  {

               EsplosioneNodo nodo = getEsplosioneNodo(getArticolo());
               List datiRigheKit = nodo.getNodiFigli();
               Iterator iter = datiRigheKit.iterator();
               while (iter.hasNext()) {
                  EsplosioneNodo datiRigaKit = (EsplosioneNodo)iter.next();
                  Articolo articoloKit = datiRigaKit.getArticolo();
                  if (articoloKit!=null && articoloKit.isArticLotto()){
                    return new ErrorMessage("THIP_BS213");
                  }
               }
           }
       }
       catch(SQLException e){
        e.printStackTrace(Trace.excStream);
       }
       return null;
      }
     */

    //Fix 3212
    public ErrorMessage checkQuadraturaLottiRigheSecondarie()
    {
        ErrorMessage err = null;
        try
        {
            Articolo articolo = getArticolo();
            //Fix 1377: aggiunto controllo su articolo a null
            if(!isOnDB() && articolo != null &&
               getTipoRiga() != TipoRiga.SPESE_MOV_VALORE &&
               articolo.getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST &&
               getRigheSecondarie().isEmpty() &&
               getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO)
            {

                // Inizio 4500
//      	EspNodoArticolo esplosione674odello = getEsplosioneNodoModello(false,
//            articolo);
                EspNodoArticolo esplosioneModello = esplosioneModelloDocumento(articolo);
                // Fine 4500
                //fix 4453 inizio
                //esplosione modello può ritornare null!!!
                if(esplosioneModello != null)
                {
                    err = checkQuadraturaLottiRigheSecondarieModello(esplosioneModello);
                }
                else
                {
                    EsplosioneNodo nodo = getEsplosioneNodo(getArticolo());
                    err = checkQuadraturaLottiRigheSecondarieDistinta(nodo);
                }
                /*
                         // 03319 : esplosioneModello è sempre diverso da null, ma può
                         // avere la lista delle componenti vuota.
                         List listaComponenti = esplosioneModello.getNodiMateriali();
                         //if (esplosioneModello != null){
                         if (!listaComponenti.isEmpty()) {
                  err = checkQuadraturaLottiRigheSecondarieModello(esplosioneModello);
                         }
                         else {
                  EsplosioneNodo nodo = getEsplosioneNodo(getArticolo());
                  err = checkQuadraturaLottiRigheSecondarieDistinta(nodo);
                         }
                 */
                //fix 4453 fine
            }
            //Fix 18309 Inizio(Blocco commentato) Annulamento del fix 18127
            //Fix 18127 inizio
	    /*
            else if(!isOnDB() && articolo != null &&
                getTipoRiga() != TipoRiga.SPESE_MOV_VALORE &&
                (articolo.getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST || articolo.getTipoParte() == ArticoloDatiIdent.KIT_GEST)&&
                !getRigheSecondarie().isEmpty() &&
                getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO){

            	err = checkQuadraturaLottiRigheSecondariePerArticoloKit();
            }
            */
            //Fix 18127 fine
            //Fix 18309 Fine
        }
        catch(SQLException e)
        {
            e.printStackTrace(Trace.excStream);
        }
        return err;
    }

  	//Fix 18127 inizio
    public ErrorMessage checkQuadraturaLottiRigheSecondariePerArticoloKit(){
    	ErrorMessage error = null;
    	List righeSec = getRigheSecondarie();
    	Iterator iter = righeSec.iterator();
    	while (iter.hasNext() ) {
    		DocumentoVenRigaSec rigaSec = (DocumentoVenRigaSec)iter.next();
    		Articolo articolo = rigaSec.getArticolo();
    		//if (articolo !=null && articolo.isArticLotto() && !controllaProposizioneLotti(articolo) && isLottiDichiarati(rigaSec)){//Fix 27337
    		if (articolo !=null && articolo.isArticLotto() && !controllaProposizioneLotti(articolo) && isLottiDichiarati(rigaSec) && !isControlloLottoDummyDaEscludereRigaSec(articolo)){//Fix 27337
    			error =  new ErrorMessage("THIP_BS213");
    			break;
    		}
    	}
    	return error;
    }

    //Questa metodo é usato in checkQuadraturaLottiRigheSecondariePerArticoloKit
    //chiamata solo in evasione per un documento in StatoAvanzamento DEFINITIVO
    public boolean isLottiDichiarati(DocumentoVenRigaSec rigaSec){
    	List righeLotto = rigaSec.getRigheLotto();
    	if (righeLotto.isEmpty()) return true;
    	QuantitaInUMRif somma = new QuantitaInUMRif();
    	somma.azzera();
    	Iterator iter = righeLotto.iterator();
    	while (iter.hasNext() ) {
    		DocumentoVenRigaLottoSec rigaLotto = (DocumentoVenRigaLottoSec) iter.next();
    		//if(rigaLotto != null && rigaLotto.getIdLotto() == Lotto.LOTTO_DUMMY)
    		somma = somma.add(rigaLotto.getQtaAttesaEvasione()); // Siamo in caso di documentoDefinitivo
    	}
    	if (somma.compareTo(rigaSec.getQtaAttesaEvasione()) <0)
    		return true;
    	else
    		return false;
    }
  	//Fix 18127 fine

    // Inizio 4500
    protected EspNodoArticolo esplosioneModelloDocumento(Articolo articolo) throws SQLException
    {

        EspNodoArticolo esplosione = null;
        boolean okModello = false;
        try
        {
            esplosione = getEsplosioneNodoModello(false, articolo, ModelloProduttivo.KIT);
            okModello = true;
        }
        catch(ThipException ex)
        {
            okModello = false;
            esplosione = null;
        }

        if(!okModello)
        {
            try
            {
                esplosione = getEsplosioneNodoModello(false, articolo, ModelloProduttivo.PRODUZIONE);
                okModello = true;
            }
            catch(ThipException ex)
            {
                okModello = false;
                esplosione = null;
            }
        }
        return esplosione;
    }

    // Fine 4500



    /*
      public ErrorMessage checkQuadraturaLottiRigheSecondarieModello(EspNodoArticolo nodoModello){
        ErrorMessage error = null;
        List datiRigheKit = nodoModello.getNodiMateriali();
        Iterator iter = datiRigheKit.iterator();
        while (iter.hasNext() ) {
          EspNodoArticolo datiRigaKit = (EspNodoArticolo)iter.next();
          Articolo articoloKit = datiRigaKit.getArticoloUsato().getArticolo();
          if (articoloKit!=null && articoloKit.isArticLotto()){
            error =  new ErrorMessage("THIP_BS213");
            break;
          }
        }
        return error;
      }
     */
    /*
      public ErrorMessage checkQuadraturaLottiRigheSecondarieDistinta(EsplosioneNodo nodo){
        ErrorMessage error = null;
        List datiRigheKit = nodo.getNodiFigli();
        Iterator iter = datiRigheKit.iterator();
        while (iter.hasNext()) {
          EsplosioneNodo datiRigaKit = (EsplosioneNodo)iter.next();
          Articolo articoloKit = datiRigaKit.getArticolo();
          if (articoloKit!=null && articoloKit.isArticLotto()){
            error =  new ErrorMessage("THIP_BS213");
            break;
          }
        }
        return error;
      }
     */
    //Fine Fix 3212

    // ini FIX 1684
    public ErrorMessage checkRigaDuplicata()
    {
        ErrorMessage em = null;
        DocumentoVendita doc = (DocumentoVendita)this.getTestata();
        if(this.iIsCheckRigaDuplicata && doc != null && !this.isOnDB())
        {
            List righe = doc.getRighe();
            if(righe != null && !righe.isEmpty())
            {
                Iterator iter = righe.iterator();
                while(iter.hasNext())
                {
                    DocumentoVenRigaPrm riga = (DocumentoVenRigaPrm)iter.next();
                    if(riga.getKey().equals(this.getKey()))
                    {
                        continue;
                    }
                    String keyArt = riga.getArticoloKey();
                    if(riga.getCausaleRigaKey().equals(riga.getCausaleRigaKey()))
                    {
                        if(keyArt.equals(this.getArticoloKey()))
                        {
                            String dr = riga.getDescrizioneArticolo();
                            if(dr == null)
                            {
                                dr = "";
                            }
                            String dt = this.getDescrizioneArticolo();
                            if(dt == null)
                            {
                                dt = "";
                            }
                            if(dr.equals(dt))
                            {
                                if(riga.getServizioQta().compareTo(this.getServizioQta()) == 0)
                                {
                                    BigDecimal pr = riga.getPrezzo();
                                    if(pr == null)
                                    {
                                        pr = new BigDecimal("0.00");
                                    }
                                    BigDecimal pt = this.getPrezzo();
                                    if(pt == null)
                                    {
                                        pt = new BigDecimal("0.00");
                                    }
                                    if(pr.compareTo(pt) == 0)
                                    {
                                        BigDecimal sa1r = riga.getScontoArticolo1();
                                        if(sa1r == null)
                                        {
                                            sa1r = new BigDecimal("0.00");
                                        }
                                        BigDecimal sa1t = this.getScontoArticolo1();
                                        if(sa1t == null)
                                        {
                                            sa1t = new BigDecimal("0.00");
                                        }
                                        if(sa1r.compareTo(sa1t) == 0)
                                        {
                                            // segnalazione
                                            String params = "'" + riga.getIdArticolo() + "'";
                                            em = new ErrorMessage("THIP_BS307", params);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return em;
    }

    // fine FIX 1684

    public Vector checkAll(BaseComponentsCollection components)
    {

        Vector errors = new Vector();
        //Fix 45246 inizio  
  	    if(isCheckRigaDaIngorare())
  	    	return errors;
  	    //Fix 45246 fine
        errors = super.checkAll(components);
        Vector otherErrors = new Vector();
        otherErrors.addElement(checkQuadraturaLottiRigheSecondarie());
        char tipoRiga = getTipoRiga();
        //Fix 6439 - inizio: aggiunto controllo
        if (tipoRiga != TipoRigaDocumentoVendita.SERVIZIO_NOLEGGIO &&
            tipoRiga != TipoRigaDocumentoVendita.SERVIZIO_ASSIST_MANUT) {
          otherErrors.addElement(checkRigheSecondarie()); //...FIX04607 - DZ
        }
        //Fix 6439 - fine
        for(int i = 0; i < otherErrors.size(); i++)
        {
            ErrorMessage err = (ErrorMessage)otherErrors.elementAt(i);
            if(err != null)
                errors.addElement(err);
        }
        //Fix 1922
        ErrorMessage em = checkRigaDuplicata();
        if(em != null)
        {
            errors.add(em);
        }
        //Fix 1922

        //Fix 1918 - inizio
        em = checkCatalogo();
        if(em != null)
        {
            errors.add(em);
        }
        //Fix 1918 - fine

        //Fix 7220 inizio
        em = checkDisponibilitaRigaDocAccPrn();
        if(em != null)
           errors.add(em);
        //Fix 7220 fine
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
        
        em = checkNumDichIntentoDaUtilizz();
        if(em != null)
        	errors.addElement(em);
        
        return errors;
    }

    protected boolean leRigheSecVannoSuiSaldi()
    {
        if(this.getArticolo().getTipoParte() == ArticoloDatiIdent.KIT_GEST)
            return false;
        return true;
    }

    public void creaRigaOmaggio(DocumentoVendita testata) throws SQLException
    {
        ListinoVenditaScaglione lvs = null;

        if(isServizioCalcDatiVendita())
        {
            if(condVen == null)
            {
                condVen = recuperaCondizioniVendita(testata);
            }
            if(condVen == null)
            {
                return;
            }
            lvs = condVen.getListinoVenditaScaglione();
        }
        else
        {
            lvs = (ListinoVenditaScaglione)Factory.createObject(
                ListinoVenditaScaglione.class);
            lvs.setKey(getServizioListVendScaglione());
            lvs.retrieve();
        }

        if(lvs != null)
        {
            ListinoVenditaOffertaOmaggio offOmg = lvs.getOffertaOmaggio();

            if(offOmg != null &&
               offOmg.getTipoOmaggioOfferta() !=
               ListinoVenditaOffertaOmaggio.INCOMPLETO)
            {
                //Quantità di riferimento omaggio-offerta
                BigDecimal quantRiferimento = offOmg.getQuantitaRiferimento();
                BigDecimal quantMin = offOmg.getQuantitaMin();
                BigDecimal quantMax = offOmg.getQuantitaMax();

                //Fix 4858 - fine
                //Quantità ordinata (riga)
                //BigDecimal quantOrdinata = getQtaInUMVen();
                //BigDecimal quantOrdinata = new BigDecimal("0.00");Fix 30871
                BigDecimal quantOrdinata = new BigDecimal("0.00").setScale(Q6Calc.get().scale(2));//Fix 30871               
                char rifUMPrz = getRiferimentoUMPrezzo();
                if(rifUMPrz == RiferimentoUmPrezzo.VENDITA)
                {
                    quantOrdinata = getQtaInUMVen();
                }
                else if(rifUMPrz == RiferimentoUmPrezzo.MAGAZZINO)
                {
                    quantOrdinata = getQtaInUMPrmMag();
                }
                //Fix 4858 - fine

                //BigDecimal quantTotOmaggioOfferta = new BigDecimal("0.00");//Fix 30871 
                BigDecimal quantTotOmaggioOfferta = new BigDecimal("0.00").setScale(Q6Calc.get().scale(2));//Fix 30871 
                //Calcola la quantità dovuta di articoli omaggio
                if(quantOrdinata.compareTo(quantRiferimento) >= 0)
                {
                    quantTotOmaggioOfferta = (quantOrdinata.divide(quantRiferimento,
                        BigDecimal.ROUND_DOWN)).
                        multiply(offOmg.getQuantitaOmaggioOfferta());
                    quantTotOmaggioOfferta =
                        quantTotOmaggioOfferta.setScale(0, BigDecimal.ROUND_DOWN);
                }

                if(quantTotOmaggioOfferta.compareTo(quantMin) != -1)
                {

                    if(quantTotOmaggioOfferta.compareTo(quantMax) == 1)
                    {
                        quantTotOmaggioOfferta = quantMax;
                    }

                    //Prepara i valori che si differenziano tra i due tipi di riga
                    CausaleRigaVendita causale = null;
                    char tipoRiga = '\0';

                    switch(offOmg.getTipoOmaggioOfferta())
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

                            // Fix 2636
                            causale = getCausaleRigaOmaggio(testata,
                                ListinoVenditaOffertaOmaggio.
                                V_PER_O);
                            if(causale != null)
                            {
                                // Fine fix 2636
                                BigDecimal newQta = getQtaInUMVen();
                                newQta = newQta.subtract(quantTotOmaggioOfferta);
                                setQtaInUMVen(newQta);
                                if(getUMPrm() != null)
                                    setQtaInUMPrm(getArticolo().convertiUM(newQta, getUMRif(),
                                        getUMPrm(), getArticoloVersRichiesta())); // fix 10955
                                if(getUMSec() != null)
                                    setQtaInUMSec(getArticolo().convertiUM(getQtaInUMPrm(),
                                        getUMPrm(), getUMSec(), getArticoloVersRichiesta())); // fix 10955
                                tipoRiga = TipoRiga.OMAGGIO;
                                // Fix 2636
                                if(this.getArticolo().isArticLotto())
                                {
                                    this.setStatoAvanzamento(StatoAvanzamento.PROVVISORIO);
                                    this.getRigheLotto().clear();
                                }
                            }

                            // Fine fix 2636
                            break;
                        case ListinoVenditaOffertaOmaggio.V_PIU_O:
                            causale = getCausaleRigaOmaggio(testata,
                                ListinoVenditaOffertaOmaggio.
                                V_PIU_O);
                            tipoRiga = TipoRiga.OMAGGIO;
                            break;
                    }

                    if(causale != null)
                    {
                        //Articolo della riga omaggio/offerta
                        Articolo articolo = offOmg.getArticolo();

                        //Crea le righe
                        rigaOmf = (DocumentoVenRigaPrm)Factory.createObject(
                            DocumentoVenRigaPrm.class);

                        rigaOmf.setRigaOfferta(
                            offOmg.getTipoOmaggioOfferta() ==
                            ListinoVenditaOffertaOmaggio.OFFERTA
                            );

                        //Chiave
                        rigaOmf.setTestata(testata);
                        rigaOmf.setNumeroRigaDocumento(
                            new Integer(DocumentoVenditaRiga.getNumeroNuovaRiga(testata) + 1)//fix 8886
                            );
                        //Campi not nullable
                        //DADEN
                        /**
                                     rigaOmf.setRigaCollegata(this);
                         */

                        rigaOmf.setTipoRiga(tipoRiga);
                        rigaOmf.setStatoAvanzamento(getStatoAvanzamento());
                        // Fix 2636
                        if(offOmg.getArticolo().isArticLotto())
                        {
                            // Inizio 3814
                            //rigaOmf.setStatoAvanzamento(StatoAvanzamento.PROVVISORIO);
                            rigaOmf.setStatoAvanzamento(getStatoAvanzamento());
                            // Fine 3814
                        }
                        // Fine fix 2636
                        //Scheda Generale
                        rigaOmf.setCausaleRiga((CausaleRigaDocVen)causale);

                        rigaOmf.setCommessa(this.getCommessa());
                        rigaOmf.setDocumentoMM(this.getDocumentoMM());

                        rigaOmf.setMagazzino(getMagazzino());
                        rigaOmf.setArticolo(articolo);
                        rigaOmf.setDescrizioneArticolo(
                            articolo.getDescrizioneArticoloNLS().getDescrizione()
                            );
                        rigaOmf.setConfigurazione(offOmg.getConfigurazione());

                        // Faccio questo perchè l'unità di misura che proviene dal listino potrebbe
                        // non essere di vendita.
                        UnitaMisura unitaMisuraVendita = offOmg.getUnitaMisura();
                        boolean passato = false;
                        List l = articolo.getArticoloDatiVendita().getForcedUMSecondarie();
                        Iterator iter = l.iterator();
                        while(iter.hasNext())
                        {
                            UnitaMisura uni = (UnitaMisura)iter.next();
                            // Fix 2614
                            //if (uni.equals(unitaMisuraVendita)){
                            if(unitaMisuraVendita != null && uni != null &&
                               uni.
                               getIdUnitaMisura().equals(unitaMisuraVendita.getIdUnitaMisura()))
                            {
                                // Fine fix 2164
                                passato = true;
                                break;
                            }
                        }
                        if(unitaMisuraVendita == null || !passato)
                        {
                            unitaMisuraVendita = articolo.getUMDefaultVendita();
                            rigaOmf.setRiferimentoUMPrezzo(RiferimentoUmPrezzo.MAGAZZINO);
                            quantTotOmaggioOfferta = articolo.convertiUM(
                                quantTotOmaggioOfferta, offOmg.getUnitaMisura(),
                                unitaMisuraVendita, rigaOmf.getArticoloVersRichiesta()); // fix 10955
                        }

                        rigaOmf.setUMRif(unitaMisuraVendita);

                        rigaOmf.setQtaInUMVen(quantTotOmaggioOfferta);
                        UnitaMisura unitaMisuraPrm = articolo.getUMPrmMag();
                        rigaOmf.setUMPrm(unitaMisuraPrm);
                        if(unitaMisuraPrm != null)
                        {
                            if(unitaMisuraPrm.equals(unitaMisuraVendita))
                            {
                                rigaOmf.setQtaInUMPrm(quantTotOmaggioOfferta);
                            }
                            else
                            {
                                rigaOmf.setQtaInUMPrm(
                                    articolo.convertiUM(
                                    quantTotOmaggioOfferta, unitaMisuraVendita,
                                    unitaMisuraPrm, rigaOmf.getArticoloVersRichiesta())); // fix 10955
                            }
                        }
                        UnitaMisura unitaMisuraSec = articolo.getUMSecMag();
                        rigaOmf.setUMSec(unitaMisuraSec);
                        if(unitaMisuraSec != null)
                        {
                            if(unitaMisuraSec.equals(unitaMisuraVendita))
                            {
                                rigaOmf.setQtaInUMSec(quantTotOmaggioOfferta);
                            }
                            else
                            {
                                rigaOmf.setQtaInUMSec(
                                    articolo.convertiUM(
                                    quantTotOmaggioOfferta, unitaMisuraVendita,
                                    unitaMisuraSec, rigaOmf.getArticoloVersRichiesta())); // fix 10955
                            }
                        }
                        rigaOmf.setDataConsegnaRichiesta(getDataConsegnaRichiesta());
                        rigaOmf.setSettConsegnaRichiesta(getSettConsegnaRichiesta());
                        rigaOmf.setDataConsegnaConfermata(getDataConsegnaConfermata());
                        rigaOmf.setSettConsegnaConfermata(getSettConsegnaConfermata());

                        /**
                                     rigaOmf.setDataBolla(getDataBolla());
                                     rigaOmf.setDataFattura(getDataFattura());
                                     rigaOmf.setNumeroBolla(getNumeroBolla());
                                     rigaOmf.setNumeroFattura(getNumeroFattura());
                         */

                        //Scheda Prezzi/Sconti
                        rigaOmf.setIdListino(offOmg.getIdListino());
                        rigaOmf.setPrezzo(offOmg.getPrezzo());
                        rigaOmf.setPrezzoExtra(offOmg.getPrezzoExtra());
                        if(rigaOmf.getTipoRiga() != TipoRiga.OMAGGIO)
                        {
                            rigaOmf.setScontoArticolo1(lvs.getScontoArticolo1());
                            rigaOmf.setScontoArticolo2(lvs.getScontoArticolo2());
                            rigaOmf.setMaggiorazione(lvs.getMaggiorazione());
                            rigaOmf.setSconto(lvs.getSconto());
                        }
                        else
                        {
                            rigaOmf.setServizioCalcDatiVendita(false);
                        }
                        AssoggettamentoIVA assIva = offOmg.getAssoggettamentoIVA();
                        if(assIva == null)
                        {
                            assIva = articolo.getAssoggettamentoIVA();
                        }
                        if(assIva == null)
                        {
                            assIva = testata.getAssoggettamentoIVA();
                        }

                        rigaOmf.setAssoggettamentoIVA(assIva);
                        //Scheda Agenti
                        rigaOmf.setProvvigione1Agente(lvs.getProvvigioneAgente());
                        rigaOmf.setProvvigione1Subagente(lvs.getProvvigioneSubagente());
						//Fix Inizio 44522 
                        rigaOmf.setAgente(getAgente());
                        rigaOmf.setSubagente(getSubagente());                        
                        rigaOmf.setResponsabileVendite(getResponsabileVendite());
                        //Fix Fine 44522

                        ArticoloVersione ver = articolo.getVersioneAtDate(
                            getDataConsegnaRichiesta());
                        if(ver.getIdVersioneSaldi() != null)
                            rigaOmf.setArticoloVersSaldi(ver.getVersioneSaldi());
                        else
                            rigaOmf.setArticoloVersSaldi(ver);
                        rigaOmf.setArticoloVersRichiesta(ver);

                        // sequenza come quella della riga +1
                        rigaOmf.setSequenzaRiga(this.getSequenzaRiga() + 1);
                        rigaOmf.getDatiComuniEstesi().setStato(this.getDatiComuniEstesi().
                            getStato());

                        // dati relativi al listino
                        rigaOmf.setListinoPrezzi(this.getListinoPrezzi());
                        rigaOmf.setProvenienzaPrezzo(this.getProvenienzaPrezzo());
						rigaOmf.setDescrizioneExtArticolo(recuperaDescExtArticolo(rigaOmf.getCodiceCliente(), rigaOmf.getIdArticolo(), rigaOmf.getIdConfigurazione()));//Fix14727 RA
                    }
                }
            }
        }
    }

    //Fix 1918 - inizio
    /**
     * Ridefinizione.
     */
    public boolean isArticoloRigaArticoloDefaultCatalogo()
    {
        boolean ret = false;

        PersDatiVen pdv = PersDatiVen.getCurrentPersDatiVen();
        String idArtPdv = pdv.getIdArticoloPerCatalogoEst();
        if(idArtPdv != null)
        {
            String idArtRiga = getIdArticolo();
            ret = idArtRiga.equals(idArtPdv);
        }

        return ret;
    }

    /**
     * Controlla se l'articolo associato al catalogo è compatibile con i dati
     * dell'articolo presenti in riga.
     * Questo metodo non viene richiamato nella checkAll ma dal form action adapter.
     *
     * @see DocumentoVenditaRigaPrmCompletaFormActionAdapter#verificaArticoloCatalogoCompat
     * @see DocumentoVenditaRigaPrmRidottaFormActionAdapter#verificaArticoloCatalogoCompat
     */
    public ErrorMessage checkArticoloCatalogoCompat()
    {
        ErrorMessage em = null;

        CatalEsterno ce = getCatalogoEsterno();
        if(ce != null)
        {
            Articolo artCatal = ce.getArticolo();
            //Fix 2380 - inizio
            if(artCatal == null)
            {
                em = new ErrorMessage("THIP_BS347");
            }
            else
            {
                //Fix 2380 - fine
                //Verifica u.m. vendita
                List umvList = artCatal.getArticoloDatiVendita().getForcedUMSecondarie();
                Iterator iterUmvList = umvList.iterator();
                boolean okUMRif = false;
                while(iterUmvList.hasNext() && !okUMRif)
                {
                    UnitaMisura um = (UnitaMisura)iterUmvList.next();
                    okUMRif = um.getIdUnitaMisura().equals(getIdUMRif());
                }
                //Verifica u.m. primaria magazzino
                boolean okUMPrmMag = artCatal.getUMPrmMag().equals(getUMPrm());
                //Verifica u.m. secondaria magazzino
                boolean okUMSecMag = true;
                if(artCatal.getUMSecMag() != null && getUMSec() != null)
                {
                    okUMSecMag = artCatal.getUMSecMag().equals(getUMSec());
                }

                //Fix 16267 MZ - inizio
                //if (!okUMRif || !okUMPrmMag || !okUMSecMag) {
                if (!okUMRif) {
                //Fix 16267 MZ - fine
                    em = new ErrorMessage("THIP_BS319");
                }
                //Fix 2380 - inizio
            }
            //Fix 2380 - fine
        }

        return em;
    }

    /**
     * Controlla che non sia stato scelto un catalogo esterno a cui non è atato
     * associato alcun articolo.
     */
    public ErrorMessage checkCatalogo()
    {
        ErrorMessage em = null;

        CatalEsterno ce = getCatalogoEsterno();
        if(ce != null && ce.getArticolo() == null)
        {
            em = new ErrorMessage("THIP_BS321");
        }

        return em;
    }

    /**
     * Assegna alla riga l'articolo del catalogo
     */
    public void impostaArticoloCatalogo()
    {
        /**
         * @todo: da implementare
         */

        /*
            CatalEsterno ce = getCatalogoEsterno();
            if (ce != null) {
              Articolo artCatal = ce.getArticolo();
              setArticolo(artCatal);
              setConfigurazione(artCatal.getConfigurazioneStd());
              setStatoAvanzamento(StatoAvanzamento.DEFINITIVO);
              setServizioCalcDatiVendita(true);
            }
         */
    }

    //Fix 1918 - fine

    //Fix 2029 - inizio
    /**
     * Completamento dati form in NEW
     */
    public void completaBO()
    {
        super.completaBO();

        CausaleRigaDocVen causale = getCausaleRiga();
        if(causale != null)
        {
            setNonFatturare(causale.isNonFatturare());
        }
        // Fix 3016
        if(this.getTestata() != null)
        {
            Cantiere can = ((DocumentoVendita)this.getTestata()).getCantiereTestata();
            if(can != null && this.getTipoRiga() == TipoRiga.MERCE)
            {
                this.setConCantiere(true);
            }
        }
        // Fine fix 3016
        // Fix 8913 - Inizio
        isBOCompleted = true;
        // Fix 8913 - Fine
    }

    //Fix 2029 - fine

    // fix 2567 ini spostati metodi in DocumentoVenRigaLottoPrm
    /*
       //ini fix 2543
       public List convalida(List errorList) {
      super.convalida(errorList);
      ErrorMessage err = GenDocCollaudoHandler.crea().elaboraDocumentoCollaudo(this, true);
      if (errorList != null && err != null) {
        errorList.add(0, err);
      }
      return errorList;
       }

       public int regressione() throws SQLException {
      int res = super.regressione();
      if (res >= 0) {
        ErrorMessage err = GenDocCollaudoHandler.crea().elaboraDocumentoCollaudo(this, false);
        if (err != null) {
          throw new ThipException(err);
        }
      }
      return res;
       }
     */
    // fix 2567 fine

    public GestibileInCollaudo getGestibileInCollaudo()
    {
        GestibileInCollaudo obj = null;
        DocumentoBase doc = this.getTestata();
        if(doc != null && doc instanceof GestibileInCollaudo)
        {
            obj = (GestibileInCollaudo)doc;
        }
        return obj;
    }

    public boolean isDaGestireInCollaudo()
    {
        // fix 2788 ini
        //boolean isOk = true;
        boolean isOk = false;
        // fix 2788 fine
        //if(((DocumentoVendita)this.getTestata()).isDocumentoDiReso())  // Fix 11420
        if(((DocumentoVendita)this.getTestata()).isDocumentoCollaudoDaGenerare())  // Fix 11420
        {
            if(this.getCausaleRiga().getGestioneCollaudo() ==
               Collaudo.ARTICOLO_COLLAUDO)
            {
                isOk = this.getArticolo().getArticoloDatiQualita().getControlloQualita();
            }
            else if(this.getCausaleRiga().getGestioneCollaudo() == Collaudo.SEMPRE)
            {
                isOk = true;
            }
        }
        return isOk;
    }

    public String getAnnoOrdine()
    {
        String s = null;
        if(this.getRigaOrdine() != null)
        {
            s = this.getRigaOrdine().getAnnoDocumento();
        }
        return s;
    }

    public String getNumeroOrdine()
    {
        String s = null;
        if(this.getRigaOrdine() != null)
        {
            s = this.getRigaOrdine().getNumeroDocumento();
        }
        return s;
    }

    public Integer getNumeroRigaOrdine()
    {
        Integer i = null;
        if(this.getRigaOrdine() != null)
        {
            i = this.getRigaOrdine().getNumeroRigaDocumento();
        }
        return i;
    }

    public Integer getDettaglioRigaOrdine()
    {
        Integer i = null;
        if(this.getRigaOrdine() != null)
        {
            i = this.getDettaglioRigaDocumento();
        }
        return i;
    }

    //fine fix 2543



    //Fix 2563 - inizio
    /**
     * Ridefinizione
     */
    public void cambiaArticolo(
        Articolo articolo,
        Configurazione config,
        boolean recuperaDatiVenAcq)
    {
        datiArticolo.setParIntestatario(getIdClienteFatturazione());
        datiArticolo.setParIdListino(getIdListino());
        datiArticolo.setParQtaUMRif(getServizioQta().getQuantitaInUMRif().toString());

        if(getIdAgente() != null)
        {
            datiArticolo.setParIdAgente(getIdAgente());
            if(getProvvigione1Agente() != null)
            {
                datiArticolo.setParProvvigione1Agente(getProvvigione1Agente().toString());
            }
        }
        if(getIdAgenteSub() != null)
        {
            datiArticolo.setParIdSubagente(getIdAgenteSub());
            if(getProvvigione1Subagente() != null)
            {
                datiArticolo.setParProvvigione1Subagente(getProvvigione1Subagente().
                    toString());
            }
        }
        String idModPag = ((DocumentoOrdineTestata)getTestata()).
            getIdModPagamento();
        if(idModPag != null)
        {
            datiArticolo.setParIdModPagamento(idModPag);
        }
        super.cambiaArticolo(articolo, config, recuperaDatiVenAcq);
		//Fix14727 Inizio RA
        try{
          setDescrizioneExtArticolo(recuperaDescExtArticolo(getIdIntestatario(), getIdArticolo(), getIdConfigurazione()));
        }
        catch(Exception e){
            e.printStackTrace(Trace.excStream);
        }
        //Fix14727 Fine RA

        setIdAgente(((DatiArticoloRigaVendita)datiArticolo).getAgentiProvvigioni().
                    getIdAgente());
        // fix 20230
        BigDecimal provvAge = ((DatiArticoloRigaVendita)datiArticolo).
            getProvvigioneAgenteNumerico();
        if (provvAge!=null)
          provvAge = provvAge.setScale(2, BigDecimal.ROUND_HALF_UP);
        //setProvvigione1Agente(((DatiArticoloRigaVendita)datiArticolo).
        //                      getProvvigioneAgenteNumerico());
        setProvvigione1Agente(provvAge);
        // fine 20230
        setIdAgenteSub(((DatiArticoloRigaVendita)datiArticolo).
                       getAgentiProvvigioni().getIdSubagente());
        // Fix 20230
        provvAge = ((DatiArticoloRigaVendita)datiArticolo).
            getProvvigioneSubagenteNumerico();
        if (provvAge!=null)
          provvAge = provvAge.setScale(2, BigDecimal.ROUND_HALF_UP);
        //setProvvigione1Subagente(((DatiArticoloRigaVendita)datiArticolo).
        //                         getProvvigioneSubagenteNumerico());
        setProvvigione1Subagente(provvAge);
        // fine 20230
        try
        {
            //fix 5330 inizio
            UnitaMisura umRif = UnitaMisura.getUM(getIdUMRif());
            /*
                   String chiaveUM =
              KeyHelper.buildObjectKey(
                  new String[] {Azienda.getAziendaCorrente(), getIdUMRif()}
              );
                   UnitaMisura umRif = (UnitaMisura)
                PersistentObject.elementWithKey(
                    UnitaMisura.class, chiaveUM, PersistentObject.NO_LOCK
                );
             */
            //fix 5330 fine

            String idUMPrmMag = datiArticolo.getIdUMPrimaria();
            setIdUMPrm(idUMPrmMag);
            //fix 5330 inizio
            UnitaMisura umPrm = UnitaMisura.getUM(idUMPrmMag);
            /*
                   chiaveUM =
              KeyHelper.buildObjectKey(
                  new String[] {Azienda.getAziendaCorrente(), idUMPrmMag}
              );
                   UnitaMisura umPrm = (UnitaMisura)
                PersistentObject.elementWithKey(
                    UnitaMisura.class, chiaveUM, PersistentObject.NO_LOCK
                );
             */
            //fix 5330 fine
            setQtaInUMPrm(articolo.convertiUM(getQtaInUMVen(), umRif, umPrm, this.getArticoloVersRichiesta())); // fix 10955

            String idUMSecMag = datiArticolo.getIdUMSecondaria();
            if(idUMSecMag != null && !idUMSecMag.equals("")) //...FIX 7093
            {
                setIdUMSec(idUMSecMag);
                //fix 5330 inizio
                UnitaMisura umSec = UnitaMisura.getUM(idUMSecMag);
                /*
                         chiaveUM =
                    KeyHelper.buildObjectKey(
                    new String[] {Azienda.getAziendaCorrente(), idUMSecMag}
                   );
                         UnitaMisura umSec = (UnitaMisura)
                    PersistentObject.elementWithKey(
                        UnitaMisura.class, chiaveUM, PersistentObject.NO_LOCK
                    );
                 */
                //fix 5330 fine
                setQtaInUMSec(articolo.convertiUM(getQtaInUMVen(), umRif, umSec, this.getArticoloVersRichiesta())); // fix 10955
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace(Trace.excStream);
        }

        if(recuperaDatiVenAcq)
        {
            try
            {
                calcolaDatiVendita((DocumentoVendita)getTestata());
            }
            catch(Exception ex)
            {

            }
        }
    }

    //Fix 2563 - fine


    //Fix 2844 - inizio
    /**
     * Ridefinizione
     */
    public ContenitoreRiga istanziaContenitore()
    {
        return(ContenitoreVenRigaPrm)Factory.createObject(ContenitoreVenRigaPrm.class);
    }

    //Fix 2844 - fine

    // fix 2921 ini
    public boolean isResoFornitoreDaTrasferire()
    {
        return false;
    }

    // fix 2921 fine
    // Fix 3212
    public void setTipoModello(ModproEsplosione esplosione)
    {
        esplosione.setTipiModello(new char[]
                                  {ModelloProduttivoPO.PRODUZIONE});
    }

    //Fine fix 3212

    //Fix 3197 - inizio
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

    protected void modificaProvv2Agente() throws SQLException
    {
        Articolo articolo = getArticolo();
        String idLineaProdotto = articolo.getIdLineaProdotto();
        String idMacroFamiglia = articolo.getIdMacroFamiglia();
        String idSubFamiglia = articolo.getIdSubFamiglia();
        String idMicroFamiglia = articolo.getIdMicroFamiglia();

//MG FIX 4348
        PersDatiVen pdv = PersDatiVen.getCurrentPersDatiVen();
        BigDecimal sconto = null;
        if(pdv.getGestionePvgSuScalaSconti()) { //27616
	BigDecimal provvSuPrezzoExtra = calcoloProvvigioniSuPrezzoExtra();//Fix 13515

        if(pdv.getScontiEsaminati() == PersDatiVen.SCONTI_TESTATA_RIGA)
            sconto =
                RicercaCondizioniDiVendita.calcoloScontoDaScontiRiga(
                getPrcScontoIntestatario(),
                getPrcScontoModalita(),
                getScontoModalita(),
                //getScontoArticolo1(),
                getValue(getScontoArticolo1(),provvSuPrezzoExtra),//Fix 13515
                //Fix 26145 - inizio
//                getScontoArticolo2(),
                getScontoArticolo2CalcoloScontoScalaSconti(),
                //Fix 26145 - fine
                //Fix 24299 - inizio
//              getMaggiorazione(),
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
//                getScontoArticolo2(),
                getScontoArticolo2CalcoloScontoScalaSconti(),
                //Fix 26145 - fine
                //Fix 24299 - inizio
//              getMaggiorazione(),
                getMaggiorazioneCalcoloScontoScalaSconti(),
                //Fix 24299 - fine
                getSconto(),
                2
                );
        
        sconto = getScontoProvv2Pers(sconto);	//Fix 28653
        
        } //27616
//MG FIX 4348

//MG FIX 10750 inizio
     // Fix 34787 inizio 
     String idcli = getCodiceCliente();
     if(this.getReperimentoPrezzi()==TipoReperimento.CLIENTE_FATTURAZIONE || this.getReperimentoPrezzi()==TipoReperimento.DA_ORDINE_CLIENTE_FATTURAZIONE)
    	idcli = this.getIdClienteFatturazione();
    //Fix 34787 Fine
     
    //if (isServeRicalProvvAg() || isServeRicalProvvSubag()) { //Fix 25214 PM
    //if (isServeRicalProvvAg() || isServeRicalProvvSubag()  || !isOnDB()) { //Fix 25214 PM//Fix 26599
      if (isServeRicalProvvAg() || isServeRicalProvvSubag()  || isRicalProvvAgSubag()) { //Fix 26599
      if (condVen == null)
        recuperaCondizioniVendita((DocumentoVendita)this.getTestata());
    }
//MG FIX 10750 fine

    //if(isServeRicalProvvAg()) //Fix 25214 PM
    //if(isServeRicalProvvAg() || !isOnDB()) //Fix 25214 PM//Fix 26599
      if(isServeRicalProvvAg() || isRicalProvvAgSubag()) //Fix 25214 PM//Fix 26599
    {
      BigDecimal provv2 = null; //27616
      	if(pdv.getGestionePvgSuScalaSconti()) { //27616      	
      	
      provv2 =
          AgentiScontiProvv.getProvvigioneDaSconto(
          Azienda.getAziendaCorrente(),
          getIdAgente(),
          //getCodiceCliente(),//Fix 22229 // Fix 34787
          idcli,//Fix 34787
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
    		if(condVen != null)//Fix 31168
    			provv2 = condVen.getProvvigioneAgente2();
    	}
    	//27616 fine
//Fix 3738 (aggiunto controllo su null) - inizio
      if(provv2 != null)
      {
        setProvvigione2Agente(provv2);
      }
//Fix 3738 - fine
    }

    //if(isServeRicalProvvSubag()) //Fix 25214 PM
    //if(isServeRicalProvvSubag() || !isOnDB()) //Fix 25214 PM//Fix 26599
    if(isServeRicalProvvSubag() || isRicalProvvAgSubag()) //Fix 26599
    {
        BigDecimal provv2 = null; //27616
    	if(pdv.getGestionePvgSuScalaSconti()) { //27616
    		
    	provv2 =
          AgentiScontiProvv.getProvvigioneDaSconto(
          Azienda.getAziendaCorrente(),
          getIdAgenteSub(),
          //getCodiceCliente(),//Fix 22229 // Fix 34787
          idcli,//Fix 34787
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
    		if(condVen != null)//Fix 31168
    			provv2 = condVen.getProvvigioneSubagente2();
    	}
    	//27616 fine
//Fix 3738 (aggiunto controllo su null) - inizio
      if(provv2 != null)
      {
        setProvvigione2Subagente(provv2);
      }
//Fix 3738 - fine
    }
    }

//Fix 3197 - fine

    //...FIX 3187 inizio
    /**
     * checkQuadraturaLotti
     * @return ErrorMessage
     */
    public ErrorMessage checkQuadraturaLotti()
    {
        if(!isOnDB())
        {
        	if (this.getTestata() != null) { // Fix 25106
            char tipoDoc = ((DocumentoVendita)this.getTestata()).
                getTipoDocVenPerGestMM();
            if(tipoDoc == DocumentoVendita.TD_VENDITA ||
               tipoDoc == DocumentoVendita.TD_SPE_CTO_TRASF)
            {
                //...Controllo che la creazione automatica sia impostata
                boolean ok = identificaLotto();
                if(ok)
                    return null;
            }
//MG FIX 4656
            if(tipoDoc == DocumentoVendita.TD_RIC_CTO_TRASF)
            {
                boolean ok = ProposizioneAutLotto.proposizioneAutomaticaAttiva(getArticolo(), PersDatiMagazzino.TIPO_VEN_CTO_TRASF,
                    ProposizioneAutLotto.CREA_DA_DOCUMENTO);
                if(ok)
                    return null;
            }
//MG FIX 4656
            //Fix 16032 inizio
            if(tipoDoc == DocumentoVendita.TD_VENDITA_RESO)
            {
              boolean ok = identificazioneAutomaticaND(getArticolo());
              if(ok)
                    return null;
            }
            //Fix 16032 fine
            //Fix 27337 inizio
            if((getRigheLotto() == null || getRigheLotto().isEmpty()) && isControlloLottoDummyDaEscludere())
            	 return null;
            //Fix 27337 fine
        	} // Fix 25106
        }
        return super.checkQuadraturaLotti();
    }

    //...FIX 3187 fine

    /**
     * creaLottiAutomatici (da ridefinire negli eredi)
     */
    protected void creaLottiAutomatici()
    {
        //...Controllo se sono in un documento non di reso
        char tipoDoc = ((DocumentoVendita)this.getTestata()).
            getTipoDocVenPerGestMM();
        if(tipoDoc == DocumentoVendita.TD_VENDITA ||
           tipoDoc == DocumentoVendita.TD_SPE_CTO_TRASF)
        {
            //...Controllo che la creazione automatica sia impostata
            boolean ok = identificaLotto();
            if(ok)
                proponiLotti(PersDatiMagazzino.TIPO_VEN,
                             ProposizioneAutLotto.CREA_DA_DOCUMENTO, getIdMagazzino());
        }
//MG FIX 4656
        else if(tipoDoc == DocumentoVendita.TD_RIC_CTO_TRASF)
        {
            // se la causale di riga non è compatibile ...//
            //if(getCausaleRiga().isGestioneCali() || this.getCausaleRiga().isGestioneMM()) // Fix 13832
            //{ // Fix 13832
                //...Controllo che la creazione automatica sia impostata
                /*
                        boolean ok = identificaLotto();
                        if (ok)
                 */
        	if(getArticolo() != null && getArticolo().isArticLotto())//Fix 40200
                proponiLotti(PersDatiMagazzino.TIPO_VEN_CTO_TRASF,
                             ProposizioneAutLotto.CREA_DA_DOCUMENTO, getIdMagazzino());
            //} Fix 13832
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
//MG FIX 4656
    }

    /**
     * proponiLotto
     * @param tipo char
     * @return boolean
     */
    public boolean identificaLotto()
    {
    	//Fix 32832 Inizio
    	if(isDisattivaPropostaAutoLotti())
    		return false;
    	//Fix 32832 Fine
        return ProposizioneAutLotto.identificazioneAutomaticaAttiva(getArticolo());
    }

    /**
     * proponiLotti
     * @param tipo char
     * @param ambito char
     */
    public void proponiLotti(char tipo, char ambito, String idMagazzino)
    {
    	BigDecimal qta = new BigDecimal(0);
        boolean isPropostaEva = true;
        if(getStatoAvanzamento() == StatoAvanzamento.PROVVISORIO &&
           getQtaPropostaEvasione() != null)
        {
            qta = getQtaPropostaEvasione().getQuantitaInUMPrm();
        }
        if(getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO &&
           getQtaAttesaEvasione() != null)
        {
            qta = getQtaAttesaEvasione().getQuantitaInUMPrm();
            isPropostaEva = false;
        }

        //35639 inizio 
        /*ProposizioneAutLotto pal = ProposizioneAutLotto.creaProposizioneAutLotto(
            tipo,
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
        pal.setIdAzienda(getIdAzienda());//Fix13493 RA
        pal.setQtaAttesaEntrataDisp(isQtaAttesaEntrataDisp()); //Fix 19215 AYM
        */        
        
        if(tipo != PersDatiMagazzino.TIPO_VEN_CTO_TRASF)
        { //MG FIX 4656

        	//35639 inizio
        	List lottiOrig = new ArrayList();
            List lottiOrdine = new ArrayList();
            if(getRigaOrdine() != null) {
            	List lottiRig = getRigaOrdine().getRigheLotto();
            	for(int i = 0; i < lottiRig.size(); i++) {
                    OrdineVenditaRigaLotto lt = (OrdineVenditaRigaLotto)lottiRig.get(i);
                    if(!lt.getIdLotto().equals(Lotto.LOTTO_DUMMY)) {
                        lottiOrig.add(lt.getLotto());
                        //lottiOrdine.add(lt);
                    }
                }
                lottiOrdine = getImpegniLottiOrdine(true); //35639
            }        
            
        	List lottiAuto = new ArrayList();
        	ProposizioneAutLotto pal = getProposizioneAutLotto();
        	if(pal == null) {
        		pal = creaProposizioneAutLotto();
        	//35639 fine
        		pal.inizializzaProposizioneAutLotto(
        				tipo,
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
        		pal.setIdAzienda(getIdAzienda());//Fix13493 RA
        		pal.setQtaAttesaEntrataDisp(isQtaAttesaEntrataDisp()); //Fix 19215 AYM
        	
        		caricaLottiGiaAssegnati(pal); //35639
        		lottiAuto = pal.proponiLottiAutomatici(); 
        		pal.setSaldiLottiProposati(lottiAuto); //35639

        	//35639 inizio
        	}
        	else {
        		lottiAuto = pal.getSaldiLottiProposati();
        	}
        	//35639 fine
            BigDecimal controlloQta = qta;
            getRigheLotto().clear();
            HashMap giacenzaResiduaSulLotti = new HashMap(); //35639

            //...Se è stato creato un lotto automatico genero una riga lotto con quel lotto
            if(lottiAuto != null && !lottiAuto.isEmpty())
            {
            	List lottiPropostiDaOrdine = new ArrayList();	//35639
                for(int j = 0; j < lottiAuto.size(); j++)
                {
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
                    
                    
                    DocumentoVenRigaLottoPrm lotto = (DocumentoVenRigaLottoPrm)Factory.
                        createObject(DocumentoVenRigaLottoPrm.class);
                    lotto.setFather(this);
                    lotto.setIdArticolo(lt.getIdArticolo());
                    lotto.setIdLotto(lt.getIdLotto());
                    // Inizio 5749
                    /*
                               BigDecimal qtaLotto =
                      ProposizioneAutLotto.calcolaQtaDisponibileLotto(
                          tipo, lt, !lottiOrig.isEmpty(), lottiOrdine
                      );
                     */
                    // Inizio 6965
                    BigDecimal qtaLotto =   
                        pal.calcolaQtaGiacenzaNetta(
                        tipo, lt, !lottiOrig.isEmpty(), lottiOrdine
                        );
                    // Fine 6965
                    // Fine 5749
                    //35639 inizio  
                    BigDecimal qtaLottoAssegnato = getQtaLottoGiaAssegnato(lt);
                    qtaLotto = qtaLotto.subtract(qtaLottoAssegnato);
                    //35639 fine  
                    
                    if(qtaLotto.compareTo(ZERO_DEC) <= 0)
                    	continue;
                    
                    BigDecimal qtaRigaOrdineLotto = getQuantitaResiduoOrdineLotto(lt); //35639
                    if(controlloQta.compareTo(qtaLotto) >= 0)
                    {
                    	
                    	//35639 inizio
                    	BigDecimal qtaDaUsare = qtaLotto;
                    	if(qtaRigaOrdineLotto != null)
                    		qtaDaUsare = (qtaRigaOrdineLotto.compareTo(qtaLotto) > 0) ? qtaLotto : qtaRigaOrdineLotto;
                    	//35639 fine
                    	
                        controlloQta = controlloQta.subtract(qtaDaUsare);
                        if(isPropostaEva)
                        {
                            // Inizio 5749
                            lotto.getQtaPropostaEvasione().setQuantitaInUMPrm(qtaDaUsare);
                            lotto.getQtaPropostaEvasione().setQuantitaInUMRif(calcolaQtaUmRif(
                            		qtaDaUsare));
                            lotto.getQtaPropostaEvasione().setQuantitaInUMSec(calcolaQtaUmSec(
                            		qtaDaUsare));
                            // Fine 5749
                        }
                        else
                        {
                            lotto.getQtaAttesaEvasione().setQuantitaInUMPrm(qtaDaUsare);
                            lotto.getQtaAttesaEvasione().setQuantitaInUMRif(calcolaQtaUmRif(
                            		qtaDaUsare));
                            lotto.getQtaAttesaEvasione().setQuantitaInUMSec(calcolaQtaUmSec(
                            		qtaDaUsare));
                        }
                        assegnaQtaLotto(lt, qtaDaUsare); //35639
                        giacenzaResiduaSulLotti.put(lt, qtaLotto.subtract(qtaDaUsare)); //35639 
                    }
                    else
                    {
                    	//35639 inizio
                    	BigDecimal qtaDaUsare = controlloQta;
                    	if(qtaRigaOrdineLotto != null)
                    		qtaDaUsare = (qtaRigaOrdineLotto.compareTo(controlloQta) > 0) ? controlloQta : qtaRigaOrdineLotto;
                    	//35639 fine
                    	
                        if(isPropostaEva)
                        {
                            lotto.getQtaPropostaEvasione().setQuantitaInUMPrm(qtaDaUsare);
                            lotto.getQtaPropostaEvasione().setQuantitaInUMRif(calcolaQtaUmRif(
                            		qtaDaUsare));
                            lotto.getQtaPropostaEvasione().setQuantitaInUMSec(calcolaQtaUmSec(
                            		qtaDaUsare));
                        }
                        else
                        {
                            lotto.getQtaAttesaEvasione().setQuantitaInUMPrm(qtaDaUsare);
                            lotto.getQtaAttesaEvasione().setQuantitaInUMRif(calcolaQtaUmRif(
                            		qtaDaUsare));
                            lotto.getQtaAttesaEvasione().setQuantitaInUMSec(calcolaQtaUmSec(
                            		qtaDaUsare));
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
                
                //Fix 23515 inizio
                //Fix 22850 PM >
                /*if (getRigheLotto().size() == 1 && !isRicalcoloQtaFattoreConv())
                {
                	 DocumentoVenRigaLottoPrm lotto = (DocumentoVenRigaLottoPrm)getRigheLotto().get(0);
                	 if(isPropostaEva)
                	 {
                		 if (lotto.getQtaPropostaEvasione().getQuantitaInUMPrm().compareTo(getQtaPropostaEvasione().getQuantitaInUMPrm())  == 0)
                			 lotto.setQtaPropostaEvasione(getQtaPropostaEvasione());
                	 }
                	 else
                	 {
                		 if (lotto.getQtaAttesaEvasione().getQuantitaInUMPrm().compareTo(getQtaAttesaEvasione().getQuantitaInUMPrm())  == 0)
                			 lotto.setQtaAttesaEvasione(getQtaAttesaEvasione());
                	 }
                }*/
                // Fix 22850 PM <
                
                gestioneRigheConNotRicalQtaFattoreConv();
                //Fix 23515 fine
            }
            // (5282)Inizio 5350 : Non è stato trovato un lotto
            else
            {
                //if(getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO)//Fix 27337
            	if(getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO && !isControlloLottoDummyDaEscludere())//Fix 27337
                {
//MG FIX 10932 inizio
//                    throw new ThipRuntimeException(new ErrorMessage("THIP_BS213", false));
                   // throw new ThipRuntimeException(new ErrorMessage("THIP300203", false)); //Fix 23604  
                      throw new ThipRuntimeException(new ErrorMessage("THIP300203", false, getIdArticolo()));//Fix 23604
//MG FIX 10932 fine
                }
            }
            // (5282)Fine 5350
        }
        else
        {
        	//35639 inizio
        	List lottiOrig = new ArrayList();
            List lottiOrdine = new ArrayList();
            if(getRigaOrdine() != null) {
            	List lottiRig = getRigaOrdine().getRigheLotto();
            	for(int i = 0; i < lottiRig.size(); i++) {
                    OrdineVenditaRigaLotto lt = (OrdineVenditaRigaLotto)lottiRig.get(i);
                    if(!lt.getIdLotto().equals(Lotto.LOTTO_DUMMY)) {
                        lottiOrig.add(lt.getLotto());
                        lottiOrdine.add(lt);
                    }
                }
            }        
            
        	ProposizioneAutLotto pal = creaProposizioneAutLotto();
        	pal.inizializzaProposizioneAutLotto(
                tipo,
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
        	pal.setIdAzienda(getIdAzienda());//Fix13493 RA
        	pal.setQtaAttesaEntrataDisp(isQtaAttesaEntrataDisp()); //Fix 19215 AYM        	
        	//35639 fine      	
        	impostaDatiPerBene(pal);//Fix 40598        	
            List lottiAuto = pal.creaLottiAutomatici();
            //Fix 39531 Inizio
        	QuantitaInUMRif qtaTotLotti = new QuantitaInUMRif();
        	qtaTotLotti.azzera();		
        	QuantitaInUMRif qtaUnitario = new QuantitaInUMRif(new BigDecimal(1),new BigDecimal(0),new BigDecimal(0));
        	if(pal.isGenAutomaticaLottiUnitari())
        	{
	        	qtaUnitario.setQuantitaInUMRif(getArticolo().convertiUMArrotondate(qtaUnitario.getQuantitaInUMPrm(), getUMPrm(), getUMRif(), getArticoloVersRichiesta()));
	        	if(getUMSec() != null)
	        		qtaUnitario.setQuantitaInUMSec(getArticolo().convertiUMArrotondate(qtaUnitario.getQuantitaInUMPrm(), getUMPrm(), getUMSec(), getArticoloVersRichiesta()));
        	}
        	//Fix 39531 Fine  
            //...Se è stato creato un lotto automatico genero una riga lotto con quel lotto
            if(lottiAuto != null && !lottiAuto.isEmpty())
            {
                getRigheLotto().clear();
                for(int j = 0; j < lottiAuto.size(); j++)
                {
                    Lotto lt = (Lotto)lottiAuto.get(j);
                    DocumentoVenRigaLotto lotto = (DocumentoVenRigaLottoPrm)Factory.createObject(DocumentoVenRigaLottoPrm.class);
                    lotto.setFather(this);
                    lotto.setIdArticolo(lt.getCodiceArticolo());
                    lotto.setIdLotto(lt.getCodiceLotto());
                    //Fix 39531 Inizio
                    if(pal.isGenAutomaticaLottiUnitari())
                    {
                    	if(getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO)
                            lotto.setQtaAttesaEvasione(lotto.getQtaAttesaEvasione().add(qtaUnitario));
                    	else
                            lotto.setQtaPropostaEvasione(lotto.getQtaPropostaEvasione().add(qtaUnitario));
                    	
                    	qtaTotLotti = qtaTotLotti.add(qtaUnitario);
                    }
                    else
                    {
                    //Fix 39531 Fine
                    lotto.setQtaAttesaEvasione(getQtaAttesaEvasione());
                    lotto.setQtaPropostaEvasione(getQtaPropostaEvasione());
                    }//Fix 39531
                    getRigheLotto().add(lotto);
                }
              	//Fix 39531 fine
                if (pal.isGenAutomaticaLottiUnitari() && getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO && qtaTotLotti.compareTo(getServizioQta()) != 0)
                		forsaProvisorio();
            	//Fix 39531 fine
                
            }
          //Fix 39531 Inizio
            else{
                if (getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO){
                	forsaProvisorio();//Fix 39531
                }
              }
          //Fix 39531 Fine
        }
        setProposizioneAutLotto(null); //38908
    }

    //...FIX 3187 fine
    //Fix 23515 inizio
    protected void gestioneRigheConNotRicalQtaFattoreConv() {
      //if (isRicalcoloQtaFattoreConv())  //Fix 26371 PM
      //  return; //Fix 26371 PM
      boolean gestitoUMSec = getUMSec()!= null;
      List lotti = getRigheLotto();
      //Fix 22850 PM >
      if (lotti.size() == 1) {
        DocumentoVenRigaLottoPrm lotto = (DocumentoVenRigaLottoPrm) lotti.get(0);
        if (isPropostaEvasione()) {
          if (lotto.getQtaPropostaEvasione().getQuantitaInUMPrm().compareTo(getQtaPropostaEvasione().getQuantitaInUMPrm()) == 0)
          {//35639
            lotto.getQtaPropostaEvasione().setQuantitaInUMPrm(getQtaPropostaEvasione().getQuantitaInUMPrm());
            lotto.getQtaPropostaEvasione().setQuantitaInUMRif(getQtaPropostaEvasione().getQuantitaInUMRif());
            if(gestitoUMSec)
              lotto.getQtaPropostaEvasione().setQuantitaInUMSec(getQtaPropostaEvasione().getQuantitaInUMSec());
          }//35639
        }
        else {
          if (lotto.getQtaAttesaEvasione().getQuantitaInUMPrm().compareTo(getQtaAttesaEvasione().getQuantitaInUMPrm()) == 0)
          {//35639
            lotto.getQtaAttesaEvasione().setQuantitaInUMPrm(getQtaAttesaEvasione().getQuantitaInUMPrm());
            lotto.getQtaAttesaEvasione().setQuantitaInUMRif(getQtaAttesaEvasione().getQuantitaInUMRif());
            if(gestitoUMSec)
              lotto.getQtaAttesaEvasione().setQuantitaInUMSec(getQtaAttesaEvasione().getQuantitaInUMSec());
          }//35639
        }
      }
      //Fix 22850 PM <
      else {
        int numLottiConQtaRifZero = 0;
        int numLottiConQtaSecZero = 0;
        BigDecimal qtaUMPrmLotti = ZERO_DEC;
        BigDecimal qtaUMRifLotti = ZERO_DEC;
        BigDecimal qtaUMSecLotti = ZERO_DEC;
        Iterator iter = lotti.iterator();
        while (iter.hasNext()) {
          DocumentoVenRigaLottoPrm lotto = (DocumentoVenRigaLottoPrm) iter.next();
          qtaUMPrmLotti = qtaUMPrmLotti.add(getQtaLottoServizio(lotto).getQuantitaInUMPrm());
          qtaUMRifLotti = qtaUMRifLotti.add(getQtaLottoServizio(lotto).getQuantitaInUMRif());
          if (getQtaLottoServizio(lotto).getQuantitaInUMRif().compareTo(ZERO_DEC) == 0)
            numLottiConQtaRifZero++;
          if (gestitoUMSec) {
            qtaUMSecLotti = qtaUMSecLotti.add(getQtaLottoServizio(lotto).getQuantitaInUMSec());
            if (getQtaLottoServizio(lotto).getQuantitaInUMSec().compareTo(ZERO_DEC) == 0)
              numLottiConQtaSecZero++;
          }
        }
        if (qtaUMPrmLotti.compareTo(getQtaRigaServizio().getQuantitaInUMPrm()) == 0){
          correttoQtaInUMRif(qtaUMRifLotti, numLottiConQtaRifZero);
          if(gestitoUMSec)
            correttoQtaInUMSec(qtaUMSecLotti, numLottiConQtaSecZero);
        }
      }
    }

  protected void correttoQtaInUMRif(BigDecimal qtaUMRifLotti, int numLottiConQtaRifZero) {
    BigDecimal deltaUMRif = ZERO_DEC;
    List lotti = this.getRigheLotto();

    deltaUMRif = getQtaRigaServizio().getQuantitaInUMRif().subtract(qtaUMRifLotti);
    if(deltaUMRif.compareTo(ZERO_DEC) > 0){
      if (numLottiConQtaRifZero == 0) {
        QuantitaInUMRif qtaServ = getQtaLottoServizio((DocumentoVenRigaLottoPrm)lotti.get(0));
        qtaServ.setQuantitaInUMRif(qtaServ.getQuantitaInUMRif().add(deltaUMRif));
      }
      else{
        Iterator iter = lotti.iterator();
        BigDecimal qtaDaAggiungere = deltaUMRif.divide(new BigDecimal(numLottiConQtaRifZero),BigDecimal.ROUND_HALF_UP);

        qtaDaAggiungere = roundQuantita(qtaDaAggiungere,getUMRif());
        BigDecimal qtaLottoPerVerifica = ZERO_DEC;
        while(iter.hasNext() && qtaLottoPerVerifica.compareTo(deltaUMRif)<0){
          DocumentoVenRigaLottoPrm lotto = (DocumentoVenRigaLottoPrm)iter.next();
          if (getQtaLottoServizio(lotto).getQuantitaInUMRif().compareTo(ZERO_DEC)==0)
          {
            BigDecimal rest = deltaUMRif.subtract(qtaLottoPerVerifica);
            BigDecimal qta = qtaDaAggiungere;
            if (qtaDaAggiungere.compareTo(rest) > 0)
              qta = rest;

            getQtaLottoServizio(lotto).setQuantitaInUMRif(getQtaLottoServizio(lotto).getQuantitaInUMRif().add(qta));
            qtaLottoPerVerifica = qtaLottoPerVerifica.add(qta);

          }
        }
      }
    }
    //Fix 26371 PM
    else if (deltaUMRif.compareTo(ZERO_DEC) < 0)
    {
    	Iterator iter = lotti.iterator();
    	BigDecimal delta = deltaUMRif.abs();
      
        while(iter.hasNext() && delta.compareTo(ZERO_DEC) > 0)
    	{
    		DocumentoVenRigaLottoPrm lotto = (DocumentoVenRigaLottoPrm)iter.next();
    		if (getQtaLottoServizio(lotto).getQuantitaInUMRif().compareTo(delta) > 0)
    		{
    			getQtaLottoServizio(lotto).setQuantitaInUMRif(getQtaLottoServizio(lotto).getQuantitaInUMRif().subtract(delta));
    			delta = ZERO_DEC;
    		}
    		else if (getQtaLottoServizio(lotto).getQuantitaInUMRif().compareTo(delta) == 0)
    		{
    			BigDecimal uno = new BigDecimal("1.00");
    			getQtaLottoServizio(lotto).setQuantitaInUMRif(uno);
    			delta = uno;
    		}
    		else if (getQtaLottoServizio(lotto).getQuantitaInUMRif().compareTo(delta) < 0)
    		{
    			BigDecimal uno = new BigDecimal("1.00");
    			delta = delta.subtract(getQtaLottoServizio(lotto).getQuantitaInUMRif()).add(uno);
    			getQtaLottoServizio(lotto).setQuantitaInUMRif(uno);
    		}    			
    	}    		
    
    	if (delta.compareTo(ZERO_DEC) > 0)
    	{
    		iter = lotti.iterator(); 
            BigDecimal qtaLottoPerVerifica = ZERO_DEC;
    		BigDecimal qtaDaSottrarre = delta.divide(new BigDecimal(lotti.size()), BigDecimal.ROUND_HALF_UP);
    		while(iter.hasNext() && qtaLottoPerVerifica.compareTo(delta) < 0)
    		{
    			DocumentoVenRigaLottoPrm lotto = (DocumentoVenRigaLottoPrm)iter.next();
    			if (getQtaLottoServizio(lotto).getQuantitaInUMRif().compareTo(qtaDaSottrarre) > 0)
    			{
    				BigDecimal rest = delta.subtract(qtaLottoPerVerifica);
    				BigDecimal qta = qtaDaSottrarre;
    				if (qtaDaSottrarre.compareTo(rest) > 0)
    					qta = rest;

    				getQtaLottoServizio(lotto).setQuantitaInUMRif(getQtaLottoServizio(lotto).getQuantitaInUMRif().subtract(qta));
    				qtaLottoPerVerifica = qtaLottoPerVerifica.add(qta);

    			}
    		}  	
    	}
    	
    }
    //Fix 26371 PM

  }

  protected void correttoQtaInUMSec(BigDecimal qtaUMSecLotti, int numLottiConQtaSecZero) {
    BigDecimal deltaUMSec = ZERO_DEC;
    List lotti = this.getRigheLotto();

    deltaUMSec = getQtaRigaServizio().getQuantitaInUMSec().subtract(qtaUMSecLotti);
    if (deltaUMSec.compareTo(ZERO_DEC) > 0) {
      if (numLottiConQtaSecZero == 0) {
        QuantitaInUMRif qtaServ = getQtaLottoServizio((DocumentoVenRigaLottoPrm) lotti.get(0));
        qtaServ.setQuantitaInUMSec(qtaServ.getQuantitaInUMSec().add(deltaUMSec));
      }
      else {
        Iterator iter = lotti.iterator();
        BigDecimal qtaDaAggiungere = deltaUMSec.divide(new BigDecimal(numLottiConQtaSecZero), BigDecimal.ROUND_HALF_UP);

        qtaDaAggiungere = roundQuantita(qtaDaAggiungere, getUMSec());
        BigDecimal qtaLottoPerVerifica = ZERO_DEC;
        while (iter.hasNext() && qtaLottoPerVerifica.compareTo(deltaUMSec) < 0) {
          DocumentoVenRigaLottoPrm lotto = (DocumentoVenRigaLottoPrm) iter.next();
          if (getQtaLottoServizio(lotto).getQuantitaInUMSec().compareTo(ZERO_DEC) == 0) {
            BigDecimal rest = deltaUMSec.subtract(qtaLottoPerVerifica);
            BigDecimal qta = qtaDaAggiungere;
            if (qtaDaAggiungere.compareTo(rest) > 0)
              qta = rest;

            getQtaLottoServizio(lotto).setQuantitaInUMSec(getQtaLottoServizio(lotto).getQuantitaInUMSec().add(qta));
            qtaLottoPerVerifica = qtaLottoPerVerifica.add(qta);
          }
        }
      }
    }
    //Fix 26371 PM
    else if (deltaUMSec.compareTo(ZERO_DEC) < 0)
    {
    	Iterator iter = lotti.iterator();
    	BigDecimal delta = deltaUMSec.abs();
    	while(iter.hasNext() && delta.compareTo(ZERO_DEC) > 0)
    	{
    		DocumentoVenRigaLottoPrm lotto = (DocumentoVenRigaLottoPrm)iter.next();
    		if (getQtaLottoServizio(lotto).getQuantitaInUMSec().compareTo(delta) > 0)
    		{
    			getQtaLottoServizio(lotto).setQuantitaInUMSec(getQtaLottoServizio(lotto).getQuantitaInUMSec().subtract(delta));
    			delta = ZERO_DEC;
    		}
    		else if (getQtaLottoServizio(lotto).getQuantitaInUMSec().compareTo(delta) == 0)
    		{
    			BigDecimal uno = new BigDecimal("1.00");
    			getQtaLottoServizio(lotto).setQuantitaInUMSec(uno);
    			delta = uno;
    		}
    		else if (getQtaLottoServizio(lotto).getQuantitaInUMSec().compareTo(delta) < 0)
    		{
    			BigDecimal uno = new BigDecimal("1.00");
    			delta = delta.subtract(getQtaLottoServizio(lotto).getQuantitaInUMSec()).add(uno);
    			getQtaLottoServizio(lotto).setQuantitaInUMSec(uno);
    		}
    	}
    	if (delta.compareTo(ZERO_DEC) > 0)
    	{
    		iter = lotti.iterator(); 
            BigDecimal qtaLottoPerVerifica = ZERO_DEC;
    		BigDecimal qtaDaSottrarre = delta.divide(new BigDecimal(lotti.size()), BigDecimal.ROUND_HALF_UP);
    		while(iter.hasNext() && qtaLottoPerVerifica.compareTo(delta) < 0)
    		{
    			DocumentoVenRigaLottoPrm lotto = (DocumentoVenRigaLottoPrm)iter.next();
    			if (getQtaLottoServizio(lotto).getQuantitaInUMSec().compareTo(qtaDaSottrarre) > 0)
    			{
    				BigDecimal rest = delta.subtract(qtaLottoPerVerifica);
    				BigDecimal qta = qtaDaSottrarre;
    				if (qtaDaSottrarre.compareTo(rest) > 0)
    					qta = rest;

    				getQtaLottoServizio(lotto).setQuantitaInUMRif(getQtaLottoServizio(lotto).getQuantitaInUMSec().subtract(qta));
    				qtaLottoPerVerifica = qtaLottoPerVerifica.add(qta);

    			}
    		}  	
    	}    	
    }
    //Fix 26371 PM
    
  }

  protected BigDecimal roundQuantita(BigDecimal qta, UnitaMisura um){
    if (qta == null)
      return null;

    BigDecimal qtaArrot = qta;
    if (um != null && um.getQtaIntera()){
      qtaArrot = qta.setScale(0,BigDecimal.ROUND_UP);
    }
    else if (this.getArticolo().isQtaIntera() && um != null && um.getKey().equals(getUMPrmKey())) {
      qtaArrot = qta.setScale(0,BigDecimal.ROUND_UP);
    }
    return qtaArrot;
  }

  protected QuantitaInUMRif getQtaLottoServizio(DocumentoVenRigaLottoPrm lotto) {
    if (isPropostaEvasione())
      return lotto.getQtaPropostaEvasione();
    else
      return lotto.getQtaAttesaEvasione();
  }

  protected QuantitaInUMRif getQtaRigaServizio() {
    if (isPropostaEvasione())
      return getQtaPropostaEvasione();
    else
      return getQtaAttesaEvasione();
  }

  protected boolean isPropostaEvasione() {
    if (getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO && getQtaAttesaEvasione() != null)
      return false;
    return true;
  }
  //Fix 23515 fine
    // fix 3016
    protected void calcolaDatiVendita(DocumentoVendita testata) throws
        SQLException
    {
        // Inizio 8393
        if (isGestioneContrattiVendita()){
          String keyContratto = KeyHelper.buildObjectKey(new String[]{getIdAzienda(), getRAnnoOrd(), getRNumeroOrd()});
          CondizioniDiVendita condVenCnt = RecuperaDatiVendita.getCondizioniContrattoVendita(keyContratto);
          if (condVenCnt != null){
            impostaCondizioniVenditaDaContratto(condVenCnt);
            return;
          }
        }
        // Fine 8393

        SchemaPrezzo schema = this.getArticolo().getArticoloDatiVendita().
            getSchemaPrzVen();
        if(schema != null &&
           schema.getTipoSchemaPrz() == SchemaPrezzo.TIPO_SCH_ACQ_VEN &&
           getProvenienzaPrezzo() != TipoRigaRicerca.MANUALE && this.isConCantiere())
        {
            java.sql.Date dataValid = null;
            //Fix 3770 - inizio
            //PersDatiVen pda = (PersDatiVen)
            //	PersDatiVen.elementWithKey(
            //    PersDatiVen.class,
            //    Azienda.getAziendaCorrente(),
            //    PersistentObject.NO_LOCK
            //  );
            PersDatiVen pda = PersDatiVen.getCurrentPersDatiVen();
            //Fix 3770 - fine
            char tipoDataPrezziSconti = testata.getCliente().
                getRifDataPerPrezzoSconti();
            if(tipoDataPrezziSconti == RifDataPrzScn.DA_CONDIZIONI_GENERALI)
            {
                tipoDataPrezziSconti = pda.getTipoDataPrezziSconti();
            }
            switch(tipoDataPrezziSconti)
            {
                case RifDataPrzScn.DATA_ORDINE:
                    dataValid = TimeUtils.getDate(testata.getDataDocumento());
                    break;
                case RifDataPrzScn.DATA_CONSEGNA:
                    dataValid = TimeUtils.getDate(this.getDataConsegnaConfermata());
                    break;
            }
            //Fix 5270 BP ini...
            //RicercaPrezziExtraVendita ricerca = new RicercaPrezziExtraVendita();
            RicercaPrezziExtraVendita ricerca = (RicercaPrezziExtraVendita)
                Factory.createObject(RicercaPrezziExtraVendita.class);
            //Fix 5270 BP fine.
            BigDecimal qtaRif = this.getServizioQta().getQuantitaInUMRif();
            if(this.isRigaReso())
            {
                qtaRif = new BigDecimal(0);
            }

            BigDecimal qtaPrm = this.getServizioQta().getQuantitaInUMPrm();
            if(this.isRigaReso())
            {
                qtaPrm = new BigDecimal(0);
            }

            BigDecimal qtaSec = this.getServizioQta().getQuantitaInUMSec();
            if(this.isRigaReso())
            {
                qtaSec = new BigDecimal(0);
            }
            CondizioniVEPrezziExtra condAcq = null;

            try
            {
                ModalitaConsegna mod = testata.getModalitaConsegna();
                CausaleDocumentoVendita causale = testata.getCausale(); //...FIX04356 - DZ
                String sMod = "0";
                if(mod != null)
                {
                    sMod = String.valueOf(mod.getTipoConsegna());
                }
                HashMap map = ricerca.ricercaPrezziExtraLaterizi(this.getIdAzienda(),
                    testata.getIdCliente(), testata.getIdDivisione(), causale.isDocCtoTrasformazione(), //...FIX04356 - DZ
                    testata.getIdValuta(), this.getIdArticolo(),
                    this.getIdConfigurazione(),
                    this.getIdUMRif(), this.getIdUMPrm(), qtaRif,
                    qtaPrm, TimeUtils.getDate(dataValid), null, null, null, false,
                    this.getIdUMSec(), qtaSec, testata.getRAnnoCantiere(),
                    testata.getRNumeroCantiere(), sMod);
                condAcq = (CondizioniVEPrezziExtra)map.get("CondVen");
                if(condAcq.getNumRigaCantiere() != null)
                {
                    Agente age = (Agente)map.get("thAgente");
                    Agente sage = (Agente)map.get("thSubagente");
                    if(age != null)
                    {
                        this.setProvvigione1Agente((BigDecimal)map.get("thProvvAgente1"));
                        this.setProvvigione2Agente((BigDecimal)map.get("thProvvAgente2"));
                        this.setAgente(age);
                    }
                    else
                    {
                        this.setProvvigione1Agente(null);
                        this.setProvvigione2Agente(null);
                        this.setAgente(null);
                    }
                    if(sage != null)
                    {
                        this.setProvvigione1Subagente((BigDecimal)map.get(
                            "thProvvSubAgente1"));
                        this.setProvvigione2Subagente((BigDecimal)map.get(
                            "thProvvSubAgente2"));
                        this.setSubagente(sage);
                    }
                    else
                    {
                        this.setProvvigione1Subagente(null);
                        this.setProvvigione2Subagente(null);
                        this.setSubagente(null);
                    }
                    //Fix 43361
                    Sconto sc = (Sconto) map.get("thSconto");
                    if (sc != null)
                  	  this.setSconto(sc);
                    this.setScontoArticolo1((BigDecimal) map.get("thScontoArticolo1"));
                    this.setScontoArticolo2((BigDecimal) map.get("thScontoArticolo2"));
                    this.setMaggiorazione((BigDecimal) map.get("thMaggiorazione"));
                    //Fix 43361
                }
            }
            catch(SQLException ex)
            {
                condAcq = null;
            }

            if(condAcq != null)
            {
                DocOrdRigaPrezziExtra rigaPrezzi = this.getRigaPrezziExtra();
                if(rigaPrezzi == null)
                {
                    this.istanziaRigaPrezziExtra();
                }
                rigaPrezzi = this.getRigaPrezziExtra();
                rigaPrezzi.aggiornaDatiDaCondVen(condAcq);
                this.setPrezzo(condAcq.getPrezzoRiga());
                this.setProvenienzaPrezzo(TipoRigaRicerca.CONTRATTO);
                this.setRiferimentoUMPrezzo(condAcq.getRiferimentoUMPrezzo());

                if((this.getIdAgente() != null || this.getIdAgenteSub() != null) &&
                   condAcq.getNumRigaCantiere() == null)
                {
                    CondizioniDiVendita cV = new CondizioniDiVendita();
                    cV.setRArticolo(this.getIdArticolo());
                    cV.setRSubAgente(this.getIdAgenteSub());
                    cV.setRAgente(this.getIdAgente());
                    cV.setRValuta(testata.getIdValuta());
                    cV.setRUnitaMisura(this.getIdUMRif());
                    cV.setRCliente(testata.getIdCliente());
                    cV.setIdAzienda(this.getIdAzienda());
                    cV.setRConfigurazione(this.getIdConfigurazione());
                    cV.setDataValidita(dataValid);
                    cV.setMaggiorazione(this.getMaggiorazione());
                    cV.setSconto(this.getSconto());
                    cV.setScontoArticolo1(this.getScontoArticolo1());
                    cV.setScontoArticolo2(this.getScontoArticolo2());
                    cV.setProvvigioneAgente1(this.getProvvigione1Agente());
                    cV.setProvvigioneSubagente1(this.getProvvigione1Subagente());
                    cV.setQuantita(this.getServizioQta().getQuantitaInUMRif());
                    cV.setPrezzo(condAcq.getPrezzoRiga());
                    cV.setRModalitaPagamento(testata.getIdModPagamento());

                    RicercaCondizioniDiVendita ric = new RicercaCondizioniDiVendita();
                    ric.setCondizioniDiVendita(cV);
                    ric.aggiornaProvvigioni();

                    if(this.getIdAgente() != null)
                    {
                        this.setProvvigione2Agente(cV.getProvvigioneAgente2());
                    }
                    if(this.getIdAgenteSub() != null)
                    {
                        this.setProvvigione2Subagente(cV.getProvvigioneSubagente2());
                    }
                }
            }
        }
        else
        {
            super.calcolaDatiVendita(testata);
        }
    }

    protected List salvoConvalida(List errorRiga)
    {
        if(errorRiga == null || errorRiga.isEmpty())
        {
            if(this.isConCantiere())
            {
                try
                {
                    int rc1 = aggiornaRigaCantiere();
                    if(rc1 < 0)
                    {
                        if(errorRiga == null)
                            errorRiga = new ArrayList();
                        errorRiga.add(new ErrorMessage("THIP110450"));
                    }
                }
                catch(SQLException ex)
                {
                    if(errorRiga == null)
                        errorRiga = new ArrayList();
                    errorRiga.add(new ErrorMessage("THIP110450"));
                    ex.printStackTrace(Trace.excStream);
                }

            }
        }
        return super.salvoConvalida(errorRiga);
    }

    public int aggiornaRigaCantiere() throws SQLException
    {
        int rc = 0;
        DocRigaPrezziExtraVendita rigaPrezzi = (DocRigaPrezziExtraVendita)this.
            getRigaPrezziExtra();
        if(rigaPrezzi != null)
        {
            Cantiere can = rigaPrezzi.getCantiere();
            if(can != null && rigaPrezzi.getRRigaCantiere() != null)
            {
                CantiereRiga canRiga = rigaPrezzi.getCantiereRiga();
                if(canRiga != null)
                {
                    QuantitaInUMRif qtaSpe = canRiga.getQuantitaSpedita();
                    if(this.getIdUMRif().equals(canRiga.getIdUMPrvVen()))
                    {
                        qtaSpe = qtaSpe.add(this.getQtaSpedita());
                    }
                    else if(this.getIdUMPrm().equals(canRiga.getIdUMPrvVen()))
                    {
                        qtaSpe = qtaSpe.add(new QuantitaInUMRif(this.getQtaSpedita().
                            getQuantitaInUMPrm(), this.getQtaSpedita().getQuantitaInUMPrm(),
                            this.getQtaSpedita().getQuantitaInUMSec()));
                    }
                    else if(this.getIdUMSec() != null &&
                            this.getIdUMSec().equals(canRiga.getIdUMPrvVen()))
                    {
                        qtaSpe = qtaSpe.add(new QuantitaInUMRif(this.getQtaSpedita().
                            getQuantitaInUMSec(), this.getQtaSpedita().getQuantitaInUMPrm(),
                            this.getQtaSpedita().getQuantitaInUMSec()));
                    }
                    else
                    {
                        BigDecimal qtaNewRif = this.getArticolo().convertiUM(this.
                            getQtaSpedita().getQuantitaInUMRif(), this.getUMRif(),
                            canRiga.getUMPrvVen(), this.getArticoloVersRichiesta()); // fix 10955
                        qtaSpe = qtaSpe.add(new QuantitaInUMRif(qtaNewRif,
                            this.getQtaSpedita().getQuantitaInUMPrm(),
                            this.getQtaSpedita().getQuantitaInUMSec()));
                    }
                    canRiga.setQuantitaSpedita(qtaSpe);
                    canRiga.aggiornaQuantitaResidua();
                    rc = canRiga.save();
                }
            }
            else if(can != null)
            {
                CantiereRiga canRiga = (CantiereRiga)Factory.createObject(CantiereRiga.class);
                canRiga.setIdAzienda(this.getIdAzienda());
                canRiga.setCostoUnitario(this.getCostoUnitario());
                canRiga.setIdAgente(this.getIdAgente());
                canRiga.setIdArticolo(this.getIdArticolo());
                canRiga.setDescrizioneArticoloPer(this.getArticolo().
                                                  getDescrizioneArticoloNLS().
                                                  getDescrizione());
                canRiga.setDescrizioneArticolo(this.getArticolo().
                                               getDescrizioneArticoloNLS().
                                               getDescrizione());
                canRiga.setIdAssoggettamentoIVA(this.getIdAssogIVA());
                canRiga.setIdCentroCosto(this.getIdCentroCosto());
                canRiga.setIdCommessa(this.getIdCommessa());
                canRiga.setIdConfigurazione(this.getIdConfigurazione());
                canRiga.setIdGrpCntCa(this.getIdGrpCntCa());
                canRiga.setIdMagazzino(this.getIdMagazzino());
                canRiga.setIdSchemaPrzVen(this.getArticolo().getArticoloDatiVendita().
                                          getIdSchemaPrzVen());
                canRiga.setIdSconto(this.getIdSconto());
                canRiga.setIdScontoModalita(this.getIdScontoMod());
                canRiga.setIdSubagente(this.getIdAgenteSub());
                canRiga.setIdUMPrm(this.getIdUMPrm());
                canRiga.setIdUMPrvVen(this.getIdUMRif());
                canRiga.setIdUMSec(this.getIdUMSec());
                canRiga.setIdUnitaMisura(this.getIdUMRif());
                canRiga.setIdVersioneRcs(this.getIdVersioneRcs());
                canRiga.setIdVersioneSal(this.getIdVersioneSal());
                canRiga.setMaggiorazione(this.getMaggiorazione());
                canRiga.setPrcScontoCliente(this.getPrcScontoIntestatario());
                canRiga.setPrcScontoModalita(this.getPrcScontoModalita());
                canRiga.setPrezzoBase(rigaPrezzi.getPrezziExtra().getPrezzoBase());
                canRiga.setPrezzoExt01(rigaPrezzi.getPrezziExtra().getPrezzoExt01());
                canRiga.setPrezzoExt02(rigaPrezzi.getPrezziExtra().getPrezzoExt02());
                canRiga.setPrezzoExt03(rigaPrezzi.getPrezziExtra().getPrezzoExt03());
                canRiga.setPrezzoExt04(rigaPrezzi.getPrezziExtra().getPrezzoExt04());
                canRiga.setPrezzoExt05(rigaPrezzi.getPrezziExtra().getPrezzoExt05());
                canRiga.setPrezzoExt06(rigaPrezzi.getPrezziExtra().getPrezzoExt06());
                canRiga.setPrezzoExt07(rigaPrezzi.getPrezziExtra().getPrezzoExt07());
                canRiga.setPrezzoExt08(rigaPrezzi.getPrezziExtra().getPrezzoExt08());
                canRiga.setPrezzoExt09(rigaPrezzi.getPrezziExtra().getPrezzoExt09());
                canRiga.setPrezzoExt10(rigaPrezzi.getPrezziExtra().getPrezzoExt10());
                canRiga.setPrezzoRiga(rigaPrezzi.getPrezziExtra().getPrezzoRiga());
                canRiga.setPrezzoXProv(rigaPrezzi.getPrezziExtra().getPrezzoXProv());
                canRiga.setPrezzoXStat1(rigaPrezzi.getPrezziExtra().getPrezzoXStat1());
                canRiga.setPrezzoXStat2(rigaPrezzi.getPrezziExtra().getPrezzoXStat2());
                canRiga.setPrezzoXStat3(rigaPrezzi.getPrezziExtra().getPrezzoXStat3());
                canRiga.setPrezzoXStat4(rigaPrezzi.getPrezziExtra().getPrezzoXStat4());
                canRiga.setProvvigione1Agente(this.getProvvigione1Agente());
                canRiga.setProvvigione1Subagente(this.getProvvigione1Subagente());
                canRiga.setProvvigione2Agente(this.getProvvigione2Agente());
                canRiga.setProvvigione2Subagente(this.getProvvigione2Subagente());
                canRiga.setRiferimentoUMPrezzo(this.getRiferimentoUMPrezzo());
                canRiga.setScontoArticolo1(this.getScontoArticolo1());
                canRiga.setScontoArticolo2(this.getScontoArticolo2());
                canRiga.setSequenzaRiga(this.getSequenzaRiga());
                canRiga.setSpecializzazioneRiga(this.getSpecializzazioneRiga());
                canRiga.setStatoAvanzamento(this.getStatoAvanzamento());
                canRiga.setStatoEvasione(StatoEvasione.SALDATO);
                canRiga.setTestata(can);
                canRiga.setTipoCostoRiferimento(this.getTipoCostoRiferimento());
                canRiga.setTipoPrezzo(this.getTipoPrezzo());
                canRiga.setTipoRigaCan(CantiereRiga.TP_RIGA_CAN_AGGIUNTA);
                canRiga.setValiditaContratto(CantiereRiga.VALIDITA_PER_QTA_CNT);
                canRiga.getDataValidita().setDBStartDate(can.getDataValidita().
                    getDBStartDate());
                canRiga.getDataValidita().setDBEndDate(DateRange.cvDBEndDate);
                canRiga.setQtaInUMPrmMag(this.getQtaSpedita().getQuantitaInUMPrm());
                canRiga.setQtaInUMSecMag(this.getQtaSpedita().getQuantitaInUMSec());
                canRiga.setQtaInUMVen(this.getQtaSpedita().getQuantitaInUMRif());
                try
                {
                    canRiga.getQuantitaSpedita().setEqual(this.getQtaSpedita());
                }
                catch(CopyException ex)
                {
                    ex.printStackTrace(Trace.excStream);
                }
                canRiga.setQuantitaResidua(new QuantitaInUMRif(new BigDecimal(0),
                    new BigDecimal(0), new BigDecimal(0)));
                rc = canRiga.save();
                // Devo
                if(rc >= 0)
                {
                    rigaPrezzi.setRRigaCantiere(canRiga.getNumeroRigaDocumento());

                }
            }
        }
        return rc;
    }

    public int regressione() throws SQLException
    {
        int rc = super.regressione();
        if(rc >= 0)
        {
            if(this.isConCantiere())
            {
                int rc1 = pulisciCantiereRiga();
                if(rc1 < 0)
                {
                    throw new ThipException(new ErrorMessage("THIP110450"));
                }
            }
        }
        return rc;
    }

    public int pulisciCantiereRiga() throws SQLException
    {
        int rc = 0;
        DocRigaPrezziExtraVendita rigaPrezzi = (DocRigaPrezziExtraVendita)this.
            getRigaPrezziExtra();
        if(rigaPrezzi != null)
        {
            CantiereRiga canRiga = rigaPrezzi.getCantiereRiga();
            if(canRiga != null)
            {
                QuantitaInUMRif qtaSpe = canRiga.getQuantitaSpedita();
                QuantitaInUMRif qtaAte = this.getQtaAttesaEvasione();
                if(this.getIdUMRif().equals(canRiga.getIdUMPrvVen()))
                {
                    qtaSpe = qtaSpe.subtract(qtaAte);
                }
                else if(this.getIdUMPrm().equals(canRiga.getIdUMPrvVen()))
                {
                    qtaSpe = qtaSpe.subtract(new QuantitaInUMRif(qtaAte.
                        getQuantitaInUMPrm(), qtaAte.getQuantitaInUMPrm(),
                        qtaAte.getQuantitaInUMSec()));
                }
                else if(this.getIdUMSec() != null &&
                        this.getIdUMSec().equals(canRiga.getIdUMPrvVen()))
                {
                    qtaSpe = qtaSpe.subtract(new QuantitaInUMRif(qtaAte.
                        getQuantitaInUMSec(), qtaAte.getQuantitaInUMPrm(),
                        qtaAte.getQuantitaInUMSec()));
                }
                else
                {
                    BigDecimal qtaNewRif = this.getArticolo().convertiUM(qtaAte.
                        getQuantitaInUMRif(), this.getUMRif(), canRiga.getUMPrvVen(), this.getArticoloVersRichiesta()); // fix 10955
                    qtaSpe = qtaSpe.subtract(new QuantitaInUMRif(qtaNewRif,
                        qtaAte.getQuantitaInUMPrm(), qtaAte.getQuantitaInUMSec()));
                }
                canRiga.setQuantitaSpedita(qtaSpe);
                canRiga.aggiornaQuantitaResidua();
                rc = canRiga.save();
            }
        }
        return rc;
    }

    // fine fix 3016

    //Fix 3230 - inizio
    public void setGeneraRigheSecondarie(boolean b)
    {
        this.iGeneraRigheSecondarie = b;
    }

    public boolean isGeneraRigheSecondarie()
    {
        return iGeneraRigheSecondarie;
    }

    /**
     * FIX04607 - DZ
     * @param b boolean
     */
    public void setDisabilitaRigheSecondarieForCM(boolean b)
    {
        iDisabilitaRigheSecondarieForCM = b;
    }

    /**
     * FIX04607 - DZ
     * @return boolean
     */
    public boolean isDisabilitaRigheSecondarieForCM()
    {
        return iDisabilitaRigheSecondarieForCM;
    }

    /**
     * Ridefinizione.
     */
    protected DocumentoOrdineRiga getRigaDestinazionePerCopia()
    {
        return(DocumentoVenRigaPrm)Factory.createObject(DocumentoVenRigaPrm.class);
    }

    /**
     * Ridefinizione.
     */
    public DocumentoOrdineRiga copiaRiga(DocumentoOrdineTestata docDest, SpecificheCopiaDocumento spec) throws CopyException
    {
        DocumentoVenRigaPrm riga = (DocumentoVenRigaPrm)(super.copiaRiga(docDest,
            spec));
        if(riga != null)
        {
            //Copia righe secondarie
            riga.setGeneraRigheSecondarie(false);
            List righe = getRigheSecondarie();
            for(Iterator iter = righe.iterator(); iter.hasNext(); )
            {
                DocumentoVenRigaSec rigaSec = (DocumentoVenRigaSec)iter.next();
                //Fix 5110 - inizio
                //rigaSec.setRigaPrimaria(riga);	//Fix 3953
                spec.setRigaPrimariaDest(riga);
                //DocumentoOrdineRiga rigaCopiata = rigaSec.copiaRiga((DocumentoOrdineTestata) riga.getTestata(), spec);
                DocumentoOrdineRiga rigaCopiata = rigaSec.copiaRiga(docDest, spec);
                //Fix 5110 - fine
                if(rigaCopiata != null)
                {
                    riga.getRigheSecondarie().add(rigaCopiata);
                }
            }
            // PAOLA
            if (spec instanceof SpecificheCopiaDocVen){
              SpecificheCopiaDocVen specV = (SpecificheCopiaDocVen)spec;
              if (specV.isDaProposta()){
                riga.setRigaDaProposta(this);
              }
            }
            // fine PAOLA
        }
        return riga;
    }

    // Inizio 4486
    //Fix 3230 - fine
    //Fix 3769 BP ini...
    public BigDecimal getPrezzoRiferimento()
    {
        DocRigaPrezziExtraVendita prezziExtra = (DocRigaPrezziExtraVendita)this.getRigaPrezziExtra();
        if(prezziExtra != null)
        {
            return prezziExtra.getPrezzoRiferimento();
        }
        return null;
    }

    public void setPrezzoRiferimento(BigDecimal b)
    {
        DocRigaPrezziExtraVendita prezziExtra = (DocRigaPrezziExtraVendita)this.getRigaPrezziExtra();
        if(prezziExtra != null)
        {
            prezziExtra.setPrezzoRiferimento(b);
        }
    }

//Fix 3769 BP fine.
    // Fine 4486

    /**
     * FIX04607 - DZ
     * Aggiunto per anticipare il controllo sull'esistenza di modello/distinta
     * per la generazione delle righe secondarie in presenza di articolo kit.
     * Questo controllo veniva fatto durante la save e l'error message -warning- restituito tramite
     * ThipException, ma questo rendeva impossibile continuare il salvataggio della riga primaria.
     * @return ErrorMessage avviso non bloccante
     */
    protected ErrorMessage checkRigheSecondarie()
    {
        //Fix 4976 - inizio
        Articolo articolo = getArticolo();
        if(articolo != null)
        {
            //Fix 4976 - fine
            if((!isOnDB() && getTipoRiga() != TipoRiga.SPESE_MOV_VALORE &&
                (articolo.getTipoParte() == ArticoloDatiIdent.KIT_GEST ||
                 articolo.getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST))
               && !isDisabilitaRigheSecondarieForCM())
            {
                ModelloProduttivo modpro = null;
                boolean okModello = false;
                Stabilimento stab = null;

                //Fix 28159 inizio
                //stab = getMagazzino().getStabilimento();
                Magazzino mag = getMagazzino();
                if(mag != null)
                    stab = mag.getStabilimento();
                //Fix 28159 Fine
                stab = (stab == null) ? PersDatiGen.getCurrentPersDatiGen().getStabilimento() : stab;
                if(stab == null)
                    return new ErrorMessage("THIP110305");
                try
                {
                    modpro =
                        ModproEsplosione.trovaModelloProduttivo(
                        getIdAzienda(), articolo.getIdArticolo(),
                        stab.getIdStabilimento(), getDataConsegnaConfermata(), getIdCommessa(),
                        ModelloProduttivo.GENERICO, new char[]
                        {ModelloProduttivo.KIT}
                        );
                    okModello = modpro != null;
                }
                catch(SQLException ex)
                {
                    okModello = false;
                }

                if(!okModello)
                {
                    try
                    {
                        modpro = ModproEsplosione.trovaModelloProduttivo(getIdAzienda(), articolo.getIdArticolo(),
                            stab.getIdStabilimento(), getDataConsegnaConfermata(), getIdCommessa(),
                            ModelloProduttivo.GENERICO, new char[]
                            {ModelloProduttivo.PRODUZIONE});
                        okModello = modpro != null;
                    }
                    catch(SQLException ex)
                    {
                        okModello = false;
                    }
                }

                if(!okModello)
                {
                    try
                    {
                        List datiRigheKit = getEsplosioneNodo(articolo).getNodiFigli();
                        if(datiRigheKit.isEmpty())
                        {
                            setGeneraRigheSecondarie(false);
                            return new ErrorMessage("THIP_BS151");
                        }
                    }
                    catch(SQLException ex)
                    {
                        ex.printStackTrace(Trace.excStream);
                    }

                }
            }
        }
        return null;
    }

    //Fix 5634 - inizio
    /**
     * Ridefinizione
     */
    public void annullaOldRiga()
    {
        super.annullaOldRiga();
        List righeSec = getRigheSecondarie();
        Iterator iter = righeSec.iterator();
        while(iter.hasNext())
        {
            DocumentoOrdineRiga rigaSec = (DocumentoOrdineRiga)iter.next();
            rigaSec.annullaOldRiga();
        }
    }

    //Fix 5634 - fine


// Fix 6150 PM Inizio
    public void impostazioniPerCopiaRiga()
    {
        super.impostazioniPerCopiaRiga();
	    //Fix Inizio 41868
        /*
        if (!Utils.areEqual(getIdOldMagazzino(), getIdMagazzino()))
        {
            Iterator i = getRigheSecondarie().iterator();
            while(i.hasNext())
            {
               DocumentoVenditaRiga rigaSec = (DocumentoVenditaRiga)i.next();
               rigaSec.setIdMagazzino(getIdMagazzino());
               rigaSec.impostazioniPerCopiaRiga();
              
            }
        }*/

            Iterator i = getRigheSecondarie().iterator();
            while(i.hasNext())
            {
               DocumentoVenditaRiga rigaSec = (DocumentoVenditaRiga)i.next();
               if (!Utils.areEqual(getIdOldMagazzino(), getIdMagazzino()))               
            	   rigaSec.setIdMagazzino(getIdMagazzino());
               rigaSec.impostazioniPerCopiaRiga();
               if(getTestata() != null && !((DocumentoOrdineTestata)getTestata()).isInCopia())//Fix 41868
	               calcolaDateRigheSecondarie(rigaSec);
            }
          //Fix Fine 41868
    }
// Fix 6150 PM Fine
	//Fix Inizio 41868
    protected void calcolaDateRigheSecondarie(DocumentoVenditaRiga rigaSec) {

    	if (datiUguali(getOldDataConsegnaRichiesta(), getDataConsegnaRichiesta()) &&
    		datiUguali(getOldDataConsegnaConfermata(), getDataConsegnaConfermata())) 		
    		return;

          rigaSec.setDataConsegnaRichiesta(getDataConsegnaRichiesta());
          rigaSec.setDataConsegnaConfermata(getDataConsegnaConfermata());         
          rigaSec.setSettConsegnaRichiesta(getSettConsegnaRichiesta());
          rigaSec.setSettConsegnaConfermata(getSettConsegnaConfermata());     
    }
  //Fix Fine 41868

    // Inizio 6209
    public BigDecimal controllaDispUnicoLottoEffettivo(DocumentoOrdineRigaLotto rigaLotto) { // Fix 6920
      BigDecimal giacCalcLotto = ZERO_DEC;
      try {
        String idAzienda = getIdAzienda();
        String idArticolo = getIdArticolo();
        Integer idVersione = getIdVersioneSal();
        String idEsternoConfig = getIdEsternoConfig();
        String idLotto = rigaLotto.getIdLotto();
        String idMagazzino = getIdMagazzino();
        BigDecimal qtaLottoPrm = ZERO_DEC;
        if (getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO)
          qtaLottoPrm = rigaLotto.getQtaAttesaEvasione().getQuantitaInUMPrm();
        else
          qtaLottoPrm = rigaLotto.getQtaPropostaEvasione().getQuantitaInUMPrm();

        char tipo = PersDatiMagazzino.TIPO_VEN;
        List lottiOrdine = new ArrayList();

        String whereLtSald = LottiSaldiTM.ID_AZIENDA + " = '" + idAzienda + "' AND " +
        LottiSaldiTM.ID_MAGAZZINO + " = '" + idMagazzino + "' AND " +
        LottiSaldiTM.ID_ARTICOLO + " = '" +idArticolo + "' AND " +
        LottiSaldiTM.ID_VERSIONE + " = " + idVersione + " AND " +
        LottiSaldiTM.ID_OPERAZIONE + " = '" + SaldoMag.OPERAZIONE_DUMMY + "' AND " +
        LottiSaldiTM.ID_LOTTO + " = '" + idLotto + "' AND " +
        LottiSaldiTM.COD_CONFIG + " = '" + ProposizioneAutLotto.calcolaCodConfig(idEsternoConfig) + "'";

        Vector lottiSaldo = LottiSaldi.retrieveList(whereLtSald, "", false);
        LottiSaldi lottoSaldo = null;

        if(!lottiSaldo.isEmpty()) {
          lottoSaldo = (LottiSaldi)lottiSaldo.elementAt(0);
          // Nel caso di documenti deve essere reperita la giacenza netta
          // Inizio 6965
          //giacCalcLotto = ProposizioneAutLotto.getInstance().calcolaQtaGiacenzaNetta(tipo, lottoSaldo, !lottiOrdine.isEmpty(), lottiOrdine); //35639
          //giacCalcLotto = getProposizioneAutLotto().calcolaQtaGiacenzaNetta(tipo, lottoSaldo, !lottiOrdine.isEmpty(), lottiOrdine); //35639
          ProposizioneAutLotto propIns = (ProposizioneAutLotto) Factory.createObject(ProposizioneAutLotto.class); //37248
          giacCalcLotto = propIns.calcolaQtaGiacenzaNetta(tipo, lottoSaldo, !lottiOrdine.isEmpty(), lottiOrdine); //37248
          // Fine 6965

          String keyRigaOrdLotto = KeyHelper.buildObjectKey(new String[]{getIdAzienda(),
              getAnnoDocumento(),getNumeroDocumento(), getNumeroRigaDocumento().toString(), getIdArticolo(), rigaLotto.getIdLotto()});
            BigDecimal qtaRigaLottoPrmOld = getDocumentoVenRigaLottoPrm(keyRigaOrdLotto);
            if (qtaRigaLottoPrmOld != null)
              giacCalcLotto = giacCalcLotto.add(qtaRigaLottoPrmOld);
        }
      }
      catch (Exception ex) {
        ex.printStackTrace(Trace.excStream);
      }
      return giacCalcLotto;
    }
    // Fine 6209

    // Inizio 6321
    public boolean isUtenteAutorizzatoForzaturaPrelLotto(){
      //...Controlla l'autorizzazione sulla forzatura
   // Fix 18156 inizio
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
      }catch(SQLException ex){
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

    // Fine 6321

    // Inizio 6920
    public BigDecimal getDocumentoVenRigaLottoPrm(String key){
      BigDecimal qtaRigaLottoPrm = null;
      try{
        DocumentoVenRigaLottoPrm rigaDocLotto = (DocumentoVenRigaLottoPrm)Factory.createObject(DocumentoVenRigaLottoPrm.class);
        rigaDocLotto.setKey(key);
        if (rigaDocLotto.retrieve()){
          qtaRigaLottoPrm = rigaDocLotto.getServizioQta().getQuantitaInUMPrm();
        }
      }catch(Exception ex){
        ex.printStackTrace(Trace.excStream);
      }
      return qtaRigaLottoPrm;
    }

    /* Fix 12673 Inizio
    public DocumentoOrdineRigaLotto getUnicoLottoEffettivo(){
      DocumentoRigaLotto rigalotto = null;
      ArrayList listaLotti = (ArrayList)getRigheLotto();
      if (listaLotti.size() == 1){
        DocumentoRigaLotto rigalottoTmp  = (DocumentoRigaLotto)listaLotti.get(0);
        //Fix 5623 - inizio (aggiunta prima condizione)
        if (rigalottoTmp.getIdLotto() != null && !rigalottoTmp.getIdLotto().equals(LOTTO_DUMMY))
        //Fix 5623 - fine
          rigalotto = rigalottoTmp;
      }
      return rigalotto;
    }
 Fix 12673 Fine */
    // Fine 6920


//MG FIX 6754 inizio
    public boolean ricalcoloDatiVenditaPerFatturazione(DocumentoVendita testata, java.sql.Date dataFat) throws SQLException {
      super.ricalcoloDatiVenditaPerFatturazione(testata, dataFat);
      List righeSec = this.getRigheSecondarie();
      if (righeSec != null && !righeSec.isEmpty()) {
        Iterator iterSec = righeSec.iterator();
        while (iterSec.hasNext()) {
          DocumentoVenRigaSec rigaSec = (DocumentoVenRigaSec) iterSec.next();
          
          //Fix 23896 PM >
          //if (rigaSec.getSpecializzazioneRiga() == RIGA_SECONDARIA_DA_FATTURARE)
          char tipoParte = getArticolo().getTipoParte();
          char tipoCalcoloPrezzo = getArticolo().getTipoCalcPrzKit();
		  


          if (rigaSec.getSpecializzazioneRiga() == RIGA_SECONDARIA_DA_FATTURARE  ||
        	((tipoParte == ArticoloDatiIdent.KIT_NON_GEST || tipoParte == ArticoloDatiIdent.KIT_GEST) && tipoCalcoloPrezzo == ArticoloDatiVendita.DA_COMPONENTI))
          //Fix 23896 PM <
            rigaSec.ricalcoloDatiVenditaPerFatturazione(testata, dataFat);
        }
      }
      return true;
    }
//MG FIX 6754 fine

    //12755 - PJ - inizio
    protected void completaRigaSecDaMaterialeOrdSrv(DocumentoVenRigaSec rigaSec, AttivitaSrvMateriale materiale) {
    }
    protected void completaRigaSecDaRisorsaOrdSrv(DocumentoVenRigaSec rigaSec, AttivitaSrvRisorsa risorsa) {
    }
    //12755 - PJ - fine

    //Fix 6439 - inizio
    //Fix 9221: assegnazioni tramite id anzichè tramite oggetti
    protected void generaRigheSecDaMaterialiOrdSrv(int tipoRigaNlgSrv) {
      //Fix 37274 Inizio
      String utilizzaQtaPrelevataSuRigaSec = ParametroPsn.getValoreParametroPsn("std.servizio.UtilizzaQtaPrelevata", "UtilizzaQtaPrelevataSuRigaSec"); 
      BigDecimal zero = new BigDecimal("0.00"); 
      zero = Q6Calc.get().setScale(zero,2); 
      //Fix 37274 Fine
      int sequenza = 0;
      OrdineServizio ordSrv = getOrdineServizio();
      if (ordSrv != null) {		//Fix 7375
        List materiali = ordSrv.getMateriali();
        Iterator iter = materiali.iterator();
        while (iter.hasNext()) {
          AttivitaSrvMateriale materiale = (AttivitaSrvMateriale)iter.next();
          //Fix 10882 inizio
          char tipoMateriale = materiale.getTipoMateriale();
          CausaleRigaDocVen  cauRigaMerce = null;
          if (getTipoRiga() == TipoRigaDocumentoVendita.SERVIZIO_ASSIST_MANUT) {
            cauRigaMerce = getPrimaCausaleRigaMerce();
            //Fix 16242-19920 - inizio
//          if (tipoMateriale != AttivitaSrvMateriale.MATERIALE || cauRigaMerce == null) //Fix 16707
            if(bloccaGenerazRigheSecDaMaterialiOrdSrv(materiale, cauRigaMerce))
            //Fix 16242-19920 - fine

            if (tipoMateriale != AttivitaSrvMateriale.MATERIALE || cauRigaMerce == null) //Fix 16707
              continue;
          }
          //Fix 10882 fine
          DocumentoVenRigaSec rigaSec =
            (DocumentoVenRigaSec)Factory.createObject(DocumentoVenRigaSec.class);
          rigaSec.setIdAzienda(Azienda.getAziendaCorrente());

          //Dati Tab Generale
          rigaSec.setSequenzaRiga(++sequenza);
          //fix 10882 rigaSec.setIdCauRig(getIdCauRig());
          //Fix 16242-19920 - inizio
//        if(getTipoRiga() == TipoRigaDocumentoVendita.SERVIZIO_ASSIST_MANUT && cauRigaMerce != null) {
          if (isCauRigheSecDaMatOrdSrvMerce(cauRigaMerce)) {
          //Fix 16242-19920 - fine
            rigaSec.setIdCauRig(cauRigaMerce.getIdCausaleRigaDocumentoVen());
            rigaSec.setTipoRiga(cauRigaMerce.getTipoRiga());
            rigaSec.setIdMagazzino(materiale.getIdMagazzinoPrl());
            rigaSec.setEscludiDaDichIntento(cauRigaMerce.isEscludiDaDichIntento());//Fix 26939
          }
          else {
            rigaSec.setIdCauRig(getIdCauRig());
            rigaSec.setTipoRiga(getTipoRiga());
            rigaSec.setEscludiDaDichIntento(isEscludiDaDichIntento());//Fix 26939
          }
          //end 10882
          rigaSec.setAnnoOrdineServizio(getAnnoOrdineServizio());
          rigaSec.setNumeroOrdineServizio(getNumeroOrdineServizio());
          rigaSec.setIdBene(materiale.getIdBene());
          //Fix 7858 - inizio
//          rigaSec.setArticolo(materiale.getArticolo());
          Articolo articoloMat = materiale.getArticolo();
          rigaSec.setIdArticolo(articoloMat.getIdArticolo());
          if (articoloMat != null) {
            rigaSec.setDescrizioneArticolo(articoloMat.getDescrizioneArticoloNLS().getDescrizione());
            rigaSec.setDescrizioneExtArticolo(materiale.getDescrizioneExtArticolo());//Fix 38696;
          }
          //Fix 7858 - fine
          rigaSec.setIdVersioneRcs(materiale.getIdVersione());
          rigaSec.setIdConfigurazione(materiale.getIdConfigurazione());
          rigaSec.setCoefficienteImpiego(materiale.getCoeffImpiegoPerDocVen());
          if (tipoRigaNlgSrv == NLG_SRV_TIPO_FATTURA) {
            if (tipoMateriale == AttivitaSrvMateriale.DOTAZIONE) {
        	  //Fix 37274 Inizio
              if (utilizzaQtaPrelevataSuRigaSec != null && utilizzaQtaPrelevataSuRigaSec.equals("Y") && materiale.getQtaPrelevataUMPrm() != null && !materiale.getQtaPrelevataUMPrm().equals(zero))
                  rigaSec.setQtaInUMVen(materiale.getQtaPrelevataUMPrm());
              else
              //Fix 37274 Fine
                  rigaSec.setQtaInUMVen(materiale.getQtaRichiestaUMPrm());
              rigaSec.setUMRif(materiale.getUMPrmMag());
              //Fix 37274 Inizio
              if (utilizzaQtaPrelevataSuRigaSec != null && utilizzaQtaPrelevataSuRigaSec.equals("Y") && materiale.getQtaPrelevataUMPrm() != null && !materiale.getQtaPrelevataUMPrm().equals(zero))
                  rigaSec.setQtaInUMPrm(materiale.getQtaPrelevataUMPrm());
              else
              //Fix 37274 Fine
                 rigaSec.setQtaInUMPrm(materiale.getQtaRichiestaUMPrm());
              rigaSec.setUMPrm(materiale.getUMPrmMag());
              if (materiale.getUMSecMag() != null) {
                //Fix 37274 Inizio
                if (utilizzaQtaPrelevataSuRigaSec != null && utilizzaQtaPrelevataSuRigaSec.equals("Y") && materiale.getQtaPrelevataUMSec() != null && !materiale.getQtaPrelevataUMSec().equals(zero))
                    rigaSec.setQtaInUMSec(materiale.getQtaPrelevataUMSec());
                else
                //Fix 37274 Fine
                    rigaSec.setQtaInUMSec(materiale.getQtaRichiestaUMSec());
                rigaSec.setUMSec(materiale.getUMSecMag());
              }
              rigaSec.setBloccoRicalcoloQtaComp(true);
            }
            else {
              //Fix 33309 Inizio
              PersDatiServizi pds = PersDatiServizi.getCurrentPersDatiServizi();
              AnagraficaBeni bene = ordSrv.getBene();
              Orario orario = null;
              if (bene != null && bene.getNOOrarioUtilizzo() != null)
                orario = bene.getNOOrarioUtilizzo();
              else
                orario = pds.getOrario();
              Calendario cal = pds.getCalendario();
              CalcoloPeriodoAddebito cpa = new CalcoloPeriodoAddebito();
              BigDecimal qta1 = new BigDecimal(0);
              BigDecimal qta2 = new BigDecimal(0);
              try {
                  //Fix 40160 Inizio
            	  //qta1 = cpa.calcoloQuantitaAddebito(getDataInizioAttivContratto(), getOraInizioAttivContratto(), getDataFineAttivContratto(), getOraFineAttivContratto(), orario, cal,  materiale.getIdUMVen(), ordSrv.isAddebitoFestivita(), ordSrv.isAddebitoSabato(), ordSrv.isAddebitoDomenica());
				  //qta2 = cpa.calcoloQuantitaAddebito(getDataInizioAttivContratto(), getOraInizioAttivContratto(), getDataFineAttivContratto(), getOraFineAttivContratto(), orario, cal,  materiale.getIdUMPrmMag(), ordSrv.isAddebitoFestivita(), ordSrv.isAddebitoSabato(), ordSrv.isAddebitoDomenica(
            	  if (ordSrv.getModoAddebito().isAddebitoMeseEffettivo() && UtilTemp.getPersDatiServiziUM(materiale.getIdUMVen()).getTipoUnitaMisura() == PersDatiServiziUM.MESE) {
            		  qta1 = cpa.calcoloQuantitaAddebitoMeseEffettivo( getDataInizioAttivContratto(), getDataInizioAttivContratto(), getDataInizioAttivContratto(), getDataFineAttivContratto(), materiale.getIdUMVen(), ordSrv.getModoAddebito().getPeriodiNumero().intValue());
            	  }
            	  else {
            		  qta1 = cpa.calcoloQuantitaAddebito(getDataInizioAttivContratto(), getOraInizioAttivContratto(), getDataFineAttivContratto(), getOraFineAttivContratto(), orario, cal,  materiale.getIdUMVen(), ordSrv.isAddebitoFestivita(), ordSrv.isAddebitoSabato(), ordSrv.isAddebitoDomenica());
            	  }
            	  if (ordSrv.getModoAddebito().isAddebitoMeseEffettivo() && UtilTemp.getPersDatiServiziUM(materiale.getIdUMPrmMag()).getTipoUnitaMisura() == PersDatiServiziUM.MESE) {	  
            		  qta2 = cpa.calcoloQuantitaAddebitoMeseEffettivo( getDataInizioAttivContratto(), getDataInizioAttivContratto(), getDataInizioAttivContratto(), getDataFineAttivContratto(), materiale.getIdUMPrmMag(), ordSrv.getModoAddebito().getPeriodiNumero().intValue());            	
            	  }
            	  else{
				      qta2 = cpa.calcoloQuantitaAddebito(getDataInizioAttivContratto(), getOraInizioAttivContratto(), getDataFineAttivContratto(), getOraFineAttivContratto(), orario, cal,  materiale.getIdUMPrmMag(), ordSrv.isAddebitoFestivita(), ordSrv.isAddebitoSabato(), ordSrv.isAddebitoDomenica());
            	  }
                  //Fix 40160 Fine 
              } catch (ThipException e) {
				e.printStackTrace();
			  }
              //Fix 40160 Inizio
        	  if(materiale.getTipoAddebitoMateriale() == AttivitaServizio.FORFAIT) {
                  rigaSec.setQtaInUMVen(new BigDecimal(1));
                  rigaSec.setUMRif(materiale.getUMVen());
                  rigaSec.setQtaInUMPrm(new BigDecimal(1));
                  rigaSec.setUMPrm(materiale.getUMPrmMag());
        	  }else {
	          //Fix 40160 Fine
	              rigaSec.setQtaInUMVen(qta1);
	              rigaSec.setUMRif(materiale.getUMVen());
	              rigaSec.setQtaInUMPrm(qta2);
	              rigaSec.setUMPrm(materiale.getUMPrmMag());
        	  }         
        	  /*rigaSec.setQtaInUMVen(getQtaInUMVen());
        	  rigaSec.setUMRif(materiale.getUMVen());
              rigaSec.setQtaInUMPrm(getQtaInUMPrm());
              rigaSec.setUMPrm(materiale.getUMPrmMag());*/
              //Fix 33309 Fine
              if (getUMSec() != null) {
            	//Fix 33309 Inizio
                //rigaSec.setQtaInUMSec(getQtaInUMSec());
            	BigDecimal qtaSec = new BigDecimal(0);
                try {
     			  qtaSec = cpa.calcoloQuantitaAddebito(getDataInizioAttivContratto(), getOraInizioAttivContratto(), getDataFineAttivContratto(), getOraFineAttivContratto(), orario, cal,  materiale.getIdUMSecMag(), ordSrv.isAddebitoFestivita(), ordSrv.isAddebitoSabato(), ordSrv.isAddebitoDomenica());
                } catch (ThipException e) {
     		     	e.printStackTrace();
     			}
                rigaSec.setQtaInUMSec(qtaSec);
            	//Fix 33309 Fine 
                rigaSec.setUMSec(getUMSec());
              }
              rigaSec.setCoefficienteImpiego(new BigDecimal(1));
            }
          }
          else {
        	//Fix 37274 Inizio
            if (utilizzaQtaPrelevataSuRigaSec != null && utilizzaQtaPrelevataSuRigaSec.equals("Y") && materiale.getQtaPrelevataUMPrm() != null && !materiale.getQtaPrelevataUMPrm().equals(zero))
            	rigaSec.setQtaInUMVen(materiale.getQtaPrelevataUMPrm());
            else
            //Fix 37274 Fine
                rigaSec.setQtaInUMVen(materiale.getQtaRichiestaUMPrm());
            
            rigaSec.setUMRif(materiale.getUMPrmMag());
            //Fix 37274 Inizio
            if (utilizzaQtaPrelevataSuRigaSec != null && utilizzaQtaPrelevataSuRigaSec.equals("Y") && materiale.getQtaPrelevataUMPrm() != null && !materiale.getQtaPrelevataUMPrm().equals(zero))
            	rigaSec.setQtaInUMPrm(materiale.getQtaPrelevataUMPrm());
            else
            //Fix 37274 Fine
               rigaSec.setQtaInUMPrm(materiale.getQtaRichiestaUMPrm());
            rigaSec.setUMPrm(materiale.getUMPrmMag());
            if (materiale.getUMSecMag() != null) {
        	  //Fix 37274 Inizio
              if (utilizzaQtaPrelevataSuRigaSec != null && utilizzaQtaPrelevataSuRigaSec.equals("Y") && materiale.getQtaPrelevataUMSec() != null && !materiale.getQtaPrelevataUMSec().equals(zero))
                  rigaSec.setQtaInUMSec(materiale.getQtaPrelevataUMSec());
              else
              //Fix 37274 Fine
                  rigaSec.setQtaInUMSec(materiale.getQtaRichiestaUMSec());
              rigaSec.setUMSec(materiale.getUMSecMag());
            }
          }
          rigaSec.setDataConsegnaRichiesta(getDataConsegnaRichiesta());
          rigaSec.setSettConsegnaRichiesta(getSettConsegnaRichiesta());
          rigaSec.setDataConsegnaConfermata(getDataConsegnaConfermata());
          rigaSec.setSettConsegnaConfermata(getSettConsegnaConfermata());
          rigaSec.setDataInizioAttivContratto(getDataInizioAttivContratto());
          rigaSec.setOraInizioAttivContratto(getOraInizioAttivContratto());
          rigaSec.setDataFineAttivContratto(getDataFineAttivContratto());
          rigaSec.setOraFineAttivContratto(getOraFineAttivContratto());
          rigaSec.setConfFineAttivContratto(isConfFineAttivContratto());
          rigaSec.getDatiComuniEstesi().setStato(getDatiComuniEstesi().getStato());

          //Dati Tab Prezzi/Sconti
          rigaSec.setIdListino(materiale.getIdListino());
          rigaSec.setPrezzo(materiale.getPrezzo());
          rigaSec.setPrezzoExtra(materiale.getPrezzoExtra());
          rigaSec.setScontoArticolo1(materiale.getScontoArticolo1());
          rigaSec.setScontoArticolo2(materiale.getScontoArticolo2());
          rigaSec.setMaggiorazione(materiale.getMaggiorazione());
          rigaSec.setIdSconto(materiale.getIdSconto());
          rigaSec.setPrcScontoIntestatario(materiale.getPrcScontoIntestatario());
          rigaSec.setPrcScontoModalita(materiale.getPrcScontoModalita());
          rigaSec.setIdScontoMod(materiale.getIdScontoMod());
          rigaSec.setIdAssogIVA(materiale.getIdAssogIVA());
          rigaSec.setRiferimentoUMPrezzo(materiale.getRiferimentoUMPrezzo());
          rigaSec.setTipoPrezzo(materiale.getTipoPrezzo());
          rigaSec.setProvenienzaPrezzo(materiale.getProvenienzaPrezzo());
          rigaSec.setBloccoRclPrzScnFatt(materiale.isBloccoRclPrzScnFatt());

          //Altri dati
          //Tipo riga
          //rigaSec.setTipoRiga(getTipoRiga()); //Fix 10882 riga spostata
          //Specializzazione riga
          switch (tipoMateriale) {
            case AttivitaSrvMateriale.ACCESSORIO:
              char rifAddOrdSrv = ordSrv.getRiferimentoAddebito();
              if (rifAddOrdSrv == OrdineServizio.RIF_ADB_PRD) {
                 rigaSec.setSpecializzazioneRiga(DocumentoBaseRiga.RIGA_SECONDARIA_PER_COMPONENTE);
              }
              else {
                char addServMateriale = materiale.getAddebitoServizio();
                //Fix 14922 inizio
                //if (addServMateriale == AttivitaServizio.CONDIZIONE_ORDINE) {
                if (addServMateriale == AttivitaServizio.CONDIZIONE_ORDINE || addServMateriale == AttivitaServizio.SI) {
                //Fix 14922 fine
                   rigaSec.setSpecializzazioneRiga(DocumentoBaseRiga.RIGA_SECONDARIA_DA_FATTURARE);
                }
                else {
                   rigaSec.setSpecializzazioneRiga(DocumentoBaseRiga.RIGA_SECONDARIA_PER_COMPONENTE);
                }
              }
              break;
            case AttivitaSrvMateriale.DOTAZIONE:
               rigaSec.setSpecializzazioneRiga(DocumentoBaseRiga.RIGA_SECONDARIA_PER_COMPONENTE);
              break;
            default:
              break;
          }
          //Non fatturare
           rigaSec.setNonFatturare(rigaSec.getSpecializzazioneRiga() != DocumentoBaseRiga.RIGA_SECONDARIA_DA_FATTURARE);
           //Fix 8520 - inizio
           rigaSec.setCollegatoAMagazzino(StatoAttivita.NON_RICHIESTO);
           rigaSec.sistemoLeQuantita();
           //Fix 8520 - fine

//MG FIX 8597 inizio: set dei dati di contabilità analitica: vengono riportati quelli della
// riga primaria dell'attività
           rigaSec.setIdCommessa(getIdCommessa());
           rigaSec.setIdCentroCosto(getIdCentroCosto());
           rigaSec.setIdGrpCntCa(getIdGrpCntCa());
           rigaSec.getDatiCA().setIdVoceSpesaCA(getDatiCA().getIdVoceSpesaCA());
           rigaSec.getDatiCA().setIdVoceCA4(getDatiCA().getIdVoceCA4());
           rigaSec.getDatiCA().setIdVoceCA5(getDatiCA().getIdVoceCA5());
           rigaSec.getDatiCA().setIdVoceCA6(getDatiCA().getIdVoceCA6());
           rigaSec.getDatiCA().setIdVoceCA7(getDatiCA().getIdVoceCA7());
           rigaSec.getDatiCA().setIdVoceCA8(getDatiCA().getIdVoceCA8());
//MG FIX 8597 fine
           rigaSec.setSalvaRigaPrimaria(false); //Fix 12508
           //12755 - PJ - inizio
           completaRigaSecDaMaterialeOrdSrv(rigaSec, materiale);
           //12755 - PJ - fine
           
         //Fix 33905 Inizio
           /*
            Fix 37023 Inizio
           DocumentoVenRigaSec rigaSecTmp = (DocumentoVenRigaSec)Factory.createObject(DocumentoVenRigaSec.class);
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
	   		Fix 37023 Fine
	   		*/
           //Fix 33905 Fine
           getRigheSecondarie().add(rigaSec);
        }
      }
    }
    //Fix 6439 - fine

    //Fix 8640 - inizio
    //Fix 9221: assegnazioni tramite id anzichè tramite oggetti
    protected void generaRigheSecDaRisorseOrdSrv(int tipoRigaNlgSrv) {
      if (tipoRigaNlgSrv == NLG_SRV_TIPO_FATTURA) {
	      OrdineServizio ordSrv = getOrdineServizio();
	      if (ordSrv != null) {
		      char rifAddOrdSrv = ordSrv.getRiferimentoAddebito();
		      if (rifAddOrdSrv == OrdineServizio.RIF_ADB_PRD_MAT) {
	          int sequenza = getRigheSecondarie().size();
		        List risorse = ordSrv.getRisorse();
		        Iterator iter = risorse.iterator();
		        while (iter.hasNext()) {
		          AttivitaSrvRisorsa risorsa = (AttivitaSrvRisorsa)iter.next();
	            char addServRisorsa = risorsa.getAddebitoServizio();
		          char tipoAddebito = risorsa.getTipoAddebitoRisorsa();
	            try {
			          if ((addServRisorsa == AttivitaServizio.CONDIZIONE_ORDINE ||
		            		 addServRisorsa == AttivitaServizio.SI)
		            		 &&
		            		(tipoAddebito == AttivitaServizio.CONSUMO ||
		            		 (tipoAddebito == AttivitaServizio.FORFAIT && UtilGener.isPrimoAddebito(ordSrv))
		            		)) {
				          DocumentoVenRigaSec rigaSec =
				            (DocumentoVenRigaSec)Factory.createObject(DocumentoVenRigaSec.class);
				          rigaSec.setIdAzienda(Azienda.getAziendaCorrente());

				          //Altri dati
				          rigaSec.setTipoRiga(getTipoRiga());
				          rigaSec.setCollegatoAMagazzino(StatoAttivita.NON_RICHIESTO);
		              rigaSec.setSpecializzazioneRiga(DocumentoBaseRiga.RIGA_SECONDARIA_DA_FATTURARE);
		              rigaSec.setNonFatturare(false);

		              //Dati Tab Generale
				          rigaSec.setSequenzaRiga(++sequenza);
				          rigaSec.setIdCauRig(getIdCauRig());
				          rigaSec.setEscludiDaDichIntento(isEscludiDaDichIntento());//Fix 26939
				          rigaSec.setAnnoOrdineServizio(ordSrv.getIdAnnoOrdine());
				          rigaSec.setNumeroOrdineServizio(ordSrv.getIdNumeroOrdine());
				          rigaSec.setIdBene(getIdBene());
				          Articolo articoloRsr = risorsa.getArticoloServizio();
				          if (articoloRsr != null) {
					          rigaSec.setIdArticolo(articoloRsr.getIdArticolo());
				            rigaSec.setDescrizioneArticolo(articoloRsr.getDescrizioneArticoloNLS().getDescrizione());
					          rigaSec.setIdVersioneRcs(articoloRsr.getVersioneAtDate(TimeUtils.getCurrentDate()).getIdVersione());
					          rigaSec.setIdConfigurazione(articoloRsr.getIdConfigurazioneStd());
				          }
				          rigaSec.setCoefficienteImpiego(risorsa.getCoeffUtilizzo());

			            //TODO Questo caso funziona solo se um della risorsa coincide con
			          	//um del prodotto. Sarà da raffinare eventualmente trasformando
			          	//qta e um come nel prodotto
				          switch (tipoAddebito) {
				          	case AttivitaServizio.CONSUMO:
					          	rigaSec.setQtaInUMVen(getQtaInUMVen());
					            rigaSec.setQtaInUMPrm(getQtaInUMVen());
				          		break;
				          	case AttivitaServizio.FORFAIT:
					          	rigaSec.setQtaInUMVen(new BigDecimal(1.0));
					            rigaSec.setQtaInUMPrm(new BigDecimal(1.0));
				          		break;
				          }
			            rigaSec.setUMRif(risorsa.getUMVen());
			            rigaSec.setUMPrm(risorsa.getUMVen());
			            if (getUMSec() != null) {
			              rigaSec.setQtaInUMSec(getQtaInUMSec());
			              rigaSec.setUMSec(getUMSec());
			            }
			            rigaSec.setCoefficienteImpiego(new BigDecimal(1));

				          rigaSec.setDataConsegnaRichiesta(getDataConsegnaRichiesta());
				          rigaSec.setSettConsegnaRichiesta(getSettConsegnaRichiesta());
				          rigaSec.setDataConsegnaConfermata(getDataConsegnaConfermata());
				          rigaSec.setSettConsegnaConfermata(getSettConsegnaConfermata());
				          rigaSec.setDataInizioAttivContratto(getDataInizioAttivContratto());
				          rigaSec.setOraInizioAttivContratto(getOraInizioAttivContratto());
				          rigaSec.setDataFineAttivContratto(getDataFineAttivContratto());
				          rigaSec.setOraFineAttivContratto(getOraFineAttivContratto());
				          rigaSec.setConfFineAttivContratto(isConfFineAttivContratto());
				          rigaSec.getDatiComuniEstesi().setStato(getDatiComuniEstesi().getStato());

				          //Dati Tab Prezzi/Sconti
				          rigaSec.setIdListino(risorsa.getIdListino());
				          rigaSec.setPrezzo(risorsa.getPrezzo());
				          rigaSec.setPrezzoExtra(risorsa.getPrezzoExtra());
				          rigaSec.setScontoArticolo1(risorsa.getScontoArticolo1());
				          rigaSec.setScontoArticolo2(risorsa.getScontoArticolo2());
				          rigaSec.setMaggiorazione(risorsa.getMaggiorazione());
				          rigaSec.setIdSconto(risorsa.getIdSconto());
				          rigaSec.setPrcScontoIntestatario(risorsa.getPrcScontoIntestatario());
				          rigaSec.setPrcScontoModalita(risorsa.getPrcScontoModalita());
				          rigaSec.setScontoModalita(risorsa.getScontoModalita());
				          rigaSec.setIdAssogIVA(risorsa.getIdAssogIVA());
				          rigaSec.setRiferimentoUMPrezzo(risorsa.getRiferimentoUMPrezzo());
				          rigaSec.setTipoPrezzo(risorsa.getTipoPrezzo());
				          rigaSec.setProvenienzaPrezzo(risorsa.getProvenienzaPrezzo());
				          rigaSec.setBloccoRclPrzScnFatt(risorsa.isBloccoRclPrzScnFatt());

				          rigaSec.sistemoLeQuantita();

									rigaSec.setIdCommessa(getIdCommessa());
									rigaSec.setIdCentroCosto(getIdCentroCosto());
									rigaSec.setIdGrpCntCa(getIdGrpCntCa());
									rigaSec.getDatiCA().setIdVoceSpesaCA(getDatiCA().getIdVoceSpesaCA());
									rigaSec.getDatiCA().setIdVoceCA4(getDatiCA().getIdVoceCA4());
									rigaSec.getDatiCA().setIdVoceCA5(getDatiCA().getIdVoceCA5());
									rigaSec.getDatiCA().setIdVoceCA6(getDatiCA().getIdVoceCA6());
									rigaSec.getDatiCA().setIdVoceCA7(getDatiCA().getIdVoceCA7());
									rigaSec.getDatiCA().setIdVoceCA8(getDatiCA().getIdVoceCA8());

   	                rigaSec.setSalvaRigaPrimaria(false);//Fix 12528
                                    //12755 - PJ - inizio
                                    completaRigaSecDaRisorsaOrdSrv(rigaSec, risorsa);
                                    //12755 - PJ - fine
                                    
                                  //Fix 33905 Inizio
                                    /*
                                     Fix 37023 Inizio
                                    DocumentoVenRigaSec rigaSecTmp = (DocumentoVenRigaSec)Factory.createObject(DocumentoVenRigaSec.class);
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
                           		Fix 37023 Fine
                            		*/
                                    //Fix 33905 Fine
				          getRigheSecondarie().add(rigaSec);
				        }
							}
							catch (Exception e) {
								e.printStackTrace(Trace.excStream);
							}
			      }
		      }
	      }
      }
    }
    //Fix 8640 - fine

    /*public void ricalcolaRighe(DocumentoVendita docVenTes) {
      List nRighePrm = docVenTes.getRighe();
      Iterator i = nRighePrm.iterator();
      HashMap collRigheIVA = new HashMap();
      while(i.hasNext()) {
        DocumentoVenRigaPrm rigaPrm = (DocumentoVenRigaPrm)i.next();
        String assIva = rigaPrm.getIdAssogIVA();
        if(collRigheIVA.containsKey(assIva)) {
          ArrayList righe = (ArrayList)collRigheIVA.get(assIva);
          righe.add(rigaPrm);
        }
        else {
          ArrayList righe = new ArrayList();
          righe.add(rigaPrm);
          collRigheIVA.put(assIva, righe);
        }
      }
      System.out.println(collRigheIVA.toString());
    }*/
    //...FIX 5518 fine

  //...FIX 7093 inizio

  /**
   * Attributo iBarcode
   */
  protected String iBarcode;

  /**
   * setBarcode
   * @param barcode String
   */
  public void setBarcode(String barcode) {
    iBarcode = barcode;
  }

  /**
   * getBarcode
   * @return String
   */
  public String getBarcode() {
    return iBarcode;
  }

  /**
   * recuperaArticoloDaBarcode
   * @return boolean
   */
  public boolean recuperaArticoloDaBarcode() {
    String barcode = getBarcode();
    if(barcode != null) {
      DocumentoVendita testata = (DocumentoVendita)getTestata();
      ArticoloBarcode articoloBarcode = ArticoloBarcode.getArticoloDaBarcode(barcode, ArticoloBarcode.AZIENDA, null, testata.getDataDocumento(), null);
      if(articoloBarcode != null) {
        ArticoloBase articolobase = articoloBarcode.getArticolo();
        if(articolobase != null) {
          setIdArticolo(articolobase.getIdArticolo());
          Articolo articolo = getArticolo();
          if(articolo != null) {
            //...Imposto l'unità di misura di riferimento con quella indicata sul barcode,
            //...se sul barcode non è indicata l'UM allora imposto quella primaria di magazzino
            String idUmRif = articolo.getIdUMPrmMag();
            if(articoloBarcode.getUnitaMisuraRif() != null)
              idUmRif = articoloBarcode.getIdUnitaMisuraRif();
            //...FIX 7053 inizio
            //creaNuovaRigaDaBarcode(articolo, idUmRif);
            creaNuovaRigaDaBarcode(articolo, idUmRif,  articoloBarcode.getIdArtVersione(), articoloBarcode.getConfigurazione());
            //...FIX 7053 fine
            return true;
          }
        }
      }
      try {
        //...Se non esiste un barcode allora cerco un articolo con codice = barcode
        Articolo articolo = Articolo.elementWithKey(getIdAzienda() + PersistentObject.KEY_SEPARATOR + barcode, PersistentObject.NO_LOCK);
        if(articolo != null) {
          creaNuovaRigaDaBarcode(articolo, articolo.getUMDefaultVendita().getIdUnitaMisura());
          return true;
        }
      }
      catch(SQLException ex) {
        ex.printStackTrace(Trace.excStream);
      }
    }
    //...Se non è stato impostato il barcode ma direttamente il codice articolo
    //...allora leggo direttamente l'articolo
    Articolo articolo = getArticolo();
    if(articolo != null) {
      creaNuovaRigaDaBarcode(articolo, articolo.getUMDefaultVendita().getIdUnitaMisura());
      return true;
    }
    return false;
  }

  //...FIX 7053 inizio

  /**
   * creaNuovaRigaDaBarcode
   * @param articolo Articolo
   * @param idUMRif String
   */
  public void creaNuovaRigaDaBarcode(Articolo articolo, String idUMRif) {
    /*setIdUMRif(idUMRif);
    setIdUMPrm(articolo.getIdUMPrmMag());
    if(articolo.getUMSecMag() != null)
      setIdUMSec(articolo.getIdUMSecMag());
    if(getDataConsegnaConfermata() == null)
      setDataConsegnaConfermata(getDataConsegnaRichiesta());
    datiArticolo.setParIdArticolo(articolo.getIdArticolo());
    cambiaArticolo(articolo, null, true);
    sistemoLeQuantita();*/
    creaNuovaRigaDaBarcode(articolo, idUMRif, null, null);
  }

  /**
   * creaNuovaRigaDaBarcode
   * @param articolo Articolo
   * @param idUMRif String
   * @param idVersione Integer
   * @param configurazione Configurazione
   */
  public void creaNuovaRigaDaBarcode(Articolo articolo, String idUMRif, Integer idVersione, Configurazione configurazione) {
    setIdUMRif(idUMRif);
    setIdUMPrm(articolo.getIdUMPrmMag());
    if(articolo.getUMSecMag() != null)
      setIdUMSec(articolo.getIdUMSecMag());
    if(getDataConsegnaConfermata() == null)
      setDataConsegnaConfermata(getDataConsegnaRichiesta());
    datiArticolo.setParIdArticolo(articolo.getIdArticolo());
    cambiaArticolo(articolo, configurazione, true);
    if(idVersione != null)
      setIdVersioneRcs(idVersione);
    sistemoLeQuantita();
  }

  //...FIX 7053 fine

  //...FIX 7093 fine
  // fix 7264
  // fix 7587
  /*
  protected boolean testTrasmissioneDoc()
 {
    DocumentoVendita dv = (DocumentoVendita) getTestata();
    if (dv.isNonDeveTrasmettereALogis()){
      return false;
    }
    return super.testTrasmissioneDoc();
  }
  */
  // fine fix 7587
  // fine fix 7264


  //Fix 7220 inizio
  public ErrorMessage checkDisponibilitaRigaDocAccPrn()
  {
	 //Fix 39456 - inizio
	 //Fix 37667 inizio 
	 if(getCausaleRiga() != null && getCausaleRiga().getAzioneMagazzino() != AzioneMagazzino.USCITA)
		   return null;
	 //Fix 37667 Fine
	 //Fix 39456 - fine
     ErrorMessage err = null;
     if(isOnDB() &&
        (isQuantitaCambiata() || isAttivoControlloAccPrn() || isStatoAvanzamentoCambiato()) && //Fix 9251
        getRigaOrdine() != null && //Fix 7187
        PersDatiATP.getCurrentPersDatiATP().getAttivazAccantPrenot() &&
        PersDatiATP.getLivelloControlloAccPrnMan() == PersDatiATP.LC_ERRORE)
     {
        BigDecimal zero = new BigDecimal("0.00");
		zero = Q6Calc.get().setScale(zero,2);//Fix 30871

        BigDecimal qtaNonDisponibileSuRiga = getQtaNonDisponibileConAccPrn();
        if(qtaNonDisponibileSuRiga.compareTo(zero) > 0)
        {
           BigDecimal qtaRigaPrm = getServizioQta().getQuantitaInUMPrm();
           BigDecimal dispSuRiga = qtaRigaPrm.subtract(qtaNonDisponibileSuRiga);
           BigDecimal dispSuRigaRif = getArticolo().convertiUM(dispSuRiga, getUMPrm(), getUMRif(), this.getArticoloVersRichiesta()); // fix 10955
           //dispSuRigaRif = dispSuRigaRif.setScale(2, BigDecimal.ROUND_HALF_UP);//Fiux 30871
			dispSuRigaRif = Q6Calc.get().setScale(dispSuRigaRif,2, BigDecimal.ROUND_HALF_UP);//Fiux 30871
           BigDecimal qtaRigaRif = getServizioQta().getQuantitaInUMRif();

           String msg_1 = ResourceLoader.getString(RES_EVA_VEN, "MsgErr_THIP200380");
           String param = qtaRigaRif + " " + msg_1 + " " + dispSuRigaRif;
           /*
           String msg = " Giacenza non sufficiente su sped " + qtaRigaRif +
                        " Massimo sped " + dispSuRigaRif;
           */
           err = new ErrorMessage("THIP200380", param);
        }
     }
     return err;
  }

  //Fix 9251 inizio
  protected boolean isStatoAvanzamentoCambiato()
  {
     boolean cambiato = false;
     if (getOldRiga() != null)
     {
        cambiato = (getOldRiga().getStatoAvanzamento() != StatoAvanzamento.DEFINITIVO &&
                    getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO);
     }
     return cambiato;
  }
  //Fix 9251 fine

  public BigDecimal getQtaNonDisponibileConAccPrn()
  {
     BigDecimal zero = new BigDecimal("0.00");
	 zero = Q6Calc.get().setScale(zero,2);//Fix 30871
     //Fix 9181 inizio
     if(!isAbilitatoMovimentiMagazzino())
        return zero;
     //Fix 9181 fine
     //Fix 9251 inizio
     if(getStatoAvanzamento() != StatoAvanzamento.DEFINITIVO)
        return zero;
     //Fix 9251 fine
     BigDecimal qtaNonDisponibileAccPrn = zero;
     if(this.getArticolo().getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST)
     {
        BigDecimal qtaRigaPrm = getServizioQta().getQuantitaInUMPrm();
        DocumentoVendita doc = (DocumentoVendita)getTestata();
        Iterator iterSec = getRigheSecondarie().iterator();
        while(iterSec.hasNext())
        {
           DocumentoVenRigaSec rigaSec = (DocumentoVenRigaSec)iterSec.next();
           BigDecimal coeffImp = rigaSec.getCoefficienteImpiego();
           if(!rigaSec.isBloccoRicalcoloQtaComp() && coeffImp.compareTo(zero) > 0)
           {
              BigDecimal qtaRigaSecDoc = qtaRigaPrm.multiply(coeffImp);
              BigDecimal qtaNonDispSec =
                 doc.getQtaNonDisponibileConAccPrn(rigaSec, rigaSec.getOldRiga(), (OrdineVenditaRiga)rigaSec.getRigaOrdine(), qtaRigaSecDoc);
              if(qtaNonDispSec.compareTo(zero) > 0)
              {
                 //BigDecimal qtaNonDispPrm = qtaNonDispSec.divide(coeffImp, 2, BigDecimal.ROUND_HALF_UP);//Fix 30871
				 BigDecimal qtaNonDispPrm = Q6Calc.get().divide(qtaNonDispSec , coeffImp, 2, BigDecimal.ROUND_HALF_UP);//Fix 30871
                 if(qtaNonDispPrm.compareTo(qtaNonDisponibileAccPrn) > 0)
                    qtaNonDisponibileAccPrn = qtaNonDispPrm;
              }
           }
        }
     }
     else
     {
        DocumentoVendita doc = (DocumentoVendita)getTestata();
        BigDecimal qtaRigaDoc = getServizioQta().getQuantitaInUMPrm();
        qtaNonDisponibileAccPrn =
           doc.getQtaNonDisponibileConAccPrn(this, this.getOldRiga(), (OrdineVenditaRiga)this.getRigaOrdine(), qtaRigaDoc);
     }
     return qtaNonDisponibileAccPrn;
  }
  //Fix 7220 Fine

//MG FIX 8659 inizio
  protected void recuperaDatiCA(DocumentoVenRigaSec rigaSec) {
    RiferimentoVociCA rifVociCA = rigaSec.getArticolo().getRiferimVociCA();
    /* sulla riga vendita riporto solo il gruppo conti definito sull'articolo */
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
    if (datiCA != null) {
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
//MG FIX 8659 fine

  //Fix 8707 - inizio
  /**
   * Richiamato in fase di cancellazione interattiva (solo da GUI) della riga.
   * Se associati alla riga esistono dei movimenti di storico matricole che
   * fanno riferimento a determinate matricole, queste ultime devono essere
   * 'regredite' in stato da VENDUTA a A MAGAZZINO e si deve eliminare in esse
   * qualsiasi riferimento a questo documento.
   * Le movimentazioni di storico devono essere eliminate.
   * @see Metodo deleteObject della classe DocumentoVenditaRigaPrmGridActionAdapter
   */
  public static synchronized ErrorMessage regressioneMatricole(DocumentoVenRigaPrm riga) throws SQLException {
  	ErrorMessage em = null;

  	if (riga.getArticolo().isArticoloMatric()) {

  		List righeSec = riga.getRigheSecondarie();
	  	Iterator iterSec = righeSec.iterator();
	  	while ((em == null || em.getSeverity() == ErrorMessage.WARNING) && iterSec.hasNext()) {
	  		DocumentoVenRigaSec rigaSec = (DocumentoVenRigaSec)iterSec.next();
	  		em = DocumentoVenRigaSec.regressioneMatricole(rigaSec);
	  	}

  		List lotti = riga.getRigheLotto();
	  	Iterator iterLotti = lotti.iterator();
	  	while ((em == null || em.getSeverity() == ErrorMessage.WARNING) && iterLotti.hasNext()) {
	  		DocumentoVenRigaLottoPrm lotto = (DocumentoVenRigaLottoPrm)iterLotti.next();
	  		List movStorMat =
	  			StoricoMatricola.getMovimentiStoricoMatricolaRigaLotto(
	  				lotto.getKey(), false
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
  // Fix 8913 - Inizio
  public void CompletaDatiCA() {
  DocumentoVendita testata = (DocumentoVendita) getTestata();
  Articolo articolo = getArticolo();
  String idGruppoContiArticolo = null;
  if (articolo.getArticoloDatiContab() != null &&
      articolo.getArticoloDatiContab().getRiferimVociCA() != null) {
      idGruppoContiArticolo = articolo.getArticoloDatiContab().getRiferimVociCA().getIdGruppoConti();
  }
  if (getIdCommessa() == null)
      setIdCommessa(testata.getIdCommessa());
  if (getIdCentroCosto() == null)
      setIdCentroCosto(testata.getIdCentroCosto());
  if (getIdGrpCntCa() == null){
      if (idGruppoContiArticolo != null)
          setIdGrpCntCa(idGruppoContiArticolo);
      else
          setIdGrpCntCa(testata.getIdGrpCntCa());
  }
  PersDatiGen pdg = PersDatiGen.getCurrentPersDatiGen();
  if (pdg.getTipoInterfCA() != PersDatiGen.NON_INTERFACCITO) {
      try {
          DatiCA datiContabili = getDatiCA();
          if (datiContabili.isIncompleto() ||
              getIdCommessa() == null || getIdCentroCosto() == null) {
              SottogruppoContiCA DatiCARecuperati =
                  GestoreDatiCA.recuperaDatiCA(GestoreDatiCA.VENDITA,
                  articolo,
                  idGruppoContiArticolo,
                  testata.getIdGrpCntCa(),
                  testata.getIdCentroCosto(),
                  testata.getIdCommessa());

              if (DatiCARecuperati != null) {
                  datiContabili.completaDatiCA(DatiCARecuperati);
                  if (getIdCommessa() == null &&
                      DatiCARecuperati.getIdCommessa() != null) {
                      setIdCommessa(DatiCARecuperati.getIdCommessa());
                  }
                  if (getIdCentroCosto() == null &&
                      DatiCARecuperati.getIdCentroCosto() != null) {
                      setIdCentroCosto(DatiCARecuperati.getIdCentroCosto());
                  }
              }
          }
      } catch (Exception ex) {
          ex.printStackTrace(Trace.excStream);
      }
  }
}

// Fix 8913 - Fine


  //Fix 9251 inizio
  //protected List rendiDefinitivoSalva() throws SQLException{ //Fix 14069 AYM
  public List rendiDefinitivoSalva() throws SQLException{

     setAttivoControlloAccPrn(true);
     List errs = super.rendiDefinitivoSalva();
     //??setAttivoControlloAccPrn(false);

     return errs;
  }
  //Fix 9251 fine

// PAOLA
public int delete() throws SQLException {
  int rc =0;
  SQLException ex = null;
  boolean hoFattoTutto = false;
  // PAOLA
  Timestamp timestampOldTestata=null;
  if (!isCancellabile()){
    return 0;
  }
  // fine PAOLa
  try {
    // PAOLA
    timestampOldTestata= this.getTestata().getTimestamp();
    // fine PAOLA
    rc = super.delete();
    hoFattoTutto = true;
  }
  catch (SQLException t) {
    t.printStackTrace(Trace.excStream);
    ex = t;
  }
  finally {
    boolean isAbilitaCommitRiga = ((DocumentoVendita)this.getTestata()).
        isAbilitaCommitRigaDaProposta();
    if (isAbilitaCommitRiga){
      isAbilitaCommitRiga = puoEssereCommittata();
    }
    boolean rilancioLeccezione = true;
    if (isAbilitaCommitRiga){
      if (rc >= 0 && ex ==null && hoFattoTutto) {
        ConnectionManager.commit();
      }
      else {
        aggiornaLaLista(rc, ex);
        rilancioLeccezione = false;
        rc =0;
        ConnectionManager.rollback();
        // PAOLA
        // Questa operazione che sembra strana deve essere fatta perchè
        // la delete è stata implementata in questo modo:
        // super.delete (che al suo interno fa la save della testata) e
        // poi chiama l'aggiornamento dei movimenti di magazzino, ovvimanete se
        // la delete è andata a buon fine. Se il problema si presenta proprio
        // nell'aggiornamento dei movimenti di magazzino, il timestamp
        // della testata risulta aggiornato perchè lo salverebbe
        // devo quindi riportare la testata ad uno stato iniziale
        // perchè se dopo questa riga che è andata male cerco di fare degli
        // aggiornamenti su altre righe otterei un optimistic look sulla testata
        DocumentoVendita testata = (DocumentoVendita)this.getTestata();
        if (timestampOldTestata!=null && testata!=null){
          Timestamp timestampNew = testata.getTimestamp();
          if (!timestampOldTestata.equals(timestampNew)){
            boolean b = testata.retrieve();
          }
        }
        // fine PAOLA
      }
    }
    if (ex!=null && rilancioLeccezione){
      // PAOLA
      if (((DocumentoVendita)this.getTestata()).
        isAbilitaCommitRigaDaProposta()){
        DocumentoVendita testata = (DocumentoVendita)this.getTestata();
        if (timestampOldTestata!=null && testata!=null){
          Timestamp timestampNew = testata.getTimestamp();
          if (!timestampOldTestata.equals(timestampNew)){
            boolean b = testata.retrieve();
          }
        }
      }
      // fine PAOLA
      throw ex;
    }
  }
  return rc;
 }

 public DocumentoVenditaRiga getRigaDaProposta(){
   return iRigaDaProposta;
 }
 public void setRigaDaProposta(DocumentoVenditaRiga d){
   iRigaDaProposta = d;
 }

 public void aggiornaLaLista(int rc, SQLException ex){
   Map lista = null;
   if (((DocumentoVendita)this.getTestata()).getChiaviRigheRollback()==null){
     lista = new HashMap();
     ((DocumentoVendita)this.getTestata()).setChiaviRigheRollback(lista);
   }
   else {
     lista = ((DocumentoVendita)this.getTestata()).getChiaviRigheRollback();
   }
   String chiaveOrdine = null;
   if (this.getRigaOrdine()!=null){
     chiaveOrdine = this.getRigaOrdine().getKey();
   }
   if (this.getRigaDaProposta()!=null){
     lista.put(this.getRigaDaProposta().getKey(), new OggettoRigheRollback(this.getRigaDaProposta().getKey(),new Integer(rc),ex,chiaveOrdine));
   }
   else {
     lista.put(this.getKey(), new OggettoRigheRollback(this.getKey(),new Integer(rc),ex,chiaveOrdine));
   }
 }

 protected boolean puoEssereCommittata(){
   return !(this instanceof DocEvaVenRigaPrm);
 }

 // PAOLA
 public boolean isCancellabile(){
   boolean ritorno = true;
   OggettoRigheRollback utility = new OggettoRigheRollback();
   if ((DocumentoVendita)this.getTestata()!=null){
     Set righeNoCance = ( (DocumentoVendita)this.getTestata()).
         getRigheDaNonCancellare();
     if (righeNoCance != null && !righeNoCance.isEmpty()) {
       if (utility.isChiaveNellaLista(righeNoCance, this.getKey())) {
         ritorno =  false;
       }
     }
     if (ritorno && this.getRigaOrdine()!=null){
       Set righeOrdine = ( (DocumentoVendita)this.getTestata()).getRigheOrdineDaNonCancellare();
       if (righeOrdine!=null && !righeOrdine.isEmpty()) {
         String chiaveRigaOrdine = this.getRigaOrdine().getKey();
         if (utility.isChiaveNellaLista(righeOrdine, chiaveRigaOrdine)) {
           ritorno =  false;
         }
       }
     }
   }
   return ritorno;
 }

// fine PAOLA

 // fix 10987
 public boolean effettuareIlControllo(List lista){
   boolean ritorno = false;
   //ho messo l'and tra l'aggiornametno saldi e il moviemtno di magazzino perchè
   // ad esempio se la riga è merce a valore non deve essere effettuato il controllo
   // è solo la and tra le due che mi può garantire, se le cose sono fatte bene che
   // potenzialmente la causale mov di magazzino prevederà un aggiornamento della giacenza
   // ma non nei casi speciali
   OggCalcoloGiaDisp ogg = null;
   // fix 11123
   //if ((!this.isOnDB()|| this.isQuantitaCambiata()) && this.isAbilitatoAggiornamentoSaldi() && this.isAbilitatoMovimentiMagazzino()) {
   if (this.isDaAggiornare() && this.isAbilitatoAggiornamentoSaldi() && this.isAbilitatoMovimentiMagazzino()) {
   // fine fix 11123
     CausaleRigaDocVen cau = this.getCausaleRiga();
     DocumentoVendita testata = (DocumentoVendita)this.getTestata();
     Articolo articolo = this.getArticolo();
     char tipoArticolo = articolo.getTipoArticolo();
     boolean artContenitore =
        tipoArticolo == Articolo.ART_CONTENITORE ||
        tipoArticolo == Articolo.CONTENITORE_TERZI;
     boolean isTrasferimento = testata.isDiTrasferimento();
     char azione = cau.getAzioneMagazzino();
     boolean kitNonGestitoAMag = (articolo.getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST);
     if (!kitNonGestitoAMag){
       if (artContenitore) {
         if (azione == AzioneMagazzino.USCITA && cau.getCauMagContenitori1() != null) {
           ritorno = true;
           ogg = (OggCalcoloGiaDisp) Factory.createObject(OggCalcoloGiaDisp.class);
           ogg.caricati(this, null, null);
           ogg.setIdCliente(this.getMagazzino().getIdCodiceClienteAzienda());
           ogg.setTipoControllo(ogg.TP_CTL_GIACENZA);
         }
         // fix 11239
         //else if (azione == AzioneMagazzino.ENTRATA &&
         //         cau.getCauMagContenitori2() != null) {
         //  ritorno = true;
         //}
         // fine fix 11239
       }
       else if (isTrasferimento) {
         //if (azione == AzioneMagazzino.ENTRATA && cau.getCauMagTrasferim() != null) { // Fix 24569
         if (azione == AzioneMagazzino.ENTRATA && cau.getCauMagTrasferim() != null && testata.getIdMagazzinoTra() != null) { // Fix 24569
           ogg = (OggCalcoloGiaDisp) Factory.createObject(OggCalcoloGiaDisp.class);
           ogg.caricati(this, null, null);
           ogg.setIdMagazzino(testata.getIdMagazzinoTra());
           ogg.setProfondita(ogg.calcolaProfonditaSaldo());
           ogg.setTipoControllo(ogg.TP_CTL_GIACENZA);
         }
         ritorno = true;
       }
       else if (azione == AzioneMagazzino.USCITA) {
         ritorno = true;
       }
     }
   }
   if (ritorno){
     if (ogg==null){
       ogg = (OggCalcoloGiaDisp) Factory.createObject(OggCalcoloGiaDisp.class);
       ogg.caricati(this, null, null);
       ogg.setTipoControllo(ogg.TP_CTL_GIACENZA);
     }
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
   // fix 11123 sostituita la chiamata al metodo isQuantitaCambiata() con isDaAggiornare()
   if (getRigheSecondarie()!=null && !getRigheSecondarie().isEmpty() &&
       (this.isDaAggiornare() ||
            isGeneraRigheSecondarie())){
     Iterator iter = this.getRigheSecondarie().iterator();
     while (iter.hasNext()) {
       DocumentoVenRigaSec rigaSec = (DocumentoVenRigaSec) iter.next();
       QuantitaInUMRif qtaRigaSec = rigaSec.ricolcoloQuantitaDaRicalcolare(this,this.getServizioQta());
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
    //Fix 30193 Inizio
    Articolo artSec = datiRigaKit.getArticoloUsato().getArticolo();
    Configurazione confSec = datiRigaKit.getConfigurazioneUsata();
    boolean controlla = true ;
    if(this.isConfigurazioneNeutra(artSec , confSec)) {
   	 controlla = false ;
    }
    if(controlla) {
   //Fix 30193 Fine
    OggCalcoloGiaDisp ogg = (OggCalcoloGiaDisp)Factory.createObject(OggCalcoloGiaDisp.class);
    ogg.caricati(datiRigaKit,this, null);
    ogg.setTipoControllo(ogg.TP_CTL_GIACENZA);
    lista.add(ogg);
    ritorno = true;
 	}//Fix 30193
  }
  return ritorno;
}

public boolean verificoGiaRigheSecondarieDistinta(EsplosioneNodo nodo, List lista){
  boolean ritorno =false;
  List datiRigheKit = nodo.getNodiFigli();
  Iterator iter = datiRigheKit.iterator();
  while (iter.hasNext()) {
    EsplosioneNodo datiRigaKit = (EsplosioneNodo)iter.next();
    //Fix 30193 Inizio
    Articolo artSec = datiRigaKit.getArticolo();
    Configurazione confSec = datiRigaKit.getConfigurazione();
    boolean controlla = true ;
    if(this.isConfigurazioneNeutra(artSec , confSec)) {
   	 controlla = false ;
    }
    if(controlla) {
   //Fix 30193 Fine
    OggCalcoloGiaDisp ogg = (OggCalcoloGiaDisp) Factory.createObject(
        OggCalcoloGiaDisp.class);
    ogg.caricati(datiRigaKit, this, null);
    ogg.setTipoControllo(ogg.TP_CTL_GIACENZA);
    lista.add(ogg);
    ritorno = true;
 	}//Fix 30193
  }
  return ritorno;
}

 // fine fix 10987

 //fix 10882
 public CausaleRigaDocVen getPrimaCausaleRigaMerce() {
   DocumentoVendita docVen = (DocumentoVendita)this.getTestata();
   CausaleDocumentoVendita causaleDoc = docVen.getCausale();
   if(causaleDoc == null) return null;
   if(causaleDoc.getCausaliRiga() == null) return null;
   Iterator it = causaleDoc.getCausaliRiga().iterator();
   while (it.hasNext()) {
     CausaleRigaDocVen causaleRiga = (CausaleRigaDocVen) it.next();
     if (causaleRiga.getTipoRiga() == TipoRigaDocumentoVendita.MERCE)
       return causaleRiga;
   }
   return null;
 }
 //end 10882


 //Fix 11084 PM Inizio
 public void disabilitaTrasmissionePPL()
 {
	 setTrasmissionePPLAbilitata(false);
	 Iterator i = getRigheSecondarie().iterator();
	 while(i.hasNext())
	 {
		 DocumentoVenditaRiga rigaSec = (DocumentoVenditaRiga)i.next();
		 rigaSec.setTrasmissionePPLAbilitata(false);
	 }

 }
//Fix 11084 PM Fine


 //Fix 11170 PM Inizio
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
	  
 }
 //Fix 37244 fine
 protected void copiaRigaImpostaProvvigioniAgenti(DocumentoOrdineRiga riga)
 {
	 try
	 {
		 DocumentoVenRigaPrm rigaPrm = (DocumentoVenRigaPrm)riga;
		 DocumentoOrdineRigaVenRecuperaDati recDati = new DocumentoOrdineRigaVenRecuperaDati();
		 recDati.setClassName("DocumentoVenditaRigaPrm");
		 recDati.setArticolo(rigaPrm.getArticolo());
		 recDati.setIdAgente(rigaPrm.getIdAgente());
		 if (rigaPrm.getProvvigione1Agente() != null)
			 recDati.setProvv1Agente(rigaPrm.getProvvigione1Agente().toString());
		 recDati.setIdSubagente(rigaPrm.getIdAgenteSub());
		 if (rigaPrm.getProvvigione1Subagente() != null)
			 recDati.setProvv1Subagente(rigaPrm.getProvvigione1Subagente().toString());
		 DocumentoVendita  testata = ((DocumentoVendita)rigaPrm.getTestata());
		 recDati.setIntestatario(testata.getIdCliente());
		 recDati.setDivisione(testata.getIdDivisione());
		 recDati.impostaProvvigioniAgente();
		 rigaPrm.setProvvigione1Agente(recDati.getAgentiProvvigioni().getProvvigioniAgente());
		 rigaPrm.setProvvigione1Subagente(recDati.getAgentiProvvigioni().getProvvigioniSubagente());
		 rigaPrm.setIdAgente(recDati.getAgentiProvvigioni().getIdAgente());
		 rigaPrm.setIdAgenteSub(recDati.getAgentiProvvigioni().getIdSubagente());
	 }
	 catch(Exception e)
	 {
		 e.printStackTrace(Trace.excStream);
	 }
 }
  //Fix 11170 PM Fine

 //Fix 12508 inizio
 public void calcolaPesiEVolume(boolean newRow)
 {
    if(!isAttivoCalcoloPesiVolumi()) //Fix 13110
       return;

    //if(isRicalcolaPesiEVolume() && (newRow || isQuantitaCambiata() || isPesiVolumiCambiati()))//Fix 41393
    if(isRicalcolaPesiEVolume() && (newRow || isQuantitaCambiata() || isPesiVolumiCambiati() || isVersioneCambiata()))//Fix 41393
    {
       //Fix 14931 inizio
       CalcolatorePesiVolume calc = CalcolatorePesiVolume.getInstance();
       calc.aggiornaPesiVolumeRiga(this);
       /*
       BigDecimal[] pesiEVolume = null;
       if(getArticolo().getTipoParte() != ArticoloDatiIdent.KIT_NON_GEST)
       {
          QuantitaInUMRif srvQta = getServizioQta();
          pesiEVolume = Articolo.getPesiEVolumeTotali(getArticolo(),
                srvQta.getQuantitaInUMRif(), srvQta.getQuantitaInUMPrm(), srvQta.getQuantitaInUMSec(),
                getUMRif(), getUMPrm(), getUMSec());
       }
       else
       {
          //Nel caso di kit non gestiti a magazzino pesi e volume sono derivati dai componenti
          pesiEVolume = new BigDecimal[]{ZERO_DEC, ZERO_DEC, ZERO_DEC};
          Iterator righeSecIter = getRigheSecondarie().iterator();
          while(righeSecIter.hasNext())
          {
             DocumentoVenRigaSec rigaSec = (DocumentoVenRigaSec)righeSecIter.next();
             if(rigaSec.getStato() != DatiComuniEstesi.ANNULLATO)
             {
                if(rigaSec.getPesoNetto() != null)
                   pesiEVolume[0] = pesiEVolume[0].add(rigaSec.getPesoNetto());
                if(rigaSec.getPesoLordo() != null)
                   pesiEVolume[1] = pesiEVolume[1].add(rigaSec.getPesoLordo());
                if(rigaSec.getVolume() != null)
                   pesiEVolume[2] = pesiEVolume[2].add(rigaSec.getVolume());
             }
          }
          pesiEVolume = Articolo.sistemaScalePesiEVolumePerRighe(pesiEVolume);
       }
       setPesoNetto(pesiEVolume[0]);
       setPesoLordo(pesiEVolume[1]);
       setVolume(pesiEVolume[2]);
       */
       //Fix 14931 fine

       //System.out.println("DOC_PRM:calcolaPesiEVolume " + pesiEVolume[0]);
    }
 }

 public void calcolaPesiEVolumeRigheSec()
 {
    if(!isAttivoCalcoloPesiVolumi()) //Fix 13110
       return;

    //Utilizzato in creazione e quindi non sente il flag di ricalcolo
    Iterator iterSec = getRigheSecondarie().iterator();
    while(iterSec.hasNext())
    {
       DocumentoVenRigaSec rigaSec = (DocumentoVenRigaSec)iterSec.next();
       rigaSec.calcolaPesiEVolume();
    }
 }

 public void aggiornaPesiEVolumeTestata(boolean rigaInCancellazione)
 {
    //Fix 14931 inizio
    CalcolatorePesiVolume calc = CalcolatorePesiVolume.getInstance();
    calc.aggiornaPesiVolumeTestataDaRiga((DocumentoVendita)getTestata(), this, rigaInCancellazione);

    /*
    //aggiornamento dei pesi e volume di testata per delta rispetto a preceente
    //da fare solo se flag di ricalcolo su testata acceso
    if(!isAttivoCalcoloPesiVolumi()) //Fix 13110
       return;

    DocumentoVendita testata = (DocumentoVendita)getTestata();
    if(!testata.isRicalcolaPesiEVolume())
       return;

    BigDecimal pesoNettoTes = getNotNullValue(testata.getPesoNetto());
    BigDecimal pesoLordoTes = getNotNullValue(testata.getPesoLordo());
    BigDecimal volumeTes = getNotNullValue(testata.getVolume());

    boolean aggiornaTes = false;
    //controllo per eliminare valroe vecchio
    boolean oldRigaNonAnnullata = getOldRiga() != null && getOldRiga().getDatiComuniEstesi().getStato() != DatiComuniEstesi.ANNULLATO;
    boolean storno = isOnDB() && oldRigaNonAnnullata && (rigaInCancellazione || isQuantitaCambiata() || isPesiVolumiCambiati());
    if(storno && getOldRiga().isPesiVolumeValorizzati())
    {
       DocumentoDocRiga oldRiga = (DocumentoDocRiga)getOldRiga();
       pesoNettoTes = pesoNettoTes.subtract(getNotNullValue(oldRiga.getPesoNetto()));
       pesoLordoTes = pesoLordoTes.subtract(getNotNullValue(oldRiga.getPesoLordo()));
       volumeTes = volumeTes.subtract(getNotNullValue(oldRiga.getVolume()));
       aggiornaTes = true;
    }

    //controllo per aggiungere valore nuovo
    boolean valida = getDatiComuniEstesi().getStato() != DatiComuniEstesi.ANNULLATO;
    boolean applica = valida && !rigaInCancellazione && (isQuantitaCambiata() || !isOnDB() || isPesiVolumiCambiati());
    if(applica && isPesiVolumeValorizzati())
    {
       pesoNettoTes = pesoNettoTes.add(getNotNullValue(getPesoNetto()));
       pesoLordoTes = pesoLordoTes.add(getNotNullValue(getPesoLordo()));
       volumeTes = volumeTes.add(getNotNullValue(getVolume()));
       aggiornaTes = true;
    }

    if(aggiornaTes)
    {
       BigDecimal[] pesiEVolume = new BigDecimal[] {pesoNettoTes, pesoLordoTes, volumeTes};
       pesiEVolume = Articolo.sistemaScalePesiEVolume(pesiEVolume);
       testata.setPesoNetto(pesiEVolume[0]);
       testata.setPesoLordo(pesiEVolume[1]);
       testata.setVolume(pesiEVolume[2]);
       //System.out.println("DOC_AGGTES:aggiornaPesiEVolumeTestata " + pesiEVolume[0]);
    }
    */
    //Fix 14931 fine
 }
 //Fix 12508 fine

 // Fix 13494 inzio
 protected void calcolaImportiRiga() {
   calcolaPrezzoDaRigheSecondarieConReset(false);
   super.calcolaImportiRiga();
 }
 // Fix 13494 fine
 //Fix14727 Inizio RA
 public String recuperaDescExtArticolo(String idCliente, String idArticolo, Integer idConfigurazione) throws SQLException {
   PersDatiVen pdv = PersDatiVen.getCurrentPersDatiVen();
   String ret = "";
   if (pdv.getGestioneDescExtArticolo() == PersDatiVen.GESTITA) {
     ArticoloCliente artCli = ArticoloCliente.getArticoloCliente(Azienda.getAziendaCorrente(), idCliente, idArticolo, idConfigurazione);
     if (artCli != null && artCli.getDescrizioneEst() != null && !isEm(artCli.getDescrizioneEst().getDescrizioneEstesa())) {
       ret = artCli.getDescrizioneEst().getDescrizioneEstesa();
     }
     else {
       Articolo articolo = (Articolo) Articolo.elementWithKey(KeyHelper.buildObjectKey(new String[] {Azienda.getAziendaCorrente(), idArticolo}), PersistentObject.NO_LOCK);
       ret = (articolo != null && articolo.getDescrizioneArticoloNLS() != null) ? articolo.getDescrizioneArticoloNLS().getDescrizioneEstesa() : null;
     }
   }
   return ret;
 }

 public boolean isEm(String tmp) {
   return (tmp != null && !tmp.equals("")) ? false : true;
 }
 //Fix14727 Fine RA
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
    QuantitaInUMRif qtaAttesa = new QuantitaInUMRif();
    qtaProp.azzera();
    qtaAttesa.azzera();
    if (getStatoAvanzamento() == StatoAvanzamento.PROVVISORIO && getQtaPropostaEvasione() != null) {
      qtaProp = getQtaPropostaEvasione();
    }
    if (getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO && getQtaAttesaEvasione() != null) {
      qtaAttesa = getQtaAttesaEvasione();

    }

    DocumentoVenRigaLottoPrm lotto = (DocumentoVenRigaLottoPrm) Factory.createObject(DocumentoVenRigaLottoPrm.class);
    lotto.setFather(this);
    lotto.setIdAzienda(getIdAzienda());
    lotto.setIdArticolo(getIdArticolo());
    lotto.setIdLotto(Lotto.LOTTO_ND);
    lotto.setQtaAttesaEvasione(lotto.getQtaAttesaEvasione().add(qtaAttesa));
    lotto.setQtaPropostaEvasione(lotto.getQtaPropostaEvasione().add(qtaProp));
    getRigheLotto().add(lotto);

  }

//Fix 16032 fine

  //Fix 16242-19920 - inizio
  protected boolean isRigaNoleggiServizi() {
	 boolean ret =
			 getTipoRiga() == TipoRigaDocumentoVendita.SERVIZIO_NOLEGGIO ||
			 (getTipoRiga() == TipoRigaDocumentoVendita.SERVIZIO_ASSIST_MANUT && getTestata() instanceof NuovoDocumentoSpedRientro);
	 return ret;
  }


  protected boolean bloccaGenerazRigheSecDaMaterialiOrdSrv(AttivitaSrvMateriale mat, CausaleRigaDocVen  cauRiga) {
         return mat.getTipoMateriale() != AttivitaSrvMateriale.MATERIALE || cauRiga == null;
  }


  protected boolean areCondizPrepConvalidaRigaSec(DocumentoVenRigaSec rigaSec) {
	 return !rigaSec.isCollegataAMagazzino() && rigaSec.getDatiComuniEstesi().getStato() != DatiComuniEstesi.ANNULLATO;
  }


  protected boolean isCauRigheSecDaMatOrdSrvMerce(CausaleRigaDocVen  cauRiga) {
	 return cauRiga != null;
  }
  //Fix 16242-19920 - fine

  //Fix 18703 inizio
  protected void calcolaImportiPercRiga(){
    setValoreRiga(new BigDecimal(0));
    setCostoRiga(new BigDecimal(0));
    setValoreImposta(new BigDecimal(0));
    setValoreTotaleRiga(new BigDecimal(0));
    setPrezzoNetto(new BigDecimal(0));
  }
  //Fix 18703 fine
  //Fix 20387 inizio
  public ErrorMessage checkIdEsternoConfigInCopia() {
    //if (!isOnDB() && !getRigheSecondarie().isEmpty()) { // Fix 23709
    //if (!getRigheSecondarie().isEmpty()) { // Fix 23709  // Fix 26072
    if (!isDaCaricamentoDiMassa() && !getRigheSecondarie().isEmpty()) { // Fix 26072
      if (!equalsObject(getIdEsternoConfig(), iOldIdEsternoConfig))
        return new ErrorMessage("THIP40T339");
    }
    return null;
  }
  //Fix 20387 fine
  //Fix 21094 inizio
  public ErrorMessage checkIdCommessa() {
    DocumentoBase testata = getTestata();
		if (testata == null) return null; // Fix 25106
    //if(!((DocumentoVendita)testata).isFatturaPA()) return null;//Fix 27148
	if(((DocumentoVendita)testata).getFatturaPA() == CausaleDocumentoVendita.TIPO_FATELETT_ALTRO) return null;//Fix 27148

    if(Utils.compare(testata.getIdCommessa() ,getIdCommessa()) ==0 ) return null;

    String cig1=null;
    String cig2=null;
    String cup1=null;
    String cup2=null;

    if (testata.getCommessa()!=null)
    {
      cig1 = testata.getCommessa().getCodiceCIG();
      cup1 = testata.getCommessa().getCodiceCUP();
    }
    if (getCommessa()!=null)
    {
      cig2 = getCommessa().getCodiceCIG();
      cup2 = getCommessa().getCodiceCUP();
    }

    if(Utils.compare(cig1, cig2) != 0  ||Utils.compare(cup1, cup2) != 0)
      return new ErrorMessage("THIP40T366");
    return null;
  }

  //Fix 21094 fine

  //Fix 18753 inizio
  public void verificaConfRigheSec(){
    Iterator iterator = getRigheSecondarie().iterator();

    while (iterator.hasNext()) {
      DocumentoVenRigaSec rigaSec = (DocumentoVenRigaSec) iterator.next();

      if (rigaSec.getArticolo() != null && rigaSec.getArticolo().isConfigurato() &&
          rigaSec.getConfigurazione() == null) {
        setStatoAvanzamento(StatoAvanzamento.PROVVISORIO);
        rigaSec.setStatoAvanzamento(StatoAvanzamento.PROVVISORIO);
        getWarningList().add(new ErrorMessage("THIP40T311", rigaSec.getIdArticolo()));
      }
    }
  }
  //Fix 18753 fine

  // Fix 22823 inizio
  public List rendiDefinitivo() throws SQLException {
  	List arr = null;
    if (this.getStatoAvanzamento() == StatoAvanzamento.PROVVISORIO) {
        rendiDefinitivoRiga();
        arr = rendiDefinitivoSalva();
    }
    return arr;
  }
  // Fix 22823 fine
  //Fix 22839 inizio
  protected Entity getEntityRiga() {
    try {
      return Entity.elementWithKey("DocVenditaRiga", Entity.NO_LOCK);
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
    DocumentoDocRiga oldRiga = getOldRiga();
    DocumentoVendita testata = (DocumentoVendita)this.getTestata();
    if (isProvenienzaPrezzoDaListini() && getTipoRiga() != TipoRiga.SPESE_MOV_VALORE) {
      if (oldRiga != null &&
          ((!datiUguali(oldRiga.getDataConsegnaConfermata(), getDataConsegnaConfermata())
            && testata.getRifDataPerPrezzoSconti() == ClienteVendita.DATA_CONSEGNA)
           || oldRiga.getServizioQta().getQuantitaInUMRif().compareTo(this.getServizioQta().getQuantitaInUMRif()) != 0))
        return true;
    }
    return false;
  }

  protected boolean isProvenienzaPrezzoDaListini() {
    if (getProvenienzaPrezzo() == PROV_PREZZO_LISTINO_GENERICO ||
        getProvenienzaPrezzo() == PROV_PREZZO_LISTINO_CLIENTE ||
        getProvenienzaPrezzo() == PROV_PREZZO_LISTINO_ZONA ||
        getProvenienzaPrezzo() == PROV_PREZZO_LISTINO_CATEG_VEN )
      return true;
    return false;
  }
  //Fix 23345 fine

//Fix 24070 MBH Inizio
  public void  disabilitaAggiornamentoSaldiMovimentiSuiLotti()
  {
	    super.disabilitaAggiornamentoSaldiMovimentiSuiLotti();
	    
	    Iterator i = getRigheSecondarie().iterator();
	    while (i.hasNext())
	    {
	    	DocumentoVenRigaSec rigaSec = (DocumentoVenRigaSec)i.next();
	    	rigaSec.disabilitaAggiornamentoSaldiMovimentiSuiLotti();
	    }
	    
  }

  //Fix 24070 MBH Fine


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

      if(this instanceof DocEvaVenRigaPrm){
        if(newRow)
          return salvaDettRigaConfDaEvasione();
        else
          return 0;
      }

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
      else if (isConfigurazioneCambiata() || isListinoCambiato() || provPrezzo == TipoRigaRicerca.MANUALE || iControlloRicalVlrDettCfg){
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

  public ListinoVendita getListino() {
    return getListinoPrezzi();
  }

  public Object getOggettoTestata() {
    return getTestata();
  }

  public BigDecimal getQtaInUMRif() {
    return getQtaInUMVen();
  }
  //Fix 24613 fine

  // Fix 25004 inizio
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
  
  // Fix 25818 inizio
  public void propagaDatiTestata(SpecificheModificheRigheOrd spec) {
    super.propagaDatiTestata(spec);
    Iterator righeSec = getRigheSecondarie().iterator();
    while (righeSec.hasNext()) {
      DocumentoVenRigaSec rigaSec = (DocumentoVenRigaSec) righeSec.next();
      rigaSec.propagaDatiTestata(spec);
    }
  }
  // Fix 25818 fine
  //Fix 26599 inizio
  protected boolean isRicalProvvAgSubag(){
	  if(isOnDB()) 
		  return false;
	  Articolo art = getArticolo();
      //if(art != null && art.getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST && art.getTipoCalcPrzKit() == ArticoloDatiVendita.DA_COMPONENTI) //Fix 32392 PM
	  if(art != null && (art.getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST || art.getTipoParte() == ArticoloDatiIdent.KIT_GEST) && art.getTipoCalcPrzKit() == ArticoloDatiVendita.DA_COMPONENTI) //Fix 32392 PM
	      return true;
	  return false;
  }
  //Fix 26599 fine
  //Fix 26807 inizio
  public int deleteOwnedRigheDdt(){
 	int retDdtRig = 0;
 	String where = DdtRigTM.ID_AZIENDA + "='" + getIdAzienda() + "' AND " + DdtRigTM.ID_ANNO_DOC_VEN + "='" + getAnnoDocumento() + "' AND " + 
 	               DdtRigTM.ID_NUMERO_DOC_VEN + "='" + getNumeroDocumento() + "' AND " +
 		           DdtRigTM.ID_RIGA_DOC_VEN + "=" + getNumeroRigaDocumento() + " AND " + DdtRigTM.ID_DET_RIGA_DOCVEN + "=" + getDettaglioRigaDocumento(); 
 	try {
 		List l = DdtRigPrm.retrieveList(where, "", true);
 		if(l != null && !l.isEmpty()){
 			DdtRigPrm ddtRigPrm = (DdtRigPrm)l.get(0);
 			retDdtRig = ddtRigPrm.delete();
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
	   if(doc.getCausale().getTipoDocumento() != CausaleDocumentoVendita.NOTA_ACCREDITO)
		   return false;
	   if(!getArticolo().isArticLotto())
	   	   return false;
	   if (getArticolo().getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST)
		   return true;
	   if(isAbilitatoMovimentiMagazzino() && isAbilitatoAggiornamentoSaldi())
		   return false;
	   return true;
  }
  
  public boolean isControlloLottoDummyDaEscludereRigaSec(Articolo articoloSec) {
	   DocumentoVendita doc = (DocumentoVendita)getTestata();	 
	   if(doc.getCausale().getTipoDocumento() != CausaleDocumentoVendita.NOTA_ACCREDITO)
		   return false;
	   if(!articoloSec.isArticLotto())
	   	   return false;
	   Articolo articoloPrm = getArticolo();
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


//Fix 28653 - inizio
protected BigDecimal getScontoProvv2Pers(BigDecimal sconto) {
	return sconto;
}
//Fix 28653 - fine

//Fix 32832 Inizio
public boolean isDisattivaPropostaAutoLotti() {
		 return this.iDisattivaPropostaAutoLotti ;
	}
	
public void setDisattivaPropostaAutoLotti(boolean disattivaPropostaAutoLotti) {
		 this.iDisattivaPropostaAutoLotti = disattivaPropostaAutoLotti;
	}
//Fix 32832 Fine


//Fix 32851 Inizio
public void setForsaQtaRigheSec(boolean daClipBoard) {
	    this.iForsaQtaRigheSec = daClipBoard;
	    setDirty();
 }

public boolean isForsaQtaRigheSec() {
	    return iForsaQtaRigheSec;
 }
//Fix 32851 Fine 

//Fix 33663 - inizio
public ErrorMessage checkProvvigione1Agente() {
	  BigDecimal cento= new BigDecimal("100.00");
	  BigDecimal menoCento= new BigDecimal("-100.00");
	  if ( !hasDifferenzaPrezzoAgente() && getProvvigione1Agente() != null  
			  && (getProvvigione1Agente().compareTo(cento)==1 || getProvvigione1Agente().compareTo(menoCento)==-1) ){
		  
		  return new ErrorMessage("THIP_TN634");
	  }
	  return null;
}

public ErrorMessage checkProvvigione1Subagente() {
	  BigDecimal cento= new BigDecimal("100.00");
	  BigDecimal menoCento= new BigDecimal("-100.00");
	  if (!hasDifferenzaPrezzoSubagente() && getProvvigione1Subagente() != null 
			  && ((getProvvigione1Subagente().compareTo(cento) ==1 || getProvvigione1Subagente().compareTo(menoCento) ==-1) )) {
		
		  return new ErrorMessage("THIP_TN634");
	  }
	  return null;
}

//Fix 33663 - fine
//Fix 33992 Inizio
public void gestioneOriginePreferenziale() {
	ricalcolaOriginePreferenziale();
	azzeraCampiOriginePrefSeNecessario();
}
	public void ricalcolaOriginePreferenziale() {
		if (getArticolo() == null)
			return;

		if(! getArticolo().getArticoloDatiVendita().isGestOriginePref())
			return ;
		
		if(isRicalOriginePref()) {
			if(! this.getArticolo().isArticLotto()) {
				setGestOriginePref(this.getArticolo().getArticoloDatiVendita().isGestOriginePref());
				setIdNazioneOrgPref(this.getArticolo().getArticoloDatiVendita().getIdNazioneOrgPref());
				setDataFineValOrgPref(this.getArticolo().getArticoloDatiVendita().getDataFineValOrgPref());
			}else if(isGestOriginePref())
			{
				gestitoOriginePreferenzialeConRigheLotti();
			}
		}
	}


public void gestitoOriginePreferenzialeConRigheLotti() {
	List listLotti = new ArrayList();
	List righeLotti = this.getRigheLotto();
	Iterator i = righeLotti.iterator();
	while(i.hasNext()) {
		DocumentoVenRigaLottoPrm rigalot = (DocumentoVenRigaLottoPrm) i.next();
		if(rigalot.getLotto() != null && rigalot.getLotto().isGestOriginePref() && rigalot.getLotto().getDataFineValOrgPref() != null && rigalot.getLotto().getDataFineValOrgPref().compareTo(this.getTestata().getDataDocumento()) >= 0)
			listLotti.add(rigalot.getLotto());
	}
	
	if(listLotti.isEmpty()) {
		setGestOriginePref(this.getArticolo().getArticoloDatiVendita().isGestOriginePref());
		setIdNazioneOrgPref(this.getArticolo().getArticoloDatiVendita().getIdNazioneOrgPref());
		setDataFineValOrgPref(this.getArticolo().getArticoloDatiVendita().getDataFineValOrgPref());
	}else {
		Iterator j = listLotti.iterator();
		String idNazione = null;
		Date minDate = null;
		while(j.hasNext()) {
			Lotto lotto = (Lotto) j.next();
			if(idNazione == null)
				idNazione = lotto.getIdNazioneOrgPref();
			if(minDate == null)
				minDate = lotto.getDataFineValOrgPref();
			
			if( idNazione != null && lotto.getIdNazioneOrgPref() != null && !idNazione.equals(lotto.getIdNazioneOrgPref())) {
				setGestOriginePref(false);
				setIdNazioneOrgPref(null);
				setDataFineValOrgPref(null);
				return;
			}
			if(minDate == null || (lotto.getDataFineValOrgPref() != null && lotto.getDataFineValOrgPref().compareTo(minDate) <= 0))
				minDate = lotto.getDataFineValOrgPref();
		}
		if(idNazione != null) {
			setGestOriginePref(true);
			setIdNazioneOrgPref(idNazione);
			setDataFineValOrgPref(minDate);
		}
	}
}
	

	public ErrorMessage checkIdNazioneOrgPref() throws SQLException
	{
	  if(isGestOriginePref()  && ( getIdNazioneOrgPref() == null || getIdNazioneOrgPref().equals(""))) 
	    return new ErrorMessage("BAS0000000");
	  return null;
	}
		public ErrorMessage checkDataFineValOrgPref() throws SQLException
	{
	  if(isGestOriginePref()  && ( getDataFineValOrgPref() == null )) 
	    return new ErrorMessage("BAS0000000");
	  return null;
	}
	
	public void azzeraCampiOriginePrefSeNecessario() {
		if(!isGestOriginePref())
		{
			//setIdNazioneOrgPref(null);//Fix 35112
			setDataFineValOrgPref(null);
		}
	}
	//
	//Fix 33992 Fine

	//Fix 35171 inizio
	//Fix 43652 inizio //spostato nel super DocumentoVenditaRiga
	/*public BigDecimal getQuantitaInUmVen(){
		BigDecimal qtaInUMVen = new BigDecimal("0");
		if(this.getServizioQta() != null){
			qtaInUMVen = this.getServizioQta().getQuantitaInUMRif();
		}
		return qtaInUMVen;
	}*/
	//Fix 43652 fine
	//Fix 35171 fine
	
//Fix 38420 Inizio
public ErrorMessage checkNumDichIntentoDaUtilizz()  {
	if(getDichIntentoDaUtilizz() == null)
		return null;
	
		DocumentoVendita doc = (DocumentoVendita)getTestata();	 
		if(doc.getCausale().getTipoDocumento() == CausaleDocumentoVendita.NOTA_ACCREDITO)
			   return null;
	    String checkIVADichInt = ParametroPsn.getValoreParametroPsn("STD.FatturaVendita", "CheckIVADichInt");
	    if(checkIVADichInt != null && checkIVADichInt.equals("Y") && this.getAssoggettamentoIVA() != null && this.getAssoggettamentoIVA().getTipoIVA() != AssoggettamentoIVA.SOGGETTO_A_CALCOLO_IVA)
	    	return new ErrorMessage("THIP40T768");

			return null;
	}
//Fix 38420 Fine
//Fix 39531 Inizio
public void forsaProvisorio()
{
   if (getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO){
       setStatoAvanzamento(StatoAvanzamento.PROVVISORIO);
       setQtaPropostaEvasione(getQtaAttesaEvasione());
       QuantitaInUMRif qtaZero = new QuantitaInUMRif();
       qtaZero.azzera();
       setQtaAttesaEvasione(qtaZero);
       Iterator i = this.getRigheLotto().iterator();
       while(i.hasNext())
       {
      	 DocumentoRigaLotto lotto = (DocumentoRigaLotto) i.next();
      	 lotto.setQtaPropostaEvasione(lotto.getQtaAttesaEvasione());
      	 lotto.setQtaAttesaEvasione(qtaZero);
       }
     }	  
}
//Fix 39531 Fine
//Fix 40084 inizio
public ErrorMessage checkDichiarazioneMatricole(char tipoDocStorMat) {
	  if(getGenIntercompany())
		  return null;
	  if(isRecuperaMatricoliDaClipboard())//Fix 41132
		  return null;//Fix 41132

	  return super.checkDichiarazioneMatricole(tipoDocStorMat);
}
//Fix 40084 fine
//Fix 40598 Inizio
public void impostaDatiPerBene(ProposizioneAutLotto pal){
	    pal.setArticolo(getArticolo());	    
}
//Fix 40598 Fine

//Fix 41132 inizio
protected boolean isRecuperaMatricoliDaClipboard() {
    	Iterator i = this.getRigheLotto().iterator();
    	while(i.hasNext())
	    	{
    		DocumentoVenRigaLotto lotto = (DocumentoVenRigaLotto) i.next();
	   	 	if(lotto.getListaMatricoliDaRegistrare().isEmpty())
	   		 	return false;
	    	}
    	return true;
	}
//Fix 41132 fine

//Fix 43795 inizio
public void serveRicalProvv(SpecificheModificheRigheOrdVen specOV)
{
 	if (specOV.isRicalcolareProvvigioni())   
 	{
 		setServeRicalProvvAg(true); 
 		setServeRicalProvvSubag(true);
 	}
}
//Fix 43795 fine

//Fix Inizio 44409	
	public void impostaStatoAvanzamentoSecondarie() {

		boolean propagaStato = isInCopiaRiga;
		if (propagaStato) {
			// propagaStato
			Iterator iter = getRigheSecondarie().iterator();
			while (iter.hasNext()) {
				DocumentoVenRigaSec ordRigaSec = (DocumentoVenRigaSec) iter.next();
				ordRigaSec.setStatoAvanzamento(getStatoAvanzamento());
			}
		}
	}
//Fix 44409 Fine
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

