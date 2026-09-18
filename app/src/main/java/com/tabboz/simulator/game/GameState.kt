@file:Suppress("unused", "PropertyName", "ObjectPropertyName", "FunctionName", "ArrayInDataClass")

package com.tabboz.simulator.game

import com.tabboz.simulator.win.Dlg

// =============================================================================
// Stato globale del gioco: tutte le variabili globali e le tabelle dati
// dei sorgenti C originali (zarrosim.h e i vari .c). I nomi sono identici
// a quelli C per rendere la traduzione meccanica.
// =============================================================================

const val VERSION = "Version 0.92q"
/** Equivalente di __DATE__ della build originale. */
const val BUILD_DATE = "Sep 18 2026"

// --- ID controlli / comandi (zarrosim.h) -------------------------------------
const val QX_NOME = 102
const val QX_BMP = 103
const val QX_SOLDI = 105
const val QX_LOAD = 106
const val QX_SAVE = 107
const val QX_CLOSE = 108
const val QX_ADDW = 119
const val QX_NOREDRAW = 120
const val QX_REDRAW = 121
const val QX_ABOUT = 120
const val QX_LOGO = 121
const val QX_SCOOTER = 130
const val QX_VESTITI = 131
const val QX_DISCO = 132
const val QX_TIPA = 133
const val QX_COMPAGNIA = 134
const val QX_FAMIGLIA = 135
const val QX_SCUOLA = 136
const val QX_LAVORO = 137
const val QX_INFO = 139
const val QX_CONFIG = 140
const val QX_TABACCHI = 141
const val QX_PALESTRA = 142
const val QX_VESTITI1 = 143
const val QX_VESTITI2 = 144
const val QX_VESTITI3 = 145
const val QX_VESTITI4 = 146
const val QX_VESTITI5 = 147
const val QX_PROMPT = 150
const val QX_NETWORK = 151
const val QX_CELLULAR = 155

// --- ID dialoghi (zarrosim.h) -------------------------------------------------
const val MAIN = 1
const val ABOUT = 2
const val WARNING = 3
const val DISCO = 4
const val FAMIGLIA = 5
const val COMPAGNIA = 6
const val SCOOTER = 7
const val VESTITI = 8
const val TIPA = 9
const val SCUOLA = 10
const val PERSONALINFO = 11
const val LOGO = 12
const val LAVORO = 13
const val CONFIGURATION = 14
const val FORMAT = 15
const val SPEGNIMI = 16
const val NETWORK = 17
const val PROMPT = 20
const val ACQUISTASCOOTER = 70
const val VENDISCOOTER = 71
const val RIPARASCOOTER = 72
const val TAROCCASCOOTER = 73
const val BAUHOUSE = 80
const val ZOCCOLARO = 81
const val FOOTSMOCKER = 82
const val ALTRIVESTITI4 = 83
const val ALTRIVESTITI5 = 84
const val ALTRIVESTITI6 = 85
const val TABACCAIO = 88
const val PALESTRA = 89
const val CERCATIPA = 91
const val LASCIATIPA = 92
const val ESCICONLATIPA = 93
const val DUEDIPICCHE = 95
const val CELLULAR = 120
const val COMPRACELLULAR = 121
const val VENDICELLULAR = 122
const val CELLULRABBONAM = 123

const val ATTESAMAX = 5

// --- Strutture (zarrosim.h) ----------------------------------------------------
// ATTENZIONE: in C l'assegnazione tra struct copia i valori. In Kotlin usare sempre
// `.copy()` quando si assegna un elemento di tabella ad una variabile (es. ScooterData = ScooterMem[0].copy()).

/** Struttura generica usata per scooter (vecchia), vestiti, discoteche, sigarette, materie, ditte, palestra. */
data class STSCOOTER(
    var speed: Int = 0,    // velocita' massima (o: fuori porta / condensato, a seconda della tabella)
    var cc: Int = 0,       // cilindrata (o: figosita' minima / nicotina*10)
    var xxx: Int = 0,      // [future espansioni] (o: incremento reputazione / voto materia)
    var fama: Int = 0,     // figosita'
    var mass: Int = 0,     // massa (o: giorno di chiusura)
    var maneuver: Int = 0, // manovrabilita'
    var prezzo: Int = 0,   // costo
    var stato: Int = 0,    // quanto e' intero; -1 nessuno
    var nome: String = "", // nome
)

