package com.better.spark.presentation.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object AppIcons {
    val Home: ImageVector
        get() = ImageVector.Builder(
            name = "Home",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter,
            strokeLineMiter = 1f
        ) {
            moveTo(10.0f, 20.0f)
            lineTo(10.0f, 14.0f)
            lineTo(14.0f, 14.0f)
            lineTo(14.0f, 20.0f)
            lineTo(19.0f, 20.0f)
            lineTo(19.0f, 12.0f)
            lineTo(22.0f, 12.0f)
            lineTo(12.0f, 3.0f)
            lineTo(2.0f, 12.0f)
            lineTo(5.0f, 12.0f)
            lineTo(5.0f, 20.0f)
            close()
        }.build()

    val TaskList: ImageVector
        get() = ImageVector.Builder(
            name = "TaskList",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter,
            strokeLineMiter = 1f
        ) {
            // First rect (3,11) to (5,13)
            moveTo(3.0f, 13.0f)
            lineTo(5.0f, 13.0f)
            lineTo(5.0f, 11.0f)
            lineTo(3.0f, 11.0f)
            lineTo(3.0f, 13.0f)
            close()
            // Second rect (3,15) to (5,17)
            moveTo(3.0f, 17.0f)
            lineTo(5.0f, 17.0f)
            lineTo(5.0f, 15.0f)
            lineTo(3.0f, 15.0f)
            lineTo(3.0f, 17.0f)
            close()
            // Third rect (3,7) to (5,9)
            moveTo(3.0f, 9.0f)
            lineTo(5.0f, 9.0f)
            lineTo(5.0f, 7.0f)
            lineTo(3.0f, 7.0f)
            lineTo(3.0f, 9.0f)
            close()
            // Bar 1 (7,11) to (21,13)
            moveTo(7.0f, 13.0f)
            lineTo(21.0f, 13.0f)
            lineTo(21.0f, 11.0f)
            lineTo(7.0f, 11.0f)
            lineTo(7.0f, 13.0f)
            close()
            // Bar 2 (7,15) to (21,17)
            moveTo(7.0f, 17.0f)
            lineTo(21.0f, 17.0f)
            lineTo(21.0f, 15.0f)
            lineTo(7.0f, 15.0f)
            lineTo(7.0f, 17.0f)
            close()
            // Bar 3 (7,7) to (21,9)
            moveTo(7.0f, 9.0f)
            lineTo(21.0f, 9.0f)
            lineTo(21.0f, 7.0f)
            lineTo(7.0f, 7.0f)
            lineTo(7.0f, 9.0f)
            close()
        }.build()

    val Motivation: ImageVector
        get() = ImageVector.Builder(
            name = "Motivation",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter,
            strokeLineMiter = 1f
        ) {
            moveTo(12.0f, 17.27f)
            lineTo(18.18f, 21.0f)
            lineTo(16.54f, 13.97f)
            lineTo(22.0f, 9.24f)
            lineTo(14.81f, 8.63f)
            lineTo(12.0f, 2.0f)
            lineTo(9.19f, 8.63f)
            lineTo(2.0f, 9.24f)
            lineTo(7.46f, 13.97f)
            lineTo(5.82f, 21.0f)
            close()
        }.build()
}
