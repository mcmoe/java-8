package h2.table;

import commons.Utils;
import h2.connection.H2Utils;
import h2.sql.ScrapaDataSQL;
import lombok.Cleanup;
import model.ScrapaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Wrapper for H2 SCRAPA_DATA table SQL transactions
 * Created by mcmoe on 4/28/2014.
 */
public class H2ScrapaData {
    private static final Logger LOGGER = LoggerFactory.getLogger(H2ScrapaData.class);

    private Statement statement;
    private PreparedStatement addDataStatement;
    private PreparedStatement getDataWhereStatement;

    private final Connection connection;

    public H2ScrapaData(Connection connection) {
        this.connection = connection;
    }

    public void createScrapaDataTable() throws SQLException {
        getStatement().execute(ScrapaDataSQL.CREATE_SCRAPA_DATA_TABLE);
    }

    public ResultSet getMetaData() throws SQLException {
        return connection.getMetaData().getColumns(null, null, ScrapaDataSQL.SCRAPA_DATA_TABLE, null);
    }

    public Optional<ScrapaData> getScrapaDataWhere(String url) throws SQLException {
        PreparedStatement statement = prepareGetScrapaDataWhereStatement();
        statement.setString(ScrapaDataSQL.COLUMNS.URL.index(), url);
        ResultSet resultSet = statement.executeQuery();
        Optional<ScrapaData> scrapaData = Optional.empty();

        if(resultSet.next()) {
            Reader characterStream = resultSet.getCharacterStream(ScrapaDataSQL.COLUMNS.DATA.index());
            String data = getString(characterStream);
            scrapaData = Optional.of(new ScrapaData(url, data));
        }

        return scrapaData;
    }

    private String getString(Reader characterStream) {
        try {
            return Utils.getString(characterStream);
        } catch (IOException e) {
            LOGGER.error("failed to read xml data from SCRAP_DATA!", e);
        }

        return "";
    }

    public List<ScrapaData> getScrapaData() throws SQLException {
        @Cleanup ResultSet resultSet = getStatement().executeQuery(ScrapaDataSQL.GET_SCRAPA_DATA);
        List<ScrapaData> scrapaData = new ArrayList<>();

        while (resultSet.next()) {
            Reader characterStream = resultSet.getCharacterStream(ScrapaDataSQL.COLUMNS.DATA.index());
            String string = resultSet.getString(ScrapaDataSQL.COLUMNS.URL.index());
            scrapaData.add(new ScrapaData(string,getString(characterStream)));
        }

        return scrapaData;
    }

    public int addScrapaData(String url, String data) throws SQLException {
        PreparedStatement addScrapaDataStatement = prepareAddScrapaDataStatement();
        addScrapaDataStatement.setString(ScrapaDataSQL.COLUMNS.URL.index(), url);
        addScrapaDataStatement.setCharacterStream(ScrapaDataSQL.COLUMNS.DATA.index(), new StringReader(data));
        return addScrapaDataStatement.executeUpdate();
    }

    public int deleteScrapaData() throws SQLException {
        return getStatement().executeUpdate(ScrapaDataSQL.DELETE_SCRAPA_DATA);
    }

    private Statement getStatement() throws SQLException {
        if(statement == null) {
            statement = H2Utils.createStatement(connection);
        }
        return statement;
    }

    private PreparedStatement prepareAddScrapaDataStatement() throws SQLException {
        if(addDataStatement == null) {
            addDataStatement = connection.prepareStatement(ScrapaDataSQL.ADD_SCRAPA_URL_DATA);
        }
        return addDataStatement;
    }

    private PreparedStatement prepareGetScrapaDataWhereStatement() throws SQLException {
        if(getDataWhereStatement == null) {
            getDataWhereStatement = connection.prepareStatement(ScrapaDataSQL.GET_SCRAPA_DATA_WHERE);
        }
        return getDataWhereStatement;
    }

    public void close() {
        try {
            if(statement != null) {
                statement.close();
            }
            if(addDataStatement != null) {
                addDataStatement.close();
            }
            if(getDataWhereStatement != null) {
                getDataWhereStatement.close();
            }
        } catch (SQLException e) {
            LOGGER.error("Exception while closing statements", e);
        }
    }
}