package com.benyaamin.rakhsh.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun CustomIconButton(@DrawableRes id: Int, onClick: () -> Unit) {
    IconButton(
        modifier = Modifier
            .size(24.dp)
            .padding(0.dp),
        onClick = {
            onClick()
        },
        content = {
            Icon(
                painter = painterResource(id),
                contentDescription = "Start Download"
            )
        }
    )
}