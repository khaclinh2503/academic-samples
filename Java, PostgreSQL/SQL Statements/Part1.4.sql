-- Philip Scuderi
-- Part 1, Question 4

SELECT
	NUM_IMPRESSIONS AS Impressions,
	SUM(NE) AS Northeast,
	SUM(NW) AS Northwest,
	SUM(SE) AS Southeast,
	SUM(SW) AS Southwest,
	SUM(NOWHERE) AS Middle_of_Nowhere
INTO
	Impressions_By_Region
FROM
	(
		SELECT
			NUM_IMPRESSIONS,
			CASE WHEN RegionID=1000 THEN NUM_USERS ELSE 0 END AS NE,
			CASE WHEN RegionID=2000 THEN NUM_USERS ELSE 0 END AS NW,
			CASE WHEN RegionID=3000 THEN NUM_USERS ELSE 0 END AS SE,
			CASE WHEN RegionID=4000 THEN NUM_USERS ELSE 0 END AS SW,
			CASE WHEN RegionID=5000 THEN NUM_USERS ELSE 0 END AS NOWHERE
		FROM
			(
				SELECT
					NUM_IMPRESSIONS,
					RegionID,
					Count(UserID) AS NUM_USERS
				FROM
					(
						SELECT
							Events.UserID,
							RegionID,
							COUNT(*) AS NUM_IMPRESSIONS
						FROM
							Events INNER JOIN Users ON Events.UserID = Users.UserID
						WHERE
							EventTypeID = (SELECT EventTypeID FROM EventTypes WHERE EventTypeDesc = 'impression')
							AND
							RegionID IS NOT NULL
						GROUP BY
							Events.UserID, RegionID
					) AS SUBQUERY_1
				GROUP BY
					RegionID,
					NUM_IMPRESSIONS
			) AS SUBQUERY_2
	) AS SUBQUERY_3
GROUP BY
	Impressions
ORDER BY
	Impressions