/** Nuove informazioni sugli scooter. */
data class NEWSTSCOOTER(
    var speed: Int = 0,       // 01 Velocita'
    var marmitta: Int = 0,    // 02 Marmitta (0-3)
    var carburatore: Int = 0, // 03 Carburatore (0-4)
    var cc: Int = 0,          // 04 Cilindrata (0-4)
    var filtro: Int = 0,      // 05 Filtro dell'aria (0-4)
    var prezzo: Int = 0,      // 06 Costo dello scooter (modifiche incluse)
    var attivita: Int = 0,    // 07 Attivita' scooter (vedi n_attivita)
    var stato: Int = 0,       // 08 Quanto e' intero (percentuale); -1 nessuno scooter
    var nome: String = "",    // 09 Nome dello scooter
    var fama: Int = 0,        // 10 Figosita' scooter
)

/** Informazioni sui telefonini. */
data class STCEL(
    var dual: Int = 0,     // Dual Band ?
    var fama: Int = 0,     // figosita'
    var stato: Int = 0,    // quanto e' intero (percentuale); -1 nessuno
    var prezzo: Int = 0,
    var nome: String = "", // nome del telefono
)

/** Informazioni sulle compagnie dei telefonini. */
data class STABB(
    var abbonamento: Int = 0, // 0 = Ricarica, 1 = Abbonamento
    var dualonly: Int = 0,    // Dual Band Only ?
    var creditorest: Int = 0, // Credito Restante (-1 = nessun abbonamento)
    var fama: Int = 0,        // figosita'
    var prezzo: Int = 0,
    var nome: String = "",    // nome della compagnia
)

data class STMESI(val nome: String, val num_giorni: Int)

data class STVACANZE(val nome: String, val giorno: Int, val mese: Int, val descrizione: String)

// =============================================================================
// VARIABILI GLOBALI (zarrosim.c)
// =============================================================================

var cheat = 0
var scelta = 0
var Andrea = ""
var Caccia = ""
var Daniele = ""
var Obscured = ""
var firsttime = 0     // 29 Novembre 1998 - 0.8.1pr
var ImgSelector = 0   // 25 Febbraio 1999 - 0.8.3pr
var TabbozRedraw = 0  // E' necessario ridisegnare il Tabbozzo ???
var ScuolaRedraw = 0  // E' necessario ridisegnare la scuola ???

var Attesa = 0          // Tempo prima che ti diano altri soldi...
var Fama = 0
var Reputazione = 0
var Studio = 0          // Quanto vai bene a scuola (1 - 100)
var Soldi: Long = 0     // u_long
var Paghetta: Long = 0  // u_long - Paghetta settimanale
var Nome = ""           // Nome del Tabbozzo
var Cognome = ""        // Cognome del Tabbozzo
var Nometipa = ""       // Nome della tipa
var City = ""           // Citta' di nascita
var Residenza = ""      // Citta' dove vive
var Street = ""         // Dove sto' tipo abita
var FigTipa = 0         // Figosita' della tipa
var Rapporti = 0        // Rapporti Tipo <-> Tipa
var Stato = 0           // Quanto stai male ???
var DDP: Long = 0       // u_long - Due di picche (log...)
var Fortuna = 0         // Fortuna del tabbozzo
var sizze = 0           // Numero di sigarette
var Tempo_trascorso_dal_pestaggio = 0
var current_testa = 0     // Grado di abbronzatura del tabbozzo
var current_gibbotto = 0  // Vestiti attuali del tabbozzo...
var current_pantaloni = 0
var current_scarpe = 0
var current_tipa = 0

var comp_giorno = 0 // giorno & mese del compleanno
var comp_mese = 0

var timer_active = 0
var fase_di_avvio = 0
var sound_active = 0
var debug_active = 0   // TABBOZ_DEBUG

/** hWnd della finestra principale (null = non esiste). */
var hWndMain: Dlg? = null

