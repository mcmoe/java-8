package parser.league;

import model.LeagueStanding;
import model.TeamGoals;
import model.TopScorer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import scraper.engine.league.LeagueScraperMocker;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit test the parsing of the scraped free-elements data using mocks
 * The mocks were built from output returned by launching the scraping manually
 * Created by mcmoe on 4/27/2014.
 */
public class LeagueParserTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeagueParserTest.class);
    @Test
    public void test_parse_and_visit_mock_2013() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        LeagueParser leagueParser = getLeagueParser("England/S2013.html", "scraped2013Table");
        leagueParser.visitTopScorers(new TopScorerVisitorTest(21));
        leagueParser.visitTeamGoals(new TeamGoalsVisitorTest(20));
        leagueParser.visitLeagueStandings(new LeagueStandingVisitorTest(20));
    }

    @Test
    public void test_parse_and_visit_mock_2012() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        LeagueParser leagueParser = getLeagueParser("England/Seasons/S2012.html", "scraped2012Table");
        leagueParser.visitTopScorers(new TopScorerVisitorTest(20));
        leagueParser.visitTeamGoals(new TeamGoalsVisitorTest(20));
        leagueParser.visitLeagueStandings(new LeagueStandingVisitorTest(20));
    }

    @Test
    public void test_parse_and_visit_mock_1986() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        LeagueParser leagueParser = getLeagueParser("England/Seasons/S1986.html", "scraped1986Table");
        leagueParser.visitTopScorers(new TopScorerVisitorTest(20));
        leagueParser.visitTeamGoals(new TeamGoalsVisitorTest(22));
        leagueParser.visitLeagueStandings(new LeagueStandingVisitorTest(22));
    }

    @Test(expected = NullPointerException.class)
    public void s() {
        LeagueParser.logRows(null);
    }

    private LeagueParser getLeagueParser(String season, String scrapedTable) throws IOException {
        return new LeagueParser(LeagueScraperMocker.getMockedData(season, scrapedTable));
    }

    private class TopScorerVisitorTest implements TopScorerVisitor {
        private final int expectedVisits;
        /* using list to preserve order */
        private List<TopScorer> all = new ArrayList<>();

        public TopScorerVisitorTest(int expectedVisits) {
            this.expectedVisits = expectedVisits;
        }

        @Override
        public void onRow(TopScorer topScorer) {
            all.add(topScorer);
        }

        @Override
        public void onExit() {
            int topScorerVisits = all.size();
            assertEquals("Expecting " + expectedVisits + " top scorers!", expectedVisits, topScorerVisits);

            for(TopScorer t : all) {
                LOGGER.info(t.toString());
            }
            // or addTopScorersRow(playerName, playerTeam, playerGoals) :)
        }
    }

    private class TeamGoalsVisitorTest implements TeamGoalsVisitor {
        private final int expectedVisits;
        /* using list to preserve order */
        private List<TeamGoals> all = new ArrayList<>();

        public TeamGoalsVisitorTest(int expectedVisits) {
            this.expectedVisits = expectedVisits;
        }

        @Override
        public void onRow(TeamGoals teamGoals) {
            all.add(teamGoals);
        }

        @Override
        public void onExit() {
            int teamGoalsVisits = all.size();
            assertEquals("Expecting " + expectedVisits + " teams!", expectedVisits, teamGoalsVisits);
            for(TeamGoals t : all) {
                LOGGER.info(t.toString());
            }
            // or addTeamGoalsRow (teamName, teamGoals) :)
        }
    }
    private class LeagueStandingVisitorTest implements LeagueStandingVisitor {
        private final int expectedVisits;
        /* using list to preserve order */
        private List<LeagueStanding> all = new ArrayList<>();

        public LeagueStandingVisitorTest(int expectedVisits) {
            this.expectedVisits = expectedVisits;
        }

        @Override
        public void onRow(LeagueStanding leagueStanding) {
            all.add(leagueStanding);
        }

        @Override
        public void onExit() {
            int leagueStandingVisits = all.size();
            assertEquals("Expecting " + expectedVisits + " standings!", expectedVisits, leagueStandingVisits);
            for(LeagueStanding t : all) {
                LOGGER.info(t.toString());
            }
            // or addLeagueStandingRow (teamName, standing) :)
        }
    }
}
