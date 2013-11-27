package pt.ist.fenixframework.data;

import pt.ist.fenixframework.core.Project;

import com.google.gson.JsonObject;

public class ModuleInstallation {

    private final String name;
    private final String version;

    public ModuleInstallation(String name, String version) {
        this.name = name;
        this.version = version;
    }

    ModuleInstallation(JsonObject json) {
        this.name = json.get("name").getAsString();
        this.version = json.get("version").getAsString();
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public boolean isVersionUnknown() {
        return version.equals(Project.VERSION_UNKNOWN);
    }

    JsonObject json() {
        JsonObject moduleJson = new JsonObject();
        moduleJson.addProperty("name", name);
        moduleJson.addProperty("version", version);
        return moduleJson;
    }

    @Override
    public String toString() {
        return name + ":" + version;
    }

}
