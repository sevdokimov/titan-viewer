package com.behavox.titanView;

import com.behavox.titanView.viewModel.Config;
import com.behavox.titanView.viewModel.NodeType;
import com.behavox.titanView.viewModel.PropertyPredicate;
import com.behavox.titanView.viewModel.StringFormatter;

import java.util.Arrays;

/**
 *
 */
public class ConfigManager {
    private static ConfigManager ourInstance = new ConfigManager();

    private Config cfg = createConfig();

    public static ConfigManager getInstance() {
        return ourInstance;
    }

    private static Config createConfig() {
        Config cfg = new Config();

        cfg.setPropertiesOrder(Arrays.asList("type", "db_id", "first_name", "last_name"));

        cfg.addNodeType(new NodeType(new PropertyPredicate("type", "person"),
                new StringFormatter("vPerson", "<span title='Person' class='glyphicon glyphicon glyphicon-user' aria-hidden='true'></span> <span class='vPerson'>%s %s <span class='dbId' title='Data base ID'>(%s)</span></span>", "first_name", "last_name", "db_id")));

        cfg.addNodeType(new NodeType(new PropertyPredicate("type", "trader"),
                new StringFormatter("vTrader", "<span title='Trader' class='glyphicon glyphicon glyphicon-user' aria-hidden='true'></span> <span class='vTrader'>%s %s <span class='dbId' title='Data base ID'>(%s)</span></span>", "first_name", "last_name", "db_id")));

        cfg.addNodeType(new NodeType(new PropertyPredicate("type", "org"),
                new StringFormatter("vOrg", "<span title='Organization' class='glyphicon glyphicon glyphicon-oil' aria-hidden='true'></span> %s <span class='dbId' title='Data base ID'>(%s)</span>", "email_domain", "db_id")));

        cfg.addNodeType(new NodeType(new PropertyPredicate("type", "email"),
                new StringFormatter("vEmail", "<span title='Email' class='glyphicon glyphicon glyphicon-envelope' aria-hidden='true'></span> %1.30s <span class='dbId' title='Data base ID'>(%s)</span>", "subject", "db_id")));

        return cfg;
    }

    private ConfigManager() {
    }

    public Config getConfig() {
        return cfg;
    }

    public void reload() {
        cfg = createConfig();
    }
}
