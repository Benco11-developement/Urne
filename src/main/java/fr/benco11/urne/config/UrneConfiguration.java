package fr.benco11.urne.config;

import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class UrneConfiguration {
    public static final YamlConfiguration CONFIG_TEMPLATE = YamlConfiguration.loadConfiguration(Objects.requireNonNull(UrneConfiguration.class.getResourceAsStream("/config.yml")));

    private final YamlConfiguration config;
    private final String token;

    private UrneConfiguration(YamlConfiguration config) {
        this.config = config;
        this.token = config.getString("token");
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public String getToken() {
        return token;
    }


    public static UrneConfiguration loadConfiguration() {
        File configFile = new File(System.getProperty("user.dir"), "config.yml");
        if(configFile.exists()) {
            YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(configFile);
            if(isValidConfiguration(yamlConfig)) {
                return new UrneConfiguration(yamlConfig);
            } else {
                try {
                    updateInvalidConfiguration(yamlConfig, configFile);
                } catch(IOException e) {
                    System.err.println("Une erreur s'est produite : ");
                    e.printStackTrace();
                }
                System.err.println("Le fichier de configuration étant incomplet, le programme ne pourra pas s'exécuter !\n " +
                        "Les champs manquant ont été rajoutés, veuillez vérifier le fichier de configuration avant de relancer le programme !");
                System.exit(0);
            }
        } else {
            try {
                configFile.createNewFile();
                updateInvalidConfiguration(YamlConfiguration.loadConfiguration(configFile), configFile);
                System.err.println("Le fichier de configuration n'a pas été trouvé");
            } catch(IOException e) {
                System.err.println("Une erreur s'est produite : ");
                e.printStackTrace();
            }
            System.exit(0);
        }
        return null;
    }

    public static void updateInvalidConfiguration(YamlConfiguration config, File f) throws IOException {
        getInvalidEntries(config).forEach(a -> config.set(a.getKey(), CONFIG_TEMPLATE.get(a.getKey())));
        config.save(f);
    }

    public static Set<Map.Entry<String, Object>> getInvalidEntries(YamlConfiguration config) {
        return CONFIG_TEMPLATE.getValues(true).entrySet().stream()
                .filter(a -> !(config.get(a.getKey()) != null && a.getValue().getClass().isInstance(config.get(a.getKey())))).collect(Collectors.toSet());
    }

    public static boolean isValidConfiguration(YamlConfiguration config) {
        return config.getValues(true).size() - getInvalidEntries(config).size() >= CONFIG_TEMPLATE.getValues(true).size();
    }
}
