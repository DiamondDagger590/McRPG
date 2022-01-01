package us.eunoians.mcrpg.database.tables.skills;

import us.eunoians.mcrpg.database.tables.SkillDAOWrapper;
import us.eunoians.mcrpg.players.McRPGPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.CompletableFuture;

public class ArcheryDAO{

    private static final int CURRENT_TABLE_VERSION = 1;

    public static void createTable(Connection connection){

    }

    public static void validateTable(Connection connection){

    }

    public static void updateTable(Connection connection){
        //TODO add new columns
    }

    public static SkillDAOWrapper getPlayerArcheryData(Connection connection){
        return null;
    }

    public static void savePlayerArcheryData(Connection connection, McRPGPlayer mcRPGPlayer){

    }
}
