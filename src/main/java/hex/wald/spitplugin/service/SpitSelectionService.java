package hex.wald.spitplugin.service;

import hex.wald.spitplugin.config.SpitSettings;
import hex.wald.spitplugin.model.SpitType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpitSelectionService {

    private final Map<UUID, SpitType> selections = new HashMap<>();

    public SpitType getSelected(UUID playerId, SpitSettings settings) {
        SpitType spitType = selections.get(playerId);
        return spitType != null && settings.isEnabled(spitType) ? spitType : settings.getDefaultType();
    }

    public void setSelected(UUID playerId, SpitType spitType) {
        selections.put(playerId, spitType);
    }
}
