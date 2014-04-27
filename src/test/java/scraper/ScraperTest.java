package scraper;

import lombok.Cleanup;
import model.TeamGoals;
import model.TopScorer;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.xpath.XPathExpressionException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static scraper.Scraper.normalizeXml;


/**
 * Attempt to load web page and scrape for data
 * Created by mcmoe on 4/19/2014.
 */
public class ScraperTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScraperTest.class);
    private static int topScorerVisits = 0;
    private static int teamGoalsVisits = 0;

    @After
    public void checkVisitsAndReset() {
        assertTrue("There should exist at least 20 top scorers!", topScorerVisits >= 20);
        assertEquals("Expecting 20 teams exactly", 20, teamGoalsVisits);
        topScorerVisits = 0;
        teamGoalsVisits = 0;
    }

    @Test @Ignore /* ignored to avoid spamming the web server! */
    public void test_get_web_page_and_scrape() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        String tableXml = Scraper.scrapeWeb();
        LOGGER.info(tableXml);
        Scraper.parseAndVisit(tableXml, new TopScorersVisitorTest(), new TeamGoalsVisitorTest());
    }

    @Test
    public void test_get_mock_2012_page_and_scrape() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        String tableXml = scrapeMock("scraped2012Table");
        LOGGER.info(tableXml);
        Scraper.parseAndVisit(tableXml, new TopScorersVisitorTest(), new TeamGoalsVisitorTest());
    }

    @Test
    public void test_get_mock_2013_page_and_scrape() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        String tableXml = scrapeMock("scraped2013Table");
        LOGGER.info(tableXml);
        Scraper.parseAndVisit(tableXml, new TopScorersVisitorTest(), new TeamGoalsVisitorTest());
    }

    @Test(expected = NullPointerException.class)
    public void s() {
        topScorerVisits = 20;
        teamGoalsVisits = 20;
        Scraper.logRows(null);
    }

    private String scrapeMock(String scrapedTable) throws IOException {
        @Cleanup InputStream inStream = getClass().getResourceAsStream(scrapedTable);
        @Cleanup BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
        return normalizeXml(reader.lines().collect(joining("")));
    }

    private class TopScorersVisitorTest implements TopScorersVisitor {
        /* using list to preserve order */
        private List<TopScorer> all = new ArrayList<>();

        @Override
        public void onRow(TopScorer topScorer) {
            all.add(topScorer);
        }

        @Override
        public void onExit() {
            topScorerVisits = all.size();
            for(TopScorer t : all) {
                LOGGER.info(t.toString());
            }
            // or addTopScorersRow(playerName, playerTeam, playerGoals) :)
        }
    }

    private class TeamGoalsVisitorTest implements TeamGoalsVisitor {
        /* using list to preserve order */
        private List<TeamGoals> all = new ArrayList<>();

        @Override
        public void onRow(TeamGoals teamGoals) {
            all.add(teamGoals);
        }

        @Override
        public void onExit() {
            teamGoalsVisits = all.size();
            for(TeamGoals t : all) {
                LOGGER.info(t.toString());
            }
            // or addTeamGoalsRow (teamName, teamGoals) :)
        }
    }
}