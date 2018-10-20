DROP TABLE IF EXISTS `game_log`;
CREATE TABLE `game_log` (
  `id`                        INT      AUTO_INCREMENT,
  `date`                      DATE     DEFAULT NULL,
  `title`                     CHAR(1)  DEFAULT NULL,
  `day_of_week`               CHAR(3)  DEFAULT NULL,
  `visiting_team`             CHAR(3)  DEFAULT NULL,
  `visiting_team_league`      CHAR(2)  DEFAULT NULL,
  `visiting_team_game_number` SMALLINT DEFAULT NULL,
  `home_team`                 CHAR(3)  DEFAULT NULL,
  `home_team_league`          CHAR(2)  DEFAULT NULL,
  `home_team_game_number`     SMALLINT DEFAULT NULL,
  `visiting_team_score`       SMALLINT DEFAULT NULL,
  `home_team_score`           SMALLINT DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX (`date`)
);