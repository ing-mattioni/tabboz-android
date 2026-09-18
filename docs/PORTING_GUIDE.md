# Guida al porting C → Kotlin (Tabboz Simulator per Android)

Obiettivo: tradurre la logica di gioco C (in `C:\Projects\tabboz\tabboz-main\*.c`) in Kotlin,
**in modo meccanico e fedele** (stessi nomi di funzioni, variabili, ID, testi, formule, casualità).
L'interfaccia utente NON va reimplementata: i dialoghi originali vengono renderizzati automaticamente
dal runtime a partire dal file `.rc`; la logica parla con la UI solo tramite l'API "Win32-like"
descritta sotto (package `com.tabboz.simulator.win`).

Il codice prodotto va in `C:\Projects\tabboz\android\app\src\main\java\com\tabboz\simulator\game\`,
package `com.tabboz.simulator.game`. Ogni file .kt inizia con:

```kotlin
@file:Suppress("FunctionName", "LocalVariableName", "unused", "UNUSED_PARAMETER", "NAME_SHADOWING")

package com.tabboz.simulator.game

import com.tabboz.simulator.win.*
```

## File già esistenti (NON ricrearli, usarli)

- `win/Win.kt` — costanti: `WM_INITDIALOG, WM_COMMAND, WM_SYSCOMMAND, WM_TIMER, WM_LBUTTONDOWN, WM_DESTROY,
  SC_CLOSE, IDOK, IDCANCEL, IDYES, IDNO, MB_OK, MB_OKCANCEL, MB_YESNO, MB_ICONHAND, MB_ICONSTOP,
  MB_ICONQUESTION, MB_ICONEXCLAMATION, MB_ICONINFORMATION`, funzioni `LOWORD(v)`, `HIWORD(v)`, `MAKELPARAM(x,y)`
  e il typealias `DlgProc = suspend (hDlg: Dlg, message: Int, wParam: Int, lParam: Int) -> Boolean`.
- `win/Dlg.kt` — classe `Dlg` = handle di finestra (l'`HWND` di un dialogo). Ha `parent: Dlg?`, `templateId`.
- `win/WinApi.kt` — le funzioni API (tutte top-level, elencate sotto).
- `game/GameState.kt` — **tutte** le variabili globali e le tabelle dati (ScooterMem, VestitiMem, DiscoMem,
  SizeMem, PalestraMem, MaterieMem, LavoroMem, CellularMem, AbbonamentMem, InfoMese, InfoSettimana, InfoVacanze,
  PezziMem, tabella, n_attivita, n_carburatore, n_cc, n_marmitta, n_filtro, ...) e le costanti degli ID
  (QX_*, MAIN, ABOUT, DISCO, ..., ATTESAMAX, VERSION, BUILD_DATE). Leggerlo prima di iniziare.
  Non ridefinire nulla di ciò che contiene. Le variabili `static` locali ad un file C (es. `numdisco`,
  `spostamento`, `descrizione`, `natale2`, `tmpsesso`, `offerta`, `costo`, `ScooterTemp`, `Rcheck`, ...) vanno
  invece dichiarate come `private var` top-level nel file Kotlin corrispondente.

## API disponibile (package `com.tabboz.simulator.win`)

| C originale | Kotlin |
|---|---|
| `DialogBox(hInst, MAKEINTRESOURCE(ID), hDlg, lpproc)` (+ `MakeProcInstance`/`FreeProcInstance`) | `DialogBox(ID, hDlg, ::Proc)` — **suspend**, ritorna Int |
| `EndDialog(hDlg, TRUE)` | `EndDialog(hDlg, 1)` |
| `MessageBox(hDlg, text, caption, MB_OK \| MB_ICONSTOP)` | `MessageBox(hDlg, text, caption, MB_OK or MB_ICONSTOP)` — **suspend**, ritorna IDOK/IDYES/IDNO/IDCANCEL |
| `SetDlgItemText(hDlg, id, s)` | `SetDlgItemText(hDlg, id, s)` (s può essere null) |
| `GetDlgItemText(hDlg, id, buf, size)` | `val s = GetDlgItemText(hDlg, id)` |
| `SendMessage(GetDlgItem(hDlg,id), BM_SETCHECK, TRUE, 0L)` | `CheckDlgButton(hDlg, id, true)` |
| `SendMessage(..., BM_GETCHECK, ...)` | `IsDlgButtonChecked(hDlg, id)` |
| `SendMessage(GetDlgItem(hDlg,110), CB_ADDSTRING, 0, "x")` | `ComboAddString(hDlg, 110, "x")` |
| `SendMessage(GetDlgItem(hDlg,110), CB_SETCURSEL, 0, 0)` | `ComboSetCurSel(hDlg, 110, 0)` |
| `EnableWindow(GetDlgItem(hDlg, 2), 0)` | `EnableWindow(hDlg, 2, false)` |
| `ShowWindow(hWnd, WIN_PICCOLO)` / `ShowWindow(hWnd, SW_HIDE)` | `ShowWindow(hWnd, false)` |
| `ShowWindow(parent, WIN_GRANDE)` / `SW_SHOWNORMAL` | `ShowWindow(parent, true)` |
| `SetTimer(hDlg, WM_TIMER, 10000, NULL)` | `SetTimer(hDlg, 10000)` |
| `KillTimer(hDlg, WM_TIMER)` | `KillTimer(hDlg)` |
| `InvalidateRect`/`UpdateWindow` | `InvalidateRect(hDlg)` |
| `LoadString(hInst, id, buf, size)` | `val s = LoadString(id)` (ritorna "" se assente) |
| `TabbozPlaySound(n)` | `TabbozPlaySound(n)` |
| `SpegniISuoni()` | `SpegniISuoni()` |
| `random(n)` | `random(n)` (per n <= 0 ritorna 0) |
| `randomize()` | `randomize()` (no-op) |
| `writelog(s)` | `writelog(s)` |
| `TabbozAddKey(k, v)` | `TabbozAddKey(k, v)` |
| `TabbozReadKey(k, buf)` (ritorna NULL se assente) | `TabbozReadKey(k)` → `String?` |
| `RRKey(k)` | `RRKey(k)` → `String` ("" se assente) |
| `atoi(s)` / `atol(s)` | `atoi(s)` (Int) / `atol(s)` (Long) — tolleranti come in C |
| `DeleteMenu(...)`+`AppendMenu(GetSubMenu(GetMenu(parent),1), MF_STRING, QX_TIPA, "&Tipa...")` | `SetMenuItemText(parent, QX_TIPA, "&Tipa...")` |
| `EM_ASM(document.querySelector(".menu106").classList.add("disabled"))` | `EnableMenuItem(hWnd, 106, false)` |
| `GetSystemMetrics`, `MoveWindow`, `GetWindowRect`, `SetFocus`, `LoadIcon`, `DestroyIcon`, `AppendMenu(GetSystemMenu...)`, `DrawMenuBar`, `BWCCRegister`, `RegisterClass`, `SetWindowPos`, `MessageBeep` | **omettere** (nessun equivalente necessario) |

`hInst`/`hInstance` come parametro delle funzioni (es. `void Evento(HANDLE hInstance)`, `void Giorno(HANDLE hInstance)`,
`PagaDisco(HANDLE hInstance)`) è in realtà un `HWND` del dialogo chiamante: tradurre con `hInstance: Dlg?`.
`MessageBox(0, ...)` → `MessageBox(null, ...)`.

## Regole di traduzione

1. **Nomi identici al C** per funzioni e variabili (anche se non idiomatici: `Soldi`, `x_giorno`, `AggiornaScooter`).
2. **Procedura di dialogo**:
   ```kotlin
   suspend fun Famiglia(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
       if (message == WM_INITDIALOG) { ...; return true }
       else if (message == WM_COMMAND) {
           when (wParam) {   // switch (LOWORD(wParam)) o switch (wParam): stessa cosa
               101 -> { ...; return true }
               IDOK, IDCANCEL -> { EndDialog(hDlg, 1); return true }
               else -> return true
           }
       }
       return false
   }
   ```
   La firma è sempre `(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean` e la funzione è `suspend`.
   Le funzioni che chiamano (direttamente o indirettamente) `MessageBox`/`DialogBox` devono essere `suspend`
   (es. `Evento`, `Giorno`, `CalcolaVelocita`, `nomoney`, `PagaDisco`, `RunVestiti`, `GiornoDiLavoro`, ...).
   Le funzioni che aggiornano solo testi (`AggiornaScooter`, `AggiornaTipa`, `ScriviVoti`, `MostraSoldi`...) NON sono suspend.
3. **`switch` con fallthrough**: attenzione ai `case` C senza `break` (es. in `eventi.c` i casi 21..30 accumulano
   `Fama -= 5`, `Fama -= 1`, `Fama -= 1`): riprodurre esattamente l'effetto cumulativo con `if`/`when` espliciti.
4. **Assegnazione di struct**: in C copia per valore → in Kotlin usare `.copy()`:
   `ScooterData = ScooterMem[0]` → `ScooterData = ScooterMem[0].copy()`; `ScooterTemp = ScooterData` →
   `ScooterTemp = ScooterData.copy()`; `CellularData = CellularMem[scelta]` → `.copy()`; `AbbonamentData = AbbonamentMem[scelta]` → `.copy()`.
   Non modificare mai gli elementi delle tabelle *Mem tranne `MaterieMem[i].xxx` (i voti), che in C viene modificato di proposito.
5. **Tipi**: `int` → `Int`; `u_long`/`long` → `Long` (`Soldi`, `Paghetta`, `DDP`, `new_counter`, `offerta`, `costo`,
   `stipendietto`, `Prezzo` in PagaDisco). `Soldi` è `Long`: `Soldi -= 10`, `Soldi < 15`, `Soldi += Paghetta` funzionano
   direttamente; `Soldi < PezziMem[i]` funziona (Long vs Int); `MostraSoldi(x)` accetta `Long`: passare `.toLong()` per gli Int
   (es. `MostraSoldi(costo)`, `MostraSoldi(VestitiMem[i].prezzo.toLong())`, `MostraSoldi(100)` → il letterale 100 è ok perché
   la firma è `MostraSoldi(i: Long)` e Kotlin converte i letterali interi).
   `char sesso` → `Char` (`sesso == 'M'`); `char ao` → `Char`; `char un_una[4]` → `String`.
   `char buf[128]`, `char tmp[1024]` → variabili `String` locali (`var tmp: String`). `char showscooter`, `char natale2`,
   `char boolean_shutdown`, `char Risposte1[3]` (già `IntArray(3)`) → `Int`.
   `BOOL`/`TRUE`/`FALSE` di ritorno delle procedure → `Boolean` `true`/`false`. `EndDialog(hDlg, TRUE)` → `EndDialog(hDlg, 1)`.
6. **`sprintf`** → `String.format` o template: `sprintf(tmp, "%d/100", Fama)` → `tmp = "$Fama/100"`;
   `sprintf(tmp, "%s n. %d", s, n)`; `"%lu000 L."` → `"${i}000 L."`; `"%c"` con `ao` → `"$ao"`; `"%lue"` → `"${i / 2}e"`;
   `sprintf(tmp, buf, MostraSoldi(...))` con `buf` caricato da LoadString (contiene `%s`) → `tmp = String.format(buf, MostraSoldi(...))`.
   Le stringhe C con `\` a fine riga (continuazione) vanno concatenate come in C (senza a capo).
   Conservare **esattamente** i testi italiani originali (compresi errori, apostrofi e spazi).
7. **`strcmp(a, b) == 0`/`!strcmp`** → `a == b`; `strcpy(Nome, "Tizio")` → `Nome = "Tizio"`; `strncpy(tmp, x, n)` → `tmp = x`;
   `strcat(messaggio, s)` → `messaggio += s`; `tmp[0] = tolower(tmp[0])` → `tmp = tmp.replaceFirstChar { it.lowercaseChar() }`;
   `Nometipa[0] = 0` → `Nometipa = ""`; `sizeof(...)` → omettere.
8. **`div`/`ldiv`**: `d = div(benzina, 10); d.quot, d.rem` → `benzina / 10`, `benzina % 10`.
9. **Operatori bit-a-bit usati come logici**: `(a != -1) & (b == 1)` → `&&`.
10. **Direttive di compilazione**: il target equivale alla build Emscripten. Includere il codice sotto `#ifdef TABBOZ_EM`
    e `#ifdef TABBOZ_DEBUG` (writelog); **escludere** `#ifndef TABBOZ_EM` (branch non-EM), `#ifndef NONETWORK` (rete),
    `#ifdef PROMPT_ACTIVE`, `#ifdef CDROM`, `#ifdef VECCHIO_RICORDO`, `#ifdef WONTFIX`, `#ifdef VERAMENTE_INUTILE`,
    `#ifdef TABBOZ_WIN`-only (es. `_argc/_argv`, `OpenFileDlg/SaveFileDlg`), `#if 0`. Il codice sotto `#ifndef NOMENU` va
    incluso usando `SetMenuItemText` (vedi tabella API). `#ifdef NOTABBOZZA` non è definito (includere il branch `#else`).
11. **Ricorda gli hook della build EM**: `case QX_BMP:` in `TabbozWndProc` → `BMPViewWndProc(hWnd, WM_LBUTTONDOWN, wParam, lParam)`;
    `case 130:` in `Tipa` → `BMPTipaWndProc(hDlg, WM_LBUTTONDOWN, wParam, lParam)`; `SalvaTutto()` alla fine di `Evento()` e
    in `Configuration`/`PersonalInfo` su IDOK/IDCANCEL.
12. **Valori di ritorno di `MessageBox`**: `i2 = MessageBox(...)` → `val i2 = MessageBox(...)`; confronti con `IDYES`/`IDNO`.
13. **`static` dentro le funzioni** (es. `static int i`, `static long offerta`, `static NEWSTSCOOTER ScooterTemp`,
    `static int scelta = 0` in `CompraCellulare`/`AbbonaCellulare` che *nasconde* la globale `scelta`): dichiarare `private var`
    top-level nel file, con nome che eviti conflitti (es. `private var CompraCellulare_scelta = 0`).
14. Nessuna semantica di overflow unsigned da riprodurre: usare `Long`/`Int` normalmente.
15. **Non aggiungere funzionalità**, non "correggere" la logica di gioco, non cambiare i numeri; è consentito solo
    aggiungere commenti. Le scelte del giocatore e le casualità devono comportarsi esattamente come nell'originale.
16. Le procedure/funzioni con nomi duplicati tra file (es. `CalcolaStudio` dichiarata in zarrosim.h ma definita in scuola.c)
    vanno definite **una sola volta**, nel file indicato dall'assegnazione.

## Funzioni condivise tra file (firme da rispettare)

```kotlin
// Zarrosim.kt (zarrosim.c + readkey.c + newproteggi.c)
fun MostraSoldi(i: Long): String
suspend fun nomoney(parent: Dlg?, tipo: Int)
fun AggiornaPrincipale(parent: Dlg)
suspend fun Atinom(hInstance: Dlg?)
fun vvc(i: Int): Int
fun SalvaTutto()
suspend fun ResetMe(primavolta: Int)
suspend fun FormatTabboz(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean
suspend fun PersonalInfo(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean
suspend fun About(...), Logo(...), Spegnimi(...), Configuration(...), Famiglia(...), Compagnia(...), TabbozWndProc(...)
suspend fun InitTabboz()
suspend fun FineProgramma(caller: String)
suspend fun WinMain(): Int        // entry point: InitTabboz + DialogBox(MAIN, null, ::TabbozWndProc) + chiusura; ritorna boolean_shutdown
fun new_reset_check(); fun new_check_i(i: Int): Int; fun new_check_l(i: Long): Long

// Tempo.kt (tempo.c)
suspend fun Giorno(hInstance: Dlg?)
suspend fun MostraPagella(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean

// Eventi.kt (eventi.c)
suspend fun Evento(hInstance: Dlg?)
suspend fun MostraMetallone(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean

// Scuola.kt (scuola.c)
suspend fun Scuola(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean
fun ScriviVoti(parent: Dlg); fun CalcolaStudio(); fun Aggiorna(parent: Dlg); suspend fun CheckVacanza(parent: Dlg): Boolean

// Lavoro.kt (lavoro.c)
suspend fun Lavoro(...), CercaLavoro(...), ElencoDitte(...)
suspend fun GiornoDiLavoro(hDlg: Dlg, s: String): Boolean; fun AggiornaLavoro(hDlg: Dlg)

// Disco.kt (disco.c)
suspend fun Disco(...); private suspend fun PagaDisco(hInstance: Dlg?)

// Scooter.kt (scooter.c)
suspend fun CalcolaVelocita(hDlg: Dlg?)
suspend fun Scooter(...), AcquistaScooter(...), VendiScooter(...), RiparaScooter(...), TruccaScooter(...), CompraUnPezzo(...), Concessionario(...)
fun AggiornaScooter(hDlg: Dlg); fun MostraSpeed(): String

// Tipa.kt (tipa.c)
suspend fun Tipa(...), DueDonne(...), CercaTipa(...), DueDiPicche(...), MostraSalutieBaci(...)
fun DescrizioneTipa(f: Int); fun DescrizioneTipo(f: Int); fun AggiornaTipa(hDlg: Dlg)

// Vestiti.kt (vestiti.c)
suspend fun Vestiti(...), CompraQualcosa(...), Tabaccaio(...), Palestra(...)
suspend fun PagaQualcosa(hInstance: Dlg?); suspend fun RunTabacchi(hDlg: Dlg); suspend fun RunPalestra(hDlg: Dlg)
suspend fun RunVestiti(hDlg: Dlg, numero: Int); suspend fun EventiPalestra(hInstance: Dlg?)

// Telefono.kt (telefono.c)
suspend fun Cellular(...), CompraCellulare(...), AbbonaCellulare(...); fun AggiornaCell(hDlg: Dlg)

// TabImg.kt (tabimg.c)
suspend fun BMPViewWndProc(hWnd: Dlg, msg: Int, wParam: Int, lParam: Int): Int
suspend fun BMPTipaWndProc(hWnd: Dlg, msg: Int, wParam: Int, lParam: Int): Int
fun RegisterBMPViewClass(); fun RegisterBMPTipaClass()
```

## Esempio completo di traduzione (da zarrosim.c)

C:
```c
BOOL FAR PASCAL Famiglia(HWND hDlg, WORD message, WORD wParam, LONG lParam)
{
    char tmp[1024];
    if (message == WM_INITDIALOG)
    {
        sprintf(tmp, "Papa', mi dai %s ?", MostraSoldi(100));
        SetDlgItemText(hDlg, 103, tmp);
        SetDlgItemText(hDlg, 104, MostraSoldi(Soldi));
        SetDlgItemText(hDlg, 105, MostraSoldi(Paghetta));
        return (TRUE);
    }
    else if (message == WM_COMMAND)
    {
        switch (LOWORD(wParam))
        {
        case 101:
            if (Studio > 40)
            {
                if (((Studio - Paghetta + Fortuna) > (75 + random(50))) & (Paghetta < 96))
                {
                    sprintf(tmp, "Va bene... ti daremo %s di paghetta in piu'...", MostraSoldi(5));
                    MessageBox(hDlg, tmp, "Aumento paghetta !", MB_OK | MB_ICONINFORMATION);
                    Paghetta += 5;
                    Evento(hDlg);
                }
                else { ... }
            }
            else { ... }
            SetDlgItemText(hDlg, 105, MostraSoldi(Paghetta));
            return (TRUE);
        case 103:
            if (sound_active) TabbozPlaySound(801);
            MessageBox(hDlg, "Non pensarci neanche lontanamente...", "Errore irrecuperabile", MB_OK | MB_ICONHAND);
            Evento(hDlg);
            return (TRUE);
        case IDOK:
        case IDCANCEL:
            EndDialog(hDlg, TRUE);
            return (TRUE);
        default:
            return (TRUE);
        }
    }
    return (FALSE);
}
```

Kotlin:
```kotlin
suspend fun Famiglia(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    var tmp: String
    if (message == WM_INITDIALOG) {
        tmp = "Papa', mi dai ${MostraSoldi(100)} ?"
        SetDlgItemText(hDlg, 103, tmp)
        SetDlgItemText(hDlg, 104, MostraSoldi(Soldi))
        SetDlgItemText(hDlg, 105, MostraSoldi(Paghetta))
        return true
    } else if (message == WM_COMMAND) {
        when (wParam) {
            101 -> { // Chiedi aumento paghetta
                if (Studio > 40) {
                    if (((Studio - Paghetta + Fortuna) > (75 + random(50))) && (Paghetta < 96)) {
                        tmp = "Va bene... ti daremo ${MostraSoldi(5)} di paghetta in piu'..."
                        MessageBox(hDlg, tmp, "Aumento paghetta !", MB_OK or MB_ICONINFORMATION)
                        Paghetta += 5
                        Evento(hDlg)
                    } else { ... }
                } else { ... }
                SetDlgItemText(hDlg, 105, MostraSoldi(Paghetta))
                return true
            }
            103 -> { // Papa, mi dai 100000 lire ?
                if (sound_active != 0) TabbozPlaySound(801)
                MessageBox(hDlg, "Non pensarci neanche lontanamente...", "Errore irrecuperabile", MB_OK or MB_ICONHAND)
                Evento(hDlg)
                return true
            }
            IDOK, IDCANCEL -> { EndDialog(hDlg, 1); return true }
            else -> return true
        }
    }
    return false
}
```

Nota: in Kotlin `if (sound_active)` non compila perché è `Int`: usare `if (sound_active != 0)`. Idem per
`timer_active`, `euro`, `firsttime`, `TabbozRedraw`, `ScuolaRedraw`, `showscooter`, `debug_active`, `natale2`,
`Risposte1[i]` (`if (Risposte1[i] != 0)`, `Risposte1[i] = if (Risposte1[i] != 0) 0 else 1`).

## Controlli custom (solo TabImg.kt)

`tabimg.c` disegna l'immagine del tabbozzo (classe "BMPView", controllo 103 del dialogo principale) e della tipa
(classe "BMPTipa", controllo 130 del dialogo Tipa). Su Android il disegno è delegato alla UI: `TabImg.kt` deve solo
registrare, in `RegisterBMPViewClass()` / `RegisterBMPTipaClass()`, la lista dei livelli bitmap da disegnare:

```kotlin
CustomControls.register("BMPView") { dlg ->
    // stesso ordine e stesse coordinate di WMPaint: sfondo, pantaloni, scarpe, giubbotto, testa
    // (IMG_X_INC = 10, IMG_Y_INC = 0; bitmap: 1560/1561 sfondo, 1520+/1525+ pantaloni, 1540+ scarpe, 1500+/1507+ giubbotto, 1390+/1370+ testa)
    listOf(BitmapLayer(1560, 0, 0), BitmapLayer(current_pantaloni + 1520, 18 + 10, 93 + 0), ...)
}
CustomControls.register("BMPTipa") { dlg -> listOf(BitmapLayer(current_tipa + 1204, 0, 0)) }
```
`BMPViewWndProc`/`BMPTipaWndProc` gestiscono solo `WM_LBUTTONDOWN` (click) come nel C (`LOWORD(lParam)` = x, `HIWORD(lParam)` = y).
`TabbozRedraw = 1` deve restare come nel C; in più, in `AggiornaPrincipale`, chiamare `InvalidateRect(parent)` per far ridisegnare l'immagine.
