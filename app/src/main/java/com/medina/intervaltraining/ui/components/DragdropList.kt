package com.medina.intervaltraining.ui.components


import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.medina.intervaltraining.ui.theme.IntervalTrainingTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun rememberDragDropState(
    lazyListState: LazyListState,
    onMove: (Int, Int) -> Unit = { _, _ -> },
    onHover: (Int, Int, Float) -> Unit = { _, _, _ -> }
): DragDropState {
    val scope = rememberCoroutineScope()
    val state = remember(lazyListState) {
        DragDropState(
            state = lazyListState,
            onMove = onMove,
            onHover = onHover,
            scope = scope
        )
    }
    return state
}

fun LazyListState.getVisibleItemInfoFor(absoluteIndex: Int): LazyListItemInfo? {
    return this
        .layoutInfo
        .visibleItemsInfo
        .getOrNull(absoluteIndex - this.layoutInfo.visibleItemsInfo.first().index)
}

val LazyListItemInfo.offsetEnd: Int
    get() = this.offset + this.size

@ExperimentalFoundationApi
@Composable
fun LazyItemScope.DraggableItem(
    dragDropState: DragDropState,
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable (isDragging: Boolean) -> Unit
) {
    val draggedOffset: Float by animateFloatAsState(dragDropState.draggingItemOffset)
    val draggedItemSize: Float by animateFloatAsState(dragDropState.draggedItemSize)
    val dragging = index == dragDropState.currentIndexOfDraggedItem
    val currentDraggedIndex = dragDropState.currentIndexOfDraggedItem ?: -1
    val currentHoveredIndex = dragDropState.currentIndexOfHoveredItem ?: -1
    val draggingModifier = if (dragging) {
        Modifier
            .zIndex(1f)
            .graphicsLayer {
                translationY = draggedOffset
                alpha = 0.5f
                clip = false
            }
    } else if (currentHoveredIndex<0) {
        Modifier.animateItemPlacement()
    } else if (index < currentDraggedIndex && index >= currentHoveredIndex) {
        Modifier
            .graphicsLayer {
                translationY = draggedItemSize*0.7f
            }
    } else if (index > currentDraggedIndex && index <= currentHoveredIndex) {
        Modifier
            .graphicsLayer {
                translationY = -draggedItemSize*0.7f
            }
    } else {
        Modifier.animateItemPlacement()
    }
    Surface(
        modifier = modifier.then(draggingModifier)
    ) {
        content(dragging)
    }
}