var euro = 0
var sesso = 'M'
var ao = 'o'
var un_una = "un"

var t_random = 0        // Attesa a random tra i vari eventi timer
var STARTcmdShow = 0    // Mostra il logo all'avvio
var boolean_shutdown = 0 // 0=resta dentro, 1=uscita, 2=shutdown

/** Scooter attuale del tabbozzo. */
var ScooterData = NEWSTSCOOTER()

/** Non usato su Android (salvataggio su file). Resta vuoto. */
var nome_del_file_su_cui_salvare = ""

// --- eventi.c ---
var messaggio = ""

// --- newproteggi.c ---
var new_counter: Long = 0  // u_long

// =============================================================================
// tempo.c
// =============================================================================

val InfoMese = arrayOf(
    STMESI("Gennaio", 31),
    STMESI("Febbraio", 28),
    STMESI("Marzo", 31),
    STMESI("Aprile", 30),
    STMESI("Maggio", 31),
    STMESI("Giugno", 30),
    STMESI("Luglio", 31),
    STMESI("Agosto", 31),
    STMESI("Settembre", 30),
    STMESI("Ottobre", 31),
    STMESI("Novembre", 30),
    STMESI("Dicembre", 31),
)

val InfoSettimana = arrayOf(
    STMESI("Lunedi'", 0),
    STMESI("Martedi'", 0),
    STMESI("Mercoledi'", 0),
    STMESI("Giovedi'", 0),
    STMESI("Venerdi'", 0),
    STMESI("Sabato", 0),
    STMESI("Domenica", 1),
)

/** Giorni di vacanza (l'ultimo elemento {NULL,0,0,NULL} del C non e' presente: usare la lista intera). */
val InfoVacanze = arrayOf(
    STVACANZE("Capodanno", 1, 1, "Oggi e' capodanno !"),
    STVACANZE("Epifania", 6, 1, "Epifania..."),
    STVACANZE("Anniversario Liberazione", 25, 4, "Oggi mi sento liberato"),
    STVACANZE("Festa dei lavoratori", 1, 5, "Nonostante nella tua vita, tu non faccia nulla, oggi fai festa anche tu..."),
    STVACANZE("Ferragosto", 15, 8, "Oggi e' ferragosto..."),
    STVACANZE("Tutti i Santi", 1, 11, "Figata, oggi e' vacanza..."),
    STVACANZE("Sant' Ambrogio", 7, 12, "Visto che siamo a Milano, oggi facciamo festa."),
    STVACANZE("Immacolata Concezione", 8, 12, "Oggi e' festa..."),
    STVACANZE("Natale", 25, 12, "Buon Natale !!!"),
    STVACANZE("Santo Stefano", 26, 12, "Buon Santo Stefano..."),
)

var x_giorno = 0
var x_mese = 0
var x_anno_bisesto = 0 // Anno Bisestile
var x_giornoset = 0
var x_vacanza = 0        // Se e' un giorno di vacanza, e' uguale ad 1 o 2 altrimenti a 0
var scad_pal_giorno = 0  // Giorno e mese in cui scadra' l'abbonamento alla palestra
var scad_pal_mese = 0

// =============================================================================
// scooter.c
// =============================================================================

val n_carburatore = arrayOf("12/10", "16/16", "19/19", "20/20", "24/24", "custom")
val n_cc = arrayOf("50cc", "70cc", "90cc", "120cc", "150cc", "3969cc")
val n_marmitta = arrayOf("standard", "silenziosa", "rumorosa", "rumorosissima")
val n_filtro = arrayOf("standard", "P1", "P2", "P2+", "Extreme")
val n_attivita = arrayOf("mancante", "funzionante", "ingrippato", "invasato", "parcheggiato", "sequestrato", "a secco")

val tabella = intArrayOf(
    65, 70, -100, -100, -100, -100,
    70, 80, 95, -100, -100, -100,
    -1000, 90, 100, 115, -100, -100,
    -1000, -1000, 110, 125, 135, -100,
    -1000, -1000, -1000, 130, 150, -100,
    -1000, -1000, -1000, -1000, -1000, 250,
)

var showscooter = 0 // char

