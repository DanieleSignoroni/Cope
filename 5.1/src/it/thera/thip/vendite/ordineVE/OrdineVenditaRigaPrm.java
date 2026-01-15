package it.thera.thip.vendite.ordineVE;

import it.thera.thip.acquisti.generaleAC.*;
import it.thera.thip.base.agentiProvv.*;
import it.thera.thip.base.articolo.*;
import it.thera.thip.base.azienda.*;
import it.thera.thip.base.catalogo.*;
import it.thera.thip.base.cliente.*;
import it.thera.thip.base.commessa.*;
import it.thera.thip.base.comuniVenAcq.*;
import it.thera.thip.base.comuniVenAcq.web.*;
import it.thera.thip.base.documenti.*;
import it.thera.thip.base.generale.*;
import it.thera.thip.base.interfca.*;
import it.thera.thip.base.listini.*;
import it.thera.thip.base.prezziExtra.*;
import it.thera.thip.cs.*;
import it.thera.thip.datiTecnici.configuratore.*;
import it.thera.thip.datiTecnici.distinta.*;
import it.thera.thip.datiTecnici.modpro.*;
import it.thera.thip.magazzino.generalemag.*;
import it.thera.thip.magazzino.saldi.*;
import it.thera.thip.vendite.contrattiVE.*;
import it.thera.thip.vendite.documentoVE.*;
import it.thera.thip.vendite.generaleVE.*;
import it.thera.thip.vendite.offerteCliente.*;
import it.thera.thip.vendite.ordineVE.web.*;
import it.thera.thip.vendite.prezziExtra.*;

import java.math.*;
import java.sql.*;
import java.util.*;

import com.thera.thermfw.base.*;
import com.thera.thermfw.cbs.*;
import com.thera.thermfw.collector.*;
import com.thera.thermfw.common.*;
import com.thera.thermfw.persist.*;
import com.thera.thermfw.security.*;
//Fine fix 3016

/*
 * Revisions:
 * Number  Date        Owner     Description
 *         13/09/2002  ME        Prima stesura
 *         20/02/2003  ME        Gestione date per righe secondarie
 *         21/02/2003  ME        Inserito completaBO per separare gestioNe date
 *                               con righe secondarie
 *         25/02/2003  ME        Eliminato metodo salvaRiga
 *         11/04/2003  ME        Spostati in OrdineRiga i metodi
 *                               abilitaCalcoloMovimentoPortafoglio,
 *                               disabilitaCalcoloMovimentoPortafoglio,
 *                               inizializzaListaMovimentiPortafoglio,
 *         13/05/2003  ME        Aggiunto metodo disabilitaCalcolaImportiRiga nella save
 *         14/05/2003  ME        Spostati in DocumentoOrdineRiga i metodi
 *                               salvaTestata, isSalvaTestata, setSalvaTestata
 *         22/05/2003  PM        Ho impostato gli attributi IdDocumentoMM e IdCommessa delle
 *                               righe secondarie e della riga omaggio con i valori degli stessi
 *                               campi della riga che glia ha generati
 *         22/05/2003  PM        Ho impostato l'attributo IdListino della riga omaggio con il valore
 *                               dello stesso campo della riga che lo ha generato
 *         06/06/2003  ME        Tolto calcolo dati vendita per righe omaggio
 *         30/09/2003  MSaccoia  Modificata gestione riga omaggio/offerta (VxO, V+O)
 * 01136   02/12/2003  PM
 * 01205   17/12/2003  PM
 *         23/12/2003  ME        Prima importazione in THIP 1.1 con nuova generazione
 *                               righe secondarie
 *         23/12/2003  ME        Inserita gestione modelli produttivi per righe kit
 *         13/01/2004  ME        Modificate chiamate a metodi getEsplosioneNodoModello e
 *                               generaRigheSecondarieEsplosioneModello
 * 01406   09/02/2004  ME
 * 01390   10/02/2004  DB
 * 01420   10/02/2004  PM
 * 01425   12/02/2004  DB
 * 01480   20/02/2004  CHAKHARI  modification in the method saveOwnedObjects,
 *                               save directly the oneTomany without using
 *                               getOneTomany().save(rc).
 * 01988   14/05/2004  PM
 * 01918   14/05/2004  ME
 * 02029   24/05/2004  ME
 * 02151   23/06/2004  Mekki     Cambiare tipo de Quantita di Esplosione Distinta:int--->BigDecimal.
 * 02001   24/05/2004  DZ        Modificato calcolaImportiRiga in seguito a spostamento di calcolaImposta
 *                               da DocumentoOrdineRiga a ImportiRigaDocumentoOrdine.
 * 02153   23/06/2004  DZ        Rivisto stornaImportiRigaDaTestata per aggiornare tutti i nuovi valori.
 *                               Corretto calcolaImportiRiga per nullPointer sui nuovi valori negli ordini vecchi.
 * 02380   07/09/2004  ME        Segnalazione errore per associazione catalogo/articolo
 *                               inesistente
 * 02407   10/09/2004  DZ        Aggiunto metodo di servizio getNotNullValue utilizzato in stornaImporti...
 * 02563   11/10/2004  ME        Aggiunto metodo cambiaArticolo
 * 02614   14/10/2004  DB
 * 02636   15/10/2004  DB
 * 02900   30/11/2004  MG        Gestine righe di spesa percentuali
 * 03197   25/01/2005  ME        Aggiunti attributi iServeRicalcoloProvvAgente e
 *                               iServeRicalcoloProvvSubagente
 * 3242    03/02/2005  GScarta
 * 03016   20/01/2005  DB
 * 03262   10/02/2005  SL        Tolto un controllo duplicato nel metodo calcolaImportiRiga();
 *                               Il controllo era "if(valoreConsegnato == null) ...";
 * 03362   10/03/2005  MN        Modificato il metodo generaRigaSecondariaModello(), aggiunto
 *                               set della UMSec.
 * 03489   04/04/2005  MN        Migrazione 2.0
 * 03611   18/04/2005  ME        Cambiato controllo su ricalcolo qta righe secondarie
 * 03659   22/04/2005  ME        Arrotondamento delle quantità delle righe
 *                               secondarie generate da distinta
 * 03700   02/05/2005  ME        Nuova gestione righe secondarie
 * 03738   09/05/2005  ME        Modifiche su recupero provv. agenti su scala sconti
 * 03770   13/05/2005  ME        Modifiche per miglioramento prestazioni
 * 03814   23/05/2005  MN        Modificato il metodo creaRigaOmaggio().
 * 03880   06/06/2005  PM        Nella generazione automatoca della riga omaggio
 *                               non viene riportato il flag "Non fatturare".
 * 03230   28/04/2005  ME        Aggiunti metodi getRigaDestinazionePerCopia e
 *                               copiaRiga e attributo iGeneraRigheSecondarie
 * 03929   16/06/2005  ME        Modificato ricalcolo prezzo da righe secondarie
 * 03769   17/05/2005  BP        Sistemazione attributo iPrezzoRiferimento
 * 03953   21/06/2005  ME        Modificato metodo copiaRiga: anticipato settaggio
 *                               riga primaria su righe secondarie
 * 03954   21/06/2005  DZ        Modificato metodo gestioneKit
 * 04060   05/07/2005  ME        Nel caso di articolo kit con tipo calcolo prezzo
 *                               da componenti non fa scattare il recupero dati vendita
 *                               alla save. Forzato il calcolo del prezzo da righe
 *                               secondarie nella save.
 * 04191   28/07/2005  ME        Modificato calcolo prezzo anche per esplosione modello
 * 04348   20/09/2005  MG        Implementazione gestione sconti provvigione agente
 * 04356   10/10/2005  DZ        Aggiunto COntoTrasformazione.
 * 04453   10/10/2005  DB        Modificato reperimento coeff. impiego e qta totale
 *                               Modificata UMRif per righe secondarie (impostava DefAcq in espl distinta)
 *                               Passata cfg in esplosione distinta
 * 04486   19/10/2005  MN        Regressione della fix 3769
 * 04500   19/10/2005  MN
 * 04607   11/11/2005  DZ        Generazione righe secondarie per articolo kit: se non esistono modPro o distinta,
 *                               la riga prm deve essere salvata ugualmente deve essere dato un warning NON bloccante
 *                               (modificato generaRigheKit, aggiunti checkAll e checkRigheSecondarie).
 * 04669  22/11/2005   DZ        checkRigheSecondarie: modifiche per ottimizzazione performances.
 * 04670  02/12/2005   MN        Gestione Unità Misura con flag Quantità intera.
 * 04749  07/12/2005   ME        Aggiunta logica per propagazione dati su righe secondarie
 * 04656  19/12/2005   MG        Aggiunto metodo creaLottiAutomatici (ridefinizione in erede)
 * 04814  22/12/2005   DZ        checkRigheSecondarie: aggiunto test articolo != null per CM.
 * 04858  05/01/2006   ME        Omaggi: sistemato calcolo delle quantità da assegnare
 *                               alla riga omaggio
 * 04976  30/01/2006   ME        Aggiunti controlli per evitare che cada l'importatore
 * 05110  01/03/2006   ME        Eliminata modifica introdotta da fix 3953:
 *                               alle righe secondarie del documento di origine
 *                               veniva assegnata la riga primaria di destinaz.
 *                               In copia aggiunta gestione particolare per righe
 *                               secondarie.
 * 05102  01/03/2006   ME        Aggiunti controlli su righe annullate per
 *                               calcolo/storno totali testata
 * 05117  14/03/2006  GN         Correzione nella gestione delle unità di misura con flag Quantità intera
 * 05330  10/04/2006  DBot       Utilizzo readOnlyElementWithKey per gestione corretta classi Cacheable
 * 05601              MN         Contratti Vendita
 * 05634  04/07/2006  ME         Aggiunto metodo annullaOldRiga
 * 05742  26/07/2006  ME         Modificato metodo riapriRiga
 * 05823  28/08/2006  ME         Modificata SELECT_STATO_EVASIONE_RIGA
 * 05772  05/09/2006  MN         Ridefinito il metodo isAggiornaQtaOrdSuContratto: se la save della
 *                               riga ordine viene chiamata dal contratto non è necessaria aggiornare le qta
 *                               sul contratto e richiamare la save (operazioni eseguite nella classe OrdineRiga).
 * 05990  09/10/2006  DB
 * 06144  02/11/2006  EP         Sostituita new con Factory.
 * 06016  07/11/2006  MN         Gestione Piano consegne.
 * 06150  30/10/2006  PM         Aggiunto metodo impostazioniPerCopiaRiga
 * 06204  13/11/2006  MG         Inserire reupero provv. agenti nel caso kit (non gestito a magazzino)
 * 06265  20/11/2006  MG         Modifica impostazioniPerCopiaRiga
 * 06323  28/11/2006  MN         In manutenzione di una riga ordine con unico
 *                               lotto effettivo, il controllo della disponibilità del lotto
 *                               deve essere eseguito anche alla save della riga ordine.Inoltre
 *                               il messaggio visualizzato(Forzatura del prelievo) deve essere
 *                               condizionato alla autorizzazioe dell'utente sul task "Forzatura
 *                               prelievo lotti"
 * 06428  19/12/2006  MN         Modificato il metodo controllaDispUnicoLottoEffettivo, corretta la
 *                               chiamta al metodo del calcolo della disponibilità (quello vecchio non
 *                               tenev conto della qta in ordine a fornitore)
 * 06481  29/01/2007  MG         Aggiunta gestione sconto fine fattura al lordo in metodo calcolaImportiRiga
 * 06920  19/03/2007  MN         Nel caso di unico lotto "effettivo", deve essere controllata la
 *                               disponibilità alla save della riga.
 * 06754  28/02/2007  MG         Gestione riga secondaria da fatturare
 * 06965  21/03/2007    MN     Modificate le chiamate ai metodi di calcolo
 *                             giacenza/disponibilità su ProposizioneAutLotto.
 * 06983   23/03/2007  MN       Aggiornamento del contratto materia prima nel caso di riga
 *                              ordine saldata manualemente e riaperta manualemente.
 * 07098   10/04/2007  DB
 * 07183  16/04/2007  DBot      Aggiunti controlli per accprn manuale
 * 08003  10/10/2007  MN        La quantità ordinata del contratto non deve essere
 *                              aggiornata se la riga ordine aperto è in stato ANNULLATO.
 * 07876  19/09/2007  DZ        checkRigheSecondarie: aggiunto test getMagazzino != null per CM.
 * 08127  25/10/2007  MN        In manutenzione di una riga ordine aperto ANNULLATA, al salvataggio il contratto
 *                              non deve essere aggiornato. Correzione della fix 8003.
 * 08393  10/12/2007  MN        Se la riga ordine è gestita a contratto, le condizioni di vendita
 *                              devono essere recuperate dal contratto.
 * 08508  15/01/2008  MN        In fase di salvataggio di una riga ordine, se la causale riga è gestita
 *                              a contratto deve essere recuperata la riga del piano consegne corrispondente.
 *                              Se la riga del PDC è diversa dalla riga ordine (qta, data rich, data conf, stato e stato av),
 *                              la riga del PDC deve essere aggiornata con i dati della riga ordine.
 * 08495  09/01/2008  MG        Modificato metodo save per gestione righe omaggio es.art.15
 * 08659  05/02/2008  MG        Aggiunta gestione dati CA in generaRigheKit
 * 08815  06/03/2008  MN        Nel metodo isRigaPDCDaAggiornare() deve essere testato che la dataConsRichiesta
 *                              e la dataConsConf della riga ordine siano diverse da null prima di eseguire la
 *                              compareTo(..).
 * 08863  14/03/2008  MN        Modificato il metood calcolaDatiVendita(), i dati di vendita devono essere
 *                              recuperati dal contratto solo quando le condizioni di vendita sono specifiche.
 *                              Questo metodo viene chiamato alla save della riga ordine , quindi dal passaggio del
 *                              piano consegne da NUOVO a CORRENTE.
 * 08913  25/03/2008  OV        Recupero dati di contabilità analitica al momento del salvataggio (per inserimenti batch)
 * 08977  04/04/2008  MN        Modiifcato il metodo generaRigheKit(...) nel calcolo della qta con gestione a qta intere
 *                              veniva considerato l'articolo della riga prm e non l'articolo del kit.
 * 08920  15/04/2008  EP        Aggiunto metodo di controllo checkStatoAnnullato() che
 *                              verifica se è possibile porre lo stato 'ANNULLATO' alla riga
 * 09061  17/04/2008  OV        Rimosso salvataggio dopo recupero dati CA
 * 09577  22/07/2008  MN        Cambiato il costruttore BigDecimal della costante ZERO_DEC.
 * 															Modificato il metodo isRigaPDCDaAggiornare(PianoConsegneRigaConsegna)(),
 *                              testata anche la qtaResidua del piano consegne.
 * 09588  23/07/2008  MN        Ridefinito metodo checkDelete(), la riga ordine non
 *                              può essere eliminata se esiste almeno una riga documento collegata.
 * 09671  25/08/2008  PM        Se una riga sec ha lo stesso articolo della riga prm e
 *                              il coefficente è 1 allora la sua quantita deve
 *                              essere uguale a quella della riga prm.
 * 09754   10/09/2008  MN       Modificato il metodo gestioneSaldoRiaperturaManuale(), il contratto mat. prima
 *                              deve essere aggiornato solo quando la riga ordine è stata saldata manualmente.
 * 10476   24/03/2009  MN       Aggiunta gestione degli attributi CommentEDI e ArticoloEDI.
 * 10719   15/04/2009  PM       gestione offerte cliente
 * 10750   29/04/2009  MG       doppia gestione provvigione scala sconti
 * 10776  30/04/09      MN     Implementato metodo checkQtaDisponibileContrMatPrima() ridefinito negli eredi, per la verifica
 *                             della qta disponibile sul contratto materia prima (gestione prezzi extra)
 * 10962   18/06/2009   AB            Gestione intercompany
 * 10955   17/06/2009  Gscarta   modificate chiamate a convertiUM dell'articolo per passare la versione
 * 11069  06/09/2009  MN        Spostati alcuni metodi della gestione intercompany
 * 11148  20/07/2009  MN       Generazione righe di spesa nella gestione intercompany
 * 11123   13/07/2009  DB
 * 11259  04/09/2009  MN       Intercompany: copia righe ordine
 * 11170  17/09/2009  PM
 * 11529  26/10/2009  ME       Propagazione commessa su righe secondarie al salvataggio
 * 11124  30/10/2009  GScarta  Nuovo metodo forzaRicalcoloDatiVendita()
 * 11707  29/12/09    MN       Implementato il metodo runGenerazioneRigheSec per la gestione della
 *                             generazione delle righe secondarie. L'implemento è stato eseguito per
 *                             agevolare lo sviluppo delle personalizzazioni.
 * 12148  08/02/2010  RH       modificato commenti
 * 12405  12/04/2010  DZ       save: modificato test per attivazione IC in caso di riga spesa.
 * 12584  04/05/2010  PM       Ridefinito metodo isCreazioneAutomaticaLottiAbilitata()
 * 12610  10/05/2010  PM       Non è possibile salvare una riga ordine già saldata e già annullata. Fix che completa la fix 12547
 * 12508  20/04/2010  DBot     Aggiunta la gestione dei pesi e del volume
 * 12673  23/06/2010   M.Anis  il metodo getUnicoLottoEffettivo() ridefinito in su erede
 * 13110  30/08/2011  DBot     Aggiunto test su attivazione calcolo pesi e volume
 * 13911   28/01/2011  PM       Migliorata la gestione degli warning nell'intercompany
 * 13494  12/11/2010  OC       corretto totale dal ordine di vendita
 * 13494  04/02/2011  MBH      trasferisci del metodo stornaImportiRigaDaTestata ha DocumentoVenRiga
 * 13515  30/11/2010  TF       Calcolo provvigioni su prezzo extra
 * 14225  31/03/2011  OC       Modifica la generazione di righe OrdineSec a partire di una distinta per copiare il campo Nota e DacumentoMM
 * 12572  13/05/2010  GScarta  Nuova gestione numero imballo
 * 14738  29/06/2011  DBot     Integrazione a standard fix 12572
 * 14931  30/08/2011  DBot     Aggiunta gestione pesi/volume ceramiche
 * 14727  14/07/2011  RA       Gestione DescrizioneExtArticolo
 * 14670  20/07/2011  Linda    Modificare il metodo generaRigheKit()
 * 16267  17/05/2012  MZ       In checkArticoloCatalogoCompat tolto controllo compatibilità su UmPrm e Umsec
 * 16024  30/03/2012  AA       Aggiunto verifica del delete di OrdineAcquistoRigaPrmIC
 * 16508  11/06/2012  AYM      Corretto il problem di salva ordine che arriva da evasione di un'offerta(un errore di optimistic lock fallito.)
 * 16754  11/09/2012  AYM      Corretto il problema "Entità non trovata" nella  elimina  di ordine vendita in  caso di presenza di riga omaggio collegata.
 * 16773  26/09/2012  TF       Inserire nella save dopo che è stata fatta l'assegnazione delle chiavi.
 * 16440  19/10/2012  Linda    Fix anticipato 16439 e 13712.
 * 17374  25/01/2013  AYM      Non  è possibile aggiungere una riga o elimina riga origine da piano di consegna.
 * 16893  22/03/2013  Ichrak   Gancio per personalizzazioni
 * 17697  17/04/2013  OC       Aggiunto il metodo impostaRilascioOrdineProdSecondarie
 * 18798  08/01/2014  CO       Gancio per personalizzazioni
 * 19628  23/04/2014  Linda    Modificato metodo aggiornaProvvigioni().
 * 18703  14/11/2013  Ichrak   Aggiungere il metodo per calcolo importi di righe spese percentuale
 * 19757  22/05/2014  Ichrak   Correzione del cast su EspNodoArticoloBase
 * 18603  23/10/2013  Linda    Modifica il metodo isRigaAContratto().
 * 18156  25/06/2013  MA       L'autorizzazione alla "forzatura lotto" in verde chiaro equivale alla non autorizzazione
 * 17639  14/05/2013  TF       Agevolazioni per personalizzazioni : metodo da protected a public
 * 20651  13/10/2014  AYM      Aggiunto il controllo di  checkQtaDisponibileContrMatPrima() nel caso di nuovo .
 * 20387  26/12/2014  Linda    Se in copia di una riga primaria che possiede righe secondarie l'utente cambia la configurazione, al salvataggio il sitema deve emettere un warning.
 * 21389  20/05/2015  RH       modificato check all caso di creazione OrdineVen da EvasioneOff con Causale con WF e creazione di scadenza auto passagio di stato WF automatico
 * 18753  18/11/2013  Linda    Modificato il metodo save().
 * 19960  22/07/2014  AYM      Sulle righe del piano quando questo passa in stato STORICO devono essere cancellati i riferimenti alla riga ordine.
 * 20569  07/04/2015  AA       Aggiunto il controllo sulla commessa nel caso di cancellazione
 * 22229  29/09/2015  Linda    Modificato metodo getProvvigioneDaSconto().
 * 22839  15/01/2016  Linda    Redefine metodo getEntityRiga().
 * 23345  04/04/2016  Linda    Aggiunto metodo controlloRicalcoloCondizioniVen().
 * 23709  03/06/2016  OCH      Se in modifica di una riga primaria che possiede righe secondarie l'utente cambia la configurazione, al salvataggio il sitema deve emettere un warning.
 * 24190  20/09/2016  OCH      Nella generazione delle righe kit se l'attributo KitRecuperaMagDaMod della causale e a true deve impostare valore del magazzino con il valore di magazzino della riga di esplosione 
 * 19453  23/09/2016  ME       Gancio per personalizzazioni in generaRigaSecondariaModello
 * 24252  03/10/2016  OCH      CalcolaDatiVendita per righe secondarie nel caso da CM e l'ordine vendita è isRicalcoloDatiVendita 
 * 24299  10/10/2016  Jackal   Gancio per consentire personalizzazioni in 
 *                             calcolo sconto su scala sconti
 * 24493  11/11/2016  OCH      Correzzione Fix 24190
 * 24613  05/12/2016  Linda    Gestione il salvataggio del dettaglio riga valore configurazione.
 * 25004  21/02/2017  OCH      Recuperato assoggIva da ArticoloFornitore se valorizzato
 * 25214  19/03/2017  PM	   Se inserisco una riga con un articolo kit non gestito il cui prezzo è dato dalla somma dei componenti al salvataggio della riga non vengono calcolate le provvigioni 2 dell'agente e del subagente.
 * 26145  17/07/2017  Jackal   Gancio per consentire personalizzazioni in 
 *                             calcolo sconto su scala sconti
 * 26488  12/10/2017  LP       Integrazione CONAI con dichiarazione intento.
 * 26599  03/11/2017  Linda    La fix 25214 deve valere solo se l'articolo è kit non gestito a magazzino. 
 * 27649  02/07/2018  LTB     Aggiunta un controllo bloccante che impedisca di inserire una riga con quantità non intera se l'um è gestita a quantità intera.  
 * 27720  11/07/2018  Jackal   Metodo calcolaImportiRiga da protected a public per personalizzazioni 
 * 27616  03/09/2018  LTB     Se effettuo una riga d'ordine di vendita con un articolo kit con "tipo calcolo prezzi = dal costo dei componenti per markup", 
 * 														deve valorizzare il campo "provvigione 2 agente", nemmeno al salvataggio.   
 * 28653  22/02/2019  Jackal    Aggiunti ganci per personalizzazioni su calcolo provvigione 2 al salvataggio
 * 29632  04/09/2019  LTB     Aggiungere il controllo sul SmartGrid  
 * 30193  13/12/2019  SZ	  Gestione della configurazione nel caso di articoli di tipo ceramico.
 * 30871  06/03/2020  SZ	  6 Decimale.
 * 32392  02/12/2020  PM      Creando una nuova riga ordine/doc vendita se l'articolo è un kit gestito a magazzino e il prezzo è data dalla somma dei componenti, se si salva la riga senza passare dal tab dei prezzi la provvigione 2 non viene recuperata. 
 * 32713  13/01/2021  SZ	  Migliorato il gestione Saldo Riapertura Manuale.	 
 * 32227  01712/2020  DB 
 * 33663  26/05/2021  YBA     Aggiungi i metodi checkProvvigione1Agente e checkProvvigione1Subagente().
 * 33762  08/06/2021  YBA      Correggere il problema che durante la  copia una riga documento di vendita, contenente un kit, e modificando la data della riga primaria la modifica non viene trasmessa alle righe secondarie
 * 33905  02/07/2021  SZ	  Nel caso di kit non gestito a magazzino il costo deve essere la somma dei costi delle righe secondarie
 * 34161  24/08/2021  YBA     Correggere il metodo gestioneDateRigheSecondarie()
 * 34991  11/01/2022  SZ	  Errato calcolo stato evasione testata ordine i caso di accodamento
 * 36208  05/07/2022  SZ	  agganciare la stessa testata alle righe per evitare rilettura continua delle testate	 
 * 36857  25/10/2022  YBA     Modificato metodo getProvvigioneDaSconto(). 
 * 37217  03/12/2022  YBA     La copia di un ordine di vendita con articoli che gestiscono i lotti unitari non copiare i lotti unitari dall'ordine di origine
 * 37244  08/12/2022  YBA     Corregere il problemea perché del copia un ordine cambiando la causale e le codizioni di vendita sono impostate Da documento gli sconti testata e la provvigione 1 delle righe non deveno essere riprese dalla testata.
 * 39402  24/07/2023  SZ      Scale errato se il database ha le quantità a 6 decimali
 * 39531  25/08/2023  SZ	  Aggiunto il generazione Automatica lotto unitario. 	
 * 40598  04/12/2023  SZ	  Generazione bene nel generazione Automatica lotto unitario.	
 * 37420  22/12/2022
 * 39017  04/07/2023  SBR     Riallineamento Intellimag al 4.7.25
 * 40083  26/10/2023  SBR     Aggiunto metodo isTipoRigaDaTrassmeso()	
 * 40694  13/12/2023  SBR	  Varie modifiche intellimag
 * 40598  04/12/2023  SZ	  Generazione bene nel generazione Automatica lotto unitario.
 * 40936  11/01/2024  HG      Mantenere la quantità in um rif inserita in ordine riga 	
 * 41393  20/02/2024  SZ	  i dati relativi a peso lordo e netto di riga vengono reperiti sempre dai dati tecnici dell'articolo, pur in presenza di dati differenti specificati nella versione indicata nella riga del documento.
 * 41316  09/02/2024  SBR     Riallineamento Intellimag al 5.0.2
 * 41868  26/03/2024  TA      Aggiunto il metodo calcolaDateRigheSecondarie(rigaSec)
 * 43361  23/09/2024  TA      Gli sconti di riga non vengono RIPORTATE nelle righe del documento in fase di ordine e/o doc. di vendita, scegliendo cliente + cantiere
 * 43795  05/11/2024  KD      redifine serveRicalProvv  
 * 44166  05/12/2024  SZ	  aggiongere il metodo checkStatoSospeso
 * 44409  25/12/2024  TA      Corretto l'anomalia si duplica una riga d'ordine che è in stato "definitiva", si imposta a "Provvisoria", le righe secondarie rimangono con stato "Definitiva"
 * 44522  15/01/2025  TA      Recupera i dati di Agente, Sub-Agente e Responsabile vendite.
 * 44784  02/05/2025  RA	  Rendi la ConfigArticoloPrezzo persistent
 * 45398  02/04/2025  TA      Sistema NullPointerException
 * 45246  16/05/2025  SZ	  Attributi Da Usare nel batch ricalcolaPrezzi 
 * 46088  09/06/2025  TA      Togliere le seguenti righe
 */

