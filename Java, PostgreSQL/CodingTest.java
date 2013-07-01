/**
 * CodingTest.java
 * @author Philip Scuderi
 */

import java.io.*;
import java.util.regex.*;
import java.sql.*;
import java.util.*;

public class CodingTest
{
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception
    {
        String basePath = "C:\\DataEngineerCodingTest\\";
        String sampleDataFile = "Data-Test-sample_data.txt";
        String userDataFile = "user_data.csv";

        // load the JDBC driver and get a database connection
        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://localhost/CodingTest";
        Properties properties = new Properties();
        properties.setProperty("user","postgres");
        properties.setProperty("password","password");
        Connection connection = DriverManager.getConnection(url, properties);

        CodingTest codingTestObj = new CodingTest(connection, basePath + sampleDataFile, basePath + userDataFile);

        System.out.println("Number of Ads\tNumber of Users\n-------------\t---------------");
        for(Map.Entry<Integer, Integer> entry : codingTestObj.GetNumAdsToNumUsers().entrySet())
            System.out.println(entry.getKey() + "\t\t" + entry.getValue());

        System.out.println("\nNumber of Attributed Activities = " + codingTestObj.GetNumAttributedActivities());
        System.out.println("\nNumber of Attributed Impressions = " + codingTestObj.GetNumAttributedImpressions());
    }

    public enum EventType
    {
        IMPRESSION,
        ACTIVITY;
    }

    protected String sampleDataPath, userDataPath;
    protected Connection connection;
    protected HashMap<String, HashMap<EventType, HashSet<Long>>> eventsByUserID;
    protected HashMap<String, Integer> regionByUser;
    protected HashMap<String, SortedMap<Long, HashSet<EventType>>> eventsByUserIDSortedByTime;

    public CodingTest(Connection connection, String sampleDataPath, String userDataPath) throws Exception
    {
        this.connection = connection;
        this.sampleDataPath = sampleDataPath;
        this.userDataPath = userDataPath;

        eventsByUserID = new HashMap<>();
        regionByUser = new HashMap<>();
        eventsByUserIDSortedByTime = new HashMap<>();

        // prepare the statements for insering users
        PreparedStatement insertUserAndRegion = connection.prepareStatement
            ("INSERT INTO Users(UserID,RegionID) VALUES(?,?)");
        PreparedStatement insertUserNoRegion = connection.prepareStatement
            ("INSERT INTO Users(UserID) VALUES(?)");

        // prepart the statement for inserting events
        PreparedStatement insertEvent = connection.prepareStatement
            (
                "INSERT INTO Events(UserID,EventTypeID,EventTime) VALUES" +
                "(" +
                    "?, " +
                    "(SELECT EventTypeID FROM EventTypes WHERE EventTypeDesc = ?), " +
                    "to_timestamp(?)" +
                ")"
            );

        String line;
        BufferedReader reader = new BufferedReader(new FileReader(userDataPath));
        while ( (line = reader.readLine()) != null)
        {
            String userId = null;
            try
            {
                userId = GetUserId(line);
            }
            catch (IllegalArgumentException e)
            { }

            Integer region = null;
            try
            {
                region = GetRegion(line);
            }
            catch (IllegalArgumentException e)
            { }

            if (userId != null)
            {
                AddToHash(userId, region);
                AddToDatabase(insertUserAndRegion, insertUserNoRegion, userId, region);
            }
        }
        reader.close();


        // read and parse each line containing data
        reader = new BufferedReader(new FileReader(sampleDataPath));
        while ( (line = reader.readLine()) != null)
        {
            String userId = null;
            try
            {
                userId = GetUserId(line);
            }
            catch (IllegalArgumentException e)
            { }

            EventType eventType =  null;
            try
            {
                eventType = GetEventType(line);
            }
            catch (IllegalArgumentException e)
            { }

            Long unixTime = null;
            try
            {
                unixTime = GetUnixTime(line);
            }
            catch (IllegalArgumentException e)
            { }

            if (userId != null && eventType != null && unixTime != null)
            {
                // process the data if we have valid data
                // we're assuming that if any of the variables in a line
                // are invalid, then the data in that line is invalid
                boolean h1 = AddToHash1(userId, eventType, unixTime);
                boolean h2 = AddToHash2(userId, eventType, unixTime);
                boolean d = AddToDatabase(insertEvent, userId, eventType, unixTime);

                if (!((h1 && h2 && d) || (!h1 && !h2 && !d)))
                    throw new Exception("Unknown error with line = " + line);
            }
        }
        reader.close();

        insertUserAndRegion.close();
        insertUserNoRegion.close();
        insertEvent.close();
    }

