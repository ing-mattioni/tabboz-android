# Tabboz Simulator per Android

Porting nativo Android (Kotlin + Jetpack Compose) di **Tabboz Simulator**, il gioco per Windows 9x
di Andrea Bonomi, Emanuele Caccialanza e Daniele Gazzarri (1997-2001), rilasciato sotto GPL 3:
<https://github.com/andreax79/tabboz>.

L'app riproduce fedelmente il gioco originale: stesse finestre (lette dal file di risorse `dialogs.rc`
originale), stesse immagini, stessi testi, stessi suoni e stessa logica di gioco (tradotta riga per riga
dal C a Kotlin). Le finestre vengono ridimensionate automaticamente per adattarsi allo schermo del telefono.

## Struttura del progetto

```
android/
  app/src/main/java/com/tabboz/simulator/
    MainActivity.kt          Activity + avvio della coroutine di gioco (WinMain)
    TabbozApplication.kt
    win/                     Runtime "Win32-like": DialogBox/MessageBox sospendibili, stato dei
                             controlli, template dei dialoghi, suoni, preferenze (registro)
    ui/                      Renderer Compose in stile Windows 98 / Borland BWCC
                             (finestre, pulsanti, radio, check, edit, menu, message box, bitmap)
    game/                    Logica di gioco tradotta dal C (un file Kotlin per file .c)
      GameState.kt           Variabili globali e tabelle dati (zarrosim.h e i vari .c)
      Zarrosim.kt Tempo.kt Eventi.kt Scooter.kt Scuola.kt Tipa.kt Disco.kt
      Vestiti.kt Lavoro.kt Telefono.kt TabImg.kt
  app/src/main/assets/       Risorse generate dall'originale (dialoghi JSON, bitmap, icone, suoni, stringhe, menu)
  app/src/main/res/font/     Font "MS Sans Serif" pixel (da 98.css)
  tools/convert_resources.py Script che genera gli asset a partire da ../tabboz-main/resources
  docs/PORTING_GUIDE.md      Regole usate per la traduzione C -> Kotlin
```

## Come si compila

Requisiti: JDK 17+ (va bene quello incluso in Android Studio), Android SDK con platform 36 e build-tools 35.

```bash
# Windows (PowerShell)
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug        # APK di debug
.\gradlew.bat assembleRelease      # APK release (firmato con la chiave di debug)
```

Gli APK vengono generati in `app/build/outputs/apk/`. Installazione su un dispositivo collegato:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

In alternativa aprire la cartella `android` con Android Studio.

## Rigenerare gli asset

Gli asset in `app/src/main/assets` sono gia' inclusi nel repository. Per rigenerarli servono i sorgenti
originali (<https://github.com/andreax79/tabboz>) in una cartella `tabboz-main` accanto a questa
(oppure indicare il percorso con `--src`):

```bash
pip install pillow
python tools/convert_resources.py --src ../tabboz-main
```

## Note sul porting

- La logica di gioco gira in una coroutine sul main thread; `DialogBox` e `MessageBox` sospendono
  la coroutine finche' l'utente non chiude la finestra, esattamente come le chiamate bloccanti Win32.
- Il salvataggio usa le `SharedPreferences` con le stesse chiavi del registro di Windows originale
  (`Soldi`, `Paghetta`, `Scooter\Speed`, ...). La partita viene salvata ad ogni evento e quando l'app va in background.
- Il tasto "indietro" di Android equivale al pulsante di chiusura della finestra in primo piano.
- La funzione "Apri/Salva con nome" (file .tbz) non e' disponibile, come nella versione web.
- Il pulsante nascosto "Reset" della finestra Configuration e' stato reso visibile per poter iniziare
  una nuova partita (scelta del sesso del tabbozzo).

## APK

Una build pronta (firmata con la chiave di debug, quindi installabile ma non pubblicabile sul Play Store)
e' in `dist/TabbozSimulator-release.apk`.

## Licenza

GPL 3, come il gioco originale. Il renderer Windows 98 e' ispirato a [novantotto](https://github.com/andreax79/novantotto)
e [98.css](https://github.com/jdan/98.css).