class DragDropState internal constructor(
    val state: LazyListState,
    private val scope: CoroutineScope,
    private val onMove: (Int, Int) -> Unit,
    private val onHover: (Int, Int, Float) -> Unit,
) {
    private var draggedDistance by mutableStateOf(0f)
    private var draggingItemInitialOffset by mutableStateOf(0)
    internal val draggingItemOffset: Float
        get() = draggingItemLayoutInfo?.let { item ->
            draggingItemInitialOffset + draggedDistance - item.offset
        } ?: 0f

    internal val draggedItemSize: Float
        get() = draggingItemLayoutInfo?.let { item -> (item.size).toFloat()
        } ?: 0f

    private val draggingItemLayoutInfo: LazyListItemInfo?
        get() = state.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == currentIndexOfDraggedItem }

    internal var previousIndexOfDraggedItem by mutableStateOf<Int?>(null)
        private set

    // used to obtain initial offsets on drag start
    private var initiallyDraggedElement by mutableStateOf<LazyListItemInfo?>(null)

    var currentIndexOfDraggedItem by mutableStateOf<Int?>(null)
    var currentIndexOfHoveredItem by mutableStateOf<Int?>(null)

    private val initialOffsets: Pair<Int, Int>?
        get() = initiallyDraggedElement?.let { Pair(it.offset, it.offsetEnd) }

    private val currentElement: LazyListItemInfo?
        get() = currentIndexOfDraggedItem?.let {
            state.getVisibleItemInfoFor(absoluteIndex = it)
        }


    fun onDragStart(offset: Offset) {
        state.layoutInfo.visibleItemsInfo
            .firstOrNull { item -> offset.y.toInt() in item.offset..(item.offset + item.size) }
            ?.also {
                currentIndexOfDraggedItem = it.index
                initiallyDraggedElement = it
                draggingItemInitialOffset = it.offset
            }
    }

    fun onDragInterrupted() {
        if (currentIndexOfDraggedItem != null) {
            previousIndexOfDraggedItem = currentIndexOfDraggedItem
            if(currentIndexOfHoveredItem!=null){
                onMove(currentIndexOfDraggedItem!!, currentIndexOfHoveredItem!!)
                currentIndexOfHoveredItem = null
            }
        }
        draggingItemInitialOffset = 0
        draggedDistance = 0f
        currentIndexOfDraggedItem = null
        initiallyDraggedElement = null
    }

    fun onDrag(offset: Offset) {
        draggedDistance += offset.y

        initialOffsets?.let { (topOffset, bottomOffset) ->
            val startOffset = topOffset + draggedDistance
            val endOffset = bottomOffset + draggedDistance
            currentElement?.let { draggedElement ->
                val hoveredElement = state.layoutInfo.visibleItemsInfo
                    .filterNot { item -> (item.offsetEnd-item.size/2) < startOffset || (item.offset+item.size/2) > endOffset }
                    .firstOrNull { item ->
                        val delta = (startOffset - draggedElement.offset)
                        when {
                            delta > 0 -> (endOffset > item.offset)
                            else -> (startOffset < item.offsetEnd)
                        } && item.index != draggedElement.index
                    }

                if(hoveredElement!=null) {
                    currentIndexOfHoveredItem = hoveredElement.index
                    currentIndexOfDraggedItem?.let { current ->
                        scope.launch {
                            onHover.invoke(
                                current,
                                hoveredElement.index,
                                0f
                            )
                        }
                    }
                }
            }
        }
    }

    fun checkForOverScroll(): Float {
        return initiallyDraggedElement?.let {
            val startOffset = it.offset + draggedDistance
            val endOffset = it.offsetEnd + draggedDistance
            return@let when {
                draggedDistance > 0 -> (endOffset - state.layoutInfo.viewportEndOffset+50f).takeIf { diff -> diff > 0 }
                draggedDistance < 0 -> (startOffset - state.layoutInfo.viewportStartOffset-50f).takeIf { diff -> diff < 0 }
                else -> null
            }
        } ?: 0f
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true,)
@Composable
fun EditorPreview() {
    IntervalTrainingTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            val listState = rememberLazyListState()
            val dragDropState = rememberDragDropState(listState)
            dragDropState.currentIndexOfDraggedItem = 1
            LazyColumn {
                item {
                    DraggableItem(
                        dragDropState = dragDropState,
                        index = 0
                    ) { isDragging ->
                        Text(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            text = "1 Not dragging",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 18.sp
                            ),
                        )
                    }
                }
                item {
                    DraggableItem(
                        modifier = Modifier.graphicsLayer {
                            translationY = 30.dp.toPx()
                        },
                        dragDropState = dragDropState,
                        index = 1
                    ) { isDragging ->
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .wrapContentSize(unbounded = true),
                            text = "2 Dragging",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 18.sp
                            ),
                        )
                    }
                }
                item {
                    DraggableItem(
                        dragDropState = dragDropState,
                        index = 2
                    ) { isDragging ->
                        Text(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            text = "3 Not Dragging",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 18.sp
                            ),
                        )
                    }
                }
                item {
                    DraggableItem(
                        dragDropState = dragDropState,
                        index = 3
                    ) { isDragging ->
                        Text(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            text = "4 Not Dragging",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 18.sp
                            ),
                        )
                    }
                }
            }
        }
    }
}