    public int GetNumAttributedActivities() throws Exception
    {
        // I understand an attributed activity to be an activity that
        // directly follows an impression (not another activity or nothing)
        // for a particular user.

        int numAttributedActivities = 0;

        // loop over all the users
        for( Map.Entry<String, SortedMap<Long, HashSet<EventType>>> userEntry : eventsByUserIDSortedByTime.entrySet())
        {
            SortedMap<Long, HashSet<EventType>> eventsForUserI = userEntry.getValue();

            int idx = 0;
            int idxLastImpression = Integer.MIN_VALUE;

            for( Map.Entry<Long, HashSet<EventType>> entry : eventsForUserI.entrySet())
            {
                HashSet<EventType> events = entry.getValue();

                if (events.size() > 1)
                    throw new Exception("Multiple Events occured at the same time, which is not currently supported.");

                for (EventType event : entry.getValue())
                {
                    ++idx;

                    switch (event)
                    {
                        case IMPRESSION:
                            // keep track of this impression's index
                            idxLastImpression = idx;
                            break;

                        case ACTIVITY:
                            // check if the current index is 1 more than
                            // the index of the last impression, and if so
                            // we have an attributed activity
                            if (idx == idxLastImpression + 1)
                                ++numAttributedActivities;
                            break;
                    }
                }
            }
        }

        return numAttributedActivities;
    }

    public int GetNumAttributedImpressions() throws Exception
    {
        int numAttributedImpressions = 0;

        // loop over all the users
        for( Map.Entry<String, SortedMap<Long, HashSet<EventType>>> userEntry : eventsByUserIDSortedByTime.entrySet())
        {
            SortedMap<Long, HashSet<EventType>> eventsForUserI = userEntry.getValue();

            int numConsecutiveImpressions = 0;

            for(Map.Entry<Long, HashSet<EventType>> entry : eventsForUserI.entrySet())
            {
                HashSet<EventType> events = entry.getValue();

                if (events.size() > 1)
                    throw new Exception("Multiple Events occured at the same time, which is not currently supported.");

                for (EventType event : entry.getValue())
                {
                    switch (event)
                    {
                        case IMPRESSION:
                            ++numConsecutiveImpressions;
                            break;

                        case ACTIVITY:
                            numAttributedImpressions += numConsecutiveImpressions;
                            numConsecutiveImpressions = 0;
                            break;
                    }
                }

            }
        }

        return numAttributedImpressions;
    }

    public SortedMap<Integer, Integer> GetNumAdsToNumUsers()
    {
        TreeMap<Integer, Integer> numAdsToNumUsers = new TreeMap<>();

        for( Map.Entry<String, HashMap<EventType, HashSet<Long>>> entry : eventsByUserID.entrySet())
        {
            HashMap<EventType, HashSet<Long>> eventsForUserI = entry.getValue();

            int numAdsForUserI = 0;

            if (eventsForUserI.containsKey(EventType.IMPRESSION))
                numAdsForUserI = eventsForUserI.get(EventType.IMPRESSION).size();
            else
                // we don't count this record because the user viewed zero
                // impressions, to count users who viewed zero impressions
                // we simply remove the else/continue clause
                continue;

            if (numAdsToNumUsers.containsKey(numAdsForUserI))
                numAdsToNumUsers.put(numAdsForUserI, numAdsToNumUsers.get(numAdsForUserI) + 1);
            else
                numAdsToNumUsers.put(numAdsForUserI, 1);
        }

        return numAdsToNumUsers;
    }

    private void AddToHash(String userId, Integer region)
    {
        regionByUser.put(userId, region);
    }

    private void AddToDatabase(PreparedStatement insertUserAndRegion, PreparedStatement insertUserNoRegion, String userId, Integer region) throws SQLException
    {
        if (region != null)
        {
            insertUserAndRegion.setString(1, userId);
            insertUserAndRegion.setInt(2, region);
            insertUserAndRegion.executeUpdate();
        }
        else
        {
            insertUserNoRegion.setString(1, userId);
            insertUserNoRegion.executeUpdate();
        }
    }

