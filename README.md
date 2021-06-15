# Omni-Notes

## Zrealizowane funkcjonalności aplikacji mobilnej

### Uwierzytelnianie poprzez konto Google

Użytkownik w trakcie użytkowania aplikacji może włączyć możliwość synchronizacji wybranych notatek z chmurą. Wymaga to
zalogowania się do konta google.


### Synchronizacja notatek z chmurą

Notatki synchronizowane w trakcie odczytu ich treści są pobierane z chmury, wraz z zawartością notatki pobierane są
również załączniki. Podczas zapisu notatki, jej treść jest zapisywana na dysku w chmurze, wszystkie nowe oraz zmienione
załączniki są ponownie przekazywane do chmury. Zmiana załącznika wykrywana jest poprzez sprawdzenie sygnatury pliku


## Użytkowanie bazowej wersji projektu.

Podstawowe informacje bazowej wersji aplikacji dostępne są w oficjalnej dokumentacji OmniNotes [LINK](https://github.com/federicoiosue/Omni-Notes/wiki). Nasz projekt został rozszerzony o funkcjonalność synchronizacji, i instrukcja jej użytkowania przedstawiona jest w poniższej dokumentacji.

## Przygotowanie środowiska przed uruchomieniem projektu

Aby funkcjonalność synchronizacji była dostępna, aplikacja musi być zarejestrowana w jednym z projektów Google cloud. Żeby to zrobić, należy wpierw  wygenerować jej hash SHA-1. Z poziomu Android Studio należy uruchomić zadanie "android -> signingReport", który wyświetli wyliczony skrót SHA w terminalu. Następnie należy podążać za instrukcjami przedstawionymi w oficjalnej dokumentacji [google identity](https://developers.google.com/identity/sign-in/android/start-integrating) i sparować aplikację z projektem Google Cloud z wykorzystaniem otrzymanego skrótu. Należy pamiętać że identyfikatorem aplikacji w trybie developerskim jest it.feio.android.omninotes.alpha, jak również o przydzieleniu uprawnienia DRIVE.FILE aplikacji pod postacią scope'u w projekcie na platformie Google Cloud. 

## Użytkowanie funkcjonalności synchronizacji
Otwierając menu boczne w aplikacji, przechodzimy do zakładki **Settings** a następnie **Enable Sync**. Po włączeniu dostępnej opcji synchronizacji, zostaniemy poproszeni o zalogowanie się przez konto Google, oraz o przyznanie uprawnień aplikacji do dysku google. Od teraz notatki będą automatycznie synchronizowane z dyskiem Google.

![usage](https://i.imgur.com/HBwOe9M.png)

## Kompilacja projektu

Kompilacja finalnego pliku .apk zaprezentowana jest w [oficjalnej dokumentacji](https://github.com/federicoiosue/Omni-Notes#build):
[![asciicast](https://asciinema.org/a/102898.png)](https://asciinema.org/a/102898)

