package hex.wald.spitplugin.service;

import hex.wald.spitplugin.model.SpitType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpitSelectionService {

    private final Map<UUID, SpitType> selections = new HashMap<UUID, SpitType>();

    public SpitType getSelected(UUID playerId) {
        SpitType spitType = selections.get(playerId);
        return spitType == null ? SpitType.BONE_MEAL : spitType;
    }

    public void setSelected(UUID playerId, SpitType spitType) {
        selections.put(playerId, spitType);
    }
}
