package com.example.languagecompose

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

/*
 App es una clase que extiende de Application, y es el punto de entrada global de tu aplicación.

 La clase Application se utiliza para mantener un contexto global en toda la aplicación.
 crear esta clase y configurarla en tu AndroidManifest.xml, se asegura que cualquier configuración global (como el idioma)
 se aplique desde el inicio, afectando a todas las actividades y componentes de la app.

 El método onCreate() se llama cuando la aplicación se inicializa por primera vez,
 antes de que cualquier actividad o servicio sea creado.

 En este método, llamas a applyLanguage(this) para aplicar el idioma que el usuario haya seleccionado previamente,
 asegurando que la interfaz siempre se muestre en el idioma correcto al iniciar la aplicación.

 Base class for maintaining global application state. You can provide your own implementation by creating a subclass and specifying
 the fully-qualified name of this subclass as the "android:name" attribute in your AndroidManifest.xml's <application> tag.
  The Application class, or your subclass of the Application class, is instantiated before any other class when the process
  for your application/package is created.

*/
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // Aplica el idioma guardado al iniciar la aplicación
        applyLanguage(this)
    }


    companion object {
        
        /*
         * applyLanguage(context) es una función que se asegura de aplicar el idioma guardado en las preferencias
         * de la aplicación al contexto que se le pasa. En este caso, se llama desde onCreate() con el contexto global (this).
         */
        fun applyLanguage(context: Context): Context {
            val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val language = sharedPreferences.getString("language", "en") ?: "en"
            Log.d("App", language)

            return setLocale(context, language)
        }


        // Actualiza la configuración del idioma
        fun setLocale(context: Context, language: String): Context {
            val locale = Locale(language)
            Locale.setDefault(locale)

            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)

            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context.createConfigurationContext(config)
            return context
        }

        /*
        * applyTheme(context) gestiona la aplicación del tema claro u oscuro basado en las preferencias del usuario.
        */
        fun getThemePreference(context: Context) : Boolean {
            val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val isDarkTheme = sharedPreferences.getBoolean("dark_theme", false) // Tema claro por defecto
            Log.d("App", "Tema oscuro activado?: $isDarkTheme")
            return isDarkTheme
        }
    }
}
