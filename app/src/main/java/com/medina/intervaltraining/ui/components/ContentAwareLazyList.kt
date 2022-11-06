package com.medina.intervaltraining.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ContentAwareLazyColumn(
    lazyColumnBody: LazyListScope.() -> Unit,
    footerContent:  @Composable() (ColumnScope.() -> Unit) = {},
    showFooter: Boolean = false,
){
    Column() {
        val state = rememberLazyListState()
        val showScrollInfoTop by remember {
            derivedStateOf {
                state.layoutInfo.visibleItemsInfo.size < state.layoutInfo.totalItemsCount &&
                        state.firstVisibleItemIndex != 0
            }
        }
        val showScrollInfoBottom by remember {
            derivedStateOf {
                state.layoutInfo.visibleItemsInfo.size < state.layoutInfo.totalItemsCount &&
                        state.firstVisibleItemIndex < state.layoutInfo.totalItemsCount - state.layoutInfo.visibleItemsInfo.size
            }
        }
        if(showScrollInfoTop){
            Icon(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 24.dp),
                imageVector = Icons.Default.ExpandLess,
                contentDescription = "#Less"
            )
        }
        LazyColumn(
            Modifier.weight(1f,false),
            state = state,
            content = lazyColumnBody
        )
        if(showScrollInfoBottom){
            Icon(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 24.dp),
                imageVector = Icons.Default.ExpandMore,
                contentDescription = "#More"
            )
        }
        if (showFooter) {
            footerContent()
        }
    }
}
