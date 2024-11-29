package com.medina.intervaltraining.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes

@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FUNCTION
)
@Preview(
    name = "Phone - Dark",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    name = "Phone - Light",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Phone - Landscape - Dark",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    name = "Phone - Landscape - Light",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Unfolded Foldable - Dark",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    name = "Unfolded Foldable - Light",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Tablet - Dark",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    name = "Tablet - Light",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@PreviewLightDark
@PreviewFontScale
@PreviewScreenSizes
annotation class PreviewLightAndDarkOnlyMobileDevicesScreenSizes


@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FUNCTION
)
@Preview(name = "Light Mode", group = "Vertical")
@Preview(name = "Dark Mode", group = "Vertical", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "Light Mode", group = "Horizontal", widthDp = 720, heightDp = 260, uiMode = Configuration.ORIENTATION_LANDSCAPE)
@Preview(name = "Dark Mode", group = "Horizontal", widthDp = 720, heightDp = 260, uiMode = Configuration.ORIENTATION_LANDSCAPE or Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Light Mode", group = "Tablet", widthDp = 720, heightDp = 460, uiMode = Configuration.ORIENTATION_LANDSCAPE)
@Preview(name = "Dark Mode", group = "Tablet", widthDp = 720, heightDp = 460, uiMode = Configuration.ORIENTATION_LANDSCAPE or Configuration.UI_MODE_NIGHT_YES)
annotation class PreviewLightAndDarkScreenSizes
