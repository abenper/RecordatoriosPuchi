package com.example.recordatoriosdepuchi

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.recordatoriosdepuchi.ui.AdminScreen
import com.example.recordatoriosdepuchi.ui.HomeScreen
import com.example.recordatoriosdepuchi.ui.viewmodel.HomeViewModel
import com.example.recordatoriosdepuchi.ui.viewmodel.HomeViewModelFactory
import com.example.recordatoriosdepuchi.utils.ReminderScheduler

/**
 * Punto de entrada principal de la aplicación.
 *
 * Esta actividad configura el entorno inmersivo para simplificar la experiencia de usuario
 * (accesibilidad cognitiva) y gestiona los permisos críticos de telefonía.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // CONFIGURACIÓN DE ACCESIBILIDAD Y UI:
        // Mantenemos la pantalla encendida para que la persona mayor no tenga que desbloquear
        // constantemente y ocultamos las barras del sistema para evitar pulsaciones accidentales.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemBars()

        // GESTIÓN DE PERMISOS DE TELEFONÍA:
        // Solicitamos ser la aplicación de teléfono predeterminada para poder filtrar
        // llamadas no deseadas (Spam) y simplificar la interfaz de llamada.
        checkDefaultDialer()

        // INICIALIZACIÓN DE SERVICIOS:
        // El Scheduler se encarga de las alarmas exactas para los recordatorios de medicación/citas.
        val scheduler = ReminderScheduler(applicationContext)

        // ARQUITECTURA MVVM:
        // Instanciamos el ViewModel usando una Factory personalizada para inyectar dependencias (Repo y Scheduler).
        val viewModel: HomeViewModel by viewModels {
            HomeViewModelFactory(
                application,
                (application as PuchiApplication).repository,
                scheduler
            )
        }

        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                AppNavigation(viewModel)
            }
        }
    }

    /**
     * Oculta las barras de navegación y estado para un modo "Kiosco" o inmersivo.
     * Facilita la visión y el uso para personas con dificultades tecnológicas.
     */
    private fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    /**
     * Comprueba y solicita al usuario que establezca esta App como el marcador (Dialer) predeterminado.
     * Necesario para Android 10+ (RoleManager) para interceptar y gestionar llamadas.
     */
    private fun checkDefaultDialer() {
        val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        if (packageName != telecomManager.defaultDialerPackage) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
                if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                    startActivityForResult(intent, 123)
                    return
                }
            }
            // Fallback para versiones anteriores
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
            intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
            startActivityForResult(intent, 123)
        }
    }
}

/**
 * Grafo de navegación de la aplicación.
 * Define las rutas entre la pantalla principal (Usuario) y el panel de administración (Tutor/Familiar).
 */
@Composable
fun AppNavigation(viewModel: HomeViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToAdmin = { navController.navigate("admin") }
            )
        }
        composable("admin") {
            AdminScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}