// speed, marmitta, carburatore, cc, filtro, prezzo, attivita, stato, nome, fama
val ScooterMem = arrayOf(
    NEWSTSCOOTER(0, 0, 0, 0, 0, 0, 0, -1, "Nessuno scooter", 0),
    NEWSTSCOOTER(65, 0, 0, 0, 0, 2498, 1, 100, "Magutty Firecow", 5),
    NEWSTSCOOTER(75, 0, 1, 1, 1, 4348, 1, 100, "Honda F98", 10),
    NEWSTSCOOTER(105, 1, 1, 2, 1, 6498, 1, 100, "Mizzubisci R200 Millenium", 15),
    NEWSTSCOOTER(75, 0, 0, 1, 1, 4298, 1, 100, "Magutty Firecow+", 7),
    NEWSTSCOOTER(100, 0, 1, 2, 1, 5998, 1, 100, "Magutty Firecow II", 10),
    NEWSTSCOOTER(100, 0, 1, 2, 1, 6348, 1, 100, "Honda F98s", 13),
    NEWSTSCOOTER(250, 0, 5, 5, 0, 1450, 1, 100, "Lexux LS400 ", 60),
)

var benzina = 0
var antifurto = 0

val PezziMem = intArrayOf(
    400, 500, 600,       // marmitte
    300, 470, 650, 800,  // carburatori
    200, 400, 800, 1000, // cc
    50, 120, 270, 400,   // filtro
)

// =============================================================================
// tipa.c (globali condivise con eventi.c e tabimg.c)
// =============================================================================

var figTemp = 0
var nomeTemp = ""
/** Serve per BMPTipaWndProc (in TabImg.kt). */
var tipahDlg: Dlg? = null

// =============================================================================
// disco.c
// =============================================================================

// speed=1: disco fuori porta (solo con lo scooter); cc: figosita' minima per entrare;
// xxx: incremento reputazione; fama: incremento fama; mass: giorno di chiusura (1=lunedi', 0=nessuno); prezzo: costo
val DiscoMem = arrayOf(
    STSCOOTER(0, 0, 0, 0, 0, 0, 0, 0, "---"),
    STSCOOTER(0, 30, 2, 15, 1, 0, 36, 0, ""),
    STSCOOTER(0, 0, 1, 7, 4, 0, 26, 0, ""),
    STSCOOTER(0, 0, 1, 8, 1, 0, 30, 0, ""),
    STSCOOTER(0, 35, 3, 15, 1, 0, 36, 0, ""),
    STSCOOTER(0, 0, 2, 6, 3, 0, 26, 0, ""),
    STSCOOTER(0, 0, 2, 5, 2, 0, 22, 0, ""),
    STSCOOTER(0, 0, 3, 8, 1, 0, 30, 0, ""),
    STSCOOTER(1, 0, 2, 9, 7, 0, 36, 0, ""),
)

// =============================================================================
// vestiti.c
// =============================================================================

// fama: incremento figosita'; prezzo: costo
val VestitiMem = arrayOf(
    STSCOOTER(0, 0, 0, 0, 0, 0, 0, 0, "---"),
    STSCOOTER(0, 0, 0, 8, 0, 0, 348, 0, ""),  // 1  -- Giubbotto "Fatiscenza"
    STSCOOTER(0, 0, 0, 9, 0, 0, 378, 0, ""),  // 2
    STSCOOTER(0, 0, 0, 7, 0, 0, 298, 0, ""),  // 3
    STSCOOTER(0, 0, 0, 8, 0, 0, 248, 0, ""),  // 4     Giacca di pelle (3 Nuovi Giubbotti)
    STSCOOTER(0, 0, 0, 9, 0, 0, 378, 0, ""),  // 5     Fatiscenza verde
    STSCOOTER(0, 0, 0, 10, 0, 0, 418, 0, ""), // 6     Fatiscenza bianco
    STSCOOTER(0, 0, 0, 3, 0, 0, 90, 0, ""),   // 7  -- Pantaloni gessati
    STSCOOTER(0, 0, 0, 5, 0, 0, 170, 0, ""),  // 8     Pantaloni tuta
    STSCOOTER(0, 0, 0, 6, 0, 0, 248, 0, ""),  // 9     Pantaloni in plastika
    STSCOOTER(0, 0, 0, 5, 0, 0, 190, 0, ""),  // 10    Pantaloni scacchiera
    STSCOOTER(0, 0, 0, 4, 0, 0, 122, 0, ""),  // 11 -- Scarpe da tabbozzi...
    STSCOOTER(0, 0, 0, 6, 0, 0, 220, 0, ""),  // 12    Buffalo
    STSCOOTER(0, 0, 0, 2, 0, 0, 58, 0, ""),   // 13    Scarpe da tabbozzi...
    STSCOOTER(0, 0, 0, 4, 0, 0, 142, 0, ""),  // 14    NUOVE Scarpe da tabbozzi...
    STSCOOTER(0, 0, 0, 4, 0, 0, 142, 0, ""),  // 15
    STSCOOTER(0, 0, 0, 5, 0, 0, 166, 0, ""),  // 16
    STSCOOTER(0, 0, 0, 6, 0, 0, 230, 0, ""),  // 17    Nuove Buffalo
)

