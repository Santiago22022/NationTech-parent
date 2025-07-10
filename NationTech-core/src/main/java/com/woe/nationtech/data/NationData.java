package com.woe.nationtech.data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NationData {
    private final UUID nationUUID;
    private final Set<String> unlockedTechnologies;
    private boolean isDirty;

    public NationData(UUID nationUUID) {
        this.nationUUID = nationUUID;
        this.unlockedTechnologies = new HashSet<>();
        this.isDirty = false;
    }

    public UUID getNationUUID() {
        return nationUUID;
    }

    public Set<String> getUnlockedTechnologies() {
        return new HashSet<>(unlockedTechnologies);
    }

    public boolean hasTechnology(String techId) {
        return unlockedTechnologies.contains(techId);
    }

    public void unlockTechnology(String techId) {
        if (unlockedTechnologies.add(techId)) {
            setDirty(true);
        }
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }
}