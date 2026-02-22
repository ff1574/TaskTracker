package com.better.spark.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.*

// ─────────────────────────────────────────────────────────────────────────────
// UI chrome icons — all sourced from Phosphor (Regular weight)
// These are used for navigation, action buttons, and task-list controls.
// ─────────────────────────────────────────────────────────────────────────────

@Composable fun BackIcon(): ImageVector        = PhosphorIcons.Regular.ArrowLeft
@Composable fun DeleteIcon(): ImageVector      = PhosphorIcons.Regular.Trash
@Composable fun PencilIcon(): ImageVector      = PhosphorIcons.Regular.PencilSimple
@Composable fun CheckIcon(): ImageVector       = PhosphorIcons.Regular.Check
@Composable fun RepeatIcon(): ImageVector      = PhosphorIcons.Regular.RepeatOnce
@Composable fun ArchiveIcon(): ImageVector     = PhosphorIcons.Regular.Archive
@Composable fun UnarchiveIcon(): ImageVector   = PhosphorIcons.Regular.ArrowUUpLeft
@Composable fun SearchIcon(): ImageVector      = PhosphorIcons.Regular.MagnifyingGlass
@Composable fun CloseIcon(): ImageVector       = PhosphorIcons.Regular.X
