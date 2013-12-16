package pt.ist.fenixframework.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import pt.ist.fenixframework.core.Project;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ModuleData implements Serializable {

    private static final long serialVersionUID = 3212877304522921585L;

    private final Set<ModuleInstallation> modules;

    public ModuleData(Iterable<Project> projects) {
        Set<ModuleInstallation> modules = new HashSet<>();
        for (Project project : projects) {
            modules.add(new ModuleInstallation(project.getName(), project.getVersion()));
        }
        this.modules = Collections.unmodifiableSet(modules);
    }

    public ModuleData(JsonElement json) {
        Set<ModuleInstallation> modules = new HashSet<>();
        for (JsonElement element : json.getAsJsonObject().get("modules").getAsJsonArray()) {
            modules.add(new ModuleInstallation(element.getAsJsonObject()));
        }
        this.modules = Collections.unmodifiableSet(modules);
    }

    public JsonElement json() {
        JsonObject obj = new JsonObject();
        JsonArray modules = new JsonArray();
        for (ModuleInstallation module : this.modules) {
            modules.add(module.json());
        }
        obj.add("modules", modules);
        return obj;
    }

    public Collection<ModuleInstallation> getInstalledModules() {
        return this.modules;
    }
}
