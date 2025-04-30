package com.example.languagecompose

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel(private val context: Context) : ViewModel() {

    private val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _language = MutableLiveData<String>().apply {
        value = getSavedLanguage()
    }
    val language: LiveData<String> = _language

    fun getSavedLanguage(): String {
        return sharedPreferences.getString("language", "English") ?: "English"
    }

    /*
        La razón por la que se utiliza viewModelScope.launch en la función setLanguage es para asegurar
        que las operaciones que pueden ser potencialmente bloqueantes (como la edición de SharedPreferences)
        se realicen en un contexto de coroutine, permitiendo que la UI permanezca receptiva.
    */
    fun setLanguage(newLanguage: String) {
        _language.value = newLanguage // Actualiza el LiveData
        sharedPreferences.edit().putString("language", newLanguage).apply() // Actualiza el LiveData
    }


    init {
        Log.d( "LanguageViewModel", "_language: ${_language.value}")
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    Column (
         verticalArrangement = Arrangement.Top
        , horizontalAlignment = Alignment.CenterHorizontally
        ,modifier = Modifier
            .fillMaxWidth(0.7f)
            .padding(16.dp)
    ){
        LanguageSettings(viewModel)
    }
}

@Composable
fun LanguageSettings(viewModel: SettingsViewModel) {

    var expanded by remember { mutableStateOf(false) }
    val selectedLanguage by viewModel.language.observeAsState(viewModel.getSavedLanguage())
    val context = LocalContext.current

    val languages = listOf("en", "es", "fr", "de")  // Usar códigos de idioma estándar
    val mapCode: (String) -> Int = { code ->
        when (code) {
            "en" -> R.string.english
            "es" -> R.string.spanish
            "fr" -> R.string.french
            "de" -> R.string.german
            else -> R.string.english // En caso de que el código no coincida
        }
    }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {

        Text(
            text = stringResource(id = R.string.select_lang),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.Center
        ){
            Text(text = stringResource(id = R.string.selected_lang) + ": ",
                color = MaterialTheme.colorScheme.onBackground
            )

            // Botón para expandir el menú
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            )
            {
                DropDownButton(stringResource(id = mapCode(selectedLanguage))) { expanded = !expanded }

                // DropdownMenu para seleccionar el espacio de color
                DropdownMenu(
                    offset = DpOffset(50.dp, 0.dp), // Ajustar
                    expanded = expanded, // Usar el estado para controlar la expansión
                    onDismissRequest = { expanded = false }, // Cerrar el menú al hacer clic fuera de él
                    modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    languages.forEach{ language ->
                        DropdownMenuItem(
                            text = { Text(stringResource(id = mapCode(language))
                                , color = MaterialTheme.colorScheme.onSecondaryContainer
                            ) },
                            colors = DropdownItemColors(),
                            onClick = {
                                viewModel.setLanguage(language)
                                // Aplicar el idioma y recrear la actividad
                                App.setLocale(context, language)
                                expanded = false // Cerrar el menú después de seleccionar
                                //(context as? Activity)?.recreate() // Recrear la actividad
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DropDownButton(text: String, onClick: () -> Unit){

        // Botón estilo "TextButton" para integrarlo visualmente
        TextButton(
            onClick = onClick // Cambiar el estado de expansión
            , elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = -2.dp
            )
            , shape = MaterialTheme.shapes.small
            , contentPadding = PaddingValues(2.dp)
            , colors = ButtonDefaults.textButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
            //  , modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Text(
                text = text
                , color = MaterialTheme.colorScheme.onSecondaryContainer
                , style = MaterialTheme.typography.bodyLarge
                , modifier = Modifier.padding(4.dp)
            )
        }
}

@Composable
fun DropdownItemColors() :MenuItemColors {
    return MenuItemColors(
        textColor = MaterialTheme.colorScheme.onSecondaryContainer,
        leadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
        trailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
        disabledTextColor = MaterialTheme.colorScheme.surfaceDim,
        disabledLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
        disabledTrailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
    )
}