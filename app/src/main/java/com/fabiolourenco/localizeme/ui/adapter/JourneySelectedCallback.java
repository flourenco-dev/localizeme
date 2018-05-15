package com.fabiolourenco.localizeme.ui.adapter;

import com.fabiolourenco.localizeme.database.entity.Journey;

import org.jetbrains.annotations.Nullable;

/**
 * Callback for JourneyAdapter to notify its parent when a Journey is selected, while making the
 * code self-contained
 */
public interface JourneySelectedCallback {

    void onSelected(@Nullable Journey journey);
}
