package com.siva.cleanmsg.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.siva.cleanmsg.app.ui.addeditcategory.AddEditCategoryScreen
import com.siva.cleanmsg.app.ui.categorylist.CategoryListScreen
import com.siva.cleanmsg.app.ui.dashboard.DashboardScreen
import com.siva.cleanmsg.app.ui.managerules.ManageRulesScreen
import com.siva.cleanmsg.app.ui.messagedetail.MessageDetailScreen
import com.siva.cleanmsg.app.ui.onboarding.OnboardingScreen
import com.siva.cleanmsg.app.ui.settings.SettingsScreen
import com.siva.cleanmsg.app.ui.theme.CleanMsgTheme
import com.siva.cleanmsg.app.util.SmsRoleUtils

private object Routes {
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val MANAGE_RULES = "manage_rules"
    const val SETTINGS = "settings"
    const val CATEGORY_KEY_ARG = "categoryKey"
    const val CATEGORY_LIST = "category_list/{$CATEGORY_KEY_ARG}"
    const val ADD_EDIT_CATEGORY = "add_edit_category?categoryKey={$CATEGORY_KEY_ARG}"
    const val SMS_ID_ARG = "smsId"
    const val MESSAGE_DETAIL = "message_detail/{$SMS_ID_ARG}"

    fun categoryList(categoryKey: String) = "category_list/$categoryKey"
    fun messageDetail(smsId: Long) = "message_detail/$smsId"
    fun addCategory() = "add_edit_category"
    fun editCategory(categoryKey: String) = "add_edit_category?categoryKey=$categoryKey"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val isOnboardingComplete = SmsRoleUtils.hasReadSmsPermission(this) && SmsRoleUtils.isDefaultSmsApp(this)
        setContent {
            CleanMsgTheme {
                CleanMsgApp(startDestination = if (isOnboardingComplete) Routes.DASHBOARD else Routes.ONBOARDING)
            }
        }
    }
}

@Composable
private fun CleanMsgApp(startDestination: String) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onCategoryClick = { categoryKey -> navController.navigate(Routes.categoryList(categoryKey)) },
                onAddCategoryClick = { navController.navigate(Routes.addCategory()) },
                onEditCategoryClick = { categoryKey -> navController.navigate(Routes.editCategory(categoryKey)) },
                onManageRulesClick = { navController.navigate(Routes.MANAGE_RULES) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(
            route = Routes.CATEGORY_LIST,
            arguments = listOf(navArgument(Routes.CATEGORY_KEY_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryKey = backStackEntry.arguments?.getString(Routes.CATEGORY_KEY_ARG).orEmpty()
            CategoryListScreen(
                categoryKey = categoryKey,
                onBack = { navController.popBackStack() },
                onMessageClick = { smsId -> navController.navigate(Routes.messageDetail(smsId)) }
            )
        }
        composable(
            route = Routes.MESSAGE_DETAIL,
            arguments = listOf(navArgument(Routes.SMS_ID_ARG) { type = NavType.LongType })
        ) { backStackEntry ->
            val smsId = backStackEntry.arguments?.getLong(Routes.SMS_ID_ARG) ?: 0L
            MessageDetailScreen(smsId = smsId, onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.ADD_EDIT_CATEGORY,
            arguments = listOf(
                navArgument(Routes.CATEGORY_KEY_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val categoryKey = backStackEntry.arguments?.getString(Routes.CATEGORY_KEY_ARG)
            AddEditCategoryScreen(categoryKey = categoryKey, onDone = { navController.popBackStack() })
        }
        composable(Routes.MANAGE_RULES) {
            ManageRulesScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
