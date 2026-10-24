package agency.lacosta.venceya.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import agency.lacosta.venceya.AppContainer
import agency.lacosta.venceya.VenceYaApp

val CreationExtras.container: AppContainer
    get() = (this[APPLICATION_KEY] as VenceYaApp).container