/** Abbonamenti Palestra: un mese, sei mesi, un anno, una lampada. */
val PalestraMem = arrayOf(
    STSCOOTER(0, 0, 0, 0, 0, 0, 50, 0, ""),
    STSCOOTER(0, 0, 0, 8, 0, 0, 270, 0, ""),
    STSCOOTER(0, 0, 0, 9, 0, 0, 500, 0, ""),
    STSCOOTER(0, 0, 0, 9, 0, 0, 14, 0, ""),
)

/** Sigarette: speed = condensato, cc = nicotina * 10, fama = incremento figosita', prezzo = costo. */
val SizeMem = arrayOf(
    STSCOOTER(5, 5, 0, 2, 0, 0, 6, 0, "Barclay"),
    STSCOOTER(8, 7, 0, 1, 0, 0, 6, 0, "Camel"),
    STSCOOTER(7, 6, 0, 2, 0, 0, 6, 0, "Davidoff Superior Lights"),
    STSCOOTER(7, 6, 0, 2, 0, 0, 6, 0, "Davidoff Mildnes"),
    STSCOOTER(13, 9, 0, 2, 0, 0, 6, 0, "Davidoff Classic"),
    STSCOOTER(9, 7, 0, 1, 0, 0, 5, 0, "Diana Blu"),
    STSCOOTER(12, 9, 0, 1, 0, 0, 5, 0, "Diana Rosse"),
    STSCOOTER(8, 7, 0, 0, 0, 0, 6, 0, "Dunhill Lights"),
    STSCOOTER(7, 5, 0, 0, 0, 0, 6, 0, "Merit"),
    STSCOOTER(14, 10, 0, 0, 0, 0, 6, 0, "Gauloises Blu"),
    STSCOOTER(7, 6, 0, 0, 0, 0, 6, 0, "Gauloises Rosse"),
    STSCOOTER(13, 10, 0, 1, 0, 0, 6, 0, "Unlucky Strike"),
    STSCOOTER(9, 7, 0, 1, 0, 0, 6, 0, "Unlucky Strike Lights"),
    STSCOOTER(8, 6, 0, 2, 0, 0, 6, 0, "Malborro Medium"),
    STSCOOTER(12, 9, 0, 2, 0, 0, 6, 0, "Malborro Rosse"),
    STSCOOTER(8, 6, 0, 2, 0, 0, 6, 0, "Malborro Lights"),
    STSCOOTER(11, 10, 0, 0, 0, 0, 5, 0, "NS Rosse"),
    STSCOOTER(9, 8, 0, 0, 0, 0, 5, 0, "NS Mild"),
    STSCOOTER(9, 7, 0, 1, 0, 0, 5, 0, "Poll Mon Blu"),
    STSCOOTER(12, 9, 0, 1, 0, 0, 5, 0, "Poll Mon Rosse"),
    STSCOOTER(12, 10, 0, 2, 0, 0, 6, 0, "Philip Morris"),
    STSCOOTER(4, 4, 0, 2, 0, 0, 6, 0, "Philip Morris Super Light"),
    STSCOOTER(10, 9, 0, 1, 0, 0, 5, 0, "Armadis"),
    STSCOOTER(11, 9, 0, 0, 0, 0, 5, 0, "Winston"),
)

