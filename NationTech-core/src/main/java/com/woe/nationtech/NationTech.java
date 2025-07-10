package com.woe.nationtech.api;

import com.palmergames.bukkit.towny.object.Nation;
import com.woe.nationtech.data.NationDataManager;
import com.woe.nationtech.data.Technology;
import com.woe.nationtech.data.TechnologyManager;
import com.woe.nationtech.ui.AdvancementUIManager;

import java.util.Set;

public class NationTechAPI {

    private static TechnologyManager technologyManager;
    private static NationDataManager nationDataManager;
    private static AdvancementUIManager advancementUIManager;

    public static void setManagers(TechnologyManager techManager, NationDataManager dataManager, AdvancementUIManager uiManager) {
        technologyManager = techManager;
        nationDataManager = dataManager;
        advancementUIManager = uiManager;
    }

    public static boolean hasTechnology(Nation nation, String techId) {
        if (nation == null |

                | techId == null) {
            return false;
        }
        return nationDataManager.getNationData(nation.getUUID()).hasTechnology(techId);
    }

    public static Set<String> getUnlockedTechnologies(Nation nation) {
        if (nation == null) {
            return Set.of();
        }
        return nationDataManager.getNationData(nation.getUUID()).getUnlockedTechnologies();
    }

    public static Technology getTechnology(String techId) {
        return technologyManager.getTechnology(techId);
    }

    public static Set<Technology> getAllTechnologies() {
        return technologyManager.getTechnologies();
    }
}