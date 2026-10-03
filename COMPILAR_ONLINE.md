# Compilar online

La opción incluida y reproducible es **GitHub Actions**.

1. Crea un repositorio nuevo en GitHub.
2. Sube todos los archivos de esta carpeta, incluyendo `.github/workflows/android.yml`.
3. Ve a **Actions**.
4. Selecciona **Android Build**.
5. Pulsa **Run workflow**.
6. Espera a que termine el job.
7. En **Artifacts**, descarga `analizador-loteria-debug-apk`.
8. Descomprime el artifact y tendrás `app-debug.apk`.

No necesitas instalar Android Studio para esa compilación online. El workflow instala Gradle y usa el Android Gradle Plugin definido por el proyecto.