// =============================================================================
// scuola.c
// =============================================================================

/** Materie scolastiche: xxx = voto (0-10). Indici 1..9 (l'indice 0 non e' usato). */
val MaterieMem = arrayOf(
    STSCOOTER(0, 0, 0, 0, 0, 0, 0, 0, "---"),
    STSCOOTER(0, 0, 0, 0, 0, 0, 0, 0, "Agraria"),
    STSCOOTER(0, 0, 0, 0, 0, 0, 0, 0, "Fisica"),
    STSCOOTER(0, 0, 0, 0, 0, 0, 0, 0, "Attivita' culturali"),
    STSCOOTER(0, 0, 0, 0, 0, 0, 0, 0, "Attivita' matematiche"),
    STSCOOTER(0, 0, 0, 0, 0, 0, 0, 0, "Scienze industriali"),
    STSCOOTER(0, 0, 0, 0, 0, 0, 0, 0, "Elettrochimica"),
    STSCOOTER(0, 0, 0, 0, 0, 0, 0, 0, "Petrolchimica"),
    STSCOOTER(0, 0, 0, 0, 0, 0, 0, 0, "Filosofia aziendale"),
    STSCOOTER(0, 0, 0, 0, 0, 0, 0, 0, "Metallurgia"),
)

// =============================================================================
// lavoro.c
// =============================================================================

var numeroditta = 0
var impegno = 0
var stipendio = 0
var giorni_di_lavoro = 0
var punti_scheda = 0

val Risposte1 = IntArray(3)
val Risposte2 = IntArray(3)
val Risposte3 = IntArray(3)

const val NUM_DITTE = 8

/** Ditte: speed = 1 lavoro fuori porta (solo con lo scooter puoi arrivarci). */
val LavoroMem = arrayOf(
    STSCOOTER(0, 0, 0, 0, 0, 0, 0, 0, "---"),
    STSCOOTER(0, 0, 0, 0, 0, 0, 0, 0, "Magneti Budelli"),
    STSCOOTER(0, 0, 0, 0, 0, 0, 0, 0, "Diamine"),
    STSCOOTER(1, 0, 0, 0, 0, 0, 0, 0, "Testmec"),
    STSCOOTER(0, 0, 0, 0, 0, 0, 0, 0, "Ti Impalo Bene Bene"),
    STSCOOTER(1, 0, 0, 0, 0, 0, 0, 0, "October"),
    STSCOOTER(1, 0, 0, 0, 0, 0, 0, 0, "Arlond's"),
    STSCOOTER(1, 0, 0, 0, 0, 0, 0, 0, "286 - Computer d' annata"),
    STSCOOTER(1, 0, 0, 0, 0, 0, 0, 0, "Ricopio"),
)

var scheda = 0  // numero scheda del quiz (0 - 9)
var accetto = 0

// =============================================================================
// telefono.c
// =============================================================================

var CellularData = STCEL()

val CellularMem = arrayOf(
    STCEL(0, 2, 100, 290, "Motorolo d170"),
    STCEL(0, 7, 100, 590, "Motorolo 8700"),
    STCEL(1, 10, 100, 990, "Macro TAC 8900"),
)

var AbbonamentData = STABB()

// abbonamento, dualonly, creditorest, fama, prezzo, nome
val AbbonamentMem = arrayOf(
    STABB(1, 0, 50, 1, 100, "Onmitel"),  // Abbonamenti
    STABB(1, 0, 50, 1, 100, "DIM"),
    STABB(1, 1, 100, 1, 100, "Vind"),
    STABB(0, 0, 50, 1, 60, "Onmitel"),   // Ricariche
    STABB(0, 0, 100, 1, 110, "Onmitel"),
    STABB(0, 0, 50, 1, 60, "DIM"),       // Ricariche
    STABB(0, 0, 100, 1, 110, "DIM"),
    STABB(0, 1, 50, 1, 50, "Vind"),      // Ricariche
    STABB(0, 1, 100, 1, 100, "Vind"),
)