//public class OrdineVenditaRigaPrm extends OrdineVenditaRiga implements RigaPrimaria {//Fix 24613
public class OrdineVenditaRigaPrm extends OrdineVenditaRiga implements RigaPrimaria,RigaConDettaglioConf {//Fix 24613

  //Fix 23345 inizio
  //ProvenienzaPrezzo
  public static final char PROV_PREZZO_LISTINO_GENERICO = '1';
  public static final char PROV_PREZZO_LISTINO_CLIENTE = '2';
  public static final char PROV_PREZZO_LISTINO_ZONA = '3';
  public static final char PROV_PREZZO_LISTINO_CATEG_VEN = '4';
  //Fix 23345 fine
//Inizio fix 1420 PM
  protected static final String SELECT_STATO_EVASIONE_RIGA =
      "SELECT " +
      OrdineVenditaRigaTM.ID_RIGA_ORD + ", " +
      OrdineVenditaRigaTM.STATO_EVASIONE + " " +
      "FROM " + OrdineVenditaRigaPrmTM.TABLE_NAME + " " +
      "WHERE " +
      OrdineVenditaRigaTM.ID_AZIENDA + "=? AND " +
      OrdineVenditaRigaTM.ID_ANNO_ORD + "=? AND " +
      OrdineVenditaRigaTM.ID_NUMERO_ORD + "=? AND " +
      OrdineVenditaRigaTM.STATO + "<>'" + DatiComuniEstesi.ANNULLATO + "'";	//Fix 5823

  protected static CachedStatement cSelectStatoEvasioneRiga =
      new CachedStatement(SELECT_STATO_EVASIONE_RIGA);
//Fine fix 1420 PM

  //Fix 16440 inizio
  // Inizio 9588
  /*protected static final String SQL_RIC_DOCVEN_RIG =
  	"SELECT COUNT(*) FROM " + DocumentoVenRigaPrmTM.TABLE_NAME +
  	" WHERE "+DocumentoVenRigaPrmTM.ID_AZIENDA + "=? AND " +
  	DocumentoVenRigaPrmTM.R_ANNO_ORD+"=? AND " 						 +
  	DocumentoVenRigaPrmTM.R_NUMERO_ORD+"=? AND "					 +
  	DocumentoVenRigaPrmTM.R_RIGA_ORD+"=? AND "						 +
  	DocumentoVenRigaPrmTM.R_DET_RIGA_ORD+"=?";
  */
  //protected static final CachedStatement csRicDocVenRig = new CachedStatement(SQL_RIC_DOCVEN_RIG);
   protected static CachedStatement csRicDocVenRig ;
   //Fix 19960 inizio
   protected static final String SELECT_RIGA_PIANO_CONS =
  		  "SELECT COUNT(*) FROM "+PianoConsegneRigaArticoloTM.TABLE_NAME +" TES ,"+PianoConsegneRigaConsegnaTM.TABLE_NAME +" RIG "+
                               " WHERE "+
  		  		                     "TES."+PianoConsegneRigaArticoloTM.ID_ANNO_PNC  +"= RIG."+PianoConsegneRigaConsegnaTM.ID_ANNO_PNC+" AND " +
  		  		                     "TES."+PianoConsegneRigaArticoloTM.ID_NUMERO_PNC+"= RIG."+PianoConsegneRigaConsegnaTM.ID_NUMERO_PNC +" AND " +
  		  		                     "TES."+PianoConsegneRigaArticoloTM.ID_RIGA_PNC  + "= RIG."+PianoConsegneRigaConsegnaTM.ID_RIGA_PNC +" AND " +
  		  		                     "TES."+PianoConsegneRigaArticoloTM.STATO_RIGA_PNC+ "='"+PianoConsegne.ST_PIANO_CORRENTE + "' AND "+
  		  		                     "RIG."+PianoConsegneRigaConsegnaTM.ID_AZIENDA+ "=? AND " +
  		  		                     "RIG."+PianoConsegneRigaConsegnaTM.ID_ANNO_ORD+" =? AND "+
  		  		                     "RIG."+PianoConsegneRigaConsegnaTM.ID_NUMERO_ORD+"=? AND "+
  		  		                     "RIG."+PianoConsegneRigaConsegnaTM.ID_RIGA_ORD+" =? AND "+
  		  		                     "RIG."+PianoConsegneRigaConsegnaTM.ID_DET_RIGA_ORD +"=?"; ;
   protected static CachedStatement cSelectRigaPianoConsegneRiga = new CachedStatement(SELECT_RIGA_PIANO_CONS);
   //Fix 19960 fine

  // Fine 9588
  //Fix 16440 fine

  //Fix 20569 Inizio
  protected static final String UPDATE_COM_RIGA_ORDVEN =
      "UPDATE " + CommessaTM.TABLE_NAME +
      " SET "   + CommessaTM.STATO_AVANZAMENTO + " = ? " +
      " , "     + CommessaTM.R_RIGA_ORD + " = ? " +
      " WHERE " + CommessaTM.ID_AZIENDA + " = ? " +
      " AND "   + CommessaTM.ID_COMMESSA + " = ? ";
  public static CachedStatement cUpdateCommessaRigaOrdVenStm = new CachedStatement(UPDATE_COM_RIGA_ORDVEN);

  protected boolean iErrorForzabileClear = false;
  //Fix 20569 Fine

  protected static final BigDecimal ZERO_DEC = new BigDecimal("0"); // Fix 9577

  protected boolean iSalvaRigheSecondarie = true;
  protected Integer iDettaglioRigaCollegata;

  //Fix 3197 - inizio
  protected boolean iServeRicalcoloProvvAgente = false;
  protected boolean iServeRicalcoloProvvSubagente = false;
  //Fix 3197 - fine

  private OrdineVenditaRigaPrm rigaOmf;
  //Attributimm

  protected OneToMany iRigheSecondarie =
      new OneToMany(
          it.thera.thip.vendite.ordineVE.OrdineVenditaRigaSec.class,
          this,
          15,
          true
      );

  //Fix 3230 - inizio
  protected boolean iGeneraRigheSecondarie = true;
  //Fix 3230 - fine

  protected boolean iDisabilitaRigheSecondarieForCM = false;  //...FIX04607 - DZ

  protected boolean iSaveFromPDC = false; // Fix 8508

  // Fix 8913 - Inizio
  public boolean isBOCompleted = false;
  // Fix 8913 - Inizio

  // Inizio 10476
  protected String iCommentEDI;
  protected String iIdArticoloEDI;
  // Fine 10476
  

  protected boolean iDesattivaInitializeOwnedObjects = false;//Fix 36208

  /**
   * Costruttore
   */
  public OrdineVenditaRigaPrm() {
    super();
    iRigheLotto = new OneToMany(OrdineVenditaRigaLottoPrm.class, this, 15, true);
    iRigaCollegata = new Proxy(OrdineVenditaRigaPrm.class);
    //Fix 2563 - inizio
    datiArticolo =
        (DatiArticoloRigaVendita) Factory.createObject(DatiArticoloRigaVendita.class);
    //Fix 2563 - fine
    iOffertaClienteRiga = new Proxy(OffertaClienteRigaPrm.class); //Fix 10719 PM
  }

  //--------------------------------------------------------//

  //Metodi get/set attributi

  /**
   * Valorizza l'attributo IdAzienda.
   * Overwriting del metodo della superclasse DocumentoRiga.
   */
  public void setIdAzienda(String idAzienda) {
    super.setIdAzienda(idAzienda);
    iRigheSecondarie.setFatherKeyChanged();
  }

  /**
   * Valorizza l'attributo AnnoDocumento.
   * Overwriting del metodo della superclasse DocumentoRiga.
   */
  public void setAnnoDocumento(String annoDocumento) {
    super.setAnnoDocumento(annoDocumento);
    iRigheSecondarie.setFatherKeyChanged();
  }

  //--------------

  /**
   * Valorizza l'attributo NumeroDocumento.
   * Overwriting del metodo della superclasse DocumentoRiga.
   */
  public void setNumeroDocumento(String numeroDocumento) {
    super.setNumeroDocumento(numeroDocumento);
    iRigheSecondarie.setFatherKeyChanged();
  }

  //--------------

  /**
   * Valorizza l'attributo NumeroRigaDocumento.
   * Overwriting del metodo della superclasse DocumentoBaseRiga.
   */
  public void setNumeroRigaDocumento(Integer numeroRigaDocumento) {
    super.setNumeroRigaDocumento(numeroRigaDocumento);
    iRigheSecondarie.setFatherKeyChanged();
  }

  //--------------

  /**
   * Valorizza l'attributo DettaglioRigaDocumento.
   * Overwriting del metodo della superclasse DocumentoBaseRiga.
   */
  public void setDettaglioRigaDocumento(Integer dettaglioRigaDocumento) {
    this.iDettaglioRigaDocumento = dettaglioRigaDocumento;
    setDirty();
  }

  //--------------------------------------------------------//
  //Metodi per gestione OneToMany

  public List getRigheSecondarie() {
    return getRigheSecondarieInternal();
  }

  protected OneToMany getRigheSecondarieInternal() {
    if (iRigheSecondarie.isNew())
      iRigheSecondarie.retrieve();
    return iRigheSecondarie;
  }

  public boolean initializeOwnedObjects(boolean result) {
    //Fix 36208 Inizio
     if(isDesattivaInitializeOwnedObjects())
	return true;
    //Fix 36208 Fine

    result = super.initializeOwnedObjects(result);
    // Fix 3016
    if (this.getTestata() != null) {
      Cantiere can = ( (OrdineVenditaTestata)this.getTestata()).
          getCantiereTestata();
      if (can != null && this.getTipoRiga() == TipoRiga.MERCE &&
          getArticolo().getArticoloDatiVendita().getSchemaPrzVen() != null &&
          this.getArticolo().getArticoloDatiVendita().getSchemaPrzVen().
          getTipoSchemaPrz() == SchemaPrezzo.TIPO_SCH_ACQ_VEN) {
        this.setConCantiere(true);
      }
    }
    return iRigheSecondarie.initialize(result);
    // Fine fix 3016
  }

  public int saveOwnedObjects(int rc) throws SQLException {
    rc = super.saveOwnedObjects(rc);
    if (isSalvaRigheSecondarie())
      //rc = getRigheSecondarieInternal().save(rc);
      rc = iRigheSecondarie.save(rc);
    return rc;
  }

  public int deleteOwnedObjects() throws SQLException {
    int ret = super.deleteOwnedObjects();
    return getRigheSecondarieInternal().delete(ret);
  }

  //--------------------------------------------------------//

  /**
   * setEqual
   * @param obj
   * @throws CopyException
   */
  public void setEqual(Copyable obj) throws CopyException {
    super.setEqual(obj);
    OrdineVenditaRigaPrm rigaPrm = (OrdineVenditaRigaPrm) obj;
    iRigheSecondarie.setEqual(rigaPrm.iRigheSecondarie);
  }

  //--------------------------------------------------------//
  //Logica

  /**
   * Completamento dati form in NEW
   */
  public void completaBO() {
    super.completaBO();

    OrdineVendita testata = (OrdineVendita) getTestata();

    setSequenzaRiga(RigaVendita.getNumeroNuovaRiga(testata));
    // Inizio 5601
    gestioneContratti();
    // Fine 5601

    CausaleRigaVendita causale = getCausaleRiga();
    //Fix 2029 - aggiunto controllo su null
    if (causale != null && causale.getTipoRiga() != TipoRiga.SPESE_MOV_VALORE) {
      setDataConsegnaProduzione(testata.getDataConsegnaProduzione());
      setSettConsegnaProduzione(testata.getSettConsegnaProduzione());
    }

    setDataConsegnaRichiesta(testata.getDataConsegnaRichiesta());
    setDataConsegnaConfermata(testata.getDataConsegnaConfermata());
    setSettConsegnaRichiesta(testata.getSettConsegnaRichiesta());
    setSettConsegnaConfermata(testata.getSettConsegnaConfermata());

    //Fix 2029 - inizio
    if (causale != null) {
      setNonFatturare(causale.isNonFatturare());
    }
    //Fix 2029 - fine
    // Fix 3016
    if (this.getTestata() != null) {
      Cantiere can = ( (OrdineVenditaTestata)this.getTestata()).
          getCantiereTestata();
      if (can != null && this.getTipoRiga() == TipoRiga.MERCE) {
        this.setConCantiere(true);
      }
    }
    // Fine fix 3016
    // Fix 8913 - Inizio
    isBOCompleted = true;
    // Fix 8913 - Fine
  }

  //--------------------------------------------------------//
  //Gestione omaggi-offerte

  /**
   * Verifica se sussistono le condizioni per creare le righe di omaggio.
   * In caso affermativo le crea.
   *
   * @param testata Testata relativa alla riga
   */
  protected int gestioneRigheOmaggio(OrdineVenditaPO testata, int rc) throws
      SQLException {
    rigaOmf.setSalvaTestata(false);
    int rc1 = rigaOmf.save();

    if (rc1 >= 0)
      rc += rc1;
    else
      rc = rc1;

    return rc;
  }

  /**
   * Ridefinizione del metodo eliminaRigaOmaggioCollegata della classe
   * OrdineVenditaRiga
   */
  protected int eliminaRigaOmaggioCollegata(String key) throws SQLException {
    int rc = 0;

    OrdineVenditaRigaPrm rigaOmf =
        (OrdineVenditaRigaPrm) Factory.createObject(OrdineVenditaRigaPrm.class);
    rigaOmf.setKey(key);
    if (rigaOmf.retrieve()) {
      rigaOmf.setSalvaTestata(false); //Senza questa riga dà OPTIMISTIC LOCK FALLITO
      rc = rigaOmf.delete();
    }

    return rc;
  }

  //--------------------------------------------------------//
  //Gestione kit

  /**
   * Restituisce un'istanza della classe EsplosioneNodo
   *
   * @param articolo Articolo In oggetto
   *
   * @return Un'istanza della classe EsplosioneNodo
   */
  protected EsplosioneNodo getEsplosioneNodo(Articolo articolo) throws
      SQLException {
    Trace.println("==============>>sono in getEsplosioneNodo");
    Trace.println(articolo.getIdArticolo());
    Trace.println(getDataConsegnaConfermata());
    Trace.println(new Integer(getQtaInUMPrmMag().intValue()).toString());

    Esplosione esplosione = new Esplosione();
    esplosione.setTipoEsplosione(Esplosione.PRODUZIONE);
    esplosione.setTrovaTestataEsatta(false); //Fix 1136
    esplosione.setIdArticolo(articolo.getIdArticolo());
    esplosione.getProprietario().setTipoProprietario(ProprietarioDistinta.
        CLIENTE);
    esplosione.getProprietario().setCliente( ( (OrdineVendita) getTestata()).
                                            getCliente()); //Fix 1136

    esplosione.setTipoDistinta(DistintaTestata.NORMALE);
    esplosione.setLivelloMassimo(new Integer(1));
    esplosione.setData(getDataConsegnaConfermata());
    esplosione.setQuantita(getQtaInUMPrmMag()); //PTF02151
    //fix 4453 inizio
    if(getIdConfigurazione() != null)
       esplosione.setIdConfigurazione(getIdConfigurazione());
    //fix 4453 fine
//    esplosione.setIdConfigurazione(getIdConfigurazione());  //serve ????
//    esplosione.setGesConfigTmp(Esplosione.CREATE_MA_NON_MEMORIZZATE);

    esplosione.run();
    Trace.println(esplosione.getKey());
    Trace.println(esplosione.getNodoRadice());
    Trace.println("==============>>BOOM");
    return esplosione.getNodoRadice();
  }

