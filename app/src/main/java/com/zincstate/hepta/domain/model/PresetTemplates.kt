package com.zincstate.hepta.domain.model

import java.time.LocalDate

enum class PresetType(val displayName: String, val description: String) {
    ZEN_SCHOLAR("ZEN SCHOLAR", "Master your craft with deep work & reading."),
    IRON_MONK("IRON MONK", "Build strength with weights & discipline."),
    VITALITY("VITALITY", "Transform with cardio & weight loss goals."),
    DIGITAL_DETOX("DIGITAL DETOX", "Unplug, refocus, and reclaim your time."),
    CREATIVE_FLOW("CREATIVE FLOW", "Unleash your art with daily inspiration."),
    ROUTINE_REBOOT("ROUTINE REBOOT", "Reset your habits and optimize your day."),
    DEEP_STUDY("DEEP STUDY", "Accelerate learning with deep study blocks."),
    DEFAULT("EMPTY SLATE", "Clear all tasks and start fresh.")
}

data class PresetTaskTemplate(
    val text: String,
    val isFocusEnabled: Boolean = false
)

object PresetTemplates {
    val presets = mapOf(
        PresetType.ZEN_SCHOLAR to listOf(
            PresetTaskTemplate("2H DEEP WORK SESSION", true),
            PresetTaskTemplate("READ 20 PAGES OF NON-FICTION"),
            PresetTaskTemplate("REVIEW DAILY NOTES"),
            PresetTaskTemplate("PLAN TOMORROW'S FOCUS")
        ),
        PresetType.IRON_MONK to listOf(
            PresetTaskTemplate("INTENSE WEIGHT TRAINING", true),
            PresetTaskTemplate("STRETCHING & MOBILITY"),
            PresetTaskTemplate("HIT PROTEIN GOAL"),
            PresetTaskTemplate("HYDRATION: 3L WATER")
        ),
        PresetType.VITALITY to listOf(
            PresetTaskTemplate("30M MORNING CARDIO", true),
            PresetTaskTemplate("LOG ALL MEALS"),
            PresetTaskTemplate("REACH 10,000 STEPS"),
            PresetTaskTemplate("NO ADDED SUGAR TODAY")
        ),
        PresetType.DIGITAL_DETOX to listOf(
            PresetTaskTemplate("PHONE-FREE MORNING (1H)", true),
            PresetTaskTemplate("READ PHYSICAL BOOK (30M)"),
            PresetTaskTemplate("30M NATURE WALK"),
            PresetTaskTemplate("HANDWRITTEN JOURNALING")
        ),
        PresetType.CREATIVE_FLOW to listOf(
            PresetTaskTemplate("INSPIRATION HUNT (MOODBOARD)"),
            PresetTaskTemplate("1H FOCUSED CREATION", true),
            PresetTaskTemplate("PRACTICE CORE TECHNIQUE"),
            PresetTaskTemplate("REVIEW PROJECT PROGRESS")
        ),
        PresetType.ROUTINE_REBOOT to listOf(
            PresetTaskTemplate("MORNING RITUAL (MEDITATE)"),
            PresetTaskTemplate("COMPLETE MOST IMPORTANT TASK (MIT)", true),
            PresetTaskTemplate("CLEAN WORKSPACE"),
            PresetTaskTemplate("EVENING BRAIN DUMP")
        ),
        PresetType.DEEP_STUDY to listOf(
            PresetTaskTemplate("1.5H UNINTERRUPTED STUDY BLOCK", true),
            PresetTaskTemplate("POMODORO: 4 x 25M SESSIONS"),
            PresetTaskTemplate("SUMMARIZE CORE CONCEPTS"),
            PresetTaskTemplate("PRACTICE ACTIVE RECALL")
        )
    )
}
