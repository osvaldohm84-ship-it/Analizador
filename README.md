# Analizador de Lotería – Android

Proyecto Android nativo en **Java**, sin Power Query y sin depender de Excel/VBA.

## Funciones
- Historial local SQLite para sorteos anteriores y futuros.
- Importación de TXT/CSV/TSV.
- Importación básica de `.xlsx` (OOXML) sin biblioteca externa.
- Importación básica de `.docx` cuando el contenido se puede interpretar como filas de 6 campos.
- Alta manual de sorteos futuros.
- Cálculo de Tops, Decenas y Terminales por rango.
- Exportación del historial a CSV.
- Historial inicial basado en `Florida_inicial.tsv`.

## Compilación
Este proyecto se compila automáticamente con GitHub Actions.
Al subir los archivos, el workflow `.github/workflows/android.yml` ejecuta Gradle y guarda el APK como artifact descargable.

Ver **Actions > Android Build** en el repositorio para descargar el APK.

## Compatibilidad
- minSdk 23 (Android 6.0).
- targetSdk 35.
- Proyecto pensado para Android Studio moderno con Android Gradle Plugin 8.x.
