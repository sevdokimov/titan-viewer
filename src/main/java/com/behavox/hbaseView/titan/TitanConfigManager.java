package com.behavox.hbaseView.titan;

import com.behavox.hbaseView.titan.viewModel.TitanConfig;
import com.behavox.hbaseView.titan.viewModel.VertexType;
import com.behavox.hbaseView.titan.viewModel.PropertyPredicate;
import com.behavox.hbaseView.titan.viewModel.StringFormatter;

import java.util.Arrays;

/**
 *
 */
public class TitanConfigManager {
    private static TitanConfigManager ourInstance = new TitanConfigManager();

    private TitanConfig cfg = createConfig();

    public static TitanConfigManager getInstance() {
        return ourInstance;
    }

    private static TitanConfig createConfig() {
        TitanConfig cfg = new TitanConfig();

        cfg.setPropertiesOrder(Arrays.asList("type", "db_id", "first_name", "last_name"));

        cfg.addType(new VertexType(new PropertyPredicate("type", "person"),
                new StringFormatter("vPerson", "<span title='Person' class='glyphicon glyphicon glyphicon-user' aria-hidden='true'></span> <span class='vPerson'>%s %s <span class='dbId' title='Data base ID'>(%s)</span></span>", "first_name", "last_name", "db_id")));

        cfg.addType(new VertexType(new PropertyPredicate("type", "trader"),
                new StringFormatter("vTrader", "<span title='Trader' class='glyphicon glyphicon glyphicon-user' aria-hidden='true'></span> <span class='vTrader'>%s %s <span class='dbId' title='Data base ID'>(%s)</span></span>", "first_name", "last_name", "db_id")));

        cfg.addType(new VertexType(new PropertyPredicate("type", "org"),
                new StringFormatter("vOrg", "<span title='Organization' class='glyphicon glyphicon glyphicon-oil' aria-hidden='true'></span> %s <span class='dbId' title='Data base ID'>(%s)</span>", "email_domain", "db_id")));

        cfg.addType(new VertexType(new PropertyPredicate("type", "email"),
                new StringFormatter("vEmail", "<span title='Email' class='glyphicon glyphicon glyphicon-envelope' aria-hidden='true'></span> %1.30s <span class='dbId' title='Data base ID'>(%s)</span>", "subject", "db_id")));

        return cfg;
    }

    private TitanConfigManager() {
    }

    public TitanConfig getConfig() {
        return cfg;
    }

    public void reload() {
        cfg = createConfig();
    }
}
