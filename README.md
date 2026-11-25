# Naktinis Laikrodis (Digital Night Clock)

Minimalistinė, pilno ekrano skaitmeninio laikrodžio „Android“ programėlė, skirta naudoti ant naktinio staliuko ar darbo stalo. Sukurta naudojant **Kotlin** ir **Jetpack Compose** su grynai juodu (`#000000`) AMOLED fonu, automatiniu naktiniu pritemdymu ir apsauga nuo ekrano išdegimo.

---

## Pagrindinės funkcijos

- **Skaitmenys per visą ekraną**: Laikas automatiškai prisitaiko prie ekrano pločio ir užpildo maksimalų horizontalų plotą (`5dp` paraštės arba `10dp`, kai įjungta OLED apsauga).
- **Visiškai juodas AMOLED fonas**: Gryna `#000000` spalva neakina tamsoje ir taupo energiją OLED ekranuose.
- **Ekranas visada įjungtas**: Aktyvuotas `FLAG_KEEP_SCREEN_ON`, todėl ekranas neužgęsta, kol programėlė atidaryta (galima išjungti nustatymuose).
- **Švarus vaizdas be trukdžių**:
  - Sistemos būsenos ir navigacijos juostos paslėptos (*Immersive Mode*).
  - **Baterijos indikatorius** (`27dp × 13.5dp`, `18sp`) apačioje dešinėje ir **naktinio režimo ženklelis** viršuje rodomi **tik palietus ekraną** ir automatiškai išnyksta po 4 sekundžių neaktyvumo.
- **Spalvų pritaikymas**:
  - 11 iš anksto paruoštų spalvų (įskaitant `#FC2407`, neoninę žalią, kibernetinę žydrą, gintarinę, baltą ir kt.).
  - **Pasirinktinė HEX spalva**: Galimybė įvesti bet kokį `#RRGGBB` kodą su gyva spalvos peržiūra.
- **7 skaitmenų šriftai**:
  - `Orbitron` (Sci-Fi / LED)
  - `Share Tech Mono` (terminalo)
  - `VT323` (retro taškinis LED)
  - `Roboto Mono`
  - Sistemos `Monospace`, `Sans-Serif` ir `Serif`
- **Automatinis ir rankinis naktinis režimas**:
  - Suplanuotas naktinis grafikas (numatyta `22:00 – 07:00`, galima keisti į `23:00 – 06:00` arba `21:00 – 08:00`).
  - Atskiri fizinio ekrano ryškumo slankikliai dienai (`20%–100%`) ir nakčiai (`1%–30%`).
  - **Raudona naktinė šviesa**: Pasirinktinis švelniai raudonas atspalvis naktį, saugantis akis tamsoje.
- **OLED apsauga nuo išdegimo**: Kas minutę skaitmenys nežymiai pasislenka keliais pikseliais.
- **Lankstūs rodymo nustatymai**: `24 val.` / `12 val.` formatas, sekundžių ir datos rodymas, ekrano orientacijos užrakinimas arba automatinis horizontalus pasukimas.

---

## Technologijos

- **Kalba**: Kotlin (`2.2.10`)
- **UI**: Jetpack Compose & Material Design 3
- **Architektūra**: MVVM (`ClockViewModel`, `StateFlow`)
- **Duomenų saugojimas**: `SharedPreferences` (`ClockSettingsRepository`)
- **Sistemos integracija**: `BroadcastReceiver` (`ACTION_BATTERY_CHANGED`), `WindowInsetsControllerCompat`, `WindowManager.LayoutParams`
- **Testavimas**: JUnit 4 + Robolectric (`4.16.1`)
- **SDK**: `minSdk 24` · `targetSdk 36` · `compileSdk 36`

---

## Projekto struktūra

```text
app/src/main/java/com/example/
├── MainActivity.kt                  # Pagrindinis aktyvumas, fullscreen režimas, FLAG_KEEP_SCREEN_ON
├── data/
│   └── ClockSettingsRepository.kt   # Nustatymų išsaugojimas į SharedPreferences
├── model/
│   └── ClockSettings.kt             # Duomenų modeliai, spalvų ir šriftų sąrašai
├── receiver/
│   └── BatteryMonitor.kt            # Baterijos lygio ir įkrovimo būsenos srautas (Flow)
└── ui/
    ├── ClockScreen.kt               # Pagrindinis ekranas, ryškumo ir valdiklių valdymas
    ├── ClockViewModel.kt            # Laikrodžio tiksėjimas, naktinis režimas, pikselių poslinkis
    ├── components/
    │   ├── BatteryIndicator.kt      # Baterijos piktograma ir procentų indikatorius
    │   ├── DigitalClockDisplay.kt   # Dinamiškai prie ekrano prisitaikantis laikrodžio tekstas
    │   └── SettingsSheet.kt         # Nustatymų langas (spalvos, šriftai, naktinis režimas)
    └── theme/
        ├── ClockFonts.kt            # Vietiniai Google Fonts šriftai iš res/font/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

---

## Kompiliavimas ir testavimas

### Reikalavimai
- JDK 11+
- Android SDK (API 36)
- Gradle

### Komandos

Sukompiliuoti debug APK:
```bash
gradle :app:assembleDebug
```
Sugeneruotas APK failas bus išsaugotas adresu:
`app/build/outputs/apk/debug/app-debug.apk`

Paleisti vietinius vienetų (unit) ir Robolectric testus:
```bash
gradle :app:testDebugUnitTest
```