  /**
   * Genera righe kit quando il kit è gestito a magazzino
   *
   * @param nodo Classe che contiene tutti i dati delle eventuali righe di kit
   */
  protected void generaRigheKit(EsplosioneNodo nodo) throws SQLException {
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
    Trace.println("==============>>calcoloDatiVendita=" + calcoloDatiVendita);

    List datiRigheKit = nodo.getNodiFigli();
    Trace.println("==============>>datiRigheKit=" + datiRigheKit.size());
    if (datiRigheKit.isEmpty()) {
//      ErrorMessage em = new ErrorMessage("THIP_BS151");
//      throw new ThipException(em);
      return;  //...FIX04607 - DZ
    }
    else {
      int sequenza = 0;
      Iterator iter = datiRigheKit.iterator();
      while (iter.hasNext()) {
        EsplosioneNodo datiRigaKit = (EsplosioneNodo) iter.next();
        Trace.println("==============>>iterazione=" + datiRigaKit);
        Trace.println("\tversione=" + datiRigaKit.getIdVersione());

        //Istanza della riga secondaria
        OrdineVenditaRigaSec rigaKit =
            (OrdineVenditaRigaSec) Factory.createObject(OrdineVenditaRigaSec.class);
        //Dati principali
        rigaKit.setServizioCalcDatiVendita(calcoloDatiVendita);

        Articolo articoloKit = datiRigaKit.getArticolo();
        Trace.println("\tversione primaria=" + getIdVersioneSal());

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

        // Inizio Fix 8977
        // Inizio 4670
        if (UnitaMisura.isPresentUMQtaIntera(umVen, umPrm, umSec, articoloKit)){ //Fix 5117
          QuantitaInUMRif qta = articoloKit.calcolaQuantitaArrotondate(qtaCalcolata, umVen, umPrm, umSec, rigaKit.getArticoloVersRichiesta(), Articolo.UM_PRM); // fix 10955
          qtaVendita = qta.getQuantitaInUMRif();
          qtaCalcolata = qta.getQuantitaInUMPrm();
          qtaSecondaria = qta.getQuantitaInUMSec();
        }
        //
        // Fine 4670
        // Fine fix 8977
        //Campi not nullable
        rigaKit.setSequenzaRiga(sequenza++);
        rigaKit.setTipoRiga(getTipoRiga());
        rigaKit.setStatoAvanzamento(getStatoAvanzamento());
        //fix 4453 inizio
        rigaKit.setCoefficienteImpiego(datiRigaKit.getCoeffImpiego());
        if (datiRigaKit.getCoeffTotale()) {
           rigaKit.setBloccoRicalcoloQtaComp(true);
           rigaKit.setCoefficienteImpiego(new BigDecimal("0"));
        }
        /*
        rigaKit.setCoefficienteImpiego(
            qtaCalcolata.divide(getQtaInUMPrmMag(), BigDecimal.ROUND_HALF_UP)
            );
        */
        //fix 4453 fine

        //Dati comuni
        rigaKit.getDatiComuniEstesi().setStato(getDatiComuniEstesi().getStato());

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
        rigaKit.setArticolo(articoloKit);
        rigaKit.setDescrizioneArticolo(
            articoloKit.getDescrizioneArticoloNLS().getDescrizione()
            );
        Integer idVersioneKit = datiRigaKit.getIdVersione();
        if (idVersioneKit != null) {
          rigaKit.setIdVersioneRcs(idVersioneKit);
          ArticoloVersione versioneKit =
              (ArticoloVersione) Factory.createObject(ArticoloVersione.class);
          String versioneKitKey =
              KeyHelper.buildObjectKey(
                  new Object[] {
                  getIdAzienda(),
                  articoloKit.getIdArticolo(),
                  idVersioneKit
          }
              );
          versioneKit.setKey(versioneKitKey);
          if (versioneKit.retrieve()) {
            ArticoloVersione versioneSaldiKit = versioneKit.getVersioneSaldi();
            if (versioneSaldiKit == null) {
              rigaKit.setIdVersioneSal(idVersioneKit);
            }
            else {
              rigaKit.setIdVersioneSal(versioneSaldiKit.getIdVersione());
            }
          }
        }

//MG FIX 8659 inizio
        rigaKit.setIdCommessa(getIdCommessa());
        rigaKit.setIdCentroCosto(getIdCentroCosto());
        recuperaDatiCA(rigaKit);
//MG FIX 8659 fine

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
        rigaKit.setListinoVendita(getListinoVendita());
        rigaKit.setIdResponsabileVendite(getIdResponsabileVendite()); //Fix 1205
        //Fix 4060 - inizio
        rigaKit.setRigaPrimaria(this);
        rigaKit.calcolaDatiVendita((OrdineVendita)rigaKit.getTestata());
        //Fix 4060 - fine
		rigaKit.setDescrizioneExtArticolo(recuperaDescExtArticolo(rigaKit.getIdCliente(), rigaKit.getIdArticolo(), rigaKit.getIdConfigurazione()));//Fix14727 RA

        //AssoggettamentoIVA assIva = articoloKit.getAssoggettamentoIVA(); // Fix 25004
	    AssoggettamentoIVA assIva = getAssoggettamentoIVAArticolo(articoloKit, rigaKit.getIdConfigurazione()); // Fix 25004
        AssoggettamentoIVA assIvaTestata = articoloKit.getAssoggettamentoIVA();//Fix 14670
        if (assIva == null) {
          assIva = getAssoggettamentoIVA();//Fix 14670
          rigaKit.setAssoggettamentoIVA(getAssoggettamentoIVA());
        }
        else {
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
        OrdineVenditaRigaSec rigaSecTmp = (OrdineVenditaRigaSec)Factory.createObject(OrdineVenditaRigaSec.class);
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

  /**
   * Stabilisce il tipo di righe kit da generare
   * FIX03954 - DZ/GN
   */
  protected void gestioneKit() throws SQLException {
    Articolo articolo = getArticolo();
    // fix 11123
    /*
    EspNodoArticolo esplosione = null;
    boolean okModello = false;
    try {
      esplosione = getEsplosioneNodoModello(false, articolo, ModelloProduttivo.KIT);
      okModello = true;
    }
    catch (ThipException ex) {
      okModello = false;
    }

    if (!okModello) {
      try {
        esplosione = getEsplosioneNodoModello(false, articolo, ModelloProduttivo.PRODUZIONE);
        okModello = true;
      }
      catch (ThipException ex) {
        okModello = false;
      }
    }
    */
   EspNodoArticolo esplosione = esplosioneModelloDocumento(articolo);
   //if (okModello){
   if (esplosione!=null){
   // fine fix 11123
     generaRigheSecondarieEsplosioneModello(false, esplosione);
   }
   else
     generaRigheKit(getEsplosioneNodo(articolo));
    calcolaPesiEVolumeRigheSec(); //Fix 12508
  }

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

  /**
   * Ridefinizione
   */
  public DocumentoOrdineRiga creaRigaSecondaria() {
    OrdineVenditaRigaSec rigaSec =
        (OrdineVenditaRigaSec) Factory.createObject(OrdineVenditaRigaSec.class);
    return rigaSec;

  }

  /**
   * Ridefinizione
   */
  //Fix 3700: introdotta nuova interfaccia EspNodoArticoloBase
  //Fix 17639 inizio
  //protected DocumentoOrdineRiga generaRigaSecondariaModello(EspNodoArticoloBase
  //    datiRigaSec, int sequenza) throws SQLException {
  public DocumentoOrdineRiga generaRigaSecondariaModello(EspNodoArticoloBase
      datiRigaSec, int sequenza) throws SQLException {
  //Fix 17639 fine

    OrdineVenditaRigaSec rigaSec =
        (OrdineVenditaRigaSec)super.generaRigaSecondariaModello(datiRigaSec,
        sequenza);
    CausaleRigaVendita causaleRigaPrm = getCausaleRiga();
    rigaSec.setCausaleRiga(causaleRigaPrm);

    if (rigaSec.getMagazzino() == null) {
    	//Fix 19453 - inizio
//    	rigaSec.setMagazzino(getMagazzino());
    	rigaSec.setMagazzino(getMagazzinoRigaSecModello(datiRigaSec));
    	//Fix 19453 - fine
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

    //fix 4453 inizio
    UnitaMisura umRif = articoloSec.getUMDefaultVendita();
    //UnitaMisura umRif = articoloSec.getUMDefaultAcquisto();
    //fix 4453 fine
    UnitaMisura umPrm = articoloSec.getUMPrmMag();
    UnitaMisura umSec = articoloSec.getUMSecMag();

    //Fix 3659 - inizio
    BigDecimal qc = datiRigaSec.getQuantitaCalcolata(); //Fix 3700
    // Qta Prm Mag
    //BigDecimal qtaCalcolata = qc.setScale(2, BigDecimal.ROUND_HALF_UP);//Fix 30871
	BigDecimal qtaCalcolata = Q6Calc.get().setScale(qc,2, BigDecimal.ROUND_HALF_UP);//Fix 30871
    //Fix 3659 - fine
    // fix 11123
    /*
    BigDecimal qtaRiferimento = articoloSec.convertiUM(qtaCalcolata, umPrm, umRif, rigaSec.getArticoloVersRichiesta()); // fix 10955
    BigDecimal qtaSecondaria = (umSec == null) ? new BigDecimal(0.0) :
        articoloSec.convertiUM(qtaRiferimento, umRif, umSec, rigaSec.getArticoloVersRichiesta()); // fix 10955

    Trace.println("\tqtaCalcolata=" + qtaCalcolata);
    Trace.println("\tqtaRiferimento=" + qtaRiferimento);
    Trace.println("\tqtaSecondaria=" + qtaSecondaria);
    */
    // fine fix 11123
    //fix 4453 inizio
    rigaSec.setCoefficienteImpiego(datiRigaSec.getCoeffImpiego());
    if (datiRigaSec.getCoeffTotale()) {
       rigaSec.setBloccoRicalcoloQtaComp(true);
       rigaSec.setCoefficienteImpiego(new BigDecimal("0"));
    }
    /*
    rigaSec.setCoefficienteImpiego(qtaCalcolata.divide(getQtaInUMPrmMag(),
        BigDecimal.ROUND_HALF_UP));
    */
    //fix 4453 fine
    // Inizio 4670 - Ricalcolo qta
    // fix 11123
    /*
    if (UnitaMisura.isPresentUMQtaIntera(umRif, umPrm, umSec, articoloSec)){ //Fix 5117
       QuantitaInUMRif qta = articoloSec.calcolaQuantitaArrotondate(qtaCalcolata,umRif, umPrm, umSec, rigaSec.getArticoloVersRichiesta(), Articolo.UM_PRM); // fix 10955
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
    // fine fix 11123
    // Fine 4670

    rigaSec.setQtaInUMRif(qtaRiferimento);
    rigaSec.setUMRif(umRif);
    rigaSec.setQtaInUMPrmMag(qtaCalcolata);
    rigaSec.setUMPrm(umPrm);
    rigaSec.setQtaInUMSecMag(qtaSecondaria);
    //Inizio 3362
    rigaSec.setUMSec(umSec);
    //Fine 3362
    rigaSec.setDataConsegnaRichiesta(getDataConsegnaRichiesta());
    rigaSec.setSettConsegnaRichiesta(getSettConsegnaRichiesta());
    rigaSec.setDataConsegnaConfermata(getDataConsegnaConfermata());
    rigaSec.setSettConsegnaConfermata(getSettConsegnaConfermata());
    rigaSec.setDataConsegnaProduzione(getDataConsegnaProduzione());
    rigaSec.setSettConsegnaProduzione(getSettConsegnaProduzione());
    rigaSec.setIdResponsabileVendite(getIdResponsabileVendite());
    // Fix 3212
    rigaSec.setListinoVendita(getListinoVendita());
    // Fine fix 3212
    //Fix 4191 - inizio
    rigaSec.setServizioCalcDatiVendita(false);
    rigaSec.setRigaPrimaria(this);
    rigaSec.calcolaDatiVendita((OrdineVendita)rigaSec.getTestata());
    //Fix 4191 - fine
	rigaSec.setDescrizioneExtArticolo(recuperaDescExtArticolo(rigaSec.getIdCliente(), rigaSec.getIdArticolo(), rigaSec.getIdConfigurazione()));//Fix14727 RA

//MG FIX 8659 inizio
    rigaSec.setIdCommessa(getIdCommessa());
    rigaSec.setIdCentroCosto(getIdCentroCosto());
    recuperaDatiCA(rigaSec);
//MG FIX 8659 fine

    rigaSec.setSalvaRigaPrimaria(false);

    aggiornaNumeroImballo(rigaSec); // fix 12572
	//Fix 33905 Inizio
	OrdineVenditaRigaSec rigaSecTmp = (OrdineVenditaRigaSec)Factory.createObject(OrdineVenditaRigaSec.class);
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


  /**
   * Quando su una riga primaria viene cambiata una delle seguenti date:<br>
   * <ul>
   * <li>richiesta consegna
   * <li>confermata consegna
   * <li>produzione consegna
   * </ul>
   * le modifiche devono essere riportate su tutte le righe secondarie
   */
  protected void gestioneDateRigheSecondarie() throws SQLException {
    //Fix1988 PM inizio
//    List righeSecondarie = getRigheSecondarie();
//    if (! righeSecondarie.isEmpty()) {
//      OrdineRiga oldRiga = getOldRiga();
//      if (oldRiga.getDataConsegnaRichiesta() != getDataConsegnaRichiesta() ||
//          oldRiga.getDataConsegnaConfermata() != getDataConsegnaConfermata() ||
//          oldRiga.getDataConsegnaProduzione() != getDataConsegnaProduzione()
//         ) {
//        Iterator iter = righeSecondarie.iterator();
//        while (iter.hasNext()) {
//          OrdineVenditaRigaSec rigaSec = (OrdineVenditaRigaSec)iter.next();
//          rigaSec.setDataConsegnaRichiesta(getDataConsegnaRichiesta());
//          rigaSec.setDataConsegnaConfermata(getDataConsegnaConfermata());
//          rigaSec.setDataConsegnaProduzione(getDataConsegnaProduzione());
//          rigaSec.setSettConsegnaRichiesta(getSettConsegnaRichiesta());
//          rigaSec.setSettConsegnaConfermata(getSettConsegnaConfermata());
//          rigaSec.setSettConsegnaProduzione(getSettConsegnaProduzione());
//        }
//      }
//    }
	//Fix 33762 inizio
	boolean propagaDati = true;
	if (isOnDB())
	{
		OrdineRiga oldRiga = getOldRiga();
		if (oldRiga != null)
		{
			if (datiUguali(oldRiga.getDataConsegnaRichiesta(), getDataConsegnaRichiesta()) &&
				datiUguali(oldRiga.getDataConsegnaConfermata(), getDataConsegnaConfermata()) &&
				//Fix 34161	datiUguali(getDataConsegnaProduzione(),getDataConsegnaProduzione()))
				datiUguali(oldRiga.getDataConsegnaProduzione(),getDataConsegnaProduzione())) //Fix 34161
				propagaDati = false;
			}
	}
	if (!propagaDati)
		return;

    //OrdineRiga oldRiga = getOldRiga();
    //if (oldRiga != null) {
	//Fix 33762 Fine
	    
	List righeSecondarie = getRigheSecondarie();
      //Fix 33762 inizio
      if (righeSecondarie.isEmpty()) 
    	  return;
      
    //Fix 33762 fine
      
      /* if (!righeSecondarie.isEmpty()) {
        if (oldRiga.getDataConsegnaRichiesta() != getDataConsegnaRichiesta() ||
            oldRiga.getDataConsegnaConfermata() != getDataConsegnaConfermata() ||
            oldRiga.getDataConsegnaProduzione() != getDataConsegnaProduzione()) { Fix 33762 */ 
          Iterator iter = righeSecondarie.iterator();
          while (iter.hasNext()) {
            OrdineVenditaRigaSec rigaSec = (OrdineVenditaRigaSec) iter.next();
            rigaSec.setDataConsegnaRichiesta(getDataConsegnaRichiesta());
            rigaSec.setDataConsegnaConfermata(getDataConsegnaConfermata());
            rigaSec.setDataConsegnaProduzione(getDataConsegnaProduzione());
            rigaSec.setSettConsegnaRichiesta(getSettConsegnaRichiesta());
            rigaSec.setSettConsegnaConfermata(getSettConsegnaConfermata());
            rigaSec.setSettConsegnaProduzione(getSettConsegnaProduzione());
          }
        //}
     // }
    //}
    //Fix1988 PM Fine
  }


  //--------------------------------------------------------//
  //Ridefinizione di altri metodi

  /**
   * Ridefinizione del metodo getNumeroRiga della classe OrdineRiga
   */
  protected void componiChiave() {
//    return RigaVendita.getNumeroNuovaRiga(getTestata());

    setNumeroRigaDocumento(
        new Integer(
            RigaVendita.getNumeroNuovaRiga(getTestata())
        )
        );

    // Fix 11976 PM >
    if (this.getNumRigaBozza() != null)
 	   this.setSequenzaRiga(getNumeroRigaDocumento().intValue());
    // Fix 11976 PM <


  }

  /**
   * Ridefinizione del metodo save()
   */
  public int save() throws SQLException {
	//Fix 45246 inizio
	  if(isSalvatagioRigaDaIngorare())
		return 0;
	//Fix 45246 fine  
    boolean newRow = !isOnDB();
    //37420 inizio
    if(PersDatiGen.isGestitioIntellimag()) {
    	cambioTestataStatoIM(newRow);
    }
    //37420 fine
    //Fix 34991 inizio
    boolean salvaTestataInit = this.isSalvaTestata();
    boolean calcoloImportiDisabilitato = isCalcolaImportiRigaDisabilitato();
    
    //Fix 34991 fine
    //Fix 11529 - inizio
    char tipoParte = getArticolo().getTipoParte();
    if (tipoParte == ArticoloDatiIdent.KIT_GEST || tipoParte == ArticoloDatiIdent.KIT_NON_GEST) {
    	propagaCommessaSuRigheSec(getRigheSecondarie());
    }
    //Fix 11529 - fine

//MG FIX 8495 inizio : per righe omaggio sconto articolo con es.art15, l'assoggettamento
// IVA della riga deve essere forzato a quello assogg.Es.Art.15 definito sulla causale
      if (!isOnDB() && getTipoRiga() == TipoRigaDocumentoVendita.OMAGGIO) {
        CausaleRigaVendita cau = this.getCausaleRiga();
        if (cau.getTpOmaggioScontoArticolo() == ScontoArticolo.SC_ART_ES_ART15 &&
            cau.getIdAssoggIvaEsArt15() != null)
          this.setIdAssogIVA(cau.getIdAssoggIvaEsArt15());
      }
//MG FIX 8495 fine

    //PM Fix 1988 Inizio

    //Fix 3197 - inizio
    //Fix 3738 (aggiunto controllo su pdv) - inizio
    PersDatiVen pdv = PersDatiVen.getCurrentPersDatiVen();
    if (pdv.getGestionePvgSuScalaSconti() &&
        (isServeRicalProvvAg() || isServeRicalProvvSubag())) {
      modificaProvv2Agente();
    }
    //Fix 3738 - fine
    //Fix 3197 - fine
    // Inizio 11707
    /*
    //Verifica se deve generare le righe secondarie di kit
    if (newRow && getTipoRiga() != TipoRiga.SPESE_MOV_VALORE &&
        (getArticolo().getTipoParte() == ArticoloDatiIdent.KIT_GEST
         ||
         getArticolo().getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST)
        ) {
      if (isGeneraRigheSecondarie() && !isDisabilitaRigheSecondarieForCM()) {  //...FIX04607 - DZ
        gestioneKit();
        calcolaPrezzoDaRigheSecondarie();
      }
    }
    else {
      if (isOnDB()) {
        gestioneDateRigheSecondarie();
      }
    }
    */
    runGenerazioneRigheSec();
    // Fine 11707

    //Salvataggio della riga
    impostaSaldoManuale();
    Iterator i = getRigheSecondarie().iterator();
    while (i.hasNext()) {
      OrdineVenditaRigaSec rigaSec = (OrdineVenditaRigaSec) i.next();
      //Fix 18753 inizio
      if ((!isOnDB() || (iOldRiga != null && iOldRiga.getStatoAvanzamento() != StatoAvanzamento.DEFINITIVO && getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO) )
          && rigaSec.getArticolo() != null && rigaSec.getArticolo().isConfigurato() &&
          rigaSec.getConfigurazione() == null)
      {
        setStatoAvanzamento(StatoAvanzamento.PROVVISORIO);
        rigaSec.setStatoAvanzamento(StatoAvanzamento.PROVVISORIO);
        getWarningList().add(new ErrorMessage("THIP40T311",rigaSec.getIdArticolo()));
      }
      //Fix 18753 fine
      rigaSec.setSalvaRigaPrimaria(false);
      rigaSec.setAggiornaRigaOfferta(false); //Fix 10719
      //Fix 3611 - inizio
      //Fix 5634 - inizio
      /*
      Aggiunto controllo su isGeneraRigheSecondarie().
      Questo controllo serve perchè è l'unico modo di sapere a livello di bo
      se sono in copia di una riga (vedi relativo datacollector - fix 4800).
      Quando si copia una riga e si cambia la qta questa deve essere riportata
      nelle righe secondarie.
      sulla riga copiata
      */
	  // Fix 24252 inizio
      boolean calcoloPrezzo = isDaCaricamentoDiMassa() && ((OrdineVendita)getTestata()).isRicalcoloDatiVendita();
      if(calcoloPrezzo)
		rigaSec.calcolaDatiVendita((OrdineVenditaPO) getTestata());
      // Fix 24252 fine
      if ((!newRow && isQuantitaCambiata()) || !isGeneraRigheSecondarie()) {
      //Fix 9671 PM Inizio
      //Fix 5634 - fine
        //rigaSec.ricalcoloQuantita(getQtaInUMPrmMag());
        rigaSec.ricalcoloQuantita(this);
        rigaSec.calcolaPesiEVolume(); //Fix 12508
      }
      //Fix 3611 - fine
      //Fix 9671 PM Fine
     }

    //Fix 12508 inizio
    calcolaPesiEVolume(newRow);
    aggiornaPesiEVolumeTestata(false);
    //Fix 12508 fine

    //Fix 1918 - inizio
    //Verifica catalogo: se nella riga esiste un catalogo a cui non è associato
    //alcun articolo la riga deve essere messa in stato di avanzamento
    //PROVVISORIO.
    if (!isOnDB()) {
      CatalEsterno ce = getCatalogoEsterno();
      if (ce != null && ce.getArticolo() == null) {
        setStatoAvanzamento(StatoAvanzamento.PROVVISORIO);
      }
    }
    //Fix 1918 - fine
    // Inizio 4500
    impostaStatoAvanzamentoSecondarie();
    // Fine 4500
    // Fix 9061 - Inizio
    if (newRow && isBOCompleted){ // se è una nuova riga inserita in modalità batch
        CompletaDatiCA();
    }
    // Fix 9061 - Fine

    impostaRilascioOrdineProdSecondarie(); // Fix 17697
	//Fix 37217 inizio
	if((isInCopiaRiga ||  ( getTestata() != null && ((OrdineVendita)this.getTestata()).isInCopia()) ) 
		&& getArticolo() != null  && (getArticolo().isArticLotto()) && (getArticolo().getArticoloDatiMagaz().isLottoUnitario()))
	{	if(getRigheLotto() != null)
			getRigheLotto().clear();
	}
	//Fix 37217 fine
    int rc = super.save();

    if (rc >= 0) {
      // Inizio 8508
      if (rc >0 && isRigaAContratto() && !getSaveFromPDC())
        aggiornaRigaPianoConsegnaCollegata();
      // Fine 8508
      setSalvaRigheSecondarie(false);
      disabilitaCalcoloMovimentiPortafoglio();
      disabilitaAggiornamentoParteIntestatario(); //Fix 1406
      disabilitaCalcolaImportiRiga();
//MG FIX 2900
      this.setSalvaTestata(false);
//MG FIX 2900
      int rc1 = super.save();
      abilitaCalcoloMovimentiPortafoglio();
      abilitaAggiornamentoParteIntestatario(); //Fix 1406
      rc = rc1 >= 0 ? rc + rc1 : rc1;
      setSalvaRigheSecondarie(true);
      //Fix 10719 PM Inizio
      //Fix 16508 inizio
      OffertaClienteRiga offertaCli=recuperaOffertaClienteRiga();
      //if (rc > 0 && getOffertaClienteRiga() != null && isAggiornaRigaOfferta()) {
      if (rc > 0 && offertaCli != null && isAggiornaRigaOfferta()) {
      	//rc1 = getOffertaClienteRiga().aggiornaDopoEvasione(this, OrdineRiga.MANUTENZIONE);
        rc1 = offertaCli.aggiornaDopoEvasione(this, OrdineRiga.MANUTENZIONE);
      //Fix 16508 fine
      	rc = rc1 >= 0 ? rc + rc1 : rc1;
      }
      //Fix 10719 PM Fine

    }

    //Verifica se deve creare delle righe omaggio
    if (rc > 0 && newRow && getTipoRiga() == TipoRiga.MERCE && !isRigaOfferta() &&
        rigaOmf != null)
      rc = gestioneRigheOmaggio( (OrdineVenditaPO) getTestata(), rc);
    else {
      if (isRigaOfferta())
        setRigaOfferta(false);
    }
    // Fix 8913 - Inizio: rimosso con fix 9061
    /*
    if (newRow && isBOCompleted){ // se è una nuova riga inserita in modalità batch
        CompletaDatiCA();
        rc = super.save();
    }*/
    // Fix 8913 - Fine
    //Fix 16773 inizio
    int yrit = afterSave(newRow);
    if (yrit < 0)
      rc = yrit;
    else
      rc = rc + yrit;
    //Fix 16773 fine
    //Fix 24613 inizio
    if(rc>=0){
      int dettCfgRit = salvaDettRigaConf(newRow);
      if (dettCfgRit < 0)
        rc = dettCfgRit;
      else
        rc = rc + dettCfgRit;
    }
    //Fix 24613 fine
    salvaConfigArticoloPrezzoList(newRow);//44784
    
    // Inizio 10962
    OrdineVendita ordTestata = (OrdineVendita)getTestata();
    // Se la riga è una spesa ed è stata generata automaticamente dalla testata, non deve essere
    // creata la riga ordine acquisto intercompany dalla save della riga ordine vendita.
    // La riga di spesa intercompany viene creata dal GeneratoreOrdineAcqTestata.
    //...FIX12405 - DZ
    //boolean isGenerazioneRigaSpesaAutomatica = (ordTestata.getRigheSpesaNuova() != null && !ordTestata.getRigheSpesaNuova().isEmpty() && getTipoRiga() == TipoRiga.SPESE_MOV_VALORE);
    if (isAttivaGestioneIntercompany() && rc > 0 &&  !isRigaSpesaAutomatica() && !ordTestata.isInCopia()) { // Fix 11259
      GeneratoreOrdineAcqRigaIC gen = (GeneratoreOrdineAcqRigaIC) Factory.createObject(GeneratoreOrdineAcqRigaIC.class);
      gen.init(getModelloGenOrdineAcq(), this);
      if (isDaSmart())// Fix 29632
    	  gen.setAutoCommit(false);//Fix 29632
      List errors = gen.generaOrdineAcquistoRigaIC();
      //Fix 13911 PM >
      //if (!errors.isEmpty())
      if (gen.esistonoErroriBloccanti(errors)) //Fix 13911
        throw new ThipException(errors);
      else
    	  getWarningList().addAll(errors);
      //Fix 13911 PM <
    }
    // Fine 10962

    //...FIX 26488
    try {
      if (rc >0 && GestioneConaiHelper.getInstance().getPersDatiConai() != null)
        GestioneConaiHelper.getInstance().aggiungiRigheOrdVenGestioneConai(this);
    }
    catch(Exception e) {
       Trace.excStream.println("#### ECCEZIONE IN GESTIONE CONAI RIGA ORD VEN ####");
       e.printStackTrace(Trace.excStream);
     } 

    //Fix 34991 inizio
    //Incaso di due righe documenti nate dalla stessa rigaOrdine, dobbiamo riabiletare il calcolo e il salvaTestata per la seconda riga
    //visto che hanno la stessa istanza della riga ordine
    this.setSalvaTestata(salvaTestataInit);
    if(!calcoloImportiDisabilitato)
    	abilitaCalcolaImportiRiga();
    //Fix 34991 fine
    
    return rc;
  }


  
  /**
   * Valorizza il Proxy AssoggettamentoIVA.
   */
  public AssoggettamentoIVA getAssoggIVADichiarIntento() {
    return (AssoggettamentoIVA)iAssoggIVADichiarIntento.getObject();
  }


  /**
   * Ridefinizione del metodo recuperoDatiVenditaSave della classe
   * OrdineVenditaRiga
   */
  protected boolean recuperoDatiVenditaSave() {
    //Fix 4060 - inizio
    Articolo articolo = getArticolo();
    //Fix 4976 - inizio
    if (articolo != null) {
    //Fix 4976 - inizio
      char tipoParte = articolo.getTipoParte();
      char tipoCalcoloPrezzo = articolo.getTipoCalcPrzKit();
      if ((tipoParte == ArticoloDatiIdent.KIT_NON_GEST || tipoParte == ArticoloDatiIdent.KIT_GEST)
          &&
          tipoCalcoloPrezzo == ArticoloDatiVendita.DA_COMPONENTI) {
        return false;
      }
      //Fix 4060 - fine
      else {
        return
          isServizioCalcDatiVendita() &&
          !isRigaOfferta() &&
          getTipoRiga() != TipoRiga.SPESE_MOV_VALORE;
       }
    }
    //Fix 4976 - inizio
    else {
      return false;
    }
    //Fix 4976 - fine
  }

  //--------------------------------------------------------//

  //Implementazione metodi astratti di PersistentObject

  protected TableManager getTableManager() throws java.sql.SQLException {
    return OrdineVenditaRigaPrmTM.getInstance();
  }

  protected void inizializzaListaMovimentiPortafoglio() {
    super.inizializzaListaMovimentiPortafoglio();
    Iterator i = getRigheSecondarie().iterator();
    while (i.hasNext()) {
      OrdineVenditaRigaSec ovrs = (OrdineVenditaRigaSec) i.next();
      ovrs.setMovimentiPortafoglio(iMovimentiPortafoglio);
      ovrs.setApplicaMovimentiSuiSaldi(false);
    }
  }


//MG FIX 6754 inizio

/*
  protected void calcolaImportiRiga() {
    try {
      ValorizzatoreImportiOrdineVendita viov = new
          ValorizzatoreImportiOrdineVendita();
      ImportiRigaOrdineVendita importi = viov.calcolaImportiRiga(this);
      setValoreOrdinato(importi.getValoreOrdinato());
      setValoreInSpedizione(importi.getValoreInSpedizione());
      setValoreConsegnato(importi.getValoreConsegnato());
      setCostoOrdinato(importi.getCostoOrdinato());
      setCostoInSpedizione(importi.getCostoInSpedizione());
      setCostoConsegnato(importi.getCostoConsegnato());


      //...FIX02001 - DZ
      setValoreImposta(importi.getImpostaValoreOrdinato());
      setValoreImpostaInSpedizione(importi.getImpostaValoreInSpedizione());
      setValoreImpostaConsegnato(importi.getImpostaValoreConsegnato());
      //...fine FIX02001 - DZ

      //Fix 5102 - inizio
      if (getDatiComuniEstesi().getStato() != DatiComuniEstesi.ANNULLATO) {
      //Fix 5102 - fine
      //...FIX02001 - DZ
//        if (getTipoRiga() != TipoRiga.OMAGGIO && getTipoRiga() != TipoRiga.SPESE_MOV_VALORE)
//        {
      List importiTestata = new ArrayList();
      OrdineVendita testata = (OrdineVendita) getTestata();
      BigDecimal valoreOrdinato = null;
      BigDecimal valoreInSpedizione = null;
      BigDecimal valoreConsegnato = null;
      BigDecimal costoOrdinato = null;
      BigDecimal costoInSpedizione = null;
      BigDecimal costoConsegnato = null;
      BigDecimal imposta = testata.getValoreImposta();
      BigDecimal impostaInSpedizione = testata.getValoreImpostaInSped();
      BigDecimal impostaConsegnato = testata.getValoreImpostaCons();

      switch (getTipoRiga()) {
        case TipoRiga.MERCE:
          valoreOrdinato = testata.getValoreOrdinato();
          valoreInSpedizione = testata.getValoreInSpedizione();
          valoreConsegnato = testata.getValoreConsegnato();
          costoOrdinato = testata.getCostoOrdinato();
          costoInSpedizione = testata.getCostoInSpedizione();
          costoConsegnato = testata.getCostoConsegnato();
          break;
        case TipoRiga.OMAGGIO:
          valoreOrdinato = testata.getValoreOmaggiOrd();
          valoreInSpedizione = testata.getValoreOmaggiInSped();
          valoreConsegnato = testata.getValoreOmaggiCons();
          costoOrdinato = testata.getCostoOmaggiOrd();
          costoInSpedizione = testata.getCostoOmaggiInSped();
          costoConsegnato = testata.getCostoOmaggiCons();
          break;
        case TipoRiga.SERVIZIO:
          valoreOrdinato = testata.getValoreServiziOrd();
          valoreInSpedizione = testata.getValoreServiziInSped();
          valoreConsegnato = testata.getValoreServiziCons();
          costoOrdinato = testata.getCostoServiziOrd();
          costoInSpedizione = testata.getCostoServiziInSped();
          costoConsegnato = testata.getCostoServiziCons();
          break;
        case TipoRiga.SPESE_MOV_VALORE:
          valoreOrdinato = testata.getValoreSpeseOrd();
          valoreInSpedizione = testata.getValoreSpeseInSped();
          valoreConsegnato = testata.getValoreSpeseCons();
          costoOrdinato = testata.getCostoSpeseOrd();
          costoInSpedizione = testata.getCostoSpeseInSped();
          costoConsegnato = testata.getCostoSpeseCons();
          break;
      }

      //ini fix 3242
      if (valoreOrdinato == null) {
        valoreOrdinato = new BigDecimal(0);
      }
      if (valoreInSpedizione == null) {
        valoreInSpedizione = new BigDecimal(0);
      }
      if (valoreConsegnato == null) {
        valoreConsegnato = new BigDecimal(0);
      }
      if (costoOrdinato == null) {
        costoOrdinato = new BigDecimal(0);
      }
      if (costoInSpedizione == null) {
        costoInSpedizione = new BigDecimal(0);
      }
      if (costoConsegnato == null) {
        costoConsegnato = new BigDecimal(0);
      }
      //fine fix 3242

      if (isOnDB()) {
        if (iOldRiga != null) {
              //Fix 5102 - inizio
              //Se la riga ERA in stato ANNULLATO non bisogna stornare altrimenti
              //il valore stornato si annulla con quello da aggiungere
              boolean storna = (iOldRiga.getDatiComuniEstesi().getStato() != DatiComuniEstesi.ANNULLATO);
              // fix 5990
              // c'è un altro caso in cui non deve stornare ed è quando la riga è una riga di spesa
              // e passa da essere spesa percentuale a spesa non percentuale
              if (this.getTipoRiga()==TipoRiga.SPESE_MOV_VALORE && iOldRiga.isSpesaPercentuale()){
                storna = false;
              }
              // fine fix 5990
              if (storna) {
          OrdineVenditaRiga ovr = (OrdineVenditaRiga) iOldRiga;
                valoreOrdinato = valoreOrdinato.subtract(ovr.getValoreOrdinato());
                valoreInSpedizione = valoreInSpedizione.subtract(ovr.getValoreInSpedizione());
                valoreConsegnato = valoreConsegnato.subtract(ovr.getValoreConsegnato());
                costoOrdinato = costoOrdinato.subtract(ovr.getCostoOrdinato());
                costoInSpedizione = costoInSpedizione.subtract(ovr.getCostoInSpedizione());
                costoConsegnato = costoConsegnato.subtract(ovr.getCostoConsegnato());
              }
             valoreOrdinato = valoreOrdinato.add(getValoreOrdinato());
             valoreInSpedizione = valoreInSpedizione.add(getValoreInSpedizione());
             valoreConsegnato = valoreConsegnato.add(getValoreConsegnato());
             costoOrdinato = costoOrdinato.add(getCostoOrdinato());
             costoInSpedizione = costoInSpedizione.add(getCostoInSpedizione());
             costoConsegnato = costoConsegnato.add(getCostoConsegnato());
              //Fix 5102 - fine

          if (imposta != null) {
               //Fix 5102 - inizio
               if (storna) {
            BigDecimal oldImposta = getOldRiga().getValoreImposta();
            if (oldImposta == null)
              oldImposta = new BigDecimal(0);
                 imposta = imposta.subtract(oldImposta);
               }
              imposta = imposta.add(getValoreImposta());
               //Fix 5102 - fine
          }
          else {
            imposta = getValoreImposta();
          }

          if (impostaInSpedizione != null) {
               //Fix 5102 - inizio
               if (storna) {
                 BigDecimal oldImpostaInSp = ((RigaVendita)getOldRiga()).getValoreImpostaInSpedizione();
            if (oldImpostaInSp == null)
              oldImpostaInSp = new BigDecimal(0);
                 impostaInSpedizione = impostaInSpedizione.subtract(oldImpostaInSp);
               }
               impostaInSpedizione = impostaInSpedizione.add(getValoreImpostaInSpedizione());
               //Fix 5102 - fine
          }
          else {
            impostaInSpedizione = getValoreImpostaInSpedizione();
          }

          if (impostaConsegnato != null) {
               //Fix 5102 - inizio
               if (storna) {
                 BigDecimal oldImpostaCons = ((RigaVendita)getOldRiga()).getValoreImpostaConsegnato();
            if (oldImpostaCons == null)
              oldImpostaCons = new BigDecimal(0);
                 impostaConsegnato = impostaConsegnato.subtract(oldImpostaCons);
               }
               impostaConsegnato = impostaConsegnato.add(getValoreImpostaConsegnato());
               //Fix 5102 - fine
          }
          else {
            impostaConsegnato = getValoreImpostaConsegnato();
          }

        }
      }
      else {
        valoreOrdinato = valoreOrdinato.add(getValoreOrdinato());
        valoreInSpedizione = valoreInSpedizione.add(getValoreInSpedizione());
        valoreConsegnato = valoreConsegnato.add(getValoreConsegnato());
        costoOrdinato = costoOrdinato.add(getCostoOrdinato());
        costoInSpedizione = costoInSpedizione.add(getCostoInSpedizione());
        costoConsegnato = costoConsegnato.add(getCostoConsegnato());

        if (imposta != null)
          imposta = imposta.add(getValoreImposta());
        else
          imposta = getValoreImposta();

        if (impostaInSpedizione != null)
          impostaInSpedizione = impostaInSpedizione.add(
              getValoreImpostaInSpedizione());
        else
          impostaInSpedizione = getValoreImpostaInSpedizione();

        if (impostaConsegnato != null)
          impostaConsegnato = impostaConsegnato.add(getValoreImpostaConsegnato());
        else
          impostaConsegnato = getValoreImpostaConsegnato();

      }

      testata.setValoreImposta(imposta);
      testata.setValoreImpostaInSped(impostaInSpedizione);
      testata.setValoreImpostaCons(impostaConsegnato);

      switch (getTipoRiga()) {
        case TipoRiga.MERCE:
          testata.setValoreOrdinato(valoreOrdinato);
          testata.setValoreInSpedizione(valoreInSpedizione);
          testata.setValoreConsegnato(valoreConsegnato);
          testata.setCostoOrdinato(costoOrdinato);
          testata.setCostoInSpedizione(costoInSpedizione);
          testata.setCostoConsegnato(costoConsegnato);
          break;
        case TipoRiga.OMAGGIO:
          testata.setValoreOmaggiOrd(valoreOrdinato);
          testata.setValoreOmaggiInSped(valoreInSpedizione);
          testata.setValoreOmaggiCons(valoreConsegnato);
          testata.setCostoOmaggiOrd(costoOrdinato);
          testata.setCostoOmaggiInSped(costoInSpedizione);
          testata.setCostoOmaggiCons(costoConsegnato);
          break;
        case TipoRiga.SERVIZIO:
          testata.setValoreServiziOrd(valoreOrdinato);
          testata.setValoreServiziInSped(valoreInSpedizione);
          testata.setValoreServiziCons(valoreConsegnato);
          testata.setCostoServiziOrd(costoOrdinato);
          testata.setCostoServiziInSped(costoInSpedizione);
          testata.setCostoServiziCons(costoConsegnato);
          break;
        case TipoRiga.SPESE_MOV_VALORE:
          testata.setValoreSpeseOrd(valoreOrdinato);
          testata.setValoreSpeseInSped(valoreInSpedizione);
          testata.setValoreSpeseCons(valoreConsegnato);
          testata.setCostoSpeseOrd(costoOrdinato);
          testata.setCostoSpeseInSped(costoInSpedizione);
          testata.setCostoSpeseCons(costoConsegnato);
          break;
      }

//        if (getTipoRiga() != TipoRiga.OMAGGIO){
//          testata.setValoreTotOrdinato(imposta.add(valoreOrdinato));
//          testata.setValoreTotInSped(impostaInSpedizione.add(valoreInSpedizione));
//          testata.setValoreTotCons(impostaConsegnato.add(valoreConsegnato));
//        }

      //...FIX02153 - DZ
      BigDecimal valoreServiziOrd = testata.getValoreServiziOrd() == null ?
          new BigDecimal("0") : testata.getValoreServiziOrd();
      BigDecimal valoreSpeseOrd = testata.getValoreSpeseOrd() == null ?
          new BigDecimal("0") : testata.getValoreSpeseOrd();
      BigDecimal valoreServiziInSped = testata.getValoreServiziInSped() == null ?
          new BigDecimal("0") : testata.getValoreServiziInSped();
      BigDecimal valoreSpeseInSped = testata.getValoreSpeseInSped() == null ?
          new BigDecimal("0") : testata.getValoreSpeseInSped();
      BigDecimal valoreServiziCons = testata.getValoreServiziCons() == null ?
          new BigDecimal("0") : testata.getValoreServiziCons();
      BigDecimal valoreSpeseCons = testata.getValoreSpeseCons() == null ?
          new BigDecimal("0") : testata.getValoreSpeseCons();
      //...fine FIX02153 - DZ

        testata.setValoreTotOrdinato(imposta.add(testata.getValoreOrdinato())
                                   .add(valoreServiziOrd
                                        .add(valoreSpeseOrd)));
        testata.setValoreTotInSped(impostaInSpedizione.add(testata.getValoreInSpedizione())
                                 .add(valoreServiziInSped
                                      .add(valoreSpeseInSped)));
        testata.setValoreTotCons(impostaConsegnato.add(testata.getValoreConsegnato())
                               .add(valoreServiziCons
                                    .add(valoreSpeseCons)));

//MG FIX 6481 inizio
        if (PersDatiVen.getCurrentPersDatiVen().getContabilizzazioneRicavi() == PersDatiVen.AL_LORDO) {
          BigDecimal valTotOrd = getNotNullValue(testata.getValoreTotOrdinato());
          BigDecimal valOrd = getNotNullValue(testata.getValoreOrdinato());
          BigDecimal valSrv = getNotNullValue(testata.getValoreServiziOrd());
          testata.setValoreTotOrdinato(valTotOrd.subtract(testata.calcolaScontoFineFatturaSoloSeAlLordo(valOrd.add(valSrv))));

          BigDecimal valTotInSped = getNotNullValue(testata.getValoreTotInSped());
          BigDecimal valInSped = getNotNullValue(testata.getValoreInSpedizione());
          BigDecimal valSrvInSped = getNotNullValue(testata.getValoreServiziInSped());
          testata.setValoreTotInSped(valTotInSped.subtract(testata.calcolaScontoFineFatturaSoloSeAlLordo(valInSped.add(valSrvInSped))));

          BigDecimal valTotCons = getNotNullValue(testata.getValoreTotCons());
          BigDecimal valCons = getNotNullValue(testata.getValoreConsegnato());
          BigDecimal valSrvCons = getNotNullValue(testata.getValoreServiziCons());
          testata.setValoreTotCons(valTotCons.subtract(testata.calcolaScontoFineFatturaSoloSeAlLordo(valCons.add(valSrvCons))));
        }
//MG FIX 6481 fine

//        }
      //...fine FIX02001 - DZ
    }
      //Fix 5102 - inizio
      else {
         //Se la riga PASSA in stato ANNULLATO è necessario stornare valore/costo
         //dalla testata. Il secondo gruppo di controlli serve per evitare che le
         //stesse operazioni fatte in fase di salvataggio avvengano anche in caso
         //di azione 'Passa a riga', che pure scatena un salvataggio
         if (isOnDB() && (getOldRiga() != null && getOldRiga().getDatiComuniEstesi().getStato() != DatiComuniEstesi.ANNULLATO)) {
           OrdineVendita testata = (OrdineVendita)getTestata();
           RigaVendita oldRiga = (RigaVendita)getOldRiga();

            switch (getTipoRiga()){
             case TipoRiga.MERCE:
                stornaImportiRigaMerce(testata, oldRiga);
                break;
              case TipoRiga.OMAGGIO:
                stornaImportiRigaOmaggio(testata, oldRiga);
                 break;
              case TipoRiga.SERVIZIO:
                stornaImportiRigaServizio(testata, oldRiga);
                 break;
              case TipoRiga.SPESE_MOV_VALORE:
                stornaImportiRigaSpesa(testata, oldRiga);
                 break;
            }

            //Imposta
            BigDecimal imposta =
               getNotNullValue(testata.getValoreImposta()).
                 subtract(
                    getNotNullValue(oldRiga.getValoreImposta())
                 );
            testata.setValoreImposta(imposta);
            //Imposta in spedizione
            BigDecimal impostaInSpedizione =
               getNotNullValue(testata.getValoreImpostaInSped()).
                 subtract(
                    getNotNullValue(oldRiga.getValoreImpostaInSpedizione())
                 );
            testata.setValoreImpostaInSped(impostaInSpedizione);
            //Imposta consegnato
            BigDecimal impostaConsegnato =
               getNotNullValue(testata.getValoreImpostaCons()).
                 subtract(
                    getNotNullValue(oldRiga.getValoreImpostaConsegnato())
                 );
            testata.setValoreImpostaCons(impostaConsegnato);

            //Totali
           testata.setValoreTotOrdinato(
               testata.getValoreOrdinato().
                     add(testata.getValoreServiziOrd()).
                     add(testata.getValoreSpeseOrd()).
                     add(testata.getValoreOmaggiOrd()).
                     add(imposta)
               );
           testata.setValoreTotInSped(
               testata.getValoreInSpedizione().
                     add(testata.getValoreServiziInSped()).
                     add(testata.getValoreSpeseInSped()).
                     add(testata.getValoreOmaggiInSped()).
                     add(impostaInSpedizione)
               );
           testata.setValoreTotCons(
               testata.getValoreConsegnato().
                     add(testata.getValoreServiziCons()).
                     add(testata.getValoreSpeseCons()).
                     add(testata.getValoreOmaggiCons()).
                     add(impostaConsegnato)
               );
//MG FIX 6481 inizio
           if (PersDatiVen.getCurrentPersDatiVen().getContabilizzazioneRicavi() == PersDatiVen.AL_LORDO) {
             BigDecimal valTotOrd = getNotNullValue(testata.getValoreTotOrdinato());
             BigDecimal valOrd = getNotNullValue(testata.getValoreOrdinato());
             BigDecimal valSrv = getNotNullValue(testata.getValoreServiziOrd());
             testata.setValoreTotOrdinato(valTotOrd.subtract(testata.calcolaScontoFineFatturaSoloSeAlLordo(valOrd.add(valSrv))));

             BigDecimal valTotInSped = getNotNullValue(testata.getValoreTotInSped());
             BigDecimal valInSped = getNotNullValue(testata.getValoreInSpedizione());
             BigDecimal valSrvInSped = getNotNullValue(testata.getValoreServiziInSped());
             testata.setValoreTotInSped(valTotInSped.subtract(testata.calcolaScontoFineFatturaSoloSeAlLordo(valInSped.add(valSrvInSped))));

             BigDecimal valTotCons = getNotNullValue(testata.getValoreTotCons());
             BigDecimal valCons = getNotNullValue(testata.getValoreConsegnato());
             BigDecimal valSrvCons = getNotNullValue(testata.getValoreServiziCons());
             testata.setValoreTotCons(valTotCons.subtract(testata.calcolaScontoFineFatturaSoloSeAlLordo(valCons.add(valSrvCons))));
           }
//MG FIX 6481 fine

         }
      }
      //Fix 5102 - fine
    }
    catch (Exception e) {
      Trace.println("Eccezione nel metodo calcolaImportiRiga() della classe " +
                    getClass().getName() + ": " + e.getMessage());
      e.printStackTrace();
    }

  }


  //Fix 5102 - inizio
  protected void stornaImportiRigaMerce(OrdineVendita testata, RigaVendita oldRiga) {
    //Valore ordinato
    BigDecimal valoreOrdinato =
       getNotNullValue(testata.getValoreOrdinato()).
         subtract(
            getNotNullValue(oldRiga.getValoreOrdinato())
         );
    testata.setValoreOrdinato(valoreOrdinato);
    //Valore in spedizione
    BigDecimal valoreInSpedizione =
       getNotNullValue(testata.getValoreInSpedizione()).
         subtract(
            getNotNullValue(oldRiga.getValoreInSpedizione())
         );
    testata.setValoreInSpedizione(valoreInSpedizione);
    //Valore consegnato
    BigDecimal valoreConsegnato =
       getNotNullValue(testata.getValoreConsegnato()).
         subtract(
            getNotNullValue(oldRiga.getValoreConsegnato())
         );
    testata.setValoreConsegnato(valoreConsegnato);

    //Costo ordinato
    BigDecimal costoOrdinato =
       getNotNullValue(testata.getCostoOrdinato()).
         subtract(
            getNotNullValue(oldRiga.getCostoOrdinato())
         );
    testata.setCostoOrdinato(costoOrdinato);
    //Costo in spedizione
    BigDecimal costoInSpedizione =
       getNotNullValue(testata.getCostoInSpedizione()).
         subtract(
            getNotNullValue(oldRiga.getCostoInSpedizione())
         );
    testata.setCostoInSpedizione(costoInSpedizione);
    //Costo consegnato
    BigDecimal costoConsegnato =
       getNotNullValue(testata.getCostoConsegnato()).
         subtract(
            getNotNullValue(oldRiga.getCostoConsegnato())
         );
    testata.setCostoConsegnato(costoConsegnato);

  }


  protected void stornaImportiRigaOmaggio(OrdineVendita testata, RigaVendita oldRiga) {
    //Valore ordinato
    BigDecimal valoreOrdinato =
       getNotNullValue(testata.getValoreOmaggiOrd()).
         subtract(
            getNotNullValue(oldRiga.getValoreOrdinato())
         );
    testata.setValoreOmaggiOrd(valoreOrdinato);
    //Valore in spedizione
    BigDecimal valoreInSpedizione =
       getNotNullValue(testata.getValoreOmaggiInSped()).
         subtract(
            getNotNullValue(oldRiga.getValoreInSpedizione())
         );
    testata.setValoreOmaggiInSped(valoreInSpedizione);
    //Valore consegnato
    BigDecimal valoreConsegnato =
       getNotNullValue(testata.getValoreOmaggiCons()).
         subtract(
            getNotNullValue(oldRiga.getValoreConsegnato())
         );
    testata.setValoreOmaggiCons(valoreConsegnato);

    //Costo ordinato
    BigDecimal costoOrdinato =
       getNotNullValue(testata.getCostoOmaggiOrd()).
         subtract(
            getNotNullValue(oldRiga.getCostoOrdinato())
         );
    testata.setCostoOmaggiOrd(costoOrdinato);
    //Costo in spedizione
    BigDecimal costoInSpedizione =
       getNotNullValue(testata.getCostoOmaggiInSped()).
         subtract(
            getNotNullValue(oldRiga.getCostoInSpedizione())
         );
    testata.setCostoOmaggiInSped(costoInSpedizione);
    //Costo consegnato
    BigDecimal costoConsegnato =
       getNotNullValue(testata.getCostoOmaggiCons()).
         subtract(
            getNotNullValue(oldRiga.getCostoConsegnato())
         );
    testata.setCostoOmaggiCons(costoConsegnato);
  }


  protected void stornaImportiRigaServizio(OrdineVendita testata, RigaVendita oldRiga) {
    //Valore ordinato
    BigDecimal valoreOrdinato =
       getNotNullValue(testata.getValoreServiziOrd()).
         subtract(
            getNotNullValue(oldRiga.getValoreOrdinato())
         );
    testata.setValoreServiziOrd(valoreOrdinato);
    //Valore in spedizione
    BigDecimal valoreInSpedizione =
       getNotNullValue(testata.getValoreServiziInSped()).
         subtract(
            getNotNullValue(oldRiga.getValoreInSpedizione())
         );
    testata.setValoreServiziInSped(valoreInSpedizione);
    //Valore consegnato
    BigDecimal valoreConsegnato =
       getNotNullValue(testata.getValoreServiziCons()).
         subtract(
            getNotNullValue(oldRiga.getValoreConsegnato())
         );
    testata.setValoreServiziCons(valoreConsegnato);

    //Costo ordinato
    BigDecimal costoOrdinato =
       getNotNullValue(testata.getCostoServiziOrd()).
         subtract(
            getNotNullValue(oldRiga.getCostoOrdinato())
         );
    testata.setCostoServiziOrd(costoOrdinato);
    //Costo in spedizione
    BigDecimal costoInSpedizione =
       getNotNullValue(testata.getCostoServiziInSped()).
         subtract(
            getNotNullValue(oldRiga.getCostoInSpedizione())
         );
    testata.setCostoServiziInSped(costoInSpedizione);
    //Costo consegnato
    BigDecimal costoConsegnato =
       getNotNullValue(testata.getCostoServiziCons()).
         subtract(
            getNotNullValue(oldRiga.getCostoConsegnato())
         );
    testata.setCostoServiziCons(costoConsegnato);
  }


  protected void stornaImportiRigaSpesa(OrdineVendita testata, RigaVendita oldRiga) {
    //Valore ordinato
    BigDecimal valoreOrdinato =
       getNotNullValue(testata.getValoreSpeseOrd()).
         subtract(
            getNotNullValue(oldRiga.getValoreOrdinato())
         );
    testata.setValoreSpeseOrd(valoreOrdinato);
    //Valore in spedizione
    BigDecimal valoreInSpedizione =
       getNotNullValue(testata.getValoreSpeseInSped()).
         subtract(
            getNotNullValue(oldRiga.getValoreInSpedizione())
         );
    testata.setValoreSpeseInSped(valoreInSpedizione);
    //Valore consegnato
    BigDecimal valoreConsegnato =
       getNotNullValue(testata.getValoreSpeseCons()).
         subtract(
            getNotNullValue(oldRiga.getValoreConsegnato())
         );
    testata.setValoreSpeseCons(valoreConsegnato);

    //Costo ordinato
    BigDecimal costoOrdinato =
       getNotNullValue(testata.getCostoSpeseOrd()).
         subtract(
            getNotNullValue(oldRiga.getCostoOrdinato())
         );
    testata.setCostoSpeseOrd(costoOrdinato);
    //Costo in spedizione
    BigDecimal costoInSpedizione =
       getNotNullValue(testata.getCostoSpeseInSped()).
         subtract(
            getNotNullValue(oldRiga.getCostoInSpedizione())
         );
    testata.setCostoSpeseInSped(costoInSpedizione);
    //Costo consegnato
    BigDecimal costoConsegnato =
       getNotNullValue(testata.getCostoSpeseCons()).
         subtract(
            getNotNullValue(oldRiga.getCostoConsegnato())
         );
    testata.setCostoSpeseCons(costoConsegnato);
  }
  //Fix 5102 - fine

  protected BigDecimal getNotNullValue(BigDecimal importo) {
    if (importo == null)
      return new BigDecimal("0");
    return importo;
  }

*/
//MG FIX 6754 fine

 /**
   * Modificato FIX02153 - DZ
   * Completamente rivisto per stornare gli importi dai nuovi valori/totali/imposte.
   */
  /* Fix 13494 The method stornaImportiRigaDaTestata is transfered to OrdineVenditaRiga
  protected void stornaImportiRigaDaTestata() {
     //Fix 5102 - inizio
    if (getDatiComuniEstesi().getStato() != DatiComuniEstesi.ANNULLATO) {
     //Fix 5102 - fine
    OrdineVendita testata = (OrdineVendita) getTestata();
    BigDecimal valoreOrdinato = null;
    BigDecimal valoreInSpedizione = null;
    BigDecimal valoreConsegnato = null;
    BigDecimal costoOrdinato = null;
    BigDecimal costoInSpedizione = null;
    BigDecimal costoConsegnato = null;
    //...FIX02407 - DZ
    BigDecimal imposta = getNotNullValue(testata.getValoreImposta());
    BigDecimal impostaInSpedizione = getNotNullValue(testata.
        getValoreImpostaInSped());
    BigDecimal impostaConsegnato = getNotNullValue(testata.getValoreImpostaCons());
    //...fine FIX02407 - DZ

    switch (getTipoRiga()) {
      case TipoRiga.MERCE:
        valoreOrdinato = testata.getValoreOrdinato();
        valoreInSpedizione = testata.getValoreInSpedizione();
        valoreConsegnato = testata.getValoreConsegnato();
        costoOrdinato = testata.getCostoOrdinato();
        costoInSpedizione = testata.getCostoInSpedizione();
        costoConsegnato = testata.getCostoConsegnato();
        break;
      case TipoRiga.OMAGGIO:
        valoreOrdinato = testata.getValoreOmaggiOrd();
        valoreInSpedizione = testata.getValoreOmaggiInSped();
        valoreConsegnato = testata.getValoreOmaggiCons();
        costoOrdinato = testata.getCostoOmaggiOrd();
        costoInSpedizione = testata.getCostoOmaggiInSped();
        costoConsegnato = testata.getCostoOmaggiCons();
        break;
      case TipoRiga.SERVIZIO:
        valoreOrdinato = testata.getValoreServiziOrd();
        valoreInSpedizione = testata.getValoreServiziInSped();
        valoreConsegnato = testata.getValoreServiziCons();
        costoOrdinato = testata.getCostoServiziOrd();
        costoInSpedizione = testata.getCostoServiziInSped();
        costoConsegnato = testata.getCostoServiziCons();
        break;
      case TipoRiga.SPESE_MOV_VALORE:
        valoreOrdinato = testata.getValoreSpeseOrd();
        valoreInSpedizione = testata.getValoreSpeseInSped();
        valoreConsegnato = testata.getValoreSpeseCons();
        costoOrdinato = testata.getCostoSpeseOrd();
        costoInSpedizione = testata.getCostoSpeseInSped();
        costoConsegnato = testata.getCostoSpeseCons();
        break;
    }

    //...FIX02407 - DZ
    valoreOrdinato = getNotNullValue(valoreOrdinato).subtract(getNotNullValue(
        getValoreOrdinato()));
    valoreInSpedizione = getNotNullValue(valoreInSpedizione).subtract(
        getNotNullValue(getValoreInSpedizione()));
    valoreConsegnato = getNotNullValue(valoreConsegnato).subtract(
        getNotNullValue(getValoreConsegnato()));
    costoOrdinato = getNotNullValue(costoOrdinato).subtract(getNotNullValue(
        getCostoOrdinato()));
    costoInSpedizione = getNotNullValue(costoInSpedizione).subtract(
        getNotNullValue(getCostoInSpedizione()));
    costoConsegnato = getNotNullValue(costoConsegnato).subtract(getNotNullValue(
        getCostoConsegnato()));

    imposta = imposta.subtract(getNotNullValue(getValoreImposta()));
    impostaInSpedizione = impostaInSpedizione.subtract(getNotNullValue(
        getValoreImpostaInSpedizione()));
    impostaConsegnato = impostaConsegnato.subtract(getNotNullValue(
        getValoreImpostaConsegnato()));
    //...fine FIX02407 - DZ

    // Fine fix 1425
    // fine fix 1390
    switch (getTipoRiga()) {
      case TipoRiga.MERCE:
        testata.setValoreOrdinato(valoreOrdinato);
        testata.setValoreInSpedizione(valoreInSpedizione);
        testata.setValoreConsegnato(valoreConsegnato);
        testata.setCostoOrdinato(costoOrdinato);
        testata.setCostoInSpedizione(costoInSpedizione);
        testata.setCostoConsegnato(costoConsegnato);
        break;
      case TipoRiga.OMAGGIO:
        testata.setValoreOmaggiOrd(valoreOrdinato);
        testata.setValoreOmaggiInSped(valoreInSpedizione);
        testata.setValoreOmaggiCons(valoreConsegnato);
        testata.setCostoOmaggiOrd(costoOrdinato);
        testata.setCostoOmaggiInSped(costoInSpedizione);
        testata.setCostoOmaggiCons(costoConsegnato);
        break;
      case TipoRiga.SERVIZIO:
        testata.setValoreServiziOrd(valoreOrdinato);
        testata.setValoreServiziInSped(valoreInSpedizione);
        testata.setValoreServiziCons(valoreConsegnato);
        testata.setCostoServiziOrd(costoOrdinato);
        testata.setCostoServiziInSped(costoInSpedizione);
        testata.setCostoServiziCons(costoConsegnato);
        break;
      case TipoRiga.SPESE_MOV_VALORE:
        testata.setValoreSpeseOrd(valoreOrdinato);
        testata.setValoreSpeseInSped(valoreInSpedizione);
        testata.setValoreSpeseCons(valoreConsegnato);
        testata.setCostoSpeseOrd(costoOrdinato);
        testata.setCostoSpeseInSped(costoInSpedizione);
        testata.setCostoSpeseCons(costoConsegnato);
        break;
    }
    // Fix 1390
    testata.setValoreImposta(imposta);
    testata.setValoreImpostaInSped(impostaInSpedizione);
    testata.setValoreImpostaCons(impostaConsegnato);

    //...FIX02407 - DZ
    if (getTipoRiga() != TipoRiga.OMAGGIO) {
      testata.setValoreTotOrdinato(getNotNullValue(testata.getValoreTotOrdinato()).
                                   subtract(getNotNullValue(getValoreImposta())).
                                   subtract(getNotNullValue(getValoreOrdinato())));
      testata.setValoreTotInSped(getNotNullValue(testata.getValoreTotInSped()).
                                 subtract(getNotNullValue(
          getValoreImpostaInSpedizione())).
                                 subtract(getNotNullValue(getValoreInSpedizione())));
      testata.setValoreTotCons(getNotNullValue(testata.getValoreTotCons()).
                               subtract(getNotNullValue(
          getValoreImpostaConsegnato())).
                               subtract(getNotNullValue(getValoreConsegnato())));
    }
    else {
      testata.setValoreTotOrdinato(getNotNullValue(testata.getValoreTotOrdinato()).
                                   subtract(getNotNullValue(getValoreImposta())));
      testata.setValoreTotInSped(getNotNullValue(testata.getValoreTotInSped()).
                                 subtract(getNotNullValue(
          getValoreImpostaInSpedizione())));
      testata.setValoreTotCons(getNotNullValue(testata.getValoreTotCons()).
                               subtract(getNotNullValue(
          getValoreImpostaConsegnato())));
    }
    //...fine FIX02407 - DZ
    // Fine fix 1390

//MG FIX 6481 inizio: per righe di tipo merce/servizi, se contabilizzazione la lordo dello sconto di fine fattura
//    Aggiungo la quota dello sconto di fine fattura
      if (getTipoRiga() != TipoRiga.OMAGGIO && getTipoRiga() != TipoRiga.SPESE_MOV_VALORE) {
        if (PersDatiVen.getCurrentPersDatiVen().getContabilizzazioneRicavi() == PersDatiVen.AL_LORDO) {
          BigDecimal valoreScFF = testata.calcolaScontoFineFatturaSoloSeAlLordo(getValoreOrdinato());
          testata.setValoreTotOrdinato(getNotNullValue(testata.getValoreTotOrdinato()).add(valoreScFF));
          valoreScFF = testata.calcolaScontoFineFatturaSoloSeAlLordo(getValoreInSpedizione());
          testata.setValoreTotInSped(getNotNullValue(testata.getValoreTotInSped()).add(valoreScFF));
          valoreScFF = testata.calcolaScontoFineFatturaSoloSeAlLordo(getValoreConsegnato());
          testata.setValoreTotCons(getNotNullValue(testata.getValoreTotCons()).add(valoreScFF));
        }
      }
//MG FIX 6481 fine
  }
  }
  Fix 13494 Fine */
  /**
   * Calcola il prezzo di una riga primaria, che ha un articolo di tipo
   * KIT e tipo calcolo prezzo DA_COMPONENTI, sommando i prezzi di tutte le
   * righe secondarie che compongono il kit e tenendo conto di un eventuale
   * percentuale di markup
   */
  public void calcolaPrezzoDaRigheSecondarieConReset(boolean reset) {
    try {
      //Fix 3929 - inizio
      char tipoParte = getArticolo().getTipoParte();
      char tipoCalcoloPrezzo = getArticolo().getTipoCalcPrzKit();
      if ( (tipoParte == ArticoloDatiIdent.KIT_NON_GEST ||
            tipoParte == ArticoloDatiIdent.KIT_GEST)
          &&
          (tipoCalcoloPrezzo == ArticoloDatiVendita.DA_COMPONENTI || (tipoCalcoloPrezzo == ArticoloDatiVendita.SUL_PRODOTTO_FINITO))) { //72269 Softre anche su pf
        //Fix 3929 - fine
        BigDecimal zero = new BigDecimal(0.0);
        BigDecimal prezzoRigaPrimaria = zero;
		BigDecimal costoRigaPrimaria = zero ;//Fix 33905
        ValorizzatoreImportiOrdineVendita viov =
          new ValorizzatoreImportiOrdineVendita();
        ImportiRigaOrdineVendita importi = viov.calcolaImportiRiga(this);

        Iterator righeSecondarie = importi.getValoriRigheSecondarie().iterator();
        while (righeSecondarie.hasNext()) {
          ImportiRigaOrdineVendita importoRigaSec =
            (ImportiRigaOrdineVendita) righeSecondarie.next();
          if (importoRigaSec.getSpecializzazioneRiga() == RIGA_SECONDARIA_PER_COMPONENTE) //MG FIX 6754 inizio
          {//Fix 33905
          	prezzoRigaPrimaria = prezzoRigaPrimaria.add(importoRigaSec.getValoreOrdinato());
          	costoRigaPrimaria = costoRigaPrimaria.add(importoRigaSec.getCostoOrdinato());// Fix 33905
          }//Fix 33905
        }

        //Fix 3929 - inizio
        //Fix 3929 - inizio
        if (this.getQtaInUMRif() != null && this.getQtaInUMRif().compareTo(new BigDecimal(0.0)) != 0)//Fix 33905
        {//Fix 33905
	        prezzoRigaPrimaria = prezzoRigaPrimaria.divide(getQtaInUMRif(), BigDecimal.ROUND_HALF_UP);
	        costoRigaPrimaria = costoRigaPrimaria.divide(getQtaInUMRif(), BigDecimal.ROUND_HALF_UP);//Fix 33905
        }//Fix 33905
        BigDecimal markup = getArticolo().getMarkupKit();
        if (markup != null && markup != zero) {
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
    catch (Exception ex) {
      ex.printStackTrace();
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
      String idSubAgente  = this.getIdSubagente();
      OrdineVendita testata = ((OrdineVendita)this.getTestata());
      if ((idAgente!=null && !idAgente.trim().equals(""))||(idSubAgente!=null && !idSubAgente.trim().equals(""))) {
        CondizioniDiVendita cV = (CondizioniDiVendita) Factory.createObject(CondizioniDiVendita.class);
        cV.setRArticolo(getIdArticolo());
        cV.setRSubAgente(idSubAgente);
        cV.setRAgente(idAgente);
        cV.setRValuta(testata.getIdValuta());
        cV.setRUnitaMisura(this.getIdUMRif());
        cV.setRCliente(this.getIdCliente());
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
        cV.setQuantita(this.getQtaInUMRif());
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
      ex.printStackTrace();
    }
  }
//MG FIX 6204

  //Pietro
  protected void setSalvaRigheSecondarie(boolean salvaRigheSecondarie) {
    iSalvaRigheSecondarie = salvaRigheSecondarie;
  }

  //Pietro
  protected boolean isSalvaRigheSecondarie() {
    return iSalvaRigheSecondarie;
  }

  //Pietro
  protected int eliminaRiga() throws SQLException {
    //Fix 16754 inizio
    if(getTipoRiga()==TipoRiga.OMAGGIO &&!getAttivaCheckCancellazione()){
     if(getIdRigaCollegata() != null && getIdDettaglioRigaCollegata() != null) {
       if (!retrieve())
        return 0;
    }
   }
  //Fix 16754 fine
    Iterator i = getRigheSecondarie().iterator();
    while (i.hasNext()) {
      OrdineVenditaRigaSec rigaSec = (OrdineVenditaRigaSec) i.next();
      rigaSec.setSalvaRigaPrimaria(false);
      rigaSec.setAggiornaRigaOfferta(false);
    }
    aggiornaPesiEVolumeTestata(true); //Fix 12508
    int rc = super.eliminaRiga();
    // Inizio 6016
    if (rc >0 && isRigaAContratto() && !getSaveFromPDC()) // Fix 8508  // fix 32227
      eliminaPianoConsegnaCollegato();
    // Fine 6016
    //Fix 20569 Inizio
    if(rc >0){
      aggiornaCommessaRif();
    }
    //Fix 20569 Fine
    // Inizio 10962 Gestione intercompany
    if (isAttivaGestioneIntercompany() && rc > 0) {
        GeneratoreOrdineAcqRigaIC gen = (GeneratoreOrdineAcqRigaIC) Factory.createObject(GeneratoreOrdineAcqRigaIC.class);
        gen.init(getModelloGenOrdineAcq(), this);
        List errors = gen.deleteOrdineAcquistoIC();
        if (!errors.isEmpty())
        throw new ThipException(errors);
     }
    // Fine 10962
    return rc;
  }

  public String getKey() {
    String idAzienda = getIdAzienda();
    String annoDocumento = getAnnoDocumento();
    String numeroDocumento = getNumeroDocumento();
    Integer numeroRigaDocumento = getNumeroRigaDocumento();
    Object[] keyParts = {
        idAzienda, annoDocumento, numeroDocumento, numeroRigaDocumento};
    return KeyHelper.buildObjectKey(keyParts);
  }

  public void setKey(String key) {
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

  protected OrdineRigaLotto creaLotto() {
    return (OrdineVenditaRigaLotto) Factory.createObject(
        OrdineVenditaRigaLottoPrm.class);
  }

  public Integer getIdDettaglioRigaCollegata() {
    return iDettaglioRigaCollegata;
  }

  /**
   * Valorizza l'attributo relativo al Proxy RigaCollegata
   * (Id dettaglio riga).
   */
  public void setIdDettaglioRigaCollegata(Integer rDettaglioRigaCollegata) {
    iDettaglioRigaCollegata = rDettaglioRigaCollegata;
    setDirty();
  }

//    protected void impostaStatoSulleRigheSecondarie()
//    {
//        char stato = getDatiComuniEstesi().getStato();
//        if (!isOnDB() || (iOldRiga.getDatiComuniEstesi().getStato() != stato))
//        {
//            Iterator i = getRigheSecondarie().iterator();
//            while(i.hasNext())
//            {
//                OrdineVenditaRigaSec ovrs = (OrdineVenditaRigaSec)i.next();
//                ovrs.getDatiComuniEstesi().setStato(stato);
//            }
//        }
//    }


  protected void impostaSaldoManuale() {
    if ( (!isOnDB() && isSaldoManuale()) ||
        (isOnDB() &&
         (getOldRiga() != null && !getOldRiga().isSaldoManuale() && isSaldoManuale()))) {
      Iterator righeSecondarie = iRigheSecondarie.iterator();
      while (righeSecondarie.hasNext()) {
        OrdineVenditaRigaSec rigaSec = (OrdineVenditaRigaSec) righeSecondarie.
            next();
        rigaSec.setSaldoManuale(true);
      }
    }
  }

  /**
   * Ridefinizione del metodo saldaRiga della classe OrdineRiga
   */
  public void saldaRiga() {
    super.saldaRiga();
    Iterator righeSecondarie = iRigheSecondarie.iterator();
    while (righeSecondarie.hasNext()) {
      OrdineVenditaRigaSec rigaSec =
          (OrdineVenditaRigaSec) righeSecondarie.next();
      rigaSec.saldaRiga();
    }
  }

  /**
   * Ridefinizione del metodo riapriRiga della classe OrdineRiga
   */
  public void riapriRiga() {
    super.riapriRiga();
    //Fix 5742 - inizio
    //Iterator righeSecondarie = iRigheSecondarie.iterator();
    Iterator righeSecondarie = getRigheSecondarie().iterator();
    //Fix 5742 - fine
    while (righeSecondarie.hasNext()) {
      OrdineVenditaRigaSec rigaSec =
          (OrdineVenditaRigaSec) righeSecondarie.next();
      // Inizio 4500
      rigaSec.setAzioneManuale(getAzioneManuale());
      // Fine 4500
      rigaSec.riapriRiga();
    }
  }

  protected void calcolaMovimentiPortafoglio() {
    super.calcolaMovimentiPortafoglio();
    Iterator i = getRigheSecondarie().iterator();
    while (i.hasNext()) {
      OrdineVenditaRigaSec ovrs = (OrdineVenditaRigaSec) i.next();
      ovrs.calcolaMovimentiPortafoglio();
    }
  }

  protected void creaOldRiga() {
    iOldRiga = (OrdineVenditaRigaPrm) Factory.createObject(OrdineVenditaRigaPrm.class);
  }

  public void creaRigaOmaggio(OrdineVenditaPO testata) throws SQLException {
    Trace.println(getKey() +
        "----------------------------gestione Righe Omaggio----------------------------");

    ListinoVenditaScaglione lvs = null;

    if (isServizioCalcDatiVendita()) {
      Trace.println("COND DATI VENDITA SI");
      if (condVen == null)
        recuperaCondizioniVendita(testata);
      if (condVen == null) {
        return;
      }
      lvs = condVen.getListinoVenditaScaglione();
    }
    else {
      Trace.println("COND DATI VENDITA NO");
      lvs = (ListinoVenditaScaglione) Factory.createObject(
          ListinoVenditaScaglione.class);
      lvs.setKey(getServizioListVendScaglione());
      lvs.retrieve();
    }

    Trace.println("ListinoVenditaScaglione=" + lvs);
    if (lvs != null) {
      ListinoVenditaOffertaOmaggio offOmg = lvs.getOffertaOmaggio();

      Trace.println("ListinoVenditaOffertaOmaggio=" + offOmg);
      Trace.println("ARTICOLO OMAGGIO=" + offOmg.getArticolo());
      Trace.println("TIPO=" + offOmg.getTipoOmaggioOfferta());

      if (offOmg != null &&
          offOmg.getTipoOmaggioOfferta() !=
          ListinoVenditaOffertaOmaggio.INCOMPLETO) {
        Trace.println(getKey() + "----------------------------superato primo controllo Righe Omaggio----------------------------");

        //Quantità di riferimento omaggio-offerta
        BigDecimal quantRiferimento = offOmg.getQuantitaRiferimento();
        BigDecimal quantMin = offOmg.getQuantitaMin();
        BigDecimal quantMax = offOmg.getQuantitaMax();

        //Fix 4858 - inizio
        //Quantità ordinata (riga)
        //BigDecimal quantOrdinata = getQtaInUMRif();
        BigDecimal quantOrdinata = new BigDecimal("0.00");
        Q6Calc.get().setScale(quantOrdinata, 2);//Fix 30871 
        char rifUMPrz = getRiferimentoUMPrezzo();
        if (rifUMPrz == RiferimentoUmPrezzo.VENDITA) {
          quantOrdinata = getQtaInUMRif();
        }
        else if (rifUMPrz == RiferimentoUmPrezzo.MAGAZZINO) {
          quantOrdinata = getQtaInUMPrmMag();
        }
        //Fix 4858 - fine

        //Calcola la quantità dovuta di articoli omaggio
        BigDecimal quantTotOmaggioOfferta = new BigDecimal("0.00");
        Q6Calc.get().setScale(quantTotOmaggioOfferta, 2);//Fix 30871 
        if (quantOrdinata.compareTo(quantRiferimento) >= 0) {
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

        if (quantTotOmaggioOfferta.compareTo(quantMin) >= 0) {
          Trace.println(getKey() + "----------------------------superato secondo controllo Righe Omaggio----------------------------");

          if (quantTotOmaggioOfferta.compareTo(quantMax) > 0)
            quantTotOmaggioOfferta = quantMax;

          //Prepara i valori che si differenziano tra i due tipi di riga
          CausaleRigaVendita causale = null;
          char tipoRiga = '\0';

          switch (offOmg.getTipoOmaggioOfferta()) {
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
              if (causale != null) {
                BigDecimal newQta = getQtaInUMRif();
                newQta = newQta.subtract(quantTotOmaggioOfferta);
                // Fix 2636
                this.setQtaInUMRif(newQta);
                // Fine fix 2636
                if (getUMPrm() != null)
                  setQtaInUMPrmMag(getArticolo().convertiUM(newQta, getUMRif(),
                      getUMPrm(), getArticoloVersRichiesta())); // fix 10955
                if (getUMSec() != null)
                  setQtaInUMSecMag(getArticolo().convertiUM(getQtaInUMPrmMag(),
                      getUMPrm(), getUMSec(), getArticoloVersRichiesta())); // fix 10955

                tipoRiga = TipoRiga.OMAGGIO;
                // Fix 2636
                if (this.getArticolo().isArticLotto()) {
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

          if (causale != null) {
            Trace.println(getKey() + "----------------------------superato terzo controllo Righe Omaggio----------------------------");

            //Articolo della riga omaggio/offerta
            Articolo articolo = offOmg.getArticolo();

            //Crea le righe
            rigaOmf = (OrdineVenditaRigaPrm) Factory.createObject(
                OrdineVenditaRigaPrm.class);

            rigaOmf.setServizioCalcDatiVendita(false);
            rigaOmf.setRigaOfferta(offOmg.getTipoOmaggioOfferta() ==
                                   ListinoVenditaOffertaOmaggio.OFFERTA);

            //Chiave
            rigaOmf.setTestata(testata);
            //PM Fix 1988 Inizio
            //rigaOmf.setNumeroRigaDocumento(new Integer(RigaVendita.getNumeroNuovaRiga(testata)));
            //PM Fix 1988 Fine


            //Fix 3880 Inizio PM
            rigaOmf.setNonFatturare(causale.isNonFatturare());
            //Fix 3880 Fine PM


            //Campi not nullable
            rigaOmf.setRigaCollegata(this);
            rigaOmf.setIdDettaglioRigaCollegata(getDettaglioRigaDocumento());
            rigaOmf.setTipoRiga(tipoRiga);
            rigaOmf.setStatoAvanzamento(getStatoAvanzamento());
            // Fix 2636
            if (offOmg.getArticolo().isArticLotto()) {
              // Inizio 3814
              //rigaOmf.setStatoAvanzamento(StatoAvanzamento.PROVVISORIO);
              rigaOmf.setStatoAvanzamento(getStatoAvanzamento());
              // Fine 3814
            }
            // Fine fix 2636
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

            //UnitaMisura unitaMisuraVendita = offOmg.getUnitaMisura();
            //if (unitaMisuraVendita == null)
            //unitaMisuraVendita = articolo.getUMDefaultVendita();

            //rigaOmf.setUMRif(unitaMisuraVendita);
            //rigaOmf.setQtaInUMRif(quantTotOmaggioOfferta);


            //

            // Faccio questo perchè l'unità di misura che proviene dal listino potrebbe
            // non essere di vendita.
            UnitaMisura unitaMisuraVendita = offOmg.getUnitaMisura();
            boolean passato = false;
            List l = articolo.getArticoloDatiVendita().getForcedUMSecondarie();
            Iterator iter = l.iterator();
            while (iter.hasNext()) {
              UnitaMisura uni = (UnitaMisura) iter.next();
              // Fix 2614
              //if (uni.equals(unitaMisuraVendita)){
              if (unitaMisuraVendita != null && uni != null &&
                  uni.
                  getIdUnitaMisura().equals(unitaMisuraVendita.getIdUnitaMisura())) {
                // Fine fix 2164
                passato = true;
                break;
              }
            }
            if (unitaMisuraVendita == null || !passato) {
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
            if (unitaMisuraPrm != null) {
              rigaOmf.setUMPrm(unitaMisuraPrm);
              if (unitaMisuraPrm.equals(unitaMisuraVendita))
                rigaOmf.setQtaInUMPrmMag(quantTotOmaggioOfferta);
              else
                rigaOmf.setQtaInUMPrmMag(articolo.convertiUM(
                    quantTotOmaggioOfferta, unitaMisuraVendita, unitaMisuraPrm, rigaOmf.getArticoloVersRichiesta())); // fix 10955
            }

            UnitaMisura unitaMisuraSec = articolo.getUMSecMag();
            if (unitaMisuraSec != null) {
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
            rigaOmf.setIdListino(offOmg.getIdListino());
            rigaOmf.setPrezzo(offOmg.getPrezzo());
            if (offOmg.getTipoOmaggioOfferta() != TipoRiga.OMAGGIO) {
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

//Inizio fix 1420 PM
  protected List getStatoEvasioneRighe() {
    String key = null;
    List ret = new ArrayList();

    synchronized (cSelectStatoEvasioneRiga) {
      ResultSet rs = null;
      try {
        //Verifica se su DB c'è la riga omaggio
        Database db = ConnectionManager.getCurrentDatabase();
        db.setString(cSelectStatoEvasioneRiga.getStatement(), 1, getIdAzienda());
        db.setString(cSelectStatoEvasioneRiga.getStatement(), 2,
                     getAnnoDocumento());
        db.setString(cSelectStatoEvasioneRiga.getStatement(), 3,
                     getNumeroDocumento());

        rs = cSelectStatoEvasioneRiga.getStatement().executeQuery();
        //Verifica se c'è la riga omaggio
        while (rs.next()) {
          OrdineRiga.StatoEvasioneRiga ser = new OrdineRiga.StatoEvasioneRiga();
          ser.setNumeroRigaDocumento(new Integer(rs.getInt(OrdineVenditaRigaTM.
              ID_RIGA_ORD)));
          ser.setStatoEvasione(rs.getString(OrdineVenditaRigaTM.STATO_EVASIONE).
                               charAt(0));
          ret.add(ser);
        }
      }
      catch (SQLException ex) {
        ex.printStackTrace();
      }
      finally {
        if (rs != null)
          try {
            rs.close();
          }
          catch (SQLException ex) {
            ex.printStackTrace();
          }
      }
    }

    return ret;

  }

//Fine fix 1420 PM


  //Fix 1918 - inizio
  /**
   * Ridefinizione.
   */
  public boolean isArticoloRigaArticoloDefaultCatalogo() {
    boolean ret = false;

    PersDatiVen pdv = PersDatiVen.getCurrentPersDatiVen();
    String idArtPdv = pdv.getIdArticoloPerCatalogoEst();
    if (idArtPdv != null) {
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
   * @see OrdineVenditaRigaPrmFormActionAdapter#verificaArticoloCatalogoCompat
   */
  public ErrorMessage checkArticoloCatalogoCompat() {
    ErrorMessage em = null;

    CatalEsterno ce = getCatalogoEsterno();
    if (ce != null) {
      Articolo artCatal = ce.getArticolo();
      //Fix 2380 - inizio
      if (artCatal == null) {
        em = new ErrorMessage("THIP_BS347");
      }
      else {
        //Fix 2380 - fine
        //Verifica u.m. vendita
        List umvList = artCatal.getArticoloDatiVendita().getForcedUMSecondarie();
        Iterator iterUmvList = umvList.iterator();
        boolean okUMRif = false;
        while (iterUmvList.hasNext() && !okUMRif) {
          UnitaMisura um = (UnitaMisura) iterUmvList.next();
          okUMRif = um.getIdUnitaMisura().equals(getIdUMRif());
        }
        //Verifica u.m. primaria magazzino
        boolean okUMPrmMag = artCatal.getUMPrmMag().equals(getUMPrm());
        //Verifica u.m. secondaria magazzino
        boolean okUMSecMag = true;
        if (artCatal.getUMSecMag() != null && getUMSec() != null) {
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
   * Ridefinizione
   */
  public void impostaArticoloCatalogo() {
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


  //Fix 2563 - inizio
  /**
   * Ridefinizione
   */
  public void cambiaArticolo(
      Articolo articolo,
      Configurazione config,
      boolean recuperaDatiVenAcq) {
    datiArticolo.setParIntestatario(getIdCliente());
    datiArticolo.setParIdListino(getIdListino());
	if(getQtaInUMRif() != null)//45398
    	datiArticolo.setParQtaUMRif(getQtaInUMRif().toString());

    if (getIdAgente() != null) {
      datiArticolo.setParIdAgente(getIdAgente());
      if (getProvvigione1Agente() != null) {
        datiArticolo.setParProvvigione1Agente(getProvvigione1Agente().toString());
      }
    }
    if (getIdSubagente() != null) {
      datiArticolo.setParIdSubagente(getIdSubagente());
      if (getProvvigione1Subagente() != null) {
        datiArticolo.setParProvvigione1Subagente(getProvvigione1Subagente().
                                                 toString());
      }
    }
    String idModPag = ( (DocumentoOrdineTestata) getTestata()).
        getIdModPagamento();
    if (idModPag != null) {
      datiArticolo.setParIdModPagamento(idModPag);
    }
    super.cambiaArticolo(articolo, config, recuperaDatiVenAcq);

    setPrcPerditaResiduo(datiArticolo.getPercentualePerditaResiduoNumerico());
    setIdAgente( ( (DatiArticoloRigaVendita) datiArticolo).getAgentiProvvigioni().
                getIdAgente());
    setProvvigione1Agente( ( (DatiArticoloRigaVendita) datiArticolo).
                          getProvvigioneAgenteNumerico());
    setIdSubagente( ( (DatiArticoloRigaVendita) datiArticolo).
                   getAgentiProvvigioni().getIdSubagente());
    setProvvigione1Subagente( ( (DatiArticoloRigaVendita) datiArticolo).
                             getProvvigioneSubagenteNumerico());

    try {
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
      setQtaInUMPrmMag(articolo.convertiUM(getQtaInUMRif(), umRif, umPrm, getArticoloVersRichiesta())); // fix 10955

      String idUMSecMag = datiArticolo.getIdUMSecondaria();
      if (idUMSecMag != null && idUMSecMag.length() > 0) {
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
        setQtaInUMSecMag(articolo.convertiUM(getQtaInUMRif(), umRif, umSec, getArticoloVersRichiesta())); // fix 10955
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }

    if (recuperaDatiVenAcq) {
      try {
        calcolaDatiVendita( (OrdineVendita) getTestata());
      }
      catch (Exception ex) {

      }
    }
  }

  //Fix 2563 - fine


  // Fix 3212
  public void setTipoModello(ModproEsplosione esplosione) {
    esplosione.setTipiModello(new char[] {ModelloProduttivoPO.PRODUZIONE});
  }

  //Fine fix 3212


  //Fix 3197 - inizio
  public void setServeRicalProvvAg(boolean b) {
    this.iServeRicalcoloProvvAgente = b;
  }

  public boolean isServeRicalProvvAg() {
    return iServeRicalcoloProvvAgente;
  }

  public void setServeRicalProvvSubag(boolean b) {
    this.iServeRicalcoloProvvSubagente = b;
  }

  public boolean isServeRicalProvvSubag() {
    return iServeRicalcoloProvvSubagente;
  }

  protected void modificaProvv2Agente() throws SQLException {
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
//          getScontoArticolo2(),
            getScontoArticolo2CalcoloScontoScalaSconti(),
            //Fix 26145 - fine
            //Fix 24299 - inizio
//          getMaggiorazione(),
          	getMaggiorazioneCalcoloScontoScalaSconti(),
          	//Fix 24299 - fine
            getSconto(),
            2
        );
    
    sconto = getScontoProvv2Pers(sconto);	//Fix 28653
    
    } //27616
//MG FIX 4348

//MG FIX 10750 inizio
    //if (isServeRicalProvvAg() || isServeRicalProvvSubag()) { //Fix 25214 PM
    //if (isServeRicalProvvAg() || isServeRicalProvvSubag() || !isOnDB()) { //Fix 25214 PM//Fix 26599
    if (isServeRicalProvvAg() || isServeRicalProvvSubag() || isRicalProvvAgSubag()) { //Fix 26599
      if (condVen == null) 
        recuperaCondizioniVendita((OrdineVendita)this.getTestata());
    }
//MG FIX 10750 fine

    //if(isServeRicalProvvAg()) //Fix 25214 PM
    //if(isServeRicalProvvAg() || !isOnDB()) //Fix 25214 PM//Fix 26599
    //if(isServeRicalProvvAg() || isRicalProvvAgSubag()) //Fix 26599
    if(isServeRicalProvvAg() || isRicalProvvAgSubag()) //Fix 26599) //Fix 26599    
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
//Fix 3738 (aggiunto controllo su null) - inizio
      if (provv2 != null) {
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

//Fix 3738 (aggiunto controllo su null) - inizio
      if (provv2 != null) {
        setProvvigione2Subagente(provv2);
      }
//Fix 3738 - fine
    }
  }

//Fix 3197 - fine
  // fix 3016
  protected void calcolaDatiVendita(OrdineVenditaPO testata) throws
      SQLException {
    SchemaPrezzo schema = this.getArticolo().getArticoloDatiVendita().
        getSchemaPrzVen();
    // Inizio 8393
    // Inizio 8863
    String keyContratto = KeyHelper.buildObjectKey(new String[]{getIdAzienda(), getAnnoDocumento(), getNumeroDocumento()});
    CondizioniDiVendita condVenCnt = RecuperaDatiVendita.getCondizioniContrattoVendita(keyContratto);
    // Se la riga è a contratto e le condizioni di vendita sul contratto
    // sono specifiche.
    if (isRigaAContratto() && condVenCnt != null){
      impostaCondizioniVenditaDaContratto(condVenCnt);
    }
    // Fine 8863
    // Fine 8393
    else if (schema != null &&
        schema.getTipoSchemaPrz() == SchemaPrezzo.TIPO_SCH_ACQ_VEN &&
        getProvenienzaPrezzo() != TipoRigaRicerca.MANUALE && this.isConCantiere()) {
      java.sql.Date dataValid = null;
      //Fix 3770 - inizio
      //PersDatiVen pda = (PersDatiVen)
      //  PersDatiVen.elementWithKey(
      //    PersDatiVen.class,
      //    Azienda.getAziendaCorrente(),
      //    PersistentObject.NO_LOCK
      //  );
      PersDatiVen pda = PersDatiVen.getCurrentPersDatiVen();
      //Fix 3770 - fine
      char tipoDataPrezziSconti = testata.getCliente().
          getRifDataPerPrezzoSconti();
      if (tipoDataPrezziSconti == RifDataPrzScn.DA_CONDIZIONI_GENERALI) {
        tipoDataPrezziSconti = pda.getTipoDataPrezziSconti();
      }
      switch (tipoDataPrezziSconti) {
        case RifDataPrzScn.DATA_ORDINE:
          dataValid = TimeUtils.getDate(testata.getDataDocumento());
          break;
        case RifDataPrzScn.DATA_CONSEGNA:
          dataValid = TimeUtils.getDate(this.getDataConsegnaConfermata());
          break;
      }
      // Fix 06144 ini
      //RicercaPrezziExtraVendita ricerca = new RicercaPrezziExtraVendita();
      RicercaPrezziExtraVendita ricerca = (RicercaPrezziExtraVendita)Factory.createObject(RicercaPrezziExtraVendita.class);
      // Fix 06144 fin

      CondizioniVEPrezziExtra condAcq = null;
      try {
        CausaleOrdineVendita causale = testata.getCausale();  //...FIX04356 - DZ
        ModalitaConsegna mod = testata.getModalitaConsegna();
        String sMod = "0";
        if (mod != null) {
          sMod = String.valueOf(mod.getTipoConsegna());
        }
        HashMap map = ricerca.ricercaPrezziExtraLaterizi(this.getIdAzienda(),
            this.getIdCliente(), testata.getIdDivisione(), causale.getContoTrasformazione(), //...FIX04356 - DZ
            testata.getIdValuta(), this.getIdArticolo(),
            this.getIdConfigurazione(),
            this.getIdUMRif(), this.getIdUMPrm(),
            this.getQuantitaOrdinata().getQuantitaInUMRif(),
            this.getQuantitaOrdinata().getQuantitaInUMPrm(),
            TimeUtils.getDate(dataValid), null, null, null, false,
            this.getIdUMSec(), this.getQuantitaOrdinata().getQuantitaInUMSec(),
            ( (OrdineVenditaTestata) testata).getRAnnoCantiere(),
            ( (OrdineVenditaTestata) testata).getRNumeroCantiere(), sMod);
        condAcq = (CondizioniVEPrezziExtra) map.get("CondVen");
        if (condAcq.getNumRigaCantiere() != null) {
          Agente age = (Agente) map.get("thAgente");
          Agente sage = (Agente) map.get("thSubagente");
          if (age != null) {
            this.setProvvigione1Agente( (BigDecimal) map.get("thProvvAgente1"));
            this.setProvvigione2Agente( (BigDecimal) map.get("thProvvAgente2"));
            this.setAgente(age);
          }
          else {
            this.setProvvigione1Agente(null);
            this.setProvvigione2Agente(null);
            this.setAgente(null);
          }
          if (sage != null) {
            this.setProvvigione1Subagente( (BigDecimal) map.get(
                "thProvvSubAgente1"));
            this.setProvvigione2Subagente( (BigDecimal) map.get(
                "thProvvSubAgente2"));
            this.setSubagente(sage);
          }
          else {
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
      catch (SQLException ex) {
        condAcq = null;
      }

      if (condAcq != null) {
        DocOrdRigaPrezziExtra rigaPrezzi = this.getRigaPrezziExtra();
        if (rigaPrezzi == null) {
          this.istanziaRigaPrezziExtra();
        }
        rigaPrezzi = this.getRigaPrezziExtra();
        rigaPrezzi.aggiornaDatiDaCondVen(condAcq);
        this.setPrezzo(condAcq.getPrezzoRiga());
        this.setProvenienzaPrezzo(TipoRigaRicerca.CONTRATTO);
        this.setRiferimentoUMPrezzo(condAcq.getRiferimentoUMPrezzo());

        if ( (this.getIdAgente() != null || this.getIdSubagente() != null) &&
            condAcq.getNumRigaCantiere() == null) {
          CondizioniDiVendita cV = new CondizioniDiVendita();
          cV.setRArticolo(this.getIdArticolo());
          cV.setRSubAgente(this.getIdSubagente());
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
          cV.setQuantita(this.getQtaInUMRif());
          cV.setPrezzo(condAcq.getPrezzoRiga());
          cV.setRModalitaPagamento(testata.getIdModPagamento());

          RicercaCondizioniDiVendita ric = new RicercaCondizioniDiVendita();
          ric.setCondizioniDiVendita(cV);
          ric.aggiornaProvvigioni();

          if (this.getIdAgente() != null) {
            this.setProvvigione2Agente(cV.getProvvigioneAgente2());
          }
          if (this.getIdSubagente() != null) {
            this.setProvvigione2Subagente(cV.getProvvigioneSubagente2());
          }

        }
      }

    }
    else {
      super.calcolaDatiVendita(testata);
    }
  }

  // Inizio 8393
  public void impostaCondizioniVenditaDaContratto(CondizioniDiVendita condVenCnt){
    if (condVenCnt != null){
      setProvenienzaPrezzo(condVenCnt.getTipoTestata());
      setPrezzo(condVenCnt.getPrezzo());
      setPrezzoExtra(condVenCnt.getPrezzoExtra());
      setScontoArticolo1(condVenCnt.getScontoArticolo1());
      setScontoArticolo2(condVenCnt.getScontoArticolo2());
      String keySconto = condVenCnt.getScontoKey();
      String idSconto = KeyHelper.getTokenObjectKey(keySconto,2);
      setIdSconto(idSconto);
      setIdScontoMod(condVenCnt.getIdScontoModalita());
      setRiferimentoUMPrezzo(condVenCnt.getUMPrezzo());
      setIdAgente(condVenCnt.getRAgente());
      setIdSubagente(condVenCnt.getRSubAgente());
      setProvvigione1Agente(condVenCnt.getProvvigioneAgente1());
      setProvvigione2Agente(condVenCnt.getProvvigioneAgente2());
      setProvvigione1Subagente(condVenCnt.getProvvigioneSubagente1());
      setProvvigione2Subagente(condVenCnt.getProvvigioneSubagente2());
    }
  }
  // Fine 839



  // fine fix 3016

  // Inizio 4486
  //Fix 3769 BP ini...
  public BigDecimal getPrezzoRiferimento() {
    OrdineRigaPrezziExtraVendita prezziExtra = (OrdineRigaPrezziExtraVendita)this.getRigaPrezziExtra();
    if (prezziExtra != null) {
      return prezziExtra.getPrezzoRiferimento();
    }
    return null;
  }

  public void setPrezzoRiferimento(BigDecimal b) {
    OrdineRigaPrezziExtraVendita prezziExtra = (OrdineRigaPrezziExtraVendita)this.getRigaPrezziExtra();
    if (prezziExtra != null) {
      prezziExtra.setPrezzoRiferimento(b);
    }
  }
//Fix 3769 BP fine.
  // Fine 4486

  //Fix 3230 - inizio
  public void setGeneraRigheSecondarie(boolean b) {
    this.iGeneraRigheSecondarie = b;
  }

  public boolean isGeneraRigheSecondarie() {
    return iGeneraRigheSecondarie;
  }

  /**
   * FIX04607 - DZ
   * @param b boolean
   */
  public void setDisabilitaRigheSecondarieForCM(boolean b) {
    iDisabilitaRigheSecondarieForCM = b;
  }

  /**
   * FIX04607 - DZ
   * @return boolean
   */
  public boolean isDisabilitaRigheSecondarieForCM() {
    return iDisabilitaRigheSecondarieForCM;
  }

  /**
   * Ridefinizione.
   */
  protected DocumentoOrdineRiga getRigaDestinazionePerCopia() {
    return (OrdineVenditaRigaPrm) Factory.createObject(OrdineVenditaRigaPrm.class);
  }


  /**
   * Ridefinizione.
   */
  public DocumentoOrdineRiga copiaRiga(DocumentoOrdineTestata docDest, SpecificheCopiaDocumento spec) throws CopyException {
    OrdineVenditaRigaPrm riga = (OrdineVenditaRigaPrm)(super.copiaRiga(docDest, spec));
    if (riga != null) {
      //Copia righe secondarie
      riga.setGeneraRigheSecondarie(false);
      List righe = getRigheSecondarie();
      for (Iterator iter = righe.iterator(); iter.hasNext(); ) {
        OrdineVenditaRigaSec rigaSec = (OrdineVenditaRigaSec) iter.next();
        //Fix 5110 - inizio
        //rigaSec.setRigaPrimaria(riga);	//Fix 3953
        spec.setRigaPrimariaDest(riga);
        //DocumentoOrdineRiga rigaCopiata = rigaSec.copiaRiga((DocumentoOrdineTestata) riga.getTestata(), spec);
        DocumentoOrdineRiga rigaCopiata = rigaSec.copiaRiga(docDest, spec);
        //Fix 5110 - fine
        if (rigaCopiata != null) {
          riga.getRigheSecondarie().add(rigaCopiata);
        }
      }
    }
    return riga;
  }

  //Fix 3230 - fine
  // Inizio 4500
  public void impostaStatoAvanzamentoSecondarie(){
    	/* Fix 44409 Inizio
  
    if (isOnDB()){
      // 5601 : Suggerita da DM
       if (getOldRiga() != null){
       // Fine 5601 Suggerita da DM
          char statoAvanzamentoOld = getOldRiga().getStatoAvanzamento();
         if (getStatoAvanzamento() != statoAvanzamentoOld){
           Iterator iter = getRigheSecondarie().iterator();
           while (iter.hasNext()){
             OrdineVenditaRigaSec ordRigaSec = (OrdineVenditaRigaSec)iter.next();
             ordRigaSec.setStatoAvanzamento(getStatoAvanzamento());
           }
         }
      }
    }
    */ 
	  //Fix 44409	  
    boolean propagaStato = isOnDB() && (getOldRiga() != null) && (getStatoAvanzamento() != getOldRiga().getStatoAvanzamento() );
    propagaStato = propagaStato || isInCopiaRiga;		
    if (propagaStato){
	    //propagaStato
	    Iterator iter = getRigheSecondarie().iterator();
	    while (iter.hasNext()){
	      OrdineVenditaRigaSec ordRigaSec = (OrdineVenditaRigaSec)iter.next();
	      ordRigaSec.setStatoAvanzamento(getStatoAvanzamento());
	    }
    }
  //Fix 44409 Fine

  }
  // Fine 4500

  /**
   * FIX04607 - DZ.
   * Aggiunto per anticipare il controllo sull'esistenza di modello/distinta
   * per la generazione delle righe secondarie in presenza di articolo kit.
   * @param components
   * @return Vector
   */
  public Vector checkAll(BaseComponentsCollection components){
    //Inizio 21389
  	if (isOnDB()){
  		components.getGroup(BaseValidationGroup.KEY_VALIDATION_GROUP).setCheckMode(BaseBOComponentManager.CHECK_NEVER);
		}
  	//fine 21389
    //Fix 45246 inizio  
  	Vector errors = new Vector();
    if(isCheckRigaDaIngorare())
    	return errors;
  	//Vector errors = super.checkAll(components);
     errors = super.checkAll(components);
    //Fix 45246 fine

    Vector otherErrors = new Vector();
    otherErrors.addElement(checkRigheSecondarie());
    for (int i = 0; i < otherErrors.size(); i++){
      ErrorMessage err = (ErrorMessage)otherErrors.elementAt(i);
      if (err != null)
        errors.addElement(err);
    }
    //Fix 08920 ini
    ErrorMessage em = checkStatoAnnullato();
    if (em != null)
      errors.addElement(em);
    //Fix 08920 fin
    //Fix 17374 inizio
     ErrorMessage error =checkRigaPianoConsegnaCollegata();
     if (error != null)
      errors.addElement(error);
     //Fix 17374 fine
     //Fix 20387 inizio
     error = checkIdEsternoConfigInCopia();
     if(error != null)
       errors.addElement(error);
     //Fix 20387 fine
     //Fix 23345 inizio
     error = controlloRicalcoloCondizioniVen();
     if (error != null)
       errors.addElement(error);
     //Fix 23345 fine
	//27649 inizio
     error = checkQtaInUMVen();
     if (error != null)
       errors.addElement(error);

     error = checkQtaInUMPrmMag();
     if (error != null)
       errors.addElement(error);

     error = checkQtaInUMSecMag();
     if (error != null)
       errors.addElement(error);
     //27649 fine   
    return errors;
  }

  //fix 7183 inizio
  protected boolean isCheckAccPrnSuSecondarie() {
     boolean checkSecondarie = isOnDB() &&
                               getRigheSecondarie().size() > 0 &&
                               getArticolo().getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST;
     return checkSecondarie;
  }

  protected ErrorMessage checkAccantonatoPrenotatoSecondarie() {
     ErrorMessage error = null;
     BigDecimal zero = new BigDecimal("0.00");
     Q6Calc.get().setScale(zero, 2);//Fix 30871 
     
     Iterator iterSec = getRigheSecondarie().iterator();
    while (iterSec.hasNext() && error == null) {
        OrdineVenditaRigaSec rigaSec = (OrdineVenditaRigaSec)iterSec.next();
        QuantitaInUM qtaAccPrn = rigaSec.getQuantitaAccantonata();

        BigDecimal qtaAccPrnPrm = qtaAccPrn.getQuantitaInUMPrm();
        if(qtaAccPrnPrm == null)
           qtaAccPrnPrm = zero;

        BigDecimal qtaAccPrnSec = qtaAccPrn.getQuantitaInUMSec();
        if(qtaAccPrnSec == null)
           qtaAccPrnSec = zero;

      if (qtaAccPrnPrm.compareTo(zero) > 0) { //la qta primaria fa da driver
           error = checkAccPrnStato();
           if(error == null)
              error = checkAccPrnSaldoManuale();
           if(error == null)
              error = checkAccPrnQuantitaRigaSec(rigaSec, qtaAccPrnPrm, qtaAccPrnSec);
        }
     }
     return error;
  }

  protected ErrorMessage checkAccPrnQuantitaRigaSec(OrdineVenditaRigaSec rigaSec, BigDecimal qtaAccPrnPrm, BigDecimal qtaAccPrnSec) {
     ErrorMessage err = null;
     BigDecimal qtaOrdRigaPrmPrm = getQtaInUMPrmMag();
     BigDecimal zero = new BigDecimal("0.00");
     Q6Calc.get().setScale(zero, 2);//Fix 30871 
     if(!rigaSec.isBloccoRicalcoloQtaComp())
     {
        BigDecimal newQtaOrdPrm = qtaOrdRigaPrmPrm.multiply(rigaSec.getCoefficienteImpiego());
        //newQtaOrdPrm = newQtaOrdPrm.setScale(qtaOrdRigaPrmPrm.scale(), BigDecimal.ROUND_HALF_UP);//Fix 30871
		//newQtaOrdPrm = Q6Calc.get().setScale(newQtaOrdPrm,qtaOrdRigaPrmPrm.scale(), BigDecimal.ROUND_HALF_UP);//Fix 30871 //Fix 39402
        newQtaOrdPrm = Q6Calc.get().setScale(newQtaOrdPrm,2, BigDecimal.ROUND_HALF_UP);//Fix 39402
        Articolo articolo = rigaSec.getArticolo();
        UnitaMisura umPrm = rigaSec.getUMPrm();
        UnitaMisura umRif = rigaSec.getUMRif();
        UnitaMisura umSec = rigaSec.getUMSec();
        BigDecimal newQtaOrdRif = (umRif == null) ? new BigDecimal(0.0) : articolo.convertiUM(newQtaOrdPrm, umPrm, umRif, rigaSec.getArticoloVersRichiesta()); // fix 10955
        BigDecimal newQtaOrdSec = (umSec == null) ? new BigDecimal(0.0) : articolo.convertiUM(newQtaOrdRif, umRif, umSec, rigaSec.getArticoloVersRichiesta()); // fix 10955

        if(UnitaMisura.isPresentUMQtaIntera(umRif, umPrm, umSec, articolo)) {
           QuantitaInUMRif qta = articolo.calcolaQuantitaArrotondate(newQtaOrdPrm, umRif, umPrm, umSec, rigaSec.getArticoloVersRichiesta(), Articolo.UM_PRM); // fix 10955
           newQtaOrdRif = qta.getQuantitaInUMRif();
           newQtaOrdPrm = qta.getQuantitaInUMPrm();
           newQtaOrdSec = qta.getQuantitaInUMSec();
        }
        BigDecimal newQtaResPrm = newQtaOrdPrm;
        newQtaResPrm = newQtaResPrm.subtract(rigaSec.getQuantitaSpedita().getQuantitaInUMPrm());

        BigDecimal newQtaResSec = zero;
      if (umSec != null) {
           newQtaResSec = newQtaOrdSec;
           newQtaResSec = newQtaResSec.subtract(rigaSec.getQuantitaSpedita().getQuantitaInUMSec());
        }

        //la chiamata è fatta su riga primaria perchè i controlli su stati etc sono da fare su primaria
        err = checkAccPrnQuantita(qtaAccPrnPrm, qtaAccPrnSec, newQtaResPrm, newQtaResSec);
     }
     return err;
  }
  //fix 7183 fine

  /**
   * FIX04607 - DZ. FIX04669 - DZ
   * Questo controllo veniva fatto durante la save e l'error message -warning- restituito tramite
   * ThipException, ma questo rendeva impossibile continuare il salvataggio della riga primaria.
   * FIX07876 - DZ: aggiunto test getMagazzino != null per CM.
   * @return ErrorMessage avviso non bloccante
   */
  protected ErrorMessage checkRigheSecondarie(){
    //Fix 4976 - inizio
    Articolo articolo = getArticolo();
    //Fix 4976 - fine
    if (articolo != null && //...FIX04814 - DZ
        (!isOnDB() && getTipoRiga() != TipoRiga.SPESE_MOV_VALORE &&
        (articolo.getTipoParte() == ArticoloDatiIdent.KIT_GEST ||
         articolo.getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST))
         && !isDisabilitaRigheSecondarieForCM()) {


      ModelloProduttivo modpro = null;
      boolean okModello = false;
      Stabilimento stab = null;
      if (getMagazzino() != null) //...FIX07876 - DZ
        stab = getMagazzino().getStabilimento();
      stab = (stab == null) ? PersDatiGen.getCurrentPersDatiGen().getStabilimento() : stab;
      if (stab == null)
        return new ErrorMessage("THIP110305");
      try {
        modpro = ModproEsplosione.trovaModelloProduttivo(getIdAzienda(), articolo.getIdArticolo(),
                 stab.getIdStabilimento(), getDataConsegnaConfermata(), getIdCommessa(),
                 ModelloProduttivo.GENERICO, new char[] {ModelloProduttivo.KIT});
        okModello = modpro != null;
      }
      catch (SQLException ex) {
        okModello = false;
      }

      if (!okModello) {
        try {
          modpro = ModproEsplosione.trovaModelloProduttivo(getIdAzienda(), articolo.getIdArticolo(),
                   stab.getIdStabilimento(), getDataConsegnaConfermata(), getIdCommessa(),
                   ModelloProduttivo.GENERICO, new char[] {ModelloProduttivo.PRODUZIONE});
          okModello = modpro != null;
        }
        catch (SQLException ex) {
          okModello = false;
        }
      }

      if (!okModello){
        try{
          List datiRigheKit = getEsplosioneNodo(articolo).getNodiFigli();
          if (datiRigheKit.isEmpty()){
            setGeneraRigheSecondarie(false);
            return new ErrorMessage("THIP_BS151");
          }
        }
        catch (SQLException ex){
          ex.printStackTrace();
        }

      }
    }
    return null;
  }


  //Fix 4749 - inizio
  /**
   * Ridefinizione
   */
  public void propagaDatiTestata(SpecificheModificheRigheOrd spec) {
    super.propagaDatiTestata(spec);
    Iterator righeSec = getRigheSecondarie().iterator();
    while (righeSec.hasNext()) {
      OrdineVenditaRigaSec rigaSec = (OrdineVenditaRigaSec)righeSec.next();
      rigaSec.propagaDatiTestata(spec);
    }
  }
  //Fix 4749 - fine


//MG FIX 4656 inizio
  /**
   * creaLottiAutomatici
   */
   public void creaLottiAutomatici() {
     List lottiOrig = new ArrayList();

     // creo lotti automatici solo per ordini in conto trasformazione
     OrdineVendita testa  = (OrdineVendita) this.getTestata();
     if (!testa.getCausale().getContoTrasformazione())
       return;

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
     impostaDatiPerBene(pal);//Fix 40598
     List lottiAuto = pal.creaLottiAutomatici();
     //Fix 39531 Inizio
 	QuantitaInUMRif qtaUnitario = new QuantitaInUMRif(new BigDecimal(1),new BigDecimal(0),new BigDecimal(0));
 	if(pal.isGenAutomaticaLottiUnitari() )
 	{
 		qtaUnitario.setQuantitaInUMRif(getArticolo().convertiUMArrotondate(qtaUnitario.getQuantitaInUMPrm(), getUMPrm(), getUMRif(), getArticoloVersRichiesta()));//To do
 		if(getUMSec() != null)
 			qtaUnitario.setQuantitaInUMSec(getArticolo().convertiUMArrotondate(qtaUnitario.getQuantitaInUMPrm(), getUMPrm(), getUMSec(), getArticoloVersRichiesta()));
 	}
    //Fix 39531 Fine
     //...Se è stato creato un lotto automatico genero una riga lotto con quel lotto
     if(lottiAuto != null && !lottiAuto.isEmpty()) {
       getRigheLotto().clear();
       for (int j = 0; j < lottiAuto.size(); j++) {
         Lotto lt = (Lotto)lottiAuto.get(j);
         //OrdineAcquistoRigaLotto lotto = (OrdineAcquistoRigaLotto)Factory.createObject(OrdineAcquistoRigaLottoPrm.class);
         OrdineRigaLotto lotto = creaLotto();
         lotto.setFather(this);
         lotto.setIdArticolo(lt.getCodiceArticolo());
         lotto.setIdLotto(lt.getCodiceLotto());
         //lotto.setQtaAttesaEvasione(getQtaAttesaEvasione());
         //lotto.setQtaPropostaEvasione(getQtaPropostaEvasione());
         //Fix 39531 Inizio
         if(pal.isGenAutomaticaLottiUnitari())
         {
	         lotto.getQuantitaOrdinata().setQuantitaInUMRif(qtaUnitario.getQuantitaInUMRif());
	         lotto.getQuantitaOrdinata().setQuantitaInUMPrm(qtaUnitario.getQuantitaInUMPrm());
	         lotto.getQuantitaOrdinata().setQuantitaInUMSec(qtaUnitario.getQuantitaInUMSec());
         }
         else
         {//Fix 39531 Fine
	         lotto.getQuantitaOrdinata().setQuantitaInUMRif(getQuantitaOrdinata().getQuantitaInUMRif());
	         lotto.getQuantitaOrdinata().setQuantitaInUMPrm(getQuantitaOrdinata().getQuantitaInUMPrm());
	         lotto.getQuantitaOrdinata().setQuantitaInUMSec(getQuantitaOrdinata().getQuantitaInUMSec());
         }//Fix 39531
         getRigheLotto().add(lotto);
       }
     }
   }
//MG FIX 4656 fine

   //Inizio 5601
   public void gestioneContratti(){
      OrdineVendita testata = (OrdineVendita)getTestata();
      String keyOrdine = testata.getKey();
      ContrattoVendita con = testata.getContrattoVendita();
      if (con != null && testata.isAContratto()){
         con.completaRigaOrdine(true, TimeUtils.getCurrentDate(),testata.getDataConsegnaRichiesta(),testata.getDataConsegnaConfermata(),new BigDecimal(0),true,this,this.getIdCommessa(),null,null,null,null);
      }
   }
   // Fine 5601

   // Inizio 5601
   public boolean isRigaAContratto(){
    if(getCausaleRiga() != null)//Fix 18603
     return ((CausaleRigaOrdVen)getCausaleRiga()).getOrdineContratto();
   return false;//Fix 18603
   }

   /**
    * Il booleano passato come parametro indica se sono in salvataggio
    * (true) oppure in cancellazione (false)
    */
   // Inizio 8003 : la quantità ordinata del contratto non deve essere aggiornata se la riga
   // è ANNULLATA
   protected int aggiornaContrattoOrdinato(boolean isActionSave)throws SQLException{
     // Inizio 8003
     int saveContratto = 0;
     // Fine 8003
     String key =  getTestataKey();
     ContrattoVendita con = ContrattoVendita.elementWithKey(key,PersistentObject.OPTIMISTIC_LOCK);
     QuantitaInUMRif zeroRifQta = new QuantitaInUMRif();
     zeroRifQta.azzera();
     boolean aggiornaQtaOrdContratto = true;
     boolean isUpdate = (this.isOnDB() && this.getOldRiga()!=null);
     char stato = getDatiComuniEstesi().getStato();
     if (con!=null && stato != DatiComuniEstesi.INCOMPLETO){
       QuantitaInUMRif qtaOrd = new QuantitaInUMRif();
       if (isActionSave){
         //qtaOrd =  agggiornaUMRif(this.getQuantitaOrdinata(), this, con);
         try{
           qtaOrd.setEqual(agggiornaUMRif(this.getQuantitaOrdinata(), this, con));
        }
        catch (CopyException ex) {
           ex.printStackTrace(Trace.excStream);
         }
       }
       // Sono in UPDATE
       if (isUpdate){
         char oldStato = getOldRiga().getDatiComuniEstesi().getStato();
         //QuantitaInUMRif qtaOrdOld = agggiornaUMRif(this.getOldRiga().getQuantitaOrdinata(), this.getOldRiga(), con);
         QuantitaInUMRif qtaOrdOld = new QuantitaInUMRif();
         try{
           qtaOrdOld.setEqual(agggiornaUMRif(this.getOldRiga().getQuantitaOrdinata(), this.getOldRiga(), con));
        }
        catch (CopyException ex) {
           ex.printStackTrace(Trace.excStream);
         }
         // Lo stato della riga ord non è cambiato e la riga non è ANNULLATA
         if (oldStato == stato && stato != DatiComuniEstesi.ANNULLATO){
           if (isActionSave)
             qtaOrd = qtaOrd.subtract(qtaOrdOld);
           else
             qtaOrd = qtaOrdOld.negateRif();
         }
         else{
           //Passaggio da Sospeso/Valido ad Annullato quindi dalla qta ordinata del contratto
           // deve essere decurtata la qta della riga old.
           // V - Valido, A - Annullato, S - Sospeso
           //V-A , S-A
           if (oldStato != DatiComuniEstesi.ANNULLATO && oldStato != DatiComuniEstesi.INCOMPLETO && stato == DatiComuniEstesi.ANNULLATO){
             qtaOrd = qtaOrdOld.negateRif();
           }
           // V-S , S-V
           else if (oldStato != DatiComuniEstesi.ANNULLATO && oldStato != DatiComuniEstesi.INCOMPLETO && stato != DatiComuniEstesi.ANNULLATO){
             qtaOrd = qtaOrd.subtract(qtaOrdOld);
           }
           // I - A
           else if (oldStato == DatiComuniEstesi.INCOMPLETO && stato == DatiComuniEstesi.ANNULLATO){
             aggiornaQtaOrdContratto = false;
           }
           // Inizio 8127 A - A
           else if (oldStato == DatiComuniEstesi.ANNULLATO &&  stato == oldStato){
             aggiornaQtaOrdContratto = false;
           }
           // Fine 8127
         }
       }
       // Sono in NUOVO: la qta ordinata del contratto deve essere aggiornata
       // solo se la riga ordine aperta è VALIDA o SOSPESA
       else
         aggiornaQtaOrdContratto = (stato != DatiComuniEstesi.INCOMPLETO);

       if (qtaOrd.compareTo(zeroRifQta) != 0 && aggiornaQtaOrdContratto){
         // Fix 18798 inizio
      	 //saveContratto = con.aggiornaQtaDaOrdine(qtaOrd,zeroRifQta,zeroRifQta,zeroRifQta,zeroRifQta);
      	 saveContratto = this.aggiornaContratto(con,qtaOrd, zeroRifQta, zeroRifQta, zeroRifQta, zeroRifQta);
      	 // Fix 18798 fine
       }

       return saveContratto;
      }
     return 0;
   }
   // Fine 8003

 //protected QuantitaInUMRif agggiornaUMRif(QuantitaInUMRif qtaUtile, DocumentoOrdineRiga docRiga, Contratto con)throws SQLException{ // Fix 40936
   protected QuantitaInUMRif agggiornaUMRif(QuantitaInUMRif qtaUtl, DocumentoOrdineRiga docRiga, Contratto con)throws SQLException{  //Fix 40936
     BigDecimal zero = new BigDecimal(0);
     //int scala = this.getQtaInUMPrmMag().scale();//Fix 39402
     int scala = Q6Calc.get().scale(2);//Fix 39402
     //zero = zero.setScale(scala, BigDecimal.ROUND_HALF_UP);//Fix 30871
	 zero = Q6Calc.get().setScale(zero,scala, BigDecimal.ROUND_HALF_UP);//Fix 30871
	// Fix 40936 inizio
	 QuantitaInUMRif qtaUtile = new QuantitaInUMRif();
	 qtaUtile.azzera();
	 qtaUtile = qtaUtile.add(qtaUtl);
	 //Fix 40936 fine
     BigDecimal qtaUtileRif = qtaUtile.getQuantitaInUMRif();
     BigDecimal qtaUtileRifNew = zero;
     if(!(docRiga.getIdUMRif().equals(con.getIdUMRif()))){
       // Faccio questo tipo di controllo perchè è bene considerare anche la possibilità
       // che l'unità di misura che ho modificato può essere uguale ad una delle unità
       // di misura di magazzino
       if (con.getIdUMRif().equals(this.getIdUMPrm())){
         qtaUtileRifNew = qtaUtile.getQuantitaInUMPrm();
         qtaUtile.setQuantitaInUMRif(qtaUtileRifNew);
       }
       else if (this.getIdUMSec()!=null && con.getIdUMRif().equals(this.getIdUMSec())){
         qtaUtileRifNew = qtaUtile.getQuantitaInUMSec();
         qtaUtile.setQuantitaInUMRif(qtaUtileRifNew);
       }
       else {
         UnitaMisura uni = docRiga.getUMRif();
         if (uni==null){
           uni = UnitaMisura.elementWithKey(KeyHelper.buildObjectKey(new String[]{this.getIdAzienda(),docRiga.getIdUMRif()}),PersistentObject.NO_LOCK);
         }
         qtaUtileRifNew = this.getArticolo().convertiUM(qtaUtileRif,uni,con.getUMRif(), getArticoloVersRichiesta()); // fix 10955
         //qtaUtileRifNew = qtaUtileRifNew.setScale(scala,BigDecimal.ROUND_HALF_UP);//Fix 30871
         qtaUtileRifNew = Q6Calc.get().setScale(qtaUtileRifNew,scala,BigDecimal.ROUND_HALF_UP);//Fix 30871
		 qtaUtile.setQuantitaInUMRif(qtaUtileRifNew);
       }
     }
     return qtaUtile;
   }

   protected int aggiornaContratto(DocumentoDocRiga docRiga, char tipoAzione)throws SQLException{
     String key = this.getTestata().getKey();
     BigDecimal zero = new BigDecimal(0);
     QuantitaInUMRif zeroRifQta = new QuantitaInUMRif();
     zeroRifQta.azzera();
     //int scala = this.getQuantitaOrdinata().getQuantitaInUMPrm().scale();//Fix 39402
     int scala = Q6Calc.get().scale(2);//Fix 39402
     //zero = zero.setScale(scala);//30871
	 zero = zero.setScale(Q6Calc.get().scale(scala));//30871
     ContrattoVendita con = ContrattoVendita.elementWithKey(key,PersistentObject.OPTIMISTIC_LOCK);
     char statoAv = docRiga.getStatoAvanzamento();
     boolean isReso = docRiga.isRigaReso();
     char statoAvOld = OrdineRiga.NUOVA_RIGA_DOC;
     // Inizio 8003
     char statoOld = '-';
     // Fine 8003
     if (docRiga.getOldRiga()!=null){
       statoAvOld = docRiga.getOldRiga().getStatoAvanzamento();
       statoOld = docRiga.getOldRiga().getDatiComuniEstesi().getStato();
     }
     QuantitaInUMRif qtaUtile = new QuantitaInUMRif();
     qtaUtile.azzera();
     if (con!=null){
       switch (tipoAzione){
         case OrdineRiga.CONVALIDA:
           qtaUtile =  agggiornaUMRif(((DocumentoVenditaRiga)docRiga).getQtaSpedita(), docRiga, con);
           if (!isReso){
             return con.aggiornaQtaDaOrdine(zeroRifQta,zeroRifQta,qtaUtile.negateRif(),qtaUtile,zeroRifQta);
           }
           else {
             return con.aggiornaQtaDaOrdine(zeroRifQta,zeroRifQta,qtaUtile.negateRif(),zeroRifQta, qtaUtile);
           }
         case OrdineRiga.REGRESSIONE:
           qtaUtile =  agggiornaUMRif(docRiga.getQtaAttesaEvasione(), docRiga, con);
           if (!isReso){
             return con.aggiornaQtaDaOrdine(zeroRifQta,zeroRifQta,qtaUtile,qtaUtile.negateRif(),zeroRifQta);
           }
           else {
             return con.aggiornaQtaDaOrdine(zeroRifQta,zeroRifQta,qtaUtile,zeroRifQta, qtaUtile.negateRif());
           }
         case OrdineRiga.MANUTENZIONE:
           QuantitaInUMRif  qtaP = new QuantitaInUMRif();
           qtaP.azzera();
           QuantitaInUMRif  qtaA = new QuantitaInUMRif();
           qtaA.azzera();
           if (statoAvOld == StatoAvanzamento.PROVVISORIO && statoOld != DatiComuniEstesi.ANNULLATO){
             qtaP =  agggiornaUMRif(docRiga.getOldRiga().getQtaPropostaEvasione(), docRiga.getOldRiga(), con);
             qtaP = qtaP.negateRif();
           }
           else if (statoAvOld == StatoAvanzamento.DEFINITIVO && statoOld != DatiComuniEstesi.ANNULLATO){
             qtaA =  agggiornaUMRif(docRiga.getOldRiga().getQtaAttesaEvasione(), docRiga.getOldRiga(), con);
             qtaA = qtaA.negateRif();
           }

           if (statoAv == StatoAvanzamento.PROVVISORIO){
             qtaUtile =  agggiornaUMRif(docRiga.getQtaPropostaEvasione(), docRiga, con);
             qtaP = qtaP.add(qtaUtile);
           }
           else if (statoAv == StatoAvanzamento.DEFINITIVO){
             qtaUtile =  agggiornaUMRif(docRiga.getQtaAttesaEvasione(), docRiga, con);
             qtaA = qtaA.add(qtaUtile);
           }
           return con.aggiornaQtaDaOrdine(zeroRifQta,qtaP,qtaA,zeroRifQta,zeroRifQta);

         case OrdineRiga.ELIMINAZIONE:
           if (statoAvOld == StatoAvanzamento.DEFINITIVO){
             qtaA =  agggiornaUMRif(docRiga.getOldRiga().getQtaAttesaEvasione(), docRiga.getOldRiga(), con);
             qtaA = qtaA.negateRif();
             return con.aggiornaQtaDaOrdine(zeroRifQta,zeroRifQta,qtaA,zeroRifQta,zeroRifQta);
           }
           if (statoAvOld == StatoAvanzamento.PROVVISORIO){
             qtaP =  agggiornaUMRif(docRiga.getOldRiga().getQtaPropostaEvasione(), docRiga.getOldRiga(), con);
             qtaP = qtaP.negateRif();
             return con.aggiornaQtaDaOrdine(zeroRifQta,qtaP,zeroRifQta,zeroRifQta,zeroRifQta);
           }
       }
     }
     return 0;
   }
   // Fine 5601


   //Fix 5634 - inizio
   /**
    * Ridefinizione
    */
   public void annullaOldRiga() {
      super.annullaOldRiga();
        List righeSec = getRigheSecondarie();
       Iterator iter = righeSec.iterator();
       while (iter.hasNext()) {
          DocumentoOrdineRiga rigaSec = (DocumentoOrdineRiga)iter.next();
          rigaSec.annullaOldRiga();
       }
   }
   //Fix 5634 - fine

   // Inizio 5772
   // Ridefinizione del metodo: se provengo dalla save di un contratto
   // non devo chiamare l'aggiornamento del contrastto e la conseguente save.
   protected boolean isAggiornaQtaOrdSuContratto(){
     OrdineVendita testata = (OrdineVendita) getTestata();
     if (testata.getActionSaveContratto())
        iAggiornaQtaOrdSuContratto = false;
      return iAggiornaQtaOrdSuContratto;
   }
   // Fine 5772

   // fix 5990
   protected void stornaImportiImpostaRiga(OrdineVendita testata, RigaVendita oldRiga) {
     //Valore ordinato
     BigDecimal valoreOrdinato =
        getNotNullValue(testata.getValoreImposta()).
          subtract(
             getNotNullValue(oldRiga.getValoreImposta())
          );
     testata.setValoreImposta(valoreOrdinato);
     //Valore in spedizione
     BigDecimal valoreInSpedizione =
        getNotNullValue(testata.getValoreImpostaInSped()).
          subtract(
             getNotNullValue(oldRiga.getValoreImpostaInSpedizione())
          );
     testata.setValoreImpostaInSped(valoreInSpedizione);
     //Valore consegnato
     BigDecimal valoreConsegnato =
        getNotNullValue(testata.getValoreImpostaCons()).
          subtract(
             getNotNullValue(oldRiga.getValoreImpostaConsegnato())
          );
     testata.setValoreImpostaCons(valoreConsegnato);

   }

   public void aggiornaValoriNuovi(){
     if (this.getOldRiga()!=null){
       this.stornaImportiRigaSpesa( (OrdineVendita) getTestata(),
                                   (RigaVendita)this.getOldRiga());
       stornaImportiImpostaRiga((OrdineVendita) getTestata(),
                                   (RigaVendita)this.getOldRiga());
     }
   }
   // fine fix 5990

   // Inizio 6016
   public void eliminaPianoConsegnaCollegato(){
     String where = PianoConsegneRigaConsegnaTM.ID_AZIENDA +"='"+getIdAzienda()+"' AND "+
     PianoConsegneRigaConsegnaTM.ID_ANNO_ORD+"='"+getAnnoDocumento()+"' AND "+
     PianoConsegneRigaConsegnaTM.ID_NUMERO_ORD+"='"+getNumeroDocumento()+"' AND "+
     PianoConsegneRigaConsegnaTM.ID_RIGA_ORD+"="+ getNumeroRigaDocumento();
     List listaConsegne = new ArrayList();
     try{
       listaConsegne = PianoConsegneRigaConsegna.retrieveList(where,"",false);
       if (listaConsegne.size()>0){
         PianoConsegneRigaConsegna cons = (PianoConsegneRigaConsegna)listaConsegne.get(0);
         cons.setDeleteFromOrdine(true);
         cons.delete();
       }
    }
    catch (Exception ex) {
       ex.printStackTrace(Trace.excStream);
     }
   }
   // Fine 6016


// Fix 6150 PM Inizio
  public void impostazioniPerCopiaRiga() {
        super.impostazioniPerCopiaRiga();
//MG FIX 6265 inizio
/*
        if (!Utils.areEqual(getIdOldMagazzino(), getIdMagazzino()))
        {
            Iterator i = getRigheSecondarie().iterator();
            while(i.hasNext())
            {
               OrdineVenditaRiga rigaSec = (OrdineVenditaRiga)i.next();
               rigaSec.setIdMagazzino(getIdMagazzino());
               rigaSec.impostazioniPerCopiaRiga();
            }
        }
*/
      Iterator i = getRigheSecondarie().iterator();
    while (i.hasNext()) {
         OrdineVenditaRiga rigaSec = (OrdineVenditaRiga)i.next();
         rigaSec.impostazioniPerCopiaRiga();
         if (!Utils.areEqual(getIdOldMagazzino(), getIdMagazzino())) {
           rigaSec.setIdMagazzino(getIdMagazzino());
         }
	 if(getTestata() != null && !((DocumentoOrdineTestata)getTestata()).isInCopia())//Fix 41868
		 calcolaDateRigheSecondarie(rigaSec);//Fix 41868
      }
  }
//MG FIX 6265 fine

// Fix 6150 PM Fine
	//Fix Inizio 41868
    protected void calcolaDateRigheSecondarie(OrdineVenditaRiga rigaSec) {

    	if (datiUguali(getOldDataConsegnaRichiesta(), getDataConsegnaRichiesta()) &&
    		datiUguali(getOldDataConsegnaConfermata(), getDataConsegnaConfermata()) &&
    		datiUguali(getOldDataConsegnaProduzione(),getDataConsegnaProduzione())) 
    			return;
   
          rigaSec.setDataConsegnaRichiesta(getDataConsegnaRichiesta());
          rigaSec.setDataConsegnaConfermata(getDataConsegnaConfermata());
          rigaSec.setDataConsegnaProduzione(getDataConsegnaProduzione());
          rigaSec.setSettConsegnaRichiesta(getSettConsegnaRichiesta());
          rigaSec.setSettConsegnaConfermata(getSettConsegnaConfermata());
          rigaSec.setSettConsegnaProduzione(getSettConsegnaProduzione());
            
    }
  //Fix Fine 41868
    

    // Inizio 6323
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
          // Nel caso di ordini deve essere reperita la disponibilità
          // Inizio 6428
          //giacCalcLotto = ProposizioneAutLotto.calcolaQtaDisponibileLotto(tipo, lottoSaldo, !lottiOrdine.isEmpty(), lottiOrdine);
          // Inizio 6965
          giacCalcLotto = ProposizioneAutLotto.getInstance().calcolaQtaDisponibileLottoOrdine(tipo, lottoSaldo, !lottiOrdine.isEmpty(), lottiOrdine);
          // Fine 6965
          // Fine 6428

          String keyRigaOrdLotto = KeyHelper.buildObjectKey(new String[]{getIdAzienda(),
            getAnnoDocumento(),getNumeroDocumento(), getNumeroRigaDocumento().toString(), getIdArticolo(), rigaLotto.getIdLotto()});
          BigDecimal qtaRigaLottoPrmOld = getOrdineVenRigaLottoPrm(keyRigaOrdLotto);
          if (qtaRigaLottoPrmOld != null)
            giacCalcLotto = giacCalcLotto.add(qtaRigaLottoPrmOld);
        }
      }
      catch (Exception ex) {
        ex.printStackTrace(Trace.excStream);
      }
      return giacCalcLotto;
    }

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
    // Fine 6323

    public BigDecimal getOrdineVenRigaLottoPrm(String key){
      BigDecimal qtaRigaLottoPrm = null;
      try{
        OrdineVenditaRigaLottoPrm rigaOrdLotto = (OrdineVenditaRigaLottoPrm)Factory.createObject(OrdineVenditaRigaLottoPrm.class);
        rigaOrdLotto.setKey(key);
        if (rigaOrdLotto.retrieve()){
            qtaRigaLottoPrm = rigaOrdLotto.getQuantitaOrdinata().getQuantitaInUMPrm();
        }
      }
      catch (Exception ex) {
        ex.printStackTrace(Trace.excStream);
      }
      return qtaRigaLottoPrm;
    }

    // Inizio 6920
    /* Fix 12673 Inizio
    public DocumentoOrdineRigaLotto getUnicoLottoEffettivo(){ // Fix 6920
        OrdineRigaLotto rigalotto = null;
        ArrayList listaLotti = (ArrayList)getRigheLotto();
        if (listaLotti.size() == 1){
          OrdineRigaLotto rigalottoTmp  = (OrdineRigaLotto)listaLotti.get(0);
          if (rigalottoTmp.getIdLotto() != null && !rigalottoTmp.getIdLotto().equals(Lotto.LOTTO_DUMMY))
            rigalotto = rigalottoTmp;
        }
        return rigalotto;
      }
  Fix 12673 Fine*/
    // Fine 6920

    // Inizio 6983
    public boolean gestioneSaldoRiaperturaManuale(ContrattoMateriaPrima con){
      boolean contrattoDaAggiornare = false;
      // Inizio 9754 : l'aggiornamento del contratto deve essere eseguito solo quando la
      // riga è stata saldata manualmente.
      boolean oldSaldoManuale = (getOldRiga() != null && getOldRiga().isSaldoManuale());
      boolean isSaldataManualmente = (!oldSaldoManuale && isSaldoManuale());
      //Fix 32713 inizio      
  	  char statoOldRiga = DatiComuniEstesi.VALIDO ;
  			 if (getOldRiga() != null)
  				statoOldRiga = getOldRiga().getDatiComuniEstesi().getStato();
  	  //Fix 32713 Fine
      //if (isSaldoManuale() || getAzioneManuale() == AZIONE_RIAPRI){
      if (isSaldataManualmente || getAzioneManuale() == AZIONE_RIAPRI){
      	// Fine 9754
        if (con != null){
          contrattoDaAggiornare = true;
          BigDecimal qtaResidua = getQuantitaResiduoConSegno().getQuantitaInUMRif();
          if (isSaldoManuale())
          {//Fix 32713
              if (statoOldRiga != DatiComuniEstesi.ANNULLATO)//Fix 32713
            	  con.aggiornaQtaOrd(qtaResidua,false);
          }//Fix 32713	             
          else if (getAzioneManuale() == AZIONE_RIAPRI)
          {//Fix 32713
        	  if ( getStato() != DatiComuniEstesi.ANNULLATO)//Fix 32713
        		  con.aggiornaQtaOrd(qtaResidua,true);
          }//Fix 32713
        }
      }
      return contrattoDaAggiornare;
    }
    // Fine 6983

    // fix 7098
  protected void impostaSaldoManualeRigheSec() {
    Iterator righeSecondarie = iRigheSecondarie.iterator();
    while (righeSecondarie.hasNext()) {
      OrdineVenditaRigaSec rigaSec = (OrdineVenditaRigaSec) righeSecondarie.next();
      rigaSec.setSaldoManuale(true);
    }
  }
    // fine fix 7098
  // Inizio 8508
  public void aggiornaRigaPianoConsegnaCollegata(){
    String where = PianoConsegneRigaConsegnaTM.ID_AZIENDA +"='"+getIdAzienda()+"' AND "+
    PianoConsegneRigaConsegnaTM.ID_ANNO_ORD+"='"+getAnnoDocumento()+"' AND "+
    PianoConsegneRigaConsegnaTM.ID_NUMERO_ORD+"='"+getNumeroDocumento()+"' AND "+
    PianoConsegneRigaConsegnaTM.ID_RIGA_ORD+"="+ getNumeroRigaDocumento();
    List listaConsegne = new ArrayList();
    try{
      listaConsegne = PianoConsegneRigaConsegna.retrieveList(where,"",false);
      if (listaConsegne.size()>0){
        PianoConsegneRigaConsegna cons = (PianoConsegneRigaConsegna)listaConsegne.get(0);
        if (isRigaPDCDaAggiornare(cons)){
          cons.setAbilitaGesRigaOrdCollegata(false);
          cons.setQtaInUMVen(getQuantitaOrdinata().getQuantitaInUMRif());
          cons.setQtaInUMPrm(getQuantitaOrdinata().getQuantitaInUMPrm());
          cons.setQtaInUMSec(getQuantitaOrdinata().getQuantitaInUMSec());
          cons.getDatiComuniEstesi().setStato(getDatiComuniEstesi().getStato());
          cons.setDataConsegRcs(getDataConsegnaRichiesta());
          cons.setDataConsEdi(getDataConsegnaRichiesta());
          cons.setDataConsegCfm(getDataConsegnaConfermata());
          if (getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO)
            cons.setStatoCnfPrv(PianoConsegneRigaConsegna.STATO_CNFPRV_CONFERMATO);
          else
            cons.setStatoCnfPrv(PianoConsegneRigaConsegna.STATO_CNFPRV_PREV_1);
          int ret = cons.save();
        }
      }
    }
    catch(Exception ex) {
      ex.printStackTrace(Trace.excStream);
    }
  }

  public boolean isRigaPDCDaAggiornare(PianoConsegneRigaConsegna cons){
    boolean ok = false;

    char statoRigaOrd = getDatiComuniEstesi().getStato();
    char statoConsegna = cons.getDatiComuniEstesi().getStato();

    char statoAv = getStatoAvanzamento();
    char statoConfPrev = cons.getStatoCnfPrv();

    BigDecimal qtaOrdVen = getQuantitaOrdinata().getQuantitaInUMRif();
    BigDecimal qtaPDCVen = cons.getQtaInUMVen();
    BigDecimal qtaOrdPrm = getQuantitaOrdinata().getQuantitaInUMPrm();
    BigDecimal qtaPDCPrm = cons.getQtaInUMPrm();
    BigDecimal qtaOrdSec = getQuantitaOrdinata().getQuantitaInUMSec();
    BigDecimal qtaPDCSec = cons.getQtaInUMSec();
    BigDecimal qtaResOrdVen = getQuantitaResiduo().getQuantitaInUMRif();
    BigDecimal qtaResOrdPrm = getQuantitaResiduo().getQuantitaInUMPrm();
    BigDecimal qtaResOrdSec = getQuantitaResiduo().getQuantitaInUMSec();
    BigDecimal qtaResPDCVen = cons.getQtaResiduaVen();
    BigDecimal qtaResPDCPrm = cons.getQtaResiduaPrm();
    BigDecimal qtaResPDCSec = cons.getQtaResiduaSec();

    java.sql.Date dataOrdConsRich = getDataConsegnaRichiesta();
    java.sql.Date dataPDCConsRich = cons.getDataConsegRcs();
    java.sql.Date dataOrdConsConf = getDataConsegnaConfermata();
    java.sql.Date dataPDCConsConf = cons.getDataConsegCfm();


    // Verifica stato incompatibile
    ok = (statoRigaOrd != statoConsegna);
    if (!ok){
      // Verifica Stato avanzamento e Stato conf/prev incompatibili
      ok = (statoAv == StatoAvanzamento.DEFINITIVO && statoConfPrev != PianoConsegneRigaConsegna.STATO_CNFPRV_CONFERMATO) ||
      (statoAv == StatoAvanzamento.PROVVISORIO && statoConfPrev == PianoConsegneRigaConsegna.STATO_CNFPRV_CONFERMATO);
    }

    if (!ok){
      // Verifica se le qta sono variate
      ok = (qtaOrdVen.compareTo(qtaPDCVen) != 0 || qtaOrdPrm.compareTo(qtaPDCPrm) != 0 ||
          qtaOrdSec.compareTo(qtaPDCSec) != 0);
    }
    // Inizio 8815
    if (!ok){
      // Verifica data cons richiesta
      ok = (dataPDCConsRich != null && dataOrdConsRich!= null &&  TimeUtils.differenceInDays(dataOrdConsRich, dataPDCConsRich)!=0 ) ||
      	!(dataPDCConsRich != null && dataOrdConsRich!= null);
    }
    if (!ok){
      // Verifica data cons confermata
    	ok = (dataPDCConsConf != null && dataOrdConsConf != null && TimeUtils.differenceInDays(dataOrdConsConf, dataPDCConsConf)!= 0) ||
    		!(dataPDCConsConf != null && dataOrdConsConf != null);
    }
    // Fine 8815
    if (!ok){
      // Verifica qta residua
    	// Inizio 9577
      ok = (
      		(qtaResPDCVen != null && qtaResOrdVen.compareTo(qtaResPDCVen) != 0) ||
      		(qtaResPDCPrm != null && qtaResOrdPrm.compareTo(qtaResPDCPrm)!=0) ||
          (qtaResPDCSec != null && qtaResOrdSec.compareTo(qtaResPDCSec) != 0)
          );
      // Fine 9577
    }
    return ok;
  }

  public void setSaveFromPDC(boolean saveFromPDC){
    this.iSaveFromPDC = saveFromPDC;
  }
  public boolean getSaveFromPDC(){
    return iSaveFromPDC;
  }

  // Fine 8508

//MG FIX 8659 inizio
  protected void recuperaDatiCA(OrdineVenditaRigaSec rigaSec) {
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
  // Fix 8913 - Inizio
  public void CompletaDatiCA() {
    OrdineVendita testata = (OrdineVendita) getTestata();
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
        }
        catch (Exception ex) {
            ex.printStackTrace(Trace.excStream);
        }
    }
}
    // Fix 8913 - Fine

  // Fix 8920 ini
  public ErrorMessage checkStatoAnnullato(){
     ErrorMessage em = null;
     //if (isOnDB() && getOldRiga() != null && this.getDatiComuniEstesi().getStato()==DatiComuniEstesi.ANNULLATO) { //Fix 12610
     if (isOnDB() && getOldRiga() != null && getOldRiga().getDatiComuniEstesi().getStato() != DatiComuniEstesi.ANNULLATO && this.getDatiComuniEstesi().getStato()==DatiComuniEstesi.ANNULLATO) { //Fix 12610
         BigDecimal qtaZero = new BigDecimal(0.0);
         if (getQtaPropostaEvasione().getQuantitaInUMPrm().compareTo(qtaZero) != 0 ||
             getQtaPropostaEvasione().getQuantitaInUMSec().compareTo(qtaZero) != 0 ||
             getQtaAttesaEvasione().getQuantitaInUMPrm().compareTo(qtaZero) != 0 ||
             getQtaAttesaEvasione().getQuantitaInUMSec().compareTo(qtaZero) != 0 ||
             (this.getTipoBloccoLSP() == TipoBloccoLSP.IMPEGNATO) ||
             isSaldoManuale()||
             (this.getStatoEvasione()!= StatoEvasione.INEVASO))
             em = new ErrorMessage("THIP200446", KeyHelper.formatKeyString(this.getKey()));
     }
     return em;
  }
  // Fix 8920 fin

  // Fix 44166 ini
  public ErrorMessage checkStatoSospeso(){
     ErrorMessage em = null;
     if (isOnDB() && getOldRiga() != null && getOldRiga().getDatiComuniEstesi().getStato() != DatiComuniEstesi.SOSPESO && this.getDatiComuniEstesi().getStato()==DatiComuniEstesi.SOSPESO) { //Fix 12610
         BigDecimal qtaZero = new BigDecimal(0.0);
         if (getQtaPropostaEvasione().getQuantitaInUMPrm().compareTo(qtaZero) != 0 ||
             getQtaPropostaEvasione().getQuantitaInUMSec().compareTo(qtaZero) != 0 ||
             getQtaAttesaEvasione().getQuantitaInUMPrm().compareTo(qtaZero) != 0 ||
             getQtaAttesaEvasione().getQuantitaInUMSec().compareTo(qtaZero) != 0 ||
             (this.getTipoBloccoLSP() == TipoBloccoLSP.IMPEGNATO) /* 46088 ini||
              isSaldoManuale()||
             (this.getStatoEvasione()!= StatoEvasione.INEVASO) 46088 fine  */)
             em = new ErrorMessage("THIP200446", KeyHelper.formatKeyString(this.getKey()));
     }
     return em;
  }
  // Fix 44166 fin
  
  // Inizio 9588
  /**
   * Controlla se esiste una riga documento collegata alla riga ordine.
   *
   */
  public ErrorMessage checkEsistenzaRigaDocCollegata(){
  	ErrorMessage err = null;
  	if (existRigaDocCollegata())
  		err = new ErrorMessage("THIP300113");
  	return err;
  }

  /**
   * Controlla se esiste almento una riga documento collegata.
   * @return
   */
  public boolean existRigaDocCollegata(){
    //Fix 16440 inizio
    if (csRicDocVenRig == null) {
      String SQL_RIC_DOCVEN_RIG_1 =
        "SELECT COUNT(*) FROM " + DocumentoVenRigaPrmTM.TABLE_NAME +
        " WHERE " + DocumentoVenRigaPrmTM.ID_AZIENDA + "=? AND " +
        DocumentoVenRigaPrmTM.R_ANNO_ORD + "=? AND " +
        DocumentoVenRigaPrmTM.R_NUMERO_ORD + "=? AND " +
        DocumentoVenRigaPrmTM.R_RIGA_ORD + "=? AND " +
        DocumentoVenRigaPrmTM.R_DET_RIGA_ORD + "=?";
        csRicDocVenRig = new CachedStatement(SQL_RIC_DOCVEN_RIG_1);
    }
    //Fix 16440 fine
  	boolean exist = false;
  	Database db = ConnectionManager.getCurrentDatabase();
  	try{
  		PreparedStatement ps = csRicDocVenRig.getStatement();
  		synchronized (ps) {
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
    catch(SQLException ex) {
  		ex.printStackTrace(Trace.excStream);
      exist = false;//Fix 16440
  	}
  	return exist;
  }

  /**
   * Ridefinizione metodo checkDelete()
   * Se esiste almeno una riga documento collegata , la riga ordine non può
   * essere eliminata
   */
  public ErrorMessage checkDelete(){
  	ErrorMessage err = super.checkDelete();
  	if (err == null)
            err = checkEsistenzaRigaDocCollegata();
        // Fix 16024 inizio
        if(err == null && isAttivaGestioneIntercompany()){
          if(getOrdineAcquistoRigaPrmIC() != null){
            err = getOrdineAcquistoRigaPrmIC().checkDelete();
            if(err != null)
              err = rebuildErrorMessageForIC(err);
          }
        }
        // Fix 16024 fine
        //Fix 17374 inizio
        //if (isRigaAContratto() && !getSaveFromPDC() && isRigaPianoConsegnaCollegata())//Fix 19960
        if (isRigaAContratto() && !getSaveFromPDC() && isRigaPianoConsegnaRigaCollegata())//Fix 19960
            return new ErrorMessage("THIP30T264");
       //Fix 17374  fine
       //Fix 20569 Inizio
       if(err == null){
         err = checkRigaOrdVenRefCommessa();
       }
       //Fix 20569 Fine
  	return err;
  }
  // Fine 9588

  // Inizio 10476

  public void setCommentEDI(String commentEDI){
  	this.iCommentEDI = commentEDI;
  }

  public String getCommentEDI(){
  	return iCommentEDI;
  }

  public void setIdArticoloEDI(String idArticoloEDI){
  	this.iIdArticoloEDI = idArticoloEDI;
  }
  public String getIdArticoloEDI(){
  	return iIdArticoloEDI;
  }

  // Fine 10476


//Fix 10719 PM Inizio
	public void setDettRigaBozza(Integer dettRigaBozza) {
		setDirty();
	}

	public Integer getDettRigaBozza() {
		return new Integer(0);
	}
	//Fix 10719 PM Fine

	// Inizio 10776
  public ErrorMessage checkQtaDisponibileContrMatPrima(){
    ErrorMessage err = null;
    //if (getOldRiga() != null && isOnDB() && getDatiComuniEstesi().getStato() == DatiComuniEstesi.VALIDO){//Fix 20651
    if(getDatiComuniEstesi().getStato() == DatiComuniEstesi.VALIDO){//Fix 20651

      BigDecimal qtaResUmVen= getQtaDisponibileContrMatPrima();
    	if (qtaResUmVen != null && getQtaInUMRif().compareTo(qtaResUmVen) > 0 )
      	err = new ErrorMessage("THIP300196", new String[]{String.valueOf(qtaResUmVen)});
    }
  	return err;
  }

  public BigDecimal getQtaDisponibileContrMatPrima(){
  	BigDecimal qtaResUmVen = null;
  	DocOrdRigaPrezziExtra rigaPrezzi = this.getRigaPrezziExtra();
    if (rigaPrezzi!=null){
    	ContrattoVEMateriaPrima con = (ContrattoVEMateriaPrima)rigaPrezzi.getContrattoMateriaPrima();
    	if (con != null){
        BigDecimal prc = con.getPrcTolleranzaCnt();
        if(prc == null)
          prc = new BigDecimal(0);
        else
          prc = prc.divide(new BigDecimal(100), prc.scale(), BigDecimal.ROUND_HALF_UP);
        qtaResUmVen = con.getQtaResUMVen();
        qtaResUmVen = qtaResUmVen.add(qtaResUmVen.multiply(prc));
        if(getOldRiga()!=null)//Fix 20651 
      	 qtaResUmVen = qtaResUmVen.add(getOldRiga().getQtaInUMRif());

    	}
    }
    return qtaResUmVen;
  }

	// Fine 10776

  // fix 11123
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
        OrdineVenditaRigaSec rigaSec = (OrdineVenditaRigaSec) iter.next();
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
     ogg.setTipoControllo(ogg.TP_CTL_DISPONIBILITA);
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
     boolean controlla = true;
     if(this.isConfigurazioneNeutra(artSec , confSec)) {
    	 controlla = false ;
     }
     if(controlla) {
    //Fix 30193 Fine
     OggCalcoloGiaDisp ogg = (OggCalcoloGiaDisp) Factory.createObject(
         OggCalcoloGiaDisp.class);
     ogg.caricati(datiRigaKit, this, null);
     ogg.setTipoControllo(ogg.TP_CTL_DISPONIBILITA);
     lista.add(ogg);
     ritorno = true;
     }//Fix 30193
   }
   return ritorno;
 }

  // fine fix 11123


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

 }
 //Fix 37244 fine

 protected void copiaRigaImpostaProvvigioniAgenti(DocumentoOrdineRiga riga)
 {
	 try
	 {
		 OrdineVenditaRigaPrm rigaPrm = (OrdineVenditaRigaPrm)riga;
		 DocumentoOrdineRigaVenRecuperaDati recDati = new DocumentoOrdineRigaVenRecuperaDati();
		 recDati.setClassName("OrdineVenditaRigaPrm");
		 recDati.setArticolo(rigaPrm.getArticolo());
		 recDati.setIdAgente(rigaPrm.getIdAgente());
		 if (rigaPrm.getProvvigione1Agente() != null)
			 recDati.setProvv1Agente(rigaPrm.getProvvigione1Agente().toString());
		 recDati.setIdSubagente(rigaPrm.getIdSubagente());
		 if (rigaPrm.getProvvigione1Subagente() != null)
			 recDati.setProvv1Subagente(rigaPrm.getProvvigione1Subagente().toString());
		 recDati.setIntestatario(rigaPrm.getIdCliente());
		 recDati.setDivisione(((OrdineVendita)rigaPrm.getTestata()).getIdDivisione());
		 recDati.impostaProvvigioniAgente();
		 rigaPrm.setProvvigione1Agente(recDati.getAgentiProvvigioni().getProvvigioniAgente());
		 rigaPrm.setProvvigione1Subagente(recDati.getAgentiProvvigioni().getProvvigioniSubagente());
		 rigaPrm.setIdAgente(recDati.getAgentiProvvigioni().getIdAgente());
		 rigaPrm.setIdSubagente(recDati.getAgentiProvvigioni().getIdSubagente());
	 }
	 catch(Exception e)
	 {
		 e.printStackTrace(Trace.excStream);
	 }
 }

 //Fix 11170 PM Fine

 // fix 11124 >
 public void forzaRicalcoloDatiVendita() throws SQLException {
	 OrdineVenditaPO testata = (OrdineVenditaPO)this.getTestata();
	 boolean old = this.isServizioCalcDatiVendita();
	 setServizioCalcDatiVendita(true);
	 this.calcolaDatiVendita(testata);
	 setServizioCalcDatiVendita(old);
 }
 // fix 11124 <

 // Inizio 11707
 // Inizio 11844
 /**
  * Gestione della generazione delle righe secondarie
  */
 public void runGenerazioneRigheSec() throws SQLException{
   //boolean newRow = !isOnDB(); //...FIX 16893
   boolean newRow = righeSecDaGenerare(); //...FIX 16893
   //Verifica se deve generare le righe secondarie di kit
   if (newRow && getTipoRiga() != TipoRiga.SPESE_MOV_VALORE &&
       (getArticolo().getTipoParte() == ArticoloDatiIdent.KIT_GEST
        ||
        getArticolo().getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST)
       ) {
     //Fix 3230 - inizio
     if (isGeneraRigheSecondarie() && !isDisabilitaRigheSecondarieForCM()) {  //...FIX04607 - DZ
       gestioneKit();
       //Fix 4060 - inizio
       calcolaPrezzoDaRigheSecondarie();
       //Fix 4060 - fine
     }
     //Fix 3230 - fine
   }
  //else {//Fix 33762
     //PM Fix 1988 Inizio
     //if (isOnDB()) {//Fix 33762 
		
       //PM Fix 1988 Fine
       gestioneDateRigheSecondarie();
     //}
  // }
 }
 // Fine 11844

 // Fine 11707

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

 //Fix 12585 PM >
 protected boolean isCreazioneAutomaticaLottiAbilitata()
 {
     return !isOnDB() && !(this.getTestata()!=null
           && ((DocumentoOrdineTestata)this.getTestata()).isInCopia())
           && ((getOffertaClienteRiga() != null && getRigheLotto().isEmpty()) || getOffertaClienteRiga() == null);
 }
 //Fix 12584 PM <


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
            pesiEVolume = Articolo.getPesiEVolumeTotali(getArticolo(),
                  getQtaInUMRif(), getQtaInUMPrmMag(), getQtaInUMSecMag(),
                  getUMRif(), getUMPrm(), getUMSec());
         }
         else
         {
            //Nel caso di kit non gestiti a magazzino pesi e volume sono derivati dai componenti
            pesiEVolume = new BigDecimal[]{ZERO_DEC, ZERO_DEC, ZERO_DEC};
            Iterator righeSecIter = getRigheSecondarie().iterator();
            while(righeSecIter.hasNext())
            {
               OrdineVenditaRigaSec rigaSec = (OrdineVenditaRigaSec)righeSecIter.next();
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

         //System.out.println("ORD_PRM:calcolaPesiEVolume " + pesiEVolume[0]);
      }
   }

   public void calcolaPesiEVolumeRigheSec()
   {
      if(!isAttivoCalcoloPesiVolumi()) //Fix 13110
         return;

      //il metodo essendo chiamato in creazione non subisce il vincolo del flag di ricalcolo
      Iterator iterSec = getRigheSecondarie().iterator();
      while(iterSec.hasNext())
      {
         OrdineVenditaRigaSec rigaSec = (OrdineVenditaRigaSec)iterSec.next();
         rigaSec.calcolaPesiEVolume();
      }
   }

   public void aggiornaPesiEVolumeTestata(boolean rigaInCancellazione)
   {
      //Fix 14931 inizio
      CalcolatorePesiVolume calc = CalcolatorePesiVolume.getInstance();
      calc.aggiornaPesiVolumeTestataDaRiga((OrdineVendita)getTestata(), this, rigaInCancellazione);

      /*
      if(!isAttivoCalcoloPesiVolumi()) //Fix 13110
         return;

      //aggiornamento dei pesi e volume di testata per delta rispetto a precedente
      //attivato solo se la testata ha flag di ricalcolo acceso
      OrdineVendita testata = (OrdineVendita)getTestata();
      if(!testata.isRicalcolaPesiEVolume())
         return;

      BigDecimal pesoNettoTes = getNotNullValue(testata.getPesoNetto());
      BigDecimal pesoLordoTes = getNotNullValue(testata.getPesoLordo());
      BigDecimal volumeTes = getNotNullValue(testata.getVolume());
      boolean aggiornaTes = false;

      //controllo per eliminare valroe vecchio
      boolean oldRigaNonAnnullata = getOldRiga() != null && getOldRiga().getDatiComuniEstesi().getStato() != DatiComuniEstesi.ANNULLATO;
      boolean storno = isOnDB() && oldRigaNonAnnullata && (rigaInCancellazione || isQuantitaCambiata()  || isPesiVolumiCambiati());
      if(storno && getOldRiga().isPesiVolumeValorizzati())
      {
         RigaVendita oldRiga = (RigaVendita)getOldRiga();
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
         //System.out.println("ORD_AGGTES:aggiornaPesiEVolumeTestata " + pesiEVolume[0]);
      }
      */
      //Fix 14931 fine
   }
   //Fix 12508 fine

   // Fix 13494 inzio
   //Fix 27720: da protected a public
   public void calcolaImportiRiga(){
     calcolaPrezzoDaRigheSecondarieConReset(false);
     super.calcolaImportiRiga();
   }
   // Fix 13494 fine
   // Fix 16024 inizio
    public ErrorMessage rebuildErrorMessageForIC(ErrorMessage err){
      String text = null;

      if (!err.getId().equals(GeneratoreOrdiniDocumentiIC.ERR_INTERCOMPANY) && !err.getId().equals(GeneratoreOrdiniDocumentiIC.WARNING_INTERCOMPANY))
        text = GeneratoreOrdiniDocumentiIC.START_MESS_ERR + err.getQualifiedText();
      else
        text = err.getQualifiedText();

      if (err.getSeverity() == ErrorMessage.ERROR && !err.getForceable())
        return new ErrorMessage(GeneratoreOrdiniDocumentiIC.ERR_INTERCOMPANY, new String[]{text});
      else
        return new ErrorMessage(GeneratoreOrdiniDocumentiIC.WARNING_INTERCOMPANY, new String[]{text});

    }
    // Fix 16024 fine

    //Fix 16508 inizio
    //Forzare la recupera del offerta cliente riga in caso : non è da evasione e Cambia StatoEvasione a Definitivo
    // cioè il caso di esecuzione del metodo OrdineVenditaDataCollector.impostaSecondoCausaleRecursive
    public OffertaClienteRiga recuperaOffertaClienteRiga(){
      OrdineVendita ordVen=(OrdineVendita)getTestata();

      //In caso non è da evasione e Cambia StatoEvasione a Definitivo
      boolean forced =  !isDaEvasioneOfferta() && ordVen !=null && ordVen.getStatoAvanzamento()!= ordVen.getOldStatoAvanzamento()
          && ordVen.getStatoAvanzamento() == StatoAvanzamento.DEFINITIVO;

        return getOffertaClienteRiga(forced);
    }

    public OffertaClienteRiga getOffertaClienteRiga(boolean forced){
        return (OffertaClienteRiga)iOffertaClienteRiga.getObject(forced);
    }
    //Fix 16508 fine

    //Fix 16773 inizio
    public int afterSave(boolean newRow) throws SQLException {
      return 0;
    }
    //Fix 16773 fine
    //Fix 17374 inizio
    public  boolean isRigaPianoConsegnaCollegata(){
     String where =PianoConsegneRigaArticoloTM.ID_AZIENDA+"='"+getIdAzienda()+"' AND "+
                   PianoConsegneRigaArticoloTM.ID_ANNO_ORD+"='"+getAnnoDocumento()+"' AND "+
                   PianoConsegneRigaArticoloTM.ID_NUMERO_ORD+"='"+getNumeroDocumento()+"' AND "+
                   PianoConsegneRigaArticoloTM.R_ARTICOLO+"='"+getIdArticolo()+"' AND "+
                   PianoConsegneRigaArticoloTM.R_VERSIONE_SAL+"='"+getIdVersioneRcs()+"' AND "+
                   PianoConsegneRigaArticoloTM.STATO_RIGA_PNC+"='"+PianoConsegne.ST_PIANO_CORRENTE + "'";

    if  (getArticolo().isConfigurato() && getConfigurazione()!=null){
      where += " AND("+PianoConsegneRigaArticoloTM.R_CONFIGURAZIONE+"='"+getIdConfigurazione()+"' OR "+
                      PianoConsegneRigaArticoloTM.R_CONFIGURAZIONE+" IS NULL )" ;
    }
                    List listaConsegne = new ArrayList();
                    try{
                      listaConsegne = PianoConsegneRigaArticolo.retrieveList(where,"",false);
                         return !listaConsegne .isEmpty();
                     }
                     catch(Exception ex) {
                       ex.printStackTrace(Trace.excStream);
                     }
      return false;
   }

   protected ErrorMessage checkRigaPianoConsegnaCollegata() {
        if ((!isOnDB()) && isRigaAContratto()&& isRigaPianoConsegnaCollegata()){
           return new ErrorMessage("THIP30T265");
        }
      return null ;
    }
   //Fix 17374 fine
   //Fix 19960 inizio
   public boolean isRigaPianoConsegnaRigaCollegata(){
   	boolean exist = false;
   	Database db = ConnectionManager.getCurrentDatabase();
   	try{
   		PreparedStatement ps = cSelectRigaPianoConsegneRiga.getStatement();
   		synchronized (ps) {
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
     catch(SQLException ex) {
   		ex.printStackTrace(Trace.excStream);
       exist = false;
   	}
   	return exist;
   }
   //Fix 19960 fine
   //...FIX 16893 inizio
   public boolean righeSecDaGenerare() {
     return!isOnDB();
   }
   //...FIX 16893 fine
   // Fix 17697 inizio
   public void impostaRilascioOrdineProdSecondarie(){
     if ((isOnDB()) &&
         (getArticolo().getTipoParte() == ArticoloDatiIdent.KIT_NON_GEST && getOldRiga() != null &&
          getRilascioOrdineProd() != ((RigaVendita) getOldRiga()).getRilascioOrdineProd())) {
       Iterator iter = getRigheSecondarie().iterator();
       while (iter.hasNext()) {
         OrdineVenditaRigaSec ordRigaSec = (OrdineVenditaRigaSec) iter.next();
         ordRigaSec.setRilascioOrdineProd(getRilascioOrdineProd());
       }
     }
   }
   // Fix 17697 fine

   // Fix 18798 inizio
   public int aggiornaContratto(ContrattoVendita con, QuantitaInUMRif qtaOrdinata, QuantitaInUMRif qtaProposta, QuantitaInUMRif qtaAttesa, QuantitaInUMRif qtaSpedita, QuantitaInUMRif qtaResa)throws SQLException{
     return con.aggiornaQtaDaOrdine(qtaOrdinata, qtaProposta, qtaAttesa, qtaSpedita, qtaResa);
   }
   // Fix 18798 fine

   //Fix 18703 inizio
   protected void calcolaImportiPercRiga(){
     setValoreOrdinato(new BigDecimal(0));
     setValoreInSpedizione(new BigDecimal(0));
     setValoreConsegnato(new BigDecimal(0));
     setCostoOrdinato(new BigDecimal(0));
     setCostoInSpedizione(new BigDecimal(0));
     setCostoConsegnato(new BigDecimal(0));
     setValoreImposta(new BigDecimal(0));
     setValoreImpostaInSpedizione(new BigDecimal(0));
     setValoreImpostaConsegnato(new BigDecimal(0));
     setValoreTotaleRiga(new BigDecimal(0));
     setValoreInSpedTotaleRiga(new BigDecimal(0));
     setValoreConsTotaleRiga(new BigDecimal(0));
     setPrezzoNetto(new BigDecimal(0));
   }
   //Fix 18703 fine
   //Fix 20387 inizio
   public ErrorMessage checkIdEsternoConfigInCopia(){
     //if (!isOnDB() && !getRigheSecondarie().isEmpty()) { // Fix 23709
     if (!getRigheSecondarie().isEmpty()) { // Fix 23709
       if (  !equalsObject(getIdEsternoConfig(),iOldIdEsternoConfig))
         return new ErrorMessage("THIP40T339");
     }
     return null;
   }
   //Fix 20387 fine

   //Fix 20569 Inizio
   public ErrorMessage checkRigaOrdVenRefCommessa(){
     Commessa commessaRif = isRigaOrdVenRefCommessa();
     if (commessaRif != null && !isErrorForzabileClear()) {
       String[] parameters = new String[] {commessaRif.getIdCommessa(), commessaRif.getDescrizione().getDescrizioneRidotta()};
       return new ErrorMessage("THIP40T375", true, parameters);
     }
     return null;
   }

   public Commessa isRigaOrdVenRefCommessa(){
     if (getIdAzienda() == null || getAnnoDocumento() == null ||
         getNumeroDocumento() == null || getNumeroRigaDocumento() == null)
       return null;
     try{
       String where = CommessaTM.ID_AZIENDA    + "='" + getIdAzienda()+"' AND "+
                      CommessaTM.R_ANNO_ORDINE + "='" + getAnnoDocumento()+"' AND "+
                      CommessaTM.R_NUMERO_ORD  + "='" + getNumeroDocumento()+"' AND "+
                      CommessaTM.R_RIGA_ORD    + "="  + getNumeroRigaDocumento();

       List commesseOrdVen = (List)Commessa.retrieveList(where, "", false);
       if (commesseOrdVen != null && !commesseOrdVen.isEmpty()) {
         return (Commessa)commesseOrdVen.get(0);
       }
     }
     catch(Exception ex){ex.printStackTrace(Trace.excStream);}
     return null;
   }

   public synchronized int aggiornaCommessaRif(){
    int ret = 0;
    Commessa commessa = isRigaOrdVenRefCommessa();
    if(commessa == null)
      return ret;

    try{
      PreparedStatement ps=cUpdateCommessaRigaOrdVenStm.getStatement();
      Database db=ConnectionManager.getCurrentDatabase();

      db.setString(ps,1,String.valueOf(Commessa.STATO_AVANZAM__PROVVISORIA));
      ps.setNull(2,java.sql.Types.INTEGER);
      db.setString(ps,3,getIdAzienda());
      db.setString(ps,4,commessa.getIdCommessa());
      ret = ps.executeUpdate();
    }
    catch(Exception ex){ex.printStackTrace(Trace.excStream);}
    return ret;
  }

  public void setErrorForzabileClear(boolean clear){
    iErrorForzabileClear = clear;
  }

  public boolean isErrorForzabileClear(){
    return iErrorForzabileClear;
  }
  //Fix 20569 Fine
  //Fix 22839 inizio
  protected Entity getEntityRiga() {
    try {
      return Entity.elementWithKey("OrdVenRigaPrm", Entity.NO_LOCK);
    }
    catch (Exception ex) {
      return null;
    }
  }
  //Fix 22839 fine
  //Fix 23345 inizio
  protected ErrorMessage controlloRicalcoloCondizioniVen() {
    ErrorMessage err = null;
    if(isOnDB()){
      String psnControlloRicalCondizVen = ParametroPsn.getValoreParametroPsn("std.vendite", "controlloRicalcoloCondizioni");
      if (isControlloRicalCondiz() && psnControlloRicalCondizVen.equals(CONTROLLO_RICAL_COND_AL_SALVATAGGIO) && isCondizVenCambiata()) {
        err = new ErrorMessage("THIP40T401");
      }
    }
    return err;
  }

  public boolean isCondizVenCambiata() {
    OrdineRiga oldRiga = getOldRiga();
    OrdineVendita testata = (OrdineVendita)this.getTestata();
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


   //Fix 19453 - inizio
   protected Magazzino getMagazzinoRigaSecModello(EspNodoArticoloBase datiRigaSec) throws SQLException {
	   return getMagazzino();
   }
   //Fix 19453 - fine


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
    if(art != null){
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
        if (provPrezzo != TipoRigaRicerca.MANUALE)
        {
          dettRigaCfg = dammiOggettoGestione();
          if((isInCopiaRiga && !iControlloRicalVlrDettCfg) || (((DocumentoOrdineTestata)this.getTestata()).isInCopia() && !isCondVenCopiaDaRicalcolare))
            yrit = dettRigaCfg.copiaDettRigaCfg(this,rigaDaCopiareKey);
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
    if(getOffertaClienteRiga() != null){
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

  public ListinoVendita getListino() {
    return getListinoVendita();
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


//Fix 28653 - inizio
protected BigDecimal getScontoProvv2Pers(BigDecimal sconto) {
	return sconto;
}
//Fix 28653 - fine
//Fix 30193 Inizio
public boolean hasRigheSecondarieConConfigurazioneNeutra() {
	boolean isOk = false; 
	Iterator iter = this.getRigheSecondarie().iterator() ;
	while(iter.hasNext()) {
		OrdineVenditaRigaSec sec = (OrdineVenditaRigaSec)iter.next();
		if(sec.getDatiComuniEstesi().getStato() == DatiComuniEstesi.VALIDO) {
			if(sec.isConfigurazioneNeutra()) {
				isOk  = true ;
				break;
			}
		}
	}
	return isOk ;
}
//Fix 30193 Fine

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

//Fix 36208 Inizio
public boolean isDesattivaInitializeOwnedObjects() {
	return iDesattivaInitializeOwnedObjects;
}
public void setDesattivaInitializeOwnedObjects(boolean desattivaInitializeOwnedObjects)
{
	this.iDesattivaInitializeOwnedObjects = desattivaInitializeOwnedObjects;
}
//Fix 36208 Fine
//Fix 40598 Inizio
public void impostaDatiPerBene(ProposizioneAutLotto pal){
	    pal.setArticolo(getArticolo());	    
}
//Fix 40598 Fine

//37420 inizio
/**
 * @param newRow
 */
public void cambioTestataStatoIM(boolean newRow) {
	if(newRow) {
		OrdineVendita testata = ((OrdineVendita) getTestata());
		if(testata != null) {
			if((testata.getStatoIntellimag() == OrdineVendita.TRASMESSO || testata.getStatoIntellimag() == OrdineVendita.PRELEVATO)&& isTipoRigaDaTrassmeso()) {//40694//40083
				testata.setStatoIntellimag(OrdineVendita.RITRASMETTERE);
			}
		}
	}
}
//37420 fine

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
