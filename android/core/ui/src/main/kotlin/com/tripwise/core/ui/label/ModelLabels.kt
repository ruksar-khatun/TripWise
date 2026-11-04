package com.tripwise.core.ui.label

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.model.ActivityCategory
import com.tripwise.core.model.ExpenseCategory
import com.tripwise.core.model.TravelInterest
import com.tripwise.core.ui.R

/** Display name + icon for model enums, so every screen labels them identically. */
data class EnumUi(@StringRes val label: Int, val icon: ImageVector)

val TravelInterest.ui: EnumUi
    get() = when (this) {
        TravelInterest.BEACHES -> EnumUi(R.string.core_ui_interest_beaches, TwIcons.Beach)
        TravelInterest.CULTURE -> EnumUi(R.string.core_ui_interest_culture, TwIcons.Culture)
        TravelInterest.FOOD -> EnumUi(R.string.core_ui_interest_food, TwIcons.Food)
        TravelInterest.NATURE -> EnumUi(R.string.core_ui_interest_nature, TwIcons.Nature)
        TravelInterest.MOUNTAINS -> EnumUi(R.string.core_ui_interest_mountains, TwIcons.Mountains)
        TravelInterest.CITY -> EnumUi(R.string.core_ui_interest_city, TwIcons.City)
        TravelInterest.ADVENTURE -> EnumUi(R.string.core_ui_interest_adventure, TwIcons.Adventure)
    }

val ActivityCategory.ui: EnumUi
    get() = when (this) {
        ActivityCategory.SIGHTSEEING -> EnumUi(R.string.core_ui_activity_sightseeing, TwIcons.Place)
        ActivityCategory.FOOD -> EnumUi(R.string.core_ui_activity_food, TwIcons.Food)
        ActivityCategory.ACTIVITY -> EnumUi(R.string.core_ui_activity_activity, TwIcons.Activity)
        ActivityCategory.TRANSPORT -> EnumUi(R.string.core_ui_activity_transport, TwIcons.Transport)
        ActivityCategory.LODGING -> EnumUi(R.string.core_ui_activity_lodging, TwIcons.Lodging)
        ActivityCategory.SHOPPING -> EnumUi(R.string.core_ui_activity_shopping, TwIcons.Shopping)
        ActivityCategory.OTHER -> EnumUi(R.string.core_ui_activity_other, TwIcons.Notes)
    }

val ExpenseCategory.ui: EnumUi
    get() = when (this) {
        ExpenseCategory.TRANSPORT -> EnumUi(R.string.core_ui_expense_transport, TwIcons.Transport)
        ExpenseCategory.ACCOMMODATION -> EnumUi(R.string.core_ui_expense_accommodation, TwIcons.Lodging)
        ExpenseCategory.FOOD -> EnumUi(R.string.core_ui_expense_food, TwIcons.Food)
        ExpenseCategory.ACTIVITIES -> EnumUi(R.string.core_ui_expense_activities, TwIcons.Activity)
        ExpenseCategory.LOCAL_TRAVEL -> EnumUi(R.string.core_ui_expense_local_travel, TwIcons.Place)
        ExpenseCategory.SHOPPING -> EnumUi(R.string.core_ui_expense_shopping, TwIcons.Shopping)
        ExpenseCategory.OTHER -> EnumUi(R.string.core_ui_expense_other, TwIcons.Notes)
    }
