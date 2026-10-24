# VenceYa

App Android para no olvidar lo que vence: suscripciones, pagos fijos, documentos, garantías y seguros. Avisa unos días antes y el mismo día. Todo se guarda en el teléfono.

Hecho por lacosta.agency.

## Probar en el teléfono

Cada cambio en `main` compila un APK nuevo y lo deja en
[Última versión de prueba](../../releases/tag/prueba). Ábrelo desde el teléfono, baja `VenceYa-prueba.apk` e instálalo.

## Código

Kotlin + Jetpack Compose, Room para los datos y WorkManager para la revisión diaria de avisos (9:00). La lógica de fechas está en `domain/` con pruebas en `app/src/test`.
