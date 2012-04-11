package com.creadri.lazyroad;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author VeraLapsa
 */
public class MinerData implements ConfigurationSerializable {

    private int[] checkIds;
    private Map<Integer, ItemStack> drops = new HashMap<Integer, ItemStack>();
    private boolean enabled = false;

    public MinerData(int[] checkIds, Map<Integer, ItemStack> drops, boolean enabled) {
        this.checkIds = checkIds;
        this.drops = drops;
        this.enabled = enabled;
    }

    public MinerData(int[] checkIds) {
        this.checkIds = checkIds;
    }

    public int[] getCheckIds() {
        return checkIds;
    }

    public void setCheckIds(int[] checkIds) {
        this.checkIds = checkIds;
    }

    public Map<Integer, ItemStack> getDrops() {
        return drops;
    }

    public void setDrops(Map<Integer, ItemStack> drops) {
        this.drops = drops;
    }

    public int size() {
        return drops.size();
    }

    public ItemStack put(Integer key, ItemStack value) {
        return drops.put(key, value);
    }

    public ItemStack get(Object key) {
        return drops.get(key);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void addACheckID(int id) {
        int[] newIds = new int[checkIds.length + 1];
        System.arraycopy(checkIds, 0, newIds, 0, checkIds.length);
        newIds[checkIds.length] = id;
        checkIds = newIds;
    }

    public boolean removeACheckId(int id) {
        int index = -1;
        for (int i = 0; i < checkIds.length; i++) {
            if (id == checkIds[i]) {
                index = i;
                break;
            }
        }
        int[] newIds = new int[checkIds.length - 1];
        try {
            System.arraycopy(checkIds, 0, newIds, 0, index);
            if (index != checkIds.length - 1) {
                System.arraycopy(checkIds, index + 1, newIds, index, checkIds.length - index - 1);
            }

            checkIds = newIds;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    String checkIdsToString() {
        String ids = " ";
        for (int i : checkIds) {
            ids += i + " ";
        }
        return ids;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        String ids = "[";
        for (int i : checkIds) {
            ids = ids.concat(i + ",");
        }
        ids = ids.replaceAll("(,)$", "]");
        if (checkIds.length > 0) {
            result.put("checkids", ids);
        }

        result.put("enabled", enabled);

        if (drops.size() > 0) {
            Map<Integer, Map<String, Object>> serdrops = new HashMap<Integer, Map<String, Object>>();
            for (int i = 0; i < drops.size(); i++) {
                serdrops.put(i, drops.get(i).serialize());
            }
            result.put("drops", serdrops);
        }
        return result;
    }

    public static MinerData deserialize(Map<String, Object> args) {
        boolean enabled = ((Boolean) args.get("enabled"));
        int[] checkids = null;
        if (args.containsKey("checkids")) {
            String ids = ((String) args.get("checkids"));
            ids = ids.replace('[', ' ').replace(']', ' ');
            String[] tmp = ids.split(",");
            checkids = new int[tmp.length];
            for (int i = 0; i < tmp.length; i++) {
                tmp[i] = tmp[i].trim();
                try {
                    checkids[i] = Integer.parseInt(tmp[i]);
                } catch (NumberFormatException numberFormatException) {
                    Logger.getLogger("Minecraft").warning("Error Parsing " + tmp[i] + " as a number.");
                }
            }
        } else {
            checkids = new int[1];
            checkids[1] = -1;
        }
        Map<Integer, ItemStack> drops = new HashMap<Integer, ItemStack>();
        if (args.containsKey("drops")) {
            Object raw = args.get("drops");
            if (raw instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) raw;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    Map<String, Object> d = (HashMap<String, Object>) entry.getValue();
                    ItemStack drop = ItemStack.deserialize(d);

                    if (drop != null) {
                        int key = (Integer) entry.getKey();
                        drops.put(key, drop);
                    }
                }
            }
        }
        MinerData result = new MinerData(checkids, drops, enabled);

        return result;
    }
}
