-- Philip Scuderi
-- Part 1, Question 5

SELECT
	COUNT(*) AS NUM_ATTRIBUTED_ACTIVITIES
FROM
	(
		SELECT
			*,
			(SELECT EventTypeID FROM Events WHERE UserID = E1.UserID AND EventTime = E1.LAST_EVENT_TIME) AS LAST_EVENT_TYPE
		FROM
			(
				SELECT
					UserID,
					ACTIVITY_TIME,
					LAST_EVENT_TIME
				FROM
					(
						SELECT
							UserID,
							EventTime AS ACTIVITY_TIME,
							(SELECT MAX(EventTime) FROM Events WHERE EventTime < E1.EventTime AND UserID = E1.UserID) AS LAST_EVENT_TIME
						FROM
							Events AS E1
						WHERE
							E1.EventTypeID = (SELECT EventTypeID FROM EventTypes WHERE EventTypeDesc = 'activity')
					) AS SUB_QUERY
				WHERE
					LAST_EVENT_TIME IS NOT NULL
			) AS E1
	) AS E2
WHERE
	LAST_EVENT_TYPE = (SELECT EventTypeID FROM EventTypes WHERE EventTypeDesc = 'impression')