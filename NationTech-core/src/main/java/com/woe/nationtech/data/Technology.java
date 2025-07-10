package com.woe.nationtech.data;

import org.bukkit.Material;
import java.util.List;

public record Technology(
        String id,
        String displayName,
        Material icon,
        List<String> description,
        List<String> dependencies,
        int cost,
        int x,
        int y,
        List<String> recipeKeys
) {}