    private boolean AddToHash1(String userId, EventType eventType, Long unixTime)
    {
        if (eventsByUserID.containsKey(userId))
        {
            HashMap<EventType, HashSet<Long>> userEventsByEventType = eventsByUserID.get(userId);

            if (userEventsByEventType.containsKey(eventType))
            {
                // finally add the unix time to the HashSet that stores times for
                // this particular user and this particular event
                return userEventsByEventType.get(eventType).add(unixTime);
            }
            else
            {
                HashSet<Long> unixTimes = new HashSet<>();
                unixTimes.add(unixTime);
                userEventsByEventType.put(eventType, unixTimes);
            }
        }
        else
        {
            HashMap<EventType, HashSet<Long>> userEventsByEventType = new HashMap();
            HashSet<Long> unixTimes = new HashSet<>();
            unixTimes.add(unixTime);
            userEventsByEventType.put(eventType, unixTimes);
            eventsByUserID.put(userId, userEventsByEventType);
        }

        return true;
    }

    private boolean AddToHash2(String userId, EventType eventType, Long unixTime)
    {
        if (eventsByUserIDSortedByTime.containsKey(userId))
        {
            SortedMap<Long, HashSet<EventType>> userEventsByTime = eventsByUserIDSortedByTime.get(userId);

            if (userEventsByTime.containsKey(unixTime))
            {
                HashSet<EventType> events = userEventsByTime.get(unixTime);

                if (events.contains(eventType))
                    return false;
                else
                {
                    events.add(eventType);
                    return true;
                }
            }
            else
            {
                HashSet<EventType> events = new HashSet<>();
                events.add(eventType);

                userEventsByTime.put(unixTime, events);

                return true;
            }
        }
        else
        {
            SortedMap<Long, HashSet<EventType>> userEventsByTime = new TreeMap<>();

            HashSet<EventType> events = new HashSet<>();
            events.add(eventType);

            userEventsByTime.put(unixTime, events);
            eventsByUserIDSortedByTime.put(userId, userEventsByTime);
            return true;
        }
    }

    private boolean AddToDatabase(PreparedStatement insertEvent, String userId, EventType eventType, Long unixTime) throws SQLException
    {
        insertEvent.setString(1, userId);

        switch (eventType)
        {
            case IMPRESSION:
                insertEvent.setString(2, "impression");
                break;

            case ACTIVITY:
                insertEvent.setString(2, "activity");
                break;
        }

        insertEvent.setLong(3, unixTime);

        try
        {
            insertEvent.executeUpdate();
        }
        catch (org.postgresql.util.PSQLException e)
        {
            // the insert violated our Unique constraint, which is OK
            // but we return false to indicate that this record
            // is a duplicate and was not added
            return false;
        }

        // record added successfully
        return true;
    }

    protected static String GetUserId(String line)
    {
        // user ID is a string of lengh 25 with word characters and dashes
        Pattern pattern = Pattern.compile("[\\w\\-]{25}");

        Matcher matcher = pattern.matcher(line);

        if (matcher.find())
            return matcher.group();

        // if we make it this far then we couldn't find a properly formatted User ID
        throw new IllegalArgumentException("line argument does not contain a properly formatted User ID");
    }

    protected static EventType GetEventType(String line)
    {
        // look for the string "impression" or "activity" in the line
        // make the search case insensitive

        String lCaseLine = line.toLowerCase();

        if (lCaseLine.contains("impression"))
            return EventType.IMPRESSION;

        if (lCaseLine.contains("activity"))
            return EventType.ACTIVITY;

        // if we make it this far then we couldn't find a properly formatted Event Type
        throw new IllegalArgumentException("line argument does not contain a properly formatted Event Type");
    }

    protected static Long GetUnixTime(String line)
    {
        // I'm assuming that unix time is a 13 digit long integer
        // followed by the newline character
        // obviously we could use other patterns to find the unix time
        Pattern pattern = Pattern.compile("[\\d]{13}$");

        Matcher matcher = pattern.matcher(line);

        if (matcher.find())
        {
            // divide by 1000 to convert to a format recognized by postgresql
            // I checked the data for any entries that are not divisible by 1000
            // and they don't exist.  We could obviously do this division
            // elsewhere or perform further error checking.
            return Long.parseLong(matcher.group()) / 1000;
        }

        // if we make it this far then we couldn't find a properly formatted Unix Time
        throw new IllegalArgumentException("line argument does not contain a properly formatted Unix Time");
    }

    protected static Integer GetRegion(String line)
    {
        // region is a 4 digit integer followed by the newline character
        Pattern pattern = Pattern.compile("[\\d]{4}$");

        Matcher matcher = pattern.matcher(line);

        if (matcher.find())
            return Integer.parseInt(matcher.group());

        // if we make it this far then we couldn't find a properly formatted Unix Time
        throw new IllegalArgumentException("line argument does not contain a properly formatted Region");
    }
}
