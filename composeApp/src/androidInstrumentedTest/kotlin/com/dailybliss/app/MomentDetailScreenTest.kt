package com.dailybliss.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.dailybliss.app.domain.model.Moment
import com.dailybliss.app.presentation.screens.detail.MomentDetailScreenContent
import com.dailybliss.app.presentation.screens.detail.MomentDetailUiState
import com.dailybliss.app.presentation.theme.DailyBlissTheme
import org.junit.Rule
import org.junit.Test

class MomentDetailScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun momentDetailScreen_displaysMomentInfo() {
        val moment = Moment(title = "Detail Title", content = "Detail Content")

        composeTestRule.setContent {
            DailyBlissTheme {
                MomentDetailScreenContent(
                    uiState = MomentDetailUiState(moment = moment),
                    onTitleChange = {},
                    onContentChange = {},
                    onAddImage = { _, _ -> },
                    onSaveChanges = {},
                    onDeleteMoment = {},
                    onConfirmDelete = {},
                    onNavigateBack = {},
                    showDeleteDialog = false,
                    onDismissDeleteDialog = {},
                    focusedValue = null,
                    onFocusedValueChange = {},
                    updateFocusedValue = null,
                    onUpdateFocusedValueChange = {},
                    targetOffset = -1,
                    onTargetOffsetReset = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Detail Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Detail Content").assertIsDisplayed()
    }
}
