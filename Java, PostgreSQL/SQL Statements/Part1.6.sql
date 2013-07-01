-- Philip Scuderi
-- Part 1, Question 6

SELECT
	COUNT(*)
FROM
	(
		SELECT
			CASE WHEN EXISTS (SELECT EventTime FROM Events WHERE EventTime > E1.EventTime AND UserID = E1.UserID AND EventTypeID = (SELECT EventTypeID FROM EventTypes WHERE EventTypeDesc = 'activity')) THEN 1 ELSE 0 END AS IS_ATTRIBUTED
		FROM
			Events AS E1
		WHERE
			E1.EventTypeID = (SELECT EventTypeID FROM EventTypes WHERE EventTypeDesc = 'impression')
	) AS SUB_QUERY
WHERE
	IS_ATTRIBUTED = 1